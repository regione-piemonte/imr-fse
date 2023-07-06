/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers;

import it.units.htl.dpacs.core.services.PacsService;

public interface HL7ServerMBean extends PacsService {

	public void setDaysToEmptyHL7Cache(String arg);

	public String getDaysToEmptyHL7Cache();

	/**
	 * Gets the protocolName attribute of the Hl7ServerService object
	 * 
	 * @return The protocolName value
	 */
	public String getProtocolName();

	/**
	 * Sets the protocolName attribute of the Hl7ServerService object
	 * 
	 * @param name
	 *            The new protocolName value
	 */
	public void setProtocolName(String name);

	/**
	 * Getter for property port.
	 * 
	 * @return Value of property port.
	 */
	int getPort();

	/**
	 * Setter for property port.
	 * 
	 * @param port
	 *            New value of property port.
	 */
	void setPort(int port);

	/**
	 * Getter for property dcmHandler.
	 * 
	 * @return Value of property dcmHandler.
	 */
	//public HL7Handler getHL7Handler();

	/**
	 * Gets the sSLContextAdapter attribute of the Hl7ServerServiceMBean object
	 * 
	 * @return The sSLContextAdapter value
	 */
	//public SSLContextAdapter getSSLContextAdapter();

	/**
	 * Getter for property rqTimeout.
	 * 
	 * @return Value of property rqTimeout.
	 */
	public int getSoTimeout();

	/**
	 * Setter for property rqTimeout.
	 * 
	 * @param soTimeout
	 *            The new soTimeout value
	 */
	public void setSoTimeout(int soTimeout);

	/**
	 * Getter for property maxClients.
	 * 
	 * @return Value of property maxClients.
	 */
	public int getMaxClients();

	/**
	 * Setter for property maxClients.
	 * 
	 * @param maxClients
	 *            New value of property maxClients.
	 */
	public void setMaxClients(int maxClients);

	/**
	 * Getter for property numClients.
	 * 
	 * @return Value of property numClients.
	 */
	public int getNumClients();

	/**
	 * Gets the sendingApps attribute of the Hl7ServerServiceMBean object
	 * 
	 * @return The sendingApps value
	 */
	public String getSendingApps();

	/**
	 * Sets the sendingApps attribute of the Hl7ServerServiceMBean object
	 * 
	 * @param sendingApps
	 *            The new sendingApps value
	 */
	public void setSendingApps(String sendingApps);

	/**
	 * Gets the receivingApps attribute of the Hl7ServerServiceMBean object
	 * 
	 * @return The receivingApps value
	 */
	public String getReceivingApps();

	/**
	 * Sets the receivingApps attribute of the Hl7ServerServiceMBean object
	 * 
	 * @param receivingApps
	 *            The new receivingApps value
	 */
	public void setReceivingApps(String receivingApps);

	/**
	 * Gets the keyFile attribute of the Hl7ServerService object
	 * 
	 * @return The keyFile value
	 */
	public String getKeyFile();

	/**
	 * Sets the keyFile attribute of the Hl7ServerService object
	 * 
	 * @param keyFile
	 *            The new keyFile value
	 */
	public void setKeyFile(String keyFile);

	/**
	 * Sets the keyPasswd attribute of the Hl7ServerService object
	 * 
	 * @param keyPasswd
	 *            The new keyPasswd value
	 */
	public void setKeyPasswd(String keyPasswd);

	/**
	 * Gets the cacertsFile attribute of the Hl7ServerService object
	 * 
	 * @return The cacertsFile value
	 */
	public String getCacertsFile();

	/**
	 * Sets the cacertsFile attribute of the Hl7ServerService object
	 * 
	 * @param cacertsFile
	 *            The new cacertsFile value
	 */
	public void setCacertsFile(String cacertsFile);

	/**
	 * Sets the cacertsPasswd attribute of the Hl7ServerService object
	 * 
	 * @param cacertsPasswd
	 *            The new cacertsPasswd value
	 */
	public void setCacertsPasswd(String cacertsPasswd);

	/**
	 * Set the assigning authority to search for when receiving an A08/A31
	 * message. The patientId with the specified AssigningAuthority is used to
	 * link the message to the existent patient.
	 * 
	 * @param assigningAuthority
	 */
	public void setPidAssigningAuthority(String assigningAuthority);
	
	/**
	 * Get the assigning authority to search for when receiving an A08/A31
	 * message. The patientId with the specified AssigningAuthority is used to
	 * link the message to the existent patient.
	 * 
	 * @param assigningAuthority
	 */
	public String getPidAssigningAuthority();
	
	public String getPidIdentifierType();
	public void setPidIdentifierType(String pidIdentifierType);
	
	public void setPidAddressType(String pidAddressType);
	public String getPidAddressType();

	public void setPidCityComponent(String pidCityComponent);
	public String getPidCityComponent();
	
	public String getVisitNumberIssuerComponent();
	public void setVisitNumberIssuerComponent(String visitNumberIssuerComponent);
	
	public String getAccNumSegment();
	public void setAccNumSegment(String accNumSegment);
	public String getAccNumField();
	public void setAccNumField(String accNumField);
	public String getAccNumComponent();
	public void setAccNumComponent(String accNumComponent);
	public String getAccNumRepetition();
	public void setAccNumRepetition(String accNumRepetition);
	public String getDefaultIdIssuer();
	
	public String getStudyDescSegment();
	public void setStudyDescSegment(String studyDescriptionSegment);
	public String getStudyDescField();
	public void setStudyDescField(String studyDescriptionField);
	public String getStudyDescComponent();
	public void setStudyDescComponent(String studyDescriptionComponent);
	public String getStudyDescRepetition();
	public void setStudyDescRepetition(String studyDescriptionRepetition);
	
	public String getMdmService();
	public void setMdmService(String mdmService);
	
	
	public String isSaveTransactionDate();
	public void setSaveTransactionDate(String save);
	
	public String getMessagesToForward();
    void setMessagesToForward(String s);

    boolean toBeForwarded(String messageType);
    
    Boolean getIsUpdateOnORM();
    String getUpdateOnORM();
    public void setUpdateOnORM(String updateOnORM) ;
	
}
