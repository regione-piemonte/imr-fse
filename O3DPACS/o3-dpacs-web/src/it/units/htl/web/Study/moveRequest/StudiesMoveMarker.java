/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study.moveRequest;

import it.units.htl.dpacs.dao.StudyMoveRequestRemote;
import it.units.htl.dpacs.helpers.CloseableUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
public class StudiesMoveMarker extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String PARAMETER_USERNAME = "username";
    private static final String PARAMETER_PASSWORD = "password";
    private static final String PARAMETER_ACCNUM = "accNum";
    private static final String PARAMETER_ENTERPRISE = "azienda";
    private static final String PARAMETER_DEPARTMENT = "struttura";
    private static final String PARAMETER_ACTION = "action";
    private static final String PARAMETER_MESSAGEID = "messageID";
    private static final String PARAMETER_RIS = "ris";
    //taskid:300060 bug:34065
    private static final String PARAMETER_PATIENTID = "patientID";
    //taskid:326054 bug:37966
    private static final String PARAMETER_IDISSUER = "idIssuer";

    private static final String SERVLET_ACTIVATION = "StudyMoveRequestEnabled";
    private static final String SERVLET_ACTIVE = "TRUE";
    private Log log = LogFactory.getLog(StudiesMoveMarker.class);
    private DataSource dataSource;

    public StudiesMoveMarker() {
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
        String action = request.getParameter(PARAMETER_ACTION);
        String accNum = request.getParameter(PARAMETER_ACCNUM);
        String enterprise = request.getParameter(PARAMETER_ENTERPRISE);
        String department = request.getParameter(PARAMETER_DEPARTMENT);
        String messageID = request.getParameter(PARAMETER_MESSAGEID);
        String ris = request.getParameter(PARAMETER_RIS);
        //taskid:300060 bug:34065
        String patientID = request.getParameter(PARAMETER_PATIENTID);
        //taskid:326054 bug:37966
        String idIssuer = request.getParameter(PARAMETER_IDISSUER);

        log.info("*******************************************");
        log.info("MarkForStudyMove in StudiesMoveMarker class");
        log.info("*******************************************");
        log.info("Param:");
        log.info("user = " + user);
        log.info("action = " + action);
        log.info("accNum = " + accNum);
        log.info("enterprise = " + enterprise);
        log.info("department = " + department);
        log.info("messageID = " + messageID);
        log.info("ris = " + ris);
        log.info("patientID = " + patientID);
        log.info("idIssuer = " + idIssuer);
        
        long ret = -1;
        if (isServletActive()) {
            long authenticatedUser = authenticate(user, pass);
            if (authenticatedUser > 0) {
            	StudyMoveRequestRemote bean = null;
                try {
//                    bean = InitialContext.doLookup("o3-dpacs/StudyMoveRequestBean/remote");
                    bean = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/StudyMoveRequestBean!it.units.htl.dpacs.dao.StudyMoveRequestRemote");
                    //taskid:300060 bug:34065 / taskid:326054 bug:37966
                    ret = bean.insertRequestStudyMove(messageID,ris,accNum, enterprise, department, action, authenticatedUser, patientID, idIssuer);
                    

                    log.info("ret = " + ret);
                } catch (Exception ex) {
                    log.error("An exception occurred when marking a study for deletion", ex);
                }
            } else {
                log.error("User not authenticated: " + user);
            }
        } else {
            log.error("Servlet not enabled: " + user);
        }

        log.info("*******************************************");
        
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
        }finally {
        	CloseableUtils.close(rs);
        	CloseableUtils.close(ps);
        	CloseableUtils.close(con);
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
        	CloseableUtils.close(rs);
        	CloseableUtils.close(ps);
        	CloseableUtils.close(con);
        }
        return active;
    }

    private void initDataSource() throws NamingException {
        dataSource = InitialContext.doLookup("java:/jdbc/dbDS");
    }
}
