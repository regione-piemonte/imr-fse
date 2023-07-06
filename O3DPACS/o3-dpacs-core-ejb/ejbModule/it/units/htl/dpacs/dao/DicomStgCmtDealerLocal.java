/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.valueObjects.Instance;

import javax.ejb.Local;

@Local
public interface DicomStgCmtDealerLocal {
    // No Exceptions need to be thrown, except for application ones
    public String[] getAEData(String ae);
    public Instance[] commitStorage(Instance[] in);
} // end interface
