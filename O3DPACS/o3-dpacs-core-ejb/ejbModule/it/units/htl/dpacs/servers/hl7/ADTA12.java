/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import it.units.htl.dpacs.dao.CannotUpdateException;
import it.units.htl.dpacs.servers.HL7Server;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.util.Terser;


public class ADTA12 extends HL7Dealer {

	public ADTA12(HL7Server hl7) {
		super(hl7);
	}

	protected void parse(Message hm) throws HL7Exception{
		Segment pid = null; // I can have one PID segment
       
        Terser terser=new Terser(hm);

        try{
        	pid=terser.getSegment(TERSER_PID);
        }catch (HL7Exception hex){
        	throw new HL7SegmentException("No PID segment");		// Must generate an AR
        }
        
        // Now I can start parsing:
        p.setPatientIdentifierList(getPatientId(pid));
        p.setLastName(get(pid,5, 1, 1));
        p.setFirstName(get(pid,5, 1, 2));
        p.setPatientAccountNumber(get(pid,18, 1, 1));

	}

	@Override
	protected void run() throws Exception{
		log.info("Received ADT^A12 about " + p.getLastName());
        if (bean.undoTransfer(p) == 0) {
            throw new CannotUpdateException(CannotUpdateException.PATIENT_PROBLEMS);
        }
	}

}
