/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier;

import it.units.htl.dpacs.postprocessing.UidGenerator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomOutputStream;

/**
 * 
 * @author giacomo petronio
 * 
 */
public class KosBuilder {

	private Log log = LogFactory.getLog(KosBuilder.class);

	/**
	 * Build a valid DICOM key object selection (KOS) referencing all the
	 * instances of all the series passed as parameter. Series and instances
	 * must belong to the same study.
	 * 
	 * @param seriesInstances
	 *            it's a Map of series. <br>
	 *            Each Serie is an entry of the map with the SeriesInstanceUid
	 *            as the key and a List of String[] as the value.<br>
	 *            Each value of the map contains a list of sopInstances (as an
	 *            array of two String) with sopInstanceUid at the first position
	 *            and sopClassUid at the second position of the array of String.<br>
	 * 
	 * @param info
	 *            information needed to build the kos DicomObject
	 * 
	 * @return the KOS DicomObject
	 * @throws InvalidParameterException
	 */
	public DicomObject build(KosMetaInfo data) {
//		log.debug("building kos");

		// array: referencedStudyInstanceUid
		String referencedStudyInstanceUid = data.getReferencedStudyInstanceUid();
		String referencedAccessionNumber = data.getReferencedAccessionNumber();
		String patientId = data.getPatientId();
		String patientIdIssuer = data.getPatientIdIssuer();
		String patientName = data.getPatientName();
		String contentDate = data.getContentDate();
		String contentTime = data.getContentTime();
		String retrieveAETitle = data.getRetrieveAETitle();

		// generated uids
		UidGenerator uidGen = new UidGenerator();
		String kosSopInstanceUid = uidGen.getNewInstanceUid();
		String seriesInstanceUid = uidGen.getNewSeriesUid();
		String kosStudyInstanceUid = uidGen.getNewStudyUid();

		DicomObject obj = new BasicDicomObject();
		obj.initFileMetaInformation(UID.KeyObjectSelectionDocumentStorage, kosSopInstanceUid,
				UID.ImplicitVRLittleEndian);

		obj.putString(Tag.SOPClassUID, VR.UI, UID.KeyObjectSelectionDocumentStorage);
		obj.putString(Tag.TransferSyntaxUID, VR.UI, UID.ImplicitVRLittleEndian);
		obj.putString(Tag.PatientName, VR.PN, patientName);
		obj.putString(Tag.StudyInstanceUID, VR.UI, kosStudyInstanceUid);
		obj.putString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUid);
		obj.putString(Tag.Modality, VR.CS, "KO");
		obj.putInt(Tag.SeriesNumber, VR.IS, 1);

		obj.putString(Tag.PatientID, VR.LO, patientId);
		obj.putString(Tag.IssuerOfPatientID, VR.LO, patientIdIssuer);

		obj.putString(Tag.SOPInstanceUID, VR.UI, kosSopInstanceUid);
		obj.putInt(Tag.InstanceNumber, VR.IS, 1);
		obj.putString(Tag.ValueType, VR.CS, "CONTAINER");
		obj.putString(Tag.ContentDate, VR.DA, contentDate);
		obj.putString(Tag.ContentTime, VR.TM, contentTime);
		obj.putString(Tag.ContinuityOfContent, VR.CS, "SEPARATE");

		DicomElement refReqSeq = obj.putSequence(Tag.CurrentRequestedProcedureEvidenceSequence);
		DicomObject refReqSeqObject = new BasicDicomObject();

		DicomElement refSeriesSeq = refReqSeqObject.putSequence(Tag.ReferencedSeriesSequence);
		refReqSeqObject.putString(Tag.StudyInstanceUID, VR.UI, referencedStudyInstanceUid);
		refReqSeqObject.putString(Tag.AccessionNumber, VR.SH, referencedAccessionNumber);

		Set<String> serieSet = data.getSeries().keySet();

		for (String serieUid : serieSet) {
			DicomObject series = new BasicDicomObject();
			series.putString(Tag.SeriesInstanceUID, VR.UI, serieUid);
			series.putString(Tag.RetrieveAETitle, VR.UI, retrieveAETitle);
			DicomElement instances = series.putSequence(Tag.ReferencedSOPSequence);
			List<String[]> instanceList = data.getSeries().get(serieUid);
			for (String[] instance : instanceList) {
				String sopInstanceUid = instance[0];
				String sopClassUid = instance[1];
				DicomObject doo = new BasicDicomObject();
				doo.putString(Tag.ReferencedSOPClassUID, VR.UI, sopClassUid);
				doo.putString(Tag.ReferencedSOPInstanceUID, VR.UI, sopInstanceUid);
				instances.addDicomObject(doo);
			}
			refSeriesSeq.addDicomObject(series);
		}
		refReqSeq.addDicomObject(refReqSeqObject);

		return obj;
	}

	/**
	 * Store the DICOM key object selection to the specified directory.
	 * 
	 * @param kos
	 * @param kosTempUrl
	 * @throws FileNotFoundException
	 *             if the specified directory does not exists
	 */
	public void store(DicomObject kos, String kosTempUrl) throws FileNotFoundException {

		String instanceUid = kos.getString(Tag.SOPInstanceUID);
		String filePath = kosTempUrl + File.separator + instanceUid;
		log.debug("storing kos file to " + filePath);

		File f = new File(filePath);

		FileOutputStream fos;
		fos = new FileOutputStream(f);

		BufferedOutputStream bos = new BufferedOutputStream(fos);
		DicomOutputStream dos = new DicomOutputStream(bos);

		try {
			dos.writeDicomFile(kos);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				dos.close();
				bos.close();
				fos.close();
			} catch (IOException ignore) {
				ignore.printStackTrace();
			}
		}
	}
}
