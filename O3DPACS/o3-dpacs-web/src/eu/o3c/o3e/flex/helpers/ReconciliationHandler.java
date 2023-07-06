/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.helpers;

import flex.messaging.FlexContext;
import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.ImageAvailabilityConfig;
import it.units.htl.maps.Patients;
import it.units.htl.maps.PatientsHome;
import it.units.htl.maps.Series;
import it.units.htl.maps.Studies;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.users.UserBean;
import it.units.htl.web.utils.MergeMaker;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Session;

@SuppressWarnings("unchecked")
public class ReconciliationHandler {
    private static Logger log = Logger.getLogger(ReconciliationHandler.class);
    private @Resource(name = "java:/jdbc/hl7DS")
    DataSource dataSource;

    public List<Patients> getStudies(Patients pp) {
        log.info("Searching for " + pp.getLastName() + " " + pp.getFirstName() + " " + pp.getPatientId() + " " + pp.getBirthDate());
        Session s = SessionManager.getInstance().openSession();
        s.setCacheMode(CacheMode.IGNORE);
        PatientsHome ph = new PatientsHome();
        if (pp.getLastName() != null && !"".equals(pp.getLastName())) {
            pp.setLastName(pp.getLastName().replace("*", "%"));
        } else {
            pp.setLastName(null);
        }
        if (pp.getPatientId() != null && !"".equals(pp.getPatientId())) {
            pp.setPatientId(pp.getPatientId());
        } else {
            pp.setPatientId(null);
        }
        if (pp.getFirstName() != null && !"".equals(pp.getFirstName())) {
            pp.setFirstName(pp.getFirstName().replace("*", "%"));
        } else {
            pp.setFirstName(null);
        }
        if (pp.getBirthDate() != null) {
            pp.setBirthDate(pp.getBirthDate());
        }
        List<Patients> ppp = ph.find(pp, s);
        return ppp;
    }

    public Boolean swapStudies(Studies source, Studies dest, ArrayList<String> uidToExclude, Boolean toReconcile) throws Exception {
        Session s = SessionManager.getInstance().openSession();
        s.refresh(source);
        s.refresh(dest);
        s.close();
        Boolean res = false;
        source.setReconcile(toReconcile);
        dest.setReconcile(toReconcile);
        try {
            UserBean userBean = (UserBean) FlexContext.getHttpRequest().getSession().getAttribute("userBean");
            if (userBean == null)
                userBean = new UserBean();
            Series[] seriesToMove = (Series[]) source.getSeries().toArray(new Series[source.getSeries().size()]);
            for (int i = 0; i < seriesToMove.length; i++) {
                if (uidToExclude != null && uidToExclude.contains(seriesToMove[i].getSeriesInstanceUid())) {
                    log.info("Skip  " + seriesToMove[i].getSeriesInstanceUid() + " was moved previously.");
                } else {
                    MergeMaker mm = new MergeMaker();
                    res = mm.putSeriesInStudy(seriesToMove[i], dest, userBean);
                }
            }
        } catch (Exception e) {
            log.info("Exception during reconciliation", e);
            throw e;
        }
        return res;
    }

    
//    move series from a RAD-4 to Another
    public Boolean moveSeries(Studies source, Studies dest) throws Exception {
        Boolean res = false;
        Connection con = null;
        CallableStatement st = null;
        try {
            UserBean userBean = (UserBean) FlexContext.getHttpRequest().getSession().getAttribute("userBean");
            if (userBean == null)
                userBean = new UserBean();
            boolean isOnlyScheduled = false;
            if (dest.getFastestAccess() == null) {
                dest.setFastestAccess(source.getFastestAccess());
                isOnlyScheduled = true;
            }
            if(dest.getStudyDate() == null){
                dest.setStudyDate(source.getStudyDate());
                dest.setStudyTime(source.getStudyTime());
            }
            Series[] seriesToMove = (Series[]) source.getSeries().toArray(new Series[source.getSeries().size()]);
            for (int i = 0; i < seriesToMove.length; i++) {
                MergeMaker mm = new MergeMaker();
                res = mm.putSeriesInStudy(seriesToMove[i], dest, userBean);
            }
            if(isOnlyScheduled){
            	ImageAvailabilityConfig iac = ImageAvailabilityConfig.getInstance();
                con = getConnection();
                st = con.prepareCall("{call updateAfterReco(?,?,?,?,?)}");
                st.setString(1, source.getStudyInstanceUid());
                st.setString(2, dest.getStudyInstanceUid());
                st.setString(3, ImageAvailabilityConfig.RECONCILIATIONSOURCE_WORKLIST);
                st.setString(4, iac.getStringForSetting());
                st.setString(5, iac.getTargetApp());
                st.execute();           
            }
        } catch (Exception e) {
            log.info("Exception during reconciliation", e);
            throw e;
        }finally{
            CloseableUtils.close(st);
            CloseableUtils.close(con);
        }
        return res;
    }
    
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
}
