/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.lang.reflect.Method;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import it.units.htl.dpacs.core.*;
import it.units.htl.dpacs.dao.*;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.net.DcmServiceException;

/**
 * The class keeps the parameters for the compression agent
 * 
 * @author Carrara
 */
public class CompressionSCP implements CompressionSCPMBean {
    private static Log log = LogFactory.getLog(CompressionSCP.class);
    private static DicomDbDealer bean = null;
    private Compression comp = null;
    private boolean serviceStatus;
    /**
     * The dir mandatory to perform compression and decompression
     */
    public static String TempDir;

    /**
     * Constructor: take the Mbean server to register itself
     * 
     * @param serv
     *            the Mbean server
     * @throws org.dcm4che.net.DcmServiceException
     */
    public CompressionSCP(MBeanServer serv) throws DcmServiceException {
        // this.server = serv;
        this.getInstance();
        if (bean == null) {
            try {
                log.debug("About to create DicomDBDealer");
                bean = InitialContext.doLookup(BeansName.LDicomDbDealer);
            } catch (NamingException nex){
                log.fatal("", nex);
                throw new DcmServiceException(org.dcm4che.dict.Status.ProcessingFailure);
            }
        }
    }

    public boolean startService() {
        try {
            loadConfiguration();
        } catch (Exception ex) {
            log.fatal("While starting CompressionSCP Service: ", ex);
            serviceStatus = false;
            return false;
        }
        serviceStatus = true;
        return true;
    }

    /**
     * takes the name of the class, goes to the clssname.xml file and loads the settings to the Mbean
     */
    @SuppressWarnings("rawtypes")
    private void loadConfiguration() {
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        VarLoader xmlLoader = null;
        ServicesConfiguration sc = sch.findByServiceName("CompressionSCP");
        xmlLoader = new VarLoader(sc.getConfiguration(), VarLoader.FROMSTRING);
        // String vectior with attributes
        String[] nameOfAttributes = xmlLoader.getNameOfValues();
        String[] valuesOfAttributes = xmlLoader.getStringValues();
        int error = 0;
        for (int i = 0; i < nameOfAttributes.length; i++) {
            error = 0;
            try {
                Class[] argomento = { String.class };
                Method method = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
                Object[] value = { new String(valuesOfAttributes[i]) };
                method.invoke(this, value);
            } catch (java.lang.NoSuchMethodException eNSME) {
                error = 1;
            } catch (java.lang.IllegalAccessException eIAE) {
                error = 1;
            } catch (java.lang.reflect.InvocationTargetException eITE) {
                error = 1;
            } catch (Exception ex) {
                ex.printStackTrace();
            } /* __*__ */// end...catch
            try {
                Class[] argomento = { int.class };
                Method method = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
                Object[] value = { new Integer(Integer.parseInt(valuesOfAttributes[i])) };
                method.invoke(this, value);
            } catch (java.lang.NoSuchMethodException eNSME) {
                error += 1;
            } catch (java.lang.IllegalAccessException eIAE) {
                error += 1;
            } catch (java.lang.reflect.InvocationTargetException eITE) {
                error += 1;
            } catch (Exception ex) {
                ex.printStackTrace();
            } /* __*__ */// end...catch
            try {
                Class[] argomento = { boolean.class };
                Method method = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
                Object[] value = { new Boolean(valuesOfAttributes[i]) };
                method.invoke(this, value);
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
                Method method = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
                Object[] value = { new ObjectName(valuesOfAttributes[i]) };
                method.invoke(this, value);
            } catch (java.lang.NoSuchMethodException eNSME) {
                if (error == 3) {
                    log.debug("No method the class, set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>value</NomeAttributo>" + nameOfAttributes[i]);
                }
            } catch (java.lang.IllegalAccessException eIAE) {
                if (error == 3) {
                    log.debug("No method the class, set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>value</NomeAttributo>" + nameOfAttributes[i]);
                }
            } catch (java.lang.reflect.InvocationTargetException eITE) {
                if (error == 3) {
                    log.debug("No method the class, set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>value</NomeAttributo>" + nameOfAttributes[i]);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String getTempDir() {
        return TempDir;
    }

    public Compression getInstance() {
        if (comp == null) {
            comp = new Compression();
        }
        return comp;
    }

    public void setTempDir(String dir) {
        TempDir = dir;
    }

    public static String getCompressionTransferSyntax(String aetitle) {
        return bean.getCompressionTransferSyntaxUID(aetitle);
    }

    public boolean statusService() {
        return serviceStatus;
    }


    public boolean reloadSettings() throws Exception {
        return startService();
    }

    public boolean stopService() throws Exception {
        throw new UnsupportedOperationException();
    }
}