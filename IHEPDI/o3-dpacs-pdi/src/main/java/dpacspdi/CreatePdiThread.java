/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package dpacspdi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import objects.Patient;
import utils.CommandUtils;
import utils.DicomViewGenerator;
import utils.DigestUtils;
import utils.MoveUtils;
import utils.ResponseUtils;
import utils.StreamUtils;
import utils.ZipUtils;

/**
 * Thread per l'esecuzione del job
 */
public class CreatePdiThread implements Runnable {

	private static final Logger log = Logger.getLogger(CreatePdiThread.class);
	
	/**
	 * Url per l'invocazione dei servizi WADO
	 */
	private String wadoUrl;
	
	/**
	 * Url specificato per l'ASL
	 */
	private String dpacsWadoUrl;
	
	/**
	 * URL per l'invocazione del servizio di notifica
	 */
	private String completeNoticeUrl;
	
	/**
	 * Numero retry per l'invocazione dei servizi dell'ASL
	 */
	private String numeRetry;
	
	/**
	 * Percorso alla directory di destinazione per il contenuto PDI
	 */
	private String dist;
	
	/**
	 * Endpoint del servizio getPatientInfo
	 */
	private String dpacsPatientInfo;
	
	/**
	 * Percorso alla directory della workarea
	 */
	private String workarea;
	
	/**
	 * Username per l'invocazione dei servizi
	 */
	private String username;
	
	/**
	 * Password per l'invocazione dei servizi
	 */
	private String password;
	
	/**
	 * Username per l'invocazione dei servizi FSE
	 */
	private String usernameFse;
	
	/**
	 * Password per l'invocazione dei servizi FSE
	 */
	private String passwordFse;
	
	/**
	 * Numero retry per l'invocazione del servizio di notifica
	 */
	private String numRetryNotifica;
	
	/**
	 * Tempo di attesa per l'invocazione del servizio di notifica
	 */
	private String sleepNotifica;
	
	/**
	 * Tempo di attesa per l'invocazione dei servizi WADO
	 */
	private String sleep;
	
	/**
	 * Tempo di attesa per l'invocazione dei servizi WADO - MOVE
	 */
	private String sleepMove;
	
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
	 * Data dello studio di riferimento
	 */
	private String studyDate;
	
	/**
	 * Id del job attivato
	 */
	private String jobId;
	
	/**
	 * Paziente di riferimento per il job
	 */
	private Patient patientInfo;
	
	/**
	 * Serie associate al paziente di riferimento per il job
	 */
	private List<String> seriesUIDs;
	
	/**
	 * Percorso dell'xslt dell'ASL di riferimento per il job
	 */
	private String xslt;
	
	/**
	 * Percorso del css dell'ASL di riferimento per il job
	 */
	private String css;
	
	/**
	 * Percorso del js dell'ASL di riferimento per il job
	 */
	private String js;
	
	/**
	 * Comando per l'esecuzione della move
	 */
	private String commandMove;
	
	/**
	 * Percorso della directory contenente il file zip del viewer
	 */
	private String viewerFolder;
	
	/**
	 * Digest del file zip generato per il contenuto PDI
	 */
	private String digest;
	
	/**
	 * ID della richiesta
	 */
	private String requestID;
	
	/**
	 * ID del paziente
	 */
	private String patientID;
	
	/**
	 * ID dello studip
	 */
	private String studyUID;

	/**
	 * Accession number dello studio
	 */
	private String accessionNumber;
	
	/**
	 * ID dell'issuer di riferimento
	 */
	private String idIssuer;
	
	/**
	 * ASL di riferimento
	 */
	private String azienda;
	
	/**
	 * RIS di riferimento
	 */
	private String ris;
	
	/**
	 * Struttura necessaria in caso di move
	 */
	private String struttura;
	
	private HttpServletResponse response;
	private PdiRetrieveManager prm;
	
	/**
	 * Istanza un thread per l'esecuzione del job
	 * @param requestID id della richiesta
	 * @param patientID id del paziente
	 * @param studyUID id dello studio
	 * @param accessionNumber accession number riferito allo studio
	 * @param idIssuer id dell'issuer
	 * @param azienda l'asl di riferimento
	 * @param struttura la struttura per un'eventuale operazione di move
	 * @param jobId id del job attivato
	 * @param ris struttura RIS
	 * @param username username per l'invocazione dei servizi
	 * @param password password per l'invocazione dei servizi
	 */
	public CreatePdiThread(String requestID, String patientID, String studyUID, String accessionNumber, 
			String idIssuer, String azienda, String struttura, String jobId, String ris, HttpServletResponse response, 
			String username, String password, PdiRetrieveManager prm) {
		this.requestID = requestID;
		this.patientID = patientID;
		this.studyUID = studyUID;
		this.accessionNumber = accessionNumber;
		this.idIssuer = idIssuer;
		this.azienda = azienda;
		this.struttura = struttura;
		this.jobId = jobId;
		this.response = response;
		this.username = username;
		this.password = password;
		this.prm = prm;
		this.ris = ris;
	}
	
