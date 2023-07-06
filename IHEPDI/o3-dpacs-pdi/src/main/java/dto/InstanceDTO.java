/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package dto;

import java.io.Serializable;

import com.google.gson.Gson;

public class InstanceDTO implements Serializable {

	private static final long serialVersionUID = 6184557074913112821L;

	private String objectUID;
	private String modalityOfSeries;
	private String phonometric;
	private String spacing;
	private String size;
	private String numberOfFrames;
	private Integer row;
	private Integer column;
		
	public String getObjectUID() {
		return objectUID;
	}
	public void setObjectUID(String objectUID) {
		this.objectUID = objectUID;
	}
	public String getModalityOfSeries() {
		return modalityOfSeries;
	}
	public void setModalityOfSeries(String modalityOfSeries) {
		this.modalityOfSeries = modalityOfSeries;
	}
	public String getPhonometric() {
		return phonometric;
	}
	public void setPhonometric(String phonometric) {
		this.phonometric = phonometric;
	}
	public String getSpacing() {
		return spacing;
	}
	public void setSpacing(String spacing) {
		this.spacing = spacing;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getNumberOfFrames() {
		return numberOfFrames;
	}
	public void setNumberOfFrames(String numberOfFrames) {
		this.numberOfFrames = numberOfFrames;
	}
	public Integer getRow() {
		return row;
	}
	public void setRow(Integer row) {
		this.row = row;
	}
	public Integer getColumn() {
		return column;
	}
	public void setColumn(Integer column) {
		this.column = column;
	}
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
	
}
