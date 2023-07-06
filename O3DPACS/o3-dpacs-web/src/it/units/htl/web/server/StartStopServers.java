/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.server;


import it.units.htl.dpacs.core.ServerRemote;
import it.units.htl.web.Study.StudyFinder;
import it.units.htl.web.ui.messaging.MessageManager;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.CreateException;
import javax.faces.event.ActionEvent;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StartStopServers {
    private static String status = "Success";
    private static String isRunning = "Stopped";
    private static boolean firstStart = true;
    private Log log = LogFactory.getLog(StartStopServers.class);
    
    public static boolean isFirstStart() {
        return firstStart;
    }

    public static void setFirstStart(boolean firstStart) {
        StartStopServers.firstStart = firstStart;
    }

    public String getstatus() {
        return StartStopServers.status;
    }

    public void setstatus(String s) {
        StartStopServers.status = s;
    }

    public String getisRunning() {
        try {
            ServerRemote serverRemote = this.getConnection();
            if (serverRemote.getStatus()) {
                isRunning = "Started";
            }
        } catch (RemoteException e) {
            log.error(e);
            isRunning = "Have an exception...see the log!";
        }
        return StartStopServers.isRunning;
    }

    public void setisRunning(String s) {
        StartStopServers.isRunning = s;
    }

    public void start(ActionEvent ae) {
        try {
            ServerRemote serverRemote = this.getConnection();
            if (!serverRemote.getStatus()) {
                if (!serverRemote.isLoaded()) {
                        serverRemote.loadDPACS();
                }
                serverRemote.startServers();
                isRunning = "Started";
                status = "success";
            }
            MessageManager.getInstance().setMessage("serverStartedCorrectly", null);
        } catch (Exception e) {
            log.error("While restarting service", e);
            MessageManager.getInstance().setMessage("serverNotStart", new String[] { e.getStackTrace()[0].getMethodName() + " " + e.getStackTrace()[0].getLineNumber() });
            status = "failed";
            isRunning = "Have an exception...see the log!";
        }
    }

    public void stop(ActionEvent ae) {
        status = "success";
        ServerRemote serverRemote = this.getConnection();
        try {
            if (serverRemote.getStatus()) {
                serverRemote.stopServers();
            }
            isRunning = "Stopped";
        } catch (RemoteException e) {
            log.error("", e);
            status = "failed";
            isRunning = "Have an exception...see the log!";
        }
    }

    public void reload(ActionEvent ae) {
        ServerRemote serverRemote = this.getConnection();
        try {
            if (serverRemote.getStatus()) {
                if (!serverRemote.reloadSettings()) {
                    status = "failed";
                    MessageManager.getInstance().setMessage("serverLoadedBadly", null);
                } else {
                    MessageManager.getInstance().setMessage("serverLoadedCorrectly", null);
                    status = "success";
                }
            } else {
                status = "failed";
                MessageManager.getInstance().setMessage("serverLoadedBadly", null);
            }
        } catch (RemoteException e) {
            log.error("", e);
            MessageManager.getInstance().setMessage("serverLoadedBadly", null);
            status = "failed";
        }
        StudyFinder.loadThumbsDimension();
    }

    public Service[] getServicesStatus() {
        ServerRemote serverRemote = this.getConnection();
        ArrayList<Service> serviceStatus = new ArrayList<Service>();
        try {
            HashMap<String, Boolean[]> res = serverRemote.getServicesStatus();
            for (String serviceName : res.keySet()) {
                Service ss = new Service();
                ss.setName(serviceName);
                ss.setEnabled(res.get(serviceName)[0]);
                ss.setStatus(res.get(serviceName)[1]);
                serviceStatus.add(ss);
            }
        } catch (Exception e) {
            log.error("While retrieving servicesStatus", e);
        }
        Service[] out = new Service[serviceStatus.size()];
        serviceStatus.toArray(out);
        return out;
    }

    protected ServerRemote getConnection() {
        ServerRemote serverRemote = null;
        try {
//            serverRemote = InitialContext.doLookup("o3-dpacs/ServerBean/remote");
            serverRemote = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/ServerBean!it.units.htl.dpacs.core.ServerRemote");
        } catch (NamingException e) {
            log.error("", e);
        } 
        return serverRemote;
    }
}