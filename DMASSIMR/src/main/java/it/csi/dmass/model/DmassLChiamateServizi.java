/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.model;

import java.sql.Timestamp;

public class DmassLChiamateServizi {

	private Long id;
	private Long id_ser;
	private String request;
	private String response;
	private Timestamp data_ins;
	private Timestamp data_mod;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getId_ser() {
		return id_ser;
	}
	public void setId_ser(Long id_ser) {
		this.id_ser = id_ser;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public Timestamp getData_ins() {
		return data_ins;
	}
	public void setData_ins(Timestamp data_ins) {
		this.data_ins = data_ins;
	}
	public Timestamp getData_mod() {
		return data_mod;
	}
	public void setData_mod(Timestamp data_mod) {
		this.data_mod = data_mod;
	}
	
	

}
