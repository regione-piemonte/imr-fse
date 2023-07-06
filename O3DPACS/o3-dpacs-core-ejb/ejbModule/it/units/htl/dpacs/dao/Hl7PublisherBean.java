/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.helpers.StudyAvailabilityPojo;
import it.units.htl.dpacs.servers.hl7.comunication.utils.TerserLocations;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v25.group.ORM_O01_ORDER;
import ca.uhn.hl7v2.model.v25.message.ORM_O01;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;

@Stateless
public class Hl7PublisherBean implements Hl7PublisherLocal, Hl7PublisherRemote {
    private Log log = LogFactory.getLog(Hl7PublisherBean.class);
    private @Resource(name = "java:/jdbc/Hl7CommServerDS")
    DataSource dataSource;
    

    public static enum MessageType {
        O01_ForComplete(1),
        A08(2),
        O01_ForUpdate(3),
        O01_ForSched(6),
        O01_ForCanc(5),
        A40(4),
        O01_NWN(7),
        O01_NWU(8);
        int _pk = 0;

        MessageType(int pk) {
            _pk = pk;
        }

        public int getPk() {
            return _pk;
        }
    };

    @Override
    public ArrayList<StudyAvailabilityPojo> getCompletedStudies(int elapsedMins) {
        log.info("Get all studies older then " + elapsedMins + " minutes.");
        ArrayList<StudyAvailabilityPojo> res = new ArrayList<StudyAvailabilityPojo>();
        Connection con = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                cs = con.prepareCall("{call getCompletedStudies(?,?)}");
                cs.registerOutParameter(1, OracleTypes.CURSOR);
            } else {
                cs = con.prepareCall("{call getCompletedStudies(?)}");
            }
            cs.setInt(1, elapsedMins);
            cs.execute();
            try {
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(1);
                } else {
                    rs = cs.getResultSet();
                }
            } catch (SQLException sex) {
            }
            if (rs != null) {
                while (rs.next()) {
                    StudyAvailabilityPojo sap = new StudyAvailabilityPojo();
                    sap.studyUID = rs.getString(1);
                    sap.completed = rs.getBoolean(2);
                    sap.published = rs.getBoolean(3);
                    sap.toReconcile = rs.getBoolean(4);
                    res.add(sap);
                }
            }
        } catch (SQLException e) {
            log.error("Unable to get connection", e);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return res;
    }

    @Override
    public boolean insertHl7Notification(String sms, StudyAvailabilityPojo study) {
        boolean done = false;
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = dataSource.getConnection();
            cs = con.prepareCall("{call insertStudyAvlNotification(?,?,?)}");
            cs.setString(1, sms);
            // WN : new study in worklist
            // WU : update in worklist study
            // NWN: new study out of the worklist
            // NWU: update a study out of worklist
            if (!study.toReconcile) {
                if (!study.published) {
                    cs.setString(2, "WN");
                } else {
                    cs.setString(2, "WU");
                }
            } else {
                if (!study.published) {
                    cs.setString(2, "NWN");
                } else {
                    cs.setString(2, "NWU");
                }
            }
            cs.setString(3, study.studyUID);
            cs.execute();
            done = true;
        } catch (SQLException e) {
            log.error("Unable to get connection", e);
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return done;
    }

    @Override
    public String generateO01Notify(StudyAvailabilityPojo study) {
        String pidIdentifierType = getPidIdentifierType();
        String retSms = null;
        HapiContext context = new DefaultHapiContext();
        Parser p = context.getGenericParser();
        ORM_O01 sms = new ORM_O01();
        Connection con = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            sms.initQuickstart("ORM", "O01", "P");
            ORM_O01_ORDER order = sms.getORDER();
            order.getMessage().addNonstandardSegment("ZDS");
            Terser terser = new Terser(sms);
            con = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                cs = con.prepareCall("{call getStudyInformation(?,?)}");
                cs.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                cs = con.prepareCall("{call getStudyInformation(?)}");
            }
            cs.setString(1, study.studyUID);
            cs.execute();
            try {
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(2);
                } else {
                    rs = cs.getResultSet();
                }
            } catch (SQLException sex) {
            }
            if (rs != null) {
                rs.next();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                terser.set(TerserLocations.PID.lastName, rs.getString(1));
                terser.set(TerserLocations.PID.firstName, rs.getString(2));
                terser.set(TerserLocations.PID.patientID, rs.getString(3));
                terser.set(TerserLocations.PID.idIssuer, rs.getString(4));
                terser.set(TerserLocations.PID.identificationType, pidIdentifierType);
                terser.set(TerserLocations.PID.patientBirthDate, (rs.getDate(5) != null) ? formatter.format(rs.getDate(5)) : null);
                terser.set(TerserLocations.PID.patientSex, rs.getString(6));
                terser.set(TerserLocations.ORC.placeOrderNumber, rs.getString(7));
                terser.set(TerserLocations.ORC.orderControl, "XO");
                if (!study.published) {
                    terser.set(TerserLocations.ORC.orderStatus, "CM");
                } else {
                    terser.set(TerserLocations.ORC.orderStatus, "SC");
                }
                terser.set(TerserLocations.OBR.placeOrderNumber, rs.getString(7));
                terser.set(TerserLocations.OBR.studyDescription, rs.getString(12));
                terser.set(TerserLocations.OBR.studyType, rs.getString(13));
                terser.set(TerserLocations.ZDS.studyUID, study.studyUID);
                terser.set(TerserLocations.ZDS.numberOfStudyRelInst, rs.getString(10));
                terser.set(TerserLocations.ZDS.studyDate, (rs.getDate(8) != null) ? formatter.format(rs.getDate(8)) : null);
                terser.set(TerserLocations.ZDS.application, "Application");
                terser.set(TerserLocations.ZDS.type, "DICOM");
                terser.set(TerserLocations.PV1.visitNumber, rs.getString(11));
            } else {
                log.error("No information about " + study.studyUID);
            }
            log.debug(p.encode(sms));
            retSms = p.encode(sms);
        } catch (HL7Exception e) {
            log.error("", e);
        } catch (IOException e) {
            log.error("", e);
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return retSms;
    }

    private String  getPidIdentifierType() {
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
                return (String) mbs.getAttribute(serviceParentName, "PidIdentifierType");
            }else{
                return null;
            }
        } catch (Exception e) {
            log.error("Unable to find pidIdentifierType", e);
            return null;
        }
    }

    @Override
    public void insertHl7MessageInQueue(String message, int typePk) {
        Connection con = null;
        PreparedStatement st = null;
        try {
            con = dataSource.getConnection();
            st = con.prepareStatement("INSERT INTO Hl7Messages (message, typeFk) VALUES (?,?)");
            st.setString(1, message);
            st.setInt(2, typePk);
            st.execute();
        } catch (Exception ex) {
            log.error("", ex);
            log.error("Unable to add this message\n" + message);
        } finally {
            CloseableUtils.close(st);
            CloseableUtils.close(con);
        }
    }

    @Override
    public boolean updateStudyAvailability(StudyAvailabilityPojo studyUID, long completedTime) {
        Connection con = null;
        PreparedStatement st = null;
        boolean ret = false;
        try {
            con = dataSource.getConnection();
            st = con.prepareStatement("UPDATE StudyAvailability  SET completed=1, completedOnSeconds = ?, published = 1  WHERE studyInstanceUid = ?");
            st.setLong(1, completedTime);
            st.setString(2, studyUID.studyUID);
            st.execute();
            ret = true;
        } catch (Exception ex) {
            log.error("", ex);
            log.error("Unable to update this study : " + studyUID);
        } finally {
            CloseableUtils.close(st);
            CloseableUtils.close(con);
        }
        return ret;
    }
}
