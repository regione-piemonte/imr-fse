package it.csi.dmass.scaricoStudi.dao;

import it.csi.dmass.scaricoStudi.model.Setting;

public interface SettingDao {
	
	public static final String SQL_GET_VALUE_BY_KEY = "select value from setting where key = ? ";				
	
	public Setting findByQuery(String query, Object... params);
}
