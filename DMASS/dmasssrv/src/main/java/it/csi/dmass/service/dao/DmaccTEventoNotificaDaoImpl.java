package it.csi.dmass.service.dao;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.dmass.service.model.DmaccTEventoNotifica;


@Repository
public class DmaccTEventoNotificaDaoImpl implements DmaccTEventoNotificaDao{

	private JdbcTemplate jdbcTemplate;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public DmaccTEventoNotificaDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}
	
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean create(DmaccTEventoNotifica model) {
		logger.info(SQL_INSERT);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_INSERT, model.getId_evento(), model.getCf_destinatario(), model.getFlag_stato_notifica()) > 0;
	}
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public Long getIdEvento(String codiceEvento) {
		logger.info(SQL_INSERT);
		logger.info(codiceEvento);
		return jdbcTemplate.queryForObject(SQL_ID_EVENTO, Long.class,codiceEvento);								
	}
	
}
