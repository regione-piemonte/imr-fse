/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.storage;

import it.units.htl.atna.AuditLogService;
import it.units.htl.dpacs.dao.CannotStoreException;
import it.units.htl.dpacs.dao.DeprecationRemote;
import it.units.htl.dpacs.dao.DicomDbDealer;
import it.units.htl.dpacs.dao.DicomStorageDealerLocal;
import it.units.htl.dpacs.dao.Hl7PublisherLocal;
import it.units.htl.dpacs.dao.ImageAvailabilityLocal;
import it.units.htl.dpacs.exceptions.BlockingException;
import it.units.htl.dpacs.exceptions.MultiplePatientsIdentifiedException;
import it.units.htl.dpacs.helpers.Anonymizer;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.helpers.CompressionSCP;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalSettings;
import it.units.htl.dpacs.helpers.GlobalSettings.PartitioningStrategy;
import it.units.htl.dpacs.helpers.ImageAvailabilityConfig;
import it.units.htl.dpacs.helpers.ImageMaskingSCP;
import it.units.htl.dpacs.helpers.IpChecker;
import it.units.htl.dpacs.helpers.MaskingParameter;
import it.units.htl.dpacs.helpers.RoiMeasure;
import it.units.htl.dpacs.helpers.StudyAvailabilityPojo;
import it.units.htl.dpacs.helpers.StudyTrackingSettings;
import it.units.htl.dpacs.postprocessing.verifier.bean.StudiesVerifierToolsLocal;
import it.units.htl.dpacs.statistics.Timer;
import it.units.htl.dpacs.statistics.TimerLogger;
import it.units.htl.dpacs.valueObjects.CodeSequence;
import it.units.htl.dpacs.valueObjects.DicomConstants;
import it.units.htl.dpacs.valueObjects.DicomMatch;
import it.units.htl.dpacs.valueObjects.Equipment;
import it.units.htl.dpacs.valueObjects.Image;
import it.units.htl.dpacs.valueObjects.Instance;
import it.units.htl.dpacs.valueObjects.InstancesAction;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che2.audit.message.ActiveParticipant;
import org.dcm4che2.audit.message.AuditEvent.ActionCode;
import org.dcm4che2.audit.message.AuditEvent.OutcomeIndicator;
import org.dcm4che2.audit.message.AuditSource;
import org.dcm4che2.audit.message.InstancesAccessedMessage;
import org.dcm4che2.audit.message.InstancesTransferredMessage;
import org.dcm4che2.audit.message.ParticipantObjectDescription;
import org.dcm4che2.data.Tag;

public class StorageServer extends DcmServiceBase implements AssociationListener {
    private static final int[] TYPE1_ATTR = {
            Tags.StudyInstanceUID,
            Tags.SeriesInstanceUID,
            Tags.SOPInstanceUID,
            Tags.SOPClassUID };
    static final Log log = LogFactory.getLog(StorageServer.class);
    private static final DcmObjectFactory objFact = DcmObjectFactory.getInstance();
    private static final DcmParserFactory parserFact = DcmParserFactory.getInstance();
    public static final String IDISSUER_O3DPACS = "O3-DPACS";
    private long CumulativeStudyOccupation = 0;
    public final StorageSCP scp;
    private DicomStorageDealerLocal bean = null;
    private String formerStudyUid = null;
    private String formerPatientPk = null;
    private String formerPatientId = null;
    private String formerSeriesUid = null;
    private Anonymizer anonimizer = null;
    private boolean trackStudyCompletion;
    private PartitioningStrategy partitioningStrategy;
    private StudyCompletionTimer sct = null;

    /**
     * As in any O3-DPACS server, the server (extended from dcm4che DcmServiceBase) in instantiated with its own Mbean
     * 
     * @param scp
     *            SCP Mbean keeping server settings
     */
    public StorageServer(StorageSCP scp) {
        this.anonimizer = new Anonymizer();
        this.scp = scp;
        trackStudyCompletion = StudyTrackingSettings.isStudyCompletionTrackingEnabled();
        partitioningStrategy = GlobalSettings.getPartitioningStrategy();
    }

    /**
     * This procedure is called every time a C_STORE DICOM message arrives to O3-DPACS
     * 
     * @param assoc
     *            the active association
     * @param rq
     *            the object containing COMMANDSET and DATASET for the current C_STORE message
     * @param rspCmd
     *            the commandSet to be filled as response
     * @exception IOException
     *                Description of the Exception
     * @exception DcmServiceException
     *                Description of the Exception
     */
    private TimerLogger tLogger = new TimerLogger();

