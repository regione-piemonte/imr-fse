/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import it.units.htl.dpacs.dao.CannotUpdateException;
import it.units.htl.dpacs.dao.HL7DealerLocal;
import it.units.htl.dpacs.dao.Hl7PublisherLocal;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.servers.HL7Server;
import it.units.htl.dpacs.valueObjects.Patient;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Composite;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.util.Terser;

public abstract class HL7Dealer implements ReceivingApplication {
    protected Patient p;
    protected HL7DealerLocal bean = null;
    protected Hl7PublisherLocal hl7publisher = null;
    protected ArrayList<String> sendingApps = null;
    protected static String pidIdentifierType = null;
    protected static String pidAssigningAuthority = null;
    protected static int idIssuerField = 5;
    protected static String pidAddressType = null;
    protected static int pidCityComponent;
    protected static String accNumSegment = null;
    protected static int visitNumberIssuerComponent = 0;
    protected static int accNumField = 0;
    protected static int accNumComponent = 0;
    protected static int accNumRepetition = 0;
    protected static String studyDescriptionSegment = null;
    protected static int studyDescriptionField = 0;
    protected static int studyDescriptionComponent = 0;
    protected static int studyDescriptionRepetition = 0;
    protected static String defaultIdIssuer = null;
    private static final String FORMAT_TO_SECONDS = "yyyyMMddHHmm";
    private static final String FORMAT_TO_DAY = "yyyyMMdd";
    private static final String FORMAT_TO_MONTH = "yyyyMM";
    private static final String FORMAT_TO_YEAR = "yyyy";
    protected static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24; // They should be the milliseconds in a day
    protected Log log;
    public static final int PID_PIDLIST = 3;
    public static final int PID_PIDLIST_ID = 1;
    public static final int PID_PIDLIST_ASSAUTH = 4;
    public static final int PID_PIDLIST_IDTYPE = 5;
    public static final int PID_ADDRESS = 11;
    public static final int PID_ADDRESS_ADDR = 1;
    public static final int PID_ADDRESS_ADDRTYPE = 7;
    public static final int PID_FIRSTREPETITION = 1;
    public static final int MRG_PIDLIST = 1;
    public static final String SEPARATOR_VISITNUMBER = "_";
    public static final String DEFAULT_ACCNUMSEGMENT = "OBR";
    public static final int DEFAULT_ACCNUMFIELD = 18;
    public static final int DEFAULT_ACCNUMCOMPONENT = 1;
    public static final int DEFAULT_ACCNUMREPETITION = 1;
    public static final String DEFAULT_STUDYDESCSEGMENT = "OBR";
    public static final int DEFAULT_STUDYDESCFIELD = 4;
    public static final int DEFAULT_STUDYDESCCOMPONENT = 2;
    public static final int DEFAULT_STUDYDESCREPETITION = 1;
    public static final int DEFAULT_PIDADDRESSCITYCOMPONENT = 3;
    public static final int PV1_VISITNUMBERFIELD = 19;
    public static final int PV1_FIRSTREPETITION = 1;
    public static final int PV1_VISITNUMBERCOMPONENT = 1;
    public static final int MRG_VISITNUMBERFIELD = 5;
    public static final String MDM_MGMT_STUDYMOVE = "STUDYMOVE";
    private static final String EMPTY_VALUE = "";
    public static final String TERSER_PID = "/.PID";
    public static final String TERSER_PV1 = "/.PV1";
    public static final String TERSER_ORC = "/.ORC";
    public static final String TERSER_OBR = "/.OBR";
    public static final String TERSER_MRG = "/.MRG";
    private HL7Server server;
    protected String meAsString = null;

