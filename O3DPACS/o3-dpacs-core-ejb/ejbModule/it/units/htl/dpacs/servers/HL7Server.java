/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers;

import it.units.htl.dpacs.core.VarLoader;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.servers.hl7.HL7DealerFactory;
import it.units.htl.dpacs.servers.hl7.HL7DealerFactory.SupportedHL7;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

//import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.app.SimpleServer;
//import org.dcm4che.server.HL7Handler;
//import org.dcm4che.server.Server;
//import org.dcm4che.server.ServerFactory;
//import org.dcm4che.util.HandshakeFailedEvent;
//import org.dcm4che.util.HandshakeFailedListener;
//import org.dcm4che.util.MLLP_Protocol;
//import org.dcm4che.util.SSLContextAdapter;

/**
 * The HL7 Server creates a Server Socket for receiving HL7 Messages
 * 
 * @author Mbe
 */
public class HL7Server implements HL7ServerMBean {
    private HL7Service hl7srv;
    private String actorName;
    private String protocol;
    private int port;
    private String sendingApps;
    private String pidIdentifierType = null;
    private String pidAssigningAuthority = null;
    private String pidAddressType = null;
    private String pidCityComponent = null;
    private int daysToEmptyCache = 0;
    @SuppressWarnings("unused")
    private MBeanServer server;
    private static Log log = LogFactory.getLog(HL7Server.class);
    private boolean serviceStatus;
    private String groupsForMessages;
    private String groupSegment;
    private int groupSequence = 0;
    private int groupRepetition = 0;
    private int groupComponent = 0;
    private String accNumSegment = null;
    private int visitNumberIssuerComponent = 0;
    private int accNumField = 0;
    private int accNumComponent = 0;
    private int accNumRepetition = 0;
    private String studyDescriptionSegment = null;
    private int studyDescriptionField = 0;
    private int studyDescriptionComponent = 0;
    private int studyDescriptionRepetition = 0;
    private String saveTransactionDate;
    private String defaultIdIssuer = null;
    private String mdmService;
    private String messagesToForward;
    private ArrayList<String> messagesToForwardAL = new ArrayList<String>();
    private String uniqueFieldForStudyUID = null;
    private int idIssuerField = 5; 
    private int procedueSequenceSegment = 4;
    private Boolean ackForUnexistingPatient = false;
    private Boolean checkPidIdentifierType = false;
    private String updateOnORM = "false";
    
    // Static --------------------------------------------------------
    /**
     * Return a path relative to the homedir
     * 
     * @param path
     * @return
     */
//    private static File toFile(String path) {
//        if (path == null || path.trim().length() == 0) {
//            return null;
//        }
//        File f = new File(path);
//        if (f.isAbsolute()) {
//            return f;
//        }
//        String serviceHomeDir = System.getProperty("user.dir");
//        return new File(serviceHomeDir, path);
//    }

    // Constructors --------------------------------------------------
    /**
     * Constructor
     * 
     * @param serv
     *            the MbeanServer
     */
    public HL7Server(MBeanServer serv) {
        this.server = serv;
    }

    // Public --------------------------------------------------------
    /**
     * @param arg
     */
    public void setDaysToEmptyHL7Cache(String arg) {
        daysToEmptyCache = Integer.parseInt(arg);
    }

    /**
     * @return
     */
    public String getDaysToEmptyHL7Cache() {
        return Integer.toString(daysToEmptyCache);
    }

    /**
     * Getter for property port.
     * 
     * @return Value of property port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Setter for property port.
     * 
     * @param newPort
     *            The new port value
     */
    public void setPort(int newPort) {
        this.port = newPort;
    }

    public String getGroupsForMessages() {
        return groupsForMessages;
    }

    public void setGroupsForMessages(String groupsForMessages) {
        this.groupsForMessages = groupsForMessages;
    }

    public String getGroupSegment() {
        return groupSegment;
    }

    public void setGroupSegment(String groupSegment) {
        this.groupSegment = groupSegment;
    }

    public String getGroupSequence() {
        return Integer.toString(groupSequence);
    }

    public void setGroupSequence(String groupSequence) {
        this.groupSequence = Integer.parseInt(groupSequence);
    }

    public String getGroupRepetition() {
        return Integer.toString(groupRepetition);
    }

    public void setGroupRepetition(String groupRepetition) {
        this.groupRepetition = Integer.parseInt(groupRepetition);
    }

    public String getGroupComponent() {
        return Integer.toString(groupComponent);
    }

