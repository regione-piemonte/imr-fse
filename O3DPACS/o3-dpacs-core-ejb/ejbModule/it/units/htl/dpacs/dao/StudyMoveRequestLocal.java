/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import javax.ejb.Local;

@Local
public interface StudyMoveRequestLocal{
	
	public long insertRequestStudyMove(String messageID, String ris, String accNum, String azienda, String struttura, String action, long userPk, String patientID, String idIssuer);
	
}	// end interface
