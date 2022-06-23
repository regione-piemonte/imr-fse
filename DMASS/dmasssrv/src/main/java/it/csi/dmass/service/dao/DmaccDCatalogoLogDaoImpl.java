package it.csi.dmass.service.dao;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;



@Repository
public class DmaccDCatalogoLogDaoImpl implements DmaccDCatalogoLogDao {
	
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	Environment environment;

	JdbcTemplate jdbcTemplate;			

	@Autowired
	public DmaccDCatalogoLogDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}
	
	@Override
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public String getDescrizioneErroreByCodice(String codice) {
		logger.info("getDescrizioneErroreByCodice",DmaccDCatalogoLogDao.DESCRIZIONE_ERRORE);								
		logger.info("getDescrizioneErroreByCodice","codice: "+codice);
		return jdbcTemplate.queryForObject(DmaccDCatalogoLogDao.DESCRIZIONE_ERRORE,String.class, codice);
	}

	
}
