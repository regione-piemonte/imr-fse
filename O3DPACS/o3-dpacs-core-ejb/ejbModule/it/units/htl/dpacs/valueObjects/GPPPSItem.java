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
 * The class keep the data of a General Purpose Performed Procedure Step Dicom
 * Object
 * @author Mbe
 */
public class GPPPSItem implements HtlVo, Serializable{
    
    public GPPPSItem(String SopInstance){
        this.setGPPPSSOPInstanceUID(SopInstance);
    }
 //--------------------------------------------INFORMATION MODULE-----------------------------------
    
    private String GPPPStatus=null;
    private String performedProcedureStepID=null;
    private Date performedProcedureStepStartDate=null;
    private Time performedProcedureStepStartTime=null;
    private Date performedProcedureStepEndDate=null;
    private Time performedProcedureStepEndTime=null;
    private String performedProcedureStepDescription=null;
    private String commentsOnPerformedProcedureStep=null;
    private CodeSequence performedWorkitemCodeSequence=null;
    private CodeSequence performedStationNameCodeSequence=null;
    private CodeSequence performedStationClassCodeSequence=null;
    private CodeSequence performedStationGeographicLocationCodeSequence=null;
    private List<CodeSequence> performedProcessingApplicationsCodeSequence=null;
    private List<HumanPerformersSequence> actualHumanPerformersSequence=null;
    private List<ReferredItemSequence> referencedPerformedProcedureStepSequence=null;
    private Log log = LogFactory.getLog("GPPPSItem");
    
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
    
    public class ReferredItemSequence {
        private String SOPClass = null;
        private String SOPInstance = null;
        public void setSOPClass(String s){SOPClass=prepareString(s,16);}
        public String getSOPClass(){return SOPClass;}
        public void setSOPInstance(String s){SOPInstance=prepareString(s,16);}
        public String getSOPInstance(){return SOPInstance;}
    }
    
    public void setGPPPSStatus(String status){
        GPPPStatus=prepareString(status,16);
    }
    
    public String getGPPPSStatus(){
        return GPPPStatus;
    }
    
    public void setPerformedProcedureStepID(String spsID){
        performedProcedureStepID=prepareString(spsID,16);
    }
    
    public String getPerformedProcedureStepID(){
        return performedProcedureStepID;
    }
    
    public void setPerformedProcedureStepDescription(String description){
        performedProcedureStepDescription=prepareString(description,8);
    }
    
    public String getPerformedProcedureStepDescription(){
        return performedProcedureStepDescription;
    }
    
    public void setCommentsOnPerformedProcedureStep(String comments){
        commentsOnPerformedProcedureStep=prepareString(comments,8);
    }
    
    public String getCommentsOnPerformedProcedureStep(){
        return commentsOnPerformedProcedureStep;
    }
    
    public void setPerformedProcedureStepStartDate(Date ppsDate){
        performedProcedureStepStartDate=ppsDate;
    }
    
    public Date getPerformedProcedureStepStartDate(){
        return performedProcedureStepStartDate;
    }
    
    public void setPerformedProcedureStepStartTime(Time ppsTime){
        performedProcedureStepStartTime=ppsTime;
    }
    
    public Time getPerformedProcedureStepStartTime(){
        return performedProcedureStepStartTime;
    }
    
    public void setPerformedProcedureStepEndDate(Date ppsDate){
        performedProcedureStepEndDate=ppsDate;
    }
    
    public Date getPerformedProcedureStepEndDate(){
        return performedProcedureStepEndDate;
    }
    
    public void setPerformedProcedureStepEndTime(Time ppsTime){
        performedProcedureStepEndTime=ppsTime;
    }
    
    public Time getPerformedProcedureStepEndTime(){
        return performedProcedureStepEndTime;
    }
    
    public void setPerformedWorkitemCodeSequence(CodeSequence pwcs){
        performedWorkitemCodeSequence=pwcs;
    }
    
    public CodeSequence getPerformedWorkitemCodeSequence(){
        return performedWorkitemCodeSequence;
    }
    
    public void addPerformedProcessingApplicationsCodeSequence(CodeSequence ppacs){
        if (performedProcessingApplicationsCodeSequence==null) performedProcessingApplicationsCodeSequence=new ArrayList<CodeSequence>(3);
        performedProcessingApplicationsCodeSequence.add(ppacs);
    }
    
    public CodeSequence[] getPerformedProcessingApplicationsCodeSequence() {
        if (performedProcessingApplicationsCodeSequence==null) return null;
        int s=performedProcessingApplicationsCodeSequence.size();
        CodeSequence[] temp=new CodeSequence[s];
        performedProcessingApplicationsCodeSequence.toArray(temp);
        return temp;
    }
    
    public void setPerformedStationNameCodeSequence(CodeSequence psncs){
        performedStationNameCodeSequence=psncs;
    }
    
