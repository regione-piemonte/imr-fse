/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.AEData;
import it.units.htl.dpacs.valueObjects.DicomMatch;
import it.units.htl.dpacs.valueObjects.Instance;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.dpacs.valueObjects.Series;
import it.units.htl.dpacs.valueObjects.Study;

import javax.ejb.Remote;

@Remote
public interface DicomMoveDealerRemote {
    public DicomMatch[] retrieveMatches(Patient pat, Study st, Series se, Instance i, String callingAE);

    public String getStudyUrlBase(String sUid, String callingAE);

    public String verifyWhereStudiesAre(String studySopInstance, String callingAE);

    public AEData getAeData(String aeData);
} // end interface
