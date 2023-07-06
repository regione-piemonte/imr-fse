/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dao.PdiRetrieveManager;
import dto.DICOMVIEWERDATA;
import dto.DICOMVIEWERDATA.CSS;
import dto.DICOMVIEWERDATA.JS;
import dto.HOMETYPE;
import dto.INDEXTYPE;
import dto.InstanceDTO;
import dto.OBJECTTYPE;
import dto.PATIENTTYPE;
import dto.SELECTTYPE;
import dto.SERIESTYPE;
import dto.SERIESTYPE.LISTOBJECT;
import dto.STUDYTYPE;
import dto.STUDYTYPE.LISTSERIES;
import dto.TYPETOC;
import dto.TYPETOC.LINKSTUDYFORTOC;
import helpers.ConfigurationSettings;
import objects.Patient;

/**
 * Classe utile alla generazione di una stringa contenente 
 * html che consente la visualizzazione di file DICOM 
 */
public class DicomViewGenerator {

	private Logger log = Logger.getLogger(DicomViewGenerator.class);

	private static final String ASSETS = "ASSETS";
	private static final String OBJECTS = "OBJECTS";
	private static final String SERIES = "SERIES";
	private static final String STUDIES = "STUDIES";
	private static final String PAGES = "PAGES";
	private static final String IHE_PDI = "IHE_PDI";
	private static final String PATH_SEPARATOR = "/";
	
	/**
	 * Directory destinazione del contenuto PDI
	 */
	private String dist;
	
	/**
	 * Studi da inserire nalla view
	 */
	private List<String> studyUIDs;
	
	/**
	 * Paziente per cui generare la view del contenuto PDI
	 */
	private Patient patient;
	
	/**
	 * Descrizione dello studio
	 */
	private String studyDescription;
	
	/**
	 * Accession number dello studio
	 */
	private String accessionNumber;
	
	/**
	 * CSS da applicare alla view
	 */
	private String css;
	
	/**
	 * JS da applicare alla view
	 */
	private String js;
	
	/**
	 * XSLT da applicare all'header della view
	 */
	private String xslt;
	
	/**
	 * Credenziali per l'invocazione dei servizi
	 */
	private String basicAuth;
	
	/**
	 * Url base per l'invocazione dei servizi
	 */
	private String url;
	
	/**
	 * Numero di retry per l'invocvazione ai servizi
	 */
	private String retries;
	
	private PdiRetrieveManager prm;
	
	public DicomViewGenerator(String dist, List<String> studyUIDs, Patient patient, 
			String css, String js, String basicAuth, PdiRetrieveManager prm, String xslt, String url, String retries) {
		this.dist = dist;
		this.studyUIDs = studyUIDs;
		this.patient = patient;
		this.css = css;
		this.js = js;
		this.prm = prm;
		this.xslt = xslt;
		this.basicAuth = basicAuth;
		this.url = url;
		this.retries = retries;
		
		log.info("dist:" +dist);
		for(String study : studyUIDs) {
			log.info("study:" +study);
		}
		if(patient != null) {
			log.info("patient: " + patient.toString());
		}
		log.info("xslt" + xslt);
			
		log.info("basicAuth" + basicAuth);
		log.info("url" + url);
		log.info("retries" + retries);
		
	}
	
	/**
	 * Questo metodo esegue metodi al fine di creare directory 
	 * e pagine .HTM per la visualizzazione dei file DICOM
	 */
	public Boolean generateDicomView() {
		makeDirs();
		log.info("Generating view...");
		
		DICOMVIEWERDATA	xmlData	= new DICOMVIEWERDATA();
		
		try {
			generateIndex(xmlData);
			generateHome(xmlData);
			generateSelect(xmlData);
			generateToc(xmlData);
			generateStudies(xmlData);
			return generateHTML(xmlData);
		} catch (Exception e) {
			log.info("View not generated, error:" + e.getMessage() , e);
			return false;
		}		
	}
	
	
	/**
	 * Questo metodo genera la pagina INDEX.HTM 
	 */
	private void generateIndex() {
		log.info("Generating index...");
		String index = new StringBuilder()
			.append("<!DOCTYPE html>"
					+ "<html lang=\"it\">\r\n"
					+ "    <head>\r\n"
					+ "        <title>DICOM Viewer</title>\r\n"
					+ "        <meta charset=\"UTF-8\">\r\n"
					+ "		   <link rel=\"stylesheet\" href=\"" + getCssAbsoluteName(css) +" />"
					+ "    </head>\r\n"
					+ "		<iframe class=\"header-div\" name=\"header\" src=\"IHE_PDI/PAGES/HEADER.htm\"></iframe>\r\n"
					+ "		<div class=\"wrapper\">\r\n"
					+ "			<div class=\"inner-wrapper\">\r\n"	
					+ "				<iframe class=\"studies-div\" name=\"studylist\" src=\"IHE_PDI/PAGES/TOC.htm\"></iframe>\r\n"
					+ "				<iframe class=\"series-div\" name=\"serieslist\" src=\"IHE_PDI/PAGES/STUDIES/" + getPathFirtStudy() + "\"></iframe>\r\n"
					+ "				<iframe class=\"objects-div\" name=\"objectlist\" src=\"IHE_PDI/PAGES/SERIES/" + getPathFirtSeries() + "\"></iframe>\r\n"
					+ "			</div>\r\n"
					+ "			<div class=\"view-div\">\r\n"	
					+ "				<iframe class=\"img-div\" name=\"view\" src=\"IHE_PDI/PAGES/OBJECTS/" + getPathFirtImg() + "\"></iframe>\r\n"
					+ "			</div>\r\n"
					+ "		</div>\r\n"
					+ "</html>")
			.toString();
		
		StreamUtils.writeFile(dist + PATH_SEPARATOR + "INDEX.HTM", index);
	}
	
	private void generateIndex(DICOMVIEWERDATA xmlData) {
		log.info("Generating index...");
		INDEXTYPE 	index	= new INDEXTYPE();
		CSS 		style 	= new CSS();
		JS 			script 	= new JS();
		
		style.setSTYLE(getCssAbsoluteName(css));
		if(StringUtils.isNotEmpty(style.getSTYLE())){
			xmlData.getCSS().add(style);
		}
		
		script.setJAVASCRIPT(getCssAbsoluteName(js));
		if(StringUtils.isNotEmpty(script.getJAVASCRIPT())){
			xmlData.getJS().add(script);
		}
		
		index.setPATHFIRSTSTUDY(getPathFirtStudy());
		index.setPATHFIRSTSERIES(getPathFirtSeries());
		index.setPATHFIRSTIMG(getPathFirtImg());
		index.setURIFILE(dist + PATH_SEPARATOR + "INDEX.HTM");
		
		xmlData.setINDEX(index);
	}
	
