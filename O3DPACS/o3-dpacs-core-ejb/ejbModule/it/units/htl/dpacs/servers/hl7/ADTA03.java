/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import it.units.htl.dpacs.servers.HL7Server;

import java.util.Iterator;
import java.util.Timer;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.util.Terser;



public class ADTA03 extends HL7Dealer {
	
	public ADTA03(HL7Server hl7) {
		super(hl7);
		daysToEmptyCache=Integer.parseInt(hl7.getDaysToEmptyHL7Cache());
	}

	private int daysToEmptyCache=-1;
	private static Timer emptyCacheTimer = null;

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
		log.info("Received ADT^A03 about " + p.getLastName());
        bean.discharge(p);
        if (emptyCacheTimer == null) {
        	emptyCacheTimer = new Timer();
            emptyCacheTimer.schedule(new DischargeEraser(), System.currentTimeMillis() + (daysToEmptyCache * MILLIS_PER_DAY), daysToEmptyCache * MILLIS_PER_DAY);
        }

	}
	
	private class DischargeEraser extends java.util.TimerTask {
        public void run() {
//            try {
//                Context jndiCon = new InitialContext();
//                HL7BeforeDischargeELocalHome homeDis = (HL7BeforeDischargeELocalHome) jndiCon.lookup("java:comp/env/ejb/BeforeDischargeData");
//                jndiCon.close();
//                
//                Collection<HL7BeforeDischargeELocal> cd = homeDis.findOlderThan(System.currentTimeMillis() - (daysToEmptyCache * MILLIS_PER_DAY));
//                // Now iterate through each item and call remove() on it
//                for (HL7BeforeDischargeELocal item : cd) {
//                    item.remove();
//                }
//            } catch (Exception ex) {
//                // Nothing particular should be done when in production!
//				log.error("", ex);
//            } // end try...catch
        }
    }

}
