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

import javax.ejb.Local;

@Local
public interface DicomQueryDealerLocal {
    // No Exceptions need to be thrown, except for application ones
    
    public DicomMatch[] patientRootMatch(Patient p, Study st, Series se, String callingAE);

    public DicomMatch[] patientRootMatch(Patient p, Study st, int limit, String callingAE, boolean dep);

    public DicomMatch[] patientRootMatch(Patient p, int limit, String callingAE);

    public DicomMatch[] queryInstanceLevel(Patient p, Study st, Series se, Instance inst, String callingAE);

    public DicomMatch[] studyRootMatch(Patient p, Study st, Series se, String callingAE);

    public DicomMatch[] studyRootMatch(Patient p, Study st, int limit, String callingAE, boolean dep); /* __*__ */// throws DcmServiceException;
    // public DicomMatch[] patientStudyOnlyMatch(Patient p, Study st, int limit);
    // public DicomMatch[] patientStudyOnlyMatch(Patient p, int limit);

    public String[/* 2 */] getMediaInfoOnStudy(String suid, boolean oldest, String callingAE);
} // end interface
