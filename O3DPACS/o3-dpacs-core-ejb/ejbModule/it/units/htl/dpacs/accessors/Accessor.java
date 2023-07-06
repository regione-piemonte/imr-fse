/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.accessors;

import it.units.htl.dpacs.valueObjects.Credentials;

import java.io.IOException;
import java.io.InputStream;


public abstract class Accessor {

	protected static String FORMAT_DATE="yyyyMMdd";
	protected static String FORMAT_DATETIME="yyyyMMdd_HHmm";
	
	protected static String SLASH="/";
	
	protected String deviceUrl;
	protected Credentials credentials;
	
	public Accessor(String deviceUrl, Credentials credentials){
		initialize(deviceUrl, credentials);
	}
	
	public void initialize(String deviceUrl, Credentials credentials){
		try{
			this.deviceUrl=deviceUrl;
			this.credentials=credentials;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public String getDeviceUrl() {
		return deviceUrl;
	}

	public void setDeviceUrl(String deviceUrl) {
		this.deviceUrl = deviceUrl;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}


	public abstract InputStream getFile(String directUrl) throws IOException;
	public abstract void close();

}