	/**
	 * Questo metodo viene utilizzato per il recupero del corretto nome del css da utilizzare.
	 */
	private static String getCssAbsoluteName(String css) {
		String cssName = "";
		
		if(StringUtils.isNotEmpty(css)) {
			System.out.println("CSS name: " + css);
			if(css.contains("/")) {
				cssName = ASSETS + "/" + css.substring(css.lastIndexOf("/")+1);
			} else {
				cssName = ASSETS + "/" + css.substring(css.lastIndexOf("\\")+1);
			}
			
		}
		
		return cssName;
	}
	
	/**
	 * Questo metodo genera la pagina HEADER.HTM facendo uso di una trasformazione XSLT
	 */
	private Boolean generateHTML(DICOMVIEWERDATA xmlData) {
		
		String htmOutPath = dist + PATH_SEPARATOR + "INDEX.HTM";
		
		log.info("XML START: "+ xmlData.toString() );
		
		return XsltUtils.makeXslTrasformation(xmlData, htmOutPath, xslt );
	}
	
	/**
	 * Questo metodo genera la pagina HEADER.HTM facendo uso di una trasformazione XSLT
	 */
	private void generateHeader() {
		String confPath = prm.getConfigParamPDI("Assets");
		String htmOutPath = getPagesDir() + PATH_SEPARATOR + "HEADER.HTM";
		String xmlInPath = confPath + "header.xml";
		
		XsltUtils.makeXslTrasformation(xmlInPath, htmOutPath, xslt + "header.xsl");
	}
	
	/**
	 * Questo metodo genera la pagina HOME.HTM
	 */
	private void generateHome() {
		log.info("Generating home...");
		
		String home = new StringBuilder()
			.append("<!DOCTYPE html>"
					+ "<html lang=\"it\">\r\n"
					+ "    <head>\r\n"
					+ "        <title>DICOM Viewer</title>\r\n"
					+ "        <meta charset=\"UTF-8\">\r\n"
					+ "    </head>\r\n"
					+ "    <body>\r\n"
					+ "        <div class=\"background\"></div>\r\n"
					+ "    </body>\r\n"
					+ "</html>")
			.toString();
		
		StreamUtils.writeFile(getPagesDir() + PATH_SEPARATOR + "HOME.HTM", home);
	}
	
	/**
	 * Questo metodo genera la pagina HOME.HTM tramite trasformazione XSLT
	 */
	private void generateHome(DICOMVIEWERDATA xmlData) {
		log.info("Generating home...");
		
		HOMETYPE home = new HOMETYPE();
		home.setURIFILE(getPagesDir() + PATH_SEPARATOR + "HOME.HTM");

		xmlData.setHOME(home);
	}
	
	/**
	 * Questo metodo genera la pagina SELECT.HTM
	 */
	private void generateSelect() {
		log.info("Generating select...");
		
		String select = new StringBuilder()
			.append("<!DOCTYPE html>"
					+ "<html lang=\"it\" xmlns=\"http://www.w3.org/1999/xhtml\">\r\n"
					+ "    <head>\r\n"
					+ "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\r\n"
					+ "        <title>DICOM Viewer</title>\r\n"
					+ "    </head>\r\n"
					+ "    <body>\r\n"
					+ "        <p>Select item above</p>\r\n"
					+ "    </body>\r\n"
					+ "</html>")
			.toString();
		
		StreamUtils.writeFile(getPagesDir() + PATH_SEPARATOR + "SELECT.HTM", select);
	}
	
	/**
	 * Questo metodo genera la pagina SELECT.HTM tramite trasformazione XSLT
	 */
	private void generateSelect(DICOMVIEWERDATA xmlData) {
		log.info("Generating select...");
		SELECTTYPE select = new SELECTTYPE();
		select.setURIFILE(getPagesDir() + PATH_SEPARATOR + "SELECT.HTM");
		
	}
	
	/**
	 * Questo metodo general la pagina TOC.HTM
	 */
	private void generateToc() {
		log.info("Generating toc...");
		
		String toc = new StringBuilder()
			.append("<!DOCTYPE html>"
					+ "<html lang=\"it\" xmlns=\"http://www.w3.org/1999/xhtml\">\r\n"
					+ "    <head>\r\n"
					+ "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\r\n"
					+ "            <title>STUDIES LIST</title>\r\n"
					+ "    </head>\r\n"
					+ "    <body>\r\n"
					+ "        Studies:<br/>\r\n"
					+ getStudiesForToc()
					+ "    </body>\r\n"
					+ "</html>")
			.toString();
		
		StreamUtils.writeFile(getPagesDir() + PATH_SEPARATOR + "TOC.HTM", toc);
	}
	
	/**
	 * Questo metodo general la pagina TOC.HTM tramite trasformazione XSLT
	 */
	private void generateToc(DICOMVIEWERDATA xmlData) {
		log.info("Generating toc...START");
		
		TYPETOC toc = new TYPETOC();
		toc.setURIFILE(getPagesDir() + PATH_SEPARATOR + "TOC.HTM");
		
		toc.getLINKSTUDYFORTOC();
		getStudiesForToc(toc.getLINKSTUDYFORTOC());
		xmlData.setTOC(toc);			
		
		log.info("Generating toc...END");
	}
	
	/**
	 * Questo metodo genera l'elenco degli studi
	 */
	private void generateStudies() {
		for (String study : studyUIDs) {
			generateStudy(study);
		}
	}
	
	/**
	 * Questo metodo genera l'elenco degli studi tramite trasformazione XSLT
	 */
	private void generateStudies(DICOMVIEWERDATA xmlData) throws Exception {
		dto.DICOMVIEWERDATA.STUDIES studies  = new dto.DICOMVIEWERDATA.STUDIES();
		for (String study : studyUIDs) {
			generateStudy(study, xmlData, studies);
		}
	}
	
