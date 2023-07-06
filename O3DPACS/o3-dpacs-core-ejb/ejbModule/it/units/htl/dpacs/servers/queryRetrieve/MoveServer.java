/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.queryRetrieve;

import it.units.htl.atna.AuditLogService;
import it.units.htl.dpacs.dao.*;
import it.units.htl.dpacs.statistics.Timer;
import it.units.htl.dpacs.statistics.TimerLogger;
import it.units.htl.dpacs.valueObjects.*;
import java.io.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.util.DcmURL;
import org.dcm4che.auditlog.User;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AsyncOpsWindow;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4che.util.SSLContextAdapter;

import it.units.htl.dpacs.helpers.AEData;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.helpers.RetrieveData;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che2.audit.message.InstancesAccessedMessage;
import org.dcm4che2.audit.message.AuditEvent.ActionCode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MoveServer extends DcmServiceBase {
    private static String loggersConfFile = null;
    private static String loggingDir = null;
    private final String[] SUPPORTED_TS = loadSupportedTS();
    private final static DcmObjectFactory objFact = DcmObjectFactory
            .getInstance();
    private final static AssociationFactory assocFact = AssociationFactory
            .getInstance();
    private final static AuditLoggerFactory alf = AuditLoggerFactory
            .getInstance();
    private final QueryRetrieveSCP scp;
    // private RetrieveLocal retrieve = null;
    private SSLContextAdapter tls = null;
    private int acTimeout = 5000;
    private int dimseTimeout = 0;
    private int soCloseDelay = 500;
    private DicomMoveDealerLocal bean = null;
    private TimerLogger tLogger = new TimerLogger();
    static final Log log = LogFactory.getLog(MoveServer.class);
    private static final String SEPARATOR_DICOM = "\\";

    private String[] loadSupportedTS() {
        ArrayList<String> loadedTs = new ArrayList<String>();
        String[] loadedTsArray = new String[0];
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        ServicesConfiguration sc = sch.findByServiceName("AcceptedTS");
        boolean takeDefault = false;
        try {
            if (sc.getConfiguration() != null) {
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                StringReader reader = new StringReader(sc.getConfiguration());
                InputSource is = new InputSource(reader);
                Document doc = docBuilder.parse(is);
                doc.getDocumentElement().normalize();
                NodeList nodeLst = doc.getElementsByTagName("root");
                Element _rootNode = (Element) nodeLst.item(0);
                NodeList ns = _rootNode.getElementsByTagName("ts");
                String supportedTs = "Supported TS on retrieve: ";
                for (int i = 0; i < ns.getLength(); i++) {
                    try {
                        loadedTs.add(ns.item(i).getTextContent());
                        supportedTs += " " + ns.item(i).getTextContent();
                    } catch (Exception e) {
                        log.warn(ns.item(i).getTextContent() + " TS is INVALID...watch the configuration.");
                    }
                }
                log.info(supportedTs);
                loadedTsArray = new String[loadedTs.size()];
            } else {
                takeDefault = true;
            }
        } catch (ParserConfigurationException e) {
            log.error("", e);
            takeDefault = true;
        } catch (IOException e) {
            log.error("couldn't open config file!", e);
            takeDefault = true;
        } catch (SAXException e) {
            log.error("Couldn't parse config file!", e);
            takeDefault = true;
        }
        if (!takeDefault) {
            return loadedTs.toArray(loadedTsArray);
        } else {
            return new String[] {
                    UIDs.ExplicitVRLittleEndian,
                    UIDs.ImplicitVRLittleEndian,
                    UIDs.JPEG2000Lossless,
                    UIDs.JPEGLossless,
                    UIDs.JPEGBaseline
            };
        }
    }

    /**
     *Constructor for the MoveServer object
     * 
     * @param scp
     *            Description of the Parameter
     */
    public MoveServer(QueryRetrieveSCP scp) {
        this.scp = scp;
        try {
            bean = InitialContext.doLookup(BeansName.LMoveDealer);
        } catch (NamingException nex) {
            log.fatal("Unable to find the MoveDealer bean!", nex);
        }
    }

    // Methods ------------------------------------------------------
    /**
     * Sets the sSLContextAdapter attribute of the StgCmtSCP object
     * 
     * @param tls
     *            The new sSLContextAdapter value
     */
    public void setSSLContextAdapter(SSLContextAdapter tls) {
        this.tls = tls;
    }

    public void setLogConfigFile(String arg) {
        loggersConfFile = arg;
    }

    public String getLogConfigFile() {
        return loggersConfFile;
    }

    public void setLogDir(String arg) {
        loggingDir = arg;
    }

    public String getLogDir() {
        return loggingDir;
    }

    /**
     * Gets the acTimeout attribute of the MoveServer object
     * 
     * @return The acTimeout value
     */
    public int getAcTimeout() {
        return this.acTimeout;
    }

    /**
     * Sets the acTimeout attribute of the MoveServer object
     * 
     * @param timeout
     *            The new acTimeout value
     */
    public void setAcTimeout(int timeout) {
        this.acTimeout = timeout;
    }

    /**
     * Gets the dimseTimeout attribute of the MoveServer object
     * 
     * @return The dimseTimeout value
     */
    public int getDimseTimeout() {
        return this.dimseTimeout;
    }

    /**
     * Sets the dimseTimeout attribute of the MoveServer object
     * 
     * @param timeout
     *            The new dimseTimeout value
     */
    public void setDimseTimeout(int timeout) {
        this.dimseTimeout = timeout;
    }

    /**
     * Gets the soCloseDelay attribute of the MoveServer object
     * 
     * @return The soCloseDelay value
     */
    public int getSoCloseDelay() {
        return this.soCloseDelay;
    }

    /**
     * Sets the soCloseDelay attribute of the MoveServer object
     * 
     * @param delay
     *            The new soCloseDelay value
     */
    public void setSoCloseDelay(int delay) {
        this.soCloseDelay = delay;
    }

    protected MultiDimseRsp doCMove(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        Timer retrieveDataTimer = new Timer();
        String callingAE = assoc.getAssociation().getCallingAET();
        Association origAssoc = assoc.getAssociation();
        Dataset ds = rq.getDataset();
        log.info(callingAE + ": dealing CMove request from "
                + origAssoc.getCallingAET());
        RetrieveData[] toMove = null;
        if (bean == null){
                throw new DcmServiceException(Status.ProcessingFailure);            
        }
        DicomMatch vo = null;
        retrieveDataTimer.start();
        String whatToSearch = "";
        try {
            vo = buildVOs(ds);
            whatToSearch = callingAE + ": ";
            if (vo.patient != null) {
                whatToSearch += "P:" + vo.patient.getPatientId();
            }
            whatToSearch += "/";
            if (vo.study != null) {
                whatToSearch += "St:" + vo.study.getStudyInstanceUid();
            }
            whatToSearch += "/";
            if (vo.series != null) {
                whatToSearch += "Se:" + vo.series.getSeriesInstanceUid();
            }
            whatToSearch += "/";
            if (vo.instance != null) {
                whatToSearch += "I:" + vo.instance.getSopInstanceUid();
            }
            log.info(whatToSearch);
            DicomMatch[] objectToMove = bean.retrieveMatches(vo.patient, vo.study, vo.series, vo.instance, callingAE);
            if(objectToMove!=null)
            	log.info(callingAE + ": Objects to move: " + objectToMove.length);
            else
            	log.info(callingAE + ": NO Objects to move: NULL");
            if ((objectToMove == null) || (objectToMove.length == 0)) {
                String URL = bean.verifyWhereStudiesAre(vo.study.getStudyInstanceUid(), callingAE);
                if (URL!=null && URL.startsWith("dicom")) {
                    log.info(callingAE + ": forwarding request to destination");
                }
                log.info(callingAE + ": FORWARDER is enabled: " + scp.getMoveForwardingEnabled());
                if (scp.getMoveForwardingEnabled()) {
                    log.debug(callingAE + ": MoveServer: forwarding move Request");
                    DcmURL moveDest = new org.dcm4che.util.DcmURL(URL);
                    return new MoveForwarder(assoc, rq, rspCmd, moveDest);
                }else{
                	throw new DcmServiceException(Status.UnableToCalculateNumberOfMatches);
                }
            }
            toMove = buildRetrieved(objectToMove, callingAE);
            objectToMove = null;
        } catch (Exception e) {
            log.error("MoveServer unable to retrieve results ", e);
            toMove = null;
            throw new DcmServiceException(Status.UnableToCalculateNumberOfMatches, e);
        }
        retrieveDataTimer.stop();
        Timer sendingDataTimer = new Timer();
        sendingDataTimer.start();
        try {
            Command cmd = rq.getCommand();
            String destAET = cmd.getString(Tags.MoveDestination);
            AEData destAEAeData = bean.getAeData(destAET);
            log.debug(callingAE + ": Destination : " + destAEAeData);
            
            if(destAEAeData == null){
                log.error("Move Server: Unknown move destination: " + destAET);
                throw new DcmServiceException(Status.MoveDestinationUnknown, destAET);
            }
            ActiveAssociation activeAssoc = null;
            InstancesAction action = null;
            if (toMove.length > 0) {
                action = makeAction(origAssoc, toMove);
                activeAssoc = openAssoc(action.listSOPClassUIDs(), destAEAeData,
                        origAssoc.getCalledAET());
            }
            StorageSCU scu = new StorageSCU(scp, activeAssoc, origAssoc, toMove, destAEAeData, origAssoc.getCallingAET(), cmd.getMessageID(), action);
            
            assoc.addCancelListener(rspCmd.getMessageIDToBeingRespondedTo(), scu);
            
            rspCmd.putUS(Tags.NumberOfRemainingSubOperations, toMove.length);
            rspCmd.putUS(Tags.NumberOfCompletedSubOperations, 0);
            rspCmd.putUS(Tags.NumberOfWarningSubOperations, 0);
            rspCmd.putUS(Tags.NumberOfFailedSubOperations, 0);
            rspCmd.putUS(Tags.Status, Status.Pending);
            log.info(callingAE + ": ### sending data to " + destAET + " at " + destAEAeData.getPort() + " to port " + destAEAeData.getHost() + " ###");
            AuditLogService logService = AuditLogService.getInstance();
            try {
                InstancesAccessedMessage msg = new InstancesAccessedMessage(
                        ActionCode.READ);
                msg.addUserPerson(activeAssoc.getAssociation().getCallingAET(), "",
                        activeAssoc.getAssociation().getCallingAET(), activeAssoc
                                .getAssociation().getSocket().getInetAddress()
                                .toString(), true);
                msg.addUserPerson(activeAssoc.getAssociation().getCalledAET(), "",
                        activeAssoc.getAssociation().getCalledAET(), activeAssoc
                                .getAssociation().getSocket().getLocalAddress()
                                .toString(), false);
                if (vo.patient != null) {
                    msg.addPatient(vo.patient.getPatientId(), vo.patient
                            .getFirstName()
                            + " " + vo.patient.getLastName());
                } else {
                    msg.addPatient("NoPatientID", "NoPatientName");
                }
                if (vo.study != null) {
                    msg.addStudy(vo.study.getStudyInstanceUid(), null);
                }
                logService.SendMessage(msg);
            } catch (Exception e) {
                log.warn("unable to send AuditLogMessage", e);
            }
            sendingDataTimer.stop();
            if (toMove != null) {
                tLogger.toLog(new String[] { tLogger.MOVE, callingAE, retrieveDataTimer.toString(), sendingDataTimer.toString(), whatToSearch, toMove.length + "" });
            }
            activeAssoc = null;
            action = null;
//            destAE = null;
            cmd = null;
            destAET = null;
            toMove = null;
            origAssoc = null;
            ds = null;
            return scu;
        } catch (DcmServiceException e) {
            log.error("", e);
            throw e;
        } catch (Exception e) {
            log.error("", e);
            throw new DcmServiceException(Status.UnableToCalculateNumberOfMatches, e);
        }
    }

    private DicomMatch buildVOs(Dataset ds) throws Exception {
        DicomMatch m = new DicomMatch();
        if (ds.contains(Tags.PatientID)) {
            m.patient = new Patient();
            m.patient.setPatientId(ds.getString(Tags.PatientID));
        } // end if
        if (ds.contains(Tags.StudyInstanceUID)){
        	String uids[]=ds.getStrings(Tags.StudyInstanceUID);
        	StringBuilder uidList=new StringBuilder(uids[0]);	 	// This will be always present
        	int len=uids.length;
        	if(len>1){
        		for(int i=1; i<len; i++)
        			uidList.append(SEPARATOR_DICOM).append(uids[i]);
        	}
            m.study = new Study(uidList.toString(), false);
            
        }
        if (ds.contains(Tags.SeriesInstanceUID)){
        	String uids[]=ds.getStrings(Tags.SeriesInstanceUID);
        	StringBuilder uidList=new StringBuilder(uids[0]);	 	// This will be always present
        	int len=uids.length;
        	if(len>1){
        		for(int i=1; i<len; i++)
        			uidList.append(SEPARATOR_DICOM).append(uids[i]);
        	}
            m.series = new Series(uidList.toString(), false);
            
        }
        if (ds.contains(Tags.SOPInstanceUID))
            m.instance = new NonImage(ds.getString(Tags.SOPInstanceUID));
        // I use NonImage 'cos I need just the UID!
        if ((m.patient != null) || (m.study != null) || (m.series != null) || (m.instance != null))
            return m;
        else
            throw new Exception("No useful data is present in the c-move request."); // If no UIDs were specified!
    } // end

    private RetrieveData[] buildRetrieved(DicomMatch[] dm, String callingAE)
            throws DcmServiceException {
        // DTODO: it builds the urls to instance as well!
        int i = dm.length;
        RetrieveData[] rd = new RetrieveData[i];
        Dataset newData = null;
        StringBuffer pn = null;
        for (i = (i - 1); i >= 0; i--) {
            newData = (Dataset) objFact.newDataset();
            newData.putLO(Tags.PatientID, dm[i].patient.getPatientId());
            newData.putLO(Tags.IssuerOfPatientID, dm[i].patient.getIdIssuer());
            pn = new StringBuffer(64);
            pn.append((dm[i].patient.getLastName() == null) ? ""
                    : dm[i].patient.getLastName());
            pn.append("^");
            pn.append((dm[i].patient.getFirstName() == null) ? ""
                    : dm[i].patient.getFirstName());
            pn.append("^");
            pn.append((dm[i].patient.getMiddleName() == null) ? ""
                    : dm[i].patient.getMiddleName());
            pn.append("^");
            pn.append((dm[i].patient.getPrefix() == null) ? "" : dm[i].patient
                    .getPrefix());
            pn.append("^");
            pn.append((dm[i].patient.getSuffix() == null) ? "" : dm[i].patient
                    .getSuffix());
            String s = pn.toString();
            // log.info("The Patient's name is "+s);
            newData.putPN(Tags.PatientName, s);
            newData.putDA(Tags.PatientBirthDate, dm[i].patient.getBirthDate());
            newData.putCS(Tags.PatientSex, dm[i].patient.getSex());
            newData.putUI(Tags.StudyInstanceUID, dm[i].study
                    .getStudyInstanceUid());
            newData.putUI(Tags.SeriesInstanceUID, dm[i].series
                    .getSeriesInstanceUid());
            newData.putUI(Tags.SOPClassUID, dm[i].instance.getSopClassUid());
            newData.putUI(Tags.SOPInstanceUID, dm[i].instance
                    .getSopInstanceUid());
            // now build the url to the instance:
            StringBuffer url = new StringBuffer(128);
            // log.info("building to move element, step 1");
            String tmpStr = bean.getStudyUrlBase(dm[i].study
                    .getStudyInstanceUid(), callingAE);
            if ((tmpStr != null)
                    && ((!tmpStr.startsWith("dicom")) || (!(tmpStr
                            .indexOf("://") > 0))))
                url.append("file:///");
            url.append(tmpStr);
            url.append("/");
            url.append(dm[i].study.getStudyInstanceUid());
            url.append("/");
            url.append(dm[i].series.getSeriesInstanceUid());
            url.append("/");
            // if(dm[i].instance.getSopInstanceUid()!=null){
            url.append(dm[i].instance.getSopInstanceUid());
            // }
            // else
            // {log.info("SopInstance is missing, probably images are elsewhere and we are dealing with a remote retrive case");}
            String type = null;
            if (dm[i].instance instanceof it.units.htl.dpacs.valueObjects.Image)
                type = "Images";
            else
                type = "non image";
            // log.info("THE URL TO THE INSTANCE is: "+url + ", and type " +
            // type);
            rd[i] = new RetrieveData(newData, url.toString(), type, dm[i].nearlineData);
            url = null;
            newData = null;
            pn = null;
            tmpStr = null;
            s = null;
        }
        dm = null;
        return rd;
    }

    private Socket createSocket(String host, int port, String[] cipherSuites)
            throws DcmServiceException {
        try {
            return cipherSuites.length == 0 ? new Socket(host, port) : tls
                    .getSocketFactory(cipherSuites).createSocket(host, port);
        } catch (Exception e) {
            throw new DcmServiceException(Status.UnableToPerformSuboperations,
                    e);
        }
    }

    private ActiveAssociation openAssoc(String[] cuids, AEData moveDest,
            String scuAET) throws DcmServiceException {
        String[] cipherSuites = moveDest.getCipherSuites();
        AAssociateRQ rq = (AAssociateRQ) assocFact.newAAssociateRQ();
        rq.setCallingAET(scuAET);
        rq.setCalledAET(moveDest.getTitle());
        // added for compatilbility with ge
        // for(int f=0;f<cuids.length;f++){
        // rq.addRoleSelection(cuids[f],true,false);
        // }
        for (int i = 0; i < cuids.length; ++i) {
            for (int j = 0; j < SUPPORTED_TS.length; j++) {
                rq.addPresContext((PresContext) assocFact.newPresContext(rq.nextPCID(),
                        cuids[i], SUPPORTED_TS[j]));
            }
            // this should generate only one pres with all the ts
            // for (int j=0; j<NATIVE_TS.length;j++){
            // rq.addPresContext(assocFact.newPresContext(rq.nextPCID(),
            // cuids[j], NATIVE_TS));
        }
        rq.setAsyncOpsWindow((AsyncOpsWindow) assocFact.newAsyncOpsWindow(0, 1));
        try {
            Association assoc = (Association) assocFact.newRequestor(createSocket(moveDest
                    .getHost(), moveDest.getPort(), cipherSuites));
            assoc.setAcTimeout(acTimeout);
            assoc.setDimseTimeout(dimseTimeout);
            assoc.setSoCloseDelay(soCloseDelay);
            PDU pdu = assoc.connect(rq);
            if (!(pdu instanceof AAssociateAC)) {
                throw new DcmServiceException(
                        Status.UnableToPerformSuboperations, "connection to "
                                + moveDest + " failed: " + pdu);
            }
            ActiveAssociation activeAssoc = (ActiveAssociation) assocFact.newActiveAssociation(
                    (Association) assoc, null);
            activeAssoc.start();
            if (assoc.countAcceptedPresContext() == 0) {
                try {
                    activeAssoc.release(true);
                } catch (Exception e) {
                    log.debug("release Association Failed:", e);
                }
                throw new DcmServiceException(
                        Status.UnableToPerformSuboperations,
                        "no presentation context accepted by " + moveDest);
            }
            return activeAssoc;
        } catch (DcmServiceException e) {
            log.fatal("Error opening association " + moveDest, e);
            throw e;
        } catch (Exception e) {
            log.fatal("Error opening association with " + moveDest, e);
            throw new DcmServiceException(Status.UnableToPerformSuboperations,
                    e);
        }
    }

    private InstancesAction makeAction(Association origAssoc,
            RetrieveData[] toMove) throws DcmServiceException {
        Dataset ds = toMove[0].getDataset();
        InstancesAction action = new InstancesAction("Access", ds
                .getString(Tags.StudyInstanceUID), new Patient(ds
                .getString(Tags.PatientID), ds.getString(Tags.PatientName)));
        action.setAccessionNumber(ds.getString(Tags.AccessionNumber));
        action.setUser((User) alf.newRemoteUser(alf.newRemoteNode(origAssoc
                .getSocket(), origAssoc.getCallingAET())));
        action.setNumberOfInstances(toMove.length);
        for (int i = 0; i < toMove.length; ++i) {
            action.addSOPClassUID(toMove[i].getDataset().getString(
                    Tags.SOPClassUID));
        }
        return action;
    }

    public DicomMoveDealerLocal getMoveBean() {
        log.info("move server reference status");
        if (bean == null){
            try {
                bean = InitialContext.doLookup(BeansName.LMoveDealer);
            } catch (NamingException cex) {
                log.fatal("", cex);
            }
        }// end try...catch
        return this.bean;
    }
}
