/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.model;

import java.sql.Timestamp;

public class DmassTAudit {

	private Long id;
	private String cod_audit;
	private String id_transazione;
	private String cf_utente;
	private String ruolo_utente;
	private String regime;
	private String cf_assistito;
	private String applicazione;
	private String appl_verticale;
	private String ip;
	private Long id_collocazione;
	private String codice_servizio;
	private Timestamp data_ins;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCod_audit() {
		return cod_audit;
	}
	public void setCod_audit(String cod_audit) {
		this.cod_audit = cod_audit;
	}
	public String getId_transazione() {
		return id_transazione;
	}
	public void setId_transazione(String id_transazione) {
		this.id_transazione = id_transazione;
	}
	public String getCf_utente() {
		return cf_utente;
	}
	public void setCf_utente(String cf_utente) {
		this.cf_utente = cf_utente;
	}
	public String getRuolo_utente() {
		return ruolo_utente;
	}
	public void setRuolo_utente(String ruolo_utente) {
		this.ruolo_utente = ruolo_utente;
	}
	public String getRegime() {
		return regime;
	}
	public void setRegime(String regime) {
		this.regime = regime;
	}
	public String getCf_assistito() {
		return cf_assistito;
	}
	public void setCf_assistito(String cf_assistito) {
		this.cf_assistito = cf_assistito;
	}
	public String getApplicazione() {
		return applicazione;
	}
	public void setApplicazione(String applicazione) {
		this.applicazione = applicazione;
	}
	public String getAppl_verticale() {
		return appl_verticale;
	}
	public void setAppl_verticale(String appl_verticale) {
		this.appl_verticale = appl_verticale;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Long getId_collocazione() {
		return id_collocazione;
	}
	public void setId_collocazione(Long id_collocazione) {
		this.id_collocazione = id_collocazione;
	}
	public String getCodice_servizio() {
		return codice_servizio;
	}
	public void setCodice_servizio(String codice_servizio) {
		this.codice_servizio = codice_servizio;
	}
	public Timestamp getData_ins() {
		return data_ins;
	}
	public void setData_ins(Timestamp data_ins) {
		this.data_ins = data_ins;
	}
	@Override
	public String toString() {
		return "DmassTAudit [id=" + id + ", cod_audit=" + cod_audit + ", id_transazione=" + id_transazione
				+ ", cf_utente=" + cf_utente + ", ruolo_utente=" + ruolo_utente + ", regime=" + regime
				+ ", cf_assistito=" + cf_assistito + ", applicazione=" + applicazione + ", appl_verticale="
				+ appl_verticale + ", ip=" + ip + ", id_collocazione=" + id_collocazione + ", codice_servizio="
				+ codice_servizio + ", data_ins=" + data_ins + "]";
	}
	
	
	

}
