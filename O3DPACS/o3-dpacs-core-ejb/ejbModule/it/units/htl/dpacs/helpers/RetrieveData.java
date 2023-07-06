/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import org.dcm4che.data.Dataset;

import it.units.htl.dpacs.valueObjects.NearlineData;

import java.io.Serializable;


/**
 * RetrieveData is a structure to compose data to answer a move request
 * It is composed by:
 * - the dataset of your image (no pixel data)
 * - the url to the file
 * - the type
 * 
 * the storage SCU class will use the url to get the file, merge the dataset 
 * keys it used previously into the file and send it to the client.
 *
 * @author Mbe
 */
public class RetrieveData implements Serializable {
  
    private static final long serialVersionUID = 7777777L;
    private final Dataset dataset;
    private final String url;
    /**
     * you should know if it's an image or not to avoid compression if it's
     * forced for the actual client.
     */
    private final String type;
    
    private NearlineData nearlineData;
    
    public RetrieveData(Dataset dataset, String url, String type) {
        this.dataset = dataset;
        this.url = url;
        this.type = type;
        this.nearlineData=null;
    }
    
    public RetrieveData(Dataset dataset, String url, String type, NearlineData nearlineData) {
        this.dataset = dataset;
        this.url = url;
        this.type = type;
        this.nearlineData=nearlineData;
    }
    
    /** Getter for property dataset.
     * @return Value of property dataset.
     */
    public final Dataset getDataset() {
        return dataset;
    }
    
    /** Getter for property url.
     * @return Value of property url.
     */
    public final String getURL() {
        return url;
    }
    public final String getType() {
        return type;
    }

	public NearlineData getNearlineData() {
		return nearlineData;
	}
    
  
}
