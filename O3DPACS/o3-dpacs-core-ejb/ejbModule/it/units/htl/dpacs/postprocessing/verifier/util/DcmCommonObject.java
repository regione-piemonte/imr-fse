/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier.util;

import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;

/**
 * @author Matteo Sangalli
 * 
 */
public class DcmCommonObject {
	public static enum QueryRetrieveLevel {
        PATIENT("PATIENT", PATIENT_RETURN_KEYS, PATIENT_LEVEL_FIND_CUID,
                PATIENT_LEVEL_GET_CUID, PATIENT_LEVEL_MOVE_CUID),
        STUDY("STUDY", STUDY_RETURN_KEYS, STUDY_LEVEL_FIND_CUID,
                STUDY_LEVEL_GET_CUID, STUDY_LEVEL_MOVE_CUID),
        SERIES("SERIES", SERIES_RETURN_KEYS, SERIES_LEVEL_FIND_CUID,
                SERIES_LEVEL_GET_CUID, SERIES_LEVEL_MOVE_CUID),
        IMAGE("IMAGE", INSTANCE_RETURN_KEYS, SERIES_LEVEL_FIND_CUID,
                SERIES_LEVEL_GET_CUID, SERIES_LEVEL_MOVE_CUID);
        
        private final String code;
        private final int[] returnKeys;
        private final String[] findClassUids;
        private final String[] getClassUids;
        private final String[] moveClassUids;

        private QueryRetrieveLevel(String code, int[] returnKeys,
                String[] findClassUids, String[] getClassUids,
                String[] moveClassUids) {
            this.code = code;
            this.returnKeys = returnKeys;
            this.findClassUids = findClassUids;
            this.getClassUids = getClassUids;
            this.moveClassUids = moveClassUids;
        }      
        
        public String getCode() {
            return code;
        }

        public int[] getReturnKeys() {
            return returnKeys;
        }
        
        public String[] getFindClassUids() {
            return findClassUids;
        }
        
        public String[] getGetClassUids() {
            return getClassUids;
        }
        
        public String[] getMoveClassUids() {
            return moveClassUids;
        }
    }

	
    private static final String[] PATIENT_LEVEL_FIND_CUID = {
        UID.PatientRootQueryRetrieveInformationModelFIND,
        UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired };

    private static final String[] STUDY_LEVEL_FIND_CUID = {
        UID.StudyRootQueryRetrieveInformationModelFIND,
        UID.PatientRootQueryRetrieveInformationModelFIND,
        UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired };

    private static final String[] SERIES_LEVEL_FIND_CUID = {
        UID.StudyRootQueryRetrieveInformationModelFIND,
        UID.PatientRootQueryRetrieveInformationModelFIND, };

    private static final String[] PATIENT_LEVEL_GET_CUID = {
        UID.PatientRootQueryRetrieveInformationModelGET,
        UID.PatientStudyOnlyQueryRetrieveInformationModelGETRetired };
    
    private static final String[] STUDY_LEVEL_GET_CUID = {
        UID.StudyRootQueryRetrieveInformationModelGET,
        UID.PatientRootQueryRetrieveInformationModelGET,
        UID.PatientStudyOnlyQueryRetrieveInformationModelGETRetired };

    private static final String[] SERIES_LEVEL_GET_CUID = {
        UID.StudyRootQueryRetrieveInformationModelGET,
        UID.PatientRootQueryRetrieveInformationModelGET };

    private static final String[] PATIENT_LEVEL_MOVE_CUID = {
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    private static final String[] STUDY_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    private static final String[] SERIES_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE };

    private static final int[] PATIENT_RETURN_KEYS = {
        Tag.PatientName,
        Tag.PatientID,
        Tag.PatientBirthDate,
        Tag.PatientSex,
        Tag.NumberOfPatientRelatedStudies,
        Tag.NumberOfPatientRelatedSeries,
        Tag.NumberOfPatientRelatedInstances };

    private static final int[] STUDY_RETURN_KEYS = {
        Tag.StudyDate,
        Tag.StudyTime,
        Tag.AccessionNumber,
        Tag.StudyID,
        Tag.StudyInstanceUID,
        Tag.NumberOfStudyRelatedSeries,
        Tag.NumberOfStudyRelatedInstances,
        Tag.PatientName,
        Tag.PatientID,
        Tag.PatientBirthDate,
        Tag.PatientSex,
        Tag.IssuerOfPatientID
        };


    private static final int[] SERIES_RETURN_KEYS = {
        Tag.Modality,
        Tag.SeriesNumber,
        Tag.SeriesInstanceUID,
        Tag.NumberOfSeriesRelatedInstances,
        Tag.NumberOfStudyRelatedSeries,
        Tag.StorageMediaFileSetID,
        Tag.StorageMediaFileSetUID};

    private static final int[] INSTANCE_RETURN_KEYS = {
        Tag.InstanceNumber,
        Tag.SOPClassUID,
        Tag.SOPInstanceUID, };
}
