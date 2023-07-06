/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.launcher.utils;

import java.util.HashMap;

public class UrlPatternManipulator {
    public static enum Pattern {
        patientId("idPat", "%PATIENT_ID%"),
        patientIdLoc("idLocPat", "%PATIENT_ID%"),
        accessionNumber("accNum", "%ACC_NUM%");
        private String code;
        private String paramName;
        
        Pattern(String _paramName, String _code) {
            code = _code;
            paramName = _paramName;
        }

        public String code() {
            return code;
        }
        public String paramName(){
            return paramName;
        }
    };
    
    public static String manipulateUrl(String url, HashMap<String, String> inputs){
        String output = "";
        output = url.replaceFirst(Pattern.accessionNumber.code, inputs.get(Pattern.accessionNumber.paramName));
        if(inputs.containsKey(Pattern.patientIdLoc.paramName)){
            output = output.replaceFirst(Pattern.patientIdLoc.code, inputs.get(Pattern.patientIdLoc.paramName));
        }else{
            output = output.replaceFirst(Pattern.patientId.code, inputs.get(Pattern.patientId.paramName));
        }
        
        return output;
    }
}
