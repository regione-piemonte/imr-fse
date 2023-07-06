/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.movement;

import it.units.htl.dpacs.deletion.StudyEraserMBean;
import it.units.htl.dpacs.deletion.StudyEraserWorker;
import it.units.htl.dpacs.helpers.DateHelper;
import it.units.htl.dpacs.postprocessing.verifier.StudiesVerifyWorker;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

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

public class StudyMovement implements StudyMovementMBean{

	private boolean serviceStatus;
	private Logger log = Logger.getLogger(StudyMovement.class);
	private Element configParam = null;
	private Timer workerTimer = new Timer();
	
	private static final String SERVICETAG_ROOT="configuration";
	private static final String SERVICETAG_STARTDATE="startDate";
	private static final String SERVICETAG_STARTTIME="startTime";
	private static final String SERVICETAG_PERIOD="timerPeriod";
	private static final String SERVICETAG_WORKERCLASS="timerClass";
	private static final String SERVICETAG_MOVENEARLINE="toNearline";
	private static final String SERVICETAG_MOVEOFFLINE="toOffline";
	private static final String SERVICETAG_DAYSONLINE="daysToKeepOnline";
	private static final String SERVICETAG_DAYSNEARLINE="daysToKeepNearline";
	private static final String SERVICETAG_ADDITIONALCONFIG="specificWorkerConfig";
	
	private static final int DEFAULT_DAYS=365;

	private static final String MOVEMENT_ENABLED="TRUE"; 
	
	private static final String SERVICE_NAME = "StudyMovement";
	
	private static final String FORMAT_DATE="dd-MM-yyyy";
	private static final String FORMAT_TIME="HH:mm";
	
	@Override
	public boolean startService() throws Exception, UnsupportedOperationException {
		
		if (!serviceStatus) {
			if (loadConfigs()) {
				workerTimer = new Timer();
				String startDateString= getConfigParam(SERVICETAG_STARTDATE);
				String startTimeString= getConfigParam(SERVICETAG_STARTTIME);
				String periodString= getConfigParam(SERVICETAG_PERIOD);
				String type=getConfigParam(SERVICETAG_WORKERCLASS);
				NodeList additionalConfig=getConfigParams(SERVICETAG_ADDITIONALCONFIG);
				Boolean isNearline=MOVEMENT_ENABLED.equalsIgnoreCase(getConfigParam(SERVICETAG_MOVENEARLINE));
				Boolean isOffline=MOVEMENT_ENABLED.equalsIgnoreCase(getConfigParam(SERVICETAG_MOVEOFFLINE));
				
				Integer daysOnline=null;
				String temp=getConfigParam(SERVICETAG_DAYSONLINE);
				try{
					daysOnline=Integer.parseInt(temp);
					if(daysOnline<=0) throw new NumberFormatException("Negative numbers not allowed");
				}catch(Exception ex){
					daysOnline=DEFAULT_DAYS;
					log.error("Error parsing "+SERVICETAG_DAYSONLINE+": "+ex.getMessage());
				}
				Integer daysNearline=null;
				temp=getConfigParam(SERVICETAG_DAYSNEARLINE);
				try{
					daysNearline=Integer.parseInt(temp);
					if(daysNearline<0) throw new NumberFormatException("Negative numbers not allowed");
				}catch(Exception ex){
					daysNearline=DEFAULT_DAYS;
					log.error("Error parsing "+SERVICETAG_DAYSNEARLINE+": "+ex.getMessage());
				}
				
				if(!isNearline){
					isOffline=false;
				}
				
				
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
				
				// Schedule the task: default configuration uses BasicStudyMovementWorker
				StudyMovementWorker worker=StudyMovementWorkerFactory.getInstance(type, isNearline, isOffline, daysOnline, daysNearline, additionalConfig);
				
				workerTimer.scheduleAtFixedRate(worker, startingDate, period);
				log.info("StudyMovement timer scheduled at "+startingDate+" every "+periodString+" minutes");
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
			
			String runFromIp=sc.getRunFromIp();
			if(runFromIp!=null && (!"".equals(runFromIp))){	// If only one node has to run this service, check whether this node is that one
				boolean schedule=false;
				
				try{
		        	Enumeration<NetworkInterface> interfaces=NetworkInterface.getNetworkInterfaces();
		        	if(interfaces!=null){ 
		        		NetworkInterface i=null;
			        	while(interfaces.hasMoreElements() && (!schedule)){
			        		i=interfaces.nextElement();
			        		Enumeration<InetAddress> addresses=i.getInetAddresses();
			        		if(addresses!=null){
			        			InetAddress a=null;
			        			while (addresses.hasMoreElements() && (!schedule)){
			        				a=addresses.nextElement();
			        				if(runFromIp.equals(a.getHostAddress())){
			        					schedule=true;
			        					break;
			        				}
			        			}
			        		}
			        	}
		        	}
		        }catch(Exception ex){
		        	schedule=false;
		        }
				
				if(!schedule){
					log.info("Service scheduled to run on "+runFromIp+", NOT this node");
					return schedule; 	// 		If this is not the node to run the service, do not load configuration
				}
				
			}
			
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

	private NodeList getConfigParams(String paramName) {
		NodeList fstNmElmntLst = configParam.getElementsByTagName(paramName);
		if ((fstNmElmntLst == null) || (fstNmElmntLst.getLength() == 0))
			return null;
		Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
		return fstNmElmnt.getChildNodes();
	}
	
}
