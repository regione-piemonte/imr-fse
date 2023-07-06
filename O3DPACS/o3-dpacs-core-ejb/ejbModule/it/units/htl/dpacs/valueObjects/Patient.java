/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.ArrayList;
import java.text.DateFormat;
// ---
// ---

/**
 * The class keep all the informations about a patient
 * @author Mbe
 */
public class Patient implements HtlVo, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    // ---
    private String patientId = null;
    // From ; Into Patients.patientID
    //	private List idsToMatch=null;
    // NOT applicable to patient level
    private String idIssuer = null;
    // From ; Into Patients.idIssuer
    private String lastName = null;
    // From ; Into Patients.lastName
    private String firstName = null;
    // From ; Into Patients.firstName
    private String middleName = null;
    // From ; Into Patients.middleName
    private String prefix = null;
    // From ; Into Patients.prefix
    private String suffix = null;
    // From ; Into Patients.suffix
    private Date birthDate = null;
    // From DICOM; Into Patients.birthDate */
    private Date birthDateLate = null;
    // For matching on ranges
    private String birthDateAsString = null;
    private Time birthTime = null;
    // From DICOM; Into Patients.birthTime */
    private Time birthTimeLate = null;
    // For matching on ranges
    private String sex = null; /*char*/
    // From ; Into Patients.sex
    private String ethnicGroup = null;
    // From ; Into PatientDemographics.ethnicGroup
    private String race = null;
    // From ; Into PatientDemographics.race
    private String patientAddress = null;
    private String patientCity = null;
    private String numberOfPatientRelatedStudies = null; /*int*/
    // From ; Into PatientDemographics.numberOfPatientRelatedStudies
    private String numberOfPatientRelatedSeries = null;
    // For DICOM matching
    private String numberOfPatientRelatedInstances = null;
    // For DICOM matching
    private String patientAccountNumber = null;
    // From ; Into PatientDemographics.patientAccountNumber
    private String patientIdentifierList = null;
    // From ; Into PatientDemographics.patientIdentifierList
    private String patientComments = null;
    // From ; Into PatientDemographics.patientComments
    private String patientState = null;
    // From ; Into WLPatientDataPerVisit.patientState
    private String patientClass = null;
    // From HL7; Into WLPatientDataPerVisit.patientClass
    private String assignedPatientLocation = null;
    // From HL7; Into WLPatientDataPerVisit.assignedPatientLocation - The temporary one is ignored!
    private String visitNumber = null;
    // From HL7; Into WLPatientDataPerVisit.visitNumber
    private String pregnancyStatus = null; /*int*/
    // From ; Into WLPatientDataPerVisit.pregnancyStatus
    private String medicalAlerts = null;
    // From ; Into WLPatientDataPerVisit.medicalAlerts
    private String patientWeight = null; /*long*/
    // From ; Into WLPatientDataPerVisit.patientWeight
    private String confidentialityConstraint = null;
    // From ; Into WLPatientDataPerVisit.confidentialityConstraint
    private String specialNeeds = null;
    // From ; Into WLPatientDataPerVisit.specialNeeds
    private List<Observation> obs = null;
    // From IHE[4,12,13]-HL7; Into HL7Observations  A list containing Observation(s)
    private List<Allergy> alls = null;
    // From IHE[4,12,13]-HL7; Into HL7Allergies A list containing Allergy(s)
    private List<OtherIdAndIssuer> otherIds = null;
    // For DPACS to track other ids and their issuers
    private char toPerform = DicomConstants.INSERT;
    // ---
    private String pk = null;
