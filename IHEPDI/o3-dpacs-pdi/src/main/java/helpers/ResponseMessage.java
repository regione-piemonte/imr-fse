/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package helpers;

/**
 * Fornisce le descrizioni per le risposte del servizio
 */
public class ResponseMessage {

	public static final String CC_SUC_200 = "Operazione completata con successo";
	
	public static final String CC_ERR_201 = "studyUID e/o accessionNumber parametri obbligatori";
	
	public static final String CC_ERR_202 = "studyUID e accessionNumber contengono entrambi piu valori";
	
	public static final String CC_ERR_203 = "RequestID al massimo di 64 caratteri";
	
	public static final String CC_ERR_204 = "Paziente non presente";
	
	public static final String CC_ERR_205 = "Il paziente associato a Study e AccessionNumber non corrisponde";
	
	public static final String CC_ERR_206 = "Accession number non fornito e studio non recuperabile o struttura non fornita";
	
	public static final String CC_ERR_207 = "Impossibile creare indice DICOMDIR";
	
	public static final String CC_ERR_209 = "Credenziali errate";
	
	public static final String CC_ERR_210 = "Impossibile caricare configurazione";
	
	public static final String CC_ERR_211 = "Impossibile ottenere risorse DICOM";
	
	public static final String CC_ERR_212 = "Servlet non attiva";

	public static final String CC_ERR_213 = "CommandMove fallito";
	
	public static final String CC_ERR_214 = "Impossibile aggiungere il viewer";
	
	public static final String CC_ERR_215 = "Impossibile inviare notifica di completamento";

	public static final String CC_ERR_216 = "Impossibile eseguire move";
	
	public static final String CC_ERR_217 = "Request ID già presente";
	
	public static final String CC_ERR_218 = "Gli Accession Number sono tutti non presenti/privi di immagine";
	
	public static final String CC_ERR_222 = "Impossibile recepire istanza DICOM da WADO";
	
	public static final String CC_ERR_223 = "Impossibile recepire istanza IMAGE da WADO";
	
	public static final String CC_ERR_300 = "Chiusura JOB Pulizia";
	
	public static final String CC_ERR_301 = "Numero thread massimo raggiunto";
	
	public static final String CC_ERR_304 = "Accession Number non valorizzato (Null)";
	
	public static final String CC_ERR_305 = "Study non valorizzato (Null)";
	
	public static final String CC_ERR_500 = "Errore interno";
}
