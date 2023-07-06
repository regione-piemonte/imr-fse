/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package dpacspdi;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import dao.PdiRetrieveManager;
import exception.ImageException;
import exception.WadoException;
import helpers.ConfigurationSettings;
import objects.DicomObject;
import utils.StreamUtils;

/**
 * Classe utile per la gestione dei thread
 * responsabili della gestione del contenuto PDI.
 */
public class ThreadPdi implements Runnable {
	
	private Logger log = Logger.getLogger(ThreadPdi.class);

	/**
	 * ID del job
	 */
	private String jobId;
	
	/**
	 * Endpoint per l'invocazione dei servizi wado
	 */
	private String endPoint;
	
	/**
	 * ID dello studio
	 */
	private String studyUID;
	
	/**
	 * Percorso di destinazione per i contenuti generati
	 */
	private String path;

	/**
	 * ID delle serie per cui gestire le dicom
	 */
	private List<String> seriesUIDs;
	
	/**
	 * Credenziali per le invocazioni dei servizi
	 */
	private String basicAuth;
	
	/**
	 * Numero di tentativi per l'invocazione dei servizi
	 */
	private String retries;

	/**
	 * Lista Accession Number partialmente ok
	 */
	private List<String> accNumPartialOK;
	
	/**
	 * Valore di ritorno del thread
	 * 0 = OK
	 * 1 = eccezione generica
	 * 2 = eccezzione DICOM WADO
	 * 3 = eccezione IMAGE WADO
	 */
	private volatile int valueReturn;

	/**
	 * Numero retry per chiamata WADO
	 */
	private Integer numRetries;
	
	/**
	 * Slepp retry per chiamata WADO
	 */
	private Integer sleepRetries;
	
	
	private PdiRetrieveManager prm;
	private static final String IHE_PDI = "IHE_PDI";
	private static final String NO_IMAGE = "NO_IMAGE";
	private static final String DICOM = "DICOM";

	/**
	 * Crea un oggetto di tipo ThreadPdi
	 * @param jobId identificativo univoco del job
	 * @param endPoint endpoint per l'ottenimento dei contenuti dei file
	 * @param studyUID identificativo univoco dello study
	 * @param seriesUIDs identificativi univoci delle series
	 * @param path path dove verranno salvati i contenuti
	 * @param basicAuth credenziali per chiamate rest
	 * @param retries numero di tentativi
	 */
	public ThreadPdi(String jobId, String endPoint, String studyUID, List<String> seriesUIDs, String path, String basicAuth, String retries, PdiRetrieveManager prm, List<String> accNunPartialOK) {
		this.jobId = jobId;
		this.endPoint = endPoint;
		this.studyUID = studyUID;
		this.seriesUIDs = seriesUIDs;
		this.path = path;
		this.prm = prm;
		this.basicAuth = basicAuth;
		this.retries = retries;
		this.accNumPartialOK	= accNunPartialOK;
		this.numRetries = StringUtils.isNotEmpty(prm.getConfigParamPDI("NumRetryWado")) ? Integer.valueOf(prm.getConfigParamPDI("NumRetryWado")) : 3;
		this.sleepRetries = StringUtils.isNotEmpty(prm.getConfigParamPDI("SleepRetryWado")) ? Integer.valueOf(prm.getConfigParamPDI("SleepRetryWado")) : 5000;
		setValueReturn(0);
	}
	
