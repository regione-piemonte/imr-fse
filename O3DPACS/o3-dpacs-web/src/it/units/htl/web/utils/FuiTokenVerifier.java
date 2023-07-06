/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.utils;

import it.units.htl.dpacs.helpers.CloseableUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class FuiTokenVerifier {

	private static final Log log = LogFactory.getLog(FuiTokenVerifier.class);
	private static final String SERVICENAME="WebSettings";
	private static final String PARAMETERNAME="oneShotToken";
	private static final String DATASOURCENAME="oneShotDatasource";
	private static final String TABLENAME="oneShotTable";
	private static final String FIELDNAME="oneShotField";
	private static final String CONFIGURATION_ERROR = "Error loading configuration";
	
	
	private ConnectionFactory cf;
	private String datasourceName;
	private String tableName;
	private String fieldName;
	private String parameterName;

	public FuiTokenVerifier() throws Exception {
		loadConfiguration();
		cf = new DatasourceConnectionFactory(datasourceName);
	}

	void setConnectionFactory(ConnectionFactory cf) {
		this.cf = cf;
	}

	public boolean isValid(String token) {
		if(parameterName==null)		// If null, one shot authentication is disabled
			return true;
		
		Connection con = null;
		PreparedStatement ps = null;
		int deletedRows = 0;
		try {
			con = cf.getConnection();
			ps = con.prepareStatement("DELETE FROM "+tableName+" WHERE "+fieldName+"=?");
			ps.setString(1, token);
			deletedRows = ps.executeUpdate();
		} catch (Exception e) {
			log.error("Unable to verify token", e);
		} finally {
			CloseableUtils.close(ps);
			CloseableUtils.close(con);
		}
		return deletedRows > 0;
	}
	
	private void loadConfiguration() throws Exception{
		// Load DS & configuration, fill attributes 
		Document config = XmlConfigLoader.getConfigurationFromDB(SERVICENAME);
    	if(config!=null){
			NodeList nodes=config.getElementsByTagName(PARAMETERNAME);
			try{
				parameterName=nodes.item(0).getTextContent();
				if("".equals(parameterName))
					parameterName=null;
			}catch(Exception ex){
				log.warn(PARAMETERNAME+" NOT defined", ex);
				throw new Exception(CONFIGURATION_ERROR);
			}
			
			if(parameterName!=null){		// If null, one shot authentication is disabled
				nodes=config.getElementsByTagName(DATASOURCENAME);
				try{
					datasourceName=nodes.item(0).getTextContent();
				}catch(Exception ex){
					log.warn(DATASOURCENAME+" NOT defined", ex);
					throw new Exception(CONFIGURATION_ERROR);
				}	
				nodes=config.getElementsByTagName(TABLENAME);
				try{
					tableName=nodes.item(0).getTextContent();
				}catch(Exception ex){
					log.warn(TABLENAME+" NOT defined", ex);
					throw new Exception(CONFIGURATION_ERROR);
				}
				nodes=config.getElementsByTagName(FIELDNAME);
				try{
					fieldName=nodes.item(0).getTextContent();
				}catch(Exception ex){
					log.warn(FIELDNAME+" NOT defined", ex);
					throw new Exception(CONFIGURATION_ERROR);
				}
			}
		}else{
			log.warn(SERVICENAME+" NOT defined!");
			throw new Exception(CONFIGURATION_ERROR);
		}
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	
}
