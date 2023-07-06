/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IheSettings implements Serializable{

	private static Log log = LogFactory.getLog(IheSettings.class);
	
	private static final long serialVersionUID = 1L;
		
	private static Boolean ihe=null;
	
	public static boolean isIhe(){
		if(ihe==null)
			loadSettings();
		return ihe.booleanValue();		// In case loadSettings fails, this will throw a NullPointerException
	}
	
	private static synchronized void loadSettings(){
		String v=GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.CONFIG_IS_IHE);
		if(v!=null){
			ihe=(("1".equals(v)) || ("true".equalsIgnoreCase(v)) || ("yes".equalsIgnoreCase(v)))? new Boolean(true) : new Boolean(false);
		}else{
			log.fatal("COULD NOT RETRIEVE IHE SETTING");
		}
	}

}
