/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import it.units.htl.dpacs.dao.Hl7PublisherBean;
import it.units.htl.dpacs.servers.HL7Server;
import it.units.htl.dpacs.valueObjects.Patient;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;

public class ADTA40 extends HL7Dealer {
    private final static int DO_NOT_GROUP = -1;
    private String groupsForMessages;
    private String groupSegment;
    private int groupSequence = 0;
    private int groupRepetition = 0;
    private int groupComponent = 0;
    private String sendingApp;
    private int group = DO_NOT_GROUP;
    private Patient np;

    public ADTA40(HL7Server hl7) {
        super(hl7);
        try {
            groupsForMessages = hl7.getGroupsForMessages();
            groupSegment = hl7.getGroupSegment();
            groupSequence = Integer.parseInt(hl7.getGroupSequence());
            groupRepetition = Integer.parseInt(hl7.getGroupRepetition());
            groupComponent = Integer.parseInt(hl7.getGroupComponent());
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    protected void parse(Message hm) throws HL7Exception {
        boolean groupA40 = false;
        Segment pid = null; // I can have one PID segment
        Segment toGroup = null;
        Segment mrg = null;
        if ((groupsForMessages != null) && (groupsForMessages.length() > 0)) {
            groupA40 = groupsForMessages.contains("A40");
        }
        Terser terser = new Terser(hm);
        try {
            pid = terser.getSegment(TERSER_PID);
        } catch (HL7Exception hex) {
            throw new HL7SegmentException("No PID segment"); // Must generate an AR
        }
        try {
            mrg = terser.getSegment(TERSER_MRG);
        } catch (HL7Exception hex) {
            throw new HL7SegmentException("No MRG segment"); // Must generate an AR
        }
        if ((groupA40) && (groupSegment != null) && (toGroup == null)) {
            toGroup = terser.getSegment("/." + groupSegment);
        }
        // Now I can start parsing
        // OLD DATA:
        p.setPatientIdentifierList(getPatientId(mrg, false));
        p.setPatientAccountNumber(get(mrg, 3, 1, 1));
        p.setPatientId(p.getPatientIdentifierList());
        p.setIdIssuer(getPatientIdIssuer(mrg, false));
        if ((p.getIdIssuer() == null) || ("".equals(p.getIdIssuer())))
            p.setIdIssuer(defaultIdIssuer);
        p.setVisitNumber(getMrgVisitNumber(mrg));
        p.setLastName(get(mrg, 7, 1, 1));
        p.setFirstName(get(mrg, 7, 1, 2));
        p.setMiddleName(get(mrg, 7, 1, 3));
        p.setPrefix(get(mrg, 7, 1, 5));
        p.setSuffix(get(mrg, 7, 1, 4) + " " + get(mrg, 7, 1, 6));
        if ((p.getPatientId() == null) || (p.getPatientId().length() == 0)) {
            p.setPatientId(p.getPatientIdentifierList());
        }
        if ((p.getPatientAccountNumber() == null) || (p.getPatientAccountNumber().length() == 0)) {
            p.setPatientAccountNumber(null);
        }
        if ((p.getIdIssuer() == null) || (p.getIdIssuer().length() == 0)) {
            p.setIdIssuer(null);
        }
        if ((p.getLastName() == null) || (p.getLastName().length() == 0)) {
            p.setLastName(null);
        }
        if ((p.getFirstName() == null) || (p.getFirstName().length() == 0)) {
            p.setFirstName(null);
        }
        if ((p.getMiddleName() == null) || (p.getMiddleName().length() == 0)) {
            p.setMiddleName(null);
        }
        if ((p.getPrefix() == null) || (p.getPrefix().length() == 0)) {
            p.setPrefix(null);
        }
        if ((p.getSuffix() == null) || (p.getSuffix().length() == 0)) {
            p.setSuffix(null);
        }
        // NEW DATA:
        np = new Patient();
        np.setPatientIdentifierList(getPatientId(pid));
        np.setLastName(get(pid, 5, 1, 1));
        np.setFirstName(get(pid, 5, 1, 2));
        np.setMiddleName(get(pid, 5, 1, 3));
        np.setPrefix(get(pid, 5, 1, 5));
        np.setSuffix(get(pid, 5, 1, 4) + " " + get(pid, 5, 1, 6));
        np.setPatientAccountNumber(get(pid, 18, 1, 1));
        if ((np.getPatientId() == null) || (np.getPatientId().length() == 0)) {
            np.setPatientId(np.getPatientIdentifierList());
        }
        if ((np.getPatientAccountNumber() == null) || (np.getPatientAccountNumber().length() == 0)) {
            np.setPatientAccountNumber(null);
        }
        if ((np.getLastName() == null) || (np.getLastName().length() == 0)) {
            np.setLastName(null);
        }
        if ((np.getFirstName() == null) || (np.getFirstName().length() == 0)) {
            np.setFirstName(null);
        }
        if ((np.getMiddleName() == null) || (np.getMiddleName().length() == 0)) {
            np.setMiddleName(null);
        }
        if ((np.getPrefix() == null) || (np.getPrefix().length() == 0)) {
            np.setPrefix(null);
        }
        if ((np.getSuffix() == null) || (np.getSuffix().length() == 0)) {
            np.setSuffix(null);
        }
        // ADDED
        np.setIdIssuer(getPatientIdIssuer(pid)); // Actually, it has subcomponents
        if ((np.getIdIssuer() == null) || ("".equals(np.getIdIssuer())))
            np.setIdIssuer(defaultIdIssuer);
        java.util.Date dtb = parseDateTime(get(pid, 7));
        if (dtb != null) {
            np.setBirthDate(new java.sql.Date(dtb.getTime()));
            np.setBirthTime(new java.sql.Time(dtb.getTime()));
        }
        np.setSex(get(pid, 8, 1, 1));
        if ((np.getSex() != null) && ((np.getSex().equals("")))) {
            np.setSex(null);
        }
        np.setRace(get(pid, 10, 1, 2));
        if ((np.getRace() != null) && ((np.getRace().equals("")))) {
            np.setRace(null);
        }
        np.setPatientAddress(getPatientAddress(pid));
        if ((np.getPatientAddress() != null) && ((np.getPatientAddress().equals("")))) {
            np.setPatientAddress(null);
        }
        np.setPatientCity(getPatientCity(pid));
        if ((np.getPatientCity() != null) && ((np.getPatientCity().equals("")))) {
            np.setPatientCity(null);
        }
        np.setPatientAccountNumber(get(pid, 18, 1, 1));
        if ((np.getPatientAccountNumber() != null) && ((np.getPatientAccountNumber().equals("")))) {
            np.setPatientAccountNumber(null);
        }
        np.setEthnicGroup(get(pid, 22, 1, 2));
        if ((np.getEthnicGroup() != null) && ((np.getEthnicGroup().equals("")))) {
            np.setEthnicGroup(null);
        }
        try {
            sendingApp = terser.get("/.MSH-3-1"); // Sending Application
        } catch (HL7Exception hex) {
            sendingApp = "";
        }
        if (groupA40) {
            try {
                group = Integer.parseInt(get(toGroup, groupSequence, groupRepetition, groupComponent).trim());
            } catch (Exception ex) {
                log.error("Unable to retrieve a number from the grouping component " + ex.getMessage());
                group = DO_NOT_GROUP;
            }
        }
        if(toBeForwarded("A40")){
            HapiContext context = new DefaultHapiContext();
            Parser p = context.getGenericParser();
            meAsString = p.encode(hm);
        }
    }

    protected void run() throws Exception {
        log.info("Received ADT^A40 about " + p.getPatientIdentifierList() + " into " + np.getPatientIdentifierList());
        int done = bean.mergePatients(p, np, (group == DO_NOT_GROUP) ? null : group, sendingApp);
        if (done == 0)
            log.warn("No studies to be moved");
        else if (done == -1)
            log.error("An error occurred merging the studies");
        else
            log.info("Merge completed");
        if (toBeForwarded("A40"))
                hl7publisher.insertHl7MessageInQueue(meAsString, Hl7PublisherBean.MessageType.A40.getPk());
    }
}
