/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dao.PdiRetrieveManager;
import helpers.ConfigurationSettings;

/**
 * Fornisce metodi per la gestione delle utenze
 */
public class UserUtils {
	
	private static final Logger log = Logger.getLogger(UserUtils.class);

	/**
	 * Autentica sulla base delle credenziali inviate nella richiesta
	 * @param username lo username dell'utente
	 * @param password la password dell'utente
	 * @param numRetries il numero di tentativi
	 * @param baseUrl base path dell'url
	 * @return true se l'autenticazione ha successo, false altrimenti
	 */
    public static boolean checkUser(String username, String password, String numRetries, String baseUrl) {
    	String url = getCheckUserUrl(username, password, baseUrl);
		log.debug("Url for CheckUser: " + url);
		int isAuth = 0;
		int code = 0;
		String msg = "";
		int retries = Integer.parseInt(numRetries);
		for (int retry = 1; retry <= retries; retry++) {
			try {
				URL urlConn = new URL(url);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				code = ((HttpURLConnection) conn).getResponseCode();
				msg = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for CheckUser: " + code + ", message: " + msg);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							isAuth = getCheckUserResult(line);
						}
					} 
					
					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /CheckUser fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/CheckUser going to sleep... " + retry);
					StreamUtils.waitForRetry(ConfigurationSettings.SLEEP);
					if (retry == retries) {
						log.info("Nessun risultato, CheckUser count: " + retry);
					} else {
						log.info("Riprovare la chiamata al servizio CheckUser, count: " + retry);
					}
				}
			}
		}
		
		if (isAuth == 0) {
			return false;
		}
		
		return true;
    }
    
    /**
	 * Fornisce il risultato del servizio CheckUser
	 * @return il risultato del servizio CheckUser
	 */
    private static int getCheckUserResult(String line) {
		log.info("Getting result for CheckUser...");
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			StringReader reader = new StringReader(line);
			InputSource is = new InputSource(reader);
			Document doc = docBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nLst = doc.getElementsByTagName("user");
			Node authNode = nLst.item(0); 
			Element authElement = (Element) authNode;
			String auth = authElement.getAttribute("auth");
			
			int isAuth = Integer.parseInt(auth);
			log.info("Check user result is " + isAuth);
			
			return isAuth;
		} catch (ParserConfigurationException e) {
			log.error("Impossibile analizzare il risultato...", e);
		} catch (SAXException e) {
			log.error("Impossibile analizzare il file di configurazione! ", e);
		} catch (Exception oex) {
			log.error("Impossibile caricare il file di configurazione...", oex);
		}
		
		return 0;
	}

	/**
     * Fornisce URL per il servizio CheckUser
     * @param username lo username dell'utente
     * @param password la password dell'utente
     * @return l'URL per il servizio CheckUser
     */
    private static String getCheckUserUrl(String username, String password, String baseUrl) {
    	PdiRetrieveManager prm = new PdiRetrieveManager();
		String dpacsWeb =	prm.getConfigParamPDI(ConfigurationSettings.DPACS_WEB);
    	String url = new StringBuilder(baseUrl)
    			.append(dpacsWeb + "/CheckUser")
    			.append("?username=")
    			.append(username)
    			.append("&password=")
    			.append(password)
    			.toString();
    	
    	log.info("Url is " + url);
    	
    	return url;
    }
}
