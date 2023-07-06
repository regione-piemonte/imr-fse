/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.utils;

import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DatasourceConnectionFactory implements ConnectionFactory {

	final Log log = LogFactory.getLog(DatasourceConnectionFactory.class);
	private String datasourceName;

	public DatasourceConnectionFactory(String datasourceName) {
		this.datasourceName = datasourceName;
	}

	@Override
	public Connection getConnection() {
		Connection con = null;
		try {
			Context jndiContext = new InitialContext();
			con = ((DataSource) jndiContext.lookup(datasourceName)).getConnection();
		} catch (Exception e) {
			log.fatal("Unable to get connection from " + datasourceName, e);
		}

		return con;
	}
}
