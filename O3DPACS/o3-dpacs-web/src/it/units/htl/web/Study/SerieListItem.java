/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Date;
/**
 * 
 * @author Sara
 *
 */
public class SerieListItem implements Serializable{
    
    private Date serieDate = null;
    private Time serieTime = null;
    private String numberOfSeriesRelatedInstances;
    private String seriesNumber;
    private String modality;
    private String seriesDescription;
    private String serieStatus;
    private String serieInstanceUid;
        
    public Date getSerieDate() {
        return serieDate;
    }
	
    public void setSerieDate(Date _serieDate) {
	this.serieDate = _serieDate;
    }
	
    public Time getSerieTime() {
        return serieTime;
    }
	
    public void setSerieTime(Time _serieTime) {
	this.serieTime = _serieTime;
    }
    
    public String getSeriesDescription() {
	//if (seriesDescription == null) seriesDescription = "";
	return seriesDescription;
    }
	
    public void setSeriesDescription(String _seriesDescription) {
	this.seriesDescription = _seriesDescription;
    }
    
    public String getSerieStatus() {
	return serieStatus;
    }
	
    public void setSerieStatus(String _serieStatus) {
	this.serieStatus = _serieStatus;
    }
    
    public String getModality() {
	return modality;
    }
    
    public void setModality(String _modality) {
	this.modality = _modality;
    }
    
    public String getSeriesNumber() {
	return seriesNumber;
    }
    
    public void setSeriesNumber(String _seriesNumber) {
	this.seriesNumber = _seriesNumber;
    } 
    
    public String getNumberOfSeriesRelatedInstances() {
	return numberOfSeriesRelatedInstances;
    }
    
    public void setNumberOfSeriesRelatedInstances(String _numberOfSeriesRelatedInstances) {
	this.numberOfSeriesRelatedInstances = _numberOfSeriesRelatedInstances;
    }
    
    public String getSerieInstanceUid() {
	return serieInstanceUid;
    }
    
    public void setSerieInstanceUid(String _serieInstanceUid) {
	this.serieInstanceUid = _serieInstanceUid;
    }
    
    public String getShortDescription(){
    	if(seriesDescription != null){
    	if(seriesDescription.length() > 15) return seriesDescription.substring(0, 12) + "...";
    	return seriesDescription;
    	}else{
    		return ""; 
    	}
    }
}