    public void setGroupComponent(String groupComponent) {
        this.groupComponent = Integer.parseInt(groupComponent);
    }

    /**
     * Getter for property dcmHandler. Mandatory to start all other DICOM Services
     * 
     * @return Value of property dcmHandler.
     */
    // public HL7Handler getHL7Handler() {
    // return handler;
    // }
    /**
     * Gets the protocolName attribute of the HL7Server object Mandatory to define if it's TLS or not.
     * 
     * @return The protocolName value
     */
    public String getProtocolName() {
        return protocol;
    }

    /**
     * Sets the protocolName attribute of the HL7Server object Mandatory to define if it's TLS or not. choices: mllp or mllp-tls or mllp-tls_ENCRIPTION see MLLP_Protocol class contants
     * 
     * @param newName
     *            The new protocolName value
     */
    public void setProtocolName(String newName) {
        this.protocol = newName;
    }

    /**
     * Gets the keyFile attribute of the HL7Server object It's the server certificate
     * 
     * @return The keyFile value
     */
    public String getKeyFile() {
        // return keyFile;
        return null;
    }

    /**
     * Sets the keyFile attribute of the HL7Server object It's the server certificate
     * 
     * @param keyFile
     *            The new keyFile value
     */
    public void setKeyFile(String keyFile) {
        // logActorConfig("KeyFile", this.keyFile, keyFile,
        // AuditLogger.SECURITY);
        // this.keyFile = keyFile;
    }

    /**
     * Sets the keyPasswd attribute of the HL7Server object
     * 
     * @param keyPasswd
     *            The new keyPasswd value
     */
    public void setKeyPasswd(String keyPasswd) {
        // logActorConfig("KeyPasswd", this.keyPasswd, keyPasswd,
        // AuditLogger.SECURITY);
        // this.keyPasswd = keyPasswd;
    }

    /**
     * Gets the cacertsFile attribute of the HL7Server object It's the list of certificates accepted from HL7 server
     * 
     * @return The cacertsFile value
     */
    public String getCacertsFile() {
        // return cacertsFile;
        return null;
    }

    /**
     * Sets the cacertsFile attribute of the HL7Server object It's the list of certificates accepted from HL7 server
     * 
     * @param cacertsFile
     *            The new cacertsFile value
     */
    public void setCacertsFile(String cacertsFile) {
        // logActorConfig("CacertsFile", this.cacertsFile, cacertsFile,
        // AuditLogger.SECURITY);
        // this.cacertsFile = cacertsFile;
    }

    /**
     * Sets the cacertsPasswd attribute of the HL7Server object
     * 
     * @param cacertsPasswd
     *            The new cacertsPasswd value
     */
    public void setCacertsPasswd(String cacertsPasswd) {
        // logActorConfig("CacertsPasswd", this.cacertsPasswd, cacertsPasswd, AuditLogger.SECURITY);
        // this.cacertsPasswd = cacertsPasswd;
    }

    /**
     * Set the assigning authority to search for in the PID segment. The patientId with the specified AssigningAuthority is used to link the message to the existent patient.
     * 
     * @param assigningAuthority
     */
    public void setPidIdentifierType(String pidIdentifierType) {
        this.pidIdentifierType = pidIdentifierType;
    }

    /**
     * Get the assigning authority to search for in the PID segment. The patientId with the specified AssigningAuthority is used to link the message to the existent patient.
     * 
     * @param assigningAuthority
     */
    public String getPidIdentifierType() {
        return this.pidIdentifierType;
    }

    /**
     * Set the assigning authority to search for in the PID segment. The patientId with the specified AssigningAuthority is used to link the message to the existent patient.
     * 
     * @param assigningAuthority
     */
    public void setPidAssigningAuthority(String pidAssigningAuthority) {
        this.pidAssigningAuthority = pidAssigningAuthority;
    }

    /**
     * Get the assigning authority to search for in the PID segment. The patientId with the specified AssigningAuthority is used to link the message to the existent patient.
     * 
     * @param assigningAuthority
     */
    public String getPidAssigningAuthority() {
        return this.pidAssigningAuthority;
    }

    public void setPidAddressType(String pidAddressType) {
        this.pidAddressType = pidAddressType;
    }

    public String getPidAddressType() {
        return this.pidAddressType;
    }

    public void setPidCityComponent(String pidCityComponent) {
        this.pidCityComponent = pidCityComponent;
    }

    public String getPidCityComponent() {
        return this.pidCityComponent;
    }

