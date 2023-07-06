/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import it.units.htl.dpacs.dao.Hl7PublisherBean;
import it.units.htl.dpacs.servers.HL7Server;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;


public class ADTA08 extends HL7Dealer {
	
	
	protected final String HL7_NULL="\"\""; 
	
	public ADTA08(HL7Server hl7) {
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

				
		p.setPatientIdentifierList(getPatientId(pid));
		if((p.getIdIssuer()==null)||("".equals(p.getIdIssuer())))
        	p.setIdIssuer(defaultIdIssuer);
		p.setPatientId(p.getPatientIdentifierList());
		p.setIdIssuer(getPatientIdIssuer(pid));
		p.setLastName(get(pid,5, 1, 1));
        p.setFirstName(get(pid,5, 1, 2));
        p.setMiddleName(get(pid, 5, 1, 3));
        p.setPrefix(get(pid, 5, 1, 5));
        p.setPatientAccountNumber(get(pid,18, 1, 1));
        
        try{
        	p.setSuffix(get(pid, 5, 1, 4) + " " + get(pid, 5, 1, 6));
        }catch(NullPointerException npex){
        	p.setSuffix(null);
        }

        String tempDate = get(pid, 7);
        if (tempDate != null) {
            if ((tempDate.equals(""))||tempDate.equals(HL7_NULL)) {
                p.setBirthDateRange(null, new java.sql.Date(System.currentTimeMillis())); // Just to state the date has to be overwritten
                p.setBirthTimeRange(null, new java.sql.Time(System.currentTimeMillis())); // Just to state the time has to be overwritten
            } else {
                java.util.Date dtb = parseDateTime(tempDate);
                if (dtb != null) {
                    p.setBirthDate(new java.sql.Date(dtb.getTime()));
                    p.setBirthTime(new java.sql.Time(dtb.getTime()));
                }
                log.debug("Date of birth: " + p.getBirthDate() + "\t Time of Birth: " + p.getBirthTime());
            } // else
        } // end if
        p.setSex(get(pid, 8, 1, 1));
        p.setRace(get(pid, 10, 1, 2));
        p.setEthnicGroup(get(pid, 22, 1, 2));
        p.setPatientAddress(getPatientAddress(pid));
        p.setPatientCity(getPatientCity(pid));
        
        
        if(toBeForwarded("A08")){
            HapiContext context = new DefaultHapiContext();
            Parser p = context.getGenericParser();
            meAsString = p.encode(hm);
        }
        
	}

	protected void run() throws Exception{
		log.info("Managing ADT^A08 about " + p.getLastName());
		bean.updatePatient(p);
		  if (toBeForwarded("A08"))
	            hl7publisher.insertHl7MessageInQueue(meAsString, Hl7PublisherBean.MessageType.A08.getPk());
	}
	
	

}
