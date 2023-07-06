/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.users;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import it.units.htl.maps.Users;
import it.units.htl.maps.UsersHome;
import it.units.htl.maps.util.SessionManager;

public class CheckUser extends HttpServlet {

	private static final long serialVersionUID = -2782339689429540091L;
	
	private Log log = LogFactory.getLog(CheckUser.class);

	public void init(ServletConfig config) throws ServletException {
		log.info("Init " + getClass().getName() + " servlet...");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.info("doGet...");
		doWork(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.info("doPost...");
		doWork(request, response);
	}
	
	protected synchronized void doWork(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		boolean areCredentialsValid = areCredentialsValid(username, password);
		if (!areCredentialsValid) {
			log.info("Credentials are not valid");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		int isAuth = 0;
		Users user = new Users();
		user.setUserName(username);
		user.setPassword(password);
        final Session session = SessionManager.getInstance().openSession();
        UsersHome userHome = new UsersHome();
        List<Users> retrievedUser = userHome.findByExample(user, session);
        if (retrievedUser.size() > 0) {
			isAuth = 1;
		}
        
		boolean resultWritten = false;
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = null;
        try {
        	writer = outputFactory.createXMLStreamWriter(response.getOutputStream());
			writer.writeStartDocument("1.0");
	        writer.writeStartElement("results");
	        writer.writeStartElement("user");
	        writer.writeAttribute("auth", "" + isAuth);
	        writer.writeEndElement();
	        writer.writeEndDocument();
	        resultWritten = true;
        } catch (XMLStreamException e) {
			e.printStackTrace();
			resultWritten = false;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (XMLStreamException e) {
					log.info("Error while checking user");
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
		}
        
        if (!resultWritten) {
        	log.info("Cannot write response");
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
	}
	
	/**
	 * Verifica che le credenziali siano valide.
	 * @param username lo username da validare
	 * @param password la password da validare
	 * @return true se le credenziali sono valide, false altrimenti
	 */
	private boolean areCredentialsValid(String username, String password) {
		return username != null && !username.isEmpty() && password != null && !password.isEmpty();
	}
}
