/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier.bean;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.exceptions.NoXdsFolder;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.helpers.LogMessage;
import it.units.htl.dpacs.helpers.MailerSystem;
import it.units.htl.dpacs.postprocessing.verifier.KosBuilder;
import it.units.htl.dpacs.postprocessing.verifier.KosMetaInfo;
import it.units.htl.dpacs.postprocessing.verifier.XdsClient;
import it.units.htl.dpacs.postprocessing.verifier.XdsMessageCreator;
import it.units.htl.dpacs.postprocessing.verifier.util.DcmCommonObject.QueryRetrieveLevel;
import it.units.htl.dpacs.postprocessing.verifier.util.DcmQuerier;
import it.units.htl.dpacs.postprocessing.verifier.util.PacsEntity;
import it.units.htl.dpacs.postprocessing.verifier.util.StudyVerificationEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;

import eu.o3c.xds.core.conv.FormatMapper;
import eu.o3c.xds.core.jaxb.AdhocQueryRequest;
import eu.o3c.xds.core.jaxb.AdhocQueryResponse;
import eu.o3c.xds.core.jaxb.IdentifiableType;
import eu.o3c.xds.core.jaxb.ProvideAndRegisterDocumentSetRequestType;
import eu.o3c.xds.core.jaxb.RegistryError;
import eu.o3c.xds.core.jaxb.RegistryResponseType;
import eu.o3c.xds.core.messages.AdhocQueryRequestMessageBuilder;
import eu.o3c.xds.core.messages.StoredQueryIds;

@Stateless
public class StudiesVerifierTools implements StudiesVerifierToolsLocal, StudiesVerifierToolsRemote {
    private Log log = LogFactory.getLog(StudiesVerifierTools.class);

    public StudiesVerifierTools() {
    }

