/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package dpacspdi;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Serivizo mock per il servizio CompleteNotice
 */
@WebServlet("/CompleteNotice")
public class CompleteNoticePdi extends HttpServlet {

	private static final long serialVersionUID = 767737854908797852L;
	private static final Logger log = Logger.getLogger(CompleteNoticePdi.class);

	/**
	 * Permette di simulare un errore nel servizio di notifica
	 * Se true errore, altrimenti successo
	 */
	private final boolean error = false;
	
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

	private synchronized void doWork(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String zipName = request.getParameter("zipName");
		String jobUID = request.getParameter("jobUID");
		String requestID = request.getParameter("requestID");
		String status = request.getParameter("status");
		String dist = request.getParameter("dist");
		String digest = request.getParameter("checksum");
		
		log.info(buildMockResponse(zipName, jobUID, requestID, status, dist, digest));
		
		// If error is set to true, it will return an error to the client
		// So, if you want to simulate an error, just switch error from true to false
		if (error) {
			log.info("Error requested because error is " + error);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Fornisce una stringa contenente i parametri ricevuti con la richiesta
	 * @param zipName il nome dello zip ricevuto con la richiesta
	 * @param jobUID l'id del job ricevuto con la richiesta
	 * @param requestID l'id della richiesta 
	 * @param status lo stato del job ricevuto con la richiesta
	 * @param dist la directory ricevuta con la richiesta
	 * @return una stringa contenente i parametri ricevuti con la richiesta
	 */
	private String buildMockResponse(String zipName, String jobUID, String requestID, String status, String dist, String digest) {
		StringBuilder mockResponseBuilder = new StringBuilder("From request: ")
			.append("?zipName=")
			.append(zipName)
			.append("&jobUID=")
			.append(jobUID)
			.append("&status=")
			.append(status);
		
		if (requestID != null && !requestID.isEmpty()) {
			mockResponseBuilder.append("&requestID=").append(requestID);
		}
		
		if (digest != null && !digest.isEmpty()) {
			mockResponseBuilder.append("&checksum=").append(digest);
		}
		
		if (dist != null && !dist.isEmpty()) {
			mockResponseBuilder.append("&dist=").append(dist);
		}
		
		return mockResponseBuilder.toString();
	}
}
