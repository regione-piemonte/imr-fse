/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.queryRetrieve;

import it.units.htl.dpacs.dao.DicomQueryDealerLocal;
import it.units.htl.dpacs.helpers.Anonymizer;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.helpers.GlobalSettings;
import it.units.htl.dpacs.helpers.GlobalSettings.PartitioningStrategy;
import it.units.htl.dpacs.statistics.Timer;
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

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmService;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che2.data.Tag;

public class FindServer /* extends */implements DcmService/* Base */{
    public static final int SUCCESS = 0x0000;
    public static final int PENDING = 0xFF00;
    public static final int CANCEL = 0xFE00;
    // out of resourcing
    public static final int FAILURE = 0xA700;
    // unable to process
    public static final int TTOWEAK = 0xC001;
    private final QueryRetrieveSCP qryRetrSCP;
    private DicomQueryDealerLocal bean = null;
    private Anonymizer anonimizator = new Anonymizer();
    private static final DcmObjectFactory objFact = DcmObjectFactory.getInstance();
    private static final AssociationFactory fact = AssociationFactory.getInstance();
    static final Log log = LogFactory.getLog(FindServer.class);
    private boolean _isNodeAnonimized = false;
    private boolean removePatId = false;
    private PartitioningStrategy partitioningStrategy;

    public FindServer(QueryRetrieveSCP scp) {
        qryRetrSCP = scp;
        partitioningStrategy=GlobalSettings.getPartitioningStrategy();
    }

    public void c_find(ActiveAssociation assoc, Dimse rq) throws IOException {
        boolean depEnabled;
        String callingAE = assoc.getAssociation().getCallingAET();
        String calledAE = assoc.getAssociation().getCalledAET();
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initCFindRSP(rqCmd.getMessageID(), rqCmd.getAffectedSOPClassUID(), PENDING);
        try {
            String level = rqCmd.getAffectedSOPClassUID();
            int lev = 0;
            // Now decide the information and query levels
            if (level == null)
                throw new DcmServiceException(Status.UnrecognizedOperation, "No Query Level Specified!!");
            // Read out dataset that contains
            Dataset ds = rq.getDataset();
            _isNodeAnonimized = Anonymizer.isAnonymized(callingAE);
            removePatId=Anonymizer.hasToRemovePatientId(callingAE);
            
            MultiDimseRsp mdr = new FindMultiDimseRsp(rq.getDataset(), assoc.getAssociation().getCalledAET(), assoc.getAssociation().getCallingAET(), _isNodeAnonimized, removePatId);
            assoc.addCancelListener(rspCmd.getMessageIDToBeingRespondedTo(), mdr.getCancelListener());
            checkFilters(ds, callingAE);
            // log.debug(callingAE + ": FindServer, General Anonymization enabled is " + qryRetrSCP.getanonimizedActivated());
            if (_isNodeAnonimized) {
                log.debug(callingAE + ": FindServer, General Anonymization enabled");
                
                ds = anonimizator.anonymize(ds, true, removePatId);
            }
            depEnabled = qryRetrSCP.getDepartmentEnabled();
            if (level.equals(UIDs.PatientRootQueryRetrieveInformationModelFIND)) {
                log.debug(callingAE + ": FindServer: PatientRoot");
                if ("PATIENT".equals(ds.getString(Tags.QueryRetrieveLevel)))
                    lev = 11;
                else if ("STUDY".equals(ds.getString(Tags.QueryRetrieveLevel)))
                    lev = 12;
                else if ("SERIES".equals(ds.getString(Tags.QueryRetrieveLevel)))
                    lev = 13;
                else if ("IMAGE".equals(ds.getString(Tags.QueryRetrieveLevel)))
                    lev = 14;
                else
                    throw new DcmServiceException(Status.UnrecognizedOperation, "Invalid QueryRetrieve Level: " + ds.getString(Tags.QueryRetrieveLevel));
            } else if (level.equals(UIDs.StudyRootQueryRetrieveInformationModelFIND)) {
                log.debug(callingAE + ": FindServer: StudyRoot");
                if ("STUDY".equals(ds.getString(Tags.QueryRetrieveLevel)))
                    lev = 22;
                else if ("SERIES".equals(ds.getString(Tags.QueryRetrieveLevel)))
                    lev = 23;
                else if ("IMAGE".equals(ds.getString(Tags.QueryRetrieveLevel)))
                    lev = 24;
                else
                    throw new DcmServiceException(Status.UnrecognizedOperation, "Invalid QueryRetrieve Level: " + ds.getString(Tags.QueryRetrieveLevel));
            } else if (level.equals(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND)) {
                log.debug(callingAE + ": FindServer: PatientStudyOnly");
                if ("PATIENT".equals(ds.getString(Tags.QueryRetrieveLevel)))
                    lev = 31;
                else if ("STUDY".equals(ds.getString(Tags.QueryRetrieveLevel)))
                    lev = 32;
                else
                    throw new DcmServiceException(Status.UnrecognizedOperation, "Invalid QueryRetrieve Level: " + ds.getString(Tags.QueryRetrieveLevel));
            }
            DicomMatch reqMatch = null;
            log.debug(callingAE + ": FindServer dealing with a request from " + assoc.getAssociation().getCallingAET());
            try {
                reqMatch = buildRequest(ds, callingAE, calledAE);
            } catch (Exception e) {
                log.error("", e);
                throw new DcmServiceException(Status.UnableToProcess, e);
            }
            // patFK=mdr.getLastReadPatPk();
            // lastStudy=mdr.getLastReadStudyUid();
            // numOfPatRelStudies = mdr.getLastPatientsNumOfStudies();
            ((FindMultiDimseRsp) mdr).performQuery(reqMatch, lev, qryRetrSCP.getQueryLimit(), depEnabled);
            writeAvailableDatasets(assoc, rq, rspCmd, mdr, callingAE);
            mdr = null;
            reqMatch = null;
            level = null;
            ds = null;
        } catch (DcmServiceException e) {
            e.writeTo(rspCmd);
            Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
            assoc.getAssociation().write(rsp);
            doAfterRsp(assoc, rsp);
        }
        rqCmd = null;
        rspCmd = null;
    }

    private boolean checkFilters(Dataset ds, String callingAE) throws DcmServiceException {
        if ((!ds.contains(Tag.PatientName) || (ds.getString(Tag.PatientName) == null))
                && (!ds.contains(Tag.PatientID) || (ds.getString(Tag.PatientID) == null))
                && (!ds.contains(Tag.StudyDate) || (ds.getString(Tag.StudyDate) == null))
                && (!ds.contains(Tag.AccessionNumber) || (ds.getString(Tag.AccessionNumber) == null))
                && ((!ds.contains(Tag.StudyInstanceUID) || ds.getString(Tag.StudyInstanceUID) == null))) {
            log.warn(callingAE + ": Too weak filters, use at least PatientName OR PatientID OR StudyDate OR AccessionNumber OR StudyInstanceUID");
            throw new DcmServiceException(Status.NoSuchArgument, "Too weak filters, use at least PatientName OR PatientID OR StudyDate OR AccessionNumber OR StudyInstanceUID");
        } else {
            return true;
        }
    }

    // Inner Class:
    private class FindMultiDimseRsp implements MultiDimseRsp, DimseListener {
        boolean anonimized = false;
        boolean removePatId = false;
        // private boolean depEnabled;
        boolean cancel = false;
        private Dataset ds = null;
        private int current = 0; // This represents the position of the next
        // Match to provide!!! It gets decreased!!!
        private DicomMatch[] dcmMatch = null;
        private String myAET = null;
        private String callingAE = null;
        private int lastPatRelSt = -1;

        public FindMultiDimseRsp(Dataset ds, String calledAET, String callingAET, boolean anonimized, boolean removePatId) throws DcmValueException {
            this.callingAE = callingAET;
            this.anonimized = anonimized;
            this.removePatId=removePatId;
            this.ds = ds;
            this.myAET = calledAET;
        }

