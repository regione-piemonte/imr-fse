/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import it.csi.dmass.dmassbatchcancpacc.env.Context;

public class DmassBatchCancpaccLogger
{
    final Logger LOG;
    
    public DmassBatchCancpaccLogger(final Logger LOG) {
        this.LOG = LOG;
    }
    
    public void error(final Exception t) {
       
    	String audit = Context.getAudit()==null?"":String.valueOf(Context.getAudit());
    	String step = "";
    	if(audit==null) {
    		audit="";
    	}
    	if(Context.getCurrentStep()!=null) {
    		step=Context.getCurrentStep();
    	}
    	    	    	
        this.LOG.error(Context.NOME_BATCH+": "+ audit + " "+ step + " errore: " + t);            
        
    }

	
    public void info(final Object message) {        
    	String audit = Context.getAudit()==null?"":String.valueOf(Context.getAudit());
    	String step = "";
    	if(Context.getCurrentStep()!=null) {
    		step=Context.getCurrentStep();
    	}
        this.LOG.info(Context.NOME_BATCH+": "+audit + " "+ step + ("".equals(step)?"":" - ") + message);     
    }
    
    public void debug(final Object message) {        
    	String audit = Context.getAudit()==null?"":String.valueOf(Context.getAudit());
    	String step = "";
    	if(Context.getCurrentStep()!=null) {
    		step=Context.getCurrentStep();
    	}    	
        this.LOG.debug(Context.NOME_BATCH+": "+audit + " "+ step + " - " + message);
    }
    
    public String getName() {
        return this.LOG.getName();
    }
    
    public void addAppender(final FileAppender fa) {
        this.LOG.addAppender((Appender)fa);
    }
    
    public void addAppender(final ConsoleAppender console) {
        this.LOG.addAppender((Appender)console);
    }
        
}
