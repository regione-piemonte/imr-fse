/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.helpers;

import flex.messaging.FlexContext;
import it.units.htl.atna.AuditLogService;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.maps.PatientDemographics;
import it.units.htl.maps.Patients;
import it.units.htl.maps.PatientsHome;
import it.units.htl.maps.Series;
import it.units.htl.maps.Studies;
import it.units.htl.maps.WlpatientDataPerVisit;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.users.UserBean;
import it.units.htl.web.utils.MergeMaker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.AuditEvent.ActionCode;
import org.dcm4che2.audit.message.InstancesAccessedMessage;
import org.dcm4che2.audit.message.PatientRecordMessage;
import org.hibernate.Criteria;
import org.hibernate.StatelessSession;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.SessionFactory;

public class MergeTools {
    static Logger log = Logger.getLogger(MergeTools.class);

    public List<Patients> getPatient(Patients pp) {
        log.info("Searching for " + pp.getLastName() + " " + pp.getFirstName() + " " + pp.getPatientId() + " " + pp.getBirthDate());
        Session s = SessionManager.getInstance().openSession();
        PatientsHome ph = new PatientsHome();
        Patients PToFind = new Patients();
        if (!"".equals(pp.getLastName())) {
            PToFind.setLastName(pp.getLastName().replace("*","%"));
        }
        if (!"".equals(pp.getPatientId())) {
            PToFind.setPatientId(pp.getPatientId());
        }
        if (!"".equals(pp.getFirstName())) {
            PToFind.setFirstName(pp.getFirstName().replace("*","%"));
        }
        if (pp.getBirthDate() != null) {
            PToFind.setBirthDate(pp.getBirthDate());
        }
        List<Patients> ppp = ph.findByExample(PToFind, s);
        return ppp;
    }

