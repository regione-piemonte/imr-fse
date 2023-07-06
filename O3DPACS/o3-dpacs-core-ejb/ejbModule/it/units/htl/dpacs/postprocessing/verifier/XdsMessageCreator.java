/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier;

import it.units.htl.dpacs.postprocessing.utils.XdsAffinityDomainCodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;

import eu.o3c.xds.core.hl7.DTM;
import eu.o3c.xds.core.hl7.DataTypeException;
import eu.o3c.xds.core.hl7.OID;
import eu.o3c.xds.core.jaxb.ProvideAndRegisterDocumentSetRequestType;
import eu.o3c.xds.core.messages.SubmitObjectsRequestMessageBuilder;
import eu.o3c.xds.core.objects.XdsCodeAttribute;
import eu.o3c.xds.core.objects.XdsDocument;
import eu.o3c.xds.core.objects.XdsSubmissionSet;

public class XdsMessageCreator {

	private static final Log log = LogFactory.getLog(XdsMessageCreator.class);

	private String xdsSourceId;

	public XdsMessageCreator(String xdsSourceId) {
		this.xdsSourceId = xdsSourceId;
	}

	public ProvideAndRegisterDocumentSetRequestType createMessage(File kosFile, String folderUuid, String xdsPrefix, String subsetUniqueId)
			throws IOException {
		
		// trim XDS Prefix
		String trimmedXdsPrefix = xdsPrefix;
		if(xdsPrefix != null){
			trimmedXdsPrefix = xdsPrefix.trim();
		}
		
		// read DicomObject
		DicomInputStream dis = new DicomInputStream(kosFile);
		DicomObject obj = dis.readDicomObject();
		dis.close();

		byte[] attachment = getBytes(kosFile);

		XdsDocument doc = new XdsDocument();
		XdsSubmissionSet subSet = new XdsSubmissionSet();

		// load codes from XdsCodes table
		Map<String, XdsCodeAttribute> xdsCodes = loadXdsCodes();
		boolean isComplete = true;
		for (String key : xdsCodes.keySet()) {
			if(xdsCodes.get(key) == null){
				log.warn("null XdsCode for "+key);
				isComplete = false;
			}
		}
		if(!isComplete){
			return null;
		}
		
		// Fill attributes
		fillXdsDocumentAttributes(doc, obj, xdsCodes, trimmedXdsPrefix);
		fillXdsSubmissionSetAttributes(subSet, obj, xdsCodes, trimmedXdsPrefix, subsetUniqueId);

		SubmitObjectsRequestMessageBuilder sorBuilder = new SubmitObjectsRequestMessageBuilder();
		sorBuilder.addDocument(doc, attachment);
		sorBuilder.setSubmissionSet(subSet);

		// studyInstanceUid
		sorBuilder.addDocumentToFolder(doc.entryUuid, folderUuid);

		ProvideAndRegisterDocumentSetRequestType pnr = sorBuilder
				.getProvideAndRegisterDocumentSetMessage();

		return pnr;
	}

	private Map<String, XdsCodeAttribute> loadXdsCodes() {
		Map<String, XdsCodeAttribute> xdsCodes = new HashMap<String, XdsCodeAttribute>();

		xdsCodes.put(XdsAffinityDomainCodes.CLASS_CODE,
				getXdsCodeAttribute(XdsAffinityDomainCodes.CLASS_CODE));
		xdsCodes.put(XdsAffinityDomainCodes.CONTENT_TYPE_CODE,
				getXdsCodeAttribute(XdsAffinityDomainCodes.CONTENT_TYPE_CODE));
		xdsCodes.put(XdsAffinityDomainCodes.FORMAT_CODE,
				getXdsCodeAttribute(XdsAffinityDomainCodes.FORMAT_CODE));
		xdsCodes.put(XdsAffinityDomainCodes.HEALTHCARE_FACILITY_TYPE_CODE,
				getXdsCodeAttribute(XdsAffinityDomainCodes.HEALTHCARE_FACILITY_TYPE_CODE));
		xdsCodes.put(XdsAffinityDomainCodes.HEALTHCARE_FACILITY_TYPE_CODE,
				getXdsCodeAttribute(XdsAffinityDomainCodes.HEALTHCARE_FACILITY_TYPE_CODE));
		xdsCodes.put(XdsAffinityDomainCodes.PRACTICE_SETTING_CODE,
				getXdsCodeAttribute(XdsAffinityDomainCodes.PRACTICE_SETTING_CODE));
		xdsCodes.put(XdsAffinityDomainCodes.TYPE_CODE,
				getXdsCodeAttribute(XdsAffinityDomainCodes.TYPE_CODE));
		
		return xdsCodes;
	}
	
	private void fillXdsSubmissionSetAttributes(XdsSubmissionSet subset, DicomObject obj, Map<String, XdsCodeAttribute> xdsCodes, String trimmedPrefix, String subsetUniqueId) {

		// source id from GlobalConfiguration
		try {
			subset.sourceId = new OID(this.xdsSourceId);
		} catch (DataTypeException e) {
			e.printStackTrace();
			log.error("Invalid value for XdsSourceId: " + this.xdsSourceId);
		}

		// contentTypecode
		subset.contentTypeCode = getXdsCodeAttribute(XdsAffinityDomainCodes.CONTENT_TYPE_CODE);

		// submissionTime now
		subset.submissionTime = DTM.now();

		// uniqueId
//		UidGenerator gen = new UidGenerator();
//		String uid = gen.getNewStudyUid();
//		try {
//			subset.uniqueId = new OID(uid);
//		} catch (DataTypeException e) {
//			e.printStackTrace();
//		}
		try{
			subset.uniqueId = new OID(subsetUniqueId);
		} catch (DataTypeException e) {
			log.error("invalid submission unique id (OID format)", e);
		}

		// patientId
		String idNumber = obj.getString(Tag.PatientID);
		if(trimmedPrefix!= null){
			idNumber = trimmedPrefix.concat(idNumber);
		}
		subset.patientId = idNumber;
	}

