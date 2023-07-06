/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Timer;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
//import javax.imageio.ImageWriter;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ServerSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
//import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
//import org.dcm4che.server.ServerFactory;
//import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.DcmProtocol;
import org.dcm4che.util.HandshakeFailedEvent;
import org.dcm4che.util.HandshakeFailedListener;
import org.dcm4che.util.SSLContextAdapter;
import org.dcm4che.util.SystemUtils;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che2.util.CloseUtils;
import org.dcm4cheri.image.ImageReaderFactory;
import org.dcm4cheri.image.ImageWriterFactory;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi;

import it.units.htl.dpacs.core.VarLoader;
import it.units.htl.dpacs.dao.DicomDbDealer;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.helpers.DateHelper;
import it.units.htl.dpacs.helpers.ImageAvailabilityConfig;
import it.units.htl.dpacs.helpers.LogMessage;
import it.units.htl.dpacs.helpers.PatientIdCheckSettings;
import it.units.htl.dpacs.helpers.PhysicalMediaTimerTask;
import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;
/**
 * The base class inherited from dcm4che. It start and stop the phisycal socket that
 * accecpt connection for ALL DICOM services.
 * 
 * @author Mbe
 */
public class DicomServer implements HandshakeFailedListener, DicomServerMBean {
	
	private ServerFactory sf = ServerFactory.getInstance();

	private AssociationFactory af = AssociationFactory.getInstance();
	private AcceptorPolicy policy = af.newAcceptorPolicy();
	private DcmServiceRegistry services = af.newDcmServiceRegistry();
	private DcmHandler handler = (DcmHandler) sf.newDcmHandler(policy, services);
	private Server dcmsrv = sf.newServer(handler);
	
	private SSLContextAdapter ssl = SSLContextAdapter.getInstance();
	private String actorName;
	private DcmProtocol protocol = DcmProtocol.DICOM_TLS;
	private String keyFile = "C:\\Documents and Settings\\cicuta\\Desktop\\DPACS2004\\src\\conf\\tls\\file.p12";
	private String keyPasswd = "mesatest";
	private File lastKeyFile;
	private long lastKeyFileModified;
	private String cacertsFile = "C:\\Documents and Settings\\cicuta\\Desktop\\DPACS2004\\src\\conf\\tls\\mesa_test.jks";
	private String cacertsPasswd = "mesatest";
	private File lastCacertsFile;
	private long lastCacertsFileModified;
	private DicomDbDealer bean = null;
	private static Log log = LogFactory.getLog(DicomServer.class);
	private boolean IsCallingLockedonKnownnodes;
	// identify the status of service
	private boolean status;
	private int _fileHasherBufferSizeInKb;
	private String _hashAlgorithm;
	private final int MAXMb = 100 * 1024 * 1024; // 100 Mb
	private final int DEFAULT_Mb = 2 * 1024 * 1024; // 2 Mb
	private final int KILOBYTE = 1024;		// 1KB
	
	private Timer physicalMediaTimer = null;
	private PhysicalMediaTimerTask physicalMediaTimerTask = null;
	private int physicalMediaTimerRepeatInD;
	private String physicalMediaTimerStartDate;

	public DicomServer(MBeanServer serv) {
		if (bean == null) {
		    try {
                log.debug("About to create DicomDBDealer");
                bean = InitialContext.doLookup(BeansName.LDicomDbDealer);
            }catch (NamingException nex) {
				log.error(LogMessage._CouldntCreate + " java:comp/env/ejb/DicomDbDealer", nex);
			}
		}
	}

	private static File toFile(String path) {
		if (path == null || path.trim().length() == 0) {
			return null;
		}
		File f = new File(path);
		if (f.isAbsolute()) {
			return f;
		}
		String serviceHomeDir = System.getProperty("user.dir");
		return new File(serviceHomeDir, path);
	}

	/**
	 * Ruturns free and total Memory by the application
	 * 
	 * @return Arralist [long free, long total]
	 */
	/**
	 * Getter for property dcmHandler. Mandatory to start all other DICOM Services
	 * 
	 * @return Value of property dcmHandler.
	 */
	public DcmHandler getDcmHandler() {
		return handler;
	}

