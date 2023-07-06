/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.worklist.utils;

import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;

import java.util.HashMap;

import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

public class WlKeys {
    
    public final static HashMap<Integer, Integer[]> requiredFilterKeys = new HashMap<Integer, Integer[]>();
    public final static HashMap<Integer, Integer[]> optionalFilterKeys = new HashMap<Integer, Integer[]>();
    public final static HashMap<Integer, String> filterKeysOnDb = new HashMap<Integer, String>();
    public static HashMap<Key, Key[]> returnKeys = new HashMap<Key, Key[]>();
    
    public static String getDbColumnNameFromTag(Integer tag){
        return filterKeysOnDb.get(tag);
    }
    
    
    static {
        requiredFilterKeys.put(Tag.ScheduledProcedureStepSequence, new Integer[] {
                Tag.ScheduledStationAETitle,
                Tag.ScheduledProcedureStepStartDate,
                Tag.ScheduledProcedureStepStartTime,
                Tag.Modality,
                Tag.ScheduledPerformingPhysicianName
        });
        requiredFilterKeys.put(Tag.RequestedProcedureID, null);
        requiredFilterKeys.put(Tag.AccessionNumber, null);
        requiredFilterKeys.put(Tag.PatientName, null);
        requiredFilterKeys.put(Tag.PatientID, null);
    };
    
