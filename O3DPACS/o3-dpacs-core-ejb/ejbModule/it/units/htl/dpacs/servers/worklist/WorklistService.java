/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.worklist;

import it.units.htl.dpacs.dao.DicomDbDealer;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.helpers.LogMessage;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import java.io.StringReader;
import java.util.concurrent.Executor;

import javax.management.MBeanServer;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.TransferCapability;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class WorklistService implements WorklistServiceMBean {
    private final Log log = LogFactory.getLog(WorklistService.class);
    private boolean _serviceStatus = false;
    private String[] _supportedTS = new String[] {
            UID.ImplicitVRLittleEndian,
            UID.ExplicitVRLittleEndian,
    };
    private String[] _supportedSopClass = new String[] {
            UID.ModalityWorklistInformationModelFIND,
            UID.VerificationSOPClass
    };
    private Device _worklistServiceDevice = new Device("WorklistService");
    public final Executor _executor = new NewThreadExecutor("WorklistService");
    private NetworkApplicationEntity _worklistApplicationEntity = new NetworkApplicationEntity();
    private NetworkConnection _serviceNetworkConnection = new NetworkConnection();
    private DicomDbDealer bean = null;
    // this document contains service configuration
    private Document _configuration = null;
    // define if WorkList Server is running on an AS (actually only jboss)
    public boolean _onAS = false;
    // define the connection string to the DB
    public String _connectionString = null;
    // define the JDBC Driver to use
    public String _JDBCDriver = null;
    // define password for db
    public String _DbPass = null;
    // define username for db
    public String _DbUser = null;

    public WorklistService() {
        
    }

    public WorklistService(MBeanServer mbs) {
        _onAS = true;
        if (bean == null) {
            try {
                log.debug("About to create DicomDBDealer");
                bean = InitialContext.doLookup(BeansName.LDicomDbDealer);
            } catch (NamingException nex){
                log.error(LogMessage._CouldntCreate + " java:comp/env/ejb/DicomDbDealer", nex);
            }
        }
        
    }

    public boolean statusService() {
        return _serviceStatus;
    }

    public boolean reloadSettings() throws Exception, UnsupportedOperationException  {
    	boolean ret=stopService();
    	if(ret)
    		ret=startService();
    	return ret;
    	
    }

    public boolean startService() throws Exception, UnsupportedOperationException {
    	if(_serviceStatus)
    		stopService();
    	if(!init())
    		return false;
        _worklistServiceDevice.startListening(_executor);
        _serviceStatus = true;
        return true;
    }

    public boolean stopService() throws Exception, UnsupportedOperationException {
        _worklistServiceDevice.stopListening();
        _serviceStatus = false;
        return true;
    }

    private TransferCapability[] getTransferCapability() {
        TransferCapability[] serviceTC = new TransferCapability[_supportedSopClass.length];
        for (int i = 0; i < _supportedSopClass.length; i++) {
            serviceTC[i] = new TransferCapability(_supportedSopClass[i], _supportedTS, TransferCapability.SCP);
        }
        return serviceTC;
    }

    private boolean init() {
    	boolean ret=false;
        try {
        	if (loadConfig()) {
                _worklistServiceDevice.setNetworkApplicationEntity(_worklistApplicationEntity);
                _worklistServiceDevice.setNetworkConnection(_serviceNetworkConnection);
                _worklistApplicationEntity.setNetworkConnection(_serviceNetworkConnection);
                _worklistApplicationEntity.setAssociationAcceptor(true);
                if (!_onAS) {
                    _connectionString = _configuration.getElementsByTagName("connectionString").item(0).getTextContent();
                    _DbUser = _configuration.getElementsByTagName("dbUser").item(0).getTextContent();
                    _DbPass = _configuration.getElementsByTagName("dbPassword").item(0).getTextContent();
                    _JDBCDriver = _configuration.getElementsByTagName("jdbcDriver").item(0).getTextContent();
                    _worklistApplicationEntity.register(new WorklistDicomService(this, getConfigParam("viewName"), getConfigParam("uniqueField"), getConfigParam("datePattern"), getConfigParam("dateTimePattern"), getConfigParam("dateFormula"), getConfigParam("dateTimeFormula")));
                } else {
                    _worklistApplicationEntity.register(new WorklistDicomService(this, getConfigParam("viewName"), getConfigParam("uniqueField"), getConfigParam("datePattern"), getConfigParam("dateTimePattern"), getConfigParam("dateFormula"), getConfigParam("dateTimeFormula")));
                }
                _serviceNetworkConnection.setPort(Integer.parseInt(getConfigParam("servicePort")));
                _worklistApplicationEntity.setTransferCapability(getTransferCapability());
                
                _worklistApplicationEntity.setDimseRspTimeout(Integer.parseInt(getConfigParam("dimSeTimeOut")));
                _worklistApplicationEntity.setMaxPDULengthReceive(Integer.parseInt(getConfigParam("MaxPDULength")));
                _worklistApplicationEntity.setMaxPDULengthSend(Integer.parseInt(getConfigParam("MaxPDULength")));
                _worklistApplicationEntity.setPackPDV(Boolean.parseBoolean(getConfigParam("packPDVs")));
                _worklistApplicationEntity.setInstalled(true);
                _serviceNetworkConnection.setSocketCloseDelay(Integer.parseInt(getConfigParam("soCloseDelay")));
                _serviceNetworkConnection.setMaxScpAssociations(Integer.parseInt(getConfigParam("maxClients")));
                _serviceNetworkConnection.setRequestTimeout(Integer.parseInt(getConfigParam("rqTimeout")));
                // load the calledAeTittles from the configuration
                String[] aeTitles = toStringArray(_configuration.getElementsByTagName("CalledAETs").item(0).getTextContent());
                if (aeTitles != null) {
                    _worklistApplicationEntity.setPreferredCalledAETitle(aeTitles);
                }
                // load the callingAetitle from the configuration
                aeTitles = toStringArray(_configuration.getElementsByTagName("CallingAETs").item(0).getTextContent());
                if (_onAS && (aeTitles != null)) {
                    // if on AS the callingAetitles will be taken from the KnownNodesList
                    _worklistApplicationEntity.setPreferredCallingAETitle(toStringArray(bean.getExistingAEs()));
                } else if (!_onAS && (aeTitles != null)) {
                    // if not on AS, callingAetitles taken from the configuration file
                    _worklistApplicationEntity.setPreferredCallingAETitle(aeTitles);
                }
                ret=true;
            }

        } catch (Exception e) {
            log.error("While loading Worklist Service config", e);
            ret=false;
        }
        return ret;
    }

    private String getConfigParam(String paramName) {
        return _configuration.getElementsByTagName(paramName).item(0).getTextContent();
    }

    private boolean loadConfig() throws Exception {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            if (_onAS) {
                ServicesConfigurationHome sch = new ServicesConfigurationHome();
                ServicesConfiguration sc = sch.findByServiceName("WorkListService");
                if (sc != null) {
                    StringReader reader = new StringReader(sc.getConfiguration());
                    InputSource is = new InputSource(reader);
                    _configuration = docBuilder.parse(is);
                } else {
                    throw new ConfigurationException("Unable to get configuration");
                }
            } else {
                _configuration = docBuilder.parse(getClass().getResourceAsStream("configuration.xml"));
            }
        } catch (Exception e) {
            log.warn("WorkList service configuration failed to load.");
            throw e;
        }
        return true;
    }

    private String[] toStringArray(String s) {
        if ("any".equals(s)) {
            return null;
        }
        return s.split(";");
    }
}