	/**
	 * Questo metodo genera il singolo elemento dell'elenco degli studi
	 * @param study identificativo univoco dello study
	 */
	private void generateStudy(String study) {
		log.info("Generating study: " + study + "...");
		List<String> seriesUIDs = getSeriesIdsFromStudy(study);
		String studyNumber = prm.getStudyNumber(study);
		getStudyInfo(study);
		String studies = new StringBuilder()
			.append("<!DOCTYPE html>"
					+ "<html lang=\"it\" xmlns=\"http://www.w3.org/1999/xhtml\">\r\n"
					+ "    <head>\r\n"
					+ "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\r\n"
					+ "            <title>SERIES LIST</title>\r\n"
					+ "    </head>\r\n"
					+ "    <body>\r\n"
					+ "        " + this.studyDescription + "<br/>\r\n"
					+ "        Series:<br/>\r\n"
					+ getSeriesForStudy(seriesUIDs, study)
					+ "    </body>\r\n"
					+ "</html>")
			.toString();
		
		StreamUtils.writeFile(getStudiesDir() + PATH_SEPARATOR + studyNumber + ".HTM", studies);
		
		generateAllSeriesForStudy(study, seriesUIDs);
	}
	
	/**
	 * Questo metodo genera il singolo elemento dell'elenco degli studi tramite XSLT
	 * @param study identificativo univoco dello study
	 */
	private void generateStudy(String study, DICOMVIEWERDATA xmlData,dto.DICOMVIEWERDATA.STUDIES studies ) throws Exception {
		STUDYTYPE st = new STUDYTYPE();
		log.info("Generating study: " + study + "...");
		List<String> seriesUIDs = getSeriesIdsFromStudy(study);
		String studyNumber = prm.getStudyNumber(study);
		getStudyInfo(study);
		
		st.setSTUDYDESCRIPTION(this.studyDescription);
		st.setURIFILE(getStudiesDir() + PATH_SEPARATOR + studyNumber + ".HTM");
		
		STUDYTYPE.LISTSERIES listoSeries = new LISTSERIES();
		getSeriesForStudy(seriesUIDs, study, listoSeries);
		if(listoSeries == null || listoSeries.getSERIES() == null || listoSeries.getSERIES().size()==0) {
			log.info("Serie vuota: " + study + "...");
			throw new Exception("CC_ERR_505");
		}
		st.setLISTSERIES(listoSeries);
		studies.getSTUDY().add(st);
		xmlData.setSTUDIES(studies);
		
		log.info("END study: " + study + "...");
	}
	
	/**
	 * Questo metodo genera l'elenco di series per uno studio
	 * @param study identificativo univoco dello studio
	 * @param seriesUIDs lista contenente gli identificativi delle series per lo studio
	 */
	private void generateAllSeriesForStudy(String study, List<String> seriesUIDs) {
		for (String series : seriesUIDs) {
			generateSeries(study, series);
		}
	}
	
	/**
	 * Questo metodo genera la sezione dove per una singola series compare l'elenco degli object associati
	 * @param study identificativo univoco dello study
	 * @param series identificativo univoco della series
	 */
	private void generateSeries(String study, String series) {
		log.info("Generating series: " + series + "...");
		List<String> objectUIDs = getDicomIdsFromSeries(series, study);
		if (objectUIDs == null || objectUIDs.isEmpty()) {
			return;
		}
		
		String seriesNumber = prm.getSeriesNumber(series);
		String seriesDesc = getSeriesDesc(series, study);
		String studyNumber = prm.getStudyNumber(study);
		String seriesHtml = new StringBuilder()
			.append("<!DOCTYPE html>"
					+ "<html lang=\"it\" xmlns=\"http://www.w3.org/1999/xhtml\">\r\n"
					+ "    <head>\r\n"
					+ "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\r\n"
					+ "        <title>STUDY</title>\r\n"
					+ "    </head>\r\n"
					+ "    <body>\r\n"
					+ "        " + seriesDesc + "<br/>\r\n"
					+ "        Objects:<br/>\r\n"
					+ getObjectsForSeries(studyNumber, objectUIDs, seriesNumber)
					+ "    </body>\r\n"
					+ "</html>")
			.toString();
		
		StreamUtils.writeFile(getSeriesDir() + PATH_SEPARATOR + seriesNumber + ".HTM", seriesHtml);
		
		generateAllObjectsForSeries(study, series, objectUIDs);
	}
	
	/**
	 * Questo metodo genera l'elenco completo degli object
	 * @param study identificativo univoco dello studio
	 * @param series identificativo univoco della serie
	 * @param objectUIDs lista contenente gli object associati alla serie
	 */
	private void generateAllObjectsForSeries(String study, String series, List<String> objectUIDs) {
		int objectIndex = 1;
		for (String object : objectUIDs) {
			generateObject(study, series, object, objectIndex, objectUIDs.size());
			objectIndex += 1;
		}
	}

