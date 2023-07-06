/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.exceptions.IncorrectPatientIdException;
import it.units.htl.dpacs.valueObjects.MoveStudyHistory;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.dpacs.valueObjects.Study;

import javax.ejb.Local;

@Local
public interface HL7DealerLocal {
    // No Exceptions need to be thrown, except for application ones
    public long initializePatient(Patient p, Study st) throws CannotStoreException;

    public long cancelDiscontinue(Patient p, Study s) throws CannotUpdateException;

    public long updateOrder(Patient p, Study s) throws CannotUpdateException;

    public int transfer(Patient p) throws CannotUpdateException;

    public int undoTransfer(Patient p) throws CannotUpdateException;

    public int discharge(Patient p) throws CannotUpdateException;

    public int undoDischarge(Patient p) throws CannotUpdateException;

    public int outToIn(Patient p) throws CannotUpdateException;

    public int inToOut(Patient p) throws CannotUpdateException;

    public int updatePatient(Patient p) throws CannotUpdateException;

    public int mergePatients(Patient old, Patient newPat, Integer groupToUse, String sendingApp) throws IncorrectPatientIdException;

    public int moveStudy(Study source, Patient oldPatient, Patient newPatient) throws CannotUpdateException, IncorrectPatientIdException;

    public int moveVisit(Patient oldPatient, Patient newPatient, String sendingApp) throws CannotUpdateException, IncorrectPatientIdException;
    
    public int insertMoveStudyHistory(MoveStudyHistory moveSH, String structASR) throws CannotUpdateException;
    
} // end interface
