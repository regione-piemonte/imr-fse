/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.LogMessage;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.Types;


public class UserManager {


	
    private static Log log = LogFactory.getLog(UserManager.class);

    private DataSource dataSource;
    

    /**
     * The constructor need the reference to the bean used to get the images.
     * @param inBean
     */
    public UserManager() throws NamingException{
    	try{
            Context jndiContext=new InitialContext();	// Lazy Initialization
            dataSource = (DataSource)jndiContext.lookup("java:/jdbc/dbDS");
        }catch(NamingException nex){
            log.fatal(LogMessage._NoDatasource, nex);
            throw nex;
        }
    }

    public HashMap<String,String> getPasswordConstraints(){
    	Connection con = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		
		HashMap<String,String> result=null;
		
		try{
			con = dataSource.getConnection();			
			boolean isOracle=Dbms.isOracle(con);
            if(isOracle){
            	cs = con.prepareCall("{call getPasswordConstraints(?)}");
            	cs.registerOutParameter(1, OracleTypes.CURSOR);
            	cs.execute();
            	rs=(ResultSet)cs.getObject(1);
            }else{
            	cs = con.prepareCall("{call getPasswordConstraints()}");
            	rs=cs.executeQuery();
            }
            
            if(rs!=null){
            	while(rs.next()){
            		if(result==null)
            			result=new HashMap<String, String>();
            		result.put(rs.getString(1),rs.getString(2));
            	}
            }
		}catch(Exception ex){
			log.error("An error occurred retrieving password constraints",ex);
		}finally{
			CloseableUtils.close(rs);
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		return result;
    }


    public String getConfigParam(String key) {
    	Connection con = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		
    	String ret=null;
		
		try{
			con = dataSource.getConnection();			
			boolean isOracle=Dbms.isOracle(con);
            if(isOracle){
            	cs = con.prepareCall("{call getGlobalConfiguration(?,?)}");
            	cs.registerOutParameter(2, OracleTypes.CURSOR);
            }else{
            	cs = con.prepareCall("{call getGlobalConfiguration(?)}");
            }
            cs.setString(1, key);
            if(isOracle){
            	cs.execute();
            	rs=(ResultSet)cs.getObject(2);
            }else{
            	rs=cs.executeQuery();
            }
            
            if((rs!=null)&&(rs.next()))
            	ret=rs.getString(1);
            
		}catch(Exception ex){
			log.error("An error occurred retrieving "+key+" parameter: ",ex);
		}finally{
			CloseableUtils.close(rs);
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		return ret;
	}
    
    public boolean changePassword(long id, String email, Date expirationDate, String oldPasswordHash, String newPasswordHash){
    	Connection con = null;
		CallableStatement cs = null;
    	
    	boolean ret=false;
		
		try{
			con = dataSource.getConnection();			

            cs = con.prepareCall("{call doChangePassword(?,?,?,?,?,?)}");

            cs.setLong(1, id);
            cs.setString(2, email);
            if(expirationDate!=null){
            	cs.setDate(3, new java.sql.Date(expirationDate.getTime()));
            }else{
            	cs.setNull(3, Types.DATE);
            }
            cs.setString(4, oldPasswordHash);
            cs.setString(5, newPasswordHash);
            cs.registerOutParameter(6, Types.INTEGER);

            cs.execute();

            ret=(cs.getInt(6)>=1);
            
		}catch(Exception ex){
			log.error("An error occurred changing password for User "+id,ex);
			ret=false;
		}finally{
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		return ret;
    }
    
    public boolean setExpirationDate(long id, Date expirationDate){
    	Connection con = null;
		CallableStatement cs = null;

		boolean ret=false;
		
		try{
			con = dataSource.getConnection();			

            cs = con.prepareCall("{call setExpirationDate(?,?,?)}");

            cs.setLong(1, id);
            if(expirationDate!=null){
            	cs.setDate(2, new java.sql.Date(expirationDate.getTime()));
            }else{
            	cs.setNull(2, Types.DATE);
            }
            cs.registerOutParameter(3, Types.INTEGER);

            cs.execute();

            ret=(cs.getInt(3)>=1);
            
		}catch(Exception ex){
			log.error("An error occurred setting validity for User "+id,ex);
			ret=false;
		}finally{
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		return ret;
    }
    
    public long getUserForEmail(String email) {
    	Connection con = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		
    	long ret=0;
		
		try{
			con = dataSource.getConnection();			
			boolean isOracle=Dbms.isOracle(con);
            if(isOracle){
            	cs = con.prepareCall("{call getUserForEmail(?,?)}");
            	cs.registerOutParameter(2, OracleTypes.CURSOR);
            }else{
            	cs = con.prepareCall("{call getUserForEmail(?)}");
            }
            cs.setString(1, email);
            if(isOracle){
            	cs.execute();
            	rs=(ResultSet)cs.getObject(2);
            }else{
            	rs=cs.executeQuery();
            }
            
            if((rs!=null)&&(rs.next()))
            	ret=rs.getLong(1);
            
		}catch(Exception ex){
			log.error("An error occurred retrieving "+email+" parameter: ",ex);
		}finally{
			CloseableUtils.close(rs);
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		return ret;
	}
    
    public boolean updatePassword(long id, String newPassword, Date expirationDate){
    	Connection con = null;
		CallableStatement cs = null;
    	
    	boolean ret = false;
		
		try{
			con = dataSource.getConnection();			

            cs = con.prepareCall("{call updatePassword(?,?,?,?)}");

            cs.setLong(1, id);
            cs.setString(2, newPassword);
            if(expirationDate!=null){
            	cs.setDate(3, new java.sql.Date(expirationDate.getTime()));
            }else{
            	cs.setNull(3, Types.DATE);
            }
            cs.registerOutParameter(4, Types.INTEGER);

            cs.execute();

            ret=(cs.getInt(4)>=1);
            
		}catch(Exception ex){
			log.error("An error occurred updating password for User "+id,ex);
			ret=false;
		}finally{
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		return ret;
    }
    
}