/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package dto;

import java.io.Serializable;

import com.google.gson.Gson;

/**
 * Modella la risposta in caso di esito negativo
 */
public class ErrorDTO implements Serializable {

	private static final long serialVersionUID = -656893579368718463L;
	
	public ErrorDTO(String esito, String codErrore, String descrErrore) {
		this.esito = esito;
		this.codErrore = codErrore;
		this.descrErrore = descrErrore;
	}

	/**
	 * Fornisce l'esito dell'operazione in errore
	 * @return l'esito dell'operazione in errore
	 */
	public String getEsito() {
		return esito;
	}
	
	/**
	 * Permette di impostare l'esito dell'operazione in errore
	 * @param esito l'esito di dell'operazione in errore
	 */
	public void setEsito(String esito) {
		this.esito = esito;
	}
	
	/**
	 * Fornisce il codice dell'operazione in errore
	 * @return il codice dell'operazione in errore
	 */
	public String getCodErrore() {
		return codErrore;
	}
	
	/**
	 * Permette di impostare il codice dell'operazione in errore
	 * @param codErrore il codice dell'operazione in errore
	 */
	public void setCodErrore(String codErrore) {
		this.codErrore = codErrore;
	}
	
	/**
	 * Fornisce la descrizione dell'operazione in errore
	 * @return la descrizione dell'operazione in errore
	 */
	public String getDescrErrore() {
		return descrErrore;
	}
	
	/**
	 * Permette di impostare la descrizione dell'operazione in errore
	 * @param descrErrore la descrizione dell'operazione in errore
	 */
	public void setDescrErrore(String descrErrore) {
		this.descrErrore = descrErrore;
	}
	
	/**
	 * Fornisce la risposta in caso di esito negativo
	 * @param codErrore il codice di errore
	 * @param descrErrore la descrizione dell'errore
	 * @return la risposta di errore
	 */
	public static String buildErrorDTO(String codErrore, String descrErrore) {
		return new Gson().toJson(new ErrorDTO(ERROR, codErrore, descrErrore));
	}
	
	/**
	 * Esito dell'operazione in errore
	 */
	private String esito;
	
	/**
	 * Codice dell'operazione in errore
	 */
	private String codErrore;
	
	/**
	 * Descrizione dell'operazione in errore
	 */
	private String descrErrore;
	
	private static final String ERROR = "Error";
}
