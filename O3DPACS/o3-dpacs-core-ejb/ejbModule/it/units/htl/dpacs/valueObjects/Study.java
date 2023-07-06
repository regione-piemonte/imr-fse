/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.ArrayList;
import java.text.DateFormat;

/**
 * The class keeps all the info of a study, most mapped in studies table
 * @author Mbe
 */
public class Study implements HtlVo, Serializable{

	 /** For use in studyStatus */
	public static final char DPACS_OPEN_STATUS='o';
    public static final char DPACS_ARCHIVED_STATUS='a';
    public static final char DPACS_OFFLINE_STATUS='p';
    public static final char DPACS_NEARLINE_STATUS='n';
    
    public static final String DICOM_AVAILABILITY_ONLINE="ONLINE";
    public static final String DICOM_AVAILABILITY_NEARLINE="NEARLINE";
    public static final String DICOM_AVAILABILITY_OFFLINE="OFFLINE";
    public static final String DICOM_AVAILABILITY_UNAVAILABLE="UNAVAILABLE";
	
      	private String studyInstanceUid=null;		
         // From ; Into Studies.studyInstanceUID
	private List<String> uidsToMatch=null;		
         // From Dicom; used only in matches
	private String studyId=null; 		
         // From ; Into Studies.studyID
	private	String studyStatusId=null;	
         // From ; Into Studies.studyStatusID
	private	Date studyDate=null;		
         // From ; Into Studies.studyDate
	private Date studyDateLate=null;	
         // For matching on ranges
	private	Time studyTime=null;		
         // From ; Into Studies.studyTime
	private Time studyTimeLate=null;		
         // From DICOM; Into Studies.studyCompletionDate
        private	Date studyCompletionDate=null;	
         // From DICOM; Into Studies.studyCompletionDate
	private Date studyCompletionDateLate=null;		
         // For matching on ranges
	private	Time studyCompletionTime=null;			
         // From DICOM; Into Studies.studyCompletionTime
	private Time studyCompletionTimeLate=null;		
         // For matching on ranges	
	private	Date studyVerifiedDate=null;		
         // From DICOM; Into Studies.studyVerifiedDate */
	private Date studyVerifiedDateLate=null;	
         // For matching on ranges
	private	Time studyVerifiedTime=null;		
         // From DICOM; Into Studies.studyVerifiedTime */
	private Time studyVerifiedTimeLate=null;	
         // For matching on ranges
	private String accessionNumber=null;		
         // From ; Into Studies.accessionNumber
	private String studyDescription=null;	
         // From ; Into Studies.studyDescription
	private CodeSequence procedureCodeSequence=null;	
         // From ; Into Studies.procedureCodeSequenceFK and ProcedureCodeSequences
	private String referringPhysiciansName=null;	
         // From ; Into Studies.referringPhysiciansName
	private List<PersonalName> namesOfPhysiciansReadingStudy=null;	
         // From ; Into Personnel and PhysiciansToStudies A list containing PersonalName(s)
	private String admittingDiagnosesDescription=null;
         // From ; Into Studies.admittingDiagnosesDescription
	private String /*char*/ studyStatus=null;	
         // From DPACS; Into Studies.studyStatus 
	private String /*char*/ resultsManagement=null;	
         // From DICOM, the Results Management Service Class
	private String /*long*/ studySize=null;	
		
	
	private String sealedBy=null;
	private String /*int*/ numberOfDeliveredCDs=null;
	private String /*long*/ numberOfStudyRelatedSeries=null;
	private String /*long*/ numberOfStudyRelatedInstances=null;
	private String[] modalitiesInStudy=null;
	//private boolean online=true;
	
	private String specificCharacterSet;
	private String fastestAccess = null;
	private String studyDateString = null;
	private char toPerform=DicomConstants.INSERT;	   
         // ---

	  // Constructors:

	public Study(String uid){
		setStudyInstanceUid(uid);
	}
	public Study(String uid, boolean truncate){
		if(truncate)
			setStudyInstanceUid(uid);
		else
			this.studyInstanceUid=uid;
	}
	
	public Study(String uId, String idIssuer, char toPerform){}
              // // Basic Constructor

	  // Accessor Methods:

	public void setStudyInstanceUid(String siu){
		uidsToMatch=null;		
                 // Avoid setting both the UID and the list of UIDs!!
		studyInstanceUid=prepareString(siu, 64);
	}
	public void addUidToMatch(String uidtm){
		studyInstanceUid=null;
		if (uidsToMatch==null)
			uidsToMatch=new ArrayList<String>(3);	
                 //	3 ids per query should be enough!
		uidsToMatch.add(prepareString(uidtm, 64));
	}
	public void setStudyId(String si){
		studyId=prepareString(si, 64);
	}
	public void setStudyStatusId(String ssi){
		studyStatusId=prepareString(ssi, 16);
	}
	public void setStudyDate(Date sd){
		studyDate=sd;
	}
	public void setStudyDateRange(Date sdEarly, Date sdLate){
		studyDate=sdEarly;
		studyDateLate=sdLate;
	}
	public void setStudyTime(Time st){
		studyTime=st;
	}
	public void setStudyTimeRange(Time stEarly, Time stLate){
		studyTime=stEarly;
		studyTimeLate=stLate;
	}
	public void setStudyCompletionDate(Date scd){
		studyCompletionDate=scd;
	}
	public void setStudyCompletionDateRange(Date scdEarly, Date scdLate){
		studyCompletionDate=scdEarly;
		studyCompletionDateLate=scdLate;
	}
	public void setStudyCompletionTime(Time sct){
		studyCompletionTime=sct;
	}
	public void setStudyCompletionTimeRange(Time sctEarly, Time sctLate){
		studyCompletionTime=sctEarly;
		studyCompletionTimeLate=sctLate;
	}
	public void setStudyVerifiedDate(Date svd){
		studyVerifiedDate=svd;
	}
	public void setStudyVerifiedDateRange(Date svdEarly, Date svdLate){
		studyVerifiedDate=svdEarly;
		studyVerifiedDateLate=svdLate;
	}
	public void setStudyVerifiedTime(Time svt){
		studyVerifiedTime=svt;
	}
	public void setStudyVerifiedTimeRange(Time svtEarly, Time svtLate){
		studyVerifiedTime=svtEarly;
		studyVerifiedTimeLate=svtLate;
	}
	public void setAccessionNumber(String an){
		accessionNumber=prepareString(an, 16);
	}
	public void setStudyDescription(String sd){
		studyDescription=prepareString(sd, 64);
	}
	public void setProcedureCodeSequence(CodeSequence pcs){
		procedureCodeSequence=pcs;
	}
	public void setReferringPhysiciansName(String rpn){
		referringPhysiciansName=prepareString(rpn, 64);
	}
	public void setAdmittingDiagnosesDescription(String add){
		admittingDiagnosesDescription=prepareString(add, 64);
	}
	public void setStudyStatus(String ss){
		studyStatus=prepareString(ss, 1);
	}
	public void setResultsManagement(String rm){
		resultsManagement=prepareString(rm, 1);
	}
	  // The following methods do the same thing, but setStudySize is ideally used by DBDealer, the other by a client
	public void setStudySize(String ss){
		if(studySize==null)	studySize=prepareLong(ss);
	}	
	public void setStudySizeToAdd(String ss){
		setStudySize(ss);
	}	
	public void setSealedBy(String sb){
		sealedBy=prepareString(sb, 64);
	}
	public void setNumberOfDeliveredCDs(String nodc){
		numberOfDeliveredCDs=prepareInt(nodc);
	}
	public void setNumberOfStudyRelatedSeries(String nsrs){
		numberOfStudyRelatedSeries=prepareLong(nsrs);
	}
	public void setNumberOfStudyRelatedInstances(String nsri){
		numberOfStudyRelatedInstances=prepareLong(nsri);
	}
	public void setModalitiesInStudy(String[] mis){
		modalitiesInStudy=mis;
	}
	public void addNameOfPhysicianReadingStudy(PersonalName pn){
		if(pn==null) return;
		if(namesOfPhysiciansReadingStudy==null) namesOfPhysiciansReadingStudy=new ArrayList<PersonalName>(3);
		namesOfPhysiciansReadingStudy.add(pn);
	}
	public void addNameOfPhysicianReadingStudy(String ln, String fn, String mn, String p, String s){
		if((ln!=null) || (fn!=null) || (mn!=null) || (p!=null) || (s!=null)){
			if(namesOfPhysiciansReadingStudy==null) namesOfPhysiciansReadingStudy=new ArrayList<PersonalName>(3);
			PersonalName pn=new PersonalName(ln, fn, mn, p, s);
			namesOfPhysiciansReadingStudy.add(pn);
		}	
                 // end if
	}

	public void resetPhysiciansReadingStudy(){
		namesOfPhysiciansReadingStudy=null;
	}
	
//	public void setOnline(boolean arg){
//		online=arg;
//	}