	private void fillXdsDocumentAttributes(XdsDocument doc, DicomObject obj, Map<String, XdsCodeAttribute> xdsCodes, String trimmedPrefix) {

		getXdsCodeAttribute(XdsAffinityDomainCodes.CLASS_CODE);

		// class code
		doc.classCode = getXdsCodeAttribute(XdsAffinityDomainCodes.CLASS_CODE);

		// confidentiality code
		doc.confidentialityCode
				.add(getXdsCodeAttribute(XdsAffinityDomainCodes.CONFIDENTIALITY_CODE));

		// creation time
		String contentDate = obj.getString(Tag.ContentDate); // YYYYMMDD
		String contentTime = obj.getString(Tag.ContentTime); // YYYYMMDDHHMMSS.FFFFFF&ZZXX
		DTM creationTime = null;
		if (contentTime != null) {
			try {
				creationTime = new DTM(contentTime);
			} catch (DataTypeException e) {
				creationTime = DTM.now();
			}
		} else if (contentDate != null) {
			try {
				creationTime = new DTM(contentDate);
			} catch (DataTypeException e) {
				creationTime = DTM.now();
			}
		}
		doc.creationTime = creationTime;

		// format code
		doc.formatCode = getXdsCodeAttribute(XdsAffinityDomainCodes.FORMAT_CODE);

		// healtcare facility typecode
		doc.healthcareFacilityTypeCode = getXdsCodeAttribute(XdsAffinityDomainCodes.HEALTHCARE_FACILITY_TYPE_CODE);

		// language code
		doc.languageCode = getXdsCodeAttribute(XdsAffinityDomainCodes.LANGUAGE_CODE).code;
		
		// mime type
		doc.mimeType = getXdsCodeAttribute(XdsAffinityDomainCodes.MIME_TYPE).code;

		// practice setting code
		doc.practiceSettingCode = getXdsCodeAttribute(XdsAffinityDomainCodes.PRACTICE_SETTING_CODE);

		// service start time (study date/time)
		// should perform a query on the study...

		// source patient id
		String sPidNumber = obj.getString(Tag.PatientID);
		doc.sourcePatientId = sPidNumber;

		// source patient info
		// should perform a query in patients table
//		doc.sourcePatientInfo = "PID-3|" + sPidNumber;
		doc.sourcePatientInfo.add("PID-3|" + sPidNumber);

		// type code
		doc.typeCode = getXdsCodeAttribute(XdsAffinityDomainCodes.TYPE_CODE);

		// unique id
		String sopInstanceUid = obj.getString(Tag.SOPInstanceUID);
		if(trimmedPrefix!= null){
			sopInstanceUid = trimmedPrefix.concat(sopInstanceUid);
		}
		doc.uniqueId = sopInstanceUid;

		// patient id
		String pidNumber = obj.getString(Tag.PatientID);
		if(trimmedPrefix!= null){
			pidNumber = trimmedPrefix.concat(pidNumber);
		}
		doc.patientId = pidNumber;
	}

	private byte[] getBytes(File file) {
		log.debug("getting bytes from " + file.getName());

		byte[] bytes = null;
		FileInputStream fis = null;
		int off = 0;

		try {
			fis = new FileInputStream(file);
			long fileSize = file.length();

			if (fileSize < Integer.MAX_VALUE) {
				bytes = new byte[(int) fileSize];

				int readBytes = 0;
				while ((readBytes = fis.read(bytes)) >= 0) {
					off += readBytes;
				}
			} else {
				// file too large
				log.warn("file too large");
			}

			if (off < bytes.length) {
				log.warn("Could not completely read file " + file.getName());
			}

		} catch (IOException e) {
			log.error("", e);
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return bytes;
	}

	private Connection getConnection() throws SQLException, NamingException {
		try {
			InitialContext jndiContext = new InitialContext();
			DataSource dataSource = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
			return dataSource.getConnection();
		} catch (SQLException sex) {
			log.fatal("Unable to create Connection to DB", sex);
			throw sex;
		} catch (NamingException nex) {
			log.fatal("Unable to retrieve the DataSource", nex);
			throw nex;
		}
	}

	private XdsCodeAttribute getXdsCodeAttribute(String codeName) {
		XdsCodeAttribute ret = null;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String select = "SELECT codeValue, displayName, codingScheme FROM XdsCodes WHERE codeName = '"
				+ codeName + "'";
		try {
			con = getConnection();
			st = con.createStatement();
			rs = st.executeQuery(select);

			if ((rs != null) && (rs.next())) {
				ret = new XdsCodeAttribute();
				ret.code = rs.getString(1);
				ret.displayName = rs.getString(2);
				ret.codingScheme = rs.getString(3);
			} else {
				log.error("null or empty resultset while querying for XdsCode: "+codeName);
			}
		} catch (Exception ex) {
			log.error("An error occurred retrieving " + codeName + " parameter: ", ex);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception ex) {
			}
			try {
				if (st != null) {
					st.close();
				}
			} catch (Exception ex) {
			}
			try {
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
			}
		}
		return ret;
	}
}
