/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.movement;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.valueObjects.Study;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;


public class BasicStudyMovementWorker extends StudyMovementWorker{
	
	private static final int MOVENEARLINE = 1;
	private static final int MOVEOFFLINE = 2;
	private static final String REASON_0 = "Study not found";
	private static final String REASON_MINUS1 = "General exception";
	private static final String REASON_MINUS2 = "Insertion/deletion discrepancy";
	private static final String REASON_UNKNOWN = "Unknown reason";
	private Log log = LogFactory.getLog(BasicStudyMovementWorker.class);
	private Log studyLog = LogFactory.getLog("MOVEMENTLOG");
	private  DataSource dataSource;
	boolean moveNearline;
	boolean moveOffline;
	int daysOnline;
	int daysNearline;
	
	
	public BasicStudyMovementWorker(Boolean isNearline, Boolean isOffline, Integer daysOnline, Integer daysNearline, NodeList additionalConfig) {
		this.moveNearline=isNearline;
		this.moveOffline=isOffline;
		this.daysOnline=daysOnline;
		this.daysNearline=daysNearline;
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
	
	private Connection getConnection(){
		try{
			return dataSource.getConnection();
		}catch(Exception ex){
			log.error(ex);
			return null;
		}
	}
	

	public synchronized void run() {
		if(!moveNearline){
			log.warn("Attention, StudyMovement service is enabled, but nearline movement is not");
			return;
		}
		move(MOVENEARLINE);
		if(moveOffline){
			move(MOVEOFFLINE);
		}		
		
		
	}

	private void move(int destination) {
		log.info("Starting "+(destination==MOVENEARLINE?"NEARLINE":"OFFLINE")+" movement");
		Connection con=getConnection();
		CallableStatement cs=null;
		CallableStatement studyCs=null;
		ResultSet rs=null;
		List<String> studies=null;
		boolean isOracle=false;
		if(con==null)
			return;
		
		try{
			con.setAutoCommit(false);
			isOracle=Dbms.isOracle(con);
			if(isOracle){
				cs=con.prepareCall("{call getStudiesOlderThan(?,?,?)}");
				cs.registerOutParameter(3, OracleTypes.CURSOR);
			}else{
				cs=con.prepareCall("{call getStudiesOlderThan(?,?)}");
			}
			cs.setInt(1, (destination==MOVENEARLINE?daysOnline:daysOnline+daysNearline));
			cs.setString(2, ""+(destination==MOVENEARLINE?Study.DPACS_OPEN_STATUS:Study.DPACS_NEARLINE_STATUS));
			cs.execute();
            
            if (isOracle) {
                rs = (ResultSet) cs.getObject(3);
            } else {
            	rs = cs.getResultSet();
            }
        
            if(rs!=null){
            	studies=new ArrayList<String>();
            	while(rs.next()){
            		studies.add(rs.getString(1));
            	}
            	log.info("Studies to move: "+studies.size());
            }
            
            if(studies!=null){
            	int outcome=0;
            	studyCs=con.prepareCall("{call moveStudy"+(destination==MOVENEARLINE?"Nearline":"Offline")+"(?,?)}");
            	studyCs.registerOutParameter(2, Types.INTEGER);
            	int i=0;
	            for(String study: studies){
	            	i++;
	            	studyLog.info((destination==MOVENEARLINE?"N":"O")+"-Starting "+i+": "+study);
	            	studyCs.setString(1, study);
	            	studyCs.execute();
	            	outcome=studyCs.getInt(2);
	            	if(outcome>0){
	            		con.commit();
	            		studyLog.info("Completed "+i+": "+study+ " - Instances: "+outcome);
	            	}else{
	            		con.rollback();
	            		String reason=null;
	            		switch(outcome){
	            			case 0: reason=REASON_0;break;
	            			case -1: reason=REASON_MINUS1;break;
	            			case -2: reason=REASON_MINUS2;break;
	            			default: reason=REASON_UNKNOWN; 
	            		}
	            		studyLog.info("Rolled back "+i+": "+study+ " - Reason: "+reason);
	            	}
	            		
	            }			
            }
			
		}catch(Exception ex){
			try{
				log.error("Rolling back from global block", ex);
				con.rollback();
			}catch(Exception iex){
				log.error(iex);
			}
		}finally{
			CloseableUtils.close(rs);
        	CloseableUtils.close(cs);
        	CloseableUtils.close(studyCs);
        	CloseableUtils.close(con);
		}
			
			
		// Identify studies
		
		// For each study, move it
				
		log.info("Finished "+(destination==MOVENEARLINE?"NEARLINE":"OFFLINE")+" movement");	
	}

	
}
