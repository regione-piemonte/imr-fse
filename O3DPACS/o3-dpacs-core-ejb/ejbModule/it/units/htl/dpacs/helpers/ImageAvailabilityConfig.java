/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ImageAvailabilityConfig {
	
	private static ImageAvailabilityConfig conf=null;	// The singleton
	private static boolean toReset=true;
	static final Log log = LogFactory.getLog(ImageAvailabilityConfig.class);
	
	private static final String SERVICENAME="ImageAvailability";
	private static final String CONFIG_ROOT="ImageAvailability";
	private static final String CONFIG_PUBLISHTO="PublishTo";
	private static final String CONFIG_TABLE="Table";
	private static final String CONFIG_REMOVESTRING="RemoveString";
	private static final String CONFIG_SETSTRING="SetString";
	private static final String CONFIG_RISAPPLICATIONENTITY="RisApplicationEntity";
	private static final String CONFIG_TARGETAPP="TargetApp";
	
	public static final String PUBLISHTO_TABLE = "T";
	public static final String PUBLISHTO_IHE = "I";
	public static final String PUBLISHTO_HL7 = "HL7";
	
	public static final String RECONCILIATIONSOURCE_MANUAL="M";
	public static final String RECONCILIATIONSOURCE_ADT="A";
	public static final String RECONCILIATIONSOURCE_WORKLIST="W";
	public static final String RECONCILIATIONSOURCE_RIS="R";
	
	private static final String EMPTY_STRING="";
	
	private boolean enabled;
	private String publicationMethod;
	private String stringForRemoving;
	private String stringForSetting;
	private String appEntityForRis;
	private String targetApp;
	private static String runFromIp = null;
	
	
	private ImageAvailabilityConfig(){
		toReset=false;
		this.enabled=false;
	}

	private static void init(){
		conf=new ImageAvailabilityConfig();
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        try { 
            ServicesConfiguration sc = sch.findByServiceName(SERVICENAME);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            StringReader reader = new StringReader(sc.getConfiguration());
            InputSource is = new InputSource(reader);
            Document doc = docBuilder.parse(is);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName(CONFIG_ROOT);
            Element rootNode = (Element) nodeList.item(0);
            NodeList ptEl = rootNode.getElementsByTagName(CONFIG_PUBLISHTO);
            conf.publicationMethod=ptEl.item(0).getTextContent();
            runFromIp  = sc.getRunFromIp();
            if(PUBLISHTO_TABLE.equals(conf.publicationMethod)){
            	Element tEl=(Element)rootNode.getElementsByTagName(CONFIG_TABLE).item(0);
            	conf.stringForRemoving=tEl.getElementsByTagName(CONFIG_REMOVESTRING).item(0).getTextContent();
            	conf.stringForSetting=tEl.getElementsByTagName(CONFIG_SETSTRING).item(0).getTextContent();            	
            	conf.appEntityForRis=tEl.getElementsByTagName(CONFIG_RISAPPLICATIONENTITY).item(0).getTextContent();
            	if(EMPTY_STRING.equals(conf.appEntityForRis))
            		conf.appEntityForRis=null;
            	conf.targetApp=tEl.getElementsByTagName(CONFIG_TARGETAPP).item(0).getTextContent();
            	if(EMPTY_STRING.equals(conf.targetApp))
            		conf.targetApp=null;
            	
            	conf.enabled=true;
            }
        
        } catch (ParserConfigurationException e) {
            log.error("Unable to parse configuration...", e);
            
        } catch (IOException e) {
            log.error("Couldn't open config file!", e);
            
        } catch (SAXException e) {
            log.error("Couldn't parse config file!", e);
            
        } catch (Exception oex) {
            log.warn("Unable to load the configuration...", oex);
            
        }
        
	}

	
	
	public static synchronized ImageAvailabilityConfig getInstance(){
		if((conf==null)||(toReset))
			init();
		return conf;
	}
	
	public static synchronized void reset(){
		// This does not set conf=null, to avoid NullPointerExceptions in case someone resets and someone 
		// else is accessing instance methods (otherwise each method should be sync'd)
		toReset=true;	
	}
	
	///////////  Instance getters and setters


	public boolean isEnabled() {
		return enabled;
	}
	
	public String getPublicationMethod() {
		return publicationMethod;
	}

	public String getStringForRemoving() {
		return stringForRemoving;
	}

	public String getStringForSetting() {
		return stringForSetting;
	}

	public String getAppEntityForRis() {
		return appEntityForRis;
	}

	public String getTargetApp() {
		return targetApp;
	}

    public String getRunFromIp() {
        return runFromIp;
    }

}
