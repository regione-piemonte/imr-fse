/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package dpacspdi;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.apache.log4j.Logger;

import dao.PdiRetrieveManager;
import helpers.ConfigurationSettings;
import helpers.ResponseCode;
import helpers.ResponseMessage;
import it.pdi.maps.ExpiredJob;
import utils.StreamUtils;

/**
 * Classe batch che si oocupa di verificare se ci sono job scaduti,
 * in caso affermativo vengono eliminati
 */
public class ExpirationThread implements Runnable {

	private Logger log = Logger.getLogger(ExpirationThread.class);

	/**
	 * Tempo di scadenza per i job non completati
	 */
	private int expireAt;
	
	private static final String EXPIRATION_STATUS = "EXPIRED";
	private static final String EXPIRATION_DESC = "JOB NOT WORKED FINE EXPIRED FORCE";
	private static final int DEFAULT_EXPIRE_AT = 1;
	private static final String WORKAREA = "Workarea";
	private static final String BATCH_SLEEP = "BatchSleep";
	
	public ExpirationThread() {}
	
	/**
	 * Permette la costruzione di un thread per verificare
	 * l'esistenza di eventuali job scaduti
	 * @param expireAt eventuale valore personalizzato per determinare la scadenza dei job
	 */
	public ExpirationThread(int expireAt) {
		this.expireAt = expireAt;
	}
	
	/**
	 * Recupera i job non completati e per ognuno di essi verifica che la data di scadenza
	 * sia passata, se cosi' dovesse essere allora il job verra' posto in uno stato expired 
	 * e l'eventuale workarea creata per il job al tempo di esecuzione verra' cancellata
	 */
	@Override
	public void run() {
		try {
			// If it is not given there's a default value
			if (expireAt < 1) {
				expireAt = DEFAULT_EXPIRE_AT;
			}
		
			PdiRetrieveManager prm = new PdiRetrieveManager();
			String workarea = prm.getConfigParamPDI(WORKAREA);
			int sleep = Integer.parseInt(prm.getConfigParamPDI(BATCH_SLEEP));
			while (true) {
				log.info("Cleaning expired jobs...");
				List<ExpiredJob> jobs = prm.getExpiredJob(expireAt);
				if (jobs != null && !jobs.isEmpty()) {
					for (ExpiredJob job : jobs) {
						log.info("Setting the job: " + job.getJobId() + " as expired...");
						prm.updateTo3pdiJob(job.getJobId(), ResponseCode.CC_ERR_300, ResponseMessage.CC_ERR_300, "", EXPIRATION_STATUS, "", job.getZipName(), job.getRequestId(), true);
						log.info("Cleaning workare for the job...");
						File directoryToClean = new File(workarea + job.getZipName());
						if (directoryToClean.exists()) {
							log.info("Cleaning directory " + directoryToClean.getAbsolutePath() + "...");
							StreamUtils.cleanLocalDirectory(directoryToClean);
						}
						
						sendCompleteNotice(job.getJobId(), job.getRequestId(), EXPIRATION_STATUS, null, job.getDigest(), job.getZipName(), prm);
					}
					log.info("Expired jobs cleaned");
				} else {
					log.info("Didn't find any expired job");
				}
				
				log.info("Waiting for next round...");
				Thread.sleep(sleep);
			}
		} catch(Exception e) {
			log.error("Batch interrupted", e);
			throw new RuntimeException("Batch interrupted");
		}
	}
	
	/**
	 * Invia notifica di fine operazione alle ASL
	 * @param jobId l'identificativo del job
	 * @param dist directory dello zip risultante
	 * @param status risultato dell'operazione
	 * @param requestID id della richiesta
	 */
	private void sendCompleteNotice(String jobId, String requestID, String status, String azienda, String digest, String zipname, PdiRetrieveManager prm) {
		log.info("Sending notice for job: " + jobId + "...");		
		int code = 0;
		String msg = "";
		String noticeUrl = getSendCompleteNoticeUrl(jobId, requestID, status, null, azienda, digest, zipname,prm);
		String numRetryNotifica = prm.getConfigParamPDI(ConfigurationSettings.NUM_RETRY_NOTIFICA);
		String usernameFse = prm.getConfigParamPDI(ConfigurationSettings.USERNAME_FSE);
		String passwordFse = prm.getConfigParamPDI(ConfigurationSettings.PASSWORD_FSE);
		String sleepNotifica = prm.getConfigParamPDI(ConfigurationSettings.SLEEP_NOTIFICA);
		int retries = Integer.parseInt(numRetryNotifica);
		for (int retry = 1; retry <= retries; retry++) {
			try {
				URL url = new URL(noticeUrl);
				URLConnection connection = url.openConnection();
				((HttpURLConnection) connection).setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setRequestProperty("Authorization", "Basic " + buildSendCompleteNoticeBasicAuth(usernameFse, passwordFse));
				code = ((HttpURLConnection) connection).getResponseCode();
				msg = ((HttpURLConnection) connection).getResponseMessage();
				log.info("Code for sendCompleteNotice: " + code + ", message: " + msg);
				if (code != 200) {
					log.info("Failed to sent notice with status: " + code + " due to: " + msg);
				} else {
					log.info("Notice sent successfully");
					return;
				}
			} catch (Exception e) {
				log.info("Error while sending complete notice due to: " + e.getMessage());
				e.printStackTrace();
			} finally {
				if (code != 200) {
					log.info("Retrying sendCompleteNotice... " + retry);
					StreamUtils.waitForRetry(Integer.parseInt(sleepNotifica));
					if (retry == retries) {
						log.info("Cannot send complete notice");
					} else {
						log.info("Riprovare la chiamata al servizio wado => requestType=CompleteNotice, count: " + retry);
					}
				}
			}
		}
	}
	
	/**
	 * Fornisce l'URL del servizio di sendCompleteNotice
	 * @param jobId l'identificativo del job complemtato
	 * @param dist directory dello zip risultante
	 * @param status risultato dell'operazione
	 * @param requestID id della richiesta
	 * @return l'URL del servizio sendCompleteNotice
	 */
	private String getSendCompleteNoticeUrl(String jobId, String requestID, String status, String dist, String azienda, String digest, String zipName, PdiRetrieveManager prm) {
		StringBuilder noticeUrlBuilder = new StringBuilder(prm.getConfigParamPDI(ConfigurationSettings.COMPLETE_NOTICE_URL))
			.append("?zipName=")
			.append(zipName)
			.append(".zip")
			.append("&jobUID=")
			.append(jobId)
			.append("&status=")
			.append(status);
		
		if (requestID != null && !requestID.isEmpty()) {
			noticeUrlBuilder.append("&requestID=").append(requestID);
		}
		
		if (digest != null && !digest.isEmpty()) {
			noticeUrlBuilder.append("&checksum=").append(digest);
		}
		
		if (dist != null && !dist.isEmpty()) {
			dist = dist.replaceAll("/", "2F");
			noticeUrlBuilder.append("&dist=").append(dist);
		}
		
		noticeUrlBuilder.append("codeError="+ResponseCode.CC_ERR_300);
		
		String noticeUrl = noticeUrlBuilder.toString();
		log.info("Url di chiamata: " + noticeUrl);
		
		return noticeUrl;
	}
	
	/**
	 * Costruisce le credenziali per il servizio sendCompleteNotice.
	 * @return le credenziali per il servizio sendCompleteNotice.
	 */
	private String buildSendCompleteNoticeBasicAuth(String usernameFse, String passwordFse) {
		return Base64.getEncoder().encodeToString((usernameFse + ":" + passwordFse)
				.getBytes(StandardCharsets.UTF_8));
	}
}