    @SuppressWarnings("unused")
    protected void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws IOException, DcmServiceException {
        String defaultIdIssuer = scp.getDefaultIdIssuer();
        Timer storageTimer = new Timer();
        Timer verifyTimer = new Timer();
        Timer writeTimer = new Timer();
        storageTimer.start();
        boolean written = false;
        Command rqCmd = rq.getCommand();
        InputStream in = null;
        String instUID = null;
        DcmDecodeParam decParam = null;
        DcmEncodeParam encParam = null;
        Dataset ds = null;
        DcmParser parser = null;
        File file = null;
        if ((trackStudyCompletion) && (sct == null)) {
            try {
                ImageAvailabilityConfig iac = ImageAvailabilityConfig.getInstance();
                if (IpChecker.isContainedInMyip(iac.getRunFromIp())) {
                    sct = new StudyCompletionTimer(Integer.parseInt(scp.getMinutesAfterStudyConsideredFinished()));
                    java.util.Timer timer = new java.util.Timer();
                    timer.scheduleAtFixedRate(sct, 10, 1000 * Integer.parseInt(scp.getSecondsBetweenTimerChecks()));
                    log.info("Timer scheduled with params (minutesAfterStudyConsideredFinished,secondsBetweenTimerChecks)=" + scp.getMinutesAfterStudyConsideredFinished() + "," + scp.getSecondsBetweenTimerChecks());
                } else {
                    log.debug("Image availability is enabled but run on " + iac.getRunFromIp());
                }
            } catch (Exception ex) {
                log.fatal("Error starting Study Completion Timer", ex);
            }
        }
        // trackStudyCompletion=StudyTrackingSettings.isStudyCompletionTrackingEnabled();
        String callingAE = assoc.getAssociation().getCallingAET();
        String forcedTs = CompressionSCP.getCompressionTransferSyntax(callingAE);
        Vector<RoiMeasure> total = null;
        if (forcedTs != null) {
            log.debug(callingAE + ": image from this AE should be compressed to Trans. Syntax: " + forcedTs);
        } else {
            log.debug(callingAE + ": No ts forced");
        }
        it.units.htl.dpacs.valueObjects.Study st = new it.units.htl.dpacs.valueObjects.Study("");
        it.units.htl.dpacs.valueObjects.Patient pat = new it.units.htl.dpacs.valueObjects.Patient();
        long sz = 0;
        try {
            if (bean == null) {
                try {
                    bean = InitialContext.doLookup(BeansName.LDicomStorageDealer);
                } catch (NamingException nex) {
                    throw new DcmServiceException(Status.ProcessingFailure);
                }
            }
            instUID = rqCmd.getAffectedSOPInstanceUID();
            String classUID = rqCmd.getAffectedSOPClassUID();
            DicomDbDealer dbDealer = InitialContext.doLookup(BeansName.LDicomDbDealer);
            boolean aeTitleIsToBeVerified = dbDealer.isToBeVerified(callingAE);
            // WARN: O3-DPACS does not support multiple instances of the same DICOM UID. Newer are discarded.
            verifyTimer.start();
            int instanceIsPresent = 0;
            instanceIsPresent = bean.isInstancePresent(instUID, classUID, scp.getTable(classUID), assoc);
            if (instanceIsPresent == -1) {
                throw new CannotStoreException(CannotStoreException.INSTANCES_PROCESSED);
            } else if ((instanceIsPresent == 1) && !aeTitleIsToBeVerified) {
                throw new CannotStoreException(CannotStoreException.INSTANCES_PRESENT);
            }
            in = rq.getDataAsStream();
            // start parsing file to store
            sz = 0;
            decParam = DcmDecodeParam.valueOf(rq.getTransferSyntaxUID());
            encParam = DcmEncodeParam.valueOf(rq.getTransferSyntaxUID());
            // ------------
            log.debug(callingAE + ": your syntax is " + rq.getTransferSyntaxUID());
            ds = objFact.newDataset();
            parser = parserFact.newDcmParser(in);
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDataset(decParam, -1);
            sz = ds.calcLength(encParam);
            if (aeTitleIsToBeVerified) {
                // here we have to check if the study is verified or not and then deprecate it
                if (formerStudyUid == null || !formerStudyUid.equals(ds.getString(Tag.StudyInstanceUID))) {
                    StudiesVerifierToolsLocal stvBean = InitialContext.doLookup(BeansName.StudiesVerifierToolsL);
                    // if the study is published I've to deprecate it, otherwise nothing to do
                    if (stvBean.isStudyPublished(ds.getString(Tag.StudyInstanceUID))) {
                        log.info("The study is published, studyUID : " + ds.getString(Tag.StudyInstanceUID) + " . I've to deprecate to republish");
                        DeprecationRemote depBean = null;
//                        depBean = InitialContext.doLookup("o3-dpacs/DeprecationBean/remote");
                        depBean = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DeprecationBean!it.units.htl.dpacs.dao.DeprecationRemote");
                        String newUid = depBean.getNewDeprecationUid();
                        log.info("Deprecated " + ds.getString(Tag.StudyInstanceUID) + "?" + depBean.deprecateStudy(ds.getString(Tag.StudyInstanceUID), newUid, false, "Deprecated by verifier, have to republish", 1));
                    } else if (instanceIsPresent == 1) {
                        throw new CannotStoreException(CannotStoreException.INSTANCES_PRESENT);
                    }
                } else if (instanceIsPresent == 1) {
                    throw new CannotStoreException(CannotStoreException.INSTANCES_PRESENT);
                }
            }
            log.debug(callingAE + ": Checking Image Elaboration Flag...");
            /*
             * ImageElaboration flag is a tag related to the node, used to enable elaborations before the image is stored. Anonimization of US is an example, standardization of DTI storing is another.
             */
            boolean isImageMaskingEnabled = ImageMaskingSCP.isImageMaskingEnabled(callingAE);
            log.debug(callingAE + ": Image Elaboration Flag: " + isImageMaskingEnabled);
            if (isImageMaskingEnabled) {
                MaskingParameter[] maskParams = ImageMaskingSCP.getMaskTags(callingAE, ds.getString(Tags.Modality));
                if (maskParams != null) {
                    for (MaskingParameter param : maskParams) {
                        // Check first tag
                        if (ds.contains(param.getTagNumber()) && ds.getString(param.getTagNumber()).equals(param.getTagValue())) {
                            // Modality doesn't need to be checked, since it's the correct one or null
                            // Check second tag:
                            if ((param.getSecondTagNumber() == null) || ((param.getSecondTagValue() != null) && (param.getSecondTagValue().equals(ds.getString(param.getSecondTagNumber()))))) {
                                log.debug("Applying mask: " + param.getMaskCoordinates());
                                total = ImageMaskingSCP.parseMaskParameters(param.getMaskCoordinates());
                                break;
                            }
                        }
                    }
                    if (total == null)
                        log.warn("No mask to apply for " + callingAE + "," + ds.getString(Tags.Modality));
                }
            }
            CumulativeStudyOccupation = CumulativeStudyOccupation + sz;
            // for consistency
            checkDataset(ds, classUID, instUID);
            ds.setFileMetaInfo(objFact.newFileMetaInfo(classUID, instUID, rq.getTransferSyntaxUID()));
            it.units.htl.dpacs.valueObjects.Series se = new it.units.htl.dpacs.valueObjects.Series();
            it.units.htl.dpacs.valueObjects.Instance ins = null;
            DicomMatch ma = null;
            // Managment of anonymization
            // if the system is Partially Anonymized the entire anonimization (node dependat) is ignored
            boolean nodeAnonymization = Anonymizer.isAnonymized(callingAE);
            if (scp.isPartiallyAnonymized() && !nodeAnonymization) {
                ma = buildValueObjs(ds, pat, st, se, callingAE);
                ds = anonimizer.removeNameAndPatientId(ds);
            } else if (nodeAnonymization) {
                // if the system isn't partially anonymized and the node must be anonymized
                // the dataset will be anonymized and then the dicom match will be rewritten
                boolean removePatId = Anonymizer.hasToRemovePatientId(callingAE);
                ds = anonimizer.anonymize(ds, false, removePatId);
                ma = buildValueObjs(ds, pat, st, se, callingAE);
            } else {
                ma = buildValueObjs(ds, pat, st, se, callingAE);
            }
            if (ma == null) {
                throw new CannotStoreException(CannotStoreException.STUDY_NOTEDITABLE);
            }
            if (!bean.isStudyEditable(ma.study.getStudyInstanceUid(), callingAE)) {
                throw new CannotStoreException(CannotStoreException.STUDY_NOTEDITABLE);
            }
            if (bean.isStudyDeleted(ma.study.getStudyInstanceUid(), callingAE)) {
                throw new CannotStoreException(CannotStoreException.STUDY_DELETED);
            }
            /*
             * Some equipment use, to support versioning, to send a command set with a different SOP instance UID from Dataset. Here you check
             */
            log.debug(callingAE + ", comm inst: " + instUID + " - dataset uid" + ma.instance.getSopInstanceUid());
            if (!instUID.equals(ma.instance.getSopInstanceUid())) {
                log.info(callingAE + ": comm inst: " + instUID + " - dataset uid: " + ma.instance.getSopInstanceUid() + " DO NOT MATCH...trying to change");
                try {
                    ma.instance.setSopInstanceUid(instUID); // If sopInstanceUID is different from that in the Command (affectedSOPInstanceUID), it is overwritten by the latter
                    ds.putUI(Tags.SOPInstanceUID, instUID);
                    log.info("CHANGED");
                } catch (Exception e) {
                    log.error("Instance severe: can't change uid", e);
                    throw new CannotStoreException(CannotStoreException.INSTANCES_PRESENT);
                }
                log.debug(callingAE + ": instance uid set to command one");
            }
            pat = ma.patient;
            st = ma.study;
            se = ma.series;
            ins = ma.instance;
            ma = null;
            // This code is to deal with the same data in an association:
            if (formerSeriesUid != null) {
                // Then this is not the first CStore in the Association
                if (formerSeriesUid.equals(se.getSeriesInstanceUid())) {
                    log.info(callingAE + ": " + "Continuing previous Series");
                    se.setToPerform(DicomConstants.UPDATE);
                    st.setToPerform(DicomConstants.UPDATE);
                    pat.setToPerform(DicomConstants.UPDATE);
                    pat.setPrimaryKey(formerPatientPk);
                } else {
                    // If the series was not the same, maybe the study is!
                    if (formerStudyUid != null) {
                        // log.info("Preceding Study not null");
                        if (formerStudyUid.equals(st.getStudyInstanceUid())) {
                            log.info(callingAE + ": " + "Continuing Previous Study");
                            st.setToPerform(DicomConstants.UPDATE);
                            pat.setToPerform(DicomConstants.UPDATE);
                            pat.setPrimaryKey(formerPatientPk);
                        }
                    }
                }
            }
            st.setStudySize(Long.toString(sz));
            se.setAETitle(assoc.getAssociation().getCallingAET());
            String[] params = {
                    scp.getTable(ins.getSopClassUid()),
                    scp.getStorage(se.getAETitle()),
                    scp.getEquipment(se.getAETitle()) };
            // Check on which table you're going to save image infos
            if (pat.getPatientId() == null) {
                pat.setPatientId(st.getStudyInstanceUid());
                pat.setIdIssuer(IDISSUER_O3DPACS);
            }
            if (partitioningStrategy.equals(PartitioningStrategy.CALLED)) {
                pat.setIdIssuer(assoc.getAssociation().getCalledAET());
            } else if (partitioningStrategy.equals(PartitioningStrategy.CALLING)) {
                pat.setIdIssuer(assoc.getAssociation().getCallingAET());
            }
            String basePath = bean.verifyToStore(pat, st, se, ins, params, scp, assoc, defaultIdIssuer, partitioningStrategy);
            formerStudyUid = st.getStudyInstanceUid();
            formerPatientId = pat.getPatientId();
            formerPatientPk = pat.getPrimaryKey();
            formerSeriesUid = se.getSeriesInstanceUid();
            // log.info(callingAE + ": " + "Storing " + formerStudyUid + ", former Patient's Primary Key set to: " + pat.getPrimaryKey());
            // these should raise a dcmserviceException, when store to disk does not go well.....
            boolean isImage = "Images".equalsIgnoreCase(params[0]);
            log.debug(callingAE + ": the file is of type " + params[0] + "and isImage is " + isImage);
            // the main store method performing the store to disk
            verifyTimer.stop();
            writeTimer.start();
            file = bean.storeToMedia(ds, st, basePath, parser, (DcmEncodeParam) decParam, scp, forcedTs, isImage, assoc, total, params[1]);
            if (!bean.updateForwardSchedule(ds.getString(Tags.StudyInstanceUID), assoc.getAssociation().getCallingAET()))
                log.warn("NOT ALL INFO ABOUT FORWARD HAVE BEEN UPDATED!");
            writeTimer.stop();
            written = true;
            log.debug(callingAE + "instance uid: " + ins.getSopInstanceUid());
            // the method should be done only if the store is well done, to take into account the new image
            int num = bean.insertInstance(pat, st, se, ins, assoc);
            final Patient p = pat;
            final Study s = st;
            try {
                InstancesAccessedMessage msg = new InstancesAccessedMessage(ActionCode.CREATE);
                msg.addUserPerson(assoc.getAssociation().getCallingAET(), "", assoc.getAssociation().getCallingAET(), assoc.getAssociation().getSocket().getInetAddress().toString(), true);
                msg.addUserPerson(assoc.getAssociation().getCalledAET(), "", assoc.getAssociation().getCalledAET(), assoc.getAssociation().getSocket().getLocalAddress().toString(), false);
                msg.addPatient(p.getPatientId(), p.getFirstName() + " " + p.getLastName());
                msg.addStudy(s.getStudyInstanceUid(), null);
                AuditLogService als = AuditLogService.getInstance();
                als.SendMessage(msg);
            } catch (Exception e) {
                log.warn("Unable to send AuditLogMessage", e);
            }
            storageTimer.stop();
            BigDecimal size = new BigDecimal(sz * 0.0009765625);
            size = size.setScale(2, BigDecimal.ROUND_HALF_UP);
            tLogger.toLog(new String[] {
                    tLogger.STORE_INST,
                    callingAE,
                    verifyTimer.toString(),
                    writeTimer.toString(),
                    st.getStudyInstanceUid(),
                    se.getSeriesInstanceUid(),
                    instUID,
                    size + "" });
            log.debug(callingAE + ": Instance elaborated in: " + storageTimer.getMeasure());
            if (aeTitleIsToBeVerified)
                storeToVerifyTable(assoc.getAssociation().getCallingAET(), st.getStudyInstanceUid());
            // Update the studyAvailabilityStatus
            ImageAvailabilityConfig iac = ImageAvailabilityConfig.getInstance();
            if (ImageAvailabilityConfig.PUBLISHTO_HL7.equals(iac.getPublicationMethod())) {
                bean.updateStudiesAvailability(st.getStudyInstanceUid());
            }
            rspCmd.putUS(Tags.Status, Status.Success);
        } catch (MultiplePatientsIdentifiedException mpiex) {
            log.error(callingAE + ": " + mpiex.getMessage(), mpiex);
            throw new DcmServiceException(Status.ProcessingFailure, mpiex.getMessage());
        } catch (BlockingException ee) {
            if (ee instanceof it.units.htl.dpacs.exceptions.IncorrectPatientIdException) {
                log.warn("" + ee.getMessage());
            } else {
                log.error("", ee);
            }
            throw new DcmServiceException(Status.ProcessingFailure);
        } catch (Exception e) {
            if (e instanceof CannotStoreException) {
                if (e.getMessage().equals(CannotStoreException.INSTANCES_PRESENT)) {
                    written = true; // can ignore the dump in any case
                    log.warn(callingAE + ": " + ": received an instance which is already stored, CAN'T STORE");
                    file = null;
                    // if you are at this point you have to choose if you should store the DUPS or not
                    if (scp.getSuccessOnDup()) {
                        formerStudyUid = null;
                        formerSeriesUid = null;
                    } else {
                        throw new DcmServiceException(Status.DuplicateSOPInstance, e);
                    }
                } else if (e.getMessage().equals(CannotStoreException.STUDY_OF_ANOTHER_PAT)) {
                    log.error(callingAE + ": The study is associated to another patient, study is:  " + st.getStudyInstanceUid() + ", patId: " + pat.getPatientId());
                    throw new DcmServiceException(Status.ProcessingFailure);
                } else if (e.getMessage().equals(CannotStoreException.PATIENT_INFO_FOR_SAME_PATIENT_ID)) {
                    log.error(callingAE + ": " + CannotStoreException.PATIENT_INFO_FOR_SAME_PATIENT_ID + ": Verify couple idIssuer/idPatient");
                    throw new DcmServiceException(Status.ProcessingFailure);
                } else if (e.getMessage().equals(CannotStoreException.CANNOT_ADD_TO_STUDY)) {
                    log.error(callingAE + ": Cannot add instance to study.");
                    throw new DcmServiceException(Status.ProcessingFailure);
                } else if (e.getMessage().equals(CannotStoreException.STUDY_DELETED)) {
                    log.error(callingAE + ": Study has already been deleted, probably after a forward");
                    throw new DcmServiceException(Status.ProcessingFailure, e);
                } else if (e.getMessage().equals(CannotStoreException.STUDY_NOTEDITABLE)) {
                    log.error(callingAE + ": " + "Study is marked as not editable");
                    throw new DcmServiceException(Status.ProcessingFailure, e);
                } else if (e.getMessage().equals(CannotStoreException.CANNOT_ADD_STUDY)) {
                    log.error(callingAE + ": " + CannotStoreException.CANNOT_ADD_STUDY);
                    throw new DcmServiceException(Status.ProcessingFailure, e);
                } else if (e.getMessage().equals(CannotStoreException.NO_PATIENT)) {
                    log.error(callingAE + ": " + CannotStoreException.NO_PATIENT);
                    throw new DcmServiceException(Status.ProcessingFailure, e);
                } else if (e.getMessage().equals(CannotStoreException.INSTANCES_PROCESSED)) {
                    log.error(callingAE + ": " + CannotStoreException.INSTANCES_PROCESSED);
                    throw new DcmServiceException(Status.ProcessingFailure, e);
                } else {
                    log.error(callingAE, e);
                }
            } else {
                log.debug(callingAE + " here you DUMP a dataset due to an exception");
                if (!scp.getUrlToDups().equals("_")) {
                    if (!written) {
                        dump(in, e, callingAE);
                    }
                }
                log.fatal(callingAE + ": severe while processing the request.", e);
                throw new DcmServiceException(Status.ProcessingFailure, e);
            }
        } finally {
            if (bean != null) {
                bean.stopProcessingInstance(instUID);
            } else {
                log.warn("Could not stop processing " + instUID);
            }
            ds = null;
            assoc = null;
            rqCmd = null;
            callingAE = null;
            instUID = null;
            decParam = null;
            encParam = null;
            parser = null;
            file = null;
        }
    }

