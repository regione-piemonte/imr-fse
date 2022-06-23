/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.exception;

public class GetElencoPacchettiScadutiException extends Exception{

	public GetElencoPacchettiScadutiException() {
		super("Chiamata a getElencoPacchettiScaduti con errori");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
