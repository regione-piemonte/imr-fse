/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dao;

import it.csi.dmass.model.DmassLChiamateServizi;

public interface DmassLChiamateServiziDao {

	public final static String SQL_INSERT = "INSERT INTO dmass_l_chiamate_servizi "
			+ "(id, id_ser, request, data_ins) "
			+ "VALUES(nextval('seq_dmass_l_chiamate_servizi_xml'), ?, ?, current_timestamp);";
	
	public final static String SQL_UPDATE = "UPDATE dmass_l_chiamate_servizi "
			+ " set response = ?,  data_ins = current_timestamp "
			+ " where id_ser = ?;";	
	
	public boolean create(DmassLChiamateServizi model);
	public boolean update(DmassLChiamateServizi model);
	
}
