/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import flex.messaging.FlexContext;
import it.units.htl.dpacs.dao.DeprecationRemote;
import it.units.htl.dpacs.dao.UserManager;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.users.JSFUtil;
import it.units.htl.web.users.UserBean;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Sara
 */
public class StudiesResultsBackingBean {
    
	private static Log log = LogFactory.getLog(StudiesResultsBackingBean.class);
	
	private String actionReason;

    public StudiesResultsBackingBean() {
    }

    private String initWadoUrl(HttpSession s){
		HttpSession session = null;
		if(s==null){
			HttpServletRequest request = (HttpServletRequest) javax.faces.context.FacesContext.getCurrentInstance().getExternalContext().getRequest();
			session = request.getSession(false);
		}else{
			session=s;
		}
		
		String wUrl="";
		if(session.getAttribute(ConfigurationSettings.CONFIG_WADOURL)==null){
			try {
				wUrl=new UserManager().getConfigParam(ConfigurationSettings.CONFIG_WADOURL);
			} catch (NamingException nex) {
				wUrl="";
			}
			session.setAttribute(ConfigurationSettings.CONFIG_WADOURL, wUrl);
		}else{
			wUrl=(String)session.getAttribute(ConfigurationSettings.CONFIG_WADOURL);
		}
		return wUrl;
	}
    
    public String geturltoRWS() {
        String RWSUrl = "";
        context().getExternalContext().getSessionMap().put("selectedStudy", study());
        StudyListItem selected = (StudyListItem) context()
                .getExternalContext()
                .getSessionMap()
                .get("selectedStudy");
        HttpServletRequest request = (HttpServletRequest) javax.faces.context.FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        String host = context().getExternalContext().getInitParameter("RWS.url");
        String wUrl=initWadoUrl(session);
        RWSUrl = host + "?arg=" + wUrl + "?requestType=DcmDir&studyUID=" + selected.getStudyInstanceUid();
        return RWSUrl;
    }

    public String serieView() {
        context().getExternalContext().getSessionMap().remove("selectedObjectBackBean");
        context().getExternalContext().getSessionMap().put("selectedStudy", study());
        StudyListItem selected = (StudyListItem) context().getExternalContext().getSessionMap().get("selectedStudy");
        ObjectList ol = ((ObjectList) context().getExternalContext().getSessionMap().get("objectList"));
        if (ol != null)
            ol.getObjectsMatched().clear();
        serieList().findSeries(selected.getStudyInstanceUid());
        return "series";
    }

    public String studyDetRemove() {
        context().getExternalContext().getSessionMap().put("selectedStudy", study());
        return "details";
    }

    public String studyRemove() {
    	boolean done = false;
        StudyListItem selected = (StudyListItem) context().getExternalContext().getSessionMap().get("selectedStudy");
        DeprecationRemote bean=null; 
        String exMessage = null;
        try{
        	log.info("Deprecating study "+selected.getStudyInstanceUid());
//        	bean = InitialContext.doLookup("o3-dpacs/DeprecationBean/remote");
        	bean = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DeprecationBean!it.units.htl.dpacs.dao.DeprecationRemote");

        	String newUid=bean.getNewDeprecationUid();
        	UserBean userBean = (UserBean) JSFUtil.getManagedObject("userBean");
            long userPk=userBean.getAccountNo();
            long pk=bean.deprecateStudy(selected.getStudyInstanceUid(), newUid, false, actionReason, userPk);
            if(pk>=0){
            	log.info("Created Deprecation record "+pk);
            	done=true;
            }else{
            	log.error("Deprecation failed! PK="+pk);
            }
        }catch(Exception ex){
        	log.error("An exception occurred during study deprecation", ex);
        	exMessage = ex.getMessage();
        }
        
        ((RecoveryBackBean) JSFUtil.getManagedObject("recoveryBackBean"))._recoveryList = null;
        if (!done) {
            MessageManager.getInstance().setMessage("studyNotDeleted", (exMessage!=null)?new String[]{exMessage}:null);
            return "notRemoved";
        }
        return "remove";
    }

    private SerieList serieList() {
        FacesContext context = context();
        ELContext elContext = FacesContext.getCurrentInstance()
                .getELContext();
        ValueExpression ve =
                context.getApplication().getExpressionFactory()
                .createValueExpression(elContext, "#{serieList}", Object.class);
        return ((SerieList) ve.getValue(elContext));
    }

    private FacesContext context() {
        return (FacesContext.getCurrentInstance());
    }

    private StudyListItem study() {
        StudyListItem study = (StudyListItem) context().getExternalContext().getRequestMap().get("item");
        return (study);
    }

    public String getActionReason() {
        return actionReason;
    }

    public void setActionReason(String actionReason) {
        this.actionReason = actionReason;
    }

    public String backToSearch() {
        return "back";
    }
}
