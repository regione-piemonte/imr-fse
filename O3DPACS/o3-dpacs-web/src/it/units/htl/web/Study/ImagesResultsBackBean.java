/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

//import javax.el.ELContext;
//import javax.el.ValueExpression;
//import it.units.htl.web.Study.deprecation.RemoveQuery;
//import it.units.htl.web.jnlp.JnlpGenerator;
import it.units.htl.web.utils.XmlConfigLoader;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ImagesResultsBackBean {

    private String actionReason;
    
    private static Log log = LogFactory.getLog(ImagesResultsBackBean.class);

    
    private static final String SERVICENAME = "WebSettings";
    private static final String SERVICEPARAM_THUMBS_DIMENSION = "thumbsDimension";
    private static final String DEFAULTCONFIG_THUMBS_DIMENSION = "200";
    private static String thumbsDimension;
    
    /** Creates a new instance of ImagesBackBean */
    public ImagesResultsBackBean() {	
    }
    
    static{
    	loadThumbsDimension ();
    }
    
    public static void loadThumbsDimension (){
    	//assignes to the thumbsDimension the value read form the DB
    	Document config = XmlConfigLoader.getConfigurationFromDB(SERVICENAME);
    	
    	if(config != null){
			NodeList nodes = config.getElementsByTagName(SERVICEPARAM_THUMBS_DIMENSION);
			try{
				thumbsDimension = nodes.item(0).getTextContent();
				Integer.parseInt(thumbsDimension);
			}catch(Exception ex){
				log.warn(SERVICEPARAM_THUMBS_DIMENSION + " NOT defined. The default setting of " + DEFAULTCONFIG_THUMBS_DIMENSION + " will be used");
				thumbsDimension = DEFAULTCONFIG_THUMBS_DIMENSION;
			}	
		}else{
			log.warn(SERVICEPARAM_THUMBS_DIMENSION + " NOT defined. The default setting of " + DEFAULTCONFIG_THUMBS_DIMENSION + " will be used");
			thumbsDimension = DEFAULTCONFIG_THUMBS_DIMENSION;
		}
    }

    public void set_selected() {
        ObjectListItem _selected = object();
        _selected.setViewedCols(_selected.getColumns());
        _selected.setViewedRows(_selected.getRows());
        context().getExternalContext().getSessionMap().put("currentImageDisplayedNumber", objectList().getObjectsMatched().indexOf(_selected));
        context().getExternalContext().getSessionMap().put("currentImageDisplayed", _selected);
    }

    public void set_selected(int position) {
        ObjectListItem _selected = objectList().getObjectsMatched().get(position);
        _selected.setViewedCols(_selected.getColumns());
        _selected.setViewedRows(_selected.getRows());
        context().getExternalContext().getSessionMap().put("currentImageDisplayedNumber", position);
        context().getExternalContext().getSessionMap().put("currentImageDisplayed", _selected);
    }

    public String get_currentImage() {
        return ((Integer) context().getExternalContext().getSessionMap().get("currentImageDisplayedNumber") + 1) + "";
    }

    public void set_currentImage(String _currImg) {
        set_selected(Integer.parseInt(_currImg) - 1);
    }

    public Integer get_totalImages() {
        return new Integer(objectList().getObjectsMatched().size());
    }

    public void get_next() {
        Integer position = (Integer) context().getExternalContext().getSessionMap().get("currentImageDisplayedNumber");
        if (position < (objectList().getObjectsMatched().size() - 1)) {
            position++;
        } else {
            position = 0;
        }
        set_selected(position);
    }

    public void get_prev() {
        Integer position = (Integer) context().getExternalContext().getSessionMap().get("currentImageDisplayedNumber");
        if (position > 0) {
            position--;
        } else {
            position = (objectList().getObjectsMatched().size() - 1);
        }
        set_selected(position);
    }

    public void removeSelected(ActionEvent AE) {
        ObjectListItem item = (ObjectListItem) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("currentImageDisplayed");
              
        item.setViewedCols(thumbsDimension);
        item.setViewedRows(thumbsDimension);
       
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("currentImageDisplayed");
        item = null;
    }
    	
    private ObjectList objectList() {
        FacesContext context = context();
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        ValueExpression ve = context.getApplication().getExpressionFactory().createValueExpression(elContext, "#{objectList}", Object.class);
        return ((ObjectList) ve.getValue(elContext));
    }

    protected FacesContext context() {
        return (FacesContext.getCurrentInstance());
    }

    private ObjectListItem object() {
        ObjectListItem object = (ObjectListItem) context().getExternalContext().getRequestMap().get("item");
        return (object);
    }

    public String getActionReason() {
        return actionReason;
    }

    public void setActionReason(String actionReason) {
        this.actionReason = actionReason;
    }

    public String backToSeries() {
        return "back";
    }

}