    /**
     * This method extracts from the dataset the information to be put in the value objects for elaboration throu O3-DPACS
     * 
     * @param ds
     *            the dataset from which you take the informations
     * @param p
     *            the patient VO
     * @param st
     *            the study VO
     * @param se
     *            the series VO
     * @param callingAE
     *            The Calling AE for current operation
     * @return A DcmMatch Object containg the array of filled VOs
     */
    @SuppressWarnings("unused")
    public DicomMatch buildValueObjs(Dataset ds, Patient p, Study st, Series se, String callingAE) {
        String patName = ds.getString(Tags.PatientName);
        log.info(callingAE + ": Patient Name: " + patName + "...Instance: " + ds.getString(Tags.SOPInstanceUID));
        int nextCaret = 0;
        if ((patName == null) || (patName.equals(""))) {
            p.setLastName(DicomConstants.JOHN_DOE);
        } else {
            String[] patientName = patName.split("(\\^)");
            if (patientName.length > 0) {
                for (int i = 0; i < patientName.length; i++) {
                    if (!patientName[i].equals("")) {
                        switch (i) {
                        case 0:
                            p.setLastName(patientName[i]);
                            break;
                        case 1:
                            p.setFirstName(patientName[i]);
                            break;
                        case 2:
                            p.setMiddleName(patientName[i]);
                            break;
                        case 3:
                            p.setSuffix(patientName[i]);
                            break;
                        case 4:
                            p.setPrefix(patientName[i]);
                            break;
                        }
                    } else {
                        if (i == 0) {
                            p.setLastName(DicomConstants.JOHN_DOE);
                        }
                    }
                }
            }
        }
        java.util.Date td = new java.util.Date();
        td = ds.getDate(Tags.PatientBirthDate);
        if (td != null) {
            p.setBirthDate(new java.sql.Date(td.getTime()));
        }
        td = ds.getDate(Tags.PatientBirthTime);
        if (td != null) {
            p.setBirthTime(new java.sql.Time(td.getTime()));
        }
        p.setSex(ds.getString(Tags.PatientSex));
        p.setPatientId(ds.getString(Tags.PatientID));
        p.setIdIssuer(ds.getString(Tags.IssuerOfPatientID));
        p.setPatientIdentifierList(p.getPatientId());
        p.setEthnicGroup(ds.getString(Tags.EthnicGroup));
        p.setPatientAddress(ds.getString(Tags.PatientAddress));
        p.setPatientComments(ds.getString(Tags.PatientComments));
        p.setPatientState(ds.getString(Tags.PatientState));
        p.setPregnancyStatus(ds.getString(Tags.PregnancyStatus));
        p.setMedicalAlerts(ds.getString(Tags.MedicalAlerts));
        p.setPatientWeight(ds.getString(Tags.PatientWeight));
        p.setConfidentialityConstraint(ds.getString(Tags.ConfidentialityPatientData));
        p.setSpecialNeeds(ds.getString(Tags.SpecialNeeds));
        st.setStudyInstanceUid(ds.getString(Tags.StudyInstanceUID));
        if (p.getPatientId() == null) {
            String newId = ((formerPatientId != null) && (st.getStudyInstanceUid().equals(formerStudyUid))) ? formerPatientId : st.getStudyInstanceUid();
            // String newId = ((formerPatientId != null) && (st.getStudyInstanceUid().equals(formerStudyUid))) ? formerPatientId : "DPACS" + System.currentTimeMillis();
            p.setPatientId(newId);
            p.setPatientIdentifierList(newId);
            p.setIdIssuer(IDISSUER_O3DPACS);
        }
        if (p.getIdIssuer() == null) {
            p.setIdIssuer(scp.getDefaultIdIssuer());
        }
        st.setStudyId(ds.getString(Tags.StudyID));
        st.setStudyStatusId(ds.getString(Tags.StudyStatusID));
        td = ds.getDate(Tags.StudyDate);
        if (td != null) {
            st.setStudyDate(new java.sql.Date(td.getTime()));
        }
        td = ds.getDate(Tags.StudyTime);
        if (td != null) {
            st.setStudyTime(new java.sql.Time(td.getTime()));
        }
        td = ds.getDate(Tags.StudyCompletionDate);
        if (td != null) {
            st.setStudyCompletionDate(new java.sql.Date(td.getTime()));
        }
        td = ds.getDate(Tags.StudyCompletionTime);
        if (td != null) {
            st.setStudyCompletionTime(new java.sql.Time(td.getTime()));
        }
        td = ds.getDate(Tags.StudyVerifiedDate);
        if (td != null) {
            st.setStudyVerifiedDate(new java.sql.Date(td.getTime()));
        }
        td = ds.getDate(Tags.StudyVerifiedTime);
        if (td != null) {
            st.setStudyVerifiedTime(new java.sql.Time(td.getTime()));
        }
        st.setAccessionNumber(ds.getString(Tags.AccessionNumber));
        st.setStudyDescription(ds.getString(Tags.StudyDescription));
        // String[] codSeq=(ds.getString(Tags.ProcedureCodeSeq)).split(); // DTODO
        Dataset dspcs = ds.getItem(Tags.ProcedureCodeSeq);
        if (dspcs != null) {
            CodeSequence cs = new CodeSequence(dspcs.getString(Tags.CodeValue), dspcs.getString(Tags.CodingSchemeDesignator), dspcs.getString(Tags.CodingSchemeVersion), dspcs.getString(Tags.CodeMeaning));
            st.setProcedureCodeSequence(cs);
        }
        st.setReferringPhysiciansName(ds.getString(Tags.ReferringPhysicianName));
        st.setAdmittingDiagnosesDescription(ds.getString(Tags.AdmittingDiagnosisDescription));
        String[] names = ds.getStrings(Tags.NameOfPhysicianReadingStudy);
        PersonalName pn = null;
        if (names != null) {
            for (int k = names.length - 1; k >= 0; k--) {
                pn = new PersonalName();
                if ((names[k] == null) || (names[k].equals(""))) {
                    pn.setLastName(DicomConstants.JOHN_DOE);
                } else {
                    if (names[k].indexOf('^') != -1) {
                        // At least the first two fields are specified
                        pn.setLastName(names[k].substring(0, names[k].indexOf('^')));
                        nextCaret = names[k].indexOf('^', names[k].indexOf('^') + 1); // this is the position of the second ^
                        if (nextCaret != -1) {
                            // At least the first three fields are specified
                            pn.setFirstName(names[k].substring(names[k].indexOf('^') + 1, nextCaret));
                            if (names[k].indexOf('^', nextCaret + 1) != -1) {
                                // At least the first four fields are specified
                                pn.setMiddleName(names[k].substring(nextCaret + 1, names[k].indexOf('^', nextCaret + 1)));
                                if (names[k].lastIndexOf('^') > names[k].indexOf('^', nextCaret + 1)) {
                                    pn.setPrefix(names[k].substring(names[k].indexOf('^', nextCaret + 1) + 1, names[k].lastIndexOf('^')));
                                    pn.setSuffix(names[k].substring(names[k].lastIndexOf('^')));
                                } else {
                                    pn.setPrefix(names[k].substring(names[k].indexOf('^', nextCaret + 1) + 1));
                                }
                            } else {
                                pn.setMiddleName(names[k].substring(nextCaret + 1));
                            }
                        } else {
                            // Last and First Name specified
                            pn.setFirstName(names[k].substring(names[k].indexOf('^') + 1));
                        }
                    } else {
                        // Just one field specified
                        pn.setLastName(names[k]);
                    }
                }
                st.addNameOfPhysicianReadingStudy(pn);
            } // end for
        }
        se.setSeriesInstanceUid(ds.getString(Tags.SeriesInstanceUID));
        se.setSeriesNumber(ds.getString(Tags.SeriesNumber));
        se.setModality(ds.getString(Tags.Modality));
        se.setBodyPartExamined(ds.getString(Tags.BodyPartExamined));
        se.setSeriesDescription(ds.getString(Tags.SeriesDescription));
        if ((ds.contains(Tag.OperatorsName)) && (ds.getStrings(Tag.OperatorsName).length > 0)) {
            se.setOperatorsName(ds.getStrings(Tag.OperatorsName)[0]);
        }
        // ----------------------------------------------
        Equipment e = new Equipment();
        e.setManufacturer(ds.getString(Tags.Manufacturer));
        e.setInstitutionName(ds.getString(Tags.InstitutionName));
        e.setStationName(ds.getString(Tags.StationName));
        e.setInstitutionalDepartmentName(ds.getString(Tags.InstitutionalDepartmentName));
        e.setManufacturersModelName(ds.getString(Tags.ManufacturerModelName));
        e.setDeviceSerialNumber(ds.getString(Tags.DeviceSerialNumber));
        td = ds.getDate(Tags.DateOfLastCalibration);
        if (td != null) {
            e.setDateOfLastCalibration(new java.sql.Date(td.getTime()));
        }
        td = ds.getDate(Tags.TimeOfLastCalibration);
        if (td != null) {
            e.setTimeOfLastCalibration(new java.sql.Time(td.getTime()));
        }
        e.setConversionType(ds.getString(Tags.ConversionType));
        e.setSecondaryCaptureDeviceId(ds.getString(Tags.SecondaryCaptureDeviceID));
        se.setEquipment(e);
        // ---
        Instance i = null;
        String sopType = scp.getTable(ds.getString(Tags.SOPClassUID)); // The name of the appropriate table is returned
        if (sopType.equals("Images")) {
            i = new Image();
            ((Image) i).setSamplesPerPixel(ds.getString(Tags.SamplesPerPixel));
            ((Image) i).setRows(ds.getString(Tags.Rows));
            ((Image) i).setColumns(ds.getString(Tags.Columns));
            ((Image) i).setBitsAllocated(ds.getString(Tags.BitsAllocated));
            ((Image) i).setBitsStored(ds.getString(Tags.BitsStored));
            ((Image) i).setHighBit(ds.getString(Tags.HighBit));
            ((Image) i).setPixelRepresentation(ds.getString(Tags.PixelRepresentation));
            ((Image) i).setNumberOfFrames(ds.getInteger(Tags.NumberOfFrames));
        } else if (sopType.equals("Overlays")) {
            i = new Overlay();
            ((Overlay) i).setOverlayNumber(ds.getString(Tags.OverlayNumber));
            ((Overlay) i).setOverlayRows(ds.getString(Tags.OverlayRows));
            ((Overlay) i).setOverlayColumns(ds.getString(Tags.OverlayColumns));
            ((Overlay) i).setOverlayType(ds.getString(Tags.OverlayType));
            ((Overlay) i).setOverlayBitsAllocated(ds.getString(Tags.OverlayBitsAllocated));
            DcmElement refIms = ds.get(Tags.RefImageSeq); // Deal with the referenced images!!!
            if (refIms != null) {
                RefToInstances rti = new RefToInstances();
                for (int ic = refIms.countItems() - 1; ic >= 0; ic--) {
                    Dataset img = refIms.getItem(ic);
                    rti.addInstance(img.getString(Tags.RefSOPInstanceUID), img.getString(Tags.RefSOPClassUID));
                }
                ((Overlay) i).setReferencedInstances(rti);
            }
            // Over dealing with referenced images
        } else if (sopType.equals("PresStates")) {
            i = new PresState();
            ((PresState) i).setPresentationLabel(ds.getString(Tags.ContentLabel));
            ((PresState) i).setPresentationDescription(ds.getString(Tags.ContentDescription));
            td = ds.getDate(Tags.PresentationCreationDate);
            if (td != null) {
                ((PresState) i).setPresentationCreationDate(new java.sql.Date(td.getTime()));
            }
            td = ds.getDate(Tags.PresentationCreationTime);
            if (td != null) {
                ((PresState) i).setPresentationCreationTime(new java.sql.Time(td.getTime()));
            }
            ((PresState) i).setPresentationCreatorsName(ds.getString(Tags.PresentationCreatorName));
            ((PresState) i).setRecommendedViewingMode(ds.getString(Tags.RecommendedViewingMode));
            DcmElement refSer = ds.get(Tags.RefSeriesSeq); // Deal with referenced series!!!
            if (refSer != null) {
                // All subsequent sequences are compulsory if this element's present!
                for (int sc = refSer.countItems() - 1; sc >= 0; sc--) {
                    RefToInstances rti = new RefToInstances();
                    Dataset series = refSer.getItem(sc); // Get the series one by one
                    Dataset series1 = refSer.getItem(sc + 1);
                    rti.setSeries(series.getString(Tags.SeriesInstanceUID));
                    DcmElement refIms = series.get(Tags.RefImageSeq);
                    for (int ic = refIms.countItems() - 1; ic >= 0; ic--) {
                        // Get info about images, one by one!
                        Dataset img = refIms.getItem(ic);
                        rti.addInstance(img.getString(Tags.RefSOPInstanceUID), img.getString(Tags.RefSOPClassUID));
                    }
                    ((PresState) i).addReferencedSeries(rti);
                } // end for through series
            }
        } else if (sopType.equals("StructReps")) {
            i = new StructRep();
            ((StructRep) i).setCompletionFlag(ds.getString(Tags.CompletionFlag));
            ((StructRep) i).setVerificationFlag(ds.getString(Tags.VerificationFlag));
            td = ds.getDate(Tags.ContentDate);
            if (td != null) {
                ((StructRep) i).setContentDate(new java.sql.Date(td.getTime()));
            }
            td = ds.getDate(Tags.ContentTime);
            if (td != null) {
                ((StructRep) i).setContentTime(new java.sql.Time(td.getTime()));
            }
            td = ds.getDate(Tags.ObservationDateTime);
            if (td != null) {
                ((StructRep) i).setObservationDateTime(new java.sql.Timestamp(td.getTime()));
            }
            Dataset cncs = ds.getItem(Tags.ConceptNameCodeSeq);
            if (cncs != null) {
                CodeSequence cs = new CodeSequence(cncs.getString(Tags.CodeValue), cncs.getString(Tags.CodingSchemeDesignator), cncs.getString(Tags.CodingSchemeVersion), cncs.getString(Tags.CodeMeaning));
                ((StructRep) i).setConceptNameCodeSequence(cs);
            }
        } else if (sopType.equals("KeyObjects")) {
            i = new KeyObject();
            td = ds.getDate(Tags.ContentDate);
            if (td != null) {
                ((KeyObject) i).setContentDate(new java.sql.Date(td.getTime()));
            }
            td = ds.getDate(Tags.ContentTime);
            if (td != null) {
                ((KeyObject) i).setContentTime(new java.sql.Time(td.getTime()));
            }
            td = ds.getDate(Tags.ObservationDateTime);
            if (td != null) {
                ((KeyObject) i).setObservationDateTime(new java.sql.Timestamp(td.getTime()));
            }
            Dataset cncs = ds.getItem(Tags.ConceptNameCodeSeq);
            if (cncs != null) {
                CodeSequence cs = new CodeSequence(cncs.getString(Tags.CodeValue), cncs.getString(Tags.CodingSchemeDesignator), cncs.getString(Tags.CodingSchemeVersion), cncs.getString(Tags.CodeMeaning));
                ((KeyObject) i).setConceptNameCodeSequence(cs);
            }
            Dataset crpes = ds.getItem(Tags.CurrentRequestedProcedureEvidenceSeq);
            if (crpes != null) {

            	DcmElement refSer = crpes.get(Tags.RefSeriesSeq); // Deal with referenced series!!!
                if (refSer != null) {
                    // All subsequent sequences are compulsory if this element's present!
                    for (int sc = refSer.countItems() - 1; sc >= 0; sc--) {
                        RefToInstances rti = new RefToInstances();
                        Dataset series = refSer.getItem(sc); // Get the series one by one
                        Dataset series1 = refSer.getItem(sc + 1);
                        rti.setSeries(series.getString(Tags.SeriesInstanceUID));
                        DcmElement refIms = series.get(Tags.RefSOPSeq);
                        for (int ic = refIms.countItems() - 1; ic >= 0; ic--) {
                            // Get info about images, one by one!
                            Dataset img = refIms.getItem(ic);
                            rti.addInstance(img.getString(Tags.RefSOPInstanceUID), img.getString(Tags.RefSOPClassUID));
                        }
                        ((KeyObject) i).addReferencedSeries(rti);
                    } // end for through series
                }
            }
        } else {
            i = new NonImage();
        }
        i.setSopInstanceUid(ds.getString(Tags.SOPInstanceUID));
        i.setSopClassUid(ds.getString(Tags.SOPClassUID));
        i.setInstanceNumber(ds.getString(Tags.InstanceNumber));
        DicomMatch temp = new DicomMatch();
        temp.patient = p;
        temp.study = st;
        temp.series = se;
        temp.instance = i;
        return temp;
    }

