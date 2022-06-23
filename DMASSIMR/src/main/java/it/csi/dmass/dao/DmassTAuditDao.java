/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dao;

import it.csi.dmass.model.DmassTAudit;

public interface DmassTAuditDao {
	

	public final static String SQL_INSERT = "INSERT INTO dmass_t_audit "
			+ "(id, cod_audit, id_transazione, cf_utente, ruolo_utente, regime, cf_assistito, "
			+ "applicazione, appl_verticale, ip, id_collocazione, codice_servizio, "
			+ "data_ins) "
			+ "VALUES(nextval('seq_dmass_t_audit'), ?, ?, ?, ?, ?, ?, ?, "
			+ "?, ?, ?, ?, current_timestamp);";
	
	public boolean create(DmassTAudit model);
		

}