	/**
	 * Questo metodo genera il singolo elemento dell'elenco degli object
	 * @param study identificativo univoco dello studio
	 * @param series identificativo univoco della serie
	 * @param objectId identificativo univoco dell'object
	 * @param objectIndex indice dell'object
	 * @param maxObjectIndex indice che rappresenta il numero massimo per lo scorrimento delle immagini
	 */
	private void generateObject(String study, String series, String objectId, int objectIndex, int maxObjectIndex) {
		String objectName = String.format("%" + (8) + "s", "" + objectIndex).replace(" ", "0");	
		
		int nextObjectIndex = objectIndex + 1;
		String nextObjectName = String.format("%" + (8) + "s", "" + nextObjectIndex).replace(" ", "0");
		
		int prevObjectIndex = objectIndex - 1;
		String prevObjectName = String.format("%" + (8) + "s", "" + prevObjectIndex).replace(" ", "0");
		
		String studyNumber = prm.getStudyNumber(study);
		String seriesNumber = prm.getSeriesNumber(series);
		String seriesDesc = getSeriesDesc(series, study);
		
		StringBuilder object = new StringBuilder()	
			.append("<!DOCTYPE html>"
					+ "<html lang=\"it\" xmlns=\"http://www.w3.org/1999/xhtml\">\r\n"
					+ "    <head>\r\n"
					+ "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\r\n"
					+ "        <title>SERIES</title>\r\n"
					+ "    </head>\r\n"
					+ "    <body>\r\n"
					+ "        Patient ID: " + patient.getPatientId() + "<br/>\r\n"
					+ "        Patient Name: " + patient.getFirstName() + " " + patient.getMiddleName() + " " + patient.getLastName() + "<br/>\r\n"
					+ "        Patient Birth: " + patient.getBirth() + "<br/>\r\n"
					+ "        Study Description: " + studyDescription + "<br/>\r\n"
					+ "        Accession Number: " + accessionNumber + "<br/>\r\n"
					+ "        Series Description: " + seriesDesc + "<br/><br/>\r\n"
					+ "        <br/>\r\n");
		
		if(objectIndex > 1) {
			object.append("<span>\r\n"
					+ "		<a target=\"view\" href=\"../OBJECTS/" + seriesNumber + "-" + prevObjectName + ".HTM\">Prev</a>\r\n" 
					+ "</span>\r\n");
			
			// Last element, so we add space after prev instead of next
			if (objectIndex == maxObjectIndex) {
				object.append("<br/><br/>\r\n");
			}
		}
		
		if (objectIndex < maxObjectIndex) {
			object.append("<span>\r\n"
					+ "		<a target=\"view\" href=\"../OBJECTS/" + seriesNumber + "-" + nextObjectName + ".HTM\">Next</a>\r\n" 
					+ "</span>\r\n"
					+ "<br/><br/>\r\n");	
		}
		
		object.append(getDicomImg(studyNumber, seriesNumber, objectName, false));
		object.append("</body>\r\n</html>");
			
		StreamUtils.writeFile(getObjectsDir() + PATH_SEPARATOR + seriesNumber + "-" + objectName + ".HTM", object.toString());
	}
	
	/**
	 * Questo metodo genera l'htm utile a mostrare un'immagine
	 * @param studyNumber numero dello studio
	 * @param seriesNumber numero della serie
	 * @param objectName nome dell'object
	 * @param isPreview true nel caso in cui l'immagine e' un'anteprima, false altrimenti
	 * @return una stringa contenente l'html utile per mostrare l'immagine
	 */
	private String getDicomImg(String studyNumber, String seriesNumber, String objectName, boolean isPreview) {
		StringBuilder dicomReturn = null;
		
		if(isPreview) {
			dicomReturn = new StringBuilder("<img src=\"../../");
			dicomReturn.append(studyNumber)
			.append(PATH_SEPARATOR)
			.append(seriesNumber)
			.append(PATH_SEPARATOR)
			.append(objectName)
			.append(".jpeg");
			
			dicomReturn.append("\" alt=\"Image missing\" style=\"width:55px; height:55px;\"/><br/><br/>\r\n");
		} else {
			dicomReturn = new StringBuilder("<a href=\"../../");
			dicomReturn.append(getObjectPath(studyNumber, seriesNumber, objectName)).append("\" target=\"_blank\">");
			
			StringBuilder dicomImg = new StringBuilder("<img src=\"../../");
			dicomImg.append(getObjectPath(studyNumber, seriesNumber, objectName));
			
			
			dicomImg.append("\" alt=\"Image missing\" /><br/><br/>\r\n");
			
			dicomReturn.append(dicomImg.toString()).append("</a><br/><br/>\r\n");
		}
		
		
		return dicomReturn.toString();

	}
	
	/**
	 * Fornisce il percorso della JPEG relativa ad una DICOM.
	 * @param studyNumber numero dello studio.
	 * @param seriesNumber numero della serie.
	 * @param objectName nome dell'object.
	 * @return il percorso della JPEG relativa ad una DICOM.
	 */
	public String getObjectPath(String studyNumber, String seriesNumber, String objectName) {
		return new StringBuilder(studyNumber)
		.append(PATH_SEPARATOR)
		.append(seriesNumber)
		.append(PATH_SEPARATOR)
		.append(objectName)
		.append(".jpeg")
		.toString();
	}
	
	/**
	 * Questo metodo genera le anteprima delle immagini dicom per una serie
	 * @param studyNumber numero dello studio
	 * @param objectUIDs identificativo univoco dell'object
	 * @param seriesNumber numero della serie
	 * @return stringa contenente html che mostra l'anteprima delle immagini dicom in una tabella 
	 */
	private String getObjectsForSeries(String studyNumber, List<String> objectUIDs, String seriesNumber) {
		log.info("Generating objects for series...");
		
		StringBuilder objectsForSeriesBuilder = new StringBuilder();
		objectsForSeriesBuilder.append("<table style=\"width:100%\">");
		int objectIndex = 1;
		while (objectIndex <= objectUIDs.size()) {
			objectsForSeriesBuilder.append("<tr>");
				
			for (int columnIndex = 1; columnIndex <= 4 && objectIndex <= objectUIDs.size(); columnIndex++) {
				String objectName = String.format("%" + (8) + "s", "" + objectIndex)
						.replace(" ", "0");
				
				objectsForSeriesBuilder.append("<td>\r\n")
					.append("<a target=\"view\" href=\"../OBJECTS/")
					.append(seriesNumber)
					.append("-")
					.append(objectName)
					.append(".HTM\">")
					.append(getDicomImg(studyNumber, seriesNumber, objectName, true))
					.append("</a><br/>\r\n")
					.append("</td>");
				
					objectIndex++;
			}
			objectsForSeriesBuilder.append("</tr>");
		}
		objectsForSeriesBuilder.append("</table>");
		
		return objectsForSeriesBuilder.toString();
	}
	
	/**
	 * Questo metodo genera l'elenco delle series per un determinato studio
	 * @param seriesUIDs identificativo univoco della serie
	 * @param study identificativo dello studio cui la serie risulta associata
	 * @return una stringa contenente html che mostra l'elenco delle series di uno studio
	 */
	private String getSeriesForStudy(List<String> seriesUIDs, String study) {
		log.info("Generating series for study...");
		
		StringBuilder seriesForStudyBuilder = new StringBuilder();
		List<String> seriesNumbers = new ArrayList<String>();
		List<String> seriesDescs = new ArrayList<String>();
		for (String series : seriesUIDs) {
			String seriesNumber = prm.getSeriesNumber(series);
			String seriesDesc = getSeriesDesc(series, study);
			List<String> objectIDs = getDicomIdsFromSeries(series, study);
			if (objectIDs == null || objectIDs.isEmpty()) {
				log.info("Empty series: " + seriesNumber + ", id: " + series);
				continue;
			}
			
			seriesNumbers.add(seriesNumber);
			seriesDescs.add(seriesDesc);
		}
		
		for (int seriesIndex = 0; seriesIndex < seriesNumbers.size(); seriesIndex++) {
			seriesForStudyBuilder.append("<a target=\"objectlist\" href=\"../SERIES/")
				.append(seriesNumbers.get(seriesIndex))
				.append(".HTM\">")
				.append(seriesDescs.get(seriesIndex))
				.append("</a><br/>\r\n");
		}
		
		return seriesForStudyBuilder.toString();
	}
	
