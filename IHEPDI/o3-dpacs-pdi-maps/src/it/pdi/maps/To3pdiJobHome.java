/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.pdi.maps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import it.units.htl.maps.util.SessionManager;


public class To3pdiJobHome {
	private static final Logger log = Logger.getLogger(To3pdiJobHome.class);

    protected SessionFactory getSessionFactory() {
        try {
        		return SessionManager.getInstance();
        } catch (Exception e) {
            log.error("Could not locate SessionFactory in JNDI", e);
            throw new IllegalStateException("Could not locate SessionFactory in JNDI");
        }
    }

    /**
	 * Inserisce un nuovo job.
	 * @param jobId l'identificativo del job.
	 * @param codice il codice dell'operazione.
	 * @param descrizione la descizione dell'operazione.
	 * @param operazione l'operazione del job.
	 * @param stato lo stato del job.
     * @throws Exception 
	 */
	public To3pdiJob insertTo3pdiJob(String codice, String descrizione, String operazione, String stato, String azienda) throws Exception {
		log.info(" codice: " + codice + " descrizione: " + descrizione + " operazione: " + operazione + " stato: " + stato);
		
		String 		res			= null;
		String		jobId		= null;
		Session 	s 			= null;
   	 	Transaction tx 			= null;
		To3pdiJob 	job 		= prepareEntity(null, codice, descrizione, operazione, stato);
		 
    	 try {
    		 s 	= 	getSessionFactory().openSession();
    		 tx	=	s.beginTransaction();
    		 s.save(job);
	    	 
	    	 if (job != null && job.getPk()!= null) {
	    		 res = String.format("%" + (8) + "s", "" + job.getPk()).replace(" ", "0");
	    		 jobId = azienda + "-" + res;
	    		 job.setJobid(jobId);
	    		 log.info("Procedo all'update per jobId: " + jobId);
	    		 updateTo3pdiJob(jobId, job.getPk(), s, tx);
	    	 }
	    	 
	    	 tx.commit();
    	 } catch (RuntimeException rex) {
    		 tx.rollback();
             log.error("insertTo3pdiJob", rex);
             throw rex;
         } catch(Exception e) { 
        	 tx.rollback();
             log.error("insertTo3pdiJob", e);
             throw e;
         } finally {
             try {
            	 if(s != null && s.isOpen()) {
            		 s.close();
            	 }
             } catch (Exception ex) {
            	 log.error("insertTo3pdiJob", ex);
             }
         }
         return job;	
	}
	
	
	/**
	 * Metodo che aggiorna lo stato di un job.
	 * @param jobId identificativo univoco del job.
	 * @param pk primary key 
	 */
	private void updateTo3pdiJob(String jobId, Long pk, Session s, Transaction tx) {
		log.info("jobId: " + jobId + " pk: " + pk);
		
		 try {    		 
    		 Query query = s.createQuery("UPDATE To3pdiJob SET job_Id = :jobId WHERE pk = :pk");
    		 query.setParameter("jobId", jobId);
    		 query.setParameter("pk", pk);
    		 query.executeUpdate();	    	
    	 } catch (RuntimeException rex) {
             log.error("updateTo3pdiJob", rex);
             throw rex;
         } 
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
		
		log.info("jobId: " + jobId + " codice: " + codice + " descrizione: " + descrizione + " operazione: " + operazione + " stato: " + stato + " digest: " + digest + " zipname: " + zipname + " request: " + request);
		
		Session 	s 			= null;
   	 	Transaction tx 			= null;
			
		 try {
    		 s 	= 	getSessionFactory().openSession();
    		 tx	=	s.beginTransaction();
    		 
    		 Query query =  null;
    		 if(isLast) {
    			 query = s.createQuery("UPDATE To3pdiJob SET datafine = SYSDATE, codice = :codice, descrizione = :descrizione, operazione = :operazione, stato = :stato, checksum = :digest, zipname = :zipname, request = :request WHERE job_Id = :jobId");
    			 //query.setParameter("datafine", new java.sql.Date(System.currentTimeMillis()));
    		 } else {
    			 query = s.createQuery("UPDATE To3pdiJob SET codice = :codice, descrizione = :descrizione, operazione = :operazione, stato = :stato, checksum = :digest, zipname = :zipname, request = :request WHERE job_Id = :jobId");
    		 }
    		 query.setParameter("codice", codice);
    		 query.setParameter("descrizione", descrizione);
    		 query.setParameter("operazione", operazione);
    		 query.setParameter("stato", stato);
    		 query.setParameter("digest", digest);
    		 query.setParameter("zipname", zipname);
    		 query.setParameter("request", request);
    		 query.setParameter("jobId", jobId);
    		 query.executeUpdate();
 
    		 tx.commit();
    	 } catch (RuntimeException rex) {
    		 tx.rollback();
             log.error("updateTo3pdiJob", rex);
             throw rex;
         } finally {
             try {
            	 if(s != null && s.isOpen()) {
            		 s.close();
            	 }
             } catch (Exception ex) {
            	log.error("updateTo3pdiJob", ex);
             }
         }
	}
	
