/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.dao;

import it.units.htl.dpacs.dao.Dbms;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.log4j.Logger;

import eu.o3c.o3e.flex.utils.OperatorDetail;
import eu.o3c.o3e.flex.utils.OperatorStat;

public class DbDealer {
    static Logger log = Logger.getLogger(DbDealer.class);

    public ArrayList<OperatorStat> getOperatorsStats(Date startDate, Date endDate) throws Exception {
        Connection con = null;
        CallableStatement st = null;
        ResultSet rs = null;
        ArrayList<OperatorStat> res = new ArrayList<OperatorStat>();
        try {
            con = getConnection();
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                st = con.prepareCall("{CALL getOperatorsStat(?,?,?)}");
                st.registerOutParameter(3, OracleTypes.CURSOR);
            } else {
                st = con.prepareCall("{CALL getOperatorsStat(?,?)}");
            }
            if (startDate != null) {
                st.setDate(1, new java.sql.Date(startDate.getTime()));
            } else {
                st.setNull(1, Types.DATE);
            }
            if (endDate != null) {
                st.setDate(2, new java.sql.Date(endDate.getTime()));
            } else {
                st.setNull(2, Types.DATE);
            }
            st.execute();
            if (isOracle) {
                rs = (ResultSet) st.getObject(3);
            } else {
                rs = st.getResultSet();
            }
            while (rs.next()) {
                OperatorStat os = new OperatorStat();
                os.setOpeCode(rs.getString(1));
                os.setNumOfseries(rs.getInt(2));
                res.add(os);
            }
            return res;
        } catch (Exception e) {
            log.error("During read OperatorsStat", e);
            throw e;
        } finally {
            try {
                if (con != null)
                    con.close();
                if (st != null)
                    st.close();
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
            }
        }
    }

    public ArrayList<OperatorDetail> getOperatorDetails(String operatorName, Date startDate, Date endDate) throws Exception {
        Connection con = null;
        CallableStatement st = null;
        ResultSet rs = null;
        ArrayList<OperatorDetail> res = new ArrayList<OperatorDetail>();
        try {
            con = getConnection();
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                st = con.prepareCall("{CALL getOperatorStatDetails(?,?,?,?)}");
                st.registerOutParameter(4, OracleTypes.CURSOR);
            } else {
                st = con.prepareCall("{CALL getOperatorStatDetails(?,?,?)}");
            }
            st.setString(1, operatorName);

            if (startDate != null) {
                st.setDate(2, new java.sql.Date(startDate.getTime()));
            } else {
                st.setNull(2, Types.DATE);
            }
            if (endDate != null) {
                st.setDate(3, new java.sql.Date(endDate.getTime()));
            } else {
                st.setNull(3, Types.DATE);
            }

            st.execute();
            if (isOracle) {
                rs = (ResultSet) st.getObject(4);
            } else {
                rs = st.getResultSet();
            }
            while(rs.next()){
                OperatorDetail opDet = new OperatorDetail();
                opDet.setStudyDate(rs.getDate(1));
                opDet.setAccessionNumber(rs.getString(2));
                opDet.setStudyUID(rs.getString(3));
                opDet.setSeriesUID(rs.getString(4));
                opDet.setModality(rs.getString(5));
                opDet.setNumberOfImages(rs.getInt(6));
                opDet.setAeTitle(rs.getString(7));
                res.add(opDet);
            }
            return res;
        }catch (Exception e) {
            log.error("During read OperatorsStat", e);
            throw e;
        } finally {
            try {
                if (con != null)
                    con.close();
                if (st != null)
                    st.close();
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
            }
        }
    }

    private Connection getConnection() throws NamingException, SQLException {
        DataSource ds;
        ds = InitialContext.doLookup("java:/jdbc/dbDS");
        return ds.getConnection();
    }
}