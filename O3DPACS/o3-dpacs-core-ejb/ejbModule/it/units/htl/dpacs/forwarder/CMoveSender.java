/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.forwarder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;

import javax.net.SocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4che.util.DcmURL;

public class CMoveSender {
    static final Log log = LogFactory.getLog(CMoveSender.class);
    private static CMoveSender inst = null;
    private static DcmObjectFactory oFact = DcmObjectFactory.getInstance();
    private static AssociationFactory aFact = AssociationFactory.getInstance();
    private static UIDDictionary uidDict = DictionaryFactory.getInstance().getDefaultUIDDictionary();
    private static final byte PCID = (byte) 1;
    private boolean cancelRequest;

    private CMoveSender() {
    }

    public static CMoveSender getInstance() {
        if (inst == null) {
            inst = new CMoveSender();
        }
        return inst;
    }

    public boolean cmove(String studyToMove, String dstAETitle, String targetIp, String targetPort, String sourceAETitle, String dicomProtocol) {
        String plainUrl = dicomProtocol + "://" + sourceAETitle + ":" + dstAETitle + "@" + targetIp + ":" + targetPort;
        DcmURL url = getQueryDest(plainUrl);
        String destAET = dstAETitle;
        SocketFactory sf = SocketFactory.getDefault();
        int opDone = 0;
        int opLeft = 0;
        int opWarning = 0;
        int opFailed = 0;
        String[] transferSyntax = { UIDs.ImplicitVRLittleEndian };
        String infoModelUid = UIDs.StudyRootQueryRetrieveInformationModelMOVE;
        PresContext movePc = aFact.newPresContext(PCID, infoModelUid, transferSyntax);
        DcmCommunication dc = null;
        try {
            dc = associate(url, sf, movePc, true);
        } catch (IOException e2) {
            log.error("", e2);
        }
        int status = Status.ProcessingFailure;
        boolean exceptionOccurred=false;
        if (dc != null) {
            DcmSubOpProgress currProgress = dc.getSubOpsProgress();
            Command cmd = oFact.newCommand();
            int msgId = dc.nextMessageID();
            cmd.initCMoveRQ(msgId, infoModelUid, 0, destAET);
            Dataset ds = oFact.newDataset();
            ds.putCS(Tags.QueryRetrieveLevel, "STUDY");
            if (studyToMove != null) {
                ds.putUI(Tags.StudyInstanceUID, studyToMove);
            }
            Dimse dimse = aFact.newDimse(PCID, cmd, ds);
            logCommand("Sending", cmd, ds);
            try {
                dc.getAssociation().write(dimse);
            } catch (IOException e1) {
                log.error("", e1);
            }
            currProgress.reset();
            setCancelRequest(false);
            Dimse rsp = null;
            //Dataset data = null;
            Command rCmd = null;
            try {
                do {
                    if (getCancelRequest()) {
                        issueCCancel(dc.getAssociation(), msgId);
                        setCancelRequest(false);
                    }
                    rsp = dc.getAssociation().read();
                    rCmd = rsp.getCommand();
                    logCommand("Received", rCmd);
                    opLeft = rCmd.getInt(Tags.NumberOfRemainingSubOperations, -1);
                    opDone = rCmd.getInt(Tags.NumberOfCompletedSubOperations, -1);
                    opFailed = rCmd
                            .getInt(Tags.NumberOfFailedSubOperations, -1);
                    opWarning = rCmd.getInt(Tags.NumberOfWarningSubOperations,
                            -1);
                    log.info("\t  Operations left: " + opLeft + ", done: "
                            + opDone + ", failed: " + opFailed + ", warning: "
                            + opWarning);
                    currProgress.writeStatus(new DcmSubOpProgress.DcmSubOpStatus(opLeft, opDone, opFailed, opWarning));
                    if ((opLeft + opDone + opFailed + opWarning) <= 0)
                        break;
                } while (rCmd.isPending());
            } catch (IOException e) {
                log.error("", e);
                exceptionOccurred=true;
            } finally {
                currProgress.end();
            }
            if ((rsp != null)&&(!exceptionOccurred))
                status = rsp.getCommand().getStatus();
            String releaseRP = "";
            try {
                releaseRP = dc.releaseAssociation();
            } catch (IOException e) {
                log.error("", e);
            }
            log.info(releaseRP);
        }
        if (status == Status.Success)
            return true;
        else
            return false;
    }

    private void logCommand(String intro, Command dcmCmd) {
        logCommand(intro, dcmCmd, null);
    }

