/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package dpacspdi;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import helpers.ResponseCode;
import helpers.ResponseMessage;
import objects.Asl;
import utils.ResponseUtils;
import utils.UserUtils;

@WebServlet("/CreatePdi")
public class CreatePdi extends HttpServlet {

	private static final long serialVersionUID = 2751927747659375251L;

	private static final Logger log = Logger.getLogger(CreatePdi.class);
	
	/**
	 * Url per l'invocazione dei servizi WADO
	 */
	private String wadoUrl;
	
	/**
	 * Numero retry per l'invocazione dei servizi dell'ASL
	 */
	private String numeRetry;
	
	/**
	 * Username per l'invocazione dei servizi
	 */
	private String username;
	
	/**
	 * Password per l'invocazione dei servizi
	 */
	private String password;
	
	/**
	 * Configurazione caricata
	 */
	private String xmlConf;
	
	/**
	 * Lista delle ASL disponibili
	 */
	private List<Asl> listAsl;
	
	/**
	 * ASL richiesta
	 */
	private Asl asl;
	
	/**
	 * Id del job attivato
	 */
	private String jobId;
	
	public CreatePdi() {}

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

	/**
	 * Si occupa della generazione del contenuto PDI, gestendo la molteplicita' di studi
	 * oppure accession number specificati in modo da recuperare tutti i contenuti PDI ad essi associati
	 */
	protected synchronized void doWork(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		PdiRetrieveManager prm = new PdiRetrieveManager();
		String numCurrentThreadStr = prm.getConfigParamPDI(ConfigurationSettings.NUM_CURRENT_THREAD);
		String numMaxThreadStr = prm.getConfigParamPDI(ConfigurationSettings.NUM_MAX_THREAD);
		Integer numMaxThread		= StringUtils.isNotEmpty(numMaxThreadStr) ? Integer.valueOf(numMaxThreadStr) : 100;
		Integer numCurrentThread 	= StringUtils.isNotEmpty(numCurrentThreadStr) ? Integer.valueOf(numCurrentThreadStr) : 0;
		
		if (isServletActive()) {
			
			if(numCurrentThread>=numMaxThread) {
				log.info("Numero thread massimo raggiunto(#"+ numCurrentThreadStr +")");
				ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_301, ResponseMessage.CC_ERR_301);
				return;
			}
			
			String requestID = request.getParameter("requestID");
			String patientID = request.getParameter("patientID");
			String studyUID = request.getParameter("studyUID");
			String accessionNumber = request.getParameter("accessionNumber");
			String idIssuer = request.getParameter("idIssuer");
			String azienda = request.getParameter("azienda");
			String struttura = request.getParameter("struttura");
			String ris = request.getParameter("ris");
			
			log.info("======================STAMPO PARAMETRI IN CREATE PDI====================");
			log.info(" ");
			log.info(" - requestID =  " + requestID);
			log.info(" - patientID = " + patientID);
			log.info(" - studyUID = " + studyUID);
			log.info(" - accessionNumber = " + accessionNumber);
			log.info(" - idIssuer = " + idIssuer);
			log.info(" - azienda = " + azienda);
			log.info(" - struttura = " + struttura);
			log.info(" - ris = " + ris);
			log.info(" ");
			log.info("======================STAMPO PARAMETRI IN CREATE PDI====================");
					
			try {
				prm = new PdiRetrieveManager();
				azienda = buildAzienda(azienda, response, prm);
				if (!loadConfigXml(azienda)) {
					log.info("Impossible load configuration");
					ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_210, ResponseMessage.CC_ERR_210);
					return;
				}
				
				boolean isAuth = verifyCredentials(request);
				if (!isAuth) {
					ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_209, ResponseMessage.CC_ERR_209);
					return;
				}
				
