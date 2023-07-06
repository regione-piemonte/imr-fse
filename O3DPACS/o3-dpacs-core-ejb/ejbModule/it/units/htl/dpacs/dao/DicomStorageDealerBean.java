/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.atna.AuditLogService;
import it.units.htl.dpacs.exceptions.IncorrectPatientIdException;
import it.units.htl.dpacs.exceptions.MultiplePatientsIdentifiedException;
import it.units.htl.dpacs.helpers.AutoReconciliation;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalSettings.PartitioningStrategy;
import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.helpers.IheSettings;
import it.units.htl.dpacs.helpers.PatientIdCheckSettings;
import it.units.htl.dpacs.helpers.RoiMeasure;
import it.units.htl.dpacs.helpers.StudyTrackingSettings;
import it.units.htl.dpacs.postprocessing.UidGenerator;
import it.units.htl.dpacs.servers.storage.StorageSCP;
import it.units.htl.dpacs.servers.storage.StorageServer;
import it.units.htl.dpacs.valueObjects.CodeSequence;
import it.units.htl.dpacs.valueObjects.DicomConstants;
import it.units.htl.dpacs.valueObjects.Image;
import it.units.htl.dpacs.valueObjects.Instance;
import it.units.htl.dpacs.valueObjects.KeyObject;
import it.units.htl.dpacs.valueObjects.NonImage;
import it.units.htl.dpacs.valueObjects.Overlay;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.dpacs.valueObjects.PersonalName;
import it.units.htl.dpacs.valueObjects.PresState;
import it.units.htl.dpacs.valueObjects.RefToInstances;
import it.units.htl.dpacs.valueObjects.Series;
import it.units.htl.dpacs.valueObjects.StructRep;
import it.units.htl.dpacs.valueObjects.Study;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che2.audit.message.AuditEvent.ActionCode;
import org.dcm4che2.audit.message.PatientRecordMessage;

@Stateless
public class DicomStorageDealerBean implements DicomStorageDealerLocal {
    private Log log = LogFactory.getLog(DicomStorageDealerBean.class);
    private Log dupConLog = LogFactory.getLog("DUPLICATE_STORAGE_CONNECTIONS");
    private StoragePerformer sp = null;
    private @Resource(name = "java:/jdbc/storageDS")
    DataSource dataSource;
    // Note: queries used in Statement are specified right inside the methods
    private static final String updatePhMedia = "UPDATE PhysicalMedia SET filledBytes=filledBytes+? WHERE pk=?";
    private static final String rockPhMedia = "UPDATE PhysicalMedia SET filledBytes=filledBytes+? WHERE pk=?";
    private static final String insertStudyLoc = "INSERT INTO StudyLocations(studyFK, physicalMediaFK, insertionDate) VALUES(?, ?, ?)";
    private static final String updateStudyLoc = "UPDATE StudyLocations SET physicalMediaFK=?, insertionDate=? WHERE studyFK=? AND physicalMediaFK=?";
    private static final String updateStudySize = "UPDATE Studies SET studySize=studySize+? WHERE studyInstanceUID=?";
    private static final String updateStudyFastAndSize = "UPDATE Studies SET studySize=studySize+?, fastestAccess=? WHERE studyInstanceUID=?";
    private static final String updateSeries = "UPDATE Series SET seriesStatus=?, numberOfSeriesRelatedInstances=numberOfSeriesRelatedInstances+? WHERE seriesInstanceUID=?";
    private static final String insertIi = "INSERT INTO Images(sopInstanceUID, sopClassUID, instanceNumber, seriesFK, deprecated, samplesPerPixel, rowsnum, columnsnum, bitsAllocated, bitsStored, highBit, pixelRepresentation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String insertNii = "INSERT INTO NonImages(sopInstanceUID, sopClassUID, instanceNumber, seriesFK, deprecated) VALUES (?, ?, ?, ?, ?)";
    private static final String insertOi = "INSERT INTO Overlays(sopInstanceUID, sopClassUID, instanceNumber, seriesFK, deprecated, overlayNumber, overlayRows, overlayColumns, overlayType, overlayBitsAllocated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String insertO2I = "INSERT INTO OverlaysToImages(overlayFK, imageFK, imageSOPClassUID) VALUES (?, ?, ?)";
    private static final String insertPsi = "INSERT INTO PresStates(sopInstanceUID, sopClassUID, instanceNumber, seriesFK, deprecated, presentationLabel, presentationDescription, presentationCreationDate, presentationCreationTime, presentationCreatorsName, recommendedViewingMode) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String insertPs2I = "INSERT INTO PresStatesToImages(presStateFK, imageFK, imageSOPClassUID) VALUES (?, ?, ?)";
    private static final String insertSri = "INSERT INTO StructReps(sopInstanceUID, sopClassUID, instanceNumber, seriesFK, deprecated, completionFlag, verificationFlag, contentDate, contentTime, observationDateTime, ConceptNameCodeSequence) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
    private static final String insertKoi = "INSERT INTO KeyObjects(sopInstanceUID, sopClassUID, instanceNumber, seriesFK, contentDate, contentTime, CodeSequencesFK, deprecated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String insertKo2i = "INSERT INTO KeyObjectReferences(keyObjectsFk, RefSeriesInstanceUID, RefSOPInstanceUID, RefSOPClassUID) VALUES (?, ?, ?, ?)";
    private static final String updateStudy = "UPDATE Studies SET studyStatusID=?, studyDate=?, studyTime=?, studyCompletionDate=?, studyCompletionTime=?, studyVerifiedDate=?, studyVerifiedTime=?, admittingDiagnosesDescription=?, studyStatus=?, "
            + /* studySize=studySize+?, accessionNumber=?, */"studyID=?, studyDescription=?, procedureCodeSequenceFK=? WHERE studyInstanceUID=?";
    private static final String updateStudyNotCodeSeq = "UPDATE Studies SET studyStatusID=?, studyDate=?, studyTime=?, studyCompletionDate=?, studyCompletionTime=?, studyVerifiedDate=?, studyVerifiedTime=?, admittingDiagnosesDescription=?, studyStatus=?, "
            + "studyID=?, studyDescription=?  WHERE studyInstanceUID=?";
    private static final String updateStudyRelatedInstances = "UPDATE Studies SET numberOfStudyRelatedInstances = numberOfStudyRelatedInstances + ? WHERE studyInstanceUID = ?";
    private static final String updateStudyRelatedSeries = "UPDATE Studies SET numberOfStudyRelatedSeries = numberOfStudyRelatedSeries + 1 WHERE studyInstanceUID = ?";
    private static final String insertNoprs = "INSERT INTO PhysiciansToStudies(nameOfPhysiciansReadingStudyFK, studyFK) VALUES(?, ?)";
    private static final String selectInsertedCodSeq = "SELECT max(pk) FROM CodeSequences WHERE (codeValue=? OR codeValue IS NULL) AND (codingSchemeDesignator=? OR codingSchemeDesignator IS NULL) AND (codingSchemeVersion=? OR codingSchemeVersion IS NULL)";
    private static final String insertCodSeq = "INSERT INTO CodeSequences(codeValue, codingSchemeDesignator, codingSchemeVersion, codeMeaning) VALUES (?, ?, ?, ?)";
    private static final String visitPres = "SELECT pk FROM WLPatientDataPerVisit WHERE studyFK=? AND patientFK=?";
    private static final String startupVisit = "INSERT INTO WLPatientDataPerVisit(patientState, patientClass, assignedPatientLocation, visitNumber, pregnancyStatus, medicalAlerts, patientWeight, confidentialityConstOnPatData , specialNeeds, patientFK, studyFK) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
    private static final String updVisit = "UPDATE WLPatientDataPerVisit SET patientState=?, pregnancyStatus=?, medicalAlerts=?, patientWeight=?, confidentialityConstOnPatData=?, specialNeeds=?, assignedPatientLocation=? WHERE studyFK=? AND patientFK=?";
    private static final String insertSeries = "INSERT INTO Series(seriesInstanceUID, studyFK, equipmentFK, seriesNumber, modality, bodyPartExamined,seriesDescription, knownNodeFK,OperatorsName) VALUES (?, ?, ?, ?, ?, ?,?, (SELECT pk FROM KnownNodes WHERE aeTitle=?),?)";
    private static final String indirectEquipment = "SELECT max(pk) FROM Equipment WHERE (UPPER(manufacturer)=UPPER(?) OR manufacturer IS NULL) AND (UPPER(institutionName)=UPPER(?) OR institutionName IS NULL) AND (UPPER(stationName)=UPPER(?) OR stationName IS NULL) AND (UPPER(institutionalDepartmentName)=UPPER(?) OR institutionalDepartmentName IS NULL) AND (UPPER(manufacturersModelName)=UPPER(?) OR manufacturersModelName IS NULL) AND (UPPER(deviceSerialNumber)=UPPER(?) OR deviceSerialNumber IS NULL) AND (dateOfLastCalibration=? OR dateOfLastCalibration IS NULL) AND (timeOfLastCalibration=? OR timeOfLastCalibration IS NULL) AND (UPPER(conversionType)=UPPER(?) OR conversionType IS NULL) AND (UPPER(secondaryCaptureDeviceID)=UPPER(?) OR secondaryCaptureDeviceID IS NULL)";
    private static final String insertIndEquipment = "INSERT INTO Equipment(manufacturer, institutionName, stationName, institutionalDepartmentName, manufacturersModelName, deviceSerialNumber, dateOfLastCalibration, timeOfLastCalibration, conversionType, secondaryCaptureDeviceID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String reOpenStudy = "UPDATE Studies SET StudyStatus='o' where StudyInstanceUID=?";
    private static final String INSERT_IMAGE_NUMOFFRAMES = "INSERT INTO ImageNumberOfFrames(sopInstanceUid, numberOfFrames) VALUES(?,?)";
    // private static final String FAKE = "Fake"; // To store fake HL7 studies, updated when Dicom instances arrive!
    // private static final String PATIENT_PRESENT = "p";
    // private static final String PATIENT_NOT_PRESENT = "n";
    private ActiveAssociation _assoc = null;
    private String callingAE = null;
    private static final byte IHE_MARK = 1;
    private static final byte IHE_DONOTMARK = 0;
    private boolean acceptDifferentAccNum;
    private boolean ihe;
    private static final String OLD_ID_ISSUER = "NONE";
    private static final String DEFAULT_CHARSET = "ISO_IR 6";

    // private static final String PATIENT_MAYBE_PRESENT = "m";
    public DicomStorageDealerBean() {
    }

