/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.storage;

import it.units.htl.dpacs.dao.DicomStgCmtDealerLocal;
import it.units.htl.dpacs.helpers.AEData;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.valueObjects.DicomConstants;
import it.units.htl.dpacs.valueObjects.Instance;
import it.units.htl.dpacs.valueObjects.NonImage;

import java.io.IOException;
import java.net.Socket;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
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
import org.dcm4che.net.RoleSelection;
import org.dcm4che.util.SSLContextAdapter;

class StorageCommitServer extends DcmServiceBase {
    private static final String[] NATIVE_TS = {UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian};
    private static final DcmObjectFactory objFact = DcmObjectFactory.getInstance();
    private static final AssociationFactory assocFact = AssociationFactory.getInstance();
//    private final StorageSCP scp;
    private SSLContextAdapter tls = null;
    private int acTimeout;
    private int dimseTimeout;
    private int soCloseDelay;
    private DicomStgCmtDealerLocal bean = null;
    private static Log log = LogFactory.getLog(StorageCommitServer.class);

    /**
     *Constructor for the StorageCommitServer object
     *
     * @param  scp  The storage SCP the StgCmt is associated
     */
    public StorageCommitServer(StorageSCP scp) {
//        this.scp = scp;
    }

    // Methods ------------------------------------------------------
    /**
     *  Sets the sSLContextAdapter attribute of the StgCmtSCP object
     *
     * @param  tls  The new sSLContextAdapter value
     */
    public void setSSLContextAdapter(SSLContextAdapter tls) {
        this.tls = tls;
    }

    /**
     *  Gets the acTimeout attribute of the StorageCommitServer object
     *  AC Timeout is the time the SCU waits for association being negotiated
     *  with the SCP before aborting the attempt
     *  It's in ms.
     * @return    The acTimeout value
     */
    public int getAcTimeout() {
        return this.acTimeout;
    }

    /**
     *  Sets the acTimeout attribute of the StorageCommitServer object
     *  AC Timeout is the time the SCU waits for association being negotiated
     *  with the SCP before aborting the attempt
     *  It's in ms.
     * @param  timeout  The new acTimeout value
     */
    public void setAcTimeout(int timeout) {
        this.acTimeout = timeout;
    }

    /**
     *  Gets the dimseTimeout attribute of the StorageCommitServer object
     *  Dimse timeout is the max value of time the DICOM service may take
     *  to be performed
     * @return    The dimseTimeout value
     */
    public int getDimseTimeout() {
        return this.dimseTimeout;
    }

    /**
     *  Sets the dimseTimeout attribute of the StorageCommitServer object
     *   Dimse timeout is the max value of time the DICOM service may take
     *  to be performed
     * @param  timeout  The new dimseTimeout value
     */
    public void setDimseTimeout(int timeout) {
        this.dimseTimeout = timeout;
    }

    /**
     *  Gets the soCloseDelay attribute of the StorageCommitServer object
     *   soCloseDelay is socket close delay
     * @return    The soCloseDelay value
     */
    public int getSoCloseDelay() {
        return this.soCloseDelay;
    }

    /**
     *  Sets the soCloseDelay attribute of the StorageCommitServer object
     *  soCloseDelay is socket close delay
     * @param  delay  The new soCloseDelay value
     */
    public void setSoCloseDelay(int delay) {
        this.soCloseDelay = delay;
    }

