package it.csi.dmass.scaricoStudi.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.dmass.scaricoStudi.util.LogUtil;



@Repository
public class DmaccDCatalogoLogDaoImpl implements DmaccDCatalogoLogDao {
	
	
	protected LogUtil log = new LogUtil(this.getClass());
	
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
		log.info("getDescrizioneErroreByCodice",DmaccDCatalogoLogDao.RICERCA_RICHIESTE);								
		log.info("getDescrizioneErroreByCodice","codice: "+codice);
		return jdbcTemplate.queryForObject(DmaccDCatalogoLogDao.RICERCA_RICHIESTE,String.class, codice);
	}

	
}