	public String getFastestAccess() {
        return fastestAccess;
    }
    public void setFastestAccess(String fastestAccess) {
        this.fastestAccess = fastestAccess;
    }
    public String getStudyInstanceUid(){
		return studyInstanceUid;
	}
	public String[] getUidsToMatch(){
		if (uidsToMatch==null) return null;
		int s=uidsToMatch.size();
		String[] temp=new String[s];
		uidsToMatch.toArray(temp);	
                 // The array is returned in temp, 'cos it's certainly long enough!
		return temp;
	}
	public String getStudyId(){
		return studyId;
	}
	public String getStudyStatusId(){
		return studyStatusId;
	}
	public Date getStudyDate(){
		return studyDate;
	}
	public Date getStudyDateLate(){
		return studyDateLate;
	}
	public Time getStudyTime(){
		return studyTime;	
	}
	public Time getStudyTimeLate(){
		return studyTimeLate;	
	}
	public Date getStudyCompletionDate(){
		return studyCompletionDate;	
	}
	public Date getStudyCompletionDateLate(){
		return studyCompletionDateLate;	
	}
	public Time getStudyCompletionTime(){
		return studyCompletionTime;
	}
	public Time getStudyCompletionTimeLate(){
		return studyCompletionTimeLate;	
	}
	public Date getStudyVerifiedDate(){
		return studyVerifiedDate;	
	}
	public Date getStudyVerifiedDateLate(){
		return studyVerifiedDateLate;	
	}
	public Time getStudyVerifiedTime(){
		return studyVerifiedTime;	
	}
	public Time getStudyVerifiedTimeLate(){
		return studyVerifiedTimeLate;	
	}
      //	/**
      //		@param df It should be got by DateFormat.getDateInstance to have consistent results
      //	*/
	public String getStudyDate(DateFormat df){
		if (studyDate==null) return null;
		return ((df==null)? Long.toString(studyDate.getTime()) : df.format(studyDate)); 	
	}
      //	/**
      //		@param df It should be got by DateFormat.getDateInstance to have consistent results
      //	*/
	public String getStudyDateLate(DateFormat df){
		if (studyDateLate==null) return null;
		return ((df==null)? Long.toString(studyDateLate.getTime()) : df.format(studyDateLate)); 
	}
      //	/**
      //		@param tf It should be got by DateFormat.getTimeInstance to have consistent results
      //	*/
	public String getStudyTime(DateFormat tf){
		if (studyTime==null) return null;
		return ((tf==null)? Long.toString(studyTime.getTime()) : tf.format(studyTime)); 
	}
      //	/**
      //		@param tf It should be got by DateFormat.getTimeInstance to have consistent results
      //	*/
	public String getStudyTimeLate(DateFormat tf){
		if (studyTimeLate==null) return null;
		return ((tf==null)? Long.toString(studyTimeLate.getTime()) : tf.format(studyTimeLate)); 
	}
      //	/**
      //		@param df It should be got by DateFormat.getDateInstance to have consistent results
      //	*/
	public String getStudyCompletionDate(DateFormat df){
		if (studyCompletionDate==null) return null;
		return ((df==null)? Long.toString(studyCompletionDate.getTime()) : df.format(studyCompletionDate)); 
	}
      //	/**
      //		@param df It should be got by DateFormat.getDateInstance to have consistent results
      //	*/
	public String getStudyCompletionDateLate(DateFormat df){
		if (studyCompletionDateLate==null) return null;
		return ((df==null)? Long.toString(studyCompletionDateLate.getTime()) : df.format(studyCompletionDateLate)); 
	}
      //	/**
      //		@param tf It should be got by DateFormat.getTimeInstance to have consistent results
      //	*/
	public String getStudyCompletionTime(DateFormat tf){
		if (studyCompletionTime==null) return null;
		return ((tf==null)? Long.toString(studyCompletionTime.getTime()) : tf.format(studyCompletionTime)); 
	}
      //	/**
      //		@param tf It should be got by DateFormat.getTimeInstance to have consistent results
      //	*/
	public String getStudyCompletionTimeLate(DateFormat tf){
		if (studyCompletionTimeLate==null) return null;
		return ((tf==null)? Long.toString(studyCompletionTimeLate.getTime()) : tf.format(studyCompletionTimeLate)); 
	}
      //	/**
      //		@param df It should be got by DateFormat.getDateInstance to have consistent results
      //	*/
	public String getStudyVerifiedDate(DateFormat df){
		if (studyVerifiedDate==null) return null;
		return ((df==null)? Long.toString(studyVerifiedDate.getTime()) : df.format(studyVerifiedDate)); 
	}
      //	/**
      //		@param df It should be got by DateFormat.getDateInstance to have consistent results
      //	*/
	public String getStudyVerifiedDateLate(DateFormat df){
		if (studyVerifiedDateLate==null) return null;
		return ((df==null)? Long.toString(studyVerifiedDateLate.getTime()) : df.format(studyVerifiedDateLate)); 
	}
      //	/**
      //		@param tf It should be got by DateFormat.getTimeInstance to have consistent results
      //	*/
	public String getStudyVerifiedTime(DateFormat tf){
		if (studyVerifiedTime==null) return null;
		return ((tf==null)? Long.toString(studyVerifiedTime.getTime()) : tf.format(studyVerifiedTime)); 
	}
      //	/**
      //	@param tf It should be got by DateFormat.getTimeInstance to have consistent results
      //	*/
	public String getStudyVerifiedTimeLate(DateFormat tf){
		if (studyVerifiedTimeLate==null) return null;
		return ((tf==null)? Long.toString(studyVerifiedTimeLate.getTime()) : tf.format(studyVerifiedTimeLate)); 
	}
	public String getAccessionNumber(){
		return accessionNumber;
	}
	public String getStudyDescription(){
		return studyDescription;
	}
	public CodeSequence getProcedureCodeSequence(){
		return procedureCodeSequence;
	}
	public String getReferringPhysiciansName(){
		return referringPhysiciansName;
	}
	public String getAdmittingDiagnosesDescription(){
		return admittingDiagnosesDescription;
	}
	public String getStudyStatus(){
		return studyStatus;
	}
	public String setResultsManagement(){
		return resultsManagement;
	}
      //	/** If called by DAO it represents the size to add into the DB, if called by a client it's the size of the whole study */
	public String getStudySize(){
		return studySize;
	}
	public String getSealedBy(){
		return sealedBy;
	}
	public String getNumberOfDeliveredCDs(){
		return numberOfDeliveredCDs;
	}
	public String getNumberOfStudyRelatedSeries(){
		return numberOfStudyRelatedSeries;
	}
	public String getNumberOfStudyRelatedInstances(){
		return numberOfStudyRelatedInstances;
	}
	public String[] getModalitiesInStudy(){
		return modalitiesInStudy;
	}
	public PersonalName[] getNamesOfPhysiciansReadingStudy(){
		if (namesOfPhysiciansReadingStudy==null) return null;
		int s=namesOfPhysiciansReadingStudy.size();
		PersonalName[] temp=new PersonalName[s];
		namesOfPhysiciansReadingStudy.toArray(temp);
                 // The array is returned in temp, 'cos it's certainly long enough!
		return temp;
	}	
	
//	public boolean isOnline(){
//		return online;
//	}

