/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.pdi.maps;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import it.units.htl.maps.util.SessionManager;

public class To3pdiConfHome {
    private static final Logger log = Logger.getLogger(To3pdiConfHome.class);

    protected SessionFactory getSessionFactory() {
        try {
        		return SessionManager.getInstance();
        } catch (Exception e) {
            log.error("Could not locate SessionFactory in JNDI", e);
            throw new IllegalStateException("Could not locate SessionFactory in JNDI");
        }
    }
    
    /**
     * Metodo che permette di recuperare i parametri di configurazione per PDI
     * @param key identificativo parametro di configurazione
     * @return valore del parametro di configurazione
     */
    public String getConfigParamPDI(String key) {
    	 String 	ret 	= null;
    	 To3pdiConf conf	= new To3pdiConf();
    	 conf.setParamkey(key);
    	 Session s = null;
    	 Criteria criteria = null;
    	 try {
    		 s = getSessionFactory().openSession();
	    	 criteria = s.createCriteria(To3pdiConf.class).add(Restrictions.eq("paramkey", key));
	    	 conf = (To3pdiConf) criteria.uniqueResult();
	    	 
	    	 if(conf != null) {
	    		 ret = conf.getParamvalue();
	    	 }
    	 } catch (RuntimeException rex) {
             log.error("getConfigParamPDI", rex);
             throw rex;
         } finally {
             try {
                 s.close();
             } catch (Exception ex) {
            	 log.error("getConfigParamPDI", ex);
             }
         }
         
         return ret;
    }

  
    /**
	 * Metodo che aggiorna il valore di un parametro di configurazione.
	 * @param key identificativo univoco della chiave.
	 * @param value nuovo valore.
	 */
	public void updateTo3pdiConf(String key, String value) {
		log.info("key: " + key + " value: " + value);
		 Session s = null;
		 try {
			 s = getSessionFactory().openSession();
			
    		 Query query = s.createQuery("UPDATE To3pdiConf SET PARAMVALUE = :value WHERE PARAMKEY = :key");
    		 query.setParameter("value", value);
    		 query.setParameter("key", key);
    		 query.executeUpdate();	    
    		 
    	 } catch (RuntimeException rex) {
             log.error("updateTo3pdiJob", rex);
             throw rex;
         } finally {
             try {
                 s.close();
             } catch (Exception ex) {
            	 log.error("getConfigParamPDI", ex);
             }
         }
         
	}

   
}