	/**
	 * Metodo per il recupero dei job scaduti
	 * @param expireAt tempo su cui si basa la scadenza dei job
	 * @return lista di job scaduti
	 */
	public List<ExpiredJob> getExpiredJob(int expireAt) {
		List<ExpiredJob> 	jobs 		= new ArrayList<ExpiredJob>();
		List<String> 	status 		= new ArrayList<String>();
		Date 			currentDate = new java.sql.Date(System.currentTimeMillis());
		List			result		= null;
		To3pdiJob 		job			= null;
		
		log.info("expireAt: " + expireAt);
		
		Session 	s 			= null;
   	 	Transaction tx 			= null;
			
		 try {
    		 s 	= 	getSessionFactory().openSession();
    		 tx	=	s.beginTransaction();
    		 
    		 status.add("SUCCESS");
    		 status.add("FAILED");
    		 
    		 Query query = s.createQuery("SELECT t FROM To3pdiJob t WHERE t.stato NOT IN (:status) and t.datafine is null");
    		 query.setParameterList("status", status);
    		 
    		 result = query.list();
    		 if(result != null) {
	    		for(int i = 0; i< result.size(); i++ ) {
	    			job	= (To3pdiJob) result.get(i);
	 				String jobId = job.getJobid();
	 				String zipName = job.getZipname();
	 				String request = job.getRequest();
	 				Date dataInizio = job.getDatainizio();
	 				String digest = job.getChecksum();
	 				log.info("Job is " + jobId + ", date of start is " + dataInizio);
	 				if (job != null && dataInizio != null) {
	 					long diffInMillies = Math.abs(currentDate.getTime() - dataInizio.getTime());
	 				    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	 				    if (diff >= expireAt) {
	 				    	log.info(jobId + " has expired");
	 				    	jobs.add(new ExpiredJob(jobId, zipName, request,digest));
	 				    } else {
	 				    	log.info(jobId + " is not an expired job");
	 				    }
	 				}
	 			}
    		 }
	    	
    		 tx.commit();
    	 } catch (RuntimeException rex) {
    		 tx.rollback();
             log.error("getExpiredJob", rex);
             throw rex;
         } finally {
             try {
            	 if(s != null && s.isOpen()) {
            		 s.close();
            	 }
             } catch (Exception ex) {
            	 log.error("getExpiredJob", ex);
             }
         }
		
		return jobs;
	}
	
	
	/**
	 * Metodo per il recupero dei job scaduti
	 * @param expireAt tempo su cui si basa la scadenza dei job
	 * @return lista di job scaduti
	 */
	public boolean checkRequestIDExist(String requestID) {
		List<ExpiredJob>	result		= null;
		To3pdiJob 			job			= null;
		Boolean				check 		= false;
		
		log.info("requestID: " + requestID);
		
		Session 	s 			= null;
   	 	Transaction tx 			= null;
			
		 try {
    		 s 	= 	getSessionFactory().openSession();
    		 tx	=	s.beginTransaction();
    		   		 
    		 Query query = s.createQuery("SELECT t FROM To3pdiJob t WHERE t.request = :requestID");
    		 query.setParameter("requestID", requestID);
    		 
    		 result = query.list();
    		 if(result != null && result.size() > 0) {
    			 check = true;
    		 }
	    	
    		 tx.commit();
    	 } catch (RuntimeException rex) {
    		 tx.rollback();
             log.error("checkRequestIDExist", rex);
             throw rex;
         } finally {
             try {
            	 if(s != null && s.isOpen()) {
            		 s.close();
            	 }
             } catch (Exception ex) {
            	 log.error("getExpiredJob", ex);
             }
         }
		
		return check;
	}
	
	
	private To3pdiJob prepareEntity(String jobId, String codice, String descrizione, String operazione, String stato) {
		To3pdiJob 	job 		= new To3pdiJob();
		
		if(jobId != null && !jobId.equals("")) {
			job.setJobid(jobId);
		}
		
		if(codice != null && !codice.equals("")) {
			job.setCodice(codice);
		}
		
		if(descrizione != null && !descrizione.equals("")) {
			job.setDescrizione(descrizione);
		}
		
		if(operazione != null && !operazione.equals("")) {
			job.setOperazione(operazione);
		}
		
		if(stato != null && !stato.equals("")) {
			job.setStato(stato);
		}
		
		return job;
	}
}
