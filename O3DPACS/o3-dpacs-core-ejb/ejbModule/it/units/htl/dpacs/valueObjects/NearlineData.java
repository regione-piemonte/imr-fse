/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.io.Serializable;

public class NearlineData implements Serializable{
	
	private String deviceType;
	private String deviceUrl;
	private String directUrl;
	private Credentials credentials;
	
	public NearlineData() {
		this.deviceType = null;
		this.deviceUrl = null;
		this.directUrl = null;
		this.credentials = null;
	}
	
	public NearlineData(String deviceType, String directUrl) {
		this.deviceType = deviceType;
		this.deviceUrl = null;
		this.directUrl = directUrl;
		this.credentials = null;
	}
	
	public NearlineData(String deviceType, String directUrl, Credentials credentials) {
		this.deviceType = deviceType;
		this.deviceUrl = null;
		this.directUrl = directUrl;
		this.credentials = credentials;
	}
	
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getDeviceUrl() {
		return deviceUrl;
	}
	public void setDeviceUrl(String deviceUrl) {
		this.deviceUrl = deviceUrl;
	}
	public String getDirectUrl() {
		return directUrl;
	}
	public void setDirectUrl(String directUrl) {
		this.directUrl = directUrl;
	}
	public Credentials getCredentials() {
		return credentials;
	}
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

}
