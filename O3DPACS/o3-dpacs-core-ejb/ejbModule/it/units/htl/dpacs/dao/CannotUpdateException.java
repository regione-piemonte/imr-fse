/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

public class CannotUpdateException extends Exception{


	private static final long serialVersionUID = 1L;
	public static final String NULL_ARGS="Some argument passed as null";
	public static final String PATIENT_KEY_NOT_SET="Patient Primary Key not set";
	public static final String SOP_CL_NOT_SUPPORTED="Sop Class not supported";
	public static final String SERIES_PROBLEMS="Problems updating series";
	public static final String STUDY_PROBLEMS="Problems updating study";
	public static final String PATIENT_PROBLEMS="Problems updating patient";
	
	public static final String HL7_COULDNOTIDENTIFYOLDPATIENT="No 'old' patient could be identified";
	public static final String HL7_COULDNOTIDENTIFYNEWPATIENT="No 'new' patient could be identified nor created";
	public static final String HL7_NOVISITFORSTUDY="No visit found for specified patient/study";
	public static final String HL7_CANNOTMOVESTUDY = "The study could not be moved";
	public static final String HL7_CANNOTMOVEVISIT = "The visit could not be moved";
		
	public CannotUpdateException(String reason){
		super(reason);
	}
}
