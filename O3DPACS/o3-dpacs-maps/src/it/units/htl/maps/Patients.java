/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Patients implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private Long pk;
    private String lastName;
    private String firstName;
    private String middleName;
    private String prefix;
    private String suffix;
    private Date birthDate;
    private Date birthTime;
    private Character sex;
    private String patientId;
    private String idIssuer;
    private Character toReconcile;
    private Boolean deprecated;
    private Long mergedBy;
    private Set studies = new HashSet(0);
    
    private PatientDemographics patientDemographics;

    public Patients() {
    }

    public Patients(String patientId) {
        this.patientId = patientId;
    }

    public Patients(String lastName, String firstName, String middleName,
            String prefix, String suffix, Date birthDate, Date birthTime,
            Character sex, String patientId, String idIssuer,
            Character toReconcile, Boolean deprecated, Long mergedBy,
             Set studies) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.birthDate = birthDate;
        this.birthTime = birthTime;
        this.sex = sex;
        this.patientId = patientId;
        this.idIssuer = idIssuer;
        this.toReconcile = toReconcile;
        this.deprecated = deprecated;
        this.mergedBy = mergedBy;
        this.studies = studies;
    }

    public Long getPk() {
        return this.pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return this.middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Date getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getBirthDateYYYYMMDD() {
        String convertedDate = "";
        if (birthDate != null) {
            Format formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd");
            convertedDate = formatter.format(birthDate);
        }
        return convertedDate;
    }

    public Date getBirthTime() {
        return this.birthTime;
    }

    public void setBirthTime(Date birthTime) {
        this.birthTime = birthTime;
    }

    public Character getSex() {
        return this.sex;
    }

    public void setSex(Character sex) {
        this.sex = sex;
    }

    public String getPatientId() {
        return this.patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getIdIssuer() {
        return this.idIssuer;
    }

    public void setIdIssuer(String idIssuer) {
        this.idIssuer = idIssuer;
    }

    public Character getToReconcile() {
        return this.toReconcile;
    }

    public void setToReconcile(Character toReconcile) {
        this.toReconcile = toReconcile;
    }

    public Boolean getDeprecated() {
        return this.deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Long getMergedBy() {
        return this.mergedBy;
    }

    public void setMergedBy(Long mergedBy) {
        this.mergedBy = mergedBy;
    }

    public Set getStudies() {
        return this.studies;
    }

    public void setStudies(Set studies) {
        this.studies = studies;
    }

	public PatientDemographics getPatientDemographics() {
		return patientDemographics;
	}

	public void setPatientDemographics(PatientDemographics patientDemographics) {
		this.patientDemographics = patientDemographics;
	}
}
