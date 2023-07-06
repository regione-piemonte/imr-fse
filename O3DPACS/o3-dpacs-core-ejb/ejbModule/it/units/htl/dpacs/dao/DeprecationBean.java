/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
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
public class DeprecationBean implements DeprecationLocal, DeprecationRemote {
    private static final String DEPRECATION_BASE = "dep-";
    private static final String DEPRECATION_SEPARATOR = "-";
    private static final String DIR_SEPARATOR = "/";
    private static final char SEPARATOR_ON_DB = '-';
    private static final char SEPARATOR_ON_FS = '_';
    private static final String EVENT_DELETION = "DEL";
    private static final String EVENT_DEPRECATION = "DEP";
    private static final int REASON_LENGTH = 256;
    private Log log = LogFactory.getLog(DeprecationBean.class);
    private @Resource(name = "java:/jdbc/dbDS")
    DataSource dataSource;

    public long deprecateSeries(String originalUid, String newUid, boolean markForDeletion, String reason, long userPk) throws Exception {
        Connection con = null;
        CallableStatement cs = null;
        CallableStatement urlProcedureStatement = null;
        long deprecationId = 0;
        String url = null;
        if (reason == null) {
            reason = "";
        }
        if (reason.length() > REASON_LENGTH) {
            reason = reason.substring(0, REASON_LENGTH);
        }
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            urlProcedureStatement = con.prepareCall("{call getBaseUrlOfSeries(?,?)}");
            urlProcedureStatement.setString(1, originalUid);
            urlProcedureStatement.registerOutParameter(2, Types.VARCHAR);
            urlProcedureStatement.execute();
            url = urlProcedureStatement.getString(2);
            if (url != null) {
                File series = new File(url + originalUid + DIR_SEPARATOR);
                long size = calculateSizeOfSeries(series);
                cs = con.prepareCall("{call deprecateSeries(?,?,?,?,?,?,?)}");
                cs.setString(1, originalUid);
                cs.setString(2, newUid);
                cs.setString(3, markForDeletion ? EVENT_DELETION : EVENT_DEPRECATION);
                cs.setString(4, reason);
                cs.setLong(5, userPk);
                cs.setLong(6, size);
                cs.registerOutParameter(7, Types.BIGINT);
                cs.execute();
                deprecationId = cs.getLong(7);
                if (deprecationId > 0) { // If something has been deprecated
                    File newSeries = new File(url + (newUid.replace(SEPARATOR_ON_DB, SEPARATOR_ON_FS)) + SEPARATOR_ON_FS + originalUid + DIR_SEPARATOR);
                    if(newSeries.exists()) {
                    	if (!series.renameTo(newSeries))
                    		throw new IOException("Could not rename " + series.getAbsolutePath() + " into " + newSeries.getAbsolutePath());
                    }
                }
                con.commit();
            }
        } catch (Exception ex) {
            try {
                con.rollback();
            } catch (Exception iex) {
                log.error("Could not rollback", iex);
            }
            log.error("", ex);
            throw ex;
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(urlProcedureStatement);
            CloseableUtils.close(con);
        }
        return deprecationId;
    };

    public long deprecateStudy(String originalUid, String newUid, boolean markForDeletion, String reason, long userPk) throws Exception {
        Connection con = null;
        PreparedStatement ps = null;
        CallableStatement cs = null;
        CallableStatement csSeries = null;
        ResultSet rsUrl = null;
        ResultSet rsSeries = null;
        long deprecationId = 0;
        String url = null;
        if (reason == null) {
            reason = "";
        }
        if (reason.length() > REASON_LENGTH) {
            reason = reason.substring(0, REASON_LENGTH);
        }
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            boolean isOracle = Dbms.isOracle(con);
            ps = con.prepareStatement("SELECT fastestAccess FROM Studies WHERE studyInstanceUID=?");
            ps.setString(1, originalUid);
            rsUrl = ps.executeQuery();
            if (rsUrl.next()) { // Just one record is returned
                url = rsUrl.getString(1);
            }
            File study = new File(url + originalUid + DIR_SEPARATOR);
            if (isOracle) {
                cs = con.prepareCall("{call deprecateStudy(?,?,?,?,?,?,?)}");
                cs.registerOutParameter(7, OracleTypes.CURSOR);
            } else {
                cs = con.prepareCall("{call deprecateStudy(?,?,?,?,?,?)}");
            }
            cs.setString(1, originalUid);
            cs.setString(2, newUid);
            cs.setString(3, markForDeletion ? EVENT_DELETION : EVENT_DEPRECATION);
            cs.setString(4, reason);
            cs.setLong(5, userPk);
            cs.registerOutParameter(6, Types.BIGINT);
            cs.execute();
            try {
                if (isOracle) {
                    rsSeries = (ResultSet) cs.getObject(7);
                } else {
                    rsSeries = cs.getResultSet();
                }
            } catch (SQLException sex) {
            }
            ArrayList<String> children = null;
            if (rsSeries != null) {
                children = new ArrayList<String>();
                while (rsSeries.next()) {
                    children.add(rsSeries.getString(1));
                }
            }
            deprecationId = cs.getLong(6);
            if (deprecationId > 0) { // If something has been deprecated
                csSeries = con.prepareCall("{call deprecateSeriesOfStudy(?,?,?)}");
                csSeries.setLong(3, deprecationId);
                // Loop through Series
                int i = 0;
                for (String series : children) {
                    csSeries.setString(1, series);
                    csSeries.setString(2, newUid + SEPARATOR_ON_DB + (++i));
                    csSeries.addBatch();
                }
                csSeries.executeBatch();
                
                // O3DPACS-32 # se lo studio non esiste nel filesystem deve continuare la deprecazione su db
                if(study.exists()) {
	                File newStudy = new File(url + (newUid.replace(SEPARATOR_ON_DB, SEPARATOR_ON_FS)) + SEPARATOR_ON_FS + originalUid + DIR_SEPARATOR);
	                if (!study.renameTo(newStudy))
	                    throw new IOException("Could not rename " + study.getAbsolutePath() + " into " + newStudy.getAbsolutePath());
                }
            }
            con.commit();
        } catch (Exception ex) {
            try {
                con.rollback();
            } catch (Exception iex) {
                log.error("Could not rollback", iex);
            }
            log.error("", ex);
            throw ex;
        } finally {
            CloseableUtils.close(rsSeries);
            CloseableUtils.close(rsUrl);
            CloseableUtils.close(ps);
            CloseableUtils.close(cs);
            CloseableUtils.close(csSeries);
            CloseableUtils.close(con);
        }
        return deprecationId;
    };

    public RecoveryItem[] getPossibleRecoveries(String objectType) {
        Connection con = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        RecoveryItem[] ret = null;
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                cs = con.prepareCall("{call getPossibleRecoveries(?,?,?)}");
                cs.registerOutParameter(3, OracleTypes.CURSOR);
            } else {
                cs = con.prepareCall("{call getPossibleRecoveries(?,?)}");
            }
            if (objectType == null) {
                cs.setNull(1, Types.VARCHAR);
            } else {
                cs.setString(1, objectType);
            }
            cs.setString(2, EVENT_DEPRECATION);
            cs.execute();
            try {
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(3);
                } else {
                    rs = cs.getResultSet();
                }
            } catch (SQLException sex) {
            }
            ArrayList<RecoveryItem> temp = null;
            if (rs != null) {
                temp = new ArrayList<RecoveryItem>();
                while (rs.next()) {
                    temp.add(new RecoveryItem(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getTimestamp(8), rs.getString(9)));
                }
            }
            if ((temp != null) && (temp.size() > 0)) {
                ret = new RecoveryItem[temp.size()];
                temp.toArray(ret);
            }
        } catch (Exception ex) {
            try {
                con.rollback();
            } catch (Exception iex) {
                log.error("Could not rollback", iex);
            }
            log.error("", ex);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return ret;
    }

    public long recoverSeries(long deprecationId, String currentUid, String originalUid, long userPk) throws Exception {
        Connection con = null;
        CallableStatement cs = null;
        CallableStatement urlProcedureStatement = null;
        long ret = 0;
        String url = null;
        String originalUidFromDb = null;
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            urlProcedureStatement = con.prepareCall("{call getBaseUrlOfSeries(?,?)}");
            urlProcedureStatement.setString(1, currentUid);
            urlProcedureStatement.registerOutParameter(2, Types.VARCHAR);
            urlProcedureStatement.execute();
            url = urlProcedureStatement.getString(2);
            File series = new File(url + (currentUid.replace(SEPARATOR_ON_DB, SEPARATOR_ON_FS)) + SEPARATOR_ON_FS + originalUid + DIR_SEPARATOR);
            if(!series.exists()) throw new Exception(series.getAbsolutePath() + " does not exist!");
            long size = 0;
            try {
                size = calculateSizeOfSeries(series);
            } catch (Exception ex) {
                log.debug("Probably study has been deprecated", ex);
                size = 0;
            }
            cs = con.prepareCall("{call recoverSeries(?,?,?,?,?,?)}");
            cs.setLong(1, deprecationId);
            cs.setString(2, currentUid);
            cs.setLong(3, userPk);
            cs.setLong(4, size);
            cs.registerOutParameter(5, Types.BIGINT);
            cs.registerOutParameter(6, Types.VARCHAR);
            cs.execute();
            ret = cs.getLong(5);
            originalUidFromDb = cs.getString(6);
            if (ret > 0 && originalUid.equals(originalUidFromDb)) { // If something has been successfully recovered
                File newSeries = new File(url + originalUidFromDb + DIR_SEPARATOR);
                if (!series.renameTo(newSeries))
                    throw new IOException("Could not rename " + series.getAbsolutePath() + " into " + newSeries.getAbsolutePath());
                con.commit();
            } else {
                con.rollback();
            }
        } catch (Exception ex) {
            try {
                con.rollback();
            } catch (Exception iex) {
                log.error("Could not rollback", iex);
            }
            log.error("", ex);
            throw ex;
        } finally {
            CloseableUtils.close(cs);
            CloseableUtils.close(urlProcedureStatement);
            CloseableUtils.close(con);
        }
        return ret;
    }

    public long recoverStudy(long deprecationId, String currentUid, String originalUid, long userPk) throws Exception {
        Connection con = null;
        CallableStatement cs = null;
        PreparedStatement ps = null;
        ResultSet rsUrl = null;
        long ret = 0;
        String url = null;
        String originalUidFromDb = null;
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            ps = con.prepareStatement("SELECT fastestAccess FROM Studies WHERE studyInstanceUID=?");
            ps.setString(1, currentUid);
            rsUrl = ps.executeQuery();
            if (rsUrl.next()) { // Just one record is returned
                url = rsUrl.getString(1);
            }
            File study = new File(url + (currentUid.replace(SEPARATOR_ON_DB, SEPARATOR_ON_FS)) + SEPARATOR_ON_FS + originalUid + DIR_SEPARATOR);
            cs = con.prepareCall("{call recoverStudy(?,?,?,?,?)}");
            cs.setLong(1, deprecationId);
            cs.setString(2, currentUid);
            cs.setLong(3, userPk);
            cs.registerOutParameter(4, Types.BIGINT);
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.execute();
            ret = cs.getLong(4);
            originalUidFromDb = cs.getString(5);
            if (ret > 0 && originalUid.equals(originalUidFromDb)) { // If something has been successfully recovered
                File newStudy = new File(url + originalUidFromDb + DIR_SEPARATOR);
                if (!study.renameTo(newStudy))
                    throw new IOException("Could not rename " + study.getAbsolutePath() + " into " + newStudy.getAbsolutePath());
                con.commit();
            } else {
                con.rollback();
            }
        } catch (Exception ex) {
            try {
                con.rollback();
            } catch (Exception iex) {
                log.error("Could not rollback", iex);
            }
            log.error("", ex);
            throw ex;
        } finally {
            CloseableUtils.close(rsUrl);
            CloseableUtils.close(cs);
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return ret;
    }

    public String getNewDeprecationUid() {
        return DEPRECATION_BASE + System.currentTimeMillis() + DEPRECATION_SEPARATOR + (new Random().nextInt(100) + 1);
    }

    public boolean deleteStudy(String studyUid, String path) {
        Connection con = null;
        CallableStatement cs = null;
        PreparedStatement ps = null;
        boolean done = false;
        try {
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            cs = con.prepareCall("{call deleteStudy(?,?,?,?)}");
            cs.setString(1, studyUid);
            cs.registerOutParameter(2, Types.BIGINT);
            cs.registerOutParameter(3, Types.BIGINT);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.execute();
            long size = cs.getLong(2);
            long physMediaPk = cs.getLong(3);
            String originalUid = cs.getString(4);
            done = deleteStudyDirectory(new File(path, studyUid.replace(SEPARATOR_ON_DB, SEPARATOR_ON_FS) + SEPARATOR_ON_FS + originalUid));
            if (done) {
                if (physMediaPk > 0) {
                    ps = con.prepareStatement("UPDATE PhysicalMedia SET filledBytes=filledBytes-? WHERE pk=?");
                    ps.setLong(1, size);
                    ps.setLong(2, physMediaPk);
                    ps.execute();
                }
            } else {
            	// JIRA O3DPACS-32 lo studio viene cancellato anche se non trovato nella directory fisica
                //con.rollback();
            }
            con.commit();
            
        } catch (Exception ex) {
            try {
                con.rollback();
            } catch (Exception iex) {
                log.error("Could not rollback", iex);
            }
            log.error("", ex);
        } finally {
            CloseableUtils.close(ps);
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return done;
    }

    private long calculateSizeOfSeries(File series) throws Exception {
        long size = 0;
        File[] instances = series.listFiles();
        for (File instance : instances)
            size += instance.length();
        return size;
    }

    private boolean deleteStudyDirectory(File study) {
        //boolean done = false;
        if (study.isDirectory()) {
            try {
                File[] seriesList = study.listFiles();
                for (File series : seriesList) {
                    if (series.isDirectory()) {
                        File[] instances = series.listFiles();
                        for (File instance : instances) {
                            if (instance.isFile())
                                instance.delete();
                        }
                    }
                    series.delete();
                }
            } catch (Exception ex) {
                log.error("Error deleting " + study.getAbsolutePath(), ex);
            }
        }
        // JIRA O3DPACS-32 lo studio viene cancellato anche se non trovato nella directory fisica
        //done = 
        study.delete(); // If anything could not be deleted, this will yield false
        //return done;
       
        // Controllo directory vuote
        boolean deleteEmptyPath = Boolean.valueOf(GlobalConfigurationLoader.getConfigParam("deleteEmptyPath"));
        if(deleteEmptyPath) {
	        File dayPath = study.getParentFile();
	        checkEmptyPath(dayPath);
	        File monthPath = dayPath.getParentFile();
	        checkEmptyPath(monthPath);            	
	        File yearPath = monthPath.getParentFile();
	        checkEmptyPath(yearPath);
        }
        return true;
    }

    // Se la directory e' vuota la cancella
	private void checkEmptyPath(File path) {
		if(path != null && path.isDirectory()) {
			File[] listFiles = path.listFiles();
			if(listFiles != null && listFiles.length == 0) {
				path.delete();
			}
		}
	}
	
}
