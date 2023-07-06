/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.dpacs.services.utils;

import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.helpers.LogMessage;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserAuthenticator {
    private static final Log log = LogFactory.getLog(UserAuthenticator.class);
    private static final String AUTHORIZATION_HEADER_FIELD = "authorizationHeader";
    private static String authorizationHeader = "Authorization";
    private static final String USERNAME = "username";
    private static final String HASHEDPASSWORD = "password";

    static{
    	authorizationHeader = GlobalConfigurationLoader.getConfigParam(AUTHORIZATION_HEADER_FIELD);
    }
    
    public static boolean isUserValid(HttpServletRequest request) throws Exception {
        DataSource dataSource = initDataSource();
        boolean ret = false;
        String username = parseHeader(request, USERNAME);
        String password = parseHeader(request, HASHEDPASSWORD);
        if (username == null || password == null) {
            // if username and/or password are missing, the user is not authenticated
            return ret;
        }
        Connection connection = null;
        CallableStatement cs = null;
        try {
            connection = dataSource.getConnection();
            cs = connection.prepareCall("{call isUserAuthenticated(?,?,?)}");
            cs.setString(1, username);
            cs.setString(2, password);
            cs.registerOutParameter(3, Types.BIT);
            cs.execute();
            ret = cs.getBoolean(3);
        } catch (Exception ex) {
            ret = false;
            log.error("Error retrieving authentication info", ex);
        } finally {
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

    private static DataSource initDataSource() {
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
    }

    private static String parseHeader(HttpServletRequest request, String usrpw) throws Exception {
        String ret = null;
        String header = request.getHeader(authorizationHeader);
        if (header != null) {
            String[] headerSplits = header.split("\\s+");
            if (headerSplits.length != 2) {
                throw new Exception("Authorization header format unexpected");
            }
            // now headerSplits[0] is supposed to be "Basic" and headerSplits[1] is the string to be decoded from Base64
//            byte[] b = StringDecoder.decode(headerSplits[1].getBytes(), "base64");
            byte[] b = Base64.decodeBase64(headerSplits[1].getBytes());
            String value = new String(b);
            // now value is "username:hashedpassword"
            int index = value.lastIndexOf(":");
            if (index == -1) {
                log.error("Cannot retrieve username:hashedpassword from basic code");
                throw new Exception("Cannot retrieve username:hashedpassword from basic code");
            }
            String usr = value.substring(0, index);
            String hpw = value.substring(index + 1);
            // now usr=username and hpw=hashedpassword
            if (USERNAME.equals(usrpw)) {
                ret = usr;
            } else if (HASHEDPASSWORD.equals(usrpw)) {
                ret = hpw;
            }
        }
        return ret;
    }
}
