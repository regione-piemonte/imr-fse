/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dao;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.dmass.model.DmassLServizi;


@Repository
public class DmassLServiziDaoImpl implements DmassLServiziDao{

	private JdbcTemplate jdbcTemplate;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public DmassLServiziDaoImpl(DataSource dmassdataSource) {
		jdbcTemplate = new JdbcTemplate(dmassdataSource);
	}		
	
	@Transactional(value="dmasstransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean create(DmassLServizi model) {
		logger.info(SQL_INSERT);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_INSERT, model.getId_ser(), model.getNome_servizio(), model.getData_richiesta(), model.getCf_utente(), 
				model.getRuolo_utente(), model.getCf_assistito(), 
				model.getApplicazione(), model.getAppl_verticale(), model.getRegime(), model.getId_collocazione(), model.getIp_richiedente(), 
				model.getCodice_servizio()) > 0;
	}
	
	@Transactional(value="dmasstransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean update(DmassLServizi model) {
		
		logger.info(SQL_UPDATE);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_UPDATE, model.getCod_esito_risposta_servizio(), model.getId_ser()) > 0;
	}
	
	@Transactional(value="dmasstransactionManager", readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public Long getNextVal() {
		logger.info("select nextval('seq_dmass_l_servizi') ");
		return jdbcTemplate.queryForObject("select nextval('seq_dmass_l_servizi') ", Long.class);		
		
	}
	
}
