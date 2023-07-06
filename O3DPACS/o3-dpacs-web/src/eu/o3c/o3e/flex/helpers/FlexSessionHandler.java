/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.helpers;

import java.util.HashMap;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;

import flex.messaging.FlexContext;
import it.units.htl.dpacs.dao.DeprecationRemote;
import it.units.htl.dpacs.dao.UserManager;
import it.units.htl.web.Study.StudyListItem;
import it.units.htl.web.users.UserBean;


import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class FlexSessionHandler {
	
    static Logger log = Logger.getLogger(FlexSessionHandler.class);

    private String authorization;
    private String studyUID;
    private String wadoUrl;
    private String keyImagesOnly;
    
	public String getStudyUID() {
        log.debug("entering getStudyInSession()");
		HttpServletRequest request = (HttpServletRequest) javax.faces.context.FacesContext.getCurrentInstance().getExternalContext().getRequest();
        studyUID = ((StudyListItem) request.getSession().getAttribute("selectedStudy")).getStudyInstanceUid();
        log.debug("leaving getStudyInSession(). Return value: '" + studyUID + "'");
        return studyUID;
	}


	public String getAuthorization() throws Exception {
		HttpServletRequest request = (HttpServletRequest) javax.faces.context.FacesContext.getCurrentInstance().getExternalContext().getRequest();
		UserBean loggedUser = (UserBean) request.getSession().getAttribute("userBean");
		
		authorization = "";
        if(loggedUser.isLdap()){
        	authorization = "WadoService:caff110499d33e2fe67e9a8e12777baa06e2e210";
        }else{
        	authorization =  loggedUser.getUserName() + ":" + loggedUser.getPassword();
        }

        byte[] b = authorization.getBytes();
        log.debug("encoding credentials: '" + authorization + "'");
        try {
            // encode and don't chunk (don't add CR/LF for long strings)
        	authorization = new String(Base64.encodeBase64(b, false));
        } catch (Exception e) {
            log.error("Cannot encode credentials in base64", e);
            throw e;
        }
        log.debug("leaving getCredentials(). Return value: '" + authorization + "'");
        return authorization;

	}

	public FlexSessionHandler() throws Exception {
    }

    public String getStudyInSession() {
        // might return null!
        log.debug("entering getStudyInSession()");
        String studyUID = ((StudyListItem) FlexContext.getHttpRequest().getSession().getAttribute("selectedStudy")).getStudyInstanceUid();
        log.debug("leaving getStudyInSession(). Return value: '" + studyUID + "'");
        return studyUID;
    }

    public String getCredentials() throws Exception {
        log.debug("entering getCredentials()");
        UserBean loggedUser =  (UserBean) FlexContext.getHttpRequest().getSession().getAttribute("userBean");
        String ret = "";
        if(loggedUser.isLdap()){
            ret = "WadoService:caff110499d33e2fe67e9a8e12777baa06e2e210";
        }else{
            ret =  ((UserBean) FlexContext.getHttpRequest().getSession().getAttribute("userBean")).getUserName() + ":"
                    + ((UserBean) FlexContext.getHttpRequest().getSession().getAttribute("userBean")).getPassword();
        }

        byte[] b = ret.getBytes();
        log.debug("encoding credentials: '" + ret + "'");
        try {
            // encode and don't chunk (don't add CR/LF for long strings)
            ret = new String(Base64.encodeBase64(b, false));
        } catch (Exception e) {
            log.error("Cannot encode credentials in base64", e);
            throw e;
        }
        log.debug("leaving getCredentials(). Return value: '" + ret + "'");
        return ret;
    }

    public String getWadoUrl() throws Exception {
        log.debug("Entering getWadoUrl()");
        wadoUrl = "";
        try {
        	wadoUrl = new UserManager().getConfigParam("WadoUrlForFlex");
        } catch (Exception nex) {
            log.error("", nex);
            throw nex;
        }
        log.debug("Leaving getWadoUrl(). Return value: '" + wadoUrl + "'");
        return wadoUrl;
    }

    public boolean deprecateSeries(String studyUID, String seriesUID, String reason) throws Exception {  
    	boolean ret=false;
    	DeprecationRemote bean=null; 
        try{
        	log.info("Deprecation of " + studyUID + " " + seriesUID + " due to this reason: " + reason);
//            bean = InitialContext.doLookup("o3-dpacs/DeprecationBean/remote");
            bean = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DeprecationBean!it.units.htl.dpacs.dao.DeprecationRemote");
            String newUid=bean.getNewDeprecationUid();
            long userPk=((UserBean) FlexContext.getHttpRequest().getSession().getAttribute("userBean")).getAccountNo();
            long pk=bean.deprecateSeries(seriesUID, newUid, false, reason, userPk);
            if(pk>=0){
            	log.info("Created Deprecation record "+pk);
            	ret=true;
            }else{
            	log.error("Deprecation failed! PK="+pk);
            }
        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        }
        return ret;
        
    }
    
    public boolean canUserDeprecate(){
        try{
            return ((HashMap<String, Boolean>) FlexContext.getHttpRequest().getSession().getAttribute("actionEnabled")).containsKey("DeprecateStudies");
        }catch (Exception e) {
            log.info("", e);
            return false;
        }
        
        
    }
	
	public String getKeyImagesOnly () throws Exception {
        log.debug("Entering getKeyImagesOnly()");
        keyImagesOnly = "false";
        try {
            keyImagesOnly = new UserManager().getConfigParam("KeyImagesOnly");
        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        }
        log.debug("Leaving getKeyImagesOnly(). Return value: '" + keyImagesOnly + "'");
        return keyImagesOnly.toLowerCase();
    }


}
