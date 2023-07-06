/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.storage;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import it.units.htl.dpacs.dao.*;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.core.*;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.util.SSLContextAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class StorageSCP implements StorageSCPMBean {
    private static String dumpDir = null;
    // incoming Transfer Syntax Accepted
    private static final String[] SUPPORTED_TS = { UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian, UIDs.JPEG2000Lossless, UIDs.JPEGLossless, UIDs.JPEGBaseline };
    private static ArrayList<String> STORAGE_AS = new ArrayList<String>(10);
    private DicomDbDealer bean = null;
    static final Map<String, Object> dumpParam = new HashMap<String, Object>(5);
	
    static {
        dumpParam.put("maxlen", new Integer(128));
        dumpParam.put("vallen", new Integer(64));
        dumpParam.put("prefix", "\t");
    }
    // Attributes ----------------------------------------------------
    private MBeanServer server;
    private Log log = LogFactory.getLog(StorageSCP.class);
    // private ObjectName auditLogName;
    private ObjectName dcmServerName;
    private DcmHandler dcmHandler;
    private StorageServer store = new StorageServer(this);
    private StorageCommitServer stgcmt = new StorageCommitServer(this);
    private Hashtable<String, String> instanceTables = null;
    private Hashtable<String, String> storageTable = null;
    private Hashtable<String, String> equipmentTable = null;
    private boolean successOnDup = false;
    private String urlToDups = null;
    private boolean isPartiallyAnonimized = false;
    private int secondsBetweenTimerChecks;
    private int minutesAfterStudyConsideredFinished;
    
    private boolean serviceStatus = false;
    
    private String defaultIdIssuer=null;
    
    
    

    /**
     * The constructor
     * 
     * @param serv
     *            The Mbean server for O3-DPACS Mbeans
     */
    public StorageSCP(MBeanServer serv) {
        this.server = serv;
        if (bean == null) {
            try {
                log.debug("About to create DicomDBDealer");
                bean = InitialContext.doLookup(BeansName.LDicomDbDealer);
            } catch (NamingException nex){
                log.fatal("Unable to get the DicomDbDealer!", nex);
            } // end try...catch
        }
    }

    /**
     * Sets the folder for storing duplicated images
     * 
     * @param arg
     *            the folder
     */
    public void setUrlToDups(String arg) {
        urlToDups = arg;
    }

    /**
     * Gets the folder for storing duplicated images
     */
    public String getUrlToDups() {
        return urlToDups;
    }

    /**
     * Gets the folder for storing images causing storing error
     * 
     * @return the folder
     */
    public String getDumpDirectory() {
        return dumpDir;
    }

    /**
     * Sets the folder for storing images causing storing error
     * 
     * @param dd
     *            the folder
     */
    public void setDumpDirectory(String dd) {
        dumpDir = dd;
    }

    /**
     * Sets the storage SCP to answer STATUS 0 (OK) if it finds a duplicated instance. This allows to avoid queue in the modalities, when they send series in a pattern like 1, 1-2, 1-2-3 as they are acquired. Default true
     * 
     * @param yesOrNo
     *            The value for success
     */
    public void setSuccessOnDup(boolean yesOrNo) {
        successOnDup = yesOrNo;
    }

    /**
     * Gets the value for return succes on duplicated images
     * 
     * @return the setting for this feature
     */
    public boolean getSuccessOnDup() {
        return successOnDup;
    }

    /**
     * Sets the auditLoggerName attribute of the StorageSCP object
     * 
     *@param auditLogName
     *            The new auditLoggerName value
     */
    public void setAuditLoggerName(ObjectName auditLogName) {
        // this.auditLogName = auditLogName;
    }

    /**
     * Gets the dcmServerName attribute of the StorageSCP object
     * 
     *@return The dcmServerName value
     */
    public ObjectName getDcmServerName() {
        return dcmServerName;
    }

    /**
     * Sets the dcmServerName attribute of the StorageSCP object
     * 
     *@param dcmServerName
     *            The new dcmServerName value
     */
    public void setDcmServerName(ObjectName dcmServerName) {
        this.dcmServerName = dcmServerName;
    }

    /**
     * Gets the acTimeout attribute of the StorageCommitServer object AC Timeout is the time the SCU waits for association being negotiated with the SCP before aborting the attempt It's in ms.
     * 
     * @return The acTimeout value
     */
    public int getAcTimeout() {
        return stgcmt.getAcTimeout();
    }

    /**
     * ?????????
     * 
     * @param arg
     * @return
     */
    public String getEquipment(String arg) {

        if (equipmentTable == null) {
            equipmentTable = bean.loadEquipmentCache();
        }
        return (String) equipmentTable.get(arg);
    }

    /**
     * Gets the current storage area for the selected AE-TITLE This goes in the cache of KnowNodes table
     * 
     * @param arg
     *            Ar-title
     * @return the storage area
     */
    public String getStorage(String arg) {
        if (storageTable == null) {
            storageTable = bean.loadStorageCache();
        }
        return (String) storageTable.get(arg);
    }
    
    public boolean isPartiallyAnonymized(){
        return isPartiallyAnonimized;
    }

    /**
     * Returns the type of instance for the given SOP Class The type is divided into: - images - non images - sr - overlays - presentation states and reflects the database structure for specific instances
     * 
     * @param arg
     *            the sop Class
     * @return the type of instance
     */
    public String getTable(String arg) {
        if (instanceTables == null) {
            instanceTables = bean.loadMappingCache();
        }
        return (String) instanceTables.get(arg);
    }

    /**
     * Sets the acTimeout attribute of the StorageCommitServer object AC Timeout is the time the SCU waits for association being negotiated with the SCP before aborting the attempt It's in ms.
     * 
     * @param timeout
     *            The new acTimeout value
     */
    public void setAcTimeout(int timeout) {
        stgcmt.setAcTimeout(timeout);
    }

    /**
     * Gets the dimseTimeout attribute of the StorageCommitServer object Dimse timeout is the max value of time the DICOM service may take to be performed
     * 
     * @return The dimseTimeout value
     */
    public int getDimseTimeout() {
        return stgcmt.getDimseTimeout();
    }

    /**
     * Sets the dimseTimeout attribute of the StorageCommitServer object Dimse timeout is the max value of time the DICOM service may take to be performed
     * 
     * @param timeout
     *            The new dimseTimeout value
     */
    public void setDimseTimeout(int timeout) {
        stgcmt.setDimseTimeout(timeout);
    }

    /**
     * Gets the soCloseDelay attribute of the StorageCommitServer object soCloseDelay is socket close delay
     * 
     * @return The soCloseDelay value
     */
    public int getSoCloseDelay() {
        return stgcmt.getSoCloseDelay();
    }

    /**
     * Sets the soCloseDelay attribute of the StorageCommitServer object soCloseDelay is socket close delay
     * 
     * @param delay
     *            The new soCloseDelay value
     */
    public void setSoCloseDelay(int delay) {
        stgcmt.setSoCloseDelay(delay);
    }

	public String getSecondsBetweenTimerChecks() {
		return Integer.toString(secondsBetweenTimerChecks);
	}

	public void setSecondsBetweenTimerChecks(String secondsBetweenTimerChecks) {
		this.secondsBetweenTimerChecks = Integer.parseInt(secondsBetweenTimerChecks);
	}

	public String getMinutesAfterStudyConsideredFinished() {
		return Integer.toString(minutesAfterStudyConsideredFinished);
	}

	public void setMinutesAfterStudyConsideredFinished(String minutesAfterStudyConsideredFinished) {
		this.minutesAfterStudyConsideredFinished = Integer.parseInt(minutesAfterStudyConsideredFinished);
	}
 
	
    public String getDefaultIdIssuer() {
		return defaultIdIssuer;
	}

	private void loadSupportedTS() {
        ArrayList<String> loadedTs = new ArrayList<String>();
        String[] loadedTsArray = new String[0];
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        ServicesConfiguration sc = sch.findByServiceName("AcceptedTS");
        boolean takeDefault = false;
        try {
        	if (sc.getConfiguration() != null) {
        		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        		StringReader reader = new StringReader(sc.getConfiguration());
        		InputSource is = new InputSource(reader);
        		Document doc = docBuilder.parse(is);
        		doc.getDocumentElement().normalize();
        		NodeList nodeLst = doc.getElementsByTagName("root");
        		Element _rootNode = (Element) nodeLst.item(0);
        		NodeList ns = _rootNode.getElementsByTagName("ts");
        		StringBuilder supportedTs = new StringBuilder("Supported TS on storage:");
        		for (int i = ns.getLength()-1; i>=0; i--) {
        			try {
        				loadedTs.add(ns.item(i).getTextContent());
        				supportedTs.append("\nTS: ").append(ns.item(i).getTextContent());
        			} catch (Exception e) {
        				log.warn(ns.item(i).getTextContent() + " TS is INVALID...watch the configuration.");
        			}
        		}
        		log.info(supportedTs.toString());
        		loadedTsArray = new String[loadedTs.size()];
        	} else {
        		takeDefault = true;
        	}
        } catch (ParserConfigurationException e) {
            log.error("", e);
            takeDefault = true;
        } catch (IOException e) {
            log.error("couldn't open config file!", e);
            takeDefault = true;
        } catch (SAXException e) {
            log.error("Couldn't parse config file!", e);
            takeDefault = true;
        }
        if (!takeDefault) {
            updatePolicy(loadedTs.toArray(loadedTsArray));
        } else {
            updatePolicy(SUPPORTED_TS);
        }
    }


    /**
     * Loads into cache: the storage area to DICOM nodes mapping, the type of instances to tables cache and the Equipment table.
     * 
     * @throws org.dcm4che.net.DcmServiceException
     */
    private void loadCache() throws DcmServiceException {
        Hashtable<String, String> temp = null;
        temp = bean.loadMappingCache();
        if (temp != null) {
            instanceTables = temp; // Don't overwrite if new table==null DTODO:Here load it anyway, use this approach in the future restartService()
        }
        temp = bean.loadStorageCache();
        if (temp != null) {
            storageTable = temp;
        }
        temp = bean.loadEquipmentCache();
        if (temp != null) {
            equipmentTable = temp; // DTODO: Check the following line!!!
        }
        // log.info("InstanceMappings: "+instanceTables.size()+" StorageMappings: "+storageTable.size()+" EquipmentMappings: "+equipmentTable.size());
        temp = null; // For GC
    }



    /**
     * Binds STORAGE AS and transfer syntax to the current service to be received
     */
    private void bindDcmServices() {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < STORAGE_AS.size(); ++i) {
            services.bind(STORAGE_AS.get(i), store);
        }
        services.bind(UIDs.StorageCommitmentPushModel, stgcmt);
        dcmHandler.addAssociationListener(store);
        
    }

    /** UnBinds STORAGE AS and transfer syntax to the current service */
    private void unbindDcmServices() {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < STORAGE_AS.size(); ++i) {
            services.unbind(STORAGE_AS.get(i));
        }
        services.unbind(UIDs.StorageCommitmentPushModel);
        dcmHandler.removeAssociationListener(store);
    }

    /**
     * Set the policies and the presentation context for Association Negotiation AS are read from cache
     * 
     * @param ts1
     *            the list of Transfer Syntax to be associated to AS
     */
    private void updatePolicy(String[] ts1) {
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        for (int i = 0; i < STORAGE_AS.size(); ++i) {
            policy.putPresContext(STORAGE_AS.get(i), ts1);
        }
        policy.putPresContext(UIDs.StorageCommitmentPushModel, ts1);
    }

    /**
     * Loads supported SOP Classes for the current service from SupportedSOP Class Table in O3-DPACS DB
     * 
     * @return the number of SOP Classes loaded for the current service
     * @throws org.dcm4che.net.DcmServiceException
     */
    private int addSupportedSOPClasses() throws DcmServiceException {
        if (bean == null) {
            try {
                log.debug("About to create DicomDBDealer");
                bean = InitialContext.doLookup("java:global/o3-dpacs-ear/o3-dpacs-core-ejb/DicomDbDealerBean!it.units.htl.dpacs.dao.DicomDbDealer");
            } catch (NamingException nex){
                log.error("Unable to create DicomDbDealer...", nex);
                throw new DcmServiceException(org.dcm4che.dict.Status.ProcessingFailure);
            }
        }
        try {
            STORAGE_AS = bean.loadAbstractSyntaxes(2);
        } catch (Exception ex) {
            log.error("", ex);
        }
        return STORAGE_AS.size();
    }

    /**
     * Takes the XML file in core package and gets the default setting for Mbean parameters
     */
    @SuppressWarnings("rawtypes")
    private void loadConfig() {
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        VarLoader xmlLoader = null;
        ServicesConfiguration sc = sch.findByServiceName("StorageSCP");
        xmlLoader = new VarLoader(sc.getConfiguration(), VarLoader.FROMSTRING);
        String[] nameOfAttributes = xmlLoader.getNameOfValues();
        String[] valuesOfAttributes = xmlLoader.getStringValues();
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
                ex.printStackTrace();
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
                ex.printStackTrace();
            }
            try {
                Class[] argomento = { boolean.class };
                Method metodo = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
                Object[] valore = { new Boolean(valuesOfAttributes[i]) };
                metodo.invoke(this, valore);
            } catch (java.lang.NoSuchMethodException eNSME) {
                error += 1;
            } catch (java.lang.IllegalAccessException eIAE) {
                error += 1;
            } catch (java.lang.reflect.InvocationTargetException eITE) {
                error += 1;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                Class[] argomento = { ObjectName.class };
                Method metodo = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
                Object[] valore = { new ObjectName(valuesOfAttributes[i]) };
                metodo.invoke(this, valore);
            } catch (java.lang.NoSuchMethodException eNSME) {
                if (error == 3) {
                    log.info("Non esiste il metodo in questa classe set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>Valore</NomeAttributo>" + nameOfAttributes[i]);
                }
            } catch (java.lang.IllegalAccessException eIAE) {
                if (error == 3) {
                    log.info("Non esiste il metodo in questa classe set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>Valore</NomeAttributo>" + nameOfAttributes[i]);
                }
            } catch (java.lang.reflect.InvocationTargetException eITE) {
                if (error == 3) {
                    log.info("Non esiste il metodo in questa classe set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>Valore</NomeAttributo>" + nameOfAttributes[i]);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
//        set the flag that identifies is the installation IS Partially Anonymized
        if(bean != null){
            isPartiallyAnonimized = bean.isPartiallyAnonymizedInstallation();
        }else{
            log.warn("Unable to know if this installation is PARTIALLY ANONYMIZED!!");
            isPartiallyAnonimized = false;
        }
        this.defaultIdIssuer=GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.DEFAULT_ID_ISSUER);
        if(this.defaultIdIssuer==null)
        	throw new RuntimeException("DEFAULT_ID_ISSUER not found");
    }

    /**
     * Return the storage server reference
     * 
     * @return the storage server
     */
    public StorageServer getStorageServer() {
        return store;
    }

    /**
     * Return the current MBeanServer
     * 
     * @return MBeanServer
     */
    public MBeanServer getMbServer() {
        if (server == null) {
            log.info("From storage scp the server is null here too");
        } else {
            log.info("in storage scp mbean server exist");
        }
        return this.server;
    }

    public boolean statusService() {
        return serviceStatus;
    }
    
    /**
     * Start the storage service (no socket), loads settings, Creates auditLog, registers StorageCommitment Service, binds SOP Classes and other mandatory prilimary task to deliver storage SCP service
     */
    public boolean startService() {
        try {
            if(serviceStatus){
                stopService();
            }
            loadConfig();
            startServer();
            serviceStatus = true;
            return true;
        } catch (Exception ex) {
            log.error("While starting Storage SCP", ex);
            serviceStatus = false;
            return false;
        }
    }

    
    private void startServer() throws Exception {
        dcmHandler = (DcmHandler) server.getAttribute(dcmServerName, "DcmHandler");
        stgcmt.setSSLContextAdapter((SSLContextAdapter) server.getAttribute(dcmServerName, "SSLContextAdapter"));
        int a = addSupportedSOPClasses();
        log.info("Loaded sop classes: " + a);
        bindDcmServices();
        loadSupportedTS();
        loadCache();
        resetInstancesInProgress();
    }

    private void resetInstancesInProgress() {
    	String[] instances=bean.getInstancesInProgress();
    	
    	if(instances!=null){
    		StringBuilder sb=new StringBuilder();
    		for(String instance: instances){
    			sb.append("'").append(instance).append("',");
    		}
    		sb.deleteCharAt(sb.length()-1);
    		log.info("Instances in progress at last shutdown: "+sb.toString());
    		
    		int deleted=bean.clearInstancesInProgress();
    		log.info("Deleted instances: "+deleted);
    		if(deleted!=instances.length)
    			log.warn("Discrepancy between sizes: deleted "+deleted+", listed "+instances.length+" instances");
    			
    	}else{
    		log.info("No instances in progress at last shutdown");
    	}
	}

    
	public boolean reloadSettings() {
        return startService();
    }

    public boolean stopService() throws Exception {
        updatePolicy(null);
        unbindDcmServices();
        dcmHandler = null;
        serviceStatus = false;
        return true;
    }

   
}