/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;


/**
 * The class has all the info of a DICOM Structured Report object
 * @author Mbe
 */
public class StructRep extends Instance{

	  // Private Attributes:
	private String completionFlag=null;		
         // From ; Into StructReps.completionFlag
	private String verificationFlag=null;	
         // From ; Into StructReps.verificationFlag
	private Date contentDate=null;		
         // From DICOM; Into StructReps.contentDate
	private Date contentDateLate=null;	
         // For matching on ranges	
	private Time contentTime=null;		
         // From DICOM; Into StructReps.contentTime
	private Time contentTimeLate=null;	
         // For matching on ranges	
	private Timestamp observationDateTime=null;	
         // From ; Into StructReps.observationDateTime
	private Timestamp observationDateTimeLate=null;	
         // For matching on ranges	

          //added for KOS
        
        private CodeSequence conceptNameCodeSequence=null;
        
	  // Constructors:

	public StructRep(){}
	public StructRep(String siu){
		sopInstanceUid=prepareString(siu, 64);
	}	

	public void setCompletionFlag(String cf){
		completionFlag=prepareString(cf, 16);
	}
	public void setVerificationFlag(String vf){
		verificationFlag=prepareString(vf, 16);
	}
	public void setContentDate(Date cd){
		contentDate=cd;
	}
	      ///** It overwrites BOTH values anyway!!! */
	public void setContentDateRange(Date cdEarly, Date cdLate){
		contentDate=cdEarly;
		contentDateLate=cdLate;
	}
	public void setContentTime(Time ct){
		contentTime=ct;
	}
	      ///** It overwrites BOTH values anyway!!! */
	public void setContentTimeRange(Time ctEarly, Time ctLate){
		contentTime=ctEarly;
		contentTimeLate=ctLate;
	}
	public void setObservationDateTime(Timestamp odt){
		observationDateTime=odt;
	}
	      ///** It overwrites BOTH values anyway!!! */
	public void setObservationDateTimeRange(Timestamp odtEarly, Timestamp odtLate){
		observationDateTime=odtEarly;
		observationDateTimeLate=odtLate;
	}
	public String getCompletionFlag(){
		return completionFlag;
	}
	public String getVerificationFlag(){
		return verificationFlag;
	}
	public Date getContentDate(){
		return contentDate;
	}
	      ///**
	      //	@param df It should be got by DateFormat.getDateInstance to have consistent results
	      //*/
	public String getContentDate(DateFormat df){
		if (contentDate==null) return null;
		return ((df==null)? Long.toString(contentDate.getTime()) : df.format(contentDate)); 
	}
	public Time getContentTime(){
		return contentTime;
	}
	      ///**
      //		@param tf It should be got by DateFormat.getTimeInstance to have consistent results
      //	*/
	public String getContentTime(DateFormat tf){
		if (contentTime==null) return null;
		return ((tf==null)? Long.toString(contentTime.getTime()) : tf.format(contentTime)); 
	}
	public Timestamp getObservationDateTime(){
		return observationDateTime;
	}
	      ///**
      //		@param dtf It should be got by DateFormat.getDateTimeInstance to have consistent results
	      //*/
     	public String getObservationDateTime(DateFormat dtf){
		if (observationDateTime==null) return null;
		return ((dtf==null)? Long.toString(observationDateTime.getTime()) : dtf.format(observationDateTime)); 
	}
      //	/** It returns null if matching ain't on range */
	public Date getContentDateLate(){
		return contentDateLate;
	}
      //	/**
      //		@param df It should be got by DateFormat.getDateInstance to have consistent results
      //	*/
	public String getContentDateLate(DateFormat df){
		if (contentDateLate==null) return null;
		return ((df==null)? Long.toString(contentDateLate.getTime()) : df.format(contentDateLate)); 
	}
	      ///** It returns null if matching ain't on range */
	public Time getContentTimeLate(){
		return contentTimeLate;
	}
      //	/**
      //		@param tf It should be got by DateFormat.getTimeInstance to have consistent results
      //	*/
	public String getContentTimeLate(DateFormat tf){
		if (contentTimeLate==null) return null;
		return ((tf==null)? Long.toString(contentTimeLate.getTime()) : tf.format(contentTimeLate)); 
	}
	      ///** It returns null if matching ain't on range */
	public Timestamp getObservationDateTimeLate(){
		return observationDateTimeLate;
	}
      //	/**
      //		@param dtf It should be got by DateFormat.getDateTimeInstance to have consistent results
      //	*/
	public String getObservationDateTimeLate(DateFormat dtf){
		if (observationDateTimeLate==null) return null;
		return ((dtf==null)? Long.toString(observationDateTimeLate.getTime()) : dtf.format(observationDateTimeLate)); 
	}

        public void setConceptNameCodeSequence(CodeSequence cs){
            conceptNameCodeSequence=cs;
        }
       
         public CodeSequence getConceptNameCodeSequence(){
            return conceptNameCodeSequence;
        }
       
	public void reset(){
		completionFlag=null;	
		verificationFlag=null;	
		contentDate=null;		
		contentDateLate=null;	
		contentTime=null;		
		contentTimeLate=null;	
		observationDateTime=null;
		observationDateTimeLate=null;
	
	}

}
