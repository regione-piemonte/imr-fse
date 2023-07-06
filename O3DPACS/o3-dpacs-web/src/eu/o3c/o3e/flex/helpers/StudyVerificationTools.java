/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.helpers;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.dao.DeprecationRemote;
import it.units.htl.dpacs.exceptions.NoXdsFolder;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.postprocessing.verifier.KosMetaInfo;
import it.units.htl.dpacs.postprocessing.verifier.bean.StudiesVerifierToolsRemote;
import it.units.htl.dpacs.postprocessing.verifier.util.DcmMover;
import it.units.htl.dpacs.postprocessing.verifier.util.PacsEntity;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.naming.ConfigurationException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oracle.jdbc.driver.OracleTypes;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.o3c.o3e.flex.utils.StudyVerificationItem;

public class StudyVerificationTools {
    private static Logger log = Logger.getLogger(StudyVerificationTools.class);
    private StudiesVerifierToolsRemote studyVerToolsBean = null;
    private DeprecationRemote deprecationBean = null;

    public StudyVerificationTools() {
        if (studyVerToolsBean == null) {
            try {
//                studyVerToolsBean = InitialContext.doLookup("o3-dpacs/StudiesVerifierTools/remote");
                studyVerToolsBean = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/StudiesVerifierTools!it.units.htl.dpacs.postprocessing.verifier.bean.StudiesVerifierToolsRemote");
            } catch (NamingException e) {
                log.fatal("Unable to get studies verification tools studyVerToolsBean!!", e);
            }
        }
        if (deprecationBean == null) {
            try {
//                deprecationBean = InitialContext.doLookup("o3-dpacs/DeprecationBean/remote");
                deprecationBean = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DeprecationBean!it.units.htl.dpacs.dao.DeprecationRemote");
            } catch (NamingException e) {
                log.fatal("Unable to get deprecation bean!!", e);
            }
        }
    }

    public boolean removeFromQueue(String studyUID) {
        log.info("Remove this study from verification queue: " + studyUID);
        return studyVerToolsBean.removeStudyFromVerificationQueue(studyUID);
    }

    public boolean addToQueue(String studyUID) {
        log.info("Add this study to verification queue: " + studyUID);
        return studyVerToolsBean.addStudyToVerificationQueue(studyUID);
    }

