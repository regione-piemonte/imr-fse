/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dto;

import org.springframework.http.HttpStatus;

public class ScaricaPacchettoOperatoreSanitarioDto {

	private String cfRichiedente;
	private String cfAssistito;
	private String idDocumentoIlec;
	private String codiceDocumentoIlec;
	private String codCL;
	private String codApplicazione; 
	private String codVerticale;
	private String codRuolo;
	private String codRegime; 
	private String idCollocazione;
	private String ip;	
	private String pin;
	private String archivioDocumentoIlec;	
	private String statoRichiesta;
	private String errore;	
	private String codiceAudit;
	private String request;	
	private String idTransazione;	
	private String codiceServizio;
	private Long idSer;
	private HttpStatus codEsitoRisposta;
	private String response;	
	
	
	
	public String getCodiceDocumentoIlec() {
		return codiceDocumentoIlec;
	}
	public void setCodiceDocumentoIlec(String codiceDocumentoIlec) {
		this.codiceDocumentoIlec = codiceDocumentoIlec;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public HttpStatus getCodEsitoRisposta() {
		return codEsitoRisposta;
	}
	public void setCodEsitoRisposta(HttpStatus codEsitoRisposta) {
		this.codEsitoRisposta = codEsitoRisposta;
	}
	public Long getIdSer() {
		return idSer;
	}
	public void setIdSer(Long idSer) {
		this.idSer = idSer;
	}
	public String getCodiceServizio() {
		return codiceServizio;
	}
	public void setCodiceServizio(String codiceServizio) {
		this.codiceServizio = codiceServizio;
	}
	public String getIdTransazione() {
		return idTransazione;
	}
	public void setIdTransazione(String idTransazione) {
		this.idTransazione = idTransazione;
	}
	public String getCodiceAudit() {
		return codiceAudit;
	}
	public void setCodiceAudit(String codiceAudit) {
		this.codiceAudit = codiceAudit;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getErrore() {
		return errore;
	}
	public void setErrore(String errore) {
		this.errore = errore;
	}
	public String getStatoRichiesta() {
		return statoRichiesta;
	}
	public void setStatoRichiesta(String statoRichiesta) {
		this.statoRichiesta = statoRichiesta;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
	public String getArchivioDocumentoIlec() {
		return archivioDocumentoIlec;
	}
	public void setArchivioDocumentoIlec(String archivioDocumentoIlec) {
		this.archivioDocumentoIlec = archivioDocumentoIlec;
	}
	public String getCfRichiedente() {
		return cfRichiedente;
	}
	public void setCfRichiedente(String cfRichiedente) {
		this.cfRichiedente = cfRichiedente;
	}
	public String getCfAssistito() {
		return cfAssistito;
	}
	public void setCfAssistito(String cfAssistito) {
		this.cfAssistito = cfAssistito;
	}
	public String getIdDocumentoIlec() {
		return idDocumentoIlec;
	}
	public void setIdDocumentoIlec(String idDocumentoIlec) {
		this.idDocumentoIlec = idDocumentoIlec;
	}
	public String getCodCL() {
		return codCL;
	}
	public void setCodCL(String codCL) {
		this.codCL = codCL;
	}
	public String getCodApplicazione() {
		return codApplicazione;
	}
	public void setCodApplicazione(String codApplicazione) {
		this.codApplicazione = codApplicazione;
	}
	public String getCodVerticale() {
		return codVerticale;
	}
	public void setCodVerticale(String codVerticale) {
		this.codVerticale = codVerticale;
	}
	public String getCodRuolo() {
		return codRuolo;
	}
	public void setCodRuolo(String codRuolo) {
		this.codRuolo = codRuolo;
	}
	public String getCodRegime() {
		return codRegime;
	}
	public void setCodRegime(String codRegime) {
		this.codRegime = codRegime;
	}
	public String getIdCollocazione() {
		return idCollocazione;
	}
	public void setIdCollocazione(String idCollocazione) {
		this.idCollocazione = idCollocazione;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	@Override
	public String toString() {
		return "ScaricaPacchettoOperatoreSanitarioDto [cfRichiedente=" + cfRichiedente + ", cfAssistito=" + cfAssistito
				+ ", idDocumentoIlec=" + idDocumentoIlec + ", codCL=" + codCL + ", codApplicazione=" + codApplicazione
				+ ", codVerticale=" + codVerticale + ", codRuolo=" + codRuolo + ", codRegime=" + codRegime
				+ ", idCollocazione=" + idCollocazione + ", ip=" + ip + "]";
	}
	
	
	
}
