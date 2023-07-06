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

/**
 * The class keeps all the ionformation of Modality Performed Procedure Step
 * @author Mbe
 */
public class MPPSItem implements HtlVo, Serializable {

    public MPPSItem(String SopInstance) {
        setMPPSSOPInstance(SopInstance);
    }

    private void setMPPSSOPInstance(String siu) {
        SOPinstanceUID = prepareString(siu, 64);
    }

    public String getMPPSSOPInstance() {
        return SOPinstanceUID;
    }
    //--------------------------------------------------------------------MPPS IOD:-----------------------------------
    //---------------------------------------------------------------SOP COMMON Module----used elsewhere---------------
    //SOP Class UID
    private static String SOPinstanceUID = null;
    //SOP INstance UID
    //Specific character set
    //Instance Creation Date
    //Instance Creation Time
    //Instance Creator UID
    //Related General SOP Class UID
    //Coding Scheme Identification Sequence
    //*Coding scheme designator
    //*Coding Scheme Registry
    //*Coding Scheme UID
    //*Coding Scheme External UID
    //*Coding Scheme Name
    //*Coding Scheme Version
    //*Responsible organization
    // Time Zone Offset From UTC
    // Contributing  Equipment Srer's Model Nameequence
    // Purpose of Reference Code Sequence
    // Manufacturer
    // Institution Name
    // Institution Address
    // Station Name
    // *Institutional Department Name
    // *Manufacturer's Model Name
    // *Device Serial Number
    // *Software Versions
    // *Spatial Resolution
    // *Date of Last alibration
    // *Time of last calibration
    // *Contribution DateTime
    //  *Contribution Description
    //*SOP instance Status
    //*Sop Authirization Date and Time
    //*SOP Authorization Comment
    //*Authoriation Equimpent certification number
    //*Encrypted Attribute Sequence
    //*Encrypted Content Trasfer Syntax UID
    //*Encrypted COntent
    //--------------------------------------------------------------------------------END OF COMMON MODULE, used elsewhere

//------------------------------------------RELATIONSHIP MODULE-----------------------------------------------------
    public class ScheduledStepAttributesSequence {

        private String studyInstanceUID = null;
        private String referencedStudySOPClass = null;
        private String referencedStudySOPInstance = null;
        private String accessionNumber = null;
        private String placerOrderNumberImSerReq = null;
        private String fillerOrderNumberImSerReq = null;
        private String requestedProcedureDescription = null;
        private String scheduledProcedureStepID = null;
        private String scheduledProcedureStepDescription = null;
        private List<CodeSequence> scheduledProtocolCodeSequence = null;

        //---------------------------------------------------------------------------------------------------------------------
        public ScheduledStepAttributesSequence() {
        }

        public void setStudyInstanceUID(String siUID) {
            studyInstanceUID = prepareString(siUID, 64);
        }

        public String getStudyInstanceUID() {
            return studyInstanceUID;
        }

        public void setReferencedStudySOPClass(String rSOPclass) {
            referencedStudySOPClass = rSOPclass;
        }

        public String getReferencedStudySOPClass() {
            return referencedStudySOPClass;
        }

        public void setReferencedStudySOPInstance(String rSOPinstance) {
            referencedStudySOPInstance = rSOPinstance;
        }

        public String getReferencedStudySOPInstance() {
            return referencedStudySOPInstance;
        }

        public void setAccessionNumber(String an) {
            accessionNumber = prepareString(an, 16);
        }

        public String getAccessionNumber() {
            return accessionNumber;
        }

        public void setPlaceOrderNumberInSerReq(String ponisr) {
            placerOrderNumberImSerReq = prepareString(ponisr, 16);
        }

        public String getPlaceOrderNumbreInSerReq() {
            return placerOrderNumberImSerReq;
        }

        public void setFillerOrderNumberInSerReq(String fonisr) {
            fillerOrderNumberImSerReq = prepareString(fonisr, 16);
        }

        public String getFillerOrderNumberInSerReq() {
            return fillerOrderNumberImSerReq;
        }

        public void setRequestedProcedureDescription(String rpd) {
            requestedProcedureDescription = prepareString(rpd, 64);
        }
        {
        }

        public String getRequestedProcedureDescription() {
            return requestedProcedureDescription;
        }

        public void setScheduledProcedureStepID(String psID) {
            scheduledProcedureStepID = psID;
        }

        public String getScheduledProcedureStepID() {
            return scheduledProcedureStepID;
        }

        public void setScheduledProcedureDescription(String psID) {
            scheduledProcedureStepID = psID;
        }

        public String getScheduledProcedureDescription() {
            return scheduledProcedureStepID;
        }

