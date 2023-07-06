/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IpChecker {
    private static Log log = LogFactory.getLog(IpChecker.class);

    public static boolean isContainedInMyip(String ip) {
        if (ip != null) {
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                if (interfaces == null)
                    return true;
                else {
                    NetworkInterface i = null;
                    while (interfaces.hasMoreElements()) {
                        i = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = i.getInetAddresses();
                        if (addresses != null) {
                            InetAddress a = null;
                            while (addresses.hasMoreElements()) {
                                a = addresses.nextElement();
                                if (ip.equals(a.getHostAddress()))
                                    return true;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("Warning, no IP found. All ENABLED services will run.", ex);
            }
            return false;
        } else {
            return true;
        }
    }
}
