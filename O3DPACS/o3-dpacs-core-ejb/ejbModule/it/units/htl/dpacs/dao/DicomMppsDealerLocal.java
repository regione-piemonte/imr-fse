/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.valueObjects.MPPSItem;

import javax.ejb.Local;

import org.dcm4che.net.ActiveAssociation;

@Local
public interface DicomMppsDealerLocal {
    // No Exceptions need to be thrown, except for application ones
    public int insertMPPSItem(MPPSItem m, ActiveAssociation assoc);

    public int updateMPPSItem(MPPSItem m, ActiveAssociation assoc);
} // end interface
