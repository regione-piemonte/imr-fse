/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.pdi.maps;


import java.util.Date;


public class To3pdiJob implements java.io.Serializable {

	private static final long serialVersionUID = 6356714585628912404L;
	private Long pk;
	private String jobid;
	private Date datainizio;
	private Date datafine;
	private String codice;
	private String descrizione;
	private String operazione;
	private String stato;
	private String checksum;
	private String zipname;
	private String request;
	
	public To3pdiJob() {
	}

	public To3pdiJob(String jobid, Date datainizio,
						Date datafine, String codice,
						String descrizione, String operazione,
						String stato, String checksum, String zipname, String request) {
		this.jobid = jobid;
		this.datainizio = datainizio;
		this.datafine = datafine;
		this.codice = codice;
		this.descrizione = descrizione;
		this.operazione = operazione;
		this.stato = stato;
		this.setChecksum(checksum);
		this.setZipname(zipname);
		this.setRequest(request);
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getJobid() {
		return jobid;
	}

	public void setJobid(String jobid) {
		this.jobid = jobid;
	}

	public Date getDatainizio() {
		return datainizio;
	}

	public void setDatainizio(Date datainizio) {
		this.datainizio = datainizio;
	}

	public Date getDatafine() {
		return datafine;
	}

	public void setDatafine(Date datafine) {
		this.datafine = datafine;
	}

	public String getCodice() {
		return codice;
	}

	public void setCodice(String codice) {
		this.codice = codice;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getOperazione() {
		return operazione;
	}

	public void setOperazione(String operazione) {
		this.operazione = operazione;
	}

	public String getStato() {
		return stato;
	}

	public void setStato(String stato) {
		this.stato = stato;
	}

	public String getZipname() {
		return zipname;
	}

	public void setZipname(String zipname) {
		this.zipname = zipname;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}
	
}
