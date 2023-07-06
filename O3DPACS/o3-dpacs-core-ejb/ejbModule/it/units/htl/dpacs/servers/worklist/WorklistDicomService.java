/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.worklist;

import it.units.htl.dpacs.helpers.StringHelper;
import it.units.htl.dpacs.postprocessing.UidGenerator;
import it.units.htl.dpacs.servers.worklist.dao.WlQueryDealer;
import it.units.htl.dpacs.servers.worklist.utils.WlKeys;
import it.units.htl.dpacs.servers.worklist.utils.WorklistResponse;
import it.units.htl.dpacs.servers.worklist.utils.WorklistResponseWriter;
import it.units.htl.dpacs.servers.worklist.utils.WlKeys.Key;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.Status;
import org.dcm4che2.net.service.CEchoSCP;
import org.dcm4che2.net.service.CFindSCP;
import org.dcm4che2.net.service.DicomService;

public class WorklistDicomService extends DicomService implements CFindSCP, CEchoSCP {
	private String viewName;
	
	private String datePattern;
	private String dateTimePattern;
	private String dateFormula;
	private String dateTimeFormula;
	
	
	private String _fieldForStudyInstanceUID;
	private WorklistService _caller = null;
	private Context jndiContext = null;
	private final Log log = LogFactory.getLog(WorklistDicomService.class);
	private String callingAeTitle;

	public WorklistDicomService(String viewName, String datePattern, String dateTimePattern, String dateFormula, String dateTimeFormula) {
		super(new String[] { UID.ModalityWorklistInformationModelFIND, UID.VerificationSOPClass });
		
		if((viewName==null)||(datePattern==null)||(dateTimePattern==null)||(dateFormula==null)||(dateTimeFormula==null)
				||
			("".equals(viewName))||("".equals(datePattern))||("".equals(dateTimePattern))||("".equals(dateFormula))||("".equals(dateTimeFormula)))
			throw new IllegalArgumentException("Incomplete configuration of query parameters");

		this.viewName = viewName;
		
		this.datePattern=datePattern;
		this.dateTimePattern=dateTimePattern;
		this.dateFormula=dateFormula;
		this.dateTimeFormula=dateTimeFormula;
		
	}

	public WorklistDicomService(WorklistService caller, String viewName, String fieldForStudyInstanceUID, String datePattern, String dateTimePattern, String dateFormula, String dateTimeFormula) {
		super(new String[] { UID.ModalityWorklistInformationModelFIND, UID.VerificationSOPClass });
		_caller = caller;
		
		if((viewName==null)||(datePattern==null)||(dateTimePattern==null)||(dateFormula==null)||(dateTimeFormula==null)
				||
			("".equals(viewName))||("".equals(datePattern))||("".equals(dateTimePattern))||("".equals(dateFormula))||("".equals(dateTimeFormula)))
			throw new IllegalArgumentException("Incomplete configuration of query parameters");

		this.viewName = viewName;
		
		this.datePattern=datePattern;
		this.dateTimePattern=dateTimePattern;
		this.dateFormula=dateFormula;
		this.dateTimeFormula=dateTimeFormula;
		
		_fieldForStudyInstanceUID = fieldForStudyInstanceUID;
	}

	public void cfind(Association as, int pcid, DicomObject cmd, DicomObject data) throws DicomServiceException, IOException {
		callingAeTitle = as.getCallingAET();
		log.info(callingAeTitle + ": Modality Worklist Request (within connction limit? " + as.getConnector().checkConnectionCountWithinLimit() + ")");
		ResultSet dataToSend = null;
		if (checkFilterFields(data)) {
			Connection con = null;
			try {
				con = getDBConnection();
				WlQueryDealer wlq = new WlQueryDealer();
				dataToSend = wlq.getResults(data, con, viewName, callingAeTitle, datePattern, dateTimePattern, dateFormula, dateTimeFormula);
				DicomObject responseCommand = CommandUtils.mkRSP(cmd, CommandUtils.SUCCESS);
				WorklistResponse response = new WorklistResponse(responseCommand, convertResultSetToDicomObjects(dataToSend, data), as);
				dataToSend.close();
				response.next();
				responseCommand = response.getCommand();
				if (CommandUtils.isPending(responseCommand)) {
					as.registerCancelRQHandler(responseCommand, response);
					_caller._executor.execute(new WorklistResponseWriter(as, pcid, response));
				} else {
					as.writeDimseRSP(pcid, responseCommand, response.getDataset());
				}
			} catch (Exception e) {
				log.error(callingAeTitle + ": Problem during worklist request...", e);
				log.error(callingAeTitle + ": " + data.toString());
				throw new DicomServiceException(cmd, Status.ProcessingFailure, e.getMessage());
			} finally {
				try {
					if (con != null)
						con.close();
				} catch (SQLException e) {
				}
			}
		} else {
			log.error(callingAeTitle + ": Too weak filter! Please look at IHE Vol.2 ");
			log.error(data.toString());
			throw new DicomServiceException(cmd, Status.ProcessingFailure, "Too weak filter! Please check IHE documentation.");
		}
	}

	public void cecho(Association as, int pcid, DicomObject cmd) throws IOException {
		log.info(as.getCallingAET() + ": Echo request arrived!");
		DicomObject responseCommand = CommandUtils.mkRSP(cmd, CommandUtils.SUCCESS);
		as.writeDimseRSP(pcid, responseCommand);
		log.info(as.getCallingAET() + ": Echo response sended!");
	}