    @SuppressWarnings("rawtypes")
    private void logCommand(String intro, Command dcmCmd, Dataset dcmDs) {
        Iterator iter;
        String logMsg = intro + " command " + dcmCmd.toString();
        if (dcmCmd.isResponse()) {
            logMsg = logMsg + " #Status: " + dcmCmd.getStatus() + "("
                    + Status.toString(dcmCmd.getStatus()) + ")";
        }
        log.info(logMsg);
        if (dcmCmd.isRequest()) {
            iter = dcmCmd.iterator();
            while (iter.hasNext()) {
                log.debug("  " + iter.next().toString());
            }
        }
        if (dcmDs != null) {
            logDataset(" associated", dcmDs);
        }
    }

    public synchronized boolean getCancelRequest() {
        return cancelRequest;
    }

    public synchronized void setCancelRequest(boolean cancel) {
        cancelRequest = cancel;
    }

    @SuppressWarnings({ "rawtypes", "deprecation" })
    private void logDataset(String intro, Dataset ds) {
        log.info(intro + " dataset " + ds.toString());
        Iterator iter = ds.iterator();
        DcmElement de;
        while (iter.hasNext()) {
            de = (DcmElement) iter.next();
            log.info("  " + de.toString());
            if (de.hasItems()) {
                for (int j = 0; j < de.vm(); j++)
                    logDataset("   item " + j, de.getItem(j));
                log.info("\n      End of sequence.");
            }
        }
    }

    /* ____ */// /********** OTHER METHODS **********/
    /* ____ */// /** Nota: usa un solo presentation context, anche se secondo lo
    // standard se ne puo' presentare piu' di uno alla volta. */
    @SuppressWarnings("unused")
    private DcmCommunication associate(DcmURL url, SocketFactory sf,
            PresContext pc, boolean requestProgressGeneration)
            throws IOException {
        DcmCommunication dc = null;
        AAssociateRQ assocRQ = aFact.newAAssociateRQ();
        System.out.println(url.getCalledAET());
        System.out.println(url.getCallingAET());
        assocRQ.setCalledAET(url.getCalledAET());
        assocRQ.setCallingAET(url.getCallingAET());
        assocRQ.setAsyncOpsWindow(aFact.newAsyncOpsWindow(0, 1));
        assocRQ.addPresContext(pc);
        /* ____ */// Open Association
        log.info("Apertura socket verso " + url.getHost() + ":"
                + url.getPort());
        /* ____ */// String [] array = sf.
        Socket sock = sf.createSocket(url.getHost(), url.getPort());
        OutputStream os = sock.getOutputStream();
        /* ____ */// sock.sendUrgentData(100);
        Association assoc = aFact.newRequestor(sock);
        /* ____ */// non imposto i valori di AcTimeout, DimseTimeout,
        // SoCloseDelay (usa i default della libreria, 5000, 0, 500)
        PDU assocAC = assoc.connect(assocRQ);
        if (assocAC instanceof AAssociateAC) { /* ____ */// forse perch? pu?
            // dare null?
            /* ____ */// Controlla se e' stata accettata la transfer syntax del
            // primo pc presentato
            if (assoc.getAcceptedTransferSyntaxUID((byte) 1) == null) {
                log.info("Presentation Context for "
                        + uidDict.lookup(pc.getAbstractSyntaxUID()).name
                        + "was not accepted by " + assoc.getCalledAET());
                /* ____ */// rimesso a posto
                assoc.release(4000);
                /* ____ */// ALDO - aumentato il tempo di release - non era
                // sufficiente con vi-ehr
                /* ____ */// assoc.release(40000);
            } else {
                /* ____ */// Associazione stabilita!
                dc = new DcmCommunication(assoc, requestProgressGeneration);
            }
        }
        return dc;
    }

    private void issueCCancel(Association as, int msgID) throws IOException {
        /* __*__ */// Effettuare i controlli necessari sull'associazione (aperta, stabilita..)
        log.info("Before C_Cancel - Association state = " + as.getState());
        if (as.getState() == Association.ASSOCIATION_ESTABLISHED) {
            log.info("Sending C_Cancel request!");
            Command cmd = oFact.newCommand();
            cmd.initCCancelRQ(msgID);
            Dimse dimse = aFact.newDimse(PCID, cmd);
            log.info("Sending command " + cmd.toString());
            as.write(dimse);
            /* __*__ */// cancelRequest = false;
        } else {
            log.info("C_Cancel request not sent! (association state <> 'established')");
        }
    }

    private DcmURL getQueryDest(String destination) {
        DcmURL tmpDestURL = null;
        if (destination != null) {
            tmpDestURL = new DcmURL(destination);
            int port = tmpDestURL.getPort();
            if ((port < 1) | (port > 65535))
                throw new java.lang.IllegalArgumentException(
                        "Port value out of range: " + port);
        }
        return tmpDestURL;
    }
}
