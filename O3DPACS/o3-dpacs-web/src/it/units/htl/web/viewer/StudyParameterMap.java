/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.viewer;

import java.util.HashMap;
import java.util.Map;


public class StudyParameterMap {
	
	private static final String INPUTPARAM_ACCESSIONNUMBER="accessionnumber";
	private static final String INPUTPARAM_STUDYINSTANCEUID="studyinstanceuid";
	private static final String INPUTPARAM_STUDYUID="studyuid";
	
	// These strings will be used to set a particular field of a study using reflection, so the case convention must be preserved!
	private static final String OUTPUTFIELD_ACCESSIONNUMBER="AccessionNumber";
	private static final String OUTPUTFIELD_STUDYINSTANCEUID="StudyInstanceUid";
	
	private static enum RequestedColumn {accessionNumber, studyInstanceUid, studyUid};

	private static Map<String, RequestedColumn> mapping = new HashMap<String, RequestedColumn>();
	
	static{	
		mapping.put(INPUTPARAM_ACCESSIONNUMBER, RequestedColumn.accessionNumber);
		mapping.put(INPUTPARAM_STUDYINSTANCEUID, RequestedColumn.studyInstanceUid);
		mapping.put(INPUTPARAM_STUDYUID, RequestedColumn.studyUid);
	}
	
	
	public static String getMappedField(String paramName){
		if(paramName==null)
			paramName=INPUTPARAM_ACCESSIONNUMBER;
		RequestedColumn req=mapping.get(paramName.toLowerCase());
		
		switch(req){
			case accessionNumber: 
									return OUTPUTFIELD_ACCESSIONNUMBER;
			case studyInstanceUid:
			case studyUid:
									return OUTPUTFIELD_STUDYINSTANCEUID;
			default:
									return OUTPUTFIELD_ACCESSIONNUMBER;
		}
	}

}
