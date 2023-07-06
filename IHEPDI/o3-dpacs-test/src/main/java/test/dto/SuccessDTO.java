/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package test.dto;

import java.io.Serializable;

import com.google.gson.Gson;

public class SuccessDTO implements Serializable {

	private static final long serialVersionUID = -7760342137799573172L;
	
	public SuccessDTO(String esito, String jobId) {
		this.esito = esito;
		this.jobId = jobId;
	}

	public String getEsito() {
		return esito;
	}
	
	public void setEsito(String esito) {
		this.esito = esito;
	}
	
	public String getJobId() {
		return jobId;
	}
	
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	/**
	 * Fornisce la risposta in caso di esito positivo.
	 * @return la risposta positiva.
	 */
	public static String buildSuccessDTO(String jobId) {
		return new Gson().toJson(new SuccessDTO(ACCEPTED, jobId));
	}
	
	private String esito;
	private String jobId;
	
	private static final String ACCEPTED = "Accepted";
}
