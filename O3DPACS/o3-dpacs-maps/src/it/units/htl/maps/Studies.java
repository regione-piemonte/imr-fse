/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;

import java.util.Date;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

public class Studies implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private String studyInstanceUid;
	private Patients patients;
	private String studyId;
	private String studyStatusId;
	private Date studyDate;
	private Date studyTime;
	private Date studyCompletionDate;
	private Date studyCompletionTime;
	private Date studyVerifiedDate;
	private Date studyVerifiedTime;
	private String accessionNumber;
	private String studyDescription;
	private Long procedureCodeSequenceFk;
	private String referringPhysiciansName;
	private String admittingDiagnosesDescription;
	private Character studyStatus;
	private Long studySize;
	private String fastestAccess;
	private String sealedBy;
	private Short numberOfDeliveredCds;
	private Boolean deprecated;
	private int numberOfStudyRelatedSeries;
	private short numberOfStudyRelatedInstances;
	private Set series = new HashSet(0);
	private WlpatientDataPerVisit wlpatientDataPerVisits;
	
	private Boolean toReconcile;
	private String mergedByPatientId;

	public Studies() {
	}

	public Studies(String studyInstanceUid, byte numberOfStudyRelatedSeries,
			short numberOfStudyRelatedInstances) {
		this.studyInstanceUid = studyInstanceUid;
		this.numberOfStudyRelatedSeries = numberOfStudyRelatedSeries;
		this.numberOfStudyRelatedInstances = numberOfStudyRelatedInstances;
	}

	public Studies(String studyInstanceUid, Patients patients, String studyId,
			String studyStatusId, Date studyDate, Date studyTime,
			Date studyCompletionDate, Date studyCompletionTime,
			Date studyVerifiedDate, Date studyVerifiedTime,
			String accessionNumber, String studyDescription,
			Long procedureCodeSequenceFk, String referringPhysiciansName,
			String admittingDiagnosesDescription, Character studyStatus,
			Long studySize, String fastestAccess, String sealedBy,
			Short numberOfDeliveredCds, Boolean deprecated,
			byte numberOfStudyRelatedSeries,
			short numberOfStudyRelatedInstances, Set series, WlpatientDataPerVisit wlpatientDataPerVisits) {
		this.studyInstanceUid = studyInstanceUid;
		this.patients = patients;
		this.studyId = studyId;
		this.studyStatusId = studyStatusId;
		this.studyDate = studyDate;
		this.studyTime = studyTime;
		this.studyCompletionDate = studyCompletionDate;
		this.studyCompletionTime = studyCompletionTime;
		this.studyVerifiedDate = studyVerifiedDate;
		this.studyVerifiedTime = studyVerifiedTime;
		this.accessionNumber = accessionNumber;
		this.studyDescription = studyDescription;
		this.procedureCodeSequenceFk = procedureCodeSequenceFk;
		this.referringPhysiciansName = referringPhysiciansName;
		this.admittingDiagnosesDescription = admittingDiagnosesDescription;
		this.studyStatus = studyStatus;
		this.studySize = studySize;
		this.fastestAccess = fastestAccess;
		this.sealedBy = sealedBy;
		this.numberOfDeliveredCds = numberOfDeliveredCds;
		this.deprecated = deprecated;
		this.numberOfStudyRelatedSeries = numberOfStudyRelatedSeries;
		this.numberOfStudyRelatedInstances = numberOfStudyRelatedInstances;
		this.series = series;
		this.wlpatientDataPerVisits = wlpatientDataPerVisits;
	}

	public String getStudyInstanceUid() {
		return this.studyInstanceUid;
	}

	public void setStudyInstanceUid(String studyInstanceUid) {
		this.studyInstanceUid = studyInstanceUid;
	}

	public Patients getPatients() {
		return this.patients;
	}

	public void setPatients(Patients patients) {
		this.patients = patients;
	}

	public String getStudyId() {
		return this.studyId;
	}

	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}

	public String getStudyStatusId() {
		return this.studyStatusId;
	}

	public void setStudyStatusId(String studyStatusId) {
		this.studyStatusId = studyStatusId;
	}

	public Date getStudyDate() {
		return this.studyDate;
	}
	
	public String getStudyDateYYYYMMDD(){
	    String convertedDate = "";
	    if(studyDate != null){
	        Format formatter;
	        formatter = new SimpleDateFormat("yyyy-MM-dd");
	        convertedDate = formatter.format(studyDate);
	    }
	    return convertedDate;
	}
	
	public void setStudyDateYYYYMMDD(){
    }

	public void setStudyDate(Date studyDate) {
		this.studyDate = studyDate;
	}

	public Date getStudyTime() {
		return this.studyTime;
	}

	public void setStudyTime(Date studyTime) {
		this.studyTime = studyTime;
	}

	public Date getStudyCompletionDate() {
		return this.studyCompletionDate;
	}

	public void setStudyCompletionDate(Date studyCompletionDate) {
		this.studyCompletionDate = studyCompletionDate;
	}

	public Date getStudyCompletionTime() {
		return this.studyCompletionTime;
	}

	public void setStudyCompletionTime(Date studyCompletionTime) {
		this.studyCompletionTime = studyCompletionTime;
	}

	public Date getStudyVerifiedDate() {
		return this.studyVerifiedDate;
	}

	public void setStudyVerifiedDate(Date studyVerifiedDate) {
		this.studyVerifiedDate = studyVerifiedDate;
	}

	public Date getStudyVerifiedTime() {
		return this.studyVerifiedTime;
	}

	public void setStudyVerifiedTime(Date studyVerifiedTime) {
		this.studyVerifiedTime = studyVerifiedTime;
	}

	public String getAccessionNumber() {
		return this.accessionNumber;
	}

	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public String getStudyDescription() {
		return this.studyDescription;
	}

	public void setStudyDescription(String studyDescription) {
		this.studyDescription = studyDescription;
	}

	public Long getProcedureCodeSequenceFk() {
		return this.procedureCodeSequenceFk;
	}

	public void setProcedureCodeSequenceFk(Long procedureCodeSequenceFk) {
		this.procedureCodeSequenceFk = procedureCodeSequenceFk;
	}

	public String getReferringPhysiciansName() {
		return this.referringPhysiciansName;
	}

	public void setReferringPhysiciansName(String referringPhysiciansName) {
		this.referringPhysiciansName = referringPhysiciansName;
	}

	public String getAdmittingDiagnosesDescription() {
		return this.admittingDiagnosesDescription;
	}

	public void setAdmittingDiagnosesDescription(
			String admittingDiagnosesDescription) {
		this.admittingDiagnosesDescription = admittingDiagnosesDescription;
	}

	public Character getStudyStatus() {
		return this.studyStatus;
	}

	public void setStudyStatus(Character studyStatus) {
		this.studyStatus = studyStatus;
	}

	public Long getStudySize() {
		return this.studySize;
	}

	public void setStudySize(Long studySize) {
		this.studySize = studySize;
	}

	public String getFastestAccess() {
		return this.fastestAccess;
	}

	public void setFastestAccess(String fastestAccess) {
		this.fastestAccess = fastestAccess;
	}

	public String getSealedBy() {
		return this.sealedBy;
	}

	public void setSealedBy(String sealedBy) {
		this.sealedBy = sealedBy;
	}

	public Short getNumberOfDeliveredCds() {
		return this.numberOfDeliveredCds;
	}

	public void setNumberOfDeliveredCds(Short numberOfDeliveredCds) {
		this.numberOfDeliveredCds = numberOfDeliveredCds;
	}

	public Boolean getDeprecated() {
		return this.deprecated;
	}

	public void setDeprecated(Boolean deprecated) {
		this.deprecated = deprecated;
	}

	public int getNumberOfStudyRelatedSeries() {
		return this.numberOfStudyRelatedSeries;
	}

	public void setNumberOfStudyRelatedSeries(int numberOfStudyRelatedSeries) {
		this.numberOfStudyRelatedSeries = numberOfStudyRelatedSeries;
	}

	public short getNumberOfStudyRelatedInstances() {
		return this.numberOfStudyRelatedInstances;
	}

	public void setNumberOfStudyRelatedInstances(
			short numberOfStudyRelatedInstances) {
		this.numberOfStudyRelatedInstances = numberOfStudyRelatedInstances;
	}

	public Set getSeries() {
		return this.series;
	}

	public void setSeries(Set series) {
		this.series = series;
	}
	
	public WlpatientDataPerVisit getWlpatientDataPerVisits() {
		return this.wlpatientDataPerVisits;
	}

	public void setWlpatientDataPerVisits(WlpatientDataPerVisit wlpatientDataPerVisits) {
		this.wlpatientDataPerVisits = wlpatientDataPerVisits;
	}

	public void setReconcile(Boolean toReconcile) {
		this.toReconcile = toReconcile;
	}

	public Boolean getReconcile() {
		return toReconcile;
	}

	public void setMergedByPatientId(String mergedByPatientId) {
		this.mergedByPatientId = mergedByPatientId;
	}

	public String getMergedByPatientId() {
		return mergedByPatientId;
	}

}
