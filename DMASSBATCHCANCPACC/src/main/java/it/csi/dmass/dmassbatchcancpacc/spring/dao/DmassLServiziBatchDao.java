/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.spring.dao;

import it.csi.dmass.dmassbatchcancpacc.model.DmassLServiziBatch;

public interface DmassLServiziBatchDao {
	
	public static final String INSERT = "insert into dmass_l_servizi_batch"
			+ "(id_ser,nome_servizio,data_inizio) "
			+ " values(?,?,?);";
	
	public static final String UPDATE = "update dmass_l_servizi_batch"
			+ " set data_fine = ?, stato_fine = ? "
			+ " where id_ser = ?;";	
	
	public static final String NEXTVAL = "select nextval('seq_dmass_l_servizi_batch');";
			
	public boolean create(DmassLServiziBatch dmassLServiziBatchDto);
	
	public boolean update(DmassLServiziBatch dmassLServiziBatchDto);
}
