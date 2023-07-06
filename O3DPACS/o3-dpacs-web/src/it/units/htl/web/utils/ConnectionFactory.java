/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.utils;

import java.sql.Connection;

public interface ConnectionFactory {

	// Giacomo's original idea: creating connections from both datasources and simple drivers
	public Connection getConnection();
	
}