    public HL7Dealer(HL7Server hl7) {
        log = LogFactory.getLog(this.getClass());
        server = hl7;
        pidIdentifierType = hl7.getPidIdentifierType();
        pidAssigningAuthority = hl7.getPidAssigningAuthority();
        pidAddressType = hl7.getPidAddressType();
        String pidCityComponentString = hl7.getPidCityComponent();
        try {
            pidCityComponent = Integer.parseInt(pidCityComponentString);
        } catch (Exception ex) {
            pidCityComponent = DEFAULT_PIDADDRESSCITYCOMPONENT;
        }
        if (pidCityComponent <= 0) {
            pidCityComponent = DEFAULT_PIDADDRESSCITYCOMPONENT;
        }
        visitNumberIssuerComponent = Integer.parseInt(hl7.getVisitNumberIssuerComponent());
        accNumSegment = hl7.getAccNumSegment();
        accNumField = Integer.parseInt(hl7.getAccNumField());
        accNumComponent = Integer.parseInt(hl7.getAccNumComponent());
        accNumRepetition = Integer.parseInt(hl7.getAccNumRepetition());
        if (accNumSegment == null)
            accNumSegment = DEFAULT_ACCNUMSEGMENT;
        if (accNumComponent == 0)
            accNumComponent = DEFAULT_ACCNUMCOMPONENT;
        if (accNumField == 0)
            accNumField = DEFAULT_ACCNUMFIELD;
        if (accNumRepetition == 0)
            accNumRepetition = DEFAULT_ACCNUMREPETITION;
        studyDescriptionSegment = hl7.getStudyDescSegment();
        studyDescriptionField = Integer.parseInt(hl7.getStudyDescField());
        studyDescriptionComponent = Integer.parseInt(hl7.getStudyDescComponent());
        studyDescriptionRepetition = Integer.parseInt(hl7.getStudyDescRepetition());
        if (studyDescriptionSegment == null)
            studyDescriptionSegment = DEFAULT_STUDYDESCSEGMENT;
        if (studyDescriptionComponent == 0)
            studyDescriptionComponent = DEFAULT_STUDYDESCCOMPONENT;
        if (studyDescriptionField == 0)
            studyDescriptionField = DEFAULT_STUDYDESCFIELD;
        if (studyDescriptionRepetition == 0)
            studyDescriptionRepetition = DEFAULT_STUDYDESCREPETITION;
        defaultIdIssuer = hl7.getDefaultIdIssuer();
        idIssuerField = hl7.getIdIssuerField();
        sendingApps = hl7.getSendingAppsList();
    }

    protected abstract void parse(Message hm) throws HL7Exception;

    protected abstract void run() throws Exception;

    protected String getUniqueField() {
        return server.getUniqueField();
    }

    protected boolean toBeForwarded(String messageType) {
        return server.toBeForwarded(messageType);
    }

    protected int getCodeSequenceSegment() {
        return server.getProcedueSequenceSegment();
    }

    protected java.util.Date parseDateTime(String arg) {
        if ((arg == null) || (arg.equals("")) || (arg.length() < 8)) {
            return null;
        }
        String trimmedDateTimeString = null;
        Date parsedDateTime = null;
        SimpleDateFormat sdf = null;
        if (arg.length() >= FORMAT_TO_SECONDS.length()) {
            sdf = new SimpleDateFormat(FORMAT_TO_SECONDS); // Now I have the whole date and time
            trimmedDateTimeString = arg.substring(0, FORMAT_TO_SECONDS.length());
        } else {
            sdf = new SimpleDateFormat(FORMAT_TO_DAY); // Now I have only the whole date
            trimmedDateTimeString = arg.substring(0, FORMAT_TO_DAY.length());
        }
        try {
            parsedDateTime = sdf.parse(trimmedDateTimeString);
        } catch (ParseException pex) {
            log.warn("error parsing date: " + arg, pex);
        }
        return parsedDateTime;
    }

    protected java.util.Date parseDate(String arg) {
        if ((arg == null) || (arg.equals("")) || (arg.length() < 4)) {
            return null;
        }
        String trimmedDateString = null;
        Date parsedDate = null;
        java.text.SimpleDateFormat sdf = null;
        if (arg.length() >= FORMAT_TO_DAY.length()) {
            sdf = new SimpleDateFormat(FORMAT_TO_DAY);
            trimmedDateString = arg.substring(0, FORMAT_TO_DAY.length());
        } else if (arg.length() >= FORMAT_TO_MONTH.length()) {
            sdf = new SimpleDateFormat(FORMAT_TO_MONTH);
            trimmedDateString = arg.substring(0, FORMAT_TO_MONTH.length());
        } else {
            sdf = new SimpleDateFormat(FORMAT_TO_YEAR);
            trimmedDateString = arg.substring(0, FORMAT_TO_YEAR.length());
        }
        try {
            parsedDate = sdf.parse(trimmedDateString);
        } catch (ParseException e) {
            log.warn("error parsing date: " + arg, e);
        }
        return parsedDate;
    }

