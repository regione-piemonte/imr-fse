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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The class keep the data of a General Purpose Scheduled Procedure Step Dicom
 * Object
 * @author Carrara
 */
public class GPSPSItem implements HtlVo, Serializable{
    
    public GPSPSItem(String SopInstance){
        this.setGPSPSSOPInstanceUID(SopInstance);
    }
    
  //--------------------------------------------INFORMATION MODULE-----------------------------------
    
    private String GPSPSStatus=null;
    private String GPSPSPriority=null;
    private String scheduledProcedureStepID=null;
    private String inputAvaibilityFlag=null;
    private Date scheduledProcedureStepStartDate=null;
    private Time scheduledProcedureStepStartTime=null;
    private Date expectedCompletionDate=null;
    private Time expectedCompletionTime=null;
    private String multipleCopiesFlag=null;
    private String studyInstanceUID=null;
    private String transactionUID=null;
    private CodeSequence scheduledWorkitemCodeSequence=null;
    private List<CodeSequence> scheduledProcessingApplicationsCodeSequence=null;
    private List<CodeSequence> scheduledStationNameCodeSequence=null;
    private List<CodeSequence> scheduledStationClassCodeSequence=null;
    private List<CodeSequence> scheduledStationGeographicLocationCodeSequence=null;
    private List<HumanPerformersSequence> scheduledHumanPerformersSequence=null;
    private List<HumanPerformersSequence> actualHumanPerformersSequence=null;
    private List<InputInformationSequence> inputInformationSequence=null;
    private List<RelevantInformationSequence> relevantInformationSequence=null;
    private List<ReferredItemSequence> resultingGPPPSSequence=null;
    private List<ReferredItemSequence> referencedPerformedProcedureStepSequence=null;
    private Log log = LogFactory.getLog("GPSPSItem");
    
    public class HumanPerformersSequence {
        
        private CodeSequence humanPerformerCodeSequence=null;
        private String humanPerformersName=null;
        private String humanPerformersOrganization=null;
        
        public void setHumanPerformersName(String hpName){
            humanPerformersName=prepareString(hpName,64);
        }
        
        public String getHumanPerformersName(){
            return humanPerformersName;
        }
        
        public void setHumanPerformersOrganization(String hpOrganization){
            humanPerformersOrganization=prepareString(hpOrganization,64);
        }
        
        public String getHumanPerformersOrganization(){
            return humanPerformersOrganization;
        }
        
        public void setHumanPerformerCodeSequence(CodeSequence hpcs){
            humanPerformerCodeSequence=hpcs;
        }
        
        public CodeSequence getHumanPerformerCodeSequence(){
            return humanPerformerCodeSequence;
        }
        
    }
    
    public class InputInformationSequence {
        
        private String studyInstanceUID=null;
        private List<ReferencedSeriesSequence> referencedSeriesSequence=null;
        
        public void setStudyInstanceUID(String siUID){
            studyInstanceUID=prepareString(siUID,64);
        }
        
        public String getStudyInstanceUID(){
            return studyInstanceUID;
        }
        
        public void setReferencedSeriesSequence(ReferencedSeriesSequence rss){
            if (referencedSeriesSequence==null) referencedSeriesSequence=new ArrayList<ReferencedSeriesSequence >(3);
            referencedSeriesSequence.add(rss);
        }
        
        public ReferencedSeriesSequence[] getReferencedSeriesSequence() {
            if (referencedSeriesSequence==null) return null;
            int s=referencedSeriesSequence.size();
            ReferencedSeriesSequence[] temp=new ReferencedSeriesSequence[s];
            referencedSeriesSequence.toArray(temp);
            return temp;
        }
        
        
    }
    
    public class ReferencedSeriesSequence {
        
        private String seriesInstanceUID=null;
        private String retrieveAETitle =null;
        private String storageMediaFileSetID = null;
        private String storageMediaFileSetUID= null;
        private List<ReferredItemSequence> referencedSOPSequence =null;
        
        
        public void setSeriesInstanceUID(String siUID){
            seriesInstanceUID=prepareString(siUID,64);
        }
        