        public void performQuery(DicomMatch reqMatch, int level, int limit, boolean depEnabled) {
            Timer qryCrono = new Timer();
            qryCrono.restart();
            if (bean == null){
                try {
                    bean = InitialContext.doLookup(BeansName.LQueryDealer);
                } catch (NamingException nex) {
                    log.error("Couldn't create bean.", nex);
                }
            }
            String qryLevel = callingAE + ": query level is: " + level;
            switch (level) {
            case 11:
                log.info(qryLevel + " PatientRoot/Patient.");
                dcmMatch = bean.patientRootMatch(reqMatch.patient, limit, this.callingAE);
                break;
            case 12:
                log.info(qryLevel + " PatientRoot/Study.");
                dcmMatch = bean.patientRootMatch(reqMatch.patient, reqMatch.study, limit, this.callingAE, depEnabled);
                break;
            case 13:
                log.info(qryLevel + " PatientRoot/Series.");
                dcmMatch = bean.patientRootMatch(reqMatch.patient, reqMatch.study, reqMatch.series, this.callingAE);
                break;
            case 14:
                log.info(qryLevel + " PatientRoot/Instance.");
                dcmMatch = bean.queryInstanceLevel(reqMatch.patient, reqMatch.study, reqMatch.series, reqMatch.instance, this.callingAE);
                break;
            case 22:
                log.info(qryLevel + " StudyRoot/Study.");
                dcmMatch = bean.studyRootMatch(reqMatch.patient, reqMatch.study, limit, this.callingAE, depEnabled);
                break;
            case 23:
                log.info(qryLevel + " StudyRoot/Series.");
                dcmMatch = bean.studyRootMatch(reqMatch.patient, reqMatch.study, reqMatch.series, this.callingAE);
                break;
            case 24:
                log.info(qryLevel + " StudyRoot/Instance.");
                dcmMatch = bean.queryInstanceLevel(reqMatch.patient, reqMatch.study, reqMatch.series, reqMatch.instance, this.callingAE);
                break;
            default:
                log.info(qryLevel + " ");
                dcmMatch = null;
                break;
            }
            if (dcmMatch != null) {
                current = dcmMatch.length - 1;
            } else {
                current = 0;
            }
            qryCrono.stop();
            log.info(this.callingAE + ": find and build dicomMatch " + current + " matches in " + qryCrono.getMeasure() + "ms....now sending...");
        }

        public int getLastPatientsNumOfStudies() {
            return lastPatRelSt;
        }

        public void dimseReceived(Association assoc, Dimse dimse) {
            dimse.getCommand().putUS(Tags.Status, Status.Cancel);
            log.info(callingAE + ": cancel received from Find Server.....");
            cancel = true;
        }

        public DimseListener getCancelListener() {
            return this;
        }

        public Dataset next(ActiveAssociation assoc, Dimse rq, Command rspCmd) {
            // cancel received
            if (cancel) {
                current = -1;
                dcmMatch = null;
            }
            Dataset rds = giveCurrentMatchAsDataset();// (Dataset)args[0];
            log.debug(callingAE + ": Anonymization for " + callingAE + " is " + anonimized);
            if (anonimized) {
                rds = anonimizator.anonymize(rds, false, removePatId);
            }
            return rds;// ret;
        }

        public boolean hasNext() {
            return (current > -1);
        }

        /** Description of the Method */
        public void release() {
            ds = null;
            current = 0;
            dcmMatch = null;
        }