	/**
	 * Questo metodo genera l'elenco delle series per un determinato studio
	 * @param seriesUIDs identificativo univoco della serie
	 * @param study identificativo dello studio cui la serie risulta associata
	 * @return una stringa contenente html che mostra l'elenco delle series di uno studio
	 */
	private void getSeriesForStudy(List<String> seriesUIDs, String study, LISTSERIES  listOfSeries) {
		log.info("Generating series for study...");
		System.out.println("Generating series for study..." + study);
		SERIESTYPE 	serie 		= new SERIESTYPE();
		OBJECTTYPE 	objectType 	= new OBJECTTYPE();
		PATIENTTYPE	paziente	= new PATIENTTYPE();
		SERIESTYPE.LISTOBJECT listOfObject = new LISTOBJECT();
		
		getStudyInfo(study);
		
		for (String series : seriesUIDs) {
			listOfObject = new LISTOBJECT();
			String seriesNumber = prm.getSeriesNumber(series);
			String seriesDesc = getSeriesDesc(series, study);
			List<InstanceDTO> objectIDs = getDicomObjectFromSeries(series, study);
			if (objectIDs == null || objectIDs.isEmpty()) {
				log.info("Empty series: " + seriesNumber + ", id: " + series);
				continue;
			} 
						
			serie = new SERIESTYPE();
			serie.setURI(seriesNumber+".HTM");
			serie.setSERIESDESC(seriesDesc);
			serie.setSERIESNUMBER(seriesNumber);
			serie.setSTUDYNUMBER(prm.getStudyNumber(study));
			serie.setURIFILE(getSeriesDir() + PATH_SEPARATOR + seriesNumber + ".HTM");
			
			if (objectIDs != null || !objectIDs.isEmpty()) {
				log.info("####START Not Empty series: " + seriesNumber + ", id: " + series + " number of object: " + objectIDs.size());
				
				serie.setMODALITY(objectIDs.get(0).getModalityOfSeries());
				InstanceDTO instance = null;
				for(int objectIndex = 1; objectIndex <= objectIDs.size(); objectIndex ++) {
					instance = new InstanceDTO();
					instance = objectIDs.get(objectIndex-1);
					
					log.info("Instance: " + instance.toString());
					
					objectType = new OBJECTTYPE();
					
					String objectName = String.format("%" + (8) + "s", "" + objectIndex).replace(" ", "0");
					
					log.info("objectName: " + objectName );
					int nextObjectIndex = objectIndex + 1;
					String nextObjectName = String.format("%" + (8) + "s", "" + nextObjectIndex).replace(" ", "0");
					log.info("nextObjectName: " + nextObjectName );
					int prevObjectIndex = objectIndex - 1;
					String prevObjectName = String.format("%" + (8) + "s", "" + prevObjectIndex).replace(" ", "0");
					
					log.info("prevObjectName: " + prevObjectName );
					objectType.setOBJECTNAME(objectName);
					objectType.setNEXTOBJECT(seriesNumber + "-" + nextObjectName + ".HTM");
					if(prevObjectIndex>0) {
						objectType.setPREVOBJECT(seriesNumber + "-" + prevObjectName + ".HTM");
					}
					objectType.setSTUDYNUMBER(prm.getStudyNumber(study));
					objectType.setSERIESNUMBER(seriesNumber);
					objectType.setSERIESDESC(seriesDesc);
					objectType.setURIFILE(getObjectsDir() + PATH_SEPARATOR + seriesNumber + "-" + objectName + ".HTM");
					objectType.setURI(seriesNumber + "-" + objectName + ".HTM");
					objectType.setNUMOBJECT(objectIDs.size());
					objectType.setOBJECTINDEX(objectIndex);
					
					if(instance != null) {
						objectType.setPHONOMETRIC(instance.getPhonometric());
						objectType.setSPACING(instance.getSpacing());
						if(StringUtils.isNotEmpty(instance.getSize())) {
							objectType.setSIZE(instance.getSize());
						}else {
							objectType.setSIZE(" ");
						}
						if(StringUtils.isNotEmpty(instance.getNumberOfFrames())) {
							objectType.setNUMBERFRAMES(Integer.valueOf(instance.getNumberOfFrames()));
						} else {
							objectType.setNUMBERFRAMES(0);
						}
												
						objectType.setROW(instance.getRow());
						objectType.setCOLUMN(instance.getColumn());
					}
					
					log.info("objectType completed " );
					
					if(patient != null) {
						// PATIENT
						paziente = new PATIENTTYPE();
						paziente.setPATIENTID(patient.getPatientId());
						paziente.setPATIENTFIRSTNAME(patient.getFirstName());
						paziente.setPATIENTMIDDLENAME(patient.getMiddleName());
						paziente.setPATIENTLASTNAME(patient.getLastName());
						paziente.setPATIENTBIRTHDAY(patient.getBirth());
						paziente.setSTUDYDESCRIPTION(studyDescription);
						paziente.setACCESSIONNUMBER(accessionNumber);
						paziente.setSERIESDESC(seriesDesc);
					
						objectType.setPATIENT(paziente);
						
						log.info("paziente completed " );
					}
					
					
					listOfObject.getOBJECT().add(objectType);
					serie.setLISTOBJECT(listOfObject);
					
					log.info("object  added " );
					
				}
				if(serie.getLISTOBJECT()!=null && serie.getLISTOBJECT().getOBJECT() != null && serie.getLISTOBJECT().getOBJECT().size() > 0) {
					
					if(serie.getLISTOBJECT().getOBJECT().size() > 1) {
						serie.setFIRSTOBJECT(serie.getLISTOBJECT().getOBJECT().get(0).getURI());
						int lastPos = serie.getLISTOBJECT().getOBJECT().size() -1;
						serie.setLASTOBJECT(serie.getLISTOBJECT().getOBJECT().get(lastPos).getURI());
					}
				}
			}
			
			
			
			if(listOfSeries != null) {
				log.info("listOfSeries  != null " );
				listOfSeries.getSERIES().add(serie);
			}
			log.info("####END Not Empty series: " + seriesNumber + ", id: " + series + " number of object: " + objectIDs.size());
			log.info("serie  added " );
		}
		
		this.studyDescription = null;
		this.accessionNumber = null;
		
	}
	
