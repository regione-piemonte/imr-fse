/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.helpers;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import eu.o3c.o3e.flex.utils.StudiesStatistic;
import eu.o3c.o3e.flex.utils.StudyVerificationItem;

public class StudiesVerifierHandler {
    private static Logger log = Logger.getLogger(StudiesVerifierHandler.class);
    
    public StudiesStatistic getStStat(){
        StudiesStatistic sts = new StudiesStatistic();
        Connection con = null;
        CallableStatement st = null;
        ResultSet rs = null;
        try{
            con = getConnection();
//            get all received studies
            st = con.prepareCall("select count(*) from Studies");
            rs = st.executeQuery();
            if(rs != null){
                while(rs.next()){
                    sts.setReceivedStudiesCounter(rs.getInt(1));
                }
            }
            st.clearBatch();
//            get the study that will be verified
            st = con.prepareCall("SELECT count(DISTINCT stv.studyfk) FROM studiestoverify stv" +
            		" where stv.studyfk not in (select studyfk from studiesverifierevents) and" +
            		" verifieddate is null");
            rs = st.executeQuery();
            if(rs!=null){
                while(rs.next()){
                    sts.setStudiesToVerifyCounter(rs.getInt(1));
                }
            }
            st.clearBatch();
//            get study with unverified 
            st = con.prepareCall("SELECT count(distinct stve.studyfk) FROM studiestoverify stv " +
            		" INNER JOIN studiesverifierevents stve ON stve.studyfk = stv.studyfk" +
            		" WHERE verifieddate IS NULL ");
            rs = st.executeQuery();
            if(rs != null){
                while(rs.next()){
                    sts.setUnverifiedStudiesCounter(rs.getInt(1));
                }
            }
//            unregistered studies
            st.clearBatch();
            st = con.prepareCall("SELECT count(distinct stve.studyfk)" +
            		" FROM studiestoverify stv" +
            		" INNER JOIN studiesverifierevents stve ON stve.studyfk = stv.studyfk" +
            		" WHERE verifieddate IS not NULL" +
            		" AND stv.jobfinishedon is null");
            rs = st.executeQuery();
            if(rs != null){
                while(rs.next()){
                    sts.setUnregStudiesCounter(rs.getInt(1));
                }
            }
            
            st.clearBatch();
            st = con.prepareCall("select count(*) from studiesToVerify where jobfinishedOn is not null");
            rs = st.executeQuery();
            if(rs != null){
                while(rs.next()){
                    sts.setSuccessStudies(rs.getInt(1));
                }
            }
            
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            try{
                if(rs != null) rs.close();
                if(st!=null)st.close();
                if(con != null)con.close();
            }catch (Exception e) {
                // TODO: handle exception
            }
        }
        return sts;
    }
    
    public ArrayList<StudyVerificationItem> getProblematicStudies(){
        ArrayList<StudyVerificationItem> studiesList = new ArrayList<StudyVerificationItem>();
        
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rsStudiesInVerification = null; 
        
        String query = "select * from vw_studiesinverification";
        try{
            con = getConnection();
            ps = con.prepareStatement(query);
            rsStudiesInVerification = ps.executeQuery();
            while(rsStudiesInVerification.next()){
                StudyVerificationItem stv = new StudyVerificationItem();
                stv.setStudyUID(rsStudiesInVerification.getString(1));
                stv.setSourcePacsFk(rsStudiesInVerification.getInt(2));
                stv.setLastInsDate(rsStudiesInVerification.getTimestamp(3));
                stv.setSourceAeTitle(rsStudiesInVerification.getString(6));
                stv.setAccessionNumber(rsStudiesInVerification.getString(7));
                stv.setToBeIgnored(rsStudiesInVerification.getBoolean(5));
                stv.setStudyStatus(rsStudiesInVerification.getInt(9));
                stv.setLastTryToChangeStatus(rsStudiesInVerification.getTimestamp(10));
                stv.setLastErrorMessage(rsStudiesInVerification.getString(11));
                studiesList.add(stv);
            }
        }catch (Exception e) {
            log.error("Unable to get the studies in verification!", e);
        }finally{
                try {
                    if(rsStudiesInVerification != null)rsStudiesInVerification.close();
                    if(ps!=null)ps.close();
                    if(con!= null)con.close();
                } catch (SQLException e) {
                }
        }
        return studiesList;
    }
    
    
    private Connection getConnection() throws SQLException, NamingException {
        try {
            InitialContext jndiContext = new InitialContext();
            DataSource dataSource = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
            return dataSource.getConnection();
        } catch (SQLException sex) {
            log.fatal("Unable to create Connection to DB", sex);
            throw sex;
        } catch (NamingException nex) {
            log.fatal("Unable to retrieve the DataSource", nex);
            throw nex;
        }
    }
}