        private Dataset giveCurrentMatchAsDataset() {
            // Timer builderCrono = new Timer();
            // builderCrono.restart();
            if (current < 0 || dcmMatch == null) {
                return null;
            }
            int cursor = dcmMatch.length - current - 1;
            if (dcmMatch[cursor] == null) {
                // This should be the only case when the last result has been
                // retrieved
                current = -1; // This should stop the loop which writes the
                // Datasets!
                return null;
            }
            if (dcmMatch[cursor].patient != null)
                log.debug(callingAE + ": BUILDING ONE DATASET: " + current + " " + dcmMatch[cursor].patient.getLastName());
            Dataset newData = objFact.newDataset();
            newData.putCS(Tags.QueryRetrieveLevel, ds.getString(Tags.QueryRetrieveLevel));
            if (dcmMatch[cursor].instance != null) {
                if (dcmMatch[cursor].instance instanceof Image) {
                    if (ds.contains(Tags.SamplesPerPixel))
                        newData.putUS(Tags.SamplesPerPixel, ((Image) dcmMatch[cursor].instance).getSamplesPerPixel());
                    if (ds.contains(Tags.Rows))
                        newData.putUS(Tags.Rows, ((Image) dcmMatch[cursor].instance).getRows());
                    if (ds.contains(Tags.Columns))
                        newData.putUS(Tags.Columns, ((Image) dcmMatch[cursor].instance).getColumns());
                    if (ds.contains(Tags.BitsAllocated))
                        newData.putUS(Tags.BitsAllocated, ((Image) dcmMatch[cursor].instance).getBitsAllocated());
                    if (ds.contains(Tags.BitsStored))
                        newData.putUS(Tags.BitsStored, ((Image) dcmMatch[cursor].instance).getBitsStored());
                    if (ds.contains(Tags.HighBit))
                        newData.putUS(Tags.HighBit, ((Image) dcmMatch[cursor].instance).getHighBit());
                    if (ds.contains(Tags.PixelRepresentation))
                        newData.putUS(Tags.PixelRepresentation, ((Image) dcmMatch[cursor].instance).getPixelRepresentation());
                    if (((Image) dcmMatch[cursor].instance).getNumberOfFrames()!=null)
                    	newData.putIS(Tags.NumberOfFrames, ((Image) dcmMatch[cursor].instance).getNumberOfFrames());
                } else if (dcmMatch[cursor].instance instanceof PresState) {
                    if (ds.contains(Tags.ContentLabel))
                        newData.putCS(Tags.ContentLabel, ((PresState) dcmMatch[cursor].instance).getPresentationLabel());
                    if (ds.contains(Tags.ContentDescription))
                        newData.putLO(Tags.ContentDescription, ((PresState) dcmMatch[cursor].instance).getPresentationDescription());
                    if (ds.contains(Tags.PresentationCreationDate))
                        newData.putDA(Tags.PresentationCreationDate, ((PresState) dcmMatch[cursor].instance).getPresentationCreationDate());
                    if (ds.contains(Tags.PresentationCreationTime))
                        newData.putTM(Tags.PresentationCreationTime, ((PresState) dcmMatch[cursor].instance).getPresentationCreationTime());
                    if (ds.contains(Tags.PresentationCreatorName))
                        newData.putPN(Tags.PresentationCreatorName, ((PresState) dcmMatch[cursor].instance).getPresentationCreatorsName());
                    if (ds.contains(Tags.RecommendedViewingMode))
                        newData.putCS(Tags.RecommendedViewingMode, ((PresState) dcmMatch[cursor].instance).getRecommendedViewingMode());
                } else if (dcmMatch[cursor].instance instanceof StructRep) {
                    if (ds.contains(Tags.CompletionFlag))
                        newData.putCS(Tags.CompletionFlag, ((StructRep) dcmMatch[cursor].instance).getCompletionFlag());
                    if (ds.contains(Tags.VerificationFlag))
                        newData.putCS(Tags.VerificationFlag, ((StructRep) dcmMatch[cursor].instance).getVerificationFlag());
                    if (ds.contains(Tags.ContentDate))
                        newData.putDA(Tags.ContentDate, ((StructRep) dcmMatch[cursor].instance).getContentDate());
                    if (ds.contains(Tags.ContentTime))
                        newData.putTM(Tags.ContentTime, ((StructRep) dcmMatch[cursor].instance).getContentTime());
                    if (ds.contains(Tags.ObservationDateTime))
                        newData.putDT(Tags.ObservationDateTime, ((StructRep) dcmMatch[cursor].instance).getObservationDateTime());
                    if ((ds.contains(Tags.ConceptNameCodeSeq)) && (((StructRep) dcmMatch[cursor].instance).getConceptNameCodeSequence() != null)) {
                        log.debug(callingAE + ": DATASET CONTAINS!!!");
                        Dataset item = newData.putSQ(Tags.ConceptNameCodeSeq).addNewItem();
                        item.putSH(Tags.CodeValue, ((StructRep) dcmMatch[cursor].instance).getConceptNameCodeSequence().getCodeValue());
                        log.debug(callingAE + ": codevalue: " + ((StructRep) dcmMatch[cursor].instance).getConceptNameCodeSequence().getCodeValue());
                        item.putSH(Tags.CodingSchemeDesignator, ((StructRep) dcmMatch[cursor].instance).getConceptNameCodeSequence().getCodingSchemeDesignator());
                        item.putSH(Tags.CodingSchemeVersion, ((StructRep) dcmMatch[cursor].instance).getConceptNameCodeSequence().getCodingSchemeVersion());
                        item.putLO(Tags.CodeMeaning, ((StructRep) dcmMatch[cursor].instance).getConceptNameCodeSequence().getCodeMeaning());
                    }
                } else if (dcmMatch[cursor].instance instanceof Overlay) {
                    if (ds.contains(Tags.OverlayNumber))
                        newData.putIS(Tags.OverlayNumber, ((Overlay) dcmMatch[cursor].instance).getOverlayNumber());
                    if (ds.contains(Tags.OverlayRows))
                        newData.putUS(Tags.OverlayRows, ((Overlay) dcmMatch[cursor].instance).getOverlayRows());
                    if (ds.contains(Tags.OverlayColumns))
                        newData.putUS(Tags.OverlayColumns, ((Overlay) dcmMatch[cursor].instance).getOverlayColumns());
                    if (ds.contains(Tags.OverlayBitsAllocated))
                        newData.putUS(Tags.OverlayBitsAllocated, ((Overlay) dcmMatch[cursor].instance).getOverlayBitsAllocated());
                    if (ds.contains(Tags.OverlayType))
                        newData.putCS(Tags.OverlayType, ((Overlay) dcmMatch[cursor].instance).getOverlayType());
                } // end if...else
                  // This part is common to all instances:
                if (ds.contains(Tags.SOPClassUID)) {
                    newData.putUI(Tags.SOPClassUID, dcmMatch[cursor].instance.getSopClassUid());
                }
                newData.putUI(Tags.SOPInstanceUID, dcmMatch[cursor].instance.getSopInstanceUid());
                newData.putIS(Tags.InstanceNumber, dcmMatch[cursor].instance.getInstanceNumber());
            }
            if (dcmMatch[cursor].series != null) {
                newData.putUI(Tags.SeriesInstanceUID, dcmMatch[cursor].series.getSeriesInstanceUid());
                newData.putIS(Tags.SeriesNumber, dcmMatch[cursor].series.getSeriesNumber());
                newData.putCS(Tags.Modality, dcmMatch[cursor].series.getModality());
                if (ds.contains(Tags.BodyPartExamined))
                    newData.putCS(Tags.BodyPartExamined, dcmMatch[cursor].series.getBodyPartExamined());
                if (ds.contains(Tags.NumberOfSeriesRelatedInstances))
                    newData.putIS(Tags.NumberOfSeriesRelatedInstances, dcmMatch[cursor].series.getNumberOfSeriesRelatedInstances());
                if (ds.contains(Tags.SeriesDescription))
                    newData.putLO(Tags.SeriesDescription, dcmMatch[cursor].series.getSeriesDescription());
                if(ds.contains(Tags.OperatorName)){
                    newData.putPN(Tag.OperatorsName, dcmMatch[cursor].series.getOperatorsName());
                }
                if (dcmMatch[cursor].series.getEquipment() != null) {
                    if (ds.contains(Tags.Manufacturer))
                        newData.putLO(Tags.Manufacturer, dcmMatch[cursor].series.getEquipment().getManufacturer());
                    if (ds.contains(Tags.InstitutionName))
                        newData.putLO(Tags.InstitutionName, dcmMatch[cursor].series.getEquipment().getInstitutionName());
                    if (ds.contains(Tags.StationName))
                        newData.putSH(Tags.StationName, dcmMatch[cursor].series.getEquipment().getStationName());
                    if (ds.contains(Tags.InstitutionalDepartmentName))
                        newData.putLO(Tags.InstitutionalDepartmentName, dcmMatch[cursor].series.getEquipment().getInstitutionalDepartmentName());
                    if (ds.contains(Tags.ManufacturerModelName))
                        newData.putLO(Tags.ManufacturerModelName, dcmMatch[cursor].series.getEquipment().getManufacturersModelName());
                    if (ds.contains(Tags.DeviceSerialNumber))
                        newData.putLO(Tags.DeviceSerialNumber, dcmMatch[cursor].series.getEquipment().getDeviceSerialNumber());
                    if (ds.contains(Tags.DateOfLastCalibration))
                        newData.putDA(Tags.DateOfLastCalibration, dcmMatch[cursor].series.getEquipment().getDateOfLastCalibration());
                    if (ds.contains(Tags.TimeOfLastCalibration))
                        newData.putTM(Tags.TimeOfLastCalibration, dcmMatch[cursor].series.getEquipment().getTimeOfLastCalibration());
                    if (ds.contains(Tags.ConversionType))
                        newData.putCS(Tags.ConversionType, dcmMatch[cursor].series.getEquipment().getConversionType());
                    if (ds.contains(Tags.SecondaryCaptureDeviceID))
                        newData.putLO(Tags.SecondaryCaptureDeviceID, dcmMatch[cursor].series.getEquipment().getSecondaryCaptureDeviceId());
                }
            }
            if (dcmMatch[cursor].study != null) {
                newData.putUI(Tags.StudyInstanceUID, dcmMatch[cursor].study.getStudyInstanceUid());
                newData.putSH(Tags.StudyID, dcmMatch[cursor].study.getStudyId());
                if(dcmMatch[cursor].study.getSpecificCharacterSet()!=null)
                	newData.putCS(Tags.SpecificCharacterSet, dcmMatch[cursor].study.getSpecificCharacterSet());
                if (ds.contains(Tags.StudyStatusID))
                    newData.putCS(Tags.StudyStatusID, dcmMatch[cursor].study.getStudyStatusId());
                newData.putDA(Tags.StudyDate, dcmMatch[cursor].study.getStudyDate());
                newData.putTM(Tags.StudyTime, dcmMatch[cursor].study.getStudyTime());
                if (ds.contains(Tags.StudyCompletionDate))
                    newData.putDA(Tags.StudyCompletionDate, dcmMatch[cursor].study.getStudyCompletionDate());
                if (ds.contains(Tags.StudyCompletionTime))
                    newData.putTM(Tags.StudyCompletionTime, dcmMatch[cursor].study.getStudyCompletionTime());
                if (ds.contains(Tags.StudyVerifiedDate))
                    newData.putDA(Tags.StudyVerifiedDate, dcmMatch[cursor].study.getStudyVerifiedDate());
                if (ds.contains(Tags.StudyVerifiedTime))
                    newData.putTM(Tags.StudyVerifiedTime, dcmMatch[cursor].study.getStudyVerifiedTime());
                newData.putSH(Tags.AccessionNumber, dcmMatch[cursor].study.getAccessionNumber());
                if (ds.contains(Tags.StudyDescription))
                    newData.putLO(Tags.StudyDescription, dcmMatch[cursor].study.getStudyDescription());
                if (ds.contains(Tags.ReferringPhysicianName))
                    newData.putPN(Tags.ReferringPhysicianName, dcmMatch[cursor].study.getReferringPhysiciansName());
                if (ds.contains(Tags.AdmittingDiagnosisDescription))
                    newData.putLO(Tags.AdmittingDiagnosisDescription, dcmMatch[cursor].study.getAdmittingDiagnosesDescription());
                if (ds.contains(Tags.NumberOfStudyRelatedSeries))
                    newData.putIS(Tags.NumberOfStudyRelatedSeries, dcmMatch[cursor].study.getNumberOfStudyRelatedSeries());
                log.debug(callingAE + ": STUDYRELATEDSERIES: " + newData.getString(Tags.NumberOfStudyRelatedSeries));
                if (ds.contains(Tags.NumberOfStudyRelatedInstances))
                    newData.putIS(Tags.NumberOfStudyRelatedInstances, dcmMatch[cursor].study.getNumberOfStudyRelatedInstances());
                if (ds.contains(Tags.ModalitiesInStudy))
                    newData.putCS(Tags.ModalitiesInStudy, dcmMatch[cursor].study.getModalitiesInStudy());
                if ((ds.contains(Tags.ProcedureCodeSeq)) && (dcmMatch[cursor].study.getProcedureCodeSequence() != null)) {
                    Dataset item = ds.putSQ(Tags.ProcedureCodeSeq).addNewItem();
                    item.putSH(Tags.CodeValue, dcmMatch[cursor].study.getProcedureCodeSequence().getCodeValue());
                    item.putSH(Tags.CodingSchemeDesignator, dcmMatch[cursor].study.getProcedureCodeSequence().getCodingSchemeDesignator());
                    item.putSH(Tags.CodingSchemeVersion, dcmMatch[cursor].study.getProcedureCodeSequence().getCodingSchemeVersion());
                    item.putLO(Tags.CodeMeaning, dcmMatch[cursor].study.getProcedureCodeSequence().getCodeMeaning());
                }
                if (ds.contains(Tags.NameOfPhysicianReadingStudy)) {
                    PersonalName[] temp = dcmMatch[cursor].study.getNamesOfPhysiciansReadingStudy();
                    String[] values = null;
                    if (temp != null) {
                        int cnt = temp.length;
                        values = new String[temp.length];
                        cnt--;
                        while (cnt >= 0) {
                            StringBuffer n = new StringBuffer(64);
                            n.append((temp[cnt].getLastName() == null) ? "" : temp[cnt].getLastName());
                            n.append("^");
                            n.append((temp[cnt].getFirstName() == null) ? "" : temp[cnt].getFirstName());
                            n.append("^");
                            n.append((temp[cnt].getMiddleName() == null) ? "" : temp[cnt].getMiddleName());
                            n.append("^");
                            n.append((temp[cnt].getPrefix() == null) ? "" : temp[cnt].getPrefix());
                            n.append("^");
                            n.append((temp[cnt].getSuffix() == null) ? "" : temp[cnt].getSuffix());
                            values[cnt] = n.toString();
                            cnt--;
                        } // end while
                    } // end if
                    newData.putPN(Tags.NameOfPhysicianReadingStudy, values);
                }
            }
            if (dcmMatch[cursor].patient != null) {
                if (ds.contains(Tags.IssuerOfPatientID))
                    newData.putLO(Tags.IssuerOfPatientID, dcmMatch[cursor].patient.getIdIssuer());
                newData.putLO(Tags.PatientID, dcmMatch[cursor].patient.getPatientId());
                StringBuffer n = new StringBuffer(64);
                n.append((dcmMatch[cursor].patient.getLastName() == null) ? "" : dcmMatch[cursor].patient.getLastName());
                n.append("^");
                n.append((dcmMatch[cursor].patient.getFirstName() == null) ? "" : dcmMatch[cursor].patient.getFirstName());
                n.append("^");
                n.append((dcmMatch[cursor].patient.getMiddleName() == null) ? "" : dcmMatch[cursor].patient.getMiddleName());
                n.append("^");
                n.append((dcmMatch[cursor].patient.getPrefix() == null) ? "" : dcmMatch[cursor].patient.getPrefix());
                n.append("^");
                n.append((dcmMatch[cursor].patient.getSuffix() == null) ? "" : dcmMatch[cursor].patient.getSuffix());
                newData.putPN(Tags.PatientName, n.toString());
                if (ds.contains(Tags.PatientBirthDate))
                    newData.putDA(Tags.PatientBirthDate, dcmMatch[cursor].patient.getBirthDate());
                if (ds.contains(Tags.PatientBirthTime))
                    newData.putTM(Tags.PatientBirthTime, dcmMatch[cursor].patient.getBirthTime());
                if (ds.contains(Tags.PatientSex))
                    newData.putCS(Tags.PatientSex, dcmMatch[cursor].patient.getSex());
                if (ds.contains(Tags.EthnicGroup))
                    newData.putSH(Tags.EthnicGroup, dcmMatch[cursor].patient.getEthnicGroup());
                if (ds.contains(Tags.PatientAddress))
                    newData.putLO(Tags.PatientAddress, dcmMatch[cursor].patient.getPatientAddress());
                if (ds.contains(Tags.PatientComments))
                    newData.putLT(Tags.PatientComments, dcmMatch[cursor].patient.getPatientComments());
                if (ds.contains(Tags.PatientState))
                    newData.putLO(Tags.PatientState, dcmMatch[cursor].patient.getPatientState());
                if (ds.contains(Tags.PregnancyStatus))
                    newData.putUS(Tags.PregnancyStatus, dcmMatch[cursor].patient.getPregnancyStatus());
                if (ds.contains(Tags.MedicalAlerts))
                    newData.putLO(Tags.MedicalAlerts, dcmMatch[cursor].patient.getMedicalAlerts());
                if (ds.contains(Tags.PatientWeight))
                    newData.putXX(Tags.PatientWeight, org.dcm4che.dict.VRs.DS, dcmMatch[cursor].patient.getPatientWeight());
                if (ds.contains(Tags.ConfidentialityPatientData))
                    newData.putLO(Tags.ConfidentialityPatientData, dcmMatch[cursor].patient.getConfidentialityConstraint());
                if (ds.contains(Tags.SpecialNeeds))
                    newData.putLO(Tags.SpecialNeeds, dcmMatch[cursor].patient.getSpecialNeeds());
                if (ds.contains(Tags.NumberOfPatientRelatedStudies))
                    newData.putIS(Tags.NumberOfPatientRelatedStudies, dcmMatch[cursor].patient.getNumberOfPatientRelatedStudies());
                if (ds.contains(Tags.NumberOfPatientRelatedSeries))
                    newData.putIS(Tags.NumberOfPatientRelatedSeries, dcmMatch[cursor].patient.getNumberOfPatientRelatedSeries());
                if (ds.contains(Tags.NumberOfPatientRelatedInstances))
                    newData.putIS(Tags.NumberOfPatientRelatedInstances, dcmMatch[cursor].patient.getNumberOfPatientRelatedInstances());
            }
            if ((dcmMatch[cursor].patient != null) && (dcmMatch[cursor].study == null)) { // PATIENT
                // LEVEL
                newData.putCS(Tags.InstanceAvailability, Study.DICOM_AVAILABILITY_ONLINE);
                newData.putAE(Tags.RetrieveAET, myAET);
            } else if (/* (dcmMatch[cursor].patient!=null)&& */(dcmMatch[cursor].study != null) && (dcmMatch[cursor].study.getStudyStatus() != null)) {
                switch (dcmMatch[cursor].study.getStudyStatus().charAt(0)) {
                case Study.DPACS_OPEN_STATUS:
                    newData.putCS(Tags.InstanceAvailability, Study.DICOM_AVAILABILITY_ONLINE);
                    newData.putAE(Tags.RetrieveAET, myAET);
                    break;
                case Study.DPACS_NEARLINE_STATUS:
                    newData.putCS(Tags.InstanceAvailability, Study.DICOM_AVAILABILITY_NEARLINE);
                    newData.putAE(Tags.RetrieveAET, myAET);
                    break;
                default:
                    newData.putCS(Tags.InstanceAvailability, Study.DICOM_AVAILABILITY_OFFLINE);
                    newData.putAE(Tags.RetrieveAET, myAET);
                    newData
                            .putSH(Tags.StorageMediaFileSetID, (dcmMatch[cursor].study.getFastestAccess() == null) ? dcmMatch[cursor].study.getStudyStatus() : dcmMatch[cursor].study
                                    .getFastestAccess());
                    newData.putUI(Tags.StorageMediaFileSetUID, (dcmMatch[cursor].study.getFastestAccess() == null) ? dcmMatch[cursor].study.getStudyStatus() : dcmMatch[cursor].study
                            .getFastestAccess());
                    break;
                }
            } else { // THIS SHOULD NEVER OCCUR
                newData.putCS(Tags.InstanceAvailability, Study.DICOM_AVAILABILITY_OFFLINE);
                newData.putAE(Tags.RetrieveAET, myAET);
            }
            log.debug(callingAE + ": \nCurrent=" + current + "\tCursor: " + cursor + "\nStudyInstanceUID: " + newData.getString(Tags.StudyInstanceUID) + "\n");
            current--;
            return newData;
        } // end giveCurrentMatchAsDataset()
    }

