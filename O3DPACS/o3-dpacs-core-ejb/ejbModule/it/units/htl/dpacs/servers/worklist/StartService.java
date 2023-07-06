/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.worklist;

import org.apache.log4j.Logger;

public class StartService {  
    public static void main(String[] args) {
	Logger log = Logger.getLogger(StartService.class);
	try{   
	    log.info("Starting O3 WorklistServer...");
	    WorklistService wls = new WorklistService();
	    wls.startService();
	    log.info("Service is started!");
	}catch (Exception e) {
	    log.fatal("Unable to start O3 WorklistServer",e);
	}
    }

}