    private void storeToVerifyTable(String aeTitle, String studyUID) {
        if (!bean.insertStudyVerificationData(aeTitle, studyUID)) {
            log.error(aeTitle + ": Unable to insert study: " + studyUID + " in studiesToVerify.");
        }
    }

    /**
     * This check the consistency of the dataset
     * 
     * @param ds
     *            the dataset to be checked
     * @param classUID
     *            the Class UID
     * @param instUID
     *            the instance UID
     * @throws org.dcm4che.net.DcmServiceException
     */
    private void checkDataset(Dataset ds, String classUID, String instUID) throws DcmServiceException {
        for (int i = 0; i < TYPE1_ATTR.length; ++i) {
            if (ds.vm(TYPE1_ATTR[i]) <= 0) {
                throw new DcmServiceException(Status.DataSetDoesNotMatchSOPClassError, "Missing Type 1 Attribute " + Tags.toString(TYPE1_ATTR[i]));
            }
            if (!instUID.equals(ds.getString(Tags.SOPInstanceUID))) {
                throw new DcmServiceException(Status.DataSetDoesNotMatchSOPClassError, "SOP Instance UID in Dataset differs from Affected SOP Instance UID");
            }
            if (!classUID.equals(ds.getString(Tags.SOPClassUID))) {
                throw new DcmServiceException(Status.DataSetDoesNotMatchSOPClassError, "SOP Class UID in Dataset differs from Affected SOP Class UID");
            }
        }
    }

