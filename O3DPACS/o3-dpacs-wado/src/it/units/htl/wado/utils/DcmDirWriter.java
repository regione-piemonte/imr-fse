/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.wado.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.CRC32;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRMap;
import org.dcm4che.media.*;
import org.dcm4che.util.UIDGenerator;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

import it.units.htl.dpacs.valueObjects.DicomMatch;
import it.units.htl.wado.dao.PacsRetrieveManager;

public class DcmDirWriter {

	private final static DirBuilderFactory fact = DirBuilderFactory.getInstance();
	private final static DcmObjectFactory dof = DcmObjectFactory.getInstance();
	private String id = "";
	private File readMeFile = null;
	private String readMeCharset = null;
	private static Logger log = Logger.getLogger(DcmDirWriter.class);

	private File dcmDirFile = null;
	
	private int objectIndex = 1;
	private String previousSeriesNumer = "";
    private static String DIRECTORY_SEPARATOR = "/";

	public DcmDirWriter(File[] dcmFiles, String studyUID, String wadoTempUrl) {
		if (wadoTempUrl.endsWith("/")) {
			this.dcmDirFile = new File(wadoTempUrl + studyUID + ".dcm");
		} else {
			this.dcmDirFile = new File(wadoTempUrl + "/" + studyUID + ".dcm");
		}

		create(dcmDirFile, dcmFiles);
	}
	
	public DcmDirWriter(List<String> studies, String wadoTempUrl, PacsRetrieveManager prm) {
		if (wadoTempUrl.endsWith("/")) {
			this.dcmDirFile = new File(wadoTempUrl + studies.get(0) + ".dcm");
		} else {
			this.dcmDirFile = new File(wadoTempUrl + "/" + studies.get(0) + ".dcm");
		}

		create(dcmDirFile, prm, studies);
	}

