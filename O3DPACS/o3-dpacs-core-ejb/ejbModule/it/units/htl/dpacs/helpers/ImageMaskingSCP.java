/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.awt.Point;
import java.lang.reflect.Method;
import java.util.Vector;

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
 * @author Carrara
 */
public class ImageMaskingSCP implements ImageMaskingSCPMBean {
    private static Log log = LogFactory.getLog(ImageMaskingSCP.class);
    private boolean serviceStatus;
    private ObjectName dcmServerName;
    private static DicomDbDealer bean = null;
    public static String TempDir;
    

    public ImageMaskingSCP(MBeanServer serv) throws DcmServiceException {
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

    public ObjectName getDcmServerName() {
        return dcmServerName;
    }

    public void setDcmServerName(ObjectName dcmServerName) {
        this.dcmServerName = dcmServerName;
    }

    public boolean startService() {
        try {
            loadConfigs();
            serviceStatus = true;
            return true;
        } catch (Exception ex) {
            log.fatal("While starting ImageMasking service. Fix the configuration! ", ex);
            serviceStatus = false;
            return false;
        }
    }

    private void loadConfigs() {
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        VarLoader xmlLoader = null;
        ServicesConfiguration sc = sch.findByServiceName("ImageMaskingSCP");
        xmlLoader = new VarLoader(sc.getConfiguration(), VarLoader.FROMSTRING);
        /* __*__ *//* Vettore di stringhe con i nomi degli attributi */
        String[] nameOfAttributes = xmlLoader.getNameOfValues();
        /* __*__ *//* Vettore di stringhe con i valori degli attributi */
        String[] valuesOfAttributes = xmlLoader.getStringValues();
        /* __*__ *//* Carica i valori degli Attributi */
        int error = 0;
        for (int i = 0; i < nameOfAttributes.length; i++) {
            error = 0;
            try {
                @SuppressWarnings("rawtypes")
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
            } /* __*__ */// end...catch
            try {
                @SuppressWarnings("rawtypes")
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
            } /* __*__ */// end...catch
            try {
                @SuppressWarnings("rawtypes")
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
            } /* __*__ */// end...catch
            try {
                @SuppressWarnings("rawtypes")
                Class[] argomento = { ObjectName.class };
                Method metodo = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
                Object[] valore = { new ObjectName(valuesOfAttributes[i]) };
                metodo.invoke(this, valore);
            } catch (java.lang.NoSuchMethodException eNSME) {
                if (error == 3) {
                    // ---NomeAttributo>"+nameOfAttributes[i]);
                }
            } catch (java.lang.IllegalAccessException eIAE) {
                if (error == 3) {
                    // ---NomeAttributo>"+nameOfAttributes[i]);
                }
            } catch (java.lang.reflect.InvocationTargetException eITE) {
                if (error == 3) {
                    // ---NomeAttributo>"+nameOfAttributes[i]);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } /* __*__ */// end...catch
        }
    }

    public String getTempDir() {
        return TempDir;
    }


    /**
     * Parses a String of parameters. This String contains the coordinates of the rectangles to be painted. Every rectangle is defined by four integer, which represent the x/y coordinate of the highest and the lowest vertex. The coordinates are separated by a comma. Every group of coordinates, relative to every rectangle, is separated from the others by a semicolon.<br>
     * <br>
     * Example: the String <code>0,0,100,100;100,100,200,300</code> represents the coordinates of two rectangles: the first that goes from point (0,0) to point (100,100) and the second that goes from point (100,100) to point (200,300).
     * 
     * @param parameters
     *            the string that must be parsed
     * @return a Vector of RoiMeasure, containing the coordinates of the black rectangles to be painted on the image.
     */
    public static Vector<RoiMeasure> parseMaskParameters(String parameters) {
        Vector<RoiMeasure> vect = new Vector<RoiMeasure>();
        if (parameters.equals(""))
            return vect;
        String[] rects = parameters.split(";");
        RoiMeasure roi = null;
        for (int i = 0; i < rects.length; i++) {
            String rect = rects[i];
            String[] points = rect.split(","); // they must be 4!!!
            int x1 = Integer.parseInt(points[0]);
            int y1 = Integer.parseInt(points[1]);
            int width = Integer.parseInt(points[2]);
            int height = Integer.parseInt(points[3]);
            roi = new RoiMeasure();
            roi.setP1(new Point(x1, y1));
            roi.setSize(width, height);
            vect.add(roi);
        }
        return vect;
    }

    public static boolean isImageMaskingEnabled(String aeTitle) {
        return bean.isImageMaskingEnabled(aeTitle);
    }

    public static MaskingParameter[] getMaskTags(String aeTitle, String modality) {
        return bean.getMaskTags(aeTitle, modality);
    }

    public boolean statusService() {
        return serviceStatus;
    }

    public boolean reloadSettings() throws Exception {
        return startService();
    }

    public boolean stopService() throws Exception {
        throw new UnsupportedOperationException("This method is not supported by this service.");
    }
}
