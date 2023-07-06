/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

public class IheStudyId {

	private String studyInstanceUid;
	private String accessionNumber;
	private String patientId;
	
	
	public IheStudyId(String studyInstanceUid, String accessionNumber, String patientId) {
		this.studyInstanceUid = studyInstanceUid;
		this.accessionNumber = accessionNumber;
		this.patientId = patientId;
	}
	
	
	public String getStudyInstanceUid() {
		return studyInstanceUid;
	}
	public void setStudyInstanceUid(String studyInstanceUid) {
		this.studyInstanceUid = studyInstanceUid;
	}
	public String getAccessionNumber() {
		return accessionNumber;
	}
	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}
	public String getPatientId() {
		return patientId;
	}
	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}
	
	
}
