/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import java.util.Iterator;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import it.units.htl.atna.AuditLogService;
import it.units.htl.atna.custom.VisitAccessedMessage;
import it.units.htl.dpacs.dao.CannotUpdateException;
import it.units.htl.dpacs.dao.ImageAvailabilityLocal;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.helpers.IheStudyId;
import it.units.htl.dpacs.helpers.ImageAvailabilityConfig;
import it.units.htl.dpacs.servers.HL7Server;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.dpacs.valueObjects.Study;

import org.dcm4che2.audit.message.AuditEvent.ActionCode;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.util.MessageNavigator;
import ca.uhn.hl7v2.util.Terser;

public class ADTA45 extends HL7Dealer {

	private Study s;
	private Patient np;
	
	private String sendingApp;
	private String sendingFac;
	
	public ADTA45(HL7Server hl7) {
		super(hl7);
	}
	
	protected void parse(Message hm) throws HL7Exception {
		Segment pid = null;
        Segment pv1 = null;
        Segment mrg = null;
        
        Terser terser=new Terser(hm);
        
        try{
        	pid=terser.getSegment(TERSER_PID);
        }catch (HL7Exception hex){
        	throw new HL7SegmentException("No PID segment");		// Must generate an AR
        }

        try{
        	pv1=terser.getSegment(TERSER_PV1);
        }catch (HL7Exception hex){}								// It's acceptable if pv1 is absent

        try{
        	mrg=terser.getSegment(TERSER_MRG);
        }catch (HL7Exception hex){
        	throw new HL7SegmentException("No MRG segment");		// Must generate an AR
        }
        
        String checkDicom=getIheZds(terser, "4");
        if("DICOM".equals(checkDicom))
        	s=new Study(getIheZds(terser, "1"));		// Instance UID
        else 
        	s=null;


        np=new Patient();
        // PID
        np.setPatientId(getPatientId(pid));
        np.setIdIssuer(getPatientIdIssuer(pid)); // Actually, it has subcomponents
        if((np.getIdIssuer()==null)||("".equals(np.getIdIssuer())))
        	np.setIdIssuer(defaultIdIssuer);
		np.setPatientIdentifierList(np.getPatientId()); // DTODO: Parse even following occurrences!
        np.setLastName(get(pid, 5, 1, 1));
        if ((np.getLastName() != null) && ((np.getLastName().equals("")) )) {
            np.setLastName(null);
        }
        np.setFirstName(get(pid, 5, 1, 2));
        if ((np.getFirstName() != null) && ((np.getFirstName().equals("")))) {
            np.setFirstName(null);
        }
        np.setMiddleName(get(pid, 5, 1, 3));
        if ((np.getMiddleName() != null) && ((np.getMiddleName().equals("")) )) {
            np.setMiddleName(null);
        }
        np.setPrefix(get(pid, 5, 1, 5));
        if ((np.getPrefix() != null) && ((np.getPrefix().equals("")) )) {
            np.setPrefix(null);
        }
        np.setSuffix(get(pid, 5, 1, 4) + " " + get(pid, 5, 1, 6));
        if ((np.getSuffix() != null) && ((np.getSuffix().equals("")) )) {
            np.setSuffix(null);
        }
        java.util.Date dtb = parseDateTime(get(pid, 7));
        if (dtb != null) {
            np.setBirthDate(new java.sql.Date(dtb.getTime()));
            np.setBirthTime(new java.sql.Time(dtb.getTime()));
        }
        np.setSex(get(pid, 8, 1, 1));
        if ((np.getSex() != null) && ((np.getSex().equals("")) )) {
            np.setSex(null);
        }
        np.setRace(get(pid, 10, 1, 2));
        if ((np.getRace() != null) && ((np.getRace().equals("")) )) {
            np.setRace(null);
        }
        np.setPatientAddress(get(pid, 11, 1, 1));
        if ((np.getPatientAddress() != null) && ((np.getPatientAddress().equals("")) )) {
            np.setPatientAddress(null);
        }
        np.setPatientCity(get(pid, 11, 1, 3));
        if ((np.getPatientCity() != null) && ((np.getPatientCity().equals("")) )) {
            np.setPatientCity(null);
        }
        np.setPatientAccountNumber(get(pid, 18, 1, 1));
        if ((np.getPatientAccountNumber() != null) && ((np.getPatientAccountNumber().equals("")) )) {
            np.setPatientAccountNumber(null);
        }
        np.setEthnicGroup(get(pid, 22, 1, 2));
        if ((np.getEthnicGroup() != null) && ((np.getEthnicGroup().equals("")) )) {
            np.setEthnicGroup(null);
        }
        // PV1
        if(pv1!=null){
	        np.setVisitNumber(getVisitNumber(pv1));
        }else{
        	np.setVisitNumber(null);
        }
        p.setPatientId(getPatientId(mrg, false));
        p.setIdIssuer(getPatientIdIssuer(mrg, false));
        if((p.getIdIssuer()==null)||("".equals(p.getIdIssuer())))
        	p.setIdIssuer(defaultIdIssuer);
        p.setPatientIdentifierList(p.getPatientId());		// It was 4,1,1
        p.setVisitNumber(getMrgVisitNumber(mrg));
        
        try{
        	sendingApp=terser.get("/.MSH-3-1");		// Sending Application 
        }catch(HL7Exception hex){
        	sendingApp="";
        }

        try{
        	sendingFac=terser.get("/.MSH-4-1");		// Sending Facility 
        }catch(HL7Exception hex){
        	sendingFac="";
        }
	}