	@Override
	public void run() {
		try {
			if (!loadConfigXml(azienda)) {
				log.error("Impossibile caricare la configurazione");
				ResponseUtils.writeErrorOnResponse(response, ResponseCode.CC_ERR_210, ResponseMessage.CC_ERR_210);
				return;
			}
			
			int parts = 0;
			List<String> partsAccNum = new ArrayList<String>();
			if (accessionNumber != null && accessionNumber.contains(ConfigurationSettings.SPLITTER)) {
				parts = 1;
				partsAccNum = Arrays.asList(accessionNumber.split(ConfigurationSettings.SPLITTER));
			}
			List<String> partsStudy = new ArrayList<String>();
			if (studyUID != null && studyUID.contains(ConfigurationSettings.SPLITTER)) {
				parts = 2;
				partsStudy = Arrays.asList(studyUID.split(ConfigurationSettings.SPLITTER));
			}
			
			log.info("Performing manifest operation...");
			List<String> accNumsToCheck = getAccessionNumberToCheck(accessionNumber, studyUID, parts, partsStudy, partsAccNum, prm);
			if ((accNumsToCheck == null || accNumsToCheck.isEmpty()) && (struttura == null || struttura.isEmpty())) {
				log.info("Accession number non fornito e studio non recuperabile o struttura non fornita");
				sendError(response, ResponseCode.CC_ERR_206, ResponseMessage.CC_ERR_206, "Stavo verificando la presenza degli esami...", requestID, azienda, prm);
				return;
			}
			
			if(accNumsToCheck != null && accNumsToCheck.size() > 0) {
				log.info("accNumsToCheck valorizzato...");
			}
			
			
			List<String> failedAccNums = getFailedAccessionNumbers(patientID, studyUID, idIssuer, accNumsToCheck, prm);
			
			if(failedAccNums != null && failedAccNums.size() > 0 && (struttura == null || struttura.isEmpty())) {
				log.info("failedAccNums valorizzato...");
				log.info("Accession number non fornito e studio non recuperabile o struttura non fornita");
				sendError(response, ResponseCode.CC_ERR_206, ResponseMessage.CC_ERR_206, "Stavo verificando la presenza degli esami...", requestID, azienda, prm);
				return;
			}
			
			List<String> accNumPartialOK = new ArrayList<String>();
			boolean moved = MoveUtils.markForStudyMove(failedAccNums, patientID, struttura, azienda, idIssuer, requestID, buildBasicAuth(), 
					username, password, getNumeRetry(), getWadoUrl(), ris, getSleepMove(), accNumPartialOK);
			
			if (!moved) {
				log.info("Error while performing move");
				sendError(response, ResponseCode.CC_ERR_216, ResponseMessage.CC_ERR_216, "Stavo eseguendo move", requestID, azienda, prm);
				return;
			}
			log.info("Manifest operation has terminated");
	
			int code = 0;
			String messCode = "";
			HashMap<String, Boolean> controlId = new HashMap<String, Boolean>();
			switch (parts) {
			case 1:
				log.info("Caso 1 - Elenco Accession Number");
				log.info("AccessionNumber length: " + partsAccNum.size());
				
				log.info("AccessionNumber Partilas length: " + accNumPartialOK.size());
				List<String> studiesList = new ArrayList<String>();
				if(StringUtils.isNoneEmpty(studyUID)) {
					studiesList.add(studyUID);
				}
				if (!isSamePatientByAccNumsAndStudies(partsAccNum,studiesList, patientID, prm, accNumPartialOK)) {
					sendError(response, ResponseCode.CC_ERR_205, ResponseMessage.CC_ERR_205, "Stavo verificando che il paziente fosse lo stesso", requestID, azienda, prm);
					return;
				}
				
				if(accNumPartialOK != null && accNumPartialOK.size() > 0 && accNumPartialOK.size()==partsAccNum.size()) {
					//sendError(response, ResponseCode.CC_ERR_206, ResponseMessage.CC_ERR_206, "Gli Accession Number sono tutti non presenti/privi di immagine", requestID, azienda, prm);
					sendError(response, ResponseCode.CC_ERR_218, ResponseMessage.CC_ERR_218, "Gli Accession Number sono tutti non presenti/privi di immagine", requestID, azienda, prm);
					return;
				}
				
				List<String> studyUIDsForDicomViewGenerator = new ArrayList<String>();
				for (int accNum = 0; accNum < partsAccNum.size(); accNum++) {
					controlId.put(partsAccNum.get(accNum), Boolean.TRUE);
										
					if(accNumPartialOK != null && !accNumPartialOK.isEmpty() && accNumPartialOK.contains(partsAccNum.get(accNum)) ) {
						continue;
					}
						
					List<String> studyUIDs = getStudyFromAccessionNumber(partsAccNum.get(accNum), patientID, idIssuer, prm);
					for (String study : studyUIDs) {
						studyUIDsForDicomViewGenerator.add(study);
					}
					
					callGetPatientInfo(patientID, studyUID, idIssuer, partsAccNum.get(accNum), prm);
					if (patientInfo == null) {
						log.info("Trying to fetch from remote source...");
						for (int j = 1; j <= Integer.parseInt(getNumeRetry()); j++) {
							String urlDcmDir = getDcmDirUrl(patientID, studyUID, partsAccNum.get(accNum));
							try {
								URL urlConn1 = new URL(urlDcmDir);
								URLConnection conn1 = urlConn1.openConnection();
								((HttpURLConnection) conn1).setRequestMethod("POST");
								conn1.setDoOutput(true);
								code = ((HttpURLConnection) conn1).getResponseCode();
								messCode = ((HttpURLConnection) conn1).getResponseMessage();
								log.info("Code for remote: " + code + ", message: " + messCode);
							} catch (Exception e) {
								log.error("Chiamata servlet /wado?requestType=DcmDir fallita:" + e.getMessage(), e);
							} finally {
								if (code != 200) {
									log.info("Fetching remote going to sleep... " + j);
									StreamUtils.waitForRetry(Integer.parseInt(getSleep()));
									if (j == Integer.parseInt(getNumeRetry())) {
										log.info("Nessun risultato, wado?requestType=DcmDir, count: " + j);
									} else {
										log.info("Riprovare la chiamata al servizio wado => requestType=DcmDir, count: " + j);
									}
								}
							}
						}
					}
					
					prm.updateTo3pdiJob(getJobId(), ResponseCode.CC_SUC_200, "Accepted", "Procedo con creazione contenuto PDI", "WORKING", "", buildZipName(requestID, azienda), requestID, false);
					
					
						
					File theDir = StreamUtils.makeDir(getWorkarea() + buildZipName(requestID, azienda), true);
					if (!createDicomDir(studyUIDsForDicomViewGenerator, theDir.getAbsolutePath(), prm, response)) {
						sendError(response, ResponseCode.CC_ERR_207, ResponseMessage.CC_ERR_207, "Stavo creando indice DICOMDIR", requestID, azienda, prm);
						return;
					}
					
					
					for (String study : studyUIDs) {
						log.info("Study for dicom thread: " + study);
						int esitoGenerazioneDICOM = runDicomThread(prm, study, patientID, partsAccNum.get(accNum), idIssuer, theDir, accNumPartialOK);
						if(esitoGenerazioneDICOM > 0) {
							String responseCode = null;
							String messageCode = null;
							switch (esitoGenerazioneDICOM) {
							case 1:
								responseCode	= ResponseCode.CC_ERR_211;
								messageCode		= ResponseMessage.CC_ERR_211;
								break;
							case 2:
								responseCode	= ResponseCode.CC_ERR_222;
								messageCode		= ResponseMessage.CC_ERR_222;
								break;
							case 3:
								responseCode	= ResponseCode.CC_ERR_223;
								messageCode		= ResponseMessage.CC_ERR_223;
								break;
							default:
								responseCode	= ResponseCode.CC_ERR_211;
								messageCode		= ResponseMessage.CC_ERR_211;
								break;
							}
							sendError(response, responseCode, messageCode, "Stavo creando contenuto PDI", requestID, azienda, prm);
							return;
						}
						
					
					}
					 Boolean check = new DicomViewGenerator(getWorkarea() + buildZipName(requestID, azienda), studyUIDsForDicomViewGenerator, patientInfo, css, js, buildBasicAuth(),	prm, xslt, getWadoUrl(), getNumeRetry()).generateDicomView();
					 if(!check) {
						 log.info("Don,t generate viewer 1");
						 String fileName = buildZipName(requestID, azienda);
						 String workarea = getWorkarea();
						 String from = workarea + buildZipName(requestID, azienda) +".zip";
						 StreamUtils.cleanLocalFile(from);
						 StreamUtils.cleanLocalDirectory(new File(workarea + fileName));
						 sendError(response, ResponseCode.CC_ERR_211, ResponseMessage.CC_ERR_211, "Stavo eseguendo la generazione del pacchetto", requestID, azienda, prm);
						 return;
					 }
				}
				
				if (!verifyResult(controlId, getWorkarea(), getJobId(), getDist(), code, messCode, requestID, "SUCCESS", prm, azienda, response)) {
					return;
				}
				break;
			case 2:
				log.info("Caso 2");
				log.info("Study length: " + partsStudy.size());
				List<String> accNums = new ArrayList<String>();
				if(StringUtils.isNotEmpty(accessionNumber)) {
					accNums.add(accessionNumber);
				}

				if (!isSamePatientByAccNumsAndStudies(accNums,partsStudy, patientID, prm, accNumPartialOK)) {
					sendError(response, ResponseCode.CC_ERR_205, ResponseMessage.CC_ERR_205, "Stavo verificando che il paziente fosse lo stesso", requestID, azienda, prm);
					return;
				}
				
				for (int study = 0; study < partsStudy.size(); study++) {
					log.info("Working for study: " + partsStudy.get(study));
					controlId.put(partsStudy.get(study), Boolean.TRUE);	
					accessionNumber = getAccessionNumberFromStudy(partsStudy.get(study), prm);
					callGetPatientInfo(patientID, partsStudy.get(study), idIssuer, accessionNumber, prm);
					if (patientInfo == null) {
						log.info("Trying to fetch from remote source...");
						for (int j = 1; j <= Integer.parseInt(getNumeRetry()); j++) {
							String urlDcmDir = getDcmDirUrl(patientID, partsStudy.get(study), accessionNumber);
							try {
								URL urlConn1 = new URL(urlDcmDir);
								URLConnection conn1 = urlConn1.openConnection();
								((HttpURLConnection) conn1).setRequestMethod("POST");
								conn1.setDoOutput(true);
								code = ((HttpURLConnection) conn1).getResponseCode();
								messCode = ((HttpURLConnection) conn1).getResponseMessage();
								log.info("Code for remote fetching: " + code + ", message: " + messCode);
							} catch (Exception e) {
								log.error("Chiamata servlet /wado?requestType=DcmDir fallita: " + e.getMessage(), e);
							} finally {
								if (code != 200) {
									log.info("Fetching remote going to sleep... " + j);
									StreamUtils.waitForRetry(Integer.parseInt(getSleep()));
									if (j == Integer.parseInt(getNumeRetry())) {
										controlId.replace(partsStudy.get(study), Boolean.FALSE);
										log.info("Nessun risultato, wado?requestType=DcmDir, count: " + j);
									} else {
										log.info("Riprovare la chiamata al servizio wado => requestType=DcmDir, count: " + j);
									}
								}
							}
						}
					}
					
					prm.updateTo3pdiJob(getJobId(), ResponseCode.CC_SUC_200, "Accepted", "Procedo con creazione contenuto PDI", "WORKING", "", buildZipName(requestID, azienda), requestID, false);
					
					if(accNumPartialOK != null && accNumPartialOK.size() > 0 && accNumPartialOK.size()==accNums.size()) {
						//sendError(response, ResponseCode.CC_ERR_206, ResponseMessage.CC_ERR_206, "Gli Accession Number Forniti sono tutti non presenti e/o privi di immagine", requestID, azienda, prm);
						sendError(response, ResponseCode.CC_ERR_218, ResponseMessage.CC_ERR_218, "Gli Accession Number sono tutti non presenti/privi di immagine", requestID, azienda, prm);
						return;
					}
					
					File theDir = StreamUtils.makeDir(getWorkarea() + buildZipName(requestID, azienda), true);
					if (!createDicomDir(partsStudy, theDir.getAbsolutePath(), prm, response)) {
						sendError(response, ResponseCode.CC_ERR_207, ResponseMessage.CC_ERR_207, "Stavo creando indice DICOMDIR", requestID, azienda, prm);
						return;
					}
					
					int esitoGenerazioneDICOM = runDicomThread(prm, partsStudy.get(study), patientID, accessionNumber, idIssuer, theDir, accNumPartialOK);
					if(esitoGenerazioneDICOM > 0) {
						String responseCode = null;
						String messageCode = null;
						switch (esitoGenerazioneDICOM) {
						case 1:
							responseCode	= ResponseCode.CC_ERR_211;
							messageCode		= ResponseMessage.CC_ERR_211;
							break;
						case 2:
							responseCode	= ResponseCode.CC_ERR_222;
							messageCode		= ResponseMessage.CC_ERR_222;
							break;
						case 3:
							responseCode	= ResponseCode.CC_ERR_223;
							messageCode		= ResponseMessage.CC_ERR_223;
							break;
						default:
							responseCode	= ResponseCode.CC_ERR_211;
							messageCode		= ResponseMessage.CC_ERR_211;
							break;
						}
						sendError(response, responseCode, messageCode, "Stavo creando contenuto PDI", requestID, azienda, prm);
						return;
					}
					
					
				}
				
				Boolean check = new DicomViewGenerator(getWorkarea() + buildZipName(requestID, azienda), partsStudy, patientInfo, css, js, buildBasicAuth(), prm, xslt, getWadoUrl(), getNumeRetry()).generateDicomView();
				
				if(!check) {
					log.info("Don,t generate viewer 2");
					String fileName = buildZipName(requestID, azienda);
					String workarea = getWorkarea();
					String from = workarea + buildZipName(requestID, azienda) +".zip";
					StreamUtils.cleanLocalFile(from);
					StreamUtils.cleanLocalDirectory(new File(workarea + fileName));
					sendError(response, ResponseCode.CC_ERR_211, ResponseMessage.CC_ERR_211, "Stavo eseguendo la generazione del pacchetto", requestID, azienda, prm);
					return;
				}
				
				if (!verifyResult(controlId, getWorkarea(), getJobId(), getDist(), code, messCode, requestID, "SUCCESS", prm, azienda, response)) {
					return;
				}
				break;
			default:
				log.info("Default");
				List<String> accsNum = new ArrayList<String>();
				accsNum.add(accessionNumber);
				
				studiesList = new ArrayList<String>();
				studiesList.add(studyUID);
				if (!isSamePatientByAccNumsAndStudies(accsNum,studiesList, patientID, prm,  accNumPartialOK)) {
					sendError(response, ResponseCode.CC_ERR_205, ResponseMessage.CC_ERR_205, "Stavo verificando che il paziente fosse lo stesso", requestID, azienda, prm);
					return;
				}
				
				List<String> studies = buildStudies(accessionNumber, studyUID, patientID,idIssuer, prm);
				callGetPatientInfo(patientID, studyUID, idIssuer, accessionNumber, prm);
				if (patientInfo == null) {
					log.info("Trying to fetch from remote source...");
					for (int j = 1; j <= Integer.parseInt(getNumeRetry()); j++) {
						String dcmDirUrl = getDcmDirUrl(patientID, studyUID, accessionNumber);
						try {
							URL urlConn1 = new URL(dcmDirUrl);
							URLConnection conn1 = urlConn1.openConnection();
							((HttpURLConnection) conn1).setRequestMethod("POST");
							conn1.setDoOutput(true);
							code = ((HttpURLConnection) conn1).getResponseCode();
							messCode = ((HttpURLConnection) conn1).getResponseMessage();
							log.info("Code for remote: " + code + ", message: " + messCode);
						} catch (Exception e) {
							log.error("Chiamata servlet /wado?requestType=DcmDir fallita: " + e.getMessage(), e);
						} finally {
							if (code != 200) {
								log.info("Fetching remote going to sleep... " + j);
								StreamUtils.waitForRetry(Integer.parseInt(getSleep()));
								if (j == Integer.parseInt(getNumeRetry())) {
									log.info("Nessun risultato, wado?requestType=DcmDir, count: " + j);
								} else {
									log.info("Riprovare la chiamata al servizio wado => requestType=DcmDir, count: " + j);
								}
							}
						}
					}
				}
				
				prm.updateTo3pdiJob(getJobId(), ResponseCode.CC_SUC_200, "Accepted", "Procedo con creazione contenuto PDI", "WORKING", "", buildZipName(requestID, azienda), requestID, false);
				
				if(accNumPartialOK != null && accNumPartialOK.size() > 0 && accNumPartialOK.size()==accsNum.size()) {
					//sendError(response, ResponseCode.CC_ERR_206, ResponseMessage.CC_ERR_206, "Gli Accession Number sono tutti non presenti/privi di immagine", requestID, azienda, prm);
					sendError(response, ResponseCode.CC_ERR_218, ResponseMessage.CC_ERR_218, "Gli Accession Number sono tutti non presenti/privi di immagine", requestID, azienda, prm);
					return;
				}
				
				File theDir = StreamUtils.makeDir(getWorkarea() + buildZipName(requestID, azienda), true);
				if (!createDicomDir(studies, theDir.getAbsolutePath(), prm, response)) {
					sendError(response, ResponseCode.CC_ERR_207, ResponseMessage.CC_ERR_207, "Stavo creando indice DICOMDIR", requestID, azienda, prm);
					return;
				}
				
				for (String study : studies) {
					
					int esitoGenerazioneDICOM = runDicomThread(prm, study, patientID, accessionNumber, idIssuer, theDir, accNumPartialOK);
					if(esitoGenerazioneDICOM > 0) {
						String responseCode = null;
						String messageCode = null;
						switch (esitoGenerazioneDICOM) {
						case 1:
							responseCode	= ResponseCode.CC_ERR_211;
							messageCode		= ResponseMessage.CC_ERR_211;
							break;
						case 2:
							responseCode	= ResponseCode.CC_ERR_222;
							messageCode		= ResponseMessage.CC_ERR_222;
							break;
						case 3:
							responseCode	= ResponseCode.CC_ERR_223;
							messageCode		= ResponseMessage.CC_ERR_223;
							break;
						default:
							responseCode	= ResponseCode.CC_ERR_211;
							messageCode		= ResponseMessage.CC_ERR_211;
							break;
						}
						sendError(response, responseCode, messageCode, "Stavo creando contenuto PDI", requestID, azienda, prm);
						return;
					}
				}
				check = new DicomViewGenerator(getWorkarea() + buildZipName(requestID, azienda), studies, patientInfo, css, js, buildBasicAuth(),	prm, xslt, getWadoUrl(), getNumeRetry()).generateDicomView();
				
				if(!check) {
					log.info("Don,t generate viewer default");
					String fileName = buildZipName(requestID, azienda);
					String workarea = getWorkarea();
					String from = workarea + buildZipName(requestID, azienda) +".zip";
					StreamUtils.cleanLocalFile(from);
					StreamUtils.cleanLocalDirectory(new File(workarea + fileName));
					sendError(response, ResponseCode.CC_ERR_211, ResponseMessage.CC_ERR_211, "Stavo eseguendo la generazione del pacchetto", requestID, azienda, prm);
					return;
				}
				boolean viewerAdded = CommandUtils.execCommandViewer(getViewerFolder(), getWorkarea() + buildZipName(requestID, azienda), 
						prm.getConfigParamPDI(ConfigurationSettings.SCP_VIEWER_TEMPLATE), getNumeRetry());
				if (!viewerAdded) {
					sendError(response, ResponseCode.CC_ERR_214, ResponseMessage.CC_ERR_214, "Stavo aggiungendo il viewer", requestID, azienda, prm);
					return;
				}
				
				ZipUtils.zipDirectory(getWorkarea() + buildZipName(requestID, azienda), getWorkarea(), buildZipName(requestID, azienda));
				digest = DigestUtils.calcolaDigest(new File(getWorkarea() + buildZipName(requestID, azienda) + ".zip"), DigestUtils.SHA1);
				boolean hasCommandMoveSucceeded = CommandUtils.execCommandMove(getWorkarea(), buildZipName(requestID, azienda), getDist(), getCommandMove(), getNumeRetry());
				if (hasCommandMoveSucceeded) {
					sendCompleteNotice(getJobId(), response, requestID, "SUCCESS", getDist(), azienda, digest, null, null, prm);
				} else {
					sendError(response, ResponseCode.CC_ERR_213, ResponseMessage.CC_ERR_213, "Stavo eseguendo CommandMove", requestID, azienda, prm);
				}
				break;
			} // close switch
	
			prm.updateTo3pdiJob(getJobId(), ResponseCode.CC_SUC_200, ResponseMessage.CC_SUC_200, ResponseMessage.CC_SUC_200, "SUCCESS", digest, buildZipName(requestID, azienda), requestID, true);
			log.info("Operazione completata con successo");
		} catch (Exception e) {
			log.error("Cannot perform operation due to: " + e.getMessage(), e);
			sendError(response, ResponseCode.CC_ERR_500, ResponseMessage.CC_ERR_500, "Stavo eseguendo job", requestID, azienda, prm);
		} finally {
			String numCurrentThreadStr = prm.getConfigParamPDI(ConfigurationSettings.NUM_CURRENT_THREAD);
			Integer numCurrentThread 	= StringUtils.isNotEmpty(numCurrentThreadStr) ? Integer.valueOf(numCurrentThreadStr) : 0;
			numCurrentThread = numCurrentThread-1;
			prm.updateTo3pdiConf(ConfigurationSettings.NUM_CURRENT_THREAD, String.valueOf(numCurrentThread));
		}
		
	}
	
