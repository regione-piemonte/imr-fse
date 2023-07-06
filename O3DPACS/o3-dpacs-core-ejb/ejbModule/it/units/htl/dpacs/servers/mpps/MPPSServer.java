/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.mpps;

import it.units.htl.dpacs.dao.DicomMppsDealerLocal;
import it.units.htl.dpacs.dao.DicomStorageDealerLocal;
import it.units.htl.dpacs.dao.ImageAvailabilityLocal;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.IheStudyId;
import it.units.htl.dpacs.helpers.ImageAvailabilityConfig;
import it.units.htl.dpacs.helpers.StudyTrackingSettings;
import it.units.htl.dpacs.postprocessing.multiframe.DcmFramesToMF;
import it.units.htl.dpacs.valueObjects.CodeSequence;
import it.units.htl.dpacs.valueObjects.MPPSItem;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4che.util.DcmURL;
import org.dcm4che.util.SSLContextAdapter;

class MPPSServer extends DcmServiceBase {
    // Constants -----------------------------------------------------
    private final static String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian };
    private static final String[] defaultCiphers = {
            "SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA",
            "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
            "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
            "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA",
            "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
            "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
            "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
            "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
            "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "SSL_RSA_WITH_NULL_MD5",
            "SSL_RSA_WITH_NULL_SHA", "SSL_DH_anon_WITH_RC4_128_MD5",
            "TLS_DH_anon_WITH_AES_128_CBC_SHA",
            "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA",
            "SSL_DH_anon_WITH_DES_CBC_SHA",
            "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5",
            "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA" };
    // Attributes ----------------------------------------------------
    private final static AssociationFactory assocFact = AssociationFactory.getInstance();
    private final MPPSSCP scp;
    private SSLContextAdapter tls = null;
    private int acTimeout;
    private int dimseTimeout;
    private int soCloseDelay;
    private DcmURL destinationOne = null;
    private DcmURL destinationTwo = null;
    private DcmURL forwardTo = null;
    private boolean trackStudyCompletion;
    private int minutesToWait = 300000; // 5mins
    private int timesToTryForward = 2;
    private DicomMppsDealerLocal bean = null;
    private DicomStorageDealerLocal stBean = null;
    private static Log log = LogFactory.getLog(MPPSServer.class);
    private MPPSItem mpps;
    private String[] involvedSeries;
    private int numOfInvolvedInstances;

    /**
     * Constructor for the MoveService object
     * 
     * @param scp
     *            Description of the Parameter
     */
    public MPPSServer(MPPSSCP scp) {
        this.scp = scp;
    }

    /**
     * Sets the sSLContextAdapter attribute of the StgCmtSCP object
     * 
     * @param tls
     *            The new sSLContextAdapter value
     */
    public void setSSLContextAdapter(SSLContextAdapter tls) {
        this.tls = tls;
    }

    /**
     * Gets the acTimeout attribute of the MoveService object
     * 
     * @return The acTimeout value
     */
    public int getAcTimeout() {
        return this.acTimeout;
    }

    /**
     * Sets the acTimeout attribute of the MoveService object
     * 
     * @param timeout
     *            The new acTimeout value
     */
    public void setAcTimeout(int timeout) {
        this.acTimeout = timeout;
    }

    /**
     * Gets the dimseTimeout attribute of the MoveService object
     * 
     * @return The dimseTimeout value
     */
    public int getDimseTimeout() {
        return this.dimseTimeout;
    }

    /**
     * Sets the dimseTimeout attribute of the MoveService object
     * 
     * @param timeout
     *            The new dimseTimeout value
     */
    public void setDimseTimeout(int timeout) {
        this.dimseTimeout = timeout;
    }

    /**
     * Gets the soCloseDelay attribute of the MoveService object
     * 
     * @return The soCloseDelay value
     */
    public int getSoCloseDelay() {
        return this.soCloseDelay;
    }

    public void setSoCloseDelay(int delay) {
        this.soCloseDelay = delay;
    }

    public String getDestinationOne() {
        return (destinationOne == null) ? "" : destinationOne.toString();
    }

    public void setDestinationOne(String destinationOne) {
        this.destinationOne = ((destinationOne == null)
                || (destinationOne.equals("") || (destinationOne.equals("-"))) ? null
                : new DcmURL(destinationOne));
    }

    public String getDestinationTwo() {
        return (destinationTwo == null) ? "" : destinationTwo.toString();
    }

    public void setDestinationTwo(String destinationTwo) {
        this.destinationTwo = (((destinationTwo == null)
                || (destinationTwo.equals("")) || (destinationTwo.equals("-"))) ? null
                : new DcmURL(destinationTwo));
    }

    public int getMinutesToWait() {
        return minutesToWait;
    }

    public void setMinutesToWait(int minutesToWait) { // You can use just
        // multiples of
        // minutes!!!
        this.minutesToWait = (int) Math.floor(minutesToWait * 60000);
    }

    public int getTimesToTryForward() {
        return timesToTryForward;
    }

    public void setTimesToTryForward(int timesToTryForward) {
        this.timesToTryForward = timesToTryForward;
    }

    /**
     * Description of the Method
     * 
     * @param assoc
     *            Description of the Parameter
     * @param rq
     *            Description of the Parameter
     * @param rspCmd
     *            Description of the Parameter
     * @return Description of the Return Value
     * @exception IOException
     *                Description of the Exception
     * @exception DcmServiceException
     *                Description of the Exception
     */
    protected Dataset doNCreate(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws IOException, DcmServiceException {
        Command cmd = rq.getCommand();
        Dataset ds = rq.getDataset();
        scp.logDataset("Create MPPS:\n", ds);
        log.info("MPPS Server dealing with NCreate " + cmd.getAffectedSOPInstanceUID());
        /* MPPSItem receivedItem = */buildValueObjs(ds, cmd.getAffectedSOPInstanceUID());
        trackStudyCompletion = StudyTrackingSettings.isStudyCompletionTrackingEnabled();
        if (bean == null) {
            try {
                bean = InitialContext.doLookup(BeansName.LMppsDealer);
            } catch (NamingException nex) {
                throw new DcmServiceException(Status.ProcessingFailure);
            }
        }
        int status = bean.insertMPPSItem(mpps, assoc);
        if ((destinationOne == null) && (destinationTwo == null)) {
            log.info(assoc.getAssociation().getCallingAET() + ": No mpps forwarding list");
        } else {
            if (destinationOne != null) {
                forwardTo = destinationOne;
                log.info("Trying Forwarding to " + destinationOne.toString());
                boolean res = forward(assoc, rq);
                if ((res == false) && (timesToTryForward > 0)) {
                    log.fatal("MPPS Server failed to forward to " + destinationOne + "...try again...");
                    MPPSServer.TimeForwarder f1 = this.new TimeForwarder();
                    f1.from = assoc;
                    f1.dimse = rq;
                    f1.forwardTo = forwardTo;
                    f1.timesToTryForward = this.timesToTryForward;
                    Timer t1 = new Timer();
                    t1.scheduleAtFixedRate(f1, minutesToWait, minutesToWait);
                } // Try to forward every 5 mins
            }
            if (destinationTwo != null) {
                forwardTo = destinationTwo;
                log.info("Trying Forwarding to " + destinationTwo.toString());
                boolean res = forward(assoc, rq);
                if ((res == false) && (timesToTryForward > 0)) {
                    log.fatal("MPPS Server failed to forward to " + destinationTwo + "...try again...");
                    MPPSServer.TimeForwarder f2 = this.new TimeForwarder();
                    f2.from = assoc;
                    f2.dimse = rq;
                    f2.forwardTo = forwardTo;
                    f2.timesToTryForward = this.timesToTryForward;
                    Timer t2 = new Timer();
                    t2.scheduleAtFixedRate(f2, minutesToWait, minutesToWait); // Try to forward every minutesToWait mins
                }
            }
        }
        if (trackStudyCompletion) {
            if (stBean == null) {
                try {
                    stBean = InitialContext.doLookup(BeansName.LDicomStorageDealer);
                } catch (NamingException cex) {
                    throw new DcmServiceException(Status.ProcessingFailure);
                }
            }
            int ret = stBean.addStudyTracking(mpps.getScheduledStepAttributesSequence()[0].getStudyInstanceUID(), ConfigurationSettings.STUDYCLOSE_MPPS);
            if (ret > 0)
                log.info("Inserted/updated to MPPS a tracking row for study " + mpps.getScheduledStepAttributesSequence()[0].getStudyInstanceUID());
        }
        if (status == 0) {
            log.info("FROM MPPSservice, MPPS Insert Ended, no severes, sending RSP with status SUCCESS");
        } else {
            throw new DcmServiceException(0x0110);
        }
        return null;
    }

    protected Dataset doNSet(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws IOException, DcmServiceException {
        String callingAe = assoc.getAssociation().getCallingAET();
        Command cmd = rq.getCommand();
        Dataset ds = rq.getDataset();
        trackStudyCompletion = StudyTrackingSettings.isStudyCompletionTrackingEnabled();
        buildValueObjs(ds, cmd.getAffectedSOPInstanceUID());
        log.info(callingAe + ": Executing MPPS N-set Request, MPPS Server dealing with NCreate " + cmd.getAffectedSOPInstanceUID());
        if (bean == null) {
            try {
                bean = InitialContext.doLookup(BeansName.LMppsDealer);
            } catch (NamingException nex) {
                throw new DcmServiceException(Status.ProcessingFailure);
            }
        }
        int status = bean.updateMPPSItem(mpps, assoc);
        if ((destinationOne == null) && (destinationTwo == null)) {
            log.info(callingAe + ": No mpps forwarding list");
        } else {
            if (destinationOne != null) {
                forwardTo = destinationOne;
                log.info(callingAe + ": Trying Forwarding to "
                        + destinationOne.toString());
                boolean res = forward(assoc, rq);
                if ((res == false) && (timesToTryForward > 0)) {
                    log.fatal(callingAe + ": MPPS Server failed to forward to "
                            + destinationOne);
                    MPPSServer.TimeForwarder f3 = this.new TimeForwarder();
                    f3.from = assoc;
                    f3.dimse = rq;
                    f3.forwardTo = forwardTo;
                    f3.timesToTryForward = this.timesToTryForward;
                    Timer t3 = new Timer();
                    t3.scheduleAtFixedRate(f3, minutesToWait, minutesToWait);
                }
            }
            if (destinationTwo != null) {
                forwardTo = destinationTwo;
                log.info(callingAe + ": Trying Forwarding to "
                        + destinationTwo.toString());
                boolean res = forward(assoc, rq);
                if ((res == false) && (timesToTryForward > 0)) {
                    log.fatal(callingAe + ": MPPS Server failed to forward to "
                            + destinationTwo);
                    MPPSServer.TimeForwarder f4 = this.new TimeForwarder();
                    f4.from = assoc;
                    f4.dimse = rq;
                    f4.forwardTo = forwardTo;
                    f4.timesToTryForward = this.timesToTryForward;
                    Timer t4 = new Timer();
                    t4.scheduleAtFixedRate(f4, minutesToWait, minutesToWait);
                }
            }
        }
        if ((trackStudyCompletion) && ("COMPLETED".equalsIgnoreCase(mpps.getPerformedProcedureStepStatus()))) { // This must be before multiframe generation, otherwise the number of instances in the PACS is different from that sent via N-SET
            if (stBean == null) {
                try {
                    stBean = InitialContext.doLookup(BeansName.LDicomStorageDealer);
                } catch (NamingException nex) {
                    throw new DcmServiceException(Status.ProcessingFailure);
                }
            }
            String study = stBean.findStudyForSeries(involvedSeries, numOfInvolvedInstances);
            int res = stBean.completeStudy(study, ConfigurationSettings.STUDYCLOSE_MPPS);
            if (res > 0) {
                log.info("Completed a tracking row for study " + study);
                ImageAvailabilityConfig iac=ImageAvailabilityConfig.getInstance();
            	if(iac.isEnabled() && ImageAvailabilityConfig.PUBLISHTO_TABLE.equals(iac.getPublicationMethod())){
            		
                    ImageAvailabilityLocal imAvailBean=null;
                    try{
                    	imAvailBean=InitialContext.doLookup(BeansName.LImageAvailability);
                    	IheStudyId ids=new IheStudyId(study, null, null);
                    	imAvailBean.insertCorrectStudy(ids, ImageAvailabilityConfig.RECONCILIATIONSOURCE_WORKLIST, iac.getStringForSetting(), iac.getTargetApp());
                    } catch (NamingException nex) {
                        log.error("Error accessing Data Access Layer: "+BeansName.LImageAvailability, nex);
                    } catch(Exception ex){
                    	log.error("An error occurred publishing studies", ex);
                    }

            	}
            	
            } else {
                log.warn("No study completed. Maybe missing some series or instances. StudyUID = " + study);
            }
        }
        // Create Multiframe image if needed
        if ((mpps.getPerformedSeriesSequence() != null) && (mpps.getPerformedSeriesSequence().length > 0)) {
            String seriesToConvert = mpps.getPerformedSeriesSequence()[0].getSeriesInstanceUID();
            DcmFramesToMF dcmFTM = new DcmFramesToMF(callingAe, assoc.getAssociation().getCalledAET(), seriesToConvert);
            String mFrameOutput = dcmFTM.processFrames();
            if (mFrameOutput != null)
                log.info("Multiframe image created at position " + mFrameOutput + " (" + dcmFTM.getLastConversionDate().toString() + ")");
        }
        if (status == 0) {
            log.info(callingAe + ": reporting enabled: " + scp.reportingEnabled + ", Mpps n-set carries status: " + mpps.getPerformedProcedureStepStatus());
            if ((scp.reportingEnabled) && ("COMPLETED".equalsIgnoreCase(mpps.getPerformedProcedureStepStatus()))) {
                log.info("Reporting is enabled, a MPPS Completed is Received, I generate a Reporting Workitem for " + mpps.getPatientName() + ", " + mpps.getPatientID());
            }
            // build reporting entry, a scheduled procedure step object internal to pacs.
            log.debug("MPPS Server ended successfully");
        } else {
            throw new DcmServiceException(0x0110);
        }
        return null;
    }

    /**
     * Description of the Method
     * 
     * @param host
     *            Description of the Parameter
     * @param port
     *            Description of the Parameter
     * @param cipherSuites
     *            Description of the Parameter
     * @return Description of the Return Value
     * @exception Exception
     *                Description of the Exception
     */
    private Socket createSocket(String host, int port, String[] cipherSuites)
            throws Exception {
        if (cipherSuites == null)
            return new Socket(host, port);
        else {
            String[] supported = new String[cipherSuites.length
                    + defaultCiphers.length];
            int i = 0;
            for (i = 0; i < defaultCiphers.length; i++)
                supported[i] = defaultCiphers[i];
            for (int j = 0; j < cipherSuites.length; j++)
                supported[i + j] = cipherSuites[j];
            return tls.getSocketFactory(supported).createSocket(host, port);
        }
    }

    /**
     * Description of the Method
     * 
     * @param from
     *            Description of the Parameter
     * @param dimse
     *            Description of the Parameter
     */
    private boolean forward(ActiveAssociation from, Dimse dimse) {
        boolean SUCCESS = false;
        try {
            String thisAET = from.getAssociation().getCalledAET();
            Association assoc = assocFact.newRequestor(createSocket(forwardTo.getHost(), forwardTo.getPort(), forwardTo.getCipherSuites()));
            assoc.setAcTimeout(acTimeout);
            assoc.setDimseTimeout(dimseTimeout);
            assoc.setSoCloseDelay(soCloseDelay);
            int pcid = dimse.pcid();
            AAssociateRQ rq = assocFact.newAAssociateRQ();
            rq.setCallingAET(thisAET);
            rq.setCalledAET(forwardTo.getCalledAET());
            rq.addPresContext(assocFact.newPresContext(0, UIDs.Verification, NATIVE_TS));
            rq.addPresContext(assocFact.newPresContext(pcid, UIDs.ModalityPerformedProcedureStep, NATIVE_TS));
            PDU pdu = assoc.connect(rq);
            if (!(pdu instanceof AAssociateAC)) {
                log.error("connection to " + forwardTo + " failed: " + pdu);
                return false;
            }
            ActiveAssociation activeAssoc = assocFact.newActiveAssociation(assoc, null);
            activeAssoc.start();
            AAssociateAC ac = (AAssociateAC) pdu;
            if (ac.getPresContext(pcid).result() == PresContext.ACCEPTANCE) {
                FutureRSP rsp = activeAssoc.invoke(dimse);
                Command rspCmd = rsp.get().getCommand();
                if (rspCmd.getStatus() == Status.Success) {
                    SUCCESS = true;
                }
                if (rspCmd.getStatus() != Status.Success) {
                    log.debug("" + forwardTo + " returns response with severe status: " + rspCmd);
                }
            } else {
                log.error("mpps rejected by " + forwardTo);
            }
            try {
                activeAssoc.release(false);
            } catch (Exception e) {
                log.debug("release association to " + forwardTo + " failed:", e);
            }
        } catch (Exception ex) {
            log.error("MPPS DESTINATION NOT REACHABLE", ex);
        }
        return SUCCESS;
    }

    private class TimeForwarder extends TimerTask {
        private ActiveAssociation from = null;
        private Dimse dimse = null;
        private DcmURL forwardTo = null;
        private int timesToTryForward = 0; // The #times you retry forwarding

        public TimeForwarder() {
        }

        public void run() {
            if (timesToTryForward <= 1)
                cancel();
            try {
                String thisAET = from.getAssociation().getCalledAET();
                timesToTryForward--;
                Association assoc = assocFact.newRequestor(createSocket(forwardTo.getHost(), forwardTo.getPort(), forwardTo.getCipherSuites()));
                assoc.setAcTimeout(acTimeout);
                assoc.setDimseTimeout(dimseTimeout);
                assoc.setSoCloseDelay(soCloseDelay);
                int pcid = dimse.pcid();
                AAssociateRQ rq = assocFact.newAAssociateRQ();
                rq.setCallingAET(thisAET);
                rq.setCalledAET(forwardTo.getCalledAET());
                rq.addPresContext(assocFact.newPresContext(0, UIDs.Verification, NATIVE_TS));
                rq.addPresContext(assocFact.newPresContext(pcid, UIDs.ModalityPerformedProcedureStep, NATIVE_TS));
                PDU pdu = assoc.connect(rq);
                if (!(pdu instanceof AAssociateAC)) {
                    log.error("connection to " + forwardTo + " failed: " + pdu);
                    return;
                }
                ActiveAssociation activeAssoc = assocFact.newActiveAssociation(assoc, null);
                activeAssoc.start();
                AAssociateAC ac = (AAssociateAC) pdu;
                if (ac.getPresContext(pcid).result() == PresContext.ACCEPTANCE) {
                    FutureRSP rsp = activeAssoc.invoke(dimse);
                    Command rspCmd = rsp.get().getCommand();
                    if (rspCmd.getStatus() == Status.Success) {
                        this.cancel();
                    } else {
                        log.debug("" + forwardTo + " returns response with severe status: " + rspCmd);
                    }
                } else {
                    log.error("mpps rejected by " + forwardTo);
                }
                try {
                    activeAssoc.release(false);
                } catch (Exception e) {
                    log.debug("release association to " + forwardTo + " failed:" + e.getMessage());
                }
            } catch (Exception e) {
                log.error("MPPS DESTINATION UNREACHABLE " + forwardTo + "\nTry: " + timesToTryForward);
            }
        }
    }

    private CodeSequence buildCodeSequence(int tag, Dataset dataset) {
        DcmElement Element = dataset.get(tag);
        Dataset newDataset = Element.getItem();
        CodeSequence cs = new CodeSequence();
        cs.setCodeValue(newDataset.getString(Tags.CodeValue));
        cs.setCodingSchemeDesignator(newDataset.getString(Tags.CodingSchemeDesignator));
        cs.setCodingSchemeVersion(newDataset.getString(Tags.CodingSchemeVersion));
        cs.setCodeMeaning(newDataset.getString(Tags.CodeMeaning));
        return cs;
    }

    private MPPSItem buildValueObjs(Dataset ds, String SOPInstance) {
        // taken from constructor
        mpps = new MPPSItem(SOPInstance);
        involvedSeries = null;
        numOfInvolvedInstances = -1;
        // Method to fill MPPS Value Object based on all DICOM MPPS IOD fields
        Date td;
        // sopInstance taken form command commented code to get it from dataset
        // ---------------------RELATIONSHIP
        // MODULES------------------------------------------------------
        mpps.setPatientName(ds.getString(Tags.PatientName));
        mpps.setPatientID(ds.getString(Tags.PatientID));
        mpps.setIssuerOfPatientID(ds.getString(Tags.IssuerOfPatientID));
        td = ds.getDate(Tags.PatientBirthDate);
        if (td != null)
            mpps.setPatientBirthDate(new java.sql.Date(td.getTime()));
        mpps.setPatientSex(ds.getString(Tags.PatientSex));
        if (ds.get(Tags.ScheduledStepAttributesSeq) != null
                && ds.get(Tags.ScheduledStepAttributesSeq).countItems() != 0) { // All subsequent sequences are compulsory if this element's present!
            DcmElement SchedStepAttrSeq = ds.get(Tags.ScheduledStepAttributesSeq); // finds out sequence
            log.debug("Trying to build SchedStepAttribute sequence");
            for (int sc = SchedStepAttrSeq.countItems() - 1; sc >= 0; sc--) {
                MPPSItem.ScheduledStepAttributesSequence ssas = mpps.new ScheduledStepAttributesSequence();
                Dataset ssasDataset = SchedStepAttrSeq.getItem(sc); // Get the series one by one filling information
                ssas.setStudyInstanceUID(ssasDataset.getString(Tags.StudyInstanceUID));
                ssas.setAccessionNumber(ssasDataset.getString(Tags.AccessionNumber));
                ssas.setPlaceOrderNumberInSerReq(ssasDataset.getString(Tags.PlacerOrderNumberImagingServiceRequestRetired));
                ssas.setFillerOrderNumberInSerReq(ssasDataset.getString(Tags.FillerOrderNumberImagingServiceRequestRetired));
                ssas.setRequestedProcedureDescription(ssasDataset.getString(Tags.RequestedProcedureDescription));
                ssas.setScheduledProcedureStepID(ssasDataset.getString(Tags.SPSID));
                ssas.setScheduledProcedureDescription(ssasDataset.getString(Tags.SPSDescription));
                // gets a subsequence
                DcmElement element = ssasDataset.get(Tags.ScheduledProtocolCodeSeq);
                if ((ssasDataset.get(Tags.ScheduledProtocolCodeSeq) != null) && (ssasDataset.get(Tags.ScheduledProtocolCodeSeq).countItems() != 0)) {
                    for (int ic = element.countItems() - 1; ic >= 0; ic--) {
                        Dataset pcsDataset = element.getItem(ic);
                        CodeSequence cs = new CodeSequence();
                        cs.setCodeValue(pcsDataset.getString(Tags.CodeValue));
                        cs.setCodingSchemeDesignator(pcsDataset.getString(Tags.CodingSchemeDesignator));
                        cs.setCodingSchemeVersion(pcsDataset.getString(Tags.CodingSchemeVersion));
                        cs.setCodeMeaning(pcsDataset.getString(Tags.CodeMeaning));
                        ssas.addScheduledProtocolCodeSequence(cs);
                    }
                }
                mpps.addScheduledStepAttribute(ssas);
            }// end for
        } else {
            log.debug("FROM PARSER : SCHED STEP IS NULL");
        }
        // ------------------------------------------------------------------END OF
        // RELATIONSHIP MODULE------------------
        // ---------------------------------------------------------------START OF
        // MMPS INFORMATION MODULE----------------------
        mpps.setPerformedStationAETitle(ds.getString(Tags.PerformedStationAET));
        mpps.setPerformedStationName(ds.getString(Tags.PerformedStationName));
        mpps.setPerformedLocation(ds.getString(Tags.PerformedLocation));
        td = ds.getDate(Tags.PPSStartDate);
        if (td != null)
            mpps.setPerformedProcedureStepStartDate(new java.sql.Date(td.getTime()));
        td = ds.getDate(Tags.PPSStartTime);
        if (td != null)
            mpps.setPerformedProcedureStepStartTime(new java.sql.Time(td.getTime()));
        mpps.setPerformedProcedureStepID(ds.getString(Tags.PPSID));
        td = ds.getDate(Tags.PPSEndDate);
        if (td != null)
            mpps.setPerformedProcedureStepEndDate(new java.sql.Date(td.getTime()));
        td = ds.getDate(Tags.PPSEndTime);
        if (td != null)
            mpps.setPerformedProcedureStepEndTime(new java.sql.Time(td.getTime()));
        mpps.setPerformedProcedureStepStatus(ds.getString(Tags.PPSStatus));
        mpps.setPerformedProcedureStepDescription(ds.getString(Tags.PPSDescription));
        mpps.setCommentsOnPPS(ds.getString(Tags.PPSComments));
        mpps.setPerformedProcedureTypeDescription(ds.getString(Tags.PerformedProcedureTypeDescription));
        if ((ds.get(Tags.ProcedureCodeSeq) != null) && (ds.get(Tags.ProcedureCodeSeq).countItems() != 0)) {
            mpps.setProcedureCodeSequence(buildCodeSequence(Tags.ProcedureCodeSeq, ds));
        }
        if ((ds.get(Tags.PPSDiscontinuationReasonCodeSeq) != null) && (ds.get(Tags.PPSDiscontinuationReasonCodeSeq).countItems() != 0)) {
            mpps.setDiscontinuationReasonCodeSequence(buildCodeSequence(Tags.PPSDiscontinuationReasonCodeSeq, ds));
        }
        // --------------------------------------------------------------------end of
        // MPPS INFORMATION MODULE-----------------
        // -------------------------------------------------------------------Start of
        // Image Acquisition Results Module--------
        mpps.setModality(ds.getString(Tags.Modality));
        // mpps.setStudyID(ds.getString(Tags.StudyID));
        DcmElement ppcs = ds.get(Tags.PerformedProtocolCodeSeq); // Series
        if (ppcs != null && ppcs.countItems() != 0) { // All subsequent sequences are compulsory if this element's present!
            for (int sc = ppcs.countItems() - 1; sc >= 0; sc--) {
                Dataset ppcsDataset = ppcs.getItem(sc);
                CodeSequence cs = new CodeSequence();
                cs.setCodeValue(ppcsDataset.getString(Tags.CodeValue));
                cs.setCodingSchemeDesignator(ppcsDataset.getString(Tags.CodingSchemeDesignator));
                cs.setCodingSchemeVersion(ppcsDataset.getString(Tags.CodingSchemeVersion));
                cs.setCodeMeaning(ppcsDataset.getString(Tags.CodeMeaning));
                mpps.addPerformedProtocolCodeSequence(cs);
            }
        }
        DcmElement psseq = ds.get(Tags.PerformedSeriesSeq);
        if (psseq != null && psseq.countItems() != 0) {
            involvedSeries = new String[psseq.countItems()]; // Create the array which will store all series UIDs
            numOfInvolvedInstances = 0;
            for (int sc = psseq.countItems() - 1; sc >= 0; sc--) { // Now I navigate through each Performed Series
                MPPSItem.PerformedSeriesSequence pss = mpps.new PerformedSeriesSequence();
                Dataset pssDataset = psseq.getItem(sc);
                String[] names = pssDataset.getStrings(Tags.PerformingPhysicianName);
                if (names != null) {
                    for (int i = 0; i < names.length; i++) {
                        pss.addPerformingPhysicianName(names[i]);
                    }
                }
                String[] oNames = pssDataset.getStrings(Tags.OperatorName);
                if (oNames != null) {
                    for (int i = 0; i < oNames.length; i++) {
                        pss.addOperatorsName(oNames[i]);
                    }
                }
                pss.setProtocolName(pssDataset.getString(Tags.ProtocolName));
                pss.setSeriesInstanceUID(pssDataset.getString(Tags.SeriesInstanceUID));
                involvedSeries[sc] = pss.getSeriesInstanceUID(); // Set above, this can't be null at final state
                pss.setSeriesDescription(pssDataset.getString(Tags.SeriesDescription));
                pss.setRetrieveAETitle(pssDataset.getString(Tags.RetrieveAET));
                DcmElement refImSeq = pssDataset.get(Tags.RefImageSeq);
                if (refImSeq != null && refImSeq.countItems() != 0) {
                    numOfInvolvedInstances += refImSeq.countItems();
                    for (int l = refImSeq.countItems() - 1; l >= 0; l--) {
                        MPPSItem.ReferredItemSequence ris = mpps.new ReferredItemSequence();
                        Dataset rifDataset = refImSeq.getItem(l);
                        ris.setSOPClass(rifDataset.getString(Tags.RefSOPClassUID));
                        ris.setSOPInstance(rifDataset.getString(Tags.RefSOPInstanceUID));
                        pss.addReferredImageSequence(ris);
                    }
                }
                DcmElement refNImSeq = pssDataset.get(Tags.RefNonImageCompositeSOPInstanceSeq);
                if (refNImSeq != null && refNImSeq.countItems() != 0) {
                    numOfInvolvedInstances += refNImSeq.countItems();
                    for (int l = refNImSeq.countItems() - 1; l >= 0; l--) {
                        MPPSItem.ReferredItemSequence rNis = mpps.new ReferredItemSequence();
                        Dataset rNifDataset = refNImSeq.getItem(l);
                        rNis.setSOPClass(rNifDataset.getString(Tags.RefSOPClassUID));
                        rNis.setSOPInstance(rNifDataset.getString(Tags.RefSOPInstanceUID));
                        pss.addReferredNONImageSequence(rNis);
                    }
                }
                mpps.addPerformedSeriesSequence(pss);
            }
        }
        if (numOfInvolvedInstances == 0)
            numOfInvolvedInstances = -1;
        // ------------------------end of
        // Image Acquisition Results Module
        // ------------------------Start of
        // Radiation Dose Module-------------------------------
        if ((ds.get(Tags.AnatomicRegionSeq) != null) && (ds.get(Tags.AnatomicRegionSeq).countItems() != 0)) {
            mpps.setAnatomicsStructureSpaceOrRegionCodeSequence(buildCodeSequence(Tags.AnatomicRegionSeq, ds));
        }
        mpps.setTotalTimeOfFluoroscopy(ds.getString(Tags.TotalTimeOfFluoroscopy));
        mpps.setTotalNumberOfExposure(ds.getString(Tags.TotalNumberOfExposures));
        mpps.setDistanceSourceToDetector(ds.getString(Tags.DistanceSourceToDetector));
        mpps.setDistanceSourceToEntrance(ds.getString(Tags.DistanceSourceToEntrance));
        mpps.setEntranceDose(ds.getString(Tags.EntranceDose));
        mpps.setEntranceDoseMGY(ds.getString(Tags.EntranceDoseInmGy));
        mpps.setCommentsOnRadiationDose(ds.getString(Tags.CommentsOnRadiationDose));
        mpps.setExposedArea(ds.getString(Tags.ExposedArea));
        mpps.setImageAreaDoseProduct(ds.getString(Tags.ImageAndFluoroscopyAreaDoseProduct));
        if ((ds.get(Tags.ExposureDoseSeq) != null) && (ds.get(Tags.ExposureDoseSeq).countItems() != 0)) {
            DcmElement expDoseSeq = ds.get(Tags.ExposureDoseSeq);
            for (int l = expDoseSeq.countItems() - 1; l >= 0; l--) {
                MPPSItem.ExposureDoseSequence eDS = mpps.new ExposureDoseSequence();
                Dataset edsDataset = expDoseSeq.getItem(l);
                eDS.setRadiationMode(edsDataset.getString(Tags.RadiationMode));
                eDS.setKvp(edsDataset.getString(Tags.KVP));
                eDS.setxRayTubeCurrent(edsDataset.getString(Tags.XRayTubeCurrent));
                eDS.setExposureTime(edsDataset.getString(Tags.ExposureTime));
                eDS.setFilterType(edsDataset.getString(Tags.FilterType));
                eDS.setFilterMaterial(edsDataset.getString(Tags.FilterMaterial));
                mpps.addExposureDoseSequence(eDS);
            }
        }
        // --------------------------end of
        // Radiation Dose Sequence---------------------------------------
        // ------------------------Start of
        // Billing and Material Management Code Module Attributes---
        log.debug("from: parser: Billing Procedure Code Sequence.....");
        if ((ds.get(Tags.BillingProcedureStepSeq) != null) && (ds.get(Tags.BillingProcedureStepSeq).countItems() != 0)) {
            DcmElement dcmE = ds.get(Tags.BillingProcedureStepSeq);
            for (int i = 0; i < ds.get(Tags.BillingProcedureStepSeq).countItems(); i++) {
                Dataset newDataset = dcmE.getItem(i);
                CodeSequence cs = new CodeSequence();
                log.debug("from: parser: Billing item sequence.....");
                cs.setCodeValue(newDataset.getString(Tags.CodeValue));
                cs.setCodingSchemeDesignator(newDataset.getString(Tags.CodingSchemeDesignator));
                cs.setCodingSchemeVersion(newDataset.getString(Tags.CodingSchemeVersion));
                cs.setCodeMeaning(newDataset.getString(Tags.CodeMeaning));
                mpps.addBillingProcedureStepCodeSequence(cs);
                log.debug("parser finished processing BILLING PROCEDURE STEP CODE SEQUENCE");
            }
        }
        if ((ds.get(Tags.FilmConsumptionSeq) != null) && (ds.get(Tags.FilmConsumptionSeq).countItems() != 0)) {
            DcmElement filmConsSeq = ds.get(Tags.FilmConsumptionSeq);
            for (int l = filmConsSeq.countItems() - 1; l >= 0; l--) {
                MPPSItem.FilmConsumptionSequence fcs = mpps.new FilmConsumptionSequence();
                Dataset fcsDataset = filmConsSeq.getItem(l);
                fcs.setNumberOfFilms(fcsDataset.getString(Tags.NumberOfFilms));
                fcs.setFilmSizeID(fcsDataset.getString(Tags.FilmSizeID));
                fcs.setMediumType(fcsDataset.getString(Tags.MediumType));
                mpps.addFilmConsumptionSequence(fcs);
            }
        }
        log.debug("from: parser: Billing supplies and devices Sequence.....");
        if ((ds.get(Tags.BillingSuppliesAndDevicesSeq) != null) && (ds.get(Tags.BillingSuppliesAndDevicesSeq).countItems() != 0)) {
            DcmElement billingItemSeq = ds.get(Tags.BillingSuppliesAndDevicesSeq);
            for (int l = billingItemSeq.countItems() - 1; l >= 0; l--) {
                log.debug("parser is processing Billing sequence");
                MPPSItem.BillingItemSequence bis = mpps.new BillingItemSequence();
                Dataset bisDataset = billingItemSeq.getItem(l);
                if (bisDataset.get(Tags.BillingItemSeq) != null) {
                    log.debug("parser is processing Billing ITEM CODESequence");
                    bis.setBillingItemCode(buildCodeSequence(Tags.BillingItemSeq, bisDataset));
                }
                if (bisDataset.get(Tags.QuantitySeq) != null) {
                    DcmElement quantitySeq = bisDataset.get(Tags.QuantitySeq);
                    Dataset quantitySeqDs = quantitySeq.getItem(0);
                    String temp = quantitySeqDs.getString(Tags.Quantity);
                    bis.setQuantity(temp);
                    if (quantitySeqDs.get(Tags.MeasuringUnitsSeq) != null) {
                        log.debug("parser is processing measuring");
                        bis.setMeasuringUnitsSequence(buildCodeSequence(
                                Tags.MeasuringUnitsSeq, quantitySeqDs));
                    }
                }
                mpps.addBillingItemSequence(bis);
            }
        }
        return mpps;
    }
}