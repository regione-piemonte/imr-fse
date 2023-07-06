/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import it.units.htl.dpacs.servers.HL7Server;


public class ADTA31 extends ADTA08 {
	
	public ADTA31(HL7Server hl7) {
		super(hl7);
	}

	protected void run() throws Exception{
		log.info("Managing ADT^A31 about " + p.getLastName());
		super.run();
	}
}
