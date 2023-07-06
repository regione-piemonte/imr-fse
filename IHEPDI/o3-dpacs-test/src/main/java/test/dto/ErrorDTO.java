/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package test.dto;

import java.io.Serializable;

import com.google.gson.Gson;

public class ErrorDTO implements Serializable {

	private static final long serialVersionUID = -656893579368718463L;
	
	public ErrorDTO(String esito, String codErrore, String descrErrore) {
		this.esito = esito;
		this.codErrore = codErrore;
		this.descrErrore = descrErrore;
	}

	public String getEsito() {
		return esito;
	}
	
	public void setEsito(String esito) {
		this.esito = esito;
	}
	
	public String getCodErrore() {
		return codErrore;
	}
	
	public void setCodErrore(String codErrore) {
		this.codErrore = codErrore;
	}
	
	public String getDescrErrore() {
		return descrErrore;
	}
	
	public void setDescrErrore(String descrErrore) {
		this.descrErrore = descrErrore;
	}
	
	/**
	 * Fornisce la risposta in caso di esito negativo.
	 * @param codErrore il codice di errore.
	 * @param descrErrore la descrizione dell'errore
	 * @return la risposta di errore
	 */
	public static String buildErrorDTO(String codErrore, String descrErrore) {
		return new Gson().toJson(new ErrorDTO(ERROR, codErrore, descrErrore));
	}
	
	private String esito;
	private String codErrore;
	private String descrErrore;
	
	private static final String ERROR = "Error";
}