	/**
	 * Ottiene i parametri di configurazione
	 */
	private void buildConfigParams(PdiRetrieveManager prm) {
		log.info("Setting config params...");
		setDpacsPatientInfo(prm.getConfigParamPDI(ConfigurationSettings.PATIENTINFO));
		setDpacsWadoUrl(prm.getConfigParamPDI(ConfigurationSettings.WADOURL));
		setCompleteNoticeUrl(prm.getConfigParamPDI(ConfigurationSettings.COMPLETE_NOTICE_URL));
		setUsernameFse(prm.getConfigParamPDI(ConfigurationSettings.USERNAME_FSE));
		setPasswordFse(prm.getConfigParamPDI(ConfigurationSettings.PASSWORD_FSE));
		setSleepNotifica(prm.getConfigParamPDI(ConfigurationSettings.SLEEP_NOTIFICA));
		setNumRetryNotifica(prm.getConfigParamPDI(ConfigurationSettings.NUM_RETRY_NOTIFICA));
		setPatientInfo(null);
		setWorkarea(prm.getConfigParamPDI(ConfigurationSettings.WORKAREA));
		setStudyDate(null);
		setSeriesUIDs(null);
		setDigest("");
		log.info("Config params set");
	}

	/**
	 * Controlla il risultato della gestione del contenuto PDI
	 * in caso di multipli accession number o studi specificati
	 * @param results risultato dell'operazione
	 * @param workarea area di lavoro dove creare il contenuto PDI
	 * @param job identificativo del job
	 * @param dist destinazione per gestire il contenuto PDI
	 * @param code codice in caso di risultato negativo
	 * @param msg messaggio in caso di risultato negativo
	 */
	private boolean verifyResult(Map<String, Boolean> results, String workarea, 
			String job, String dist, int code, String msg, String requestID, String status, 
			PdiRetrieveManager prm, String azienda, HttpServletResponse response) {
		if (results.containsValue(Boolean.TRUE)) {
			boolean viewerAdded = CommandUtils.execCommandViewer(getViewerFolder(), getWorkarea() + buildZipName(requestID, azienda), 
					prm.getConfigParamPDI(ConfigurationSettings.SCP_VIEWER_TEMPLATE), getNumeRetry());
			if (!viewerAdded) {
				sendError(response, ResponseCode.CC_ERR_214, ResponseMessage.CC_ERR_214, "Stavo aggiungendo il viewer", requestID, azienda, prm);
				return false;
			}
			
			ZipUtils.zipDirectory(workarea + buildZipName(requestID, azienda), workarea, buildZipName(requestID, azienda));
			digest = DigestUtils.calcolaDigest(new File(getWorkarea() + buildZipName(requestID, azienda) + ".zip"), DigestUtils.SHA1);
			boolean hasCommandMoveSucceeded = CommandUtils.execCommandMove(workarea, buildZipName(requestID, azienda), getDist(), getCommandMove(), getNumeRetry());
			if (hasCommandMoveSucceeded) {
				sendCompleteNotice(getJobId(), response, requestID, "SUCCESS", getDist(), azienda, digest, null, null, prm);
				
				return true;
			} else {
				sendError(response, ResponseCode.CC_ERR_213, ResponseMessage.CC_ERR_213, "Stavo eseguendo CommandMove", requestID, azienda, prm);
				
				return false;
			}			
		} else {
			sendCompleteNotice(job, response, requestID, "ERROR", "", azienda, "",  String.valueOf(code), msg, prm);
			
			return false;
		}
	}
	
	/**
	 * Recupera l'ASL desiderata dalla lista di ASL
	 * @param azienda il nome dell'azienda desiderata
	 * @return true se asl presente, false altrimenti
	 */
	private boolean findAsl(String azienda) {
		boolean isAslPresent = false;
		
		log.info("ASL list has " + listAsl.size() + " asl");
		for (Asl asl : listAsl) {
			if (asl.getNome().equals(azienda)) {
				isAslPresent = true;
				
				setWadoUrl(asl.getEndpoint());
				setNumeRetry(asl.getNumeRetry());
				setDist(asl.getDist());
				setXslt(asl.getXslt());
				setCss(asl.getCss());
				setJs(asl.getJs());
				setCommandMove(asl.getCommandMove());
				setViewerFolder(asl.getViewerFolder());
				setSleep(asl.getSleep());
				setSleepMove(asl.getSleepMove());
				log.info(asl.toString());
			}
		}
		
		if (!isAslPresent) {
			log.info("ASL not present");
		}
		
		return isAslPresent;
	}

