/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dao;

import it.csi.dmass.model.DmassLServizi;

public interface DmassLServiziDao {
	
	public final static String SQL_INSERT = "INSERT INTO dmass_l_servizi "
	+ "(id_ser, nome_servizio, data_richiesta, cf_utente, ruolo_utente, cf_assistito, "
	+ "applicazione, appl_verticale, regime, id_collocazione, ip_richiedente, "
	+ "codice_servizio, data_ins) "
	+ "VALUES(?, ?, ?, ?, ?, ?, ?, "
	+ "?, ?, ?, ?, ?, current_timestamp);";
	
	
	public final static String SQL_UPDATE = "UPDATE dmass_l_servizi set cod_esito_risposta_servizio = ?, data_ins = current_timestamp where id_ser = ? ";
	
	public boolean create(DmassLServizi model);
	public boolean update(DmassLServizi model);
		
	public Long getNextVal();

}
