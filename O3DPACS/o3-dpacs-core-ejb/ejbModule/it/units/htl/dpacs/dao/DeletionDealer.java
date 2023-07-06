/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import javax.ejb.Local;
import javax.ejb.Stateless;

@Local
public interface DeletionDealer{
	public void deleteStudy(String studyUid) throws CannotDeleteException; 
}
