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

import it.csi.dmass.model.DmassLChiamateServizi;


@Repository
public class DmassLChiamateServiziDaoImpl implements DmassLChiamateServiziDao{

	private JdbcTemplate jdbcTemplate;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public DmassLChiamateServiziDaoImpl(DataSource dmassdataSource) {
		jdbcTemplate = new JdbcTemplate(dmassdataSource);
	}
	
	@Transactional(value="dmasstransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean create(DmassLChiamateServizi model) {
		logger.info(SQL_INSERT);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_INSERT, model.getId_ser(), model.getRequest()) > 0;
	}
	
	@Transactional(value="dmasstransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean update(DmassLChiamateServizi model) {
		logger.info(SQL_UPDATE);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_UPDATE, model.getResponse(), model.getId_ser()) > 0;
	}
	
}