    @PostConstruct
    public void init() {
        try {
            ihe = IheSettings.isIhe();
            sp = new StoragePerformer();
            acceptDifferentAccNum = ("true".equalsIgnoreCase(GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.CONFIG_ACCEPT_DIFFERENT_ACCNUM)));
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    public void setSessionContext(SessionContext sc) {
    }

    public File storeToMedia(Dataset data, Study study, String url, DcmParser parser, DcmEncodeParam decParam, StorageSCP scp, String forcedTs, boolean isImage, ActiveAssociation assoc,
            Vector<RoiMeasure> total, String prefStorageFK) throws StorageException {
        if (data == null)
            throw new StorageException(StorageException.NO_DATA_PROVIDED);
        if (url == null)
            throw new StorageException(StorageException.NO_MEDIA_AVAILABLE);
        _assoc = assoc;
        callingAE = assoc.getAssociation().getCallingAET();
        String stUid = data.getString(Tags.StudyInstanceUID);
        log.debug(callingAE + ": dealing " + stUid + " should write a file here: \n " + url);
        short tracker = 0;
        boolean studyPresent = false;
        boolean spaceOnMedia = false;
        File file = null;
        long bytes = 2L * 1024L + Long.parseLong(study.getStudySize());
        try {
            // long storedBytes = 0L; // The amount of bytes already stored for
            // this study: used if I have to move a study!
            spaceOnMedia = isSpaceOnPartition(/* url, */bytes, callingAE, prefStorageFK);
            // TODO
            // Beware!! Are there troubles if two threads both see no
            // space and try to deal with the situation?
            // Not in dealing files,there may be in dealing with DB!!!
            if (spaceOnMedia == false) {
                log.fatal(":::::::::::::NO MORE SPACE ON " + url + "::::::::::::");
                throw new StorageException(StorageException.NO_MEDIA_AVAILABLE);
                // transaction rollback to be implemented
            }
            studyPresent = isStudyPresent(stUid);
            copyMediaPolicyIntoStudy(stUid, prefStorageFK);
        } catch (SQLException ex) {
            String errorMsg = "SQLException occured while collecting information for storage";
            log.error(errorMsg, ex);
            throw new StorageException(errorMsg);
        }
        if (sp == null)
            sp = new StoragePerformer();
        try {
            if (spaceOnMedia) {
                log.debug(callingAE + ": dealing " + stUid + "StorageDealer writing on media");
                log.info(callingAE + ": writing here: " + url);
                file = sp.writeDataToDisk(data, parser, decParam, url, scp, forcedTs, isImage, callingAE, total);
                writeHashtoDB(data, sp.getHash());
                bytes = file.length();
                updateDB(stUid, bytes, studyPresent ? 0 : -1, 0, 0, url, callingAE, null);
                // 1st 0->Insert a new row in StudyLocations
                tracker++;
                updateStudies(stUid, bytes, studyPresent ? null : url, callingAE);
                log.debug("dealing " + stUid + "StorageDealer updated DB");
            }
        } catch (DcmServiceException ex) {
            // DcmServiceException thrown by writeDataToDisk
            log.error(callingAE + ": Unable to write to disk", ex);
            throw new StorageException(callingAE + ": Unable to write to disk");
        } catch (SQLException ex) {
            String errorMessage;
            if (tracker == 0) {
                errorMessage = callingAE + ": Unable to update DB";
            } else if (tracker == 1) {
                errorMessage = callingAE + ": Unable to update fastestAccess";
            } else {
                errorMessage = callingAE + ": An exception occurred - ";
            }
            log.error(errorMessage, ex);
            throw new StorageException(errorMessage);
        }
        // If I'm here, I can track the study availability if (!studyPresent)
        if ((!studyPresent) && (StudyTrackingSettings.isStudyCompletionTrackingEnabled())) {
            int added = addStudyTracking(stUid, ConfigurationSettings.STUDYCLOSE_STORAGE);
            if (added == 0)
                log.error("Could not add study tracking for study " + stUid);
        }
        if (!studyPresent) {
            String characterSet = data.getString(Tags.SpecificCharacterSet);
            if (characterSet != null && (!"".equals(characterSet)) && (!DEFAULT_CHARSET.equals(characterSet))) {
                addSpecificCharSet(stUid, characterSet);
            }
        }
        return file;
    }

    // Return the url where the study should be written to if there's enough space!
    public String verifyToStore(Patient p, Study s, Series se, Instance i, String[] params, StorageSCP scp, ActiveAssociation assoc, String defaultIdIssuer, PartitioningStrategy strategy) throws CannotStoreException {
        _assoc = assoc;
        callingAE = _assoc.getAssociation().getCallingAET();
        ihe = IheSettings.isIhe();
        String url = null;
        if (p == null)
            throw new CannotStoreException(CannotStoreException.NULL_PATIENT);
        if (i == null)
            throw new CannotStoreException(CannotStoreException.NULL_INSTANCES);
        if (s == null)
            throw new CannotStoreException(CannotStoreException.NULL_STUDY);
        if (se == null)
            throw new CannotStoreException(CannotStoreException.NULL_SERIES);
        if (se.getAETitle() == null)
            throw new CannotStoreException(CannotStoreException.NULL_SERIES);
        if (p.getPrimaryKey() == null) {
            // String[] res = null;
            String[] ids = null;
            try {
                if (AutoReconciliation.isEnabled() && ihe) {
                    ids = getStudyIds(s.getStudyInstanceUid(), p.getPatientId());
                    if (ids[0] == null) {
                        // s.setAccessionNumber(null);
                    } else {
                        p.setPatientId(ids[4]);
                        p.setIdIssuer(ids[5]);
                        p.setPrimaryKey(ids[2]);
                        s.setAccessionNumber(ids[1]);
                    }
                } else {
                    ids = getStudyIds(s.getStudyInstanceUid());
                }
                // 0: studyInstanceUID
                // 1: accessionNumber
                // 2: patient PK
                // 3: patientDemo PK
                // 4: patientID
                // 5: idIssuer
                // 6: mergedByPatientId
                // res = identifyPatient(p, s, callingAE);
            } catch (SQLException sex) {
                log.warn(callingAE + ": Couldn't identify study", sex);
                throw new CannotStoreException("Couldn't verify if study is already present.");
            }
            if (ids[0] == null) { // The study is not present
                // NOW I HAVE TO ADD THE STUDY
                Long patPk = null;
                if (ihe) {
                    try {
                        patPk = identifyPatient(p, callingAE, s.getStudyInstanceUid(), s.getAccessionNumber(), scp.getDefaultIdIssuer(), true, assoc.getAssociation().getCalledAET(), strategy);
                        if (patPk == 0) {
                            patPk = storeNewPatient(p);
                        } else if (patPk < 0) {
                            log.error("More than one patient identified - patientId=" + p.getPatientId());
                            throw new MultiplePatientsIdentifiedException(p.getPatientId());
                        }
                        p.setPrimaryKey("" + patPk);
                    } catch (SQLException sex) {
                        log.error(CannotStoreException.NO_PATIENT, sex);
                        throw new CannotStoreException(CannotStoreException.NO_PATIENT);
                    } catch (IncorrectPatientIdException e) {
                        throw e;
                    }
                    startupStudy(s, patPk, callingAE, IHE_MARK);
                    startUpVisit(p, s);
                } else {
                    try {
                        patPk = identifyPatient(p, callingAE, s.getStudyInstanceUid(), s.getAccessionNumber(), scp.getDefaultIdIssuer(), false, assoc.getAssociation().getCalledAET(), strategy);
                        if (patPk == 0) {
                            patPk = storeNewPatient(p); // This doesn't return null, rather throws a SQLException
                        } else if (patPk < 0) {
                            log.error("More than one patient identified - patientId=" + p.getPatientId());
                            throw new MultiplePatientsIdentifiedException(p.getPatientId());
                        }
                        p.setPrimaryKey("" + patPk); // a null patPk would mean that storeNewPatient threw an Exception
                    } catch (SQLException sex) {
                        log.error(CannotStoreException.NO_PATIENT + ", Patient ID = " + p.getPatientId(), sex);
                        throw new CannotStoreException(CannotStoreException.NO_PATIENT);
                    } catch (IncorrectPatientIdException e) {
                        throw e;
                    }
                    startupStudy(s, patPk, callingAE, IHE_DONOTMARK);
                    startUpVisit(p, s);
                }
            } else {
                // The study is present (either scheduled or this is the second instance of this study)
                if (((ids[1] != null) && (ids[1].equals(s.getAccessionNumber()) || acceptDifferentAccNum) || ((ids[1] == null) && ((ihe) || (s.getAccessionNumber() == null) || acceptDifferentAccNum)))
                        &&
                        (((p.getPatientId().equalsIgnoreCase(ids[4])) &&
                                ((ids[5] == null) || (p.getPatientId() == null) || (ids[5].equalsIgnoreCase(p.getPatientId())) || (ids[5].equals(OLD_ID_ISSUER)) || (defaultIdIssuer.equals(p.getIdIssuer())) || (ids[5].equals(p.getIdIssuer()))))
                                || (p.getPatientId().equalsIgnoreCase(ids[6]))
                                || (ids[4].equalsIgnoreCase(s.getStudyInstanceUid())) /* This was added to store studies with patientId=NULL, which are set to studyInstanceUID */
                        )) {
                    // STORE: everything OK. Update Study info and store series+instances as usual
                    p.setPrimaryKey(ids[2]);
                    // If I'm here I shouldn't do anything yet...
                } else {
                    log.error("CANNOTADDSTUDY: studyInstanceUID DB: " + ids[0]);
                    log.error("CANNOTADDSTUDY: accessionNumber DB: " + ((ids[1] == null) ? "NULL" : ids[1]) + " accessionNumber Dicom: " + ((s.getAccessionNumber() == null) ? "NULL" : s.getAccessionNumber()));
                    log.error("CANNOTADDSTUDY: patientId DB: " + ((ids[4] == null) ? "NULL" : ids[4]) + " patientId Dicom: " + ((p.getPatientId() == null) ? "NULL" : p.getPatientId()));
                    throw new CannotStoreException(CannotStoreException.CANNOT_ADD_STUDY);
                }
            }
        }
        // Once I'm here, I must already have the study on DB: make sure it was added before!!!
        if ((url = fetchStudyFastestAccess(s, se.getAETitle(), Long.parseLong(p.getPrimaryKey()), params[1], callingAE)) == null) {
            throw new CannotStoreException(CannotStoreException.CANNOT_ADD_TO_STUDY);
        }
        if (!isVisitPresent(p, s)) { // Davide 20110320: This should be useless, now: the visit is created before in any case
            startUpVisit(p, s);
        }
        // The visit is present->UPDATE
        if (s.getToPerform() != DicomConstants.UPDATE) {
            updateVisit(p, s);
            try {
                verifyPatientStudyConsistency(s.getStudyInstanceUid(), Long.parseLong(p.getPrimaryKey()));
            } catch (CannotStoreException ex) {
                throw ex;
            }
        }
        // This throws a CannotStoreException if the study already belongs to
        // another patient!
        /* Check whether series is present, if it ain't insert it! */
        if (se.getToPerform() != DicomConstants.UPDATE)
            try {
                if (!isSeriesPresent(se, s.getStudyInstanceUid())) {
                    log.info(callingAE + ": Series not present! " + se.getSeriesInstanceUid());
                    startupSeries(se, s.getStudyInstanceUid(), params[2], callingAE);
                } else {
                    log.debug(callingAE + ": Skipped stuff about Series");
                }
            } catch (Exception e) {
                throw new CannotStoreException("Problem with the series" + e.getMessage());
            }
        if (s.getToPerform() != DicomConstants.UPDATE) {
            if (isStudyDeprecated(s.getStudyInstanceUid()))
                throw new CannotStoreException(CannotStoreException.CANC_DISC_STUDY);
        } else
            log.debug(callingAE + ": Study " + s.getStudyInstanceUid() + " should be stored on " + url);
        return url;
    }

    /**
     * Insert instance. Insert an instance image into the database
     * 
     * @param patient
     *            the patient object related to the instance
     * @param study
     *            the study object related to the instance
     * @param series
     *            the series object related to the instance
     * @param instance
     *            the instance to insert to the database
     * @return the number of stored instance.
     * @throws CannotUpdateException
     *             the cannot update exception
     */
    public int insertInstance(Patient patient, Study study, Series series, Instance instance, ActiveAssociation assoc) throws CannotUpdateException {
        _assoc = assoc;
        callingAE = _assoc.getAssociation().getCallingAET();
        int tot = 0; // To track what's been inserted
        log.debug(callingAE + ": StorageDealer: About to insert an instance");
        if ((patient == null) || (study == null) || (series == null) || (instance == null))
            throw new CannotUpdateException(CannotUpdateException.NULL_ARGS);
        if (patient.getPrimaryKey() == null)
            throw new CannotUpdateException(CannotUpdateException.PATIENT_KEY_NOT_SET);
        if (instance instanceof Image)
            tot += storeImageInstance((Image) instance, series.getSeriesInstanceUid(), callingAE);
        else if (instance instanceof PresState)
            tot += storePresStateInstance((PresState) instance, series.getSeriesInstanceUid(), callingAE);
        else if (instance instanceof StructRep)
            tot += storeStructRepInstance((StructRep) instance, series.getSeriesInstanceUid(), callingAE);
        else if (instance instanceof Overlay)
            tot += storeOverlayInstance((Overlay) instance, series.getSeriesInstanceUid(), callingAE);
        else if (instance instanceof NonImage)
            tot += storeNonImageInstance((NonImage) instance, series.getSeriesInstanceUid(), callingAE);
        else if (instance instanceof KeyObject)
        	tot += storeKeyObjectInstance((KeyObject) instance, series.getSeriesInstanceUid(), callingAE);
        else
            throw new CannotUpdateException(CannotUpdateException.SOP_CL_NOT_SUPPORTED);
        try {
            if (storeSeries(series, tot, study.getStudyInstanceUid(), callingAE) == null) {
                throw new CannotUpdateException(CannotUpdateException.SERIES_PROBLEMS);
            }
        } catch (SQLException e) {
            log.error(callingAE, e);
            throw new CannotUpdateException(CannotUpdateException.SERIES_PROBLEMS);
        }
        // Deal with Study:
        if (completeStudyStorage(study, patient.getPrimaryKey(), callingAE) == null)
            throw new CannotUpdateException(CannotUpdateException.STUDY_PROBLEMS); // AAAHHH
        return tot;
    }

    /**
     * @return 1 if the instance is already stored, -1 if the image is being processed, 0 if the image is not stored
     */
    public int isInstancePresent(String u, String s, String table, ActiveAssociation assoc) {
        // It returns true if at least one instance is already present in the DB
        _assoc = assoc;
        callingAE = _assoc.getAssociation().getCallingAET();
        Connection con = null;
        CallableStatement cs = null;
        int status = 1;
        // Pretend they're all present: the client shouldn't be able to proceed!
        if ((u == null) || (s == null)) {
            return status;
        }
        if (table != null) {
            try {
                con = getDBConnection();
                cs = con.prepareCall("{call isInstancePresent(?,?,?)}");
                cs.setString(1, u);
                cs.setString(2, table);
                cs.registerOutParameter(3, Types.INTEGER);
                cs.execute();
                status = cs.getInt(3);
                switch (status) {
                case 1:
                    log.info(callingAE + ": duplicated istance: " + u);
                    break;
                case 0:
                    log.debug(callingAE + ": sopInstance: " + u + " does not exist");
                    break;
                case -1:
                    dupConLog.info(callingAE); // Log that the instance is already being processed
                    break;
                }
            } catch (SQLException sex) {
                log.error(callingAE + " isInstancePresent: ", sex);
            } catch (Exception ex) {
                log.error(callingAE + " isInstancePresent: ", ex);
            } finally {
                CloseableUtils.close(cs);
                CloseableUtils.close(con);
            }
        }
        return status;
    }

    public boolean insertStudyVerificationData(String sourceAeTitle, String studyInstanceUid) {
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call insertStudyToVerify(?,?,?,?,?)}");
            // create a unique xdsMessageId
            UidGenerator uidGenerator = new UidGenerator();
            String xdsMessageId = uidGenerator.getNewInstanceUid();
            cs.setString(1, studyInstanceUid);
            cs.setString(2, sourceAeTitle);
            cs.setTimestamp(3, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            cs.setString(4, xdsMessageId);
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute();
            return true;
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return false;
    }

    // PRIVATE METHODS
    private Connection getDBConnection() throws SQLException {
        Connection dbConn;
        try {
            dbConn = dataSource.getConnection();
            return dbConn;
        } catch (SQLException sex) {
            log.error("Unable to get DB Connection.", sex);
            throw sex;
        }
    }

    private boolean isSpaceOnPartition(/* String part, */long size, String callingAE, String prefStorageFK) throws SQLException {
        Connection con = getDBConnection();
        Statement stat = null;
        ResultSet rs = null;
        String newPart = getRelatedUrl(prefStorageFK, callingAE);
        boolean b = false;
        try {
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT capacityInBytes, filledBytes FROM PhysicalMedia WHERE urlToStudy='" + newPart + "'");
            String dim = (size / 1024 / 1024 == 0) ? size / 1024 + "Kb" : size / 1024 / 1024 + " Mb";
            log.info(callingAE + ": SIZE TO STORE: " + dim);
            if (rs.next()) {
                long capacity = rs.getLong(1);
                if (rs.wasNull()) {
                    // null value means unbounded PhysicalMedia
                    return true;
                }
                b = (capacity > (rs.getLong(2) + size));
            }
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
        return b;
    } // end isSpaceOnPartition

    private boolean isStudyPresent(String suid) throws SQLException {
        Connection con = getDBConnection();
        Statement stat = null;
        ResultSet rs = null;
        boolean ans = false;
        try {
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT fastestAccess FROM Studies WHERE studyInstanceUID='" + suid + "'");
            if (rs.next()) {
                rs.getString(1);
                ans = !rs.wasNull();
            }
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
        return ans;
    } // end isStudyPresent

    private int updateDB(String u, long b, long oldB, long fk, long d, String ur, String callingAE, String param) throws SQLException { // u=studyUID,
        // b=#written bytes, oldB: the bytes of this study already on media; if
        // -1 you don't have to update StudyLocations!
        Connection con = null;
        Statement stat = null;
        PreparedStatement updatePhMediaPS = null;
        PreparedStatement rockPhMediaPS = null;
        Statement stExistStudyLoc = null;
        PreparedStatement updateStudyLocPS = null;
        PreparedStatement insertStudyLocPS = null;
        ResultSet rs = null;
        ResultSet rsExistStudyLoc = null;
        int r = 0;
        try {
            con = getDBConnection();
            String newPart = null;
            stat = con.createStatement();
            if (param != null) {
                newPart = getRelatedUrl(param, callingAE);
            } else {
                newPart = ur;
            }
            rs = stat.executeQuery("SELECT pk FROM PhysicalMedia WHERE urlToStudy='" + newPart + "'");
            int pk = -1;
            if (rs.next()) {
                pk = rs.getInt(1);
            }
            if (oldB <= 0) {
                updatePhMediaPS = con.prepareStatement(updatePhMedia);
                updatePhMediaPS.setLong(1, b);
                updatePhMediaPS.setInt(2, pk);
                r = updatePhMediaPS.executeUpdate();
            } else {
                rockPhMediaPS = con.prepareStatement(rockPhMedia);
                if (d == 1) { // If the study was actually deleted!
                    rockPhMediaPS.setLong(1, -oldB); // I have to subtract!!!
                    rockPhMediaPS.setLong(2, fk); // The old location
                    r = rockPhMediaPS.executeUpdate();
                } // end if
                rockPhMediaPS.setLong(1, oldB); // I have to add!!!
                rockPhMediaPS.setInt(2, pk); // The new location
                r = rockPhMediaPS.executeUpdate();
            }
            // Now I have to update StudyLocations
            if (oldB != -1L) {
                String qryExtistStudyLoc = "SELECT COUNT(1) as how FROM StudyLocations WHERE studyFK='" + u + "' and PhysicalMediaFK='" + pk + "'";
                stExistStudyLoc = con.createStatement();
                rsExistStudyLoc = stExistStudyLoc.executeQuery(qryExtistStudyLoc);
                boolean exist = false;
                if (rsExistStudyLoc.next()) {
                    exist = true;
                }
                if (oldB == 0 && !exist) {
                    insertStudyLocPS = con.prepareStatement(insertStudyLoc);
                    insertStudyLocPS.setString(1, u);
                    insertStudyLocPS.setInt(2, pk);
                    insertStudyLocPS.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    // log.info(callingAE+": ---"+insertStudyLocPS);
                    r += (insertStudyLocPS.executeUpdate());
                } else { // oldB>0
                    updateStudyLocPS = con.prepareStatement(updateStudyLoc);
                    updateStudyLocPS.setInt(1, pk); // The new physicalMedia
                    updateStudyLocPS.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    updateStudyLocPS.setString(3, u);
                    updateStudyLocPS.setLong(4, fk); // The old physicalMedia
                    r += (updateStudyLocPS.executeUpdate());
                } // end if...else
            }
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(rsExistStudyLoc);
            CloseableUtils.close(insertStudyLocPS);
            CloseableUtils.close(updateStudyLocPS);
            CloseableUtils.close(stExistStudyLoc);
            CloseableUtils.close(rockPhMediaPS);
            CloseableUtils.close(updatePhMediaPS);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
        return r;
    } // end updateDB

    private int updateStudies(String suid, long bs, String fast, String callingAE) throws SQLException {
        Connection con = getDBConnection();
        PreparedStatement updateStudyFastAndSizePS = null;
        PreparedStatement updateStudySizePS = null;
        int r = 0;
        try {
            if (fast != null) {
                updateStudyFastAndSizePS = con.prepareStatement(updateStudyFastAndSize);
                updateStudyFastAndSizePS.setLong(1, bs);
                updateStudyFastAndSizePS.setString(2, fast);
                updateStudyFastAndSizePS.setString(3, suid);
                r = updateStudyFastAndSizePS.executeUpdate();
            } else {
                updateStudySizePS = con.prepareStatement(updateStudySize);
                updateStudySizePS.setLong(1, bs);
                updateStudySizePS.setString(2, suid);
                r = updateStudySizePS.executeUpdate();
            }
        } finally {
            CloseableUtils.close(updateStudySizePS);
            CloseableUtils.close(updateStudyFastAndSizePS);
            CloseableUtils.close(con);
        }
        return r;
    } // end updateStudies

    /**
     * @param studyUid
     * @return 0: studyInstanceUID 1: accessionNumber 2: patient PK 3: patientDemo PK 4: patientID 5: mergedByPatientId
     */
    private String[/* 7 */] getStudyIds(String studyUid) throws SQLException {
        String selectQuery = "SELECT St.studyInstanceUID, St.accessionNumber, P.pk, PD.pk, P.patientID, P.idIssuer, St.mergedByPatientId " +
                " FROM Studies St " +
                " LEFT JOIN Patients P ON P.pk=St.patientFK" +
                " LEFT JOIN PatientDemographics PD ON PD.patientFK=P.pk" +
                " WHERE St.studyInstanceUID=?";
        String[] ret = new String[] { null, null, null, null, null, null, null };
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getDBConnection();
            ps = con.prepareStatement(selectQuery);
            ps.setString(1, studyUid);
            rs = ps.executeQuery();
            if (rs.next()) {
                for (int i = ret.length - 1; i >= 0; i--) {
                    ret[i] = rs.getString(i + 1); // pks are read as string: if the value was null, null is returned
                }
            } else {
                log.warn("No record present for study: " + studyUid);
            }
        } catch (SQLException sex) {
            log.error("Exception retrieving study IDs", sex);
            throw sex;
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return ret;
    }

    /**
     * @param studyUid
     * @return 0: studyInstanceUID 1: accessionNumber 2: patient PK 3: patientDemo PK 4: patientID 5: mergedByPatientId
     */
    private String[] getStudyIds(String studyUid, String patientId) throws SQLException {
        // check if
        String checkStudyUID = "SELECT studyInstanceUID, pt.patientId FROM Studies st " +
                "INNER JOIN Patients pt on pt.pk = st.patientFk" +
                " WHERE accessionNumber = ?";
        String[] ret = new String[] { null, null, null, null, null, null, null };
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getDBConnection();
            ps = con.prepareStatement(checkStudyUID);
            ps.setString(1, patientId);
            rs = ps.executeQuery();
            if (rs.next()) {
                String scheduledStudyUID = rs.getString(1);
                if (!scheduledStudyUID.equals(studyUid)) {
                    String updateStudyUID = "UPDATE Studies SET studyInstanceUID = ? WHERE accessionNumber = ?";
                    ps = con.prepareStatement(updateStudyUID);
                    ps.setString(1, studyUid);
                    ps.setString(2, patientId);
                    ps.executeUpdate();
                }
            }
            ret = getStudyIds(studyUid);
        } catch (SQLException sex) {
            log.error("Exception retrieving study IDs", sex);
            throw sex;
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return ret;
    }

    // pat.pk patDemo.pk pat.present?
    private long identifyPatient(Patient p, String callingAE, String studyUid, String accessionNumber, String defaultIdIssuer, boolean isIhe, String calledAE, PartitioningStrategy strategy) throws SQLException, CannotStoreException {
        // Added unused accessionNumber to allow future updates in PatientId management
        long ret = 0;
        boolean withDoubts = false;
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call identifyPatient(?,?,?,?,?,?,?,?)}");
            cs.setString(1, p.getPatientId());
            cs.setString(2, p.getIdIssuer());
            cs.setString(3, p.getLastName());
            cs.setString(4, p.getFirstName());
            cs.setDate(5, p.getBirthDate());
            cs.setByte(6, (byte) (p.getIdIssuer().equals(defaultIdIssuer) ? 1 : 0));
            cs.registerOutParameter(7, Types.BIGINT);
            cs.registerOutParameter(8, Types.TINYINT);
            cs.execute();
            ret = cs.getLong(7);
            withDoubts = (cs.getByte(8) == 1 ? true : false);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        if (withDoubts && ret > 0) {
            log.warn("Identified patient " + ret + " but " + studyUid + " (studyUID) will be used as Patient Id (demographics mismatch)");
            p.setPatientId(studyUid);
            if (strategy.equals(PartitioningStrategy.CALLED))
                p.setIdIssuer(calledAE);
            else if (strategy.equals(PartitioningStrategy.CALLING))
                p.setIdIssuer(callingAE);
            else
                p.setIdIssuer(StorageServer.IDISSUER_O3DPACS);
            p.setPatientIdentifierList(studyUid);
            ret = 0;
        }
        return ret;
    }

    /**
     * Store series. Insert into table Series, the relative object attribute and the number of related instances
     * 
     * @param series
     *            the series object to store to database
     * @param numOfInstancesToAdd
     *            the number of instances to add to the table
     * @param studyInstanceUID
     * @return the seriesInstanceUID or null if none.
     */
    private String storeSeries(Series series, int numOfInstancesToAdd, String studyInstanceUID, String callingAE) throws SQLException {
        Connection con = getDBConnection();
        PreparedStatement updateSeriesPS = null;
        Statement st = null;
        PreparedStatement updateNumberOfInstances = null;
        ResultSet numberOfStudyRelatedInstancesRS = null;
        try {
            // con.setAutoCommit(false);
            updateSeriesPS = con.prepareStatement(updateSeries);
            updateSeriesPS.setString(1, series.getSeriesStatus()); // ---
            updateSeriesPS.setInt(2, numOfInstancesToAdd);
            updateSeriesPS.setString(3, series.getSeriesInstanceUid());
            log.debug(callingAE + ": About to update the series");
            st = con.createStatement();
            numberOfStudyRelatedInstancesRS = st.executeQuery("SELECT numberOfStudyRelatedInstances FROM Studies WHERE studyInstanceUID = '" + studyInstanceUID + "'");
            if (numberOfStudyRelatedInstancesRS.next()) {
                if (numberOfStudyRelatedInstancesRS.getInt("numberOfStudyRelatedInstances") == 0) {
                    st.executeUpdate("UPDATE Studies SET numberOfStudyRelatedInstances = (SELECT SUM(numberOfSeriesRelatedInstances) FROM Series WHERE studyFK = '" + studyInstanceUID
                            + "' AND deprecated=0 ) WHERE studyInstanceUID = '" + studyInstanceUID + "'");
                    log.debug(callingAE + ": Updated numberOfStudyRelatedInstances in Studies table: " + studyInstanceUID);
                }
                updateNumberOfInstances = con.prepareStatement(updateStudyRelatedInstances);
                updateNumberOfInstances.setInt(1, numOfInstancesToAdd);
                updateNumberOfInstances.setString(2, studyInstanceUID);
                updateNumberOfInstances.executeUpdate();
            } else
                log.error("cannot find a value in numberOfStudyRelatedInstances...CHECK IT");
            if (updateSeriesPS.executeUpdate() == 1) {
                series.setToPerform(DicomConstants.ALREADY_PRESENT);
                return series.getSeriesInstanceUid();
            }
        } catch (SQLException sex) {
            log.error(callingAE, sex);
        } catch (Exception ex) {
            log.error(callingAE, ex);
        } finally {
            CloseableUtils.close(numberOfStudyRelatedInstancesRS);
            CloseableUtils.close(updateNumberOfInstances);
            CloseableUtils.close(st);
            CloseableUtils.close(updateSeriesPS);
            CloseableUtils.close(con);
        }
        return null;
    }

    private int storeImageInstance(Image i, String seriesFK, String callingAE) {
        int res = 0; // To know how many rows were affected (should be 1, can be 0)
        Connection con = null;
        PreparedStatement insertIiPS = null;
        PreparedStatement insertIiNofPS = null;
        try {
            con = getDBConnection();
            insertIiPS = con.prepareStatement(insertIi);
            insertIiPS.setString(1, i.getSopInstanceUid());
            insertIiPS.setString(2, i.getSopClassUid());
            if (i.getInstanceNumber() == null)
                insertIiPS.setNull(3, Types.BIGINT);
            else
                insertIiPS.setLong(3, Long.parseLong(i.getInstanceNumber()));
            insertIiPS.setString(4, seriesFK);
            if (i.isDeprecated()) {
                insertIiPS.setInt(5, 1);
            } else {
                insertIiPS.setInt(5, 0);
            }
            if (i.getSamplesPerPixel() == null)
                insertIiPS.setNull(6, Types.INTEGER);
            else
                insertIiPS.setInt(6, Integer.parseInt(i.getSamplesPerPixel()));
            if (i.getRows() == null)
                insertIiPS.setNull(7, Types.INTEGER);
            else
                insertIiPS.setInt(7, Integer.parseInt(i.getRows()));
            if (i.getColumns() == null)
                insertIiPS.setNull(8, Types.INTEGER);
            else
                insertIiPS.setInt(8, Integer.parseInt(i.getColumns()));
            if (i.getBitsAllocated() == null)
                insertIiPS.setNull(9, Types.INTEGER);
            else
                insertIiPS.setInt(9, Integer.parseInt(i.getBitsAllocated()));
            if (i.getBitsStored() == null)
                insertIiPS.setNull(10, Types.INTEGER);
            else
                insertIiPS.setInt(10, Integer.parseInt(i.getBitsStored()));
            if (i.getHighBit() == null)
                insertIiPS.setNull(11, Types.INTEGER);
            else
                insertIiPS.setInt(11, Integer.parseInt(i.getHighBit()));
            if (i.getPixelRepresentation() == null)
                insertIiPS.setNull(12, Types.INTEGER);
            else
                insertIiPS.setInt(12, Integer.parseInt(i.getPixelRepresentation()));
            res = insertIiPS.executeUpdate();
            log.debug(callingAE + ": Added " + res + " images");
            if (i.getNumberOfFrames() != null && i.getNumberOfFrames() > 0) {
                insertIiNofPS = con.prepareStatement(INSERT_IMAGE_NUMOFFRAMES);
                insertIiNofPS.setString(1, i.getSopInstanceUid());
                insertIiNofPS.setInt(2, i.getNumberOfFrames());
                insertIiNofPS.executeUpdate();
            }
        } catch (Exception ex) {
            log.error("Couldn't insert Image Instance", ex);
        } finally {
            CloseableUtils.close(insertIiNofPS);
            CloseableUtils.close(insertIiPS);
            CloseableUtils.close(con);
        }
        return res;
    }

    private int storeNonImageInstance(NonImage ni, String seriesFK, String callingAE) {
        int res = 0; // To know how many rows were affected (should be 1, can be 0)
        Connection con = null;
        PreparedStatement insertNiiPS = null;
        try {
            con = getDBConnection();
            insertNiiPS = con.prepareStatement(insertNii);
            insertNiiPS.setString(1, ni.getSopInstanceUid());
            insertNiiPS.setString(2, ni.getSopClassUid());
            if (ni.getInstanceNumber() == null)
                insertNiiPS.setNull(3, Types.BIGINT);
            else
                insertNiiPS.setLong(3, Long.parseLong(ni.getInstanceNumber()));
            insertNiiPS.setString(4, seriesFK);
            insertNiiPS.setBoolean(5, ni.isDeprecated()); // ---
            res = insertNiiPS.executeUpdate();
        } catch (SQLException sex) {
            log.error(callingAE, sex);
        } catch (Exception ex) {
            log.error(callingAE, ex);
        } finally {
            CloseableUtils.close(insertNiiPS);
            CloseableUtils.close(con);
        }
        return res;
    }

    private int storeOverlayInstance(Overlay o, String seriesFK, String callingAE) {
        int res = 0; // To know how many rows were affected (should be 1, can be 0)
        String siuid = o.getSopInstanceUid(); // Not to call the same method
        // repeatedly
        Connection con = null;
        PreparedStatement insertOiPS = null;
        PreparedStatement insertO2IPS = null;
        try {
            con = getDBConnection();
            insertOiPS = con.prepareStatement(insertOi);
            insertO2IPS = con.prepareStatement(insertO2I);
            insertOiPS.setString(1, siuid);
            insertOiPS.setString(2, o.getSopClassUid());
            if (o.getInstanceNumber() == null)
                insertOiPS.setNull(3, Types.BIGINT);
            else
                insertOiPS.setLong(3, Long.parseLong(o.getInstanceNumber()));
            insertOiPS.setString(4, seriesFK);
            insertOiPS.setBoolean(5, o.isDeprecated()); // ---
            if (o.getOverlayNumber() == null)
                insertOiPS.setNull(6, Types.BIGINT);
            else
                insertOiPS.setLong(6, Long.parseLong(o.getOverlayNumber()));
            if (o.getOverlayRows() == null)
                insertOiPS.setNull(7, Types.INTEGER);
            else
                insertOiPS.setInt(7, Integer.parseInt(o.getOverlayRows()));
            if (o.getOverlayColumns() == null)
                insertOiPS.setNull(8, Types.INTEGER);
            else
                insertOiPS.setInt(8, Integer.parseInt(o.getOverlayColumns()));
            insertOiPS.setString(9, o.getOverlayType());
            if (o.getOverlayBitsAllocated() == null)
                insertOiPS.setNull(10, Types.INTEGER);
            else
                insertOiPS.setInt(10, Integer.parseInt(o.getOverlayBitsAllocated()));
            res = insertOiPS.executeUpdate(); //
            RefToInstances r2i = o.getReferencedInstances();
            // Get the list of all images referenced by this overlay instance
            String[] temp = null;
            while ((temp = r2i.getNextInstance()) != null) {
                insertO2IPS.setString(1, siuid);
                insertO2IPS.setString(2, temp[0]);
                insertO2IPS.setString(3, temp[1]);
                insertO2IPS.executeUpdate();
            } // end while
        } catch (SQLException sex) {
            log.error(callingAE, sex);
        } catch (Exception ex) {
            log.error(callingAE, ex);
        } finally {
            CloseableUtils.close(insertO2IPS);
            CloseableUtils.close(insertOiPS);
            CloseableUtils.close(con);
        }
        return res;
    } // end storeOverlayInstance()

    private int storePresStateInstance(PresState ps, String seriesFK, String callingAE) {
        // To know how many rows were affected (should be 1, can be 0)
        int res = 0;
        // Not to call the same method repeatedly
        String siuid = ps.getSopInstanceUid();
        Connection con = null;
        PreparedStatement insertPsiPS = null;
        PreparedStatement insertPs2IPS = null;
        try {
            con = getDBConnection();
            insertPsiPS = con.prepareStatement(insertPsi);
            insertPs2IPS = con.prepareStatement(insertPs2I);
            insertPsiPS.setString(1, siuid);
            insertPsiPS.setString(2, ps.getSopClassUid());
            if (ps.getInstanceNumber() == null)
                insertPsiPS.setNull(3, Types.BIGINT);
            else
                insertPsiPS.setLong(3, Long.parseLong(ps.getInstanceNumber()));
            insertPsiPS.setString(4, seriesFK);
            insertPsiPS.setBoolean(5, ps.isDeprecated()); // ---
            insertPsiPS.setString(6, ps.getPresentationLabel());
            insertPsiPS.setString(7, ps.getPresentationDescription());
            insertPsiPS.setDate(8, ps.getPresentationCreationDate());
            insertPsiPS.setTime(9, ps.getPresentationCreationTime());
            insertPsiPS.setString(10, ps.getPresentationCreatorsName());
            insertPsiPS.setString(11, ps.getRecommendedViewingMode());
            res = insertPsiPS.executeUpdate();
            RefToInstances[] r2i = ps.getReferencedSeries();
            // Get the list of all images referenced by this overlay instance
            String[] temp = null;
            for (int l = r2i.length - 1; l >= 0; l--) {
                while ((temp = r2i[l].getNextInstance()) != null) {
                    insertPs2IPS.setString(1, siuid);
                    insertPs2IPS.setString(2, temp[0]);
                    insertPs2IPS.setString(3, temp[1]);
                    insertPs2IPS.executeUpdate();
                } // end while
            } // end for
        } catch (SQLException sex) {
            log.error(callingAE, sex);
        } catch (Exception ex) {
            log.error(callingAE, ex);
        } finally {
            CloseableUtils.close(insertPsiPS);
            CloseableUtils.close(insertPs2IPS);
            CloseableUtils.close(con);
        }
        return res;
    } // end storePresStateInstance()

    private int storeStructRepInstance(StructRep sr, String seriesFK, String callingAE) {
        int res = 0; // To know how many rows were affected (should be 1, can be
        // 0)
        Connection con = null;
        PreparedStatement insertSriPS = null;
        try {
            con = getDBConnection();
            insertSriPS = con.prepareStatement(insertSri);
            insertSriPS.setString(1, sr.getSopInstanceUid());
            insertSriPS.setString(2, sr.getSopClassUid());
            if (sr.getInstanceNumber() == null)
                insertSriPS.setNull(3, Types.BIGINT);
            else
                insertSriPS.setLong(3, Long.parseLong(sr.getInstanceNumber()));
            insertSriPS.setString(4, seriesFK);
            insertSriPS.setBoolean(5, sr.isDeprecated()); // ---
            insertSriPS.setString(6, sr.getCompletionFlag());
            insertSriPS.setString(7, sr.getVerificationFlag());
            insertSriPS.setDate(8, sr.getContentDate());
            insertSriPS.setTime(9, sr.getContentTime());
            insertSriPS.setTimestamp(10, sr.getObservationDateTime());
            // -------------------------------------
            long pcsFK = storeCodeSequence(sr.getConceptNameCodeSequence(), callingAE);
            log.debug(callingAE + ": SR References CS " + pcsFK);
            insertSriPS.setLong(11, pcsFK);
            res = insertSriPS.executeUpdate();
        } catch (SQLException sex) {
            log.error(callingAE, sex);
        } catch (Exception ex) {
            log.error(callingAE, ex);
        } finally {
            CloseableUtils.close(insertSriPS);
            CloseableUtils.close(con);
        }
        return res;
    } // end storeStructRepInstance()
    
    private int storeKeyObjectInstance(KeyObject ko, String seriesFK, String callingAE) {
        int res = 0; // To know how many rows were affected (should be 1, can be
        // 0)
        Connection con = null;
        PreparedStatement insertKoiPS = null;
        PreparedStatement insertKoi2PS = null;
        try {
            con = getDBConnection();
            insertKoiPS = con.prepareStatement(insertKoi);
            insertKoi2PS = con.prepareStatement(insertKo2i);
            insertKoiPS.setString(1, ko.getSopInstanceUid());
            insertKoiPS.setString(2, ko.getSopClassUid());
            if (ko.getInstanceNumber() == null)
                insertKoiPS.setNull(3, Types.BIGINT);
            else
                insertKoiPS.setLong(3, Long.parseLong(ko.getInstanceNumber()));     
            insertKoiPS.setString(4, seriesFK);
            insertKoiPS.setDate(5, ko.getContentDate());
            insertKoiPS.setTime(6, ko.getContentTime());
            long pcsFK = storeCodeSequence(ko.getConceptNameCodeSequence(), callingAE);
            log.debug(callingAE + ": KO References CS " + pcsFK);
            insertKoiPS.setLong(7, pcsFK);
            insertKoiPS.setBoolean(8, ko.isDeprecated()); // ---
            res = insertKoiPS.executeUpdate();
            
            RefToInstances[] refIns = ko.getReferencedSeries();
            String[] temp = null;
            log.debug(callingAE + ": KO Instance References");
            for (int l = refIns.length - 1; l >= 0; l--) {
                while ((temp = refIns[l].getNextInstance()) != null) {
                	insertKoi2PS.setString(1, ko.getSopInstanceUid());
                	insertKoi2PS.setString(2, refIns[l].getSeries());
                	insertKoi2PS.setString(3, temp[0]);
                	insertKoi2PS.setString(4, temp[1]);
                	insertKoi2PS.executeUpdate();
                } // end while
            } // end for
        } catch (SQLException sex) {
            log.error(callingAE, sex);
        } catch (Exception ex) {
            log.error(callingAE, ex);
        } finally {
            CloseableUtils.close(insertKoiPS);
            CloseableUtils.close(insertKoi2PS);
            CloseableUtils.close(con);
        }
        return res;
    } // end storeKeyObjectInstance()

    private String completeStudyStorage(Study study, String patientFK, String callingAE) {
        if (study.getToPerform() == DicomConstants.ALREADY_PRESENT)
            return study.getStudyInstanceUid();
        Connection con = null;
        PreparedStatement updateStudyPS = null;
        PreparedStatement insertNoprsPS = null;
        try {
            con = getDBConnection();
            String sUid = study.getStudyInstanceUid();
            // log.info(sUid);
            long pcsFK = storeCodeSequence(study.getProcedureCodeSequence(), callingAE);
            log.debug(callingAE + ": Study References CS " + pcsFK);
            boolean overwriteCodeSeq = "true".equals(GlobalConfigurationLoader.getConfigParam("overwriteStudyCodeSeq"));
            if (overwriteCodeSeq)
                updateStudyPS = con.prepareStatement(updateStudy);
            else
                updateStudyPS = con.prepareStatement(updateStudyNotCodeSeq);
            updateStudyPS.setString(1, study.getStudyStatusId());
            updateStudyPS.setDate(2, study.getStudyDate());
            updateStudyPS.setTime(3, study.getStudyTime());
            updateStudyPS.setDate(4, study.getStudyCompletionDate());
            updateStudyPS.setTime(5, study.getStudyCompletionTime());
            updateStudyPS.setDate(6, study.getStudyVerifiedDate());
            updateStudyPS.setTime(7, study.getStudyVerifiedTime());
            updateStudyPS.setString(8, study.getAdmittingDiagnosesDescription());
            updateStudyPS.setString(9, ((study.getStudyStatus() == null) ? "o" : study.getStudyStatus()));
            // updateStudyPS.setLong(10, Long.parseLong(s.getStudySize()));
            // updateStudyPS.setString(10, study.getAccessionNumber());
            updateStudyPS.setString(10, study.getStudyId());
            updateStudyPS.setString(11, study.getStudyDescription());
            if (overwriteCodeSeq)
                updateStudyPS.setLong(12, pcsFK);
            if (overwriteCodeSeq)
                updateStudyPS.setString(13, sUid);
            else
                updateStudyPS.setString(12, sUid);
            // updateStudyPS.setLong(15, Long.parseLong(patientFK));
            int res = updateStudyPS.executeUpdate();
            log.debug(callingAE + ": res=" + res + " storeStudy():      About to deal with Personnel!!!!!!");
            PersonalName[] pn = study.getNamesOfPhysiciansReadingStudy(); // Now I need to store data about the personnel
            if (pn != null) {
                insertNoprsPS = con.prepareStatement(insertNoprs);
                for (int i = pn.length - 1; i >= 0; i--) {
                    long pk = storePersonnel(pn[i]); // Update table
                    // Personnel...
                    insertNoprsPS.setLong(1, pk); // ... then the relationship between Personnel and Studies
                    insertNoprsPS.setString(2, sUid);
                    try {
                        insertNoprsPS.executeUpdate();
                    } catch (SQLException sex) {
                        log.debug(callingAE + ": The physician was already associated with the study!");
                        log.debug(sex.getStackTrace());
                        // Nothing should be performed: the personnel is already associated to this study!
                    } // end try...catch
                } // end for
            } // end if
            study.resetPhysiciansReadingStudy(); // If this instance of Study
            // will be used again, it won't try to store its Physicians again!
            study.setToPerform(DicomConstants.ALREADY_PRESENT);
            pn = null;
            return sUid;
        } catch (SQLException sex) {
            log.error(callingAE, sex);
        } catch (Exception ex) {
            log.error(callingAE, ex);
        } finally {
            CloseableUtils.close(insertNoprsPS);
            CloseableUtils.close(updateStudyPS);
            CloseableUtils.close(con);
        }
        return null;
    }

    private long storePersonnel(PersonalName pn) throws SQLException {
        long res = 0;
        CallableStatement cs = null;
        Connection con = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call identifyPersonnel(?,?,?,?,?,?)}");
            cs.setString(1, pn.getLastName());
            cs.setString(2, pn.getFirstName());
            cs.setString(3, pn.getMiddleName());
            cs.setString(4, pn.getPrefix());
            cs.setString(5, pn.getSuffix());
            cs.registerOutParameter(6, Types.BIGINT);
            cs.execute();
            res = cs.getLong(6);
        } catch (Exception ex) {
            throw new SQLException(ex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return res;
    } // end storePersonnel()

    private long storeCodeSequence(CodeSequence cs, String callingAE) throws SQLException {
        Connection con = null;
        PreparedStatement selectInsertedCodSeqPS = null;
        PreparedStatement insertCodSeqPS = null;
        ResultSet rs = null;
        int res = 0; // To know how many rows were affected (should be 1, can be
        // 0)
        log.debug(callingAE + ": Trying to store a CodeSequence");
        long insId = 0; // The Id of the inserted row.
        if ((cs == null) || ((cs.getCodeValue() == null) && (cs.getCodingSchemeDesignator() == null) && (cs.getCodingSchemeVersion() == null))) { // No
            // code
            // Sequence was
            // provided...
            // */
            log.debug(callingAE + ": CodeSequence is null!!!!!!!!!!");
            return insId; // ... Return the default one!!!
        }
        try {
            con = getDBConnection();
            selectInsertedCodSeqPS = con.prepareStatement(selectInsertedCodSeq);
            selectInsertedCodSeqPS.setString(1, cs.getCodeValue());
            selectInsertedCodSeqPS.setString(2, cs.getCodingSchemeDesignator());
            selectInsertedCodSeqPS.setString(3, cs.getCodingSchemeVersion());
            rs = selectInsertedCodSeqPS.executeQuery();
            if (rs.next()) {
                log.debug(callingAE + ": Already Stored: " + rs.getLong(1));
                insId = rs.getLong(1); // The CodeSequence was already present!
                // Otherwise, go on and insert it!
                if (insId != 0) {
                    return insId; // if the returned row is the first one,
                    // assume the current codeSeq is to be added
                    // and go on
                }
            }
            insertCodSeqPS = con.prepareStatement(insertCodSeq);
            insertCodSeqPS.setString(1, cs.getCodeValue());
            insertCodSeqPS.setString(2, cs.getCodingSchemeDesignator());
            insertCodSeqPS.setString(3, cs.getCodingSchemeVersion());
            insertCodSeqPS.setString(4, cs.getCodeMeaning());
            // log.debug(insertCodSeqPS.toString());
            res = insertCodSeqPS.executeUpdate();
            /* if(INFO) */log.debug(callingAE + ": Inserted a CodeSequence: ");
            if (res == 1) {
                rs = selectInsertedCodSeqPS.executeQuery(); // The
                // PreparedStatement
                // is ready since
                // the previous
                // call!
                rs.next(); // Exactly one record should be in the ResultSet
                insId = rs.getLong(1); // Get the value of the first and only
                // column
                /* if(INFO) */log.debug(callingAE + ": Retrieved CodeSequence " + insId);
            } // end if
        } catch (SQLException sex) {
            log.error(callingAE, sex);
        } catch (Exception ex) {
            log.error(callingAE, ex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(insertCodSeqPS);
            CloseableUtils.close(selectInsertedCodSeqPS);
            CloseableUtils.close(con);
        }
        return insId; // It returns the id of the inserted row.
    } // end storeCodeSequence()

    private Long storeNewPatient(Patient p) throws SQLException, IncorrectPatientIdException {
        Long ret = null;
        Connection con = null;
        CallableStatement cs = null;
        String patientIdRegEx = PatientIdCheckSettings.getPatientIdRegEx();
        if (patientIdRegEx != null) {
            log.debug("Checking patientId with this regEx: " + patientIdRegEx);
            Pattern pp = Pattern.compile(patientIdRegEx);
            if (!pp.matcher(p.getPatientId()).find()) {
                throw new IncorrectPatientIdException("The patientId: " + p.getPatientId() + " is not valid for regEx: " + patientIdRegEx);
            }
        }
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call addNewPatient(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            cs.setString(1, ((p.getLastName() == null) || ("".equals(p.getLastName())) ? null : p.getLastName().toUpperCase()));
            cs.setString(2, ((p.getFirstName() == null) || ("".equals(p.getFirstName())) ? null : p.getFirstName().toUpperCase()));
            cs.setString(3, ("".equals(p.getMiddleName()) ? null : p.getMiddleName()));
            cs.setString(4, ("".equals(p.getPrefix()) ? null : p.getPrefix()));
            cs.setString(5, ("".equals(p.getSuffix()) ? null : p.getSuffix()));
            cs.setDate(6, p.getBirthDate());
            cs.setTime(7, p.getBirthTime());
            cs.setString(8, ((p.getSex() == null) || ("".equals(p.getSex())) ? null : p.getSex().toUpperCase()));
            cs.setString(9, p.getPatientId().toUpperCase());
            cs.setString(10, p.getIdIssuer().toUpperCase());
            cs.setString(11, p.getEthnicGroup());
            cs.setString(12, p.getPatientComments());
            cs.setString(13, p.getRace());
            cs.setString(14, p.getPatientAddress());
            cs.setString(15, ((p.getPatientAccountNumber() == null) || ("".equals(p.getPatientAccountNumber())) ? null : p.getPatientAccountNumber().toUpperCase()));
            cs.setString(16, ((p.getPatientIdentifierList() == null) || ("".equals(p.getPatientIdentifierList())) ? null : p.getPatientIdentifierList().toUpperCase()));
            cs.setString(17, p.getPatientCity());
            cs.registerOutParameter(18, Types.BIGINT);
            cs.execute();
            ret = cs.getLong(18);
            if (cs.wasNull())
                throw new SQLException("A null patient pk was returned after Patient creation");
            try {
                PatientRecordMessage msg = new PatientRecordMessage(ActionCode.CREATE);
                String patientId, patientName = "";
                if (((p.getPatientId() != null) && (!"".equals(p.getPatientId())))) {
                    patientId = p.getPatientId();
                } else {
                    patientId = "EmptyPatientID";
                }
                if ((p.getLastName() != null) && (!"".equals(p.getLastName()))) {
                    patientName += p.getLastName();
                }
                patientName += "^";
                if ((p.getFirstName() != null) && (!"".equals(p.getFirstName()))) {
                    patientName += p.getFirstName();
                }
                msg.addPatient(patientId, patientName);
                msg.addUserPerson(_assoc.getAssociation().getCallingAET(), "", "", _assoc.getAssociation().getSocket().getInetAddress().toString(), true);
                msg.addUserPerson(_assoc.getAssociation().getCalledAET(), "", "", _assoc.getAssociation().getSocket().getLocalAddress().toString(), false);
                AuditLogService atnaService = AuditLogService.getInstance();
                atnaService.SendMessage(msg);
            } catch (Exception e) {
                log.warn("Unable to send AuditLogMessage", e);
            }
        } catch (SQLException sex) {
            throw sex;
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return ret;
    }

    private boolean isVisitPresent(Patient p, Study s) {
        boolean res = false;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getDBConnection();
            ps = con.prepareStatement(visitPres);
            ps.setString(1, s.getStudyInstanceUid());
            ps.setLong(2, Long.parseLong(p.getPrimaryKey()));
            rs = ps.executeQuery();
            if ((rs.next()) && (rs.getString("pk") != null)) {
                res = true;
                log.debug(callingAE + ": THE VISIT WAS PRESENT!!");
            }
        } catch (SQLException sex) {
            log.error(callingAE + "Couldn't verify if visit is present.", sex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return res;
    }

    private boolean startUpVisit(Patient p, Study s) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getDBConnection();
            ps = con.prepareStatement(startupVisit);
            ps.setString(1, p.getPatientState());
            ps.setString(2, p.getPatientClass());
            ps.setString(3, p.getAssignedPatientLocation());
            ps.setString(4, p.getVisitNumber());
            if (p.getPregnancyStatus() == null)
                ps.setNull(5, Types.SMALLINT);
            else
                ps.setShort(5, Short.parseShort(p.getPregnancyStatus()));
            ps.setString(6, p.getMedicalAlerts());
            if (p.getPatientWeight() == null)
                ps.setNull(7, Types.BIGINT);
            else
                ps.setLong(7, Long.parseLong(p.getPatientWeight()));
            ps.setString(8, p.getConfidentialityConstraint());
            ps.setString(9, p.getSpecialNeeds());
            ps.setLong(10, Long.parseLong(p.getPrimaryKey()));
            ps.setString(11, s.getStudyInstanceUid());
            log.debug(callingAE + ": INSERTING VISIT: " + ps);
            ps.executeUpdate();
        } catch (SQLException sex) {
            log.error("Couldn't insert new visit.", sex);
            return false;
        } finally {
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return true;
    }

    private boolean updateVisit(Patient p, Study s) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getDBConnection();
            ps = con.prepareStatement(updVisit);
            ps.setString(1, p.getPatientState());
            if (p.getPregnancyStatus() == null)
                ps.setNull(2, Types.SMALLINT);
            else
                ps.setShort(2, Short.parseShort(p.getPregnancyStatus()));
            ps.setString(3, p.getMedicalAlerts());
            if (p.getPatientWeight() == null)
                ps.setNull(4, Types.BIGINT);
            else
                ps.setLong(4, Long.parseLong(p.getPatientWeight()));
            ps.setString(5, p.getConfidentialityConstraint());
            ps.setString(6, p.getSpecialNeeds());
            ps.setString(7, p.getAssignedPatientLocation());
            ps.setString(8, s.getStudyInstanceUid());
            ps.setLong(9, Long.parseLong(p.getPrimaryKey()));
            log.debug(callingAE + ": visist updated. ");
            ps.executeUpdate();
        } catch (SQLException sex) {
            log.error("Couldn't update visit", sex);
            return false;
        } finally {
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return true;
    }

    /**
     * Returns the url where this study is stored, or null if you can't add to it
     * 
     * @param study
     * @param aet
     * @param pfk
     * @param physicalMediaPk
     * @param callingAE
     * @return
     */
    private String fetchStudyFastestAccess(Study study, String aet, long pfk, String physicalMediaPk, String callingAE) {
        // THIS CAN BE MERGED INTO getStudyIds WHEN THE WHOLE STUFF IS REFACTORED
        char studyStatus = Study.DPACS_OPEN_STATUS;
        String fastestAccess = null;
        Connection con = null;
        Statement stat = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getDBConnection();
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT studyInstanceUID, fastestAccess, studyStatus FROM Studies WHERE studyInstanceUID='" + study.getStudyInstanceUid() + "'");
            if (rs.next()) { // If no row was returned, the study doesn't exist, otherwise at this point it's an error!!!!
                log.debug(callingAE + ": Study did exist!");
                studyStatus = rs.getString(3).charAt(0);
                fastestAccess = rs.getString(2);
                log.debug(callingAE + ": Study stored in " + fastestAccess);
                if (fastestAccess == null) {
                    if (studyStatus == Study.DPACS_OPEN_STATUS) {
                        String url = getRelatedUrl(physicalMediaPk, callingAE);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                        String date = sdf.format(new Date());
                        if (!url.endsWith("/"))
                            url += "/";
                        url = url + date + "/";
                        return url;
                    } else {
                        log.info(callingAE + ": For Dicom Storage Dealer: Study Status is not OPEN");
                        return null;
                    }
                } else { // a fastestAccess was found
                    if (studyStatus == Study.DPACS_OPEN_STATUS) {
                        return fastestAccess;
                    } else {
                        if (studyStatus == Study.DPACS_ARCHIVED_STATUS) { // Davide 20110318 ?????????????????? I guess it's to be thought about more...
                            ps = con.prepareStatement(reOpenStudy);
                            ps.setString(1, study.getStudyInstanceUid());
                            log.info(callingAE + ": Adding series to an ARCHIVED Study, re-opening : " + study.getStudyInstanceUid());
                            try {
                                ps.executeUpdate();
                            } catch (SQLException sex) {
                                log.error(callingAE + ": Error re-opening study...", sex);
                            }
                            return fastestAccess;
                        } else {
                            return null;
                        }
                    }
                }
            } else {
                throw new Exception("STUDY NOT FOUND IN fetchStudyFastestAccess! CHECK!");
            }
        } catch (Exception ex) {
            log.error(callingAE, ex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(stat);
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return null; // Never reached!!
    } // end fetchStudyFastestAccess()

    private int startupStudy(Study s, long patientFK, String callingAE, byte mark) throws CannotStoreException {
        int res = 1;
        Connection con = null;
        CallableStatement cs = null;
        try {
            long pcsFK = 0;
            if (s.getProcedureCodeSequence() != null) {
                pcsFK = storeCodeSequence(s.getProcedureCodeSequence(), callingAE);
            }
            con = getDBConnection();
            cs = con.prepareCall("{call addNewStudy(?,?,?,?,?,?,?,?)}");
            cs.setString(1, s.getStudyInstanceUid());
            cs.setString(2, s.getAccessionNumber());
            cs.setLong(3, patientFK);
            cs.setString(4, s.getStudyId());
            cs.setString(5, s.getStudyDescription());
            cs.setLong(6, pcsFK);
            cs.setString(7, s.getReferringPhysiciansName());
            cs.setByte(8, mark);
            cs.executeUpdate();
        } catch (Exception ex) {
            log.error(callingAE, ex);
            res = 0;
            throw new CannotStoreException(CannotStoreException.CANNOT_ADD_TO_STUDY);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return res;
    }

    private String getRelatedUrl(String physicalMediaPk, String callingAE) throws SQLException {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        try {
            con = getDBConnection();
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT urlToStudy, available, name FROM PhysicalMedia WHERE pk=" + physicalMediaPk);
            if (rs.next()) {
                log.debug(callingAE + ": getRelatedUrl found one result");
                if ((rs.getInt(2) == 1)) {
                    return rs.getString(1);
                } else {
                    log.error("The PhysicalMedia '" + rs.getString(3) + "' is UNAVAILABLE");
                    return null;
                }
            } else {
                log.error(callingAE + ": Maybe modality has presented with Wrong AE-TITLE or is BADLY associated with storage Area"); // end
                return null;
            }
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
    }

    private void verifyPatientStudyConsistency(String uid, long pk) throws CannotStoreException {
        // Verify whether the study is already present as belonging to another patient
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        try {
            con = getDBConnection();
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT patientFK FROM Studies WHERE studyInstanceUID='" + uid + "'");
            if ((!rs.next()) || (rs.getLong(1) != pk)) {
                throw new CannotStoreException(CannotStoreException.STUDY_OF_ANOTHER_PAT);
            }
        } catch (SQLException sex) {
            log.error(callingAE + ": while retrieving Study Infos.", sex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
    }

    private boolean isSeriesPresent(Series s, String studyUID) throws Exception {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        boolean b = false;
        try {
            con = getDBConnection();
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT seriesInstanceUID, studyFk FROM Series WHERE seriesInstanceUID='" + s.getSeriesInstanceUid() + "'");
            if (rs.next()) {
                String studyFk = rs.getString(2);
                if (studyUID.equals(studyFk)) {
                    b = true;
                } else {
                    log.error(callingAE + ": For series " + s.getSeriesInstanceUid() + " found this studyFk " + studyFk + " but is arrived with " + studyUID);
                    throw new CannotStoreException("The series belongs to another study!!");
                }
            }
        } catch (CannotStoreException ee) {
            throw ee;
        } catch (Exception ex) {
            log.error(callingAE + ": Couldn't verify if series is present.", ex);
            throw ex;
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
        return b;
    } // end isSeriesPresent()

    /**
     * Startup series. Insert a new series and update numberOfStudiesRealtedSeries in Studies database table. It use transaction so if something go wrong it rollback and catch an exception
     * 
     * @param series
     *            the series object to store
     * @param studyFK
     *            the studyInstanceUID referenced to the series
     * @param equipmentFK
     *            the value of primary key associated to the node equipment
     */
    private void startupSeries(Series series, String studyFK, String equipmentFK, String callingAE) {
        Connection myConn = null;
        PreparedStatement indirectEquipmentPS = null;
        PreparedStatement insertIndEquipmentPS = null;
        PreparedStatement insertSeriesPS = null;
        Statement st = null;
        ResultSet rs = null;
        ResultSet numberOfStudyRelatedSeriesRS = null;
        try {
            myConn = getDBConnection();
            // if(equipmentTable==null) loadEquipmentCache(); // AAAHHH DTODO
            // even the method
            // equipmentFK=(String)equipmentTable.get(s.getAETitle());
            if (equipmentFK == null) { // It means I probably have a Storage
                // request from another PACS
                // This should just occur when a DICOM storage is requested!
                if (series.getEquipment() == null) {
                    // stops the execution of the method
                    return; // This causes a mangle of
                    // CannotUpdateExceptions!!!!
                }
                indirectEquipmentPS = myConn.prepareStatement(indirectEquipment);
                indirectEquipmentPS.setString(1, series.getEquipment().getManufacturer());
                indirectEquipmentPS.setString(2, series.getEquipment().getInstitutionName());
                indirectEquipmentPS.setString(3, series.getEquipment().getStationName());
                indirectEquipmentPS.setString(4, series.getEquipment().getInstitutionalDepartmentName());
                indirectEquipmentPS.setString(5, series.getEquipment().getManufacturersModelName());
                indirectEquipmentPS.setString(6, series.getEquipment().getDeviceSerialNumber());
                indirectEquipmentPS.setDate(7, series.getEquipment().getDateOfLastCalibration());
                indirectEquipmentPS.setTime(8, series.getEquipment().getTimeOfLastCalibration());
                indirectEquipmentPS.setString(9, series.getEquipment().getConversionType());
                indirectEquipmentPS.setString(10, series.getEquipment().getSecondaryCaptureDeviceId());
                // log.info(callingAE+": EQUIPMENT: "+indirectEquipmentPS);
                rs = indirectEquipmentPS.executeQuery();
                if (rs.next()) {
                    if (rs.getString(1) == null) {
                        log.info(callingAE + ": EQUIPMENT: No results were returned -----");
                        insertIndEquipmentPS = myConn.prepareStatement(insertIndEquipment);
                        insertIndEquipmentPS.setString(1, series.getEquipment().getManufacturer());
                        insertIndEquipmentPS.setString(2, series.getEquipment().getInstitutionName());
                        insertIndEquipmentPS.setString(3, series.getEquipment().getStationName());
                        insertIndEquipmentPS.setString(4, series.getEquipment().getInstitutionalDepartmentName());
                        insertIndEquipmentPS.setString(5, series.getEquipment().getManufacturersModelName());
                        insertIndEquipmentPS.setString(6, series.getEquipment().getDeviceSerialNumber());
                        insertIndEquipmentPS.setDate(7, series.getEquipment().getDateOfLastCalibration());
                        insertIndEquipmentPS.setTime(8, series.getEquipment().getTimeOfLastCalibration());
                        insertIndEquipmentPS.setString(9, series.getEquipment().getConversionType());
                        insertIndEquipmentPS.setString(10, series.getEquipment().getSecondaryCaptureDeviceId());
                        // log.info(callingAE+": EQUIPMENT: "+insertIndEquipmentPS);
                        insertIndEquipmentPS.executeUpdate();
                        rs = indirectEquipmentPS.executeQuery();
                        if (rs.next())
                            equipmentFK = rs.getString(1);
                    } else {
                        equipmentFK = rs.getString(1);
                    }
                } // end if
            } // end if equipmentFK==null
            insertSeriesPS = myConn.prepareStatement(insertSeries);
            insertSeriesPS.setString(1, series.getSeriesInstanceUid());
            insertSeriesPS.setString(2, studyFK);
            insertSeriesPS.setInt(3, Integer.parseInt(equipmentFK));
            if (series.getSeriesNumber() != null) {
                insertSeriesPS.setLong(4, Long.parseLong(series.getSeriesNumber()));
            } else {
                insertSeriesPS.setNull(4, Types.BIGINT);
            }
            if (series.getModality() != null) {
                insertSeriesPS.setString(5, series.getModality().toUpperCase());
            } else {
                insertSeriesPS.setString(5, series.getModality());
            }
            insertSeriesPS.setString(6, series.getBodyPartExamined());
            insertSeriesPS.setString(7, series.getSeriesDescription());
            insertSeriesPS.setString(8, callingAE);
            if (series.getOperatorsName() != null) {
                insertSeriesPS.setString(9, series.getOperatorsName().toUpperCase());
            } else {
                insertSeriesPS.setNull(9, Types.VARCHAR);
            }
            insertSeriesPS.executeUpdate();
            st = myConn.createStatement();
            numberOfStudyRelatedSeriesRS = st.executeQuery("SELECT numberOfStudyRelatedSeries FROM Studies WHERE studyInstanceUID = '" + studyFK + "'");
            if (numberOfStudyRelatedSeriesRS.next()) {
                if (numberOfStudyRelatedSeriesRS.getInt("numberOfStudyRelatedSeries") == 0) {
                    st.executeUpdate("UPDATE Studies SET numberOfStudyRelatedSeries = (SELECT COUNT(seriesInstanceUID) FROM Series WHERE studyFK = '" + studyFK
                            + "' AND deprecated=0 ) WHERE studyInstanceUID ='" + studyFK + "'");
                    log.debug(callingAE + ": Updated numberOfStudyRelatedSeries in Studies table: " + studyFK);
                } else {
                    PreparedStatement updateNumberOfSeries = myConn.prepareStatement(updateStudyRelatedSeries);
                    updateNumberOfSeries.setString(1, studyFK);
                    updateNumberOfSeries.executeUpdate();
                    updateNumberOfSeries.close();
                    log.debug("Updated numberOfStudyRelatedSeries in Studies table: numberOfStudyRelatedSeries++");
                }
            } else
                log.error(callingAE + ": Cannot find a value in studyRelatedSeries....CHECK IT");
        } catch (SQLException sex) {
            log.error(callingAE, sex);
        } catch (Exception ex) {
            log.error(callingAE, ex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(numberOfStudyRelatedSeriesRS);
            CloseableUtils.close(st);
            CloseableUtils.close(insertSeriesPS);
            CloseableUtils.close(insertIndEquipmentPS);
            CloseableUtils.close(indirectEquipmentPS);
            CloseableUtils.close(myConn);
        }
    }

    /**
     * Checks if is study deprecated.
     * 
     * @param sUid
     *            the studyInstanceUID to verify if deprecated.
     * @return true, if study is deprecated, otherwise false.
     */
    private boolean isStudyDeprecated(String sUid) {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        boolean ret = true; // It defaults to deprecated!!
        try {
            con = getDBConnection();
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT deprecated FROM Studies WHERE studyInstanceUID='" + sUid + "'");
            if (rs.next()) {
                if (rs.getBoolean(1) == false)
                    ret = false;
            }
        } catch (SQLException sex) {
            log.error(callingAE, sex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
        return ret;
    }

    private boolean writeHashtoDB(Dataset data, String hash) {
        log.debug(data.getString(Tags.SOPInstanceUID) + "'s hash = " + hash);
        String UID = data.getString(Tags.SOPInstanceUID);
        Connection con = null;
        Statement stat = null;
        boolean done = false;
        try {
            con = getDBConnection();
            stat = con.createStatement();
            done = stat.execute("INSERT INTO HashTable(sopInstanceUID,hash) VALUES ('" + UID + "','" + hash + "')");
        } catch (SQLException e) {
            log.error(callingAE + ": While inserting image hash in the DB", e);
        } finally {
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
        return done;
    }

    public boolean updateForwardSchedule(String studyUid, String callingAeTitle) {
        boolean result = true;
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call updateForwardSchedule(?,?)}");
            cs.setString(1, studyUid);
            cs.setString(2, callingAeTitle);
            cs.execute();
        } catch (SQLException sex) {
            log.error(callingAeTitle + ": Error updating Forwarder schedule ", sex);
            result = false;
        } catch (Exception ex) {
            log.error(callingAeTitle + ": Error updating Forwarder schedule ", ex);
            result = false;
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return result;
    }

    public boolean isStudyDeleted(String studyUid, String callingAeTitle) {
        boolean deleted = false;
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        StringBuilder selectString = new StringBuilder();
        selectString.append("SELECT COUNT(studyInstanceUID) FROM Studies WHERE fastestAccess IS NULL AND studyStatus='");
        selectString.append(Study.DPACS_ARCHIVED_STATUS).append("' AND studyInstanceUID='").append(studyUid).append("'");
        try {
            con = getDBConnection();
            stat = con.createStatement();
            rs = stat.executeQuery(selectString.toString());
            rs.next(); // always one row
            if (rs.getInt(1) > 0)
                deleted = true;
        } catch (SQLException sex) {
            log.error(callingAeTitle + ": Error reading study deletion status ", sex);
            deleted = false;
        } catch (Exception ex) {
            log.error(callingAeTitle + ": Error reading study deletion status ", ex);
            deleted = false;
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
        return deleted;
    }

    public boolean isStudyEditable(String studyInstanceUid, String callingAeTitle) {
        boolean editable = true;
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        StringBuilder selectString = new StringBuilder();
        selectString.append("SELECT studyInstanceUID FROM Studies WHERE isEditable=0 AND studyInstanceUID='").append(studyInstanceUid).append("'");
        try {
            con = getDBConnection();
            stat = con.createStatement();
            rs = stat.executeQuery(selectString.toString());
            if ((rs != null) && (rs.next()) && (studyInstanceUid.equals(rs.getString(1))))
                editable = false;
        } catch (SQLException sex) {
            log.error(callingAeTitle + ": Error reading study editability status ", sex);
            editable = false;
        } catch (Exception ex) {
            log.error(callingAeTitle + ": Error reading study editability status ", ex);
            editable = false;
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
        return editable;
    }

    private void copyMediaPolicyIntoStudy(String studyUid, String mediaPk) {
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call mapSettingFromMediaToStudy(?,?)}");
            cs.setString(1, mediaPk);
            cs.setString(2, studyUid);
            cs.execute();
        } catch (Exception ex) {
            log.error("Error mapping media storage setting into study ", ex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
    }

    public long completeOldStudies(String insertedBy, int minutes) {
        long ret = -1;
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call completeOldStudies(?,?,?)}");
            cs.setString(1, insertedBy);
            cs.setInt(2, minutes);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            ret = cs.getLong(3);
        } catch (Exception ex) {
            log.error("Error completing studies", ex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return ret;
    }

    public int addStudyTracking(String studyInstanceUid, String insertedBy) {
        int ret = -1;
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call addStudyTracking(?,?,?)}");
            cs.setString(1, studyInstanceUid);
            cs.setString(2, insertedBy);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            ret = cs.getInt(3);
        } catch (Exception ex) {
            log.error("Error adding studies", ex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return ret;
    }

    public void addSpecificCharSet(String studyInstanceUid, String charset) {
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call addSpecificCharSet(?,?)}");
            cs.setString(1, studyInstanceUid);
            cs.setString(2, charset);
            cs.execute();
        } catch (Exception ex) {
            log.error("Error adding specific Charset", ex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
    }

    public int completeStudy(String studyInstanceUid, String insertedBy) {
        int ret = -1;
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call completeStudy(?,?,?)}");
            cs.setString(1, studyInstanceUid);
            cs.setString(2, insertedBy);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            ret = cs.getInt(3);
        } catch (Exception ex) {
            log.error("Error closing study", ex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return ret;
    }

    public String findStudyForSeries(String[] seriesInstanceUids, int numberOfStudyRelatedInstances) {
        Connection con = null;
        Statement stat = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSet psRs = null;
        String ret = null;
        if (seriesInstanceUids == null)
            return ret;
        StringBuilder query = new StringBuilder();
        try {
            query.append("SELECT DISTINCT studyFK FROM Series WHERE seriesInstanceUID IN (");
            for (String uid : seriesInstanceUids)
                query.append("'").append(uid).append("',");
            query.deleteCharAt(query.length() - 1).append(")");
            con = getDBConnection();
            stat = con.createStatement();
            rs = stat.executeQuery(query.toString());
            if (rs.next()) {
                ret = rs.getString(1);
                if (numberOfStudyRelatedInstances >= 0) { // verify only if the value was specified
                    ps = con.prepareStatement("SELECT studyInstanceUID FROM Studies WHERE numberOfStudyRelatedInstances=? AND studyInstanceUID=?");
                    ps.setInt(1, numberOfStudyRelatedInstances);
                    ps.setString(2, ret);
                    psRs = ps.executeQuery();
                    if (!psRs.next())
                        ret = null;
                }
            }
        } catch (Exception ex) {
            log.error("Error closing study", ex);
        } finally {
            CloseableUtils.close(psRs);
            CloseableUtils.close(rs);
            CloseableUtils.close(ps);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
        return ret;
    }

    @Override
    public boolean stopProcessingInstance(String uid) {
        Connection con = null;
        CallableStatement cs = null;
        boolean outcome = false;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call stopProcessingInstance(?,?)}");
            cs.setString(1, uid);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            outcome = (cs.getInt(2) == 1);
        } catch (Exception ex) {
            log.error(callingAE + " stopProcessingInstance: ", ex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return outcome;
    }

    @Override
    public void updateStudiesAvailability(String studyInstanceUID) {
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call updateStudiesAvailability(?)}");
            cs.setString(1, studyInstanceUID);
            cs.execute();
        } catch (Exception ex) {
            log.error(callingAE + " stopProcessingInstance: ", ex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
    }
} // end class
