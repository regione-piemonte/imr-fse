/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

/**
 * The class is a generic child of instance for any object which has not its own
 * instance type
 * @author Mbe
 */
public class NonImage extends Instance{

	public NonImage(){}
	public NonImage(String siu){
		sopInstanceUid=prepareString(siu, 64);
	}	
	public NonImage(String siu, String sop){
		sopInstanceUid=prepareString(siu, 64);
		sopClassUid=prepareString(sop, 64);
	}	

	}	