	@Override
	public void run() {
		log.info("ThreadPdi with id: " + getJobId());
		try {

			for (String seriesUID : seriesUIDs) {
				log.info("Getting objectUIDs for series: " + seriesUID);
				List<DicomObject> objects = getDicomIdsBySeries(seriesUID, studyUID);
				if (objects != null && !objects.isEmpty()) {
					int objectIndex = 1;
					for (DicomObject object : objects) {
						String objectName = String.format("%" + (8) + "s", "" + objectIndex).replace(" ", "0");
						String seriesNumber = prm.getSeriesNumber(seriesUID);
						String studyNumber = prm.getStudyNumber(studyUID);

						int code = 0;
						String msg = "";
												
						for (int retry = 1; retry <= this.numRetries; retry++) {	
							String urlWado = getDicomUrl(seriesUID, object.getId());

							URL urlConnWado = new URL(urlWado);
							URLConnection connWado = urlConnWado.openConnection();
							((HttpURLConnection) connWado).setRequestMethod("POST");
							connWado.setDoOutput(true);
							code = ((HttpURLConnection) connWado).getResponseCode();
							msg = ((HttpURLConnection) connWado).getResponseMessage();
							log.info("Code for WADO => application/dicom: " + code + ", message: " + msg);
							if (code == 200) {
								if (connWado != null && connWado.getInputStream() != null) {	
	
									if(connWado.getInputStream().available() < 200) {
										log.info("WADO => application/dicom input stream, retry #: " + retry);
										if (retry == this.numRetries.intValue()) {
											throw new WadoException();
										}
									}
									makeDir(path + File.separator + DICOM, DICOM);
									makeDir(path + File.separator + DICOM + File.separator + studyNumber, "study");
									makeDir(path + File.separator + DICOM + File.separator + studyNumber + File.separator + seriesNumber, "series");
	
									log.info("Saving as DICOM from: " + urlWado);
									saveAsDicom(connWado.getInputStream(), 
											path + File.separator + DICOM + File.separator + studyNumber + File.separator + seriesNumber + File.separator + objectName);
									log.info("Saving as DICOM FINISH");
								} else if (retry == this.numRetries.intValue()) {
									throw new WadoException();
								}
								
								break;
							} else if (retry == this.numRetries.intValue()) {
								throw new WadoException();
							}
						}
												
						String urlWadoJ = getImgUrl(seriesUID, object);

						URL urlConnWadoJ = new URL(urlWadoJ);
						URLConnection connWadoJ = urlConnWadoJ.openConnection();
						((HttpURLConnection) connWadoJ).setRequestMethod("POST");
						connWadoJ.setDoOutput(true);
						code = ((HttpURLConnection) connWadoJ).getResponseCode();
						msg = ((HttpURLConnection) connWadoJ).getResponseMessage();
						log.info("Code for WADO => image/jpeg: " + code + ", message: " + msg + " seriesNumber:" +seriesNumber);
						if (code == 200) {
							if (connWadoJ != null && connWadoJ.getInputStream() != null && connWadoJ.getContentLengthLong() == 0L) {
								if(StringUtils.isNotEmpty(object.getRows()) && StringUtils.isNotEmpty(object.getColumns()) ) {
									log.info("E' UNA IMMAGINE");
									sendEmail(seriesUID, studyNumber);
									throw new ImageException();
								} else {
									log.info("NON E' UNA IMMAGINE");
								}
							}
							if (connWadoJ != null && connWadoJ.getInputStream() != null && StringUtils.isNotEmpty(object.getRows()) && StringUtils.isNotEmpty(object.getColumns())) {
								makeDir(path + File.separator + IHE_PDI, IHE_PDI);
								makeDir(path + File.separator + IHE_PDI + File.separator + studyNumber, "study");
								makeDir(path + File.separator + IHE_PDI + File.separator + studyNumber + File.separator + seriesNumber, "series");

								log.info("Saving as JPEG from: " + urlWadoJ);
								saveAsJpeg(path + File.separator + IHE_PDI + File.separator + studyNumber + File.separator + seriesNumber + File.separator + objectName + ".jpeg", connWadoJ.getInputStream());
							}
						} else {
							throw new ImageException();
						}
															
						
						objectIndex += 1;
					}
				}  else {
					log.info("DICOM ERRORE GENERICO");
					throw new Exception();
				}
			}
			
			// Nel caso di Accession Number privi di immagini o assenti aggiungo le directory nella creazione del file zip
			
			if(this.accNumPartialOK != null && !this.accNumPartialOK.isEmpty() ) {
				
				makeDir(path + File.separator + IHE_PDI + File.separator + NO_IMAGE, NO_IMAGE);
				for(String accNum : accNumPartialOK) {
					makeDir(path + File.separator + IHE_PDI + File.separator + NO_IMAGE + File.separator + accNum, "ACCESSION NUMBER P_OK" + accNum);
				}
			}

		
		} catch (WadoException e) {
			setValueReturn(2);
			log.error("Calling 2 servlet /wado?requestType=WADO => application/dicom failed: " + e.getMessage(), e);
			return;
		} catch (ImageException e) {
			setValueReturn(3);
			log.error("Calling 3 servlet /wado?requestType=WADO => application/dicom failed: " + e.getMessage(), e);
			return;
		} catch (Exception e) {
			setValueReturn(1);
			log.error("Calling 1 servlet /wado?requestType=WADO => application/dicom failed: " + e.getMessage(), e);
			return;
		}
	}
	
	

	public int getValueReturn() {
		return valueReturn;
	}

	public void setValueReturn(int valueReturn) {
		this.valueReturn = valueReturn;
	}

