/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import it.units.htl.dpacs.dao.Dbms;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GlobalConfigurationLoader {
    private static Log log = LogFactory.getLog(GlobalConfigurationLoader.class);

    /**
     * @return null if the requested parameter is not enabled, otherwise the correct value
     * */
    public static String getConfigParam(String key) {
        String ret = null;
        Connection connection = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            connection = getDataSource().getConnection();
            boolean isOracle = Dbms.isOracle(connection);
            if (isOracle) {
                cs = connection.prepareCall("{call getGlobalConfiguration(?,?)}");
                cs.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                cs = connection.prepareCall("{call getGlobalConfiguration(?)}");
            }
            cs.setString(1, key);
            if (isOracle) {
                cs.execute();
                rs = (ResultSet) cs.getObject(2);
            } else {
                rs = cs.executeQuery();
            }
            if ((rs != null) && (rs.next()))
                ret = rs.getString(1);
        } catch (Exception ex) {
            log.error("An error occurred retrieving " + key + " parameter: ", ex);
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
            	log.warn("Could not close connection", ex);
            }
        }
        return ret;
    }

    private static DataSource getDataSource() throws NamingException {
        try {
            Context jndiContext = new InitialContext(); 
            return (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
        } catch (NamingException nex) {
            log.fatal(LogMessage._NoDatasource, nex);
            throw nex;
        }
    }
    
}
