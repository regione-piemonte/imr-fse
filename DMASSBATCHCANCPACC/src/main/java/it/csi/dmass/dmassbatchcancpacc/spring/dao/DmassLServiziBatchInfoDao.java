/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.spring.dao;

import java.sql.Timestamp;

import it.csi.dmass.dmassbatchcancpacc.model.DmassLServiziBatchInfo;

public interface DmassLServiziBatchInfoDao {
	
	public static final String INSERT = "insert into dmass_l_servizi_batch_info"
			+ "(id_info,id_ser,info,tipo_info,data_ins) "
			+ " values(?,?,?,?,?);";
	
	public static final String NEXTVAL = "select nextval('seq_dmass_l_servizi_batch_info');";
	
	public static final String IMR_CANCELLA_PAC_TIMELOCK = "select value as imr_cancella_pac_timelock from dmass_t_configurazione where key = 'imr_cancella_pac_timelock' ;"; 
	
	public static final String T_LOCK_CANCELLA_PACC_DATA_INS = "select data_ins from dmass_t_lock_cancella_pacc; ";	
			
	public boolean create(DmassLServiziBatchInfo dmassLServiziBatchInfo);
	
	public Long getImr_cancella_pac_timelock();
	
	public Timestamp getT_lock_cancella_pacc_data_ins();

	boolean insertBatchInEsecuzione(Timestamp data_ins);

	boolean delete();
}
