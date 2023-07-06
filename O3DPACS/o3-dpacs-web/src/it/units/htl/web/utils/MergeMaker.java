/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.utils;

import it.units.htl.atna.AuditLogService;
import it.units.htl.maps.PatientDemographics;
import it.units.htl.maps.Patients;
import it.units.htl.maps.PhysicalMedia;
import it.units.htl.maps.Series;
import it.units.htl.maps.Studies;
import it.units.htl.maps.WlpatientDataPerVisit;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.users.UserBean;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che2.audit.message.InstancesAccessedMessage;
import org.dcm4che2.audit.message.AuditEvent.ActionCode;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Restrictions;

public class MergeMaker {
	private static Log log = LogFactory.getLog(MergeMaker.class);

	/**
	 * Assign a study to a new patient. This method updates the following:
	 * <ul>
	 * <li>assign the patientDataPerVisit to the new Patient</li>
	 * <li>assign the Study to the new Patient</li>
	 * <li>number of studies of both the old and new Patient</li>
	 * </ul>
	 * If the target patient is the same of the current study owner, no operation is
	 * performed.
	 * 
	 * @param studySRC
	 *            the study to be reassigned
	 * @param patientDST
	 *            the target patient
	 * @param whoDO
	 *            the performing user, needed for AuditLog
	 * @return
	 */
	public Boolean putStudyInPatient(Studies studySRC, Patients patientDST, UserBean whoDO) {
		StatelessSession mergeSession;
		// same patient?
		if (studySRC.getPatients().getPatientId().equals(patientDST.getPatientId())) {
			log.debug(" The target patient is the same of the current one. No merge has to be performed");
			return false;
		}
		mergeSession = SessionManager.getInstance().openStatelessSession();
		mergeSession.beginTransaction();

		WlpatientDataPerVisit wlSrc = studySRC.getWlpatientDataPerVisits();
		wlSrc.setPatients(patientDST);
		mergeSession.update(wlSrc);

		// update the former patient related study number
		PatientDemographics formerPatientDemographics = studySRC.getPatients().getPatientDemographics();
		int formerPatientStudyNumber = formerPatientDemographics.getNumberOfPatientRelatedStudies();
		formerPatientDemographics.setNumberOfPatientRelatedStudies(formerPatientStudyNumber - 1);
		mergeSession.update(formerPatientDemographics);

		// update the new patient related study number
		PatientDemographics newPatientDemographics = patientDST.getPatientDemographics();
		int newPatientStudyNumber = newPatientDemographics.getNumberOfPatientRelatedStudies();
		newPatientDemographics.setNumberOfPatientRelatedStudies(newPatientStudyNumber + 1);
		mergeSession.update(newPatientDemographics);

		// assign the new patient to the study
		studySRC.setPatients(patientDST);
		mergeSession.update(studySRC);
		// commit
		mergeSession.getTransaction().commit();

		try {
			InstancesAccessedMessage msg = new InstancesAccessedMessage(ActionCode.UPDATE);
			try {
				msg.addUserPerson(whoDO.getAccountNo() + "", "", whoDO.getFirstName() + whoDO.getLastName(), InetAddress.getLocalHost().toString(), true);
			} catch (UnknownHostException e) {
				log.warn("Couldn't get local ip", e);
			}
			msg.addPatient(studySRC.getPatients().getPatientId(), studySRC.getPatients().getFirstName() + "^" + studySRC.getPatients().getLastName());
			msg.addStudy(studySRC.getStudyInstanceUid(), null);
			AuditLogService als = AuditLogService.getInstance();
			als.SendMessage(msg);
		} catch (Exception e) {
			log.warn("Unable to send AuditLogMessage", e);
		}
		log.info("***** Merged by " + whoDO.getFirstName() + " " + whoDO.getLastName() + ": Study: " + studySRC.getStudyInstanceUid() + " moved to: " + patientDST.getPatientId() + ": "
				+ patientDST.getFirstName() + " " + patientDST.getLastName() + "*****");
		return true;
	}