        public String getSeriesInstanceUID(){
            return seriesInstanceUID;
        }
        
        public void setRetrieveAETitle(String raeTitle){
            retrieveAETitle=prepareString(raeTitle,64);
        }
        
        public String getRetrieveAETitle(){
            return retrieveAETitle;
        }
        
        public void setStorageMediaFileSetID(String smfsID){
            storageMediaFileSetID=prepareString(smfsID,64);
        }
        
        public String getStorageMediaFileSetID(){
            return storageMediaFileSetID;
        }
        
        public void setStorageMediaFileSetUID(String smfsUID){
            storageMediaFileSetUID=prepareString(smfsUID,64);
        }
        
        public String getStorageMediaFileSetUID(){
            return storageMediaFileSetUID;
        }
        
        public void addReferencedSOPSequence(ReferredItemSequence rsops){
            if (referencedSOPSequence==null) referencedSOPSequence=new ArrayList<ReferredItemSequence>(3);
            referencedSOPSequence.add(rsops);
        }
        
        public ReferredItemSequence[] getReferencedSOPSequence() {
            if (referencedSOPSequence==null) return null;
            int s=referencedSOPSequence.size();
            ReferredItemSequence[] temp=new ReferredItemSequence[s];
            referencedSOPSequence.toArray(temp);
            return temp;
        }
        
    }
    
    public class ReferredItemSequence {
        
        private String SOPClass = null;
        private String SOPInstance = null;
        
        public void setSOPClass(String s){SOPClass=prepareString(s,64);}
        public String getSOPClass(){return SOPClass;}
        
        public void setSOPInstance(String s){SOPInstance=prepareString(s,64);}
        public String getSOPInstance(){return SOPInstance;}
    }
    
    public class RelevantInformationSequence {
        
        private String studyInstanceUID=null;
        private List<ReferencedSeriesSequence> referencedSeriesSequence=null;
        
        public void setStudyInstanceUID(String siUID){
            studyInstanceUID=prepareString(siUID,64);
        }
        
        public String getStudyInstanceUID(){
            return studyInstanceUID;
        }
        
        public void addReferencedSeriesSequence(ReferencedSeriesSequence rss){
            if (referencedSeriesSequence==null) referencedSeriesSequence=new ArrayList<ReferencedSeriesSequence>(3);
            referencedSeriesSequence.add(rss);
        }
        
        public ReferencedSeriesSequence[] getReferencedSeriesSequence() {
            if (referencedSeriesSequence==null) return null;
            int s=referencedSeriesSequence.size();
            ReferencedSeriesSequence[] temp=new ReferencedSeriesSequence[s];
            referencedSeriesSequence.toArray(temp);
            return temp;
        }
        
    }
    
    public void setGPSPSStatus(String status){
        GPSPSStatus=prepareString(status,16);
    }
    
    public String getGPSPSStatus(){
        return GPSPSStatus;
    }
    
    public void setGPSPSPriority(String priority){
        GPSPSPriority=prepareString(priority,8);
    }
    
    public String getGPSPSPriority(){
        return GPSPSPriority;
    }
    
    public void setScheduledProcedureStepID(String spsID){
        scheduledProcedureStepID=prepareString(spsID,16);
    }
    
    public String getScheduledProcedureStepID(){
        return scheduledProcedureStepID;
    }
    
    public void setInputAvaibilityFlag(String iaFlag){
        inputAvaibilityFlag=prepareString(iaFlag,8);
    }
    
    public String getInputAvaibilityFlag(){
        return inputAvaibilityFlag;
    }
    
    public void setMultipleCopiesFlag(String mcFlag){
        multipleCopiesFlag=prepareString(mcFlag,64);
    }
    
    public String getMultipleCopiesFlag(){
        return multipleCopiesFlag;
    }
    
    public void setStudyInstanceUID(String siUID){
        studyInstanceUID=prepareString(siUID,64);
    }
    
    public String getStudyInstanceUID(){
        return studyInstanceUID;
    }
    
    public void setTransactionUID(String tUID){
        transactionUID=prepareString(tUID,64);
    }
    
    public String getTransactionUID(){
        return transactionUID;
    }
    