        public void addScheduledProtocolCodeSequence(CodeSequence spcs) {
            if (scheduledProtocolCodeSequence == null) {
                scheduledProtocolCodeSequence = new ArrayList<CodeSequence>(3); //	3 ids per query should be enough!
            }
            scheduledProtocolCodeSequence.add(spcs);
        }

        public CodeSequence[] getScheduledProtocolCodeSequence() {
            if (scheduledProtocolCodeSequence == null) {
                return null;
            }
            int s = scheduledProtocolCodeSequence.size();
            CodeSequence[] temp = new CodeSequence[s];
            scheduledProtocolCodeSequence.toArray(temp);
            // The array is returned in temp, 'cos it's certainly long enough!
            return temp;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------
    private String patientName = null;
    //*Patient Name
    private String patientID = null;
    //*Patient ID
    private String issuerOfPatientID = null;
    //*Issuer of Patient ID
    private Date birthDate = null;
    //*Patient's Birth Date
    private String patientSex = null;
    //*Patient's Sex
    //*Referenced Patient Sequence (single Molteplicity)
    private String referencedStudySequenceSOPClass = null;
    //Referenced SOP Class UID
    private String referencedStudySequenceSOPInstance = null;
    //Referenced SOP Instance UID
    private List<ScheduledStepAttributesSequence> scheduledStepAttributesSequence = null; //Scheduled Step Attributes sequence (MultipleMoltiplicity)

    //Study Instance UID
    //Referenced Study Sequence (single)
    //*Referenced SOP Class UID
    //*Referenced SOp Instance UID
    //*Accession Number
    //*Place Order Number/imaging Service Request
    //*Filler Order Number/imaging Service Request
    //*Requested Procedure Description
    //*Scheduled Procedure Step ID
    //*Scheduled Procedure Step Description
    //*Scheduled pRotocol Code Sequence (more)
    //include code macro
    //*Protocol COntext Sequence (more)
    //*Content Item Modifier Sequence (more)
    //-----------------------------------------------------------------------------------------------------------------------------------------
    public void setPatientName(String pn) {
        patientName = pn;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientID(String pid) {
        patientID = pid;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setIssuerOfPatientID(String pid) {
        issuerOfPatientID = null;
    }

    public String getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    public void setPatientBirthDate(Date d) {
        birthDate = d;
    }

    public Date getPatientBirthDate() {
        return birthDate;
    }

    public void setPatientSex(String s) {
        patientSex = s;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public void setReferencedStudySequenceSOPClass(String s) {
        referencedStudySequenceSOPClass = s;
    }

    public String getReferencedStudySequenceSOPClass() {
        return referencedStudySequenceSOPClass;
    }

    public void setReferencedStudySequenceSOPInstance(String s) {
        referencedStudySequenceSOPInstance = s;
    }

    public String getReferencedStudySequenceSOPInstance() {
        return referencedStudySequenceSOPInstance;
    }

    public void addScheduledStepAttribute(ScheduledStepAttributesSequence pcs) {
        if (scheduledStepAttributesSequence == null) {
            scheduledStepAttributesSequence = new ArrayList<ScheduledStepAttributesSequence>(3); //	3 ids per query should be enough!
        }
        scheduledStepAttributesSequence.add(pcs);
    }

    public ScheduledStepAttributesSequence[] getScheduledStepAttributesSequence() {
        if (scheduledStepAttributesSequence == null) {
            return null;
        }
        int s = scheduledStepAttributesSequence.size();
        ScheduledStepAttributesSequence[] temp = new ScheduledStepAttributesSequence[s];
        scheduledStepAttributesSequence.toArray(temp); // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }
    //------------------------------------------------------end of relationship module---------------------------------------------------------
    //--------------------------------------------------------------MPPS Information-----------------------------------------------------------
    private String performedStationAETitle = null;
    //*Performed Station AE Title
    private String performedStationName = null;
    //*Performed Station Name
    private String performedLocation = null;
    //*Performed Location
    private Date performedProcedureStepStartDate = null;
    //*Performed Procedure Step Start Date
    private Time performedProcedureStepStartTime = null;
    //*Performed Procedure Step Start TIme
    private String performedProcedureStepID = null;
    //*Performed Procedure Step ID
    private Date performedProcedureStepEndDate = null;
    //*Performed Procedure Step End Date
    private Time performedProcedureStepEndTime = null;
    //*Performed Procedure Step End TIme
    private String performedProcedureStepStatus = null;
    //*Performed Procedure Step Status
    private String performedProcedureStepDescription = null;
    //*Performed Procedure Step Description
    private String commentsOnPPS = null;
    //*Comments on the Performed Procedure Step
    private String performedProcedureTypeDescription = null;
    //*Performed Procedure Type Descriptionnull
    private CodeSequence procedureCodeSequence = null;
    //*Procedure Code Sequence
    //include code sequence
    private CodeSequence ppsDiscontinuationReasonCodeSequence = null;
    //*Performed Procedure Step Discontinuation Reason COde Sequence
    //include code sequence

    //--------------------------------------------------------MPPS INFO SET AND GET METHODS----------------------------------------------------
    public void setPerformedStationAETitle(String pst) {
        performedStationAETitle = prepareString(pst, 16);
    }
    //*Performed Station AE Title

    public String getPerformedStationAETitle() {
        return performedStationAETitle;
    }

    public void setPerformedLocation(String pl) {
        performedLocation = pl;
    }

    public String getPerformedLocation() {
        return performedLocation;
    }

    public void setPerformedStationName(String psn) {
        performedStationName = prepareString(psn, 16);
    }

    public String getPerformedStationName() {
        return performedStationName;
    }
    //*Performed Station Name

    public void setPerformedProcedureStepStartDate(Date ppssd) {
        performedProcedureStepStartDate = ppssd;
    }

    public Date getPerformedProcedureStepStartDate() {
        return performedProcedureStepStartDate;
    }
    //*Performed Procedure Step Start Date

    public void setPerformedProcedureStepStartTime(Time ppsst) {
        performedProcedureStepStartTime = ppsst;
    }

    public Time getPerformedProcedureStepStartTime() {
        return performedProcedureStepStartTime;
    }
    //*Performed Procedure Step Start TIme

    public void setPerformedProcedureStepID(String ppsID) {
        performedProcedureStepID = prepareString(ppsID, 16);
    }

    public String getPerformedProcedureStepID() {
        return performedProcedureStepID;
    }
    //*Performed Procedure Step ID

    public void setPerformedProcedureStepEndDate(Date ppsed) {
        performedProcedureStepEndDate = ppsed;
    }

    public Date getPerformedProcedureStepEndDate() {
        return performedProcedureStepEndDate;
    }
    //*Performed Procedure Step End Date

    public void setPerformedProcedureStepEndTime(Time ppsed) {
        performedProcedureStepEndTime = ppsed;
    }

    public Time getPerformedProcedureStepEndTime() {
        return performedProcedureStepEndTime;
    }
    //*Performed Procedure Step End TIme

    public void setPerformedProcedureStepStatus(String ppss) {
        performedProcedureStepStatus = prepareString(ppss, 16);
    }

    public String getPerformedProcedureStepStatus() {
        return performedProcedureStepStatus;
    }
    //*Performed Procedure Step Status

    public void setPerformedProcedureStepDescription(String ppsd) {
        performedProcedureStepDescription = prepareString(ppsd, 64);
    }

    public String getPerformedProcedureStepDescription() {
        return performedProcedureStepDescription;
    }
    //*Performed Procedure Step Description

    public void setCommentsOnPPS(String comments) {
        commentsOnPPS = prepareString(comments, 256);
    }

    public String getCommentsOnPPS() {
        return commentsOnPPS;
    }
    //*Comments on the Performed Procedure Step

    public void setPerformedProcedureTypeDescription(String ppstd) {
        performedProcedureTypeDescription = prepareString(ppstd, 64);
    }

    public String getPerformedProcedureTypeDescription() {
        return performedProcedureTypeDescription;
    }
    //*Performed Procedure Type Descriptionnull

    public void setProcedureCodeSequence(CodeSequence pcs) {
        procedureCodeSequence = pcs;
    }

    public CodeSequence getProcedureCodeSequence() {
        return procedureCodeSequence;
    }

    //*Procedure Code Sequence
    //include code sequence
    public void setDiscontinuationReasonCodeSequence(CodeSequence drcs) {
        ppsDiscontinuationReasonCodeSequence = drcs;
    }

    public CodeSequence getDiscontinuationReasonCodeSequence() {
        return ppsDiscontinuationReasonCodeSequence;
    }
    //*Performed Procedure Step Discontinuation Reason COde Sequence
    //include code sequence
    //-----------------------------------------------------------end MMPS INFO METHODS------------------------------------------------------------------
    ///--------------------------------------------------------------------------end of MPPS info module----------------------------------------

//------------------------------------------------------------Image Acquisition Result Module-----------------------------------------------
    public class PersonIdentificationMacro {

        private CodeSequence personIdentificationCodeSequence = null;
        private String personsAddress = null;
        private String personsTelephoneNumbers = null;
        private String institutionName = null;
        private String institutionAddress = null;
        private CodeSequence institutionCodeSequence = null;

        //-----------------------------------------------------------------------------------------------------------------------------------
        public void setPersonIdentificationCodeSequence(CodeSequence pics) {
            personIdentificationCodeSequence = pics;
        }

        public CodeSequence getPersonIdentificationCodeSequence() {
            return personIdentificationCodeSequence;
        }

        public void setPersonAddress(String pa) {
            personsAddress = pa;
        }

        public String getPersonAddress() {
            return personsAddress;
        }

        public void setPersonsTelephoneNumber(String pcs) {
            personsTelephoneNumbers = pcs;
        }

        public String getPersonsTelephoneNumbers() {
            return personsTelephoneNumbers;
            // The array is returned in temp, 'cos it's certainly long enough!
        }

        public void setInstitutionName(String in) {
            institutionName = in;
        }

        public String getInstitutionName() {
            return institutionName;
        }

        public void setInstitutionAddress(String in) {
            institutionAddress = in;
        }

        public String getInstitutionAddress() {
            return institutionAddress;
        }

        public void setInstitutionCodeSequence(CodeSequence pics) {
            institutionCodeSequence = pics;
        }

        public CodeSequence getInstitutionCodeSequence() {
            return institutionCodeSequence;
        }
    }

    public class ReferredItemSequence {

        private String SOPClass = null;
        private String SOPInstance = null;

        public void setSOPClass(String s) {
            SOPClass = prepareString(s, 64);
        }

        public String getSOPClass() {
            return SOPClass;
        }

        public void setSOPInstance(String s) {
            SOPInstance = prepareString(s, 64);
        }

        public String getSOPInstance() {
            return SOPInstance;
        }
    }

//-------------------------------------------------------------------------------------------------------------------------------------
    public class PerformedSeriesSequence {

        private List<String> performingPhysiciansName = null;
        //performing physician's Name
        private List<PersonIdentificationMacro> performingPhysicianIdentificationSequence = null;
        //performing phisycian's Identification Sequence (more)
        //include person's identification macro
        private List<String> operatorsName = null;
        //Operator's Name (more)
        private List<PersonIdentificationMacro> operatorsIdentificationSequence = null;
        //Operator Identification Sequence (more)
        //include person IDentification macro table
        private String protocolName = null;
        //protocol name
        private String seriesInstanceUID = null;
        //Series Instace UID
        private String seriesDescription = null;
        //Series Description
        private String retrieveAETitle = null;
        //Retrieve AE Title
        private List<ReferredItemSequence> referredImageSequence = null;
        //Referenced Image Sequence (more)
        //Referenced SOP class UID
        //Referenced SOp Instance UID
        private List<ReferredItemSequence> referredNONImageSequence = null;

        //Referenced NOn-Image COmposite SOP Instance Sequence (more)
        //Referenced SOP Class UID
        //Referenced SOP Instance UID
        //-------------------------------------------------------------------------------------------------
        public void addPerformingPhysicianName(String n) {
            if (performingPhysiciansName == null) {
                performingPhysiciansName = new ArrayList<String>(3);
            }
            //	3 ids per query should be enough!
            performingPhysiciansName.add(prepareString(n, 180));
        }

        public String[] getPerformingPhysicianName() {
            if (performingPhysiciansName == null) {
                return null;
            }
            int s = performingPhysiciansName.size();
            String[] temp = new String[s];
            performingPhysiciansName.toArray(temp);
            // The array is returned in temp, 'cos it's certainly long enough!
            return temp;
        }

        public void addPerformingPhysicianIdentificationSequence(PersonIdentificationMacro pcs) {
            if (performingPhysicianIdentificationSequence == null) {
                performingPhysicianIdentificationSequence = new ArrayList<PersonIdentificationMacro>(3);
            }
            //	3 ids per query should be enough!
            performingPhysicianIdentificationSequence.add(pcs);
        }

        public PersonIdentificationMacro[] getPerformingPhisycianIdentificationSequence() {
            if (performingPhysicianIdentificationSequence == null) {
                return null;
            }
            int s = performingPhysicianIdentificationSequence.size();
            PersonIdentificationMacro[] temp = new PersonIdentificationMacro[s];
            performingPhysicianIdentificationSequence.toArray(temp);
            // The array is returned in temp, 'cos it's certainly long enough!
            return temp;
        }

        public void addOperatorsName(String n) {
            if (operatorsName == null) {
                operatorsName = new ArrayList<String>(3);
            }
            //	3 ids per query should be enough!
            operatorsName.add(prepareString(n, 180));
        }

        public String[] getOperatorsName() {
            if (operatorsName == null) {
                return null;
            }
            int s = operatorsName.size();
            String[] temp = new String[s];
            operatorsName.toArray(temp);
            // The array is returned in temp, 'cos it's certainly long enough!
            return temp;
        }

        public void setOperatorIdentificationSequence(PersonIdentificationMacro pcs) {
            if (operatorsIdentificationSequence == null) {
                operatorsIdentificationSequence = new ArrayList<PersonIdentificationMacro>(3);
            }
            //	3 ids per query should be enough!
            operatorsIdentificationSequence.add(pcs);
        }

        public PersonIdentificationMacro[] getOperatorsIdentificationSequence() {
            if (operatorsIdentificationSequence == null) {
                return null;
            }
            int s = operatorsIdentificationSequence.size();
            PersonIdentificationMacro[] temp = new PersonIdentificationMacro[s];
            operatorsIdentificationSequence.toArray(temp);
            // The array is returned in temp, 'cos it's certainly long enough!
            return temp;
        }

        public void setProtocolName(String pn) {
            protocolName = prepareString(pn, 64);
        }

        public String getProtocolName() {
            return protocolName;
        }
        //protocol name

        public void setSeriesInstanceUID(String pn) {
            seriesInstanceUID = prepareString(pn, 64);
        }

        public String getSeriesInstanceUID() {
            return seriesInstanceUID;
        }
        //protocol name

        public void setSeriesDescription(String pn) {
            seriesDescription = prepareString(pn, 16);
        }

        public String getseriesDescription() {
            return seriesDescription;
        }
        //protocol name

        public void setRetrieveAETitle(String pn) {
            retrieveAETitle = prepareString(pn, 16);
        }

        public String getRetrieveAETitle() {
            return retrieveAETitle;
        }
        //protocol name

        public void addReferredImageSequence(ReferredItemSequence pcs) {
            if (referredImageSequence == null) {
                referredImageSequence = new ArrayList<ReferredItemSequence>(3);
            }
            //	3 ids per query should be enough!
            referredImageSequence.add(pcs);
        }

        public ReferredItemSequence[] getReferredImageSequence() {
            if (referredImageSequence == null) {
                return null;
            }
            int s = referredImageSequence.size();
            ReferredItemSequence[] temp = new ReferredItemSequence[s];
            referredImageSequence.toArray(temp);
            // The array is returned in temp, 'cos it's certainly long enough!
            return temp;
        }

        public void addReferredNONImageSequence(ReferredItemSequence pcs) {
            if (referredNONImageSequence == null) {
                referredNONImageSequence = new ArrayList<ReferredItemSequence>(3);
            }
            //	3 ids per query should be enough!
            referredNONImageSequence.add(pcs);
        }

        public ReferredItemSequence[] getReferredNONImageSequence() {
            if (referredNONImageSequence == null) {
                return null;
            }
            int s = referredNONImageSequence.size();
            ReferredItemSequence[] temp = new ReferredItemSequence[s];
            referredNONImageSequence.toArray(temp);
            // The array is returned in temp, 'cos it's certainly long enough!
            return temp;
        }
    }
    //----------------------------S---------------------------------------------------------------------------------------------------------------------
    private String modality = null;
    //Modality
    //private String studyID = null;
    //Study ID
    private List<CodeSequence> performedProtocolCodeSequence = null;
    //Performed Protocol Code Sequence (more)
    //*Include Code sequence
    //protocol context sequence
    //content item macro
    private List<PerformedSeriesSequence> performedSeriesSequence = null;
    //performed Series Sequence (more)
    //private String performingPhysiciansName = null;
    //performing physician's Name
    //performing phisycian's Identification Sequence (more)
    //include person's identification macro
    //private List operatorsName = null;
    //Operator's Name (more)
    //Operator Identification Sequence (more)
    //include person IDentification macro table
    //private String protocolName = null;
    //protocol name
    //Series Instace UID
    //private seriesDescription
    //Serie Description

    //private retrieveAETitle
    //Retrieve AE Title
    //Referenced Image Sequence (more)
    //Referenced SOP class UID
    //Referenced SOp Instance UID
    //Referenced NOn-Image COmposite SOP Instance Sequence (more)
    //Referenced SOP Class UID
    //Referenced SOP Instance UID
    //------------------------------------------------------------------------------------------------------------------------------------------
    public void setModality(String m) {
        modality = prepareString(m, 16);
    }

    public String getModality() {
        return modality;
    }
    //public void setStudyID(String m){studyID=m;}
    //public String getStudyID(){return studyID;}

    public void addPerformedProtocolCodeSequence(CodeSequence pcs) {
        if (performedProtocolCodeSequence == null) {
            performedProtocolCodeSequence = new ArrayList<CodeSequence>(3);
        }
        //	3 ids per query should be enough!
        performedProtocolCodeSequence.add(pcs);
    }

    public CodeSequence[] getPerformedProtocolCodeSequence() {
        if (performedProtocolCodeSequence == null) {
            return null;
        }
        int s = performedProtocolCodeSequence.size();
        CodeSequence[] temp = new CodeSequence[s];
        performedProtocolCodeSequence.toArray(temp);
        // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }

    public void addPerformedSeriesSequence(PerformedSeriesSequence pcs) {
        if (performedSeriesSequence == null) {
            performedSeriesSequence = new ArrayList<PerformedSeriesSequence>(3);
        }
        //	3 ids per query should be enough!
        performedSeriesSequence.add(pcs);
    }

    public PerformedSeriesSequence[] getPerformedSeriesSequence() {
        if (performedSeriesSequence == null) {
            return null;
        }
        int s = performedSeriesSequence.size();
        PerformedSeriesSequence[] temp = new PerformedSeriesSequence[s];
        performedSeriesSequence.toArray(temp);
        // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }
    //---------------------------------------------------------end of image Acquisition results module--------------------------------------------------

//--------------------------------------------------------------------------Radiation Dose Module------------------------------------------
    public class ExposureDoseSequence {

        private String radiationMode = null;
        private String kvp = null;
        private String xRayTubeCurrent = null;
        private String exposureTime = null;
        private String filterType = null;
        private String filterMaterial = null;

        //-----------------------------------methods---------------------------------------------------------------------------------
        public void setRadiationMode(String rm) {
            radiationMode = prepareString(rm, 16);
        }

        public String getRadiationMode() {
            return radiationMode;
        }

        public void setKvp(String rm) {
            kvp = prepareDouble(rm);
        }

        //PARSED!!!
        public String getKvp() {
            return kvp;
        }

        public void setxRayTubeCurrent(String rm) {
            xRayTubeCurrent = prepareInt(rm);
        }

        public String getxRayTubeCurrent() {
            return radiationMode;
        }

        public void setExposureTime(String rm) {
            exposureTime = prepareInt(rm);
        }

        public String getExposureTime() {
            return exposureTime;
        }

        public void setFilterType(String rm) {
            filterType = prepareString(rm, 16);
        }

        public String getFilterType() {
            return filterType;
        }

        public void setFilterMaterial(String rm) {
            filterMaterial = prepareString(rm, 16);
        }

        public String getFilterMaterial() {
            return filterMaterial;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------
    private CodeSequence anatomicsStructureSpaceOrRegionCodeSequence = null;
    //*Anatomic Structure, Space ore Region Sequence
    //*include code sequence macro
    private String totalTimeOfFluoroscopy = null; /*int*/
    //*Total time of Fluoroscopy
    private String totalNumberOfExposure = null; /*int*/
    //*Total Number of Exposures
    private String distanceSourceToDetector = null; /*double parsed from scientific*/
    //*Distance Source to Detector
    private String distanceSourceToEntrance = null; /*double parsed form scientific*/
    //*Distance Source to Entranceprivate String
    private String entranceDose = null; /*int*/
    //*Entrance Dose
    private String entranceDoseMGY = null; /*double parsed form scientific*/
    //*Entrence Dose in mGy
    private String exposedArea = null; /*int*/
    //*ExposedArea
    private String imageAreaDoseProduct = null; /*double parsed form scientific*/
    //*Image Area DOse Product
    private String commentsOnRadiationDose = null;
    //*Comments on Radiation Dose
    private List<ExposureDoseSequence> exposureDoseSequence = null;

    //*Exposure Dose Sequence (more)
    //private String radiationMode = null;
    //*Radiation Mode
    //private String kvp /*double precision*/ = null;
    //*KVp
    //String xRayTubeCurrent = null;
    //*X-ray Tube Current in uA
    //private String exposureTime = null;
    //*Exposure Time
    //private String filterType = null;
    //*Filter Type
    //private String filterMaterial = null;
    //*Filter Material
    //-----------------------------------------------------------------------------------------------------------------------------------
    public void setAnatomicsStructureSpaceOrRegionCodeSequence(CodeSequence cs) {
        anatomicsStructureSpaceOrRegionCodeSequence = cs;
    }

    public CodeSequence getAnatomicsStructureSpaceOrRegionCodeSequence() {
        return anatomicsStructureSpaceOrRegionCodeSequence;
    }

    public void setTotalTimeOfFluoroscopy(String ttf) {
        totalTimeOfFluoroscopy = prepareInt(ttf);
    }

    public String getTotalTimeOfFluoroscopy() {
        return totalTimeOfFluoroscopy;
    }

    public void setTotalNumberOfExposure(String tne) {
        totalNumberOfExposure = prepareInt(tne);
    }

    public String getTotalNumberOfExposures() {
        return totalNumberOfExposure;
    }

    public void setDistanceSourceToDetector(String dstd) {
        distanceSourceToDetector = prepareDouble(dstd);
    }

    public String getDistanceSourceToDetector() {
        return distanceSourceToDetector;
    }

    public void setDistanceSourceToEntrance(String dste) {
        distanceSourceToEntrance = prepareDouble(dste);
    }

    public String getDistanceSourceToEntrance() {
        return distanceSourceToEntrance;
    }

    public void setEntranceDose(String ed) {
        entranceDose = prepareInt(ed);
    }

    public String getEntranceDose() {
        return entranceDose;
    }

    public void setEntranceDoseMGY(String edMGY) {
        entranceDoseMGY = prepareDouble(edMGY);
    }

    public String getEntranceDoseMGY() {
        return entranceDoseMGY;
    }

    public void setExposedArea(String ea) {
        exposedArea = prepareInt(ea);
    }

    public String getExposedArea() {
        return exposedArea;
    }

    public void setImageAreaDoseProduct(String iadp) {
        imageAreaDoseProduct = prepareDouble(iadp);
    }

    public String getImageAreaDoseProduct() {
        return imageAreaDoseProduct;
    }

    public void setCommentsOnRadiationDose(String comments) {
        commentsOnRadiationDose = prepareString(comments, 256);
    }

    public String getCommentsOnRadiationDose() {
        return commentsOnRadiationDose;
    }

    public void addExposureDoseSequence(ExposureDoseSequence pcs) {
        if (exposureDoseSequence == null) {
            exposureDoseSequence = new ArrayList<ExposureDoseSequence>(3);
        }
        //	3 ids per query should be enough!
        exposureDoseSequence.add(pcs);
    }

    public ExposureDoseSequence[] getExposureDoseSequence() {
        if (exposureDoseSequence == null) {
            return null;
        }
        int s = exposureDoseSequence.size();
        ExposureDoseSequence[] temp = new ExposureDoseSequence[s];
        exposureDoseSequence.toArray(temp);
        // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }
    //------------------------------------------------------------------------------------------end of radiation Dose Module-------------------

//-----------------------------------------------------------------------------Billing and Material Management Code Module Attributes
    public class FilmConsumptionSequence {

        private String numberOfFilms = null;
        //*Number of Films
        private String mediumType = null;
        //*Medium Type
        private String filmSizeID = null;

        //Film Size ID
        //---------------------------------------METHODS
        public void setNumberOfFilms(String nf) {
            numberOfFilms = prepareInt(nf);
        }

        public String getNumberOfFilms() {
            return numberOfFilms;
        }

        public void setMediumType(String mt) {
            mediumType = prepareString(mt, 16);
        }

        public String getMediumType() {
            return mediumType;
        }

        public void setFilmSizeID(String mt) {
            filmSizeID = prepareString(mt, 16);
        }

        public String getFilmSizeID() {
            return filmSizeID;
        }
        //        /*public void addMediumType(String mt){
        //          if (mediumType==null) mediumType=new ArrayList(3);
        //	3 ids per query should be enough!
        //            //     exposureDoseSequence.add(prepareString(mt,16));}
        //    /* public String[] getMediumType() {
        //                 if (mediumType==null) return null;
        //                int s=mediumType.size();
        //                String[] temp=new String[s];
        //                mediumType.toArray(temp);
        // The array is returned in temp, 'cos it's certainly long enough!
        //         //         return temp;}
        //           public void addFilmSizeID(String mt){
        //                     if (mediumType==null) mediumType=new ArrayList(3);
        //                       //	3 ids per query should be enough!
        //                      exposureDoseSequence.add(prepareString(mt,16));}
        //          public String[] getFilmSizeID() {
        //                     if (filmSizeID==null) return null;
        //                     int s=filmSizeID.size();
        //    //                    String[] temp=new String[s];
        //               filmSizeID.toArray(temp);
        //                     // The array is returned in temp, 'cos it's certainly long enough!
        //                return temp;}
    }

//----------------------------FILM CONSUPTION---------------------------------------------------------------------------------------
    public class BillingItemSequence {

        private CodeSequence billingItemCode = null;
        private String quantity = null;
        private CodeSequence measuringUnitsSequence = null;
        //-------------------------------

        public void setBillingItemCode(CodeSequence bic) {
            billingItemCode = bic;
        }

        public CodeSequence getBillingItemCode() {
            return billingItemCode;
        }

        public void setQuantity(String q) {
            quantity = q;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setMeasuringUnitsSequence(CodeSequence bic) {
            measuringUnitsSequence = bic;
        }

        public CodeSequence getMeasuringUnitsSequence() {
            return measuringUnitsSequence;
        }
    }
    //----------------------------------------------------------------------------------------------------------------------------------
    private List<CodeSequence> billingProcedureStepCodeSequence = null;
    //*Billing Procedure Step Sequence (more)
    //include Code Sequence Macro
    private List<FilmConsumptionSequence> filmConsumptionSequence = null;
    //*Film Consuption Sequence (more)
    //private String numberOfFilms = null;                                                     //*Number of Films
    //private String mediumType = null;                                                        //*Medium Type
    //private String filmSizeID = null;                                                        //Film Size ID
    //Billing Supplies and Device Sequence (more)
    private List<BillingItemSequence> billingItemSequence = null;
    //*Billing Item Sequence    one
    //include code sequence
    //Quantity Seuquence one
    //private String/*int*/ quantity = null;                                                            //quantity
    //private CodeSequence measuringUnitsCodeSequence = null;                                           //Measuring Units Sequence
    //include Code Seuquence Macro

    //--------------------------------------------------------------------------------------------end of Billing Module---------------
    public void addBillingProcedureStepCodeSequence(CodeSequence bic) {
        if (billingProcedureStepCodeSequence == null) {
            billingProcedureStepCodeSequence = new ArrayList<CodeSequence>(3);
        }
        //	3 ids per query should be enough!
        billingProcedureStepCodeSequence.add(bic);
    }

    public CodeSequence[] getBillingProcedureStepCodeSequence() {
        if (billingProcedureStepCodeSequence == null) {
            return null;
        }
        int s = billingProcedureStepCodeSequence.size();
        CodeSequence[] temp = new CodeSequence[s];
        billingProcedureStepCodeSequence.toArray(temp);
        // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }

    public void addFilmConsumptionSequence(FilmConsumptionSequence mt) {
        if (filmConsumptionSequence == null) {
            filmConsumptionSequence = new ArrayList<FilmConsumptionSequence>(3);
        }
        //	3 ids per query should be enough!
        filmConsumptionSequence.add(mt);
    }

    public FilmConsumptionSequence[] getFilmConsumptionSequence() {
        if (filmConsumptionSequence == null) {
            return null;
        }
        int s = filmConsumptionSequence.size();
        FilmConsumptionSequence[] temp = new FilmConsumptionSequence[s];
        filmConsumptionSequence.toArray(temp);
        // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }

    public void addBillingItemSequence(BillingItemSequence mt) {
        if (billingItemSequence == null) {
            billingItemSequence = new ArrayList<BillingItemSequence>(3);
        }
        //	3 ids per query should be enough!
        billingItemSequence.add(mt);
    }

    public BillingItemSequence[] getBillingItemSequence() {
        if (billingItemSequence == null) {
            return null;
        }
        int s = billingItemSequence.size();
        BillingItemSequence[] temp = new BillingItemSequence[s];
        billingItemSequence.toArray(temp);
        // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }

    //------------------------------------------------------------Data Verification Methods------------------------
    public char getToPerform() {
        return ' ';
    }

    public String prepareString(String arg, int len) {
        if (arg == null) {
            return null;
        }
        String temp = arg.trim();
        return (temp.length() > len) ? temp.substring(0, len) : temp;
    }

    public String prepareDouble(String arg) {
        if (arg == null) {
            return null;
        }
        String temp = null;
        try {
            temp = (Double.valueOf(arg.trim())).toString();
        } catch (NumberFormatException e) {
            temp = null;
        }
        return temp;
    }

    public String prepareLong(String arg) {
        if (arg == null) {
            return null;
        }
        String temp = null;
        try {
            temp = (Long.valueOf(arg.trim())).toString();
        } catch (NumberFormatException e) {
            temp = null;
        }
        return temp;
    }

    public String prepareInt(String arg) {
        if (arg == null) {
            return null;
        }
        String temp = null;
        try {
            temp = (Integer.valueOf(arg.trim())).toString();
        } catch (NumberFormatException e) {
            temp = null;
        }
        return temp;
    }

    public void reset() {
        //return null
        //log.debug("ciao");
    }

    public void setToPerform(char arg) {
    }
}