	/**
	 * Invia notifica di fine operazione alle ASL
	 * @param jobId l'identificativo del job
	 * @param dist directory dello zip risultante
	 * @param status risultato dell'operazione
	 * @param requestID id della richiesta
	 */
	private void sendCompleteNotice(String jobId, HttpServletResponse response,
			String requestID, String status, String dist, String azienda, String digest, String codErrore, String msgErr, PdiRetrieveManager prm) {
		log.info("Sending notice for job: " + jobId + "...");		
		int code = 0;
		String msg = "";
		String noticeUrl = getSendCompleteNoticeUrl(jobId, requestID, status, dist, azienda, digest, codErrore, msgErr);
		int retries = Integer.parseInt(getNumRetryNotifica());
		for (int retry = 1; retry <= retries; retry++) {
			try {
				URL url = new URL(noticeUrl);
				URLConnection connection = url.openConnection();
				((HttpURLConnection) connection).setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setRequestProperty("Authorization", "Basic " + buildSendCompleteNoticeBasicAuth());
				code = ((HttpURLConnection) connection).getResponseCode();
				msg = ((HttpURLConnection) connection).getResponseMessage();
				log.info("Code for sendCompleteNotice: " + code + ", message: " + msg);
				if (code != 200) {
					log.info("Failed to sent notice with status: " + code + " due to: " + msg);
				} else {
					log.info("Notice sent successfully");
					return;
				}
			} catch (Exception e) {
				log.info("Error while sending complete notice due to: " + e.getMessage());
				e.printStackTrace();
			} finally {
				if (code != 200) {
					log.info("Retrying sendCompleteNotice... " + retry);
					StreamUtils.waitForRetry(Integer.parseInt(getSleepNotifica()));
					if (retry == retries) {
						log.info("Cannot send complete notice");
						prm.updateTo3pdiJob(getJobId(), ResponseCode.CC_ERR_215, ResponseMessage.CC_ERR_215, "Stavo inviando notidica di completamento", "FAILED", "", buildZipName(requestID, azienda), requestID, true);
						return;
					} else {
						log.info("Riprovare la chiamata al servizio wado => requestType=CompleteNotice, count: " + retry);
					}
				}
			}
		}
	}

	/**
	 * Fornisce l'URL del servizio di sendCompleteNotice
	 * @param jobId l'identificativo del job complemtato
	 * @param dist directory dello zip risultante
	 * @param status risultato dell'operazione
	 * @param requestID id della richiesta
	 * @return l'URL del servizio sendCompleteNotice
	 */
	private String getSendCompleteNoticeUrl(String jobId, String requestID, String status, String dist, String azienda, String digest, String codError, String msgError) {
		StringBuilder noticeUrlBuilder = new StringBuilder(getCompleteNoticeUrl())
			.append("?zipName=")
			.append(buildZipName(requestID, azienda))
			.append(".zip")
			.append("&jobUID=")
			.append(jobId)
			.append("&status=")
			.append(status);
		
		if (requestID != null && !requestID.isEmpty()) {
			noticeUrlBuilder.append("&requestID=").append(requestID);
		}
		
		if (digest != null && !digest.isEmpty()) {
			noticeUrlBuilder.append("&checksum=").append(digest);
		}
		
		if (dist != null && !dist.isEmpty()) {
			dist = dist.replaceAll("/", "2F");
			noticeUrlBuilder.append("&dist=").append(dist);
		}
		
		if (codError != null && !codError.isEmpty()) {
			
			noticeUrlBuilder.append("&codeError=").append(codError);
		}
		
		String noticeUrl = noticeUrlBuilder.toString();
		log.info("Url di chiamata: " + noticeUrl);
		
		return noticeUrl;
	}
	
