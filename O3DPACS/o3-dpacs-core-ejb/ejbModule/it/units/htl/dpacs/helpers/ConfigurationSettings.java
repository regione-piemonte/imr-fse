/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

public class ConfigurationSettings {

	public static final String CONFIG_IS_IHE = "IS_IHE";
	public static final String CONFIG_KEY = "DS_KEY";
	public static final String CONFIG_WADOURL = "WadoUrl";
	public static final String CONFIG_TRACKSTUDYCOMPLETION = "trackStudyCompletion";
	public static final String CONFIG_PARTITIONINGSTRATEGY = "partitioningStrategy";
	public static final String CONFIG_GATEWAY = "ActAsGateway";
	public static final String CONFIG_ACCEPT_DIFFERENT_ACCNUM= "acceptDifferentAccNum";

	public static final String MBEANSERVER_NAME = "MBeanServerName";
	public static final String JMX_CONNECTOR_PORT = "JMX_CONNECTOR_PORT";

	public static String CONFIG_WADOURL_FLEX = "WadoUrlForFlex";
	/**
	 * Parameters: password, expirationDate
	 */
	public static final String EMAIL_EVENT_NEWPASSWORD = "NewPwd";

	/**
	 * Parameters: username, password, expirationDate
	 */
	public static final String EMAIL_EVENT_ADDEDASUSER = "AddedAsUser";

	/**
	 * Parameters: expirationDate
	 */
	public static final String EMAIL_EVENT_CHANGEDPASSWORD = "ChangedPwd";

	/**
	 * Parameters: originalSeries, newSeries, error
	 */
	public static final String EMAIL_EVENT_ERRORINMF = "ErrorInMultiFrame";

	/**
	 * Parameters: only one string with the list of every unverified studies
	 */
	public static final String EMAIL_EVENT_STUDYVERIFIER = "StudyVerifier";

	/**
	 * Parameters: only one string with the list of any physicalMedia name
	 * running out of space
	 */
	public static final String EMAIL_EVENT_PMOUTOFSPACE = "PmOutOfSpace";

	/**
	 * Parameters: missing mandatory attribute, one string with the list of
	 * all problematic studies
	 */
	public static final String EMAIL_EVENT_REMOVEDVERIFIEDSTUDIES = "RemVerifiedStudies";

	public static final String EMAIL_MESSAGE_NOFRAMES = "No frames to use for Multiframe";
	public static final String EMAIL_MESSAGE_UNKNOWNREASON = "Unknown reason";
	public static final String EMAIL_MESSAGE_DBERROR = "Could not update db metadata";

	public static final String PWD_CANREPEAT = "PWD_CANREPEAT";
	public static final String PWD_MINIMUM_LENGTH = "PWD_MINIMUM_LENGTH";
	public static final String PWD_LETTERS_MAND = "PWD_LETTERS_MAND";
	public static final String PWD_DIGITS_MAND = "PWD_DIGITS_MAND";
	public static final String PWD_SYMBOLS_MAND = "PWD_SYMBOLS_MAND";
	public static final String PWD_BOTHCASES_MAND = "PWD_BOTHCASES_MAND";
	public static final String PWD_VALIDITY_DAYS = "PWD_VALIDITY_DAYS";
	public static final String PWD_WARN_DAYS_B4_EXP = "PWD_WARN_DAYS_B4_EXP";

	public static final String XDS_SOURCE_ID = "XDS_SOURCE_ID";
	public static final String XDS_REPOSITORY_URL = "XDS_REPOSITORY_URL";
	public static final String XDS_REGISTRY_URL = "XDS_REGISTRY_URL";
	public static final String XDS_KOS_TEMP_URL = "XDS_KOS_TEMP_URL";
	public static final String XDS_SINGLE_KOS_TEMP_URL = "XDS_SINGLE_KOS_TEMP_URL";
	
	
	public static final String STUDYCLOSE_STORAGE = "S";
	public static final String STUDYCLOSE_MPPS = "M";
	public static final String STUDYCLOSE_STGCMT = "C";
	
	public static final String PATIENT_ID_REGEX = "PatientIdRegEx";
	
	public static final String DEFAULT_ID_ISSUER = "DEFAULT_ID_ISSUER";
	
	public static final String AUTO_RECONCILIATION= "autoReconciliation";
	
	public static final String ENC_KEY = "dencodingKey";
	
	public static final String DB_DATE_FORMAT = "dbDateFormat";
	
	
}