	/**
	 * Move a Serie from its original Study to another Study of any patient. It copies the
	 * series folder from its original location to the target study folder. Each dicom
	 * file is copied and the StudyInstanceUID is modified, original files are not
	 * modified in any way. This method updates the following fields in DB:
	 * <ul>
	 * <li>PhysicalMedia filledBytes of the target study only</li>
	 * <li>number of series and instances of both original and target study</li>
	 * <li>study size of both original and target study</li>
	 * <li>the study owner of the serie</li>
	 * </ul>
	 * 
	 * @param serieSRC
	 * @param studyDST
	 * @param whoDO
	 * @return
	 */
	public Boolean putSeriesInStudy(Series serieSRC, Studies studyDST, UserBean whoDO) {
		StatelessSession mergeSession;
	
		long copiedBytes = 0;
		File srcSeriesFolder = new File(serieSRC.getStudies().getFastestAccess() + serieSRC.getStudies().getStudyInstanceUid() + "/" + serieSRC.getSeriesInstanceUid());
		Studies studySRC = serieSRC.getStudies();
		if (srcSeriesFolder.isDirectory()) {
			String dstDir = studyDST.getFastestAccess() + studyDST.getStudyInstanceUid() + "/" + serieSRC.getSeriesInstanceUid();
			File dstSeriesFolder = new File(dstDir);
			if (!dstSeriesFolder.exists()) {
				if (dstSeriesFolder.mkdirs()) {
					log.debug("Folder already exists...");
				} else {
					log.error("putSeriesInStudy: Could not create destination folder..." + dstDir);
				}
			}
			try {
				copiedBytes = copySeriesFiles(srcSeriesFolder, dstSeriesFolder, studyDST.getStudyInstanceUid());
			} catch (Exception e) {
				log.error("Error copying series files from '" + srcSeriesFolder.getAbsolutePath() + "' to '" + dstSeriesFolder.getAbsolutePath() + "'", e);
				return false;
			}

			long srcSeriesSize = getFolderSize(srcSeriesFolder);
			long dstSeriesSize = getFolderSize(dstSeriesFolder);

			String fastestAccess = studyDST.getFastestAccess();
			String urlToStudy = getUrlToStudy(fastestAccess);
			PhysicalMedia physicalMedia = getPhysicalMedia(urlToStudy);

			// begin transaction
			mergeSession = SessionManager.getInstance().openStatelessSession();
			
			mergeSession.beginTransaction();

			// update the physicalMedia filledBytes of the target study
			long filledBytes = physicalMedia.getFilledBytes();
			physicalMedia.setFilledBytes(filledBytes + copiedBytes);
			
			mergeSession.update(physicalMedia);

			// update the number of study related series and instances of the original study
			int serie = studySRC.getNumberOfStudyRelatedSeries() - 1;
			studySRC.setNumberOfStudyRelatedSeries(serie);
			short quanti = (short) (studySRC.getNumberOfStudyRelatedInstances() - serieSRC.getNumberOfSeriesRelatedInstances());
			studySRC.setNumberOfStudyRelatedInstances(quanti);

			// update the study size (old and new)
			long studySize = studySRC.getStudySize();
			studySRC.setStudySize(studySize - srcSeriesSize);
			studySize = studyDST.getStudySize();
			studyDST.setStudySize(studySize + dstSeriesSize);

			// assign the new study to the serie
			serieSRC.setStudies(studyDST);

			// update the number of study related series and instances of the target study
			studyDST.setNumberOfStudyRelatedSeries(serieSRC.getStudies().getNumberOfStudyRelatedSeries() + 1);
			quanti = (short) (studyDST.getNumberOfStudyRelatedInstances() + serieSRC.getNumberOfSeriesRelatedInstances());
			studyDST.setNumberOfStudyRelatedInstances(quanti);

			mergeSession.update(studySRC);
			mergeSession.update(serieSRC);
			mergeSession.update(studyDST);
			mergeSession.getTransaction().commit();
		} else {
			log.error("Source folder not found!");
			return false;
		}

		try {
			InstancesAccessedMessage msg = new InstancesAccessedMessage(ActionCode.UPDATE);
			try {
				msg.addUserPerson(whoDO.getAccountNo() + "", "", whoDO.getFirstName() + whoDO.getLastName(), InetAddress.getLocalHost().toString(), true);
			} catch (UnknownHostException e) {
				log.warn("Couldn't get local ip", e);
			}

			msg.addPatient(studySRC.getPatients().getPatientId(), studySRC.getPatients().getFirstName() + "^" + studySRC.getPatients().getLastName());
			msg.addStudy(studySRC.getStudyInstanceUid(), null);
			AuditLogService als = AuditLogService.getInstance();
			als.SendMessage(msg);
		} catch (Exception e) {
			log.warn("Unable to send AuditLogMessage", e);
		}
		log.info("***** Merged by " + whoDO.getFirstName() + " " + whoDO.getLastName() + " Serie: " + serieSRC.getSeriesInstanceUid() + " of Study : " + studySRC.getStudyInstanceUid() + " moved to: "
				+ studyDST.getPatients().getPatientId() + ": " + studyDST.getPatients().getFirstName() + " " + studyDST.getPatients().getLastName() + ", in study: " + studyDST.getStudyInstanceUid());
		return true;
	}

