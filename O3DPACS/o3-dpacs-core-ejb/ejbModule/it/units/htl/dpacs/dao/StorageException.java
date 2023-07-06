/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

public class StorageException extends Exception{

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final String NO_MEDIA_AVAILABLE="No available storage media";
	public static final String NO_DATA_PROVIDED="No data were provided";
//	public static final String INSUFFICIENT_META_INFO="Info about study or series not specified";

	// Constructor
	public StorageException(String reason){
		super(reason);
	}

	public static void main(String args[]){

	}	// end main
}	// end class
