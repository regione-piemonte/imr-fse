/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

public class CannotDeleteException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public static final String ERROR_IO="An error occurred removing the files from the File System";
	public static final String ERROR_STUDY="An error occurred updating a study";
	public static final String ERROR_STUDYLOCATIONS="An error occurred deleting study locations";
	public static final String ERROR_SERIES="An error occurred deprecating a series";
	public static final String ERROR_INSTANCES="An error occurred deleting an instance";
	public static final String ERROR_HASH="An error occurred deleting instance hashes";
	
	public CannotDeleteException(String reason){
		super(reason);
	}
}
