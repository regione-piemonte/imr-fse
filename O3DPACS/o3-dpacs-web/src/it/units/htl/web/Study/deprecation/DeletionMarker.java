/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study.deprecation;

import it.units.htl.dpacs.dao.DeprecationRemote;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet implementation class DeletionMarker
 */
public class DeletionMarker extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String PARAMETER_USERNAME = "username";
    private static final String PARAMETER_PASSWORD = "password";
    private static final String PARAMETER_STUDY = "studyUID";
    private static final String PARAMETER_SERIES = "seriesUID";
    private static final String PARAMETER_ACCNUM = "accNum";
    private static final String PARAMETER_PATIENTID = "patId";
    private static final String PARAMETER_REASON = "reason";
    private static final String SERVLET_ACTIVATION = "DeletionServletEnabled";
    private static final String SERVLET_ACTIVE = "TRUE";
    private Log log = LogFactory.getLog(DeletionMarker.class);
    private DataSource dataSource;

    public DeletionMarker() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doWork(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doWork(request, response);
    }

    protected void doWork(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String user = request.getParameter(PARAMETER_USERNAME);
        String pass = request.getParameter(PARAMETER_PASSWORD);
        String studyUid = request.getParameter(PARAMETER_STUDY);
        String seriesUid = request.getParameter(PARAMETER_SERIES);
        String reason = request.getParameter(PARAMETER_REASON);
        String patientId = request.getParameter(PARAMETER_PATIENTID);
        String accNum = request.getParameter(PARAMETER_ACCNUM);
        long ret = -1;
        if (isServletActive()) {
            long authenticatedUser = authenticate(user, pass);
            if (authenticatedUser > 0) {
                DeprecationRemote bean = null;
                try {
//                    bean = InitialContext.doLookup("o3-dpacs/DeprecationBean/remote");
                    bean = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DeprecationBean!it.units.htl.dpacs.dao.DeprecationRemote");
                    boolean markForDeletion = Boolean.valueOf(GlobalConfigurationLoader.getConfigParam("markForDeletion"));
                    if (seriesUid != null) {
                        String newUid = bean.getNewDeprecationUid();
                        ret = bean.deprecateSeries(seriesUid, newUid, markForDeletion, reason, authenticatedUser);
                    } else {
                        ArrayList<String> studiesToDeprecate = new ArrayList<String>();
                        if (accNum != null && patientId != null) {
                            studiesToDeprecate = getStudyUIDsFromAccNum(accNum, patientId);
                        } else if (studyUid != null) {
                            studiesToDeprecate.add(studyUid);
                        } else {
                            log.error("No filter specified at least AccNum + PatId OR StudyUID");
                        }
                        if (studiesToDeprecate.size() == 0) {
                            log.info("Studies not found, nothing to do.");
                            ret = 0;
                        }
                        for (String singleDepUID : studiesToDeprecate) {
                            String newUid = bean.getNewDeprecationUid();
                            ret = bean.deprecateStudy(singleDepUID, newUid, markForDeletion, reason, authenticatedUser);
                        }
                    }
                } catch (Exception ex) {
                    log.error("An exception occurred when marking a study for deletion", ex);
                }
            } else {
                log.error("User not authenticated: " + user);
            }
        } else {
            log.error("Servlet not enabled: " + user);
        }
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        if (ret == 0)
            out.println(ret);
        else if (ret < 0) {
            out.println(-1);
        } else {
            out.println(1);
        }
        out.close();
    }

    private ArrayList<String> getStudyUIDsFromAccNum(String accNum, String patId) {
        log.info("Searching studies for this accNum " + accNum + " belong to this patient patId " + patId);
        ArrayList<String> res = new ArrayList<String>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (dataSource == null)
                initDataSource();
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT * FROM Studies st INNER JOIN Patients pt ON pt.pk = st.patientFk where st.accessionNumber = ? and pt.patientId = ?");
            ps.setString(1, accNum);
            ps.setString(2, patId);
            rs = ps.executeQuery();
            while (rs.next()) {
                res.add(rs.getString(1));
            }
            log.info("I've found " + res.size() + " studies.");
        } catch (Exception ex) {
            log.error("Error finding studies of accession number: " + accNum, ex);
        } finally {
            try {
                rs.close();
            } catch (SQLException sex) {
            }
            try {
                ps.close();
            } catch (SQLException sex) {
            }
            try {
                con.close();
            } catch (SQLException sex) {
            }
        }
        return res;
    }

    // private ArrayList<String> getStudyUIDsFromPatId(String patId) {
    // ArrayList<String> res = new ArrayList<String>();
    // Connection con = null;
    // PreparedStatement ps = null;
    // ResultSet rs = null;
    // try {
    // if (dataSource == null)
    // initDataSource();
    // con = dataSource.getConnection();
    // ps = con.prepareStatement("SELECT studyInstanceUID from Studies st  INNER JOIN Patients pt ON pt.pk = st.patientFk where pt.patientId = ? and st.deprecated = 0");
    // ps.setString(1, patId);
    // rs = ps.executeQuery();
    // while (rs.next()) {
    // res.add(rs.getString(1));
    // }
    // } catch (Exception ex) {
    // log.error("Error finding studies for patientId : " + patId, ex);
    // } finally {
    // try {
    // rs.close();
    // } catch (SQLException sex) {
    // }
    // try {
    // ps.close();
    // } catch (SQLException sex) {
    // }
    // try {
    // con.close();
    // } catch (SQLException sex) {
    // }
    // }
    // return res;
    // }
    private long authenticate(String user, String pass) {
        long ret = -1;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (dataSource == null)
                initDataSource();
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT pk FROM Users WHERE userName=? AND password=? AND pwdExpirationDate IS NULL");
            ps.setString(1, user);
            ps.setString(2, pass);
            rs = ps.executeQuery();
            if (rs.next()) { // Just one record is returned
                ret = rs.getLong(1);
            }
        } catch (Exception ex) {
            log.error("Error authenticating " + user, ex);
        } finally {
            try {
                rs.close();
            } catch (SQLException sex) {
            }
            try {
                ps.close();
            } catch (SQLException sex) {
            }
            try {
                con.close();
            } catch (SQLException sex) {
            }
        }
        return ret;
    }

    private boolean isServletActive() {
        boolean active = false;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (dataSource == null)
                initDataSource();
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT paramValue FROM GlobalConfiguration WHERE paramKey=? AND enabled=1");
            ps.setString(1, SERVLET_ACTIVATION);
            rs = ps.executeQuery();
            if (rs.next()) { // Just one record is returned
                String temp = rs.getString(1);
                if (temp != null)
                    active = temp.equals(SERVLET_ACTIVE);
            }
        } catch (Exception ex) {
            log.error("Error validating servlet activation ", ex);
        } finally {
            try {
                rs.close();
            } catch (SQLException sex) {
            }
            try {
                ps.close();
            } catch (SQLException sex) {
            }
            try {
                con.close();
            } catch (SQLException sex) {
            }
        }
        return active;
    }

    private void initDataSource() throws NamingException {
        dataSource = InitialContext.doLookup("java:/jdbc/dbDS");
    }
}