    /**
     * Gets the soTimeout attribute of the HL7Server object
     * 
     * @return The soTimeout value
     */
    public int getSoTimeout() {
        // return handler.getSoTimeout();
        return SimpleServer.SO_TIMEOUT;
    }

    /**
     * Sets the soTimeout attribute of the HL7Server object
     * 
     * @param newSoTimeout
     *            The new soTimeout value
     */
    public void setSoTimeout(int newSoTimeout) {
        // handler.setSoTimeout(newSoTimeout);
    }

    /**
     * Getter for property maxClients. The max number of HL7 client allowed to connect at the same time
     * 
     * @return Value of property maxClients.
     */
    public int getMaxClients() {
        // return hl7srv.getMaxClients();
        return 0;
    }

    /**
     * Setter for property maxClients. The max number of HL7 client allowed to connect at the same time
     * 
     * @param newMaxClients
     *            The new maxClients value
     */
    public void setMaxClients(int newMaxClients) {
        // hl7srv.setMaxClients(newMaxClients);
    }

    /**
     * Getter for property numClients. The current number of client connected
     * 
     * @return Value of property numClients.
     */
    public int getNumClients() {
        // return hl7srv.getNumClients();
        try {
            return ((SimpleServer) hl7srv).getRemoteConnections().size();
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Gets the sendingApps attribute of the HL7Server object
     * 
     * @return The sendingApps value
     */
    public String getSendingApps() {
        return this.sendingApps;
        // return toString(handler.getSendingApps());
    }

    public ArrayList<String> getSendingAppsList() {
        if ("any".equals(this.sendingApps) || ("".equals(sendingApps)) || (this.sendingApps == null)) {
            return null;
        }
        StringTokenizer stk = new StringTokenizer(this.sendingApps, ";");
        ArrayList<String> a = new ArrayList<String>(stk.countTokens());
        while (stk.hasMoreTokens())
            a.add(stk.nextToken());
        return a;
    }

    /**
     * Sets the sendingApps attribute of the HL7Server object
     * 
     * @param newSendingApps
     *            The new sendingApps value
     */
    public void setSendingApps(String newSendingApps) {
        // handler.setSendingApps(toStringArray(newSendingApps));
        this.sendingApps = newSendingApps;
    }

    /**
     * Gets the receivingApps attribute of the HL7Server object
     * 
     * @return The receivingApps value
     */
    public String getReceivingApps() {
        // return toString(handler.getReceivingApps());
        return null;
    }

    /**
     * Sets the receivingApps attribute of the HL7Server object
     * 
     * @param newReceivingApps
     *            The new receivingApps value
     */
    public void setReceivingApps(String newReceivingApps) {
        // handler.setReceivingApps(toStringArray(newReceivingApps));
    }

    public String getDefaultIdIssuer() {
        return defaultIdIssuer;
    }

    @Override
    public void setMessagesToForward(String s) {
        log.info("These messages will be forwarded to the subscribers: " + s);
        if (s != null && !"".equals(s)) {
            messagesToForward = s;
            String[] mess = messagesToForward.split(",");
            for (int i = 0; i < mess.length; i++) {
                messagesToForwardAL.add(mess[i]);
            }
        }
    }

    @Override
    public String getMessagesToForward() {
        return messagesToForward;
    }
    @Override
    public boolean toBeForwarded(String messageType){
        return messagesToForwardAL.contains(messageType);
    }

    // ServiceMBeanSupport overrides ---------------------------------
    /**
     * Gets the name attribute of the HL7Server object
     * 
     * @return The name value
     */
    public String getName() {
        return actorName;
    }

    /**
     * Gets the objectName attribute of the HL7Server object
     * 
     * @param server
     *            Description of the Parameter
     * @param name
     *            Description of the Parameter
     * @return The objectName value
     */
    protected ObjectName getObjectName(MBeanServer server, ObjectName name) {
        actorName = name.getKeyProperty("name");
        return name;
    }

    /** Description of the Method */
    public void initServices() {
        for (SupportedHL7 s : SupportedHL7.values()) {
            int index = s.type().indexOf("^");
            String messageType = s.type().substring(0, index);
            String triggerEvent = s.type().substring(index + 1);
            hl7srv.registerApplication(messageType, triggerEvent, HL7DealerFactory.getInstance(s, this));
        }
    }

    /**
     * Loads setting from XML file
     */
    private void loadOnStart() {
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        VarLoader xmlLoader = null;
        ServicesConfiguration sc = sch.findByServiceName("HL7Server");
        xmlLoader = new VarLoader(sc.getConfiguration(), VarLoader.FROMSTRING);
        /* Vettore di stringhe con i nomi degli attributi */
        String[] nameOfAttributes = xmlLoader.getNameOfValues();
        /* Vettore di stringhe con i valori degli attributi */
        String[] valuesOfAttributes = xmlLoader.getStringValues();
        /* Carica i valori degli Attributi */
        int error = 0;
        for (int i = 0; i < nameOfAttributes.length; i++) {
            error = 0;
            try {
                Class[] argomento = { String.class };
                Method metodo = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
                Object[] valore = { new String(valuesOfAttributes[i]) };
                metodo.invoke(this, valore);
            } catch (java.lang.NoSuchMethodException eNSME) {
                error = 1;
            } catch (java.lang.IllegalAccessException eIAE) {
                error = 1;
            } catch (java.lang.reflect.InvocationTargetException eITE) {
                error = 1;
            } catch (Exception ex) {
                log.error("", ex);
            }
            try {
                Class[] argomento = { int.class };
                Method metodo = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
                Object[] valore = { new Integer(Integer.parseInt(valuesOfAttributes[i])) };
                metodo.invoke(this, valore);
            } catch (java.lang.NoSuchMethodException eNSME) {
                error += 1;
            } catch (java.lang.IllegalAccessException eIAE) {
                error += 1;
            } catch (java.lang.reflect.InvocationTargetException eITE) {
                error += 1;
            } catch (Exception ex) {
                log.error("", ex);
            }
            try {
                Class[] argomento = { ObjectName.class };
                Method metodo = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
                Object[] valore = { new ObjectName(valuesOfAttributes[i]) };
                metodo.invoke(this, valore);
            } catch (java.lang.NoSuchMethodException nsmex) {
                if (error != 1) {
                    log.fatal("HL7Server: Error when reading files: ", nsmex);
                }
            } catch (java.lang.IllegalAccessException iaex) {
                if (error != 1) {
                    log.fatal("HL7Server: Error when reading files: ", iaex);
                }
            } catch (java.lang.reflect.InvocationTargetException itex) {
                if (error != 1) {
                    log.fatal("HL7Server: Error when reading files: ", itex);
                }
            } catch (Exception ex) {
                log.fatal("HL7Server: Error when reading files: ", ex);
            }
        }
        this.defaultIdIssuer = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.DEFAULT_ID_ISSUER);
        if (this.defaultIdIssuer == null)
            throw new RuntimeException("DEFAULT_ID_ISSUER not found");
        log.debug("Finito il caricamento");
    }

    /* (Fine) Parte in aggiunta (Utile per il caricamento....) */
    public boolean statusService() {
        return serviceStatus;
    }

    public boolean reloadSettings() throws Exception {
        return startService();
    }

    public boolean startService() {
        try {
            stopService();
            loadOnStart();
            log.debug("HL7 Server loaded configuration file");
            HapiContext context = new DefaultHapiContext();
            boolean useTls = this.protocol.endsWith("s");
            hl7srv = context.newServer(port, useTls);
            // auditLog = (AuditLogger) server.getAttribute(auditLogName, "AuditLogger");
            initServices();
            hl7srv.startAndWait();
            serviceStatus = true;
            return true;
        } catch (Exception ex) {
            log.fatal("Error when starting HL7 Server: ", ex);
            serviceStatus = false;
            return false;
        }
    }

    public boolean stopService() throws Exception {
        if (serviceStatus) {
            hl7srv.stop();
            hl7srv.waitForTermination();
            try {
                Thread.sleep(2000);
                log.info("Waited for HL7Server to release port");
            } catch (Exception ex) {
                log.error(ex);
            }
            hl7srv = null;
            serviceStatus = false;
        }
        return true;
    }

    public String getAccNumSegment() {
        return accNumSegment;
    }

    public void setAccNumSegment(String accNumSegment) {
        if ("".equals(accNumSegment))
            this.accNumSegment = null;
        else
            this.accNumSegment = accNumSegment;
    }

    public String getVisitNumberIssuerComponent() {
        return "" + visitNumberIssuerComponent;
    }

    public void setVisitNumberIssuerComponent(String visitNumberIssuerComponent) {
        if ((visitNumberIssuerComponent != null) && (!"".equals(visitNumberIssuerComponent)))
            this.visitNumberIssuerComponent = Integer.parseInt(visitNumberIssuerComponent);
        else
            this.visitNumberIssuerComponent = 0;
    }

    public String getAccNumField() {
        return "" + accNumField;
    }

    public void setAccNumField(String accNumField) {
        if ((accNumField != null) && (!"".equals(accNumField)))
            this.accNumField = Integer.parseInt(accNumField);
        else
            this.accNumField = 0;
    }

    public String getAccNumComponent() {
        return "" + accNumComponent;
    }

    public void setAccNumComponent(String accNumComponent) {
        if ((accNumComponent != null) && (!"".equals(accNumComponent)))
            this.accNumComponent = Integer.parseInt(accNumComponent);
        else
            this.accNumComponent = 0;
    }

    public String getAccNumRepetition() {
        return "" + accNumRepetition;
    }

    public void setAccNumRepetition(String accNumRepetition) {
        if ((accNumRepetition != null) && (!"".equals(accNumRepetition)))
            this.accNumRepetition = Integer.parseInt(accNumRepetition);
        else
            this.accNumRepetition = 0;
    }

    public String getStudyDescSegment() {
        return studyDescriptionSegment;
    }

    public void setStudyDescSegment(String studyDescriptionSegment) {
        if ("".equals(accNumSegment))
            this.studyDescriptionSegment = null;
        else
            this.studyDescriptionSegment = studyDescriptionSegment;
    }

    public String getStudyDescField() {
        return "" + studyDescriptionField;
    }

    public void setStudyDescField(String studyDescriptionField) {
        if ((studyDescriptionField != null) && (!"".equals(studyDescriptionField)))
            this.studyDescriptionField = Integer.parseInt(studyDescriptionField);
        else
            this.studyDescriptionField = 0;
    }

    public String getStudyDescComponent() {
        return "" + studyDescriptionComponent;
    }

    public void setStudyDescComponent(String studyDescriptionComponent) {
        if ((studyDescriptionComponent != null) && (!"".equals(studyDescriptionComponent)))
            this.studyDescriptionComponent = Integer.parseInt(studyDescriptionComponent);
        else
            this.studyDescriptionComponent = 0;
    }

    public String getStudyDescRepetition() {
        return "" + studyDescriptionRepetition;
    }

    public void setStudyDescRepetition(String studyDescriptionRepetition) {
        if ((studyDescriptionRepetition != null) && (!"".equals(studyDescriptionRepetition)))
            this.studyDescriptionRepetition = Integer.parseInt(studyDescriptionRepetition);
        else
            this.studyDescriptionRepetition = 0;
    }

    public String isSaveTransactionDate() {
        return saveTransactionDate;
    }

    public void setSaveTransactionDate(String save) {
        if ("1".equals(save) || "TRUE".equalsIgnoreCase(save))
            this.saveTransactionDate = "true";
        else
            this.saveTransactionDate = "false";
    }

    @Override
    public String getMdmService() {
        return mdmService;
    }

    @Override
    public void setMdmService(String mdmService) {
        this.mdmService = mdmService;
    }

    public String getUniqueField() {
        return uniqueFieldForStudyUID;
    }

    public void setUniqueField(String uniqueFieldForStudyUID) {
        this.uniqueFieldForStudyUID = uniqueFieldForStudyUID;
    }

    public int getIdIssuerField() {
        return idIssuerField;
    }

    public void setIdIssuerField(int idIssuerField) {
        this.idIssuerField = idIssuerField;
    }

    public int getProcedueSequenceSegment() {
        return procedueSequenceSegment;
    }

    public void setProcedueSequenceSegment(int procedueSequenceSegment) {
        this.procedueSequenceSegment = procedueSequenceSegment;
    }

    public boolean getAckForUnexistingPatient() {
        return ackForUnexistingPatient;
    }

    public void setAckForUnexistingPatient(String value) {
        ackForUnexistingPatient = Boolean.parseBoolean(value);
    }

    public Boolean getCheckPidIdentifierType() {
        return checkPidIdentifierType;
    }

    public void setCheckPidIdentifierType(String value) {
        this.checkPidIdentifierType = Boolean.parseBoolean(value);
    }

    public String getUpdateOnORM() {
        return updateOnORM;
    }

    public void setUpdateOnORM(String value) {
        log.debug("Set UpdateOnORM : " + updateOnORM);
        updateOnORM =  value;
    }
    
    public Boolean getIsUpdateOnORM(){
        return  Boolean.parseBoolean(updateOnORM);
    }
    
    
}