    public void setScheduledProcedureStepStartDate(Date spsDate){
        scheduledProcedureStepStartDate=spsDate;
    }
    
    public Date getScheduledProcedureStepStartDate(){
        return scheduledProcedureStepStartDate;
    }
    
    public void setScheduledProcedureStepStartTime(Time spsTime){
        scheduledProcedureStepStartTime=spsTime;
    }
    
    public Time getScheduledProcedureStepStartTime(){
        return scheduledProcedureStepStartTime;
    }
    public void setExpectedCompletionDate(Date ecDate){
        expectedCompletionDate=ecDate;
    }
    
    public Date getExpectedCompletionDate(){
        return expectedCompletionDate;
    }
    
    public void setExpectedCompletionTime(Time ecTime){
        expectedCompletionTime=ecTime;
    }
    
    public Time getExpectedCompletionTime(){
        return expectedCompletionTime;
    }
    
    public void setScheduledWorkitemCodeSequence(CodeSequence swcs){
        scheduledWorkitemCodeSequence=swcs;
    }
    
    public CodeSequence getScheduledWorkitemCodeSequence(){
        return scheduledWorkitemCodeSequence;
    }
    
    public void addScheduledProcessingApplicationsCodeSequence(CodeSequence spacs){
        if (scheduledProcessingApplicationsCodeSequence==null) scheduledProcessingApplicationsCodeSequence=new ArrayList<CodeSequence>(3);
        scheduledProcessingApplicationsCodeSequence.add(spacs);
    }
    
    public CodeSequence[] getScheduledProcessingApplicationsCodeSequence() {
        if (scheduledProcessingApplicationsCodeSequence==null) return null;
        int s=scheduledProcessingApplicationsCodeSequence.size();
        CodeSequence[] temp=new CodeSequence[s];
        scheduledProcessingApplicationsCodeSequence.toArray(temp);
        return temp;
    }
    
    public void addScheduledStationNameCodeSequence(CodeSequence sstcs){
        if (scheduledStationNameCodeSequence==null) scheduledStationNameCodeSequence=new ArrayList<CodeSequence>(3);
        scheduledStationNameCodeSequence.add(sstcs);
    }
    
    public CodeSequence[] getScheduledStationNameCodeSequence() {
        if (scheduledStationNameCodeSequence==null) return null;
        int s=scheduledStationNameCodeSequence.size();
        CodeSequence[] temp=new CodeSequence[s];
        scheduledStationNameCodeSequence.toArray(temp);
        return temp;
    }
    
    public void addscheduledStationClassCodeSequence(CodeSequence ssccs){
        if (scheduledStationClassCodeSequence==null) scheduledStationClassCodeSequence=new ArrayList<CodeSequence>(3);
        scheduledStationClassCodeSequence.add(ssccs);
    }
    public CodeSequence[] getScheduledStationClassCodeSequence() {
        if (scheduledStationClassCodeSequence==null) return null;
        int s=scheduledStationClassCodeSequence.size();
        CodeSequence[] temp=new CodeSequence[s];
        scheduledStationClassCodeSequence.toArray(temp);
        return temp;
    }
    
    public void addScheduledStationGeographicLocationCodeSequence(CodeSequence ssglcs){
        if (scheduledStationGeographicLocationCodeSequence==null) scheduledStationGeographicLocationCodeSequence=new ArrayList<CodeSequence>(3);
        scheduledStationGeographicLocationCodeSequence.add(ssglcs);
    }
    
    public CodeSequence[] getScheduledStationGeographicLocationCodeSequence() {
        if (scheduledStationGeographicLocationCodeSequence==null) return null;
        int s=scheduledStationGeographicLocationCodeSequence.size();
        CodeSequence[] temp=new CodeSequence[s];
        scheduledStationGeographicLocationCodeSequence.toArray(temp);
        return temp;
    }
    
    public void addResultingGPPPSSequence(ReferredItemSequence rgpppss){
        if (resultingGPPPSSequence==null) resultingGPPPSSequence=new ArrayList<ReferredItemSequence>(3);
        resultingGPPPSSequence.add(rgpppss);
    }
    
