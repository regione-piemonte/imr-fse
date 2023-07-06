/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * @author max.ritossa@gmail.com
 * 
 * This class connect to O3DPACS server to grab 
 */
public class RemoteConnector {
	private Log log = LogFactory.getLog(RemoteConnector.class);
	private Object objRef;
	private InitialContext start;

	/**
	 * Instantiates a new remote connector.
	 */
	public RemoteConnector(String lookupObj) throws NamingException {
		start = new InitialContext();
		objRef = start.lookup(lookupObj);
	}
	
	public Object getObject(){
		return objRef;
	}
}
