/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.pdi.maps;

public class To3pdiConf implements java.io.Serializable {

	private static final long serialVersionUID = -2514220455710356863L;
	private String paramkey;
	private String paramvalue;
	private Integer enabled;
	
	public To3pdiConf() {
	}

	public To3pdiConf(String paramkey, String paramvalue, Integer enabled, String stationName) {
		this.paramkey = paramkey;
		this.paramvalue = paramvalue;
		this.enabled = enabled;
	}

	public String getParamkey() {
		return paramkey;
	}

	public void setParamkey(String paramkey) {
		this.paramkey = paramkey;
	}

	public String getParamvalue() {
		return paramvalue;
	}

	public void setParamvalue(String paramvalue) {
		this.paramvalue = paramvalue;
	}

	public Integer getEnabled() {
		return enabled;
	}

	public void setEnabled(Integer enabled) {
		this.enabled = enabled;
	}

}
