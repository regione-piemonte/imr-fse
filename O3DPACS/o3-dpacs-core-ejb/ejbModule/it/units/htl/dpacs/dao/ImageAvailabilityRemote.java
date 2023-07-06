/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.IheStudyId;

import javax.ejb.Remote;

@Remote
public interface ImageAvailabilityRemote {
	public boolean insertCorrectStudy(IheStudyId ids, String reconciliationSource, String stringToSet, String targetApp);
	public int insertCorrectStudies(long completedOnSecond, String reconciliationSource, String stringToSet, String targetApp);
	public boolean reconcileWrongStudy(IheStudyId ids, String reconciliationSource, String stringToSet, String stringToRemove, String targetApp);
	public boolean reconcileWrongStudies(IheStudyId idsOne, IheStudyId idsTwo, String reconciliationSource, String stringToSet, String stringToRemove, String targetApp);
} // end interface
