/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.model;

import java.sql.Timestamp;

public class DmassLServizi {

	private Long id_ser;
	private String nome_servizio;
	private Timestamp data_richiesta;
	private Timestamp data_risposta;
	private String cf_utente;
	private String ruolo_utente;
	private String cf_assistito;
	private String applicazione;
	private String appl_verticale;
	private String regime;
	private Long id_collocazione;
	private String cod_esito_risposta_servizio;
	private String ip_richiedente;
	private String codice_servizio;
	private Timestamp data_ins;
	private Timestamp data_mod;
	public Long getId_ser() {
		return id_ser;
	}
	public void setId_ser(Long id_ser) {
		this.id_ser = id_ser;
	}
	public String getNome_servizio() {
		return nome_servizio;
	}
	public void setNome_servizio(String nome_servizio) {
		this.nome_servizio = nome_servizio;
	}
	public Timestamp getData_richiesta() {
		return data_richiesta;
	}
	public void setData_richiesta(Timestamp data_richiesta) {
		this.data_richiesta = data_richiesta;
	}
	public Timestamp getData_risposta() {
		return data_risposta;
	}
	public void setData_risposta(Timestamp data_risposta) {
		this.data_risposta = data_risposta;
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
	public String getRegime() {
		return regime;
	}
	public void setRegime(String regime) {
		this.regime = regime;
	}
	public Long getId_collocazione() {
		return id_collocazione;
	}
	public void setId_collocazione(Long id_collocazione) {
		this.id_collocazione = id_collocazione;
	}
	public String getCod_esito_risposta_servizio() {
		return cod_esito_risposta_servizio;
	}
	public void setCod_esito_risposta_servizio(String cod_esito_risposta_servizio) {
		this.cod_esito_risposta_servizio = cod_esito_risposta_servizio;
	}
	public String getIp_richiedente() {
		return ip_richiedente;
	}
	public void setIp_richiedente(String ip_richiedente) {
		this.ip_richiedente = ip_richiedente;
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
	public Timestamp getData_mod() {
		return data_mod;
	}
	public void setData_mod(Timestamp data_mod) {
		this.data_mod = data_mod;
	}
	@Override
	public String toString() {
		return "DmassLServizi [id_ser=" + id_ser + ", nome_servizio=" + nome_servizio + ", data_richiesta="
				+ data_richiesta + ", data_risposta=" + data_risposta + ", cf_utente=" + cf_utente + ", ruolo_utente="
				+ ruolo_utente + ", cf_assistito=" + cf_assistito + ", applicazione=" + applicazione
				+ ", appl_verticale=" + appl_verticale + ", regime=" + regime + ", id_collocazione=" + id_collocazione
				+ ", cod_esito_risposta_servizio=" + cod_esito_risposta_servizio + ", ip_richiedente=" + ip_richiedente
				+ ", codice_servizio=" + codice_servizio + ", data_ins=" + data_ins + ", data_mod=" + data_mod + "]";
	}
	
	
	
	

}
