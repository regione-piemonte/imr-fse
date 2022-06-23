/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.model;

import java.sql.Timestamp;

public class DmassLServiziBatchInfo {

	private Long id_info;
	private Long id_ser;
	private String info;
	private String info_dettaglio;
	private String tipo_info;
	private Timestamp data_ins;
	public Long getId_info() {
		return id_info;
	}
	public void setId_info(Long id_info) {
		this.id_info = id_info;
	}
	public Long getId_ser() {
		return id_ser;
	}
	public void setId_ser(Long id_ser) {
		this.id_ser = id_ser;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getInfo_dettaglio() {
		return info_dettaglio;
	}
	public void setInfo_dettaglio(String info_dettaglio) {
		this.info_dettaglio = info_dettaglio;
	}
	public String getTipo_info() {
		return tipo_info;
	}
	public void setTipo_info(String tipo_info) {
		this.tipo_info = tipo_info;
	}
	public Timestamp getData_ins() {
		return data_ins;
	}
	public void setData_ins(Timestamp data_ins) {
		this.data_ins = data_ins;
	}
	@Override
	public String toString() {
		return "DmassLServiziBatchInfo [id_info=" + id_info + ", id_ser=" + id_ser + ", info=" + info
				+ ", info_dettaglio=" + info_dettaglio + ", tipo_info=" + tipo_info + ", data_ins=" + data_ins + "]";
	}
	
	
	
	
	

}
