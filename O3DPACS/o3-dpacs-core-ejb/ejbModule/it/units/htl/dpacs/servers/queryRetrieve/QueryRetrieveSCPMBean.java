/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.queryRetrieve;

import it.units.htl.dpacs.core.services.PacsService;

import javax.management.ObjectName;

public interface QueryRetrieveSCPMBean extends PacsService{

	public boolean getDepartmentEnabled();

	public void setDepartmentEnabled(boolean DepartmentEnabled);;

	// by marco
	public FindServer getFindServer();

	public MoveServer getMoveServer();


	/**
	 * Gets the dcmServerName attribute of the QueryRetrieveSCPMBean object
	 * 
	 * @return The dcmServerName value
	 */
	public ObjectName getDcmServerName();

	/**
	 * Sets the dcmServerName attribute of the QueryRetrieveSCPMBean object
	 * 
	 * @param dcmServerName
	 *            The new dcmServerName value
	 */
	public void setDcmServerName(ObjectName dcmServerName);

	public int getQueryLimit();

	public void setQueryLimit(int queryLimit);

	/**
	 * Gets the acTimeout attribute of the StgCmtService object
	 * 
	 * @return The acTimeout value
	 */
	public int getAcTimeout();

	/**
	 * Sets the acTimeout attribute of the StgCmtService object
	 * 
	 * @param timeout
	 *            The new acTimeout value
	 */
	public void setAcTimeout(int timeout);

	/**
	 * Gets the dimseTimeout attribute of the StgCmtService object
	 * 
	 * @return The dimseTimeout value
	 */
	public int getDimseTimeout();

	/**
	 * Sets the dimseTimeout attribute of the StgCmtService object
	 * 
	 * @param timeout
	 *            The new dimseTimeout value
	 */
	public void setDimseTimeout(int timeout);

	/**
	 * Gets the soCloseDelay attribute of the StgCmtService object
	 * 
	 * @return The soCloseDelay value
	 */
	public int getSoCloseDelay();

	/**
	 * Sets the soCloseDelay attribute of the StgCmtService object
	 * 
	 * @param delay
	 *            The new soCloseDelay value
	 */
	public void setSoCloseDelay(int delay);

	public void setOldestIfOffline(boolean arg);

	public boolean getOldestIfOffline();

	public void setMoveForwardingEnabled(boolean arg);

	public boolean getMoveForwardingEnabled();
	
	public boolean isPartiallyAnonymized();

}