//  patient name in DICOM Format
    private String dcmPatientName = null;
    
   
    /**
     * Instantiates a new patient.
     */
    public Patient() {
    }

       /**
     * Instantiates a new patient.
     *
     * @param pId the id
     * @param idIssuer the id issuer
     */
    public Patient(String pId, String idIssuer) {
    }

    /**
     * Instantiates a new patient.
     *
     * @param pId the id
     * @param idIssuer the id issuer
     * @param toPerform the to perform
     */
    public Patient(String pId, String idIssuer, char toPerform) {
    }
    
    public void setLastName(String ln) {
        lastName = prepareString(ln, 60);
        if(lastName!=null)
        	lastName=lastName.toUpperCase();
    }

    public void setFirstName(String fn) {
        firstName = prepareString(fn, 60);
        if(firstName!=null)
        	firstName=firstName.toUpperCase();
    }

    public void setMiddleName(String mn) {
        middleName = prepareString(mn, 60);
        if(middleName!=null)
        	middleName=middleName.toUpperCase();
    }

    public void setPrefix(String p) {
        prefix = prepareString(p, 60);
        if(prefix!=null)
        	prefix=prefix.toUpperCase();
    }

    public void setSuffix(String s) {
        suffix = prepareString(s, 60);
        if(suffix!=null)
        	suffix=suffix.toUpperCase();
    }

    public void setBirthDate(Date bd) {
        birthDate = bd;
    }

    public void setBirthDateRange(Date bdEarly, Date bdLate) {
        birthDate = bdEarly;
        birthDateLate = bdLate;
    }

    public void setBirthTime(Time bt) {
        birthTime = bt;
    }

    public void setBirthTimeRange(Time btEarly, Time btLate) {
        birthTime = btEarly;
        birthTimeLate = btLate;
    }

    public void setSex(String s) {
        sex = prepareString(s, 1);
        if(sex!=null)
        	sex=sex.toUpperCase();
    }

    public void setPatientId(String pi) {
        //idsToMatch=null;
        // Avoid setting both the single ID and the list of IDs
        patientId = prepareString(pi, 64);
        if(patientId!=null)
        	patientId=patientId.toUpperCase();
    }

    public void setIdIssuer(String ii) {
        idIssuer = prepareString(ii, 64);
        if(idIssuer!=null)
        	idIssuer=idIssuer.toUpperCase();
    }

    public void setEthnicGroup(String eg) {
        ethnicGroup = prepareString(eg, 16);
    }

    public void setRace(String r) {
        race = prepareString(r, 32);
    }

    public void setPatientAddress(String pa) {
        patientAddress = prepareString(pa, 64);
    }

    public void setPatientCity(String pc) {
        patientCity = prepareString(pc, 64);
    }

    public void setNumberOfPatientRelatedStudies(String noprs) {
        // ---
        numberOfPatientRelatedStudies = prepareInt(noprs);
    }

    public void setNumberOfPatientRelatedSeries(String noprs) {
        // ---
        numberOfPatientRelatedSeries = prepareInt(noprs);
    }

    public void setNumberOfPatientRelatedInstances(String nopri) {
        // ---
        numberOfPatientRelatedInstances = prepareInt(nopri);
    }

    public void setPatientAccountNumber(String pan) {
        patientAccountNumber = prepareString(pan, 32);
    }

    // /** BEWARE: it can have the side effect of setting patientId and idIssuer as well */
    public void setPatientIdentifierList(String pil, boolean ihe) {
        patientIdentifierList = prepareString(pil, 64);
        if(patientIdentifierList!=null)
        	patientIdentifierList=patientIdentifierList.toUpperCase();
        if (ihe) {
            patientId = patientIdentifierList;
            //  // FOR IHE!!!
            idIssuer = "STATO ITALIANO";
            //  // TODO: who decides the issuers' ids?
        } // end if
    }

    public void setPatientIdentifierList(String pil) {
        setPatientIdentifierList(pil, false);
        //  // TODO: This sets just one attribute!!!
    }

    public void setPatientComments(String pc) {
        patientComments = prepareString(pc, 256);
    }

    public void setPatientState(String ps) {
        patientState = prepareString(ps, 64);
    }

    public void setPatientClass(String pc) {
        patientClass = prepareString(pc, 1);
    }

    public void setAssignedPatientLocation(String apl) {
        assignedPatientLocation = prepareString(apl, 80);
    }

    public void setVisitNumber(String vn) {
        visitNumber = prepareString(vn, 20);
    }

    public void setPregnancyStatus(String ps) {
        pregnancyStatus = prepareInt(ps);
    }

    public void setMedicalAlerts(String ma) {
        medicalAlerts = prepareString(ma, 64);
    }

    public void setPatientWeight(String pw) {
        patientWeight = prepareLong(pw);
    }

    public void setConfidentialityConstraint(String cc) {
        confidentialityConstraint = prepareString(cc, 64);
    }

    public void setSpecialNeeds(String sn) {
        specialNeeds = prepareString(sn, 64);
    }

    public void setPrimaryKey(String pk) {
        //  // This represents the pk of table Patients!!!!
        this.pk = pk;
    }

    public void addObservation(Observation o) {
        if (o == null) {
            return;
        }
        if (obs == null) {
            obs = new ArrayList<Observation>(3);
        }
        obs.add(o);
    }

    public void addObservation(String vt, String ov, String u, String ors) {
        if ((vt != null) || (ov != null) || (u != null) || (ors != null)) {
            if (obs == null) {
                obs = new ArrayList<Observation>(3);
            }
            Observation o = new Observation(vt, ov, u, ors);
            obs.add(o);
        } // end if
    }

    public void addAllergy(Allergy a) {
        if (a == null) {
            return;
        }
        if (alls == null) {
            alls = new ArrayList<Allergy>(3);
        }
        alls.add(a);
    }

    public void addAllergy(String at, String am, String as, String ar, Date idd) {
        if ((at != null) || (am != null) || (as != null) || (ar != null) || (idd != null)) {
            if (alls == null) {
                alls = new ArrayList<Allergy>(3);
            }
            Allergy a = new Allergy(at, am, as, ar, idd);
            alls.add(a);
        } // end if
    }

    public void addOtherId(OtherIdAndIssuer oi) {
        if (oi == null) {
            return;
        }
        if (otherIds == null) {
            otherIds = new ArrayList<OtherIdAndIssuer>(3);
        }
        otherIds.add(oi);
    }

    public void addOtherId(String id, String issuer) {
        if ((id != null) || (issuer != null)) {
            if (otherIds == null) {
                otherIds = new ArrayList<OtherIdAndIssuer>(3);
            }
            OtherIdAndIssuer oi = new OtherIdAndIssuer(id, issuer);
            otherIds.add(oi);
        } // end if
    }

    public String getPatientId() {
        return patientId;
    }

    public String getIdIssuer() {
        return idIssuer;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public Date getBirthDateLate() {
        return birthDateLate;
    }

    // /**
    // @param df It should be got by DateFormat.getDateInstance to have consistent results
    // */
    public String getBirthDate(DateFormat df) {
        if (birthDate == null) {
            return null;
        }
        return (df == null) ? Long.toString(birthDate.getTime()) : df.format(birthDate);
    }

    // /**
    // @param df It should be got by DateFormat.getDateInstance to have consistent results
    // */
    public String getBirthDateLate(DateFormat df) {
        if (birthDateLate == null) {
            return null;
        }
        return (df == null) ? Long.toString(birthDateLate.getTime()) : df.format(birthDateLate);
    }

    public Time getBirthTime() {
        return birthTime;
    }

    public Time getBirthTimeLate() {
        return birthTimeLate;
    }

    // /**
    // @param tf It should be got by DateFormat.getTimeInstance to have consistent results
    // */
    public String getBirthTime(DateFormat tf) {
        if (birthTime == null) {
            return null;
        }
        return (tf == null) ? Long.toString(birthTime.getTime()) : tf.format(birthTime);
    }

    // /**
    // @param tf It should be got by DateFormat.getTimeInstance to have consistent results
    // */
    public String getBirthTimeLate(DateFormat tf) {
        if (birthTimeLate == null) {
            return null;
        }
        return (tf == null) ? Long.toString(birthTimeLate.getTime()) : tf.format(birthTimeLate);
    }

    public String getSex() {
        return sex;
    }

    public String getEthnicGroup() {
        return ethnicGroup;
    }

    public String getRace() {
        return race;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public String getPatientCity() {
        return patientCity;
    }

    public String getNumberOfPatientRelatedStudies() {
        // ---
        return numberOfPatientRelatedStudies;
    }

    public String getNumberOfPatientRelatedSeries() {
        // ---
        return numberOfPatientRelatedSeries;
    }

    public String getNumberOfPatientRelatedInstances() {
        // ---
        return numberOfPatientRelatedInstances;
    }

    public String getPatientAccountNumber() {
        return patientAccountNumber;
    }

    public String getPatientIdentifierList() {
        return patientIdentifierList;
    }

    public String getPatientComments() {
        return patientComments;
    }

    public String getPatientState() {
        return patientState;
    }

    public String getPatientClass() {
        return patientClass;
    }

    public String getAssignedPatientLocation() {
        return assignedPatientLocation;
    }

    public String getVisitNumber() {
        return visitNumber;
    }

    public String getPregnancyStatus() {
        return pregnancyStatus;
    }

    public String getMedicalAlerts() {
        return medicalAlerts;
    }

    public String getPatientWeight() {
        return patientWeight;
    }

    public String getConfidentialityConstraint() {
        return confidentialityConstraint;
    }

    public String getSpecialNeeds() {
        return specialNeeds;
    }

    public String getPrimaryKey() {
        return pk;
    }

    public String getDcmPatientName() {
        return dcmPatientName;
    }

    public void setDcmPatientName(String dcmPatientName) {
        this.dcmPatientName = dcmPatientName;
    }

    public Observation[] getObservations() {
        if (obs == null) {
            return null;
        }
        int s = obs.size();
        Observation[] temp = new Observation[s];
        obs.toArray(temp);
        //  // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }

    public Allergy[] getAllergies() {
        if (alls == null) {
            return null;
        }
        int s = alls.size();
        Allergy[] temp = new Allergy[s];
        alls.toArray(temp);
        //  // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }

    public OtherIdAndIssuer[] getOtherIds() {
        if (otherIds == null) {
            return null;
        }
        int s = otherIds.size();
        OtherIdAndIssuer[] temp = new OtherIdAndIssuer[s];
        otherIds.toArray(temp);
        //  // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }

    public String prepareString(String arg, int len) {
        if (arg == null) {
            return null;
        }
        String temp = arg.trim();
        return (temp.length() > len) ? temp.substring(0, len) : temp;
    }

    public String prepareLong(String arg) {
        if (arg == null) {
            return null;
        }
        String temp = null;
        try {
            temp = (Long.valueOf(arg.trim())).toString();
        } catch (NumberFormatException e) {
            temp = null;
        }
        return temp;
    }

    public String prepareInt(String arg) {
        if (arg == null) {
            return null;
        }
        String temp = null;
        try {
            temp = (Integer.valueOf(arg.trim())).toString();
        } catch (NumberFormatException e) {
            temp = null;
        }
        return temp;
    }

    public void setToPerform(char arg) {
        if ((arg == DicomConstants.INSERT) || (arg == DicomConstants.FIND) || (arg == DicomConstants.UPDATE)) {
            toPerform = arg;
        } else {
            toPerform = DicomConstants.INSERT;
        }
    }

    public char getToPerform() {
        return toPerform;
    }

    public void reset() {
        patientId = null;
        //idsToMatch=null;
        idIssuer = null;
        lastName = null;
        firstName = null;
        middleName = null;
        prefix = null;
        suffix = null;
        birthDate = null;
        birthDateLate = null;
        birthDateAsString = null;
        birthTime = null;
        birthTimeLate = null;
        sex = null;
        ethnicGroup = null;
        race = null;
        patientAddress = null;
        numberOfPatientRelatedStudies = null;
        numberOfPatientRelatedSeries = null;
        numberOfPatientRelatedInstances = null;
        patientAccountNumber = null;
        patientIdentifierList = null;
        patientComments = null;
        patientState = null;
        patientClass = null;
        assignedPatientLocation = null;
        visitNumber = null;
        pregnancyStatus = null;
        medicalAlerts = null;
        patientWeight = null;
        confidentialityConstraint = null;
        specialNeeds = null;
        obs = null;
        alls = null;
        otherIds = null;
        toPerform = DicomConstants.INSERT;
        pk = null;
    }
    // end reset()

// Inner Classes:
    public final class Observation {

        // ---
        private String valueType = null;
        // From ; Into HL7Observations.valueType
        private String observationValue = null;
        // From ; Into HL7Observations.observationValue
        private String units = null;
        // From ; Into HL7Observations.units
        private String observResultStatus = null; /*char*/
        // From ; Into HL7Observations.observResultStatus

        // Accessor Methods:
        public void setValueType(String vt) {
            valueType = prepareString(vt, 3);
        }

        public void setObservationValue(String ov) {
            observationValue = prepareString(ov, 65536);
        }

        public void setUnits(String u) {
            units = prepareString(u, 60);
        }

        public void setObservResultStatus(String ors) {
            observResultStatus = prepareString(ors, 1);
        }

        public String getValueType() {
            return valueType;
        }

        public String getObservationValue() {
            return observationValue;
        }

        public String getUnits() {
            return units;
        }

        public String getObservResultStatus() {
            return observResultStatus;
        }
        // Constructors:

        public Observation() {
        }
        // Default Constructor

        public Observation(String vt, String ov, String u, String ors) {
            valueType = prepareString(vt, 3);
            observationValue = prepareString(ov, 65536);
            units = prepareString(u, 60);
            observResultStatus = prepareString(ors, 1);
        }
    } // end inner class Observations

    public final class Allergy {

        // ---
        private String allergyType = null;
        // From ; Into HL7Allergies.allergyType
        private String allergyMnemonic = null;
        // From ; Into HL7Allergies.allergyMnemonic
        private String allergySeverity = null;
        // From ; Into HL7Allergies.allergySeverity
        private String allergyReaction = null;
        // From ; Into HL7Allergies.allergyReaction
        private Date identificationDate = null;
        // From ; Into HL7Allergies.identificationDate

        // Accessor Methods:
        public void setAllergyType(String at) {
            allergyType = prepareString(at, 2);
        }

        public void setAllergyMnemonic(String am) {
            allergyMnemonic = prepareString(am, 64);
        }

        public void setAllergySeverity(String as) {
            allergySeverity = prepareString(as, 2);
        }

        public void setAllergyReaction(String ar) {
            allergyReaction = prepareString(ar, 64);
        }

        public void setIdentificationDate(Date idd) {
            identificationDate = idd;
        }

        public String getAllergyType() {
            return allergyType;
        }

        public String getAllergyMnemonic() {
            return allergyMnemonic;
        }

        public String getAllergySeverity() {
            return allergySeverity;
        }

        public String getAllergyReaction() {
            return allergyReaction;
        }

        public Date getIdentificationDate() {
            return identificationDate;
        }
        // Constructors:

        public Allergy() {
        }
        // Default Constructor

        public Allergy(String at, String am, String as, String ar, Date idd) {
            allergyType = prepareString(at, 2);
            allergyMnemonic = prepareString(am, 64);
            allergySeverity = prepareString(as, 2);
            allergyReaction = prepareString(ar, 64);
            identificationDate = idd;
        }
    } // end inner class Allergies

    public final class OtherIdAndIssuer {

        // Private Attributes:
        private String otherId = null;
        private String otherIssuer = null;
        // Accessor Methods:

        public void setOtherId(String id) {
            otherId = prepareString(id, 64);
        }

        public void setOtherIdIssuer(String issuer) {
            otherIssuer = prepareString(issuer, 64);
        }

        public String getOtherId() {
            return otherId;
        }

        public String getOtherIdIssuer() {
            return otherIssuer;
        }
        // Constructors:

        public OtherIdAndIssuer() {
        }
        // Default Constructor

        public OtherIdAndIssuer(String id, String issuer) {
            otherId = prepareString(id, 64);
            otherIssuer = prepareString(issuer, 64);
        }
        
    }
    // end inner class OtherIdAndIssuer


    public String getBirthDateAsString() {
        return birthDateAsString;
    }

    public void setBirthDateAsString(String birthDateAsString) {
        this.birthDateAsString = birthDateAsString;
    }
}