    public String getDefaultidIssuer() {
        return GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.DEFAULT_ID_ISSUER);
    }

    public boolean updatePatient(Patients p) {
        try {
            UserBean userBean = (UserBean) FlexContext.getHttpRequest().getSession().getAttribute("userBean");
            AuditLogService atnaService = AuditLogService.getInstance();
            Patients patientToSave = (Patients) p;
            toUpperCase(patientToSave);
            log.debug("Try to save modify of Patient pk = " + patientToSave.getPk());
            StatelessSession s = SessionManager.getInstance().openStatelessSession();
            s.beginTransaction();
            try {
                s.update(patientToSave);
            } catch (Exception e) {
                log.error("Error during modify patient data...", e);
                MessageManager.getInstance().setMessage(e.getMessage());
                return false;
            }
            s.getTransaction().commit();
            try {
                PatientRecordMessage msg = new PatientRecordMessage(ActionCode.UPDATE);
                msg.addPatient(patientToSave.getPatientId(), patientToSave.getFirstName() + "^" + patientToSave.getLastName());
                try {
                    msg.addUserPerson(userBean.getUserName(),
                            userBean.getAccountNo() + "",
                            userBean.getFirstName() + " " + userBean.getLastName(),
                            InetAddress.getLocalHost().toString(), true);
                } catch (UnknownHostException e) {
                    log.warn("Couldn't get local ip", e);
                }
                atnaService.SendMessage(msg);
            } catch (Exception e) {
                log.warn("Unable to send AuditLogMessage", e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public PatientDemographics doTest() {
        return new PatientDemographics();
    }

    public WlpatientDataPerVisit getPat() {
        return new WlpatientDataPerVisit();
    }

    public boolean updateStudy(Studies st) {
        UserBean userBean = (UserBean) FlexContext.getHttpRequest().getSession().getAttribute("userBean");
        AuditLogService atnaService = AuditLogService.getInstance();
        log.debug("Try to save modify of Study pk = " + st.getStudyInstanceUid());
        StatelessSession s = SessionManager.getInstance().openStatelessSession();
        s.beginTransaction();
        try {
            s.update(st);
        } catch (Exception e) {
            log.error("Error during modify patient data...", e);
            MessageManager.getInstance().setMessage(e.getMessage());
            return false;
        }
        s.getTransaction().commit();
        try {
            InstancesAccessedMessage msg = new InstancesAccessedMessage(ActionCode.UPDATE);
            msg.addPatient(st.getPatients().getPatientId(), st.getPatients().getFirstName() + "^" + st.getPatients().getLastName());
            msg.addStudy(st.getStudyInstanceUid(), null);
            try {
                msg.addUserPerson(userBean.getUserName() + ": Modified Study",
                        userBean.getAccountNo() + "",
                        userBean.getFirstName() + " " + userBean.getLastName(),
                        InetAddress.getLocalHost().toString(), true);
            } catch (UnknownHostException e) {
                log.warn("Couldn't get local ip", e);
            }
            atnaService.SendMessage(msg);
        } catch (Exception e) {
            log.warn("Unable to send AuditLogMessage", e);
        }
        return true;
    }

    public boolean putStudyInPatient(Studies st, Patients pt) {
        try {
            log.info(st.getStudyInstanceUid() + " " + pt.getPk());
            MergeMaker maker = new MergeMaker();
            UserBean userBean = (UserBean) FlexContext.getHttpRequest().getSession().getAttribute("userBean");
            maker.putStudyInPatient(st, pt, userBean);
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
        return true;
    }

    public boolean putSeriesInStudies(Series se, Studies st) {
        MergeMaker mm = new MergeMaker();
        UserBean userBean = (UserBean) FlexContext.getHttpRequest().getSession().getAttribute("userBean");
        mm.putSeriesInStudy(se, st, userBean);
        return true;
    }

    public Long addNewPatient(Patients p) throws Exception {
        Long ret = null;
        CallableStatement cs = null;
        Connection con = null;
        try {
            Session s = null;
            try {
                s = SessionManager.getInstance().openSession();
                Criteria criteria = s.createCriteria(Patients.class);
                criteria.add(Restrictions.eq("patientId", p.getPatientId().toUpperCase()));
                criteria.add(Restrictions.eq("idIssuer", p.getIdIssuer().toUpperCase()));
                List<?> patients = (List<?>) criteria.list();
                if (patients.size() > 0) {
                    throw new Exception("The couple patientId/idIssuer already exists!");
                }
            } finally {
                s.close();
            }
            con = getDBConnection();
            cs = con.prepareCall("{call addNewPatient(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            cs.setString(1, ((p.getLastName() == null) || ("".equals(p.getLastName())) ? null : p.getLastName().toUpperCase()));
            cs.setString(2, ((p.getFirstName() == null) || ("".equals(p.getFirstName())) ? null : p.getFirstName().toUpperCase()));
            cs.setString(3, ("".equals(p.getMiddleName()) ? null : p.getMiddleName()));
            cs.setString(4, ("".equals(p.getPrefix()) ? null : p.getPrefix()));
            cs.setString(5, ("".equals(p.getSuffix()) ? null : p.getSuffix()));
            cs.setDate(6, (p.getBirthDate() != null) ? new Date(p.getBirthDate().getTime()) : null);
            cs.setTime(7, null);
            cs.setString(8, ((p.getSex() == null) || ("".equals(p.getSex())) ? null : (p.getSex() + "").toUpperCase()));
            cs.setString(9, p.getPatientId().toUpperCase());
            cs.setString(10, ((p.getIdIssuer() == null) || ("".equals(p.getIdIssuer())) ? getDefaultidIssuer() : p.getIdIssuer().toUpperCase()));
            cs.setString(11, null);
            cs.setString(12, null);
            cs.setString(13, null);
            cs.setString(14, null);
            cs.setString(15, null);
            cs.setString(16, null);
            cs.setString(17, null);
            cs.registerOutParameter(18, Types.BIGINT);
            cs.execute();
            ret = cs.getLong(18);
            if (cs.wasNull())
                throw new SQLException("A null patient pk was returned after Patient creation");
            // Maybe send an AuditMessage about new patient creation
        } catch (Exception e) {
            log.error("", e);
            throw e;
        } finally {
            try {
                cs.close();
            } catch (Exception ex) {
            }
            try {
                con.close();
            } catch (Exception ex) {
            }
        }
        return ret;
    }

    private void toUpperCase(Patients p) {
        if (p.getPatientId() != null)
            p.setPatientId(p.getPatientId().toUpperCase());
        if (p.getIdIssuer() != null)
            p.setIdIssuer(p.getIdIssuer().toUpperCase());
        if (p.getFirstName() != null)
            p.setFirstName(p.getFirstName().toUpperCase());
        if (p.getLastName() != null)
            p.setLastName(p.getLastName().toUpperCase());
        if (p.getMiddleName() != null)
            p.setMiddleName(p.getMiddleName().toUpperCase());
        if (p.getPrefix() != null)
            p.setPrefix(p.getPrefix().toUpperCase());
        if (p.getSuffix() != null)
            p.setSuffix(p.getSuffix().toUpperCase());
    }

    private Connection getDBConnection() throws SQLException, NamingException {
        Context jndiContext = new InitialContext();
        DataSource ds = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
        return ds.getConnection();
    }
}
