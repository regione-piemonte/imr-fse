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

@Repository
public class DmassDRuoloDaoImpl implements DmassDRuoloDao{

private JdbcTemplate jdbcTemplate;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public DmassDRuoloDaoImpl(DataSource dmassdataSource) {
		jdbcTemplate = new JdbcTemplate(dmassdataSource);
	}
	
	@Override
	@Transactional(value="dmasstransactionManager", readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public String getCodiceRuoloIni(String codice_ruolo) {
		logger.info("select codice_ruolo_ini from dmacc_d_ruolo where codice_ruolo = ?");
		logger.info("codice_ruolo: "+codice_ruolo);
		return jdbcTemplate.queryForObject("select codice_ruolo_ini from dmass_d_ruolo where codice_ruolo = ? ",
				String.class,codice_ruolo);
	}

	

}