    public void c_store(ActiveAssociation assoc, Dimse rq) throws IOException {
    }

    public void c_get(ActiveAssociation assoc, Dimse rq) throws IOException {
    }

    public void c_move(ActiveAssociation assoc, Dimse rq) throws IOException {
    }

    public void c_echo(ActiveAssociation assoc, Dimse rq) throws IOException {
        Command rqCmd = rq.getCommand();
        Command rspCmd = objFact.newCommand();
        rspCmd.initCEchoRSP(rqCmd.getMessageID(), rqCmd.getAffectedSOPClassUID(), SUCCESS);
        doCEcho(assoc, rq, rspCmd);
        Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
        assoc.getAssociation().write(rsp);
        doAfterRsp(assoc, rsp);
    }

    public void n_create(ActiveAssociation assoc, Dimse rq) throws IOException {
    }

    public void n_set(ActiveAssociation assoc, Dimse rq) throws IOException {
    }

    public void n_get(ActiveAssociation assoc, Dimse rq) throws IOException {
    }

    public void n_delete(ActiveAssociation assoc, Dimse rq) throws IOException {
    }

    public void n_action(ActiveAssociation assoc, Dimse rq) throws IOException {
    }

    public void n_event_report(ActiveAssociation assoc, Dimse rq) throws IOException {
    }