    public ReferredItemSequence[] getResultingGPPPSSequence() {
        if (resultingGPPPSSequence==null) return null;
        int s=resultingGPPPSSequence.size();
        ReferredItemSequence[] temp=new ReferredItemSequence[s];
        resultingGPPPSSequence.toArray(temp);
        return temp;
    }
    
    public void addActualHumanPerformersSequence(HumanPerformersSequence ahps){
        if (actualHumanPerformersSequence==null) actualHumanPerformersSequence=new ArrayList<HumanPerformersSequence>(3);
        actualHumanPerformersSequence.add(ahps);
    }
    
    public HumanPerformersSequence[] getActualHumanPerformersSequence() {
        if (actualHumanPerformersSequence==null) return null;
        int s=actualHumanPerformersSequence.size();
        HumanPerformersSequence[] temp=new HumanPerformersSequence[s];
        actualHumanPerformersSequence.toArray(temp);
        return temp;
    }
    
    public void addScheduledHumanPerformersSequence(HumanPerformersSequence shps){
        if (scheduledHumanPerformersSequence==null) scheduledHumanPerformersSequence=new ArrayList<HumanPerformersSequence>(3);
        scheduledHumanPerformersSequence.add(shps);
    }
    
    public HumanPerformersSequence[] getScheduledHumanPerformersSequence() {
        if (scheduledHumanPerformersSequence==null) return null;
        int s=scheduledHumanPerformersSequence.size();
        HumanPerformersSequence[] temp=new HumanPerformersSequence[s];
        scheduledHumanPerformersSequence.toArray(temp);
        return temp;
    }
    
    public void addInputInformationSequence(InputInformationSequence iis){
        if (inputInformationSequence==null) inputInformationSequence=new ArrayList<InputInformationSequence>(3);
        inputInformationSequence.add(iis);
    }
    
    public InputInformationSequence[] getInputInformationSequence() {
        if (inputInformationSequence==null) return null;
        int s=inputInformationSequence.size();
        InputInformationSequence[] temp=new InputInformationSequence[s];
        inputInformationSequence.toArray(temp);
        return temp;
    }
    
    public void addRelevantInformationSequence(RelevantInformationSequence ris){
        if (relevantInformationSequence==null) relevantInformationSequence=new ArrayList<RelevantInformationSequence>(3);
        relevantInformationSequence.add(ris);
    }
    
    public RelevantInformationSequence[] getRelevantInformationSequence() {
        if (relevantInformationSequence==null) return null;
        int s=relevantInformationSequence.size();
        RelevantInformationSequence[] temp=new RelevantInformationSequence[s];
        relevantInformationSequence.toArray(temp);
        return temp;
    }
    
    public void addReferencedPerformedProcedureStepSequence(ReferredItemSequence rppss){
        if (referencedPerformedProcedureStepSequence==null) referencedPerformedProcedureStepSequence=new ArrayList<ReferredItemSequence>(3);
        referencedPerformedProcedureStepSequence.add(rppss);
    }
    
    public ReferredItemSequence[] getReferencedPerformedProcedureStepSequence() {
        if (referencedPerformedProcedureStepSequence==null) return null;
        int s=referencedPerformedProcedureStepSequence.size();
        ReferredItemSequence[] temp=new ReferredItemSequence[s];
        referencedPerformedProcedureStepSequence.toArray(temp);
        return temp;
    }
    
  //-----------------------------------------END OF INFORMATION MODULE----------------------------------
    
  //---------------------------------------------SOP COMMON MODULE--------------------------------------
    
      //SOP Class UID it's always the same
    
    private static String SOPinstanceUID=null;
    
    private void setGPSPSSOPInstanceUID(String siu){
        SOPinstanceUID=prepareString(siu, 64);
        log.debug("From Item: SOP Instance is :  "   + SOPinstanceUID);
    }
    
    public String getGPSPSSOPInstanceUID(){
        return SOPinstanceUID;
    }
    
  //-----------------------------------------END OF COMMON MODULE---------------------------------------
    
  //------------------------------------------RELATIONSHIP MODULE---------------------------------------
    
