/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dao;

import it.csi.dmass.model.DmassLErroriServizi;

public interface DmassLErroriServiziDao {
	
	
	public final static String SQL_INSERT = "INSERT INTO dmass_l_errori_servizi "
			+ "(id_err, id_ser, cod_errore, descr_errore, tipo_errore, info_aggiuntive, data_ins) "
			+ "VALUES(nextval('seq_dmass_l_errori_servizi_xml'), ?, ?, ?, ?, ?, current_timestamp);";	
	
	public boolean create(DmassLErroriServizi model);
	
}