    @Override
    public boolean verifyStudy(String studyInstanceUID, Integer numberOfStoredInstance, PacsEntity sourcePacs, Integer timeout) throws Exception {
        DcmQuerier querier = new DcmQuerier(sourcePacs.getRemoteAeTitle(), sourcePacs.getRemoteIp(), sourcePacs.getRemotePort()
                , sourcePacs.getLocalAeTitle(), 2350, timeout);
        HashMap<Integer, String> filterKey = new HashMap<Integer, String>();
        filterKey.put(Tag.StudyInstanceUID, studyInstanceUID);
        List<DicomObject> res = null;
        //taskid:330912 bug:3848
        log.info("verifyStudy|Filter studyInstanceUID:" +studyInstanceUID);
        res = querier.doQuery(filterKey, QueryRetrieveLevel.STUDY);
        if (res != null) {
            for (DicomObject study : res) {
                if (study.getInt(Tag.NumberOfStudyRelatedInstances) == numberOfStoredInstance) {
                    return true;
                } else {
                    log.info("This study isn't arrived all yet. StudyUID = " + studyInstanceUID);
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean validateStudy(String mandatoryAttribute, String studyUID, String _getFolderBy) throws Exception {
        String sql = "SELECT std.studyInstanceUid FROM Studies std " +
                "INNER JOIN StudiesToVerify stv ON std.studyInstanceUid = stv.studyFk " +
                "WHERE stv.verifiedDate IS NOT NULL AND " +
                "(std.??? IS NULL OR std.??? = '') ";
        sql = sql.replace("???", mandatoryAttribute);
        if (studyUID != null) {
            sql.concat(" AND std.studyInstanceUid = '" + studyUID + "'");
        }
        Connection con = null;
        Statement st = null;
        PreparedStatement stDel = null;
        ResultSet rs = null;
        List<String> invalidStudies = new ArrayList<String>(0);
        try {
            con = getConnection();
            st = con.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                String studyInstanceUid = rs.getString(1);
                removeStudyFromVerificationQueue(studyInstanceUid);
                invalidStudies.add(studyInstanceUid);
            }
        } catch (SQLException e) {
            log.error("Unable to get invalid studies", e);
        } catch (NamingException e) {
            log.error("Unable to get invalid studies", e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (stDel != null)
                    stDel.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
            }
        }
        if (!invalidStudies.isEmpty()) {
            Iterator<String> it = invalidStudies.iterator();
            String studies = "";
            while (it.hasNext()) {
                String name = it.next();
                studies = studies.concat(name);
                if (it.hasNext()) {
                    studies = studies.concat(", ");
                }
                insertVerificationEvent(name, StudyVerificationEvent.STUDY_NOT_VALID, null);
            }
            MailerSystem.send(ConfigurationSettings.EMAIL_EVENT_REMOVEDVERIFIEDSTUDIES, new String[] { _getFolderBy, studies });
        } else {
            return true;
        }
        return false;
    }

    @Override
    public boolean createKosForStudy(KosMetaInfo kosMetaInfo, String kosTempUrl, String studyUID) throws Exception {
        KosBuilder kosBuilder = new KosBuilder();
        DicomObject kos = kosBuilder.build(kosMetaInfo);
        try {
            kosBuilder.store(kos, kosTempUrl);
        } catch (FileNotFoundException e) {
            log.error("", e);
            insertVerificationEvent(studyUID, StudyVerificationEvent.KOS_CREATION_EXCEPTION, e.getMessage());
            throw e;
        }
        return true;
    }

    @Override
    public boolean sendKosToXDS(File kos, Map<String, KosMetaInfo> kosMetaInfoMap, String getFolderBy, String xdsPrefix, Integer maxConnectionPerHost) throws Exception {
        String refStudyInstanceUid = null;
        try {
            String xdsSourceID = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.XDS_SOURCE_ID);
            String xdsRegistryUrl = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.XDS_REGISTRY_URL);
            String xdsRepositoryUrl = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.XDS_REPOSITORY_URL);
            XdsMessageCreator xmc = new XdsMessageCreator(xdsSourceID);
            XdsClient sender = new XdsClient(xdsRepositoryUrl);
            sender.setWsAction(XdsClient.WS_ACTION_PROVIDE_AND_REGISTER_DOCUMENT_SET);
            sender.setMtomEnabled(true);
            sender.setMaxConnectionsPerHost(maxConnectionPerHost);
            FormatMapper conv = new FormatMapper();
            DicomInputStream dis = new DicomInputStream(kos);
            DicomObject obj = dis.readDicomObject();
            dis.close();
            DicomObject refReqSeq = obj.get(Tag.CurrentRequestedProcedureEvidenceSequence).getDicomObject();
            refStudyInstanceUid = refReqSeq.getString(Tag.StudyInstanceUID);
            String xdsQueryParameter = refReqSeq.getString(Tag.forName(getFolderBy));
            if (xdsPrefix != null && (xdsPrefix.length() != 0)) {
                xdsQueryParameter = xdsPrefix.concat(xdsQueryParameter);
            }
            String folderUuid = getXdsFolderUuid(xdsQueryParameter, xdsRegistryUrl, maxConnectionPerHost);
            if (folderUuid != null) {
                KosMetaInfo kosMetaInfo = kosMetaInfoMap.get(refStudyInstanceUid);
                ProvideAndRegisterDocumentSetRequestType pnr = xmc.createMessage(kos, folderUuid, xdsPrefix, kosMetaInfo.getXdsMessageId());
                if (pnr != null) {
                    OMElement payload = conv.toOMElement(pnr);
                    OMElement repositoryResponse = sender.send(payload);
                    if (repositoryResponse != null) {
                        RegistryResponseType rrt = conv.toRegistryResponseType(repositoryResponse);
                        String status = rrt.getStatus();
                        if (status.endsWith("Success")) {
                            insertEndJob(refStudyInstanceUid);
                            return true;
                        } else {
                            Iterator<RegistryError> errorIterator = rrt.getRegistryErrorList().getRegistryError().iterator();
                            boolean isDuplicateSubsetUniqueId = false;
                            while (errorIterator.hasNext()) {
                                RegistryError error = errorIterator.next();
                                log.warn("Xds Repository error: " + error.getErrorCode() + "; " + error.getCodeContext());
                                if (error.getErrorCode().equalsIgnoreCase("XDSDuplicateUniqueIdInRegistry")) {
                                    if (error.getCodeContext().contains(kosMetaInfo.getXdsMessageId())) {
                                        // duplicate unique id (duplicate subset.uniqueId)
                                        isDuplicateSubsetUniqueId = true;
                                    }
                                }
                            }
                            if (isDuplicateSubsetUniqueId) {
                                // check number of retries
                                if (kosMetaInfo.getAttemptsCounter() < 1) {
                                    // first failure: log error
                                    log.error("duplicate submissionSet uniqueId at first attempt: " + kosMetaInfo.getXdsMessageId());
                                    log.error("problematic verified study: " + refStudyInstanceUid);
                                    incrementAttemptsCounter(refStudyInstanceUid, kosMetaInfo.getAttemptsCounter());
                                    throw new Exception("duplicate submissionSet uniqueId at first attempt: " + kosMetaInfo.getXdsMessageId());
                                } else {
                                    // iterated failure: warn and remove from queue
                                    log.warn("iterated duplicate submissionSet uniqueId error for id: " + kosMetaInfo.getXdsMessageId());
                                    log.warn("problematic verified study: " + refStudyInstanceUid + " This was the attempt number: " + kosMetaInfo.getAttemptsCounter() + 1);
                                    log.warn("removing verified study from the queue");
                                    removeStudyFromVerificationQueue(refStudyInstanceUid);
                                    throw new Exception("iterated duplicate submissionSet uniqueId error for id: " + kosMetaInfo.getXdsMessageId());
                                }
                            } else {
                                log.warn("Xds Repository refused the kos");
                                throw new Exception("Xds Repository refused the kos");
                            }
                        }
                    } else {
                        log.warn("Null response from xds repository");
                        throw new Exception("Null response from xds repository");
                    }
                } else {
                    log.error("Cannot build the xds message to send!");
                    throw new Exception("Cannot build the xds message to send!");
                }
            } else {
                throw new NoXdsFolder("No folder for " + xdsQueryParameter);
            }
        } catch (Exception e) {
            insertVerificationEvent(refStudyInstanceUid, StudyVerificationEvent.KOS_NOT_SENT, e.getClass() + " " + e.getMessage());
            throw e;
        }
        // unreachable place
    }