    protected void initializeDao() throws NamingException {
        bean = InitialContext.doLookup(BeansName.LHl7Dealer);
        hl7publisher = InitialContext.doLookup(BeansName.LHl7Publisher);
    }

    protected final String getPatientId(Segment pid) {
        return getPatientId(pid, true);
    }

    protected final String getPatientId(Segment pid, boolean isPid) {
        String ret = null;
        try {
            if ((pidIdentifierType != null) && (pidIdentifierType.length() != 0)) {
                int patientIdList = getNumOfRepetitions(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST));
                for (int i = PID_FIRSTREPETITION; i <= patientIdList; i++) { // Loop through repetitions
                    int si = getNumOfComponents(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i);
                    if (si >= PID_PIDLIST_IDTYPE) {
                        String identifierType = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i, PID_PIDLIST_IDTYPE); // Get the type in the message
                        if (pidIdentifierType.equalsIgnoreCase(identifierType)) {
                            ret = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i, PID_PIDLIST_ID); // Then this is the address
                            break;
                        }
                    }
                }
                if (ret == null) // if the type was not found
                    ret = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), PID_FIRSTREPETITION, PID_PIDLIST_ID);
            } else if ((pidAssigningAuthority != null) && (pidAssigningAuthority.length() != 0)) {
                int patientIdList = getNumOfRepetitions(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST));
                for (int i = PID_FIRSTREPETITION; i <= patientIdList; i++) { // Loop through repetitions
                    int si = getNumOfComponents(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i);
                    if (si >= PID_PIDLIST_ASSAUTH) {
                        String assigningAuth = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i, PID_PIDLIST_ASSAUTH); // Get the type in the message
                        if (pidAssigningAuthority.equalsIgnoreCase(assigningAuth)) {
                            ret = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i, PID_PIDLIST_ID); // Then this is the address
                            break;
                        }
                    }
                }
                if (ret == null) // if the type was not found
                    ret = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), PID_FIRSTREPETITION, PID_PIDLIST_ID);
            } else {
                ret = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), PID_FIRSTREPETITION, PID_PIDLIST_ID);
            }
        } catch (Exception ex) {
            log.error("Error reading Patient Id", ex);
            ret = null;
        }
        return ret;
    }

    protected final String getPatientIdIssuer(Segment pid) {
        return getPatientIdIssuer(pid, true);
    }

    protected final String getPatientIdIssuer(Segment pid, boolean isPid) {
        String ret = null;
        try {
            if ((pidIdentifierType != null) && (pidIdentifierType.length() != 0)) {
                int patientIdList = getNumOfRepetitions(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST));
                for (int i = PID_FIRSTREPETITION; i <= patientIdList; i++) { // Loop through repetitions
                    int si = getNumOfComponents(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i);
                    if (si >= PID_PIDLIST_IDTYPE) {
                        String identifierType = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i, PID_PIDLIST_IDTYPE); // Get the type in the message
                        if (pidIdentifierType.equalsIgnoreCase(identifierType)) {
                            ret = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i, idIssuerField); // Then this is the address
                            break;
                        }
                    }
                }
                if (ret == null) // if the type was not found
                    ret = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), PID_FIRSTREPETITION, PID_PIDLIST_IDTYPE);
            } else if ((pidAssigningAuthority != null) && (pidAssigningAuthority.length() != 0)) {
                int patientIdList = getNumOfRepetitions(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST));
                for (int i = PID_FIRSTREPETITION; i <= patientIdList; i++) { // Loop through repetitions
                    int si = getNumOfComponents(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i);
                    if (si >= PID_PIDLIST_ASSAUTH) {
                        String assigningAuth = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i, PID_PIDLIST_ASSAUTH); // Get the type in the message
                        if (pidAssigningAuthority.equalsIgnoreCase(assigningAuth)) {
                            ret = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), i, PID_PIDLIST_ASSAUTH); // Then this is the address
                            break;
                        }
                    }
                }
                if (ret == null) // if the type was not found
                    ret = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), PID_FIRSTREPETITION, PID_PIDLIST_ASSAUTH);
            } else {
                ret = get(pid, ((isPid) ? PID_PIDLIST : MRG_PIDLIST), PID_FIRSTREPETITION, PID_PIDLIST_ASSAUTH);
            }
        } catch (Exception ex) {
            log.error("Error reading Patient Id Issuer", ex);
            ret = null;
        }
        return ret;
    }

    protected final String getPatientAddress(Segment pid) {
        String ret = null;
        try {
            if (pidAddressType != null && pidAddressType.length() != 0) {
                int patientAddresses = getNumOfRepetitions(pid, PID_ADDRESS);
                for (int i = PID_FIRSTREPETITION; i <= patientAddresses; i++) { // Loop through repetitions
                    int si = getNumOfComponents(pid, PID_ADDRESS, i);
                    if (si >= PID_ADDRESS_ADDRTYPE) {
                        String addressType = get(pid, PID_ADDRESS, i, PID_ADDRESS_ADDRTYPE); // Get the type in the message
                        if (pidAddressType.equalsIgnoreCase(addressType)) {
                            ret = get(pid, PID_ADDRESS, i, PID_ADDRESS_ADDR); // Then this is the address
                            break;
                        }
                    }
                }
                if (ret == null) // if the type was not found
                    ret = get(pid, PID_ADDRESS, PID_FIRSTREPETITION, PID_ADDRESS_ADDR);
            } else {
                ret = get(pid, PID_ADDRESS, PID_FIRSTREPETITION, PID_ADDRESS_ADDR);
            }
        } catch (Exception ex) {
            log.error("Error reading Patient Address", ex);
            ret = null;
        }
        return ret;
    }

    protected final String getPatientCity(Segment pid) {
        String ret = null;
        try {
            if (pidAddressType != null && pidAddressType.length() != 0) {
                int patientAddresses = getNumOfRepetitions(pid, PID_ADDRESS);
                for (int i = PID_FIRSTREPETITION; i <= patientAddresses; i++) { // Loop through repetitions
                    int si = getNumOfComponents(pid, PID_ADDRESS, i);
                    if (si >= PID_ADDRESS_ADDRTYPE) {
                        String addressType = get(pid, PID_ADDRESS, i, PID_ADDRESS_ADDRTYPE); // Get the type in the message
                        if (pidAddressType.equalsIgnoreCase(addressType)) {
                            ret = get(pid, PID_ADDRESS, i, pidCityComponent); // Then this is the address
                            break;
                        }
                    }
                }
                if (ret == null) // if the type was not found
                    ret = get(pid, PID_ADDRESS, PID_FIRSTREPETITION, pidCityComponent);
            } else {
                ret = get(pid, PID_ADDRESS, PID_FIRSTREPETITION, pidCityComponent);
            }
        } catch (Exception ex) {
            log.error("Error reading Patient City", ex);
            ret = null;
        }
        return ret;
    }

    protected final String getVisitNumber(Segment pv1) {
        String temp = get(pv1, PV1_VISITNUMBERFIELD, PV1_FIRSTREPETITION, PV1_VISITNUMBERCOMPONENT);
        if (visitNumberIssuerComponent > 0 && get(pv1, PV1_VISITNUMBERFIELD, PV1_FIRSTREPETITION, visitNumberIssuerComponent) != null && (!"".equals(get(pv1, PV1_VISITNUMBERFIELD, PV1_FIRSTREPETITION, visitNumberIssuerComponent))))
            temp += (SEPARATOR_VISITNUMBER + get(pv1, PV1_VISITNUMBERFIELD, PV1_FIRSTREPETITION, visitNumberIssuerComponent));
        if ("".equals(temp))
            temp = null;
        return temp;
    }

    protected final String getMrgVisitNumber(Segment mrg) {
        String temp = get(mrg, MRG_VISITNUMBERFIELD, PV1_FIRSTREPETITION, PV1_VISITNUMBERCOMPONENT);
        if (visitNumberIssuerComponent > 0 && get(mrg, MRG_VISITNUMBERFIELD, PV1_FIRSTREPETITION, visitNumberIssuerComponent) != null && (!"".equals(get(mrg, MRG_VISITNUMBERFIELD, PV1_FIRSTREPETITION, visitNumberIssuerComponent))))
            temp += (SEPARATOR_VISITNUMBER + get(mrg, MRG_VISITNUMBERFIELD, PV1_FIRSTREPETITION, visitNumberIssuerComponent)); // Just the field changes, component and repetition stay the same as PV1
        if ("".equals(temp))
            temp = null;
        return temp;
    }

    protected final String getAccessionNumberSegment() {
        return accNumSegment;
    }

    protected final String getAccessionNumber(Segment segment) throws IllegalArgumentException {
        if (segment.getName().equalsIgnoreCase(accNumSegment))
            return get(segment, accNumField, accNumRepetition, accNumComponent);
        else
            throw new IllegalArgumentException("The accession number segment was not correct: passed " + segment.getName() + " expected " + accNumSegment);
    }

    protected final String getStudyDescriptionSegment() {
        return studyDescriptionSegment;
    }

    protected final String getStudyDescription(Segment segment) throws IllegalArgumentException {
        if (segment.getName().equalsIgnoreCase(studyDescriptionSegment))
            return get(segment, studyDescriptionField, studyDescriptionRepetition, studyDescriptionComponent);
        else
            throw new IllegalArgumentException("The study description segment was not correct: passed " + segment.getName() + " expected " + studyDescriptionSegment);
    }

    public boolean canProcess(Message msg) {
        if (sendingApps == null)
            return true;
        Terser terser = new Terser(msg);
        try {
            String sendingApp = terser.get("/.MSH-3-1"); // Sending Application
            return sendingApps.contains(sendingApp);
        } catch (HL7Exception hex) {
            return false;
        }
    }

    public Message processMessage(Message msg, Map<String, Object> metadata) throws ReceivingApplicationException, ca.uhn.hl7v2.HL7Exception {
        synchronized (this) { // This should sync on the instance (-> concrete implementation instead of abstract class)
                              // The goal is avoiding that a call to the scheduler build a monitor also for the merger
                              // Beware that MDM^T* rely on this synchronization when managing moveSH
            p = new Patient();
            try {
                if (server.getCheckPidIdentifierType()) {
                    if (!isIdentifierTypePresent(msg))
                        throw new NoIdentificationTypeException("PidIdentificationType not found! " + pidIdentifierType);
                }
                parse(msg);
                if (bean == null) {
                    initializeDao();
                }
                run();
                return msg.generateACK();
            } catch (HL7SegmentException hsex) {
                try {
                    return msg.generateACK(AcknowledgmentCode.AR, hsex);
                } catch (IOException ioex) {
                    throw new ca.uhn.hl7v2.HL7Exception(ioex);
                }
            } catch (NamingException nex) {
                try {
                    return msg.generateACK(AcknowledgmentCode.AR, new ca.uhn.hl7v2.HL7Exception("Server Internal Error"));
                } catch (IOException ioex) {
                    throw new ca.uhn.hl7v2.HL7Exception(ioex);
                }
            } catch (CannotUpdateException cuex) {
                try {
                    if (server.getAckForUnexistingPatient()) {
                        log.debug("Cannot update but send the ACK anyway \n" + meAsString);
                        return msg.generateACK();
                    }else{
                        log.error("Unable tu update patients." + cuex.getMessage());
                        return msg.generateACK(AcknowledgmentCode.AR, new ca.uhn.hl7v2.HL7Exception(cuex.getMessage()));
                    }
                } catch (IOException e) {
                    throw new ca.uhn.hl7v2.HL7Exception(e);
                }
            } catch (NoIdentificationTypeException ex) {
                log.debug("This message will be ignored ", ex);
                try {
                    return msg.generateACK();
                } catch (IOException e) {
                    throw new ca.uhn.hl7v2.HL7Exception(e);
                }
            } catch (Exception ex) {
                log.fatal("HL7 Server: " + ex.getMessage());
                try {
                    return msg.generateACK(AcknowledgmentCode.AR, new ca.uhn.hl7v2.HL7Exception(ex));
                } catch (IOException ioex) {
                    throw new ca.uhn.hl7v2.HL7Exception(ioex);
                }
            }
        } // end synchronized
    }

    protected String getIheZds(Terser terser, String component) {
        try {
            return terser.get("/.ZDS-1-" + component);
        } catch (HL7Exception e) {
            return "";
        }
    }

    protected int getNumOfRepetitions(Segment segment, int seq) {
        int ret = 0;
        try {
            Type[] repetitions = segment.getField(seq);
            return (repetitions == null) ? 0 : repetitions.length;
        } catch (Exception ex) {
            ret = 0;
        }
        return ret;
    }

    protected int getNumOfComponents(Segment segment, int seq, int rep) {
        int ret = 0;
        try {
            Type[] repetitions = segment.getField(seq);
            if (repetitions == null || repetitions.length < rep)
                return 0;
            if (repetitions[rep - 1] instanceof Composite) {
                Type[] components = ((Composite) repetitions[rep - 1]).getComponents();
                ret = (components == null) ? 0 : components.length;
            } else {
                ret = 1;
            }
        } catch (Exception ex) {
            ret = 0;
        }
        return ret;
    }

    protected String get(Segment segment, int seq, int rep, int comp) {
        String ret = EMPTY_VALUE;
        try {
            Type[] repetitions = segment.getField(seq);
            if (repetitions == null || repetitions.length < rep)
                return EMPTY_VALUE;
            if (repetitions[rep - 1] instanceof Composite) {
                ret = ((Composite) repetitions[rep - 1]).getComponent(comp - 1).encode();
            } else if (repetitions[rep - 1] instanceof Varies) {
                if (((Varies) repetitions[rep - 1]).getData() instanceof Composite)
                    ret = ((Composite) ((Varies) repetitions[rep - 1]).getData()).getComponent(comp - 1).encode();
                else
                    ret = repetitions[rep - 1].encode();
            } else {
                ret = repetitions[rep - 1].encode();
            }
        } catch (Exception ex) {
            ret = EMPTY_VALUE;
        }
        return ret;
    }

    protected String get(Segment segment, int seq, int rep) throws HL7Exception {
        return get(segment, seq, rep, 1);
    }

    protected String get(Segment segment, int seq) throws HL7Exception {
        return get(segment, seq, 1, 1);
    }

    protected boolean isIdentifierTypePresent(Message msh) {
        Terser te = new Terser(msh);
        try {
            Segment seg = te.getSegment(TERSER_PID);
            boolean retPID = false;
            boolean retMRG = false;
            int patientIdList = getNumOfRepetitions(seg, PID_PIDLIST);
            for (int i = PID_FIRSTREPETITION; i <= patientIdList; i++) { // Loop through repetitions
                int si = getNumOfComponents(seg, PID_PIDLIST, i);
                if (si >= PID_PIDLIST_IDTYPE) {
                    String identifierType = get(seg, PID_PIDLIST, i, PID_PIDLIST_IDTYPE); // Get the type in the message
                    if (pidIdentifierType.equalsIgnoreCase(identifierType)) {
                        retPID = true;
                        break;
                    }
                }
            }
            try {
                Segment mrg = te.getSegment(TERSER_MRG);
                int patientIdListMRG = getNumOfRepetitions(mrg, MRG_PIDLIST);
                for (int i = PID_FIRSTREPETITION; i <= patientIdListMRG; i++) { // Loop through repetitions
                    int si = getNumOfComponents(mrg, MRG_PIDLIST, i);
                    if (si >= PID_PIDLIST_IDTYPE) {
                        String identifierType = get(mrg, MRG_PIDLIST, i, PID_PIDLIST_IDTYPE); // Get the type in the message
                        if (pidIdentifierType.equalsIgnoreCase(identifierType)) {
                            retMRG = true;
                            return retMRG && retPID;
                        }
                    }
                }
            } catch (HL7Exception ex) {
                retMRG = true;
                return retPID;
            }
            return retPID && retMRG;
        } catch (HL7Exception e) {
            log.warn("No PID present!", e);
            return false;
        }
    }
}
