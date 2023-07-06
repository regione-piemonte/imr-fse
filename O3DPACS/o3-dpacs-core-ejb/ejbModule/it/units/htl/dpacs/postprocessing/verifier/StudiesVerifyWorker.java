/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.dao.DeprecationLocal;
import it.units.htl.dpacs.exceptions.NoXdsFolder;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.helpers.LogMessage;
import it.units.htl.dpacs.helpers.MailerSystem;
import it.units.htl.dpacs.postprocessing.verifier.bean.StudiesVerifierToolsLocal;
import it.units.htl.dpacs.postprocessing.verifier.util.DcmMover;
import it.units.htl.dpacs.postprocessing.verifier.util.PacsEntity;
import it.units.htl.dpacs.postprocessing.verifier.util.VerificationItem;

import java.io.File;
import java.io.FileInputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import oracle.jdbc.driver.OracleTypes;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.o3c.xds.core.conv.FormatMapper;
import eu.o3c.xds.core.jaxb.RegistryError;
import eu.o3c.xds.core.jaxb.RegistryResponseType;

public class StudiesVerifyWorker extends TimerTask {
    private static final Object EMAIL_EVENT_NOERRORS = "No errors to notify";
    private Log log = LogFactory.getLog(StudiesVerifyWorker.class);
    private long _threshold;
    private int _queryTimeoutInS;
    private String _emailMessagesLanguage = "en";
    private String _xdsPrefix;
    private String _getFolderBy;
    private int _axisClientMaxConnectionsPerHost = 20;
    private StudiesVerifierToolsLocal studiesVerifierTools;
    private DeprecationLocal deprecationBean = null;
    Map<String, KosMetaInfo> kosMetaInfoMap = null;
    private Date reportTime = null;
    private Date lastExecutionTime = null;

    public void setReportTime(Date reportTime) {
        this.reportTime = reportTime;
    }

    public StudiesVerifyWorker(long thresholdInS, String emailMessagesLanguage, int queryTimeoutInS, String xdsPrefix, String getFolderBy) {
        _threshold = thresholdInS;
        _emailMessagesLanguage = emailMessagesLanguage;
        _queryTimeoutInS = queryTimeoutInS;
        _xdsPrefix = xdsPrefix;
        _getFolderBy = getFolderBy;
        if (studiesVerifierTools == null) {
            try {
                studiesVerifierTools = InitialContext.doLookup(BeansName.StudiesVerifierToolsL);
            } catch (NamingException nex) {
                log.error("Unable to create DicomDbDealer...", nex);
            }
        }
        if (deprecationBean == null) {
            try {
                deprecationBean = InitialContext.doLookup("java:global/o3-dpacs-ear/o3-dpacs-core-ejb/DeprecationBean!it.units.htl.dpacs.dao.DeprecationLocal");
            } catch (NamingException e) {
                log.fatal("Unable to get deprecation bean!!", e);
            }
        }
        lastExecutionTime = new Date();
    }

    public void setAxisClientMaxConnections(int maxConnectionsPerHost) {
        this._axisClientMaxConnectionsPerHost = maxConnectionsPerHost;
    }

