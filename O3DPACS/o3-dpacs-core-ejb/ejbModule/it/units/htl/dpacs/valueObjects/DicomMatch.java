/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.io.Serializable;

/**
 * The retrieved match in query retrieve and move, including the patient, study, 
 * series and Instance
 * @author Mbe
 */
public class DicomMatch implements Serializable{	
 /* Actually nothing more than a simple Composite! */

	public Patient patient=null;
	public Study study=null;
	public Series series=null;
	public Instance instance=null;	
        // To be cast to the right type!
	
	public NearlineData nearlineData;
    
}	

