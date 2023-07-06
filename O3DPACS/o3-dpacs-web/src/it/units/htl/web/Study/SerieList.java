/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.HttpFlexSession;
import flex.messaging.client.FlexClient;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.users.JSFUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;


import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;

/**
 * 
 * @author Sara
 *
 */
public class SerieList {
	
	
    private ArrayList<SerieListItem> seriesMatched;
    private List<SelectItem> seriesCombo;
    private String selectedSeries;
    private HashMap<String,String> objectToSeries;
    
    

    public SerieList() {
    	seriesMatched = new ArrayList<SerieListItem>();
    	seriesCombo = new ArrayList<SelectItem>();
    	objectToSeries = new HashMap<String, String>();
    }


	public void findSeries(String studyInstanceUid) {
		seriesMatched.clear();
		StudyFinder.getSeries(studyInstanceUid);
		for(int i = 0; i<seriesMatched.size();i++) {
			objectToSeries.put(seriesMatched.get(i).toString(),seriesMatched.get(i).getSerieInstanceUid());
		}
	}
    
	public ArrayList<SerieListItem> getSeriesMatched() {
        return seriesMatched;
    }
    public void setSeriesMatched(ArrayList<SerieListItem> arrayList) {
        this.seriesMatched = arrayList;
    }


	public void add(SerieListItem serieListItem) {
		seriesMatched.add(serieListItem);
		
	}
	
	public void setSeriesCombo(List<SelectItem> series) {
		this.seriesCombo = series;
	}
	
	public List<SelectItem> getSeriesCombo () {
		ArrayList<SelectItem> ret = new ArrayList<SelectItem>();
		for (int i = 0; i < seriesMatched.size(); i++) {
			ret.add(new SelectItem(seriesMatched.get(i),seriesMatched.get(i).getSerieDate() + " - " + seriesMatched.get(i).getModality() + " - "  + seriesMatched.get(i).getNumberOfSeriesRelatedInstances()));
		}
		seriesCombo = ret;
		return seriesCombo;
	}
	
	public void selectSeries(ValueChangeEvent event){
		String s = (String)event.getNewValue();
		selectedSeries= objectToSeries.get(s);
		JSFUtil.storeOnSession(FacesContext.getCurrentInstance(), "selectedSeries", selectedSeries);
//		FlexSession session = FlexContext.getFlexSession();
//		session.setAttribute("selectedSeries", selectedSeries);
	}
	
	public String getSelectedSeries(){
		return selectedSeries;
	}
}