	protected void run() throws Exception {
		log.info("Received ADT^A45 about " + p.getPatientIdentifierList() + " into " + np.getPatientIdentifierList());
		int moved=0;
		if(s==null){
			moved=bean.moveVisit(p, np, sendingApp);
		}else{
			moved=bean.moveStudy(s, p, np);
			ImageAvailabilityConfig iac=ImageAvailabilityConfig.getInstance();
        	if(iac.isEnabled() && ImageAvailabilityConfig.PUBLISHTO_TABLE.equals(iac.getPublicationMethod())){
        		
                ImageAvailabilityLocal imAvailBean=null;
                try{
                	imAvailBean=InitialContext.doLookup(BeansName.LImageAvailability);
                	IheStudyId ids=new IheStudyId(s.getStudyInstanceUid(), null, null);
                	imAvailBean.reconcileWrongStudy(ids, sendingApp.equals(iac.getAppEntityForRis())?ImageAvailabilityConfig.RECONCILIATIONSOURCE_RIS:ImageAvailabilityConfig.RECONCILIATIONSOURCE_ADT, iac.getStringForSetting(), iac.getStringForRemoving(), iac.getTargetApp());
                } catch (NamingException nex) {
                    log.error("Error accessing Data Access Layer: "+BeansName.LImageAvailability, nex);
                } catch(Exception ex){
                	log.error("An error occurred publishing studies", ex);
                }

        	}
		}
		
		try{
			VisitAccessedMessage vam = new VisitAccessedMessage(ActionCode.UPDATE);
			try {
				if(s!=null){
					vam.addStudy(s.getStudyInstanceUid(), null);
				}else{
					vam.addVisit(p.getVisitNumber());
				}
				if(np.getVisitNumber()!=null)
					vam.addNewVisit(np.getVisitNumber());
				
				vam.addUserProcess("ADTA45", new String[]{sendingApp}, "ADTA45", sendingFac, true);
				
				String name=p.getLastName();
				name= (name==null)? "^"+p.getFirstName() : (name+"^"+p.getFirstName());
				vam.addPatient(p.getPatientId(), name);
			} catch (Exception ex) {
				log.error("Could not send Audit Message", ex);
			}
			AuditLogService als = AuditLogService.getInstance();
			als.SendMessage(vam);
		}catch (Exception e) {
			log.warn("Unable to send AuditLogMessage", e);
		}
		
		//if(moved==0)		// One should never reach here
        //	throw new CannotUpdateException("No studies to be moved");
		
        log.info("Moved "+moved+" studies");
	}

}
