/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.users;

import java.sql.*;

import javax.naming.Context;

import javax.naming.InitialContext;

import javax.sql.DataSource;

/**
 *
 * @author francesco.feront
 */

public class Connector {
    
    private static Connector singletonInstance = null;
    
    private Context jndiCon=null;
    
    private DataSource dataSource=null;
    
    /** Creates a new instance of Connector */
    public Connector() {
    }
      public static Connector getInstance() {
        
        if(singletonInstance==null) singletonInstance = new Connector();
        return singletonInstance;
    }

    public Connection getConnection(){
        Connection connection=null;
        try{
            if(jndiCon==null)jndiCon=new InitialContext();	// Lazy Initialization
            if(dataSource==null)dataSource = (DataSource)jndiCon.lookup("java:/jdbc/dbDS");//("java:mppsDS");		// DTODO: to jdbc/stgCmtDS
            connection = dataSource.getConnection();
            //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");//sun.jdbc.odbc.JdbcOdbcDriver");//
            //connection = DriverManager.getConnection("jdbc:odbc:db1");//,"postgres","o3dpacs");//jdbc:odbc:db1","","");//
        }catch(Exception e){
            
        }
        return connection;
    }
}
