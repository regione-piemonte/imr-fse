/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.queryRetrieve;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.util.SSLContextAdapter;

import it.units.htl.dpacs.dao.*;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.core.*;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

public class QueryRetrieveSCP implements QueryRetrieveSCPMBean {
    boolean anonimizedActivated = true;
    private final static String[] NATIVE_TS = {
            UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian
            };
    private static boolean oldestIfOffline = true;
    private static boolean moveForwardingEnabled = false;
    private boolean DepartmentEnabled;
    private MBeanServer server;
    private static Log log = LogFactory.getLog(QueryRetrieveSCP.class);
    private static ArrayList<String> MOVE_AS = new ArrayList<String>(5);
    private static ArrayList<String> QUERY_AS = new ArrayList<String>(5);
    final static Map<String, Object> dumpParam = new HashMap<String, Object>(5);
    static {
        dumpParam.put("maxlen", new Integer(128));
        dumpParam.put("vallen", new Integer(64));
        dumpParam.put("prefix", "\t");
    }
    private ObjectName auditLogName;
    private ObjectName dcmServerName;
    private DcmHandler dcmHandler;
    private FindServer find = new FindServer(this);
    private MoveServer move = new MoveServer(this);
    private DicomDbDealer bean = null;
    private int queryLimit;
    private boolean serviceStatus;
    private boolean isPartiallyAnonymized;

    public QueryRetrieveSCP(MBeanServer serv) { // Constructor
        this.server = serv;
        if (bean == null) {
            try {
                log.debug("About to create DicomDBDealer");
                bean = InitialContext.doLookup(BeansName.LDicomDbDealer);
            } catch (NamingException nex){
                log.error("Unable to create DicomDbDealer...", nex);
            }
        }
    }

    public boolean getDepartmentEnabled() {
        return DepartmentEnabled;
    }

    public void setDepartmentEnabled(boolean DepartmentEnabled) {
        this.DepartmentEnabled = DepartmentEnabled;
    }

    public boolean getanonimizedActivated() {
        return anonimizedActivated;
    }

    public void setanonimizedActivated(boolean b) {
        anonimizedActivated = b;
    }

    /**
     * Gets the auditLoggerName attribute of the QueryRetrieveSCP object
     * 
     *@return The auditLoggerName value
     */
    public ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public int getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
    }

    /**
     * Gets the dcmServerName attribute of the QueryRetrieveSCP object
     * 
     *@return The dcmServerName value
     */
    public ObjectName getDcmServerName() {
        return dcmServerName;
    }

    /**
     * Sets the dcmServerName attribute of the QueryRetrieveSCP object
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
        return move.getAcTimeout();
    }

    /**
     * Sets the acTimeout attribute of the StgCmtService object
     * 
     *@param timeout
     *            The new acTimeout value
     */
    public void setAcTimeout(int timeout) {
        move.setAcTimeout(timeout);
    }

    /**
     * Gets the dimseTimeout attribute of the StgCmtService object
     * 
     *@return The dimseTimeout value
     */
    public int getDimseTimeout() {
        return move.getDimseTimeout();
    }

    /**
     * Sets the dimseTimeout attribute of the StgCmtService object
     * 
     *@param timeout
     *            The new dimseTimeout value
     */
    public void setDimseTimeout(int timeout) {
        move.setDimseTimeout(timeout);
    }

    /**
     * Gets the soCloseDelay attribute of the StgCmtService object
     * 
     *@return The soCloseDelay value
     */
    public int getSoCloseDelay() {
        return move.getSoCloseDelay();
    }

    /**
     * Sets the soCloseDelay attribute of the StgCmtService object
     * 
     *@param delay
     *            The new soCloseDelay value
     */
    public void setSoCloseDelay(int delay) {
        move.setSoCloseDelay(delay);
    }

    public boolean isPartiallyAnonymized(){
        return isPartiallyAnonymized;
    }
    
    
    private void startServer() throws Exception {
        dcmHandler = (DcmHandler) server.getAttribute(dcmServerName, "DcmHandler");
        move.setSSLContextAdapter((SSLContextAdapter) server.getAttribute(dcmServerName, "SSLContextAdapter"));
        addSupportedSOPClasses();
        bindDcmServices();
        updatePolicy(NATIVE_TS);
    }


    private void bindDcmServices() {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < QUERY_AS.size(); ++i) {
            services.bind((String) QUERY_AS.get(i), find);
        }
        for (int i = 0; i < MOVE_AS.size(); ++i) {
            services.bind((String) MOVE_AS.get(i), move);
        }
    }

    /** Description of the Method */
    private void unbindDcmServices() {
        DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
        for (int i = 0; i < QUERY_AS.size(); ++i) {
            services.unbind((String) QUERY_AS.get(i));
        }
        for (int i = 0; i < MOVE_AS.size(); ++i) {
            services.unbind((String) MOVE_AS.get(i));
        }
    }

    /**
     * Description of the Method
     * 
     *@param tsuids
     *            Description of the Parameter
     */
    private void updatePolicy(String[] tsuids) {        
        AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
        for (int i = 0; i < QUERY_AS.size(); ++i) {
            policy.putPresContext((String) QUERY_AS.get(i), tsuids);
        }
        for (int i = 0; i < MOVE_AS.size(); ++i) {
            policy.putPresContext((String) MOVE_AS.get(i), tsuids);
        }
    }

    @SuppressWarnings("rawtypes")
    private void loadOnStart() {
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        VarLoader xmlLoader = null;
        ServicesConfiguration sc = sch.findByServiceName("QueryRetrieveSCP");
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
                log.debug("", ex);
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
                log.debug("", ex);
            }
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
        
        if(bean != null){
            isPartiallyAnonymized = bean.isPartiallyAnonymizedInstallation();
        }else{
            log.warn("Unable to know if this installation is PARTIALLY ANONYMIZED!!");
            isPartiallyAnonymized = false;
        }
    } 

    private int addSupportedSOPClasses() {
        try {
            QUERY_AS = bean.loadAbstractSyntaxes(0);
            MOVE_AS = bean.loadAbstractSyntaxes(1);
        } catch (Exception ex) {
                log.error("", ex);
        }
        return 0;
    }

    public void setOldestIfOffline(boolean arg) {
        oldestIfOffline = arg;
    }

    public boolean getOldestIfOffline() {
        return oldestIfOffline;
    }

    public void setMoveForwardingEnabled(boolean arg) {
        moveForwardingEnabled = arg;
    }

    public boolean getMoveForwardingEnabled() {
        return moveForwardingEnabled;
    }

    public FindServer getFindServer() {
        return find;
    }

    public MoveServer getMoveServer() {
        return move;
    }

    public boolean startService() {
        try {
            if(serviceStatus){
                stopService();
            }
            loadOnStart();
            startServer();
        } catch (Exception ex) {
            log.fatal("Failed to start Q/R Service due to: ", ex);
            serviceStatus = false;
            return false;
        }
        serviceStatus = true;
        return true;
    }

    public boolean stopService() throws Exception {
        if(serviceStatus){
            updatePolicy(null);
            unbindDcmServices();
            dcmHandler = null;
            serviceStatus = false;
        }
        return true;
    }
    
    public boolean statusService() {
        return serviceStatus;
    }

    public boolean reloadSettings() throws Exception {
        return startService();
    }
}
