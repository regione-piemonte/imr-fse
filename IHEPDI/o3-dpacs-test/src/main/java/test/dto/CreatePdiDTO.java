/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package test.dto;

public class CreatePdiDTO {

	private String testCode;
	private String testDescription;
	private String requestID;
	private String patientID;
	private String studyUID;
	private String accessionNumber;
	private String idIssuer;
	private String azienda;
	private String struttura;
	private CreatePdiResponseDTO expected;
	
	public CreatePdiDTO(String testCode, String testDescription, String requestID, String patientID, String studyUID,
			String accessionNumber, String idIssuer, String azienda, String struttura, CreatePdiResponseDTO expected) {
		this.testCode = testCode;
		this.testDescription = testDescription;
		this.requestID = requestID;
		this.patientID = patientID;
		this.studyUID = studyUID;
		this.accessionNumber = accessionNumber;
		this.idIssuer = idIssuer;
		this.azienda = azienda;
		this.struttura = struttura;
		this.expected = expected;
	}
	
	public String getTestCode() {
		return testCode;
	}
	
	public void setTestCode(String testCode) {
		this.testCode = testCode;
	}
	
	public String getTestDescription() {
		return testDescription;
	}
	
	public void setTestDescription(String testDescription) {
		this.testDescription = testDescription;
	}
	
	public String getRequestID() {
		return requestID;
	}
	
	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}
	
	public String getPatientID() {
		return patientID;
	}
	
	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}
	
	public String getStudyUID() {
		return studyUID;
	}
	
	public void setStudyUID(String studyUID) {
		this.studyUID = studyUID;
	}
	
	public String getAccessionNumber() {
		return accessionNumber;
	}
	
	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}
	
	public String getIdIssuer() {
		return idIssuer;
	}
	
	public void setIdIssuer(String idIssuer) {
		this.idIssuer = idIssuer;
	}
	
	public String getAzienda() {
		return azienda;
	}
	
	public void setAzienda(String azienda) {
		this.azienda = azienda;
	}
	
	public String getStruttura() {
		return struttura;
	}
	
	public void setStruttura(String struttura) {
		this.struttura = struttura;
	}

	public CreatePdiResponseDTO getExpected() {
		return expected;
	}

	public void setExpected(CreatePdiResponseDTO expected) {
		this.expected = expected;
	}
	
	@Override
	public String toString() {
		return "CreatePdiDTO [testCode=" + testCode + ", testDescription=" + testDescription + ", requestID="
				+ requestID + ", patientID=" + patientID + ", studyUID=" + studyUID + ", accessionNumber="
				+ accessionNumber + ", idIssuer=" + idIssuer + ", azienda=" + azienda + ", struttura=" + struttura
				+ ", expected=" + expected.toString() + "]";
	}
}
