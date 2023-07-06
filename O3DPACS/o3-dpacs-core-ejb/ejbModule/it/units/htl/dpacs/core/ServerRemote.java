/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.core;

import java.rmi.RemoteException;
import java.util.HashMap;

import javax.ejb.Remote;
import javax.management.MBeanServer;

@Remote
public interface ServerRemote{
    public boolean loadDPACS()throws RemoteException;
    public boolean reloadSettings()throws RemoteException;
    public void stopMbeanServer() throws RemoteException;
    public MBeanServer getReferencedMBeanServer() throws RemoteException,Exception;
    public boolean startServers() throws java.rmi.RemoteException;
    public boolean stopServers() throws java.rmi.RemoteException;
    public boolean getStatus() throws RemoteException;
    public boolean isLoaded() throws RemoteException;
    public HashMap<String, Boolean[]> getServicesStatus() throws RemoteException; 
}