	/**
	 * Returns the urlToStudy given the fastestAccess. It removes the tailing day, month
	 * and year.
	 * 
	 * @param fastestAccess
	 * @return
	 */
	private String getUrlToStudy(String fastestAccess) {
		// remove day, month, year from fastestAccess to get the urlToStudy
		int li = fastestAccess.lastIndexOf("/"); // remove tailing slash
		fastestAccess = fastestAccess.substring(0, li);
		li = fastestAccess.lastIndexOf("/"); // remove day
		fastestAccess = fastestAccess.substring(0, li);
		li = fastestAccess.lastIndexOf("/"); // remove month
		fastestAccess = fastestAccess.substring(0, li);
		li = fastestAccess.lastIndexOf("/"); // remove year
		fastestAccess = fastestAccess.substring(0, li + 1);
		return fastestAccess;
	}

	private static Dataset getDataset(File DicomSource) throws IOException {
		FileInputStream in = new FileInputStream(DicomSource);
		BufferedInputStream dcm_in = new BufferedInputStream(in, 1000000);
		dcm_in.mark(1000000);
		DcmParser dcm_dp = DcmParserFactory.getInstance().newDcmParser(dcm_in);
		FileFormat dcm_ff;
		dcm_ff = dcm_dp.detectFileFormat();
		Dataset dcm_ds = DcmObjectFactory.getInstance().newDataset();
		dcm_ds.readFile(dcm_in, dcm_ff, -1);
		dcm_in.close();
		dcm_in = null;
		dcm_ff = null;
		in.close();
		return dcm_ds;
	}

	/**
	 * Copy all the dicom files located in the source series folder to the new target
	 * series folder. During the copy procedure, the StudyInstanceUid tag is updated
	 * accordingly to the given parameter. Skip any files already present in the target
	 * folder.<br>
	 * Returns the total size of the copied files only.
	 * 
	 * @param sourceFolder
	 * @param targetFolder
	 * @param newStudyInstanceUid
	 * @return
	 * @throws IOException
	 */
	private long copySeriesFiles(File sourceFolder, File targetFolder, String newStudyInstanceUid) throws IOException {
		long totalBytesCopied = 0;

		File sourceFile = null;
		File targetFile = null;
		String[] files = sourceFolder.list();
		for (String currentFileName : files) {
			sourceFile = new File(sourceFolder, currentFileName);
			targetFile = new File(targetFolder, currentFileName);
			if (!targetFile.exists()) {
				log.debug(currentFileName + " not yet present in the target series folder, start copying");
				Dataset srcDataset = getDataset(sourceFile);
				FileOutputStream out = new FileOutputStream(targetFile);

				// update only the studyInstanceUid
				srcDataset.putUI(Tags.StudyInstanceUID, newStudyInstanceUid);

				DcmEncodeParam dcmEncoder = DcmEncodeParam.valueOf(srcDataset.getFileMetaInfo().getTransferSyntaxUID());
				srcDataset.writeFile(out, dcmEncoder);
				totalBytesCopied += out.getChannel().size();
				out.close();
			} else {
				log.debug(currentFileName + " already present in the target series folder, skipping");
			}
		}

		return totalBytesCopied;
	}

	/**
	 * Returns the total size of the files located in this directory. It does not iterate
	 * through any contained directory.
	 * 
	 * @param directory
	 * @return
	 */
	private long getFolderSize(File folderName) {
		long totalSize = 0;
		File curFile = null;
		String[] files = folderName.list();
		for (String fileName : files) {
			curFile = new File(folderName, fileName);
			if (curFile.isFile()) {
				totalSize += curFile.length();
			}
		}

		return totalSize;
	}

	/**
	 * Retrieve the PhysicalMedia given the urlToStudy
	 * 
	 * @param urlToStudy
	 * @return
	 */
	private PhysicalMedia getPhysicalMedia(String urlToStudy) {
		// get the PhysicalMedia of the target study
		Session session = SessionManager.getInstance().openSession();
		Criteria criteria = session.createCriteria(PhysicalMedia.class);
		criteria.add(Restrictions.eq("urlToStudy", urlToStudy));
		PhysicalMedia pm = (PhysicalMedia) criteria.list().get(0);
		session.close();

		return pm;
	}
}