	/**
	 * Questo metodo genera la lista degli studi
	 * @return una stringa contenente l'html che rappresenta la lista degli studi
	 */
	private String getStudiesForToc() {
		log.info("Generating studies for toc...");
		
		StringBuilder studiesForTocBuilder = new StringBuilder();
		for (String study : studyUIDs) {
			String studyNumber = prm.getStudyNumber(study);
			getStudyInfo(study);
			studiesForTocBuilder.append("<a target=\"serieslist\" href=\"../PAGES/STUDIES/")
				.append(studyNumber)
				.append(".HTM\">")
				.append(studyDescription)
				.append("</a><br/>\r\n");
		}
		
		this.studyDescription = null;
		this.accessionNumber = null;
		
		return studiesForTocBuilder.toString();
	}
	
	/**
	 * Questo metodo genera la lista degli studi
	 * @return una stringa contenente l'html che rappresenta la lista degli studi
	 */
	private void getStudiesForToc(List<LINKSTUDYFORTOC> listOfStudy) {
		log.info("Generating studies for toc...START");
		LINKSTUDYFORTOC link = new LINKSTUDYFORTOC();
		StringBuilder studiesForTocBuilder = new StringBuilder();
		for (String study : studyUIDs) {
			log.info("studies for toc...ID:" + study);
			String studyNumber = prm.getStudyNumber(study);
			getStudyInfo(study);
			link = new LINKSTUDYFORTOC();
			link.setURI(studyNumber+".HTM");
			link.setSTUDYDESCRIPTION(studyDescription);
			
			listOfStudy.add(link);
		}
		log.info("Generating studies for toc...END");
		this.studyDescription = null;
		this.accessionNumber = null;
		
	}
	
	/**
	 * Questo metodo recupera la descrizione e l'accession number di uno studio
	 * @param study identificativo univoco dello studio
	 */
	private void getStudyInfo(String study) {
		String[] studyInfo = getStudyInfoFromStudy(study);
		this.studyDescription = studyInfo[0];
		this.accessionNumber = studyInfo[1];
	}
	

	
	/**
	 * Questo metodo crea le varie directory necessarie alla generazione della view
	 */
	private void makeDirs() {
		generateAssets(prm.getConfigParamPDI("Assets"), getAssetsDir());

		log.info("Making directories....");
		StreamUtils.makeDir(getPagesDir(), PAGES);
		StreamUtils.makeDir(getStudiesDir(), STUDIES);
		StreamUtils.makeDir(getSeriesDir(), SERIES);
		StreamUtils.makeDir(getObjectsDir(), OBJECTS);
	}
	

	
	/**
	 * Si occupa di trasferire gli asset nella directory di riferimento
	 * @param assetsFrom la directory da cui recuperare gli asset
	 * @param assetsTo la directory in cui trasferire gli asset
	 */
	private void generateAssets(String assetsFrom, String assetsTo) {
		StreamUtils.makeDir(assetsTo, ASSETS);
		
		log.info("Copying assets from " + assetsFrom + " to " + assetsTo +  "...");
		try {
			String aslFolder = css.substring(0, css.lastIndexOf("/") + 1);
			File dir = new File(assetsFrom + aslFolder);
			List<String> paths = Arrays.asList(dir.list());
			log.info("Paths: " + paths);
			File dirto = new File(assetsTo);
		
			
  		  	FileUtils.copyDirectory(dir, dirto);
  		  	
			String xsltName = xslt.substring(xslt.lastIndexOf("/")+1);
			
			File xsltDest = new File(dirto.getAbsolutePath() + File.separator + xsltName);
			
			System.out.println(xsltDest.getAbsolutePath());
			xsltDest.delete();

			
		} catch (Exception e) {
			log.error("Cannot copy assets due to: " + e.getMessage(), e);
		} 
		log.info("Assets copyed");
	}
	
	/**
	 * Metodo utile al recupero del path della prima immagine da mostrare nella view
	 * @return stringa contenente la parte finale del path per il recupero dell'immagine
	 */
	private String getPathFirtImg() {
		StringBuilder pathFirtsImg = new StringBuilder();
		List<String> seriesNumbers = new ArrayList<String>();
		
		String study = studyUIDs.get(0);
		List<String> seriesList = getSeriesIdsFromStudy(study);
		for(String series : seriesList) {
			seriesNumbers.add(prm.getSeriesNumber(series));
		}
		
		pathFirtsImg.append(seriesNumbers.get(0))
			.append("-")
			.append("00000001")
			.append(".HTM");
		
		return pathFirtsImg.toString();
 	}
	
	/**
	 * Metodo utile al recupero del path della prima series da mostrare nella view
	 * @return stringa contenente la parte finale del path per il recupero della serie
	 */
	private String getPathFirtSeries() {
		StringBuilder pathFirtsSeries = new StringBuilder();
		List<String> seriesNumbers = new ArrayList<String>();
		
		String study = studyUIDs.get(0);
		List<String> seriesList = getSeriesIdsFromStudy(study);
		for(String series : seriesList) {
			seriesNumbers.add(prm.getSeriesNumber(series));
		}
		
		pathFirtsSeries.append(seriesNumbers.get(0)).append(".HTM");
		
		return pathFirtsSeries.toString();
 	}
	
	/**
	 * Metodo utile al recupero del path del primo studio da mostrare nella view
	 * @return stringa contenente la parte finale del path per il recupero dello studio
	 */
	private String getPathFirtStudy() {
		StringBuilder pathFirtsStudy = new StringBuilder();
		String study = studyUIDs.get(0);
		String studyNumber = prm.getStudyNumber(study);
		pathFirtsStudy.append(studyNumber).append(".HTM");
		
		return pathFirtsStudy.toString();
 	}
	
