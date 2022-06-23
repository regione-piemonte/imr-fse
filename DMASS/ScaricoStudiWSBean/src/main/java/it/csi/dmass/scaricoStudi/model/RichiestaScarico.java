package it.csi.dmass.scaricoStudi.model;

import java.sql.Timestamp;

public class RichiestaScarico{

	private String codicefiscale;
	private String email;
	private String errore;
	private String idreferto;
	private String pin;
	private String statorichiesta;
	private String strutturasanitaria;
	private String asr;
	private String sistemaoperativo;
	private String accession_numbers;
	private Timestamp datainsrichiesta;
	private Timestamp datainviomail;
	private Timestamp dataoraerrore;
	private Timestamp dataultimodownload;
	private Long idrichiestascarico;
	private Long numerotentativi;
	private Long periodoconservazione;
	private Long mail_id;
	private Long pacchetto_id;
	private Long richiestacancellazione_id;
	private Long scaricopacchetto_id;
	private Boolean fuoriregione;
	private String requestid;
	
	private String zipname;
	private String directory;	
	private String checksum;
	
	public String getCodicefiscale() {
		return codicefiscale;
	}
	public void setCodicefiscale(String codicefiscale) {
		this.codicefiscale = codicefiscale;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getErrore() {
		return errore;
	}
	public void setErrore(String errore) {
		this.errore = errore;
	}
	public String getIdreferto() {
		return idreferto;
	}
	public void setIdreferto(String idreferto) {
		this.idreferto = idreferto;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
	public String getStatorichiesta() {
		return statorichiesta;
	}
	public void setStatorichiesta(String statorichiesta) {
		this.statorichiesta = statorichiesta;
	}
	public String getStrutturasanitaria() {
		return strutturasanitaria;
	}
	public void setStrutturasanitaria(String strutturasanitaria) {
		this.strutturasanitaria = strutturasanitaria;
	}
	public String getAsr() {
		return asr;
	}
	public void setAsr(String asr) {
		this.asr = asr;
	}
	public String getSistemaoperativo() {
		return sistemaoperativo;
	}
	public void setSistemaoperativo(String sistemaoperativo) {
		this.sistemaoperativo = sistemaoperativo;
	}
	public String getAccession_numbers() {
		return accession_numbers;
	}
	public void setAccession_numbers(String accession_numbers) {
		this.accession_numbers = accession_numbers;
	}
	public Timestamp getDatainsrichiesta() {
		return datainsrichiesta;
	}
	public void setDatainsrichiesta(Timestamp datainsrichiesta) {
		this.datainsrichiesta = datainsrichiesta;
	}
	public Timestamp getDatainviomail() {
		return datainviomail;
	}
	public void setDatainviomail(Timestamp datainviomail) {
		this.datainviomail = datainviomail;
	}
	public Timestamp getDataoraerrore() {
		return dataoraerrore;
	}
	public void setDataoraerrore(Timestamp dataoraerrore) {
		this.dataoraerrore = dataoraerrore;
	}
	public Timestamp getDataultimodownload() {
		return dataultimodownload;
	}
	public void setDataultimodownload(Timestamp dataultimodownload) {
		this.dataultimodownload = dataultimodownload;
	}
	public Long getIdrichiestascarico() {
		return idrichiestascarico;
	}
	public void setIdrichiestascarico(Long idrichiestascarico) {
		this.idrichiestascarico = idrichiestascarico;
	}
	public Long getNumerotentativi() {
		return numerotentativi;
	}
	public void setNumerotentativi(Long numerotentativi) {
		this.numerotentativi = numerotentativi;
	}
	public Long getPeriodoconservazione() {
		return periodoconservazione;
	}
	public void setPeriodoconservazione(Long periodoconservazione) {
		this.periodoconservazione = periodoconservazione;
	}
	public Long getMail_id() {
		return mail_id;
	}
	public void setMail_id(Long mail_id) {
		this.mail_id = mail_id;
	}
	public Long getPacchetto_id() {
		return pacchetto_id;
	}
	public void setPacchetto_id(Long pacchetto_id) {
		this.pacchetto_id = pacchetto_id;
	}
	public Long getRichiestacancellazione_id() {
		return richiestacancellazione_id;
	}
	public void setRichiestacancellazione_id(Long richiestacancellazione_id) {
		this.richiestacancellazione_id = richiestacancellazione_id;
	}
	public Long getScaricopacchetto_id() {
		return scaricopacchetto_id;
	}
	public void setScaricopacchetto_id(Long scaricopacchetto_id) {
		this.scaricopacchetto_id = scaricopacchetto_id;
	}
	public Boolean getFuoriregione() {
		return fuoriregione;
	}
	public void setFuoriregione(Boolean fuoriregione) {
		this.fuoriregione = fuoriregione;
	}
	
	public String getRequestid() {
		return requestid;
	}
	public void setRequestid(String requestid) {
		this.requestid = requestid;
	}
	
	public String getZipname() {
		return zipname;
	}
	public void setZipname(String zipname) {
		this.zipname = zipname;
	}
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	public String getChecksum() {
		return checksum;
	}
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
	@Override
	public String toString() {
		return "RichiestaScarico [codicefiscale=" + codicefiscale + ", email=" + email + ", errore=" + errore
				+ ", idreferto=" + idreferto + ", pin=" + pin + ", statorichiesta=" + statorichiesta
				+ ", strutturasanitaria=" + strutturasanitaria + ", asr=" + asr + ", sistemaoperativo="
				+ sistemaoperativo + ", accession_numbers=" + accession_numbers + ", datainsrichiesta="
				+ datainsrichiesta + ", datainviomail=" + datainviomail + ", dataoraerrore=" + dataoraerrore
				+ ", dataultimodownload=" + dataultimodownload + ", idrichiestascarico=" + idrichiestascarico
				+ ", numerotentativi=" + numerotentativi + ", periodoconservazione=" + periodoconservazione
				+ ", mail_id=" + mail_id + ", pacchetto_id=" + pacchetto_id + ", richiestacancellazione_id="
				+ richiestacancellazione_id + ", scaricopacchetto_id=" + scaricopacchetto_id + ", fuoriregione="
				+ fuoriregione + "]";
	}			
	
}