	private void create(File dirFile, File[] files) {
		Properties cfg = null;
		InputStream in = DcmDirWriter.class.getResourceAsStream("/it/units/htl/wado/utils/dcmdir.properties");
		try {
			cfg = new Properties();
			cfg.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignore) {
				}
			}
		}
		this.id = replace(cfg.getProperty("fs-id", ""), "<none>", "");
		replace(cfg.getProperty("fs-uid", ""), "<auto>", "");
		String rm = replace(cfg.getProperty("readme"), "<none>", null);

		if (rm != null) {
			this.readMeFile = new File(rm);
			this.readMeCharset = replace(cfg.getProperty("readme-charset"), "<none>", null);
		}

		HashMap<String, Dataset> map = new HashMap<String, Dataset>();
		for (Enumeration<?> en = cfg.keys(); en.hasMoreElements();) {
			addDirBuilderPrefElem(map, (String) en.nextElement());
		}
		DirBuilderPref pref = fact.newDirBuilderPref();
		for (Iterator<Map.Entry<String,Dataset>> it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Dataset> entry = it.next();
			pref.setFilterForRecordType((String) entry.getKey(), (Dataset) entry.getValue());
		}
		pref.setFilterForRecordType("PRIVATE", dof.newDataset());
		String uid = UIDGenerator.getInstance().createUID();

		File rootDir = dirFile.getParentFile();

		if (rootDir != null && !rootDir.exists()) {
			log.warn("wadoTmpUrl '" + rootDir + "' directory didn't exist");
			rootDir.mkdirs();
		}
		DirWriter writer = null;
		try {
			writer = fact.newDirWriter(dirFile, uid, id, readMeFile, readMeCharset, encodeParam());
			DirBuilder builder = fact.newDirBuilder(writer, pref);
			int[] counter = new int[2];
			for (int j = 0; j < files.length; j++) {
				append(builder, files[j], counter);
			}
			writer.close();
		} catch (Exception e) {
			log.error("Exception on dcmdir creation", e);
		}
	}

	private void append(DirBuilder builder, File file, int[] counter) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; ++i) {
				append(builder, files[i], counter);
			}
		} else {
			InputStream in = null;
			try {
				Dataset ds = DcmObjectFactory.getInstance().newDataset();
				in = new BufferedInputStream(new FileInputStream(file));
				ds.readFile(in, FileFormat.DICOM_FILE, Tags.PixelData);

				String seriesInstanceUid = ds.getString(Tags.SeriesInstanceUID);
				String sopInstanceUid = ds.getString(Tags.SOPInstanceUID);
				String referencedFileId = seriesInstanceUid + "/" + sopInstanceUid;
				
				
				// in this way the reference to the dicom file is specified manually
				counter[1] += builder.addFileRef(new String[] { referencedFileId }, ds);
				++counter[0];

			} catch (IllegalArgumentException e) {
				log.error("Exception on dcmdir creation", e);
			} catch (IOException e) {
				log.error("Exception on dcmdir creation", e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					log.error("Error closing InputStream of an instance while creating dcmdir", e);
				}
			}
		}
	}
	
	/**
	 * Metodo che crea l'indice dicomDir
	 * @param dirFile directory dell'indice dicomDir 
	 * @param studies lista di studies per l'indice dicomDir
	 */
	private void create(File dirFile, PacsRetrieveManager prm, List<String> studies) {
		log.info("Creating DICOMDIR generator...");
		Properties cfg = null;
		log.info("Getting properties...");
		InputStream in = DcmDirWriter.class.getResourceAsStream("/it/units/htl/wado/utils/dcmdir.properties");
		log.info("Properties obtained...");
		try {
			cfg = new Properties();
			cfg.load(in);
		} catch (Exception e) {
			log.info("Opening properties failed due to: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignore) {
				}
			}
		}
		this.id = replace(cfg.getProperty("fs-id", ""), "<none>", "");
		replace(cfg.getProperty("fs-uid", ""), "<auto>", "");
		String rm = replace(cfg.getProperty("readme"), "<none>", null);
		if (rm != null) {
			this.readMeFile = new File(rm);
			this.readMeCharset = replace(cfg.getProperty("readme-charset"), "<none>", null);
		}
		HashMap<String, Dataset> map = new HashMap<String, Dataset>();
		for (Enumeration<?> en = cfg.keys(); en.hasMoreElements();) {
			addDirBuilderPrefElem(map, (String) en.nextElement());
		}
		DirBuilderPref pref = fact.newDirBuilderPref();
		for (Iterator<Map.Entry<String,Dataset>> it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Dataset> entry = it.next();
			pref.setFilterForRecordType((String) entry.getKey(), (Dataset) entry.getValue());
		}
		pref.setFilterForRecordType("PRIVATE", dof.newDataset());
		String uid = UIDGenerator.getInstance().createUID();
		File rootDir = dirFile.getParentFile();
		if (rootDir != null && !rootDir.exists()) {
			log.warn("wadoTmpUrl '" + rootDir + "' directory didn't exist");
			rootDir.mkdirs();
		}
		DirWriter writer = null;
		try {
			writer = fact.newDirWriter(dirFile, uid, id, readMeFile, readMeCharset, encodeParam());
			DirBuilder builder = fact.newDirBuilder(writer, pref);
			for (String study : studies) {
		        DicomMatch[] dicomFilesUrl = prm.getFiles(study, null, null);
		        if (dicomFilesUrl == null) {
		            log.error("No objects found for study " + study);
		            return;
		        }
		        log.error("Objects found for study " + study);
		        File[] files = new File[dicomFilesUrl.length];
		        for (int i = 0; i < dicomFilesUrl.length; i++) {
		            StringBuilder url = new StringBuilder();
		            log.info("################Study ID 1:" + dicomFilesUrl[i].study.getStudyId());
		            url.append(dicomFilesUrl[i].study.getFastestAccess());
		            url.append(dicomFilesUrl[i].study.getStudyInstanceUid());
		            url.append(DIRECTORY_SEPARATOR).append(dicomFilesUrl[i].series.getSeriesInstanceUid());
		            url.append(DIRECTORY_SEPARATOR).append(dicomFilesUrl[i].instance.getSopInstanceUid());
		            log.info("#####URL: "+ url.toString());
		            files[i] = new File(url.toString());
		        }
		        
		        study = getStudyNumber(study);
				int[] counter = new int[2];
				for (int j = 0; j < files.length; j++) {
					append(builder, files[j], counter, study, prm, dicomFilesUrl[j]);
				}
			}
			writer.close();
		} catch (Exception e) {
			log.error("Exception on dcmdir creation due to: " + e.getMessage());
		}
	}
	
	/**
	 * Aggiunge le informazioni sul file dicom nell'indice
	 * @param file file dicom
	 * @param studyUID identificativo dello studio del file dicom
	 */
	private void append(DirBuilder builder, File file, int[] counter, 
			String studyUID, PacsRetrieveManager prm, DicomMatch dm) throws NamingException {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; ++i) {
				append(builder, files[i], counter, studyUID, prm, dm);
			}
		} else {
			InputStream in = null;
			try {
				Dataset ds = DcmObjectFactory.getInstance().newDataset();
				in = new BufferedInputStream(new FileInputStream(file));
				ds.readFile(in, FileFormat.DICOM_FILE, Tags.PixelData);
				String studyID = ds.getString(Tags.StudyID);
				String seriesNumber = getSeriesNumber(ds.getString(Tags.SeriesInstanceUID));
				String objectName = "";
				if (previousSeriesNumer.equals("")) {
					objectName = String.format("%" + (8) + "s", "" + objectIndex).replace(" ", "0");
					objectIndex += 1;
					previousSeriesNumer = seriesNumber;
				} else if (previousSeriesNumer.equals(seriesNumber)) {
					objectName = String.format("%" + (8) + "s", "" + objectIndex).replace(" ", "0");
					objectIndex += 1;
				} else if (!previousSeriesNumer.equals(seriesNumber)) {
					objectIndex = 1;
					objectName = String.format("%" + (8) + "s", "" + objectIndex).replace(" ", "0");
					objectIndex += 1;
					previousSeriesNumer = seriesNumber;
				}
				studyID = studyUID;
				String referencedFileId = "DICOM\\" + studyID + "\\" + seriesNumber + "\\" + objectName;
				
				 
				DicomObject newData = new BasicDicomObject();  
				ds.putLO(Tag.PatientID, dm.patient.getPatientId());
				//ds.putString(Tag.PatientID, VR.CS, dm.patient.getPatientId());
				ds.putLO(Tag.IssuerOfPatientID, dm.patient.getIdIssuer());
				//ds.putString(Tag.IssuerOfPatientID, VR.CS, dm.patient.getIdIssuer());
				ds.putPN(Tag.PatientName, dm.patient.getDcmPatientName());
				//ds.putString(Tag.PatientName, VR.PN, dm.patient.getDcmPatientName());
				ds.putDA(Tag.PatientBirthDate, dm.patient.getBirthDate());
				//ds.putDate(Tag.PatientBirthDate, VR.DA, dm.patient.getBirthDate());
				//ds.putCS(Tag.PatientID, dm.patient.getPatientId());
				 //ds.putString(Tag.PatientID, VR.CS, dm.patient.getPatientId());
		        
				ds.putUI(Tag.StudyInstanceUID, dm.study.getStudyInstanceUid());
				 //ds.putString(Tag.StudyInstanceUID, VR.UI, dm.study.getStudyInstanceUid());
				ds.putSH(Tag.AccessionNumber, dm.study.getAccessionNumber());
				//ds.putString(Tag.AccessionNumber, VR.SH, dm.study.getAccessionNumber());
				ds.putSH(Tag.StudyID, dm.study.getStudyId()); 
				//ds.putString(Tag.StudyID, VR.SH, dm.study.getStudyId());
				ds.putDA(Tag.StudyDate, dm.study.getStudyDate(new SimpleDateFormat("yyyyMMdd")));
				//ds.putString(Tag.StudyDate, VR.DA, dm.study.getStudyDate(new SimpleDateFormat("yyyyMMdd")));
				ds.putTM(Tag.StudyTime,dm.study.getStudyTime(new SimpleDateFormat("hhmmss"))); 
				//ds.putString(Tag.StudyTime, VR.TM, dm.study.getStudyTime(new SimpleDateFormat("hhmmss")));
				ds.putDA(Tag.StudyVerifiedDate,dm.study.getStudyVerifiedDate(new SimpleDateFormat("hhmmss")));
				//ds.putString(Tag.StudyVerifiedDate, VR.DA, dm.study.getStudyVerifiedDate(new SimpleDateFormat("yyyyMMdd")));
				ds.putTM(Tag.StudyVerifiedTime,dm.study.getStudyVerifiedTime(new SimpleDateFormat("hhmmss")));
				//ds.putString(Tag.StudyVerifiedTime, VR.TM, dm.study.getStudyVerifiedTime(new SimpleDateFormat("hhmmss")));
				ds.putDA(Tag.StudyCompletionDate, dm.study.getStudyCompletionDate(new SimpleDateFormat("yyyyMMdd")));
				//ds.putString(Tag.StudyCompletionDate, VR.DA, dm.study.getStudyCompletionDate(new SimpleDateFormat("yyyyMMdd")));
				ds.putTM(Tag.StudyCompletionTime, dm.study.getStudyCompletionTime(new SimpleDateFormat("hhmmss")));
				 //ds.putString(Tag.StudyCompletionTime, VR.TM, dm.study.getStudyCompletionTime(new SimpleDateFormat("hhmmss")));
				
				counter[1] += builder.addFileRef(new String[] { referencedFileId }, ds);
				
				
				++counter[0];
			} catch (IllegalArgumentException e) {
				log.error("Exception on dcmdir creation due to: ", e);
			} catch (IOException e) {
				log.error("Exception on dcmdir creation due to: ", e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					log.error("Error closing InputStream of an instance while creating dcmdir", e);
				}
			}
		}
	}
	
	/**
	 * Aggiunge le informazioni sul file dicom nell'indice
	 * @param file file dicom
	 * @param studyUID identificativo dello studio del file dicom
	 */
	private void append(DirBuilder builder, File file, int[] counter, 
			String studyUID, PacsRetrieveManager prm) throws NamingException {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; ++i) {
				append(builder, files[i], counter, studyUID, prm);
			}
		} else {
			InputStream in = null;
			try {
				Dataset ds = DcmObjectFactory.getInstance().newDataset();
				in = new BufferedInputStream(new FileInputStream(file));
				ds.readFile(in, FileFormat.DICOM_FILE, Tags.PixelData);
				String studyID = ds.getString(Tags.StudyID);
				String seriesNumber = getSeriesNumber(ds.getString(Tags.SeriesInstanceUID));
				String objectName = "";
				if (previousSeriesNumer.equals("")) {
					objectName = String.format("%" + (8) + "s", "" + objectIndex).replace(" ", "0");
					objectIndex += 1;
					previousSeriesNumer = seriesNumber;
				} else if (previousSeriesNumer.equals(seriesNumber)) {
					objectName = String.format("%" + (8) + "s", "" + objectIndex).replace(" ", "0");
					objectIndex += 1;
				} else if (!previousSeriesNumer.equals(seriesNumber)) {
					objectIndex = 1;
					objectName = String.format("%" + (8) + "s", "" + objectIndex).replace(" ", "0");
					objectIndex += 1;
					previousSeriesNumer = seriesNumber;
				}
				studyID = studyUID;
				String referencedFileId = "DICOM\\" + studyID + "\\" + seriesNumber + "\\" + objectName;
				counter[1] += builder.addFileRef(new String[] { referencedFileId }, ds);
				++counter[0];
			} catch (IllegalArgumentException e) {
				log.error("Exception on dcmdir creation due to: ", e);
			} catch (IOException e) {
				log.error("Exception on dcmdir creation due to: ", e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					log.error("Error closing InputStream of an instance while creating dcmdir", e);
				}
			}
		}
	}

	private DcmEncodeParam encodeParam() {
		return new DcmEncodeParam(ByteOrder.LITTLE_ENDIAN, true, false, false, true, true, true);
	}

	private void addDirBuilderPrefElem(HashMap<String, Dataset> map, String key) {
		if (!key.startsWith("dir.")) {
			return;
		}

		int pos2 = key.lastIndexOf('.');
		String type = key.substring(4, pos2).replace('_', ' ');
		Dataset ds = map.get(type);
		if (ds == null) {
			map.put(type, ds = dof.newDataset());
		}
		int tag = Tags.forName(key.substring(pos2 + 1));
		ds.putXX(tag, VRMap.DEFAULT.lookup(tag));
	}

	private String replace(String val, String from, String to) {
		return from.equals(val) ? to : val;
	}

	public File getDcmDirFile() {
		return this.dcmDirFile;
	}
	
	/**
	 * Metodo che recupera il numero di una series
	 * @param series identificativo della series
	 * @return numero della series
	 */
	public String getSeriesNumber(String series) {
		byte[] bytes = series.getBytes();
		CRC32 crc = new CRC32();
		crc.update(bytes, 0, bytes.length);
		String seriesNumber = Long.toHexString(crc.getValue()).toString().toUpperCase();
		log.info("Series to hex ---> " + series + " series number ---> " + seriesNumber);
		
		return seriesNumber;
	}
	
	/**
	 * Metodo che recupera il numero di uno studio
	 * @param study identificativo dello study
	 * @return numero dello studio
	 */
	public String getStudyNumber(String study) {
		byte[] bytes = study.getBytes();
		CRC32 crc = new CRC32();
		crc.update(bytes, 0, bytes.length);
		String studyNumber = Long.toHexString(crc.getValue()).toString().toUpperCase();
		log.info("Study to hex ---> " + study + " study number ---> " + studyNumber);
		
		return studyNumber;
	}
}
