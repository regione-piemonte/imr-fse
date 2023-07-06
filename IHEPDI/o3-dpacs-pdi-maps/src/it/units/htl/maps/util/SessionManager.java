/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class SessionManager {
	private static volatile SessionFactory INSTANCE;
	
	private static Log log =  LogFactory.getLog(SessionManager.class);
	
	protected SessionManager(){}
	private static synchronized SessionFactory tryCreateInstance(){
		if(INSTANCE == null){
			try {
				Context itx = new InitialContext();	
				
				
				Configuration configuration = new Configuration();
				configuration.configure("hibernate.cfg.xml");
				INSTANCE = configuration.buildSessionFactory();				
			} catch (NamingException e) {

				log.error("",e);
			}
		}
		return INSTANCE;
	}
	
	public static SessionFactory getInstance(){
		SessionFactory sm = INSTANCE;
		if(sm == null){
			log.info("Creating new SessionFactory for o3-dpacs-maps...");
			sm = tryCreateInstance();
		}
		return sm;
	}
}