    // DcmServiceBase overwrites -------------------------------------
    /**
     * The method receives a N-ACTION message associated to StgCmt SOP classes
     *
     * @param  assoc                    the current association
     * @param  rq                       the request
     * @param  rspCmd                   the future response
     * @return                          the dataset to be returned
     * @exception  DcmServiceException  Description of the Exception
     * @exception  IOException          Description of the Exception
     */
    protected Dataset doNAction(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws DcmServiceException, IOException {
        String callingAE = assoc.getAssociation().getCallingAET();
        if (bean == null) {
            try {
                bean = InitialContext.doLookup(BeansName.LDicomStorageCommitmentDealer);
            } catch (NamingException cex) {
                throw new DcmServiceException(Status.ProcessingFailure);
            } 
        }
        log.debug(callingAE + ": dealing with an NAction request");
        Command cmd = rq.getCommand();
        Dataset ds = rq.getDataset();
        if (!UIDs.StorageCommitmentPushModelSOPInstance.equals(cmd.getRequestedSOPInstanceUID())) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        if (cmd.getInt(Tags.ActionTypeID, -1) != 1) {
            throw new DcmServiceException(Status.NoSuchActionType).setActionTypeID(cmd.getInt(Tags.ActionTypeID, -1));
        }
        if (ds.vm(Tags.TransactionUID) != 1 || !ds.contains(Tags.RefSOPSeq)) {
            throw new DcmServiceException(Status.InvalidArgumentValue);
        }
        Association a = assoc.getAssociation();
        String[] tempAe = bean.getAEData(a.getCallingAET());
        if (tempAe[2] == null) {
            throw new DcmServiceException(Status.ProcessingFailure, "Unkown AE: " + a.getCallingAET());
        }
        AEData ae = new AEData(tempAe[0], tempAe[1], Integer.parseInt(tempAe[2]), tempAe[3], (DicomConstants.MOBILE.equals(tempAe[4])) ? true : false);
        String transUID = ds.getString(Tags.TransactionUID);
        if (transUID == null) {
            throw new DcmServiceException(Status.InvalidArgumentValue, "Transaction UID Missing");
        }
        DcmElement refSeq = ds.get(Tags.RefSOPSeq);
        if (refSeq == null) {
            throw new DcmServiceException(Status.InvalidArgumentValue, "Referenced SOP Sequence Missing");
        }
        a.putProperty("stgcmt.toVerify." + cmd.getMessageID(), ds);
        a.putProperty("stgcmt.ae." + cmd.getMessageID(), ae);
        log.debug(callingAE + ": finished dealing with an NAction request");
        cmd = null;
        ds = null;
        a = null;
        tempAe = null;
        ae = null;
        refSeq = null;
        return null;
    }

    /**
     * Builds NonImages with SOPClassUID and SOPInstanceUID
     * @param de
     * @return
     */
    private Instance[] prepareVOs(DcmElement de) {
        // I have to build NonImages with SOPClassUID and SOPInstanceUID
        //wrong method: log.info("dcm element sequence multiplicity: "+de.length())	;
        log.info("dcm element sequence multiplicity: "+de.countItems())	;
        Instance[] r = new Instance[de.countItems()];
        for (int i = de.countItems() - 1; i >= 0; i--) {
            Dataset item = de.getItem(i);
            r[i] = new NonImage(item.getString(Tags.RefSOPInstanceUID), item.getString(Tags.RefSOPClassUID));
            item = null;
        }
        return r; // This returns an array which contains all the instances to be committed!
    }

    /**
     * Convenience method
     * @param i
     * @param d
     * @return
     */
    private Dataset prepareDs(Instance[] i, Dataset d) {
        DcmElement success = d.putSQ(Tags.RefSOPSeq);
        DcmElement failed = null;
        for (int j = i.length - 1; j >= 0; j--) {
            // Now go through all instances and update the sequences in the dataset
            Dataset item = objFact.newDataset();
            item.putUI(Tags.RefSOPClassUID, i[j].getSopClassUid());
            item.putUI(Tags.RefSOPInstanceUID, i[j].getSopInstanceUid());
            if (i[j].isDeprecated()) {
                // I use this attribute to show success or failure
                success.addItem(item);
            } else {
                if (failed == null) {
                    failed = d.putSQ(Tags.FailedSOPSeq);
                }
                item.putUS(Tags.FailureReason, Status.NoSuchObjectInstance); // This is the only reason why DPACS could not be able to commit an instance
                failed.addItem(item);
            } 
        }

        return d;
    }

    /**
     *  After sending the N-ACTION response, and after habing checked for instances,
     * you send an N-EVENT report message to report the outcome of your verification
     *
     * @param  assoc  Description of the Parameter
     * @param  rsp    Description of the Parameter
     */
    protected void doAfterRsp(ActiveAssociation assoc, Dimse rsp) {
        int msgID = rsp.getCommand().getMessageIDToBeingRespondedTo();
        Association a = assoc.getAssociation();
        //get the dataset have to verify
        Dataset toVerify = (Dataset)a.getProperty("stgcmt.toVerify." + msgID);
        DcmElement refSeq = toVerify.get(Tags.RefSOPSeq);
        Dataset resDs = objFact.newDataset();
        String transUID = toVerify.getString(Tags.TransactionUID);        
        resDs.putUI(Tags.TransactionUID, transUID);
        Instance[] ins = prepareVOs(refSeq);
        ins = bean.commitStorage(ins);
      	resDs = prepareDs(ins, resDs);
        AEData ae = (AEData) a.getProperty("stgcmt.ae." + msgID);
        if (ae != null) {
            sendResult(a.getCalledAET(), ae, resDs);
            log.info(ae + ": Commitment results sended.");
        } else {
            log.warn("Couldn't send commitment results, no CalledAET.");
        }
    }

    /**
     * N-Event is sent of a new association. The methods opens a new socket with
     * the parameters specified in the knownNodes table
     * @param host host
     * @param port port
     * @param cipherSuites cipherSuites
     * @return the socket
     * @throws java.lang.Exception
     */
    private Socket createSocket(String host, int port, String[] cipherSuites) throws Exception {
        return cipherSuites.length == 0 ? new Socket(host, port) : tls.getSocketFactory(cipherSuites).createSocket(host, port);
    }

    /**
     * Override to make an association for N-EVENT report
     * @param calledAET
     * @param ae aeDATA, an object containing the node information
     * @return AAssociateRQ
     */
    private AAssociateRQ makeAAssociateRQ(String calledAET, AEData ae) {
        AAssociateRQ rq = assocFact.newAAssociateRQ();
        rq.setCallingAET(calledAET);
        rq.setCalledAET(ae.getTitle());
        rq.addPresContext(assocFact.newPresContext(1, UIDs.StorageCommitmentPushModel, NATIVE_TS));
        rq.addRoleSelection(assocFact.newRoleSelection(UIDs.StorageCommitmentPushModel, false, true));
        return rq;
    }

    /**
     *
     * @param ac
     * @return
     */
    private boolean checkAAssociateAC(AAssociateAC ac) {
        RoleSelection rs = ac.getRoleSelection(UIDs.StorageCommitmentPushModel);
        return ac.getPresContext(1).result() == PresContext.ACCEPTANCE && rs != null && rs.scp();
    }

    /**
     * makes NEvent Report RQ
     * @param result
     * @return
     */
    private Dimse makeNEventReportRQ(Dataset result) {
        Command cmd = objFact.newCommand();
        cmd.initNEventReportRQ(1, UIDs.StorageCommitmentPushModel, UIDs.StorageCommitmentPushModelSOPInstance, result.contains(Tags.FailedSOPSeq) ? 2 : 1);
        return assocFact.newDimse(1, cmd, result);
    }

    /**
     * sends the result of commitment via N-EVENT report
     * @param calledAET
     * @param ae aeDATA, an object containing the node information
     * @param result
     */
    private void sendResult(String calledAET, AEData ae, Dataset result) {
        //log.info(callingAE+": StorageCommitServer sending the response");
        //log.debug("StorageCommitServer sending the response");
        try {
            Association assoc = assocFact.newRequestor(createSocket(ae.getHost(), ae.getPort(), ae.getCipherSuites()));
            assoc.setAcTimeout(acTimeout);
            assoc.setDimseTimeout(dimseTimeout);
            assoc.setSoCloseDelay(soCloseDelay);
            PDU pdu = assoc.connect(makeAAssociateRQ(calledAET, ae));
            if (!(pdu instanceof AAssociateAC)) {
                log.error("connection to " + ae + " failed: " + pdu);
                return;
            }
            ActiveAssociation activeAssoc = assocFact.newActiveAssociation(assoc, null);
            activeAssoc.start();
            if (checkAAssociateAC((AAssociateAC) pdu)) {
                //scp.logDataset("Storage Commitment Result:\n", result);
                FutureRSP rsp = activeAssoc.invoke(makeNEventReportRQ(result));
                Command rspCmd = rsp.get().getCommand();
                if (rspCmd.getStatus() != Status.Success) {
                    log.debug("" + ae + " returns N-EVENT-REPORT with severe status: " + rspCmd);
                }
            } else {
                log.error("storage commitment (requestor=SCP) rejected by " + ae);
            }
            try {
                activeAssoc.release(false);
            } catch (Exception e) {
                log.debug("release association to " + ae + " failed:" + e.getMessage());
            }
        } catch (Exception e) {
            //log.debug("StorageCommitServer failed to send stgcmt result to "+ae+" "+e.getMessage());
            log.fatal("StorageCommitServer failed to send stgcmt result to " + ae, e);
        }
    }
}