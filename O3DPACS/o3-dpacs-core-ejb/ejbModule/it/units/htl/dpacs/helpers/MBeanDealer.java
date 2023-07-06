/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.server.ServerFactory;

import it.units.htl.dpacs.servers.DicomServer;

public class MBeanDealer {
    static final Log log = LogFactory.getLog(MBeanDealer.class);

    public static String getPropertyFromMbean(ObjectName objectName, String method) {
        String result = null;
        MBeanServer mbs = null;
        mbs = MBeanServerUtils.getDpacsMBeanServer();
        Exception lastException = null;
        {
            try {
            	MBeanInfo mbi = mbs.getMBeanInfo(objectName);
            	
                for (int i = 0; i < mbi.getAttributes().length; i++) {
                    String attributeName = mbi.getAttributes()[i].getName();
                    if(attributeName.equals(method))
                    {
                    	result = mbs.getAttribute(objectName, attributeName).toString();
                    	break;
                    }
                }

                //result = (String) mbs.invoke(objectName, method, null, null);

            } catch (Exception ex) {
                lastException = ex;
            }
        }
        if (result == null) {
            log.error("", lastException);
            return null;
        }
        return result;
    }
}
