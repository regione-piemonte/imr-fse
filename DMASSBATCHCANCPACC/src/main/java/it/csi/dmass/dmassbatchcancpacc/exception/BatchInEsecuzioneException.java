/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.exception;

public class BatchInEsecuzioneException extends Exception{

	public BatchInEsecuzioneException() {
		super("Il servizio e' gia' in esecuzione");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
