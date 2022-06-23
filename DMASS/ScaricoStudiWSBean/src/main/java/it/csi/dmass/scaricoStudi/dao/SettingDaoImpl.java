package it.csi.dmass.scaricoStudi.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.csi.dmass.scaricoStudi.model.Setting;
import it.csi.dmass.scaricoStudi.util.LogUtil;

@Repository
public class SettingDaoImpl implements SettingDao{

	private JdbcTemplate jdbcTemplate;	
	
	protected LogUtil log = new LogUtil(this.getClass());	
	
	@Autowired
	public SettingDaoImpl(DataSource dmaccdataSource) {
		jdbcTemplate = new JdbcTemplate(dmaccdataSource);
	}					
	
	@Transactional(value="dmacctransactionManager", readOnly = false, propagation = Propagation.REQUIRED)
	public Setting findByQuery(String query, Object... params) {
		log.info("findByQuery",query);								
		log.info("findByQuery","params: "+toString(params));
		List<Setting> listSetting = jdbcTemplate.query(query,params, new SettingMapper());
		
		if(listSetting!=null && listSetting.size()>0) {
			return listSetting.get(0);
		}else {
			return new Setting();
		}
	}
	
	public String toString(Object... params) {
		List<String> ret = new ArrayList<String>();
		for (Object param : params) {
			ret.add(param.toString());
		}
		return "["+String.join(",",ret)+"]";
	}
		
	
	public class SettingMapper implements RowMapper<Setting>{

		@Override
		public Setting mapRow(ResultSet rs, int arg1) throws SQLException {
			
			Setting setting = new Setting();						
			setting.setValue(rs.getString("value"));											
			return setting;
		}
		
	}

}
