/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import javax.ejb.Remote;

@Remote
public interface StudyMoveRemote {
	
	public long moveStudy(String moveStudyHistoryUid,Integer timeout);
	public long retrieveStudyMetadata(long moveStudyHistoryUid,Integer timeout);
	
}