    static{
        optionalFilterKeys.put(Tag.CurrentPatientLocation, null);
    }
    
    
    static{
        filterKeysOnDb.put(Tag.ScheduledStationAETitle, "SSAETitle");
        filterKeysOnDb.put(Tag.ScheduledProcedureStepStartDate,"SPSStartDate");
        filterKeysOnDb.put(Tag.ScheduledProcedureStepStartTime,"SPSStartTime");
        filterKeysOnDb.put(Tag.Modality,"Modality");
        filterKeysOnDb.put(Tag.ScheduledPerformingPhysicianName,"SPPhysicianName");
        filterKeysOnDb.put(Tag.ScheduledProcedureStepDescription,"SPSDescription");
        filterKeysOnDb.put(Tag.ScheduledProcedureStepID,"SPSId");
        filterKeysOnDb.put(Tag.RequestedProcedureDescription,"ReqProcDescription");
        filterKeysOnDb.put(Tag.RequestedProcedureID,"RPId");
        filterKeysOnDb.put(Tag.StudyInstanceUID,"StudyInstUID");
        filterKeysOnDb.put(Tag.ReferencedSOPClassUID + Tag.ReferencedStudySequence,"StSeqRefSOPClassUID");
        filterKeysOnDb.put(Tag.ReferencedSOPInstanceUID + Tag.ReferencedStudySequence,"StSeqRefSOPInstUID");
        filterKeysOnDb.put(Tag.AccessionNumber,"AccessionNumber");
        filterKeysOnDb.put(Tag.RequestingPhysician,"ReqPhysician");
        filterKeysOnDb.put(Tag.ReferringPhysicianName,"RefPhysicianName");
        filterKeysOnDb.put(Tag.AdmissionID,"AdmissionID");
        filterKeysOnDb.put(Tag.CurrentPatientLocation,"CurrPatLoc");
        filterKeysOnDb.put(Tag.ReferencedSOPClassUID + Tag.ReferencedPatientSequence,"PatSeqSOPClassUID");
        filterKeysOnDb.put(Tag.ReferencedSOPInstanceUID + Tag.ReferencedPatientSequence,"PatSeqSOPInstUID");
        filterKeysOnDb.put(Tag.PatientName,"PatientName");
        filterKeysOnDb.put(Tag.PatientID,(GlobalConfigurationLoader.getConfigParam("WlPatientID")!=null)?GlobalConfigurationLoader.getConfigParam("WlPatientID"):"PatientID");
        filterKeysOnDb.put(Tag.PatientBirthDate,"PatBirthDate");
        filterKeysOnDb.put(Tag.PatientSex,"PatSex");
        filterKeysOnDb.put(Tag.PregnancyStatus,"PatPregnStatus");
        filterKeysOnDb.put(Tag.ConfidentialityConstraintOnPatientDataDescription,"ConfConstrPatData");
        filterKeysOnDb.put(Tag.PatientState,"PatState");
        filterKeysOnDb.put(Tag.MedicalAlerts,"PatMedicalAlerts");
        filterKeysOnDb.put(Tag.Allergies,"PatAllergies");
        filterKeysOnDb.put(Tag.PatientWeight,"PatWeight");
        filterKeysOnDb.put(Tag.SpecialNeeds,"PatSpecialNeeds");      
    };
    
    
    static{
        returnKeys.put(new Key(Tag.ScheduledProcedureStepSequence, 1, VR.SQ), new Key[]{
            new Key(Tag.ScheduledStationAETitle, 1, VR.AE),
            new Key(Tag.ScheduledProcedureStepStartDate, 1, VR.DA),
            new Key(Tag.ScheduledProcedureStepStartTime, 1, VR.TM),
            new Key(Tag.ScheduledPerformingPhysicianName, 2, VR.PN),
            new Key(Tag.ScheduledProcedureStepDescription, 1, VR.LO),
            new Key(Tag.ScheduledProcedureStepID, 1, VR.SH),
            new Key(Tag.Modality, 1, VR.SH)
        });
        returnKeys.put(new Key(Tag.RequestedProcedureDescription, 1, VR.LO), null);
        returnKeys.put(new Key(Tag.RequestedProcedureID, 1, VR.SH), null);
        returnKeys.put(new Key(Tag.StudyInstanceUID, 1, VR.UI), null);
        returnKeys.put(new Key(Tag.ReferencedStudySequence, 2, VR.SQ), new Key[]{
            new Key(Tag.ReferencedSOPClassUID, 1, VR.UI),
            new Key(Tag.ReferencedSOPInstanceUID, 1, VR.UI)
        });
        returnKeys.put(new Key(Tag.AccessionNumber, 2, VR.SH), null);
        returnKeys.put(new Key(Tag.RequestingPhysician, 2, VR.PN), null);
        returnKeys.put(new Key(Tag.ReferringPhysicianName, 2, VR.PN), null);
        returnKeys.put(new Key(Tag.AdmissionID, 2, VR.LO), null);
        returnKeys.put(new Key(Tag.CurrentPatientLocation, 2 , VR.LO), null);
        returnKeys.put(new Key(Tag.ReferencedPatientSequence, 2, VR.SQ), new Key[]{
            new Key(Tag.ReferencedSOPClassUID, 1, VR.UI),
            new Key(Tag.ReferencedSOPInstanceUID, 1, VR.UI)
        });
        returnKeys.put(new Key(Tag.PatientName, 1, VR.PN), null);
        returnKeys.put(new Key(Tag.PatientID, 1, VR.LO), null);
        returnKeys.put(new Key(Tag.PatientBirthDate, 2, VR.DA), null);
        returnKeys.put(new Key(Tag.PatientSex, 2, VR.CS), null);
        returnKeys.put(new Key(Tag.ConfidentialityConstraintOnPatientDataDescription, 2, VR.LO), null);
        returnKeys.put(new Key(Tag.PatientState, 2, VR.LO), null);
        returnKeys.put(new Key(Tag.PregnancyStatus, 2, VR.US), null);
        returnKeys.put(new Key(Tag.MedicalAlerts,2 , VR.LO), null);
        returnKeys.put(new Key(Tag.Allergies, 2, VR.LO), null);
        returnKeys.put(new Key(Tag.PatientWeight, 2, VR.DS), null);
        returnKeys.put(new Key(Tag.SpecialNeeds, 2, VR.LO), null);
    }

    

    public static class Key{
        public Integer _tag;
        public int _type;
        public VR _vr;
        public Key(Integer tag, int type, VR vr){
            _tag = tag;
            _type = type;
            _vr = vr;
        }
    }
}
