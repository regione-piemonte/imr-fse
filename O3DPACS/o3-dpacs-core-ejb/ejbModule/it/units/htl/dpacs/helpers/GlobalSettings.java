/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GlobalSettings implements Serializable{

	private static Log log = LogFactory.getLog(GlobalSettings.class);
	
	private static final long serialVersionUID = 1L;
		
	public static enum PartitioningStrategy {
		CALLED,
		CALLING,
		NONE;
	}
	
	private static boolean loaded=false;
	
	private static PartitioningStrategy partitioningStrategy=null;
	
	public static PartitioningStrategy getPartitioningStrategy(){
		if(!loaded)
			loadSettings();
		return partitioningStrategy;
	}
	
	private static boolean gateway=false;
	
	public static boolean isGateway(){
		if(!loaded)
			loadSettings();
		return gateway;
	}
	
	private static synchronized void loadSettings(){
		String v=GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.CONFIG_PARTITIONINGSTRATEGY);
		
		if(v!=null){
			if("CALLED".equals(v.toUpperCase()))
				partitioningStrategy=PartitioningStrategy.CALLED;
			else if("CALLING".equals(v.toUpperCase()))
				partitioningStrategy=PartitioningStrategy.CALLING;
			else 
				partitioningStrategy=PartitioningStrategy.NONE;
		}else{
			partitioningStrategy=PartitioningStrategy.NONE;
			log.info("COULD NOT RETRIEVE PARTITIONINGSTRATEGY SETTING");
		}
		
		v=GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.CONFIG_GATEWAY);
		if(v!=null)
			gateway="TRUE".equals(v.toUpperCase());
		loaded=true;
	}

}
