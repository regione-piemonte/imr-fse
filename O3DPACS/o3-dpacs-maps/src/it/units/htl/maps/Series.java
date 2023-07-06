/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;

import java.sql.Date;

public class Series implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private String seriesInstanceUid;
	private Studies studies;
	private Equipment equipment;
	private Long seriesNumber;
	private String modality;
	private String bodyPartExamined;
	private Character seriesStatus;
	private Integer numberOfSeriesRelatedInstances;
	private Boolean deprecated;
	private String seriesDescription;
	private Date convertedToMF;
	

	public Series() {
	}

	public Series(String seriesInstanceUid) {
		this.seriesInstanceUid = seriesInstanceUid;
	}

	public Series(String seriesInstanceUid, Studies studies,
			Equipment equipment, Long seriesNumber, String modality,
			String bodyPartExamined, Character seriesStatus,
			Integer numberOfSeriesRelatedInstances, Boolean deprecated,
			String seriesDescription) {
		this.seriesInstanceUid = seriesInstanceUid;
		this.studies = studies;
		this.equipment = equipment;
		this.seriesNumber = seriesNumber;
		this.modality = modality;
		this.bodyPartExamined = bodyPartExamined;
		this.seriesStatus = seriesStatus;
		this.numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances;
		this.deprecated = deprecated;
		this.seriesDescription = seriesDescription;
		
	}
	
	public Series(String seriesInstanceUid, Studies studies,
			Equipment equipment, Long seriesNumber, String modality,
			String bodyPartExamined, Character seriesStatus,
			Integer numberOfSeriesRelatedInstances, Boolean deprecated,
			String seriesDescription, Date convertedToMF) {
		this.seriesInstanceUid = seriesInstanceUid;
		this.studies = studies;
		this.equipment = equipment;
		this.seriesNumber = seriesNumber;
		this.modality = modality;
		this.bodyPartExamined = bodyPartExamined;
		this.seriesStatus = seriesStatus;
		this.numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances;
		this.deprecated = deprecated;
		this.seriesDescription = seriesDescription;
		this.convertedToMF = convertedToMF;
		
	}

	public String getSeriesInstanceUid() {
		return this.seriesInstanceUid;
	}

	public void setSeriesInstanceUid(String seriesInstanceUid) {
		this.seriesInstanceUid = seriesInstanceUid;
	}

	public Studies getStudies() {
		return this.studies;
	}

	public void setStudies(Studies studies) {
		this.studies = studies;
	}

	public Equipment getEquipment() {
		return this.equipment;
	}

	public void setEquipment(Equipment equipment) {
		this.equipment = equipment;
	}

	public Long getSeriesNumber() {
		return this.seriesNumber;
	}

	public void setSeriesNumber(Long seriesNumber) {
		this.seriesNumber = seriesNumber;
	}

	public String getModality() {
		return this.modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	public String getBodyPartExamined() {
		return this.bodyPartExamined;
	}

	public void setBodyPartExamined(String bodyPartExamined) {
		this.bodyPartExamined = bodyPartExamined;
	}

	public Character getSeriesStatus() {
		return this.seriesStatus;
	}

	public void setSeriesStatus(Character seriesStatus) {
		this.seriesStatus = seriesStatus;
	}

	public Integer getNumberOfSeriesRelatedInstances() {
		return this.numberOfSeriesRelatedInstances;
	}

	public void setNumberOfSeriesRelatedInstances(
			Integer numberOfSeriesRelatedInstances) {
		this.numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances;
	}

	public Boolean getDeprecated() {
		return this.deprecated;
	}

	public void setDeprecated(Boolean deprecated) {
		this.deprecated = deprecated;
	}

	public String getSeriesDescription() {
		return this.seriesDescription;
	}

	public void setSeriesDescription(String seriesDescription) {
		this.seriesDescription = seriesDescription;
	}
	
	public Date getConvertedToMF() {
		return this.convertedToMF;
	}
	
	public void setConvertedToMF(Date convertedToMF) {
		this.convertedToMF = convertedToMF;
	}

}
