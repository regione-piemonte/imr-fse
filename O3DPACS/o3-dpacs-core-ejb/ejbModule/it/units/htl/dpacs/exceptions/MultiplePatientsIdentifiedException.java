/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.exceptions;

import it.units.htl.dpacs.dao.CannotStoreException;

public class MultiplePatientsIdentifiedException extends CannotStoreException {

	private static final long serialVersionUID = 1L;
	
	private static final String MESSAGE = "More than one patient has been identified";

	public MultiplePatientsIdentifiedException(String patientId) {
		super(MESSAGE+": "+patientId);
	}
	
	public MultiplePatientsIdentifiedException() {
		super(MESSAGE);
	}

}
