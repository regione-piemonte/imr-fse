/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.exceptions.IncorrectPatientIdException;
import it.units.htl.dpacs.exceptions.MultiplePatientsIdentifiedException;
import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.helpers.GlobalSettings;
import it.units.htl.dpacs.helpers.IheStudyId;
import it.units.htl.dpacs.helpers.ImageAvailabilityConfig;
import it.units.htl.dpacs.helpers.PatientIdCheckSettings;
import it.units.htl.dpacs.valueObjects.CodeSequence;
import it.units.htl.dpacs.valueObjects.MoveStudyHistory;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.dpacs.valueObjects.Study;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.swing.text.DateFormatter;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class HL7DealerBean implements HL7DealerLocal, HL7DealerRemote {
//    private static final long serialVersionUID = 8103647153902666681L;
    private static final String OLD_DEFAULT_ISSUER = "NONE";
    private static final String identPatient = "SELECT P.lastName, P.firstName, P.middleName, P.birthDate, P.sex, P.pk, PD.patientAccountNumber, PD.patientIdentifierList, PD.pk, P.patientID, P.idIssuer, P.birthTime, PD.ethnicGroup FROM Patients P INNER JOIN PatientDemographics PD ON PD.patientFK=P.pk WHERE P.deprecated=0 AND P.patientID=? AND P.idIssuer=? AND (P.lastName LIKE ? or P.lastName is null) AND (PD.patientAccountNumber LIKE ? OR PD.patientAccountNumber IS NULL)";
    private static final String updateIdIssuer = "UPDATE Patients SET idIssuer=? WHERE pk=?";
    // private static final String identHL7Pat = "SELECT Patients.pk, PatientDemographics.pk, MAX(WLPatientDataPerVisit.pk), WLPatientDataPerVisit.assignedPatientLocation, WLPatientDataPerVisit.patientClass FROM Patients, PatientDemographics, WLPatientDataPerVisit WHERE UPPER(PatientDemographics.patientIdentifierList)=UPPER(?) AND UPPER(PatientDemographics.patientAccountNumber)=UPPER(?) AND UPPER(Patients.lastName)=UPPER(?) AND UPPER(Patients.firstName)=UPPER(?) AND PatientDemographics.patientFK=Patients.pk AND WLPatientDataPerVisit.patientFK=Patients.pk GROUP BY Patients.pk, PatientDemographics.pk, WLPatientDataPerVisit.assignedPatientLocation, WLPatientDataPerVisit.patientClass";
    private static final String hl7Out2In = "UPDATE WLPatientDataPerVisit SET patientClass=?, assignedPatientLocation=? WHERE pk=?";
    private static final String hl7In2Out = "UPDATE WLPatientDataPerVisit SET patientClass=?, assignedPatientLocation=NULL WHERE pk=?";
    private static final String updateOrder = "UPDATE WLPatientDataPerVisit SET visitNumber=? WHERE studyFK=?";
    private static final String updateAfterDicom = "SELECT Patients.patientID, Patients.idIssuer, Patients.lastname, Patients.firstName, Patients.middleName, Patients.prefix, Patients.suffix, Patients.birthDate, Patients.birthTime, Patients.sex, PatientDemographics.patientIdentifierList, PatientDemographics.race, PatientDemographics.patientAddress, PatientDemographics.patientCity, PatientDemographics.patientAccountNumber, Studies.accessionNumber, Patients.pk, PatientDemographics.pk FROM Patients, PatientDemographics, Studies WHERE PatientDemographics.patientFK=Patients.pk AND Studies.patientFK=Patients.pk AND UPPER(Patients.patientID)=UPPER(?) AND (UPPER(Patients.lastName) LIKE UPPER(?) OR Patients.lastName IS NULL) AND UPPER(PatientDemographics.patientIdentifierList)=UPPER(?) AND Studies.studyInstanceUID=?";
    private static final String updateOnORM = "SELECT Patients.patientID, Patients.idIssuer, Patients.lastname, Patients.firstName, Patients.middleName, Patients.prefix, Patients.suffix, Patients.birthDate, Patients.birthTime, Patients.sex, PatientDemographics.patientIdentifierList, PatientDemographics.race, PatientDemographics.patientAddress, PatientDemographics.patientCity, PatientDemographics.patientAccountNumber, Studies.accessionNumber, Patients.pk, PatientDemographics.pk FROM Patients, PatientDemographics, Studies WHERE PatientDemographics.patientFK=Patients.pk AND Studies.patientFK=Patients.pk AND UPPER(Patients.patientID)=UPPER(?) AND (UPPER(Patients.lastName) LIKE UPPER(?) OR Patients.lastName IS NULL) AND UPPER(PatientDemographics.patientIdentifierList)=UPPER(?) ";
//    private static final String insertPatVisit = "INSERT INTO WLPatientDataPerVisit(patientClass, assignedPatientLocation, visitNumber, patientFK, studyFK) VALUES(?, ?, ?, ?, ?)";
//    private static final String upPatVisit = "UPDATE WLPatientDataPerVisit SET patientClass=?, assignedPatientLocation=?, visitNumber=? WHERE patientFK=? AND studyFK=?";
//    private static final String selectLatestPatVisit = "SELECT MAX(pk) FROM WLPatientDataPerVisit WHERE patientFK=? AND studyFK=?";
    private static final String startStudy = "INSERT INTO Studies(studyInstanceUID, accessionNumber, patientFK, procedureCodeSequenceFK, studyDescription, studyDate) VALUES(?, ?, ?, ?, ?, ?)";
    private static final String updatePatientStudies = "UPDATE PatientDemographics SET numberOfPatientRelatedStudies=numberOfPatientRelatedStudies+? WHERE patientFK=?";
    private static final String selectInsertedCodSeq = "SELECT max(pk) FROM CodeSequences WHERE (codeValue=? OR codeValue IS NULL) AND (codingSchemeDesignator=? OR codingSchemeDesignator IS NULL) AND (codingSchemeVersion=? OR codingSchemeVersion IS NULL)";
    private static final String insertCodSeq = "INSERT INTO CodeSequences(codeValue, codingSchemeDesignator, codingSchemeVersion, codeMeaning) VALUES (?, ?, ?, ?)";
    static final Log log = LogFactory.getLog(HL7DealerBean.class);
    private @Resource(name = "java:/jdbc/hl7DS")
    DataSource dataSource;
    private static final String insertMoveStudyHistory = "INSERT INTO MoveStudyHistory (calledAet, callingAet, moveAet, accessionNumber, structAsr, knownNodeFk,  ris, messageId) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String selectKnownNodeStruct = "SELECT a.callingAet, a.knownNodeFk, k.aeTitle calledAet FROM knownNodeToStructAsr a, knownNodes k where a.structAsr = ? and k.pk = a.knownNodeFk";
    @EJB
    private ImageAvailabilityLocal iaBean;
    private String dbDateFormat = "yyyy-MM-dd";

    public HL7DealerBean() {
        dbDateFormat = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.DB_DATE_FORMAT);
    }

    @PostConstruct
    public void ejbCreate() {
    } // end ejbCreate

    public void ejbRemove() {
    }

    public void setSessionContext(SessionContext sc) {
    }

    // Business Methods
    private Connection getDBConnection() throws SQLException {
        Connection dbConn;
        try {
            dbConn = dataSource.getConnection();
            return dbConn;
        } catch (SQLException ex) {
            log.error("Unable to get DB Connection.", ex);
            throw ex;
        }
    }

    public long initializePatient(Patient p, Study st) throws CannotStoreException {
        if (p == null)
            throw new CannotStoreException(CannotStoreException.NULL_PATIENT);
        if ((p.getPatientId() == null) || (p.getIdIssuer() == null))
            throw new CannotStoreException(CannotStoreException.NO_ID_INFO);
        log.info("HL7Dealer: Initializing Patient " + p.getPatientIdentifierList());
        Long patPk = identifyPatient(p);
        if (patPk == null) {
            try {
                patPk = storeNewPatient(p); // This doesn't return null, rather throws a SQLException
                p.setPrimaryKey("" + patPk);
            } catch (SQLException sex) {
                log.error(CannotStoreException.NO_PATIENT, sex);
                throw new CannotStoreException(CannotStoreException.NO_PATIENT);
            } catch (IncorrectPatientIdException e) {
                throw e;
            }
        } else {
            p.setPrimaryKey("" + patPk);
            updateStoredPatient(p, st);
        } // end if
        log.info("HL7Dealer: Identified Patient " + patPk);
        if (st.getStudyInstanceUid() != null) {
            if (!isAlreadyThere(st.getStudyInstanceUid())) {
                log.debug("Initializing study " + st.getStudyInstanceUid());
                if (startupStudy(st, Long.parseLong(p.getPrimaryKey())) != 1)
                    return -1; // -1 if you have the patient, not the study!
            } else {
                reconcileStudy(p, st);
            }
            try {
                // I pass also Patient's pk to use as foreign key
                long fk = addPatientVisit(p, patPk, st.getStudyInstanceUid());
                log.debug("Last visit for this patient (pk =" + patPk + ") is : " + fk);
            } catch (SQLException sex) {
                log.warn("Visit already present!!!!!!!!!!!!!!!!!!!!!!", sex);
            } // end try...catch
        }
        log.info("HL7Dealer: Initialized Patient");
        return 1; // TODO: Return parameters!
    }

    private void reconcileStudy(Patient p, Study s) {
        Connection con = null;
        CallableStatement cs = null;
        byte publishOnDb = 0;
        String recSource = null;
        String stringToSet = null;
        String targetApp = null;
        try {
            ImageAvailabilityConfig iac = ImageAvailabilityConfig.getInstance();
            if (iac.isEnabled() && ImageAvailabilityConfig.PUBLISHTO_TABLE.equals(iac.getPublicationMethod())) {
                publishOnDb = 1;
                recSource = ImageAvailabilityConfig.RECONCILIATIONSOURCE_WORKLIST;
                stringToSet = iac.getStringForSetting();
                targetApp = iac.getTargetApp();
            }
            con = getConnection();
            cs = con.prepareCall("{call reconcileStudy(?,?,?,?,?,?,?,?)}");
            cs.setString(1, s.getStudyInstanceUid());
            cs.setString(2, s.getAccessionNumber());
            cs.setString(3, p.getPatientId());
            cs.setString(4, p.getIdIssuer());
            cs.setByte(5, publishOnDb);
            cs.setString(6, recSource);
            cs.setString(7, stringToSet);
            cs.setString(8, targetApp);
            cs.execute();
        } catch (Exception ex) {
            log.error("HL7Dealer: Error reconciling study " + s.getStudyInstanceUid(), ex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
    }

    public long cancelDiscontinue(Patient p, Study s) throws CannotUpdateException {
        long l = 0;
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getConnection();
            cs = con.prepareCall("{call cancelDiscontinueStudy(?,?)}");
            cs.setString(1, s.getStudyInstanceUid());
            cs.setString(2, p.getPatientId());
            cs.execute();
        } catch (Exception ex) {
            log.error("HL7Dealer: Error discontinuing/cancelling a patient: ", ex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return l;
    } // end cancelDiscontinue

    public long updateOrder(Patient p, Study s) throws CannotUpdateException {
        long l = 0;
        log.info("HL7Dealer: About to update info about study " + s.getStudyInstanceUid());
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(updateOrder);
            ps.setString(1, p.getVisitNumber());
            ps.setString(2, s.getStudyInstanceUid());
            if (s.getStudyInstanceUid() != null)
                l = ps.executeUpdate();
        } catch (Exception ex) {
            log.error("HL7Dealer: Error discontinuing/cancelling a patient: ", ex);
            throw new CannotUpdateException(CannotUpdateException.STUDY_PROBLEMS);
        } finally {
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return l;
    } // end updateOrder

    public int transfer(Patient p) throws CannotUpdateException {
        int ans = 0;
        ans = performTransfer(p);
        return ans;
    } // end transfer

    public int undoTransfer(Patient p) throws CannotUpdateException {
        log.warn("Transfer not yet supported!!");
        int ans = 0;
        return ans;
    } // end undoTransfer

    public int discharge(Patient p) throws CannotUpdateException {
        log.warn("Discharge not yet supported!!");
        int ans = 0;
        return ans;
    } // end discharge

    public int undoDischarge(Patient p) throws CannotUpdateException {
        log.warn("UndoDischarge not yet supported!!");
        int ans = 0;
        return ans;
    }

    public int outToIn(Patient p) throws CannotUpdateException {
        int ans = 0;
        try {
            String[] pks = null;
            // pks=findPatient(p); // Almost the same as identifyPatient(), but based on something more and returns something different!
            if (pks == null)
                throw new CannotUpdateException(CannotUpdateException.PATIENT_PROBLEMS);
            ans = performOut2In(p, Long.parseLong(pks[2]));
        } catch (SQLException sex) {
            log.error("An error occurred: ", sex);
        }
        return ans;
    } // end outToIn

    public int inToOut(Patient p) throws CannotUpdateException {
        int ans = 0;
        try {
            String[] pks = null;
            // pks=findPatient(p);
            if (pks == null)
                throw new CannotUpdateException(CannotUpdateException.PATIENT_PROBLEMS);
            ans = performIn2Out(p, Long.parseLong(pks[2]));
        } catch (SQLException sex) {
            log.error("An error occurred: ", sex);
        }
        return ans;
    } // end inToOut

    private Connection getConnection() throws SQLException, NamingException {
        try {
            InitialContext jndiContext = new InitialContext();
            DataSource dataSource = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
            return dataSource.getConnection();
        } catch (SQLException sex) {
            log.fatal("Unable to create Connection to DB", sex);
            throw sex;
        } catch (NamingException nex) {
            log.fatal("Unable to retrieve the DataSource", nex);
            throw nex;
        }
    }

    /**
     * Update informations about patient modifying both Patient and PatientDemographics tables of the patient with the specified primaryKey (Patient.pk)<br>
     * 
     * @param p
     *            Patient
     * @param patientPk
     *            is the primary key of the patient to be updated
     * @return the total number of the rows updated (the sum of the updated rows in Patient and PatientDemographics tables)
     */
    private int updatePatientInformation(Patient p, long patientPk) {
        int updatedRows = 0;
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getConnection();
            cs = con.prepareCall("{call updatePatientInformation(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            cs.setLong(1, patientPk);
            cs.setString(2, p.getLastName());
            cs.setString(3, p.getFirstName());
            cs.setString(4, p.getMiddleName());
            cs.setString(5, p.getPrefix());
            cs.setString(6, p.getSuffix());
            cs.setString(7, p.getSex());
            cs.setDate(8, p.getBirthDate());
            cs.setTime(9, p.getBirthTime());
            cs.setString(10, p.getRace());
            cs.setString(11, p.getPatientAccountNumber());
            cs.setString(12, p.getPatientAddress());
            cs.setString(13, p.getPatientCity());
            cs.registerOutParameter(14, Types.INTEGER);
            cs.execute();
            updatedRows = cs.getInt(14);
        } catch (SQLException e) {
            log.error("", e);
        } catch (NamingException e) {
            log.error("", e);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return updatedRows;
    }

    public int updatePatient(Patient p) throws CannotUpdateException {
        int ans = 0;
        long[] pks = null;
        log.info("HL7Dealer: Updating patient info about (patientIdentifierList):" + p.getPatientIdentifierList());
        try {
            pks = retrievePks(p);
            if (pks == null)
                throw new CannotUpdateException(CannotUpdateException.PATIENT_PROBLEMS);
            updatePatientInformation(p, pks[0]);
            int modified = resetStudiesToReconcile(null, pks[0]);
            log.info("Patient " + pks[0] + ": reset " + modified + " studies");
        } catch (SQLException sex) {
            log.fatal("HL7Dealer: Error updating a patient: ", sex);
        }
        return ans;
    } // end updatePatient

    public int mergePatients(Patient old, Patient newPat, Integer groupToUse, String sendingApp) throws IncorrectPatientIdException {
        int ans = 0;
        log.info("HL7Dealer: About to merge " + old.getPatientIdentifierList() + " into " + newPat.getPatientIdentifierList());
        long oldPatPk = 0;
        long oldPatDemosPk = 0;
        long newPatPk = 0;
        long newPatDemosPk = 0;
        String patientIdRegEx = PatientIdCheckSettings.getPatientIdRegEx();
        if (patientIdRegEx != null) {
            log.debug("Checking patientId with this regEx: " + patientIdRegEx);
            Pattern pp = Pattern.compile(patientIdRegEx);
            if (!pp.matcher(old.getPatientId()).find()) {
                throw new IncorrectPatientIdException("The patientId: " + old.getPatientId() + " is not valid for regEx: " + patientIdRegEx);
            }
            if (!pp.matcher(newPat.getPatientId()).find()) {
                throw new IncorrectPatientIdException("The patientId: " + newPat.getPatientId() + " is not valid for regEx: " + patientIdRegEx);
            }
        }
        Connection con = null;
        CallableStatement csOld = null;
        CallableStatement csNew = null;
        CallableStatement studiesCs = null;
        CallableStatement moveCs = null;
        ResultSet rs = null;
        ArrayList<StudyMovementInfo> studies = null;
        try {
            con = getDBConnection();
            con.setAutoCommit(false); // Transaction dealt at this level to allow committing only after data have been persisted on disk
            // IDENTIFY OR BRING TO A COMMON STRUCTURE:
            // Old Patient:
            csOld = con.prepareCall("{call getOrCreatePatient(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            if (old.getLastName() != null) {
                csOld.setString(1, old.getLastName());
            } else {
                csOld.setNull(1, Types.VARCHAR);
            }
            if (old.getFirstName() != null) {
                csOld.setString(2, old.getFirstName());
            } else {
                csOld.setNull(2, Types.VARCHAR);
            }
            if (old.getMiddleName() != null) {
                csOld.setString(3, old.getMiddleName());
            } else {
                csOld.setNull(3, Types.VARCHAR);
            }
            if (old.getPrefix() != null) {
                csOld.setString(4, old.getPrefix());
            } else {
                csOld.setNull(4, Types.VARCHAR);
            }
            if (old.getSuffix() != null) {
                csOld.setString(5, old.getSuffix());
            } else {
                csOld.setNull(5, Types.VARCHAR);
            }
            if (old.getPatientId() != null) {
                csOld.setString(6, old.getPatientId());
            } else {
                csOld.setNull(6, Types.VARCHAR);
            }
            if (old.getIdIssuer() != null) {
                csOld.setString(7, old.getIdIssuer());
            } else {
                csOld.setNull(7, Types.VARCHAR);
            }
            if (old.getPatientIdentifierList() != null) {
                csOld.setString(8, old.getPatientIdentifierList());
            } else {
                csOld.setNull(8, Types.VARCHAR);
            }
            if (old.getPatientAccountNumber() != null) {
                csOld.setString(9, old.getPatientAccountNumber());
            } else {
                csOld.setNull(9, Types.VARCHAR);
            }
            if (old.getVisitNumber() != null) {
                csOld.setString(10, old.getVisitNumber());
            } else {
                csOld.setNull(10, Types.VARCHAR);
            }
            csOld.registerOutParameter(11, Types.BIGINT);
            csOld.registerOutParameter(12, Types.BIGINT);
            if (old.getBirthDate() != null) {
                csOld.setDate(13, old.getBirthDate());
            } else {
                csOld.setNull(13, Types.DATE);
            }
            if (old.getBirthTime() != null) {
                csOld.setTime(14, old.getBirthTime());
            } else {
                csOld.setNull(14, Types.TIME);
            }
            if (old.getSex() != null) {
                csOld.setString(15, old.getSex());
            } else {
                csOld.setNull(15, Types.VARCHAR);
            }
            if (old.getRace() != null) {
                csOld.setString(16, old.getRace());
            } else {
                csOld.setNull(16, Types.VARCHAR);
            }
            if (old.getPatientAddress() != null) {
                csOld.setString(17, old.getPatientAddress());
            } else {
                csOld.setNull(17, Types.VARCHAR);
            }
            if (old.getPatientCity() != null) {
                csOld.setString(18, old.getPatientCity());
            } else {
                csOld.setNull(18, Types.VARCHAR);
            }
            if (old.getEthnicGroup() != null) {
                csOld.setString(19, old.getEthnicGroup());
            } else {
                csOld.setNull(19, Types.VARCHAR);
            }
            csOld.execute();
            oldPatPk = csOld.getLong(11);
            oldPatDemosPk = csOld.getLong(12);
            log.info("OLD (Patient,PatientDemographics)=(" + oldPatPk + "," + oldPatDemosPk + ")");
            if (oldPatPk == -1)
                throw new MultiplePatientsIdentifiedException(old.getPatientId());
            // Now I should have the structure concerning the patient to be merged and its main pks
            // New Patient:
            csNew = con.prepareCall("{call getOrCreatePatient(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            if (newPat.getLastName() != null) {
                csNew.setString(1, newPat.getLastName());
            } else {
                csNew.setNull(1, Types.VARCHAR);
            }
            if (newPat.getLastName() != null) {
                csNew.setString(2, newPat.getFirstName());
            } else {
                csNew.setNull(2, Types.VARCHAR);
            }
            csNew.setString(3, newPat.getMiddleName());
            csNew.setString(4, newPat.getPrefix());
            csNew.setString(5, newPat.getSuffix());
            if (newPat.getPatientId() != null) {
                csNew.setString(6, newPat.getPatientId());
            } else {
                csNew.setNull(6, Types.VARCHAR);
            }
            if (newPat.getIdIssuer() != null) {
                csNew.setString(7, newPat.getIdIssuer());
            } else {
                csNew.setNull(7, Types.VARCHAR);
            }
            if (newPat.getPatientIdentifierList() != null) {
                csNew.setString(8, newPat.getPatientIdentifierList());
            } else {
                csNew.setNull(8, Types.VARCHAR);
            }
            if (newPat.getPatientAccountNumber() != null) {
                csNew.setString(9, newPat.getPatientAccountNumber());
            } else {
                csNew.setNull(9, Types.VARCHAR);
            }
            csNew.setString(10, null);
            csNew.registerOutParameter(11, Types.BIGINT);
            csNew.registerOutParameter(12, Types.BIGINT);
            csNew.setDate(13, newPat.getBirthDate());
            csNew.setTime(14, newPat.getBirthTime());
            csNew.setString(15, newPat.getSex());
            csNew.setString(16, newPat.getRace());
            csNew.setString(17, newPat.getPatientAddress());
            csNew.setString(18, newPat.getPatientCity());
            csNew.setString(19, newPat.getEthnicGroup());
            csNew.execute();
            newPatPk = csNew.getLong(11);
            newPatDemosPk = csNew.getLong(12);
            log.info("NEW (Patient,PatientDemographics)=(" + newPatPk + "," + newPatDemosPk + ")");
            if (newPatPk == -1)
                throw new MultiplePatientsIdentifiedException(newPat.getPatientId());
            // Now I should have the structure concerning the patient to merge and its main pks
            // Retrieve the studies to move:
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                studiesCs = con.prepareCall("{call getPatientHl7ContextStudies(?,?,?)}");
                studiesCs.registerOutParameter(3, OracleTypes.CURSOR);
            } else {
                studiesCs = con.prepareCall("{call getPatientHl7ContextStudies(?,?)}");
            }
            studiesCs.setLong(1, oldPatPk);
            studiesCs.setInt(2, (groupToUse == null) ? 0 : groupToUse);
            studiesCs.execute();
            if (isOracle) {
                rs = (ResultSet) studiesCs.getObject(3);
            } else {
                rs = studiesCs.getResultSet();
            }
            if (rs != null) {
                studies = new ArrayList<StudyMovementInfo>();
                while (rs.next()) {
                    StudyMovementInfo smi = new StudyMovementInfo();
                    smi.setStudyUid(rs.getString(1));
                    smi.setPath(rs.getString(2));
                    studies.add(smi);
                }
            }
            if ((studies != null) && (studies.size() > 0)) {
                ans = 1; // some studies need to be moved
                moveCs = con.prepareCall("{call moveStudyToPatient(?,?,?,?,?)}");
                moveCs.setLong(2, oldPatPk);
                moveCs.setLong(3, newPatPk);
                moveCs.setLong(4, oldPatDemosPk);
                moveCs.setLong(5, newPatDemosPk);
                for (StudyMovementInfo study : studies) {
                    // 20111221: Among the queries to perform was also UPDATE Patients SET mergedBy=? WHERE pk=? now removed!!
                    moveCs.setString(1, study.getStudyUid());
                    moveCs.addBatch();
                }
                moveCs.executeBatch();
            }
            con.commit();
            ImageAvailabilityConfig iac = ImageAvailabilityConfig.getInstance();
            if (iac.isEnabled() && ImageAvailabilityConfig.PUBLISHTO_TABLE.equals(iac.getPublicationMethod())) {
                if ((studies != null) && (studies.size() > 0)) {
                    for (StudyMovementInfo study : studies) {
                        try {
                            IheStudyId ids = new IheStudyId(study.getStudyUid(), null, null);
                            iaBean.reconcileWrongStudy(ids, sendingApp.equals(iac.getAppEntityForRis()) ? ImageAvailabilityConfig.RECONCILIATIONSOURCE_RIS : ImageAvailabilityConfig.RECONCILIATIONSOURCE_ADT, iac.getStringForSetting(), iac.getStringForRemoving(), iac.getTargetApp());
                        } catch (Exception ex) {
                            log.error("An error occurred publishing studies", ex);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ans = -1;
            try {
                con.rollback();
            } catch (Exception iex) {
                log.error("Could not rollback", iex);
            }
            log.error("An error occurred merging the patients: ", ex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(moveCs);
            CloseableUtils.close(studiesCs);
            CloseableUtils.close(csNew);
            CloseableUtils.close(csOld);
            CloseableUtils.close(con);
        }
        return ans; // -1: no studies to be moved, 0: an exception occurred; 1: at least one study to move
    } // end mergePatients

    public int moveStudy(Study source, Patient oldPatient, Patient newPatient) throws CannotUpdateException, IncorrectPatientIdException {
        int ret = 0;
        Long newPatPk = null;
        Long oldPatPk = identifyPatient(oldPatient);
        if (oldPatPk == null)
            throw new CannotUpdateException(CannotUpdateException.HL7_COULDNOTIDENTIFYOLDPATIENT);
        oldPatient.setPrimaryKey("" + oldPatPk);
        newPatPk = identifyPatient(newPatient);
        if (newPatPk == null) {
            try {
                newPatPk = storeNewPatient(newPatient);
                log.info("Created patient " + newPatPk);
            } catch (IncorrectPatientIdException e) {
                throw e;
            } catch (Exception ex) {
                log.error(CannotUpdateException.HL7_COULDNOTIDENTIFYNEWPATIENT, ex);
                throw new CannotUpdateException(CannotUpdateException.HL7_COULDNOTIDENTIFYNEWPATIENT);
            }
        }
        // Retrieve the primary key of the visit, also considering what was passed in MRG-5
        Long visitPk = retrieveVisitPerStudy(source.getStudyInstanceUid(), oldPatPk, oldPatient.getVisitNumber());
        if (visitPk == null) {
            log.error("Could not retrieve visit for (Study OldPatient)=(" + source.getStudyInstanceUid() + " " + oldPatPk + ")");
            throw new CannotUpdateException(CannotUpdateException.HL7_NOVISITFORSTUDY);
        }
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getDBConnection();
            con.setAutoCommit(false);
            cs = con.prepareCall("{call moveStudy(?,?,?,?,?,?)}");
            cs.setString(1, source.getStudyInstanceUid());
            cs.setLong(2, oldPatPk);
            cs.setLong(3, newPatPk);
            cs.setString(4, newPatient.getVisitNumber());
            cs.setLong(5, visitPk);
            cs.registerOutParameter(6, Types.INTEGER);
            cs.execute();
            ret = cs.getInt(6);
            con.commit();
            log.info("Moved study " + source.getStudyInstanceUid() + ": OldPat=" + oldPatPk + " NewPat=" + newPatPk + " visit=" + visitPk + ((newPatient.getVisitNumber() == null) ? "" : " newVisit=" + newPatient.getVisitNumber()));
        } catch (Exception ex) {
            if (con != null)
                try {
                    con.rollback();
                } catch (Exception iex) {
                }
            log.error("Error moving study " + source.getStudyInstanceUid(), ex);
            throw new CannotUpdateException(CannotUpdateException.HL7_CANNOTMOVESTUDY);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return ret;
    }

    public int moveVisit(Patient oldPatient, Patient newPatient, String sendingApp) throws CannotUpdateException, IncorrectPatientIdException {
        int ret = 0;
        Long newPatPk = null;
        Long oldPatPk = identifyPatient(oldPatient);
        if (oldPatPk == null)
            throw new CannotUpdateException(CannotUpdateException.HL7_COULDNOTIDENTIFYOLDPATIENT);
        oldPatient.setPrimaryKey("" + oldPatPk);
        newPatPk = identifyPatient(newPatient);
        if (newPatPk == null) {
            try {
                newPatPk = storeNewPatient(newPatient);
                log.info("Created patient " + newPatPk);
            } catch (IncorrectPatientIdException e) {
                throw e;
            } catch (Exception ex) {
                log.error(CannotUpdateException.HL7_COULDNOTIDENTIFYNEWPATIENT, ex);
                throw new CannotUpdateException(CannotUpdateException.HL7_COULDNOTIDENTIFYNEWPATIENT);
            }
        }
        Connection con = null;
        CallableStatement csSelect = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        ArrayList<String> studies = null;
        try {
            con = getDBConnection();
            con.setAutoCommit(false);
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                csSelect = con.prepareCall("{call getStudiesForMoveVisit(?,?,?)}");
                csSelect.registerOutParameter(3, OracleTypes.CURSOR);
            } else {
                csSelect = con.prepareCall("{call getStudiesForMoveVisit(?,?)}");
            }
            csSelect.setLong(1, oldPatPk);
            csSelect.setString(2, oldPatient.getVisitNumber());
            csSelect.execute();
            if (isOracle) {
                rs = (ResultSet) csSelect.getObject(3);
            } else {
                rs = csSelect.getResultSet();
            }
            if (rs != null) {
                studies = new ArrayList<String>();
                while (rs.next()) {
                    studies.add(rs.getString(1));
                }
            }
            cs = con.prepareCall("{call moveVisit(?,?,?,?,?)}");
            cs.setLong(1, oldPatPk);
            cs.setLong(2, newPatPk);
            cs.setString(3, oldPatient.getVisitNumber());
            cs.setString(4, newPatient.getVisitNumber());
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute();
            ret = cs.getInt(5);
            con.commit();
            log.info("Moved visit: OldPat=" + oldPatPk + " NewPat=" + newPatPk + " visit=" + oldPatient.getVisitNumber() + ((newPatient.getVisitNumber() == null) ? "" : " newVisit=" + newPatient.getVisitNumber()));
            ImageAvailabilityConfig iac = ImageAvailabilityConfig.getInstance();
            if (iac.isEnabled() && ImageAvailabilityConfig.PUBLISHTO_TABLE.equals(iac.getPublicationMethod())) {
                if ((studies != null) && (studies.size() > 0)) {
                    for (String study : studies) {
                        try {
                            IheStudyId ids = new IheStudyId(study, null, null);
                            iaBean.reconcileWrongStudy(ids, sendingApp.equals(iac.getAppEntityForRis()) ? ImageAvailabilityConfig.RECONCILIATIONSOURCE_RIS : ImageAvailabilityConfig.RECONCILIATIONSOURCE_ADT, iac.getStringForSetting(), iac.getStringForRemoving(), iac.getTargetApp());
                        } catch (Exception ex) {
                            log.error("An error occurred publishing studies", ex);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (con != null)
                try {
                    con.rollback();
                } catch (Exception iex) {
                }
            log.error("Error moving visit " + oldPatient.getVisitNumber(), ex);
            throw new CannotUpdateException(CannotUpdateException.HL7_CANNOTMOVESTUDY);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(cs);
            CloseableUtils.close(csSelect);
            CloseableUtils.close(con);
        }
        return ret;
    }

    // Private Methods:
    private Long identifyPatient(Patient p) {
        Long ret = null;
        // The next lines are to avoid NullPointerExceptions throughout the code:
        Connection con = null;
        PreparedStatement identPatientPS = null;
        PreparedStatement updateIssuerPS = null;
        ResultSet rs = null;
        try {
            con = getDBConnection();
            identPatientPS = con.prepareStatement(identPatient);
            identPatientPS.setString(1, p.getPatientId());
            identPatientPS.setString(2, p.getIdIssuer());
            identPatientPS.setString(3, "%"/* (((p.getLastName() == null)||(p.getLastName().equals(""))) ? "%" : p.getLastName().toUpperCase()) */); // Davide 20110322 Fix: it was done to accept ORM after the Study with data different from what's in DICOM (test 419)
            identPatientPS.setString(4, ((p.getPatientAccountNumber() == null) ? "%" : p.getPatientAccountNumber()));
            log.debug(identPatientPS.toString());
            rs = identPatientPS.executeQuery();
            if (rs.next()) {
                ret = rs.getLong(6); // Patients.pk
                log.debug("Identified 1st try: " + ret);
            } else {
                identPatientPS.setString(2, OLD_DEFAULT_ISSUER);
                rs = identPatientPS.executeQuery();
                if (rs.next()) { // The patient is present with the old id issuer, so this field must be updated
                    ret = rs.getLong(6); // Patients.pk
                    log.debug("Identified with old idIssuer: " + ret);
                    updateIssuerPS = con.prepareStatement(updateIdIssuer);
                    updateIssuerPS.setString(1, p.getIdIssuer());
                    updateIssuerPS.setLong(2, ret);
                    updateIssuerPS.executeUpdate();
                } else {
                    ret = null;
                    log.debug("PATIENT NOT PRESENT!!!!");
                }
            } // end if ...else
        } catch (Exception ex) {
            log.error("An error occurred: ", ex);
            ret = null;
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(identPatientPS);
            CloseableUtils.close(updateIssuerPS);
            CloseableUtils.close(con);
        } // end try...catch
        return ret;
    } // end identifyPatient()

    private int performTransfer(Patient p) throws CannotUpdateException {
        log.warn("Perform transfer not yet supported!");
        int ans = 0;
        return ans;
    } // end performTransfer

    private long[] retrievePks(Patient p) throws SQLException {
        long[] pks = null;
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        try {
            con = getDBConnection();
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT Patients.pk, PatientDemographics.pk, MAX(WLPatientDataPerVisit.pk) FROM Patients, PatientDemographics, WLPatientDataPerVisit WHERE UPPER(PatientDemographics.patientIdentifierList)=UPPER('"
                    + p.getPatientIdentifierList()
                    + "') AND PatientDemographics.patientFK=Patients.pk AND WLPatientDataPerVisit.patientFK=Patients.pk GROUP BY Patients.pk, PatientDemographics.pk");
            if (rs.next()) {
                pks = new long[3];
                pks[0] = rs.getLong(1);
                pks[1] = rs.getLong(2);
                pks[2] = rs.getLong(3);
            } else {
                pks = null;
            }
        } catch (Exception ex) {
            throw new SQLException(ex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
        return pks;
    } // end retrievePks

    private String[] findPatient(Patient p) throws SQLException {
        String[] args = null;
        // PreparedStatement identHL7PatPS = null;
        // Connection con=null;
        // ResultSet rs=null;
        // try{
        // con=getDBConnection();
        // identHL7PatPS=con.prepareStatement(identHL7Pat);
        // identHL7PatPS.setString(1, p.getPatientIdentifierList());
        // identHL7PatPS.setString(2, p.getPatientAccountNumber());
        // identHL7PatPS.setString(3, p.getLastName());
        // identHL7PatPS.setString(4, p.getFirstName());
        // rs = identHL7PatPS.executeQuery();
        // if (rs.next()) {
        // args = new String[3];
        // args[0] = rs.getString(1);
        // args[1] = rs.getString(2);
        // args[2] = rs.getString(3);
        // args[3] = rs.getString(4); // AssignedPatientLocation
        // args[4] = rs.getString(5); // PatientClass
        // } // end if
        // }catch(Exception ex){
        // throw new SQLException(ex);
        // }finally{
        // try{rs.close();}catch(Exception ex){log.warn("Could not close identHL7PatPS",ex);}
        // try{identHL7PatPS.close();}catch(Exception ex){log.warn("Could not close identHL7PatPS",ex);}
        // try{con.close();}catch(Exception ex){log.warn("Could not close identHL7PatPS Connection",ex);}
        // }
        return args;
    } // end findPatient()

    private int performIn2Out(Patient p, long pk) throws SQLException {
        int ans = 0;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getDBConnection();
            ps = con.prepareStatement(hl7In2Out);
            ps.setString(1, p.getPatientClass());
            ps.setLong(2, pk);
            ans = ps.executeUpdate();
        } catch (Exception ex) {
            throw new SQLException(ex);
        } finally {
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return ans;
    } // end performIn2Out

    private int performOut2In(Patient p, long pk) throws SQLException {
        int ans = 0;
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getDBConnection();
            ps = con.prepareStatement(hl7Out2In);
            ps.setString(1, p.getPatientClass());
            ps.setString(2, p.getAssignedPatientLocation());
            ps.setLong(3, pk);
            ans = ps.executeUpdate();
        } catch (Exception ex) {
            throw new SQLException(ex);
        } finally {
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return ans;
    } // end performOut2In

    private Long storeNewPatient(Patient p) throws SQLException, IncorrectPatientIdException {
        Long ret = null;
        Connection con = null;
        CallableStatement cs = null;
        String patientIdRegEx = PatientIdCheckSettings.getPatientIdRegEx();
        if (patientIdRegEx != null) {
            log.debug("Checking patientId with this regEx: " + patientIdRegEx);
            Pattern pp = Pattern.compile(patientIdRegEx);
            if (!pp.matcher(p.getPatientId()).find()) {
                throw new IncorrectPatientIdException("The patientId: " + p.getPatientId() + " is not valid for regEx: " + patientIdRegEx);
            }
        }
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call addNewPatient(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            cs.setString(1, ((p.getLastName() == null) || ("".equals(p.getLastName())) ? null : p.getLastName().toUpperCase()));
            cs.setString(2, ((p.getFirstName() == null) || ("".equals(p.getFirstName())) ? null : p.getFirstName().toUpperCase()));
            cs.setString(3, ("".equals(p.getMiddleName()) ? null : p.getMiddleName()));
            cs.setString(4, ("".equals(p.getPrefix()) ? null : p.getPrefix()));
            cs.setString(5, ("".equals(p.getSuffix()) ? null : p.getSuffix()));
            cs.setDate(6, p.getBirthDate());
            cs.setTime(7, p.getBirthTime());
            cs.setString(8, ((p.getSex() == null) || ("".equals(p.getSex())) ? null : p.getSex().toUpperCase()));
            cs.setString(9, p.getPatientId().toUpperCase());
            cs.setString(10, p.getIdIssuer().toUpperCase());
            cs.setString(11, p.getEthnicGroup());
            cs.setString(12, p.getPatientComments());
            cs.setString(13, p.getRace());
            cs.setString(14, p.getPatientAddress());
            cs.setString(15, ((p.getPatientAccountNumber() == null) || ("".equals(p.getPatientAccountNumber())) ? null : p.getPatientAccountNumber().toUpperCase()));
            cs.setString(16, ((p.getPatientIdentifierList() == null) || ("".equals(p.getPatientIdentifierList())) ? null : p.getPatientIdentifierList().toUpperCase()));
            cs.setString(17, p.getPatientCity());
            cs.registerOutParameter(18, Types.BIGINT);
            cs.execute();
            ret = cs.getLong(18);
            if (cs.wasNull())
                throw new SQLException("A null patient pk was returned after Patient creation");
            // DTODO: 20110331 Maybe send an AuditMessage about new patient creation
        } catch (SQLException sex) {
            throw sex;
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return ret;
    }

    private Long retrieveVisitPerStudy(String studyUid, long patPk, String visitNumber) {
        Connection con = null;
        CallableStatement cs = null;
        Long ret = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call retrieveVisitPerStudy(?,?,?,?)}");
            cs.setString(1, studyUid);
            cs.setLong(2, patPk);
            cs.setString(3, visitNumber);
            cs.registerOutParameter(4, Types.BIGINT);
            cs.execute();
            ret = cs.getLong(4);
        } catch (SQLException sex) {
            log.error("Error Retrieveing visit for study=" + studyUid + ", patient=" + patPk, sex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return ret;
    }

    private void updateStoredPatient(Patient p, Study s) {
        Connection con = null;
        Statement stat = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getDBConnection();
            stat = con.createStatement();
            if (isUpdateOnORM())
                ps = con.prepareStatement(updateOnORM);
            else
                ps = con.prepareStatement(updateAfterDicom);
            ps.setString(1, p.getPatientId().toUpperCase());
            ps.setString(2, "%"/* (p.getLastName() == null) ? "%" : p.getLastName() */); // Davide 20110322 Fix: it was done to accept ORM after the Study with data different from what's in DICOM (test 419)
            ps.setString(3, p.getPatientIdentifierList());
            if (!isUpdateOnORM())
                ps.setString(4, s.getStudyInstanceUid());
            rs = ps.executeQuery();
            if (rs.next()) { // If I'm here and not in storeNewPatient, then I'm sure I have results!!
                // Now I build the single queries I need to update the tables
                StringBuffer basePat = null; // I'll use Lazy Initialization
                StringBuffer basePatDemo = null;
                StringBuffer baseSt = null;
                if ((p.getPatientId() != null) && (!p.getPatientId().equals(rs.getString(1)))) { // If HL7 sent me something and this something is different from what I have, then change it!
                    if (basePat == null) {
                        basePat = new StringBuffer(256);
                        basePat.append("UPDATE Patients SET ");
                    }
                    basePat.append("patientID='").append(p.getPatientId().toUpperCase().replace("'", "''")).append("', ");
                } // end if
                if ((p.getIdIssuer() != null) && (!p.getIdIssuer().equals(rs.getString(2)))) {
                    if (basePat == null) {
                        basePat = new StringBuffer(256);
                        basePat.append("UPDATE Patients SET ");
                    }
                    basePat.append("idIssuer='").append(p.getIdIssuer().replace("'", "''")).append("', ");
                } // end if
                if (((p.getLastName() != null) && (!p.getLastName().equals(rs.getString(3)))) ||
                        ((p.getLastName() == null) && (rs.getString(3) != null))) {
                    if (basePat == null) {
                        basePat = new StringBuffer(256);
                        basePat.append("UPDATE Patients SET ");
                    }
                    if (p.getLastName() != null)
                        basePat.append("lastName='").append(p.getLastName().toUpperCase().replace("'", "''")).append("', ");
                    else
                        basePat.append("lastName=null , ");
                } // end if
                if (((p.getFirstName() != null) && (!p.getFirstName().equals(rs.getString(4)))) ||
                        ((p.getFirstName() == null) && (rs.getString(4) != null))) {
                    if (basePat == null) {
                        basePat = new StringBuffer(256);
                        basePat.append("UPDATE Patients SET ");
                    }
                    if (p.getFirstName() != null)
                        basePat.append("firstName='").append(p.getFirstName().toUpperCase().replace("'", "''")).append("', ");
                    else
                        basePat.append("firstName=null , ");
                } // end if
                if (((p.getMiddleName() != null) && (!p.getMiddleName().equals(rs.getString(5)))) ||
                        ((p.getMiddleName() == null) && (rs.getString(5) != null))) {
                    if (basePat == null) {
                        basePat = new StringBuffer(256);
                        basePat.append("UPDATE Patients SET ");
                    }
                    if (p.getMiddleName() != null)
                        basePat.append("middleName='").append(p.getMiddleName()).append("', ");
                    else
                        basePat.append("middleName=null , ");
                } // end if
                if (((p.getPrefix() != null) && (!p.getPrefix().equals(rs.getString(6)))) ||
                        ((p.getPrefix() == null) && (rs.getString(6) != null))) {
                    if (basePat == null) {
                        basePat = new StringBuffer(256);
                        basePat.append("UPDATE Patients SET ");
                    }
                    if (p.getPrefix() != null)
                        basePat.append("prefix='").append(p.getPrefix()).append("', ");
                    else
                        basePat.append("prefix=null, ");
                } // end if
                if (((p.getSuffix() != null) && (!p.getSuffix().equals(rs.getString(7)))) ||
                        ((p.getSuffix() == null) && (rs.getString(7) != null))) {
                    if (basePat == null) {
                        basePat = new StringBuffer(256);
                        basePat.append("UPDATE Patients SET ");
                    }
                    if (p.getSuffix() != null)
                        basePat.append("suffix='").append(p.getSuffix()).append("', ");
                    else
                        basePat.append("suffix=null , ");
                } // end if
                if ((p.getSex() != null) && (!p.getSex().equals(rs.getString(10)))) {
                    if (basePat == null) {
                        basePat = new StringBuffer(256);
                        basePat.append("UPDATE Patients SET ");
                    }
                    basePat.append("sex='").append(p.getSex().toUpperCase()).append("', ");
                } // end if
                if ((p.getBirthDate() != null) && !p.getBirthDate().equals(rs.getDate(8)) ||
                        (p.getBirthDate() == null && rs.getDate(8) != null)) {
                    if (basePat == null) {
                        basePat = new StringBuffer(256);
                        basePat.append("UPDATE Patients SET ");
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat(dbDateFormat);
                    if (p.getBirthDate() != null)
                        basePat.append("birthDate='").append(formatter.format(p.getBirthDate())).append("', ");
                    else
                        basePat.append("birthDate=null , ");
                }
                // end if
                if ((p.getPatientIdentifierList() != null) && (!p.getPatientIdentifierList().equals(rs.getString(11)))) {
                    if (basePatDemo == null) {
                        basePatDemo = new StringBuffer(256);
                        basePatDemo.append("UPDATE PatientDemographics SET ");
                    }
                    basePatDemo.append("patientIdentifierList='").append(p.getPatientIdentifierList()).append("', ");
                } // end if
                if ((p.getRace() != null) && (!p.getRace().equals(rs.getString(12)))) {
                    if (basePatDemo == null) {
                        basePatDemo = new StringBuffer(256);
                        basePatDemo.append("UPDATE PatientDemographics SET ");
                    }
                    basePatDemo.append("race='").append(p.getRace()).append("', ");
                } // end if
                if ((p.getPatientAddress() != null) && (!p.getPatientAddress().replaceAll("'", "''").equals(rs.getString(13)))) {
                    if (basePatDemo == null) {
                        basePatDemo = new StringBuffer(256);
                        basePatDemo.append("UPDATE PatientDemographics SET ");
                    }
                    basePatDemo.append("patientAddress='").append(p.getPatientAddress().replaceAll("'", "''")).append("', ");
                } // end if
                if ((p.getPatientCity() != null) && (!p.getPatientCity().replaceAll("'", "''").equals(rs.getString(14)))) {
                    if (basePatDemo == null) {
                        basePatDemo = new StringBuffer(256);
                        basePatDemo.append("UPDATE PatientDemographics SET ");
                    }
                    basePatDemo.append("patientCity='").append(p.getPatientCity().replaceAll("'", "''")).append("', ");
                } // end if
                if ((p.getPatientAccountNumber() != null) && (!p.getPatientAccountNumber().equals(rs.getString(15)))) {
                    if (basePatDemo == null) {
                        basePatDemo = new StringBuffer(256);
                        basePatDemo.append("UPDATE PatientDemographics SET ");
                    }
                    basePatDemo.append("patientAccountNumber='").append(p.getPatientAccountNumber()).append("', ");
                } // end if
                if ((s.getAccessionNumber() != null) && (!s.getAccessionNumber().equals(rs.getString(16)))) {
                    if (baseSt == null) {
                        baseSt = new StringBuffer(128);
                        baseSt.append("UPDATE Studies SET ");
                    }
                    baseSt.append("accessionNumber='").append(s.getAccessionNumber()).append("' WHERE studyInstanceUID='").append(s.getStudyInstanceUid()).append("'");
                } // end if
                  // Now I can update what needed:
                if (basePat != null) {
                    basePat.deleteCharAt(basePat.length() - 2);
                    basePat.append("WHERE pk=").append(rs.getString(17));
                    log.debug(basePat.toString());
                    stat.executeUpdate(basePat.toString());
                }
                if (basePatDemo != null) {
                    basePatDemo.deleteCharAt(basePatDemo.length() - 2);
                    basePatDemo.append("WHERE pk=").append(rs.getString(18));
                    log.debug(basePatDemo.toString());
                    stat.executeUpdate(basePatDemo.toString());
                }
                if (baseSt != null) {
                    stat.executeUpdate(baseSt.toString());
                }
                // Now I should be done!
            }
        } catch (Exception sex) {
            log.error("An error occurred in updateStoredPatient: ", sex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(ps);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
    } // end updateStoredPatient

    private boolean isAlreadyThere(String suid) {
        Connection con = null;
        Statement stat = null;
        ResultSet rs = null;
        boolean present = false;
        try {
            con = getDBConnection();
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT studyInstanceUID FROM Studies WHERE studyInstanceUID='" + suid + "'");
            if (rs.next())
                if (suid.equals(rs.getString(1)))
                    present = true;
        } catch (Exception ex) {
            log.error("An error occurred:", ex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(stat);
            CloseableUtils.close(con);
        }
        return present;
    }

    private long addPatientVisit(Patient p, Long patientFK, String studyFK) throws SQLException {
        long res = 0;
        CallableStatement cs = null;
        Connection con = null;
        try {
            con = getDBConnection();
            cs = con.prepareCall("{call addPatientVisit(?,?,?,?,?,?)}");
            cs.setString(1, studyFK);
            cs.setLong(2, patientFK);
            cs.setString(3, p.getVisitNumber());
            cs.setString(4, p.getPatientClass());
            cs.setString(5, p.getAssignedPatientLocation());
            cs.registerOutParameter(6, Types.BIGINT);
            cs.execute();
            res = cs.getLong(6);
        } catch (Exception ex) {
            throw new SQLException(ex);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return res;
    }

    private int startupStudy(Study s, long patientFK) {
        int res = 0;
        Connection con = null;
        PreparedStatement startStudyPS = null;
        PreparedStatement updatePatientStudiesPS = null;
        try {
            con = getDBConnection();
            long pcsFK = storeCodeSequence(s.getProcedureCodeSequence());
            startStudyPS = con.prepareStatement(startStudy);
            startStudyPS.setString(1, s.getStudyInstanceUid());
            startStudyPS.setString(2, s.getAccessionNumber());
            startStudyPS.setLong(3, patientFK);
            startStudyPS.setLong(4, pcsFK);
            startStudyPS.setString(5, s.getStudyDescription());
            if (s.getStudyDate() != null)
                startStudyPS.setDate(6, s.getStudyDate());
            else {
                startStudyPS.setNull(6, Types.DATE);
            }
            try {
                res = startStudyPS.executeUpdate();
            } catch (SQLException sex) {
                log.error("An error occurred:", sex);
                res = -1; // The study was already present, the booking has been sent twice!!
            } // end try...catch
            if (res != -1) {
                updatePatientStudiesPS = con.prepareStatement(updatePatientStudies);
                updatePatientStudiesPS.setShort(1, (short) res);
                updatePatientStudiesPS.setLong(2, patientFK);
                res = updatePatientStudiesPS.executeUpdate();
            }
        } catch (Exception ex) {
            log.error("An error occurred:", ex);
        } finally {
            CloseableUtils.close(startStudyPS);
            CloseableUtils.close(updatePatientStudiesPS);
            CloseableUtils.close(con);
        }
        return res;
    } // end startupStudy()

    private long storeCodeSequence(CodeSequence cs) {
        int res = 0; // To know how many rows were affected (should be 1, can be 0)
        log.debug("Trying to store a CodeSequence");
        long insId = 0; // The Id of the inserted row.
        if ((cs == null) || ((cs.getCodeValue() == null) && (cs.getCodingSchemeDesignator() == null) && (cs.getCodingSchemeVersion() == null))) { // No code Sequence was provided...
            // log.debug("CodeSequence is null!!!!!!!!!!");
            return insId; // ... Return the default one!!!
        }
        Connection con = null;
        PreparedStatement selectInsertedCodSeqPS = null;
        PreparedStatement insertCodSeqPS = null;
        ResultSet rs = null;
        try { // Check whether the CodSeq is already in the DB, stored by a previous study, for instance...
            con = getDBConnection();
            selectInsertedCodSeqPS = con.prepareStatement(selectInsertedCodSeq);
            selectInsertedCodSeqPS.setString(1, cs.getCodeValue());
            selectInsertedCodSeqPS.setString(2, cs.getCodingSchemeDesignator());
            selectInsertedCodSeqPS.setString(3, cs.getCodingSchemeVersion());
            rs = selectInsertedCodSeqPS.executeQuery();
            if (rs.next()) {
                log.debug("Already Stored: " + rs.getLong(1));
                insId = rs.getLong(1); // The CodeSequence was already present! Otherwise, go on and insert it!
                if (insId != 0)
                    return insId; // if the returned row is the first one, assume the current codeSeq is to be added and go on
            }
            insertCodSeqPS = con.prepareStatement(insertCodSeq);
            insertCodSeqPS.setString(1, cs.getCodeValue());
            insertCodSeqPS.setString(2, cs.getCodingSchemeDesignator());
            insertCodSeqPS.setString(3, cs.getCodingSchemeVersion());
            insertCodSeqPS.setString(4, cs.getCodeMeaning());
            res = insertCodSeqPS.executeUpdate();
            log.debug("Inserted a CodeSequence: ");
            if (res == 1) {
                rs = selectInsertedCodSeqPS.executeQuery(); // The PreparedStatement is ready since the previous call!
                rs.next(); // Exactly one record should be in the ResultSet
                insId = rs.getLong(1); // Get the value of the first and only column
                log.debug("Retrieved CodeSequence " + insId);
            } // end if
        } catch (Exception ex) {
            log.error("An error occurred:", ex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(selectInsertedCodSeqPS);
            CloseableUtils.close(insertCodSeqPS);
            CloseableUtils.close(con);
        }
        return insId; // It returns the id of the inserted row.
    } // end storeCodeSequence()

    private int resetStudiesToReconcile(String studyUid, Long patientPk) {
        int updatedRows = 0;
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getConnection();
            cs = con.prepareCall("{call resetStudiesToReconcile(?,?,?)}");
            if (studyUid == null)
                cs.setNull(1, Types.VARCHAR);
            else
                cs.setString(1, studyUid);
            if (patientPk == null)
                cs.setNull(2, Types.BIGINT);
            else
                cs.setLong(2, patientPk);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            updatedRows = cs.getInt(3);
        } catch (SQLException e) {
            log.error("", e);
        } catch (NamingException e) {
            log.error("", e);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return updatedRows;
    }

    private class StudyMovementInfo {
        private String studyUid;
        private String path;

        public String getStudyUid() {
            return studyUid;
        }

        public void setStudyUid(String studyUid) {
            this.studyUid = studyUid;
        }

        @SuppressWarnings("unused")
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public int insertMoveStudyHistory(MoveStudyHistory moveSH, String structASR) throws CannotUpdateException {
        int ans = 0;
        log.info("HL7Dealer: Updating patient info about Ris/IDM/ACCNUM = " + moveSH.getRis() + "---" + moveSH.getMessageId() + "---" + moveSH.getAccessionnumber() + "---" + structASR);
        try {
            int ris = addMoveStudyHistory(moveSH, structASR);
            if (ris == 0)
                log.info("AccessionNumber " + moveSH.getAccessionnumber() + " stored");
            else
                log.info("AccessionNumber " + moveSH.getAccessionnumber() + " not stored");
        } catch (SQLException sex) {
            log.fatal("HL7Dealer: Error updating a patient: ", sex);
        }
        return ans;
    } // end insertMoveStudyHistory

    private int addMoveStudyHistory(MoveStudyHistory moveSH, String structASR) throws SQLException {
        int res = 0;
        PreparedStatement insertMoveStudyHistoryPS = null;
        PreparedStatement selectStructPS = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            String callingAET = "";
            String knownNode = "";
            String calledAET = "";
            con = getDBConnection();
            selectStructPS = con.prepareStatement(selectKnownNodeStruct);
            selectStructPS.setString(1, structASR);
            rs = selectStructPS.executeQuery();
            if (rs != null && rs.next()) {
                callingAET = rs.getString(1);
                knownNode = rs.getString(2);
                calledAET = rs.getString(3);
                log.debug("Trying to add a MoveStudyHistory!!");
                insertMoveStudyHistoryPS = con.prepareStatement(insertMoveStudyHistory);
                insertMoveStudyHistoryPS.setString(1, calledAET);
                insertMoveStudyHistoryPS.setString(2, callingAET);
                insertMoveStudyHistoryPS.setString(3, callingAET);
                insertMoveStudyHistoryPS.setString(4, moveSH.getAccessionnumber());
                insertMoveStudyHistoryPS.setString(5, structASR);
                insertMoveStudyHistoryPS.setString(6, knownNode);
                insertMoveStudyHistoryPS.setString(7, moveSH.getRis());
                insertMoveStudyHistoryPS.setString(8, moveSH.getMessageId());
                int temp = insertMoveStudyHistoryPS.executeUpdate();
            } else {
                log.error("Struct not present !!");
                res = -1;
            }
        } catch (SQLException sex) { // This occurs if the visit was already present.
            log.error("Error in addMoveStudyHistory: " + sex.getMessage());
        } catch (Exception ex) {
            throw new SQLException(ex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(selectStructPS);
            CloseableUtils.close(insertMoveStudyHistoryPS);
            CloseableUtils.close(con);
        }
        return res;
    } // end addMoveStudyHistory()

    private Boolean isUpdateOnORM() {
        try {
            MBeanServer mbs = null;
            String mbsName = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.MBEANSERVER_NAME);
            for (MBeanServer ser : MBeanServerFactory.findMBeanServer(null)) {
                if (mbsName.equals(ser.getDefaultDomain())) {
                    mbs = ser;
                }
            }
            if (mbs != null) {
                ObjectName serviceParentName = new ObjectName("it.units.htl.dpacs.servers:type=HL7Server,index=2");
                return (Boolean) mbs.getAttribute(serviceParentName, "IsUpdateOnORM");
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("Unable to find UpdateOnORM", e);
            return false;
        }
    }
} // end class
