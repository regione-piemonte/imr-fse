/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.atna.custom;

import org.dcm4che2.audit.message.AuditEvent.ActionCode;
import org.dcm4che2.audit.message.ParticipantObject.IDTypeCode;
import org.dcm4che2.audit.message.ParticipantObject.TypeCode;
import org.dcm4che2.audit.message.ParticipantObject.TypeCodeRole;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessageSupport;
import org.dcm4che2.audit.message.ParticipantObject;
import org.dcm4che2.audit.message.ParticipantObjectDescription;

public class VisitAccessedMessage extends AuditMessageSupport {

	public VisitAccessedMessage(ActionCode action) {
		super(AuditEvent.ID.PATIENT_CARE_EPISODE, action);
	}
	
	public ParticipantObject addStudy(String uid, ParticipantObjectDescription desc) {
        return addParticipantObject(ParticipantObject.createStudy(uid, desc));
    }
	
	public ParticipantObject addVisit(String visitNumber) {
		ParticipantObject visit=new ParticipantObject(visitNumber, IDTypeCode.ENCOUNTER_NUMBER);
		visit.setParticipantObjectTypeCode(TypeCode.PERSON);
        visit.setParticipantObjectTypeCodeRole(TypeCodeRole.REPORT);
        return addParticipantObject(visit);
    }
	
	public ParticipantObject addNewVisit(String visitNumber) {
		ParticipantObject visit=new ParticipantObject(visitNumber, IDTypeCode.ENCOUNTER_NUMBER);
		visit.setParticipantObjectTypeCode(TypeCode.PERSON);
        visit.setParticipantObjectTypeCodeRole(TypeCodeRole.DATA_DESTINATION);		// THIS DISTINGUISHES THE NEW VISIT FROM THE OLD ONE
        return addParticipantObject(visit);
    }

    @Override
    public void validate() {
        super.validate();
        
        boolean found=false;
        // If at least a Visit or a Study is specified, succeed!
        for (ParticipantObject po : participantObjects) {
            if((ParticipantObject.TypeCodeRole.REPORT == po.getParticipantObjectTypeCodeRole() && ParticipantObject.IDTypeCode.STUDY_INSTANCE_UID == po.getParticipantObjectIDTypeCode())	// Study
               ||
               (ParticipantObject.TypeCodeRole.REPORT == po.getParticipantObjectTypeCodeRole() && ParticipantObject.IDTypeCode.ENCOUNTER_NUMBER == po.getParticipantObjectIDTypeCode())	// Visit
              ){
                found=true;
                break;	// No need to continue
            }
        }
        if(!found){
            throw new IllegalStateException("No Study or Visit identification");
        }
    }
    
}