    // AssociationListener implementation ----------------------------
    /**
     * Ovverride of event closed association
     * 
     * @param src
     *            The closed association
     */
    public void closed(Association src) {
        logInstancesStored(src);
    }

    /**
     * Ovverride of event closing association
     * 
     * @param src
     *            the closing association
     */
    public void closing(Association src) {
        formerStudyUid = null;
        formerPatientId = null;
        formerPatientPk = null;
        formerSeriesUid = null;
        // Tidy up previous data
    }

    /**
     * Ovverride of event error association
     * 
     * @param src
     *            the current association
     * @param ioe
     *            Description of the Parameter
     */
    public void error(Association src, IOException ioe) {
    }

    /**
     * Ovverride of event error association
     * 
     * @param src
     *            the current association
     * @param dimse
     */
    public void received(Association src, Dimse dimse) {
    }

    /**
     * Description of the Method
     * 
     * @param src
     *            Description of the Parameter
     * @param pdu
     *            Description of the Parameter
     */
    public void received(Association src, PDU pdu) {
    }

    /**
     * Description of the Method
     * 
     * @param src
     *            Description of the Parameter
     * @param pdu
     *            Description of the Parameter
     */
    public void write(Association src, PDU pdu) {
    }

    /**
     * Override
     * 
     * @param src
     * @param dimse
     */
    public void write(Association src, Dimse dimse) {
    }

