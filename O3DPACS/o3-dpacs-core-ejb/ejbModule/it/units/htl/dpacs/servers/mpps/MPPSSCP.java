/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.mpps;

import it.units.htl.dpacs.core.VarLoader;
import it.units.htl.dpacs.dao.DicomDbDealer;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.util.SSLContextAdapter;

public class MPPSSCP implements MPPSSCPMBean {
    // for reporting
    boolean reportingEnabled = false;
    private boolean serviceStatus;
    // private static String loggersConfFile=null;
    // private static String loggingDir=null;
    private final static String[] NATIVE_TS = {
            UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian
            };
    private static ArrayList<String> MPPS_AS = new ArrayList<String>(1);
    final static Map<String, Object> dumpParam = new HashMap<String, Object>(5);
    static {
        dumpParam.put("maxlen", new Integer(128));
        dumpParam.put("vallen", new Integer(64));
        dumpParam.put("prefix", "\t");
    }
    // Attributes ----------------------------------------------------
    private MBeanServer server;
    private static Log log = LogFactory.getLog(MPPSSCP.class);
    private ObjectName dcmServerName;
    private DcmHandler dcmHandler;
    private MPPSServer mpps = new MPPSServer(this);
    private DicomDbDealer bean = null;

    public MPPSSCP(MBeanServer serv, boolean isRepEnabled) { // Constructor
        this.server = serv;
        this.reportingEnabled = isRepEnabled;
        log.debug("Reporting Manager Module activation is " + isRepEnabled);
    }

    // Methods
    void logDataset(String prompt, Dataset ds) {
        try {
            StringWriter w = new StringWriter();
            w.write(prompt);
            ds.dumpDataset(w, dumpParam);
        } catch (Exception e) {
            // log.warn("Failed to dump dataset", e);
        }
    }

    // MPPSSCPMBean implementation ---------------------------
    /**
     * Gets the dcmServerName attribute of the MPPSSCP object
     * 
     *@return The dcmServerName value
     */
    public ObjectName getDcmServerName() {
        return dcmServerName;
    }

    /**
     * Sets the dcmServerName attribute of the MPPSSCP object
     * 
     *@param dcmServerName
     *            The new dcmServerName value
     */
    public void setDcmServerName(ObjectName dcmServerName) {
        this.dcmServerName = dcmServerName;
    }

    /**
     * Gets the acTimeout attribute of the StgCmtService object
     * 
     *@return The acTimeout value
     */
    public int getAcTimeout() {
        return mpps.getAcTimeout();
    }

    /**
     * Sets the acTimeout attribute of the StgCmtService object
     * 
     *@param timeout
     *            The new acTimeout value
     */
    public void setAcTimeout(int timeout) {
        mpps.setAcTimeout(timeout);
    }

    /**
     * Gets the dimseTimeout attribute of the StgCmtService object
     * 
     *@return The dimseTimeout value
     */
    public int getDimseTimeout() {
        return mpps.getDimseTimeout();
    }

    /**
     * Sets the dimseTimeout attribute of the StgCmtService object
     * 
     *@param timeout
     *            The new dimseTimeout value
     */
    public void setDimseTimeout(int timeout) {
        mpps.setDimseTimeout(timeout);
    }

    /**
     * Gets the soCloseDelay attribute of the StgCmtService object
     * 
     *@return The soCloseDelay value
     */
    public int getSoCloseDelay() {
        return mpps.getSoCloseDelay();
    }

    /**
     * Sets the soCloseDelay attribute of the StgCmtService object
     * 
     *@param delay
     *            The new soCloseDelay value
     */
    public void setSoCloseDelay(int delay) {
        mpps.setSoCloseDelay(delay);
    }

    public String getDestinationOne() {
        return mpps.getDestinationOne();
    }

    public void setDestinationOne(String destinationOne) {
        mpps.setDestinationOne(destinationOne);
    }

    public String getDestinationTwo() {
        return mpps.getDestinationTwo();
    }

    public void setDestinationTwo(String destinationTwo) {
        mpps.setDestinationTwo(destinationTwo);
    }

    public int getMinutesToWait() {
        return (int) Math.floor(mpps.getMinutesToWait() / 60000);
    }

