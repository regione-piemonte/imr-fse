/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.deletion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TimerTask;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.dao.DeprecationRemote;
import oracle.jdbc.driver.OracleTypes;

public class StudyEraserWorker extends TimerTask{
	
	private static final String REASON = "TIMER";
	private int minimumLife;
	private int hoursToRecover;
	private long userPk;
	private Log log = LogFactory.getLog(StudyEraserWorker.class);
	private  DataSource dataSource;
	
	
	public StudyEraserWorker(int minimumLife, int hoursToRecover, String user, String pass) {
		this.minimumLife = minimumLife;
		this.hoursToRecover = hoursToRecover;
		setDatasource();
		userPk=getUser(user, pass);
	}
	
	private void setDatasource() {
        try {
        	dataSource = (DataSource) InitialContext.doLookup("java:/jdbc/dbDS");
            log.debug("Connected to the DB");
        } catch (NamingException nex) {
            log.error("Unable to retrieve DataSource to DB: ", nex);
        }
    }
	
	private long getUser(String user, String pass) {
		long ret=-1;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs=null;
		try{
			con = dataSource.getConnection();
			ps=con.prepareStatement("SELECT pk FROM Users WHERE userName=? AND password=? AND pwdExpirationDate IS NULL");
			ps.setString(1, user);
			ps.setString(2, pass);
			rs=ps.executeQuery();
			if(rs.next()){	// Just one record is returned
				ret=rs.getLong(1);
			}
		}catch(Exception ex){
			log.error("Error authenticating "+user,ex);
		}finally{
			try {rs.close();} catch (SQLException sex) {}
			try {ps.close();} catch (SQLException sex) {}
			try {con.close();} catch (SQLException sex) {}
		}
		return ret;
	}

	public synchronized void run() {
		String[] toMark=getStudiesToMark();
		DeprecationRemote bean=null;
		log.info("Start this round");
		if(toMark!=null){
			try{
				bean = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DeprecationBean!it.units.htl.dpacs.dao.DeprecationRemote");
				long ret=-1;
				for(String studyUid: toMark){
					
						String newUid=bean.getNewDeprecationUid();
						ret=bean.deprecateStudy(studyUid, newUid, true, REASON, userPk);
						if(ret<=0)
							log.error("Error deprecating "+studyUid);
					
				}
			}catch(Exception ex){
				log.error("An exception occurred when marking a study for deletion", ex);
			}
			log.info("Finished marking process");
		}
		Hashtable<String, String> toDelete=getStudiesToDelete();
		if(toDelete!=null){
			try{
				if(bean==null)
					bean = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DeprecationBean!it.units.htl.dpacs.dao.DeprecationRemote");
				for(String study:toDelete.keySet()){
					boolean done=bean.deleteStudy(study, toDelete.get(study));
					if(!done)
						log.error("Study could not be deleted: "+study);
				}
			}catch(Exception ex){
				log.error("An exception occurred when deleting a study", ex);
			}
			log.info("Finished deletion process");
		}
		log.info("End work for this round");
	}

	private String[] getStudiesToMark(){
		String[] ret=null;
		Connection con = null;
		CallableStatement cs = null;
		ResultSet rs=null;
		
		try{
			con = dataSource.getConnection();
			boolean isOracle = Dbms.isOracle(con);
			
			if(isOracle){
				cs = con.prepareCall("{call getStudiesToMark(?,?)}");
				cs.registerOutParameter(2, OracleTypes.CURSOR);
			}else{
				cs = con.prepareCall("{call getStudiesToMark(?)}");
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

	private Hashtable<String, String> getStudiesToDelete(){
		Hashtable<String, String> ret=null;
		Connection con = null;
		CallableStatement cs = null;
		ResultSet rs=null;
		
		try{
			con = dataSource.getConnection();
			boolean isOracle = Dbms.isOracle(con);
			
			if(isOracle){
				cs = con.prepareCall("{call getStudiesToDelete(?,?,?)}");
				cs.registerOutParameter(3, OracleTypes.CURSOR);
			}else{
				cs = con.prepareCall("{call getStudiesToDelete(?,?)}");
			}
			cs.setInt(1, this.hoursToRecover);
			cs.setInt(2, this.minimumLife);
            
            cs.execute();
            try {
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(3);
                } else {
                    rs= cs.getResultSet();
                }
            } catch (SQLException sex) {
            } 
            
            if(rs!=null){
            	ret=new Hashtable<String, String>();
            	while(rs.next()){
            		ret.put(rs.getString(1), rs.getString(2));
            	}
            }
            
            
		} catch (Exception ex) {
			try{con.rollback();}catch(Exception iex){log.error("Could not rollback", iex);}
			log.error("", ex);
		} finally {
			try {rs.close();} catch (SQLException sex) {}
			try {cs.close();} catch (SQLException sex) {}
			try {con.close();} catch (SQLException sex) {}
		}
		if(ret!=null && ret.size()==0)
			ret=null;
		return ret;
	}
	
}
