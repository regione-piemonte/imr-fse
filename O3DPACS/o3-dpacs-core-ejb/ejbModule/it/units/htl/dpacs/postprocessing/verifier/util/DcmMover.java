/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.TransferCapability;



public class DcmMover {
    private static Logger log = Logger.getLogger(DcmMover.class);
    private Device device;
    private Executor executor;
    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
    private DcmCommonObject.QueryRetrieveLevel qrlevel = DcmCommonObject.QueryRetrieveLevel.STUDY;
    private NetworkConnection conn = new NetworkConnection();
    private NetworkConnection remoteConn = new NetworkConnection();
    private NetworkApplicationEntity localAE = new NetworkApplicationEntity();
    private Association assoc;

    public DcmMover(String remoteAEtitle, String remoteIp, int remotePort, String localAEtitle,  int localPort) {
        //taskid:330912 bug:38483
        log.info("DcmMover parameters|remoteAEtitle:" + remoteAEtitle + " remoteIp:" + remoteIp + " remotePort:" + remotePort + " localAEtitle:" + localAEtitle + " localPort:" + localPort);

        device = new Device("dcmMover");
        executor = new NewThreadExecutor("dcmMover");
        remoteConn.setPort(remotePort);
        remoteConn.setHostname(remoteIp);
        remoteAE.setNetworkConnection(remoteConn);
        remoteAE.setAETitle(remoteAEtitle);
        device.setNetworkApplicationEntity(localAE);
        device.setNetworkConnection(conn);
        conn.setPort(localPort);
        localAE.setNetworkConnection(conn);
        localAE.setAETitle(localAEtitle);
//        log.info("Test the configuration: " + doEcho());
    }
    
    public void doMove(String what, String where) throws ConfigurationException, IOException, InterruptedException, Exception {
        log.debug("Let's to send a move request!");
        localAE.setTransferCapability(
                new TransferCapability[] {
                new TransferCapability(UID.StudyRootQueryRetrieveInformationModelMOVE,
                new String[] { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian }, TransferCapability.SCU) });
        DicomObject studyToMove = new BasicDicomObject();
        setQueryLevel(qrlevel, studyToMove);
        String[] moveSopClass = qrlevel.getMoveClassUids();
        studyToMove.putString(Tag.StudyInstanceUID, null, what);
        assoc = localAE.connect(remoteAE, executor);
        // List<DicomObject> results = new ArrayList<DicomObject>();
        TransferCapability tc = selectTransferCapability(moveSopClass);
        String cuids = tc.getSopClass();
        String transferSyntax = selectTransferSyntax(tc);
        DimseRSPHandler rspHandler = new DimseRSPHandler() {
            public void onDimseRSP(Association as, DicomObject cmd, DicomObject data) {
                onMoveRSP(as, cmd, data);
            }
        };
        assoc.cmove(cuids, 0, studyToMove, transferSyntax, where, rspHandler);
        assoc.waitForDimseRSP();
        Exception e = getAssocException();
        if (e != null) {
            log.error("Exception ocurred on the association while retrieving: ", e);
            throw e;
        } else if (isAbortingMove(moveStatus)) {
            StringBuffer str = new StringBuffer("Query/Retrieve SCP terminated move prematurally. Move status = ");
            str.append(Integer.toHexString(moveStatus));
            if (moveError != null) {
                str.append(", Move error = " + moveError);
            }
            log.error(str);
            throw new Exception("MoveStatus: " + Integer.toHexString(moveStatus) + ((moveError != null)?"|moveError:"+moveError:""));
        }else if(moveStatus == 0xc000){
                log.error("Unable to process the requested move. MoveStatus: " + Integer.toHexString(moveStatus) + " Study is: " + what);
                throw new Exception("MoveStatus: " + Integer.toHexString(moveStatus));
        }
        assoc.release(true);
    }

    public String selectTransferSyntax(TransferCapability tc) {
        String[] tcuids = tc.getTransferSyntax();
        if (Arrays.asList(tcuids).indexOf(UID.DeflatedExplicitVRLittleEndian) != -1) {
            return UID.DeflatedExplicitVRLittleEndian;
        }
        return tcuids[0];
    }

    private TransferCapability selectTransferCapability(String[] cuid) {
        TransferCapability tc;
        for (int i = 0; i < cuid.length; i++) {
            tc = assoc.getTransferCapabilityAsSCU(cuid[i]);
            if (tc != null) {
                return tc;
            }
        }
        return null;
    }

    int moveStatus, completed, warning, failed;
    String moveError;

    protected void onMoveRSP(Association as, DicomObject cmd, DicomObject data) {
        if (!CommandUtils.isPending(cmd)) {
            moveStatus = cmd.getInt(Tag.Status);
            if (isAbortingMove(moveStatus)) {
                checkError(cmd);
            } else {
                completed += cmd.getInt(Tag.NumberOfCompletedSuboperations);
                warning += cmd.getInt(Tag.NumberOfWarningSuboperations);
                failed += cmd.getInt(Tag.NumberOfFailedSuboperations);
            }
            log.debug("Move status : " + moveStatus);
            log.debug("Completed   : " + completed);
            log.debug("Warning     : " + warning);
            log.debug("Failed      : " + failed);
        }
    }

    protected boolean isAbortingMove(int moveStatus) {
        switch (moveStatus) {
        case 0xa701:
        case 0xa702:
        case 0xa801: //uknown destination exception
        case 0xa900:
        case 0xc002:
        case 0xfe00:
        case 0xb000:
            return true;
        case 0xff00:
        case 0xff01:
        case 0x0000:
        case 0xc000:
            return false;
        default:
            return true;
        }
    }

    protected void checkError(DicomObject moveRspCmd) {
        String error = moveRspCmd.getString(Tag.ErrorComment);
        if (null != error) {
            moveError = error;
        }
    }

    public Exception getAssocException() {
        final String fn = "getAssocException: ";
        Method assocGetException = null;
        Exception assocE = null;
        try {
            assocGetException = assoc.getClass().getDeclaredMethod("getException", new Class[0]);
            assocGetException.setAccessible(true);
        } catch (Exception e) {
            log.error(fn + "Failed to check for association exception.", e);
        }
        try {
            assocE = (Exception) assocGetException.invoke(assoc, new Object[0]);
        } catch (Exception e) {
            log.error(fn + "Failed to check for association exception.", e);
        }
        return assocE;
    }

    private void setQueryLevel(DcmCommonObject.QueryRetrieveLevel qrlevel, DicomObject prova) {
        log.debug("Set queries level " + qrlevel.getCode());
        this.qrlevel = qrlevel;
        prova.putString(Tag.QueryRetrieveLevel, VR.CS, qrlevel.getCode());
        for (int tag : qrlevel.getReturnKeys()) {
            prova.putNull(tag, null);
        }
    }
}
