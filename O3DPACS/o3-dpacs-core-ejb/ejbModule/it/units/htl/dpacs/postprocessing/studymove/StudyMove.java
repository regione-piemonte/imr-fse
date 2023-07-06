/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.studymove;

import it.units.htl.dpacs.helpers.DateHelper;

import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

public class StudyMove implements StudyMoveMBean{

	private boolean serviceStatus;
	private Logger log = Logger.getLogger(StudyMove.class);
	private Element configParam = null;
	private Timer workerTimer = new Timer();
	
	private static final String SERVICETAG_ROOT="configuration";
	private static final String SERVICETAG_WORKERTYPE="worker";
	private static final String SERVICETAG_STARTDATE="startDate";
	private static final String SERVICETAG_STARTTIME="startTime";
	private static final String SERVICETAG_PERIOD="periodInMinutes";
	private static final String SERVICETAG_MINIMUMLIFE="minimumLifeInHours";
	private static final String SERVICETAG_QUERYTIMEOUTINS="queryTimeoutInS";
	
	private static final String SERVICE_NAME = "StudyMove";

	
	private static final String FORMAT_DATE="dd-MM-yyyy";
	private static final String FORMAT_TIME="HH:mm";
	
	@Override
	public boolean startService() throws Exception, UnsupportedOperationException {
		log.info("startService StudyMove...");
		if (!serviceStatus) {
			log.info("startService StudyMove Not Service Status...");
			if (loadConfigs()) {
				log.info("startService StudyMove Ok Config...");
				workerTimer = new Timer();
				
				String workerType= getConfigParam(SERVICETAG_WORKERTYPE);
				String startDateString= getConfigParam(SERVICETAG_STARTDATE);
				String startTimeString= getConfigParam(SERVICETAG_STARTTIME);
				String periodString= getConfigParam(SERVICETAG_PERIOD);
				String minimumLifeString= getConfigParam(SERVICETAG_MINIMUMLIFE);
				String timeoutString= getConfigParam(SERVICETAG_QUERYTIMEOUTINS);

				
				Date startDate=null;
				Date startTime=null;
				
				if(startDateString!=null){
					SimpleDateFormat sdf=new SimpleDateFormat(FORMAT_DATE);
					startDate=sdf.parse(startDateString);
				}
				
				if(startTimeString!=null){
					SimpleDateFormat sdf=new SimpleDateFormat(FORMAT_TIME);
					startTime=sdf.parse(startTimeString);
				}
				
				Date startingDate=getNextUsefulStartDate(startDate, startTime);
				
				long period=Long.parseLong(periodString)*60*1000;	// Minutes into millis
				int minimumLife=Integer.parseInt(minimumLifeString);
				int timeout = Integer.parseInt(timeoutString);
				
				// Schedule the task
				StudyMoveWorker worker=StudyMoveWorkerFactory.getInstance(workerType,minimumLife,timeout);
				if(worker!=null){
					workerTimer.scheduleAtFixedRate(worker, startingDate, period);
					log.info("StudyMove service ("+workerType+") scheduled at "+startingDate+" every "+period+" minutes");
					serviceStatus = true;
					return true;
				}else{
					log.error("StudyMove service ("+workerType+") could not be instantiated");
					serviceStatus = false;
					return false;
				}
			} else {
				log.info("startService StudyMove Config Not Found");
				serviceStatus = false;
				return false;
			}
		} else {
			log.info("StudyMove loaded correctly");
			return true;
		}
		
		
		
	}

/*	private Date getNextUsefulStartDate(Date startDate, Date startTime) {
		int year=0;
		int month=0;
		int day=0;
		int hour=0;
		int minute=0;
		Calendar cal=Calendar.getInstance();
		//cal.add(Calendar.DAY_OF_MONTH, 1);
		if(startDate!=null){
			Calendar calStart=Calendar.getInstance();
			calStart.setTime(startDate);
			if(cal.before(calStart)){
				cal.setTime(startDate);
			}
		}
		year=cal.get(Calendar.YEAR);
		month=cal.get(Calendar.MONTH);
		day=cal.get(Calendar.DAY_OF_MONTH);
		cal=null;
		cal=Calendar.getInstance();
		if(startTime!=null){
			cal.setTime(startTime);
			hour=cal.get(Calendar.HOUR_OF_DAY);
			minute=cal.get(Calendar.MINUTE);
		}else{
			hour=0;
			minute=1;
		}
		Calendar ret=new GregorianCalendar(year, month, day, hour, minute);
		if(ret.before(Calendar.getInstance()))
			ret.add(Calendar.DAY_OF_MONTH, 1);
		return ret.getTime();
		
	}
*/
	
	private Date getNextUsefulStartDate(Date startDate, Date startTime) {
		Calendar cal=Calendar.getInstance();
		Calendar calStart=Calendar.getInstance();
		
		if(startDate!=null) {
			calStart.setTime(startDate);
		}

		if(startTime!=null) {
			Calendar calTime = Calendar.getInstance();
			calTime.setTime(startTime);
			calStart.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
			calStart.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
		}
		
		return calStart.before(cal) ? cal.getTime() : calStart.getTime();
	}
	

	@Override
	public boolean stopService() throws Exception, UnsupportedOperationException {
		if (serviceStatus) {
			workerTimer.cancel();
			workerTimer.purge();
			workerTimer = null;
		}
		serviceStatus = false;
		return true;
	}

	@Override
	public boolean statusService() {
		return serviceStatus;
	}

	@Override
	public boolean reloadSettings() throws Exception, UnsupportedOperationException {
		if (serviceStatus) {
			stopService();
		}
		return startService();
	}
	
	private boolean loadConfigs() throws ConfigurationException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		ServicesConfigurationHome sch = new ServicesConfigurationHome();
		try {
			ServicesConfiguration sc = sch.findByServiceName(SERVICE_NAME);
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			StringReader reader = new StringReader(sc.getConfiguration());
			InputSource is = new InputSource(reader);
			Document doc = docBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName(SERVICETAG_ROOT);
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
