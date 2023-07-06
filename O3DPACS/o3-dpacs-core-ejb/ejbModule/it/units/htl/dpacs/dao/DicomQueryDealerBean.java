/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.LogMessage;
import it.units.htl.dpacs.statistics.Timer;
import it.units.htl.dpacs.statistics.TimerLogger;
import it.units.htl.dpacs.valueObjects.CodeSequence;
import it.units.htl.dpacs.valueObjects.DicomConstants;
import it.units.htl.dpacs.valueObjects.DicomMatch;
import it.units.htl.dpacs.valueObjects.Equipment;
import it.units.htl.dpacs.valueObjects.Image;
import it.units.htl.dpacs.valueObjects.Instance;
import it.units.htl.dpacs.valueObjects.NonImage;
import it.units.htl.dpacs.valueObjects.Overlay;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.dpacs.valueObjects.PersonalName;
import it.units.htl.dpacs.valueObjects.PresState;
import it.units.htl.dpacs.valueObjects.Series;
import it.units.htl.dpacs.valueObjects.StructRep;
import it.units.htl.dpacs.valueObjects.Study;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class DicomQueryDealerBean implements DicomQueryDealerLocal, DicomQueryDealerRemote {
    private boolean oracle;
    private static final TimerLogger tLogger = new TimerLogger();
    // private SessionContext context;
    
    private @Resource(name="java:/jdbc/queryDS") DataSource dataSource;
    // Note: Some queries are defined inside some methods, e.g.
    // findNumberOf*Related* at the end!
    // private static final String patientRootMatchP =
    // "SELECT Patients.pk, Patients.lastName, Patients.firstName, Patients.middleName, Patients.prefix, Patients.suffix, Patients.birthDate, Patients.birthTime, Patients.sex, Patients.patientID, Patients.idIssuer, /*PatientDemographics.ethnicGroup, PatientDemographics.patientComments, PatientDemographics.patientAddress, PatientDemographics.numberOfPatientRelatedStudies, WLPatientDataPerVisit.studyFK, WLPatientDataPerVisit.patientState, WLPatientDataPerVisit.pregnancyStatus, WLPatientDataPerVisit.medicalAlerts, WLPatientDataPerVisit.patientWeight, WLPatientDataPerVisit.confidentialityConstOnPatData , WLPatientDataPerVisit.specialNeeds, WLPatientDataPerVisit.assignedPatientLocation FROM Patients, PatientDemographics, WLPatientDataPerVisit WHERE Patients.deprecated=FALSE AND WLPatientDataPerVisit.deprecated=FALSE AND PatientDemographics.patientFK=Patients.pk AND WLPatientDataPerVisit.patientFK=Patients.pk";
    // // 890
    private static final String patientRootMatchP = "SELECT DISTINCT Patients.pk, Patients.lastName, Patients.firstName, Patients.middleName, Patients.prefix, Patients.suffix, Patients.birthDate, Patients.birthTime, Patients.sex, Patients.patientID, Patients.idIssuer, PatientDemographics.numberOfPatientRelatedStudies FROM Patients, PatientDemographics, Studies WHERE Studies.deprecated=0 AND Studies.patientFK=Patients.pk AND PatientDemographics.patientFK=Patients.pk "; // 890
    private static final String patientRootMatchPStMandatoryFieldsSelect = "SELECT DISTINCT " + "Patients.pk, Patients.lastName,Patients.firstName,Patients.middleName,Patients.prefix,Patients.suffix," + "Patients.birthDate,Patients.birthTime,Patients.sex,Patients.patientID,Patients.idIssuer," + "Studies.studyStatus,Studies.studyInstanceUID,Studies.studyID,Studies.studyStatusID,Studies.studyDate,Studies.studyTime,Studies.studyCompletionDate," + "Studies.studyCompletionTime,Studies.studyVerifiedDate,Studies.studyVerifiedTime,Studies.accessionNumber,Studies.studyDescription," + "Studies.referringPhysiciansName,Studies.admittingDiagnosesDescription,Studies.fastestAccess, Studies.numberOfStudyRelatedSeries, Studies.numberOfStudyRelatedInstances, Studies.specificCharSet ";
    private static final String patientRootMatchPStMandatoryFieldsFromTables = " FROM Patients, Studies, Series";
    private static final String patientRootMatchPStMandatoryFieldsWhere = " WHERE Studies.deprecated=0 AND Series.deprecated=0 AND Studies.patientFK=Patients.pk AND Studies.studyinstanceuid=Series.studyFK";
    private static final String patientRootMatchPStOptionalPatientDataFields = ", PatientDemographics.ethnicGroup, PatientDemographics.patientComments, PatientDemographics.patientAddress, PatientDemographics.numberOfPatientRelatedStudies, " + "WLPatientDataPerVisit.studyFK, WLPatientDataPerVisit.patientState, WLPatientDataPerVisit.pregnancyStatus, WLPatientDataPerVisit.medicalAlerts, " + "WLPatientDataPerVisit.patientWeight, WLPatientDataPerVisit.confidentialityConstOnPatData , WLPatientDataPerVisit.specialNeeds, " + "WLPatientDataPerVisit.assignedPatientLocation ";
    private static final String patientRootMatchPStOptionalPatientDataTable = ", PatientDemographics, WLPatientDataPerVisit";
    private static final String patientRootMatchPStOptionalPatientDataWhere = " AND PatientDemographics.patientFK=WLPatientDataPerVisit.patientFK and WLPatientDataPerVisit.studyFK=Studies.studyInstanceUID  ";
    private static final String patientRootMatchPStCodesequenceFields = ", CodeSequences.codeValue, CodeSequences.codingSchemeDesignator, CodeSequences.codingSchemeVersion, CodeSequences.codeMeaning ";
    private static final String patientRootMatchPStCodesequenceTables = ", CodeSequences ";
    private static final String patientRootMatchPStCodesequenceWhere = " AND CodeSequences.pk=Studies.procedureCodeSequenceFK ";
    private static final String patientRootMatchPStSe = "SELECT Patients.pk, Patients.lastName, Patients.firstName, Patients.middleName, Patients.prefix, Patients.suffix, "
            + "Patients.birthDate, Patients.birthTime, Patients.sex, Patients.patientID, Patients.idIssuer, PatientDemographics.ethnicGroup, "
            + "PatientDemographics.patientComments, PatientDemographics.patientAddress, PatientDemographics.numberOfPatientRelatedStudies, "
            + "WLPatientDataPerVisit.studyFK, WLPatientDataPerVisit.patientState, WLPatientDataPerVisit.pregnancyStatus, "
            + "WLPatientDataPerVisit.medicalAlerts, WLPatientDataPerVisit.patientWeight, WLPatientDataPerVisit.confidentialityConstOnPatData ,"
            + " WLPatientDataPerVisit.specialNeeds, WLPatientDataPerVisit.assignedPatientLocation, "
            + "Studies.studyInstanceUID, Studies.studyID, Studies.studyStatusID, Studies.studyDate, Studies.studyTime, Studies.studyCompletionDate, "
            + "Studies.studyCompletionTime, Studies.studyVerifiedDate, Studies.studyVerifiedTime, Studies.accessionNumber, Studies.studyDescription, "
            + "Studies.referringPhysiciansName,Studies.admittingDiagnosesDescription, CodeSequences.codeValue, CodeSequences.codingSchemeDesignator, "
            + "CodeSequences.codingSchemeVersion, CodeSequences.codeMeaning, Series.seriesInstanceUID, Series.seriesNumber, Series.modality,"
            + "Series.bodyPartExamined, Series.seriesStatus, Series.numberOfSeriesRelatedInstances, Equipment.equipmentType, Equipment.manufacturer, Equipment.institutionName, Equipment.stationName, Equipment.institutionalDepartmentName, Equipment.manufacturersModelName, Equipment.deviceSerialNumber, Equipment.dateOfLastCalibration, Equipment.timeOfLastCalibration, Equipment.lastCalibratedBy, Equipment.conversionType, Equipment.secondaryCaptureDeviceID, Studies.fastestAccess, Series.seriesDescription, Studies.studyStatus, Studies.fastestAccess, " +
            "Series.operatorsName, Studies.specificCharSet  " +
            " FROM Patients, PatientDemographics, WLPatientDataPerVisit, Studies, CodeSequences, Series, Equipment WHERE Series.deprecated=0 AND Series.studyFK=Studies.studyInstanceUID AND Equipment.pk=Series.equipmentFK AND PatientDemographics.patientFK=Patients.pk AND WLPatientDataPerVisit.patientFK=Patients.pk AND Studies.patientFK=Patients.pk AND WLPatientDataPerVisit.studyFK=Studies.studyInstanceUID AND Studies.procedureCodeSequenceFK=CodeSequences.pk"; // 2098
    // //
    // Bytes!
    private static final String storageMediaBase = "SELECT StudyLocations.StudyFK, StudyLocations.physicalMediaFK, StudyLocations.furtherDeviceInfo, PhysicalMedia.Name, PhysicalMedia.urlToStudy FROM StudyLocations, PhysicalMedia WHERE StudyLocations.physicalMediaFK=PhysicalMedia.pk AND StudyLocations.StudyFK='";
    private static final String storageMediaEnd = "' ORDER BY StudyLocations.insertionDate ";

    private static final String orderSeriesEnd=" ORDER BY Series.seriesNumber ASC, Series.seriesInstanceUID ASC";
    
    final Log log = LogFactory.getLog(DicomQueryDealerBean.class);
    
    private static final int INSTANCETYPE_IMAGE=1;
    private static final int INSTANCETYPE_PRESSTATE=2;
    private static final int INSTANCETYPE_STRUCTREP=3;
    private static final int INSTANCETYPE_OVERLAY=4;
    private static final int INSTANCETYPE_NONIMAGE=5;
    
    private static final int QUERY_INST_IN_SERIESINSTANCEUID=1;
    private static final int QUERY_INST_IN_MULTIPLEINSTANCEUIDS=2;
    private static final int QUERY_INST_IN_MULTIPLESOPCLASSES=3;
    private static final int QUERY_INST_IN_SOPINSTANCEUID=4;
    private static final int QUERY_INST_IN_SOPCLASSUID=5;
    private static final int QUERY_INST_IN_INSTANCENUMBER=6;
    private static final int QUERY_INST_IN_SAMPLESPERPIXEL=7;
    private static final int QUERY_INST_IN_ROWSNUM=8;
    private static final int QUERY_INST_IN_COLUMNSNUM=9;
    private static final int QUERY_INST_IN_BITSALLOCATED=10;
    private static final int QUERY_INST_IN_BITSSTORED=11;
    private static final int QUERY_INST_IN_HIGHBIT=12;
    private static final int QUERY_INST_IN_PIXELREPRESENTATION=13;
    private static final int QUERY_INST_IN_NUMBEROFFRAMES=14;
    private static final int QUERY_INST_IN_PRESENTATIONLABEL=15;
    private static final int QUERY_INST_IN_PRESENTATIONDESCRIPTION=16;
    private static final int QUERY_INST_IN_PRESENTATIONCREATIONDATE=17;
    private static final int QUERY_INST_IN_PRESENTATIONCREATIONDATELATE=18;
    private static final int QUERY_INST_IN_PRESENTATIONCREATIONTIME=19;
    private static final int QUERY_INST_IN_PRESENTATIONCREATIONTIMELATE=20;
    private static final int QUERY_INST_IN_PRESENTATIONCREATORSNAME=21;
    private static final int QUERY_INST_IN_RECOMMENDEDVIEWINGMODE=22;
    private static final int QUERY_INST_IN_COMPLETIONFLAG=23;
    private static final int QUERY_INST_IN_VERIFICATIONFLAG=24;
    private static final int QUERY_INST_IN_CONTENTDATE=25;
    private static final int QUERY_INST_IN_CONTENTDATELATE=26;
    private static final int QUERY_INST_IN_CONTENTTIME=27;
    private static final int QUERY_INST_IN_CONTENTTIMELATE=28;
    private static final int QUERY_INST_IN_OBSERVATIONDATETIME=29;
    private static final int QUERY_INST_IN_OBSERVATIONDATETIMELATE=30;
    private static final int QUERY_INST_IN_OVERLAYNUMBER=31;
    private static final int QUERY_INST_IN_OVERLAYROWS=32;
    private static final int QUERY_INST_IN_OVERLAYCOLUMNS=33;
    private static final int QUERY_INST_IN_OVERLAYTYPE=34;
    private static final int QUERY_INST_IN_OVERLAYBITSALLOCATED=35;
    private static final int QUERY_INST_OUT_STUDYINSTANCEUID=36;
    private static final int QUERY_INST_OUT_FASTESTACCESS=37;
    private static final int QUERY_INST_OUT_STUDYSTATUS=38;
    private static final int QUERY_INST_OUT_SPECIFICCHARSET=39;
    private static final int QUERY_INST_OUT_RESULTSET=40;
    
    private static final int QUERY_INST_RES_NI_INSTANCETYPE=1;
    private static final int QUERY_INST_RES_NI_SOPINSTANCEUID=2;
    private static final int QUERY_INST_RES_NI_SOPCLASSUID=3;
    private static final int QUERY_INST_RES_NI_INSTANCENUMBER=4;
    
    private static final int QUERY_INST_RES_IM_SAMPLESPERPIXEL=5;
    private static final int QUERY_INST_RES_IM_ROWSNUM=6;
    private static final int QUERY_INST_RES_IM_COLUMNSNUM=7;
    private static final int QUERY_INST_RES_IM_BITSALLOCATED=8;
    private static final int QUERY_INST_RES_IM_BITSSTORED=9;
    private static final int QUERY_INST_RES_IM_HIGHBIT=10;
    private static final int QUERY_INST_RES_IM_PIXELREPRESENTATION=11;
    private static final int QUERY_INST_RES_IM_NUMBEROFFRAMES=12;
    
    private static final int QUERY_INST_RES_PS_PRESENTATIONLABEL=5;
    private static final int QUERY_INST_RES_PS_PRESENTATIONDESCRIPTION=6;
    private static final int QUERY_INST_RES_PS_PRESENTATIONCREATIONDATE=7;
    private static final int QUERY_INST_RES_PS_PRESENTATIONCREATIONTIME=8;
    private static final int QUERY_INST_RES_PS_PRESENTATIONCREATORSNAME=9;
    private static final int QUERY_INST_RES_PS_RECOMMENDEDVIEWINGMODE=10;
    
    private static final int QUERY_INST_RES_SR_COMPLETIONFLAG=5;
    private static final int QUERY_INST_RES_SR_VERIFICATIONFLAG=6;
    private static final int QUERY_INST_RES_SR_CONTENTDATE=7;
    private static final int QUERY_INST_RES_SR_CONTENTTIME=8;
    private static final int QUERY_INST_RES_SR_OBSERVATIONDATETIME=9;
    private static final int QUERY_INST_RES_SR_CONCEPTNAMECODESEQUENCE=10;
    private static final int QUERY_INST_RES_SR_CODEVALUE=11;
    private static final int QUERY_INST_RES_SR_CODINGSCHEMEDESIGNATOR=12;
    private static final int QUERY_INST_RES_SR_CODINGSCHEMEVERSION=13;
    private static final int QUERY_INST_RES_SR_CODEMEANING=14;
    
    private static final int QUERY_INST_RES_OV_OVERLAYNUMBER=5;
    private static final int QUERY_INST_RES_OV_OVERLAYROWS=6;
    private static final int QUERY_INST_RES_OV_OVERLAYCOLUMNS=7;
    private static final int QUERY_INST_RES_OV_OVERLAYBITSALLOCATED=8;
    private static final int QUERY_INST_RES_OV_OVERLAYTYPE=9;
    
    /**
     * Instantiates a new dicom query dealer SL bean.
     */
    public DicomQueryDealerBean() {
    }

    public void ejbRemove() {
        log.trace("Going to remove ejb");        
    }

    /*
     * (non-Javadoc)
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext sc) {
        // this.context = sc;
    }

    /**
     * Study root match at instance level.
     * 
     * @param p
     *            the patient who's searching for
     * @param st
     *            the study associated to the patient
     * @param se
     *            the series of the study
     * @param inst
     *            the instance of the series
     * @return the dicom match[]
     */
    public DicomMatch[] queryInstanceLevel(Patient p, Study st, Series se, Instance inst, String callingAE) {
        Connection con = null;
        //Statement stat = null;
        CallableStatement cs=null;
        ResultSet rs = null;
        
    	try{
            con = dataSource.getConnection();
        }catch (SQLException ex) {
            log.error(LogMessage._NoDBConnection, ex);
            return new DicomMatch[0];
        }
    	
        oracle = Dbms.isOracle(con);
        log.debug("QueryDealer: Starting a StudyRoot search at instance level");
        // I rely on having just se.getSeriesInstanceUid()!=null and inst
        if ((se == null) || (se.getSeriesInstanceUid() == null) || (inst == null)) {
            return null;
        }
        List<DicomMatch> dms = new ArrayList<DicomMatch>(11);


        Instance tIn = null;
        Timer qryCrono = new Timer();
        Timer bldCrono = new Timer();
        int queryParametersLength=QUERY_INST_OUT_STUDYINSTANCEUID;	// The size is the position of the first OUT parameter
        String[] queryParameters=new String[queryParametersLength];
        try{
            modDateFormat(con);
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
            	cs=con.prepareCall("{call queryInstanceLevel(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            	cs.registerOutParameter(QUERY_INST_OUT_RESULTSET, OracleTypes.CURSOR);
            }else{
            	cs=con.prepareCall("{call queryInstanceLevel(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            }
            bldCrono.restart();
            
            // stat.setMaxRows(0);
            String[] uidsToMatch=inst.getUidsToMatch();
            String[] sopClassesToMatch=inst.getSopClassesToMatch();
            cs.setString(QUERY_INST_IN_SERIESINSTANCEUID, se.getSeriesInstanceUid());
            queryParameters[QUERY_INST_IN_SERIESINSTANCEUID]="'"+se.getSeriesInstanceUid()+"'";
            
            if(uidsToMatch!=null){
            	StringBuilder sb=new StringBuilder();
            	for(String temp:uidsToMatch){
            		if(!DicomConstants.UNIVERSAL_MATCHING.equals(temp))
            			sb.append("'").append(temp).append("',");
            	}
            	if(sb.length()>0){
            		sb.deleteCharAt(sb.length()-1);
            		cs.setShort(QUERY_INST_IN_MULTIPLEINSTANCEUIDS, (short)1);
            		cs.setString(QUERY_INST_IN_SOPINSTANCEUID, sb.toString());
            		queryParameters[QUERY_INST_IN_MULTIPLEINSTANCEUIDS]="1";
            		queryParameters[QUERY_INST_IN_SOPINSTANCEUID]="|"+sb.toString()+"|";
            	}else{
            		cs.setShort(QUERY_INST_IN_MULTIPLEINSTANCEUIDS, (short)0);
            		cs.setNull(QUERY_INST_IN_SOPINSTANCEUID, Types.VARCHAR);
            		queryParameters[QUERY_INST_IN_MULTIPLEINSTANCEUIDS]="0";
            		queryParameters[QUERY_INST_IN_SOPINSTANCEUID]="NULL";
            	}
            }else{
            	cs.setShort(QUERY_INST_IN_MULTIPLEINSTANCEUIDS, (short)0);
            	queryParameters[QUERY_INST_IN_MULTIPLEINSTANCEUIDS]="0";
            	if(inst.getSopInstanceUid()!=null){
            		if(!DicomConstants.UNIVERSAL_MATCHING.equals(inst.getSopInstanceUid())){
            			cs.setString(QUERY_INST_IN_SOPINSTANCEUID, inst.getSopInstanceUid());
                		queryParameters[QUERY_INST_IN_SOPINSTANCEUID]="'"+inst.getSopInstanceUid()+"'";
            		}else{
            			cs.setNull(QUERY_INST_IN_SOPINSTANCEUID, Types.VARCHAR);
                		queryParameters[QUERY_INST_IN_SOPINSTANCEUID]="NULL";
            		}
            	}else{
            		cs.setNull(QUERY_INST_IN_SOPINSTANCEUID, Types.VARCHAR);
            		queryParameters[QUERY_INST_IN_SOPINSTANCEUID]="NULL";
            	}
            }
            if(sopClassesToMatch!=null){
            	StringBuilder sb=new StringBuilder();
            	for(String temp:sopClassesToMatch){
            		if(!DicomConstants.UNIVERSAL_MATCHING.equals(temp))
            			sb.append("'").append(temp).append("',");
            	}
            	if(sb.length()>0){
            		sb.deleteCharAt(sb.length()-1);
            		cs.setShort(QUERY_INST_IN_MULTIPLESOPCLASSES, (short)1);
            		cs.setString(QUERY_INST_IN_SOPCLASSUID, sb.toString());
            		queryParameters[QUERY_INST_IN_MULTIPLESOPCLASSES]="1";
            		queryParameters[QUERY_INST_IN_SOPCLASSUID]="|"+sb.toString()+"|";
            	}else{
            		cs.setShort(QUERY_INST_IN_MULTIPLESOPCLASSES, (short)0);
            		cs.setNull(QUERY_INST_IN_SOPCLASSUID, Types.VARCHAR);
            		queryParameters[QUERY_INST_IN_MULTIPLESOPCLASSES]="0";
            		queryParameters[QUERY_INST_IN_SOPCLASSUID]="NULL";
            	}
            }else{
            	cs.setShort(QUERY_INST_IN_MULTIPLESOPCLASSES, (short)0);
            	queryParameters[QUERY_INST_IN_MULTIPLESOPCLASSES]="0";
            	if(inst.getSopClassUid()!=null){
            		if(!DicomConstants.UNIVERSAL_MATCHING.equals(inst.getSopClassUid())){
            			cs.setString(QUERY_INST_IN_SOPCLASSUID, inst.getSopClassUid());	
            			queryParameters[QUERY_INST_IN_SOPCLASSUID]="'"+inst.getSopClassUid()+"'";
            		}else{
            			cs.setNull(QUERY_INST_IN_SOPCLASSUID, Types.VARCHAR);
            			queryParameters[QUERY_INST_IN_SOPCLASSUID]="NULL";
            		}
            	}else{
            		cs.setNull(QUERY_INST_IN_SOPCLASSUID, Types.VARCHAR);
            		queryParameters[QUERY_INST_IN_SOPCLASSUID]="NULL";
            	}
            }
            if(inst.getInstanceNumber()!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(inst.getInstanceNumber())){
            	cs.setLong(QUERY_INST_IN_INSTANCENUMBER, Long.parseLong(inst.getInstanceNumber()));
            	queryParameters[QUERY_INST_IN_INSTANCENUMBER]=inst.getInstanceNumber();
            }else{
            	cs.setNull(QUERY_INST_IN_INSTANCENUMBER, Types.BIGINT);
            	queryParameters[QUERY_INST_IN_INSTANCENUMBER]="NULL";
            }
            
            if(inst instanceof Image){
            	String temp=((Image)inst).getSamplesPerPixel();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setInt(QUERY_INST_IN_SAMPLESPERPIXEL, Integer.parseInt(temp));
            		queryParameters[QUERY_INST_IN_SAMPLESPERPIXEL]=temp;
            	}else{
            		cs.setNull(QUERY_INST_IN_SAMPLESPERPIXEL, Types.INTEGER);
            		queryParameters[QUERY_INST_IN_SAMPLESPERPIXEL]="NULL";
            	}
            	temp=((Image)inst).getRows();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setInt(QUERY_INST_IN_ROWSNUM, Integer.parseInt(temp));
            		queryParameters[QUERY_INST_IN_ROWSNUM]=temp;
            	}else{
            		cs.setNull(QUERY_INST_IN_ROWSNUM, Types.INTEGER);
            		queryParameters[QUERY_INST_IN_ROWSNUM]="NULL";
            	}
            	temp=((Image)inst).getColumns();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setInt(QUERY_INST_IN_COLUMNSNUM, Integer.parseInt(temp));
            		queryParameters[QUERY_INST_IN_COLUMNSNUM]=temp;
            	}else{
            		cs.setNull(QUERY_INST_IN_COLUMNSNUM, Types.INTEGER);
            		queryParameters[QUERY_INST_IN_COLUMNSNUM]="NULL";
            	}
            	temp=((Image)inst).getBitsAllocated();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setInt(QUERY_INST_IN_BITSALLOCATED, Integer.parseInt(temp));
            		queryParameters[QUERY_INST_IN_BITSALLOCATED]=temp;
            	}else{
            		cs.setNull(QUERY_INST_IN_BITSALLOCATED, Types.INTEGER);
            		queryParameters[QUERY_INST_IN_BITSALLOCATED]="NULL";
            	}
            	temp=((Image)inst).getBitsStored();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setInt(QUERY_INST_IN_BITSSTORED, Integer.parseInt(temp));
            		queryParameters[QUERY_INST_IN_BITSSTORED]=temp;
            	}else{
            		cs.setNull(QUERY_INST_IN_BITSSTORED, Types.INTEGER);
            		queryParameters[QUERY_INST_IN_BITSSTORED]="NULL";
            	}
            	temp=((Image)inst).getHighBit();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setInt(QUERY_INST_IN_HIGHBIT, Integer.parseInt(temp));
            		queryParameters[QUERY_INST_IN_HIGHBIT]=temp;
            	}else{
            		cs.setNull(QUERY_INST_IN_HIGHBIT, Types.INTEGER);
            		queryParameters[QUERY_INST_IN_HIGHBIT]="NULL";
            	}
            	temp=((Image)inst).getPixelRepresentation();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setInt(QUERY_INST_IN_PIXELREPRESENTATION, Integer.parseInt(temp));
            		queryParameters[QUERY_INST_IN_PIXELREPRESENTATION]=temp;
            	}else{
            		cs.setNull(QUERY_INST_IN_PIXELREPRESENTATION, Types.INTEGER);
            		queryParameters[QUERY_INST_IN_PIXELREPRESENTATION]="NULL";
            	}
            	if(((Image)inst).getNumberOfFrames()!=null){
            		cs.setShort(QUERY_INST_IN_NUMBEROFFRAMES, (short)1);
            		queryParameters[QUERY_INST_IN_NUMBEROFFRAMES]="1";
            	}else{
            		cs.setNull(QUERY_INST_IN_NUMBEROFFRAMES, Types.SMALLINT);
            		queryParameters[QUERY_INST_IN_NUMBEROFFRAMES]="NULL";
            	}
            }else{
            	cs.setNull(QUERY_INST_IN_SAMPLESPERPIXEL, Types.INTEGER);
            	cs.setNull(QUERY_INST_IN_ROWSNUM, Types.INTEGER);
            	cs.setNull(QUERY_INST_IN_COLUMNSNUM, Types.INTEGER);
            	cs.setNull(QUERY_INST_IN_BITSALLOCATED, Types.INTEGER);
            	cs.setNull(QUERY_INST_IN_BITSSTORED, Types.INTEGER);
            	cs.setNull(QUERY_INST_IN_HIGHBIT, Types.INTEGER);
            	cs.setNull(QUERY_INST_IN_PIXELREPRESENTATION, Types.INTEGER);
            	cs.setNull(QUERY_INST_IN_NUMBEROFFRAMES, Types.INTEGER);
            	queryParameters[QUERY_INST_IN_SAMPLESPERPIXEL]="NULL";
            	queryParameters[QUERY_INST_IN_ROWSNUM]="NULL";
            	queryParameters[QUERY_INST_IN_COLUMNSNUM]="NULL";
            	queryParameters[QUERY_INST_IN_BITSALLOCATED]="NULL";
            	queryParameters[QUERY_INST_IN_BITSSTORED]="NULL";
            	queryParameters[QUERY_INST_IN_HIGHBIT]="NULL";
            	queryParameters[QUERY_INST_IN_PIXELREPRESENTATION]="NULL";
            	queryParameters[QUERY_INST_IN_NUMBEROFFRAMES]="NULL";
            }
            
            if(inst instanceof PresState){
            	String temp=((PresState)inst).getPresentationLabel();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setString(QUERY_INST_IN_PRESENTATIONLABEL, temp);
                	queryParameters[QUERY_INST_IN_PRESENTATIONLABEL]="'"+temp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_PRESENTATIONLABEL, Types.VARCHAR);
                	queryParameters[QUERY_INST_IN_PRESENTATIONLABEL]="NULL";
            	}
            	temp=((PresState)inst).getPresentationDescription();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setString(QUERY_INST_IN_PRESENTATIONDESCRIPTION, temp);
            		queryParameters[QUERY_INST_IN_PRESENTATIONDESCRIPTION]="'"+temp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_PRESENTATIONDESCRIPTION, Types.VARCHAR);
            		queryParameters[QUERY_INST_IN_PRESENTATIONDESCRIPTION]="NULL";
            	}
            	Date tempDate=((PresState)inst).getPresentationCreationDate();
            	if(tempDate!=null){
            		cs.setDate(QUERY_INST_IN_PRESENTATIONCREATIONDATE, tempDate);
            		queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONDATE]="'"+tempDate+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_PRESENTATIONCREATIONDATE, Types.DATE);
            		queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONDATE]="NULL";
            	}
            	tempDate=((PresState)inst).getPresentationCreationDateLate();
            	if(tempDate!=null){
            		cs.setDate(QUERY_INST_IN_PRESENTATIONCREATIONDATELATE, tempDate);
            		queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONDATELATE]="'"+tempDate+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_PRESENTATIONCREATIONDATELATE, Types.DATE);
            		queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONDATELATE]="NULL";
            	}
            	Time tempTime=((PresState)inst).getPresentationCreationTime();
            	if(tempTime!=null){
            		cs.setTime(QUERY_INST_IN_PRESENTATIONCREATIONTIME, tempTime);
            		queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONTIME]="'"+tempTime+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_PRESENTATIONCREATIONTIME, Types.TIME);
            		queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONTIME]="NULL";
            	}
            	tempTime=((PresState)inst).getPresentationCreationTimeLate();
            	if(tempTime!=null){
            		cs.setTime(QUERY_INST_IN_PRESENTATIONCREATIONTIMELATE, tempTime);
            		queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONTIMELATE]="'"+tempTime+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_PRESENTATIONCREATIONTIMELATE, Types.TIME);
            		queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONTIMELATE]="NULL";
            	}
            	
            	temp=((PresState)inst).getPresentationCreatorsName();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setString(QUERY_INST_IN_PRESENTATIONCREATORSNAME, temp);
            		queryParameters[QUERY_INST_IN_PRESENTATIONCREATORSNAME]="'"+temp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_PRESENTATIONCREATORSNAME, Types.VARCHAR);
            		queryParameters[QUERY_INST_IN_PRESENTATIONCREATORSNAME]="NULL";
            	}
            	temp=((PresState)inst).getRecommendedViewingMode();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setString(QUERY_INST_IN_RECOMMENDEDVIEWINGMODE, temp);
            		queryParameters[QUERY_INST_IN_RECOMMENDEDVIEWINGMODE]="'"+temp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_RECOMMENDEDVIEWINGMODE, Types.VARCHAR);
            		queryParameters[QUERY_INST_IN_RECOMMENDEDVIEWINGMODE]="NULL";
            	}
            }else{
            	cs.setNull(QUERY_INST_IN_PRESENTATIONLABEL, Types.VARCHAR);
            	cs.setNull(QUERY_INST_IN_PRESENTATIONDESCRIPTION, Types.VARCHAR);
            	cs.setNull(QUERY_INST_IN_PRESENTATIONCREATIONDATE, Types.DATE);
            	cs.setNull(QUERY_INST_IN_PRESENTATIONCREATIONDATELATE, Types.DATE);
            	cs.setNull(QUERY_INST_IN_PRESENTATIONCREATIONTIME, Types.DATE);
            	cs.setNull(QUERY_INST_IN_PRESENTATIONCREATIONTIMELATE, Types.DATE);
            	cs.setNull(QUERY_INST_IN_PRESENTATIONCREATORSNAME, Types.VARCHAR);
            	cs.setNull(QUERY_INST_IN_RECOMMENDEDVIEWINGMODE, Types.VARCHAR);
            	queryParameters[QUERY_INST_IN_PRESENTATIONLABEL]="NULL";
            	queryParameters[QUERY_INST_IN_PRESENTATIONDESCRIPTION]="NULL";
            	queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONDATE]="NULL";
            	queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONDATELATE]="NULL";
            	queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONTIME]="NULL";
            	queryParameters[QUERY_INST_IN_PRESENTATIONCREATIONTIMELATE]="NULL";
            	queryParameters[QUERY_INST_IN_PRESENTATIONCREATORSNAME]="NULL";
            	queryParameters[QUERY_INST_IN_RECOMMENDEDVIEWINGMODE]="NULL";
            }
            
            if(inst instanceof StructRep){
            	String temp=((StructRep)inst).getCompletionFlag();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setString(QUERY_INST_IN_COMPLETIONFLAG, temp);
            		queryParameters[QUERY_INST_IN_COMPLETIONFLAG]="'"+temp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_COMPLETIONFLAG, Types.VARCHAR);
            		queryParameters[QUERY_INST_IN_COMPLETIONFLAG]="NULL";
            	}
            	temp=((StructRep)inst).getVerificationFlag();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setString(QUERY_INST_IN_VERIFICATIONFLAG, temp);
            		queryParameters[QUERY_INST_IN_VERIFICATIONFLAG]="'"+temp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_VERIFICATIONFLAG, Types.VARCHAR);
            		queryParameters[QUERY_INST_IN_VERIFICATIONFLAG]="NULL";
            	}
            	
            	Date tempDate=((StructRep)inst).getContentDate();
            	if(tempDate!=null){
            		cs.setDate(QUERY_INST_IN_CONTENTDATE, tempDate);
            		queryParameters[QUERY_INST_IN_CONTENTDATE]="'"+temp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_CONTENTDATE, Types.DATE);
            		queryParameters[QUERY_INST_IN_CONTENTDATE]="NULL";
            	}
            	tempDate=((StructRep)inst).getContentDateLate();
            	if(tempDate!=null){
            		cs.setDate(QUERY_INST_IN_CONTENTDATELATE, tempDate);
            		queryParameters[QUERY_INST_IN_CONTENTDATELATE]="'"+temp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_CONTENTDATELATE, Types.DATE);
            		queryParameters[QUERY_INST_IN_CONTENTDATELATE]="NULL";
            	}
            	Time tempTime=((StructRep)inst).getContentTime();
            	if(tempTime!=null){
            		cs.setTime(QUERY_INST_IN_CONTENTTIME, tempTime);
            		queryParameters[QUERY_INST_IN_CONTENTTIME]="'"+temp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_CONTENTTIME, Types.TIME);
            		queryParameters[QUERY_INST_IN_CONTENTTIME]="NULL";
            	}
            	tempTime=((StructRep)inst).getContentTimeLate();
            	if(tempTime!=null){
            		cs.setTime(QUERY_INST_IN_CONTENTTIMELATE, tempTime);
            		queryParameters[QUERY_INST_IN_CONTENTTIMELATE]="'"+temp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_CONTENTTIMELATE, Types.TIME);
            		queryParameters[QUERY_INST_IN_CONTENTTIMELATE]="NULL";
            	}
            	Timestamp tempTimestamp=((StructRep)inst).getObservationDateTime();
            	if(tempTimestamp!=null){
            		cs.setTimestamp(QUERY_INST_IN_OBSERVATIONDATETIME, tempTimestamp);
            		queryParameters[QUERY_INST_IN_OBSERVATIONDATETIME]="'"+tempTimestamp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_OBSERVATIONDATETIME, Types.TIMESTAMP);
            		queryParameters[QUERY_INST_IN_OBSERVATIONDATETIME]="NULL";
            	}
            	tempTimestamp=((StructRep)inst).getObservationDateTimeLate();
            	if(tempTimestamp!=null){
            		cs.setTimestamp(QUERY_INST_IN_OBSERVATIONDATETIMELATE, tempTimestamp);
            		queryParameters[QUERY_INST_IN_OBSERVATIONDATETIMELATE]="'"+tempTimestamp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_OBSERVATIONDATETIMELATE, Types.TIMESTAMP);
            		queryParameters[QUERY_INST_IN_OBSERVATIONDATETIME]="NULL";
            	}
            	
            }else{
            	cs.setNull(QUERY_INST_IN_COMPLETIONFLAG, Types.VARCHAR);
            	cs.setNull(QUERY_INST_IN_VERIFICATIONFLAG, Types.VARCHAR);
            	cs.setNull(QUERY_INST_IN_CONTENTDATE, Types.DATE);
            	cs.setNull(QUERY_INST_IN_CONTENTDATELATE, Types.DATE);
            	cs.setNull(QUERY_INST_IN_CONTENTTIME, Types.DATE);
            	cs.setNull(QUERY_INST_IN_CONTENTTIMELATE, Types.DATE);
            	cs.setNull(QUERY_INST_IN_OBSERVATIONDATETIME, Types.DATE);
            	cs.setNull(QUERY_INST_IN_OBSERVATIONDATETIMELATE, Types.DATE);
            	queryParameters[QUERY_INST_IN_COMPLETIONFLAG]="NULL";
            	queryParameters[QUERY_INST_IN_VERIFICATIONFLAG]="NULL";
            	queryParameters[QUERY_INST_IN_CONTENTDATE]="NULL";
            	queryParameters[QUERY_INST_IN_CONTENTDATELATE]="NULL";
            	queryParameters[QUERY_INST_IN_CONTENTTIME]="NULL";
            	queryParameters[QUERY_INST_IN_CONTENTTIMELATE]="NULL";
            	queryParameters[QUERY_INST_IN_OBSERVATIONDATETIME]="NULL";
            	queryParameters[QUERY_INST_IN_OBSERVATIONDATETIMELATE]="NULL";
            }
            
            if(inst instanceof Overlay){
            	String temp=((Overlay)inst).getOverlayNumber();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setLong(QUERY_INST_IN_OVERLAYNUMBER, Long.parseLong(temp));
            		queryParameters[QUERY_INST_IN_OVERLAYNUMBER]="'"+temp+"'";
            	}else{
            		cs.setNull(QUERY_INST_IN_OVERLAYNUMBER, Types.BIGINT);
            		queryParameters[QUERY_INST_IN_OVERLAYNUMBER]="NULL";
            	}
            	temp=((Overlay)inst).getOverlayRows();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setInt(QUERY_INST_IN_OVERLAYROWS, Integer.parseInt(temp));
            		queryParameters[QUERY_INST_IN_OVERLAYROWS]=temp;
            	}else{
            		cs.setNull(QUERY_INST_IN_OVERLAYROWS, Types.INTEGER);
            		queryParameters[QUERY_INST_IN_OVERLAYROWS]="NULL";
            	}
            	temp=((Overlay)inst).getOverlayColumns();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setInt(QUERY_INST_IN_OVERLAYCOLUMNS, Integer.parseInt(temp));
            		queryParameters[QUERY_INST_IN_OVERLAYCOLUMNS]=temp;
            	}else{
            		cs.setNull(QUERY_INST_IN_OVERLAYCOLUMNS, Types.INTEGER);
            		queryParameters[QUERY_INST_IN_OVERLAYCOLUMNS]="NULL";
            	}
            	temp=((Overlay)inst).getOverlayType();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setString(QUERY_INST_IN_OVERLAYTYPE, temp);
            		queryParameters[QUERY_INST_IN_OVERLAYTYPE]=temp;
            	}else{
            		cs.setNull(QUERY_INST_IN_OVERLAYTYPE, Types.VARCHAR);
            		queryParameters[QUERY_INST_IN_OVERLAYTYPE]="NULL";
            	}
            	temp=((Overlay)inst).getOverlayBitsAllocated();
            	if(temp!=null && !DicomConstants.UNIVERSAL_MATCHING.equals(temp)){
            		cs.setInt(QUERY_INST_IN_OVERLAYBITSALLOCATED, Integer.parseInt(temp));
            		queryParameters[QUERY_INST_IN_OVERLAYBITSALLOCATED]=temp;
            	}else{
            		cs.setNull(QUERY_INST_IN_OVERLAYBITSALLOCATED, Types.INTEGER);
            		queryParameters[QUERY_INST_IN_OVERLAYBITSALLOCATED]="NULL";
            	}
            }else{
            	cs.setNull(QUERY_INST_IN_OVERLAYNUMBER, Types.BIGINT);
            	cs.setNull(QUERY_INST_IN_OVERLAYROWS, Types.INTEGER);
            	cs.setNull(QUERY_INST_IN_OVERLAYCOLUMNS, Types.INTEGER);
            	cs.setNull(QUERY_INST_IN_OVERLAYTYPE, Types.VARCHAR);
            	cs.setNull(QUERY_INST_IN_OVERLAYBITSALLOCATED, Types.INTEGER);
            	queryParameters[QUERY_INST_IN_OVERLAYNUMBER]="NULL";
            	queryParameters[QUERY_INST_IN_OVERLAYROWS]="NULL";
            	queryParameters[QUERY_INST_IN_OVERLAYCOLUMNS]="NULL";
            	queryParameters[QUERY_INST_IN_OVERLAYTYPE]="NULL";
            	queryParameters[QUERY_INST_IN_OVERLAYBITSALLOCATED]="NULL";
            }
            
            cs.registerOutParameter(QUERY_INST_OUT_STUDYINSTANCEUID, Types.VARCHAR);
            cs.registerOutParameter(QUERY_INST_OUT_FASTESTACCESS, Types.VARCHAR);
            cs.registerOutParameter(QUERY_INST_OUT_STUDYSTATUS, Types.VARCHAR);
            cs.registerOutParameter(QUERY_INST_OUT_SPECIFICCHARSET, Types.VARCHAR);
                
