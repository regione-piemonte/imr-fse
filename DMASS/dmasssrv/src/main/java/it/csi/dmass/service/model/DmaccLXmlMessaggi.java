package it.csi.dmass.service.model;

import java.sql.Timestamp;

public class DmaccLXmlMessaggi {

	private Long id;
	private String wso2_id;
	private String xml_in;
	private String xml_out;
	private String xml_in_promemoria;
	private String xml_out_promemoria;
	private Timestamp data_inserimento;
	private String xml_in_servizio;
	private String xml_out_servizio;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getWso2_id() {
		return wso2_id;
	}
	public void setWso2_id(String wso2_id) {
		this.wso2_id = wso2_id;
	}
	public String getXml_in() {
		return xml_in;
	}
	public void setXml_in(String xml_in) {
		this.xml_in = xml_in;
	}
	public String getXml_out() {
		return xml_out;
	}
	public void setXml_out(String xml_out) {
		this.xml_out = xml_out;
	}
	public String getXml_in_promemoria() {
		return xml_in_promemoria;
	}
	public void setXml_in_promemoria(String xml_in_promemoria) {
		this.xml_in_promemoria = xml_in_promemoria;
	}
	public String getXml_out_promemoria() {
		return xml_out_promemoria;
	}
	public void setXml_out_promemoria(String xml_out_promemoria) {
		this.xml_out_promemoria = xml_out_promemoria;
	}
	public Timestamp getData_inserimento() {
		return data_inserimento;
	}
	public void setData_inserimento(Timestamp data_inserimento) {
		this.data_inserimento = data_inserimento;
	}
	public String getXml_in_servizio() {
		return xml_in_servizio;
	}
	public void setXml_in_servizio(String xml_in_servizio) {
		this.xml_in_servizio = xml_in_servizio;
	}
	public String getXml_out_servizio() {
		return xml_out_servizio;
	}
	public void setXml_out_servizio(String xml_out_servizio) {
		this.xml_out_servizio = xml_out_servizio;
	}
	@Override
	public String toString() {
		return "DmaccLXmlMessaggi [id=" + id + ", wso2_id=" + wso2_id + ", xml_in=" + xml_in + ", xml_out=" + xml_out
				+ ", xml_in_promemoria=" + xml_in_promemoria + ", xml_out_promemoria=" + xml_out_promemoria
				+ ", data_inserimento=" + data_inserimento + ", xml_in_servizio=" + xml_in_servizio
				+ ", xml_out_servizio=" + xml_out_servizio + "]";
	}
	
	
	
	
}