	/**
	 * Fornisce l'url per le immagini jpeg
     * @param object dicom per jpeg
     * @param seriesUID identificativo dello serie
	 * @return l'url per le immagini jpeg
	 */
	private String getImgUrl(String seriesUID, DicomObject object) {
		String url = new StringBuilder(getEndPoint())
				.append("/o3-dpacs-wado/wado")
				.append("?requestType=WADO")
				.append("&studyUID=")
				.append(getStudyUID())
				.append("&seriesUID=")
				.append(seriesUID)
				.append("&objectUID=")
				.append(object.getId())
				.append("&frameNumber=")
				.append(object.getActualFrames())
				.append("&contentType=image/jpeg")
				.toString();
		
		log.info("Url di chiamata WADO => image/jpeg: " + url);
		
		return url;
	}

	/**
	 * Fornisce l'url per le immagini dicom
     * @param objectUID identificativo della dicom
     * @param seriesUID identificativo dello serie
	 * @return l'url per le immagini dicom
	 */
	private String getDicomUrl(String seriesUID, String objectUID) {
		String url = new StringBuilder(getEndPoint())
				.append("/o3-dpacs-wado/wado")
				.append("?requestType=WADO")
				.append("&studyUID=")
				.append(getStudyUID())
				.append("&seriesUID=")
				.append(seriesUID)
				.append("&objectUID=")
				.append(objectUID)
				.append("&contentType=application/dicom")
				.toString();
		
		log.info("Url di chiamata WADO => application/dicom: " + url);
		
		return url;
	}
	