    public void setMinutesToWait(int minutesToWait) {
        mpps.setMinutesToWait(minutesToWait);
    }

    public int getTimesToTryForward() {
        return mpps.getTimesToTryForward();
    }

    public void setTimesToTryForward(int timesToTryForward) {
        mpps.setTimesToTryForward(timesToTryForward);
    }

    /**
     * Description of the Method
     * 
     *@param s
     *            Description of the Parameter
     *@return Description of the Return Value
     */
    static String hypenToNull(String s) {
        return "-".equals(s) ? null : s;
    }

    /**
     * Description of the Method
     * 
     *@param s
     *            Description of the Parameter
     *@return Description of the Return Value
     */
    static String nullToHypen(String s) {
        return s == null ? "-" : s;
    }



    private void startServer() throws Exception {
        dcmHandler = (DcmHandler) server.getAttribute(dcmServerName, "DcmHandler");
        mpps.setSSLContextAdapter((SSLContextAdapter) server.getAttribute(dcmServerName, "SSLContextAdapter"));
        addSupportedSOPClasses();
        bindDcmServices();
        updatePolicy(NATIVE_TS);
    }

    /** Description of the Method */
    private void bindDcmServices() {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < MPPS_AS.size(); ++i) {
            services.bind((String) MPPS_AS.get(i), mpps);
        }
    }

    private void unbindDcmServices() {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < MPPS_AS.size(); ++i) {
            services.unbind((String) MPPS_AS.get(i));
        }
    }

    @SuppressWarnings("rawtypes")
    private void loadOnStart() {
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        VarLoader xmlLoader = null;
        ServicesConfiguration sc = sch.findByServiceName("MPPSSCP");
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
                log.debug("", ex);
            } // end...catch
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
                log.debug("", ex);
            } // end...catch
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
                log.debug("", ex);
            } // end...catch
            try {
                Class[] argomento = { ObjectName.class };
                Method metodo = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
                Object[] valore = { new ObjectName(valuesOfAttributes[i]) };
                metodo.invoke(this, valore);
            } catch (java.lang.NoSuchMethodException eNSME) {
                if (error == 3) {
                    log.debug("Non esiste il metodo in questa classe set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>Valore</NomeAttributo>" + nameOfAttributes[i]);
                }
            } catch (java.lang.IllegalAccessException eIAE) {
                if (error == 3) {
                    log.debug("Non esiste il metodo in questa classe set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>Valore</NomeAttributo>" + nameOfAttributes[i]);
                }
            } catch (java.lang.reflect.InvocationTargetException eITE) {
                if (error == 3) {
                    log.debug("Non esiste il metodo in questa classe set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>Valore</NomeAttributo>" + nameOfAttributes[i]);
                }
            } catch (Exception ex) {
                log.debug("", ex);
            }
        }
    }

    private int addSupportedSOPClasses() throws DcmServiceException {
        try {
            if (bean == null)
                try {
                    log.debug("About to create DicomDBDealer");
                    bean = InitialContext.doLookup(BeansName.LDicomDbDealer);
                }catch (NamingException nex) {
                    log.error("",nex);
                    throw new DcmServiceException(org.dcm4che.dict.Status.ProcessingFailure);
                } // end try...catch
            MPPS_AS = bean.loadAbstractSyntaxes(3);
        } catch (Exception ex) {
            log.debug("",ex);
        }
        return 0;
    }

    /**
     * Description of the Method
     * 
     *@param tsuids
     *            Description of the Parameter
     */
    private void updatePolicy(String[] tsuids) {
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        policy.putPresContext(UIDs.ModalityPerformedProcedureStep, tsuids);
    }

    public boolean statusService() {
        return serviceStatus;
    }

    public boolean reloadSettings() throws Exception {
        return startService();
    }
    

    public boolean startService() {
        try {
            if(serviceStatus){
                stopService();
            }
            loadOnStart();
            startServer();
            serviceStatus = true;
            return true;
        } catch (Exception ex) {
            log.fatal("While starting MPPS Service: ", ex);
            serviceStatus = false;
            return false;
        }
    } 

    public boolean stopService() throws Exception {
        updatePolicy(null);
        unbindDcmServices();
        dcmHandler = null;
        serviceStatus = false;
        return true;
    }
}
