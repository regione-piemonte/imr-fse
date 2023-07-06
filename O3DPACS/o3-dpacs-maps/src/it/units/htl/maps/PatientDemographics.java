/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;

import java.io.Serializable;

public class PatientDemographics implements Serializable{

	private static final long serialVersionUID = 8315006348023998672L;
	
	long pk;
	String ethnicGroup;
	String patientComments;
	String race;
	String patientAddress;
	String patientCity;
	String patientAccountNumber;
	String patientIdentifierList;
	int numberOfPatientRelatedStudies;
	Patients patient;
	
	
	public long getPk() {
		return pk;
	}
	public void setPk(long pk) {
		this.pk = pk;
	}
	public String getEthnicGroup() {
		return ethnicGroup;
	}
	public void setEthnicGroup(String ethnicGroup) {
		this.ethnicGroup = ethnicGroup;
	}
	public String getPatientComments() {
		return patientComments;
	}
	public void setPatientComments(String patientComments) {
		this.patientComments = patientComments;
	}
	public String getRace() {
		return race;
	}
	public void setRace(String race) {
		this.race = race;
	}
	public String getPatientAddress() {
		return patientAddress;
	}
	public void setPatientAddress(String patientAddress) {
		this.patientAddress = patientAddress;
	}
	public String getPatientCity() {
		return patientCity;
	}
	public void setPatientCity(String patientCity) {
		this.patientCity = patientCity;
	}
	public String getPatientAccountNumber() {
		return patientAccountNumber;
	}
	public void setPatientAccountNumber(String patientAccountNumber) {
		this.patientAccountNumber = patientAccountNumber;
	}
	public String getPatientIdentifierList() {
		return patientIdentifierList;
	}
	public void setPatientIdentifierList(String patientIdentifierList) {
		this.patientIdentifierList = patientIdentifierList;
	}
	public int getNumberOfPatientRelatedStudies() {
		return numberOfPatientRelatedStudies;
	}
	public void setNumberOfPatientRelatedStudies(int numberOfPatientRelatedStudies) {
		this.numberOfPatientRelatedStudies = numberOfPatientRelatedStudies;
	}
	public Patients getPatient() {
		return patient;
	}
	public void setPatient(Patients patient) {
		this.patient = patient;
	}
	

}