	/**
     * Metodo che permette di recuperare gli identificativi delle Series in uno studio
     * @param studyUID identificativo dello studio
     * @param seriesUID identificativo dello serie
     * @return lista di DICOM recuperate
     */
	private List<DicomObject> getDicomIdsBySeries(String seriesUID, String studyUID) {
		List<DicomObject> dicoms = new ArrayList<DicomObject>();
		String url = getGetInstanceUrl(seriesUID, studyUID);
		log.info("Url for getInstances: " + url);
		
		int code = 0;
		String msg = "";
		int retries = Integer.parseInt(this.retries);
		for (int retry = 1; retry <= retries; retry++) {
			try {
				URL urlConn = new URL(url);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + basicAuth);
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
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /getInstances fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/getInstances going to sleep... " + retry);
					StreamUtils.waitForRetry(ConfigurationSettings.SLEEP);
					if (retry == retries) {
						log.info("Nessun risultato, getInstances count: " + retry);
						return null;
					} else {
						log.info("Riprovare la chiamata al servizio getInstances, count: " + retry);
					}
				}
			}
		}
		
		return dicoms;
	}
	
	
	/**
     * Metodo che permette di inviare un'email tramite procedura send_mail_alert
     * @param studyUID identificativo dello studio
     * @param seriesUID identificativo dello serie
     * @return isSend 
     */
	private boolean sendEmail(String seriesUID, String studyUID) {
		boolean isSend		= true;
		String subject		= null;
		String object		= null;
		String todayStr		= null;
		String dateLastSend	= null;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Calendar today = new GregorianCalendar();
		todayStr	= sdf.format(today.getTime());
		
		if(StringUtils.isNotEmpty(todayStr)) {
			log.info("TODAY: "+todayStr);
		}
		
		dateLastSend = prm.getConfigParamPDI(ConfigurationSettings.CHECK_IS_SEND);
		
		if(StringUtils.isNotEmpty(dateLastSend)) {
			log.info("DATA LAST SEND: "+dateLastSend);
		}
		
		if(!dateLastSend.equalsIgnoreCase(todayStr)){
			subject = prm.getConfigParamPDI("subjectMail");
			object	= prm.getConfigParamPDI("objectMail");
			
			if(StringUtils.isNotEmpty(subject)) {
				subject = subject.replace("{1}", seriesUID);
			}
			
			log.info("object: " + object + " subject: " + subject);
			try {
				isSend = prm.sendEmail(object, subject);
				
				prm.updateTo3pdiConf(ConfigurationSettings.CHECK_IS_SEND, todayStr);
			} catch (Exception e) {
				isSend = false;
				log.error("ERRORE INVIO EMAIL: " + e.getMessage(), e);
			}
			
			
		} else {
			log.info("EMAIL GIA' INVIATA");
		}
		
		
		
		return isSend;
	}

	/**
     * Metodo che permette di recuperare gli identificativi delle DICOM in una serie
     * @return lista di DICOM recuperate
     */
	private List<DicomObject> getDicomsBySeries(String line) {
		List<DicomObject> UIDs = new ArrayList<DicomObject>();
		
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
				
				String dicomId = eElement.getAttribute("uid");
				String dicomFrames = eElement.getAttribute("numberOfFrames");
				String columns = eElement.getAttribute("columns") != null ? eElement.getAttribute("columns") : "";
				String rows = eElement.getAttribute("rows") != null ? eElement.getAttribute("rows") : "";
				UIDs.add(new DicomObject(dicomId, dicomFrames,rows, columns ));
			}
		} catch (ParserConfigurationException e) {
			log.error("Impossibile analizzare il risultato...", e);
			return null;
		} catch (SAXException e) {
			log.error("Impossibile analizzare il file di configurazione! ", e);
			return null;
		} catch (Exception oex) {
			log.error("Impossibile caricare il file di configurazione...", oex);
			return null;
		}
		
		return UIDs;
	}
	
	/**
	 * Fornisce l'URL del servizio getInstance
	 * @param seriesUID l'identificativo della serie
	 * @param studyUID l'identificativo dello studio
	 * @return l'URL del servizio getInstance
	 */
	private String getGetInstanceUrl(String seriesUID, String studyUID) {
		String url = new StringBuilder(getEndPoint())
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
	 * Metodo utile al salvataggio di un'immagine in formato JPEG
	 * @param outputPath path dove salvare l'immagine
	 * @param imageStream stream contenente l'immagine
	 * @throws IOException 
	 */
	private void saveAsJpeg(String outputPath, InputStream imageStream) throws IOException {
		
			FileOutputStream outputStream = new FileOutputStream(outputPath);
			byte[] buffer = new byte[8 * 1024];
			int bytesRead;
			while ((bytesRead = imageStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.close();	
			
//			BufferedImage image = ImageIO.read(imageStream);
//			
//			log.info("Encoding image");
//			JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(outputStream);						
//			JPEGEncodeParam jpegEncodeParam = enc.getDefaultJPEGEncodeParam(image);
//			jpegEncodeParam.setQuality(100/100, false);
//
//			log.info("Saving image in " + outputPath);
//			enc.encode(image, jpegEncodeParam);
//
//			outputStream.close();
		
	}
	

	/**
	 * Metodo utile al salvataggio di un'immagine in formato JPEG
	 * @param outputPath path dove salvare l'immagine
	 * @param imageStream stream contenente l'immagine
	 */
	@SuppressWarnings("unused")
	private void saveAsJpegFromDicom(String outputPath, String dicomPath) {
		try {
			File myDicomFile = new File(dicomPath);
			BufferedImage myJpegImage = null;
			Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
			ImageReader reader = (ImageReader) iter.next();
			DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();
			
			try {
				   ImageInputStream iis = ImageIO.createImageInputStream(myDicomFile);
				   reader.setInput(iis, false);   
				   myJpegImage = reader.read(0, param);   
				   iis.close();
				   
				   if (myJpegImage == null) {
					      log.info("\nError: couldn't read dicom image!");
					      return;
					   }
				   File myJpegFile = new File(outputPath);   
				   OutputStream output = new BufferedOutputStream(new FileOutputStream(myJpegFile));
				   JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
				   encoder.encode(myJpegImage);
				   output.close();
			} 
			catch(IOException e) {
			   log.error("\nError: couldn't read dicom image!"+ e.getMessage(), e);
			   return;
			}
			
		} catch (Exception e) {
			log.error("Cannot write JPEG due to " + e.getMessage(), e);
			e.printStackTrace();
		}
	}

	/**
	 * Metodo utile al salvataggio di un'immagine in formato DICOM
	 * @param dicomStream stream contenente il file DICOM
	 * @throws IOException 
	 */
	private void saveAsDicom(InputStream dicomStream, String outputPath) throws IOException {
		
			FileOutputStream outputStream = new FileOutputStream(outputPath);
			byte[] buffer = new byte[8 * 1024];
			int bytesRead;
			
			while ((bytesRead = dicomStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			
			outputStream.close();	
		
	}
	
	/**
	 * Crea una directory nel percorso specificato
	 * @param directoryPath percorso in cui creare la cartella
	 * @param createdDirectory nome della cartella da creare
	 */
	private void makeDir(String directoryPath, String createdDirectory) {
		File directory = new File(directoryPath);
		log.info("Creating directory for " + createdDirectory);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}
	
	/**
	 * Fornisce l'endpoint per l'invocazione dei servizi wado
	 * @return l'endpoint per l'invocazione dei servizi wado
	 */
	private String getEndPoint() {
		return endPoint;
	}

	/**
	 * Fornisce lo studio di riferimento per il contenuto pdi
	 * @return lo studio di riferimento per il contenuto pdi
	 */
	private String getStudyUID() {
		return studyUID;
	}

	/**
	 * Fornisce l'id del job
	 * @return l'id del job
	 */
	private String getJobId() {
		return jobId;
	}
}
