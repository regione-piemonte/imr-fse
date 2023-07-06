/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.FileHasher;
import it.units.htl.dpacs.helpers.LogMessage;
import it.units.htl.dpacs.helpers.MBeanDealer;
import it.units.htl.dpacs.valueObjects.DicomConstants;
import it.units.htl.dpacs.valueObjects.Instance;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class DicomStgCmtDealerBean implements DicomStgCmtDealerLocal {
    
    private @Resource(name="java:/jdbc/stgCmtDS") DataSource dataSource;
    private Log log = LogFactory.getLog(DicomStgCmtDealerBean.class);
    private static final String aeData = "SELECT ip, port, cipherSuites, mobile FROM KnownNodes WHERE aeTitle=?";
    private static final String qrySavedHash = "SELECT hash FROM HashTable WHERE sopInstanceUid = ? ";

    public DicomStgCmtDealerBean() {
    }
    public void setSessionContext(SessionContext sc) {
    }

    public String[] getAEData(String ae) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try{
            con = dataSource.getConnection();
        } catch (SQLException ex) {
            log.error(LogMessage._NoDBConnection, ex);
        }
        String[] args = null;
        try{
        	ps = con.prepareStatement(aeData);
            ps.setString(1, ae);
            rs = ps.executeQuery();
            if (rs.next()) {
                log.info("Retrieved data for " + ae);
                args = new String[5];
                args[0] = ae;
                args[1] = rs.getString(1);
                args[2] = rs.getString(2);
                args[3] = rs.getString(3);
                args[4] = rs.getString(4);
            }
        }catch (SQLException sex) {
            log.fatal("Error Retrieving AE Data: ", sex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(ps);
        	CloseableUtils.close(con);
        }
        return args;
    }

    public Instance[] commitStorage(Instance[] in) {
        Connection con = null;
        CallableStatement urlToInstCS = null;
        PreparedStatement stGetHash = null;
        ResultSet rsHash = null;
        
        String hashAlgorithm = null;
        
        try{
            hashAlgorithm = MBeanDealer.getPropertyFromMbean(new ObjectName("it.units.htl.dpacs.servers:type=DicomServer,index=3"), "HashAlgorithm");
            //hashAlgorithm = MBeanDealer.getPropertyFromMbean(new ObjectName("it.units.htl.dpacs.servers:type=DicomServer,index=3"), "getHashAlgorithm"); //GDC
            log.info("About to commit " + in.length + " instances");
            con = dataSource.getConnection();
            urlToInstCS = con.prepareCall("{call getUrlToOnlineInstance(?,?)}");
            stGetHash = con.prepareStatement(qrySavedHash);
            for (int i = in.length - 1; i >= 0; i--) {
                urlToInstCS.setString(1, in[i].getSopInstanceUid());
                urlToInstCS.registerOutParameter(2, Types.VARCHAR);
                log.debug("Looking for physical path: " + in[i].getSopInstanceUid());
                urlToInstCS.execute();
                String instanceUrl = urlToInstCS.getString(2);
                if (instanceUrl != null) {
                    File currInstance = new File(instanceUrl);
                    if (currInstance.exists()) {
                        
                        String currHash;
                        FileHasher fh = new FileHasher();
                        currHash = fh.doHash(currInstance, hashAlgorithm);
                        
                        stGetHash.setString(1, in[i].getSopInstanceUid());
                        rsHash = stGetHash.executeQuery();
                        while (rsHash.next()) {
                            if (rsHash.getString(1).equals(currHash)) {
                                log.debug("Hashes match" + in[i].getSopInstanceUid());
                                in[i].setDeprecated(DicomConstants.DEPRECATED);
                            } else {
                                log.warn("Hashes do not match: " + in[i].getSopInstanceUid());
                            }
                        }
                    } else {
                        log.error("Couldn't find the specified instance: " + instanceUrl);
                        continue;
                    }
                }
            }
            log.info("StgCmtDealer: " + in.length + " instances committed");
        
        } catch (MalformedObjectNameException monex) {
            log.fatal("Error, the ObjectName is malformed", monex);
        } catch (Exception ex) {
        	log.error("Error Committing an Instance: ", ex);
        }finally{
        	CloseableUtils.close(rsHash);
        	CloseableUtils.close(stGetHash);
        	CloseableUtils.close(urlToInstCS);
        	CloseableUtils.close(con);
        }
        return in;
    }

    
}
