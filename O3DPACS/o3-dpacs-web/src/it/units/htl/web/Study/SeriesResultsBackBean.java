/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;


import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.utils.XmlConfigLoader;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * 
 * @author Sara
 *
 */
public class SeriesResultsBackBean {
	
	    private String actionReason;
	    
	    private static Log log = LogFactory.getLog(SeriesResultsBackBean.class);
	    
	    private static final String SERVICENAME = "WebSettings";
	    
	    private static final String SERVICEPARAM_THUMBS_PAGE = "thumbsPerPage";
	    private static final String DEFAULTCONFIG_THUMBS_PAGE = "25";
	    
	    private static final String SERVICEPARAM_THUMBS_ROW = "thumbsPerRow";
	    private static final String DEFAULTCONFIG_THUMBS_ROW = "5";
	    private static String thumbsPerPage;
	    private static String thumbsPerRow;

		/** Creates a new instance of StudiesBackBean */
	    public SeriesResultsBackBean() {
	    	Document config = XmlConfigLoader.getConfigurationFromDB(SERVICENAME);

	    	if(config != null){
				NodeList nodes = config.getElementsByTagName(SERVICEPARAM_THUMBS_PAGE);
				try{
					thumbsPerPage = nodes.item(0).getTextContent();
					Integer.parseInt(thumbsPerPage);
				}catch(Exception ex){
					log.warn(SERVICEPARAM_THUMBS_PAGE + " NOT defined. The default setting of " + DEFAULTCONFIG_THUMBS_PAGE + " will be used");
					thumbsPerPage = DEFAULTCONFIG_THUMBS_PAGE;
				}	
			}else{
				log.warn(SERVICEPARAM_THUMBS_PAGE + " NOT defined. The default setting of " + DEFAULTCONFIG_THUMBS_PAGE + " will be used");
				thumbsPerPage = DEFAULTCONFIG_THUMBS_PAGE;
			}	

			if(config != null){
				NodeList nodes = config.getElementsByTagName(SERVICEPARAM_THUMBS_ROW);
				try{
					thumbsPerRow = nodes.item(0).getTextContent();
					Integer.parseInt(thumbsPerRow);
				}catch(Exception ex){
					log.warn(SERVICEPARAM_THUMBS_ROW + " NOT defined. The default setting of " + DEFAULTCONFIG_THUMBS_ROW + " will be used");
					thumbsPerRow = DEFAULTCONFIG_THUMBS_ROW;
				}	
			}else{
				log.warn(SERVICEPARAM_THUMBS_ROW + " NOT defined. The default setting of " + DEFAULTCONFIG_THUMBS_ROW + " will be used");
				thumbsPerRow = DEFAULTCONFIG_THUMBS_ROW;
			}	    	
	    }
	    
	    public void objectView(){
	    	context().getExternalContext().getSessionMap().put("selectedSerie", serie());
	    	SerieListItem selected = (SerieListItem) context().getExternalContext().getSessionMap().get("selectedSerie");
	    	objectList().findObjects(serie().getSerieInstanceUid());
	    }
	    
	    public String serieDetRemove() {
	    	context().getExternalContext().getSessionMap().put("selectedSerie", serie());
	        return "details";
	    }
	    
	    public void removeSerieSelected(PhaseEvent e){
	    	if (e.getPhaseId() == PhaseId.RENDER_RESPONSE) {
	    		((MessageManager)context().getExternalContext().getSessionMap().get("messageManager")).resetMessage();
	    		SerieListItem sl = ((SerieListItem)context().getExternalContext().getSessionMap().get("selectedSerie"));
	    		if(sl != null)sl = null;
			}	    	
	    }
	    
	    
	    private ObjectList objectList() {
	         FacesContext context = context();
	         ELContext elContext = FacesContext.getCurrentInstance()
	            .getELContext();
	         ValueExpression ve =
	             context.getApplication().getExpressionFactory()
	                    .createValueExpression(elContext, "#{objectList}", Object.class);
	         return ((ObjectList) ve.getValue(elContext));
	     }
		
		protected FacesContext context() {
	        return (FacesContext.getCurrentInstance());
	    }
		
		 private SerieListItem serie() {
		        SerieListItem serie = (SerieListItem) context().getExternalContext().getRequestMap().get("item");
		        return (serie);
		    }


		public String getActionReason() {
			return actionReason;
		}


		public void setActionReason(String actionReason) {
			this.actionReason = actionReason;
		}

		public String backToStudies() {
			return "back";
		}

		// Reads thumbsPerPage from the DB
		public String getThumbsPerPage(){
		    return thumbsPerPage;
		}
			
		// Reads thumbsPerRow from the DB
		public String getThumbsPerRow(){
		    return thumbsPerRow;
		}
}