	/**
	 * Fornisce le credenziali di autenticazione per le chiamate ai servizi.
	 */
	private String buildBasicAuth() {
		return Base64.getEncoder().encodeToString((username + ":" + password)
				.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Costruisce le credenziali per il servizio sendCompleteNotice.
	 * @return le credenziali per il servizio sendCompleteNotice.
	 */
	private String buildSendCompleteNoticeBasicAuth() {
		return Base64.getEncoder().encodeToString((usernameFse + ":" + passwordFse)
				.getBytes(StandardCharsets.UTF_8));
	}
	

	/**
	 * Costruisce un paziente sulla della risposta fornita dal servizio /getPatientInfo.
	 * @param line la risposta del servizio.
	 * @param studyUID lo studio di riferimento.
	 */
	private void buildPatient(String line, String studyUID) {
		patientInfo = new Patient();
		seriesUIDs = new ArrayList<String>();

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

				patientInfo.setPatientId(patient.getAttribute("patientID") != null ? patient.getAttribute("patientID") : null);
				patientInfo.setLastName(patient.getAttribute("lastName") != null ? patient.getAttribute("lastName") : null);
				patientInfo.setMiddleName(patient.getAttribute("middleName") != null ? patient.getAttribute("middleName") : null);
				patientInfo.setFirstName(patient.getAttribute("firstName") != null ? patient.getAttribute("firstName") : null);
				patientInfo.setBirth(patient.getAttribute("birthDate") != null ? patient.getAttribute("birthDate") : null);
				
				NodeList studyList = patient.getElementsByTagName("study");
				log.info("studyList recuperato");
				if(studyList != null && studyList.getLength() > 0) {
					if (getStudyDate() == null ) {
						Element fistStudy = (Element) studyList.item(0);
						setStudyDate(fistStudy.getAttribute("date"));
						log.info("studyDate ---> " + getStudyDate());
					}
					
					for (int studyIndex = 0; studyIndex < studyList.getLength(); studyIndex++) {
						Element study = (Element) studyList.item(studyIndex);
	
						String studyUIDFromNode = study.getAttribute("uid");
						log.info("Study uid " + studyUIDFromNode);
						if (studyUIDFromNode.equals(studyUID)) {
							NodeList seriesList = study.getElementsByTagName("series");
							for (int seriesIndex = 0; seriesIndex < seriesList.getLength(); seriesIndex++) {
								Element series = (Element) seriesList.item(seriesIndex);
								seriesUIDs.add(series.getAttribute("uid"));
							}
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
			log.error("Impossibile analizzare il risultato...", e);
			return;
		} catch (SAXException e) {
			log.error("Impossibile analizzare il file di configurazione! ", e);
			return;
		} catch (Exception oex) {
			log.error("Impossibile recuperare le informazione...", oex);
			return;
		}
		
		log.info("PatientID: " + patientInfo.getPatientId());
		log.info("FirstName: " + patientInfo.getFirstName());
		log.info("LastName: " + patientInfo.getLastName());
		log.info("Birth: " + patientInfo.getBirth());
		
		return;
	}
	
	/**
	 * Fornisce l'id del paziente a partire dalle info del paziente sulla base dello studio
	 * @param studyUID l'id dello studio per l'accession number
	 * @return l'id del paziente
	 */
	private String getAccessionNumberFromPatient(String line, String studyUID) {
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
				Node nNode = nLst.item(i);
				log.info("Current Element: " + nNode.getNodeName());
				Element eElement = (Element) nNode;

				NodeList studyList = eElement.getElementsByTagName("study");
				for (int studyIndex = 0; studyIndex < studyList.getLength(); studyIndex++) {
					Element study = (Element) studyList.item(studyIndex);

					String studyUIDFromNode = study.getAttribute("uid");
					log.info("Study uid " + studyUIDFromNode);
					if (studyUIDFromNode.equals(studyUID)) {
						String obtainedAccNum = study.getAttribute("accessionNumber");
						log.info("obtainedAccNum ---> " + obtainedAccNum);
						return obtainedAccNum;
					}
				}
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
	 * Fornisce l'id del paziente a partire dalle info del paziente sulla base dell'accession number
	 * @param l'accession number relativo allo studio
	 * @return l'id del paziente
	 */
	private List<String> getStudyFromPatient(String line, String accessionNumber) {
		List<String> studies = new ArrayList<String>();
		log.info("getStudyFromPatient");
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
				Node nNode = nLst.item(i);
				log.info("Current Element: " + nNode.getNodeName());
				Element eElement = (Element) nNode;

				NodeList studyList = eElement.getElementsByTagName("study");
				for (int studyIndex = 0; studyIndex < studyList.getLength(); studyIndex++) {
					Element study = (Element) studyList.item(studyIndex);

					String accNumFromNode = study.getAttribute("accessionNumber");
					log.info("Accession number: " + accNumFromNode);
					if (accNumFromNode.equals(accessionNumber)) {
						String obtainedStudy = study.getAttribute("uid");
						log.info("obtainedStudy ---> " + obtainedStudy);
						studies.add(obtainedStudy);
					}
				}
			}
		} catch (ParserConfigurationException e) {
			log.error("Impossibile analizzare il risultato: ", e);
			return null;
		} catch (SAXException e) {
			log.error("Impossibile analizzare il file di configurazione: ", e);
			return null;
		} catch (Exception oex) {
			log.error("Impossibile caricare il file di configurazione: ", oex);
			return null;
		}
		
		return studies;
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
     * Permette di recuperare gli identificativi delle DICOM in una serie
     * @return lista di identificativi recuperati
     */
	private List<String> getDicomsBySeries(String line) {
		List<String> UIDs = new ArrayList<String>();
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			StringReader reader = new StringReader(line);
			InputSource is = new InputSource(reader);
			Document doc = docBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nLst = doc.getElementsByTagName("patient");
			Node nNode = nLst.item(0); 
			log.info("Current Element: " + nNode.getNodeName());
			Element eElement = (Element) nNode;
			nNode = eElement.getElementsByTagName("study").item(0);
			log.info("Current Element: " + nNode.getNodeName());
			nLst = doc.getElementsByTagName("series");
			NodeList instanceList = doc.getElementsByTagName("instance");
			for (int instanceIndex = 0; instanceIndex < instanceList.getLength(); instanceIndex++) {
				log.info("Current Element: " + instanceList.item(instanceIndex).getNodeName());
				eElement = (Element) instanceList.item(instanceIndex);
				
				UIDs.add(eElement.getAttribute("uid"));
			}
		} catch (ParserConfigurationException e) {
			log.error("Impossibile analizzare il risultato: ", e);
			return null;
		} catch (SAXException e) {
			log.error("Impossibile analizzare il file di configurazione: ", e);
			return null;
		} catch (Exception oex) {
			log.error("Impossibile caricare il file di configurazione: ", oex);
			return null;
		}
		
		return UIDs;
	}
	
    /**
     * Metodo utile al caricamento della configurazione
     * @param azienda ASL specificata nella richiesta
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
					asl.setJs(eElement.getElementsByTagName("js").item(0) != null? eElement.getElementsByTagName("js").item(0).getTextContent(): null);
					asl.setCommandMove(eElement.getElementsByTagName("commandMove").item(0) != null? eElement.getElementsByTagName("commandMove").item(0).getTextContent(): null);
					asl.setViewerFolder(eElement.getElementsByTagName("viewer").item(0) != null? eElement.getElementsByTagName("viewer").item(0).getTextContent(): null);
					asl.setSleep(eElement.getElementsByTagName("time_lapse").item(0) != null? eElement.getElementsByTagName("time_lapse").item(0).getTextContent(): null);
					asl.setSleepMove(eElement.getElementsByTagName("time_lapse_move").item(0) != null? eElement.getElementsByTagName("time_lapse_move").item(0).getTextContent(): null);
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
		
		buildConfigParams(prm);
		if(!findAsl(azienda)) {
			log.info("Cannot load ASL configuration");
			return false;
		}
		
		return true;
	}

    /**
     * Controlla che il paziente sia il medesismo per tutti gli studi.
     * @param accNumsOrStudies gli identificativi degli studi oppure gli accession number degli studi.
     * @param patientID l'identificativo del paziente.
     * @param isStudy se si sta effettuando la verifica per studio o meno.
     * @return true se il paziente e' il medisimo, false altrimenti.
     */
    private boolean isSamePatientByAccNumsOrStudies(List<String> accNumsOrStudies, 
    		String patientID, PdiRetrieveManager prm, boolean isStudy) {
    	List<String> patients = new ArrayList<String>();
    	String patient = "";
    	for (String accNumOrStudy : accNumsOrStudies) {
    		if (isStudy) {
    			log.info("Checking by study...");
    			patient = getPatientFromStudy(accNumOrStudy, prm);
    		} else {
    			log.info("Checking by accession number...");
    			patient = getPatientFromAccessionNumber(accNumOrStudy, prm);
    		}
    		patients.add(patient);
    	}
    	log.info("Patients for pdi: " + patients);
    	
    	return isSamePatient(patients, patientID);
    }
    
    /**
     * Controlla che il paziente sia il medesismo per tutti gli studi.
     * @param accNums gli identificativi degli accession number degli studi.
     * @param accStudies gli identificativi degli studi.
     * @param patientID l'identificativo del paziente.
     * @param isStudy se si sta effettuando la verifica per studio o meno.
     * @return true se il paziente e' il medisimo, false altrimenti.
     */
    private boolean isSamePatientByAccNumsAndStudies(List<String> accNums, List<String> studies,	String patientID, PdiRetrieveManager prm, List<String> accNumPartialOK) {
    	List<String> patients = new ArrayList<String>();
    	String patient = "";
    	for (String study : studies) {
    		log.info("Checking by study...");
    		if(StringUtils.isNotEmpty(study)) {
    			log.info("..." + study);
    			patient = getPatient(study,null,patient, prm);
    			patients.add(patient);
    		}
    	}
    	for (String accessionNumber : accNums) {
    		log.info("Checking by accession number...");
    		
    		if(accNumPartialOK != null && !accNumPartialOK.isEmpty() && accNumPartialOK.contains(accessionNumber)) {
    			log.info("accNumPartialOK==accessionNumber");
    			if(StringUtils.isNotEmpty(patientID)) {
    				patients.add(patientID);
    			}
    		} else {

	    		log.info("Checking by accession number...");
	    		if(StringUtils.isNotEmpty(accessionNumber)) {
	    			patient = getPatient(null,accessionNumber,patientID, prm);
	    			patients.add(patient);
	    		}
    		}
    	}
    	
    	return isSamePatient(patients, patientID);
    }
    
    /**
     * Controlla che il paziente sia il medesismo per tutti gli 
     * identificativi forniti.
     * @param patients e' la lista degli identificativi dei paziente da controllare.
     * @param patientID l'identificativo del paziente.
     * @return true se il paziente e' il medisimo, false altrimenti.
     */
    private boolean isSamePatient(List<String> patients, String patientID) {
    	log.info("Verifying if patient is the same...");
    	    	
    	if(patients != null && !patients.isEmpty()) {
    		
    		log.info("patients != null && !patients.isEmpty()");
    		
	    	for (String patient : patients) {
	    		log.info("String patient : patients");
	    		for (String compareToPatient : patients) {
	    			log.info("String compareToPatient : patients");
	    			log.info("compareToPatient: " + compareToPatient + "patient: " + patient);
	    			if (patient==null || compareToPatient == null || !patient.equals(compareToPatient)) {
	    				if(patientID != null) {
	    					log.info("Not all the studies are for the same patient " + patientID);
	    				}
		    			return false;
	    			}
	    		}
	    	}
    	}
    	
    	if ((patientID != null && !patientID.isEmpty()) && (patients != null && !patients.isEmpty())) {
    		log.info("patientID != null && (patients != null && !patients.isEmpty())");
    		log.info("patientID: " + patientID);
	    	for (String patient : patients) {
	    		log.info("String patient : patients: " + patient);
	    		if(patient == null || patientID == null) {
	    			return false;
	    		}
	    		boolean patientIsNotTheSame = !patient.equals(patientID);
	    		if (patientIsNotTheSame) {
	    			log.info("Not all the studies are for the same patient " + patientID);
	    			return false;
	    		}
	    	}
    	}
    	
    	log.info("Patient is the same");
    	return true;
    }
	
	/**
	 * Crea l'indice DICOMDIR
	 * @param studies gli studi da inserire nell'indice
	 * @param to la directory dove salvare l'indice
	 * @return true se l'indice viene generato correttamente, false altrimenti
	 */
	private boolean createDicomDir(List<String> studies, String to, 
			PdiRetrieveManager prm, HttpServletResponse response) {
		int code = 0;
		String messCode = "";
		log.info("Calling /DicomDir...");
		int retries = Integer.parseInt(getNumeRetry());
		for (int retry = 1; retry <= retries; retry++) {
			String dicomDirUrl = getDicomDirUrl(studies);
			try {
				URL urlConn = new URL(dicomDirUrl);
				URLConnection connection = urlConn.openConnection();
				((HttpURLConnection) connection).setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setRequestProperty("Authorization", "Basic " + buildBasicAuth());
				code = ((HttpURLConnection) connection).getResponseCode();
				messCode = ((HttpURLConnection) connection).getResponseMessage();
				log.info("Code for DicomDir: " + code + ", message: " + messCode);
				if (code == 200) {
					if (connection != null && connection.getInputStream() != null) {
						FileOutputStream outputStream = 
								new FileOutputStream(to + "/DICOMDIR");
						byte[] buffer = new byte[9 * 1024];
						int bytesRead;
						
						InputStream in = null;
						in = connection.getInputStream();
						
						log.info("DICOMDIR input stream:"+ in != null); 
						
						
						while ((bytesRead = in.read(buffer)) != -1) {
							outputStream.write(buffer, 0, bytesRead);
						}
						if(outputStream != null) {
							outputStream.close();
						}
						if(in != null) {
							in.close();
						}
						
						log.info("DICOMDIR file saved successfully");
						return true;
					}
					
					break;
				}
			} catch (Exception e) {
				log.error("Chiamata servlet /DicomDir fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/DicomDir going to sleep... " + retry);
					StreamUtils.waitForRetry(Integer.parseInt(getSleep()));
					if (retry == retries) {
						log.info("Nessun risultato, DicomDir count: " + retry);
						return false;
					} else {
						log.info("Riprovare la chiamata al servizio DicomDir, count: " + retry);
					}
				}  
			}
		}
		
		return false;
    }
	
	/**
	 * Fornisce URL per l'invocazione del servizio DICOMDIR per la generazione dell'indice DICOMDIR
	 * @param studies gli studi per cui creare l'indice
	 * @return l'url per l'invocazione del servizio DICOMDIR 
	 */
	private String getDicomDirUrl(List<String> studies) {
		StringBuilder studiesByComma = new StringBuilder();
		for (String study : studies) {
			studiesByComma.append(study).append(ConfigurationSettings.SPLITTER);
		}
		
		String url = new StringBuilder(getWadoUrl())
				.append("/o3-dpacs-wado/DicomDir")
				.append("?studies=")
				.append(studiesByComma.toString())
				.toString();
		
		log.info("Url for DICOMDIR is " + url);
		
		return url;
	}
	
	/**
	 * Lancia l thread per la gestione del contenuto PDI
	 * @param studyUID l'identificativo dello studio
	 * @param patientID l'identificativo del paziente
	 * @param accessionNumber l'accession number dello studio
	 * @param idIssuer id dell'issuer
	 * @param theDir directory per il contenuto PDI
	 */
	private int runDicomThread(PdiRetrieveManager prm, String studyUID, 
			String patientID, String accessionNumber, String idIssuer, File theDir, List<String> accNumPartialOK) {			
		log.info("Getting seriesUIDs...runDicomThread");
		callGetPatientInfo(patientID, studyUID, idIssuer, accessionNumber, prm);
		List<String> seriesUIDs = getSeriesUIDs();
		log.info("StudyUID: " + studyUID + ", series: " + seriesUIDs);
		ThreadPdi threadPdi = new ThreadPdi(getJobId(), wadoUrl, studyUID, seriesUIDs, 
				theDir.getAbsolutePath(), buildBasicAuth(), getNumeRetry(), prm, accNumPartialOK);
		Thread thread = new Thread(threadPdi);
		log.info("Starting thread...");
		thread.start();
		try {
			thread.join();
			int valueReturn = threadPdi.getValueReturn();
			log.info("Thread finished: " +valueReturn);
			return threadPdi.getValueReturn();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			return 1;
		}
	}

	/**
	 * Chiama il servizio getInstance
	 * @param seriesUID l'identificativo della serie
	 * @param studyUID l'identificativo dello studio
	 * @return gli identificativi delle DICOM
	 */
	private List<String> callGetInstance(String seriesUID, String studyUID, PdiRetrieveManager prm) {
		List<String> dicoms = new ArrayList<String>();
		String url = getGetInstanceUrl(seriesUID, studyUID);
		log.info("Url for getInstances: " + url);
		
		int code = 0;
		String msg = "";
		int retries = Integer.parseInt(getNumeRetry());
		for (int retry = 1; retry <= retries; retry++) {
			try {
				URL urlConn = new URL(url);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + buildBasicAuth());
				code = ((HttpURLConnection) conn).getResponseCode();
				msg = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for getInstances: " + code + ", message: " + msg);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							dicoms = getDicomsBySeries(line);
						}
					} 
					
					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /getInstances fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/getInstances going to sleep... " + retry);
					StreamUtils.waitForRetry(Integer.parseInt(getSleep()));
					if (retry == retries) {
						log.info("Nessun risultato, getInstances count: " + retry);
						if(code == 500) {
							dicoms.add("-1");
						}
						return dicoms; 
					} else {
						log.info("Riprovare la chiamata al servizio getInstances, count: " + retry);
					}
				}
			}
		}
		
		return dicoms;
	}

	/**
	 * Fornisce l'URL del servizio getInstance.
	 * @param seriesUID l'identificativo della serie.
	 * @param studyUID l'identificativo dello studio.
	 * @return l'URL del servizio getInstance.
	 */
	private String getGetInstanceUrl(String seriesUID, String studyUID) {
		String url = new StringBuilder(getWadoUrl())
			.append("/o3-dpacs-wado/getInstances")
			.append("?seriesUID=")
			.append(seriesUID)
			.append("&studyUID=")
			.append(studyUID)
			.toString();
		
		log.info("Url di chiamata: " + url);
		
		return url;
	}
	
	/**
	 * Fornisce l'URL del servizio patientInfo.
	 * @param patientID l'identificativo del paziente.
	 * @param studyUID l'identificativo dello studio.
	 * @param idIssuer l'id dell'issuer.
	 * @param accessionNumber l'accession number dello studio.
	 * @return l'URL del servizio patientInfo.
	 */
	private String getPatientInfoUrl(String patientID, String studyUID, String idIssuer, String accessionNumber) {
		StringBuilder urlBuilder = new StringBuilder(getWadoUrl())
			.append(getDpacsPatientInfo())
			.append("?");
		
		
		
		StringBuilder paramsBuilder = new StringBuilder();
		if (patientID != null && !patientID.isEmpty()) {
			paramsBuilder.append("patientID=").append(patientID);
		}
		
		if (accessionNumber != null && !accessionNumber.isEmpty()) {
			if(StringUtils.isNotEmpty(paramsBuilder.toString())) {
				paramsBuilder.append("&");
			}
			paramsBuilder.append("accessionNumber=").append(accessionNumber);
		}
		
		if (studyUID != null && !studyUID.isEmpty()) {
			if(StringUtils.isNotEmpty(paramsBuilder.toString())) {
				paramsBuilder.append("&");
			}
			paramsBuilder.append("&studyUID=").append(studyUID);
		}
		
		if (idIssuer != null && !idIssuer.isEmpty()) {
			if(StringUtils.isNotEmpty(paramsBuilder.toString())) {
				paramsBuilder.append("&");
			}
			paramsBuilder.append("&idIssuer=").append(idIssuer);
		}
		
		if(StringUtils.isNotEmpty(paramsBuilder.toString())) {
			paramsBuilder.append("&");
		}
		
		paramsBuilder.append("showOnlyThis=true");
		
		urlBuilder.append(paramsBuilder.toString());
		String url = urlBuilder.toString();
		
		log.info("Url di chiamata: " + url);
		
		return url;
	}
	
	/**
	 * Fornisce l'url per l'ottenimento delle info del paziente sulla base dello studio
	 * @param studyUID lo studio del paziente
	 * @return l'url per l'ottenimento delle info del paziente sulla base dello studio
	 */
	private String getPatientInfoUrlForAccNum(String studyUID) {
		String url = new StringBuilder(getWadoUrl())
			.append(getDpacsPatientInfo())
			.append("?studyUID=")
			.append(studyUID)
			.append("&showOnlyThis=true")
			.toString();
		
		log.info("Url di chiamata: " + url);
		
		return url;
	}
	
	/**
	 * Fornisce l'url per l'ottenimento delle info del paziente sulla base dell'accession number
	 * @param accessionNumber l'accession number dello studio
	 * @return l'url per l'ottenimento delle info del paziente sulla base dell'accession number
	 */
	private String getPatientInfoUrlForStudy(String accessionNumber) {
		String url = new StringBuilder(getWadoUrl())
			.append(getDpacsPatientInfo())
			.append("?accessionNumber=")
			.append(accessionNumber)
			.append("&showOnlyThis=true")
			.toString();
		
		log.info("Url di chiamata: " + url);
		
		return url;
	}
	
	/**
	 * Fornisce l'URL del servizio Wado per DcmDir.
	 * @param patientID l'identificativo del paziente.
	 * @param studyUID l'identificativo dello studio.
	 * @param accessionNumber l'accession number dello studio.
	 * @return l'URL del servizio Wado per DcmDir.
	 */
	private String getDcmDirUrl(String patientID, String studyUID, String accessionNumber) {
		String url = new StringBuilder(getWadoUrl())
			.append(getDpacsWadoUrl())
			.append("?requestType=DcmDir")
			.append("&accessionNumber=")
			.append(accessionNumber)
			.append("&studyUID=")
			.append(studyUID)
			.append("&patientID=")
			.append(patientID)
			.toString();

		log.info("Url di chiamata: " + url);
		
		return url;
	}
	
	/**
	 * Chiama il servizio getPatientInfo
	 * @param patientID l'identificativo del paziente
	 * @param studyUID l'identificativo dello studio
	 * @param idIssuer l'id dell'issuer
	 * @param accessionNumber l'accession number dello studio
	 */
	private Boolean callGetPatientInfo(String patientID, String studyUID, 
			String idIssuer, String accessionNumber, PdiRetrieveManager prm) {		
		int code = 0;
		String messCode = "";
		log.info("Calling /getPatientInfo...");
		int retries = Integer.parseInt(getNumeRetry());
		for (int retry = 1; retry <= retries; retry++) {
			String patientUrl = getPatientInfoUrl(patientID, studyUID, idIssuer, accessionNumber);
			try {
				URL urlConn = new URL(patientUrl);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + buildBasicAuth());
				code = ((HttpURLConnection) conn).getResponseCode();
				messCode = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for getPatient: " + code + ", message: " + messCode);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							buildPatient(line, studyUID);
						}
					} 

					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /getPatientInfo fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/getPatientInfo going to sleep... " + retry);
					StreamUtils.waitForRetry(Integer.parseInt(getSleep()));
					if (retry == retries) {
						log.info("Nessun risultato, patientInfo count: " + retry);
						return false;
					} else {
						log.info("Riprovare la chiamata al servizio patientInfo, count: " + retry);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Metodo che permette il recupero dell'accessionNumber di uno studio
	 * @param studyUID identificativo dello studio
	 * @return accessionNumber dello studio
	 */
	private String getAccessionNumberFromStudy(String studyUID,  PdiRetrieveManager prm) {	
		String accNum = "";
		int code = 0;
		String messCode = "";
		log.info("Calling /getPatientInfo...");
		int retries = Integer.parseInt(getNumeRetry());
		for (int retry = 1; retry <= retries; retry++) {
			String patientUrl = getPatientInfoUrlForAccNum(studyUID);
			try {
				URL urlConn = new URL(patientUrl);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + buildBasicAuth());
				code = ((HttpURLConnection) conn).getResponseCode();
				messCode = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for getPatient: " + code + ", message: " + messCode);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							accNum = getAccessionNumberFromPatient(line, studyUID);
						}
					} 

					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /getPatientInfo fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/getPatientInfo going to sleep... " + retry);
					StreamUtils.waitForRetry(Integer.parseInt(getSleep()));
					if (retry == retries) {
						log.info("Nessun risultato, patientInfo count: " + retry);
						return null;
					} else {
						log.info("Riprovare la chiamata al servizio patientInfo, count: " + retry);
					}
				}
			}
		}
		
		return accNum;
	}
	
	/**
	 * Fornisce l'id del paziente a partire da uno studio
	 * @param studyUID lo studio del paziente
	 * @return l'id del paziente
	 */
	private String getPatientFromStudy(String studyUID, PdiRetrieveManager prm) {	
		String patient = "";
		int code = 0;
		String messCode = "";
		log.info("Calling /getPatientInfo...");
		int retries = Integer.parseInt(getNumeRetry());
		for (int retry = 1; retry <= retries; retry++) {
			String patientUrl = getPatientInfoUrlForAccNum(studyUID);
			try {
				URL urlConn = new URL(patientUrl);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + buildBasicAuth());
				code = ((HttpURLConnection) conn).getResponseCode();
				messCode = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for getPatient: " + code + ", message: " + messCode);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							patient = getPatientIDFromPatient(line);
						}
					} 

					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /getPatientInfo fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/getPatientInfo going to sleep... " + retry);
					StreamUtils.waitForRetry(Integer.parseInt(getSleep()));
					if (retry == retries) {
						log.info("Nessun risultato, patientInfo count: " + retry);
						return null;
					} else {
						log.info("Riprovare la chiamata al servizio patientInfo, count: " + retry);
					}
				}
			}
		}
		
		return patient;
	}
	
	/**
	 * Fornisce l'id del paziente a partire da uno studio
	 * @param studyUID lo studio del paziente
	 * @param accNumber Accession Number del paziente
	 * @param patientID ID del paziente
	 * @return l'id del paziente
	 */
	private String getPatient(String studyUID, String accNumber, String patientID, PdiRetrieveManager prm) {	
		String patient = "";
		int code = 0;
		String messCode = "";
		log.info("Calling /getPatientInfo...");
		int retries = Integer.parseInt(getNumeRetry());
		for (int retry = 1; retry <= retries; retry++) {
			String patientUrl = getPatientInfoUrl(patientID, studyUID, null, accNumber);
			try {
				URL urlConn = new URL(patientUrl);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + buildBasicAuth());
				code = ((HttpURLConnection) conn).getResponseCode();
				messCode = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for getPatient: " + code + ", message: " + messCode);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							patient = getPatientIDFromPatient(line);
						}
					} 

					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /getPatientInfo fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/getPatientInfo going to sleep... " + retry);
					StreamUtils.waitForRetry(Integer.parseInt(getSleep()));
					if (retry == retries) {
						log.info("Nessun risultato, patientInfo count: " + retry);
						return null;
					} else {
						log.info("Riprovare la chiamata al servizio patientInfo, count: " + retry);
					}
				}
			}
		}
		
		return patient;
	}

	/**
	 * Fornisce l'id del paziente a partire dall'accession number
	 * @param accessionNumber l'accession number dello studio del paziente
	 * @return l'id del paziente
	 */
	private String getPatientFromAccessionNumber(String accessionNumber, PdiRetrieveManager prm) {	
		String patient = "";
		int code = 0;
		String messCode = "";
		log.info("Calling /getPatientInfo...");
		int retries = Integer.parseInt(getNumeRetry());
		for (int retry = 1; retry <= retries; retry++) {
			String patientUrl = getPatientInfoUrlForStudy(accessionNumber);
			try {
				URL urlConn = new URL(patientUrl);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + buildBasicAuth());
				code = ((HttpURLConnection) conn).getResponseCode();
				messCode = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for getPatient: " + code + ", message: " + messCode);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							patient = getPatientIDFromPatient(line);
						}
					} 

					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /getPatientInfo fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/getPatientInfo going to sleep... " + retry);
					StreamUtils.waitForRetry(Integer.parseInt(getSleep()));
					if (retry == retries) {
						log.info("Nessun risultato, patientInfo count: " + retry);
						return null;
					} else {
						log.info("Riprovare la chiamata al servizio patientInfo, count: " + retry);
					}
				}
			}
		}
		
		return patient;
	}

	/**
	 * Metodo che permette il recupero dell'identificativo dello studio tramite l'accessionNumber
	 * @param accessionNumber accessionNumber dello studio
	 * @return identificativo dello studio
	 */
	private List<String> getStudyFromAccessionNumber(String accessionNumber, String patiendID, String idIssuer, PdiRetrieveManager prm) {	
		List<String> studies = new ArrayList<String>();
		int code = 0;
		String messCode = "";
		log.info("Calling getStudyFromAccessionNumber/getPatientInfo...");
		int retries = Integer.parseInt(getNumeRetry());
		for (int retry = 1; retry <= retries; retry++) {
			String patientUrl = getPatientInfoUrl(patientID, studyUID, idIssuer, accessionNumber); //getPatient(null, accessionNumber, patiendID, prm);
			log.info("patient URL:" + patientUrl);
			try {
				URL urlConn = new URL(patientUrl);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + buildBasicAuth());
				code = ((HttpURLConnection) conn).getResponseCode();
				messCode = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for getPatient: " + code + ", message: " + messCode);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							studies = getStudyFromPatient(line, accessionNumber);
						}
					} 

					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /getPatientInfo fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/getPatientInfo going to sleep... " + retry);
					StreamUtils.waitForRetry(Integer.parseInt(getSleep()));
					if (retry == retries) {
						log.info("Nessun risultato, patientInfo count: " + retry);
						return null;
					} else {
						log.info("Riprovare la chiamata al servizio patientInfo, count: " + retry);
					}
				}
			}
		}
		
		return studies;
	}
	
	/**
	 * Fornisce gli studi sulla base dell'accession number
	 * @param accessionNumber accession number fornito con la richiesta
	 * @param studyUID identificativo dello studio fornito con la richiesta
	 * @return la lista di studi sulla base dell'accession number
	 */
	private List<String> buildStudies(String accessionNumber, String studyUID, String patientID, String idIssuer, PdiRetrieveManager prm) {
		List<String> studies = new ArrayList<String>();	
		if ((accessionNumber != null && !accessionNumber.isEmpty()) && (studyUID == null || studyUID.isEmpty())) {
			studies = getStudyFromAccessionNumber(accessionNumber, patientID, idIssuer,  prm);
		} else if ((accessionNumber == null || accessionNumber.isEmpty()) && (studyUID != null && !studyUID.isEmpty())) {
			accessionNumber = getAccessionNumberFromStudy(accessionNumber, studyUID, idIssuer, prm);
			studies.add(studyUID);
		} else {
			studies.add(studyUID);
		}
		
		log.info("Studies ---> " + studies);
	
		return studies;
	}

	/**
	 * Fornisce l'accession number a partire dallo studio
	 * @param studyUID l'identificativo dello studio
	 * @param accessionNumber l'accession number dello studio
	 * @return l'accessionNumer
	 */
	private String getAccessionNumberFromStudy(String accessionNumber, String studyUID, String idIssuer,	PdiRetrieveManager prm) {
		if (accessionNumber == null || accessionNumber.isEmpty()) {
			log.info("Retrieving accession number id from study");
			accessionNumber = getAccessionNumberFromStudy(studyUID, prm);
		}
		
		return accessionNumber;
	}
	
	/**
	 * Fornisce gli accession number da controllare
	 * @param accessionNumber l'eventuale accession number presente
	 * @param studyUID l'identificativo dello studio
	 * @param parts indica il rapporto di presenza tra studi e accession number
	 * @param partsStudy gli eventuali molteplici study
	 * @param partsAccNum gli eventuali moltepli accession number
	 * @return la lista di accession number da controllare
	 */
	private List<String> getAccessionNumberToCheck(String accessionNumber, String studyUID, 
			int parts, List<String> partsStudy, List<String> partsAccNum, PdiRetrieveManager prm) {
		List<String> accNumsToCheck = new ArrayList<String>();
		if (accessionNumber == null && parts == 0) {
			accessionNumber = getAccessionNumberFromStudy(studyUID, prm);
			if (accessionNumber == null || accessionNumber.isEmpty()) {
				return null;
			}
			accNumsToCheck.add(accessionNumber);
		} else if (parts == 2) {
			for (String study : partsStudy) {
				String obtainedAccNum = getAccessionNumberFromStudy(study, prm);
				if (obtainedAccNum == null || obtainedAccNum.isEmpty()) {
					return null;
				}
				log.info("obtainedAccNum ---> " + obtainedAccNum);
				accNumsToCheck.add(obtainedAccNum);
			}
		} else if (parts == 1) {
			// Molteplici accession numer forniti
			accNumsToCheck = partsAccNum;
		} else {
			// Accession number fornito
			accNumsToCheck.add(accessionNumber);
		}
		
		return accNumsToCheck;
	}
	
	/**
	 * Fornisce la lista di accession number per cui risulta necessaria la move
	 * @param patientID l'id del paziente
	 * @param studyUID l'identificativo dello studio
	 * @param idIssuer l'id dell'issuer
	 * @param accNumsToCheck gli accession number da controllare
	 * @return lista di accession number per cui risulta necessaria la move
	 * @throws Exception 
	 */
	private List<String> getFailedAccessionNumbers(String patientID, String studyUID, 
			String idIssuer, List<String> accNumsToCheck, PdiRetrieveManager prm) throws Exception {
		List<String> failedAccNums = new ArrayList<String>();
		Set<String>failedAccNumsSet = new HashSet<String>();
		log.info("getFailedAccessionNumbers ---> " + studyUID + " | " + patientID);
		if(accNumsToCheck != null) {
			for (String accNum : accNumsToCheck) {
				if (studyUID == null || studyUID.isEmpty()) {
					List<String> studies = new ArrayList<String>();
					studies	= getStudyFromAccessionNumber(accNum, patientID, idIssuer, prm); 
					if(studies != null && !studies.isEmpty()) {
						studyUID = studies.get(0);
					} else {
						failedAccNumsSet.add(accNum);
						//failedAccNums.add(accNum);
						log.info("Accession number to add 1: " + accNum);
					}
					log.info("StudyUID to add ---> " + studyUID);
				} 
				
				if(StringUtils.isNotEmpty(accNum)) {
					log.info("Checking accession number ---> " + accNum);
					boolean check = callGetPatientInfo(patientID, studyUID, idIssuer, accNum, prm);
					System.out.println("Controllo se paziente esiste: " + check);
					if (seriesUIDs == null || !check) {
						log.info("Accession number failed because patient is not present");
						failedAccNumsSet.add(accNum);
						//failedAccNums.add(accNum);
					} else {
						for (String series : seriesUIDs) {
							log.info("Checking series ---> " + series);
							List<String> exams = callGetInstance(series, studyUID, prm);
							if (exams == null || exams.isEmpty() ) {
								log.info("Accession number to add 2: " + accNum);
								failedAccNumsSet.add(accNum);
								//failedAccNums.add(accNum);
							}
							
							if(exams != null && exams.equals("-1")) {
								throw new Exception("Non riesco a recuperare l'istanza dicom");
							}
							
						}
					}
				}
			}
		}
		log.info("END OF getFailedAccessionNumbers ---> " + studyUID + " | " + patientID);
		
		for(String acc : failedAccNumsSet){
			   failedAccNums.add(acc);
		}
		
		return failedAccNums;
	}

	/**
	 * Scrive errore sulla risposta e invia notifica di errore
	 * @param response la risposta su cui scrivere l'errore
	 * @param codErrore il codice dell'errore riscontrato
	 * @param descrErrore la descrizione dell'errore riscontrato
	 * @param operazione l'operazione durante la quale risulta verificatosi l'errore
	 * @param requestID l'id della richiesta
	 */
	private void sendError(HttpServletResponse response, String codErrore, String descrErrore, 
			String operazione, String requestID, String azienda, PdiRetrieveManager prm) {
		try {
		log.info("Sending error: " + codErrore + " - " + descrErrore);
		ResponseUtils.writeErrorOnResponse(response, codErrore, descrErrore);
		prm.updateTo3pdiJob(getJobId(), codErrore, descrErrore, operazione, "ERROR", "", buildZipName(requestID, azienda), requestID, true);
		sendCompleteNotice(getJobId(), response, requestID, "ERROR", "", azienda, "", codErrore, descrErrore, prm);
		} catch (Exception e) {
			log.info("Cannot send error", e);
		}
	}
	
	/**
	 * Fornisce il nome del pacchetto zip
	 * @param requestID l'id della richiesta
	 * @param azienda l'asl
	 * @return il nome del pacchetto zip
	 */
	private String buildZipName(String requestID, String azienda) {
		if (requestID != null && !requestID.isEmpty()) {
			return requestID;
		}
		
		Patient patient = getPatientInfo();
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
		String date = dateOnly.format(cal.getTime());
		
		String zipName = new StringBuilder(patient.getFirstName())
				.append(patient.getMiddleName())
				.append(patient.getLastName())
				.append(patient.getPatientId())
				.append(patient.getBirth())
				.append(date)
				.append(getStudyDate())
				.append(azienda)
				.toString();
		
		return zipName.replaceAll("[^a-zA-Z0-9\\.\\-]", "-");
	}
	
	/**
	 * Permette di ottenere l'id del job 
	 * @return l'id del job 
	 */
	private String getJobId() {
		return jobId;
	}

	/**
	 * Permette di ottenere la directory della workarea
	 * @return la directory della workarea
	 */
	private String getWorkarea() {
		return workarea;
	}

	/**
	 * Permette di impostare il percorso alla directory della workarea
	 * @param workarea il percorso alla directory della workarea
	 */
	private void setWorkarea(String workarea) {
		this.workarea = workarea;
	}

	/**
	 * Permette di ottenere l'url per l'invocazione dei servizi dell'ASL
	 * @return l'url per l'invocazione dei servizi dell'ASL
	 */
	private String getWadoUrl() {
		return wadoUrl;
	}

	/**
	 * Permette di impostare l'url per l'invocazione dei servizi dell'ASL
	 * @param wadoUrl l'url per l'invocazione dei servizi dell'ASL
	 */
	private void setWadoUrl(String wadoUrl) {
		this.wadoUrl = wadoUrl;
	}

	/**
	 * Permette di ottenere il numero dei tentativi per l'invocazione dei servizi dell'ASL
	 * @return il numero dei tentativi per l'invocazione dei servizi dell'ASL
	 */
	private String getNumeRetry() {
		return numeRetry;
	}

	/**
	 * Permette di impostare il numero dei tentativi per l'invocazione dei servizi dell'ASL
	 * @param numeRetry il numero dei tentativi per l'invocazione dei servizi dell'ASL
	 */
	private void setNumeRetry(String numeRetry) {
		this.numeRetry = numeRetry;
	}
	
	/**
	 * Permette di ottenere la directory destinazione del contenuto PDI
	 * @return la directory destinazione del contenuto PDI
	 */
	private String getDist() {
		return dist;
	}
    
	/**
	 * Permette di impostare il percorso alla directory destinazione del contenuto PDI
	 * @param dist la directory destinazione del contenuto PDI
	 */
    private void setDist(String dist) {
		this.dist = dist;
	}
	
	/**
	 * Permette di ottenere l'url per l'invocazione dei servizi wado
	 * @return l'url per l'invocazione dei servizi wado
	 */
	private String getDpacsWadoUrl() {
		return dpacsWadoUrl;
	}

	/**
	 * Permette di impostare l'url per l'invocazione dei servizi wado
	 * @param dpacsWadoUrl l'url per l'invocazione dei servizi wado
	 */
	private void setDpacsWadoUrl(String dpacsWadoUrl) {
		this.dpacsWadoUrl = dpacsWadoUrl;
	}

	/**
	 * Permette di ottenere l'endpoint per il servizio getPatientInfo
	 * @return l'endpoint per il servizio getPatientInfo
	 */
	private String getDpacsPatientInfo() {
		return dpacsPatientInfo;
	}

	/**
	 * Permette di impostare l'endpoint per il servizio getPatientInfo
	 * @param dpacsPatientInfo l'endpoint per il servizio getPatientInfo
	 */
	private void setDpacsPatientInfo(String dpacsPatientInfo) {
		this.dpacsPatientInfo = dpacsPatientInfo;
	}

	/**
	 * Permette di ottenere il paziente per cui si sta eseguendo il job
	 * @return il paziente per cui si sta eseguendo il job
	 */
	private Patient getPatientInfo() {
		return patientInfo;
	}

	/**
	 * Permette di impostare il paziente per cui si sta eseguendo il job
	 * @param patientInfo il paziente per cui si sta eseguendo il job
	 */
	private void setPatientInfo(Patient patientInfo) {
		this.patientInfo = patientInfo;
	}

	/**
	 * Permette di impostare il percorso all'XSLT dell'ASL
	 * @param xslt il percorso all'XSLT dell'ASL
	 */
	private void setXslt(String xslt) {
		this.xslt = xslt;
	}

	/**
	 * Permette di impostare il percorso al CSSdell'ASL
	 * @param css il percorso al CSS dell'ASL
	 */
	private void setCss(String css) {
		this.css = css;
	}


	public String getJs() {
		return js;
	}

	/**
	 * Permette di impostare il percorso al JS dell'ASL
	 * @param css il percorso al JS dell'ASL
	 */
	public void setJs(String js) {
		this.js = js;
	}

	/**
	 * Permette di ottenere l'endpoint per il servizio di notifica
	 * @return l'endpoint per il servizio di notifica
	 */
	private String getCompleteNoticeUrl() {
		return completeNoticeUrl;
	}

	/**
	 * Permette di impostare l'endpoint per il servizio di notifica
	 * @param completeNoticeUrl l'endpoint per il servizio di notifica
	 */
	private void setCompleteNoticeUrl(String completeNoticeUrl) {
		this.completeNoticeUrl = completeNoticeUrl;
	}

	/**
	 * Permette di impostare l'username per i servizi FSE
	 * @param usernameFse l'username per i servizi FSE
	 */
	private void setUsernameFse(String usernameFse) {
		this.usernameFse = usernameFse;
	}

	/**
	 * Permette di impostare la password per i servizi FSE
	 * @param passwordFse la password per i servizi FSE
	 */
	private void setPasswordFse(String passwordFse) {
		this.passwordFse = passwordFse;
	}

	/**
	 * Permette di ottenere il numero dei tentativi per l'invocazione del servizio di notifica
	 * @return il numero dei tentativi per l'invocazione del servizio di notifica
	 */
	private String getNumRetryNotifica() {
		return numRetryNotifica;
	}

	/**
	 * Permette di impostare il numero dei tentativi per l'invocazione del servizio di notifica
	 * @param numRetryNotifica il numero dei tentativi per l'invocazione del servizio di notifica
	 */
	private void setNumRetryNotifica(String numRetryNotifica) {
		this.numRetryNotifica = numRetryNotifica;
	}

	/**
	 * Permette di ottenere il tempo di attesa per l'invocazione del servizio di notifica
	 * @return il tempo di attesa  per l'invocazione del servizio di notifica
	 */
	private String getSleepNotifica() {
		return sleepNotifica;
	}

	/**
	 * Permette di impostare il tempo di attesa per l'invocazione del servizio di notifica
	 * @param sleepNotifica il tempo di attesa  per l'invocazione del servizio di notifica
	 */
	private void setSleepNotifica(String sleepNotifica) {
		this.sleepNotifica = sleepNotifica;
	}

	/**
	 * Permette di ottenere le serie associate al paziente per cui si sta eseguendo il job
	 * @return le serie associate al paziente per cui si sta eseguendo il job
	 */
	private List<String> getSeriesUIDs() {
		return this.seriesUIDs;
	}
	
	/**
	 * Permette di impostare le serie associate al paziente per cui si sta eseguendo il job
	 * @param seriesUIDs le serie associate al paziente per cui si sta eseguendo il job
	 */
	private void setSeriesUIDs(List<String> seriesUIDs) {
		this.seriesUIDs = seriesUIDs;
	}
	
	/**
	 * Permette di ottenere il comando da eseguire per l'operazione move
	 * @return il comando da eseguire per l'operazione move
	 */
	private String getCommandMove() {
		return commandMove;
	}

	/**
	 * Permette di impostare il comando da eseguire per l'operazione move
	 * @param commandMove il comando da eseguire per l'operazione move
	 */
	private void setCommandMove(String commandMove) {
		this.commandMove = commandMove;
	}
	
	/**
	 * Permette di ottenere la directory del viewer
	 * @return il percorso alla directory del viewer
	 */
	private String getViewerFolder() {
		return viewerFolder;
	}

	/**
	 * Permette di impostare la directory del viewer
	 * @param viewerFolder il percorso alla directory del viewer
	 */
	private void setViewerFolder(String viewerFolder) {
		this.viewerFolder = viewerFolder;
	}

	/**
	 * Permette di impostare il checksum del file zip generato
	 * @param digest il checksum del file zip generato
	 */
	private void setDigest(String digest) {
		this.digest = digest;
	}
	
	/**
	 * Permette di ottenere la data di riferimeno per lo studio se necessaria
	 * @return la data di riferimeno per lo studio
	 */
	private String getStudyDate() {
		return studyDate;
	}

	/**
	 * Permette di impostare la data di riferimeno per lo studio se necessaria
	 * @param studyDate la data di riferimeno per lo studio
	 */
	private void setStudyDate(String studyDate) {
		this.studyDate = studyDate;
	}

	/**
	 * Permette di ottenere il tempo di attesa per l'invocazione dei servizi dell'ASL
	 * @return il tempo di attesa  per l'invocazione dei servizi dell'ASL
	 */
	private String getSleep() {
		return sleep;
	}

	/**
	 * Permette di impostare il tempo di attesa per l'invocazione dei servizi dell'ASL
	 * @param sleep il tempo di attesa per l'invocazione dei servizi dell'ASL
	 */
	private void setSleep(String sleep) {
		this.sleep = sleep;
	}
	
	/**
	 * Permette di impostare il tempo di attesa per l'invocazione dei servizi dell'ASL
	 * @param sleep il tempo di attesa per l'invocazione dei servizi dell'ASL
	 */
	private void setSleepMove(String sleepMove) {
		this.sleepMove = sleepMove;
	}
	
	/**
	 * Permette di impostare il tempo di attesa per l'invocazione dei servizi dell'ASL
	 * @param sleep il tempo di attesa per l'invocazione dei servizi dell'ASL
	 */
	private String getSleepMove() {
		return sleepMove;
	}
	
	
}