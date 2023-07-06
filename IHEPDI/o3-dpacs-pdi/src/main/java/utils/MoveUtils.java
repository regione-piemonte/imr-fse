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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
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
 * Fornisce i metodi per l'esecuzione dell'operazione move
 */
public class MoveUtils {
	
	private static final Logger log = Logger.getLogger(MoveUtils.class);
	
	/**
	 * Esegue la move per gli accession number specificati
	 * @param failedAccNums accession number per cui risulta necessaria la move
	 * @param patientID l'id del paziente
	 * @param struttura la struttura per la move
	 * @param azienda l'azienda per la move
	 * @param idIssuer l'id dell'issuer per la move
	 * @param requestID l'id della richiesta
	 * @param basicAuth le credenziali per richiedere la movimentazione
	 * @param user lo username per la movimentazione
	 * @param pwd la password per la movimentazione
	 * @param retries il numero di tentavi per la richiesta
	 * @param wado basepath dell'url
	 * @return true se la move andata a buon fine, false altrimenti
	 */
	public static boolean markForStudyMove(List<String> failedAccNums, String patientID, String struttura, 
			String azienda, String idIssuer, String requestID, String basicAuth, String user, 
			String pwd, String retries, String wado, String ris, String sleepMove, List<String> accNumPartialOK) {
		if (failedAccNums.size() == 0) {
			log.info("No accession number has failed");
			return true;
		}
				
		boolean check = false;
		int moveCounter = 0;
		Boolean[] alreadyMoving = initBooleanArray(failedAccNums.size());
		Boolean[] moveCompleted = initBooleanArray(failedAccNums.size());
		
		
		log.info("Accession number to check: " + moveCompleted.length);
		do {
			for (int failedAccNumIndex = 0; failedAccNumIndex < failedAccNums.size(); failedAccNumIndex++) {
				String failedAccNum = failedAccNums.get(failedAccNumIndex);
				boolean isAlreadyMoving = alreadyMoving[failedAccNumIndex];
				int moved = -1;
				if (!isAlreadyMoving) {
					moved = move(failedAccNum, patientID, struttura, azienda, idIssuer, requestID, 
							basicAuth, user, pwd, retries, wado, ris);
					alreadyMoving[failedAccNumIndex] = Boolean.TRUE;
				} else {
					moved = isMoveCompleted(failedAccNum, patientID, struttura, azienda, idIssuer, 
							requestID, basicAuth, user, pwd, retries, wado, ris);
				}
				
				if (moved == -1) {
					log.info("Cannot continue with move");
					return false;
				}
				
				if (moved == 0) {
					log.info("A move has not been completed yet");
					moveCompleted[failedAccNumIndex] = false;
					moveCounter++; 
					if (moveCounter > 5) {
						log.info("Dopo 5 tentativi passo ad un altro Accession Number.");
						return false;
					}
				} 
				
				if (moved >= 1) {
					log.info("A move has been completed");
					if(moved>1) {
						accNumPartialOK.add(failedAccNum);
					}
					moveCompleted[failedAccNumIndex] = true;
				}
			}
			
			boolean wait = false;
			for (int i = 0; i < moveCompleted.length; i++) {
				if (!moveCompleted[i]) {
					log.info("Still have to wait for move completition...");
					wait = true;
				}
			}
			
			if (wait) {
				log.info("A move has not terminated yet");
				
				int sleep = ConfigurationSettings.SLEEP_MOVE;
				if(StringUtils.isNotEmpty(sleepMove)) {
					log.info("Sleep Move valorizzata");
					sleep = Integer.valueOf(sleepMove);
				}
				log.info("Sleep:" + sleep);
				StreamUtils.waitForRetry(sleep);
			} else {
				log.info("All moves have terminated");
				check = true;
			}
		} while (!check);
		
						
		return true;
	}
	
