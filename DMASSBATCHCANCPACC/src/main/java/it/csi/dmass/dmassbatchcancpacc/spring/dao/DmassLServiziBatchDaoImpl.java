/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.spring.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import it.csi.dmass.dmassbatchcancpacc.logging.DmassBatchCancpaccLogger;
import it.csi.dmass.dmassbatchcancpacc.model.DmassLServiziBatch;

@Repository
public class DmassLServiziBatchDaoImpl implements DmassLServiziBatchDao{

	private JdbcTemplate jdbcTemplate;	
	
	@Autowired
	DmassBatchCancpaccLogger dmassBatchCancpaccLogger;
	
	@Autowired
	Environment environment; 		
		
	@Autowired
	public DmassLServiziBatchDaoImpl(DataSource dmaessdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaessdataSource);
	}

	@Override
	public boolean  create(DmassLServiziBatch dmassLServiziBatchDto) {

		dmassLServiziBatchDto.setId_ser(jdbcTemplate.queryForObject(NEXTVAL, Long.class));

		dmassBatchCancpaccLogger.info(INSERT);
		dmassBatchCancpaccLogger.info("Id_ser"+dmassLServiziBatchDto.getId_ser()+", None_servizio: "+
				dmassLServiziBatchDto.getNome_servizio()+", Data_inizio"+dmassLServiziBatchDto.getData_inizio());
		return jdbcTemplate.update(INSERT, dmassLServiziBatchDto.getId_ser(),
				dmassLServiziBatchDto.getNome_servizio(),dmassLServiziBatchDto.getData_inizio())>0;
	}
			
	@Override
	public boolean  update(DmassLServiziBatch dmassLServiziBatchDto) {

		dmassLServiziBatchDto.setId_ser(jdbcTemplate.queryForObject(NEXTVAL, Long.class));
		dmassBatchCancpaccLogger.info(UPDATE);
		dmassBatchCancpaccLogger.info("Data_fine"+dmassLServiziBatchDto.getData_fine()+", Id_ser: "+
				dmassLServiziBatchDto.getId_ser());
		return jdbcTemplate.update(UPDATE,dmassLServiziBatchDto.getData_fine(),dmassLServiziBatchDto.getStato_fine(), dmassLServiziBatchDto.getId_ser())>0;
	}	
	
	

}
