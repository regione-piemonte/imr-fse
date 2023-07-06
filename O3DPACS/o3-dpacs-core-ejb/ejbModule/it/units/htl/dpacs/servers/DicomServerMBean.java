/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers;

import it.units.htl.dpacs.core.services.PacsService;

import java.util.ArrayList;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.util.SSLContextAdapter;

/**
 * Interface to DICOM server MBean
 * 
 * @author Mbe
 */
public interface DicomServerMBean extends PacsService {

	public String getProtocolName();

	public void setProtocolName(String name);

	int getPort();

	void setPort(int port);

	public SSLContextAdapter getSSLContextAdapter();

	public DcmHandler getDcmHandler();

	public int getRqTimeout();

	public void setRqTimeout(int rqTimeout);

	public int getDimseTimeout();

	public void setDimseTimeout(int dimseTimeout);

	public int getSoCloseDelay();

	public void setSoCloseDelay(int soCloseDelay);

	public int getMaxClients();

	public void setMaxClients(int maxClients);

	public int getNumClients();

	public String getCallingAETs();

	public void setCallingAETs(String callingAETs);

	public String getCalledAETs();

	public void setCalledAETs(String calledAETs);

	public int getMaxPDULength();

	public void setMaxPDULength(int maxPDULength);

	public String getKeyFile();

	public void setKeyFile(String keyFile);

	public void setKeyPasswd(String keyPasswd);

	public int getNumIdleThreads();

	public String getCacertsFile();

	public void setCacertsFile(String cacertsFile);

	public void setIsCallingLockedonKnownnodes(boolean isCallingLocked);

	public boolean getIsCallingLockedonKnownnodes();

	public void setCacertsPasswd(String cacertsPasswd);

	public ArrayList<Long> getMemoryStatistics();

	public int getRightNumClients();

	public void setRightNumClients(int numClients);
	
	public String getFileHasherBufferSizeInKb();
	
	public void setFileHasherBufferSizeInKb(String fileHasherBufferSizeInKb);
	
	public String getHashAlgorithm();
	
	public void setHashAlgorithm(String hashAlgorithm);
	
}
