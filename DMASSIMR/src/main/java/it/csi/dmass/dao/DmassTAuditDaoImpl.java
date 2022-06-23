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

import it.csi.dmass.model.DmassTAudit;


@Repository
public class DmassTAuditDaoImpl implements DmassTAuditDao{

	private JdbcTemplate jdbcTemplate;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public DmassTAuditDaoImpl(DataSource dmassdataSource) {
		jdbcTemplate = new JdbcTemplate(dmassdataSource);
	}	
	
	@Transactional(value="dmasstransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean create(DmassTAudit model) {
		logger.info(SQL_INSERT);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_INSERT, model.getCod_audit(), model.getId_transazione(), model.getCf_utente(), 
				model.getRuolo_utente(), model.getRegime(), model.getCf_assistito(), 
				model.getApplicazione(), model.getAppl_verticale(), model.getIp(), model.getId_collocazione(), model.getCodice_servizio()) > 0;
	}
	
	
}
