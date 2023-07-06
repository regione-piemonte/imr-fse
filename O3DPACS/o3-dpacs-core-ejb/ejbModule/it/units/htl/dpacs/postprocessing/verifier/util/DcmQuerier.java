/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.TransferCapability;



/**
 * @author Matteo Sangalli
 * 
 */
public class DcmQuerier {
    private static Logger log = Logger.getLogger(DcmQuerier.class);
    private Device device;
    private Executor executor;
    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
    private DcmCommonObject.QueryRetrieveLevel qrlevel = DcmCommonObject.QueryRetrieveLevel.STUDY;
    private NetworkConnection conn = new NetworkConnection();
    private NetworkConnection remoteConn = new NetworkConnection();
    private NetworkApplicationEntity localAE = new NetworkApplicationEntity();
    private Association assoc;
    private DicomObject searchKeys;

    public DcmQuerier(String remoteAEtitle, String remoteIp, int remotePort, String localAEtitle, int localPort, int queryTimeout) {
        //taskid:330912 bug:38483
        log.info("DcmQuerier parameters|remoteAEtitle:" + remoteAEtitle + " remoteIp:" + remoteIp + " remotePort:" + remotePort + " localAEtitle:" + localAEtitle + " localPort:" + localPort + " queryTimeout:" +queryTimeout);

        device = new Device("dcmQueryRetriever");
        executor = new NewThreadExecutor("dcmQueryRetriever");
        remoteConn.setPort(remotePort);
        remoteConn.setHostname(remoteIp);
        remoteAE.setNetworkConnection(remoteConn);
        remoteAE.setAETitle(remoteAEtitle);
        
        device.setNetworkApplicationEntity(localAE);
        device.setNetworkConnection(conn);
        conn.setPort(localPort);
        localAE.setNetworkConnection(conn);
        localAE.setAETitle(localAEtitle);
        localAE.setTransferCapability(new TransferCapability[] { new TransferCapability(UID.StudyRootQueryRetrieveInformationModelFIND, new String[] { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian }, TransferCapability.SCU) });
        
        conn.setRequestTimeout(queryTimeout*1000); 
        conn.setAcceptTimeout(queryTimeout*1000); 
        remoteConn.setRequestTimeout(queryTimeout*1000); 
        remoteConn.setAcceptTimeout(queryTimeout*1000); 
    }

    /**
     * 
     * this method set the filter for the query
     * 
     * @param matchingKeys
     *            filters applies on the query (DicomTagName, Value)
     */
    private void setFilters(HashMap<Integer, String> matchingKeys) {
        //taskid:330912 bug:38483
        log.info("Query filter: ");
        for (Integer key : matchingKeys.keySet()) {
            //taskid:330912 bug:38483
            log.info(key + " = " + matchingKeys.get(key));
            searchKeys.putString(key, null, matchingKeys.get(key));
        }
    }

    public List<DicomObject> doQuery(HashMap<Integer, String> matchingKeys, DcmCommonObject.QueryRetrieveLevel qrLevel) throws ConfigurationException, IOException, InterruptedException {
        searchKeys = new BasicDicomObject();
        setQueryLevel(qrLevel);
        String[] findSopClassUids = qrlevel.getFindClassUids();
        List<DicomObject> results = new ArrayList<DicomObject>();
        setFilters(matchingKeys);
        assoc = localAE.connect(remoteAE, executor);
        TransferCapability tc = selectTransferCapability(findSopClassUids);
        String cuids = tc.getSopClass();
        String transferSyntax = selectTransferSyntax(tc);
        DimseRSP rsp = assoc.cfind(cuids, 0, searchKeys, transferSyntax, 0);
        while (rsp.next()) {
            DicomObject cmd = rsp.getCommand();
            if (CommandUtils.isPending(cmd)) {
                DicomObject data = rsp.getDataset();
                results.add(data);
            }
        }
        assoc.release(true);
        return results;
    }

    public String selectTransferSyntax(TransferCapability tc) {
        String[] tcuids = tc.getTransferSyntax();
        if (Arrays.asList(tcuids).indexOf(UID.DeflatedExplicitVRLittleEndian) != -1)
            return UID.DeflatedExplicitVRLittleEndian;
        return tcuids[0];
    }

    public TransferCapability selectTransferCapability(String[] cuid) {
        TransferCapability tc;
        for (int i = 0; i < cuid.length; i++) {
            tc = assoc.getTransferCapabilityAsSCU(cuid[i]);
            if (tc != null)
                return tc;
        }
        return null;
    }

    private void setQueryLevel(DcmCommonObject.QueryRetrieveLevel qrlevel) {
        log.debug("Set queries level " + qrlevel.getCode());
        this.qrlevel = qrlevel;
        searchKeys.putString(Tag.QueryRetrieveLevel, VR.CS, qrlevel.getCode());
        for (int tag : qrlevel.getReturnKeys()) {
            searchKeys.putNull(tag, null);
        }
    }
}
