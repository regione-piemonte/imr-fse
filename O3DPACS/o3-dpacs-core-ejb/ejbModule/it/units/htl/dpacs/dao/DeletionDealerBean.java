/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.Types;
//import org.jboss.ejb3.annotation.LocalBinding;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.LogMessage;

@Stateless
public class DeletionDealerBean implements DeletionDealer{
	private static final long serialVersionUID = 1L;
	@Resource(name="java:/jdbc/dbDS")
	private DataSource dataSource;
	//private @Resource(name="java:/jdbc/dbDS") DataSource dataSource;
	private long currPhysMediaId;
    static final Log log = LogFactory.getLog("DeletionDealer");
        
	public DeletionDealerBean(){
	}

	@Remove
	public void ejbRemove(){
	}

	public void setSessionContext(SessionContext sc){
	}

	// Business Methods
	public void deleteStudy(String studyUid) throws CannotDeleteException{
		
		boolean hashesDeleted=true;
		currPhysMediaId=0;
		String studyFastestAccess=null;
		
		
		// Deprecate Series
		try{
			deprecateSeries(studyUid);			
		}catch(Exception ex){
			log.error(studyUid+": Error deprecating series ",ex);
			throw new CannotDeleteException(CannotDeleteException.ERROR_SERIES);
		}

		// Update Study
		try{
			studyFastestAccess=updateStudy(studyUid);			
		}catch(Exception ex){
			log.error(studyUid+": Error updating study ",ex);
			throw new CannotDeleteException(CannotDeleteException.ERROR_STUDY);
		}
		// Delete from StudyLocations
		try{
			deleteStudyLocations(studyUid);			
		}catch(Exception ex){
			log.error(studyUid+": Error deleting study locations ",ex);
			throw new CannotDeleteException(CannotDeleteException.ERROR_STUDYLOCATIONS);
		}
		// Delete from Instance Tables and hash
		try{
			hashesDeleted=deleteInstances(studyUid);			
		}catch(Exception ex){
			if(hashesDeleted){
				log.error(studyUid+": Error deleting instances ",ex);
				throw new CannotDeleteException(CannotDeleteException.ERROR_INSTANCES);
			}else{
				log.error(studyUid+": Error deleting instance hashes ",ex);
				throw new CannotDeleteException(CannotDeleteException.ERROR_HASH);
			}
		}
		// Delete from File System
		boolean deletedDir=false;
		try{
			File path=new File(((studyFastestAccess.endsWith("/")||studyFastestAccess.endsWith("\\"))?studyFastestAccess:studyFastestAccess+"/")+studyUid);
			log.info("About to delete folder "+path.getAbsolutePath());
			deleteDirectory(path);
			deletedDir = !path.exists();
		}catch(Exception ex){
			log.error(studyUid+": Error deleting study locations ",ex);
			throw new CannotDeleteException(CannotDeleteException.ERROR_STUDYLOCATIONS);
		}
		
		try{
			if(deletedDir){
				updatePhysicalMediaSize(studyUid, currPhysMediaId);
			}
		}catch(Exception ex){
			log.error(studyUid+": Error updating physical media free size ",ex);
			throw new CannotDeleteException(CannotDeleteException.ERROR_STUDYLOCATIONS);
		}
	
	}

	private void deprecateSeries(String studyUid) throws Exception{
		Connection con = null;
		Statement stat = null;
		
		String query="UPDATE Series SET deprecated= 1 WHERE studyFK='"+studyUid+"'";

		try{
			con = dataSource.getConnection();
			stat = con.createStatement();
			stat.executeUpdate(query);
		}catch(SQLException sex){
			log.error("An error occcurred: ",sex);
			throw sex;
		}finally{
			CloseableUtils.close(stat);
			CloseableUtils.close(con);
		}
	}
	
	private String updateStudy(String studyUid) throws Exception{
		Connection con = null;
		CallableStatement cs = null;
		String fastestAccess = null;
		
		try{
			con = dataSource.getConnection();
			cs = con.prepareCall("{call archiveStudy(?,?,?)}");
			cs.setString(1,studyUid);
			cs.registerOutParameter(2, Types.VARCHAR);
			cs.registerOutParameter(3, Types.BIGINT);
			cs.execute();
			
			fastestAccess=cs.getString(2);
			currPhysMediaId=cs.getLong(3);
				
		}catch(SQLException sex){
			log.error("An error occcurred: ",sex);
			fastestAccess=null;
			throw sex;
		}finally{
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		
		return fastestAccess;
	}
	
	private void updatePhysicalMediaSize(String sid, long pmid) throws Exception{
		Connection con = null;
		CallableStatement cs = null;
		
		try{
			con = dataSource.getConnection();
			cs = con.prepareCall("{call updatePhysicalMediaSize(?,?,?)}");
			cs.setString(1,sid);
			cs.setLong(2, pmid);
			cs.registerOutParameter(3, Types.BIGINT);
			cs.execute();
			
			if(cs.getLong(3)==0)
				throw new Exception("Could not find the studySize or studySize=0");
				
		}catch(Exception ex){
			log.error("An error occcurred updating Physical Media size: ",ex);
			throw ex;
		}finally{
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
	}
	
	private void deleteStudyLocations(String studyUid) throws Exception{
		Connection con = null;
		Statement stat = null;
		
		String query="DELETE FROM studyLocations WHERE studyFK='"+studyUid+"'";

		try{
			con = dataSource.getConnection();
			stat=con.createStatement();
			stat.executeUpdate(query);
		}catch(SQLException sex){
			log.error("An error occcurred: ",sex);
			throw sex;
		}finally{
			CloseableUtils.close(stat);
			CloseableUtils.close(con);
		}
	}
	
	private boolean deleteInstances(String studyUid) throws Exception{
		Connection con = null;
		CallableStatement cs = null;
		
		boolean deletedHashes=true; 
		try{
			con = dataSource.getConnection();
			cs = con.prepareCall("{call deleteInstances(?)}");
			cs.setString(1,studyUid);
			cs.execute();			
		}catch(SQLException sex){
			log.error("An error occcurred: ",sex);
			deletedHashes=false;
			throw sex;
		}finally{
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		return deletedHashes;
		
	}
	
	private void deleteDirectory(File directoryToEmpty) throws Exception{
		deleteDirectory(directoryToEmpty, true);
	}
	
	private void deleteDirectory(File directoryToEmpty, boolean deleteDir) throws Exception{
		
		if(directoryToEmpty.exists()){
			File[] files = directoryToEmpty.listFiles();
			int filesNum = files.length;
			for (int i = filesNum - 1; i >= 0; --i) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i], true);
				}else{
			        if(!files[i].delete()){
			        	log.warn("Unable to delete "+files[i].getAbsolutePath());
			        }
			    }	
			}
			
			if(deleteDir){
				if(!directoryToEmpty.delete()){
					log.warn("Unable to delete "+directoryToEmpty.getAbsolutePath());
				}
			}
		}
	}
	
}	// end class
