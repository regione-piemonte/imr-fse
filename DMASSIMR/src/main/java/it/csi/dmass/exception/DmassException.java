/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.exception;
public class DmassException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3494171832811079788L;

	public DmassException(String msg) {
        super(msg);
    }
}
