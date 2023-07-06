/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.worklist.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.Status;

public class WorklistResponse implements DimseRSP {
    private final Log log = LogFactory.getLog(WorklistResponse.class);
    private DicomObject _responseCommand;
    private ArrayList<DicomObject> _dataToSend;
    private boolean _cancelStatus;
    private String callingAe;
        
    public WorklistResponse(DicomObject responseCommand, ArrayList<DicomObject> dataToSend, Association sendChannel) {
        _responseCommand = responseCommand;
        _dataToSend = dataToSend;
        callingAe = sendChannel.getCallingAET();
        _responseCommand.putInt(Tag.Status, VR.US, Status.Pending);
    }

    public void cancel(Association a) throws IOException {
        log.info(a.getCallingAET() + ": received a c-cancel request!");
        _cancelStatus = true;
    }

    public DicomObject getCommand() {
        return _responseCommand;
    }

    public DicomObject getDataset() {
        if(_dataToSend.size() == 0) return null;
        DicomObject current = _dataToSend.get(0);
        if(!current.contains(Tag.SpecificCharacterSet)){
            current.putNull(Tag.SpecificCharacterSet, VR.CS);
        }
        _dataToSend.remove(0);
        _dataToSend.trimToSize();
        return current;       
    }
    public synchronized boolean next() throws IOException, InterruptedException {
        if (_cancelStatus) {
            _responseCommand.putInt(Tag.Status, VR.US, Status.Cancel);
        } else {
            try {
                if (!_dataToSend.isEmpty() || _dataToSend.size() != 0) {
                        _responseCommand.putInt(Tag.Status, VR.US, Status.Pending);
                        _responseCommand.putInt(Tag.NumberOfRemainingSuboperations, VR.US, _dataToSend.size());
                        return true;
                }
            } catch (Exception e) {
                log.error(callingAe + ": while building WorklistResponse.", e);
                _responseCommand.putInt(Tag.Status, VR.US, Status.ProcessingFailure);
                _responseCommand.putString(Tag.ErrorComment, VR.LO, e.getMessage()); 
            }
        }
        _responseCommand.putInt(Tag.Status, VR.US, Status.Success);
        return false;
    }

}
