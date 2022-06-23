/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class DmaccDRuoloMapper implements RowMapper<DmaccDRuolo>{

	@Override
	public DmaccDRuolo mapRow(ResultSet rs, int rowNum) throws SQLException {

		DmaccDRuolo dmaccDRuolo = new DmaccDRuolo();	
		dmaccDRuolo.setCodice_ruolo_ini(rs.getString("codice_ruolo_ini"));
		return dmaccDRuolo;
	}

}
