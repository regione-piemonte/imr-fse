/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.startup;

import it.units.htl.dpacs.core.ServerRemote;

import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class O3Dpacs {
    private Log log = LogFactory.getLog(O3Dpacs.class);

    public boolean startServer() {
        try {
            log.info("=======> O3-DPACS starting up <=======");
//            ServerRemote serverManager = InitialContext.doLookup("o3-dpacs/ServerBean/remote");
            ServerRemote serverManager = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/ServerBean!it.units.htl.dpacs.core.ServerRemote");
            serverManager.startServers();
            log.info("=======> O3-DPACS started up! <=======");
            return true;
        } catch (Exception ex) {
            log.error("O3-DPACS", ex);
            return false;
        }
    }
}