	public int getNumIdleThreads() {
		return dcmsrv.getNumIdleThreads();
	}

	public ArrayList<Long> getMemoryStatistics() {
		// returns two values, free, total
		ArrayList<Long> memory = new ArrayList<Long>(2);
		memory.add(new Long(Runtime.getRuntime().freeMemory()));
		memory.add(new Long(Runtime.getRuntime().totalMemory()));
		return memory;
	}

	/**
	 * Getter for property port.
	 * 
	 * @return Value of property port.
	 */
	public int getPort() {
		return dcmsrv.getPort();
	}

	/**
	 * Setter for property port.
	 * 
	 * @param newPort
	 *            The new port value
	 */
	public void setPort(int newPort) {
		dcmsrv.setPort(newPort);
	}

	/**
	 * Gets the sSLContextAdapter attribute of the DicomServer object
	 * 
	 * @return The sSLContextAdapter value
	 */

	public SSLContextAdapter getSSLContextAdapter() {
		return ssl;
	}

	/**
	 * Gets the protocolName attribute of the object
	 * 
	 * @return The protocolName value
	 */
	public String getProtocolName() {
		return protocol.toString();
	}

	/**
	 * Sets the protocolName attribute of the DicomServer object
	 * 
	 * @param newName
	 *            The new protocolName value
	 */
	public void setProtocolName(String newName) {
		String oldName = protocol.toString();
		this.protocol = DcmProtocol.valueOf(newName);
		log.debug("Protocol is " + protocol + ". (previous: " + oldName + ")...");
	}

	/**
	 * Gets the keyFile attribute of the DicomServer object
	 * 
	 * @return The keyFile value
	 */
	public String getKeyFile() {
		return keyFile;
	}

