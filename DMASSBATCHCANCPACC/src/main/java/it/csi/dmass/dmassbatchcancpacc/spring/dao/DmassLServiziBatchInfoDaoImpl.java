/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.spring.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import it.csi.dmass.dmassbatchcancpacc.logging.DmassBatchCancpaccLogger;
import it.csi.dmass.dmassbatchcancpacc.model.DmassLServiziBatchInfo;

@Repository
public class DmassLServiziBatchInfoDaoImpl implements DmassLServiziBatchInfoDao{

	private JdbcTemplate jdbcTemplate;	
	
	@Autowired
	DmassBatchCancpaccLogger dmassBatchCancpaccLogger;
	
	@Autowired
	Environment environment; 		
	
	
	@Autowired
	public DmassLServiziBatchInfoDaoImpl(DataSource dmaerrdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaerrdataSource);
	}
	
	@Override
	public boolean  create(DmassLServiziBatchInfo dmassLServiziBatchInfo) {

		dmassLServiziBatchInfo.setId_info(jdbcTemplate.queryForObject(NEXTVAL, Long.class));		

		return jdbcTemplate.update(INSERT, dmassLServiziBatchInfo.getId_info(),
				dmassLServiziBatchInfo.getId_ser(),
				dmassLServiziBatchInfo.getInfo(),
				dmassLServiziBatchInfo.getTipo_info(),
				dmassLServiziBatchInfo.getData_ins())>0;
	}
	
	@Override
	public boolean  insertBatchInEsecuzione(Timestamp data_ins) {			
		dmassBatchCancpaccLogger.info("insert into dmass_t_lock_cancella_pacc(data_ins) values(?)");
		dmassBatchCancpaccLogger.info(data_ins);
		return jdbcTemplate.update("insert into dmass_t_lock_cancella_pacc(data_ins) values(?)", data_ins)>0;
	}
		
	@Override
	public boolean  delete() {		
		dmassBatchCancpaccLogger.info("delete from dmass_t_lock_cancella_pacc");		
		return jdbcTemplate.update("delete from dmass_t_lock_cancella_pacc")>0;
	}
	
	public Long getImr_cancella_pac_timelock() {
		dmassBatchCancpaccLogger.info(IMR_CANCELLA_PAC_TIMELOCK);
		Long imr_cancella_pac_timelock = this.getObjectByQuery(IMR_CANCELLA_PAC_TIMELOCK, Long.class);
		return imr_cancella_pac_timelock;
		
	}
	
	public Timestamp getT_lock_cancella_pacc_data_ins() {
		dmassBatchCancpaccLogger.info(T_LOCK_CANCELLA_PACC_DATA_INS);
		Timestamp imr_cancella_pac_timelock = this.getObjectByQuery(T_LOCK_CANCELLA_PACC_DATA_INS, Timestamp.class);
		return imr_cancella_pac_timelock;
		
	}
	
	private <T> T getObjectByQuery(String sql,Class<T> clazz) {
		T t = null;
		List<T> list = jdbcTemplate.queryForList(sql, clazz);
		if(list!=null && list.size()>0) {
			t = list.get(0); 
		}
		return t;
	}

}