    public CodeSequence getPerformedStationNameCodeSequence(){
        return performedStationNameCodeSequence;
    }
    
    public void setPerformedStationGeographicLocationCodeSequence(CodeSequence psglcs){
        performedStationGeographicLocationCodeSequence=psglcs;
    }
    
    public CodeSequence getPerformedStationGeographicLocationCodeSequence(){
        return performedStationGeographicLocationCodeSequence;
    }
    
    public void setPerformedStationClassCodeSequence(CodeSequence psccs){
        performedStationClassCodeSequence=psccs;
    }
    
    public CodeSequence getPerformedStationClassCodeSequence(){
        return performedStationClassCodeSequence;
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
    
    private void setGPPPSSOPInstanceUID(String siu){
        SOPinstanceUID=prepareString(siu, 64);
        log .debug("From Item: SOP Instance is :  "   + SOPinstanceUID);
    }
    
    public String getGPPPSSOPInstanceUID(){
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
    private List<ReferencedGPSPSSequence> referencedGPSPSSequence=null;
    
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
    
    public class ReferencedGPSPSSequence {
        
        private String SOPClass = null;
        private String SOPInstance = null;
        private String transactionUID = null;
        
        public void setSOPClass(String s){
            SOPClass=prepareString(s,16);
        }
        
        public String getSOPClass(){
            return SOPClass;
        }
        
        public void setSOPInstance(String s){
            SOPInstance=prepareString(s,16);
        }
        
        public String getSOPInstance(){
            return SOPInstance;
        }
        
        public void setTransactionUID(String trans){
            transactionUID=prepareString(trans,16);
        }
        
        public String getTransactionUID(){
            return transactionUID;
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
    
    public void addReferencedGPSPSSequence(ReferencedGPSPSSequence refGPSPSs){
        if (referencedGPSPSSequence==null) referencedGPSPSSequence=new ArrayList<ReferencedGPSPSSequence>(3);
        referencedGPSPSSequence.add(refGPSPSs);
    }
    
    public ReferencedGPSPSSequence[] getReferencedGPSPSSequence() {
        if (referencedGPSPSSequence==null) return null;
        int s=referencedGPSPSSequence.size();
        ReferencedGPSPSSequence[] temp=new ReferencedGPSPSSequence[s];
        referencedGPSPSSequence.toArray(temp);
        return temp;
    }
    
  //------------------------------------------END OF RELATIONSHIP MODULE---------------------------------
    
  //------------------------------------------RESULTS MODULE---------------------------------------------
    
    private List<OutputInformationSequence> outputInformationSequence=null;
    private List<CodeSequence> requestedSubsequentWorkitemCodeSequence=null;
    private List<CodeSequence> nonDICOMOutputCodeSequence=null;
    
    public class OutputInformationSequence {
        
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
    
    public void addOutputInformationSequence(OutputInformationSequence ois){
        if (outputInformationSequence==null) outputInformationSequence=new ArrayList<OutputInformationSequence>(3);
        outputInformationSequence.add(ois);
    }
    
    public OutputInformationSequence[] getOutputInformationSequence() {
        if (outputInformationSequence==null) return null;
        int s=outputInformationSequence.size();
        OutputInformationSequence[] temp=new OutputInformationSequence[s];
        outputInformationSequence.toArray(temp);
        return temp;
    }
    
    public void addNonDICOMOutputCodeSequence(CodeSequence ndocs){
        if (nonDICOMOutputCodeSequence==null) nonDICOMOutputCodeSequence=new ArrayList<CodeSequence>(3);
        nonDICOMOutputCodeSequence.add(ndocs);
    }
    
    public CodeSequence[] getNonDICOMOutputCodeSequence() {
        if (nonDICOMOutputCodeSequence==null) return null;
        int s=nonDICOMOutputCodeSequence.size();
        CodeSequence[] temp=new CodeSequence[s];
        nonDICOMOutputCodeSequence.toArray(temp);
        return temp;
    }
    
    public void addRequestedSubsequentWorkitemCodeSequence(CodeSequence rswcs){
        if (requestedSubsequentWorkitemCodeSequence==null) requestedSubsequentWorkitemCodeSequence=new ArrayList<CodeSequence>(3);
        requestedSubsequentWorkitemCodeSequence.add(rswcs);
    }
    
    public CodeSequence[] getRequestedSubsequentWorkitemCodeSequence() {
        if (requestedSubsequentWorkitemCodeSequence==null) return null;
        int s=requestedSubsequentWorkitemCodeSequence.size();
        CodeSequence[] temp=new CodeSequence[s];
        requestedSubsequentWorkitemCodeSequence.toArray(temp);
        return temp;
    }
    
  //------------------------------------------END OF RESULTS MODULE--------------------------------------
    
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
