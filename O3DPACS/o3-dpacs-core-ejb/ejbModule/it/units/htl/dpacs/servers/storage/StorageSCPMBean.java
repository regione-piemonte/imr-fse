/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.storage;

import it.units.htl.dpacs.core.services.PacsService;

import javax.management.ObjectName;
import javax.management.MBeanServer;

public interface StorageSCPMBean extends PacsService{
	public void setSuccessOnDup(boolean yesOrNo);

	public boolean getSuccessOnDup();

	public void setUrlToDups(String arg);

	public String getUrlToDups();

	public String getDumpDirectory();

	public void setDumpDirectory(String dd);

	public StorageServer getStorageServer();

	public MBeanServer getMbServer();

	// public ObjectName getAuditLoggerName();

	/**
	 * Sets the auditLoggerName attribute of the StorageSCPMBean object
	 * 
	 * @param auditLogName
	 *            The new auditLoggerName value
	 */
	public void setAuditLoggerName(ObjectName auditLogName);

	/**
	 * Gets the dcmServerName attribute of the StorageSCPMBean object
	 * 
	 * @return The dcmServerName value
	 */
	public ObjectName getDcmServerName();

	/**
	 * Sets the dcmServerName attribute of the StorageSCPMBean object
	 * 
	 * @param dcmServerName
	 *            The new dcmServerName value
	 */
	public void setDcmServerName(ObjectName dcmServerName);

	/**
	 * Gets the acTimeout attribute of the StorageCommitServer object
	 * 
	 * @return The acTimeout value
	 */
	public int getAcTimeout();

	/**
	 * Sets the acTimeout attribute of the StorageCommitServer object
	 * 
	 * @param timeout
	 *            The new acTimeout value
	 */
	public void setAcTimeout(int timeout);

	/**
	 * Gets the dimseTimeout attribute of the StorageCommitServer object
	 * 
	 * @return The dimseTimeout value
	 */
	public int getDimseTimeout();

	/**
	 * Sets the dimseTimeout attribute of the StorageCommitServer object
	 * 
	 * @param timeout
	 *            The new dimseTimeout value
	 */
	public void setDimseTimeout(int timeout);

	/**
	 * Gets the soCloseDelay attribute of the StorageCommitServer object
	 * 
	 * @return
	 */
	public int getSoCloseDelay();

	/**
	 * Sets the soCloseDelay attribute of the StorageCommitServer object
	 * 
	 * @param delay
	 *            The new soCloseDelay value
	 */
	public void setSoCloseDelay(int delay);
	
	public String getSecondsBetweenTimerChecks();
	public void setSecondsBetweenTimerChecks(String secondsBetweenTimerChecks);
	public String getMinutesAfterStudyConsideredFinished();
	public void setMinutesAfterStudyConsideredFinished(String minutesAfterStudyConsideredFinished);

	public String getDefaultIdIssuer();
}
