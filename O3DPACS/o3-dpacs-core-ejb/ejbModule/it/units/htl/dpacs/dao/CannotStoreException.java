/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

public class CannotStoreException extends Exception{

	private static final long serialVersionUID = 1L;
	public static final String NULL_PATIENT="Patient passed as null";
	public static final String NULL_SERIES="Series passed as null";
	public static final String NULL_STUDY="Study passed as null";
	public static final String CANC_DISC_STUDY="Study has been cancelled or discontinued";
    public static final String CANNOT_ADD_TO_STUDY="Cannot add to study";
    public static final String CANNOT_ADD_STUDY="Cannot add study: patient/study mismatch";
	public static final String NULL_INSTANCES="Instances passed as null";
	public static final String INSTANCES_PRESENT="Some instances already present";
	public static final String INSTANCES_PROCESSED="Instance already being processed";
	public static final String NO_ID_INFO="Not enough data about patient id";
	public static final String NO_AE_TITLE="The series doesn't specify its AETitle";
	public static final String STUDY_OF_ANOTHER_PAT="The study already belongs to another patient!";
	public static final String PATIENT_INFO_FOR_SAME_PATIENT_ID = "Probably:Patient ID already Present with other patient infos.";
	public static final String STUDY_DELETED = "The study has been deleted, probably after a forward!";
	public static final String STUDY_NOTEDITABLE = "Study not editable!";
	public static final String NO_PATIENT="No patient could be identified or created";
	
	public CannotStoreException(String reason){
		super(reason);
	}

}
