/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.accessors;

public class NoSuchAccessorException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public NoSuchAccessorException(String message){
		super(message);
	}
}