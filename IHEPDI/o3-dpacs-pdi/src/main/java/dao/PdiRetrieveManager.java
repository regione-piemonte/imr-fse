/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package dao;

import java.util.List;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;

import it.pdi.maps.ExpiredJob;
import it.pdi.maps.To3pdiConfHome;
import it.pdi.maps.To3pdiJob;
import it.pdi.maps.To3pdiJobHome;
import it.pdi.maps.Utility;

/**
 * Fornisce accesso alla configurazione tramite accesso al DB
 */
public class PdiRetrieveManager {
	
	private static final Logger log = Logger.getLogger(PdiRetrieveManager.class);
	
	 /**
     * Metodo che permette di recuperare i parametri di configurazione per PDI
     * @param key identificativo parametro di configurazione
     * @return valore del parametro di configurazione
     */
    public String getConfigParamPDI(String key) {
    	 To3pdiConfHome configurazione = new To3pdiConfHome();
    	 return configurazione.getConfigParamPDI(key); 
    }
    
	/**
	 * Inserisce un nuovo job.
	 * @param codice il codice dell'operazione.
	 * @param descrizione la descizione dell'operazione.
	 * @param operazione l'operazione del job.
	 * @param stato lo stato del job.
	 * @throws Exception 
	 */
	public String insertTo3pdiJob(String codice, String descrizione, String operazione, String stato, String azienda) throws Exception {
		To3pdiJobHome job = new To3pdiJobHome();
		To3pdiJob createdJob = job.insertTo3pdiJob(codice, descrizione, operazione, stato, azienda);
		
		return createdJob.getJobid();
	}
	
	/**
	 * Inserisce un nuovo job.
	 * @param codice il codice dell'operazione.
	 * @param descrizione la descizione dell'operazione.
	 * @param operazione l'operazione del job.
	 * @param stato lo stato del job.
	 * @throws Exception 
	 */
	public boolean sendEmail(String object, String subject)  {
		Utility utility = new Utility();
		
		return utility.sendEmail(subject, subject);
	}
	 
		/**
		 * Check per verifica requesti di duplicato.
		 * @param codice il codice dell'operazione.
		 * @param descrizione la descizione dell'operazione.
		 * @param operazione l'operazione del job.
		 * @param stato lo stato del job.
		 * @throws Exception 
		 */
		public Boolean checkRequestIDExist(String requestID) throws Exception {
			To3pdiJobHome job = new To3pdiJobHome();
			Boolean check = job.checkRequestIDExist(requestID);
			
			return check;
		}
    
	/**
	 * Metodo che aggiorna lo stato di un job.
	 * @param jobId identificativo univoco del job.
	 * @param codice codice di aggiornamento.
	 * @param descrizione descrizione dell'aggiornamento.
	 * @param operazione operazione effettuata.
	 * @param stato nuovo stato del job.
	 */
	public void updateTo3pdiJob(String jobId, String codice, String descrizione, String operazione, String stato, String digest, String zipname, String request, boolean isLast) {
		To3pdiJobHome job = new To3pdiJobHome();
		job.updateTo3pdiJob(jobId, codice, descrizione, operazione, stato, digest, zipname, request, isLast);
	}
	
	
	/**
	 * Metodo che aggiorna il valore di un parametro di configurazione.
	 * @param key identificativo univoco della chiave.
	 * @param value nuovo valore.
	 */
	public void updateTo3pdiConf(String key, String value) {
		To3pdiConfHome conf = new To3pdiConfHome();
		conf.updateTo3pdiConf(key, value);
		
	}
	
	/**
	 * Metodo per il recupero dei job scaduti
	 * @param expireAt tempo su cui si basa la scadenza dei job
	 * @return lista di job scaduti
	 */
	public List<ExpiredJob> getExpiredJob(int expireAt) {
		To3pdiJobHome job = new To3pdiJobHome();
		return job.getExpiredJob(expireAt);
	}
	
	/**
	 * Metodo che recupera il numero di una series
	 * @param series identificativo della series
	 * @return numero della series
	 */
	public String getSeriesNumber(String series) {
		byte[] bytes = series.getBytes();
		CRC32 crc = new CRC32();
		crc.update(bytes, 0, bytes.length);
		String seriesNumber = Long.toHexString(crc.getValue()).toString().toUpperCase();
		log.info("Series to hex: " + series + " series number: " + seriesNumber);

		return seriesNumber;
	}
	
	/**
	 * Metodo che recupera il numero di uno studio
	 * @param study identificativo dello study
	 * @return numero dello studio
	 */
	public String getStudyNumber(String study) {
		byte[] bytes = study.getBytes();
		CRC32 crc = new CRC32();
		crc.update(bytes, 0, bytes.length);
		String studyNumber = Long.toHexString(crc.getValue()).toString().toUpperCase();
		log.info("Study to hex: " + study + " study number: " + studyNumber);
		
		return studyNumber;
	}
}