////////////////////////////////////////////////////////////////
                qryCrono.restart();
                // log.debug(callingAE + ": exec this " + qry.substring(qry.indexOf("WHERE")));
                qryCrono.stop();
                cs.execute();
                try {
                    if (isOracle) {
                        rs = (ResultSet) cs.getObject(QUERY_INST_OUT_RESULTSET);
                    } else {
                        rs = cs.getResultSet();
                    }
                } catch (SQLException sex) {} // This is because the cursor/resultset could not be open 
                if (rs != null) {
                	DicomMatch dm=null;
	                while (rs.next()) {
	                    dm = new DicomMatch();
	                    Instance tempIn = null;
	                    switch(rs.getInt(QUERY_INST_RES_NI_INSTANCETYPE)){
		                    case INSTANCETYPE_IMAGE:
		                    	tempIn = new Image();
		                        ((Image) tempIn).setSamplesPerPixel(rs.getString(QUERY_INST_RES_IM_SAMPLESPERPIXEL));
		                        ((Image) tempIn).setRows(rs.getString(QUERY_INST_RES_IM_ROWSNUM));
		                        ((Image) tempIn).setColumns(rs.getString(QUERY_INST_RES_IM_COLUMNSNUM));
		                        ((Image) tempIn).setBitsAllocated(rs.getString(QUERY_INST_RES_IM_BITSALLOCATED));
		                        ((Image) tempIn).setBitsStored(rs.getString(QUERY_INST_RES_IM_BITSSTORED));
		                        ((Image) tempIn).setHighBit(rs.getString(QUERY_INST_RES_IM_HIGHBIT));
		                        ((Image) tempIn).setPixelRepresentation(rs.getString(QUERY_INST_RES_IM_PIXELREPRESENTATION));
		                        int num=rs.getInt(QUERY_INST_RES_IM_NUMBEROFFRAMES);
	                        	((Image) tempIn).setNumberOfFrames(rs.wasNull()?null:num);
		                    	break;
		                    case INSTANCETYPE_PRESSTATE:
		                    	tempIn = new PresState();
		                        ((PresState) tempIn).setPresentationLabel(rs.getString(QUERY_INST_RES_PS_PRESENTATIONLABEL));
		                        ((PresState) tempIn).setPresentationDescription(rs.getString(QUERY_INST_RES_PS_PRESENTATIONDESCRIPTION));
		                        ((PresState) tempIn).setPresentationCreationDate(rs.getDate(QUERY_INST_RES_PS_PRESENTATIONCREATIONDATE));
		                        ((PresState) tempIn).setPresentationCreationTime(rs.getTime(QUERY_INST_RES_PS_PRESENTATIONCREATIONTIME));
		                        ((PresState) tempIn).setPresentationCreatorsName(rs.getString(QUERY_INST_RES_PS_PRESENTATIONCREATORSNAME));
		                        ((PresState) tempIn).setRecommendedViewingMode(rs.getString(QUERY_INST_RES_PS_RECOMMENDEDVIEWINGMODE));
		                    	break;
		                    case INSTANCETYPE_STRUCTREP:
		                    	tempIn = new StructRep();
		                        ((StructRep) tempIn).setCompletionFlag(rs.getString(QUERY_INST_RES_SR_COMPLETIONFLAG));
		                        ((StructRep) tempIn).setVerificationFlag(rs.getString(QUERY_INST_RES_SR_VERIFICATIONFLAG));
		                        ((StructRep) tempIn).setContentDate(rs.getDate(QUERY_INST_RES_SR_CONTENTDATE));
		                        ((StructRep) tempIn).setContentTime(rs.getTime(QUERY_INST_RES_SR_CONTENTTIME));
		                        ((StructRep) tempIn).setObservationDateTime(rs.getTimestamp(QUERY_INST_RES_SR_OBSERVATIONDATETIME));
		                        ((StructRep) tempIn).setConceptNameCodeSequence(new CodeSequence(rs.getString(QUERY_INST_RES_SR_CODEVALUE), rs.getString(QUERY_INST_RES_SR_CODINGSCHEMEDESIGNATOR), rs.getString(QUERY_INST_RES_SR_CODINGSCHEMEVERSION), rs.getString(QUERY_INST_RES_SR_CODEMEANING)));
		                    	break;
		                    case INSTANCETYPE_OVERLAY:
		                    	tempIn = new Overlay();
		                        ((Overlay) tempIn).setOverlayNumber(rs.getString(QUERY_INST_RES_OV_OVERLAYNUMBER));
		                        ((Overlay) tempIn).setOverlayRows(rs.getString(QUERY_INST_RES_OV_OVERLAYROWS));
		                        ((Overlay) tempIn).setOverlayColumns(rs.getString(QUERY_INST_RES_OV_OVERLAYCOLUMNS));
		                        ((Overlay) tempIn).setOverlayBitsAllocated(rs.getString(QUERY_INST_RES_OV_OVERLAYBITSALLOCATED));
		                        ((Overlay) tempIn).setOverlayType(rs.getString(QUERY_INST_RES_OV_OVERLAYTYPE));
		                    	break;
	                    	default:
	                    		tempIn=new NonImage();
	                    		break;
	                    }
	                    tempIn.setSopInstanceUid(rs.getString(QUERY_INST_RES_NI_SOPINSTANCEUID));
                		tempIn.setSopClassUid(rs.getString(QUERY_INST_RES_NI_SOPCLASSUID));
                		tempIn.setInstanceNumber(rs.getString(QUERY_INST_RES_NI_INSTANCENUMBER));
                		
	                    dm.instance = tempIn;
	                    dms.add(dm);
	                    // cleaning
	                    dm = null;
	                    tempIn = null;
	                } // end while
                }
                Study tempSt = new Study(cs.getString(QUERY_INST_OUT_STUDYINSTANCEUID));
                tempSt.setFastestAccess(cs.getString(QUERY_INST_OUT_FASTESTACCESS));
                tempSt.setStudyStatus(cs.getString(QUERY_INST_OUT_STUDYSTATUS));
                tempSt.setSpecificCharacterSet(cs.getString(QUERY_INST_OUT_SPECIFICCHARSET));
                Series tempSe = new Series(se.getSeriesInstanceUid());
                for(DicomMatch match: dms){
                	match.study=tempSt;
                	match.series=tempSe;
                }
                
                log.trace("size:" + dms.size());
                
            // if(readRows==0){
            log.trace("About to add a NULL dataset");
            dms.add(null); // If less rows than the maximum have been read, no
            // more results can be found by another query: add a
            // null DicomMatch to notify no more calls are
            // needed!!!
            // }
        }catch (SQLException sex){
            log.error("QueryDealer: An exception occurred when looking for matches: ", sex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(cs);
        	CloseableUtils.close(con);
        }
        bldCrono.stop();
        StringBuilder queryString=new StringBuilder();
        for(int i=1; i<queryParametersLength; i++){
        	queryString.append(queryParameters[i]).append(',');
        }
        tLogger.toLog(new String[] { tLogger.QUERY_STU_INS, callingAE, qryCrono.toString(), bldCrono.toString(), queryString.toString(), dms.size() + "" });
        DicomMatch[] tempAr = new DicomMatch[dms.size()];
        dms.toArray(tempAr);
        dms = null;
        return tempAr; // I need to cast it to DicomMatch[]!!!
    }

    /**
     * Study root match at series level.
     * 
     * @param p
     *            the p
     * @param st
     *            the st
     * @param se
     *            the se
     * @return the dicom match[]
     */
    public DicomMatch[] studyRootMatch(Patient p, Study st, Series se, String callingAE) {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        
    	Timer qryTimer = new Timer();
        Timer bldTimer = new Timer();
        try{
            con = dataSource.getConnection();
        }catch (SQLException ex){
            log.error(callingAE + ": " + LogMessage._NoDBConnection, ex);
            return new DicomMatch[0];
        }
        oracle = Dbms.isOracle(con);
        
        log.debug(callingAE + ": Starting a StudyRoot search at series level");
        /*
         * Sequence Matching Not Supported, as well as namesOfPhysiciansReadingStudy!!!!
         */
        List<DicomMatch> dms = new ArrayList<DicomMatch>(11); // To store all the found matches!!!
        String qry = "";
        try{
            modDateFormat(con);
            stat = con.createStatement();
            // stat.setMaxRows(0);
            qry = preparePRPStSe(p, st, se);
            qryTimer.restart();
            log.debug(callingAE + ": exec this " + qry.substring(qry.lastIndexOf("WHERE")));
            rs = stat.executeQuery(qry);
            qryTimer.stop();
            log.debug(callingAE + ": in " + qryTimer.getMeasure() + "ms.");
            bldTimer.restart();
            while (rs.next()) { // Now process the results:
                DicomMatch dm = new DicomMatch();
                Patient tempPat = new Patient();
                Study tempSt = new Study("");
                Series tempSe = new Series();
                tempPat.setPrimaryKey(rs.getString(1));
                tempPat.setLastName(rs.getString(2));
                tempPat.setFirstName(rs.getString(3));
                tempPat.setMiddleName(rs.getString(4));
                tempPat.setPrefix(rs.getString(5));
                tempPat.setSuffix(rs.getString(6));
                tempPat.setBirthDate(rs.getDate(7));
                tempPat.setBirthTime(rs.getTime(8));
                tempPat.setSex(rs.getString(9));
                tempPat.setPatientId(rs.getString(10));
                tempPat.setIdIssuer(rs.getString(11));
                tempPat.setEthnicGroup(rs.getString(12));
                tempPat.setPatientComments(rs.getString(13));
                tempPat.setPatientAddress(rs.getString(14));
                tempPat.setNumberOfPatientRelatedStudies(rs.getString(15));
                tempPat.setPatientState(rs.getString(17)); // 16 is WLPatientDataPerVisit.studyFK
                tempPat.setPregnancyStatus(rs.getString(18));
                tempPat.setMedicalAlerts(rs.getString(19));
                tempPat.setPatientWeight(rs.getString(20));
                tempPat.setConfidentialityConstraint(rs.getString(21));
                tempPat.setSpecialNeeds(rs.getString(22));
                tempPat.setAssignedPatientLocation(rs.getString(23));
                tempSt.setStudyInstanceUid(rs.getString(24));
                tempSt.setStudyId(rs.getString(25));
                tempSt.setStudyStatusId(rs.getString(26));
                tempSt.setStudyDate(rs.getDate(27));
                tempSt.setStudyTime(rs.getTime(28));
                tempSt.setStudyCompletionDate(rs.getDate(29));
                tempSt.setStudyCompletionTime(rs.getTime(30));
                tempSt.setStudyVerifiedDate(rs.getDate(31));
                tempSt.setStudyVerifiedTime(rs.getTime(32));
                tempSt.setStudyStatus(rs.getString("studyStatus"));
                tempSt.setFastestAccess(rs.getString("fastestAccess"));
                tempSt.setAccessionNumber(rs.getString(33));
                tempSt.setStudyDescription(rs.getString(34));
                tempSt.setReferringPhysiciansName(rs.getString(35));
                tempSt.setAdmittingDiagnosesDescription(rs.getString(36));
                tempSt.setProcedureCodeSequence(new CodeSequence(rs.getString(37), rs.getString(38), rs.getString(39), rs.getString(40)));
                tempSe.setSeriesInstanceUid(rs.getString(41));
                tempSe.setSeriesNumber(rs.getString(42));
                tempSe.setModality(rs.getString(43));
                tempSe.setBodyPartExamined(rs.getString(44));
                tempSe.setSeriesStatus(rs.getString(45));
                tempSe.setNumberOfSeriesRelatedInstances(rs.getString(46));
                Equipment te = new Equipment();
                te.setEquipmentType(rs.getString(47));
                te.setManufacturer(rs.getString(48));
                te.setInstitutionName(rs.getString(49));
                te.setStationName(rs.getString(50));
                te.setInstitutionalDepartmentName(rs.getString(51));
                te.setManufacturersModelName(rs.getString(52));
                te.setDeviceSerialNumber(rs.getString(53));
                te.setDateOfLastCalibration(rs.getDate(54));
                te.setTimeOfLastCalibration(rs.getTime(55));
                te.setLastCalibratedBy(rs.getString(56));
                te.setConversionType(rs.getString(57));
                te.setSecondaryCaptureDeviceId(rs.getString(58));
                tempSe.setEquipment(te);
                tempSe.setSeriesDescription(rs.getString(60));
                tempSe.setOperatorsName(rs.getString(63));
                tempSt.setSpecificCharacterSet(rs.getString(64));
                // --------------------------------------
                // tempSt.setOnline(!rs.wasNull());
                dm.patient = tempPat;
                dm.study = tempSt;
                dm.series = tempSe;
                dms.add(dm);
                // cleaning
                dm = null;
                tempPat = null;
                tempSt = null;
                tempSe = null;
            }
            
            // Now I have to process each study anyway, to get the physicians
            // reading the study: I get the number of Study related series and
            // instances as well:
            DicomMatch temp = null;
            for (int i = dms.size() - 1; i >= 0; i--) {
                temp = dms.remove(i);
                if (temp == null)
                    continue;
                PersonalName[] res = getPhysReadingStudy(temp.study.getStudyInstanceUid());
                if (res != null)
                    for (int j = res.length - 1; j >= 0; j--)
                        temp.study.addNameOfPhysicianReadingStudy(res[j]);
                if (st.getNumberOfStudyRelatedSeries() != null) {
                    long n = findNumberOfStudyRelatedSeries(temp.study.getStudyInstanceUid());
                    // log.debug("#StudyRelInst "+n);
                    temp.study.setNumberOfStudyRelatedSeries(Long.toString(n));
                } // end if #StudyRelSeries
                if (st.getNumberOfStudyRelatedSeries() != null) {
                    long n = findNumberOfStudyRelatedInstances(temp.study.getStudyInstanceUid());
                    // log.debug("#StudyRelSer "+n);
                    temp.study.setNumberOfStudyRelatedInstances(Long.toString(n));
                } // end if #StudyRelInstances
                dms.add(i, temp);
                // cleaning
                temp = null;
            }
            if (p != null)
                if (p.getNumberOfPatientRelatedSeries() != null) {
                    long n = 0;
                    for (int i = dms.size() - 1; i >= 0; i--) {
                        temp = dms.remove(i);
                        if (temp == null)
                            continue;
                        n = findNumberOfPatientRelatedSeries(Long.parseLong(temp.patient.getPrimaryKey()));
                        temp.patient.setNumberOfPatientRelatedSeries(Long.toString(n));
                        dms.add(i, temp);
                        temp = null;
                    } // end for
                } // end if
            if (p != null)
                if (p.getNumberOfPatientRelatedInstances() != null) {
                    long n = 0;
                    for (int i = dms.size() - 1; i >= 0; i--) {
                        temp = dms.remove(i);
                        if (temp == null)
                            continue;
                        n = findNumberOfPatientRelatedInstances(Long.parseLong(temp.patient.getPrimaryKey()));
                        temp.patient.setNumberOfPatientRelatedInstances(Long.toString(n));
                        dms.add(i, temp);
                        temp = null;
                    } // end for
                } // end if
            log.trace("About to add a NULL dataset");
            dms.add(null);
        } catch (SQLException sex) {
            log.error(callingAE + ":An exception occurred when looking for matches: ", sex);
        } catch (Exception ex) {
            log.error(callingAE + ":An exception occurred when looking for matches: ", ex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        }
        bldTimer.stop();
        tLogger.toLog(new String[] { tLogger.QUERY_STU_SER, callingAE, qryTimer.toString(), bldTimer.toString(), qry.substring(qry.lastIndexOf("WHERE")), dms.size() + "" });
        DicomMatch[] tempAr = new DicomMatch[dms.size()];
        dms.toArray(tempAr);
        // cleaning
        dms = null;
        return tempAr; // I need to cast it to DicomMatch[]!!!
    }

    /**
     * Study root match a study level.
     * 
     * @param p
     *            the p
     * @param st
     *            the st
     * @param limit
     *            the limit
     * @param callingAETitle
     *            the calling AE title
     * @param depEnabled
     *            the dep enabled
     * @return the dicom match[]
     */
    public DicomMatch[] studyRootMatch(Patient p, Study st, int limit, String callingAE, boolean depEnabled) {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        
    	Timer qryCrono = new Timer();
        Timer bldCrono = new Timer();
        try{
            con = dataSource.getConnection();
        }catch (SQLException ex) {
            log.error(callingAE + ": " + LogMessage._NoDBConnection, ex);
            return new DicomMatch[0];
        }
        oracle = Dbms.isOracle(con);
        
        log.info(callingAE + ": Starting a StudyRoot search at study level");
        /*
         * Sequence Matching Not Supported, as well as namesOfPhysiciansReadingStudy!!!!
         */
        List<DicomMatch> dms = new ArrayList<DicomMatch>((limit == 0) ? 17 : limit); // To store all the found matches!!!
        // long startingPoint = System.currentTimeMillis();// to perfomance
        String query = "";
        try{
        	bldCrono.restart();
            modDateFormat(con);
            stat = con.createStatement();
            stat.setMaxRows(limit);
            // -------------------------------------primo giro mandatory
            // control if the query is acceptable
            // query,if optional, codesequence
            query = preparePRPSt(p, st, callingAE, depEnabled, limit);
            boolean optional = query.contains("WLpatientpervisit");
            boolean codesequences = query.contains("CodeSequences");
            // exit if query aims to return all the content of pacs
            // if ((!query.contains("(Patients.lastName)")) && (!query.contains("(Patients.firstName)")) && (!query.contains("Studies.studyDate<")) && (!query.contains("(Patients.patientID)")) && (!query.contains("(accessionNumber)")) && (!query.contains("Studies.studyInstanceUID='"))) {
            // log.warn(callingAE + ": query not Acceptable, too weak filter");
            // throw new SQLException("Query too long");
            // }
            // log.debug(callingAE + ": exec this " + query.substring(query.lastIndexOf("WHERE")));
            // show only contraint
            // .substring(query.lastIndexOf("WHERE"))
            qryCrono.restart();
            log.info(" studyRootMatch metod query ===> "+query);
            rs = stat.executeQuery(query);
            qryCrono.stop();
            // log.debug(callingAE + ": in " + qryCrono.getMeasure() + "ms.");
            int readRows = 0;
            
            while (rs.next()) {
                // ogni riga dell'rs contiene la coppia /paziente/studio
                readRows++; // You're reading one row!
                DicomMatch dm = new DicomMatch();
                Patient tempPat = new Patient();
                Study tempSt = new Study("");
                // setting the fields in the returning objects....
                // /////////////////////////////////////////////////////////////////////////////Table patient
                tempPat.setPrimaryKey(rs.getString(1));
                tempPat.setLastName(rs.getString(2));
                tempPat.setFirstName(rs.getString(3));
                tempPat.setMiddleName(rs.getString(4));
                tempPat.setPrefix(rs.getString(5));
                tempPat.setSuffix(rs.getString(6));
                tempPat.setBirthDate(rs.getDate(7));
                tempPat.setBirthTime(rs.getTime(8));
                tempPat.setSex(rs.getString(9));
                tempPat.setPatientId(rs.getString(10));
                tempPat.setIdIssuer(rs.getString(11));
                // //////////////////////////////////////////////////////////////////////////////////////////
                tempSt.setStudyInstanceUid(rs.getString(13));
                tempSt.setStudyId(rs.getString(14));
                tempSt.setStudyStatusId(rs.getString(15));
                tempSt.setStudyStatus(rs.getString(12));
                tempSt.setStudyDate(rs.getDate(16));
                tempSt.setStudyTime(rs.getTime(17));
                tempSt.setStudyCompletionDate(rs.getDate(18));
                tempSt.setStudyCompletionTime(rs.getTime(19));
                tempSt.setStudyVerifiedDate(rs.getDate(20));
                tempSt.setStudyVerifiedTime(rs.getTime(21));
                tempSt.setAccessionNumber(rs.getString(22));
                tempSt.setStudyDescription(rs.getString(23));
                tempSt.setReferringPhysiciansName(rs.getString(24));
                tempSt.setAdmittingDiagnosesDescription(rs.getString(25));
                tempSt.setNumberOfStudyRelatedSeries(rs.getString(27));
                tempSt.setNumberOfStudyRelatedInstances(rs.getString(28));
                tempSt.setFastestAccess(rs.getString(26));
                tempSt.setSpecificCharacterSet(rs.getString(29));
                if (optional) {
                    tempPat.setEthnicGroup(rs.getString("ethnicGroup"));
                    tempPat.setPatientComments(rs.getString("patientComments"));
                    tempPat.setPatientAddress(rs.getString("patientAddress"));
                    tempPat.setNumberOfPatientRelatedStudies(rs.getString("numberOfPatientRelatedStudies"));
                    // ////////////////end patient demographics////////////////////////////////////////////////////
                    tempPat.setPatientState(rs.getString("patientState")); // 16
                    // is
                    // WLPatientDataPerVisit.studyFK
                    tempPat.setPregnancyStatus(rs.getString("pregnancyStatus"));
                    tempPat.setMedicalAlerts(rs.getString("medicalAlerts"));
                    tempPat.setPatientWeight(rs.getString("patientWeight"));
                    tempPat.setConfidentialityConstraint(rs.getString("confidentialityConstOnPatData"));
                    tempPat.setSpecialNeeds(rs.getString("specialNeeds"));
                    tempPat.setAssignedPatientLocation(rs.getString("assignedPatientLocation"));
                    // /////////////////////////////////////////end wilpatient
                    // per visit
                }
                if (codesequences) {
                    tempSt.setProcedureCodeSequence(new CodeSequence(rs.getString("codeValue"), rs.getString("codingSchemeDesignator"), rs.getString("codingSchemeVersion"), rs.getString("codeMeaning")));
                }
                // tempSt.setOnline(!rs.wasNull());
                dm.patient = tempPat;
                dm.study = tempSt;
                dms.add(dm);

                log.info(" studyRootMatch metod patient ===> " + tempPat.getLastName() + " " + tempPat.getFirstName() );
                log.info(" studyRootMatch metod study ===> " + tempSt.getAccessionNumber() + " - " + tempSt.getStudyId() + " - " + tempSt.getStudyInstanceUid());
                log.info(" studyRootMatch metod =========================== ");
            } // end while
            
            DicomMatch temp = null;
            for (int i = dms.size() - 1; i >= 0; i--) {
                temp = dms.remove(i);
                if (temp == null) {
                    continue; // Go on if this was the very last result!
                }
                if (st.getModalitiesInStudy() != null) {
                    log.trace(callingAE + ": Query requires Modalities in study");
                    String[] argh = findModalitiesInStudy(temp.study.getStudyInstanceUid());
                    temp.study.setModalitiesInStudy(argh);
                }
                PersonalName[] res = getPhysReadingStudy(temp.study.getStudyInstanceUid());
                if (res != null)
                    for (int j = res.length - 1; j >= 0; j--)
                        temp.study.addNameOfPhysicianReadingStudy(res[j]);
                if (st != null) {
                    log.trace("to get rid: " + String.valueOf(0) + "  " + temp.study.getNumberOfStudyRelatedSeries());
                    if (String.valueOf(0).equalsIgnoreCase(temp.study.getNumberOfStudyRelatedSeries())) {
                        log.trace(callingAE + ": Query requires number of study related series, there's no count, going to sum");
                        // log.debug("get number of related series");
                        // ------------the code should be put over during the
                        // tempstudy population
                        long n = findNumberOfStudyRelatedSeries(temp.study.getStudyInstanceUid());
                        // if(DEBUG)log.debug("#StudyRelInst "+n);
                        temp.study.setNumberOfStudyRelatedSeries(Long.toString(n));
                    } // end if #StudyRelSeries
                    if (st.getNumberOfStudyRelatedInstances() != null) {
                        if (String.valueOf(0).equalsIgnoreCase(temp.study.getNumberOfStudyRelatedInstances()))
                            log.trace(callingAE + ": Query requires number of study related instances,  there's no count, going to sum");
                        // log.debug("get number of related instances");
                        long n = findNumberOfStudyRelatedInstances(temp.study.getStudyInstanceUid());
                        // if(DEBUG)log.debug("#StudyRelSer "+n);
                        temp.study.setNumberOfStudyRelatedInstances(Long.toString(n));
                    } // end if #StudyRelInstances
                } // end if
                // log.debug(dms.size());
                dms.add(i, temp);
                // log.debug(dms.size());
            } // end for
            if (p != null) {
                if (p.getNumberOfPatientRelatedSeries() == DicomConstants.FIND_NUMBER_OF_RELATED) {
                    log.fatal(callingAE + ": Query requires number of patient related series");
                    long n = 0;
                    for (int i = dms.size() - 1; i >= 0; i--) {
                        temp = dms.remove(i);
                        if (temp == null)
                            continue; // Go on if this was the very last
                        // result!
                        n = findNumberOfPatientRelatedSeries(Long.parseLong(temp.patient.getPrimaryKey()));
                        temp.patient.setNumberOfPatientRelatedSeries(Long.toString(n));
                        dms.add(i, temp);
                    } // end for
                } // end if
                if (p.getNumberOfPatientRelatedInstances() == DicomConstants.FIND_NUMBER_OF_RELATED) {
                    log.fatal(callingAE + ": Query requires number of patient related instances");
                    long n = 0;
                    for (int i = dms.size() - 1; i >= 0; i--) {
                        temp = dms.remove(i);
                        if (temp == null)
                            continue; // Go on if this was the very last
                        // result!
                        n = findNumberOfPatientRelatedInstances(Long.parseLong(temp.patient.getPrimaryKey()));
                        temp.patient.setNumberOfPatientRelatedInstances(Long.toString(n));
                        dms.add(i, temp);
                    } // end for
                } // end if
            } // end if
            if ((readRows <= limit) || (limit == 0) || (readRows == 0)) {
                log.trace(callingAE + "About to add a NULL dataset, with readRows: " + readRows);
                dms.add(null); // If less rows than the maximum have been read,
                // no more results can be found by another
                // query: add a null DicomMatch to notify no
                // more calls are needed!!!
            }
            
            bldCrono.stop();
            // log.debug(callingAE + ": this time for reconstruct results: " + bldCrono.getMeasure() + "ms.");
        } catch (SQLException sex) {
            if ("Query too long".equals(sex.getMessage())) {
                log.warn(callingAE + ":Query should have at least one among name, lastname, Pid, and date, accession number or studyID");
                dms.add(null);
            } else {
                log.error(callingAE + ": when looking for matches: ", sex);
            }
        } catch (Exception ex) {
            log.error(callingAE + ":An exception occurred when looking for matches: ", ex);
        } finally {
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        }
        tLogger.toLog(new String[] { tLogger.QUERY_STU_STU, callingAE, qryCrono.toString(), bldCrono.toString(), query.substring(query.lastIndexOf("WHERE")), dms.size() + "" });
        DicomMatch[] tempAr = new DicomMatch[dms.size()];
        dms.toArray(tempAr);
        dms = null;
        return tempAr; // I need to cast it to DicomMatch[]!!!
    } // end studyRootMatch()

  

    /**
     * Patient root match.
     * 
     * @param p
     *            the p
     * @param st
     *            the st
     * @param se
     *            the se
     * @return the dicom match[]
     */
    public DicomMatch[] patientRootMatch(Patient p, Study st, Series se, String callingAE) {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        
    	Timer qryCrono = new Timer();
        Timer bldCrono = new Timer();
        try{
            con = dataSource.getConnection();
        }catch (SQLException ex){
            log.error(callingAE + ": " + LogMessage._NoDBConnection, ex);
            return new DicomMatch[0];
        }
        oracle = Dbms.isOracle(con);
        
        // log.debug(callingAE + ": Starting a PatientRoot search at series level");
        /*
         * Sequence Matching Not Supported, as well as namesOfPhysiciansReadingStudy!!!!
         */
        List<DicomMatch> dms = new ArrayList<DicomMatch>(11); // To store all
        // the found
        // matches!!!
        String qry = "";
        try{
            modDateFormat(con);
            stat = con.createStatement();
            stat.setMaxRows(0);
            qry = preparePRPStSe(p, st, se);
            // log.debug(callingAE + ": Exec this:" + qry.substring(qry.lastIndexOf("WHERE")));
            qryCrono.restart();
            rs = stat.executeQuery(qry);
            qryCrono.stop();
            // log.debug(callingAE + ": in " + qryCrono.getMeasure() + "ms.");
            bldCrono.start();
            while (rs.next()) { // Now process the results:
                DicomMatch dm = new DicomMatch();
                Patient tempPat = new Patient();
                Study tempSt = new Study("");
                Series tempSe = new Series();
                tempPat.setPrimaryKey(rs.getString(1));
                tempPat.setLastName(rs.getString(2));
                tempPat.setFirstName(rs.getString(3));
                tempPat.setMiddleName(rs.getString(4));
                tempPat.setPrefix(rs.getString(5));
                tempPat.setSuffix(rs.getString(6));
                tempPat.setBirthDate(rs.getDate(7));
                tempPat.setBirthTime(rs.getTime(8));
                tempPat.setSex(rs.getString(9));
                tempPat.setPatientId(rs.getString(10));
                tempPat.setIdIssuer(rs.getString(11));
                tempPat.setEthnicGroup(rs.getString(12));
                tempPat.setPatientComments(rs.getString(13));
                tempPat.setPatientAddress(rs.getString(14));
                tempPat.setNumberOfPatientRelatedStudies(rs.getString(15));
                tempPat.setPatientState(rs.getString(17)); // 16 is
                // WLPatientDataPerVisit.studyFK
                tempPat.setPregnancyStatus(rs.getString(18));
                tempPat.setMedicalAlerts(rs.getString(19));
                tempPat.setPatientWeight(rs.getString(20));
                tempPat.setConfidentialityConstraint(rs.getString(21));
                tempPat.setSpecialNeeds(rs.getString(22));
                tempPat.setAssignedPatientLocation(rs.getString(23));
                tempSt.setStudyInstanceUid(rs.getString(24));
                tempSt.setStudyId(rs.getString(25));
                tempSt.setStudyStatusId(rs.getString(26));
                tempSt.setStudyDate(rs.getDate(27));
                tempSt.setStudyTime(rs.getTime(28));
                tempSt.setStudyCompletionDate(rs.getDate(29));
                tempSt.setStudyCompletionTime(rs.getTime(30));
                tempSt.setStudyVerifiedDate(rs.getDate(31));
                tempSt.setStudyVerifiedTime(rs.getTime(32));
                tempSt.setAccessionNumber(rs.getString(33));
                tempSt.setStudyDescription(rs.getString(34));
                tempSt.setReferringPhysiciansName(rs.getString(35));
                tempSt.setAdmittingDiagnosesDescription(rs.getString(36));
                tempSt.setProcedureCodeSequence(new CodeSequence(rs.getString(37), rs.getString(38), rs.getString(39), rs.getString(40)));
                tempSe.setSeriesInstanceUid(rs.getString(41));
                tempSe.setSeriesNumber(rs.getString(42));
                tempSe.setModality(rs.getString(43));
                tempSe.setBodyPartExamined(rs.getString(44));
                tempSe.setSeriesStatus(rs.getString(45));
                tempSe.setNumberOfSeriesRelatedInstances(rs.getString(46));
                Equipment te = new Equipment();
                te.setEquipmentType(rs.getString(47));
                te.setManufacturer(rs.getString(48));
                te.setInstitutionName(rs.getString(49));
                te.setStationName(rs.getString(50));
                te.setInstitutionalDepartmentName(rs.getString(51));
                te.setManufacturersModelName(rs.getString(52));
                te.setDeviceSerialNumber(rs.getString(53));
                te.setDateOfLastCalibration(rs.getDate(54));
                te.setTimeOfLastCalibration(rs.getTime(55));
                te.setLastCalibratedBy(rs.getString(56));
                te.setConversionType(rs.getString(57));
                te.setSecondaryCaptureDeviceId(rs.getString(58));
                tempSe.setEquipment(te);
                rs.getString(59);
                // added by marco for series description
                tempSe.setSeriesDescription(rs.getString(60));
                tempSt.setStudyStatus(rs.getString(61));
                tempSe.setOperatorsName(rs.getString(63));
                tempSt.setSpecificCharacterSet(rs.getString(64));
                // --------------------------------------
                // tempSt.setOnline(!rs.wasNull());
                dm.patient = tempPat;
                dm.study = tempSt;
                dm.series = tempSe;
                dms.add(dm);
            } // end while
            log.trace("About to add a NULL dataset");
            // Now I have to process each study anyway, to get the physicians
            // reading the study: I get the number of Study related series and
            // instances as well:
            DicomMatch temp = null;
            for (int i = dms.size() - 1; i >= 0; i--) {
                temp = dms.remove(i);
                if (temp == null)
                    continue;
                // String[]
                // argh=findModalitiesInStudy(temp.study.getStudyInstanceUid());
                // temp.study.setModalitiesInStudy(argh);
                PersonalName[] res = getPhysReadingStudy(temp.study.getStudyInstanceUid());
                if (res != null)
                    for (int j = res.length - 1; j >= 0; j--)
                        temp.study.addNameOfPhysicianReadingStudy(res[j]);
                if (st != null)
                    if (st.getNumberOfStudyRelatedSeries() != null) {
                        long n = findNumberOfStudyRelatedSeries(temp.study.getStudyInstanceUid());
                        // log.debug("#StudyRelInst "+n);
                        temp.study.setNumberOfStudyRelatedSeries(Long.toString(n));
                    } // end if #StudyRelSeries
                if (st != null)
                    if (st.getNumberOfStudyRelatedSeries() != null) {
                        long n = findNumberOfStudyRelatedInstances(temp.study.getStudyInstanceUid());
                        // log.debug("#StudyRelSer "+n);
                        temp.study.setNumberOfStudyRelatedInstances(Long.toString(n));
                    } // end if #StudyRelInstances
                dms.add(i, temp);
            }
            if (p != null)
                if (p.getNumberOfPatientRelatedSeries() != null) {
                    long n = 0;
                    for (int i = dms.size() - 1; i >= 0; i--) {
                        temp = dms.remove(i);
                        if (temp == null)
                            continue;
                        n = findNumberOfPatientRelatedSeries(Long.parseLong(temp.patient.getPrimaryKey()));
                        temp.patient.setNumberOfPatientRelatedSeries(Long.toString(n));
                        dms.add(i, temp);
                    } // end for
                } // end if
            if (p != null)
                if (p.getNumberOfPatientRelatedInstances() != null) {
                    long n = 0;
                    for (int i = dms.size() - 1; i >= 0; i--) {
                        temp = dms.remove(i);
                        if (temp == null)
                            continue;
                        n = findNumberOfPatientRelatedInstances(Long.parseLong(temp.patient.getPrimaryKey()));
                        temp.patient.setNumberOfPatientRelatedInstances(Long.toString(n));
                        dms.add(i, temp);
                    } // end for
                } // end if
            dms.add(null);
        } catch (SQLException sex) {
            log.error(callingAE + ": An exception occurred when looking for matches: ", sex);
        }catch (Exception ex){
            log.error(callingAE + ": An exception occurred when looking for matches: ", ex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);        	
        }
        bldCrono.stop();
        tLogger.toLog(new String[] { tLogger.QUERY_PAT_SER, callingAE, qryCrono.toString(), bldCrono.toString(), qry.substring(qry.lastIndexOf("WHERE")), dms.size() + "" });
        DicomMatch[] tempAr = new DicomMatch[dms.size()];
        dms.toArray(tempAr);
        dms = null;
        return tempAr; // I need to cast it to DicomMatch[]!!!
    } // end patientRootMatch()

    /**
     * Patient root match.
     * 
     * @param p
     *            the
     * @param st
     *            the st
     * @param limit
     *            the limit
     * @param callingAE
     *            the calling AE
     * @param dep
     *            the dep
     * @return the dicom match[]
     */
    public DicomMatch[] patientRootMatch(Patient p, Study st, int limit, String callingAE, boolean depEnabled){
    	Connection con = null;
    	Statement stat = null;
    	ResultSet rs = null;
    	
        Timer qryCrono = new Timer();
        Timer bldCrono = new Timer();
        String query = "";
        try {
            con = dataSource.getConnection();
        } catch (SQLException ex) {
            log.error(callingAE + ": " + LogMessage._NoDBConnection, ex);
            return new DicomMatch[0];
        }
        oracle = Dbms.isOracle(con);
        
        log.debug(callingAE + ":  Starting a PatientRoot search at study level");
        /*
         * Sequence Matching Not Supported, as well as namesOfPhysiciansReadingStudy!!!!
         */
        List<DicomMatch> dms = new ArrayList<DicomMatch>((limit == 0) ? 17 : limit);
        // To store all the found matches!!!
        try{
            modDateFormat(con);
            stat = con.createStatement();
            stat.setMaxRows(limit);
            query = preparePRPSt(p, st, callingAE, depEnabled, limit);
            // rs = stat.executeQuery(preparePRPSt(p, st, callingAE, dep,
            // limit));
            boolean optional = query.contains("WLpatientpervisit");
            boolean codesequences = query.contains("CodeSequences");
            // log.debug(callingAE + ": exec :" + query.substring(query.lastIndexOf("WHERE")));
            qryCrono.start();
            rs = stat.executeQuery(query);
            qryCrono.stop();
            int readRows = 0;
            bldCrono.start();
            while (rs.next()) { // Now process the results:
                readRows++; // You're reading one row!
                DicomMatch dm = new DicomMatch();
                Patient tempPat = new Patient();
                Study tempSt = new Study("");
                // /////////////////////////////////////////////////////////////////////////////
                /*
                 * tempPat.setPrimaryKey(rs.getString(1)); tempPat.setLastName(rs.getString(2)); tempPat.setFirstName(rs.getString(3)); tempPat.setMiddleName(rs.getString(4)); tempPat.setPrefix(rs.getString(5)); tempPat.setSuffix(rs.getString(6)); tempPat.setBirthDate(rs.getDate(7)); tempPat.setBirthTime(rs.getTime(8)); tempPat.setSex(rs.getString(9)); tempPat.setPatientId(rs.getString(10)); tempPat.setIdIssuer(rs.getString(11)); tempPat.setEthnicGroup(rs.getString(12)); tempPat.setPatientComments(rs.getString(13)); tempPat.setPatientAddress(rs.getString(14)); tempPat.setNumberOfPatientRelatedStudies(rs.getString(15)); tempPat.setPatientState(rs.getString(17)); // 16 is // WLPatientDataPerVisit.studyFK tempPat.setPregnancyStatus(rs.getString(18)); tempPat.setMedicalAlerts(rs.getString(19)); tempPat.setPatientWeight(rs.getString(20)); tempPat.setConfidentialityConstraint(rs.getString(21)); tempPat.setSpecialNeeds(rs.getString(22)); tempPat.setAssignedPatientLocation(rs.getString(23));
                 */
                // /////////////////////////////////////////////////////////////////////////////Table
                // patient
                tempPat.setPrimaryKey(rs.getString(1));
                tempPat.setLastName(rs.getString(2));
                tempPat.setFirstName(rs.getString(3));
                tempPat.setMiddleName(rs.getString(4));
                tempPat.setPrefix(rs.getString(5));
                tempPat.setSuffix(rs.getString(6));
                tempPat.setBirthDate(rs.getDate(7));
                tempPat.setBirthTime(rs.getTime(8));
                tempPat.setSex(rs.getString(9));
                tempPat.setPatientId(rs.getString(10));
                tempPat.setIdIssuer(rs.getString(11));
                /*
                 * tempSt.setStudyInstanceUid(rs.getString(24)); tempSt.setStudyId(rs.getString(25)); tempSt.setStudyStatusId(rs.getString(26)); tempSt.setStudyDate(rs.getDate(27)); tempSt.setStudyTime(rs.getTime(28)); tempSt.setStudyCompletionDate(rs.getDate(29)); tempSt.setStudyCompletionTime(rs.getTime(30)); tempSt.setStudyVerifiedDate(rs.getDate(31)); tempSt.setStudyVerifiedTime(rs.getTime(32)); tempSt.setAccessionNumber(rs.getString(33)); tempSt.setStudyDescription(rs.getString(34)); tempSt.setReferringPhysiciansName(rs.getString(35)); tempSt.setAdmittingDiagnosesDescription(rs.getString(36)); tempSt.setProcedureCodeSequence(new CodeSequence(rs .getString(37), rs.getString(38), rs.getString(39), rs .getString(40))); rs.getString(41); // = fastestAccess tempSt.setOnline(!rs.wasNull());
                 */
                tempSt.setStudyInstanceUid(rs.getString(13));
                tempSt.setStudyId(rs.getString(14));
                tempSt.setStudyStatusId(rs.getString(15));
                tempSt.setStudyDate(rs.getDate(16));
                tempSt.setStudyTime(rs.getTime(17));
                tempSt.setStudyCompletionDate(rs.getDate(18));
                tempSt.setStudyCompletionTime(rs.getTime(19));
                tempSt.setStudyVerifiedDate(rs.getDate(20));
                tempSt.setStudyVerifiedTime(rs.getTime(21));
                tempSt.setAccessionNumber(rs.getString(22));
                tempSt.setStudyDescription(rs.getString(23));
                tempSt.setReferringPhysiciansName(rs.getString(24));
                tempSt.setAdmittingDiagnosesDescription(rs.getString(25));
                // --------------------------it cost no more to put them either
                tempSt.setNumberOfStudyRelatedSeries(rs.getString(27));
                tempSt.setNumberOfStudyRelatedInstances(rs.getString(28));
                tempSt.setStudyStatus(rs.getString(12));
                tempSt.setSpecificCharacterSet(rs.getString(29));
                if (optional) {
                    tempPat.setEthnicGroup(rs.getString("ethnicGroup"));
                    tempPat.setPatientComments(rs.getString("patientComments"));
                    tempPat.setPatientAddress(rs.getString("patientAddress"));
                    tempPat.setNumberOfPatientRelatedStudies(rs.getString("numberOfPatientRelatedStudies"));
                    // ////////////////end patient
                    // demographics////////////////////////////////////////////////////
                    tempPat.setPatientState(rs.getString("patientState")); // 16
                    // is
                    // WLPatientDataPerVisit.studyFK
                    tempPat.setPregnancyStatus(rs.getString("pregnancyStatus"));
                    tempPat.setMedicalAlerts(rs.getString("medicalAlerts"));
                    tempPat.setPatientWeight(rs.getString("patientWeight"));
                    tempPat.setConfidentialityConstraint(rs.getString("confidentialityConstOnPatData"));
                    tempPat.setSpecialNeeds(rs.getString("specialNeeds"));
                    tempPat.setAssignedPatientLocation(rs.getString("assignedPatientLocation"));
                    // /////////////////////////////////////////end wilpatient
                    // per visit
                }
                if (codesequences) {
                    tempSt.setProcedureCodeSequence(new CodeSequence(rs.getString("codeValue"), rs.getString("codingSchemeDesignator"), rs.getString("codingSchemeVersion"), rs.getString("codeMeaning")));
                }
                rs.getString("fastestAccess"); // =fastestAccess
                // tempSt.setOnline(!rs.wasNull());
                // ///////////////////////////////////////////////////////////////
                dm.patient = tempPat;
                dm.study = tempSt;
                /*
                 * log.debug(""); log.debug("To parse one Dataset: "+(endingLoop-startingLoop)+"milliseconds"); log.debug("");
                 */
                dms.add(dm);
                /*
                 * log.debug(""); log.debug("To add one Dataset into ArrayList: "+(System.currentTimeMillis()-endingLoop)+"milliseconds"); log.debug("");
                 */
            } // end while
            // Now I have to process each study anyway, to get the physicians
            // reading the study: I get the number of Study related series and
            // instances as well:
            DicomMatch temp = null;
            for (int i = dms.size() - 1; i >= 0; i--) {
                temp = dms.remove(i);
                if (temp == null)
                    continue; // Go on if this was the very last result!
                // long startModality = System.currentTimeMillis();
                if (st.getModalitiesInStudy() != null) {
                    log.trace(callingAE + ": Query requires Modalities in study");
                    String[] argh = findModalitiesInStudy(temp.study.getStudyInstanceUid());
                    temp.study.setModalitiesInStudy(argh);
                }
                PersonalName[] res = getPhysReadingStudy(temp.study.getStudyInstanceUid());
                if (res != null)
                    for (int j = res.length - 1; j >= 0; j--)
                        temp.study.addNameOfPhysicianReadingStudy(res[j]);
                if (st != null) {
                    log.trace("to get rid: " + String.valueOf(0) + "  " + temp.study.getNumberOfStudyRelatedSeries());
                    if (String.valueOf(0).equalsIgnoreCase(temp.study.getNumberOfStudyRelatedSeries())) {
                        log.trace(callingAE + ": Query requires number of study related series, there's no count, going to sum");
                        // log.debug("get number of related series");
                        // ------------the code should be put over during the
                        // tempstudy population
                        long n = findNumberOfStudyRelatedSeries(temp.study.getStudyInstanceUid());
                        // if(DEBUG)log.debug("#StudyRelInst "+n);
                        temp.study.setNumberOfStudyRelatedSeries(Long.toString(n));
                    } // end if #StudyRelSeries
                    if (st.getNumberOfStudyRelatedInstances() != null) {
                        if (String.valueOf(0).equalsIgnoreCase(temp.study.getNumberOfStudyRelatedInstances()))
                            log.trace(callingAE + ": Query requires number of study related instances,  there's no count, going to sum");
                        // log.debug("get number of related instances");
                        long n = findNumberOfStudyRelatedInstances(temp.study.getStudyInstanceUid());
                        // if(DEBUG)log.debug("#StudyRelSer "+n);
                        temp.study.setNumberOfStudyRelatedInstances(Long.toString(n));
                    } // end if #StudyRelInstances
                } // end if
                // log.debug(dms.size());
                dms.add(i, temp);
                // log.debug(dms.size());
            } // end for
            if (p != null) {
                if (p.getNumberOfPatientRelatedSeries() == DicomConstants.FIND_NUMBER_OF_RELATED) {
                    log.warn(callingAE + ": Query requires number of patient related series");
                    long n = 0;
                    for (int i = dms.size() - 1; i >= 0; i--) {
                        temp = dms.remove(i);
                        if (temp == null)
                            continue; // Go on if this was the very last
                        // result!
                        n = findNumberOfPatientRelatedSeries(Long.parseLong(temp.patient.getPrimaryKey()));
                        temp.patient.setNumberOfPatientRelatedSeries(Long.toString(n));
                        dms.add(i, temp);
                    } // end for
                } // end if
                if (p.getNumberOfPatientRelatedInstances() == DicomConstants.FIND_NUMBER_OF_RELATED) {
                    log.warn(callingAE + ": Query requires number of patient related instances");
                    long n = 0;
                    for (int i = dms.size() - 1; i >= 0; i--) {
                        temp = dms.remove(i);
                        if (temp == null)
                            continue; // Go on if this was the very last
                        // result!
                        n = findNumberOfPatientRelatedInstances(Long.parseLong(temp.patient.getPrimaryKey()));
                        temp.patient.setNumberOfPatientRelatedInstances(Long.toString(n));
                        dms.add(i, temp);
                    } // end for
                } // end if
            } // end if
            if ((readRows <= limit) || (limit == 0) || (readRows == 0)) {
                log.trace("About to add a NULL dataset, with readRows: " + readRows);
                dms.add(null); // If less rows than the maximum have been read,
                // no more results can be found by another
                // query: add a null DicomMatch to notify no
                // more calls are needed!!!
            }
        } catch (SQLException sex) {
            log.error(callingAE + ": An exception occurred when looking for matches: ", sex);
        } catch (Exception ex) {
            log.error(callingAE + ": An exception occurred when looking for matches: ", ex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        }
        bldCrono.stop();
        tLogger.toLog(new String[] { tLogger.QUERY_PAT_STU, callingAE, qryCrono.toString(), bldCrono.toString(), query.substring(query.lastIndexOf("WHERE")), dms.size() + "" });
        DicomMatch[] tempAr = new DicomMatch[dms.size()];
        dms.toArray(tempAr);
        dms = null;
        return tempAr; // I need to cast it to DicomMatch[]!!!
    } // end patientRootMatch()

    /**
     * Patient root match.
     * 
     * @param p
     *            the p
     * @param limit
     *            the limit
     * @return the dicom match[]
     */
    public DicomMatch[] patientRootMatch(Patient p, int limit, String callingAE) {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        
    	try{
            con = dataSource.getConnection();
        } catch (SQLException ex) {
            log.error(callingAE + ": " + LogMessage._NoDBConnection, ex);
            return new DicomMatch[0];
        }
        oracle = Dbms.isOracle(con);
        
        log.debug(callingAE + ": QueryDealer: Starting a PatientRoot search at patient level");
        /* Sequence Matching Not Supported!!!! */
        List<DicomMatch> dms = new ArrayList<DicomMatch>((limit == 0) ? 17 : limit); // To store all the found matches!!!
        try{
            modDateFormat(con);
            stat = con.createStatement();
            stat.setMaxRows(limit);
            String query = preparePRP(p, limit);
            if ((!query.contains("(Patients.lastName)")) && (!query.contains("(Patients.firstName)")) && (!query.contains("Studies.studyDate")) && (!query.contains("(Patients.patientID)")) && (!query.contains("(accessionNumber)")) && (!query.contains("Studies.studyInstanceUID='"))) {
                log.warn(callingAE + ": ERROR: query not Acceptable, too weak filter");
                throw new SQLException("Query too long");
            }
            Timer qryCrono = new Timer();
            log.debug(callingAE + ": exec this :" + query.substring(query.lastIndexOf("WHERE")));
            qryCrono.restart();
            rs = stat.executeQuery(query);
            qryCrono.stop();
            log.debug(callingAE + ": in " + qryCrono.getMeasure() + "ms.");
            int readRows = 0;
            // log.debug("\tLimit: "+limit+"\tpatPkOffset"+patPkOffset);
            while (rs.next()) { // Now process the results:
                readRows++; // You're reading one row!
                // log.debug("Reading line "+readRows);
                DicomMatch dm = new DicomMatch();
                Patient tempPat = new Patient();
                tempPat.setPrimaryKey(rs.getString(1));
                tempPat.setLastName(rs.getString(2));
                tempPat.setFirstName(rs.getString(3));
                tempPat.setMiddleName(rs.getString(4));
                tempPat.setPrefix(rs.getString(5));
                tempPat.setSuffix(rs.getString(6));
                tempPat.setBirthDate(rs.getDate(7));
                tempPat.setBirthTime(rs.getTime(8));
                tempPat.setSex(rs.getString(9));
                tempPat.setPatientId(rs.getString(10));
                tempPat.setIdIssuer(rs.getString(11));
                // tempPat.setEthnicGroup(rs.getString(12));
                // tempPat.setPatientComments(rs.getString(13));
                // tempPat.setPatientAddress(rs.getString(14));
                tempPat.setNumberOfPatientRelatedStudies(rs.getString(12));
                // tempPat.setPatientState(rs.getString(17)); // 16 is
                // WLPatientDataPerVisit.studyFK
                // tempPat.setPregnancyStatus(rs.getString(18));
                // tempPat.setMedicalAlerts(rs.getString(19));
                // tempPat.setPatientWeight(rs.getString(20));
                // tempPat.setConfidentialityConstraint(rs.getString(21));
                // tempPat.setSpecialNeeds(rs.getString(22));
                // tempPat.setAssignedPatientLocation(rs.getString(23));
                dm.patient = tempPat;
                dms.add(dm);
            } // end while
            if ((readRows <= limit) || (limit == 0) || (readRows == 0)) {
                log.trace("About to add a NULL dataset, with readRows: " + readRows);
                dms.add(null); // If less rows than the maximum have been read,
                // no more results can be found by another
                // query: add a null DicomMatch to notify no
                // more calls are needed!!!
            }
        } catch (SQLException sex) {
            if (sex.getMessage().equals("Query too long")) {
                log.warn(callingAE + ": Too weak filter, query too long.");
            } else {
                log.warn(callingAE + ": An exception occurred when looking for matches: ", sex);
            }
        } catch (Exception ex) {
            log.warn(callingAE + ": An exception occurred when looking for matches: ", ex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        }
        DicomMatch[] tempAr = new DicomMatch[dms.size()];
        dms.toArray(tempAr);
        dms = null;
        return tempAr; // I need to cast it to DicomMatch[]!!!
    } // end patientRootMatch()

    // -----------------public private methods
    /**
     * Gets the media info on study.
     * 
     * @param suid
     *            the suid
     * @param oldest
     *            the oldest
     * @return the media info on study
     */
    public String[] getMediaInfoOnStudy(String suid, boolean oldest, String callingAE) {
    	Connection con = null;
    	Statement stat = null;
    	ResultSet rs = null;
    	
        try {
            con = dataSource.getConnection();
        } catch (SQLException ex) {
            log.error(LogMessage._NoDBConnection, ex);
            return new String[] { "", "" };
        }
        oracle = Dbms.isOracle(con);
        
        log.debug(callingAE + ": Retrieving StorageMediaFileSet info for " + suid);
        String[] res = { "", "" };
        String qry = "";
        try{
            modDateFormat(con);
            stat = con.createStatement();
            stat.setMaxRows(0);
            qry = storageMediaBase + suid + storageMediaEnd + ((oldest) ? "ASC" : "DESC");
            rs = stat.executeQuery(qry); // If I want the oldest
            // first, I need an ascending order to have it as the first result!
            if (rs.next()) {
                // ok, the ids become zero-length
                res[0] = rs.getString("Name"); // Name!
                res[1] = rs.getString("furtherDeviceInfo"); // FurtherDeviceInfo used as UID
            } else {
                log.error(callingAE + ": NO STUDYLOCATION ENTRY FOR THIS STUDY: " + suid);
            }
        } catch (SQLException sex) {
            log.error(callingAE + ": was searching " + qry.substring(qry.lastIndexOf("WHERE")), sex);
        } catch (Exception ex) {
            log.error(callingAE + ": was searching " + qry.substring(qry.lastIndexOf("WHERE")), ex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        } // end try...catch...finally
        return res;
    } // end getMediaInfoOnStudy

    // Private Methods:-------------------------------
    /**
     * Prepare PR p st se in.
     * 
     * @param p
     *            the p
     * @param st
     *            the st
     * @param s
     *            the s
     * @param i
     *            the i
     * @return the string
     */
//    private String preparePRPStSeIn(Patient p, Study st, Series s, Instance i) {
//        StringBuffer sb = new StringBuffer(1000); // It starts from a basis of 479!
//        if (i instanceof Image) {
//        	
//            sb.append(((Image)i).getNumberOfFrames()==null?patientRootMatchPStSeIm:patientRootMatchPStSeImAndNumOfFr); // Start the query with the basic requirements
//            // NOTE: numberOfFrames is used only as return parameter, not for matching
//            sb.append("'").append(s.getSeriesInstanceUid()).append("' AND Images.SeriesFK='").append(s.getSeriesInstanceUid()).append("'");
//            if ((i.getSopInstanceUid() != null) && (!i.getSopInstanceUid().equals(""))) { // Only a single UID was specified for matching!
//                if (!i.getSopInstanceUid().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Images.sopInstanceUID='").append(i.getSopInstanceUid()).append("'");
//            } else if (i.getUidsToMatch() != null) { // A list of UIDs was required for matching!
//                String[] rUids = i.getUidsToMatch();
//                sb.append(" AND (");
//                for (int k = rUids.length - 1; k >= 0; k--) {
//                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
//                        sb.append("Images.sopInstanceUID='").append(rUids[k]).append("'");
//                    if (k > 0)
//                        sb.append(" OR ");
//                } // end for
//                sb.append(")");
//            }
//            if ((i.getSopClassUid() != null) && (!i.getSopClassUid().equals(""))) {
//                // Only a single UID was specified for matching!
//                if (!i.getSopClassUid().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Images.sopClassUID='").append(i.getSopClassUid()).append("'");
//            } else if (i.getSopClassesToMatch() != null) { // A list of UIDs was required for matching!
//                String[] rUids = i.getSopClassesToMatch();
//                sb.append(" AND (");
//                for (int k = rUids.length - 1; k >= 0; k--) {
//                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
//                        sb.append("Images.sopClassUID='").append(rUids[k]).append("'");
//                    if (k > 0)
//                        sb.append(" OR ");
//                } // end for
//                sb.append(")");
//            } // end UIDs
//            if (i.getInstanceNumber() != null) {
//                if (!i.getInstanceNumber().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Images.instanceNumber = UPPER('").append(i.getInstanceNumber()).append("')");
//            }
//            if (((Image) i).getSamplesPerPixel() != null) {
//                if (!((Image) i).getSamplesPerPixel().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Images.samplesPerPixel LIKE ").append(((Image) i).getSamplesPerPixel());
//            }
//            if (((Image) i).getRows() != null) {
//                if (!((Image) i).getRows().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Images.rowsNum=").append(((Image) i).getRows());
//            }
//            if (((Image) i).getColumns() != null) {
//                if (!((Image) i).getColumns().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Images.columnsNum=").append(((Image) i).getColumns());
//            }
//            if (((Image) i).getBitsAllocated() != null) {
//                if (!((Image) i).getBitsAllocated().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Images.bitsAllocated=").append(((Image) i).getBitsAllocated());
//            }
//            if (((Image) i).getBitsStored() != null) {
//                if (!((Image) i).getBitsStored().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Images.bitsStored=").append(((Image) i).getBitsStored());
//            }
//            if (((Image) i).getHighBit() != null) {
//                if (!((Image) i).getHighBit().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Images.highBit=").append(((Image) i).getHighBit());
//            }
//            if (((Image) i).getPixelRepresentation() != null) {
//                if (!((Image) i).getPixelRepresentation().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Images.pixelRepresentation=").append(((Image) i).getPixelRepresentation());
//            }
//            //sb.append(" ORDER BY Images.sopInstanceUID");
//        } else if (i instanceof PresState) {
//            sb.append(patientRootMatchPStSePs); // Start the query with the basic requirements
//            sb.append("'").append(s.getSeriesInstanceUid()).append("' AND PresStates.SeriesFK='").append(s.getSeriesInstanceUid()).append("'");
//            if ((i.getSopInstanceUid() != null) && (!i.getSopInstanceUid().equals(""))) { // Only a single UID was specified for matching!
//                if (!i.getSopInstanceUid().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND PresStates.sopInstanceUID='").append(i.getSopInstanceUid()).append("'");
//            } else if (i.getUidsToMatch() != null) { // A list of UIDs was required for matching!
//                String[] rUids = i.getUidsToMatch();
//                sb.append(" AND (");
//                for (int k = rUids.length - 1; k >= 0; k--) {
//                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
//                        sb.append("PresStates.sopInstanceUID='").append(rUids[k]).append("'");
//                    if (k > 0)
//                        sb.append(" OR ");
//                } // end for
//                sb.append(")");
//            }
//            if ((i.getSopClassUid() != null) && (!i.getSopClassUid().equals(""))) { // Only a single UID
//                // was specified for
//                // matching!
//                if (!i.getSopClassUid().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND PresStates.sopClassUID='").append(i.getSopClassUid()).append("'");
//            } else if (i.getSopClassesToMatch() != null) { // A list of UIDs
//                // was required for
//                // matching!
//                String[] rUids = i.getSopClassesToMatch();
//                sb.append(" AND (");
//                for (int k = rUids.length - 1; k >= 0; k--) {
//                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
//                        sb.append("PresStates.sopClassUID='").append(rUids[k]).append("'");
//                    if (k > 0)
//                        sb.append(" OR ");
//                } // end for
//                sb.append(")");
//            } // end UIDs
//            if (i.getInstanceNumber() != null) {
//                if (!i.getInstanceNumber().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND PresStates.instanceNumber = UPPER('").append(i.getInstanceNumber()).append("')");
//            }
//            if (((PresState) i).getPresentationLabel() != null) {
//                if (!((PresState) i).getPresentationLabel().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND PresStates.presentationLabel = UPPER('").append(((PresState) i).getPresentationLabel()).append("')");
//            }
//            if (((PresState) i).getPresentationDescription() != null) {
//                if (!((PresState) i).getPresentationDescription().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND PresStates.presentationDescription = UPPER('").append(((PresState) i).getPresentationDescription()).append("')");
//            }
//            if (((PresState) i).getPresentationCreationDate() != null) {
//                if (((PresState) i).getPresentationCreationDateLate() == null)
//                    sb.append(" AND PresStates.presentationCreationDate='").append(((PresState) i).getPresentationCreationDate()).append("'");
//                else
//                    sb.append(" AND PresStates.presentationCreationDate>='").append(((PresState) i).getPresentationCreationDate()).append("' AND PresStates.presentationCreationDate<='").append(((PresState) i).getPresentationCreationDateLate()).append("'");
//            }
//            if (((PresState) i).getPresentationCreationTime() != null) {
//                if (((PresState) i).getPresentationCreationTimeLate() == null)
//                    sb.append((oracle) ? " AND to_char(PresStates.presentationCreationDate, 'HH24:MI:SS')='" : " AND PresStates.presentationCreationDate='").append(((PresState) i).getPresentationCreationTime()).append("'");
//                else
//                    sb.append((oracle) ? " AND to_char(PresStates.presentationCreationDate, 'HH24:MI:SS')>='" : " AND PresStates.presentationCreationTime>='").append(((PresState) i).getPresentationCreationTime()).append((oracle) ? "' AND to_char(PresStates.presentationCreationDate, 'HH24:MI:SS')<='" : "' AND PresStates.presentationCreationTime<='").append(((PresState) i).getPresentationCreationTimeLate()).append("'");
//            }
//            if (((PresState) i).getPresentationCreatorsName() != null) {
//                if (!((PresState) i).getPresentationCreatorsName().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND PresStates.presentationCreatorsName = UPPER('").append(((PresState) i).getPresentationCreatorsName()).append("')");
//            }
//            if (((PresState) i).getRecommendedViewingMode() != null) {
//                if (!((PresState) i).getRecommendedViewingMode().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND PresStates.recommendedViewingMode = UPPER('").append(((PresState) i).getRecommendedViewingMode()).append("')");
//            }
//        } else if (i instanceof StructRep) {
//            sb.append(patientRootMatchPStSeSr); // Start the query with the basic requirements
//            sb.append("'").append(s.getSeriesInstanceUid()).append("' AND StructReps.SeriesFK='").append(s.getSeriesInstanceUid()).append("'");
//            if ((i.getSopInstanceUid() != null) && (!i.getSopInstanceUid().equals(""))) { // Only a single
//                // UID was
//                // specified for
//                // matching!
//                if (!i.getSopInstanceUid().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND StructReps.sopInstanceUID='").append(i.getSopInstanceUid()).append("'");
//            } else if (i.getUidsToMatch() != null) { // A list of UIDs was
//                // required for
//                // matching!
//                String[] rUids = i.getUidsToMatch();
//                sb.append(" AND (");
//                for (int k = rUids.length - 1; k >= 0; k--) {
//                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
//                        sb.append("StructReps.sopInstanceUID='").append(rUids[k]).append("'");
//                    if (k > 0)
//                        sb.append(" OR ");
//                } // end for
//                sb.append(")");
//            }
//            if ((i.getSopClassUid() != null) && (!i.getSopClassUid().equals(""))) { // Only a single UID
//                // was specified for
//                // matching!
//                if (!i.getSopClassUid().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND StructReps.sopClassUID='").append(i.getSopClassUid()).append("'");
//            } else if (i.getSopClassesToMatch() != null) { // A list of UIDs
//                // was required for
//                // matching!
//                String[] rUids = i.getSopClassesToMatch();
//                sb.append(" AND (");
//                for (int k = rUids.length - 1; k >= 0; k--) {
//                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
//                        sb.append("StructReps.sopClassUID='").append(rUids[k]).append("'");
//                    if (k > 0)
//                        sb.append(" OR ");
//                } // end for
//                sb.append(")");
//            } // end UIDs
//            if (i.getInstanceNumber() != null) {
//                if (!i.getInstanceNumber().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND StructReps.instanceNumber = UPPER('").append(i.getInstanceNumber()).append("')");
//            }
//            if (((StructRep) i).getCompletionFlag() != null) {
//                if (!((StructRep) i).getCompletionFlag().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND StructReps.completionFlag LIKE UPPER('").append(((StructRep) i).getCompletionFlag()).append("')");
//            }
//            if (((StructRep) i).getVerificationFlag() != null) {
//                if (!((StructRep) i).getVerificationFlag().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND StructReps.verificationFlag LIKE UPPER('").append(((StructRep) i).getVerificationFlag()).append("')");
//            }
//            if (((StructRep) i).getContentDate() != null) {
//                if (((StructRep) i).getContentDateLate() == null)
//                    sb.append(" AND StructReps.contentDate='").append(((StructRep) i).getContentDate()).append("'");
//                else
//                    sb.append(" AND StructReps.contentDate>='").append(((StructRep) i).getContentDate()).append("' AND StructReps.contentDate<='").append(((StructRep) i).getContentDateLate()).append("'");
//            }
//            if (((StructRep) i).getContentTime() != null) {
//                if (((StructRep) i).getContentTimeLate() == null)
//                    sb.append((oracle) ? " AND to_char(StructReps.contentTime, 'HH24:MI:SS')='" : " AND StructReps.contentTime='").append(((StructRep) i).getContentTime()).append("'");
//                else
//                    sb.append((oracle) ? " AND to_char(StructReps.contentTime, 'HH24:MI:SS')>='" : " AND StructReps.contentTime>='").append(((StructRep) i).getContentTime()).append((oracle) ? "' AND to_char(StructReps.contentTime, 'HH24:MI:SS')<='" : "' AND StructReps.contentTime<='").append(((StructRep) i).getContentTimeLate()).append("'");
//            }
//            if (((StructRep) i).getObservationDateTime() != null) {
//                if (((StructRep) i).getObservationDateTimeLate() == null)
//                    sb.append(" AND StructReps.observationDateTime='").append(((StructRep) i).getObservationDateTime()).append("'");
//                else
//                    sb.append(" AND StructReps.observationDateTime>='").append(((StructRep) i).getObservationDateTime()).append("' AND StructReps.observationDateTime<='").append(((StructRep) i).getObservationDateTimeLate()).append("'");
//            }
//            // YOU SHOULD NOT MATCH FOR CONCEPT NAME CODE SEQUENCE
//            log.debug("Query ------------- " + sb.toString());
//        } else if (i instanceof Overlay) {
//            sb.append(patientRootMatchPStSeOv); // Start the query with the
//            // basic requirements
//            sb.append("'").append(s.getSeriesInstanceUid()).append("' AND Overlays.SeriesFK='").append(s.getSeriesInstanceUid()).append("'");
//            if ((i.getSopInstanceUid() != null) && (!i.getSopInstanceUid().equals(""))) { // Only a single
//                // UID was
//                // specified for
//                // matching!
//                if (!i.getSopInstanceUid().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Overlays.sopInstanceUID='").append(i.getSopInstanceUid()).append("'");
//            } else if (i.getUidsToMatch() != null) { // A list of UIDs was
//                // required for
//                // matching!
//                String[] rUids = i.getUidsToMatch();
//                sb.append(" AND (");
//                for (int k = rUids.length - 1; k >= 0; k--) {
//                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
//                        sb.append("Overlays.sopInstanceUID='").append(rUids[k]).append("'");
//                    if (k > 0)
//                        sb.append(" OR ");
//                } // end for
//                sb.append(")");
//            }
//            if ((i.getSopClassUid() != null) && (!i.getSopClassUid().equals(""))) { // Only a single UID
//                // was specified for
//                // matching!
//                if (!i.getSopClassUid().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Overlays.sopClassUID='").append(i.getSopClassUid()).append("'");
//            } else if (i.getSopClassesToMatch() != null) { // A list of UIDs
//                // was required for
//                // matching!
//                String[] rUids = i.getSopClassesToMatch();
//                sb.append(" AND (");
//                for (int k = rUids.length - 1; k >= 0; k--) {
//                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
//                        sb.append("Overlays.sopClassUID='").append(rUids[k]).append("'");
//                    if (k > 0)
//                        sb.append(" OR ");
//                } // end for
//                sb.append(")");
//            } // end UIDs
//            if (i.getInstanceNumber() != null) {
//                if (!i.getInstanceNumber().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Overlays.instanceNumber = UPPER('").append(i.getInstanceNumber()).append("')");
//            }
//            if (((Overlay) i).getOverlayNumber() != null) {
//                if (!((Overlay) i).getOverlayNumber().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Overlays.overlayNumber=").append(((Overlay) i).getOverlayNumber());
//            }
//            if (((Overlay) i).getOverlayRows() != null) {
//                if (!((Overlay) i).getOverlayRows().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Overlays.overlayRows=").append(((Overlay) i).getOverlayRows());
//            }
//            if (((Overlay) i).getOverlayColumns() != null) {
//                if (!((Overlay) i).getOverlayColumns().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Overlays.overlayColumns=").append(((Overlay) i).getOverlayColumns());
//            }
//            if (((Overlay) i).getOverlayType() != null) {
//                if (!((Overlay) i).getOverlayType().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Overlays.overlayType = UPPER('").append(((Overlay) i).getOverlayType()).append("')");
//            }
//            if (((Overlay) i).getOverlayBitsAllocated() != null) {
//                if (!((Overlay) i).getOverlayBitsAllocated().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND Overlays.overlayBitsAllocated=").append(((Overlay) i).getOverlayBitsAllocated());
//            }
//        } else { // NonImage
//            sb.append(patientRootMatchPStSeNi); // Start the query with the
//            // basic requirements
//            sb.append("'").append(s.getSeriesInstanceUid()).append("' AND NonImages.SeriesFK='").append(s.getSeriesInstanceUid()).append("'");
//            if ((i.getSopInstanceUid() != null) && (!i.getSopInstanceUid().equals(""))) { // Only a single
//                // UID was
//                // specified for
//                // matching!
//                if (!i.getSopInstanceUid().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND NonImages.sopInstanceUID='").append(i.getSopInstanceUid()).append("'");
//            } else if (i.getUidsToMatch() != null) { // A list of UIDs was
//                // required for
//                // matching!
//                String[] rUids = i.getUidsToMatch();
//                sb.append(" AND (");
//                for (int k = rUids.length - 1; k >= 0; k--) {
//                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
//                        sb.append("NonImages.sopInstanceUID='").append(rUids[k]).append("'");
//                    if (k > 0)
//                        sb.append(" OR ");
//                } // end for
//                sb.append(")");
//            }
//            if ((i.getSopClassUid() != null) && (!i.getSopClassUid().equals(""))) { // Only a single UID
//                // was specified for
//                // matching!
//                if (!i.getSopClassUid().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND NonImages.sopClassUID='").append(i.getSopClassUid()).append("'");
//            } else if (i.getSopClassesToMatch() != null) { // A list of UIDs
//                // was required for
//                // matching!
//                String[] rUids = i.getSopClassesToMatch();
//                sb.append(" AND (");
//                for (int k = rUids.length - 1; k >= 0; k--) {
//                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
//                        sb.append("NonImages.sopClassUID='").append(rUids[k]).append("'");
//                    if (k > 0)
//                        sb.append(" OR ");
//                } // end for
//                sb.append(")");
//            } // end UIDs
//            if (i.getInstanceNumber() != null) {
//                if (!i.getInstanceNumber().equals(DicomConstants.UNIVERSAL_MATCHING))
//                    sb.append(" AND NonImages.instanceNumber = UPPER('").append(i.getInstanceNumber()).append("')");
//            }
//        } // end if...else
//        // Now to deal with wildcards: * -> % , ? -> _ - these characters are not to be used in a normal query!!!
//        String arg = ((sb.toString()).replaceAll("\\" + DicomConstants.MULTIPLE_WC_MATCHING, "%")).replaceAll("\\" + DicomConstants.CHARACTER_WC_MATCHING, "_");
//        // log.debug(arg);
//        return arg + orderInstancesEnd;
//    } // end preparePRPStSeIn()

    /**
     * Prepare PRP st se.
     * 
     * @param p
     *            the p
     * @param st
     *            the st
     * @param se
     *            the se
     * @return the string
     */
    private String preparePRPStSe(Patient p, Study st, Series se) {
        StringBuffer sb = new StringBuffer(2600); 
        sb.append(patientRootMatchPStSe); 
        
        if (p != null) {
            if (p.getLastName() != null) {
                // replace all eventual * con %
                if (!p.getLastName().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.lastName) LIKE UPPER('").append(p.getLastName().replaceAll("'", "''")).append("')");
            }
            if (p.getFirstName() != null) {
                if (!p.getFirstName().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.firstName) LIKE UPPER('").append(p.getFirstName().replaceAll("'", "''")).append("')");
            }
            if (p.getMiddleName() != null) {
                if (!p.getMiddleName().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.middleName) LIKE UPPER('").append(p.getMiddleName().replaceAll("'", "''")).append("')");
            }
            if (p.getPrefix() != null) {
                if (!p.getPrefix().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.prefix) LIKE UPPER('").append(p.getPrefix().replaceAll("'", "''")).append("')");
            }
            if (p.getSuffix() != null) {
                if (!p.getSuffix().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.suffix) LIKE ('").append(p.getSuffix().replaceAll("'", "''")).append("')");
            }
            if (p.getBirthDate() != null) {
                if (p.getBirthDateLate() == null)
                    sb.append(" AND Patients.birthDate='").append(p.getBirthDate()).append("'");
                else
                    sb.append(" AND Patients.birthDate>='").append(p.getBirthDate()).append("' AND Patients.birthDate<='").append(p.getBirthDateLate()).append("'");
            }
            if (p.getBirthTime() != null) {
                if (p.getBirthTimeLate() == null)
                    sb.append((oracle) ? " AND to_char(Patients.birthTime, 'HH24:MI:SS')='" : " AND Patients.birthTime='").append(p.getBirthTime()).append("'");
                else
                    sb.append((oracle) ? " AND to_char(Patients.birthTime, 'HH24:MI:SS')>='" : " AND Patients.birthTime>='").append(p.getBirthTime()).append((oracle) ? "' AND to_char(Patients.birthTime, 'HH24:MI:SS')<='" : "' AND Patients.birthTime<='").append(p.getBirthTimeLate()).append("'");
            }
            if (p.getSex() != null) {
                if (!p.getSex().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.sex) LIKE UPPER('").append(p.getSex().replaceAll("\\" + DicomConstants.MULTIPLE_WC_MATCHING, "%")).append("')");
            }
            if (p.getPatientId() != null) {
                if (!p.getPatientId().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND Patients.patientID LIKE UPPER('").append(p.getPatientId().replace("'","''")).append("')");
            }
            if (p.getIdIssuer() != null) {
                if (!p.getIdIssuer().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND Patients.idIssuer LIKE UPPER('").append(p.getIdIssuer().replace("'","''")).append("')");
            }
            if (p.getEthnicGroup() != null) {
                if (!p.getEthnicGroup().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.ethnicGroup LIKE UPPER('").append(p.getEthnicGroup().replaceAll("'", "''")).append("')");
            }
            if (p.getPatientComments() != null) {
                if (!p.getPatientComments().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.patientComments LIKE UPPER('").append(p.getPatientComments().replaceAll("'", "''")).append("')");
            }
            if (p.getPatientAddress() != null) {
                if (!p.getPatientAddress().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.patientAddress LIKE UPPER('").append(p.getPatientAddress().replaceAll("'", "''")).append("')");
            }
            if (p.getNumberOfPatientRelatedStudies() != null) { // If a value is specifiedsider it, otherwise I just want to get it as a result!
                if (!p.getNumberOfPatientRelatedStudies().equals(DicomConstants.FIND_NUMBER_OF_RELATED))
                    sb.append(" AND PatientDemographics.numberOfPatientRelatedStudies=").append(p.getNumberOfPatientRelatedStudies());
            }
            if (p.getPatientState() != null) {
                if (!p.getPatientState().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.patientState LIKE UPPER('").append(p.getPatientState()).append("')");
            }
            if (p.getPregnancyStatus() != null) {
                if (!p.getPregnancyStatus().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.pregnancyStatus=").append(p.getPregnancyStatus());
            }
            if (p.getMedicalAlerts() != null) {
                if (!p.getMedicalAlerts().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.medicalAlerts LIKE ('").append(p.getMedicalAlerts()).append("')");
            }
            if (p.getPatientWeight() != null) {
                if (!p.getPatientWeight().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.patientWeight=").append(p.getPatientWeight());
            }
            if (p.getConfidentialityConstraint() != null) {
                if (!p.getConfidentialityConstraint().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.confidentialityConstOnPatData LIKE UPPER('").append(p.getConfidentialityConstraint()).append("')");
            }
            if (p.getSpecialNeeds() != null) {
                if (!p.getSpecialNeeds().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.specialNeeds LIKE UPPER('").append(p.getSpecialNeeds()).append("')");
            }
            if (p.getAssignedPatientLocation() != null) {
                if (!p.getAssignedPatientLocation().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.assignedPatientLocation LIKE UPPER('").append(p.getAssignedPatientLocation()).append("')");
            }
        } // end if(p!=null)
        // Now deal with Study info:
        if (st != null) {
            if ((st.getStudyInstanceUid() != null) && (!st.getStudyInstanceUid().equals(""))) {
                // Only a single UID was specified for matching!
                if (!st.getStudyInstanceUid().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND Studies.studyInstanceUID='").append(st.getStudyInstanceUid()).append("'");
            } else if (st.getUidsToMatch() != null) { // A list of UIDs was
                // required for
                // matching!
                String[] rUids = st.getUidsToMatch();
                sb.append(" AND (");
                for (int k = rUids.length - 1; k >= 0; k--) {
                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
                        sb.append("Studies.studyInstanceUID='").append(rUids[k]).append("'");
                    if (k > 0)
                        sb.append(" OR ");
                } // end for
                sb.append(")");
            } // end UIDs
            if (st.getStudyId() != null) {
                if (!st.getStudyId().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND studyID = UPPER('").append(st.getStudyId()).append("')");
            }
            if (st.getStudyStatusId() != null) {
                if (!st.getStudyStatusId().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND studyStatusID = UPPER('").append(st.getStudyStatusId()).append("')");
            }
            String studyDate = st.getStudyDateString();
            if (studyDate != null) {
                if (studyDate.contains("-")) {
                    studyDate.replace(" ", "");
                    if (studyDate.startsWith("-")) {
                        sb.append(" AND Studies.studyDate <= '" + st.getStudyDateLate() + "' ");
                    } else if (studyDate.endsWith("-")) {
                        sb.append(" AND Studies.studyDate >= '" + st.getStudyDate() + "' ");
                    } else {
                        sb.append(" AND Studies.studyDate >= '" + st.getStudyDate() + "' ");
                        sb.append(" AND Studies.studyDate <= '" + st.getStudyDateLate() + "' ");
                    }
                } else {
                    sb.append(" SPSStartDate='" + st.getStudyDate() + "' AND");
                }
            }
            if (st.getStudyTime() != null) {
                if (st.getStudyTimeLate() == null)
                    sb.append((oracle) ? " AND to_char(Studies.studyTime, 'HH24:MI:SS')='" : " AND Studies.studyTime='").append(st.getStudyTime()).append("'");
                else
                    sb.append((oracle) ? " AND to_char(Studies.studyTime, 'HH24:MI:SS')>='" : " AND Studies.studyTime>='").append(st.getStudyTime()).append((oracle) ? "' AND to_char(Studies.studyTime, 'HH24:MI:SS')<='" : "' AND Studies.studyTime<='").append(st.getStudyTimeLate()).append("'");
            }
            if (st.getStudyCompletionDate() != null) {
                if (st.getStudyCompletionDateLate() == null)
                    sb.append(" AND Studies.studyCompletionDate='").append(st.getStudyCompletionDate()).append("'");
                else
                    sb.append(" AND Studies.studyCompletionDate>='").append(st.getStudyCompletionDate()).append("' AND Studies.studyCompletionDate<='").append(st.getStudyCompletionDateLate()).append("'");
            }
            if (st.getStudyCompletionTime() != null) {
                if (st.getStudyCompletionTimeLate() == null)
                    sb.append((oracle) ? " AND to_char(Studies.studyCompletionTime, 'HH24:MI:SS')='" : " AND Studies.studyCompletionTime='").append(st.getStudyCompletionTime()).append("'");
                else
                    sb.append((oracle) ? " AND to_char(Studies.studyCompletionTime, 'HH24:MI:SS')>='" : " AND Studies.studyCompletionTime>='").append(st.getStudyCompletionTime()).append((oracle) ? "' AND to_char(Studies.studyCompletionTime, 'HH24:MI:SS')<='" : "' AND Studies.studyCompletionTime<='").append(st.getStudyCompletionTimeLate()).append("'");
            }
            if (st.getStudyVerifiedDate() != null) {
                if (st.getStudyVerifiedDateLate() == null)
                    sb.append(" AND Studies.studyVerifiedDate='").append(st.getStudyVerifiedDate()).append("'");
                else
                    sb.append(" AND Studies.studyVerifiedDate>='").append(st.getStudyVerifiedDate()).append("' AND Studies.studyVerifiedDate<='").append(st.getStudyVerifiedDateLate()).append("'");
            }
            if (st.getStudyVerifiedTime() != null) {
                if (st.getStudyVerifiedTimeLate() == null)
                    sb.append((oracle) ? " AND to_char(Studies.studyVerifiedTime, 'HH24:MI:SS')='" : " AND Studies.studyVerifiedTime='").append(st.getStudyVerifiedTime()).append("'");
                else
                    sb.append((oracle) ? " AND to_char(Studies.studyVerifiedTime, 'HH24:MI:SS')>='" : " AND Studies.studyVerifiedTime>='").append(st.getStudyVerifiedTime()).append((oracle) ? "' AND to_char(Studies.studyVerifiedTime, 'HH24:MI:SS')<='" : "' AND Studies.studyVerifiedTime<='").append(st.getStudyVerifiedTimeLate()).append("'");
            }
            if (st.getAccessionNumber() != null) {
                if (!st.getAccessionNumber().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND accessionNumber LIKE UPPER('").append(st.getAccessionNumber()).append("')");
            }
            if (st.getStudyDescription() != null) {
                if (!st.getStudyDescription().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND studyDescription LIKE UPPER('").append(st.getStudyDescription()).append("')");
            }
            if (st.getReferringPhysiciansName() != null) {
                if (!st.getReferringPhysiciansName().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND COALESCE(UPPER(referringPhysiciansName),' ') LIKE UPPER('").append(st.getReferringPhysiciansName()).append("')");
            }
            if (st.getAdmittingDiagnosesDescription() != null) {
                if (!st.getAdmittingDiagnosesDescription().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND COALESCE(UPPER(admittingDiagnosesDescription),' ') LIKE UPPER('").append(st.getAdmittingDiagnosesDescription()).append("')");
            }
        }
        // Now to deal with Series:
        if (se != null) {
            if (se.getSeriesInstanceUid() != null) { // Only a single UID was specified for matching!
                if (!se.getSeriesInstanceUid().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND Series.seriesInstanceUID='").append(se.getSeriesInstanceUid()).append("'");
            } else if (se.getUidsToMatch() != null) { // A list of UIDs was required for matching!
                String[] rUids = se.getUidsToMatch();
                sb.append(" AND (");
                for (int k = rUids.length - 1; k >= 0; k--) {
                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
                        sb.append("Series.seriesInstanceUID='").append(rUids[k]).append("'");
                    if (k > 0)
                        sb.append(" OR ");
                } // end for
                sb.append(")");
            } // end UIDs
            if (se.getSeriesNumber() != null) {
                if (!se.getSeriesNumber().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND Series.seriesNumber LIKE ").append(se.getSeriesNumber());
            }
            if (se.getModality() != null) {
                String modality = se.getModality();
                if (!(modality.contains(","))) {
                    if (!se.getModality().equals(DicomConstants.UNIVERSAL_MATCHING))
                        sb.append(" AND Series.modality LIKE '").append(se.getModality()).append("'");
                } else {
                    String[] modalities = modality.split(",");
                    sb.append(" AND (");
                    for (int i = 0; i < modalities.length; i++) {
                        if (!modalities[i].equals(DicomConstants.UNIVERSAL_MATCHING))
                            sb.append("Series.modality LIKE '").append(modalities[i]).append("'");
                        if ((modalities.length - 1) != i)
                            sb.append(" OR ");
                    }
                    sb.append(")");
                }
            }
            if (se.getBodyPartExamined() != null) {
                if (!se.getBodyPartExamined().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND Series.bodyPartExamined LIKE UPPER('").append(se.getBodyPartExamined()).append("')");
            }
            if (se.getNumberOfSeriesRelatedInstances() != null) {
                if (!se.getNumberOfSeriesRelatedInstances().equals(DicomConstants.FIND_NUMBER_OF_RELATED))
                    sb.append(" AND Series.numberOfSeriesRelatedInstances=").append(se.getNumberOfSeriesRelatedInstances());
            }
            // added by marco for series description
            if (se.getSeriesDescription() != null) {
                if (!se.getSeriesDescription().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND SeriesDescription LIKE UPPER('").append(se.getSeriesDescription()).append("')");
            }
            if(se.getOperatorsName() != null){
                if((!se.getOperatorsName().equals(DicomConstants.UNIVERSAL_MATCHING)) && (!DicomConstants.MULTIPLE_WC_MATCHING.equals(se.getOperatorsName())))
                    sb.append(" AND Series.operatorsName LIKE UPPER('").append(se.getOperatorsName()).append("')");
            }
            // ------------------------------------------------------------------------------
            if (se.getEquipment() != null) {
                Equipment e = se.getEquipment();
                if (e.getManufacturer() != null)
                    if (!e.getManufacturer().equals(DicomConstants.UNIVERSAL_MATCHING))
                        sb.append(" AND UPPER(Equipment.manufacturer) LIKE UPPER('").append(e.getManufacturer()).append("')");
                if (e.getInstitutionName() != null)
                    if (!e.getInstitutionName().equals(DicomConstants.UNIVERSAL_MATCHING))
                        sb.append(" AND UPPER(Equipment.institutionName) LIKE UPPER('").append(e.getInstitutionName()).append("')");
                if (e.getStationName() != null)
                    if (!e.getStationName().equals(DicomConstants.UNIVERSAL_MATCHING))
                        sb.append(" AND UPPER(Equipment.stationName) LIKE UPPER('").append(e.getStationName()).append("')");
                if (e.getInstitutionalDepartmentName() != null)
                    if (!e.getInstitutionalDepartmentName().equals(DicomConstants.UNIVERSAL_MATCHING))
                        sb.append(" AND UPPER(Equipment.institutionalDepartmentName) LIKE UPPER('").append(e.getInstitutionalDepartmentName()).append("')");
                if (e.getManufacturersModelName() != null)
                    if (!e.getManufacturersModelName().equals(DicomConstants.UNIVERSAL_MATCHING))
                        sb.append(" AND UPPER(Equipment.manufacturersModelName) LIKE UPPER('").append(e.getManufacturersModelName()).append("')");
                if (e.getDeviceSerialNumber() != null)
                    if (!e.getDeviceSerialNumber().equals(DicomConstants.UNIVERSAL_MATCHING))
                        sb.append(" AND Equipment.deviceSerialNumber = UPPER('").append(e.getDeviceSerialNumber()).append("')");
                if (e.getConversionType() != null)
                    if (!e.getConversionType().equals(DicomConstants.UNIVERSAL_MATCHING))
                        sb.append(" AND (Equipment.conversionType) = UPPER('").append(e.getConversionType()).append("')");
                if (e.getSecondaryCaptureDeviceId() != null)
                    if (!e.getSecondaryCaptureDeviceId().equals(DicomConstants.UNIVERSAL_MATCHING))
                        sb.append(" AND (Equipment.secondaryCaptureDeviceId) = UPPER('").append(e.getSecondaryCaptureDeviceId()).append("')");
                if (e.getDateOfLastCalibration() != null) {
                    if (e.getDateOfLastCalibrationLate() == null)
                        sb.append(" AND Equipment.dateOfLastCalibration='").append(e.getDateOfLastCalibration()).append("'");
                    else
                        sb.append(" AND Equipment.dateOfLastCalibration>='").append(e.getDateOfLastCalibration()).append("' AND Equipment.dateOfLastCalibration<='").append(e.getDateOfLastCalibrationLate()).append("'");
                } // end if
                if (e.getTimeOfLastCalibration() != null) {
                    if (e.getTimeOfLastCalibrationLate() == null)
                        sb.append((oracle) ? " AND to_char(Equipment.timeOfLastCalibration, 'HH24:MI:SS')='" : " AND Equipment.timeOfLastCalibration='").append(e.getTimeOfLastCalibration()).append("'");
                    else
                        sb.append((oracle) ? " AND to_char(Equipment.timeOfLastCalibration, 'HH24:MI:SS')>='" : " AND Equipment.timeOfLastCalibration>='").append(e.getTimeOfLastCalibration()).append((oracle) ? "' AND to_char(Equipment.timeOfLastCalibration, 'HH24:MI:SS')<='" : "' AND Equipment.timeOfLastCalibration<='").append(e.getTimeOfLastCalibrationLate()).append("'");
                } // end if
            }
        } // end if(se!=null)
        // Now to deal with wildcards: * -> % , ? -> _ - these characters are not to be used in a normal query!!!
        String arg = ((sb.toString()).replaceAll("\\" + DicomConstants.MULTIPLE_WC_MATCHING, "%")).replaceAll("\\" + DicomConstants.CHARACTER_WC_MATCHING, "_");
        return arg + orderSeriesEnd;
    } // end preparePRPStSe()

    /**
     * Prepare PRP st.
     * 
     * @param p
     *            the p
     * @param st
     *            the st
     * @param callingAETitle
     *            the calling AE title
     * @param depEnabled
     *            the dep enabled
     * @param limit
     *            the limit
     * @return the string
     */
    private String preparePRPSt(Patient p, Study st, String callingAETitle, boolean depEnabled, int limit) {
        StringBuffer sbField = new StringBuffer(2000); // It starts from a basis of 1504!
        StringBuffer sbTable = new StringBuffer(2000);
        StringBuffer sbWhere = new StringBuffer(2000);
        
        // prepare mandatory query
        sbField.append(patientRootMatchPStMandatoryFieldsSelect); // Start the query with the basic requirements
        sbTable.append(patientRootMatchPStMandatoryFieldsFromTables); // Start the query with the basic requirements
        sbWhere.append(patientRootMatchPStMandatoryFieldsWhere); // Start the query with the basic requirements dealing from clause
        
        if(depEnabled)
        	sbTable.append(", KnownNodes ");

        // dealing where clause
        // Deal with Patient info: mandatory part
        if (p != null) {
            if (p.getLastName() != null) {
                if (!p.getLastName().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (Patients.lastName) LIKE UPPER('").append(p.getLastName().replaceAll("'", "''")).append("')");
            }
            if (p.getFirstName() != null) {
                if (!p.getFirstName().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (Patients.firstName) LIKE UPPER('").append(p.getFirstName().replaceAll("'", "''")).append("')");
            }
            if (p.getMiddleName() != null) {
                if (!p.getMiddleName().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (Patients.middleName) LIKE UPPER('").append(p.getMiddleName().replaceAll("'", "''")).append("')");
            }
            if (p.getPrefix() != null) {
                if (!p.getPrefix().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (Patients.prefix) = UPPER('").append(p.getPrefix().replaceAll("'", "''")).append("')");
            }
            if (p.getSuffix() != null) {
                if (!p.getSuffix().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (Patients.suffix) = ('").append(p.getSuffix().replaceAll("'", "''")).append("')");
            }
            String patBirthDate = p.getBirthDateAsString();
            if(patBirthDate != null){
                if(patBirthDate.contains("-")){
                    patBirthDate.replace(" ", "");
                    if(patBirthDate.startsWith("-")){
                        sbWhere.append(" AND Patients.birthDate<='").append(p.getBirthDateLate()).append("'");
                    }else if(patBirthDate.endsWith("-")){
                        sbWhere.append(" AND Patients.birthDate>='").append(p.getBirthDate()).append("'");
                    }else{
                        sbWhere.append(" AND Patients.birthDate>='").append(p.getBirthDate()).append("'");
                        sbWhere.append(" AND Patients.birthDate<='").append(p.getBirthDateLate()).append("'");
                    }
                    
                }else{
                    sbWhere.append(" AND Patients.birthDate='").append(p.getBirthDate()).append("'");
                }
            }
            if (p.getBirthTime() != null) {
                if (p.getBirthTimeLate() == null)
                    sbWhere.append((oracle) ? " AND to_char(Patients.birthTime, 'HH24:MI:SS')='" : " AND Patients.birthTime='").append(p.getBirthTime()).append("'");
                else
                    sbWhere.append((oracle) ? " AND to_char(Patients.birthTime, 'HH24:MI:SS')>='" : " AND Patients.birthTime>='").append(p.getBirthTime()).append((oracle) ? "' AND to_char(Patients.birthTime, 'HH24:MI:SS')<='" : "' AND Patients.birthTime<='").append(p.getBirthTimeLate()).append("'");
            }
            if (p.getSex() != null) {
                if (!p.getSex().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (Patients.sex) LIKE UPPER('").append(p.getSex()).append("')");
            }
            if (p.getPatientId() != null) {
                if (!p.getPatientId().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (Patients.patientID) LIKE UPPER('").append(p.getPatientId().replace("'","''")).append("')");
            }
            if (p.getIdIssuer() != null) {
                if (!p.getIdIssuer().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (Patients.idIssuer) LIKE UPPER('").append(p.getIdIssuer().replace("'","''")).append("')");
            }
        }
        // Now deal with Study info:
        if (st != null) {
            if ((st.getStudyInstanceUid() != null) && (!st.getStudyInstanceUid().equals(""))) { // Only a single UID was
                // specified for matching!
                if (!st.getStudyInstanceUid().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND Studies.studyInstanceUID='").append(st.getStudyInstanceUid()).append("'");
            } else if (st.getUidsToMatch() != null) { // A list of UIDs was required for matching!
                String[] rUids = st.getUidsToMatch();
                sbWhere.append(" AND (");
                for (int k = rUids.length - 1; k >= 0; k--) {
                    if (!rUids[k].equals(DicomConstants.UNIVERSAL_MATCHING))
                        sbWhere.append("Studies.studyInstanceUID='").append(rUids[k]).append("'");
                    if (k > 0)
                        sbWhere.append(" OR ");
                } // end for
                sbWhere.append(")");
            } // end UIDs
            if (st.getStudyId() != null) {
                if (!st.getStudyId().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND studyID LIKE UPPER('").append(st.getStudyId()).append("')");
            }
            if (st.getStudyStatusId() != null) {
                if (!st.getStudyStatusId().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND studyStatusID LIKE UPPER('").append(st.getStudyStatusId()).append("')");
            }
            String studyDate = st.getStudyDateString();
            if (studyDate != null) {
                if (studyDate.contains("-")) {
                    studyDate.replace(" ", "");
                    if (studyDate.startsWith("-")) {
                        sbWhere.append(" AND Studies.studyDate <= '" + st.getStudyDateLate() + "' ");
                    } else if (studyDate.endsWith("-")) {
                        sbWhere.append(" AND Studies.studyDate >= '" + st.getStudyDate() + "' ");
                    } else {
                        sbWhere.append(" AND Studies.studyDate >= '" + st.getStudyDate() + "' ");
                        sbWhere.append(" AND Studies.studyDate <= '" + st.getStudyDateLate() + "' ");
                    }
                } else {
                    sbWhere.append(" AND Studies.studyDate = '" + st.getStudyDate() + "' ");
                }
            }
            if (st.getStudyTime() != null) {
                if (st.getStudyTimeLate() == null)
                    sbWhere.append((oracle) ? " AND to_char(Studies.studyTime, 'HH24:MI:SS')='" : " AND Studies.studyTime='").append(st.getStudyTime()).append("'");
                else
                    sbWhere.append((oracle) ? " AND to_char(Studies.studyTime, 'HH24:MI:SS')>='" : " AND Studies.studyTime>='").append(st.getStudyTime()).append((oracle) ? "' AND to_char(Studies.studyTime, 'HH24:MI:SS')<='" : "' AND Studies.studyTime<='").append(st.getStudyTimeLate()).append("'");
            }
            if (st.getStudyCompletionDate() != null) {
                if (st.getStudyCompletionDateLate() == null)
                    sbWhere.append(" AND Studies.studyCompletionDate='").append(st.getStudyCompletionDate()).append("'");
                else
                    sbWhere.append(" AND Studies.studyCompletionDate>='").append(st.getStudyCompletionDate()).append("' AND Studies.studyCompletionDate<='").append(st.getStudyCompletionDateLate()).append("'");
            }
            if (st.getStudyCompletionTime() != null) {
                if (st.getStudyCompletionTimeLate() == null)
                    sbWhere.append((oracle) ? " AND to_char(Studies.studyCompletionTime, 'HH24:MI:SS')='" : " AND Studies.studyCompletionTime='").append(st.getStudyCompletionTime()).append("'");
                else
                    sbWhere.append((oracle) ? " AND to_char(Studies.studyCompletionTime, 'HH24:MI:SS')>='" : " AND Studies.studyCompletionTime>='").append(st.getStudyCompletionTime()).append((oracle) ? "' AND to_char(Studies.studyCompletionTime, 'HH24:MI:SS')<='" : "' AND Studies.studyCompletionTime<='").append(st.getStudyCompletionTimeLate()).append("'");
            }
            if (st.getStudyVerifiedDate() != null) {
                if (st.getStudyVerifiedDateLate() == null)
                    sbWhere.append(" AND Studies.studyVerifiedDate='").append(st.getStudyVerifiedDate()).append("'");
                else
                    sbWhere.append(" AND Studies.studyVerifiedDate>='").append(st.getStudyVerifiedDate()).append("' AND Studies.studyVerifiedDate<='").append(st.getStudyVerifiedDateLate()).append("'");
            }
            if (st.getStudyVerifiedTime() != null) {
                if (st.getStudyVerifiedTimeLate() == null)
                    sbWhere.append((oracle) ? " AND to_char(Studies.studyVerifiedTime, 'HH24:MI:SS')='" : " AND Studies.studyVerifiedTime='").append(st.getStudyVerifiedTime()).append("'");
                else
                    sbWhere.append((oracle) ? " AND to_char(Studies.studyVerifiedTime, 'HH24:MI:SS')>='" : " AND Studies.studyVerifiedTime>='").append(st.getStudyVerifiedTime()).append((oracle) ? "' AND to_char(Studies.studyVerifiedTime, 'HH24:MI:SS')<='" : "' AND Studies.studyVerifiedTime<='").append(st.getStudyVerifiedTimeLate()).append("'");
            }
            if (st.getAccessionNumber() != null) {
                if (!st.getAccessionNumber().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (accessionNumber) LIKE UPPER('").append(st.getAccessionNumber()).append("')");
            }
            if (st.getStudyDescription() != null) {
                if (!st.getStudyDescription().equals(DicomConstants.UNIVERSAL_MATCHING))
                    if (st.getStudyDescription().contains("*")) {
                        sbWhere.append(" AND (studyDescription) LIKE ('").append(st.getStudyDescription().replaceAll("'", "''")).append("')");
                    } else
                        sbWhere.append(" AND (studyDescription) = UPPER('").append(st.getStudyDescription()).append("')");
            }
            if (st.getReferringPhysiciansName() != null) {
                if (!st.getReferringPhysiciansName().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND COALESCE(UPPER(referringPhysiciansName),' ') LIKE UPPER('").append(st.getReferringPhysiciansName()).append("')");
            }
            if (st.getAdmittingDiagnosesDescription() != null) {
                if (!st.getAdmittingDiagnosesDescription().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND COALESCE(UPPER(admittingDiagnosesDescription),' ') LIKE UPPER('").append(st.getAdmittingDiagnosesDescription()).append("')");
            }
            if ((st.getModalitiesInStudy() != null)) {
                String[] tmis = st.getModalitiesInStudy();
                // Sandro Version
                if (tmis.length != 0)
                    sbWhere.append(" AND (");
                for (int i = 0; i < tmis.length; i++) {
                    String[] modalities = tmis[i].split(",");
                    for (int j = 0; j < modalities.length; j++) {
                        if (!modalities[j].equals(DicomConstants.UNIVERSAL_MATCHING))
                            sbWhere.append("Series.modality='").append(modalities[j]).append("'");
                        if ((modalities.length - 1) != j)
                            sbWhere.append(" OR ");
                    }
                    if ((tmis.length - 1) != i)
                        sbWhere.append(" OR ");
                }
                if (tmis.length != 0)
                    sbWhere.append(")");
                // Sandro Version End
            }
        }
        // ----------------------------------------------OPTIONAL DATA WHERE
        boolean optional = false;
        if (p != null) {
            if (p.getEthnicGroup() != null) {
                if (!p.getEthnicGroup().equals(DicomConstants.UNIVERSAL_MATCHING)) {
                    sbWhere.append(" AND (PatientDemographics.ethnicGroup) = UPPER('").append(p.getEthnicGroup().replaceAll("'", "''")).append("')");
                    optional = true;
                }
            }
            if (p.getPatientComments() != null) {
                if (!p.getPatientComments().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (PatientDemographics.patientComments) = UPPER('").append(p.getPatientComments().replaceAll("'", "''")).append("')");
                optional = true;
            }
            if (p.getPatientAddress() != null) {
                if (!p.getPatientAddress().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (PatientDemographics.patientAddress) = UPPER('").append(p.getPatientAddress().replaceAll("'", "''")).append("')");
                optional = true;
            }
            if (p.getNumberOfPatientRelatedStudies() != null) { // If a value is specified consider it, otherwise
                // I just want to get it as a result!
                if (!p.getNumberOfPatientRelatedStudies().equals(DicomConstants.FIND_NUMBER_OF_RELATED))
                    sbWhere.append(" AND PatientDemographics.numberOfPatientRelatedStudies=").append(p.getNumberOfPatientRelatedStudies());
                optional = true;
            }
            if (p.getPatientState() != null) {
                if (!p.getPatientState().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (PatientDemographics.patientState) = UPPER('").append(p.getPatientState()).append("')");
                optional = true;
            }
            if (p.getPregnancyStatus() != null) {
                if (!p.getPregnancyStatus().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND PatientDemographics.pregnancyStatus=").append(p.getPregnancyStatus());
                optional = true;
            }
            if (p.getMedicalAlerts() != null) {
                if (!p.getMedicalAlerts().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (PatientDemographics.medicalAlerts) = UPPER('").append(p.getMedicalAlerts()).append("')");
                optional = true;
            }
            if (p.getPatientWeight() != null) {
                if (!p.getPatientWeight().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND PatientDemographics.patientWeight=").append(p.getPatientWeight());
                optional = true;
            }
            if (p.getConfidentialityConstraint() != null) {
                if (!p.getConfidentialityConstraint().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (PatientDemographics.confidentialityConstOnPatData) = UPPER('").append(p.getConfidentialityConstraint()).append("')");
                optional = true;
            }
            if (p.getSpecialNeeds() != null) {
                if (!p.getSpecialNeeds().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (PatientDemographics.specialNeeds) = UPPER('").append(p.getSpecialNeeds()).append("')");
                optional = true;
            }
            if (p.getAssignedPatientLocation() != null) {
                if (!p.getAssignedPatientLocation().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sbWhere.append(" AND (PatientDemographics.assignedPatientLocation) = UPPER('").append(p.getAssignedPatientLocation()).append("')");
                optional = true;
            }
        }
        if (optional) {
            sbField.append(patientRootMatchPStOptionalPatientDataFields); // Start the query with the basic requirements
            sbTable.append(patientRootMatchPStOptionalPatientDataTable); // Start the query with the basic requirements
            sbWhere.append(patientRootMatchPStOptionalPatientDataWhere);
        }
        // ----------------------------end optional
        if ((st != null) && (st.getProcedureCodeSequence() != null)) {
            sbField.append(patientRootMatchPStCodesequenceFields); // Start the query with the basic requirements
            sbTable.append(patientRootMatchPStCodesequenceTables); // Start the query with the basic requirements
            sbWhere.append(patientRootMatchPStCodesequenceWhere);
        }
        if (depEnabled) {
            // I USED FIELD "INSTITUTIONALDEPARTMENTNAME FOR THE QUERY
            sbWhere.append(" AND Series.knownNodeFk=KnownNodes.pk AND KnownNodes.AETitle='"+callingAETitle+"' ");
        }
        
        // Check studies move not completed
        //sbWhere.append(" AND Studies.accessionNumber NOT IN (SELECT accessionNumber FROM MoveStudyHistory WHERE endMov is NULL)");
        sbWhere.append(" AND studies.accessionnumber NOT IN (SELECT mvinner.accessionnumber FROM movestudyhistory mvinner ");
        sbWhere.append(" WHERE mvinner.accessionnumber = studies.accessionnumber AND mvinner.eventtime =  ");
        sbWhere.append(" ( SELECT MAX(mv.eventtime) FROM movestudyhistory mv WHERE mv.accessionnumber = studies.accessionnumber AND IDRETRY <= (SELECT paramValue FROM GLOBALCONFIGURATION WHERE paramKey = 'MoveStudyMaxRetry') ) AND endmov IS NULL )");
        
        sbWhere.append(" ORDER BY Studies.studydate DESC, Studies.studytime DESC");

        StringBuffer sb = new StringBuffer(3000);
        sb.append(sbField).append(sbTable).append(sbWhere);
        // -----------
        String arg = ((sb.toString()).replaceAll("\\" + DicomConstants.MULTIPLE_WC_MATCHING, "%")).replaceAll("\\" + DicomConstants.CHARACTER_WC_MATCHING, "_");
        // log.info(arg);
        return arg;
    } // end preparePRPSt()

    /**
     * Prepare PRP.
     * 
     * @param p
     *            the p
     * @param limit
     *            the limit
     * @return the string
     */
    private String preparePRP(Patient p, int limit) {
        StringBuffer sb = new StringBuffer(1400); 
        sb.append(patientRootMatchP);
        if (p != null) {
            if (p.getLastName() != null) {
                if (!p.getLastName().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.lastName) LIKE UPPER('").append(p.getLastName().replaceAll("'", "''")).append("')");
            }
            if (p.getFirstName() != null) {
                if (!p.getFirstName().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.firstName) LIKE UPPER('").append(p.getFirstName().replaceAll("'", "''")).append("')");
            }
            if (p.getMiddleName() != null) {
                if (!p.getMiddleName().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.middleName) LIKE UPPER('").append(p.getMiddleName().replaceAll("'", "''")).append("')");
            }
            if (p.getPrefix() != null) {
                if (!p.getPrefix().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.prefix) LIKE UPPER('").append(p.getPrefix().replaceAll("'", "''")).append("')");
            }
            if (p.getSuffix() != null) {
                if (!p.getSuffix().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.suffix) LIKE UPPER('").append(p.getSuffix().replaceAll("'", "''")).append("')");
            }
            String patBirthDate = p.getBirthDateAsString();
            if(patBirthDate != null){
                if(patBirthDate.contains("-")){
                    patBirthDate.replace(" ", "");
                    if(patBirthDate.startsWith("-")){
                        sb.append(" AND Patients.birthDate<='").append(p.getBirthDateLate()).append("'");
                    }else if(patBirthDate.endsWith("-")){
                        sb.append(" AND Patients.birthDate>='").append(p.getBirthDate()).append("'");
                    }else{
                        sb.append(" AND Patients.birthDate>='").append(p.getBirthDate()).append("'");
                        sb.append(" AND Patients.birthDate<='").append(p.getBirthDateLate()).append("'");
                    }
                    
                }else{
                    sb.append(" AND Patients.birthDate='").append(p.getBirthDate()).append("'");
                }
            }
            if (p.getBirthTime() != null) {
                if (p.getBirthTimeLate() == null)
                    sb.append((oracle) ? " AND to_char(Patients.birthTime, 'HH24:MI:SS')='" : " AND Patients.birthTime='").append(p.getBirthTime()).append("'");
                else
                    sb.append((oracle) ? " AND to_char(Patients.birthTime, 'HH24:MI:SS')>='" : " AND Patients.birthTime>='").append(p.getBirthTime()).append((oracle) ? "' AND to_char(Patients.birthTime, 'HH24:MI:SS')<='" : "' AND Patients.birthTime<='").append(p.getBirthTimeLate()).append("'");
            }
            if (p.getSex() != null) {
                if (!p.getSex().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.sex) LIKE UPPER('").append(p.getSex()).append("')");
            }
            if (p.getPatientId() != null) {
                if (!p.getPatientId().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.patientID) LIKE UPPER('").append(p.getPatientId().replace("'","''")).append("')");
            }
            if (p.getIdIssuer() != null) {
                if (!p.getIdIssuer().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (Patients.idIssuer) LIKE UPPER('").append(p.getIdIssuer().replace("'","''")).append("')");
            }
            if (p.getEthnicGroup() != null) {
                if (!p.getEthnicGroup().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (PatientDemographics.ethnicGroup) LIKE UPPER('").append(p.getEthnicGroup().replaceAll("'", "''")).append("')");
            }
            if (p.getPatientComments() != null) {
                if (!p.getPatientComments().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (PatientDemographics.patientComments) LIKE UPPER('").append(p.getPatientComments().replaceAll("'", "''")).append("')");
            }
            if (p.getPatientAddress() != null) {
                if (!p.getPatientAddress().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (PatientDemographics.patientAddress) LIKE UPPER('").append(p.getPatientAddress().replaceAll("'", "''")).append("')");
            }
            if (p.getNumberOfPatientRelatedStudies() != null) { // If a value is specified consider it, otherwise
                // I just want to get it as a result!
                if (!p.getNumberOfPatientRelatedStudies().equals(DicomConstants.FIND_NUMBER_OF_RELATED))
                    sb.append(" AND PatientDemographics.numberOfPatientRelatedStudies=").append(p.getNumberOfPatientRelatedStudies());
            }
            if (p.getPatientState() != null) {
                if (!p.getPatientState().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (PatientDemographics.patientState) LIKE UPPER('").append(p.getPatientState()).append("')");
            }
            if (p.getPregnancyStatus() != null) {
                if (!p.getPregnancyStatus().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.pregnancyStatus=").append(p.getPregnancyStatus());
            }
            if (p.getMedicalAlerts() != null) {
                if (!p.getMedicalAlerts().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (PatientDemographics.medicalAlerts) LIKE UPPER('").append(p.getMedicalAlerts()).append("')");
            }
            if (p.getPatientWeight() != null) {
                if (!p.getPatientWeight().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND PatientDemographics.patientWeight=").append(p.getPatientWeight());
            }
            if (p.getConfidentialityConstraint() != null) {
                if (!p.getConfidentialityConstraint().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (PatientDemographics.confidentialityConstOnPatData) LIKE UPPER('").append(p.getConfidentialityConstraint()).append("')");
            }
            if (p.getSpecialNeeds() != null) {
                if (!p.getSpecialNeeds().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (PatientDemographics.specialNeeds) LIKE UPPER('").append(p.getSpecialNeeds()).append("')");
            }
            if (p.getAssignedPatientLocation() != null) {
                if (!p.getAssignedPatientLocation().equals(DicomConstants.UNIVERSAL_MATCHING))
                    sb.append(" AND (PatientDemographics.assignedPatientLocation) LIKE UPPER('").append(p.getAssignedPatientLocation()).append("')");
            }
        } // end if(p!=null)
        // sb.append(" LIMIT " + limit);
        // Now to deal with wildcards: * -> % , ? -> _ - these characters are
        // not to be used in a normal query!!!
        String arg = ((sb.toString()).replaceAll("\\" + DicomConstants.MULTIPLE_WC_MATCHING, "%")).replaceAll("\\" + DicomConstants.CHARACTER_WC_MATCHING, "_");
        // log.info(arg);
        return arg;
    } // end preparePRP()

    /**
     * Find number of patient related series.
     * 
     * @param patientPK
     *            the patient PK
     * @return the long
     * @throws SQLException
     *             the SQL exception
     */
    private long findNumberOfPatientRelatedSeries(long patientPK) throws SQLException {
    	Connection con = null;
    	Statement stat = null;
    	ResultSet rs = null;
                
        try{
            con = dataSource.getConnection();
            oracle = Dbms.isOracle(con);
            modDateFormat(con);
            stat = con.createStatement();
            stat.setMaxRows(0);
            rs = stat.executeQuery("SELECT COUNT(Series.seriesInstanceUID) FROM Series WHERE Series.studyFK IN (SELECT Studies.studyInstanceUID FROM Studies WHERE Studies.patientFK=" + patientPK + ")");
            if (rs.next()) {
                long result = rs.getLong(1);
                return result;
            }
        }catch (SQLException e) {
            throw e;
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);        	
        }
        return 0;
    } // end findNumberOfPatientRelatedSeries()

    /**
     * Find number of patient related instances.
     * 
     * @param patientPK
     *            the patient PK
     * @return the long
     * @throws SQLException
     *             the SQL exception
     */
    private long findNumberOfPatientRelatedInstances(long patientPK) throws SQLException {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            oracle = Dbms.isOracle(con);
            modDateFormat(con);
            stat = con.createStatement();
            stat.setMaxRows(0);
            rs = stat.executeQuery("SELECT SUM(Series.numberOfSeriesRelatedInstances) FROM Series WHERE Series.studyFK IN (SELECT Studies.studyInstanceUID FROM Studies WHERE Studies.patientFK=" + patientPK + ")");
            if (rs.next()) {
                long result = rs.getLong(1);
                return result;
            }
        } catch (SQLException e) {
            throw e;
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);    
        }
        return 0;
    } // end findNumberOfPatientRelatedInstances()

    /**
     * Find number of study related series.
     * 
     * @param studyUid
     *            the study uid
     * @return the long
     * @throws SQLException
     *             the SQL exception
     */
    private long findNumberOfStudyRelatedSeries(String studyUid) throws SQLException {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        
        try{
            con = dataSource.getConnection();
            oracle = Dbms.isOracle(con);
            modDateFormat(con);
            stat = con.createStatement();
            stat.setMaxRows(0);
            rs = stat.executeQuery("SELECT COUNT(seriesInstanceUID) FROM Series WHERE studyFK='" + studyUid + "' AND Series.deprecated=0");
            if (rs.next()) {
                long result = rs.getLong(1);
                return result;
            }
        } catch (SQLException e) {
            throw e;
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);    
        }
        return 0;
    }

    /**
     * Find number of study related instances.
     * 
     * @param studyUid
     *            the study uid
     * @return the long
     * @throws SQLException
     * @throws SQLException
     *             the SQL exception
     */
    private long findNumberOfStudyRelatedInstances(String studyUid) throws SQLException {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        
        try {
            con = dataSource.getConnection();
            oracle = Dbms.isOracle(con);
            modDateFormat(con);
            stat = con.createStatement();
            stat.setMaxRows(0);
            rs = stat.executeQuery("SELECT SUM(numberOfSeriesRelatedInstances) FROM Series WHERE studyFK='" + studyUid + "' AND Series.Deprecated=0");
            if (rs.next()) {
                long result = rs.getLong(1);
                return result;
            }
        } catch (SQLException e) {
            throw e;
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);    
        }
        return 0;
    } // end findNumberOfStudyRelatedInstances()

    /**
     * Gets the phys reading study.
     * 
     * @param studyUid
     *            the study uid
     * @return the phys reading study
     * @throws SQLException
     *             the SQL exception
     */
    private PersonalName[] getPhysReadingStudy(String studyUid) throws SQLException {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        
        List<PersonalName> temp = new ArrayList<PersonalName>(7);
        try{
            con = dataSource.getConnection();
            oracle = Dbms.isOracle(con);
            modDateFormat(con);
            stat = con.createStatement();
            stat.setMaxRows(0);
            rs = stat.executeQuery("SELECT lastName, firstName, middleName, prefix, suffix FROM Personnel,PhysiciansToStudies WHERE Personnel.pk=PhysiciansToStudies.nameOfPhysiciansReadingStudyFK AND PhysiciansToStudies.studyFK='" + studyUid + "'");
            while (rs.next())
                temp.add(new PersonalName(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
            int s = temp.size();
            if (s > 0) {
                PersonalName[] res = new PersonalName[s];
                temp.toArray(res); // return an array, cast to the appropriate
                // class!
                return res;
            } // end if
        } catch (SQLException e) {
            throw e;
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        }
        return null;
    } // end getPhysReadingStudy()

    /**
     * Find modalities in study.
     * 
     * @param suid
     *            the suid
     * @return the string[]
     * @throws SQLException
     *             the SQL exception
     */
    private String[] findModalitiesInStudy(String suid) throws SQLException {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        
        String[] retArg = null;
        
        try{
            con = dataSource.getConnection();
            oracle = Dbms.isOracle(con);
            modDateFormat(con);
            stat = con.createStatement();
            stat.setMaxRows(0);
            rs = stat.executeQuery("SELECT modality FROM Series WHERE studyFK='" + suid + "' and deprecated=0");
            Set<String> tmis = new HashSet<String>(3);
            while (rs.next()) {
                if (rs.getString("modality") != null) {
                    tmis.add(rs.getString(1));
                }
            }
            retArg = new String[tmis.size()];
            tmis.toArray(retArg);
        } catch (SQLException e) {
            throw e;
        } finally {
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        }
        return retArg;
    }// end findModalitiesInStudy

    private void modDateFormat(Connection dbCon) throws SQLException {
        if (dbCon.getMetaData().getURL().contains("oracle")) {
            dbCon.createStatement().execute("alter session set nls_date_format=\"YYYY-MM-DD\"");
        }
    }
}