	private boolean checkFilterFields(DicomObject data) {
		for (Integer tag : WlKeys.requiredFilterKeys.keySet()) {
			if (WlKeys.requiredFilterKeys.get(tag) == null) {
				String toCheck = data.getString(tag);
				if ((toCheck != null) && (!"".equals(toCheck))) {
					return true;
				}
			} else {
				for (Integer nestedTag : WlKeys.requiredFilterKeys.get(tag)) {
					String toCheck = data.getNestedDicomObject(tag).getString(nestedTag);
					if ((toCheck != null) && (!"".equals(toCheck))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private ArrayList<DicomObject> convertResultSetToDicomObjects(ResultSet source, DicomObject requestedFields) throws SQLException {
		ArrayList<DicomObject> convertedData = new ArrayList<DicomObject>();
		while (source.next()) {
			DicomObject singleResult = new BasicDicomObject();
			for (Key k : WlKeys.returnKeys.keySet()) {
				if (WlKeys.returnKeys.get(k) == null) {
					insertIntoDicomObject(k, null, singleResult, source);
				} else {
					DicomObject sequenceContent = new BasicDicomObject();
					for (Key kk : WlKeys.returnKeys.get(k)) {
						insertIntoDicomObject(kk, k, sequenceContent, source);
//						if (WlKeys.getDbColumnNameFromTag(kk._tag) != null) {
//							insertIntoDicomObject(kk, sequenceContent, source);
//						} else {
//							insertIntoDicomObject(kk, sequenceContent, source);
//						}
					}
					if (sequenceContent.isEmpty()) {
						singleResult.putNull(k._tag, k._vr);
					} else {
						DicomElement sequence = singleResult.putSequence(k._tag);
						sequence.addDicomObject(sequenceContent);
					}

				}
			}
			Iterator<DicomElement> requestedKeys = requestedFields.iterator();
			while (requestedKeys.hasNext()) {
				DicomElement reqField = requestedKeys.next();
				if (!singleResult.contains(reqField.tag())) {
					log.debug(callingAeTitle + ": The system has not this information (so it puts null): " + reqField.tag());
					singleResult.putNull(reqField.tag(), reqField.vr());
				}
			}
			convertedData.add(singleResult);

		}

		log.info(callingAeTitle + ": The system found " + convertedData.size() + " results.");
		return convertedData;
	}

	private void insertIntoDicomObject(Key key, Key parentKey, DicomObject destination, ResultSet resultSet) throws SQLException {
		
		String colName = WlKeys.getDbColumnNameFromTag(key._tag);
		if(colName == null) {
			colName = WlKeys.getDbColumnNameFromTag(parentKey._tag + key._tag);
		}

		switch (key._vr.code()) {
		case 0x4441: // DA Date
			java.util.Date d= null;
			try{
				d=resultSet.getTimestamp(WlKeys.getDbColumnNameFromTag(key._tag));
			}catch(SQLException sex){
				log.debug("ERROR READING TIMESTAMP, WILL TRY DATE");
				d=resultSet.getDate(WlKeys.getDbColumnNameFromTag(key._tag));
				log.debug("READ DATE CORRECTLY");
			}
			destination.putDate(key._tag, key._vr, d);
			break;
		case 0x544d: // TM Time
			java.util.Date t= null;
			try{
				t=resultSet.getTimestamp(WlKeys.getDbColumnNameFromTag(key._tag));
			}catch(SQLException sex){
				log.debug("ERROR READING TIMESTAMP, WILL TRY TIME");
				t=resultSet.getTime(WlKeys.getDbColumnNameFromTag(key._tag));
				log.debug("READ TIME CORRECTLY");
			}
			destination.putDate(key._tag, key._vr, t);
			break;
		default:
			String value = resultSet.getString(colName);
			if (value != null && !value.trim().equals("")) {
				destination.putString(key._tag, key._vr, (String) value.toString());
			} else {
				insertNullValues(key, destination, resultSet);
			}
		} // switch
	}

	private void insertNullValues(Key k, DicomObject destination, ResultSet resultSet) {
		if (Tag.StudyInstanceUID == k._tag) {
			String uniqueField = null;
			try {
				uniqueField = resultSet.getString(_fieldForStudyInstanceUID);
			} catch (SQLException e) {
				log.error("No unique field is provided by the view, so I put null studyInstanceUID...maybe the worklist will not be retrieve...", e);
			}
			if (uniqueField != null) {
				String studyInstanceUID = UidGenerator.O3EnterpriseUidRoot + "." + StringHelper.convertUniqueField(uniqueField);
				studyInstanceUID = studyInstanceUID.replace("..", ".");
				destination.putString(k._tag, k._vr, studyInstanceUID);
			} else {
				destination.putString(k._tag, k._vr, null);
			}
		} else if (k._type == 1) {
			log.debug(callingAeTitle + ": Keep attention the system can not insert a DICOM type 1, 'cause is null");
			if ((k._tag != Tag.ReferencedSOPClassUID) && (k._tag != Tag.ReferencedSOPInstanceUID)) {
				destination.putNull(k._tag, k._vr);
			}
		} else if (k._type == 2) {
			destination.putNull(k._tag, k._vr);
		} else {
			log.debug(callingAeTitle + ": this Tag " + k._tag + " is DICOM type 3, so the system can omit that.");
		}
	}


	private Connection getDBConnection() throws Exception {
		if (_caller._onAS) {
			if (jndiContext == null) {
				jndiContext = new InitialContext();
			}
			DataSource ds = (DataSource) jndiContext.lookup("java:/jdbc/worklistDS");
			return ds.getConnection();
		} else {
			String userName = _caller._DbUser;
			String password = _caller._DbPass;
			Class.forName(_caller._JDBCDriver).newInstance();
			Connection conn = DriverManager.getConnection(_caller._connectionString, userName, password);
			return conn;
		}
	}
}
