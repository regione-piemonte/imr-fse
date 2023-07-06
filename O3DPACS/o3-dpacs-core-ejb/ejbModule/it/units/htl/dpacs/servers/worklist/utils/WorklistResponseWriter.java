/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.worklist.utils;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.DicomServiceException;

public class WorklistResponseWriter implements Runnable {
    private final Log log = LogFactory.getLog(WorklistResponseWriter.class);
    private WorklistResponse _what;
    private Association _assoc;
    private int _presCtxId;

    public WorklistResponseWriter(Association as, int presContId, WorklistResponse what) {
        _what = what;
        _presCtxId = presContId;
        _assoc = as;
    }
    public void run() {
        try {
            do {
                _assoc.writeDimseRSP(_presCtxId, _what.getCommand(), _what.getDataset());
            } while (_what.next());
            _assoc.writeDimseRSP(_presCtxId, _what.getCommand());
        } catch (DicomServiceException e) {
            try {
                log.error(_assoc.getCallingAET() + ": Unable to write response...", e);
                _assoc.writeDimseRSP(_presCtxId, e.getCommand(), e.getDataset());
            } catch (IOException ioex) {
                log.error(_assoc.getCallingAET() + ":Unable to write exception response...", ioex);
            }
        } catch (Throwable e) {
            log.error(_assoc.getCallingAET() + ": while sending response...", e);
            _assoc.abort();
        } 
    }
}
