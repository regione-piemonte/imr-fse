/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PatientIdCheckSettings  implements Serializable {
private static Log log = LogFactory.getLog(IheSettings.class);
    
    private static final long serialVersionUID = 1L;
        
    private static String verificationRegEx=null;
    
    public static String getPatientIdRegEx(){
        return verificationRegEx;// In case loadSettings fails, this will throw a NullPointerException
    }
    
    public  static synchronized void loadSettings(){
        String v=GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.PATIENT_ID_REGEX);
        if(v!=null){
            log.warn("THE PATIENT ID WILL NOT BE CHECKED!! with this regex: " + v + " . Other patientId will be ");
        }
        verificationRegEx = v;
    }
}
