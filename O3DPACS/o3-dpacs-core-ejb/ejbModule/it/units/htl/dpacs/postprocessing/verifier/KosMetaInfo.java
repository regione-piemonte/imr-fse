/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KosMetaInfo implements Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    String referencedStudyInstanceUid;
	String patientId;
	String patientIdIssuer;
	String patientName;
	String contentDate;
	String contentTime;
	String retrieveAETitle;
	String referencedAccessionNumber;
	String xdsMessageId;
	
	public String getXdsMessageId() {
		return xdsMessageId;
	}

	public void setXdsMessageId(String xdsAssignedMessageId) {
		this.xdsMessageId = xdsAssignedMessageId;
	}

	public int getAttemptsCounter() {
		return attemptsCounter;
	}

	public void setAttemptsCounter(int attemptsCounter) {
		this.attemptsCounter = attemptsCounter;
	}

	int attemptsCounter;
	
	Map<String, List<String[]>> series;
	
	public String getReferencedAccessionNumber() {
		return referencedAccessionNumber;
	}

	public void setReferencedAccessionNumber(String referencedAccessionNumber) {
		this.referencedAccessionNumber = referencedAccessionNumber;
	}
	
	public KosMetaInfo(){
		series = new HashMap<String, List<String[]>>();
	}

	public String getReferencedStudyInstanceUid() {
		return referencedStudyInstanceUid;
	}

	public void setReferencedStudyInstanceUid(String referencedStudyInstanceUid) {
		this.referencedStudyInstanceUid = referencedStudyInstanceUid;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public String getPatientIdIssuer() {
		return patientIdIssuer;
	}

	public void setPatientIdIssuer(String patientIdIssuer) {
		this.patientIdIssuer = patientIdIssuer;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getContentDate() {
		return contentDate;
	}

	public void setContentDate(String contentDate) {
		this.contentDate = contentDate;
	}

	public String getContentTime() {
		return contentTime;
	}

	public void setContentTime(String contentTime) {
		this.contentTime = contentTime;
	}

	public String getRetrieveAETitle() {
		return retrieveAETitle;
	}

	public void setRetrieveAETitle(String retrieveAETitle) {
		this.retrieveAETitle = retrieveAETitle;
	}

	public Map<String, List<String[]>> getSeries() {
		return series;
	}

}
