/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.valueObjects.RecoveryItem;

import javax.ejb.Local;
import javax.ejb.Stateless;

@Local
public interface DeprecationLocal {
    public long deprecateSeries(String originalUid, String newUid, boolean markForDeletion, String reason, long userPk) throws Exception;

    public long deprecateStudy(String originalUid, String newUid, boolean markForDeletion, String reason, long userPk) throws Exception;

    public RecoveryItem[] getPossibleRecoveries(String objectType);

    public long recoverSeries(long deprecationId, String currentUid, String originalUid, long userPk) throws Exception;

    public long recoverStudy(long deprecationId, String currentUid, String originalUid, long userPk) throws Exception;

    public String getNewDeprecationUid();

    public boolean deleteStudy(String studyUid, String path);
} // end interface
