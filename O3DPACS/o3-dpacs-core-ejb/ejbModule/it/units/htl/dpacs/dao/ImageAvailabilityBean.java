/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.IheStudyId;
import it.units.htl.dpacs.helpers.LogMessage;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class ImageAvailabilityBean implements ImageAvailabilityLocal, ImageAvailabilityRemote {

	private Log log = LogFactory.getLog(ImageAvailabilityBean.class);
	private @Resource(name="java:/jdbc/imAvailDS") DataSource dataSource;
	
	/*This inserts one SET record*/
	@Override
	public boolean insertCorrectStudy(IheStudyId ids, String reconciliationSource, String stringToSet, String targetApp) {
		Connection con = null;
		CallableStatement cs = null;
		
		try{
			con = dataSource.getConnection();
			con.setAutoCommit(false);
			cs = con.prepareCall("{call iaInsertCorrectStudy(?,?,?,?,?,?,?)}");

			cs.setString(1, ids.getStudyInstanceUid());
			if(ids.getAccessionNumber()!=null)
				cs.setString(2, ids.getAccessionNumber());
			else
				cs.setNull(2, Types.VARCHAR);
			if(ids.getPatientId()!=null)
				cs.setString(3, ids.getPatientId());
			else
				cs.setNull(3, Types.VARCHAR);
			cs.setString(4, stringToSet);
			cs.setString(5, reconciliationSource);
			cs.setString(6, targetApp);
			cs.registerOutParameter(7, Types.INTEGER);
			cs.execute();
			con.commit();
			
			return (cs.getInt(7)==1);
			
		}catch (SQLException e) {
			try{con.rollback();}catch(Exception iex){log.error("Could not rollback", iex);}
			log.error("", e);
		}finally{
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		
		return false;
	}

	/*This inserts one SET record per study closed by timer*/
	@Override
	public int insertCorrectStudies(long completedOnSecond, String reconciliationSource, String stringToSet, String targetApp) {
		Connection con = null;
		CallableStatement cs = null;
		
		try{
			con = dataSource.getConnection();
			con.setAutoCommit(false);
			cs = con.prepareCall("{call iaInsertCorrectStudies(?,?,?,?,?)}");

			cs.setLong(1, completedOnSecond);
			cs.setString(2, stringToSet);
			cs.setString(3, reconciliationSource);
			cs.setString(4, targetApp);
			cs.registerOutParameter(5, Types.INTEGER);
			cs.execute();
			con.commit();
			
			return cs.getInt(5);
			
		}catch (SQLException e){
			try{con.rollback();}catch(Exception iex){log.error("Could not rollback", iex);}
			log.error("", e);
			return -1;
		}finally{
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		
	}

	/*This inserts one REMOVE record if the study was already present, one SET record anyway*/
	@Override
	public boolean reconcileWrongStudy(IheStudyId ids, String reconciliationSource, String stringToSet, String stringToRemove, String targetApp) {
		Connection con = null;
		CallableStatement cs = null;
		
		try{
			con = dataSource.getConnection();
			con.setAutoCommit(false);
			cs = con.prepareCall("{call iaReconcileWrongStudy(?,?,?,?,?,?,?,?)}");

			cs.setString(1, ids.getStudyInstanceUid());
			if(ids.getAccessionNumber()!=null)
				cs.setString(2, ids.getAccessionNumber());
			else
				cs.setNull(2, Types.VARCHAR);
			if(ids.getPatientId()!=null)
				cs.setString(3, ids.getPatientId());
			else
				cs.setNull(3, Types.VARCHAR);
			cs.setString(4, stringToSet);
			cs.setString(5, stringToRemove);
			cs.setString(6, reconciliationSource);
			cs.setString(7, targetApp);
			cs.registerOutParameter(8, Types.INTEGER);
			cs.execute();
			con.commit();
			
			return (cs.getInt(8)==1);
			
		}catch (SQLException e){
			try{con.rollback();}catch(Exception iex){log.error("Could not rollback", iex);}
			log.error("", e);
		}finally{
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		
		return false;
	}

	/* This inserts two REMOVE records, then two SET records
	 * It checks whether */
	@Override
	public boolean reconcileWrongStudies(IheStudyId idsOne, IheStudyId idsTwo, String reconciliationSource, String stringToSet, String stringToRemove, String targetApp) {
		boolean ret=false;
		Connection con = null;
		CallableStatement cs = null;
		try{
			con = dataSource.getConnection();
			con.setAutoCommit(false);
			cs = con.prepareCall("{call iaSwapPatientAssociation(?,?,?,?,?,?,?,?,?,?,?)}");

			cs.setString(1, idsOne.getStudyInstanceUid());
			cs.setString(2, idsOne.getAccessionNumber());
			cs.setString(3, idsOne.getPatientId());
			cs.setString(4, idsTwo.getStudyInstanceUid());
			cs.setString(5, idsTwo.getAccessionNumber());
			cs.setString(6, idsTwo.getPatientId());
			cs.setString(7, stringToSet);
			cs.setString(8, stringToRemove);
			cs.setString(9, reconciliationSource);
			cs.setString(10, targetApp);
			cs.registerOutParameter(11, Types.INTEGER);
			cs.execute();
			int res=cs.getInt(11);
			
			if(res==2){
				con.commit();
				ret=true;
			}else{
				try{con.rollback();}catch(Exception iex){log.error("Could not rollback", iex);}
				log.error("Problems swapping ("+idsOne.getStudyInstanceUid()+","+idsOne.getAccessionNumber()+","+idsOne.getPatientId()+") and ("+idsTwo.getStudyInstanceUid()+","+idsTwo.getAccessionNumber()+","+idsTwo.getPatientId()+"):"+res);
			} 
			
		}catch (Exception e){
			try{con.rollback();}catch(Exception iex){}
			log.error("", e);
		}finally{
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		
		return ret;
	}
    

    
}