    private String patientName=null;
    private String patientID=null;
    private String issuerOfPatientID=null;
    private Date birthDate=null;
    private String patientSex=null;
    
    private List<ReferencedRequestSequence> referencedRequestSequence=null;
    
    public class ReferencedRequestSequence {
        String studyInstanceUID=null;
        String accessionNumber=null;
        String requestedProcedureID=null;
        String requestedProcedureDescription=null;
        String requestingPhysician=null;
        CodeSequence requestedProcedureCodeSequence=null;
        ReferredItemSequence referencedStudySequence=null;
        
        public void setStudyInstanceUID(String siUID){
            studyInstanceUID=prepareString(siUID,64);
        }
        
        public String getStudyInstanceUID(){
            return studyInstanceUID;
        }
        
        public void setAccessionNumber(String an){
            accessionNumber=prepareString(an,16);
        }
        
        public String getAccessionNumber(){
            return accessionNumber;
        }
        
        public void setRequestedProcedureID(String rpID){
            requestedProcedureID=prepareString(rpID,64);
        }
        
        public String getrequestedProcedureID(){
            return requestedProcedureID;
        }
        
        public void setRequestedProcedureDescription(String rpD){
            requestedProcedureDescription=prepareString(rpD,16);
        }
        
        public String getRequestedProcedureDescription(){
            return requestedProcedureDescription;
        }
        
        public void setRequestingPhysician(String rp){
            requestingPhysician=prepareString(rp,64);
        }
        
        public String getRequestingPhysician(){
            return requestingPhysician;
        }
        
        public void setRequestedProcedureCodeSequence(CodeSequence rpcs){
            requestedProcedureCodeSequence=rpcs;
        }
        
        public CodeSequence getRequestedProcedureCodeSequence(){
            return requestedProcedureCodeSequence;
        }
        
        public void setReferencedStudySequence(ReferredItemSequence rss){
            referencedStudySequence=rss;
        }
        
        public ReferredItemSequence getReferencedStudySequence() {
            return referencedStudySequence;
        }
        
    }
    
    public void setPatientName(String pn){
        patientName=pn;
    }
    
    public String getPatientName(){
        return patientName;
    }
    
    public void setPatientID(String pid){
        patientID=pid;
    }
    
    public String getPatientID(){
        return patientID;
    }
    
    public void setIssuerOfPatientID(String pid){
        issuerOfPatientID=null;
    }
    
    public String getIssuerOfPatientID(){
        return issuerOfPatientID;
    }
    
    public void setPatientBirthDate(Date d){
        birthDate=d;
    }
    
    public Date getPatientBirthDate(){
        return birthDate;
    }
    
    public void setPatientSex(String s){
        patientSex=s;
    }
    
    public String getPatientSex(){
        return patientSex;
    }
    
    public void addReferencedRequestSequence(ReferencedRequestSequence rss){
        if (referencedRequestSequence==null) referencedRequestSequence=new ArrayList<ReferencedRequestSequence>(3);
        referencedRequestSequence.add(rss);
    }
    
    public ReferencedRequestSequence[] getReferencedRequestSequence() {
        if (referencedRequestSequence==null) return null;
        int s=referencedRequestSequence.size();
        ReferencedRequestSequence[] temp=new ReferencedRequestSequence[s];
        referencedRequestSequence.toArray(temp);
        return temp;
    }
    
  //------------------------------------------END OF RELATIONSHIP MODULE---------------------------------
    
  //-------------------------------------------Data Verification Methods---------------------------------
    
    public void setToPerform(char arg) {
    }
    
    public char getToPerform() {
        return ' ';
    }
    
    public  String prepareString(String arg, int len){
        if (arg==null) return null;
        String temp=arg.trim();
        return ((temp.length()>len)? temp.substring(0, len) : temp);
    }
    
    public String prepareDouble(String arg){
        if(arg==null) return null;
        String temp=null;
        try{
            temp=(Double.valueOf(arg.trim())).toString();
        }catch(NumberFormatException e){
            temp=null;
        }
        return temp;
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
    
    public void reset() {
          //return null
      
    }
    
}
