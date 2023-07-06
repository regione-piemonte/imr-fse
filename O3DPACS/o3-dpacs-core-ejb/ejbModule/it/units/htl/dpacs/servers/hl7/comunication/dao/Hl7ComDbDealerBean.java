/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7.comunication.dao;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.servers.hl7.comunication.utils.Hl7Message;
import it.units.htl.dpacs.servers.hl7.comunication.utils.Subscriber;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.jboss.logging.MDC;
import org.apache.log4j.MDC;

@Stateless
public class Hl7ComDbDealerBean implements Hl7ComDbDealerLocal {
    private @Resource(name = "java:/jdbc/Hl7CommServerDS")
    DataSource dataSource;
    private Log log = LogFactory.getLog(Hl7ComDbDealerBean.class);

    public Hl7ComDbDealerBean() throws Exception {
        dataSource = InitialContext.doLookup("java:/jdbc/Hl7CommServerDS");
    }
    @Override
    public void setContext(String name) {
        MDC.put("queueConsumer", name);
    }
    
    @Override
    public void removeContext() {
        MDC.remove("queueConsumer");
    }
    
    
    @Override
    public ArrayList<Subscriber> getSubscribers() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<Subscriber> subscribers = new ArrayList<Subscriber>();
        try {
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT * FROM Hl7Nodes");
            rs = ps.executeQuery();
            while (rs.next()) {
                Subscriber sb = new Subscriber();
                sb.pk = rs.getLong(1);
                sb.name = rs.getString(2);
                sb.address = rs.getString(3);
                sb.configuration = rs.getString(4);
                subscribers.add(sb);
            }
            log.info("Found " + subscribers.size() + " subscribers.");
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return subscribers;
    }

    @Override
    public ArrayList<Hl7Message> getMessageForSubscriber(long pk) throws SQLException {
        Connection con = null;
        ResultSet rs = null;
        CallableStatement cs = null;
        ArrayList<Hl7Message> res = new ArrayList<Hl7Message>();
        try {
            con = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                cs = con.prepareCall("{call getMessagesForSubscriber(?,?)}");
                cs.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                cs = con.prepareCall("{call getMessagesForSubscriber(?)}");
            }
            cs.setLong(1, pk);
            cs.execute();
            try {
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(2);
                } else {
                    rs = cs.getResultSet();
                }
            } catch (SQLException sex) {
            }
            if (rs != null) {
                while (rs.next()) {
                    Hl7Message sms = new Hl7Message();
                    sms.pk = rs.getLong(1);
                    sms.message = rs.getString(2);
                    sms.retries = rs.getInt(4);
                    res.add(sms);
                }
            }
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
        return res;
    }

    @Override
    public void updateRetries(Hl7Message sms, long nodePk)  {
        Connection con = null;
        ResultSet rs = null;
        CallableStatement cs = null;
        try {
            con = dataSource.getConnection();
            cs = con.prepareCall("{call updateHl7MessageRetries(?,?,?)}");
            cs.setLong(1, nodePk);
            cs.setLong(2, sms.pk);
            if(sms.retries == null){
                cs.setNull(3, Types.BIGINT);
            }else{
                cs.setLong(3, sms.retries);
            }
            
            cs.execute();
        } catch (SQLException e) {
           log.warn("Unable to update the retries for node/msg: " + nodePk+"/" + sms.pk);
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(cs);
            CloseableUtils.close(con);
        }
    }
}