    /**
     * Saves the dataset in a file
     * 
     * @param is
     *            InputStream to the dataset
     * @param ex
     * @param callingAE
     *            The calling AET
     */
    private void dump(InputStream is, Exception ex, String callingAE) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(scp.getDumpDirectory() + "/" + (ex.getClass().toString().substring(ex.getClass().toString().lastIndexOf(".") + 1)) + System.currentTimeMillis() + ".dump");
            int c;
            byte[] buffer = new byte[512];
            while ((c = is.read(buffer)) != -1) {
                fos.write(buffer, 0, c);
            }
            log.debug(callingAE + ": Dumped a Dataset!");
            log.debug(callingAE + ": Correctly Dumped one Dataset!");
            fos.close();
        } catch (IOException ioex) {
            log.warn(callingAE + ": Unable to dump a Dataset!!!", ioex);
            // log.fatal("Unable to dump a Dataset!!!");
        }
    }

    /**
     * Method to log and take account of the list of instances stored in one association. This is a key for logging and XDS-I image sharing, for automatic submission to XDS repository
     * 
     * @param assoc
     *            the current association
     * @param ds
     *            the dataset which is being stored
     */
    public void logInstancesStored(Association assoc, Dataset ds) {
        try {
            InstancesAction stored = (InstancesAction) assoc.getProperty("InstancesStored");
            if (stored != null) {
                for (int i = 0; i < stored.listStudyInstanceUIDs().length; i++) {
                    log.debug(stored.listStudyInstanceUIDs()[i]);
                }
            }
            String suid = ds.getString(Tags.StudyInstanceUID);
            if (stored != null && !stored.listStudyInstanceUIDs()[0].equals(suid)) {
                log.debug("About to audit stored images");
                logInstancesStored(assoc);
                stored = null;
            }
            if (stored == null) {
                log.debug("creating new action");
                stored = new InstancesAction("Create", suid, new Patient(ds.getString(Tags.PatientID), ds.getString(Tags.PatientName)));
                // , ds.getString(Tags.IssuerOfPatientID), ds.getString(Tags.PatientBirthDate), ds.getString(Tags.PatientAddress), ds.getString(Tags.PatientSex)));
                stored.setAccessionNumber(ds.getString(Tags.AccessionNumber));
                // adding XDS content
                stored.setStudyDate(ds.getString(Tags.StudyDate));
                stored.setStudyTime(ds.getString(Tags.StudyTime));
                stored.setXDSDocumentEntryAuthorPerson(ds.getString(Tags.ReferringPhysicianName));
                stored.setAEtitle(assoc.getCalledAET());
                // ------------------------------------------
                assoc.putProperty("InstancesStored", stored);
            }
            stored.incNumberOfInstances(1);
            stored.addSOPClassUID(ds.getString(Tags.SOPClassUID));
            stored.addSeries(ds.getString(Tags.SeriesInstanceUID));
            stored.addInstances(ds.getString(Tags.SOPInstanceUID));
            stored = null;
        } catch (Exception e) {
            log.fatal("Could not audit log InstancesStored:", e);
        }
    }

    /**
     * Description of the Method
     * 
     * @param assoc
     *            Description of the Parameter
     */
    void logInstancesStored(Association assoc) {
        InstancesAction stored = (InstancesAction) assoc.getProperty("InstancesStored");
        if (stored != null) {
            try {
                InstancesTransferredMessage msg = new InstancesTransferredMessage((ActionCode.CREATE));
                msg.setOutcomeIndicator(OutcomeIndicator.SUCCESS);
                msg.addActiveParticipant(ActiveParticipant.createActiveNode(assoc.getCallingAET(), true));
                msg.addActiveParticipant(ActiveParticipant.createActiveNode(assoc.getCalledAET(), false));
                msg.addAuditSource(new AuditSource(assoc.getCalledAET()));
                if ((stored.getPatient().getPatientId() != null) && (stored.getPatient().getLastName() != null))
                    msg.addPatient(stored.getPatient().getPatientId(), stored.getPatient().getLastName() + ", " + stored.getPatient().getFirstName());
                else
                    log.warn("can't add patient in audit log message for instances stored");
                msg.addStudy(stored.listStudyInstanceUIDs()[0], new ParticipantObjectDescription().addStudy("date: " + stored.getStudyDate() + " of " + stored.getNumberOfInstances() + " images."));
                if (AuditLogService.getInstance() != null)
                    AuditLogService.getInstance().SendMessage(msg);
                else
                    log.warn("Writer for auditLog is null, can't audit for instances stored");
            } catch (Exception el) {
                log.error("", el);
            }
        } else {
            log.debug("No instances to log for");
        }
        assoc.putProperty("InstancesStored", null);
        stored = null;
    }

    private class StudyCompletionTimer extends TimerTask {
        private int toleranceInMinutes;

        public StudyCompletionTimer(int toleranceInMinutes) {
            this.toleranceInMinutes = toleranceInMinutes;
        }

        public void run() {
            ImageAvailabilityConfig iac = ImageAvailabilityConfig.getInstance();
            if (!ImageAvailabilityConfig.PUBLISHTO_HL7.equals(iac.getPublicationMethod())) {
                try {
                    if (bean == null) {
                        try {
                            bean = InitialContext.doLookup(BeansName.LDicomStorageDealer);
                        } catch (NamingException nex) {
                            log.error("Error accessing Data Access Layer", nex);
                        }
                    }
                    long completedOnSeconds = bean.completeOldStudies(ConfigurationSettings.STUDYCLOSE_STORAGE, toleranceInMinutes);
                    // If completedOnSeconds >0 some studies have been closed: those must be published, if the system is configured properly
                    if (completedOnSeconds == 0)
                        log.debug("No studies closed");
                    else {
                        log.info("Completed Studies: " + completedOnSeconds);
                        if (iac.isEnabled() && ImageAvailabilityConfig.PUBLISHTO_TABLE.equals(iac.getPublicationMethod())) {
                            ImageAvailabilityLocal imAvailBean = null;
                            try {
                                imAvailBean = InitialContext.doLookup(BeansName.LImageAvailability);
                                imAvailBean.insertCorrectStudies(completedOnSeconds, ImageAvailabilityConfig.RECONCILIATIONSOURCE_WORKLIST, iac.getStringForSetting(), iac.getTargetApp());
                            } catch (NamingException nex) {
                                log.error("Error accessing Data Access Layer: " + BeansName.LImageAvailability, nex);
                            } catch (Exception ex) {
                                log.error("An error occurred publishing studies", ex);
                            }
                        }
                    }
                } catch (Exception ex) {
                }
            } else {
                try {
                    Hl7PublisherLocal hlp = InitialContext.doLookup(BeansName.LHl7Publisher);
                    ArrayList<StudyAvailabilityPojo> studyList = hlp.getCompletedStudies(toleranceInMinutes);
                    for (StudyAvailabilityPojo study : studyList) {
                        String sms = hlp.generateO01Notify(study);
                        if (sms != null) {
                            if (hlp.insertHl7Notification(sms, study)) {
                                hlp.updateStudyAvailability(study, System.currentTimeMillis());
                                log.info("Completed " + study.studyUID);
                            } else {
                                log.error("Unable to insert hl7 notification for study " + study.studyUID);
                            }
                        } else {
                            log.error("Unable to get the hl7 message for : " + study.studyUID);
                        }
                    }
                } catch (Exception e) {
                    log.fatal("Something goes wrong during study availabiilty notification", e);
                } catch (Throwable tex) {
                    log.fatal("Something goes wrong during study availabiilty notification", tex);
                }
            }
        }
    }
}