    private void doCEcho(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws IOException {
        rspCmd.putUS(Tags.Status, SUCCESS);
    }

    private void doAfterRsp(ActiveAssociation assoc, Dimse rsp) {
    }

    private void writeAvailableDatasets(ActiveAssociation assoc, Dimse rq, Command rspCmd, MultiDimseRsp mdr, String callingAE) throws IOException, DcmServiceException {
        try {
            do {
                if (((FindMultiDimseRsp) mdr).cancel) {
                    rspCmd.putUS(Tags.Status, Status.Cancel);
                    Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
                    assoc.getAssociation().write(rsp);
                    log.info(callingAE + ": Received a CANCEL request and set the status!!!");
                    break;
                }
                Dataset rspData = mdr.next(assoc, rq, rspCmd);
                if (rspData == null) {
                    rspCmd.putUS(Tags.Status, Status.Success);
                    log.debug(callingAE + ": Received NULL dataset, status=" + rspCmd.getString(Tags.Status));
                    log.debug(callingAE + ": FindServer sent last result");
                } else {
                    log.debug(callingAE + ": Processing a request, another one should follow!");
                    if (_isNodeAnonimized) {
                        anonimizator.anonymize(rspData, false,removePatId);
                    }
                    rspCmd.putUS(Tags.Status, Status.Pending);
                } // end if...else
                Dimse rsp = fact.newDimse(rq.pcid(), rspCmd, rspData);
                assoc.getAssociation().write(rsp);
            } while (mdr.hasNext());
        } finally {
            mdr.release();
        }
    }

    // Inner classes -------------------------------------------------
    public static interface MultiDimseRsp {
        DimseListener getCancelListener();

        public Dataset next(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws DcmServiceException;

        public boolean hasNext();

        public int getLastPatientsNumOfStudies();

        void release();
    }

    // Private FindServer methods:
    private DicomMatch buildRequest(Dataset d, String callingAE, String calledAE) {
        // Check to see whether Study, Series or Instance Data have been filled,
        // otherwise leave the object null!!!!
        // I have to pick all data from the dataset and store 'em in the
        // ValueObjects!
        Patient p = null;
        Study st = null;
        Series se = null;
        Instance i = null;
        // Patient Data:
        if (d.contains(Tags.IssuerOfPatientID)) {
            if (p == null)
                p = new Patient();
            p.setIdIssuer(d.getString(Tags.IssuerOfPatientID));
        }
        if (d.contains(Tags.PatientID)) {
            if (p == null)
                p = new Patient();
            String tpid = d.getString(Tags.PatientID);
            if ((tpid != null) && (!DicomConstants.UNIVERSAL_MATCHING.equals(tpid)) && (!DicomConstants.MULTIPLE_WC_MATCHING.equals(tpid))) {
                log.debug("tpid _" + tpid + "_");
                log.debug(tpid.length());
                if (tpid.length() > 0) {
                    log.debug("" + (byte) tpid.charAt(0));
                }
                p.setPatientId(tpid);
            } else {
                p.setPatientId(null);
            }
        }
        if (d.contains(Tags.PatientName)) {
            if (p == null)
                p = new Patient();
            String patName = d.getString(Tags.PatientName);
            log.debug(callingAE + ": Personal Name: " + patName);
            int nextCaret = 0;
            if ((patName != null) && (!patName.equals(""))) {
                if (patName.indexOf('^') != -1) { // At least the first two
                    // fields are specified
                    p.setLastName(patName.substring(0, patName.indexOf('^')));
                    nextCaret = patName.indexOf('^', patName.indexOf('^') + 1);
                    // this is the position of the second ^
                    if (nextCaret != -1) { // At least the first three fields
                        // are specified
                        p.setFirstName(patName.substring(patName.indexOf('^') + 1, nextCaret));
                        if (patName.indexOf('^', nextCaret + 1) != -1) {
                            p.setMiddleName(patName.substring(nextCaret + 1, patName.indexOf('^', nextCaret + 1)));
                            if (patName.lastIndexOf('^') > patName.indexOf('^', nextCaret + 1)) {
                                p.setPrefix(patName.substring(patName.indexOf('^', nextCaret + 1) + 1, patName.lastIndexOf('^')));
                                p.setSuffix(patName.substring(patName.lastIndexOf('^') + 1));
                            } else
                                p.setPrefix(patName.substring(patName.indexOf('^', nextCaret + 1) + 1));
                        } else
                            p.setMiddleName(patName.substring(nextCaret + 1));
                    } else {
                        // Last and First Name specified
                        p.setFirstName(patName.substring(patName.indexOf('^') + 1));
                    }
                } else if (patName.indexOf('|') != -1) {
                    // for siemens workstation tha don't use ^ but |
                    p.setLastName(patName.substring(0, patName.indexOf('|')));
                    nextCaret = patName.indexOf('|', patName.indexOf('|') + 1);
                    if (nextCaret != -1) {
                        p.setFirstName(patName.substring(patName.indexOf('|') + 1, nextCaret));
                        if (patName.indexOf('|', nextCaret + 1) != -1) {
                            p.setMiddleName(patName.substring(nextCaret + 1, patName.indexOf('|', nextCaret + 1)));
                            if (patName.lastIndexOf('|') > patName.indexOf('|', nextCaret + 1)) {
                                p.setPrefix(patName.substring(patName.indexOf('|', nextCaret + 1) + 1, patName.lastIndexOf('|')));
                                p.setSuffix(patName.substring(patName.lastIndexOf('|') + 1));
                            } else
                                p.setPrefix(patName.substring(patName.indexOf('|', nextCaret + 1) + 1));
                        } else
                            p.setMiddleName(patName.substring(nextCaret + 1));
                    } else {
                        p.setFirstName(patName.substring(patName.indexOf('|') + 1));
                    }
                } else {
                    p.setLastName(patName);
                }
            }
        }
        if (d.contains(Tags.PatientBirthDate)) {
            if (p == null)
                p = new Patient();
            java.util.Date[] dr = d.getDateRange(Tags.PatientBirthDate);
            if (dr != null) {
                p.setBirthDateAsString(d.getString(Tags.PatientBirthDate));
                if ((dr[0] != null) && (dr[1] != null)) {
                    p.setBirthDateRange(new java.sql.Date(dr[0].getTime()), new java.sql.Date(dr[1].getTime()));
                } else if (dr[0] != null) {
                    p.setBirthDateRange(new java.sql.Date(dr[0].getTime()), null);
                } else if (dr[1] != null) {
                    p.setBirthDateRange(null, new java.sql.Date(dr[1].getTime()));
                }
            }
        }
        if (d.contains(Tags.PatientBirthTime)) {
            if (p == null)
                p = new Patient();
            java.util.Date[] dr = d.getDateRange(Tags.PatientBirthTime);
            if (dr != null)
                if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no value
                    // was specified, it
                    // is just wanted
                    // back!
                    if (dr[0] == null)
                        p.setBirthTimeRange(null, new java.sql.Time(dr[1].getTime()));
                    else if (dr[1] == null)
                        p.setBirthTimeRange(new java.sql.Time(dr[0].getTime()), null);
                    else
                        p.setBirthTimeRange(new java.sql.Time(dr[0].getTime()), new java.sql.Time(dr[1].getTime()));
                }
        }
        if (d.contains(Tags.PatientSex)) {
            if (p == null)
                p = new Patient();
            p.setSex(d.getString(Tags.PatientSex));
        }
        if (d.contains(Tags.EthnicGroup)) {
            if (p == null)
                p = new Patient();
            p.setEthnicGroup(d.getString(Tags.EthnicGroup));
        }
        if (d.contains(Tags.PatientAddress)) {
            if (p == null)
                p = new Patient();
            p.setPatientAddress(d.getString(Tags.PatientAddress));
        }
        if (d.contains(Tags.PatientComments)) {
            if (p == null)
                p = new Patient();
            p.setPatientComments(d.getString(Tags.PatientComments));
        }
        if (d.contains(Tags.PatientState)) {
            if (p == null)
                p = new Patient();
            p.setPatientState(d.getString(Tags.PatientState));
        }
        if (d.contains(Tags.PregnancyStatus)) {
            if (p == null)
                p = new Patient();
            p.setPregnancyStatus(d.getString(Tags.PregnancyStatus));
        }
        if (d.contains(Tags.MedicalAlerts)) {
            if (p == null)
                p = new Patient();
            p.setMedicalAlerts(d.getString(Tags.MedicalAlerts));
        }
        if (d.contains(Tags.PatientWeight)) {
            if (p == null)
                p = new Patient();
            p.setPatientWeight(d.getString(Tags.PatientWeight));
        }
        if (d.contains(Tags.ConfidentialityPatientData)) {
            if (p == null)
                p = new Patient();
            p.setConfidentialityConstraint(d.getString(Tags.ConfidentialityPatientData));
        }
        if (d.contains(Tags.SpecialNeeds)) {
            if (p == null)
                p = new Patient();
            p.setSpecialNeeds(d.getString(Tags.SpecialNeeds));
        }
        if (d.contains(Tags.NumberOfPatientRelatedStudies)) {
            if (p == null)
                p = new Patient();
            p.setNumberOfPatientRelatedStudies(DicomConstants.FIND_NUMBER_OF_RELATED);
        }
        if (d.contains(Tags.NumberOfPatientRelatedSeries)) {
            if (p == null)
                p = new Patient();
            p.setNumberOfPatientRelatedSeries(DicomConstants.FIND_NUMBER_OF_RELATED);
        }
        if (d.contains(Tags.NumberOfPatientRelatedInstances)) {
            if (p == null)
                p = new Patient();
            p.setNumberOfPatientRelatedInstances(DicomConstants.FIND_NUMBER_OF_RELATED);
        }
        // Study Data:
        if (d.contains(Tags.StudyInstanceUID)) {
            if (st == null)
                st = new Study("");
            String[] matches = d.getStrings(Tags.StudyInstanceUID);
            if (matches.length == 1)
                st.setStudyInstanceUid(matches[0]);
            else {
                for (int u = 0; u < matches.length; u++)
                    st.addUidToMatch(matches[u]);
            }
        }
        if (d.contains(Tags.StudyID)) {
            if (st == null)
                st = new Study("");
            st.setStudyId(d.getString(Tags.StudyID));
        }
        if (d.contains(Tags.StudyStatusID)) {
            if (st == null)
                st = new Study("");
            st.setStudyStatusId(d.getString(Tags.StudyStatusID));
        }
        if (d.contains(Tags.StudyDate)) {
            if (st == null)
                st = new Study("");
            java.util.Date[] dr = d.getDateRange(Tags.StudyDate);
            if (dr != null) {
                st.setStudyDateString(d.getString(Tags.StudyDate));
                if ((dr[0] != null) && (dr[1] != null)) {
                    st.setStudyDateRange(new java.sql.Date(dr[0].getTime()), new java.sql.Date(dr[1].getTime()));
                } else if (dr[0] != null) {
                    st.setStudyDateRange(new java.sql.Date(dr[0].getTime()), null);
                } else if (dr[1] != null) {
                    st.setStudyDateRange(null, new java.sql.Date(dr[1].getTime()));
                }
            }
        }
        if (d.contains(Tags.StudyTime)) {
            if (st == null)
                st = new Study("");
            java.util.Date[] dr = d.getDateRange(Tags.StudyTime);
            if (dr != null)
                if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no value
                    // was specified, it
                    // is just wanted
                    // back!
                    if (dr[0] == null)
                        st.setStudyTimeRange(null, new java.sql.Time(dr[1].getTime()));
                    else if (dr[1] == null)
                        st.setStudyTimeRange(new java.sql.Time(dr[0].getTime()), null);
                    else
                        st.setStudyTimeRange(new java.sql.Time(dr[0].getTime()), new java.sql.Time(dr[1].getTime()));
                }
        }
        if (d.contains(Tags.StudyCompletionDate)) {
            if (st == null)
                st = new Study("");
            java.util.Date[] dr = d.getDateRange(Tags.StudyCompletionDate);
            if (dr != null)
                if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no value
                    // was specified, it
                    // is just wanted
                    // back!
                    if (dr[0] == null)
                        st.setStudyCompletionDateRange(null, new java.sql.Date(dr[1].getTime()));
                    else if (dr[1] == null)
                        st.setStudyCompletionDateRange(new java.sql.Date(dr[0].getTime()), null);
                    else
                        st.setStudyCompletionDateRange(new java.sql.Date(dr[0].getTime()), new java.sql.Date(dr[1].getTime()));
                }
        }
        if (d.contains(Tags.StudyCompletionTime)) {
            if (st == null)
                st = new Study("");
            java.util.Date[] dr = d.getDateRange(Tags.StudyCompletionTime);
            if (dr != null)
                if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no value
                    // was specified, it
                    // is just wanted
                    // back!
                    if (dr[0] == null)
                        st.setStudyCompletionTimeRange(null, new java.sql.Time(dr[1].getTime()));
                    else if (dr[1] == null)
                        st.setStudyCompletionTimeRange(new java.sql.Time(dr[0].getTime()), null);
                    else
                        st.setStudyCompletionTimeRange(new java.sql.Time(dr[0].getTime()), new java.sql.Time(dr[1].getTime()));
                }
        }
        if (d.contains(Tags.StudyVerifiedDate)) {
            if (st == null)
                st = new Study("");
            java.util.Date[] dr = d.getDateRange(Tags.StudyVerifiedDate);
            if (dr != null)
                if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no value
                    // was specified, it
                    // is just wanted
                    // back!
                    if (dr[0] == null)
                        st.setStudyVerifiedDateRange(null, new java.sql.Date(dr[1].getTime()));
                    else if (dr[1] == null)
                        st.setStudyVerifiedDateRange(new java.sql.Date(dr[0].getTime()), null);
                    else
                        st.setStudyVerifiedDateRange(new java.sql.Date(dr[0].getTime()), new java.sql.Date(dr[1].getTime()));
                }
        }
        if (d.contains(Tags.StudyVerifiedTime)) {
            if (st == null)
                st = new Study("");
            java.util.Date[] dr = d.getDateRange(Tags.StudyVerifiedTime);
            if (dr != null)
                if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no value
                    // was specified, it
                    // is just wanted
                    // back!
                    if (dr[0] == null)
                        st.setStudyVerifiedTimeRange(null, new java.sql.Time(dr[1].getTime()));
                    else if (dr[1] == null)
                        st.setStudyVerifiedTimeRange(new java.sql.Time(dr[0].getTime()), null);
                    else
                        st.setStudyVerifiedTimeRange(new java.sql.Time(dr[0].getTime()), new java.sql.Time(dr[1].getTime()));
                }
        }
        if (d.contains(Tags.AccessionNumber)) {
            if (st == null)
                st = new Study("");
            st.setAccessionNumber(d.getString(Tags.AccessionNumber));
        }
        if (d.contains(Tags.StudyDescription)) {
            if (st == null)
                st = new Study("");
            st.setStudyDescription(d.getString(Tags.StudyDescription));
        }
        if (d.contains(Tags.ReferringPhysicianName)) {
            if (st == null)
                st = new Study("");
            st.setReferringPhysiciansName(d.getString(Tags.ReferringPhysicianName));
        }
        if (d.contains(Tags.AdmittingDiagnosisDescription)) {
            if (st == null)
                st = new Study("");
            st.setAdmittingDiagnosesDescription(d.getString(Tags.AdmittingDiagnosisDescription));
        }
        if (d.contains(Tags.NumberOfStudyRelatedSeries)) {
            if (st == null)
                st = new Study("");
            st.setNumberOfStudyRelatedSeries(DicomConstants.FIND_NUMBER_OF_RELATED);
        }
        if (d.contains(Tags.NumberOfStudyRelatedInstances)) {
            if (st == null)
                st = new Study("");
            st.setNumberOfStudyRelatedInstances(DicomConstants.FIND_NUMBER_OF_RELATED);
        }
        if (d.contains(Tags.ModalitiesInStudy)) {
            if (st == null)
                st = new Study("");
            st.setModalitiesInStudy(d.getStrings(Tags.ModalitiesInStudy));
        }
        // Series Data:
        if (d.contains(Tags.SeriesInstanceUID)) { // DTODO: deal with UIDs
            if (se == null)
                se = new Series();
            String[] matches = d.getStrings(Tags.SeriesInstanceUID);
            if (matches.length == 1)
                se.setSeriesInstanceUid(matches[0]);
            else {
                for (int u = 0; u < matches.length; u++)
                    se.addUidToMatch(matches[u]);
            }
        }
        if (d.contains(Tags.SeriesNumber)) {
            if (se == null)
                se = new Series();
            se.setSeriesNumber(d.getString(Tags.SeriesNumber));
        }
        if (d.contains(Tags.Modality)) {
            if (se == null)
                se = new Series();
            se.setModality(d.getString(Tags.Modality));
        }
        if (d.contains(Tags.BodyPartExamined)) {
            if (se == null)
                se = new Series();
            se.setBodyPartExamined(d.getString(Tags.BodyPartExamined));
        }
        if (d.contains(Tags.NumberOfSeriesRelatedInstances)) {
            if (se == null)
                se = new Series();
            se.setNumberOfSeriesRelatedInstances(DicomConstants.FIND_NUMBER_OF_RELATED);
        }
        if(d.contains(Tag.OperatorsName)){
            if(se == null)
                se = new Series();
            if(d.getStrings(Tag.OperatorsName).length > 0)
            se.setOperatorsName(d.getStrings(Tag.OperatorsName)[0]);
        }
        // Equipment Data:
        Equipment eq = null;
        if (d.contains(Tags.Manufacturer)) {
            if (se == null)
                se = new Series();
            if (eq == null)
                eq = new Equipment();
            eq.setManufacturer(d.getString(Tags.Manufacturer));
        }
        if (d.contains(Tags.InstitutionName)) {
            if (se == null)
                se = new Series();
            if (eq == null)
                eq = new Equipment();
            eq.setInstitutionName(d.getString(Tags.InstitutionName));
        }
        if (d.contains(Tags.StationName)) {
            if (se == null)
                se = new Series();
            if (eq == null)
                eq = new Equipment();
            eq.setStationName(d.getString(Tags.StationName));
        }
        if (d.contains(Tags.InstitutionalDepartmentName)) {
            if (se == null)
                se = new Series();
            if (eq == null)
                eq = new Equipment();
            eq.setInstitutionalDepartmentName(d.getString(Tags.InstitutionalDepartmentName));
        }
        if (d.contains(Tags.ManufacturerModelName)) {
            if (se == null)
                se = new Series();
            if (eq == null)
                eq = new Equipment();
            eq.setManufacturersModelName(d.getString(Tags.ManufacturerModelName));
        }
        if (d.contains(Tags.DeviceSerialNumber)) {
            if (se == null)
                se = new Series();
            if (eq == null)
                eq = new Equipment();
            eq.setDeviceSerialNumber(d.getString(Tags.DeviceSerialNumber));
        }
        if (d.contains(Tags.DateOfLastCalibration)) {
            if (se == null)
                se = new Series("");
            if (eq == null)
                eq = new Equipment();
            java.util.Date[] dr = d.getDateRange(Tags.DateOfLastCalibration);
            if (dr != null)
                if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no value
                    // was specified, it
                    // is just wanted
                    // back!
                    if (dr[0] == null)
                        eq.setDateOfLastCalibrationRange(null, new java.sql.Date(dr[1].getTime()));
                    else if (dr[1] == null)
                        eq.setDateOfLastCalibrationRange(new java.sql.Date(dr[0].getTime()), null);
                    else
                        eq.setDateOfLastCalibrationRange(new java.sql.Date(dr[0].getTime()), new java.sql.Date(dr[1].getTime()));
                }
        }
        if (d.contains(Tags.TimeOfLastCalibration)) {
            if (se == null)
                se = new Series("");
            if (eq == null)
                eq = new Equipment();
            java.util.Date[] dr = d.getDateRange(Tags.TimeOfLastCalibration);
            if (dr != null)
                if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no value
                    // was specified, it
                    // is just wanted
                    // back!
                    if (dr[0] == null)
                        eq.setTimeOfLastCalibrationRange(null, new java.sql.Time(dr[1].getTime()));
                    else if (dr[1] == null)
                        eq.setTimeOfLastCalibrationRange(new java.sql.Time(dr[0].getTime()), null);
                    else
                        eq.setTimeOfLastCalibrationRange(new java.sql.Time(dr[0].getTime()), new java.sql.Time(dr[1].getTime()));
                }
        }
        if (d.contains(Tags.ConversionType)) {
            if (se == null)
                se = new Series();
            if (eq == null)
                eq = new Equipment();
            eq.setConversionType(d.getString(Tags.ConversionType));
        }
        if (d.contains(Tags.SecondaryCaptureDeviceID)) {
            if (se == null)
                se = new Series();
            if (eq == null)
                eq = new Equipment();
            eq.setSecondaryCaptureDeviceId(d.getString(Tags.SecondaryCaptureDeviceID));
        }
        // if (se != null)
        // se.setEquipment(eq);
        if (d.contains(Tags.SamplesPerPixel)) {
            if (i == null)
                i = new Image();
            ((Image) i).setSamplesPerPixel(d.getString(Tags.SamplesPerPixel));
        }
        if (d.contains(Tags.Rows)) {
            if (i == null)
                i = new Image();
            ((Image) i).setRows(d.getString(Tags.Rows));
        }
        if (d.contains(Tags.Columns)) {
            if (i == null)
                i = new Image();
            ((Image) i).setColumns(d.getString(Tags.Columns));
        }
        if (d.contains(Tags.BitsAllocated)) {
            if (i == null)
                i = new Image();
            ((Image) i).setBitsAllocated(d.getString(Tags.BitsAllocated));
        }
        if (d.contains(Tags.BitsStored)) {
            if (i == null)
                i = new Image();
            ((Image) i).setBitsStored(d.getString(Tags.BitsStored));
        }
        if (d.contains(Tags.HighBit)) {
            if (i == null)
                i = new Image();
            ((Image) i).setHighBit(d.getString(Tags.HighBit));
        }
        if (d.contains(Tags.PixelRepresentation)) {
            if (i == null)
                i = new Image();
            ((Image) i).setPixelRepresentation(d.getString(Tags.PixelRepresentation));
        }
        if (d.contains(Tags.NumberOfFrames)) {
            if (i == null)
                i = new Image();
            ((Image) i).setNumberOfFrames(0);	// The tag is contained, so the value is wanted, but MATCHING IS NOT PERFORMED!!!
        }
        try {
            if (d.contains(Tags.ContentLabel)) {
                if (i == null)
                    i = new PresState();
                ((PresState) i).setPresentationLabel(d.getString(Tags.ContentLabel));
            }
            if (d.contains(Tags.ContentDescription)) {
                if (i == null)
                    i = new PresState();
                ((PresState) i).setPresentationDescription(d.getString(Tags.ContentDescription));
            }
            if (d.contains(Tags.PresentationCreationDate)) { //
                // PresentationCreationDate
                if (i == null)
                    i = new PresState();
                java.util.Date[] dr = d.getDateRange(Tags.PresentationCreationDate);
                if (dr != null)
                    if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no
                        // value was
                        // specified, it
                        // is just
                        // wanted back!
                        if (dr[0] == null)
                            ((PresState) i).setPresentationCreationDateRange(null, new java.sql.Date(dr[1].getTime()));
                        else if (dr[1] == null)
                            ((PresState) i).setPresentationCreationDateRange(new java.sql.Date(dr[0].getTime()), null);
                        else
                            ((PresState) i).setPresentationCreationDateRange(new java.sql.Date(dr[0].getTime()), new java.sql.Date(dr[1].getTime()));
                    }
            }
            if (d.contains(Tags.PresentationCreationTime)) {
                if (i == null)
                    i = new PresState();
                java.util.Date[] dr = d.getDateRange(Tags.PresentationCreationTime);
                if (dr != null)
                    if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no
                        // value was
                        // specified, it
                        // is just
                        // wanted back!
                        if (dr[0] == null)
                            ((PresState) i).setPresentationCreationTimeRange(null, new java.sql.Time(dr[1].getTime()));
                        else if (dr[1] == null)
                            ((PresState) i).setPresentationCreationTimeRange(new java.sql.Time(dr[0].getTime()), null);
                        else
                            ((PresState) i).setPresentationCreationTimeRange(new java.sql.Time(dr[0].getTime()), new java.sql.Time(dr[1].getTime()));
                    }
            }
            if (d.contains(Tags.PresentationCreatorName)) {
                if (i == null)
                    i = new PresState();
                ((PresState) i).setPresentationCreatorsName(d.getString(Tags.PresentationCreatorName));
            }
            if (d.contains(Tags.RecommendedViewingMode)) {
                if (i == null)
                    i = new PresState();
                ((PresState) i).setRecommendedViewingMode(d.getString(Tags.RecommendedViewingMode));
            }
            if (d.contains(Tags.CompletionFlag)) {
                if (i == null)
                    i = new StructRep();
                ((StructRep) i).setCompletionFlag(d.getString(Tags.CompletionFlag));
            }
            if (d.contains(Tags.VerificationFlag)) {
                if (i == null)
                    i = new StructRep();
                ((StructRep) i).setVerificationFlag(d.getString(Tags.VerificationFlag));
            }
            if (d.contains(Tags.ContentDate)) { //
                // PresentationCreationDate
                if (i == null)
                    i = new StructRep();
                java.util.Date[] dr = d.getDateRange(Tags.ContentDate);
                if (dr != null)
                    if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no
                        // value was
                        // specified, it
                        // is just
                        // wanted back!
                        if (dr[0] == null)
                            ((StructRep) i).setContentDateRange(null, new java.sql.Date(dr[1].getTime()));
                        else if (dr[1] == null)
                            ((StructRep) i).setContentDateRange(new java.sql.Date(dr[0].getTime()), null);
                        else
                            ((StructRep) i).setContentDateRange(new java.sql.Date(dr[0].getTime()), new java.sql.Date(dr[1].getTime()));
                    }
            }
            if (d.contains(Tags.ContentTime)) { //
                // PresentationCreationTime
                if (i == null)
                    i = new StructRep();
                java.util.Date[] dr = d.getDateRange(Tags.ContentTime);
                if (dr != null)
                    if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no
                        // value was
                        // specified, it
                        // is just
                        // wanted back!
                        if (dr[0] == null)
                            ((StructRep) i).setContentTimeRange(null, new java.sql.Time(dr[1].getTime()));
                        else if (dr[1] == null)
                            ((StructRep) i).setContentTimeRange(new java.sql.Time(dr[0].getTime()), null);
                        else
                            ((StructRep) i).setContentTimeRange(new java.sql.Time(dr[0].getTime()), new java.sql.Time(dr[1].getTime()));
                    }
            }
            if (d.contains(Tags.ObservationDateTime)) { //
                // PresentationCreationTime
                if (i == null)
                    i = new StructRep();
                java.util.Date[] dr = d.getDateRange(Tags.ObservationDateTime);
                if (dr != null)
                    if ((dr[0] != null) || (dr[1] != null)) { // Otherwise no
                        // value was
                        // specified, it
                        // is just
                        // wanted back!
                        if (dr[0] == null)
                            ((StructRep) i).setObservationDateTimeRange(null, new java.sql.Timestamp(dr[1].getTime()));
                        else if (dr[1] == null)
                            ((StructRep) i).setObservationDateTimeRange(new java.sql.Timestamp(dr[0].getTime()), null);
                        else
                            ((StructRep) i).setObservationDateTimeRange(new java.sql.Timestamp(dr[0].getTime()), new java.sql.Timestamp(dr[1].getTime()));
                    }
            }
            if (d.contains(Tags.OverlayNumber)) {
                if (i == null)
                    i = new Overlay();
                ((Overlay) i).setOverlayNumber(d.getString(Tags.OverlayNumber));
            }
            if (d.contains(Tags.OverlayRows)) {
                if (i == null)
                    i = new Overlay();
                ((Overlay) i).setOverlayRows(d.getString(Tags.OverlayRows));
            }
            if (d.contains(Tags.OverlayColumns)) {
                if (i == null)
                    i = new Overlay();
                ((Overlay) i).setOverlayColumns(d.getString(Tags.OverlayColumns));
            }
            if (d.contains(Tags.OverlayBitsAllocated)) {
                if (i == null)
                    i = new Overlay();
                ((Overlay) i).setOverlayBitsAllocated(d.getString(Tags.OverlayBitsAllocated));
            }
            if (d.contains(Tags.OverlayType)) {
                if (i == null)
                    i = new Overlay();
                ((Overlay) i).setOverlayType(d.getString(Tags.OverlayType));
            }
        } catch (ClassCastException ccex) {
            ccex.printStackTrace();
        }
        if (d.contains(Tags.SOPInstanceUID)) {
            if (i == null)
                i = new NonImage();
            String[] matches = d.getStrings(Tags.SOPInstanceUID);
            if (matches.length == 1)
                i.setSopInstanceUid(matches[0]);
            else {
                for (int u = 0; u < matches.length; u++)
                    i.addUidToMatch(matches[u]);
            }
        }
        if (d.contains(Tags.SOPClassUID)) {
            if (i == null)
                i = new NonImage();
            String[] matches = d.getStrings(Tags.SOPClassUID);
            if (matches.length == 1)
                i.setSopClassUid(matches[0]);
            else {
                for (int u = 0; u < matches.length; u++)
                    i.addSopClassToMatch(matches[u]);
            }
        }
        if (d.contains(Tags.InstanceNumber)) {
            if (i == null)
                i = new NonImage();
            i.setInstanceNumber(d.getString(Tags.InstanceNumber));
        }
        if(p!=null){
        	if(partitioningStrategy.equals(PartitioningStrategy.CALLED))
        		p.setIdIssuer(calledAE);
        	else if(partitioningStrategy.equals(PartitioningStrategy.CALLING))
        		p.setIdIssuer(callingAE);
        }
        DicomMatch dm = new DicomMatch();
        dm.patient = p;
        dm.study = st;
        dm.series = se;
        dm.instance = i;
        return dm;
    }
}