	/**
     * Metodo che permette di recuperare gli identificativi delle serie in uno studio
     * @param studyUID identificativo dello studio
     * @param seriesUID identificativo dello serie
     * @return lista di identificativi recuperati
     */
	private List<String> getDicomIdsFromSeries(String seriesUID, String studyUID) {
		List<String> dicoms = new ArrayList<String>();
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
					
					break;
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
     * Metodo che permette di recuperare gli identificativi delle serie in uno studio
     * @param studyUID identificativo dello studio
     * @param seriesUID identificativo dello serie
     * @return lista di identificativi recuperati
     */
	private List<InstanceDTO> getDicomObjectFromSeries(String seriesUID, String studyUID) {
		List<InstanceDTO> dicoms = new ArrayList<InstanceDTO>();
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
							dicoms = getDicomsObjectBySeries(line);
						}
					} 
					
					break;
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
     * Metodo che permette di recuperare gli identificativi delle DICOM in una Serie
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
     * Metodo che permette di recuperare gli identificativi delle DICOM in una Serie
     * @return lista di identificativi recuperati
     */
	private List<InstanceDTO> getDicomsObjectBySeries(String line) {
		List<InstanceDTO> listOfInstance = new ArrayList<InstanceDTO>();
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		InstanceDTO instance	= new InstanceDTO();
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
			String modality = null;
			if(nLst != null && nLst.item(0) != null) {
				modality = ((Element) nLst.item(0)).getAttribute("modality");
			}
			NodeList instanceList = doc.getElementsByTagName("instance");
			for (int instanceIndex = 0; instanceIndex < instanceList.getLength(); instanceIndex++) {
				log.info("Current Element: " + instanceList.item(instanceIndex).getNodeName());
				eElement = (Element) instanceList.item(instanceIndex);
				if(eElement != null) {
					instance = new InstanceDTO();
					instance.setObjectUID(eElement.getAttribute("uid"));
					instance.setModalityOfSeries(modality);
					instance.setPhonometric(eElement.getAttribute("photometricInterpretation"));
					String column = eElement.getAttribute("columns") != null ? eElement.getAttribute("columns") : "";
					String row = eElement.getAttribute("rows") != null ? eElement.getAttribute("rows") : "";
					if(StringUtils.isNotEmpty(row) && StringUtils.isNotEmpty(column)) {
						instance.setSize(column + "x" + row);
					}
					String numberOfFrames = eElement.getAttribute("numberOfFrames") != null ? eElement.getAttribute("numberOfFrames")  : "";
					instance.setNumberOfFrames(numberOfFrames);
					
					if(StringUtils.isNotEmpty(row)) {
						instance.setRow(Integer.valueOf(row));
					} else {
						instance.setRow(0);
					}
					

					if(StringUtils.isNotEmpty(row)) {
						instance.setColumn(Integer.valueOf(column));
					} else {
						instance.setColumn(0);
					}
										
					NodeList spacingLst = doc.getElementsByTagName("pixelSpacing");
					if(spacingLst !=null && spacingLst.item(0) != null) {
						String rowSpacing = null;
						String columnSpacing = null;
						
						columnSpacing = ((Element) spacingLst.item(0)).getAttribute("columnSpacing");
						rowSpacing = ((Element) spacingLst.item(0)).getAttribute("rowSpacing");
						
						instance.setSpacing(columnSpacing + "x" + rowSpacing); 
					}
					
					listOfInstance.add(instance);
				}
				
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
		
		return listOfInstance;
	}
	
	/**
	 * Fornisce l'URL del servizio getInstance per l'ottenimento degli id delle dicom
	 * @param seriesUID l'identificativo della serie
	 * @param studyUID l'identificativo dello studio
	 * @return l'URL del servizio getInstance
	 */
	private String getGetInstanceUrl(String seriesUID, String studyUID) {
		String url = new StringBuilder(this.url)
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
     * Metodo che permette di recuperare gli identificativi delle serie in uno studio
     * @param studyUID identificativo dello studio
     * @return lista di identificativi recuperati
     */
	private List<String> getSeriesIdsFromStudy(String studyUID) {	
		List<String> seriesUIDs = new ArrayList<String>();
		
		int code = 0;
		String messCode = "";
		log.info("Calling /getPatientInfo...");
		int retries = Integer.parseInt(this.retries);
		for (int retry = 1; retry <= retries; retry++) {
			String patientUrl = getPatientInfoUrl(studyUID);
			try {
				URL urlConn = new URL(patientUrl);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + basicAuth);
				code = ((HttpURLConnection) conn).getResponseCode();
				messCode = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for getPatient: " + code + ", message: " + messCode);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							seriesUIDs = getSeriesFromPatient(line, studyUID);
						}
					} 

					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /getPatientInfo fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/getPatientInfo going to sleep... " + retry);
					StreamUtils.waitForRetry(ConfigurationSettings.SLEEP);
					if (retry == retries) {
						log.info("Nessun risultato, patientInfo count: " + retry);
						return null;
					} else {
						log.info("Riprovare la chiamata al servizio patientInfo, count: " + retry);
					}
				}
			}
		}
		
		return seriesUIDs;
	}
	
	/**
     * Metodo che permette di recuperare gli identificativi delle serie in uno studio dalle info del paziente
     * @param studyUID identificativo dello studio
     * @return lista di identificativi recuperati
     */
	private List<String> getSeriesFromPatient(String line, String studyUID) {
		List<String> seriesUIDs = new ArrayList<String>();

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
						NodeList seriesList = study.getElementsByTagName("series");
						for (int seriesIndex = 0; seriesIndex < seriesList.getLength(); seriesIndex++) {
							Element series = (Element) seriesList.item(seriesIndex);
							seriesUIDs.add(series.getAttribute("uid"));
						}
					}
				}
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

		return seriesUIDs;
	}
	
	/**
	 * Metodo che permette il recupero della descrizione di una serie
	 * @param seriesUID identificativo della serie
	 * @param studyUID identificativo dello studio
	 * @return descrizione della serie
	 */
	private String getSeriesDesc(String seriesUID, String studyUID) {	
		String description = "";
		
		int code = 0;
		String messCode = "";
		log.info("Calling /getPatientInfo...");
		int retries = Integer.parseInt(this.retries);
		for (int retry = 1; retry <= retries; retry++) {
			String patientUrl = getPatientInfoUrl(studyUID);
			try {
				URL urlConn = new URL(patientUrl);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + basicAuth);
				code = ((HttpURLConnection) conn).getResponseCode();
				messCode = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for getPatient: " + code + ", message: " + messCode);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							description = getSeriesDescFromPatient(line, studyUID, seriesUID);
						}
					} 

					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /getPatientInfo fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/getPatientInfo going to sleep... " + retry);
					StreamUtils.waitForRetry(ConfigurationSettings.SLEEP);
					if (retry == retries) {
						log.info("Nessun risultato, patientInfo count: " + retry);
						return null;
					} else {
						log.info("Riprovare la chiamata al servizio patientInfo, count: " + retry);
					}
				}
			}
		}
		
		
		return description;
	}
	
	/**
	 * Metodo che permette il recupero della descrizione di una serie dalle info del paziente
	 * @param seriesUID identificativo della serie
	 * @param studyUID identificativo dello studio
	 * @return descrizione della serie
	 */
	private String getSeriesDescFromPatient(String line, String studyUID, String seriesUID) {
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
						NodeList seriesList = study.getElementsByTagName("series");
						for (int seriesIndex = 0; seriesIndex < seriesList.getLength(); seriesIndex++) {
							Element series = (Element) seriesList.item(seriesIndex);
							String seriesToCompare = series.getAttribute("uid");
							if (seriesUID.equals(seriesToCompare)) {
								return series.getAttribute("description");
							}
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
			log.error("Impossibile analizzare il risultato...", e);
		} catch (SAXException e) {
			log.error("Impossibile analizzare il file di configurazione! ", e);
		} catch (Exception oex) {
			log.error("Impossibile caricare il file di configurazione...", oex);
		}
		return null;
	}
	
	/**
	 * Metodo che restituisce le informazioni dello studio
	 * @param studyUID identificativo dello studio
	 * @return informazioni dello studio
	 */
	private String[] getStudyInfoFromStudy(String studyUID) {	
		int code = 0;
		String messCode = "";
		log.info("Calling /getPatientInfo...");
		int retries = Integer.parseInt(this.retries);
		for (int retry = 1; retry <= retries; retry++) {
			String patientUrl = getPatientInfoUrl(studyUID);
			try {
				URL urlConn = new URL(patientUrl);
				URLConnection conn = urlConn.openConnection();
				((HttpURLConnection) conn).setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic " + basicAuth);
				code = ((HttpURLConnection) conn).getResponseCode();
				messCode = ((HttpURLConnection) conn).getResponseMessage();
				log.info("Code for getPatient: " + code + ", message: " + messCode);
				if (code == 200) {
					if (conn != null && conn.getInputStream() != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							return getStudyInfoFromPatient(line, studyUID);
						}
					} 

					break;
				} 
			} catch (Exception e) {
				log.error("Chiamata servlet /getPatientInfo fallita: " + e.getMessage(), e);
			} finally {
				if (code != 200) {
					log.info("/getPatientInfo going to sleep... " + retry);
					StreamUtils.waitForRetry(ConfigurationSettings.SLEEP);
					if (retry == retries) {
						log.info("Nessun risultato, patientInfo count: " + retry);
						return null;
					} else {
						log.info("Riprovare la chiamata al servizio patientInfo, count: " + retry);
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Metodo che restituisce le informazioni dello studio dalle info del paziente
	 * @param studyUID identificativo dello studio
	 * @return informazioni dello studio
	 */
	private String[] getStudyInfoFromPatient(String line, String studyUID) {
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
						String desc = study.getAttribute("description");
						String accessionNumber = study.getAttribute("accessionNumber");
						return new String[] {desc, accessionNumber};
					}
				}
			}
		} catch (ParserConfigurationException e) {
			log.error("Impossibile analizzare il risultato...", e);
		} catch (SAXException e) {
			log.error("Impossibile analizzare il file di configurazione! ", e);
		} catch (Exception oex) {
			log.error("Impossibile caricare il file di configurazione...", oex);
		}
		return null;
	}
	
	/**
	 * Fornisce l'URL del servizio patientInfo
	 * @param studyUID l'identificativo dello studio
	 * @return l'URL del servizio patientInfo
	 */
	private String getPatientInfoUrl(String studyUID) {
		String url = new StringBuilder(this.url)
			.append("/o3-dpacs-wado/getPatientInfo")
			.append("?studyUID=")
			.append(studyUID)
			.append("&showOnlyThis=true")
			.toString();
		
		log.info("Url di chiamata: " + url);
		
		return url;
	}
	
	/**
	 * Fornisce il percorso della directory degli asset
	 * @return il percorso della directory degli asset
	 */
	private String getAssetsDir() {
		return new StringBuilder(dist)
				.append(PATH_SEPARATOR)
				.append(IHE_PDI)
				.append(PATH_SEPARATOR)
				.append(ASSETS)
				.append(PATH_SEPARATOR)
				.toString();
	}
	
	/**
	 * Fornisce il percorso della directory delle immagini
	 * @return il percorso della directory delle immagini
	 */
	private String getObjectsDir() {
		return new StringBuilder(dist)
				.append(PATH_SEPARATOR)
				.append(IHE_PDI)
				.append(PATH_SEPARATOR)
				.append(PAGES)
				.append(PATH_SEPARATOR)
				.append(OBJECTS)
				.toString();
	}

	/**
	 * Fornisce il percorso della directory delle serie
	 * @return il percorso della directory delle serie
	 */
	private String getSeriesDir() {
		return new StringBuilder(dist)
				.append(PATH_SEPARATOR)
				.append(IHE_PDI)
				.append(PATH_SEPARATOR)
				.append(PAGES)
				.append(PATH_SEPARATOR)
				.append(SERIES)
				.toString();
	}

	/**
	 * Fornisce il percorso della directory degli studi
	 * @return il percorso della directory degli studi
	 */
	private String getStudiesDir() {
		return new StringBuilder(dist)
				.append(PATH_SEPARATOR)
				.append(IHE_PDI)
				.append(PATH_SEPARATOR)
				.append(PAGES)
				.append(PATH_SEPARATOR)
				.append(STUDIES)
				.toString();
	}

	/**
	 * Fornisce il percorso della directory delle pagine
	 * @return il percorso della directory delle pagine
	 */
	private String getPagesDir() {
		return new StringBuilder(dist)
				.append(PATH_SEPARATOR)
				.append(IHE_PDI)
				.append(PATH_SEPARATOR)
				.append(PAGES)
				.toString();
	}
}