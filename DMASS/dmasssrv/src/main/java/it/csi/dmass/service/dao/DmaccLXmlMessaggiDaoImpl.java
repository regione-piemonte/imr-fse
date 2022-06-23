package it.csi.dmass.service.dao;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.dmass.service.model.DmaccLXmlMessaggi;

@Repository
public class DmaccLXmlMessaggiDaoImpl implements DmaccLXmlMessaggiDao{

	private JdbcTemplate jdbcTemplate;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public DmaccLXmlMessaggiDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public boolean create(DmaccLXmlMessaggi model) {
		logger.info(SQL_INSERT);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_INSERT, model.getWso2_id(), model.getXml_in(), model.getXml_out(), model.getXml_in_promemoria(), 
				model.getXml_out_promemoria(), model.getData_inserimento(), model.getXml_in_servizio(), model.getXml_out_servizio()) > 0;
	}
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public boolean update(DmaccLXmlMessaggi model) {
		logger.info(SQL_UPDATE);
		logger.info(model.toString());
		return jdbcTemplate.update(SQL_UPDATE, model.getXml_out(), model.getWso2_id()) > 0;
	}
}
