/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.MaskingParameter;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class DicomDbDealerBean implements DicomDbDealer{
    private static final long serialVersionUID = 1L;
    // DTODO: caches (must be global) and a way to reload them: separate method for each cache, each caled before cache's use if cache==null!
    private @Resource(name="java:/jdbc/dbDS") DataSource dataSource;
    static final Log log = LogFactory.getLog(DicomDbDealerBean.class);

    public DicomDbDealerBean() {

    }
    @PostConstruct
    public void init(){
        try{
            loadMappingCache();
            loadStorageCache();
            loadEquipmentCache();
        } catch (Exception ex) {
            log.fatal("Couldn't instantiate the service.", ex);
        }
    }

    public void ejbRemove() {
    }

    public void setSessionContext(SessionContext sc) {
        // this.context = sc;
    }

    // Business Methods
    public Hashtable<String, String> loadMappingCache() {
    	Connection con = null;
    	Statement stat = null;
    	ResultSet rs = null;
    	
        Hashtable<String, String> instanceTables = null;
        try{
            con = dataSource.getConnection();
            stat = con.createStatement();
            log.debug("Created the Statement");
            rs = stat.executeQuery("SELECT sopClassUID, tableNameIfInstance FROM SupportedSOPClasses WHERE tableNameIfInstance IS NOT NULL");
            instanceTables = new Hashtable<String, String>(83, 0.5f); // High space overhead: retrievals are frequent!!! I need it sync'd
            while (rs.next()) {
                // Load the cache
                instanceTables.put(rs.getString(1), rs.getString(2)); // sopClass->tableName
            }
            log.debug("DBDealer: Loaded Supported SOP Classes");
//            TODO AD: cancella
//            rs.close();
//            con.close();
        } catch (SQLException sex) {
            log.fatal("DBDealer: Failed to load Supported SOP Classes", sex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        }
        return instanceTables;
    } // end loadMappingCache

    public Hashtable<String, String> loadStorageCache() {
    	Connection con = null;
    	Statement stat = null;
    	ResultSet rs = null;
    	
        Hashtable<String, String> storageTable = null;
        try{
            con = dataSource.getConnection();
            stat = con.createStatement();
            log.debug("Created the Statement");
            rs = stat.executeQuery("SELECT aeTitle, preferredStorageFK FROM KnownNodes");
            storageTable = new Hashtable<String, String>(199, 0.5f); // High space overhead: retrievals are frequent!!! I need it sync'd
            while (rs.next()) {
                // Load the cache
                try {
                    storageTable.put(rs.getString(1), rs.getString(2)); // aeTitle->preferredStorage
                } catch (Exception ex) {
                    log.fatal(rs.getString(1) + " has preferred storage setted to " + rs.getString(2));
                }
            }
            log.debug("DBDealer: Loaded Storage Mappings");
        } catch (SQLException sex) {
            log.fatal("DBDealer: Failed to load Storage Mappings", sex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        }
        return storageTable;
    } // end loadStorageCache

    public String getExistingAEs(){
    	Connection con = null;
    	Statement stat = null;
    	ResultSet rs = null;
    	
        StringBuffer Aes = new StringBuffer(100);
        try{
            con = dataSource.getConnection();
            stat = con.createStatement();
            log.debug("Created the Statement");
            rs = stat.executeQuery("SELECT aeTitle, equipmentFK FROM KnownNodes WHERE equipmentFK IS NOT NULL");
            // equipmentTable=new Hashtable(400, 0.5f);
            while (rs.next()) {
                Aes.append(";" + rs.getString(1));
            }
            // aeTitle->equipmentFK
            log.debug("DBDealer: Loaded known AEs string");
        } catch (SQLException sex) {
            log.fatal("DBDealer: Failed to load AETitle", sex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        }
        if (Aes.toString().length() != 0) {
            return Aes.toString().substring(1);
        } else {
            return "";
        }
    }

    public Hashtable<String, String> loadEquipmentCache(){
    	Connection con = null;
    	Statement stat = null;
    	ResultSet rs = null;
    	
        Hashtable<String, String> equipmentTable = null;
        try{
            con = dataSource.getConnection();
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT aeTitle, equipmentFK FROM KnownNodes WHERE equipmentFK IS NOT NULL");
            equipmentTable = new Hashtable<String, String>(400, 0.5f);
            while (rs.next()) {
                equipmentTable.put(rs.getString(1), Integer.toString(rs.getInt(2))); // aeTitle->equipmentFK
            }
        } catch (SQLException sex) {
            log.fatal("DBDealer: Failed to load Equipment", sex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        }
        return equipmentTable;
    }

    public ArrayList<String> loadAbstractSyntaxes(int type){
    	Connection con = null;
    	Statement stat = null;
    	ResultSet rs = null;
    	
        ArrayList<String> al = null;
        try{
            con = dataSource.getConnection();
            stat = con.createStatement();
            log.debug("Created the Statement");
            al = new ArrayList<String>();
            rs = stat.executeQuery("SELECT sopClassUid FROM SupportedSOPClasses WHERE service=" + type);
            StringBuilder sb=new StringBuilder("Abstract syntaxes - Service "+type);
            String sop=null;
            while (rs.next()) {
            	sop=rs.getString(1);
                al.add(sop);
                sb.append("\nAS: ").append(sop);
            }
            log.info(sb.toString());
        }catch (SQLException sex) {
            log.fatal("DBDealer: Failed to load Abstract Syntaxes", sex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(stat);
        	CloseableUtils.close(con);
        }
        return al;
    } // end loadEquipmentCache

    public String getCompressionTransferSyntaxUID(String aetitle){
    	Connection con = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	
        String tsuid = null;
       
        try{
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT transferSyntaxUid FROM KnownNodes WHERE aetitle=?");
            ps.setString(1, aetitle);
            rs = ps.executeQuery();
            while (rs.next()) {
                tsuid = rs.getString(1);
            }
            log.debug(aetitle + ", DBDealer: Forced Transfer Syntax is " + tsuid);
        }catch (SQLException sex) {
            log.error(aetitle + ": Failed to load Compression Transfer Syntax", sex);
        }finally{
        	CloseableUtils.close(rs);
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return tsuid;
    }

    public MaskingParameter[] getMaskTags(String aeTitle, String modality) {
    	Connection con = null;
        CallableStatement cs = null;
        ResultSet rs = null;
    	
    	MaskingParameter[] res = null;
        ArrayList<MaskingParameter> r=null;
                
        try{
        	con = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                cs = con.prepareCall("{call getImageMaskingParams(?,?,?)}");
                cs.registerOutParameter(3, OracleTypes.CURSOR);
            } else {
                cs = con.prepareCall("{call getImageMaskingParams(?,?)}");
            }
            
            cs.setString(1, aeTitle);
            cs.setString(2, modality);
            
            cs.execute();
            try {
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(3);
                } else {
                	rs = cs.getResultSet();
                }
            } catch (SQLException sex) {
            }
            
            if(rs!=null){
            	r=new ArrayList<MaskingParameter>(5);
            	while(rs.next()){
            		Integer secondNum=rs.getInt(4);
            		if(rs.wasNull())
            			secondNum=null;
            		r.add(new MaskingParameter(rs.getInt(1), rs.getString(2), rs.getString(3), secondNum, rs.getString(5), rs.getString(6)));
            	}
            }
            
        }catch(Exception ex){
        	log.error("Error retrieving Image Masking parameters for "+aeTitle+","+modality,ex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(cs);
        	CloseableUtils.close(con);
        }

        if ((r != null) && (r.size() > 0)) {
            res = new MaskingParameter[r.size()];
            r.toArray(res);
        }
        return res;
    }

    public boolean isImageMaskingEnabled(String aeTitle) {
    	Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
    	
    	boolean elaborationEnabled = false;
        
        try{
        	con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT isImageMaskingEnabled FROM KnownNodes WHERE aeTitle=?");
            ps.setString(1, aeTitle);
            rs = ps.executeQuery();
            while (rs.next()) {
                elaborationEnabled = rs.getBoolean(1);
            }
            
        } catch (Exception ex) {
            log.error("DBDealer: Failed to load Image Elaboration Flag",ex);
        } finally {
        	CloseableUtils.close(rs);
        	CloseableUtils.close(ps);
        	CloseableUtils.close(con);
        }
        return elaborationEnabled;
    }

    public boolean isAnonymized(String aetitle) {
        Connection con = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null; 
    	
    	boolean isAnonymized = false;
                
        try{
        	con = dataSource.getConnection();
            log.debug("Created the Statement; aetitle is " + aetitle);
            ps = con.prepareStatement("SELECT isAnonimized FROM KnownNodes WHERE aeTitle=?");
            ps.setString(1, aetitle);
            rs = ps.executeQuery();
            while (rs.next()) {
                isAnonymized = rs.getBoolean("isAnonimized");
            }
            log.debug("DBDealer: Anonimized checked");
        } catch (Exception sex) {
            log.fatal("DBDealer: failed to check anonymization", sex);
            sex.printStackTrace();
        } finally {
        	CloseableUtils.close(rs);
        	CloseableUtils.close(ps);
        	CloseableUtils.close(con);
        }
        return isAnonymized;
    }

    public boolean isToBeVerified(String aeTitle){
    	Connection con = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null; 
    	
    	try{
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT toVerify FROM KnownNodes WHERE aeTitle=?");
            ps.setString(1, aeTitle);
            rs = ps.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        }catch (Exception e) {
            log.error("Unable to know if " + aeTitle + " is to be verified...", e);
            return false;
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(ps);
        	CloseableUtils.close(con);        	
        }
        
    }
    
    public boolean isPartiallyAnonymizedInstallation(){
        Connection con = null;
    	PreparedStatement ps = null;
        
        try{
        	con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT * FROM PartiallyAnonymized");
            ps.execute();
        } catch (SQLException e) {
            return false;
        }finally{
        	CloseableUtils.close(ps);
        	CloseableUtils.close(con);
        }
        log.warn("####################################################################################");
        log.warn("# WARNING: ALL THE DATA STORED ON THIS INSTALLATION WILL BE PARTIALLY ANONIMIZED!! #");
        log.warn("# WARNING: ALL THE DATA STORED ON THIS INSTALLATION WILL BE PARTIALLY ANONIMIZED!! #");
        log.warn("# WARNING: ALL THE DATA STORED ON THIS INSTALLATION WILL BE PARTIALLY ANONIMIZED!! #");
        log.warn("# WARNING: ALL THE DATA STORED ON THIS INSTALLATION WILL BE PARTIALLY ANONIMIZED!! #");
        log.warn("# WARNING: ALL THE DATA STORED ON THIS INSTALLATION WILL BE PARTIALLY ANONIMIZED!! #");
        log.warn("# WARNING: ALL THE DATA STORED ON THIS INSTALLATION WILL BE PARTIALLY ANONIMIZED!! #");
        log.warn("# WARNING: ALL THE DATA STORED ON THIS INSTALLATION WILL BE PARTIALLY ANONIMIZED!! #");
        log.warn("# WARNING: ALL THE DATA STORED ON THIS INSTALLATION WILL BE PARTIALLY ANONIMIZED!! #");
        log.warn("# WARNING: ALL THE DATA STORED ON THIS INSTALLATION WILL BE PARTIALLY ANONIMIZED!! #");
        log.warn("# WARNING: ALL THE DATA STORED ON THIS INSTALLATION WILL BE PARTIALLY ANONIMIZED!! #");
        log.warn("# WARNING: ALL THE DATA STORED ON THIS INSTALLATION WILL BE PARTIALLY ANONIMIZED!! #");
        log.warn("####################################################################################");
        return true;
    }
    
	public boolean hasToRemovePatientId(String aeTitle) {
		Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
		
		boolean remove = false;
        
        try{
        	con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT removePatientId FROM KnownNodes WHERE aeTitle=?");
            ps.setString(1, aeTitle);
            rs = ps.executeQuery();
            while (rs.next()) {
            	remove = rs.getBoolean(1);
            }
            
        }catch (Exception ex) {
            log.error("DBDealer: Failed to load removePatientId flag",ex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(ps);
        	CloseableUtils.close(con);
        }
        return remove;
	}
	
	public String[] getInstancesInProgress(){
    	Connection con = null;
        CallableStatement cs=null;
        ResultSet rs=null;
        List<String> r=null;
        String[] res=null;
        
        try{
            con = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                cs = con.prepareCall("{call getInstancesInProgress(?)}");
                cs.registerOutParameter(1, OracleTypes.CURSOR);
            } else {
                cs = con.prepareCall("{call getInstancesInProgress()}");
            }
            cs.execute();
            
            
            try {
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(1);
                } else {
                	rs = cs.getResultSet();
                }
            } catch (SQLException sex) {}
            
            if(rs!=null){
            	r=new ArrayList<String>();
            	while(rs.next()){
            		r.add(rs.getString(1));
            	}
            }
            
            
        } catch (Exception ex) {
            log.error("Error in getInstancesInProgress: ", ex);
        }finally{
        	CloseableUtils.close(rs);
        	CloseableUtils.close(cs);
        	CloseableUtils.close(con);
        }
        if ((r != null) && (r.size() > 0)) {
            res = new String[r.size()];
            r.toArray(res);
        }
        return res;
    }
    
    public int clearInstancesInProgress(){
    	Connection con = null;
        CallableStatement cs=null;
        int outcome=0;
        
        try{
            con = dataSource.getConnection();
            cs=con.prepareCall("{call clearInstancesInProgress(?)}");
            cs.registerOutParameter(1, Types.INTEGER);
            cs.execute();
            outcome=cs.getInt(1);
            
        } catch (Exception ex) {
            log.error("Error in clearInstancesInProgress: ", ex);
        }finally{
        	CloseableUtils.close(cs);
        	CloseableUtils.close(con);
        }
        
        return outcome;
    }
    
} 
