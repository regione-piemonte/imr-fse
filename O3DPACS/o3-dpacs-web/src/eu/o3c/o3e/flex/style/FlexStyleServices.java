/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.style;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.helpers.LogMessage;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FlexStyleServices extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final String  FLEX_STYLE_PARAM_KEY = "FlexStyle";
	
	private Log log = LogFactory.getLog(FlexStyleServices.class);
	private DataSource dataSource;
	
	public FlexStyleServices() {
        super();
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doWork(request, response);
		return;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//not implemented
	}

	private void doWork(HttpServletRequest request, HttpServletResponse response){
		dataSource = initDataSource();
		String css = getCssFromDb(response);
		try {
			if (css == null) {
				log.error("Css is null after reading from db. Please check if 'FlexStyle' record is inside 'GlobalConfiguration' table.");
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Css is null after reading from db. Please check if 'FlexStyle' record is inside 'GlobalConfiguration' table.");
				return;
			}
			response.getWriter().write(css);
			return;
		} catch (IOException ioe) {
			log.error("Error while sending flex css response.",ioe);
			return;
		} 	
	}

	private String getCssFromDb(HttpServletResponse response) {
		String ret = null;
		Connection connection = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		boolean isOracle = Dbms.isOracle(connection);
		try {
			connection = dataSource.getConnection();
			if (isOracle) {
                cs = connection.prepareCall("{call getGlobalConfiguration(?,?)}");
                cs.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                cs = connection.prepareCall("{call getGlobalConfiguration(?)}");
            }
			cs.setString(1, FLEX_STYLE_PARAM_KEY);
            cs.execute();
            if (isOracle) {
                rs = (ResultSet) cs.getObject(2);
            } else {
                rs = cs.getResultSet();
            }
            if (rs != null && rs.next()) {
            	ret = rs.getString(1);
            }
		} catch (Exception ex) {
            ret = null;
            log.error("Error reading flex css from db.", ex);
            try {response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error reading flex css from db.");} catch(Exception e) {}
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

	private DataSource initDataSource() {
		DataSource ds = null;
		try {
			Context jndiContext = new InitialContext();
			ds = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
		} catch (NamingException nex) {
			log.fatal(LogMessage._NoDatasource, nex);
			try {
				throw nex;
			} catch (Exception e) {
			}
		}
		return ds;
	}
}
