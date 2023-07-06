/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7.comunication.utils;

public class TerserLocations {
    public static class MSH {
        
    }
        
    
    public static class MSA{
        public static String status = "/MSA-1";
    }
    
    public static class ERR{
        public static String errorMessageCode = "/ERR-3-1";
        public static String errorDesc = "/ERR-3-9";
    }
    
    public static class PID {
        public static String patientID = "/PATIENT/PID-3-1";
        public static String idIssuer = "/PATIENT/PID-3-4";
        public static String identificationType = "/PATIENT/PID-3-5";
        public static String lastName  = "/PATIENT/PID-5-1";
        public static String firstName  = "/PATIENT/PID-5-2";
        public static String patientBirthDate = "/PATIENT/PID-7-1";
        public static String patientSex = "/PATIENT/PID-8";
    }
    
    public static class PV1{
        public static String visitNumber = "/PATIENT/PATIENT_VISIT/PV1-19-1";
    }

    public static class ORC {
//      XO sempre
        public static String orderControl = "/ORDER/ORC-1";
//        Accession number
        public static String placeOrderNumber = "/ORDER/ORC-2-1";
//  CM complete
        public static String orderStatus = "/ORDER/ORC-5";
        public static String transactionDate = "/ORDER/ORC-9";
    }
    
    public static class OBR{
        public static String placeOrderNumber = "/ORDER/ORDER_DETAIL/OBR-2-1";
        public static String studyType   = "/ORDER/ORDER_DETAIL/OBR-4-1";
        public static String studyDescription = "/ORDER/ORDER_DETAIL/OBR-4-2" ;
    }
    
    public static class ZDS{
        public static String studyUID  = "ZDS-1-1";
        public static String numberOfStudyRelInst = "ZDS-1-2";
        public static String application = "ZDS-1-3";
        public static String type = "ZDS-1-4";
        public static String studyDate = "ZDS-1-5";
    }
    
  

}
