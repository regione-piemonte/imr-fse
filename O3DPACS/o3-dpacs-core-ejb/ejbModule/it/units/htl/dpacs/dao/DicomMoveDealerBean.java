/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.AEData;
import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.valueObjects.DicomMatch;
import it.units.htl.dpacs.valueObjects.Image;
import it.units.htl.dpacs.valueObjects.Instance;
import it.units.htl.dpacs.valueObjects.NearlineData;
import it.units.htl.dpacs.valueObjects.NonImage;
import it.units.htl.dpacs.valueObjects.Overlay;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.dpacs.valueObjects.PresState;
import it.units.htl.dpacs.valueObjects.Series;
import it.units.htl.dpacs.valueObjects.StructRep;
import it.units.htl.dpacs.valueObjects.Study;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class DicomMoveDealerBean implements DicomMoveDealerLocal, DicomMoveDealerRemote {
    private static final long serialVersionUID = 1L;
    private @Resource(name = "java:/jdbc/moveDS")
    DataSource dataSource;
    private static final String SEPARATOR_DICOM = "\\";
    private static final String SEPARATOR_LIST_OF_UIDS = "','";
    private static final String baseUrl = "SELECT fastestAccess FROM Studies WHERE studyInstanceUID=?";
    static final Log log = LogFactory.getLog(DicomMoveDealerBean.class);

    public DicomMoveDealerBean() {
    }

    
    @PreDestroy
    public void ejbRemove() {
    }

    public DicomMatch[] retrieveMatches(Patient pat, Study st, Series se, Instance i, String callingAE) {
    	Connection con = null;
    	CallableStatement cs = null;
        ResultSet rs = null;
    	log.debug(callingAE + ": MoveDealer: Retrieving data");
        DicomMatch[] res = null;
        ArrayList<DicomMatch> rl = null;
        
        try{
            con = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                cs = con.prepareCall("{call getDataForCMove(?,?,?,?,?)}");
                cs.registerOutParameter(5, OracleTypes.CURSOR);
            } else {
                cs = con.prepareCall("{call getDataForCMove(?,?,?,?)}");
            }
            // Fill parameters for the Stored Procedure
            if ((i != null) && (i.getSopInstanceUid() != null)) { // INSTANCE LEVEL
                cs.setString(4, i.getSopInstanceUid().replace(SEPARATOR_DICOM, SEPARATOR_LIST_OF_UIDS));
                cs.setNull(1, Types.VARCHAR);
                cs.setNull(2, Types.VARCHAR);
                cs.setNull(3, Types.VARCHAR);
            } else {
                cs.setNull(4, Types.VARCHAR);
                if ((se != null) && (se.getSeriesInstanceUid() != null)) { // SERIES LEVEL
                    cs.setString(3, se.getSeriesInstanceUid().replace(SEPARATOR_DICOM, SEPARATOR_LIST_OF_UIDS));
                    cs.setNull(1, Types.VARCHAR);
                    cs.setNull(2, Types.VARCHAR);
                } else {
                    cs.setNull(3, Types.VARCHAR);
                    if ((st != null) && (st.getStudyInstanceUid() != null)) { // STUDY LEVEL
                        cs.setString(2, st.getStudyInstanceUid().replace(SEPARATOR_DICOM, SEPARATOR_LIST_OF_UIDS));
                        cs.setNull(1, Types.VARCHAR);
                    } else {
                        cs.setNull(2, Types.VARCHAR);
                        if ((pat != null) && (pat.getPatientId() != null)) { // PATIENT LEVEL
                            cs.setString(1, pat.getPatientId());
                        } else {
                            throw new Exception("No LEVEL was specified");
                        }
                    }
                }
            }
            cs.execute();
            try {
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(5);
                } else {
                    rs = cs.getResultSet();
                }
            } catch (SQLException sex) {
            } // This is because the cursor/resultset could not be open (it's inside an "if" in the stored procedure)!!
            if (rs != null) {
                rl = new ArrayList<DicomMatch>(101);
                while (rs.next()) {
                    DicomMatch row = new DicomMatch();
                    row.patient = new Patient();
                    row.patient.setLastName(rs.getString(7));
                    row.patient.setFirstName(rs.getString(8));
                    row.patient.setMiddleName(rs.getString(9));
                    row.patient.setPrefix(rs.getString(10));
                    row.patient.setSuffix(rs.getString(11));
                    row.patient.setPatientId(rs.getString(5));
                    row.patient.setIdIssuer(rs.getString(6));
                    row.patient.setBirthDate(rs.getDate(12));
                    row.patient.setSex(rs.getString(13));
                    row.patient.setPrimaryKey(rs.getString(14));
                    row.study = new Study(rs.getString(4));
                    row.study.setFastestAccess(rs.getString(18));
                    row.study.setStudyStatus(rs.getString(16));
                    row.series = new Series(rs.getString(3));
                    String instanceType = rs.getString(15);
                    if (Instance.TYPE_IMAGE.equals(instanceType))
                        row.instance = new Image(rs.getString(1));
                    else if (Instance.TYPE_OVERLAY.equals(instanceType))
                        row.instance = new Overlay(rs.getString(1));
                    else if (Instance.TYPE_STRUCT_REP.equals(instanceType))
                        row.instance = new StructRep(rs.getString(1));
                    else if (Instance.TYPE_PRES_STATE.equals(instanceType))
                        row.instance = new PresState(rs.getString(1));
                    else
                        row.instance = new NonImage(rs.getString(1));
                    row.instance.setSopClassUid(rs.getString(2));
                    String deviceType = rs.getString(17);
                    if ((row.study.getStudyStatus().charAt(0) == Study.DPACS_NEARLINE_STATUS) && (deviceType != null)) {
                        row.nearlineData = new NearlineData(deviceType, rs.getString(19));
                        row.nearlineData.setDeviceUrl(row.study.getFastestAccess());
                    }
                    rl.add(row);
                    row = null;
                } // end while
            }
        } catch (SQLException sex) {
            log.fatal("MoveDealer: SQL Error Retrieving data: ", sex);
            return null;
        }catch (Exception ex) {
            log.fatal("MoveDealer: Error Retrieving data: ", ex);
        }finally {
        	CloseableUtils.close(rs);
        	CloseableUtils.close(cs);
        	CloseableUtils.close(con);
        }
        if ((rl != null) && (rl.size() > 0)) {
            res = new DicomMatch[rl.size()];
            rl.toArray(res);
        }
        return res;
    }

    public String getStudyUrlBase(String sUid, String callingAE) {
        Connection con = null;
        PreparedStatement baseUrlPS = null;
        ResultSet rs = null;
    	String res = null;        
        
        try{
            // TODO: It should fill patient data as well!
            con = dataSource.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        try{
        	baseUrlPS = con.prepareStatement(baseUrl);
            baseUrlPS.setString(1, sUid);
            rs = baseUrlPS.executeQuery();
            if (rs.next()){
            	res = rs.getString(1);
            }           
            
        }catch (SQLException sex) {
            log.fatal("MoveDealer: Error Retrieving Base Path for a Study: " + sex.getMessage() + "\n" + sex.getStackTrace()[0], sex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(baseUrlPS);
        	CloseableUtils.close(con);        	
        } // end try...catch...finally
        return res;
    } // end getStudyUrlBase

    public String verifyWhereStudiesAre(String studySopInstance, String callingAE) {
    	Connection con = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	
    	String URL = null;
        log.info(callingAE + ": DicomMoveBean: DicomMatch is null, images are not here... Determining if they are elsewhere");
        String verifyWhereImagesAre = "SELECT fastestAccess FROM Studies WHERE studyInstanceUID=?";
        
        try{
            // TODO: It should fill patient data as well!
            con = dataSource.getConnection();
            ps = con.prepareStatement(verifyWhereImagesAre);
            ps.setString(1, studySopInstance);
            rs = ps.executeQuery();
            if(rs.next() == true){
                log.info(callingAE + ": retrieving study location");
                URL = rs.getString(1);
                log.info(callingAE + ": Study location is: " + URL);
            }else
                log.info(callingAE + ": error: neither the study is present");
            
            
        } catch (SQLException sex) {
        	log.error(callingAE+": Retrieving study location", sex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(ps);
        	CloseableUtils.close(con);
        }
        return URL;
    }

    public AEData getAeData(String aeTitle) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try{
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT aeTitle,ip, port,cipherSuites, isAnonimized, canDeanonymized FROM KnownNodes WHERE aeTitle=?");
            ps.setString(1, aeTitle);
            rs = ps.executeQuery();
            if (rs.next()) {
                AEData foundData = new AEData(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getBoolean(5), rs.getBoolean(6));
                return foundData;
            }
        }catch (SQLException e){
            log.error("", e);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(ps);
        	CloseableUtils.close(con);                  	
        }
        return null;
    }
}