	public String prepareString(String arg, int len){
		if (arg==null) return null;
		String temp=arg.trim();
		return ((temp.length()>len)? temp.substring(0, len) : temp);
	}
	
	public String prepareLong(String arg){
		if(arg==null) return null;
		String temp=null;
		try{
			temp=(Long.valueOf(arg.trim())).toString();
		}catch(NumberFormatException e){
			temp=null;
		}
		return temp;
	}
	public String prepareInt(String arg){
		if(arg==null) return null;
		String temp=null;
		try{
			temp=(Integer.valueOf(arg.trim())).toString();
		}catch(NumberFormatException e){
			temp=null;
		}
		return temp;
	}
	public void setToPerform(char arg){
		if((arg==DicomConstants.INSERT) || (arg==DicomConstants.FIND) || (arg==DicomConstants.UPDATE)) toPerform=arg;
		else toPerform=DicomConstants.INSERT;
	}
	public char getToPerform(){
		return toPerform;
	}


	public void reset(){
		studyInstanceUid=null;
		uidsToMatch=null;		
		studyId=null; 		
		studyStatusId=null;	
		studyDate=null;		
		studyDateLate=null;		
		studyTime=null;		
		studyTimeLate=null;		
		studyCompletionDate=null;
		studyCompletionDateLate=null;
		studyCompletionTime=null;
		studyCompletionTimeLate=null;
		studyVerifiedDate=null;
		studyVerifiedDateLate=null;
		studyVerifiedTime=null;
		studyVerifiedTimeLate=null;
		accessionNumber=null;		
		studyDescription=null;	
		procedureCodeSequence=null;	
		referringPhysiciansName=null;	
		namesOfPhysiciansReadingStudy=null;	
		admittingDiagnosesDescription=null;	
		studyStatus=null;	
		studySize=null;		
		sealedBy=null;
		numberOfDeliveredCDs=null;
		toPerform=DicomConstants.INSERT;
		studyDateString=null;
	}
    public String getStudyDateString() {
        return studyDateString;
    }
    public void setStudyDateString(String studyDateString) {
        this.studyDateString = studyDateString;
    }
	public String getSpecificCharacterSet() {
		return specificCharacterSet;
	}
	public void setSpecificCharacterSet(String specificCharacterSet) {
		this.specificCharacterSet = specificCharacterSet;
	}
	
}
