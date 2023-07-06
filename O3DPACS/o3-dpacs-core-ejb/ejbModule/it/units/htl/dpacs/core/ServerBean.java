/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.core;

import it.units.htl.dpacs.core.services.PacsService;
import it.units.htl.dpacs.core.services.Service;
import it.units.htl.dpacs.core.services.ServicesManager;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.ejb.CreateException;
import javax.ejb.Stateless;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jdmk.comm.AuthInfo;
import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * The class is an EJB with remote interface that implement an MBean Server, to allow all the components of O3-DPACS to be registered in it. Thus you have your own structure and thus you can achieve portability across different Application Servers
 * 
 * @author Mbe
 */
@Stateless(mappedName="o3-dpacs/ServerBean/remote")
public class ServerBean implements ServerRemote {
    private Log log = LogFactory.getLog(ServerBean.class);
    private MBeanServer mbs = null;
    private boolean isReportingEnabled = false;
    // private JMXConnectorServer mbsRemote = null;
    private boolean serversStatus = false;
    private boolean isLoaded = false;
    private ServicesManager sM = ServicesManager.getInstance();
    private ArrayList<String> myIps = new ArrayList<String>();

    public void readManifest() {
    	try {
    		
    		InputStream inputStream =  this.getClass().getClassLoader().getResourceAsStream("/META-INF/MANIFEST.MF");
    		Manifest manifest = new Manifest(inputStream);    
    		Attributes attr = manifest.getMainAttributes();
    		
    		log.info("****************** PROJECT VERSION *************************************");
    		log.info(attr.getValue("Specification-Title") + '-' + attr.getValue("Specification-Version")); 
    		log.info("************************************************************************");
    		
		}  catch (IOException e) {
			log.error(e.getMessage());
		}
    }
    
