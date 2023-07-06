/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.utils; 

import it.units.htl.dpacs.core.ServerRemote;
import it.units.htl.web.RemoteConnector;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class ServerAliveTester
 */
public class ServerAliveTester extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(ServerAliveTester.class);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServerAliveTester() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		resp(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		resp(request, response);
	}
	
	protected void resp(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException { 
		PrintWriter out = response.getWriter();
		
		// Simple code for testing active or not
		if(isAlive()){
			//out.println("Active");
			response.sendError(HttpServletResponse.SC_OK);
		} else {
			//out.println("Not Active");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	protected boolean isAlive(){
		return getisRunning();
	}
	
	protected ServerRemote getConnection() {
		ServerRemote serverRemote = null;
		try {
//			serverRemote = InitialContext.doLookup("o3-dpacs/ServerBean/remote");
			serverRemote = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/ServerBean!it.units.htl.dpacs.core.ServerRemote");
		} catch (NamingException e) {
			log.error("", e);
		}
		return serverRemote;
	}

	public boolean getisRunning() { 
		boolean isRunning = false;
		
		try {
			ServerRemote serverRemote = this.getConnection();
			if (serverRemote.getStatus()){
				isRunning = true;
			}
		} catch (RemoteException e) {
			log.error(e);
			isRunning = false;
		}
		
		return isRunning;
	}
}
