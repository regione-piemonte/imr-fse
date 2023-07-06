/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TimerLogger {
	private Log log = LogFactory.getLog(TimerLogger.class);
	
	public String QUERY_PAT_INS = "QRY/PAT/INS";
	public String QUERY_PAT_SER = "QRY/PAT/SER";
	public String QUERY_PAT_STU = "QRY/PAT/STU";
	public String QUERY_STU_INS = "QRY/STU/INS";
	public String QUERY_STU_SER = "QRY/STU/SER";
	public String QUERY_STU_STU = "QRY/STU/STU";
	public String STORE_INST = "STORE/INS";
	public String STORE_STU = "STORE/STU";
	public String MOVE = "MOVE";
	
	public void toLog(String[] message){
		String msg = ""; 
		for(int i = 0; i < message.length; i++){
			msg += message[i];
			if(i < message.length -1){
				msg += "|";
			}
		}
		log.debug(msg);
	}
}