    public ServerBean() {
    	
    	readManifest();
    	
        String mbsName = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.MBEANSERVER_NAME);
        for (MBeanServer ser : MBeanServerFactory.findMBeanServer(null)) {
            if (mbsName.equals(ser.getDefaultDomain())) {
                mbs = ser;
                isLoaded = true;
                serversStatus = true;
            }
        }
        if (mbs == null) {
            loadDPACS();
            //GDC: task: 312586 Bug:36178
            startServers();           
        }
    }

    public void ejbCreate() throws CreateException {
    }

    public void stopMbeanServer() {
        this.mbs = null;
    }

    /**
     * The method creates an Mbean Server and registers the MBean for each O3-DPACS service, including other managable elements (helpers). It also stars HTTP adaptor for viewing the managed variables
     * 
     * @return a boolean value as operation status
     * @throws CreateException
     */
    public boolean loadDPACS() {
        Boolean status = false;
        log.info("###### Instantiating and registering services of O3-DPACS #####");
        createMBeanServer();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null)
                log.error("NO NETWORK INTERFACES RETRIEVED!!!");
            else {
                log.info("Recognized network interfaces:");
                NetworkInterface i = null;
                while (interfaces.hasMoreElements()) {
                    i = interfaces.nextElement();
                    log.info("   " + i.getDisplayName() + " - " + i.getName());
                    Enumeration<InetAddress> addresses = i.getInetAddresses();
                    if (addresses != null) {
                        InetAddress a = null;
                        while (addresses.hasMoreElements()) {
                            a = addresses.nextElement();
                            log.info("      " + a.getHostAddress());
                            myIps.add(a.getHostAddress());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Warning, no IP found. All ENABLED services will run.", ex);
            myIps = null;
        }
        try {
            ObjectName nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.servers:type=HL7Server,index=2");
            mbs.registerMBean(new it.units.htl.dpacs.servers.HL7Server(mbs), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.servers:type=DicomServer,index=3");
            mbs.registerMBean(new it.units.htl.dpacs.servers.DicomServer(mbs), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.servers.queryRetrieve:type=QueryRetrieveSCP,index=4");
            mbs.registerMBean(new it.units.htl.dpacs.servers.queryRetrieve.QueryRetrieveSCP(mbs), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.servers.storage:type=StorageSCP,index=5");
            mbs.registerMBean(new it.units.htl.dpacs.servers.storage.StorageSCP(mbs), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.servers.mpps:type=MPPSSCP,index=8");
            mbs.registerMBean(new it.units.htl.dpacs.servers.mpps.MPPSSCP(mbs, isReportingEnabled), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.helpers:type=CompressionSCP,index=9");
            mbs.registerMBean(new it.units.htl.dpacs.helpers.CompressionSCP(mbs), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.helpers:type=ImageMaskingSCP,index=10");
            mbs.registerMBean(new it.units.htl.dpacs.helpers.ImageMaskingSCP(mbs), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.forwarder:type=Forwarder,index=11");
            mbs.registerMBean(new it.units.htl.dpacs.forwarder.ForwarderService(mbs), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.servers.worklist:type=WorklistService,index=12");
            mbs.registerMBean(new it.units.htl.dpacs.servers.worklist.WorklistService(mbs), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.postprocessing.verifier:type=StudiesVerifierBean,index=13");
            mbs.registerMBean(new it.units.htl.dpacs.postprocessing.verifier.StudiesVerifier(), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.atna:type=AuditLogService,index=14");
            mbs.registerMBean(it.units.htl.atna.AuditLogService.getInstance(), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.deletion:type=StudyEraserBean,index=15");
            mbs.registerMBean(new it.units.htl.dpacs.deletion.StudyEraser(), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.movement:type=StudyMovement,index=16"); // This moves studies nearline and offline
            mbs.registerMBean(new it.units.htl.dpacs.movement.StudyMovement(), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.postprocessing.studymove:type=StudyMoveBean,index=17");
            mbs.registerMBean(new it.units.htl.dpacs.postprocessing.studymove.StudyMove(), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            nomeOggettoMBean = new ObjectName("it.units.htl.dpacs.servers.hl7.comunication:type=Hl7CommunicationServer,index=18");
            mbs.registerMBean(new it.units.htl.dpacs.servers.hl7.comunication.Hl7CommunicationServer(), nomeOggettoMBean);
            log.info("Instantiated and registered to MBServer: " + nomeOggettoMBean.getCanonicalName());
            status = true;
        } catch (Exception e) {
            log.fatal("", e);
        }
        try {
            // Authentication information for the HTTP jmx Viewer
            AuthInfo authInfo1 = new AuthInfo("Administrator", "o3-dpacs");
            AuthInfo[] authInfo = { authInfo1 };
            HtmlAdaptorServer adaptor = new HtmlAdaptorServer(8082, authInfo);
            ObjectName adaptorName = new ObjectName("adaptor:protocol=HTTP");
            mbs.registerMBean(adaptor, adaptorName);
            log.info("Instantiated and registered to MBServer: " + adaptorName.getCanonicalName());
            adaptor.start();
            log.info("###### Ended instantiation of service of O3-DPACS #####");
            status = status && true;
        } catch (MalformedObjectNameException e) {
            log.error("", e);
            status = status && false;
        } catch (NullPointerException e) {
            log.error("", e);
            status = status && false;
        } catch (InstanceAlreadyExistsException e) {
            log.error("", e);
            status = status && false;
        } catch (MBeanRegistrationException e) {
            log.error("", e);
            status = status && false;
        } catch (NotCompliantMBeanException e) {
            log.error("", e);
            status = status && false;
        }
        isLoaded = status;
        return status;
    }

    /**
     * The method call "start" method for each managed bean. For manager DICOM servers (query, store etc) this mean loading setting, doing binding and other initialization stuff (assgning SOP Classes for each service) For DICOM and HL7 server, it will also start the server, on the predefined port
     * 
     * @return a boolean value indicating whether operations completed correctly
     */
    public boolean startServers() {
        Boolean status = false;
        log.info("###### Starting the O3-DPACS services ######");
        ServicesConfigurationHome sch = null;
        try {
            ServicesConfiguration s = new ServicesConfiguration();
            s.setEnabled(true);
            sch = new ServicesConfigurationHome();
            List<ServicesConfiguration> ss = sch.findByExample(s);
            for (ServicesConfiguration servicesConfiguration : ss) {
                Service servGen = ServicesManager.getInstance().getServiceByName(servicesConfiguration.getServiceName());
                if (servGen != null) {
                    servGen.setEnabled(true);
                }
            }
            for (ServicesConfiguration servicesConfiguration : ss) {
                Service servGen = ServicesManager.getInstance().getServiceByName(servicesConfiguration.getServiceName());
                if(servGen != null ) {
                    servGen.setRunFromIp(servicesConfiguration.getRunFromIp());
                    log.info("Has " + servGen.getName() + " started? " + startService(servGen));
                }
            }
            log.info("###### Ended starting process ######");
            status = true;
            serversStatus = true;
        } catch (Exception ex) {
            log.error("While starting o3-dpacs services", ex);
        }
        return status;
    }

    /**
     * The method call "stop" method for each managed bean. For DICOM and HL7 server, it will also stop the server, on the predefined port
     * 
     * @return a boolean value indicating whether operations completed correctly
     */
    public boolean stopServers() {
        log.info("###### Stopping the O3-DPACS services ######");
        Boolean status = false;
        try {
            ServicesConfiguration s = new ServicesConfiguration();
            s.setEnabled(true);
            ServicesConfigurationHome sch = new ServicesConfigurationHome();
            List<ServicesConfiguration> ss = sch.findByExample(s);
            for (ServicesConfiguration servicesConfiguration : ss) {
                Service servGen = ServicesManager.getInstance().getServiceByName(servicesConfiguration.getServiceName());
                if (servGen != null) {
                    try {
                        log.info("Has " + servGen.getName() + " been stopped? " + stopService(servGen));
                    } catch (Exception ex) {
                        if (!(ex.getCause() instanceof UnsupportedOperationException)) {
                            log.error("During stopping " + servGen.getName(), ex);
                            return false;
                        }
                    }
                }
            }
            status = true;
            log.info("###### The O3-DPACS services stopped ######");
        } catch (NullPointerException e) {
            log.error("While stopping services...", e);
        }
        serversStatus = false;
        return status;
    }

    public boolean reloadSettings() {
        Boolean status = false;
        try {
            log.info("###### Reloading O3-DPACS configurations ######");
            ServicesConfiguration s = new ServicesConfiguration();
            s.setEnabled(true);
            ServicesConfigurationHome sch = new ServicesConfigurationHome();
            List<ServicesConfiguration> ss = sch.findByExample(s);
            for (ServicesConfiguration servicesConfiguration : ss) {
                Service servGen = ServicesManager.getInstance().getServiceByName(servicesConfiguration.getServiceName());
                if (servGen != null) {
                    try {
                        ObjectName nomeOggettoMBean = new ObjectName(servGen.getMBeanName());
                        boolean res = (Boolean) mbs.invoke(nomeOggettoMBean, PacsService.RELOAD_SETTINGS, new Object[] {}, new String[] {});
                        log.info("Has " + servGen.getName() + " been reloaded? " + res);
                    } catch (Exception ex) {
                        if (!(ex.getCause() instanceof UnsupportedOperationException)) {
                            log.error("During reloading " + servGen.getName());
                            return false;
                        }
                    }
                }
            }
            status = true;
        } catch (Exception e) {
            status = false;
        }
        log.info("###### Finish Reloading ######");
        return status;
    }

    public boolean getStatus() {
        return serversStatus;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Return the current MbeanServer for O3-DPACS
     * 
     * @return the current Mbean Server
     * @throws java.lang.Exception
     */
    public MBeanServer getReferencedMBeanServer() throws Exception {
        if (mbs == null) {
            log.fatal("ServerBean is null");
        }
        log.info("Default Domain is " + mbs.getDefaultDomain());
        return mbs;
    }

    public HashMap<String, Boolean[]> getServicesStatus() throws RemoteException {
        HashMap<String, Boolean[]> servicesStatus = new HashMap<String, Boolean[]>();
        try {
            ServicesConfigurationHome sch = new ServicesConfigurationHome();
            List<ServicesConfiguration> ss = sch.getAll();
            for (ServicesConfiguration servicesConfiguration : ss) {
                Service servGen = ServicesManager.getInstance().getServiceByName(servicesConfiguration.getServiceName());
                if (servGen != null) {
                    try {
                        ObjectName nomeOggettoMBean = new ObjectName(servGen.getMBeanName());
                        //boolean res = (Boolean) mbs.invoke(nomeOggettoMBean, PacsService.GET_STATUS, null, null);
                        boolean res= (Boolean)mbs.invoke(nomeOggettoMBean, PacsService.GET_STATUS, new Object[] {}, new String[] {});
                        servicesStatus.put(servGen.getName(), new Boolean[] { servicesConfiguration.isEnabled(), res });
                    } catch (Exception ex) {
                    	log.error(ex.getMessage());
                    	log.error(ex.getCause());
                        log.error("During getting status " + servGen.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("While retrieving services status", e);
        }
        return servicesStatus;
    }

    /**
     * @param id
     *            : the id of the service that has to start. The list of service is in ServiceManager class
     * @return TRUE if the service is started correctly, FALSE otherwhyse
     * @see it.units.htl.dpacs.core.services.ServicesManager
     */
    private boolean startService(Service se) {
        Service startingService = sM.getServiceById(se.getId());
        if (startingService.getDependency() != null) {
            Integer[] upperServices = sM.getServiceById(se.getId()).getDependency();
            for (int i = 0; i < upperServices.length; i++) {
                try {
                    Service parent = sM.getServiceById(upperServices[i]);
                    if (parent.isEnabled()) {
                        ObjectName serviceParentName = new ObjectName(parent.getMBeanName());
                        Boolean isParentStarted = (Boolean) mbs.invoke(serviceParentName, PacsService.GET_STATUS, new Object[] {}, new String[] {});
                        if (!isParentStarted) {
                            // service isn't started yet
                            if (!startService(parent)) {
                                log.info("Unable to start: " + sM.getServiceById(i).getName());
                                return false;
                            }
                        }
                    } else {
                        log.fatal(parent.getName() + " <-- this service is disabled, I can't start --> " + startingService.getName());
                        return false;
                    }
                } catch (Exception e) {
                    log.error("Unable to retrieve " + se.getName() + " status.");
                    return false;
                }
            }
        }
        ObjectName nomeOggettoMBean = null;
        try {
            if (checkIfServiceCanRun(se)) {
                nomeOggettoMBean = new ObjectName(se.getMBeanName());
                mbs.invoke(nomeOggettoMBean, PacsService.START_SERVICE, new Object[] {}, new String[] {});
            } else {
                log.warn("The service " + se.getName() + " can not run from myIps! " + se.getRunFromIp());
                return false;
            }
        } catch (Exception e) {
            log.error("Unable to start: " + se.getName(), e);
            return false;
        }
        return true;
    }

    private boolean stopService(Service se) {
        if (se.getDependency() != null) {
            Integer[] upperServices = se.getDependency();
            for (int i = 0; i < upperServices.length; i++) {
                try {
                    Service parent = sM.getServiceById(upperServices[i]);
                    if (parent.isEnabled()) {
                        ObjectName serviceParentName = new ObjectName(parent.getMBeanName());
                        Boolean isParentStarted = (Boolean) mbs.invoke(serviceParentName, PacsService.GET_STATUS, new Object[] {}, new String[] {});
                        if (isParentStarted) {
                            // service is started yet
                            if (!stopService(parent)) {
                                log.info("Unable to stop: " + sM.getServiceById(i).getName());
                                return false;
                            }
                        }
                    } else {
                        log.fatal(parent.getName() + " <-- this service is disabled, I can't stop --> " + se.getName());
                        return false;
                    }
                } catch (Exception e) {
                    log.error("Unable to retrieve " + se.getName() + " status.");
                    return false;
                }
            }
        }
        ObjectName nomeOggettoMBean = null;
        try {
            if (checkIfServiceCanRun(se)) {
                nomeOggettoMBean = new ObjectName(se.getMBeanName());
                mbs.invoke(nomeOggettoMBean, PacsService.STOP_SERVICE, new Object[] {}, new String[] {});
            } else {
                log.error("This services cna not run from myIps: " + se.getName());
                return false;
            }
        } catch (Exception e) {
            if (!(e.getCause() instanceof UnsupportedOperationException)) {
                log.error("Unable to stop: " + se.getName(), e);
                return false;
            }
        }
        return true;
    }

    /**
     * The method created an MBean server, create a registry and registers a link to the server in the registry in order to allow it to be remotely found
     * 
     * @throws CreateException
     */
    private void createMBeanServer() {
        String jmxPort = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.JMX_CONNECTOR_PORT);
        if (jmxPort == null) {
            log.error("NO JMX CONNECTOR PORT CONFIGURED!!");
            throw new RuntimeException("NO JMX CONNECTOR PORT CONFIGURED!!");
        }
        try {
            if (mbs == null) {
                if (LocateRegistry.getRegistry(jmxPort) == null) {
                    LocateRegistry.createRegistry(Integer.parseInt(jmxPort));
                }
                String mbsName = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.MBEANSERVER_NAME);
                if (!MBeanServerFactory.findMBeanServer(mbsName).isEmpty()) {
                    mbs = MBeanServerFactory.findMBeanServer(mbsName).get(0);
                } else {
                    mbs = MBeanServerFactory.createMBeanServer(mbsName);
                }
            }
        } catch (RemoteException e) {
            log.error("", e);
        }
        log.info("MBean Server created. Default Domain is " + mbs.getDefaultDomain());
    }

    private boolean checkIfServiceCanRun(Service sc) {
        if (myIps != null) {
            if (sc.getRunFromIp() != null) {
                return myIps.contains(sc.getRunFromIp());
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
