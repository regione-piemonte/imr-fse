/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package helpers;

/**
 * Fornisce i codici per le risposte del servizio
 */
public class ResponseCode {
	
	/**
	 *  Operazione effettuta con successo
	 */
	public static final String CC_SUC_200 = "CC_SUC_200";
	
	/**
	 *  studyUID e accessionNumber non valorizzati
	 */
	public static final String CC_ERR_201 = "CC_ERR_201";
	
	/** 
	 * studyUID e accessionNumber entrambi liste
	 */
	public static final String CC_ERR_202 = "CC_ERR_202";

	/**
	 *  requestID lunghezza maggiore di 64 caratteri
	 */
	public static final String CC_ERR_203 = "CC_ERR_203";
	
	/**
	 *  Non presente il paziente
	 */
	public static final String CC_ERR_204 = "CC_ERR_204";
	
	/**
	 *  I parametri accessionNumber e studyID non fanno riferimento allo stesso paziente
	 */
	public static final String CC_ERR_205 = "CC_ERR_205";
	
	/**
	 *  accessionNumber non fornito e studi non recuperabili
	 *  studi non recuperabili
	 */
	public static final String CC_ERR_206 = "CC_ERR_206";
	
	/**
	 *  Creazione DICOMDIR fallita
	 */
	public static final String CC_ERR_207 = "CC_ERR_207";
	
	/**
	 *  Credenziali errate
	 */
	public static final String CC_ERR_209 = "CC_ERR_209";
	
	/**
	 *  Errore nel caricare la configurazione
	 */
	public static final String CC_ERR_210 = "CC_ERR_210";
	
	/**
	 *  Errore nel creare contenuto PDI
	 */
	public static final String CC_ERR_211 = "CC_ERR_211";
	
	/**
	 *  Servlet non attiva
	 */
	public static final String CC_ERR_212 = "CC_ERR_212";
	
	/**
	 *  CommandMove fallita
	 */
	public static final String CC_ERR_213 = "CC_ERR_213";
		
	/**
	 *  Impossibile aggiungere il viewer
	 */
	public static final String CC_ERR_214 = "CC_ERR_214";
	
	/**
	 *  Impossibile inviare notifica di completamneto
	 */
	public static final String CC_ERR_215 = "CC_ERR_215";
	
	/**
	 *  Impossibile eseguire move
	 */
	public static final String CC_ERR_216 = "CC_ERR_216";

	/**
	 *  Request ID gia' presente
	 */
	public static final String CC_ERR_217 = "CC_ERR_217";
	
	/**
	 *  accessionNumber non fornito
	 */
	public static final String CC_ERR_218 = "CC_ERR_218";
	
	/**
	 *  Impossibile recepire istanza DICOM da WADO 
	 */
	public static final String CC_ERR_222= "CC_ERR_222";
	

	/**
	 *  Impossibile recepire istanza IMAGE da WADO 
	 */
	public static final String CC_ERR_223= "CC_ERR_223";
	
	/**
	 *  Chiusura batch pulizia
	 */
	public static final String CC_ERR_300 = "CC_ERR_300";

	/**
	 *  Numero thread massimo raggiunto
	 */
	public static final String CC_ERR_301 = "CC_ERR_301";
	
	/**
	 *  Job scaduto
	 */
	public static final String CC_ERR_310 = "CC_ERR_310";
	
	/**
	 *  Accession number nullo
	 */
	public static final String CC_ERR_304 = "CC_ERR_304";
	
	
	/**
	 *  Study  nullo
	 */
	public static final String CC_ERR_305 = "CC_ERR_305";
	
	/**
	 *  Errore interno
	 */
	public static final String CC_ERR_500 = "CC_ERR_500";
}
