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

import it.csi.dmass.model.DmassLErroriServizi;


@Repository
public class DmassLErroriServiziDaoImpl implements DmassLErroriServiziDao{

	private JdbcTemplate jdbcTemplate;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public DmassLErroriServiziDaoImpl(DataSource dmassdataSource) {
		jdbcTemplate = new JdbcTemplate(dmassdataSource);
	}
	
	@Transactional(value="dmasstransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean create(DmassLErroriServizi model) {
		logger.info(SQL_INSERT);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_INSERT, model.getId_ser(), model.getCod_errore(), 
				(model.getDescr_errore()==null?"unknow":model.getDescr_errore()),
				model.getTipo_errore(), model.getInfo_aggiuntive()) > 0;
	}
	
}
