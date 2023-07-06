/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package dto;

import java.io.Serializable;

import com.google.gson.Gson;

/**
 * Modella una risposta in caso di successo
 */
public class SuccessDTO implements Serializable {

	private static final long serialVersionUID = -7760342137799573172L;
	
	public SuccessDTO(String esito, String jobId) {
		this.esito = esito;
		this.jobId = jobId;
	}

	/**
	 * Fornisce l'esito dell'operazione
	 * @return l'esito dell'operazione 
	 */
	public String getEsito() {
		return esito;
	}
	
	/**
	 * Permette di impostare l'esito dell'operazione
	 * @param esito l'esito dell'operazione
	 */
	public void setEsito(String esito) {
		this.esito = esito;
	}
	
	/**
	 * Fornisce l'id del job avviato
	 * @return l'id del job avviato
	 */
	public String getJobId() {
		return jobId;
	}
	
	/**
	 * Permette di impostare l'id del job avviato
	 * @param jobId l'id del job avviato
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	/**
	 * Fornisce la risposta in caso di esito positivo
	 * @param jobId l'id del job avviato
	 * @return la risposta positiva
	 */
	public static String buildSuccessDTO(String jobId) {
		return new Gson().toJson(new SuccessDTO(ACCEPTED, jobId));
	}
	
	/**
	 * Esito dell'operazione
	 */
	private String esito;
	
	/**
	 * ID del job avviato
	 */
	private String jobId;
	
	private static final String ACCEPTED = "Accepted";
}
