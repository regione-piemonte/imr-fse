/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MBeanServerUtils {
    public static String mBeanServerDpacsName;
    private static Log log = LogFactory.getLog(MBeanServerUtils.class);

    public MBeanServerUtils() {
    }

    public static MBeanServer findMBeanServerByName(String name) {
        MBeanServer ret = null;
        ArrayList<MBeanServer> al = MBeanServerFactory.findMBeanServer(null);
        for (int i = 0; i < al.size(); i++) {
            MBeanServer currentMBS = al.get(i);
            String mbsName = null;
            try {
                mbsName = currentMBS.getDefaultDomain();
            } catch (Exception e) {
                log.error("Error retrieving MBeanServer name", e);
            }
            if (name.equals(mbsName)) {
                return currentMBS;
            }
        }
        return ret;
    }

    public static MBeanServer getDpacsMBeanServer() {
        if (mBeanServerDpacsName == null) {
            mBeanServerDpacsName = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.MBEANSERVER_NAME);
        }
        return findMBeanServerByName(mBeanServerDpacsName);
    }
}
