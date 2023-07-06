/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.util.LinkedHashSet;
import org.dcm4che.auditlog.User;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 1.1 $ $Date: 2008/01/06 15:21:45 $
 * @since August 27, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class InstancesAction {
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------

    
    private String AEtitle;
    
    private String action;
    private String accessionNumber;
    
    private LinkedHashSet suids = new LinkedHashSet(3);
    private Patient patient;
    
    private LinkedHashSet cuids = new LinkedHashSet(7);
    private LinkedHashSet series = new LinkedHashSet(7);
    private LinkedHashSet instances = new LinkedHashSet(7);
    private User user;
    private String mppsUID;
    private int numberOfInstances = 0;
    private String patientSex = null;
    private String patientBirthDate = null;
    private String studyInstanceUID = null;      // di questi bisogna creare le variabili nella classe StrutturaDati
    private String studyDate = null;
    private String studyTime=null;
    private String referringPhysicianName = null;
    private String studyID = null;;
    //variable added for XDS-I
    
    private String AuthorInstitution=null; //gia inserita sia come XDSDocumentEntry sia come XDSSubmissionSet
    
    //Come creare i seguenti campi?
                /*
                private String StudyInstanceUID=null;
                private Date StudyDate=null; /////////
                private String StudyTime=null;
                private String ReferringPhysicianName=null;
                private String StudyID=null;
                private String AccessionNumber=null;
                private String SeriesInstanceUID=null;
                private String SeriesNumber=null;
                private String SOPClassUID=null;
                private String SOPInstanceUID=null;
                private String AETitle=null;
                 */
    
    
    private String XDSDocumentEntry=null;
    private String XDSDocumentEntry__UUID=null; //nota bene: questo � il campo contenente il codice UUID relativo al campo XDSDocumentEntry, non il campo XDSDocumentEntryUUID
    private String XDSDocumentEntryHealthCareFaclilityTypeCode=null;
    private String XDSDocumentEntryHealthCareFaclilityTypeCodeUUID=null;
    private String XDSDocumentEntryConfidentialityCode=null;
    private String XDSDocumentEntryConfidentialityCodeUUID=null;
    private String XDSDocumentEntryFormatCode=null;
    private String XDSDocumentEntryFormatCodeUUID=null;
    private String XDSDocumentEntryPraticeSettingCode=null;
    private String XDSDocumentEntryPraticeSettingCodeUUID=null;
    private String XDSDocumentEntryTypeCode=null;
    private String XDSDocumentEntryTypeCodeUUID=null;
    private String XDSDocumentEntryClassCode=null;
    private String XDSDocumentEntryClassCodeUUID=null;
    private String XDSDocumentEntryUniqueID=null;
    private String XDSDocumentEntryUniqueIDUUID=null;
    private String XDSDocumentEntryPatientID=null;
    private String XDSDocumentEntryPatientIDUUID=null;
    private String XDSSubmissionSet=null;
    private String XDSSubmissionSetUUID=null;
    private String XDSSubmissionSetUniqueID=null;
    private String XDSSubmissionSetUniqueIDUUID=null;
    private String XDSSubmissionSetSourceID=null;
    private String XDSSubmissionSetSourceIDUUID=null;
    private String XDSSubmissionSetPatientID=null;
    private String XDSSubmissionSetPatientIDUUID=null;
    private String XDSSubmissionSetContentTypeCode=null;
    private String XDSSubmissionSetContentTypeCodeUUID=null;
    private String XDSFolder=null;
    private String XDSFolderUUID=null;
    private String XDSFolderPatientID=null;
    private String XDSFolderPatientIDUUID=null;
    private String XDSFolderUniqueID=null;
    private String XDSFolderUniqueIDUUID=null;
    private String XDSFolderComments=null;
    
    private String XDSDocumentEntryAuthorSpecialty=null;
    private String XDSDocumentEntryAuthorInstitution=null;
    private String XDSDocumentEntryAuthorPerson=null;
    private String XDSDocumentEntryAuthorRole=null;
    private String XDSDocumentEntryAuthorDisplayName=null;
    private String XDSDocumentEntryCreationTime=null; /// � una data
    private String XDSDocumentEntryEventCodeList=null;
    private String XDSDocumentEntryEventCodeDisplay=null;
    private String XDSDocumentEntryNameList=null;
    private String XDSDocumentEntryLanguageCode=null;
    private String XDSDocumentEntryLegalAuthenticator=null;
    private String XDSDocumentEntryMimeType=null;
    private String XDSDocumentEntryParentDocumentRelatioship=null;
    private String XDSDocumentEntryParentDocumentID=null;
    
    private String XDSDocumentEntryPraticeSettingCodeDisplayName=null;
    private String XDSDocumentEntryTypeCodeDisplayName=null;
    
    private String XDSDocumentEntryServiceStartTime=null; /// � una data
    private String XDSDocumentEntryServiceStopTime=null; ///� una data
    private String XDSDocumentEntrySourcePatientID=null;
                /*
                private String XDSDocumentEntrySourcePatientInfo=null;  /// � costituito dalla somma dei singoli dati-paziente seguenti:
                 */
    private String PatientName=null;
    private String PatientId=null;
    private String PatientSex=null;
    private String PatientAddress=null;
    private String PatientBirthDate=null;
    
    private String XDSDocumentEntryTitle=null;
    private String XDSDocumentEntryUUID=null;
    private String XDSDocumentEntrySize=null;
    private String XDSDocumentEntryHash=null;
    private String XDSDocumentEntryAvailabilityStatus=null;
    private String XDSSubmissionSetAuthorDepartment=null;
    private String XDSSubmissionSetAuthorInstitution=null;
    private String XDSSubmissionSetAuthorPerson=null;
    private String XDSSubmissionSetComments=null;
    
    private String XDSSubmissionSetContentTypeCodeDisplayName=null;
    
    private String XDSSubmissionSetSubmissionTime=null; ///� una data
    
    
    // Constructors --------------------------------------------------
    public InstancesAction(String action, String suid, Patient patient) {
        this.action = action;
        addStudyInstanceUID(suid);
        this.patient = patient;
    }
    
    
    //Metodi set dei campi UUID
    public final void setXDSDocumentEntry__UUID(String XDSDocumentEntry__UUID) {
        this.XDSDocumentEntry__UUID = XDSDocumentEntry__UUID;
    }
    
    public final void setXDSDocumentEntryHealthCareFaclilityTypeCodeUUID(String XDSDocumentEntryHealthCareFaclilityTypeCodeUUID) {
        this.XDSDocumentEntryHealthCareFaclilityTypeCodeUUID = XDSDocumentEntryHealthCareFaclilityTypeCodeUUID;
    }
    
    public final void setXDSDocumentEntryConfidentialityCodeUUID(String XDSDocumentEntryConfidentialityCodeUUID) {
        this.XDSDocumentEntryConfidentialityCodeUUID = XDSDocumentEntryConfidentialityCodeUUID;
    }
    
    public final void setXDSDocumentEntryFormatCodeUUID(String XDSDocumentEntryFormatCodeUUID) {
        this.XDSDocumentEntryFormatCodeUUID = XDSDocumentEntryFormatCodeUUID;
    }
    
    public final void setXDSDocumentEntryPraticeSettingCodeUUID(String XDSDocumentEntryPraticeSettingCodeUUID) {
        this.XDSDocumentEntryPraticeSettingCodeUUID = XDSDocumentEntryPraticeSettingCodeUUID;
    }
    
    public final void setXDSDocumentEntryTypeCodeUUID(String XDSDocumentEntryTypeCodeUUID) {
        this.XDSDocumentEntryTypeCodeUUID = XDSDocumentEntryTypeCodeUUID;
    }
    
    public final void setXDSDocumentEntryClassCodeUUID(String XDSDocumentEntryClassCodeUUID) {
        this.XDSDocumentEntryClassCodeUUID = XDSDocumentEntryClassCodeUUID;
    }
    
    public final void setXDSDocumentEntryUniqueIDUUID(String XDSDocumentEntryUniqueIDUUID) {
        this.XDSDocumentEntryUniqueIDUUID = XDSDocumentEntryUniqueIDUUID;
    }
    
    public final void setXDSDocumentEntryPatientIDUUID(String XDSDocumentEntryPatientIDUUID) {
        this.XDSDocumentEntryPatientIDUUID = XDSDocumentEntryPatientIDUUID;
    }
    
    public final void setXDSSubmissionSetUUID(String XDSSubmissionSetUUID) {
        this.XDSSubmissionSetUUID = XDSSubmissionSetUUID;
    }
    
    public final void setXDSSubmissionSetUniqueIDUUID(String XDSSubmissionSetUniqueIDUUID) {
        this.XDSSubmissionSetUniqueIDUUID = XDSSubmissionSetUniqueIDUUID;
    }
    
    public final void setXDSSubmissionSetSourceIDUUID(String XDSSubmissionSetSourceIDUUID) {
        this.XDSSubmissionSetSourceIDUUID = XDSSubmissionSetSourceIDUUID;
    }
    
    public final void setXDSSubmissionSetPatientIDUUID(String XDSSubmissionSetPatientIDUUID) {
        this.XDSSubmissionSetPatientIDUUID = XDSSubmissionSetPatientIDUUID;
    }
    
    public final void setXDSSubmissionSetContentTypeCodeUUID(String XDSSubmissionSetContentTypeCodeUUID) {
        this.XDSSubmissionSetContentTypeCodeUUID = XDSSubmissionSetContentTypeCodeUUID;
    }
    
    public final void setXDSFolderUUID(String XDSFolderUUID) {
        this.XDSFolderUUID = XDSFolderUUID;
    }
    
    public final void setXDSFolderPatientIDUUID(String XDSFolderPatientIDUUID) {
        this.XDSFolderPatientIDUUID = XDSFolderPatientIDUUID;
    }
    
    public final void setXDSFolderUniqueIDUUID(String XDSFolderUniqueIDUUID) {
        this.XDSFolderUniqueIDUUID = XDSFolderUniqueIDUUID;
    }
    
    //metodi set dei campi XDS
    
    public final void setXDSDocumentEntry(String XDSDocumentEntry) {
        this.XDSDocumentEntry = XDSDocumentEntry;
    }
    
    public final void setXDSDocumentEntryHealthCareFaclilityTypeCode(String XDSDocumentEntryHealthCareFaclilityTypeCode) {
        this.XDSDocumentEntryHealthCareFaclilityTypeCode = XDSDocumentEntryHealthCareFaclilityTypeCode;
    }
    
    public final void setXDSDocumentEntryConfidentialityCode(String XDSDocumentEntryConfidentialityCode) {
        this.XDSDocumentEntryConfidentialityCode = XDSDocumentEntryConfidentialityCode;
    }
    
    public final void setXDSDocumentEntryFormatCode(String XDSDocumentEntryFormatCode) {
        this.XDSDocumentEntryFormatCode = XDSDocumentEntryFormatCode;
    }
    
    public final void setXDSDocumentEntryPraticeSettingCode(String XDSDocumentEntryPraticeSettingCode) {
        this.XDSDocumentEntryPraticeSettingCode = XDSDocumentEntryPraticeSettingCode;
    }
    
    public final void setXDSDocumentEntryTypeCode(String XDSDocumentEntryTypeCode) {
        this.XDSDocumentEntryTypeCode = XDSDocumentEntryTypeCode;
    }
    
    public final void setXDSDocumentEntryClassCode(String XDSDocumentEntryClassCode) {
        this.XDSDocumentEntryClassCode = XDSDocumentEntryClassCode;
    }
    
    public final void setXDSDocumentEntryUniqueID(String XDSDocumentEntryUniqueID) {
        this.XDSDocumentEntryUniqueID = XDSDocumentEntryUniqueID;
    }
    
    public final void setXDSDocumentEntryPatientID(String XDSDocumentEntryPatientID) {
        this.XDSDocumentEntryPatientID = XDSDocumentEntryPatientID;
    }
    
    public final void setXDSSubmissionSet(String XDSSubmissionSet) {
        this.XDSSubmissionSet = XDSSubmissionSet;
    }
    
    public final void setXDSSubmissionSetUniqueID(String XDSSubmissionSetUniqueID) {
        this.XDSSubmissionSetUniqueID = XDSSubmissionSetUniqueID;
    }
    
    public final void setXDSSubmissionSetSourceID(String XDSSubmissionSetSourceID) {
        this.XDSSubmissionSetSourceID = XDSSubmissionSetSourceID;
    }
    
    public final void setXDSSubmissionSetPatientID(String XDSSubmissionSetPatientID) {
        this.XDSSubmissionSetPatientID = XDSSubmissionSetPatientID;
    }
    
    public final void setXDSSubmissionSetContentTypeCode(String XDSSubmissionSetContentTypeCode) {
        this.XDSSubmissionSetContentTypeCode = XDSSubmissionSetContentTypeCode;
    }
    
    public final void setXDSFolder(String XDSFolder) {
        this.XDSFolder = XDSFolder;
    }
    
    public final void setXDSFolderPatientID(String XDSFolderPatientID) {
        this.XDSFolderPatientID = XDSFolderPatientID;
    }
    
    public final void setXDSFolderUniqueID(String XDSFolderUniqueID) {
        this.XDSFolderUniqueID = XDSFolderUniqueID;
    }
    
    public final void setXDSFolderComments(String XDSFolderComments) {
        this.XDSFolderComments = XDSFolderComments;
    }
    
    public final void setXDSDocumentEntryAuthorSpecialty(String XDSDocumentEntryAuthorSpecialty) {
        this.XDSDocumentEntryAuthorSpecialty = XDSDocumentEntryAuthorSpecialty;
    }
    
    public final void setXDSDocumentEntryAuthorInstitution(String XDSDocumentEntryAuthorInstitution) {
        this.XDSDocumentEntryAuthorInstitution = XDSDocumentEntryAuthorInstitution;
    }
    
    public final void setXDSDocumentEntryAuthorPerson(String XDSDocumentEntryAuthorPerson) {
        this.XDSDocumentEntryAuthorPerson = XDSDocumentEntryAuthorPerson;
    }
    
    public final void setXDSDocumentEntryAuthorRole(String XDSDocumentEntryAuthorRole) {
        this.XDSDocumentEntryAuthorRole = XDSDocumentEntryAuthorRole;
    }
    
    public final void setXDSDocumentEntryAuthorDisplayName(String XDSDocumentEntryAuthorDisplayName) {
        this.XDSDocumentEntryAuthorDisplayName = XDSDocumentEntryAuthorDisplayName;
    }
    
    public final void setXDSDocumentEntryCreationTime(String XDSDocumentEntryCreationTime) {
        this.XDSDocumentEntryCreationTime = XDSDocumentEntryCreationTime;
    }
    
    public final void setXDSDocumentEntryEventCodeList(String XDSDocumentEntryEventCodeList) {
        this.XDSDocumentEntryEventCodeList = XDSDocumentEntryEventCodeList;
    }
    
    public final void setXDSDocumentEntryEventCodeDisplay(String XDSDocumentEntryEventCodeDisplay) {
        this.XDSDocumentEntryEventCodeDisplay = XDSDocumentEntryEventCodeDisplay;
    }
    
    public final void setXDSDocumentEntryNameList(String XDSDocumentEntryNameList) {
        this.XDSDocumentEntryNameList = XDSDocumentEntryNameList;
    }
    
    public final void setXDSDocumentEntryLanguageCode(String XDSDocumentEntryLanguageCode) {
        this.XDSDocumentEntryLanguageCode = XDSDocumentEntryLanguageCode;
    }
    
    public final void setXDSDocumentEntryLegalAuthenticator(String XDSDocumentEntryLegalAuthenticator) {
        this.XDSDocumentEntryLegalAuthenticator = XDSDocumentEntryLegalAuthenticator;
    }
    
    public final void setXDSDocumentEntryMimeType(String XDSDocumentEntryMimeType) {
        this.XDSDocumentEntryMimeType = XDSDocumentEntryMimeType;
    }
    
    public final void setXDSDocumentEntryParentDocumentRelatioship(String XDSDocumentEntryParentDocumentRelatioship) {
        this.XDSDocumentEntryParentDocumentRelatioship = XDSDocumentEntryParentDocumentRelatioship;
    }
    
    public final void setXDSDocumentEntryParentDocumentID(String XDSDocumentEntryParentDocumentID) {
        this.XDSDocumentEntryParentDocumentID = XDSDocumentEntryParentDocumentID;
    }
    
    public final void setXDSDocumentEntryPraticeSettingCodeDisplayName(String XDSDocumentEntryPraticeSettingCodeDisplayName) {
        this.XDSDocumentEntryPraticeSettingCodeDisplayName = XDSDocumentEntryPraticeSettingCodeDisplayName;
    }
    
    public final void setXDSDocumentEntryTypeCodeDisplayName(String XDSDocumentEntryTypeCodeDisplayName) {
        this.XDSDocumentEntryTypeCodeDisplayName = XDSDocumentEntryTypeCodeDisplayName;
    }
    
    public final void setXDSDocumentEntryServiceStartTime(String XDSDocumentEntryServiceStartTime) {
        this.XDSDocumentEntryServiceStartTime = XDSDocumentEntryServiceStartTime;
    }
    
    public final void setXDSDocumentEntryServiceStopTime(String XDSDocumentEntryServiceStopTime) {
        this.XDSDocumentEntryServiceStopTime = XDSDocumentEntryServiceStopTime;
    }
    
    public final void setXDSDocumentEntrySourcePatientID(String XDSDocumentEntrySourcePatientID) {
        this.XDSDocumentEntrySourcePatientID = XDSDocumentEntrySourcePatientID;
    }
    
    /*
    public final void setXDSDocumentEntrySourcePatientInfo(String XDSDocumentEntrySourcePatientInfo) { //in realt� questo campo ha i sottocampi seguenti per cui l'ho messo solo come promemoria
        this.XDSDocumentEntrySourcePatientInfo = XDSDocumentEntrySourcePatientInfo;
    }
     */
    
    public final void setPatientName(String PatientName) {
        this.PatientName = PatientName;
    }
    
    public final void setPatientId(String PatientId) {
        this.PatientId = PatientId;
    }
    
    public final void setPatientSex(String PatientSex) {
        this.PatientSex = PatientSex;
    }
    
    public final void setPatientAddress(String PatientAddress) {
        this.PatientAddress = PatientAddress;
    }
    
    public final void setPatientBirthDate(String PatientBirthDate) {
        this.PatientBirthDate = PatientBirthDate;
    }
    
    public final void setXDSDocumentEntryTitle(String XDSDocumentEntryTitle) {
        this.XDSDocumentEntryTitle = XDSDocumentEntryTitle;
    }
    
    public final void setXDSDocumentEntryUUID(String XDSDocumentEntryUUID) {
        this.XDSDocumentEntryUUID = XDSDocumentEntryUUID;
    }
    
    public final void setXDSDocumentEntrySize(String XDSDocumentEntrySize) {
        this.XDSDocumentEntrySize = XDSDocumentEntrySize;
    }
    
    public final void setXDSDocumentEntryHash(String XDSDocumentEntryHash) {
        this.XDSDocumentEntryHash = XDSDocumentEntryHash;
    }
    
    public final void setXDSDocumentEntryAvailabilityStatus(String XDSDocumentEntryAvailabilityStatus) {
        this.XDSDocumentEntryAvailabilityStatus = XDSDocumentEntryAvailabilityStatus;
    }
    
    public final void setXDSSubmissionSetAuthorDepartment(String XDSSubmissionSetAuthorDepartment) {
        this.XDSSubmissionSetAuthorDepartment = XDSSubmissionSetAuthorDepartment;
    }
    
    public final void setXDSSubmissionSetAuthorInstitution(String XDSSubmissionSetAuthorInstitution) {
        this.XDSSubmissionSetAuthorInstitution = XDSSubmissionSetAuthorInstitution;
    }
    
    public final void setXDSSubmissionSetAuthorPerson(String XDSSubmissionSetAuthorPerson) {
        this.XDSSubmissionSetAuthorPerson = XDSSubmissionSetAuthorPerson;
    }
    
    public final void setXDSSubmissionSetComments(String XDSSubmissionSetComments) {
        this.XDSSubmissionSetComments = XDSSubmissionSetComments;
    }
    
    public final void setXDSSubmissionSetContentTypeCodeDisplayName(String XDSSubmissionSetContentTypeCodeDisplayName) {
        this.XDSSubmissionSetContentTypeCodeDisplayName = XDSSubmissionSetContentTypeCodeDisplayName;
    }
    
    public final void setXDSSubmissionSetSubmissionTime(String XDSSubmissionSetSubmissionTime) {
        this.XDSSubmissionSetSubmissionTime = XDSSubmissionSetSubmissionTime;
    }
    
    
    //metodi get dei campi UUID
    
    public String getXDSDocumentEntry__UUID(){
        return this.XDSDocumentEntry__UUID;
    }
    
    public String getXDSDocumentEntryHealthCareFaclilityTypeCodeUUID(){
        return this.XDSDocumentEntryHealthCareFaclilityTypeCodeUUID;
    }
    
    public String getXDSDocumentEntryConfidentialityCodeUUID(){
        return this.XDSDocumentEntryConfidentialityCodeUUID;
    }
    
    public String getXDSDocumentEntryFormatCodeUUID(){
        return this.XDSDocumentEntryFormatCodeUUID;
    }
    
    public String getXDSDocumentEntryPraticeSettingCodeUUID(){
        return this.XDSDocumentEntryPraticeSettingCodeUUID;
    }
    
    public String getXDSDocumentEntryTypeCodeUUID(){
        return this.XDSDocumentEntryTypeCodeUUID;
    }
    
    public String getXDSDocumentEntryClassCodeUUID(){
        return this.XDSDocumentEntryClassCodeUUID;
    }
    
    public String getXDSDocumentEntryUniqueIDUUID(){
        return this.XDSDocumentEntryUniqueIDUUID;
    }
    
    public String getXDSDocumentEntryPatientIDUUID(){
        return this.XDSDocumentEntryPatientIDUUID;
    }
    
    public String getXDSSubmissionSetUUID(){
        return this.XDSSubmissionSetUUID;
    }
    
    public String getXDSSubmissionSetUniqueIDUUID(){
        return this.XDSSubmissionSetUniqueIDUUID;
    }
    
    public String getXDSSubmissionSetSourceIDUUID(){
        return this.XDSSubmissionSetSourceIDUUID;
    }
    
    public String getXDSSubmissionSetPatientIDUUID(){
        return this.XDSSubmissionSetPatientIDUUID;
    }
    
    public String getXDSSubmissionSetContentTypeCodeUUID(){
        return this.XDSSubmissionSetContentTypeCodeUUID;
    }
    
    public String getXDSFolderUUID(){
        return this.XDSFolderUUID;
    }
    
    public String getXDSFolderPatientIDUUID(){
        return this.XDSFolderPatientIDUUID;
    }
    
    public String getXDSFolderUniqueIDUUID(){
        return this.XDSFolderUniqueIDUUID;
    }
    
    
    //Metodi get dei campi XDS ------------------------------
    
    public String getXDSDocumentEntry(){
        return this.XDSDocumentEntry;
    }
    
    public String getXDSDocumentEntryHealthCareFaclilityTypeCode(){
        return this.XDSDocumentEntryHealthCareFaclilityTypeCode;
    }
    
    public String getXDSDocumentEntryConfidentialityCode(){
        return this.XDSDocumentEntryConfidentialityCode;
    }
    
    public String getXDSDocumentEntryFormatCode(){
        return this.XDSDocumentEntryFormatCode;
    }
    
    public String getXDSDocumentEntryPraticeSettingCode(){
        return this.XDSDocumentEntryPraticeSettingCode;
    }
    
    public String getXDSDocumentEntryTypeCode(){
        return this.XDSDocumentEntryTypeCode;
    }
    
    public String getXDSDocumentEntryClassCode(){
        return this.XDSDocumentEntryClassCode;
    }
    
    public String getXDSDocumentEntryUniqueID(){
        return this.XDSDocumentEntryUniqueID;
    }
    
    public String getXDSDocumentEntryPatientID(){
        return this.XDSDocumentEntryPatientID;
    }
    
    public String getXDSSubmissionSet(){
        return this.XDSSubmissionSet;
    }
    
    public String getXDSSubmissionSetUniqueID(){
        return this.XDSSubmissionSetUniqueID;
    }
    
    public String getXDSSubmissionSetSourceID(){
        return this.XDSSubmissionSetSourceID;
    }
    
    public String getXDSSubmissionSetPatientID(){
        return this.XDSSubmissionSetPatientID;
    }
    
    public String getXDSSubmissionSetContentTypeCode(){
        return this.XDSSubmissionSetContentTypeCode;
    }
    
    public String getXDSFolder(){
        return this.XDSFolder;
    }
    
    public String getXDSFolderPatientID(){
        return this.XDSFolderPatientID;
    }
    
    public String getXDSFolderUniqueID(){
        return this.XDSFolderUniqueID;
    }
    
    public String getXDSFolderComments(){
        return this.XDSFolderComments;
    }
    
    public String getXDSDocumentEntryAuthorSpecialty(){
        return this.XDSDocumentEntryAuthorSpecialty;
    }
    
    public String getXDSDocumentEntryAuthorInstitution(){
        return this.XDSDocumentEntryAuthorInstitution;
    }
    
    public String getXDSDocumentEntryAuthorPerson(){
        return this.XDSDocumentEntryAuthorPerson;
    }
    
    public String getXDSDocumentEntryAuthorRole(){
        return this.XDSDocumentEntryAuthorRole;
    }
    
    public String getXDSDocumentEntryAuthorDisplayName(){
        return this.XDSDocumentEntryAuthorDisplayName;
    }
    
    public String getXDSDocumentEntryCreationTime(){
        return this.XDSDocumentEntryCreationTime;
    }
    
    public String getXDSDocumentEntryEventCodeList(){
        return this.XDSDocumentEntryEventCodeList;
    }
    
    public String getXDSDocumentEntryEventCodeDisplay(){
        return this.XDSDocumentEntryEventCodeDisplay;
    }
    
    public String getXDSDocumentEntryNameList(){
        return this.XDSDocumentEntryNameList;
    }
    
    public String getXDSDocumentEntryLanguageCode(){
        return this.XDSDocumentEntryLanguageCode;
    }
    
    public String getXDSDocumentEntryLegalAuthenticator(){
        return this.XDSDocumentEntryLegalAuthenticator;
    }
    
    public String getXDSDocumentEntryMimeType(){
        return this.XDSDocumentEntryMimeType;
    }
    
    public String getXDSDocumentEntryParentDocumentRelatioship(){
        return this.XDSDocumentEntryParentDocumentRelatioship;
    }
    
    public String getXDSDocumentEntryParentDocumentID(){
        return this.XDSDocumentEntryParentDocumentID;
    }
    
    public String getXDSDocumentEntryPraticeSettingCodeDisplayName(){
        return this.XDSDocumentEntryPraticeSettingCodeDisplayName;
    }
    
    public String getXDSDocumentEntryTypeCodeDisplayName(){
        return this.XDSDocumentEntryTypeCodeDisplayName;
    }
    
    public String getXDSDocumentEntryServiceStartTime(){
        return this.XDSDocumentEntryServiceStartTime;
    }
    
    public String getXDSDocumentEntryServiceStopTime(){
        return this.XDSDocumentEntryServiceStopTime;
    }
    
    public String getXDSDocumentEntrySourcePatientID(){
        return this.XDSDocumentEntrySourcePatientID;
    }
    /*
    public String getXDSDocumentEntrySourcePatientInfo(){  //il campo SourcePatientInfo non dovrebbe esistere
        return this.XDSDocumentEntrySourcePatientInfo;
    }
     */
    public String getXDSDocumentEntryTitle(){
        return this.XDSDocumentEntryTitle;
    }
    
    public String getXDSDocumentEntryUUID(){
        return this.XDSDocumentEntryUUID;
    }
    
    public String getXDSDocumentEntrySize(){
        return this.XDSDocumentEntrySize;
    }
    
    public String getXDSDocumentEntryHash(){
        return this.XDSDocumentEntryHash;
    }
    
    public String getXDSDocumentEntryAvailabilityStatus(){
        return this.XDSDocumentEntryAvailabilityStatus;
    }
    
    public String getXDSSubmissionSetAuthorDepartment(){
        return this.XDSSubmissionSetAuthorDepartment;
    }
    
    public String getXDSSubmissionSetAuthorInstitution(){
        return this.XDSSubmissionSetAuthorInstitution;
    }
    
    public String getXDSSubmissionSetAuthorPerson(){
        return this.XDSSubmissionSetAuthorPerson;
    }
    
    public String getXDSSubmissionSetComments(){
        return this.XDSSubmissionSetComments;
    }
    
    public String getXDSSubmissionSetContentTypeCodeDisplayName(){
        return this.XDSSubmissionSetContentTypeCodeDisplayName;
    }
    
    public String getXDSSubmissionSetSubmissionTime(){
        return this.XDSSubmissionSetSubmissionTime;
    }
    
    public String getPatientName(){
        return this.PatientName;
    }
    
    public String getPatientId(){
        return this.PatientId;
    }
    
    public String getPatientSex(){
        return this.PatientSex;
    }
    
    public String getPatientAddress(){
        return this.PatientAddress;
    }
    
    public String getPatientBirthDate(){
        return this.PatientBirthDate;
    }
    
    // Methods -------------------------------------------------------
    
    public final void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }
    
    public final void addStudyInstanceUID(String suid) {
        suids.add(suid);
    }
    
    public final void addInstances(String suid) {
        instances.add(suid);
    }
    
    public final String[] listInstances() {
        return (String[]) instances.toArray(new String[instances.size()]);
    }
    
    public final void addSeries(String suid) {
        series.add(suid);
    }
    
    public final String[] listSeries() {
        return (String[]) series.toArray(new String[series.size()]);
    }
    
    
    public final String[] listStudyInstanceUIDs() {
        return (String[]) suids.toArray(new String[suids.size()]);
    }
    
    public final void addSOPClassUID(String cuid) {
        cuids.add(cuid);
    }
    
    public final void clearSOPClassUIDs() {
        cuids.clear();
    }
    
    public final String[] listSOPClassUIDs() {
        return (String[]) cuids.toArray(new String[cuids.size()]);
    }
    
    public final void setUser(User user) {
        this.user = user;
    }
    
    public final void setNumberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }
    
    public final void incNumberOfInstances(int inc) {
        this.numberOfInstances += inc;
    }
    
    public final int getNumberOfInstances() {
        return this.numberOfInstances;
    }
    public final void setMPPSInstanceUID(String mppsUID) {
        this.mppsUID = mppsUID;
    }
    
    public final String getMPPSInstanceUID() {
        return mppsUID;
        
        
    }
    public String getAccessionNumber(){
        return accessionNumber;
    }
    public Patient getPatient(){
        return patient;
    }
    
    public String getStudyDate(){
        return studyDate;
        
    }
    public void setStudyDate(String d)
    {
        studyDate=d;
    }
    public String getStudyTime(){
        return studyTime;
        
    }
    public void setStudyTime(String d){
        studyTime=d;
    }
    
    public void setAEtitle(String s){
        AEtitle=s;
    }
    public String getAEtitle(){
        return AEtitle;
    }
    
    
    
}
