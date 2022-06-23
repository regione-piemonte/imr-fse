package it.csi.dmass.service.dao;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.dmass.service.model.DmaccLErrori;


@Repository
public class DmaccLErroriDaoImpl implements DmaccLErroriDao{

	private JdbcTemplate jdbcTemplate;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public DmaccLErroriDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public String getDescrizioneErrore(String codiceErrore) {
		return jdbcTemplate.queryForObject(SQL_DESCRIOZNE_ERRORE, String.class,codiceErrore);								
	}
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean create(DmaccLErrori model) {
		logger.info(SQL_INSERT);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_INSERT, model.getWso2Id(), model.getCodErrore(), model.getDescrErrore(), model.getTipoErrore(),  
				model.getInformazioniAggiuntive()) > 0;
	}
	
}