    private void incrementAttemptsCounter(String studyInstanceUid, int oldValue) {
        Connection con = null;
        Statement st = null;
        int newValue = oldValue + 1;
        try {
            con = getConnection();
            st = con.createStatement();
            st.executeUpdate("UPDATE StudiesToVerify SET attemptsCounter = " + newValue + " WHERE studyFK = '" + studyInstanceUid + "'");
        } catch (SQLException e) {
            log.error("", e);
        } catch (NamingException e) {
            log.error("", e);
        } finally {
            try {
                st.close();
            } catch (SQLException sex) {
                log.warn(LogMessage._ClosingRsOrStat, sex);
            }
            try {
                con.close();
            } catch (SQLException sex) {
                log.warn(LogMessage._ConnectionNotClose, sex);
            }
        }
    }

    private String getXdsFolderUuid(String uniqueId, String registryUrl, Integer axisClientMaxConnPerHost) throws AxisFault, JAXBException, Exception {
        log.debug("querying XDS registry for folder with uniqueId: " + uniqueId);
        AdhocQueryRequestMessageBuilder builder = new AdhocQueryRequestMessageBuilder();
        builder.setQueryId(StoredQueryIds.GET_FOLDERS);
        builder.setReturnType("ObjectRef");
        builder.addQueryParameter("$XDSFolderUniqueId", uniqueId);
        AdhocQueryRequest request = builder.getMessage();
        FormatMapper conv = new FormatMapper();
        OMElement payload = null;
        try {
            payload = conv.toOMElement(request);
        } catch (JAXBException e) {
            // this would be a bug in AdhocQueryRequestMessageBuilder
            e.printStackTrace();
        }
        XdsClient client = new XdsClient(registryUrl);
        client.setWsAction(XdsClient.WS_ACTION_REGISTRY_STORED_QUERY);
        client.setMtomEnabled(false);
        client.setMaxConnectionsPerHost(axisClientMaxConnPerHost);
        OMElement response = client.send(payload);
        AdhocQueryResponse queryResp = null;
        queryResp = conv.toAdhocQueryResponse(response);
        List<JAXBElement<? extends IdentifiableType>> objectList = queryResp.getRegistryObjectList().getIdentifiable();
        String folderEntryUuid = null;
        for (JAXBElement<? extends IdentifiableType> jaxbElement : objectList) {
            if (jaxbElement.getName().getLocalPart().equals("ObjectRef")) {
                folderEntryUuid = jaxbElement.getValue().getId();
            }
        }
        return folderEntryUuid;
    }