				if(StringUtils.isNotEmpty(requestID)) {
					log.info("Check request duplicata");
					if(prm.checkRequestIDExist(requestID)) {
						ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_217, ResponseMessage.CC_ERR_217);
						return;
					}
				}
				
				if (!isRequestIDValid(requestID, response)) {
					return;
				}
				
				if (!areAccNumAndStudyValid(accessionNumber, studyUID, prm, response)) {
					return;
				}
				
				if(StringUtils.isNotEmpty(accessionNumber)) {
					if(accessionNumber.endsWith(",")) {
						ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_304, ResponseMessage.CC_ERR_304);
						return;
					}
				}
				
				
				if(StringUtils.isNotEmpty(studyUID)) {
					if(studyUID.endsWith(",")) {
						ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_305, ResponseMessage.CC_ERR_305);
						return;
					}
				}
				
				String createdJobId = prm.insertTo3pdiJob("", "", "", "START", azienda);
				setJobId(createdJobId);
				ResponseUtils.writeSuccessOnResponse(response, getJobId());
				
				numCurrentThread = numCurrentThread +1;
				prm.updateTo3pdiConf(ConfigurationSettings.NUM_CURRENT_THREAD, String.valueOf(numCurrentThread));
				CreatePdiThread createPdiThread = new CreatePdiThread(requestID, patientID, studyUID, accessionNumber, idIssuer, 
						azienda, struttura, createdJobId, ris, response, username, password, prm);
				Thread thread = new Thread(createPdiThread);
				thread.start();
				
				
				
				return;
			} catch (Exception e) {
				ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_500, ResponseMessage.CC_ERR_500);
				log.error("Errore interno: " + e.getMessage(),e);
				return;
			}
		} else {
			ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_212, ResponseMessage.CC_ERR_212);
		}
	}
	
	public static void main(String[] args) {
		String accessionNumber = "123,345";
		if(StringUtils.isNotEmpty(accessionNumber)) {
			if(accessionNumber.endsWith(",")) {
				System.out.println("Sbagliato");
			}
		}
	}
	
	/**
	 * Recupera la configurazione dell'ASL desiderata dalla lista di ASL
	 * @param azienda il nome dell'azienda desiderata
	 */
	private boolean loadAslConfiguration(String azienda) {
		boolean isAslPresent = false;
		
		log.info("ASL list has " + listAsl.size() + " asl");
		for (Asl asl : listAsl) {
			if (asl.getNome().equals(azienda)) {
				log.info("ASL is present");
				isAslPresent = true;
				
				setWadoUrl(asl.getEndpoint());
				setNumeRetry(asl.getNumeRetry());
				log.info("Azienda: " + azienda + ", wadoUrl: " + getWadoUrl() + ", numeRetry: " + getNumeRetry());
			}
		}
		
		if (!isAslPresent) {
			log.info("ASL not present");
		}
		
		return isAslPresent;
	}
	
	/**
	 * Fornisce l'id del paziente a partire dalle info del paziente
	 * @return l'id del paziente
	 */
	@SuppressWarnings("unused")
	private String getPatientIDFromPatient(String line) {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			StringReader reader = new StringReader(line);
			InputSource is = new InputSource(reader);
			Document doc = docBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nLst = doc.getElementsByTagName("patient");
			for (int i = 0; i < nLst.getLength(); i++) {
				Node patientNode = nLst.item(i);
				log.info("Current Element: " + patientNode.getNodeName());
				Element patient = (Element) patientNode;
				String obtainedPatientID = patient.getAttribute("patientID");
				log.info("obtainedPatientID ---> " + obtainedPatientID);
				return obtainedPatientID;
			}
		} catch (ParserConfigurationException e) {
			log.error("Impossibile analizzare il risultato: ", e);
		} catch (SAXException e) {
			log.error("Impossibile analizzare il file di configurazione: ", e);
		} catch (Exception oex) {
			log.error("Impossibile caricare il file di configurazione: ", oex);
		}
		
		return null;
	}
	
    /**
     * Metodo utile al caricamento della configurazione
     * @return true se la configurazione viene caricata correttamente, false altrimenti
     */
	private boolean loadConfigXml(String azienda) {
		listAsl = new ArrayList<Asl>();
		PdiRetrieveManager prm;
		try {
			prm = new PdiRetrieveManager();
			xmlConf = prm.getConfigParamPDI(ConfigurationSettings.XML_FILE_CONF);
			log.info("File configurazione: " + xmlConf);
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			try {
				docBuilder = docBuilderFactory.newDocumentBuilder();
				StringReader reader = new StringReader(xmlConf);
				InputSource is = new InputSource(reader);
				Document doc = docBuilder.parse(is);
				doc.getDocumentElement().normalize();
				NodeList nLst = doc.getElementsByTagName(ConfigurationSettings.ROOTXML);
				for (int i = 0; i < nLst.getLength(); i++) {
					asl = new Asl();
					Node nNode = nLst.item(i);
					Element eElement = (Element) nNode;
					asl.setIdAsl(eElement.getAttribute("id") != null ? eElement.getAttribute("id") : null);
					asl.setNome(eElement.getElementsByTagName("nome").item(0) != null? eElement.getElementsByTagName("nome").item(0).getTextContent(): null);
					asl.setEndpoint(eElement.getElementsByTagName("endpoint").item(0) != null? eElement.getElementsByTagName("endpoint").item(0).getTextContent(): null);
					asl.setNumeRetry(eElement.getElementsByTagName("nume_retry").item(0) != null? eElement.getElementsByTagName("nume_retry").item(0).getTextContent(): null);
					asl.setDist(eElement.getElementsByTagName("dist").item(0) != null? eElement.getElementsByTagName("dist").item(0).getTextContent(): null);
					asl.setXslt(eElement.getElementsByTagName("xslt").item(0) != null? eElement.getElementsByTagName("xslt").item(0).getTextContent(): null);
					asl.setCss(eElement.getElementsByTagName("css").item(0) != null? eElement.getElementsByTagName("css").item(0).getTextContent(): null);
					asl.setCommandMove(eElement.getElementsByTagName("commandMove").item(0) != null? eElement.getElementsByTagName("commandMove").item(0).getTextContent(): null);
					asl.setViewerFolder(eElement.getElementsByTagName("viewer").item(0) != null? eElement.getElementsByTagName("viewer").item(0).getTextContent(): null);
					
					listAsl.add(asl);
				}
			} catch (ParserConfigurationException e) {
				log.error("Impossibile analizzare la configurazione: ", e);
				return false;
			} catch (IOException e) {
				log.error("Impossibile aprire il file di configurazione: ", e);
				return false;
			} catch (SAXException e) {
				log.error("Impossibile analizzare il file di configurazione: ", e);
				return false;
			} catch (Exception oex) {
				log.error("Impossibile caricare il file di configurazione: ", oex);
				return false;
			}
		} catch (Exception ne) {
			log.error("Errore inizializzazione: ", ne);
			return false;
		}
		
		if(!loadAslConfiguration(azienda)) {
			log.info("Cannot load ASL configuration");
			return false;
		}
		
		return true;
	}

	/**
	 * Gestisce le credenziali fornite con la richiesta
	 */
	private boolean verifyCredentials(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		log.info("AuthHeader: " + authHeader);
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();
				if (basic.equalsIgnoreCase("Basic")) {
					byte[] bytes = null;
					try {
						bytes = Base64.getDecoder().decode(st.nextToken());
					} catch (Exception e) {
						log.error("Cannot decode authorization due to: " + e.getMessage(), e);
						e.printStackTrace();
					}
					String credentials = new String(bytes);
					log.debug("Credenziali: " + credentials);
					int p = credentials.indexOf(":");
					if (p != -1) {
						username = credentials.substring(0, p).trim();
						password = credentials.substring(p + 1).trim();
						return UserUtils.checkUser(username, password, getNumeRetry(), getWadoUrl());
					} else {
						log.error("Token di autenticazione non valido");
					}
				}
			}
		}
		
		return false;
	}
    
    /**
     * Verifica che la servlet sia attiva
     * @return true se servlet attiva, false altrimenti
     */
	private boolean isServletActive() {
		log.info("Verifying if servlet is active...");
		try {
			PdiRetrieveManager prm = new PdiRetrieveManager();
			String servletActive = prm.getConfigParamPDI(ConfigurationSettings.SERVLET_ACTIVATION);
			if (servletActive.equalsIgnoreCase(ConfigurationSettings.SERVLET_ACTIVE)) {
				log.info("Servlet is active");
				
				return true;
			}
		} catch (Exception e) {
			log.info("Cannot check if servlet is active");
		}
		
		log.info("Servlet is not active");
		
		return false;
	}
	
	/**
	 * Verifica che l'accession number e lo studio forniti siano corretti
	 * @param accessionNumber l'accession number da controllare
	 * @param studyUID lo studio da controllare
	 * @return true se l'accession number e lo studio forniti risultano corretti, false altrimenti
	 */
	private boolean areAccNumAndStudyValid(String accessionNumber, String studyUID, 
			PdiRetrieveManager prm, HttpServletResponse response) throws IOException {
		log.info("Checking if accession number and stuidyUID are both absent...");
		boolean bothAbsent = (studyUID == null || studyUID.equals("")) && (accessionNumber == null || accessionNumber.equals(""));
		if (bothAbsent) {
			ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_201, ResponseMessage.CC_ERR_201);
			log.info("studyUID e accessionNumber assenti, almeno uno dei due obbligatorio");
			
			return false;
		}

		log.info("Checking if accession number and stuidyUID are both multiple...");
		if ((accessionNumber != null && accessionNumber.contains(ConfigurationSettings.SPLITTER)) && (studyUID != null && studyUID.contains(ConfigurationSettings.SPLITTER))) {
			log.info("studyUID e accessionNumber contengono entrambi piu valori");
			ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_202, ResponseMessage.CC_ERR_202);
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Verifica che il parametro requestID sia valido avendo al massimo 64 caratteri
	 * @param requestID il parametro da verificare
	 * @return true se requestID valide, false altrimenti
	 */
	private boolean isRequestIDValid(String requestID, HttpServletResponse response) throws IOException {
		if (requestID == null) {
			return true;
		}
		
		if (requestID.length() > 64) {
			log.info("Parameter requestID is not valid");
			ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_203, ResponseMessage.CC_ERR_203);
			
			return false;
		}
		
		return true;
	}

	/**
	 * Fornisce il valore corretto per l'azienda
	 * @param azienda l'eventuale ASL inviata con la richiesta
	 */
	private String buildAzienda(String azienda, HttpServletResponse response, PdiRetrieveManager prm) throws IOException {
		if (azienda == null || azienda.isEmpty()) {
			azienda =  prm.getConfigParamPDI(ConfigurationSettings.DEFAULT_ID_ASL);
		}
				
		return azienda;
	}

	/**
	 * Permette di ottenere l'id del job 
	 * @return l'id del job 
	 */
	private String getJobId() {
		return jobId;
	}

	/**
	 * Permette di impostare l'id del job 
	 * @param jobId l'id del job 
	 */
	private void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * Permette di ottenere l'url per l'invocazione dei servizi
	 * @return l'url per l'invocazione dei servizi
	 */
	private String getWadoUrl() {
		return wadoUrl;
	}

	/**
	 * Permette di impostare l'url per l'invocazione dei servizi
	 * @param wadoUrl l'url per l'invocazione dei servizi
	 */
	private void setWadoUrl(String wadoUrl) {
		this.wadoUrl = wadoUrl;
	}

	/**
	 * Permette di ottenere il numero di tentativi per le invocazioni ai servizi
	 * @return numero di tentativi per le invocazioni ai servizi
	 */
	private String getNumeRetry() {
		return numeRetry;
	}

	/**
	 * Permette di impostare il numero di tentativi per le invocazioni ai servizi
	 * @param numeRetry numero di tentativi per le invocazioni ai servizi
	 */
	private void setNumeRetry(String numeRetry) {
		this.numeRetry = numeRetry;
	}
}