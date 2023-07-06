/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import it.units.htl.dpacs.servers.HL7Server;

import java.util.Timer;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.util.Terser;



public class ADTA02 extends HL7Dealer {
	
	public ADTA02(HL7Server hl7) {
		super(hl7);
		daysToEmptyCache=Integer.parseInt(hl7.getDaysToEmptyHL7Cache());
	}

	private int daysToEmptyCache=-1;
	private static Timer emptyCacheTimer = null;

	protected void parse(Message hm) throws HL7Exception{
		Segment pid = null; // I can have one PID segment
        Segment pv1 = null; // I can have one PV1 segment
        
        Terser terser=new Terser(hm);

        try{
        	pid=terser.getSegment(TERSER_PID);
        }catch (HL7Exception hex){
        	throw new HL7SegmentException("No PID segment");		// Must generate an AR
        }
        
        try{
        	pv1=terser.getSegment(TERSER_PV1);
        }catch (HL7Exception hex){
        	throw new HL7SegmentException("No PV1 segment");		// Must generate an AR
        }
        // Now I can start parsing:
        p.setPatientIdentifierList(getPatientId(pid));
        p.setLastName(get(pid,5, 1, 1));
        p.setFirstName(get(pid,5, 1, 2));
        p.setPatientAccountNumber(get(pid,18, 1, 1));
        p.setAssignedPatientLocation(get(pv1,3, 1, 3) + "_" + get(pv1,3, 1, 1) + "_" + get(pv1,3, 1, 2)); // bed_pointOfCare_room

	}

	@Override
	protected void run() throws Exception{
		log.info("Received ADT^A02 about " + p.getLastName());
        bean.transfer(p);
        if (emptyCacheTimer == null) {
            emptyCacheTimer = new Timer();
            emptyCacheTimer.schedule(new TransferEraser(), System.currentTimeMillis() + (daysToEmptyCache * MILLIS_PER_DAY), daysToEmptyCache * MILLIS_PER_DAY);
        }

	}
	
	private class TransferEraser extends java.util.TimerTask {
        public void run() {
//            try {
//                Context jndiCon = new InitialContext();
//                HL7BeforeTransferELocalHome homeTrans = (HL7BeforeTransferELocalHome) jndiCon.lookup("java:comp/env/ejb/BeforeTransferData");
//                jndiCon.close();
//                
//                Collection<HL7BeforeTransferELocal> ct = homeTrans.findOlderThan(System.currentTimeMillis() - (daysToEmptyCache * MILLIS_PER_DAY));
//                // Now iterate through each item and call remove() on it
//                for (HL7BeforeTransferELocal item : ct) {
//                    item.remove();
//                }
//            } catch (Exception ex) {
//                // Nothing particular should be done when in production!
//				log.error("", ex);
//            } // end try...catch
        }
    }

}
