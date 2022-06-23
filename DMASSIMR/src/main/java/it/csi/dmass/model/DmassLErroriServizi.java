/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.model;

import java.sql.Timestamp;

public class DmassLErroriServizi {

	private Long id_err;
	private Long id_ser;
	private String cod_errore;
	private String descr_errore;
	private String tipo_errore;
	private String info_aggiuntive;
	private Timestamp data_ins;
	public Long getId_err() {
		return id_err;
	}
	public void setId_err(Long id_err) {
		this.id_err = id_err;
	}
	public Long getId_ser() {
		return id_ser;
	}
	public void setId_ser(Long id_ser) {
		this.id_ser = id_ser;
	}
	public String getCod_errore() {
		return cod_errore;
	}
	public void setCod_errore(String cod_errore) {
		this.cod_errore = cod_errore;
	}
	public String getDescr_errore() {
		return descr_errore;
	}
	public void setDescr_errore(String descr_errore) {
		this.descr_errore = descr_errore;
	}
	public String getTipo_errore() {
		return tipo_errore;
	}
	public void setTipo_errore(String tipo_errore) {
		this.tipo_errore = tipo_errore;
	}
	public String getInfo_aggiuntive() {
		return info_aggiuntive;
	}
	public void setInfo_aggiuntive(String info_aggiuntive) {
		this.info_aggiuntive = info_aggiuntive;
	}
	public Timestamp getData_ins() {
		return data_ins;
	}
	public void setData_ins(Timestamp data_ins) {
		this.data_ins = data_ins;
	}
	@Override
	public String toString() {
		return "DmassLErroreServizi [id_err=" + id_err + ", id_ser=" + id_ser + ", cod_errore=" + cod_errore
				+ ", descr_errore=" + descr_errore + ", tipo_errore=" + tipo_errore + ", info_aggiuntive="
				+ info_aggiuntive + ", data_ins=" + data_ins + "]";
	}		
}
