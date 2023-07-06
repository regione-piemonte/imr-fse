/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.StudyAvailabilityPojo;

import java.util.ArrayList;

import javax.ejb.Local;

@Local
public interface Hl7PublisherLocal {
    public ArrayList<StudyAvailabilityPojo> getCompletedStudies(int elapsedMins);
    public boolean insertHl7Notification(String sms, StudyAvailabilityPojo study);
    public String generateO01Notify(StudyAvailabilityPojo studyUID);
    public void insertHl7MessageInQueue(String message, int typePk);
    public boolean updateStudyAvailability(StudyAvailabilityPojo studyUID, long completedTime);
}
