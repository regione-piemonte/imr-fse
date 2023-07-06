/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.kos;

import it.units.htl.web.Study.ObjectListItem;

import java.util.ArrayList;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;

import org.ajax4jsf.component.html.HtmlAjaxCommandLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FilterManager {
    private Log log = LogFactory.getLog(FilterManager.class);
    private FilteredSeriesListItem currentSeries = null;
    private ObjectListItem currentInstance = null;
    private Integer currentPosition;
    
    
    public void selectFilteredSeries(ActionEvent e) {
        HtmlAjaxCommandLink source = (HtmlAjaxCommandLink) e.getSource();
        FilteredSeriesListItem selectedFilteredSeries = (FilteredSeriesListItem) source.getData();
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        session.setAttribute("filteredObject", selectedFilteredSeries.getFilteredObject());
        currentSeries = selectedFilteredSeries;
    }

    public void selectFilteredInstance(ActionEvent e) {
        HtmlAjaxCommandLink source = (HtmlAjaxCommandLink) e.getSource();
        ObjectListItem selectedFilteredInstance = (ObjectListItem) source.getData();
        HttpSession session = getSession();
        session.setAttribute("selectedFilteredObject", selectedFilteredInstance);
        currentInstance = selectedFilteredInstance;
        ArrayList<ObjectListItem> instances = currentSeries.getFilteredObject().getObjectsMatched();
        currentPosition = instances.indexOf(currentInstance);
    }

    private boolean ignore = false;
    
    public void nextInstance() {
        ArrayList<ObjectListItem> instances = currentSeries.getFilteredObject().getObjectsMatched();
        int cur_pos = instances.indexOf(currentInstance);
        if (cur_pos < instances.size()-1) {
            cur_pos++;
        } else {
            cur_pos = 0;
        }
        ignore = true;
        setCurrentSelection(instances, cur_pos);
    }

    public void prevInstance() {
        ArrayList<ObjectListItem> instances = currentSeries.getFilteredObject().getObjectsMatched();
        int cur_pos = instances.indexOf(currentInstance);
        if (cur_pos > 0) {
            cur_pos--;
        } else {
            cur_pos = instances.size() - 1;
        }
        ignore = true;
        setCurrentSelection(instances, cur_pos);
    }
    
    public Integer getCurrentInstancePosition() {
            return currentPosition + 1;
    }

    public void setCurrentInstancePosition(Integer pos) {
        ignore = false;
        ArrayList<ObjectListItem> instances = currentSeries.getFilteredObject().getObjectsMatched();
        setCurrentSelection(instances, pos - 1);
    }

    public int getTotalInstances() {
        return currentSeries.getFilteredObject().getObjectsMatched().size();
    }

    public void valueChangeListener(javax.faces.event.ValueChangeEvent e) {
        if(!ignore){
            if ( (Integer)e.getNewValue() < (Integer)e.getOldValue()) {
                prevInstance();
            } else {
                nextInstance();
            }   
        }
    }

    private void setCurrentSelection(ArrayList<ObjectListItem> instances, int pos) {
        currentInstance = instances.get(pos);
        currentPosition = instances.indexOf(currentInstance);
        getSession().setAttribute("selectedFilteredObject", instances.get(pos));
    }

    private HttpSession getSession() {
        return (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    }

}
