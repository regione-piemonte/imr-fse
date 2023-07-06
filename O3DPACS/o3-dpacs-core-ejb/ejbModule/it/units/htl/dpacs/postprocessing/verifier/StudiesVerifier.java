/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier;

import it.units.htl.dpacs.helpers.DateHelper;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import javax.naming.ConfigurationException;
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

public class StudiesVerifier implements StudiesVerifierMBean {
	private static final String FORMAT_TIME = "HH:mm";
	// ServiceName : StudiesVerifier
	private boolean serviceStatus;
	//	private Log log = LogFactory.getLog(StudiesVerifier.class);
	private Logger log = Logger.getLogger(StudiesVerifier.class);

	private Element configParam = null;
	private Timer workerTimer = new Timer();

	public boolean statusService() {
		return serviceStatus;
	}

	public boolean reloadSettings() throws Exception, UnsupportedOperationException {
		if (serviceStatus) {
			stopService();
		}
		return startService();
	}

	public boolean startService() throws Exception, UnsupportedOperationException {
		if (!serviceStatus) {
			if (loadConfigs()) {
				workerTimer = new Timer();
				long firstDelayInMs = Long.parseLong(getConfigParam("firstDelayInS")) * 1000;
				long repeatEachInMs = Long.parseLong(getConfigParam("repeatEachInS")) * 1000;
				long thresholdInS = Long.parseLong(getConfigParam("thresholdInS"));
				
				DateFormat df= new SimpleDateFormat(FORMAT_TIME);
				String reportTimeString=getConfigParam("reportTime");
				Date reportTime=null;
				if(reportTimeString != null){
					try{
						reportTime=df.parse(reportTimeString);
					}catch(Exception ex){
						log.warn("An error occurred converting reportTime: "+ex.getMessage());
						reportTime=null;
					}
				}
				
				String lang = getConfigParam("EmailMessagesLanguage");
				String xdsPrefix = getConfigParam("attributesPrefix");
				if (xdsPrefix != null)
					xdsPrefix = xdsPrefix.trim();

				String getFolderBy = getConfigParam("getFolderByTag");
				if (getFolderBy != null) {
					if (!(getFolderBy.equals("AccessionNumber") || !getFolderBy.equals("StudyInstanceUID"))) {
						getFolderBy = "AccessionNumber";
					}
				} else {
					getFolderBy = "AccessionNumber";
				}

				int queryTimeoutInS = Integer.parseInt(getConfigParam("queryTimeoutInS"));
				String firstExecutionTime = getConfigParam("firstExecutionTime");

				Date scheduledDate = null;
				if (firstExecutionTime != null) {
					//					scheduledDate = parseFirstExecutionTime(firstExecutionTime);
					scheduledDate = DateHelper.getFirstUsefulDate(firstExecutionTime);
				} else {
					log.warn("missing firstExecutionTime parameter in StudyVerifier service");
				}
				
				String maxConnectionsPerHostParam = getConfigParam("maxConnectionsPerHost");
				int maxConnectionsPerHost = 20;
				if((maxConnectionsPerHostParam != null) && (maxConnectionsPerHostParam.trim().length()>0)){
					maxConnectionsPerHost = Integer.parseInt(maxConnectionsPerHostParam);
				}
				
				// Schedule the task
				StudiesVerifyWorker w = new StudiesVerifyWorker(thresholdInS, lang, queryTimeoutInS, xdsPrefix, getFolderBy);
				w.setAxisClientMaxConnections(maxConnectionsPerHost);
				w.setReportTime(reportTime);
				if (scheduledDate != null) {
					log.info("StudyVerifier scheduled for " + scheduledDate + ", repeating every " + (repeatEachInMs / 1000) + "s");
					workerTimer.scheduleAtFixedRate(w, scheduledDate, repeatEachInMs);
				} else {
					log.info("StudyVerifier scheduled in " + (firstDelayInMs / 1000) + "s, repeating every " + (repeatEachInMs / 1000) + "s");
					workerTimer.scheduleAtFixedRate(w, firstDelayInMs, repeatEachInMs);
				}

				serviceStatus = true;
				return true;
			} else {
				serviceStatus = false;
				return false;
			}
		} else {
			return true;
		}
	}

	public boolean stopService() throws Exception, UnsupportedOperationException {
		if (serviceStatus) {
			workerTimer.cancel();
			workerTimer.purge();
			workerTimer = null;
		}
		serviceStatus = false;
		return true;
	}

	// private methods
	private boolean loadConfigs() throws ConfigurationException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		ServicesConfigurationHome sch = new ServicesConfigurationHome();
		try {
			ServicesConfiguration sc = sch.findByServiceName("StudiesVerifier");
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			StringReader reader = new StringReader(sc.getConfiguration());
			InputSource is = new InputSource(reader);
			Document doc = docBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("configuration");
			Node _rootNode = nodeLst.item(0);
			configParam = (Element) _rootNode;
		} catch (ParserConfigurationException e) {
			log.error("Unable to parse configuration...", e);
			return false;
		} catch (IOException e) {
			log.error("Couldn't open config file!", e);
			return false;
		} catch (SAXException e) {
			log.error("Couldn't parse config file!", e);
			return false;
		} catch (Exception oex) {
			log.warn("Unable to load the configuration...", oex);
			return false;
		}
		return true;
	}

	private String getConfigParam(String paramName) {
		NodeList fstNmElmntLst = configParam.getElementsByTagName(paramName);
		if ((fstNmElmntLst == null) || (fstNmElmntLst.getLength() == 0))
			return null;
		Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
		NodeList fstNm = fstNmElmnt.getChildNodes();
		Node node = fstNm.item(0);
		if (node != null) {
			return node.getNodeValue();
		} else {
			return null;
		}
	}

}
