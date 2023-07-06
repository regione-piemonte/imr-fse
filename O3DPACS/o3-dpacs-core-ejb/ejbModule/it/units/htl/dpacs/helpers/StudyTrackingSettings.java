/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.Serializable;

public class StudyTrackingSettings implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private static Boolean studyCompletionTrackingEnabled=null;
	
	public static boolean isStudyCompletionTrackingEnabled(){
		if(studyCompletionTrackingEnabled==null)
			loadSettings();
		return studyCompletionTrackingEnabled.booleanValue();		// In case loadSettings fails, this will throw a NullPointerException
	}
	
	private static synchronized void loadSettings(){
		String v=GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.CONFIG_TRACKSTUDYCOMPLETION);
		studyCompletionTrackingEnabled=((v==null)?new Boolean(false):"1".equals(v));
	}

}
