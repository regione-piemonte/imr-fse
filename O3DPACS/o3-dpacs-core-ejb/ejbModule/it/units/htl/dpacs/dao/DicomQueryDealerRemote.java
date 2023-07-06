/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.valueObjects.DicomMatch;
import it.units.htl.dpacs.valueObjects.Instance;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.dpacs.valueObjects.Series;
import it.units.htl.dpacs.valueObjects.Study;

import javax.ejb.Remote;

@Remote
public interface DicomQueryDealerRemote {
    // No Exceptions need to be thrown, except for application ones

    public DicomMatch[] patientRootMatch(Patient p, Study st, Series se, String callingAE)
            throws java.rmi.RemoteException;

    public DicomMatch[] patientRootMatch(Patient p, Study st, int limit, String callingAE, boolean dep)
            throws java.rmi.RemoteException;

    public DicomMatch[] patientRootMatch(Patient p, int limit, String callingAE)
            throws java.rmi.RemoteException;

    public DicomMatch[] queryInstanceLevel(Patient p, Study st, Series se,
            Instance inst, String callingAE) throws java.rmi.RemoteException;

    public DicomMatch[] studyRootMatch(Patient p, Study st, Series se, String callingAE)
            throws java.rmi.RemoteException;

    public DicomMatch[] studyRootMatch(Patient p, Study st, int limit, String callingAE, boolean dep)
            throws java.rmi.RemoteException;

    public String[/* 2 */] getMediaInfoOnStudy(String suid, boolean oldest, String callingAE)
            throws java.rmi.RemoteException;
}
