/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.dto;

public class AccessionNumber {

	private String number;
	private String aetitle;
	private String patientID;
	private String issuer;

	public AccessionNumber() {
		this.number = "";
		this.aetitle = "";
		this.patientID = "";
		this.issuer = "";
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getAetitle() {
		return aetitle;
	}

	public void setAetitle(String aetitle) {
		this.aetitle = aetitle;
	}

	public String getPatientID() {
		return patientID;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	@Override
	public String toString() {
		return "AccessionNumber [number=" + number + ", aetitle=" + aetitle + ", patientID=" + patientID + ", issuer="
				+ issuer + "]";
	}

}
