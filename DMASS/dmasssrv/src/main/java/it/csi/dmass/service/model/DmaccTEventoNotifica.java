package it.csi.dmass.service.model;

import java.sql.Timestamp;

public class DmaccTEventoNotifica {
	
	private Long id;
	private Long id_evento;
	private String cf_destinatario;
	private String flag_stato_notifica;
	private Timestamp data_inserimento;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getId_evento() {
		return id_evento;
	}
	public void setId_evento(Long id_evento) {
		this.id_evento = id_evento;
	}
	public String getCf_destinatario() {
		return cf_destinatario;
	}
	public void setCf_destinatario(String cf_destinatario) {
		this.cf_destinatario = cf_destinatario;
	}
	public String getFlag_stato_notifica() {
		return flag_stato_notifica;
	}
	public void setFlag_stato_notifica(String flag_stato_notifica) {
		this.flag_stato_notifica = flag_stato_notifica;
	}
	public Timestamp getData_inserimento() {
		return data_inserimento;
	}
	public void setData_inserimento(Timestamp data_inserimento) {
		this.data_inserimento = data_inserimento;
	}
	@Override
	public String toString() {
		return "DmaccTEventoNotifica [id=" + id + ", id_evento=" + id_evento + ", cf_destinatario=" + cf_destinatario
				+ ", flag_stato_notifica=" + flag_stato_notifica + "]";
	}
	
	

}
