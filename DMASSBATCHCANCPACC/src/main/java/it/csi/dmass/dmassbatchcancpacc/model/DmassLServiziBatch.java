/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.model;

import java.sql.Timestamp;

public class DmassLServiziBatch {

	private Long id_ser;
	private String nome_servizio;
	private Timestamp data_inizio;
	private Timestamp data_fine;
	private String stato_fine;
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
	public Timestamp getData_inizio() {
		return data_inizio;
	}
	public void setData_inizio(Timestamp data_inizio) {
		this.data_inizio = data_inizio;
	}
	public Timestamp getData_fine() {
		return data_fine;
	}
	public void setData_fine(Timestamp data_fine) {
		this.data_fine = data_fine;
	}
	public String getStato_fine() {
		return stato_fine;
	}
	public void setStato_fine(String stato_fine) {
		this.stato_fine = stato_fine;
	}
	@Override
	public String toString() {
		return "DmassLServiziBatch [id_ser=" + id_ser + ", nome_servizio=" + nome_servizio + ", stato_fine="
				+ stato_fine + "]";
	}
	
	

}
