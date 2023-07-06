/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.dpacs.services;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.LogMessage;
import it.units.htl.wado.dao.PacsRetrieveManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.o3c.o3e.dpacs.services.utils.UserAuthenticator;
import eu.o3c.o3e.dpacs.services.utils.objects.Patient;
import eu.o3c.o3e.dpacs.services.utils.objects.Series;
import eu.o3c.o3e.dpacs.services.utils.objects.Study;

/**
 * Servlet implementation class PatientHistoryServices
 */
public class PatientHistoryServices extends HttpServlet {
    private static final long serialVersionUID = 1L;
	private static final String OFFLINE_STUDY = "p";
    private final String WADOURL = "WadoUrl";
    private PacsRetrieveManager prm = null;
    private static final String RELOADMESSAGE_SUCCESS = "Successfully reloaded configuration";
    private Log log = LogFactory.getLog(PatientHistoryServices.class);
    private DataSource dataSource;
    private String wadoUrl;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public PatientHistoryServices() {
        super();
    }

    public void init() throws ServletException {
        try {
            prm = new PacsRetrieveManager();
            
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doWork(request, response);
        return;
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doWork(request, response);
        return;
    }

    private void doWork(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!request.getParameterNames().hasMoreElements()) {
            response.getWriter().write("");
            response.getWriter().close();
            return;
        }
        String requestType = request.getParameter("requestType");
        if ("Reload".equals(requestType)) {
            processReload(request, response);
            return;
        } 
        
        // first of all get the filters
        String patientID = prm.isPatientIdEnabled()?request.getParameter("patientID"):null;
        String studyUID = prm.isStudyUidEnabled()?request.getParameter("studyUID"):null;
        String accessionNumber = prm.isAccNumEnabled()?request.getParameter("accessionNumber"):null;
        String idIssuer = prm.isPatientIdEnabled()?request.getParameter("idIssuer"):null;
        boolean showOnlyThis = prm.isShowOnlyThisEnabled()?Boolean.valueOf(request.getParameter("showOnlyThis")):true;
        try {
            // check if the user is valid
            if (UserAuthenticator.isUserValid(request)) {
                Patient patientBasicInfo = new Patient();
                PacsRetrieveManager prm;
                long queryIdentifier = System.currentTimeMillis();
                log.debug(queryIdentifier  + "|"+ patientID+"|"+studyUID+"|"+accessionNumber+"|"+idIssuer); 
                try {
                    prm = new PacsRetrieveManager();
                    wadoUrl = prm.getConfigParam(WADOURL);
                    if(idIssuer == null && patientID!=null) idIssuer = prm.getConfigParam(ConfigurationSettings.DEFAULT_ID_ISSUER);
                } catch (NamingException nex) {
                    log.error("Error initializing wadourl");
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error initializing wadourl");
                    return;
                }
                // check the filters, if at least one is available
               patientBasicInfo = getPatientInfo(patientID, idIssuer, accessionNumber, studyUID);
                if (patientBasicInfo == null) {
                    log.error("Error retreiving patient basic information. Patient might not exist.");
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No results found for " + request.getQueryString());
                    return;
                }
                // setting wadoUrl
                
                // retrieve all the study of the patient
                ArrayList<Study> studies = new ArrayList<Study>();
                
                if (showOnlyThis && ((accessionNumber != null)||(studyUID != null))) {
                    // I can show only one study if I've the accNum or the studyUId (and the parameter is true)
                    if (accessionNumber != null) {
                        studies = getStudy(patientBasicInfo.getPk(), accessionNumber);
                    } else {
                        studies = getStudy(patientBasicInfo.getPk(),studyUID);
                    }
                } else {
                    studies =getStudy(patientBasicInfo.getPk(),null);
                }
                for (Study s : studies) {
                    // for each study retrieve the information about the series
                	if(OFFLINE_STUDY.equals(s.getStuydStatus()))
                		continue;	// go to the next study if this one is offline 
                    ArrayList<Series> se = getSeries(s.getUid());
                    s.setSeries(se);
                }
                try {
                    // write the collected data
                    writeResponse(patientBasicInfo, studies, response);
                } catch (Exception ex) {
                    log.error("Unable to create/send the XML response", ex);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                    return;
                }
                log.debug(queryIdentifier  + " end in " + (System.currentTimeMillis()-queryIdentifier) + "ms");
            } else {
                // if authentication fails, the servlet responds 401 (UNAUTHORIZED)
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Client unauthorized");
                return;
            }
        } catch (Exception e) {
            log.fatal("While performing the request.", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }
    }

    private void writeResponse(Patient patientBasicInfo, ArrayList<Study> studies, HttpServletResponse response) throws Exception {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(response.getOutputStream());
        writer.writeStartDocument("1.0");
        writer.writeStartElement("results");
        writer.writeStartElement("patient");
        writer.writeAttribute("patientID", (patientBasicInfo.getPatientId() == null) ? "" : patientBasicInfo.getPatientId());
        writer.writeAttribute("lastName", (patientBasicInfo.getLastName() == null) ? "" : patientBasicInfo.getLastName());
        writer.writeAttribute("middleName", (patientBasicInfo.getMiddleName() == null) ? "" : patientBasicInfo.getMiddleName());
        writer.writeAttribute("firstName", (patientBasicInfo.getFirstName() == null) ? "" : patientBasicInfo.getFirstName());
        writer.writeAttribute("birthDate", (patientBasicInfo.getBirthDate() != null) ? patientBasicInfo.getBirthDate().toString() : "");
        for (Study s : studies) {
            if (!s.getSeries().isEmpty()||(OFFLINE_STUDY.equals(s.getStuydStatus()))) {
                writer.writeStartElement("study");
                writer.writeAttribute("uid", (s.getUid() == null) ? "" : s.getUid());
                writer.writeAttribute("date", (s.getDate() != null) ? s.getDate().toString() : "");
                String studyTime = "";
                if (s.getTime() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("k:mm:ss");
                    try {
                        studyTime = sdf.format(s.getTime());
                    } catch (RuntimeException rex) {
                        log.warn("Error parsing study time.", rex);
                        studyTime = "";
                    }
                }
                writer.writeAttribute("time", (studyTime == null) ? "" : studyTime);
                writer.writeAttribute("description", (s.getDescription() == null) ? "" : s.getDescription());
                writer.writeAttribute("accessionNumber", (s.getAccessionNumber() == null) ? "" : s.getAccessionNumber());
                writer.writeAttribute("studyStatus", s.getStuydStatus());
                if(s.getSeries()!=null){
	                for (Series se : s.getSeries()) {
	                    writer.writeStartElement("series");
	                    writer.writeAttribute("uid", (se.getSeriesInstanceUid() == null) ? "" : se.getSeriesInstanceUid());
	                    writer.writeAttribute("modality", (se.getModality() == null) ? "" : se.getModality());
	                    writer.writeAttribute("thumbnail", (se.getThumbnail() == null) ? "" : se.getThumbnail());
	                    writer.writeAttribute("numberOfInstances", (se.getNumberOfInstances() == null) ? "" : se.getNumberOfInstances());
	                    writer.writeAttribute("description", (se.getDescription() == null) ? "" : se.getDescription());
	                    writer.writeEndElement();
	                }
                }
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
    }

    private ArrayList<Series> getSeries(String study) {
        ArrayList<Series> ret = new ArrayList<Series>();
        Connection connection = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            dataSource = getDataSource();
            connection = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(connection);
            if (isOracle) {
                cs = connection.prepareCall("{call getAllSeriesFromStudy(?,?)}");
                cs.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                cs = connection.prepareCall("{call getAllSeriesFromStudy(?)}");
            }
            cs.setString(1, study);
            cs.execute();
            if (isOracle) {
                rs = (ResultSet) cs.getObject(2);
            } else {
                rs = cs.getResultSet();
            }
            if (rs != null) {
                while (rs.next()) {
                    Series series = new Series();
                    series.setSeriesInstanceUid(rs.getString(1));
                    series.setModality(rs.getString(2));
                    series.setNumberOfInstances(rs.getString(3));
                    series.setDescription(rs.getString(4));
                    series.setThumbnail(wadoUrl + "?requestType=WADO&amp;studyUID=" + study + "&amp;seriesUID=" + series.getSeriesInstanceUid() + "&amp;objectUID=" + rs.getString(5) + "&amp;contentType=image%2Fjpeg");
                    ret.add(series);
                }
            }
        } catch (Exception ex) {
            ret = null;
            log.error("Error retrieving series basic information.", ex);
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
            try {
                cs.close();
            } catch (Exception ex) {
            }
            try {
                connection.close();
            } catch (Exception ex) {
            }
        }
        return ret;
    }

    private ArrayList<Study> getStudy(long patientFk, String filter) {
        ArrayList<Study> ret = new ArrayList<Study>();
        Connection connection = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            dataSource = getDataSource();
            connection = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(connection);
            if (isOracle) {
                cs = connection.prepareCall("{call getStudy(?,?,?)}");
                cs.registerOutParameter(3, OracleTypes.CURSOR);
            } else {
                cs = connection.prepareCall("{call getStudy(?,?)}");
            }
            cs.setLong(1, patientFk);
            cs.setString(2, filter);
            cs.execute();
            if (isOracle) {
                rs = (ResultSet) cs.getObject(3);
            } else {
                rs = cs.getResultSet();
            }
            if (rs != null) {
                while (rs.next()) {
                    Study studies = new Study();
                    studies.setDate(rs.getDate(1));
                    studies.setTime(rs.getTimestamp(2));
                    studies.setDescription(rs.getString(3));
                    studies.setAccessionNumber((rs.getString(4)));
                    studies.setUid(rs.getString(5));
                    studies.setStuydStatus(rs.getString(6));
                    ret.add(studies);
                }
            }
        } catch (Exception ex) {
            ret = null;
            log.error("Error retrieving study basic information.", ex);
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
            try {
                cs.close();
            } catch (Exception ex) {
            }
            try {
                connection.close();
            } catch (Exception ex) {
            }
        }
        return ret;
    }

    private Patient getPatientInfo(String patientId, String idissuer, String accessionNumber, String studyUid) {
        Patient result = new Patient();
        Connection connection = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            dataSource = getDataSource();
            connection = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(connection);
            if (isOracle) {
                cs = connection.prepareCall("{call getPatientInfo(?,?,?,?,?)} ");
                cs.registerOutParameter(5, OracleTypes.CURSOR);
            } else {
                cs = connection.prepareCall("{call getPatientInfo(?,?,?,?)}");
            }
            
            cs.setString(1, patientId);
            cs.setString(2, idissuer);
            cs.setString(3, accessionNumber);
            cs.setString(4, studyUid);
            cs.execute();
            if (isOracle) {
                rs = (ResultSet) cs.getObject(5);
            } else {
                rs = cs.getResultSet();
            }
            if (rs != null && rs.next()) {
                result.setPk(rs.getLong(1));
                result.setPatientId(rs.getString(7));
                result.setLastName(rs.getString(2));
                result.setMiddleName(rs.getString(4));
                result.setFirstName(rs.getString(3));
                result.setBirthDate(rs.getDate(9));
            } else {
                log.error("No patient basic info found!");
                return null;
            }
        } catch (Exception ex) {
            result = null;
            log.error("Error retrieving patient basic info", ex);
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
            try {
                cs.close();
            } catch (Exception ex) {
            }
            try {
                connection.close();
            } catch (Exception ex) {
            }
        }
        return result;
    }

    private DataSource getDataSource() {
        if (dataSource == null) {
            DataSource ds = null;
            try {
                Context jndiContext = new InitialContext();
                ds = (DataSource) jndiContext.lookup("java:/jdbc/wadoDS");
            } catch (NamingException nex) {
                log.fatal(LogMessage._NoDatasource, nex);
                try {
                    throw nex;
                } catch (Exception e) {
                }
            }
            return ds;
        } else {
            return dataSource;
        }
    }
    
    protected void processReload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        prm.reloadSettings();
        log.info(RELOADMESSAGE_SUCCESS);
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException ioex) {
            log.error("Error fetching output stream", ioex);
            throw ioex;
        }
        out.println(RELOADMESSAGE_SUCCESS);
    }
}
