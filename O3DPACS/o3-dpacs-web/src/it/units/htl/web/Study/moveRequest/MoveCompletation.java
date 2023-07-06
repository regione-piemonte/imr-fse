/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study.moveRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.units.htl.dpacs.helpers.CloseableUtils;
import oracle.jdbc.driver.OracleTypes;

public class MoveCompletation extends HttpServlet {

	private static final long serialVersionUID = -6698886958651530445L;
	
	private Log log = LogFactory.getLog(MoveCompletation.class);
	
    private static final String PARAMETER_USERNAME = "username";
    private static final String PARAMETER_PASSWORD = "password";
    private static final String PARAMETER_ACCNUM = "accNum";
    private static final String PARAMETER_DEPARTMENT = "struttura";
    private static final String PARAMETER_MESSAGEID = "messageID";
    private static final String PARAMETER_RIS = "ris";
    private static final String PARAMETER_ID_ISSUER = "idIssuer";
    private static final String PARAMETER_PATIENTID = "patientID";
    private DataSource dataSource;
    private static volatile Boolean oracle;
    
	public void init(ServletConfig config) throws ServletException {
		log.info("Init " + getClass().getName() + " servlet...");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.info("doGet...");
		doWork(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.info("doPost...");
		doWork(request, response);
	}

	protected synchronized void doWork(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		String user = request.getParameter(PARAMETER_USERNAME);
        String pass = request.getParameter(PARAMETER_PASSWORD);
        String accNum = request.getParameter(PARAMETER_ACCNUM);
        String struttura = request.getParameter(PARAMETER_DEPARTMENT);
        String messageID = request.getParameter(PARAMETER_MESSAGEID);
        String ris = request.getParameter(PARAMETER_RIS);
        String idIssuer = request.getParameter(PARAMETER_ID_ISSUER);
        String patientID = request.getParameter(PARAMETER_PATIENTID);
        
        boolean isAuth = verify(user, pass);
        if (!isAuth) {
        	log.info("Wrong credentials");
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        	return;
        }
                
        int result = isMoveCompleted(accNum, ris, messageID, struttura, patientID, idIssuer);
        
        boolean resultWritten = false;
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = null;
        try {
        	writer = outputFactory.createXMLStreamWriter(response.getOutputStream());
			writer.writeStartDocument("1.0");
	        writer.writeStartElement("results");
	        writer.writeStartElement("move");
	        writer.writeAttribute("completed", "" + result);
	        writer.writeEndElement();
	        writer.writeEndDocument();
	        resultWritten = true;
        } catch (XMLStreamException e) {
			e.printStackTrace();
			resultWritten = false;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (XMLStreamException e) {
					log.info("Error while generating result");
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
		}
        
        if (!resultWritten) {
        	log.info("Cannot write response");
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
	}
	
	private int isMoveCompleted(String accNum, String ris, String messageID, 
    		String struttura, String patientID, String idIssuer) {
    	log.info("Calling DB function for isMoveCompleted...");
    	
    	int ret = 0;
        Connection connection = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
        	if (dataSource == null) {
        		dataSource = InitialContext.doLookup("java:/jdbc/dbDS");
        	}
        	
            connection = dataSource.getConnection();
            boolean isOracle = isOracle(connection);
            if (isOracle) {
                cs = connection.prepareCall("{? = call ISMOVECOMPLETEDCHECKALL(?,?,?,?,?,?)}");
            } else {
                cs = connection.prepareCall("{? = call ISMOVECOMPLETEDCHECKALL(?,?,?,?,?,?)}");
            }
            cs.registerOutParameter(1, OracleTypes.NUMBER);
            if (isBlank(accNum)) {
            	cs.setNull(2, OracleTypes.VARCHAR);
            } else {
            	cs.setString(2, accNum);
            }
            
            if (isBlank(idIssuer)) {
            	cs.setNull(3, OracleTypes.VARCHAR);
            } else {
            	cs.setString(3, ris);
            }
            
            if (isBlank(messageID)) {
            	cs.setNull(4, OracleTypes.VARCHAR);
            } else {
            	cs.setString(4, messageID);
            }
            
            if (isBlank(struttura)) {
            	cs.setNull(5, OracleTypes.VARCHAR);
            } else {
            	cs.setString(5, struttura);
            }
            
            if (isBlank(patientID)) {
            	cs.setNull(6, OracleTypes.VARCHAR);
            } else {
            	cs.setString(6, patientID);
            }
            
            if (isBlank(idIssuer)) {
            	cs.setNull(7, OracleTypes.VARCHAR);
            } else {
            	cs.setString(7, idIssuer);
            }
            
            log.info("Executing function...");
            if (isOracle) {
                cs.execute();
                BigDecimal result = cs.getBigDecimal(1);
                log.info("Result obtained: " + result);
                ret = result.intValue();
            } else {
                rs = cs.executeQuery();
                BigDecimal result = cs.getBigDecimal(1);
                log.info("Result obtained: " + result);
                ret = result.intValue();
            }
        } catch (Exception ex) {
            log.error("An error occurred retrieving result for isMoveCompleted: ", ex);
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

	private boolean verify(String user, String pass) {
    	log.info("Checking user authentication...");
    	
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	if (dataSource == null) {
        		dataSource = InitialContext.doLookup("java:/jdbc/dbDS");
        	}
            
            log.info("Datasource per auth ottenuto");
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT pk FROM Users WHERE userName=? AND password=? AND pwdExpirationDate IS NULL");
            ps.setString(1, user);
            ps.setString(2, pass);
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception ex) {
            log.error("Error authenticating " + user, ex);
            return false;
        } finally {
        	CloseableUtils.close(rs);
        	CloseableUtils.close(ps);
        	CloseableUtils.close(con);
        }
        
        return false;
    }
	
	/**
	 * Metodo utile alla verifica del tipo di connessione
	 * @param conn connessione
	 * @return true nel caso in cui la connessione fa riferimento ad un database di tipo Oracle, false altrimenti
	 */
	public static boolean isOracle(Connection conn){
		if(oracle==null){
			try {
				oracle=new Boolean(conn.getMetaData().getURL().startsWith("jdbc:oracle:"));
			} catch (Exception ex) {
				oracle=new Boolean(false);
			}
		}
		return oracle.booleanValue();
	}
	
	private boolean isBlank(String s) {
		return s == null || s.isEmpty();
	}
}
