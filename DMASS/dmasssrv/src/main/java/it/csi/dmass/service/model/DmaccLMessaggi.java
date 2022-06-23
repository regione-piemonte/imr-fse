package it.csi.dmass.service.model;

import java.sql.Timestamp;

public class DmaccLMessaggi {

	private Long id_xml;
	private String wso2_id;
	private String servizio_xml;
	private String uuid;
	private String chiamante;
	private Long stato_xml;
	private Timestamp data_ricezione;
	private Timestamp data_risposta;
	private Timestamp data_invio_a_promemoria;
	private Timestamp data_risposta_a_promemoria;
	private Timestamp data_ins;
	private Timestamp data_mod;
	private String id_messaggio_orig;
	private String cf_assistito;
	private String cf_utente;
	private String ruolo_utente;
	private String nre;
	private String cod_esito_risposta_promemoria;
	private String tipo_prescrizione;
	private String regione_prescrizione;
	private String info_aggiuntive_errore;
	private Timestamp data_invio_servizio;
	private Timestamp data_risposta_servizio;
	private String cod_esito_risposta_servizio;
	private String lista_codici_servizio;	
	private String stato_delega;
	private String applicazione;
	private String codice_servizio;
	private String appl_verticale;
	private String ip_richiedente;
	
	public Long getId_xml() {
		return id_xml;
	}
	public void setId_xml(Long id_xml) {
		this.id_xml = id_xml;
	}
	public String getWso2_id() {
		return wso2_id;
	}
	public void setWso2_id(String wso2_id) {
		this.wso2_id = wso2_id;
	}
	public String getServizio_xml() {
		return servizio_xml;
	}
	public void setServizio_xml(String servizio_xml) {
		this.servizio_xml = servizio_xml;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getChiamante() {
		return chiamante;
	}
	public void setChiamante(String chiamante) {
		this.chiamante = chiamante;
	}
	public Long getStato_xml() {
		return stato_xml;
	}
	public void setStato_xml(Long stato_xml) {
		this.stato_xml = stato_xml;
	}
	public Timestamp getData_ricezione() {
		return data_ricezione;
	}
	public void setData_ricezione(Timestamp data_ricezione) {
		this.data_ricezione = data_ricezione;
	}
	public Timestamp getData_risposta() {
		return data_risposta;
	}
	public void setData_risposta(Timestamp data_risposta) {
		this.data_risposta = data_risposta;
	}
	public Timestamp getData_invio_a_promemoria() {
		return data_invio_a_promemoria;
	}
	public void setData_invio_a_promemoria(Timestamp data_invio_a_promemoria) {
		this.data_invio_a_promemoria = data_invio_a_promemoria;
	}
	public Timestamp getData_risposta_a_promemoria() {
		return data_risposta_a_promemoria;
	}
	public void setData_risposta_a_promemoria(Timestamp data_risposta_a_promemoria) {
		this.data_risposta_a_promemoria = data_risposta_a_promemoria;
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
	public String getId_messaggio_orig() {
		return id_messaggio_orig;
	}
	public void setId_messaggio_orig(String id_messaggio_orig) {
		this.id_messaggio_orig = id_messaggio_orig;
	}
	public String getCf_assistito() {
		return cf_assistito;
	}
	public void setCf_assistito(String cf_assistito) {
		this.cf_assistito = cf_assistito;
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
	public String getNre() {
		return nre;
	}
	public void setNre(String nre) {
		this.nre = nre;
	}
	public String getCod_esito_risposta_promemoria() {
		return cod_esito_risposta_promemoria;
	}
	public void setCod_esito_risposta_promemoria(String cod_esito_risposta_promemoria) {
		this.cod_esito_risposta_promemoria = cod_esito_risposta_promemoria;
	}
	public String getTipo_prescrizione() {
		return tipo_prescrizione;
	}
	public void setTipo_prescrizione(String tipo_prescrizione) {
		this.tipo_prescrizione = tipo_prescrizione;
	}
	public String getRegione_prescrizione() {
		return regione_prescrizione;
	}
	public void setRegione_prescrizione(String regione_prescrizione) {
		this.regione_prescrizione = regione_prescrizione;
	}
	public String getInfo_aggiuntive_errore() {
		return info_aggiuntive_errore;
	}
	public void setInfo_aggiuntive_errore(String info_aggiuntive_errore) {
		this.info_aggiuntive_errore = info_aggiuntive_errore;
	}
	public Timestamp getData_invio_servizio() {
		return data_invio_servizio;
	}
	public void setData_invio_servizio(Timestamp data_invio_servizio) {
		this.data_invio_servizio = data_invio_servizio;
	}
	public Timestamp getData_risposta_servizio() {
		return data_risposta_servizio;
	}
	public void setData_risposta_servizio(Timestamp data_risposta_servizio) {
		this.data_risposta_servizio = data_risposta_servizio;
	}
	public String getCod_esito_risposta_servizio() {
		return cod_esito_risposta_servizio;
	}
	public void setCod_esito_risposta_servizio(String cod_esito_risposta_servizio) {
		this.cod_esito_risposta_servizio = cod_esito_risposta_servizio;
	}
	public String getLista_codici_servizio() {
		return lista_codici_servizio;
	}
	public void setLista_codici_servizio(String lista_codici_servizio) {
		this.lista_codici_servizio = lista_codici_servizio;
	}
	public String getStato_delega() {
		return stato_delega;
	}
	public void setStato_delega(String stato_delega) {
		this.stato_delega = stato_delega;
	}
	public String getApplicazione() {
		return applicazione;
	}
	public void setApplicazione(String applicazione) {
		this.applicazione = applicazione;
	}
	public String getCodice_servizio() {
		return codice_servizio;
	}
	public void setCodice_servizio(String codice_servizio) {
		this.codice_servizio = codice_servizio;
	}
	public String getAppl_verticale() {
		return appl_verticale;
	}
	public void setAppl_verticale(String appl_verticale) {
		this.appl_verticale = appl_verticale;
	}
	public String getIp_richiedente() {
		return ip_richiedente;
	}
	public void setIp_richiedente(String ip_richiedente) {
		this.ip_richiedente = ip_richiedente;
	}
	@Override
	public String toString() {
		return "DmaccLMessaggi [id_xml=" + id_xml + ", stato_xml=" + stato_xml + ", cod_esito_risposta_servizio="
				+ cod_esito_risposta_servizio + ", lista_codici_servizio=" + lista_codici_servizio + ", stato_delega="
				+ stato_delega + ", applicazione=" + applicazione + ", codice_servizio=" + codice_servizio
				+ ", appl_verticale=" + appl_verticale + ", ip_richiedente=" + ip_richiedente + "]";
	}
	
	
}
