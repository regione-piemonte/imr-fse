/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import it.units.htl.dpacs.dao.Hl7PublisherBean;
import it.units.htl.dpacs.helpers.StringHelper;
import it.units.htl.dpacs.postprocessing.UidGenerator;
import it.units.htl.dpacs.servers.HL7Server;
import it.units.htl.dpacs.servers.hl7.comunication.utils.TerserLocations;
import it.units.htl.dpacs.valueObjects.CodeSequence;
import it.units.htl.dpacs.valueObjects.Study;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;



public class ORMO01 extends HL7Dealer {
	
	public boolean saveTransactionDate;
	
	public ORMO01(HL7Server hl7) {
		super(hl7);
		saveTransactionDate=Boolean.parseBoolean(hl7.isSaveTransactionDate());
	}

	private Study s;
	private String cd;

	protected void parse(Message hm) throws HL7Exception{
		cd = null;
        Segment pid = null; // I can have one PID segment
        Segment pv1 = null; // I can have one PV1 segment
        Segment orc = null; // I can have more ORC segments, but only the first will be considered
        Segment obr = null; // I can have more OBR segments, but only the first will be considered
        
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
        
        try{
        	orc=terser.getSegment(TERSER_ORC);
        }catch (HL7Exception hex){
        	throw new HL7SegmentException("No ORC segment");		// Must generate an AR
        }
        
        try{
        	obr=terser.getSegment(TERSER_OBR);
        }catch (HL7Exception hex){
        	throw new HL7SegmentException("No OBR segment");		// Must generate an AR
        }
        
        String checkDicom=getIheZds(terser, "4");
        if("DICOM".equals(checkDicom))
        	s=new Study(getIheZds(terser, "1"));		// Instance UID
        else 
        	s=null;
        if ((s==null)||(s.getStudyInstanceUid() == null)) {
            if(getUniqueField() != null){
                log.debug("No ZDS but unique field: " + getUniqueField() + " value : " + terser.get(getUniqueField()));
                String uniqueField = terser.get(getUniqueField());
                if(uniqueField != null && !"".equals(uniqueField)){
                    String studyInstanceUID = UidGenerator.O3EnterpriseUidRoot + "." + StringHelper.convertUniqueField(uniqueField);
                    s = new Study(studyInstanceUID);
                    hm.addNonstandardSegment("ZDS");
                    terser=new Terser(hm);
                    terser.set(TerserLocations.ZDS.studyUID, studyInstanceUID);
                    terser.set(TerserLocations.ZDS.application, "Application");
                    terser.set(TerserLocations.ZDS.type, "DICOM");
                }else{
                    throw new HL7SegmentException("No Study Instance UID provided");
                }      
            }else{
                throw new HL7SegmentException("No Study Instance UID provided");
            }
        }

        // Go on parsing according to segment order
        // PID
        p.setPatientId(getPatientId(pid));
        p.setIdIssuer(getPatientIdIssuer(pid)); // Actually, it has subcomponents
        if((p.getIdIssuer()==null)||("".equals(p.getIdIssuer())))
        	p.setIdIssuer(defaultIdIssuer);
		p.setPatientIdentifierList(p.getPatientId());
        p.setLastName(get(pid, 5, 1, 1));
        if ((p.getLastName() != null) && ((p.getLastName().equals("")) )) {
            p.setLastName(null);
        }
        p.setFirstName(get(pid, 5, 1, 2));
        if ((p.getFirstName() != null) && ((p.getFirstName().equals("")))) {
            p.setFirstName(null);
        }
        p.setMiddleName(get(pid, 5, 1, 3));
        if ((p.getMiddleName() != null) && ((p.getMiddleName().equals("")) )) {
            p.setMiddleName(null);
        }
        p.setPrefix(get(pid, 5, 1, 5));
        if ((p.getPrefix() != null) && ((p.getPrefix().equals("")) )) {
            p.setPrefix(null);
        }
        p.setSuffix(get(pid, 5, 1, 4) + " " + get(pid, 5, 1, 6));
        if ((p.getSuffix() != null) && ((p.getSuffix().equals("")) )) {
            p.setSuffix(null);
        }
        java.util.Date dtb = parseDateTime(get(pid, 7));
        if (dtb != null) {
            p.setBirthDate(new java.sql.Date(dtb.getTime()));
            p.setBirthTime(new java.sql.Time(dtb.getTime()));
        }
        p.setSex(get(pid, 8, 1, 1));
        if ((p.getSex() != null) && ((p.getSex().equals("")) )) {
            p.setSex(null);
        }
        p.setRace(get(pid, 10, 1, 2));
        if ((p.getRace() != null) && ((p.getRace().equals("")) )) {
            p.setRace(null);
        }
        p.setPatientAddress(getPatientAddress(pid));
        if ((p.getPatientAddress() != null) && ((p.getPatientAddress().equals("")) )) {
            p.setPatientAddress(null);
        }
        p.setPatientCity(getPatientCity(pid));
        if ((p.getPatientCity() != null) && ((p.getPatientCity().equals("")) )) {
            p.setPatientCity(null);
        }
        p.setPatientAccountNumber(get(pid, 18, 1, 1));
        if ((p.getPatientAccountNumber() != null) && ((p.getPatientAccountNumber().equals("")) )) {
            p.setPatientAccountNumber(null);
        }
        p.setEthnicGroup(get(pid, 22, 1, 2));
        if ((p.getEthnicGroup() != null) && ((p.getEthnicGroup().equals("")) )) {
            p.setEthnicGroup(null);
        }
        // PV1
        p.setPatientClass(get(pv1, 2, 1, 1));
        if ((p.getPatientClass() != null) && ((p.getPatientClass().equals("")) )) {
            p.setPatientClass(null);
        }
        p.setAssignedPatientLocation(get(pv1, 3, 1, 3) + "_" + get(pv1, 3, 1, 1) + "_" + get(pv1, 3, 1, 2)); // bed_pointOfCare_room
        p.setVisitNumber(getVisitNumber(pv1));
        // ORC
        cd = get(orc, 1); // Order Control Code
        // OBR Note: I parse just the first pair!
        
        if(this.saveTransactionDate){
	        java.util.Date dtt = parseDateTime(get(orc, 9));
	        if (dtt != null) {
	            s.setStudyDate(new java.sql.Date(dtt.getTime()));
	        }
        }
        
        
        String accNumSegment=getAccessionNumberSegment();
        if("PID".equalsIgnoreCase(accNumSegment))
        	s.setAccessionNumber(getAccessionNumber(pid));
        else if("ORC".equalsIgnoreCase(accNumSegment))
        	s.setAccessionNumber(getAccessionNumber(orc));
        else if("OBR".equalsIgnoreCase(accNumSegment))
        	s.setAccessionNumber(getAccessionNumber(obr));
        
        String studyDescSegment=getStudyDescriptionSegment();
        if("ORC".equalsIgnoreCase(studyDescSegment))
        	s.setStudyDescription(getStudyDescription(orc));
        else if("OBR".equalsIgnoreCase(studyDescSegment))
        	s.setStudyDescription(getStudyDescription(obr));

        s.setProcedureCodeSequence(new CodeSequence(get(obr, getCodeSequenceSegment(), 1, 1), get(obr, getCodeSequenceSegment(), 1), get(obr, getCodeSequenceSegment(), 1), get(obr, getCodeSequenceSegment(), 1,2)));

        if(toBeForwarded("O01")){
            HapiContext context = new DefaultHapiContext();
            Parser p = context.getGenericParser();
            meAsString = p.encode(hm);
        }
        
	}

	protected void run() throws Exception{
		log.info("Received ORM^O01 " + cd + " about " + p.getLastName());
		if ("NW".equals(cd)) {
		    if (toBeForwarded("O01^NW"))
                hl7publisher.insertHl7MessageInQueue(meAsString, Hl7PublisherBean.MessageType.O01_ForSched.getPk());
            bean.initializePatient(p, s);
        } else if (("CA".equals(cd)) || ("DC".equals(cd))) {
            if (toBeForwarded("O01^CA"))
                hl7publisher.insertHl7MessageInQueue(meAsString, Hl7PublisherBean.MessageType.O01_ForCanc.getPk());
            bean.cancelDiscontinue(p, s);
        } else if ("XO".equals(cd)) {
            if (toBeForwarded("O01^XO"))
                hl7publisher.insertHl7MessageInQueue(meAsString, Hl7PublisherBean.MessageType.O01_ForUpdate.getPk());
        	bean.updateOrder(p,s);
        } else {
            throw new HL7SegmentException("No Valid Order Control Code");
        }
	}

}
