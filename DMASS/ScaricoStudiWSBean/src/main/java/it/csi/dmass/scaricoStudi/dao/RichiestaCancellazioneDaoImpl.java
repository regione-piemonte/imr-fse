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

import it.csi.dmass.scaricoStudi.util.LogUtil;



@Repository
public class RichiestaCancellazioneDaoImpl implements RichiestaCancellazioneDao {
	
	
	protected LogUtil log = new LogUtil(this.getClass());
	
	@Autowired
	Environment environment;

	JdbcTemplate jdbcTemplate;			

	@Autowired
	public RichiestaCancellazioneDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}
	
	public String toString(Object... params) {
		List<String> ret = new ArrayList<String>();
		for (Object param : params) {
			ret.add(param.toString());
		}
		return "["+String.join(",",ret)+"]";
	}
		

	@Override
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public boolean insert(String insert, Object... params) {
		log.info("insert",insert);
		log.info("insert","params: "+toString(params));
		return jdbcTemplate.update(insert, params) > 0;
	}
	
}
