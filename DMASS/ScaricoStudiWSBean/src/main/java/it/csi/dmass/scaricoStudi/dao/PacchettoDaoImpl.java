package it.csi.dmass.scaricoStudi.dao;

import java.math.BigDecimal;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.dmass.scaricoStudi.util.LogUtil;



@Repository
public class PacchettoDaoImpl implements PacchettoDao {
	
	
	protected LogUtil log = new LogUtil(this.getClass());
	
	@Autowired
	Environment environment;

	JdbcTemplate jdbcTemplate;			

	@Autowired
	public PacchettoDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}
	
	@Override
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public BigDecimal getDimensionePacchetto(BigDecimal id) {
		log.info("getDimensionePacchetto",PacchettoDao.DIMENSIONE_PACCHETTO);								
		log.info("getDimensionePacchetto","id: "+id);
		return jdbcTemplate.queryForObject(PacchettoDao.DIMENSIONE_PACCHETTO,BigDecimal.class, id);
	}

	
}