	/**
	 * Sets the keyFile attribute of the DicomServer object
	 * 
	 * @param keyFile
	 *            The new keyFile value
	 */
	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}

	/**
	 * Sets the keyPasswd attribute of the DicomServer object
	 * 
	 * @param keyPasswd
	 *            The new keyPasswd value
	 */
	public void setKeyPasswd(String keyPasswd) {
		this.keyPasswd = keyPasswd;
		log.debug("Set key password " + this.keyPasswd);
	}

	/**
	 * Gets the cacertsFile attribute of the DicomServer object
	 * 
	 * @return The cacertsFile value
	 */
	public String getCacertsFile() {
		return cacertsFile;
	}

	/**
	 * Sets the cacertsFile attribute of the DicomServer object
	 * 
	 * @param cacertsFile
	 *            The new cacertsFile value
	 */
	public void setCacertsFile(String cacertsFile) {
		this.cacertsFile = cacertsFile;
		log.debug("Set cacert file " + this.cacertsFile);
	}

	/**
	 * Sets the cacertsPasswd attribute of the DicomServer object
	 * 
	 * @param cacertsPasswd
	 *            The new cacertsPasswd value
	 */
	public void setCacertsPasswd(String cacertsPasswd) {
		this.cacertsPasswd = cacertsPasswd;
		log.debug("Set cacert password " + this.cacertsPasswd);
	}

	/**
	 * Getter for property rqTimeout.
	 * 
	 * @return Value of property rqTimeout.
	 */
	public int getRqTimeout() {
		return handler.getRqTimeout();
	}

	/**
	 * Setter for property rqTimeout.
	 * 
	 * @param newRqTimeout
	 *            The new rqTimeout value
	 */
	public void setRqTimeout(int newRqTimeout) {
		handler.setRqTimeout(newRqTimeout);
	}

	/**
	 * Getter for property dimseTimeout.
	 * 
	 * @return Value of property dimseTimeout.
	 */
	public int getDimseTimeout() {
		return handler.getDimseTimeout();
	}

	/**
	 * Setter for property dimseTimeout.
	 * 
	 * @param newDimseTimeout
	 *            The new dimseTimeout value
	 */
	public void setDimseTimeout(int newDimseTimeout) {
		handler.setDimseTimeout(newDimseTimeout);
	}

	/**
	 * Getter for property soCloseDelay.
	 * 
	 * @return Value of property soCloseDelay.
	 */
	public int getSoCloseDelay() {
		return handler.getSoCloseDelay();
	}

	/**
	 * Setter for property soCloseDelay.
	 * 
	 * @param newSoCloseDelay
	 *            The new soCloseDelay value
	 */
	public void setSoCloseDelay(int newSoCloseDelay) {
		handler.setSoCloseDelay(newSoCloseDelay);
	}

	/**
	 * Gets the packPDVs attribute of the DicomServer object
	 * 
	 * @return The packPDVs value
	 */
	public boolean isPackPDVs() {
		return handler.isPackPDVs();
	}

	/**
	 * Sets the packPDVs attribute of the DicomServer object
	 * 
	 * @param newPackPDVs
	 *            The new packPDVs value
	 */
	public void setPackPDVs(boolean newPackPDVs) {
		handler.setPackPDVs(newPackPDVs);
	}

	/**
	 * Getter for property maxClients.
	 * 
	 * @return Value of property maxClients.
	 */
	public int getMaxClients() {
		return dcmsrv.getMaxClients();
	}

	/**
	 * Setter for property maxClients.
	 * 
	 * @param newMaxClients
	 *            The new maxClients value
	 */
	public void setMaxClients(int newMaxClients) {
		dcmsrv.setMaxClients(newMaxClients);
	}

	/**
	 * Getter for property numClients.
	 * 
	 * @return Value of property numClients.
	 */
	public int getNumClients() {
		return dcmsrv.getNumClients();
	}

	public int getRightNumClients() {
		return dcmsrv.getNumClients();
	}

	public void setRightNumClients(int numClients) {
		if (numClients != 0) {
			if (numClients == 100)
				numClients = 0;
			dcmsrv.setNumClients(numClients);
		}
	}

	/**
	 * Getter for property callingAETs.
	 * 
	 * @return Value of property callingAETs.
	 */
	public String getCallingAETs() {
		return toString(policy.getCallingAETs());
	}

	/**
	 * Setter for property callingAETs.
	 * 
	 * @param newCallingAETs
	 *            The new callingAETs value
	 */
	public void setCallingAETs(String newCallingAETs) {
		policy.setCallingAETs(toStringArray(newCallingAETs));
	}

	/**
	 * Sets if admitted Calling AE are taken from Knownnodes table
	 * 
	 * @param isCallingLocked
	 */
	public void setIsCallingLockedonKnownnodes(boolean isCallingLocked) {
		if (isCallingLocked) {
			String aes = bean.getExistingAEs();
			if (!aes.equals("")) {
				policy.setCallingAETs(toStringArray(aes));
				IsCallingLockedonKnownnodes = true;
				log.info("Enabled filter on calling AETs based on known nodes");
			} else {
				log.warn("No AETitle founded on the DB..is the first Running?");
			}
		} else {
			policy.setCallingAETs(toStringArray("any"));
			IsCallingLockedonKnownnodes = false;
			log.info("Disabled filter on calling AETs");
		}
	}

	/**
	 * Gets if admitted Calling AE are taken from Knownnodes table
	 * 
	 * @return
	 */
	public boolean getIsCallingLockedonKnownnodes() {
		return IsCallingLockedonKnownnodes;
	}

	/**
	 * Getter for property calledAETs.
	 * 
	 * @return Value of property calledAETs.
	 */
	public String getCalledAETs() {
		return toString(policy.getCalledAETs());
	}

	/**
	 * Setter for property calledAETs.
	 * 
	 * @param newCalledAETs
	 *            The new calledAETs value
	 */
	public void setCalledAETs(String newCalledAETs) {
		policy.setCalledAETs(toStringArray(newCalledAETs));
	}

	/**
	 * Getter for property maxPDULength.
	 * 
	 * @return Value of property maxPDULength.
	 */
	public int getMaxPDULength() {
		return policy.getMaxPDULength();
	}

	/**
	 * Setter for property maxPDULength.
	 * 
	 * @param newMaxPDULength
	 *            The new maxPDULength value
	 */
	public void setMaxPDULength(int newMaxPDULength) {
		policy.setMaxPDULength(newMaxPDULength);
	}

	/**
	 * Gets the name attribute of the DicomServer object
	 * 
	 * @return The name value
	 */
	public String getName() {
		return actorName;
	}

	/**
	 * Getter for property FileHasherBufferSizeInKb
	 * 
	 * @return Value of property FileHasherBufferSizeInKb
	 */
	public String getFileHasherBufferSizeInKb() {
		return "" + _fileHasherBufferSizeInKb;
	}

	/**
	 * Setter for property FileHasherBufferSizeInKb
	 * 
	 * @param _fileHasherBufferSizeInKb
	 *            is the buffer size for hashing files
	 */
	public void setFileHasherBufferSizeInKb(String fileHasherBufferSizeInKb) {
		try {
			int tmp_size = Integer.parseInt(fileHasherBufferSizeInKb) * KILOBYTE ;
			if (tmp_size > MAXMb) {
				_fileHasherBufferSizeInKb = MAXMb;
				log.warn("The specified buffer size is bigger than allowed, a default of 100 Mb will be used");
			} else if (tmp_size <= 0) {
				_fileHasherBufferSizeInKb = DEFAULT_Mb;
				log.warn("The specified buffer size is smaller than allowed, a default of 2 Mb will be used");
			} else {
				_fileHasherBufferSizeInKb = tmp_size;
			}
			if (tmp_size > 0 && tmp_size <= 10) {
				log.warn("The specified buffer size of " + fileHasherBufferSizeInKb + " Kb is quite small");
			}
		} catch (Exception exp) {
			_fileHasherBufferSizeInKb = DEFAULT_Mb;
			log.warn("The specified buffer size is not correct, a default of 2 Mb will be used");
		}
	}

	/**
	 * Getter for property HashAlgorithm
	 * 
	 * @return Value of property HashAlgorithm
	 */
	public String getHashAlgorithm() {
		return _hashAlgorithm;
	}

	/**
	 * Setter for property HashAlgorithm
	 * 
	 * @param _hashAlgorithm
	 *            is the name of hash algorithm to be used
	 */
	public void setHashAlgorithm(String hashAlgorithm) {
		try {
			MessageDigest.getInstance(hashAlgorithm);
			this._hashAlgorithm = hashAlgorithm;
		} catch (Exception ex) {
			log.warn("The specified Hash Algorithm is not supported, default SHA-1 will be used");
			_hashAlgorithm = "SHA-1";
		}
	}

	public String getPhysicalMediaTimerStartDate() {
		log.debug("entering setPhysicalMediaTimerStartDate(), return=" + this.physicalMediaTimerStartDate);
		return this.physicalMediaTimerStartDate;
	}

	public void setPhysicalMediaTimerStartDate(String startDate) {
		log.debug("entering setPhysicalMediaTimerStartDate(String startDate), startDate=" + startDate);
		this.physicalMediaTimerStartDate = startDate;
	}

	public int getPhysicalMediaTimerRepeatInD() {
		log.debug("entering getPhysicalMediaTimerRepeatInD(), return=" + this.physicalMediaTimerRepeatInD);
		return this.physicalMediaTimerRepeatInD;
	}

	public void setPhysicalMediaTimerRepeatInD(int days) {
		log.debug("entering setPhysicalMediaTimerRepeatInD(int days), days=" + days);
		this.physicalMediaTimerRepeatInD = days;
	}

	
	public boolean stopService() throws Exception {
		if (status) {
			dcmsrv.stop();
			dcmsrv.removeHandshakeFailedListener(this);
			if (physicalMediaTimer != null) {
				physicalMediaTimer.cancel();
			}
			status = false;
			ImageAvailabilityConfig.reset();
		}
		return true;
	}

	/**
	 * Start the server
	 * 
	 * @return TRUE if the service is started, FALSE otherwise
	 */
	public boolean startService() {
		try {
			if (status) {
				stopService();
			}
			loadSettings();
			checkJaiImageIo();
			schedulePhysicalMediaTimer();
			
			PatientIdCheckSettings.loadSettings();
			
			ImageAvailabilityConfig.getInstance();
			dcmsrv.addHandshakeFailedListener(this);
			dcmsrv.setServerSocketFactory(getServerSocketFactory());
			dcmsrv.start();
			status = true;
		} catch (Exception ex) {
			log.fatal("Error when starting Dicom Server: ", ex);
			status = false;
		}
		return status;
	}

	/**
	 * Start the timer that will checks the remaining space on disk of each PhysicalMedia
	 * entry
	 */
	private void schedulePhysicalMediaTimer() {
		if (this.physicalMediaTimerRepeatInD < 1) {
			this.physicalMediaTimerRepeatInD = 1;
		}
		long repeatInMs = this.physicalMediaTimerRepeatInD * 24 * 60 * 60 * 1000;
		Date firstExecutionDate = DateHelper.getFirstUsefulDate(this.physicalMediaTimerStartDate);
		if (firstExecutionDate == null) {
			firstExecutionDate = new Date();
		}

		physicalMediaTimer = new Timer("PhysicalMediaTimer");
		physicalMediaTimerTask = new PhysicalMediaTimerTask();
		physicalMediaTimer.scheduleAtFixedRate(physicalMediaTimerTask, firstExecutionDate, repeatInMs);
		log.info("physical media timer task scheduled for " + firstExecutionDate + ", repeating every " + this.physicalMediaTimerRepeatInD + " day(s)");
	}

	/**
	 * Verify the installation of JAI Imageio.<br>
	 * Throws a Runtime exception if fails.
	 */
	private void checkJaiImageIo() { 
		ImageWriterFactory.getInstance().getWriterForTransferSyntax(UIDs.JPEGLossless);
		ImageWriterFactory.getInstance().getWriterForTransferSyntax(UIDs.JPEG2000Lossless);
		
		ImageReaderFactory.getInstance().getReaderForTransferSyntax(UIDs.JPEGLossless);
		ImageReaderFactory.getInstance().getReaderForTransferSyntax(UIDs.JPEG2000Lossless);
	}

	@SuppressWarnings("rawtypes")
	private void loadSettings() throws Exception {
		ServicesConfigurationHome sch = new ServicesConfigurationHome();
		VarLoader xmlLoader = null;
		ServicesConfiguration sc = sch.findByServiceName("DicomServer");
		xmlLoader = new VarLoader(sc.getConfiguration(), VarLoader.FROMSTRING);
		// String vector with attributes
		String[] nameOfAttributes = xmlLoader.getNameOfValues();
		String[] valuesOfAttributes = xmlLoader.getStringValues();
		int error = 0;
		for (int i = 0; i < nameOfAttributes.length; i++) {
			error = 0;
			try {
				Class[] argomento = { String.class };
				Method method = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
				Object[] value = { new String(valuesOfAttributes[i]) };
				method.invoke(this, value);
			} catch (java.lang.NoSuchMethodException eNSME) {
				error = 1;
			} catch (java.lang.IllegalAccessException eIAE) {
				error = 1;
			} catch (java.lang.reflect.InvocationTargetException eITE) {
				error = 1;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				Class[] argomento = { int.class };
				Method method = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
				Object[] value = { new Integer(Integer.parseInt(valuesOfAttributes[i])) };
				method.invoke(this, value);
			} catch (java.lang.NoSuchMethodException eNSME) {
				error += 1;
			} catch (java.lang.IllegalAccessException eIAE) {
				error += 1;
			} catch (java.lang.reflect.InvocationTargetException eITE) {
				error += 1;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				Class[] argomento = { boolean.class };
				Method method = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
				Object[] value = { new Boolean(valuesOfAttributes[i]) };
				method.invoke(this, value);
			} catch (java.lang.NoSuchMethodException eNSME) {
				error += 1;
			} catch (java.lang.IllegalAccessException eIAE) {
				error += 1;
			} catch (java.lang.reflect.InvocationTargetException eITE) {
				error += 1;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				Class[] argomento = { ObjectName.class };
				Method method = this.getClass().getMethod("set" + nameOfAttributes[i], argomento);
				Object[] value = { new ObjectName(valuesOfAttributes[i]) };
				method.invoke(this, value);
			} catch (java.lang.NoSuchMethodException eNSME) {
				if (error == 3) {
					log.debug("No method the class, set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>value</NomeAttributo>"
							+ nameOfAttributes[i]);
				}
			} catch (java.lang.IllegalAccessException eIAE) {
				if (error == 3) {
					log.debug("No method the class, set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>value</NomeAttributo>"
							+ nameOfAttributes[i]);
				}
			} catch (java.lang.reflect.InvocationTargetException eITE) {
				if (error == 3) {
					log.debug("No method the class, set" + nameOfAttributes[i] + "\n" + "O probabile errore di nome nel file xml" + "\n" + "Sintassi nel file xml <NomeAttributo>value</NomeAttributo>"
							+ nameOfAttributes[i]);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}

	private ServerSocketFactory getServerSocketFactory() throws GeneralSecurityException, IOException {
		if (!protocol.isTLS()) {
			return ServerSocketFactory.getDefault();
		}
		reloadKey();
		reloadCacerts();
		// String[] ps=protocol.getCipherSuites();
		log.info("");
		String[] temp = { "SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
				"SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA",
				"SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
				"SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "SSL_RSA_WITH_NULL_MD5", "SSL_RSA_WITH_NULL_SHA", "SSL_DH_anon_WITH_RC4_128_MD5", "TLS_DH_anon_WITH_AES_128_CBC_SHA",
				"SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", "SSL_DH_anon_WITH_DES_CBC_SHA", "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5", "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA" };
		String[] temp1 = protocol.getCipherSuites();
		String[] supported = new String[temp1.length + temp.length];
		int i = 0;
		for (i = 0; i < temp.length; i++) {
			supported[i] = temp[i];
		}
		for (int j = 0; j < temp1.length; j++) {
			supported[i + j] = temp1[j];
		}
		for (int k = 0; k < supported.length; k++) {
			log.info(" --- " + supported[k]);
		}
		return ssl.getServerSocketFactory(supported);
	}

	private void reloadKey() throws GeneralSecurityException, IOException {
		File f = toFile(keyFile);
		if (f.equals(lastKeyFile) && f.lastModified() == lastKeyFileModified) {
			return;
		}
		char[] passwd = keyPasswd.toCharArray();
		ssl.setKey(ssl.loadKeyStore(f, passwd), passwd);
		lastKeyFile = f;
		lastKeyFileModified = f.lastModified();
	}

	private void reloadCacerts() throws GeneralSecurityException, IOException {
		File f = toFile(cacertsFile);
		if (f.equals(lastCacertsFile) && f.lastModified() == lastCacertsFileModified) {
			return;
		}
		ssl.setTrust(ssl.loadKeyStore(f, cacertsPasswd.toCharArray()));
		lastCacertsFile = f;
		lastCacertsFileModified = f.lastModified();
	}

	private String toString(String[] a) {
		if (a == null) {
			return "any";
		}
		if (a.length == 0) {
			return "";
		}
		if (a.length == 1) {
			return a[0];
		}
		StringBuffer sb = new StringBuffer(a[0]);
		for (int i = 1; i < a.length; ++i) {
			sb.append('\\').append(a[i]);
		}
		return sb.toString();
	}

	private String[] toStringArray(String s) {
		if ("any".equals(s)) {
			return null;
		}
		return s.split(";");
	}

	// -------- // HandshakeFailedListener Implementation and other methods-------------------------------
	/**
	 * Description of the Method
	 * 
	 * @param event
	 *            Description of the Parameter
	 */
	public void handshakeFailed(HandshakeFailedEvent event) {
		// public SecurityAdministration(int outcome, String eventTypeCode, String detectorProcessId, String[] userIds, String requestedResourceId, String requestDescription, String asid, String aesid, String[] codes){ // otherAETitles shall be of the form AETITLES=aet1,aet2 if DICOM
		// String[] id = {System.getProperty("User")};
		// auditLog.logSecurityAdministration(6, "code=\"110126\" codeSystemName=\"DCM\" displayName=\"Node Authentication\"", "o3-dpacs dcmServer", id, event.getSocket().getInetAddress().getHostAddress(), event.getException().getMessage(), "o3-dpacs", "to be setted", new String[1]);
		// if (auditLog != null) {
		// auditLog.logSecurityAlert("NodeAuthentification",
		// alf.newRemoteUser(alf.newRemoteNode(event.getSocket(), null)),
		// event.getException().getMessage());
		// }
	}

	protected ObjectName getObjectName(MBeanServer server, ObjectName name) {
		actorName = name.getKeyProperty("name");
		return name;
	}

	public boolean statusService() {
		return status;
	}

	public boolean reloadSettings() throws Exception {
		return startService();
	}
}