    public synchronized void run() {
        try {
            log.info("Studies verifier tools is null?" + (studiesVerifierTools == null));
            String kosTempUrl = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.XDS_KOS_TEMP_URL);
            log.info("Executing Study Verifier task...");
            log.info("Cleaning kosTempFolder");
            emptyDirectory(kosTempUrl);
            // converted
            log.info("Verifying pending studies");
            verifyPendingStudies();
            // converted
            log.info("Validating verified studies");
            validateVerifiedStudies(_getFolderBy);
            // converted
            log.info("Creating kos files");
            createDicomKosFiles(kosTempUrl);
            log.info("Sending kos to xds repo!");
            sendAll(kosTempUrl);
            log.info("Study Verifier task finished.");
            lastExecutionTime = new Date();
        } catch (RuntimeException runtime) {
            log.error("Runtime Exception in Study Verifier Task", runtime);
        }
    }

    /**
     * This task checks the presence of the specified needed attribute for each verified studies
     */
    private void validateVerifiedStudies(String mandatoryAttribute) {
        try {
            studiesVerifierTools.validateStudy(mandatoryAttribute, null, _getFolderBy);
        } catch (Exception e) {
            log.error("Unable to verify studies.", e);
        }
    }

    private void verifyPendingStudies() {
        HashMap<String, String[]> unverifiedStudy = new HashMap<String, String[]>();
        StringBuilder mailBodyParam = new StringBuilder();
        Connection con = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                cs = con.prepareCall("{call getStudiesToVerify(?,?)}");
                cs.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                cs = con.prepareCall("{call getStudiesToVerify(?)}");
            }
            cs.setTimestamp(1, new Timestamp(System.currentTimeMillis() - _threshold * 1000));
            cs.execute();
            if (isOracle) {
                rs = (ResultSet) cs.getObject(2);
            } else {
                rs = cs.getResultSet();
            }
            ArrayList<VerificationItem> items = getVerificationItem(rs);
            if (items.size() == 0)
                log.debug("Nothing to verify");
            ArrayList<VerificationItem> retryItem = new ArrayList<VerificationItem>();
            for (VerificationItem item : items) {
                try {
                    if (studiesVerifierTools.verifyStudy(item.getStudyUid(), item.getNumOfStudyRelatedInstances(), item.getSource(), _queryTimeoutInS)) {
                        CallableStatement updateCS = con.prepareCall("{CALL insertVerifiedDate(?,?)}");
                        updateCS.setString(1, item.getStudyUid());
                        updateCS.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                        updateCS.execute();
                    } else {
                        unverifiedStudy.put(item.getStudyUid(), new String[] { item.getSource().getDicomUrl(), "Different Image Number" });
                        studiesVerifierTools.insertVerificationEvent(item.getStudyUid(), 1, null);
                        retryItem.add(item);
                    }
                } catch (Exception e) {
                    log.error("While querying for NumberOfStudyRelatedInstances ", e);
                    studiesVerifierTools.insertVerificationEvent(item.getStudyUid(), 2, (e.getClass() + " " + e.getMessage()));
                    unverifiedStudy.put(item.getStudyUid(), new String[] { item.getSource().getDicomUrl(), e.getClass() + " " + e.getMessage() });
                }
            }
            for (VerificationItem item : retryItem) {
                String newUid = deprecationBean.getNewDeprecationUid();
                String usersPk = GlobalConfigurationLoader.getConfigParam("userPkForWebDeprecation");
                int userPk = Integer.parseInt((usersPk != null) ? usersPk : "2");
                try {
                    log.info("Deprecation before move request, results: " + deprecationBean.deprecateStudy(item.getStudyUid(), newUid, false, "Forced move by StudyVerifierWorker", userPk));
                    DcmMover dcmv = new DcmMover(item.getSource().getRemoteAeTitle(), item.getSource().getRemoteIp(), item.getSource().getRemotePort(), item.getSource().getLocalAeTitle(), 4569);
                    //taskid:330912 bug:38483
                    log.info("verifyPendingStudies|Move studyInstanceUID:" +item.getStudyUid() + " destAEtitle:" +item.getSource().getLocalAeTitle());
                    dcmv.doMove(item.getStudyUid(), item.getSource().getLocalAeTitle());
                    cs = con.prepareCall("{CALL getStudyNumberOfImages(?,?)}");
                    cs.setString(1, item.getStudyUid());
                    cs.registerOutParameter(2, Types.INTEGER);
                    cs.execute();
                    log.debug("This study had " + item.getNumOfStudyRelatedInstances() + " and  now  it has" + cs.getInt(2));
                    item.setNumOfStudyRelatedInstances(cs.getInt(2));
                    if (studiesVerifierTools.verifyStudy(item.getStudyUid(), item.getNumOfStudyRelatedInstances(), item.getSource(), _queryTimeoutInS)) {
                        CallableStatement updateCS = con.prepareCall("{CALL insertVerifiedDate(?,?)}");
                        updateCS.setString(1, item.getStudyUid());
                        updateCS.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                        updateCS.execute();
                    } else {
                        unverifiedStudy.put(item.getStudyUid(), new String[] { item.getSource().getDicomUrl(), "Different Image Number" });
                        studiesVerifierTools.insertVerificationEvent(item.getStudyUid(), 1, null);
                    }
                } catch (Exception e) {
                    log.error("While querying for NumberOfStudyRelatedInstances ", e);
                    studiesVerifierTools.insertVerificationEvent(item.getStudyUid(), 2, (e.getClass() + " " + e.getMessage()));
                    unverifiedStudy.put(item.getStudyUid(), new String[] { item.getSource().getDicomUrl(), e.getClass() + " " + e.getMessage() });
                }
            }
            boolean sendEmail = false;
            if (reportTime == null) {
                sendEmail = true;
            } else {
                Calendar now = new GregorianCalendar();
                Calendar lastExecution = new GregorianCalendar();
                lastExecution.setTime(lastExecutionTime);
                Calendar whenToSend = new GregorianCalendar();
                whenToSend.setTime(reportTime);
                whenToSend.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));
                sendEmail = (((now.after(whenToSend)) || (now.equals(whenToSend))) && (whenToSend.after(lastExecution)));
            }
            if (sendEmail || (!unverifiedStudy.isEmpty())) {
                if (!unverifiedStudy.isEmpty()) {
                    for (String key : unverifiedStudy.keySet()) {
                        log.warn(key + " on " + unverifiedStudy.get(key)[0] + " due to " + unverifiedStudy.get(key)[1]);
                        if (sendEmail)
                            mailBodyParam.append(key + "; " + unverifiedStudy.get(key)[0] + "; " + unverifiedStudy.get(key)[1] + "\n");
                    }
                } else if (reportTime != null) {
                    mailBodyParam.append(EMAIL_EVENT_NOERRORS);
                }
            }
            if (mailBodyParam.length() != 0) {
                MailerSystem.send(ConfigurationSettings.EMAIL_EVENT_STUDYVERIFIER, _emailMessagesLanguage, new String[] { mailBodyParam.toString() });
            }
        } catch (SQLException e) {
            log.error("", e);
        } catch (NamingException e) {
            log.error("", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sex) {
                    log.warn(LogMessage._ConnectionNotClose, sex);
                }
            }
            if (cs != null) {
                try {
                    cs.close();
                } catch (SQLException sex) {
                    log.warn(LogMessage._ConnectionNotClose, sex);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException sex) {
                    log.warn(LogMessage._ConnectionNotClose, sex);
                }
            }
        }
    }

    private void createDicomKosFiles(String kosTempUrl) {
        log.debug("Let's create dicom kos files from verified studies...");
        this.kosMetaInfoMap = null;
        CallableStatement verifiedCs = null;
        Connection con = null;
        try {
            con = getConnection();
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                verifiedCs = con.prepareCall("{CALL getVerifiedStudiesInstances(?)}");
                verifiedCs.registerOutParameter(1, OracleTypes.CURSOR);
            } else {
                verifiedCs = con.prepareCall("{CALL getVerifiedStudiesInstances()}");
            }
            verifiedCs.execute();
            ResultSet verifiedRs = null;
            if (isOracle) {
                verifiedRs = (ResultSet) verifiedCs.getObject(1);
            } else {
                verifiedRs = verifiedCs.getResultSet();
            }
            this.kosMetaInfoMap = parseResultSet(verifiedRs);
            if (this.kosMetaInfoMap != null) {
                Set<String> keys = this.kosMetaInfoMap.keySet();
                for (String studyInstanceUid : keys) {
                    KosMetaInfo kosMetaInfo = this.kosMetaInfoMap.get(studyInstanceUid);
                    studiesVerifierTools.createKosForStudy(kosMetaInfo, kosTempUrl, studyInstanceUid);
                }
            }
        } catch (SQLException e) {
            log.error("", e);
        } catch (NamingException e) {
            log.error("", e);
        } catch (Exception e) {
            log.error("Unable to create kos file for study");
            // add log on the DB
        } finally {
            try {
                con.close();
            } catch (SQLException sex) {
                log.warn(LogMessage._ConnectionNotClose, sex);
            }
        }
    }

    /**
     * Send all the files present in kosDir directory to the XDS repository at the xdsRepositoryUrl
     * 
     * @param kosDir
     *            the directory with kos files to send
     * @param xdsSourceId
     *            the id of the Pacs as an Xds Source Actor
     * @param xdsRepositoryUrl
     *            the url of the Xds Repository
     */
    private void sendAll(String kosDir) {
        /* Watch the kos directory for any kos file to be sent */
        File dir = new File(kosDir);
        File[] files = dir.listFiles();
        log.info(files.length + " kos files to send");
        for (File file : files) {
            if (file.isFile()) {
                try {
                    studiesVerifierTools.sendKosToXDS(file, kosMetaInfoMap, _getFolderBy, _xdsPrefix, _axisClientMaxConnectionsPerHost);
                } catch (NoXdsFolder noex) {
                    log.warn(noex.getMessage());
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
    }

    /**
     * Delete any file present in the directory
     * 
     * @param dirName
     */
    private void emptyDirectory(String dirName) {
        File dir = new File(dirName);
        File[] files = dir.listFiles();
        log.debug("deleting " + files.length + " files in " + dirName);
        for (File file : files) {
            if (file.isFile()) {
                boolean isDeleted = file.delete();
                if (!isDeleted) {
                    log.debug("couldnt' delete file: " + file.getName());
                }
            }
        }
    }

    private ArrayList<VerificationItem> getVerificationItem(ResultSet rs) throws SQLException {
        ArrayList<VerificationItem> res = new ArrayList<VerificationItem>();
        while (rs.next()) {
            VerificationItem ve = new VerificationItem();
            PacsEntity pE = new PacsEntity(rs.getString(4), Integer.parseInt(rs.getString(5)), rs.getString(3), rs.getString(6));
            ve.setSource(pE);
            ve.setStudyUid(rs.getString(1));
            ve.setNumOfStudyRelatedInstances(rs.getInt(2));
            res.add(ve);
        }
        return res;
    }

    /**
     * Parse the ResultSet retrieved by the getVerifiedStudiesInstances Stored Procedure
     * 
     * @param rs
     *            the ResultSet retrieved by getVerifiedStudiesInstances Stored Procedure
     * @return a Map with many KosMetaInfo as the different studies found in the ResultSet. The key is the studyInstanceUid.
     * @throws SQLException
     */
    private Map<String, KosMetaInfo> parseResultSet(ResultSet rs) throws SQLException {
        log.debug("parsing found verified studies");
        if ((rs != null) && (rs.next())) {
            Map<String, KosMetaInfo> resultMap = new HashMap<String, KosMetaInfo>();
            do {
                String studyInstanceUid = rs.getString(4);
                String serieInstanceUid = rs.getString(5);
                String accessionNumber = rs.getString(8);
                String xdsMessageId = rs.getString(9);
                int attemptsCounter = rs.getInt(10);
                String[] instance = new String[2];
                instance[0] = rs.getString(6); // sopInstanceUid
                instance[1] = rs.getString(7); // sopClassUid
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
            } while (rs.next());
            log.debug("found " + resultMap.size() + " different verified studies");
            return resultMap;
        } else {
            log.debug("no verified studies");
            return null;
        }
    }

    /**
     * Perform an XDS GetFolders query passing the folder.uniqueId parameter
     * 
     * @param uniqueId
     * @return folder.entryUuid
     * @throws AxisFault
     * @throws JAXBException
     *             in case of errors parsing the received response xml
     */
    // private String getXdsFolderUuid(String uniqueId, String registryUrl) throws AxisFault, JAXBException, Exception {
    // log.debug("querying XDS registry for folder with uniqueId: " + uniqueId);
    // AdhocQueryRequestMessageBuilder builder = new AdhocQueryRequestMessageBuilder();
    // builder.setQueryId(StoredQueryIds.GET_FOLDERS);
    // builder.setReturnType("ObjectRef");
    // builder.addQueryParameter("$XDSFolderUniqueId", uniqueId);
    // AdhocQueryRequest request = builder.getMessage();
    // FormatMapper conv = new FormatMapper();
    // OMElement payload = null;
    // try {
    // payload = conv.toOMElement(request);
    // } catch (JAXBException e) {
    // // this would be a bug in AdhocQueryRequestMessageBuilder
    // e.printStackTrace();
    // }
    // XdsClient client = new XdsClient(registryUrl);
    // client.setWsAction(XdsClient.WS_ACTION_REGISTRY_STORED_QUERY);
    // client.setMtomEnabled(false);
    // client.setMaxConnectionsPerHost(this._axisClientMaxConnectionsPerHost);
    // OMElement response = client.send(payload);
    // AdhocQueryResponse queryResp = null;
    // queryResp = conv.toAdhocQueryResponse(response);
    // List<JAXBElement<? extends IdentifiableType>> objectList = queryResp.getRegistryObjectList().getIdentifiable();
    // String folderEntryUuid = null;
    // for (JAXBElement<? extends IdentifiableType> jaxbElement : objectList) {
    // if (jaxbElement.getName().getLocalPart().equals("ObjectRef")) {
    // folderEntryUuid = jaxbElement.getValue().getId();
    // }
    // }
    // return folderEntryUuid;
    // }
    /**
     * @param format
     *            "yyyyMMdd" for Date or "yyyyMMddHHmmss" for Time
     */
    private String getInstantDateTime(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        return dateFormat.format(date);
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

    public static void main(String[] args) {
        // String folderUniqueId = "1.3.76.13.21180.2.20090305231618.1427620.3";
        // String registryUrl = "http://localhost:3333/tf6/services/xdsregistryb";
        // StudiesVerifyWorker w = new StudiesVerifyWorker(0, "", 1, 10000, "RISTO1", "");
        // try {
        // System.out.println("Sending query to: " + registryUrl);
        // String uuid = w.getXdsFolderUuid(folderUniqueId, registryUrl);
        // System.out.println("response: " + uuid);
        // } catch (AxisFault e) {
        // e.printStackTrace();
        // } catch (JAXBException e) {
        // e.printStackTrace();
        // }
        try {
            System.out.println("start..");
            File file = new File("D:/errore-xds.xml");
            FileInputStream fis = new FileInputStream(file);
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader reader = xif.createXMLStreamReader(fis);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement response = builder.getDocumentElement();
            FormatMapper fm = new FormatMapper();
            RegistryResponseType rrt = fm.toRegistryResponseType(response);
            Iterator<RegistryError> i = rrt.getRegistryErrorList().getRegistryError().iterator();
            while (i.hasNext()) {
                RegistryError error = i.next();
                System.out.println("errorCode: " + error.getErrorCode());
                System.out.println("codeContext: " + error.getCodeContext());
                boolean c = error.getCodeContext().contains("123.456.123");
                System.out.println("Contiene: " + c);
            }
            // OMElement errorList = response.getFirstChildWithName(new QName("RegistryErrorList"));
            // Iterator<?> i = errorList.getChildrenWithLocalName("RegistryError");
            // while (i.hasNext()) {
            // OMElement error = (OMElement) i.next();
            // String val = error.getAttributeValue(new QName("errorCode"));
            // System.out.println("errorCode: " + val);
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
