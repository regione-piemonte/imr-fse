/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;

public class WlpatientDataPerVisit implements java.io.Serializable {


    private static final long serialVersionUID = 1L;
    private Long pk;
	private Patients patients;
	private Studies studies;
	private String patientstate;
	private Character patientClass;
	private String assignedPatientLocation;
	private String visitNumber;
	private Short pregnancyStatus;
	private String medicalAlerts;
	private Long patientWeight;
	private String confidentialityConstraintOnPatientData;
	private String specialNeeds;
//	private String studyFk;
	private Boolean deprecated;

	private String studyFk;
	
	public WlpatientDataPerVisit() {
	}

	public WlpatientDataPerVisit(Patients patients, String patientstate,
			Character patientClass, String assignedPatientLocation,
			String visitNumber, Short pregnancyStatus, String medicalAlerts,
			Long patientWeight, String confidentialityConstraintOnPatientData,
			String specialNeeds, Studies studies, Boolean deprecated) {
		this.patients = patients;
		this.patientstate = patientstate;
		this.patientClass = patientClass;
		this.assignedPatientLocation = assignedPatientLocation;
		this.visitNumber = visitNumber;
		this.pregnancyStatus = pregnancyStatus;
		this.medicalAlerts = medicalAlerts;
		this.patientWeight = patientWeight;
		this.confidentialityConstraintOnPatientData = confidentialityConstraintOnPatientData;
		this.specialNeeds = specialNeeds;
		this.studies = studies;
		this.deprecated = deprecated;
	}

	public Long getPk() {
		return this.pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public Patients getPatients() {
		return this.patients;
	}

	public void setPatients(Patients patients) {
		this.patients = patients;
	}

	public String getPatientstate() {
		return this.patientstate;
	}

	public void setPatientstate(String patientstate) {
		this.patientstate = patientstate;
	}

	public Character getPatientClass() {
		return this.patientClass;
	}

	public void setPatientClass(Character patientClass) {
		this.patientClass = patientClass;
	}

	public String getAssignedPatientLocation() {
		return this.assignedPatientLocation;
	}

	public void setAssignedPatientLocation(String assignedPatientLocation) {
		this.assignedPatientLocation = assignedPatientLocation;
	}

	public String getVisitNumber() {
		return this.visitNumber;
	}

	public void setVisitNumber(String visitNumber) {
		this.visitNumber = visitNumber;
	}

	public Short getPregnancyStatus() {
		return this.pregnancyStatus;
	}

	public void setPregnancyStatus(Short pregnancyStatus) {
		this.pregnancyStatus = pregnancyStatus;
	}

	public String getMedicalAlerts() {
		return this.medicalAlerts;
	}

	public void setMedicalAlerts(String medicalAlerts) {
		this.medicalAlerts = medicalAlerts;
	}

	public Long getPatientWeight() {
		return this.patientWeight;
	}

	public void setPatientWeight(Long patientWeight) {
		this.patientWeight = patientWeight;
	}

	public String getConfidentialityConstraintOnPatientData() {
		return this.confidentialityConstraintOnPatientData;
	}

	public void setConfidentialityConstraintOnPatientData(
			String confidentialityConstraintOnPatientData) {
		this.confidentialityConstraintOnPatientData = confidentialityConstraintOnPatientData;
	}

	public String getSpecialNeeds() {
		return this.specialNeeds;
	}

	public void setSpecialNeeds(String specialNeeds) {
		this.specialNeeds = specialNeeds;
	}


	public Boolean getDeprecated() {
		return this.deprecated;
	}

	public void setDeprecated(Boolean deprecated) {
		this.deprecated = deprecated;
	}

    public String getStudyFk() {
        return studyFk;
    }

    public void setStudyFk(String studyFk) {
        this.studyFk = studyFk;
    }

}