	/**
	 * Inizia il processo di movimentazione degli esami
	 * @param accNum l'accession number di riferimento per lo studio
	 * @param patientID l'id del paziente
	 * @param struttura la struttura di riferimento
	 * @param azienda l'azienda per la movimentazione
	 * @param idIssuer l'id dell'iussuer
	 * @param basicAuth le credenziali per richiedere la movimentazione
	 * @param user lo username per la movimentazione
	 * @param pwd la password per la movimentazione
	 * @param numRetries il numero di tentavi per la richiesta
	 * @return true se movimentazione terminata, false altrimenti
	 */
	private static int move(String accNum, String patientID, String struttura, 
			String azienda, String idIssuer, String requestID, String basicAuth, 
			String user, String pwd, String numRetries, String wado, String ris) {
		String url = getMarkForStudyMoveUrl(accNum, patientID, struttura, azienda, idIssuer, requestID, user, pwd, wado, ris);
		log.info("Url for markForStudyMove: " + url);
		
		int code = 0;
		String msg = "";
		int retries = Integer.parseInt(numRetries);
		for (int retry = 1; retry <= retries; retry++) {
			try {
				URL urlConn = new URL(url);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + basicAuth);
				code = ((HttpURLConnection) conn).getResponseCode();
				msg = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for markForMove: " + code + ", message: " + msg);
				if (code == 200) {
					log.info("call to markForStudyMove succedeed"); 
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						if ((line = reader.readLine()) != null) {
							if (line.equals("-1")) {
								log.info("Cannot perform move due to bad request");
								return -1;
							}
						}
					}
					
					return isMoveCompleted(accNum, patientID, struttura, azienda, idIssuer, requestID, basicAuth, user, pwd, numRetries, wado, ris);
				}
			} catch (Exception e) {
				log.error("Chiamata servlet /markForStudyMove fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/markForStudyMove going to sleep... " + retry);
					StreamUtils.waitForRetry(ConfigurationSettings.SLEEP);
					if (retry == retries) {
						log.info("Nessun risultato, markForStudyMove count: " + retry);
					} else {
						log.info("Riprovare la chiamata al servizio markForStudyMove, count: " + retry);
					}
				}
			}
		}
		
		return -1;
	}
	
	 /**
     * Metodo che permette di ottenere informazione sullo stato della move
	 * @param accNum l'accession number di riferimento per lo studio
	 * @param patientID l'id del paziente
	 * @param struttura la struttura di riferimento
	 * @param azienda l'azienda per la movimentazione
	 * @param idIssuer l'id dell'iussuer
	 * @param basicAuth le credenziali per richiedere la movimentazione
	 * @param user lo username per la movimentazione
	 * @param pwd la password per la movimentazione
	 * @param numRetries il numero di tentavi per la richiesta
     * @return lo stato della move
     */
	private static int isMoveCompleted(String accNum, String patientID, String struttura, 
			String azienda, String idIssuer, String requestID, String basicAuth, 
			String user, String pwd, String numRetries, String wado, String ris) {
		String url = getIsMoveCompletedUrl(accNum, patientID, struttura, azienda, idIssuer, requestID, user, pwd, wado, ris);
		log.info("Url for isMoveCompleted: " + url);
		int moved = 0;
		int code = 0;
		String msg = "";
		int retries = Integer.parseInt(numRetries);
		for (int retry = 1; retry <= retries; retry++) {
			try {
				URL urlConn = new URL(url);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + basicAuth);
				code = ((HttpURLConnection) conn).getResponseCode();
				msg = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for isMoveCompleted: " + code + ", message: " + msg);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							moved = getIsMoveCompletedResult(line);
						}
					} 
					
					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /isMoveCompleted fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/isMoveCompleted going to sleep... " + retry);
					StreamUtils.waitForRetry(ConfigurationSettings.SLEEP);
					if (retry == retries) {
						log.info("Nessun risultato, isMoveCompleted count: " + retry);
					} else {
						log.info("Riprovare la chiamata al servizio isMoveCompleted, count: " + retry);
					}
				}
			}
		}
		
		return moved;
	}
	
	/**
	 * Fornisce il risultato della move
	 * @return il risultato della move
	 */
	private static int getIsMoveCompletedResult(String line) {
		log.info("Getting result for isMoveCompleted...");
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			StringReader reader = new StringReader(line);
			InputSource is = new InputSource(reader);
			Document doc = docBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nLst = doc.getElementsByTagName("move");
			Node moveNode = nLst.item(0); 
			Element moveElement = (Element) moveNode;
			String completed = moveElement.getAttribute("completed");
			
			int result = Integer.parseInt(completed);
			log.info("Move result is " + result);
			
			return result;
		} catch (ParserConfigurationException e) {
			log.error("Impossibile analizzare il risultato...", e);
		} catch (SAXException e) {
			log.error("Impossibile analizzare il file di configurazione! ", e);
		} catch (Exception oex) {
			log.error("Impossibile caricare il file di configurazione...", oex);
		}
		
		return -1;
	}

	/**
	 * Inizializza un array di booleani impostati a false
	 * @param size la dimensione per l'array
	 * @return l'array creato
	 */
	private static Boolean[] initBooleanArray(int size) {
		Boolean[] booleans = new Boolean[size];
		for (int i = 0; i < size; i++) {
			booleans[i] = Boolean.FALSE;
		}
		
		return booleans; 
	}
	
	/**
	 * Fornisce l'URL del servizio markForStudyMove
	 * @param seriesUID l'identificativo della serie
	 * @param studyUID l'identificativo dello studio
	 * @return l'URL del servizio getInstance
	 */
	private static String getMarkForStudyMoveUrl(String accNum, String patientID, 
			String struttura, String azienda, String idIssuer, String requestID, 
			String user, String pwd, String wado, String ris) {
		PdiRetrieveManager prm = new PdiRetrieveManager();
		String dpacsWeb =	prm.getConfigParamPDI(ConfigurationSettings.DPACS_WEB);
		StringBuilder urlBl = new StringBuilder(wado)
			.append(dpacsWeb + "markForStudyMove")
			.append("?action=move")
			.append("&username=")
			.append(user)
			.append("&password=")
			.append(pwd)
			.append("&accNum=")
			.append(accNum);
		
		if(StringUtils.isNotEmpty(azienda)) {
			urlBl = urlBl.append("&azienda=")
			.append(azienda);
		}
		
		if(StringUtils.isNotEmpty(struttura)) {
			urlBl = urlBl.append("&struttura=")
					.append(struttura);
		}
		
		if(StringUtils.isNotEmpty(requestID)) {
			urlBl = urlBl.append("&messageID=")
					.append(requestID);
		}
		
		if(StringUtils.isNotEmpty(ris)) {
			urlBl = urlBl.append("&ris=")
					.append(ris);
		}
		if(StringUtils.isNotEmpty(patientID)) {
			urlBl = urlBl.append("&patientID=")
					.append(patientID);
		}
		if(StringUtils.isNotEmpty(idIssuer)) {
			urlBl = urlBl.append("&idIssuer=")
					.append(idIssuer);
		}
		
		log.debug("Url di chiamata per move: " + urlBl.toString());
		
		return urlBl.toString();
	}
	
	/**
	 * Fornisce l'URL del servizio isMoveCompleted
	 * @param seriesUID l'identificativo della serie
	 * @param studyUID l'identificativo dello studio
	 * @return l'URL del servizio getInstance
	 */
	private static String getIsMoveCompletedUrl(String accNum, String patientID, 
			String struttura, String azienda, String idIssuer, String requestID, 
			String user, String pwd, String wado, String ris) {
		PdiRetrieveManager prm = new PdiRetrieveManager();
		String dpacsWeb =	prm.getConfigParamPDI(ConfigurationSettings.DPACS_WEB);

		String url	= null;
		StringBuilder sb = new StringBuilder(wado)
			.append(dpacsWeb + "isMoveCompleted")
			.append("?username=")
			.append(user)
			.append("&password=")
			.append(pwd)
			.append("&accNum=")
			.append(accNum);
		if(StringUtils.isNotEmpty(azienda)) {
			sb = sb.append("&azienda=")
					.append(azienda);
		}
	
		if(StringUtils.isNotEmpty(struttura)) {
			sb = sb.append("&struttura=")
					.append(struttura);
		}
		
		if(StringUtils.isNotEmpty(requestID)) {
			sb = sb.append("&messageID=")
					.append(requestID);
		}
		
		if(StringUtils.isNotEmpty(ris)) {
			sb = sb.append("&ris=")
					.append(ris);
		}
		
		if(StringUtils.isNotEmpty(idIssuer)) {
			sb = sb.append("&idIssuer=")
					.append(idIssuer);
		}
		
		if(StringUtils.isNotEmpty(patientID)) {
			sb = sb.append("&patientID=")
					.append(patientID);
		}
			
		url = sb.toString();	
				
		log.info("Url di chiamata per isMoveCompleted: " + url);
		
		return url;
	}
	
	
}
