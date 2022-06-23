package it.csi.dmass.scaricoStudi.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.dmass.scaricoStudi.model.RichiestaScarico;
import it.csi.dmass.scaricoStudi.model.RichiestaScaricoMapper;
import it.csi.dmass.scaricoStudi.util.LogUtil;



@Repository
public class RichiestaScaricoDaoImpl implements RichiestaScaricoDao {
	
	
	protected LogUtil log = new LogUtil(this.getClass());
	
	@Autowired
	Environment environment;

	JdbcTemplate jdbcTemplate;			

	@Autowired
	public RichiestaScaricoDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}
	
	public String toString(Object... params) {
		List<String> ret = new ArrayList<String>();
		for (Object param : params) {
			if(param!=null) {
				ret.add(param.toString());
			}
		}
		return "["+String.join(",",ret)+"]";
	}
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public List<RichiestaScarico> getByQuery(String query,Object... params) {		
		log.info("getByQuery",query);								
		log.info("getByQuery","params: "+toString(params));
		return jdbcTemplate.query(query,params, new RichiestaScaricoMapper());
	}	
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean update(String update, Object... params) {
		log.info("update",update);
		log.info("update","params: "+toString(params));
		return jdbcTemplate.update(update, params) > 0;
	}
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean update(String statoRichiesta, String errore, String codiceFiscale, String idReferto, String asr) {
		log.info("update",UPDATE_STATO_RICHIESTA);
		return jdbcTemplate.update(UPDATE_STATO_RICHIESTA, statoRichiesta, errore, codiceFiscale, idReferto, asr) > 0;
	}

	@Override
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean insert(String insert, Object... params) {
		log.info("insert",INSERT_RICHIESTA_SCARICO);
		log.info("insert","params: "+toString(params));
		return jdbcTemplate.update(INSERT_RICHIESTA_SCARICO, params) > 0;
	}
	
}
