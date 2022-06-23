/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.exception;

import java.util.List;
import java.util.stream.Collectors;

import it.csi.dmass.dmassbatchcancpacc.client.ScaricoStudiWSBean.Errore;

public class CancellaPacchettiException extends Exception{

	public CancellaPacchettiException( List<Errore> errori) {		
		super(String.join(",",errori.stream().map(e->e.getCodice()+": "+e.getDescrizione()).collect(Collectors.toList())));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