    public void insertVerificationEvent(String studyUID, Integer eventType, String eventDescriprion) {
        Connection con = null;
        CallableStatement cs = null;
        try {
            con = getConnection();
            cs = con.prepareCall("{call insertVerificationEvent(?,?,?)}");
            cs.setString(1, studyUID);
            cs.setInt(2, eventType);
            if (eventDescriprion != null) {
                if (eventDescriprion.length() > 255)
                    eventDescriprion = eventDescriprion.substring(0, 250) + "...";
                cs.setString(3, eventDescriprion);
            } else {
                cs.setNull(3, java.sql.Types.VARCHAR);
            }
            cs.execute();
        } catch (SQLException e) {
            log.warn("Unable to add a verification event for stUID = " + studyUID);
        } catch (NamingException e) {
            log.warn("Unable to get DB connection.");
        } finally {
            try {
                if (cs != null)
                    cs.close();
                if (con != null)
                    con.close();
            } catch (Exception e) {
            }
        }
    }

    public boolean removeStudyFromVerificationQueue(String studyUID) {
        Connection con = null;
        PreparedStatement st = null;
        try {
            con = getConnection();
            st = con.prepareStatement("UPDATE StudiesToVerify set toBeIgnored = 1 WHERE studyFk = ?");
            st.setString(1, studyUID);
            if (st.executeUpdate() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.warn("Unable to remove this study: " + studyUID + " from queue", e);
            return false;
        } finally {
            try {
                if (st != null)
                    st.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
            }
        }
    }

    @Override
    public boolean addStudyToVerificationQueue(String studyUID) {
        Connection con = null;
        PreparedStatement st = null;
        try {
            con = getConnection();
            st = con.prepareStatement("UPDATE StudiesToVerify set toBeIgnored = 0 WHERE studyFk = ?");
            st.setString(1, studyUID);
            if (st.executeUpdate() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.warn("Unable to remove this study: " + studyUID + " from queue", e);
            return false;
        } finally {
            try {
                if (st != null)
                    st.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
            }
        }
    }

    public boolean isStudyPublished(String studyUID) {
        Connection con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            st = con.prepareStatement("Select * from  StudiesToVerify where  toBeIgnored = 0 and studyFk = ? and jobFinishedOn is not null");
            st.setString(1, studyUID);
            rs = st.executeQuery();
            return rs.next();
        } catch (Exception e) {
            log.warn("Unable to know if the study is published. StudyUID: " + studyUID, e);
            return false;
        } finally {
            try{
                if(rs != null)
                    rs.close();
            }catch (Exception e) {
            }
            try {
                if (st != null)
                    st.close();
            } catch (SQLException e) {
            }
            try {
                if (con != null)
                    con.close();
            } catch (Exception e) {
            }
        }
    }

    private void insertEndJob(String studyUID) {
        Connection con = null;
        PreparedStatement st = null;
        try {
            con = getConnection();
            if (Dbms.isOracle(con)) {
                st = con.prepareStatement("UPDATE StudiesToVerify set jobFinishedOn = sysdate WHERE studyFk = ?");
            } else {
                st = con.prepareStatement("UPDATE StudiesToVerify set jobFinishedOn = sysdate() WHERE studyFk = ?");
            }
            st.setString(1, studyUID);
            st.executeUpdate();
        } catch (Exception e) {
            log.warn("Unable to remove this study: " + studyUID + " from queue", e);
        } finally {
            try {
                if (st != null)
                    st.close();
                if (con != null)
                    con.close();
            } catch (SQLException e) {
            }
        }
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
