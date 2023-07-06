/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.valueObjects.MoveStudyHistory;
import it.units.htl.dpacs.valueObjects.RecoveryItem;

import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class StudyMoveRequestBean implements StudyMoveRequestLocal, StudyMoveRequestRemote {
    private Log log = LogFactory.getLog(StudyMoveRequestBean.class);
    private @Resource(name = "java:/jdbc/dbDS") DataSource dataSource;
    //taskid:300060 bug:34065 / taskid:326054 bug:37966
    private static final String insertMoveStudyHistory = "INSERT INTO MoveStudyHistory (calledAet, callingAet, moveAet, accessionNumber, structAsr, knownNodeFk,  ris, messageId, patientid, idIssuer) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String selectKnownNodeStruct  = "SELECT a.callingAet, a.knownNodeFk, k.aeTitle calledAet FROM knownNodeToStructAsr a, knownNodes k where a.structAsr = ? and k.pk = a.knownNodeFk";
    
    public long insertRequestStudyMove(String messageID, String ris, String accNum, String azienda, String struttura, String action, long userPk, String patientID, String idIssuer) {


        log.info("****************************************************");
        log.info("insertRequestStudyMove in StudyMoveRequestBean class");
        log.info("****************************************************");
    	
    	MoveStudyHistory moveSH = new MoveStudyHistory();
        moveSH.setAccessionnumber(accNum);
        moveSH.setMessageId(messageID);
        moveSH.setRis(ris);
        //taskid:300060 bug:34065
        moveSH.setPatientID(patientID);
        //taskid:326054 bug:37966
        moveSH.setIdIssuer(idIssuer);
        int res = 0;
    	try {
			res = addMoveStudyHistory(moveSH, struttura);
		} catch (SQLException sex) {
			log.error("Error in insertRequestStudyMove",sex);
			return -1;
		}       
    	
        log.info("moveSH = " +  moveSH);
        log.info("res = " +  res);
        log.info("****************************************************");
    	
    	return res;
    };

    public int insertMoveStudyHistory(MoveStudyHistory moveSH, String structASR) throws CannotUpdateException {
        int ans = 0;
        
        log.info("HL7Dealer: Updating patient info about Ris/IDM/ACCNUM = " + moveSH.getRis()+"---"+moveSH.getMessageId()+"---"+moveSH.getAccessionnumber()+"---"+structASR);

        try {

        	int ris = addMoveStudyHistory(moveSH, structASR);
        	
        	if (ris == 0)
        	   log.info("AccessionNumber "  + moveSH.getAccessionnumber() + " stored");
        	else
        	   log.info("AccessionNumber "  + moveSH.getAccessionnumber() + " not stored");
        } catch (SQLException sex) {
            log.fatal("HL7Dealer: Error updating a patient: ", sex);
        }
       
        return ans;
    } // end insertMoveStudyHistory
    

	private int addMoveStudyHistory(MoveStudyHistory moveSH, String structASR) throws SQLException {
		int res = 0;
		PreparedStatement insertMoveStudyHistoryPS = null;
		PreparedStatement selectStructPS = null;
		Connection con = null;
		ResultSet rs = null;
		try {

			String callingAET = "";
			String knownNode = "";
			String calledAET = "";

			con = dataSource.getConnection();
			con.setAutoCommit(false);

			selectStructPS = con.prepareStatement(selectKnownNodeStruct);
			selectStructPS.setString(1, structASR);
			rs = selectStructPS.executeQuery();

			if (rs != null && rs.next()) {
				callingAET = rs.getString(1);
				knownNode = rs.getString(2);
				calledAET = rs.getString(3);
				log.debug("Trying to add a MoveStudyHistory!!");
				insertMoveStudyHistoryPS = con.prepareStatement(insertMoveStudyHistory);
				insertMoveStudyHistoryPS.setString(1, calledAET);
				insertMoveStudyHistoryPS.setString(2, callingAET);
				insertMoveStudyHistoryPS.setString(3, callingAET);
				insertMoveStudyHistoryPS.setString(4, moveSH.getAccessionnumber());
				insertMoveStudyHistoryPS.setString(5, structASR);
				insertMoveStudyHistoryPS.setString(6, knownNode);
				insertMoveStudyHistoryPS.setString(7, moveSH.getRis());
				insertMoveStudyHistoryPS.setString(8, moveSH.getMessageId());
				//taskid:300060 bug:34065
				insertMoveStudyHistoryPS.setString(9, moveSH.getPatientID());
		    //taskid:326054 bug:37966
				insertMoveStudyHistoryPS.setString(10, moveSH.getIdIssuer());
				
				insertMoveStudyHistoryPS.executeUpdate();
				con.commit();
			} else {
				log.debug("Struct not present !!");
				res = -1;
			}

		} catch (SQLException sex) { // This occurs if the visit was already present.
			log.error("Error in addMoveStudyHistory: " + sex.getMessage());
		} catch (Exception ex) {
			throw new SQLException(ex);
		} finally {
			CloseableUtils.close(rs);
			CloseableUtils.close(selectStructPS);
			CloseableUtils.close(insertMoveStudyHistoryPS);
			CloseableUtils.close(con);
		}
		return res;
	} // end addMoveStudyHistory()
}
