/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package helpers;

/**
 * Costanti per la configurazione di O3-DPACS-PDI
 */
public class ConfigurationSettings {
	
	public static final String DEFAULT_ID_ASL = "DEFAULT_ID_ASL";
	
	public static final String DEFAULT_ID_STRUTTURA = "DEFAULT_ID_STRUTTURA";
	
	public static final String XML_FILE_CONF = "DpacspdiConfiguration";
	
	public static final int SLEEP = 5000;
	
	public static final int SLEEP_MOVE = 30000;
	
	public static final String FILE_TO_MOVE_TEMPLATE = "${fileToMove}";
	
	public static final String DIST_TEMPLATE = "${dist}";
	
	public static final String FROM_TEMPLATE = "${from}";
	
	public static final String WADOURL = "WadoUrl";
	
	public static final String PATIENTINFO = "PatientInfo";
	
	public static final String WORKAREA = "Workarea";
	
	public static final String ROOTXML = "asl";
	
	public static final String COMPLETE_NOTICE_URL = "CompleteNoticeUrl";
	
	public static final String USERNAME_FSE = "UserNameFSE";
	
	public static final String PASSWORD_FSE = "PasswordFSE";
	
	public static final String NUM_RETRY_NOTIFICA = "NumRetryNotifica";
	
	public static final String SLEEP_NOTIFICA = "SleepRetryNotifica";
	
	public static final String SCP_VIEWER_TEMPLATE = "ScpViewer";
	
	public static final String IS_COPY_STANDARD_ACTIVATE = "isCopyStandardActivate";
	
	public static final String SPLITTER = ",";
	
	public static final String SERVLET_ACTIVATION = "CreatePdiRequestEnabled";
	
	public static final String SERVLET_ACTIVE = "TRUE";
	
	public static final String NUM_CURRENT_THREAD = "NumCurrentThread";
	
	public static final String NUM_MAX_THREAD = "NumThreadMax";
	
	public static final String DPACS_WEB = "DPACSWeb";
	
	public static final String CHECK_IS_SEND = "CheckIsSend";
}
