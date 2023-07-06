/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.pdi.maps;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;

import it.units.htl.maps.util.SessionManager;

public class Utility {

	private static final Logger log = Logger.getLogger(Utility.class);

	protected SessionFactory getSessionFactory() {
		try {
			return SessionManager.getInstance();
		} catch (Exception e) {
			log.error("Could not locate SessionFactory in JNDI", e);
			throw new IllegalStateException("Could not locate SessionFactory in JNDI");
		}
	}

	/**
	 * Inserisce una nuova email per invio notifica non elaborazione.
	 * @param subject oggetto dell'email.
	 * @param body corpo dell'email
     * 
	 */
	public boolean sendEmail(String subject, String body) {
		log.info("sendEmail subject: " + subject + " body: " + body);
		
		boolean		check		= true;
		Session 	s 			= null;
   	 	Transaction tx 			= null;
		 
   	 	try {
    		 
    		 s 	= 	getSessionFactory().openSession();
    		 tx	=	s.beginTransaction();
    		 
    		 s.doWork(new Work() {
				@Override
				public void execute(Connection con) throws SQLException {
					log.info("sendEmail execute");
					CallableStatement cs = null;
					try {
						cs = con.prepareCall("{call send_mail_alert(?,?)}");
						cs.setString(1, subject);
						cs.setString(2, body);
						cs.execute();
					} catch (Exception e) {
						log.error("sendEmail error: " + e.getMessage(), e);
					}finally {
						if(cs != null) {
							cs.close();
						}
						if(con != null) {
							con.close();
						}
					}
					
				}
			});
//    		 Connection con = SessionManager.getInstance().co
//    		 CallableStatement cs  = 
//    		 
//    		 ProcedureCall spq = s.createStoredProcedureCall("send_mail_alert");
//    		 spq.registerParameter("p_subject", String.class,  ParameterMode.IN);
//    		 spq.registerParameter("p_body", String.class, ParameterMode.IN);
//    		
//    		 spq.getOutputs();
    		 
	    	 
	    	 tx.commit();
    	 } catch (RuntimeException rex) {
    		 tx.rollback();
             log.error("Utility.sendEmail: ", rex);
             throw rex;
         } catch(Exception e) { 
        	 tx.rollback();
             log.error("Utility.sendEmail:", e);
             throw e;
         } finally {
             try {
            	 if(s != null && s.isOpen()) {
            		 s.close();
            	 }
             } catch (Exception ex) {
            	 log.error("Utility.sendEmail:", ex);
             }
         }
         return check;	
	}
}
