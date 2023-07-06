/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package dpacspdi;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import dao.PdiRetrieveManager;

/**
 * Servlet che esponde servizi per la gestione della pulizia
 */
@WebServlet("/ExpirationPdi")
public class ExpirationPdi extends HttpServlet {

	private static final long serialVersionUID = 4636823804979427312L;

	private Logger log = Logger.getLogger(ExpirationPdi.class);

	private static final String ON = "ON";
	private static final String OFF = "OFF";
	private static final String EXPIRE_AT = "ExpireAt";
	
	/**
	 * Mode per il job
	 */
	private String mode;
	
	/**
	 * Riferimento al job eventualmente in esecuzione
	 */
	private Thread thread;
	
	/**
	 * Istanzia un oggetto di tipo ExpirationPdi
	 */
	public ExpirationPdi() {
		mode = OFF;
		thread = null;
	}

	public void init(ServletConfig config) throws ServletException {
		log.info("Init servlet with mode ON...");
		mode = OFF;
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

	/**
	 * Metodo che si occupa di pulire i job
	 * Se la modalita' e' on pulisce i job scaduti se presenti
	 */
	protected synchronized void doWork(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter writer = response.getWriter();
		String givenMode = request.getParameter("mode").toUpperCase();
		if (modeIsNotValid(givenMode)) {
			log.info("The given mode is not valid: " + givenMode);
			writer.append("The given mode is not valid: " + givenMode);
			return;
		}

		// If the given mode is the same as the actual mode
		// then nothing has to be done.
		if (isModeUnchanged(givenMode)) {
			writer.append("Mode is already set to: " + givenMode);
			return;
		}
		
		// If the mode is off, it means that the mode sent
		// with the request is on. So, the thread has to be started.
		// Otherwise, the mode is on, so we just need to set it to off.
		if (isModeOff()) {
			setMode(ON);
			log.info("Mode is now ON, starting the thread...");
			boolean jobStartedSuccessfully = cleanExpiredJobs();
			if (!jobStartedSuccessfully) {
				log.info("Cannot start batch");
				writer.append("Batch cannot be started");
				return;
			}
			log.info("Batch started...");
			writer.append("Batch started");
		} else {
			if (thread.isAlive()) {
				try {
					thread.interrupt();
				} catch (Exception e) {
					log.error("Batch stopped: " + e.getMessage(), e);
				}
			} else {
				log.info("Batch has completed its job...");
			}
			
			setMode(OFF);
			log.info("Mode is now OFF");
			writer.append("Batch stopped");
		}
	}

	/**
	 * Metodo che controlla se la modalita' e' off
	 * @return true se la modalita' e' off, false altrimenti
	 */
	private boolean isModeOff() {
		return mode.equals(OFF);
	}

	/**
	 * Metodo che controlla se la modalita' fornita e' la stessa
	 * @param mode nuova modalita'
	 * @return true se la modalita' e' la stessa, false altrimenti
	 */
	private boolean isModeUnchanged(String mode) {
		if (getMode().equalsIgnoreCase(mode)) {
			log.info("Mode is already set to: " + mode);
			return true;
		}

		return false;
	}

	/**
	 * Metodo che controlla se la modalita' fornita e' valida
	 * @param mode nuova modalita'
	 * @return true se la modalita' e' valida, false altrimenti
	 */
	private boolean modeIsNotValid(String mode) {
		if (mode == null || mode.isEmpty()) {
			log.info("Mode was not provided");
			return true;
		}

		if (!mode.equalsIgnoreCase(ON) && !mode.equalsIgnoreCase(OFF)) {
			log.info("The provided mode is not valid");
			return true;
		}

		return false;
	}

	/**
	 * Metodo che avvia il thread di pulizia
	 * @param expireAt tempo su cui si basa la scadenza dei job
	 */
	private boolean cleanExpiredJobs() {
		try {
			log.info("Getting expiration time...");
			int expireAt = Integer.parseInt(new PdiRetrieveManager().getConfigParamPDI(EXPIRE_AT));
			log.info("Creating thread for cleaning jobs older than " + expireAt + "...");
			ExpirationThread expirationThread = new ExpirationThread(expireAt);
			thread = new Thread(expirationThread);
			log.info("Starting thread for expired jobs...");
			thread.start();
			return true;
			
		} catch (Exception e) {
			log.error("Error while starting batch due to: " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Fornisce accesso alla modalita' di esecuzione del job
	 * @return la modalita' di esecuzione del job
	 */
	private String getMode() {
		return mode;
	}

	/**
	 * Permette di impostare la modalita' di esecuzione del job
	 * @param mode la modalita' di esecuzione del job
	 */
	private void setMode(String mode) {
		this.mode = mode;
	}
}