    public boolean requestCMove(String studyUID, int sourceNodeFk) throws Exception {
        log.info("Request C-MOVE of " + studyUID + " from sourceNodeFk " + sourceNodeFk);
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        DcmMover dcmv = null;
        String prefCallAeTitle = null;
        String newUid = null;
        try {
            con = getConnection();
            ps = con.prepareStatement("SELECT aeTitle,ip,port, prefCallingAet FROM KnownNodes where pk=?");
            ps.setInt(1, sourceNodeFk);
            rs = ps.executeQuery();
            if (!rs.next())
                throw new Exception("No pacs found with pk " + sourceNodeFk);
            StudyStatus st = getRealStudyUid(studyUID);
            if (st.deprecated == false) {
                newUid = deprecationBean.getNewDeprecationUid();
                String usersPk = GlobalConfigurationLoader.getConfigParam("userPkForWebDeprecation");
                int userPk = Integer.parseInt((usersPk != null) ? usersPk : "2");
                log.info("Deprecation before move request, results: " + deprecationBean.deprecateStudy( st.studyUID, newUid, false, "Forced move by StudyVerifier", userPk));
            }else{
                log.info("The study " + studyUID + " is already deprecated, the real UID is  "+ st.studyUID);
            }
            dcmv = new DcmMover(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), 4569);
            prefCallAeTitle = rs.getString(4);
            //taskid:330912 bug:38483
            log.info("requestCMove|Move studyInstanceUID:" +st.studyUID + " destAEtitle:" +prefCallAeTitle);
            dcmv.doMove(st.studyUID, prefCallAeTitle);
        } catch (Exception e) {
            log.error("While reading known node!", e);
            studyVerToolsBean.insertVerificationEvent((newUid!=null)?newUid:studyUID, 0, e.getMessage());
            throw e;
        } finally {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
            if (con != null)
                con.close();
        }
        studyVerToolsBean.insertVerificationEvent(studyUID, 0, "Move done!");
        return true;
    }

    public boolean forceChangeStatus(StudyVerificationItem stv) {
        Connection con = null;
        CallableStatement cs = null;
        CallableStatement updateCS = null;
        ResultSet rs = null;
        log.info("Study Status " + stv.getStudyStatus());
        String singleKosTempURL = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.XDS_SINGLE_KOS_TEMP_URL);
        String kosDirPath = singleKosTempURL + "/" + stv.getStudyUID() + "/";
        try {
            Element e = loadConfigs();
            String xdsPrefix = getConfigParam("attributesPrefix", e);
            if (xdsPrefix != null)
                xdsPrefix = xdsPrefix.trim();
            String getFolderBy = getConfigParam("getFolderByTag", e);
            if (getFolderBy != null) {
                if (!(getFolderBy.equals("AccessionNumber") || !getFolderBy.equals("StudyInstanceUID"))) {
                    getFolderBy = "AccessionNumber";
                }
            } else {
                getFolderBy = "AccessionNumber";
            }
            int queryTimeoutInS = Integer.parseInt(getConfigParam("queryTimeoutInS", e));
            Integer maxConnectionsPerHostParam = Integer.parseInt(getConfigParam("maxConnectionsPerHost", e));
            con = getConnection();
            boolean isOracle = Dbms.isOracle(con);
            switch (stv.getStudyStatus()) {
            case 0:
            case 1:
            case 2:
                if (isOracle) {
                    cs = con.prepareCall("{call getStudyToVerify(?,?)}");
                    cs.registerOutParameter(2, OracleTypes.CURSOR);
                } else {
                    cs = con.prepareCall("{call getStudyToVerify(?)}");
                }
                cs.setString(1, stv.getStudyUID());
                cs.execute();
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(2);
                } else {
                    rs = cs.getResultSet();
                }
                if ((rs != null) && (rs.next())) {
                    PacsEntity pE = new PacsEntity(rs.getString(4), Integer.parseInt(rs.getString(5)), rs.getString(3), rs.getString(6));
                    try {
                        if (studyVerToolsBean.verifyStudy(rs.getString(1), rs.getInt(2), pE, queryTimeoutInS)) {
                            if (cs != null)
                                cs.clearBatch();
                            cs = con.prepareCall("{CALL insertVerifiedDate(?,?)}");
                            cs.setString(1, stv.getStudyUID());
                            cs.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                            cs.execute();
                        } else {
                            studyVerToolsBean.insertVerificationEvent(rs.getString(1), 1, null);
                        }
                    } catch (Exception ee) {
                        log.error("While querying for NumberOfStudyRelatedInstances ", ee);
                        studyVerToolsBean.insertVerificationEvent(rs.getString(1), 2, (ee.getClass() + " " + ee.getMessage()));
                        return false;
                    }
                }
            case 5:
                log.info("Validating study: " + stv.getStudyUID());
                studyVerToolsBean.validateStudy(getFolderBy, stv.getStudyUID(), getFolderBy);
            case 3:
            case 4:
                if (cs != null)
                    cs.clearBatch();
                if (isOracle) {
                    cs = con.prepareCall("{CALL getInstancesOfVerifiedStudy(?,?)}");
                    cs.registerOutParameter(2, OracleTypes.CURSOR);
                } else {
                    cs = con.prepareCall("{CALL getInstancesOfVerifiedStudy(?)}");
                }
                cs.setString(1, stv.getStudyUID());
                cs.execute();
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(2);
                } else {
                    rs = cs.getResultSet();
                }
                Map<String, KosMetaInfo> resultMap = new HashMap<String, KosMetaInfo>();
                if (rs != null) {
                    while (rs.next()) {
                        String studyInstanceUid = rs.getString(4);
                        String serieInstanceUid = rs.getString(5);
                        String accessionNumber = rs.getString(8);
                        String xdsMessageId = rs.getString(9);
                        int attemptsCounter = rs.getInt(10);
                        String[] instance = new String[2];
                        instance[0] = rs.getString(6);
                        instance[1] = rs.getString(7);
                        if (resultMap.containsKey(studyInstanceUid)) {
                            // study already present, but what about the serie?
                            KosMetaInfo kosMetaInfo = resultMap.get(studyInstanceUid);
                            if (kosMetaInfo.getSeries().containsKey(serieInstanceUid)) {
                                // serie already present, add instance
                                // log.info("old study, old serie");
                                List<String[]> serie = kosMetaInfo.getSeries().get(serieInstanceUid);
                                serie.add(instance);
                            } else {
                                // new serie
                                // log.info("old study, new serie");
                                List<String[]> serie = new ArrayList<String[]>();
                                serie.add(instance);
                                kosMetaInfo.getSeries().put(serieInstanceUid, serie);
                            }
                        } else {
                            // new study
                            // log.info("new study");
                            KosMetaInfo kosMetaInfo = new KosMetaInfo();
                            kosMetaInfo.setContentDate(getInstantDateTime("yyyyMMddHH"));
                            kosMetaInfo.setContentTime(getInstantDateTime("yyyyMMddHHmmss"));
                            kosMetaInfo.setPatientId(rs.getString(1));
                            kosMetaInfo.setPatientIdIssuer(rs.getString(2));
                            kosMetaInfo.setPatientName(rs.getString(3));
                            kosMetaInfo.setReferencedStudyInstanceUid(studyInstanceUid);
                            kosMetaInfo.setReferencedAccessionNumber(accessionNumber);
                            kosMetaInfo.setXdsMessageId(xdsMessageId);
                            kosMetaInfo.setAttemptsCounter(attemptsCounter);
                            kosMetaInfo.setRetrieveAETitle("O3-DPACS");
                            List<String[]> serie = new ArrayList<String[]>();
                            serie.add(instance);
                            kosMetaInfo.getSeries().put(serieInstanceUid, serie);
                            resultMap.put(studyInstanceUid, kosMetaInfo);
                        }
                    }
                }
                File dir = new File(kosDirPath);
                if (dir.exists())
                    deleteDirectory(dir);
                dir.mkdirs();
                dir = null;
                studyVerToolsBean.createKosForStudy(resultMap.get(stv.getStudyUID()), singleKosTempURL + "/" + stv.getStudyUID(), stv.getStudyUID());
                File dirFiles = new File(kosDirPath);
                File[] files = dirFiles.listFiles();
                File kosFile = files[0];
                studyVerToolsBean.sendKosToXDS(kosFile, resultMap, getFolderBy, xdsPrefix, maxConnectionsPerHostParam);
            default:
                break;
            }
        } catch (NoXdsFolder e) {
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.error("Something wrong during the forceVerification! ", e);
            return false;
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (cs != null)
                    cs.close();
                if (updateCS != null)
                    updateCS.close();
                if (con != null)
                    con.close();
                File dir = new File(singleKosTempURL + "/" + stv.getStudyUID() + "/");
                if (dir.exists())
                    deleteDirectory(dir);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return true;
    }

    private String getInstantDateTime(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        return dateFormat.format(date);
    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    private String getConfigParam(String paramName, Element configParam) {
        NodeList fstNmElmntLst = configParam.getElementsByTagName(paramName);
        if ((fstNmElmntLst == null) || (fstNmElmntLst.getLength() == 0))
            return null;
        Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
        NodeList fstNm = fstNmElmnt.getChildNodes();
        Node node = fstNm.item(0);
        if (node != null) {
            return node.getNodeValue();
        } else {
            return null;
        }
    }

    private Element loadConfigs() throws ConfigurationException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        try {
            ServicesConfiguration sc = sch.findByServiceName("StudiesVerifier");
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            StringReader reader = new StringReader(sc.getConfiguration());
            InputSource is = new InputSource(reader);
            Document doc = docBuilder.parse(is);
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("configuration");
            Node _rootNode = nodeLst.item(0);
            return (Element) _rootNode;
        } catch (ParserConfigurationException e) {
            log.error("Unable to parse configuration...", e);
        } catch (IOException e) {
            log.error("Couldn't open config file!", e);
        } catch (SAXException e) {
            log.error("Couldn't parse config file!", e);
        } catch (Exception oex) {
            log.warn("Unable to load the configuration...", oex);
        }
        return null;
    }

    private StudyStatus getRealStudyUid(String passedUID) {
        StudyStatus st = new StudyStatus();
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getConnection();
            cs = con.prepareCall("{CALL getRealStudyUID(?,?,?)}");
            cs.setString(1, passedUID);
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.registerOutParameter(3, Types.BIT);
            
            cs.execute();
            st.studyUID = cs.getString(2);
            st.deprecated = cs.getBoolean(3);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cs != null)
                try {
                    cs.close();
                } catch (SQLException e) {
                }
            if (con != null)
                try {
                    con.close();
                } catch (SQLException e) {
                }
        }
        return st;
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

    private class StudyStatus {
        public String studyUID;
        public boolean deprecated;
    }
}
