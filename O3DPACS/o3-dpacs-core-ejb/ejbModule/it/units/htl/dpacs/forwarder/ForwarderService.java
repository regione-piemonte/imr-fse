/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.forwarder;

import it.units.htl.dpacs.dao.CannotDeleteException;
import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.dao.DeletionDealer;
import it.units.htl.dpacs.dao.DicomMoveDealerRemote;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import java.io.IOException;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

import javax.ejb.CreateException;
import javax.management.MBeanServer;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.Types;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ForwarderService implements ForwarderServiceMBean {
    // SERVICE NAME: Forwarder
    private static final long serialVersionUID = 1L;
    private static final int START_MILLIS_DEFAULT = 25000;
    static final Log log = LogFactory.getLog(ForwarderService.class);
    private Timer t;
    private TimerTask ts;
    private Element configParam = null;
    private DicomMoveDealerRemote bean = null;
    private boolean serviceStatus;

    
    
    
    public ForwarderService(MBeanServer mbs) {
    }

    private void execForward() {
        doForward();
    }

    
    
    private Boolean doForward() {
        log.info("Starting forward process...");
        int forwardTolerance = 0;
        int backProcesses = 0;
        try {
            forwardTolerance = Integer.parseInt(getConfigParam("forwardTolerance"));
            backProcesses = Integer.parseInt(getConfigParam("forwardBackProcesses"));
        } catch (Exception ex) {
            log.error("Unable to retrieve forwardTolerance: Set to 0");
        }
        Connection myCon = null;
        CallableStatement cs = null;
        CallableStatement csUp = null;
        ResultSet rsRemainingStudy = null;
        long currentProcess = -1;
        try {
            if (bean == null) {
                bean = InitialContext.doLookup(BeansName.RMoveDealer);
            }
            myCon = getDatasource().getConnection();
            int activeForwards = getActiveForwardsNumber(myCon);
            if (activeForwards == 0) {
                currentProcess = addForwardProcess(myCon);
                boolean toDelete = (("1".equals(getConfigParam("deleteForwarded"))) ? true : false);
                boolean isOracle = Dbms.isOracle(myCon);
                if (isOracle) {
                    cs = myCon.prepareCall("{call selectStudiesToForward(?,?)}");
                    cs.registerOutParameter(2, OracleTypes.CURSOR);
                } else {
                    cs = myCon.prepareCall("{call selectStudiesToForward(?)}");
                }
                cs.setInt(1, forwardTolerance);
                cs.execute();
                if (isOracle) {
                    rsRemainingStudy = (ResultSet) cs.getObject(2);
                } else {
                    rsRemainingStudy = cs.getResultSet();
                }
                if (rsRemainingStudy != null) {
                    csUp = myCon.prepareCall("{call updateSchedule(?,?,?)}");
                    while (rsRemainingStudy.next()) {
                        String opPK = rsRemainingStudy.getString(1);
                        String UID = rsRemainingStudy.getString(2);
                        String AET = rsRemainingStudy.getString(3);
                        @SuppressWarnings("unused")
                        String aePort = rsRemainingStudy.getString(5);
                        String endConfirmation = rsRemainingStudy.getString(6);
                        int relatedInstances = rsRemainingStudy.getInt(7);
                        log.debug("Sending STUDY: " + UID + "---->" + AET);
                        boolean opResult = CMoveSender.getInstance().cmove(UID, AET, getConfigParam("forwardIp"), getConfigParam("forwardPort"), getConfigParam("forwardAeTitle"), getConfigParam("forwardProtocol"));
                        if (opResult) {
                            if (("C".equals(endConfirmation))) {
                                storageCommit(UID);
                            }
                            csUp.setShort(1, (short) relatedInstances);
                            csUp.setLong(2, currentProcess);
                            csUp.setLong(3, Long.parseLong(opPK));
                            csUp.execute();
                            log.debug("Study forwarded and status updated");
                        } else {
                            log.warn("Not able to forward the study: " + UID);
                        }
                    }
                    try {
                        if (csUp != null)
                            csUp.close();
                    } catch (Exception ex) {
                        log.warn("Unable to close csUp", ex);
                    }
                }
                if (backProcesses > 0)
                    dealWithPreviouslyForwarded(myCon, currentProcess - backProcesses, currentProcess);
                if (toDelete) // Retrieve all studies that can be deleted
                    deleteStudies(myCon, currentProcess, backProcesses);
            } else {
                log.warn("A schedule process seems to be already running");
            }
        } catch (SQLException e) {
            log.error("Couldn't get DB connection.", e);
//            dataSource = null;
            return false;
        } catch (CreateException cex) {
            log.error("", cex);
        } catch (javax.naming.NamingException nex) {
            log.error("", nex);
        } catch (Exception ex) {
            log.error("", ex);
        } finally {
            if (currentProcess >= 0) {
                try {
                    completeForwardProcess(myCon, currentProcess);
                } catch (Exception ex) {
                    log.error("ERROR updating forwardProcess completion date", ex);
                }
            }
            if (rsRemainingStudy != null) {
                try {
                    rsRemainingStudy.close();
                } catch (Exception ex) {
                }
            }
            if (cs != null) {
                try {
                    cs.close();
                } catch (Exception ex) {
                }
            }
            if (myCon != null) {
                try {
                    myCon.close();
                } catch (SQLException e) {
                    log.error("Couldn't close DB connection!", e);
                }
            }
        }
        log.info("Forward finished...");
        return true;
    }

    
    private DataSource getDatasource() {
        try {
            DataSource ds = (DataSource) InitialContext.doLookup("java:/jdbc/dbDS");
            log.debug("Connected to the DB");
            return ds;
        } catch (NamingException nex) {
            log.error("Unable to retrieve DataSource to DB: ", nex);
            return null;
        }
    }
    
    private void dealWithPreviouslyForwarded(Connection con, long lowerProcess, long currentProcess) throws Exception {
        // StringBuilder studiesQuery = new StringBuilder();
        CallableStatement cs = null;
        CallableStatement csUp = null;
        ResultSet rsRemainingStudy = null;
        try {
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                cs = con.prepareCall("{call selectForwardedStudies(?,?)}");
                cs.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                cs = con.prepareCall("{call selectForwardedStudies(?)}");
            }
            cs.setLong(1, lowerProcess);
            cs.execute();
            if (isOracle) {
                rsRemainingStudy = (ResultSet) cs.getObject(2);
            } else {
                rsRemainingStudy = cs.getResultSet();
            }
            if (rsRemainingStudy != null) {
                csUp = con.prepareCall("{call updateForwardedSchedule(?,?,?)}");
                while (rsRemainingStudy.next()) {
                    String opPK = rsRemainingStudy.getString(1);
                    String UID = rsRemainingStudy.getString(2);
                    String AET = rsRemainingStudy.getString(3);
                    String endConfirmation = rsRemainingStudy.getString(6);
                    int relatedInstances = rsRemainingStudy.getInt(7);
                    log.debug("Sending STUDY: " + UID + "---->" + AET);
                    boolean opResult = CMoveSender.getInstance().cmove(UID, AET, getConfigParam("forwardIp"), getConfigParam("forwardPort"), getConfigParam("forwardAeTitle"), getConfigParam("forwardProtocol"));
                    if (opResult) {
                        if (("C".equals(endConfirmation)))
                            storageCommit(UID);
                        csUp.setShort(1, (short) relatedInstances);
                        csUp.setLong(2, currentProcess);
                        csUp.setLong(3, Long.parseLong(opPK));
                        csUp.execute();
                        log.debug("Study forwarded and status updated");
                    } else {
                        log.warn("Not able to forward the study: " + UID);
                    }
                }
                try {
                    csUp.close();
                } catch (Exception ex) {
                    log.warn("Unable to close csUp", ex);
                }
            }
        } catch (Exception ex) {
            log.error("Error checking previously forwarded studies: ", ex);
            throw ex;
        } finally {
            if (rsRemainingStudy != null)
                try {
                    rsRemainingStudy.close();
                } catch (SQLException sex) {
                    log.error("Unable to close resultset");
                }
            if (cs != null)
                try {
                    cs.close();
                } catch (SQLException sex) {
                    log.error("Unable to close callable statement");
                }
        }
    }

    private void storageCommit(String studyUid) {
        try {
            // DTODO: Perform storage Commitment
        } catch (Exception ex) {
        } finally {
        }
    }

    private int getActiveForwardsNumber(Connection con) {
        int result = 0;
        String canActivate = "SELECT COUNT(pk) FROM ScheduleProcesses WHERE finishedOnUtc IS NULL ";
        Statement cas = null;
        try {
            cas = con.createStatement();
            ResultSet cars = cas.executeQuery(canActivate);
            cars.next(); // A ResultSet is always present, since COUNT() always returns something
            result = Integer.parseInt(cars.getString(1));
        } catch (Exception ex) {
            result = 0;
            log.error("Unable to retrieve active forwards number: " + ex.getMessage());
        } finally {
            try {
                cas.close();
            } catch (SQLException sex) {
                log.error("Unable to close activeProcess statement");
            }
        }
        return result;
    }

    private long addForwardProcess(Connection con) throws Exception {
        long result = 0;
        CallableStatement cs = null;
        try {
            cs = con.prepareCall("{call initScheduleProcess(?)}");
            cs.registerOutParameter(1, Types.BIGINT);
            cs.execute();
            result = cs.getLong(1);
        } catch (Exception ex) {
            log.error("Unable to retrieve active forwards number: " + ex.getMessage());
            throw ex;
        } finally {
            try {
                if (cs != null)
                    cs.close();
            } catch (SQLException sex) {
                log.error("Unable to close activeProcess statement");
            }
        }
        return result;
    }

    private boolean completeForwardProcess(Connection con, long processId) throws Exception {
        boolean result = true;
        CallableStatement cs = null;
        try {
            cs = con.prepareCall("{call completeForwardProcess(?)}");
            cs.setLong(1, processId);
            cs.execute();
        } catch (Exception ex) {
            log.error("Unable to complete process " + processId + ": " + ex.getMessage());
            throw ex;
        } finally {
            if (cs != null) {
                try {
                    cs.close();
                } catch (SQLException sex) {
                    log.error("Unable to close completeForwardProcess statement");
                }
            }
        }
        return result;
    }

    private void deleteStudies(Connection con, long currentProcess, int backLimit) {
        CallableStatement css = null;
        ResultSet rsRemainingStudy = null;
        try {
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                css = con.prepareCall("{call selectStudiesToArchive(?,?)}");
                css.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                css = con.prepareCall("{call selectStudiesToArchive(?)}");
            }
            css.setLong(1, currentProcess - backLimit);
            css.execute();
            if (isOracle) {
                rsRemainingStudy = (ResultSet) css.getObject(2);
            } else {
                rsRemainingStudy = css.getResultSet();
            }
            if (rsRemainingStudy != null) {
                DeletionDealer bean = null;
                try {
                    bean = (DeletionDealer) InitialContext.doLookup(BeansName.LDeletionDealer);
                } catch (javax.naming.NamingException nex) {
                    throw new CannotDeleteException("Unable to find DeletionDealer");
                } 
                while (rsRemainingStudy.next()) {
                    String UID = rsRemainingStudy.getString(1);
                    Statement cs = con.createStatement();
                    String checkQuery = "SELECT COUNT(pk) FROM forwardSchedule WHERE forwardedOnUtc IS NULL AND studyFK='" + UID + "'";
                    ResultSet rsCheck = cs.executeQuery(checkQuery);
                    if ((rsCheck != null) && (rsCheck.next()) && (rsCheck.getInt(1) == 0)) {
                        log.debug("Study " + UID + " is to delete");
                        bean.deleteStudy(UID);
                    }
                    try {
                        cs.close();
                    } catch (Exception ex) {
                        log.error("Unable to close SELECT statement (cs)");
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error checking previously forwarded studies: ", ex);
        } finally {
            try {
                if (rsRemainingStudy != null)
                    rsRemainingStudy.close();
            } catch (SQLException sex) {
                log.error("Unable to close selectStudiesToArchive resultSet");
            }
            try {
                if (css != null)
                    css.close();
            } catch (SQLException sex) {
                log.error("Unable to close selectStudiesToArchive statement");
            }
        }
    }

    private boolean loadConfig() {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        try { // Try reading configuration using new approach
            ServicesConfiguration sc = sch.findByServiceName("Forwarder");
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            StringReader reader = new StringReader(sc.getConfiguration());
            InputSource is = new InputSource(reader);
            Document doc = docBuilder.parse(is);
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("root");
            Node _rootNode = nodeLst.item(0);
            configParam = (Element) _rootNode;
        } catch (ParserConfigurationException e) {
            log.error("Unable to parse configuration...", e);
            return false;
        } catch (IOException e) {
            log.error("Couldn't open config file!", e);
            return false;
        } catch (SAXException e) {
            log.error("Couldn't parse config file!", e);
            return false;
        } catch (Exception oex) {
            log.warn("Unable to load the configuration...", oex);
            return false;
        }
        return true;
    }

    private String getConfigParam(String paramName) {
        NodeList fstNmElmntLst = configParam.getElementsByTagName(paramName);
        if ((fstNmElmntLst == null) || (fstNmElmntLst.getLength() == 0))
            return null;
        Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
        NodeList fstNm = fstNmElmnt.getChildNodes();
        return ((Node) fstNm.item(0)).getNodeValue();
    }

    public boolean startService() {
        stopService();
        if (!loadConfig()) {
            log.error("Unable to load configuration!");
            return false;
        }
        t = new Timer("Forwarder", true);
        ts = new TimerTask() {
            public synchronized void run() {
                execForward();
            }
        };
        int period = Integer.parseInt(getConfigParam("forwardPeriod"));
        String startIn = getConfigParam("firstRunAfter");
        int startInMillis = (startIn == null) ? START_MILLIS_DEFAULT : (1000 * Integer.parseInt(startIn));
        t.schedule(ts, startInMillis, period * 1000);
        serviceStatus = true;
        return true;
    }

    public boolean stopService() {
        if (t == null)
            return false;
        t.cancel();
        ts = null;
        t = null;
        serviceStatus = false;
        return true;
    }

    public boolean getStatus() {
        return serviceStatus;
    }
    
    public boolean statusService() {
        return serviceStatus;
    }
    
    public boolean reloadSettings() throws Exception, UnsupportedOperationException {
        return startService();
    }
}
