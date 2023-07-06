/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

public class MaskingParameter {

	private Integer tagNumber;
	private String tagValue;
	private String modality;
	private Integer secondTagNumber;
	private String secondTagValue;
	private String maskCoordinates;
	
	
	public MaskingParameter(){}
	
	public MaskingParameter(Integer tagNumber, String tagValue, String modality, Integer secondTagNumber, String secondTagValue, String maskCoordinates) {
		this.tagNumber = tagNumber;
		this.tagValue = tagValue;
		this.modality = modality;
		this.secondTagNumber = secondTagNumber;
		this.secondTagValue = secondTagValue;
		this.maskCoordinates = maskCoordinates;
	}
	
	public Integer getTagNumber() {
		return tagNumber;
	}
	public void setTagNumber(Integer tagNumber) {
		this.tagNumber = tagNumber;
	}
	public String getTagValue() {
		return tagValue;
	}
	public void setTagValue(String tagValue) {
		this.tagValue = tagValue;
	}
	public String getModality() {
		return modality;
	}
	public void setModality(String modality) {
		this.modality = modality;
	}
	public Integer getSecondTagNumber() {
		return secondTagNumber;
	}
	public void setSecondTagNumber(Integer secondTagNumber) {
		this.secondTagNumber = secondTagNumber;
	}
	public String getSecondTagValue() {
		return secondTagValue;
	}
	public void setSecondTagValue(String secondTagValue) {
		this.secondTagValue = secondTagValue;
	}
	public String getMaskCoordinates() {
		return maskCoordinates;
	}
	public void setMaskCoordinates(String maskCoordinates) {
		this.maskCoordinates = maskCoordinates;
	}
	
}
