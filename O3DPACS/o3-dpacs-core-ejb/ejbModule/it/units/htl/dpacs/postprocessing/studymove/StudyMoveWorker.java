/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.studymove;

import it.units.htl.dpacs.dao.Dbms;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TimerTask;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class StudyMoveWorker extends TimerTask {

	protected int minimumLife;
	protected Integer timeout;
	protected Log log = LogFactory.getLog(StudyMoveWorker.class);
	protected DataSource dataSource;
	
	public StudyMoveWorker(int minimumLife, int timeout){
		this.minimumLife = minimumLife;
		this.timeout = new Integer(timeout);
		setDatasource();	
	}
	
	private void setDatasource() {
        try {
        	dataSource = (DataSource) InitialContext.doLookup("java:/jdbc/dbDS");
            log.debug("Connected to the DB");
        } catch (NamingException nex) {
            log.error("Unable to retrieve DataSource to DB: ", nex);
        }
    }
	
	protected String[] getAccessionNumberToMark(){
		String[] ret=null;
		Connection con = null;
		CallableStatement cs = null;
		ResultSet rs=null;
		
		try{
			con = dataSource.getConnection();
			boolean isOracle = Dbms.isOracle(con);
			
			if(isOracle){
				cs = con.prepareCall("{call getAccessionNumberToMove(?,?)}");
				cs.registerOutParameter(2, OracleTypes.CURSOR);
			}else{
				cs = con.prepareCall("{call getAccessionNumberToMove(?)}");
			}
			cs.setInt(1, minimumLife);
            
            cs.execute();
            try {
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(2);
                } else {
                    rs= cs.getResultSet();
                }
            } catch (SQLException sex) {
            } 
            ArrayList<String> temp=null;
            if(rs!=null){
            	temp=new ArrayList<String>();
            	while(rs.next()){
            		temp.add(rs.getString(1));
            	}
            }
            
            if ((temp != null) && (temp.size() > 0)) {
                ret = new String[temp.size()];
                temp.toArray(ret);
            }
            
		} catch (Exception ex) {
			try{con.rollback();}catch(Exception iex){log.error("Could not rollback", iex);}
			log.error("", ex);
		} finally {
			try {rs.close();} catch (SQLException sex) {}
			try {cs.close();} catch (SQLException sex) {}
			try {con.close();} catch (SQLException sex) {}
		}
		return ret;
	}
	
	public abstract void run();

}
