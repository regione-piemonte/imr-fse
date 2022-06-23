/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.env;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import it.csi.dmass.dmassbatchcancpacc.client.ScaricoStudiWSBean.GetElencoPacchettiScadutiResponse;
import it.csi.dmass.dmassbatchcancpacc.spring.AppConfig;

public class Context
{	
	public static final String NOME_BATCH="Cancella Pacchetti Scaduti Batch";
	
    private static Context instance;
    private Properties config;
    
    private String currentStep;                  
    
    private Long id_ser;
    private Timestamp data_ins;
    private GetElencoPacchettiScadutiResponse getElencoPacchettiScadutiResponse;
    private String statoFine;
    
    private static AnnotationConfigApplicationContext context;               

	public static Context instance() {
    	return instance;
    }
    
    public static void init() {
        if (instance == null) {        	
            instance = new Context();             
            context = new AnnotationConfigApplicationContext(AppConfig.class);  
            
            instance.config = (Properties)context.getBean("config");

        }       
    }      
    
    public static <T> T getBean(Class<T> clazz) {
    	return context.getBean(clazz);
    }
                    
    
    public static String getProperty(final String key) {
        return instance.config.getProperty(key);
    }       
    
    public static Date getAudit() {
        try {
			return new SimpleDateFormat("yyyyMMddHHmmss").parse(instance.config.getProperty("AUDIT"));
		} catch (ParseException e) {
			return null;
		}
    }       

	public static String getCurrentStep() {
		return instance.currentStep;
	}

	public static void setCurrentStep(String currentStep) {
		instance.currentStep = currentStep;
	}

	public static Long getId_ser() {
		return instance.id_ser;
	}

	public static void setId_ser(Long id_ser) {
		instance.id_ser = id_ser;
	}

	public static Timestamp getData_ins() {
		return instance.data_ins;
	}

	public static void setData_ins(Timestamp data_ins) {
		instance.data_ins = data_ins;
	}

	public static GetElencoPacchettiScadutiResponse getGetElencoPacchettiScadutiResponse() {
		return instance.getElencoPacchettiScadutiResponse;
	}

	public static void setGetElencoPacchettiScadutiResponse(GetElencoPacchettiScadutiResponse getElencoPacchettiScadutiResponse) {
		instance.getElencoPacchettiScadutiResponse = getElencoPacchettiScadutiResponse;
	}

	public static String getStatoFine() {
		return instance.statoFine;
	}

	public static void setStatoFine(String statoFine) {
		instance.statoFine = statoFine;
	}	
		

	
}
