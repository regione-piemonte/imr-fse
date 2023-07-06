/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.multiframe;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.FileHasher;
import it.units.htl.dpacs.helpers.LogMessage;
import it.units.htl.dpacs.helpers.MBeanDealer;
import it.units.htl.dpacs.helpers.MailerSystem;
import it.units.htl.dpacs.postprocessing.UidGenerator;
import it.units.htl.maps.KnownNodes;
import it.units.htl.maps.KnownNodesHome;
import it.units.htl.maps.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.*;



public class DcmFramesToMF {

	private String senderAeTitle;
	private String pacsAeTitle;
	private String seriesUid; 

	private String frameIncrementPointer;
	private Float frameTime;

	private String desiredOutputPath;
	private String outputSeriesPath;
	private Date conversionDate;

	private static Log log = LogFactory.getLog(DcmFramesToMF.class);
	private static final String DIRECTORY_SEPARATOR = "/";

	public DcmFramesToMF(String senderAeTitle, String pacsAeTitle, String seriesUid) {

		init();

		this.senderAeTitle = senderAeTitle;
		this.pacsAeTitle = pacsAeTitle;
		this.seriesUid = seriesUid;

	}

	private void init() {
		this.frameIncrementPointer = null;
		this.frameTime = 0.0f;
	}

	public Date getLastConversionDate() {

		return this.conversionDate;
	}// End getLastConversionDate()

	public String processFrames() {

		String outputPath = null;
		boolean aeTitleFound = false;

		String newSeriesUid = null;
		String newInstanceUid = null;
		String newInstanceSopClass = null;
		Long newInstanceSize = 0L;
		String studyUid = null;
		String seriesModality = null;

		// Try to set needed info
		try {
			aeTitleFound = this.findNodeFrameInfo();
		} catch (RuntimeException re) {
			log.error("Cannot create Multiframe image.", re);
			log.error("Processing cannot be done.");
			return null;
		}
		// Continue only if sender AeTitle is valid
		if (!aeTitleFound) {
			log.debug("Conversion not needed for " + senderAeTitle);
			return null;
		} else {
			log.info("Multiframe image creation started...");

			MultiFrameGenerator mfg = new MultiFrameGenerator();

			try {
				String[] framesUrl = null;
				framesUrl = this.findReferredFrames();

				if ((framesUrl != null) && (!framesUrl[0].equalsIgnoreCase("null"))) {
					log.info("Starting post-processing...");

					File[] instances = getInstances(this.seriesUid, framesUrl[0]);
					studyUid = framesUrl[1];

					// Get new study/series/instance
					UidGenerator uGen = new UidGenerator();
					String[] newUid = uGen.getCompleteUidInfo();

					// We DO NOT WANT to get studyUid from here
					newSeriesUid = newUid[1];
					newInstanceUid = newUid[2];

					this.outputSeriesPath = this.desiredOutputPath + newSeriesUid;

					this.conversionDate = new Date(uGen.getInstanceCreationTime());

					this.createOutputDirectory(outputSeriesPath);

					outputPath = outputSeriesPath + DIRECTORY_SEPARATOR + newInstanceUid;
					File dest = new File(outputPath);

					mfg.recodeImages(instances, dest, newInstanceUid, newSeriesUid, this.frameIncrementPointer, this.frameTime);

					seriesModality = mfg.getMfModality(new File(outputPath));
					newInstanceSopClass = mfg.getMfSopClass(new File(outputPath));
				} else {
					sendMail(ConfigurationSettings.EMAIL_EVENT_ERRORINMF, newSeriesUid, ConfigurationSettings.EMAIL_MESSAGE_NOFRAMES);
					log.error("No frames could be found for " + seriesUid);
					return null;
				}
			} catch (IOException ioex) {
				log.error("Frame images found, but processing could NOT be done.", ioex);
				this.rollback(this.seriesUid);
				sendMail(ConfigurationSettings.EMAIL_EVENT_ERRORINMF, newSeriesUid, ioex.getMessage());
				this.removeOutputDirectory(outputSeriesPath);
				return null;
			} catch (RuntimeException rex) {
				log.error("Processing could NOT be done.", rex);
				this.rollback(this.seriesUid);
				sendMail(ConfigurationSettings.EMAIL_EVENT_ERRORINMF, newSeriesUid, rex.getMessage());
				this.removeOutputDirectory(outputSeriesPath);
				return null;
			} catch (Exception ex) {
				this.rollback(this.seriesUid);
				sendMail(ConfigurationSettings.EMAIL_EVENT_ERRORINMF, newSeriesUid, ex.getMessage());
				this.removeOutputDirectory(outputSeriesPath);
				log.error("", ex);
				return null;
			}
		}

		// USELESS, I suppose!
		//		if (outputPath == null) {
		//			this.rollback(this.seriesUid);
		//			sendMail(ConfigurationSettings.EMAIL_EVENT_ERRORINMF, newSeriesUid, ConfigurationSettings.EMAIL_MESSAGE_UNKNOWNREASON);
		//			if (outputSeriesPath != null)
		//				this.removeOutputDirectory(outputSeriesPath);
		//			return null;
		//		}

		// I'm here only if no errors occurred

		log.info("Collecting new image parameters for storage purposes");

		File newInstanceFile = new File(outputPath);
		newInstanceSize = newInstanceFile.length();
		String newInstanceHash;
		String hashAlgorithm = null;
		try {
			//hashAlgorithm = MBeanDealer.getPropertyFromMbean(new ObjectName("it.units.htl.dpacs.servers:type=DicomServer,index=3"), "getHashAlgorithm"); //GDC
			hashAlgorithm = MBeanDealer.getPropertyFromMbean(new ObjectName("it.units.htl.dpacs.servers:type=DicomServer,index=3"), "HashAlgorithm");
		} catch (MalformedObjectNameException mone) {
			//mone.printStackTrace();
			sendMail(ConfigurationSettings.EMAIL_EVENT_ERRORINMF, newSeriesUid, mone.getMessage());
			this.removeOutputDirectory(outputSeriesPath);
			log.error("", mone);
		} catch (NullPointerException npe) {
			sendMail(ConfigurationSettings.EMAIL_EVENT_ERRORINMF, newSeriesUid, npe.getMessage());
			this.removeOutputDirectory(outputSeriesPath);
			log.error("", npe);
			//npe.printStackTrace();
		}
		if (hashAlgorithm == null) {
			return null;
		}
		newInstanceHash = new FileHasher().doHash(newInstanceFile, hashAlgorithm);
		log.info("Storage of new image started...");
		boolean isStored = storeMfImageInfo(studyUid, newSeriesUid, newInstanceUid, newInstanceHash, newInstanceSopClass, newInstanceSize, pacsAeTitle, senderAeTitle, seriesModality);
		if (isStored) {
			log.info("Done.");
		} else {
			log.warn("Image created but could NOT perform storage. Please check database configurations.");
			this.rollback(this.seriesUid);
			sendMail(ConfigurationSettings.EMAIL_EVENT_ERRORINMF, newSeriesUid, ConfigurationSettings.EMAIL_MESSAGE_DBERROR);
			if (outputSeriesPath != null)
				this.removeOutputDirectory(outputSeriesPath);
			return null;
		}

		return outputPath;
	}// End processFrames()

	private void createOutputDirectory(String outputDir) throws Exception {

		try {
			new File(outputDir).mkdir(); // This starts at study level, so no need for mkdirs()
		} catch (Exception ioe) {
			throw ioe;
		}
	}// End createOutputDirectory()

	@SuppressWarnings("unchecked")
	private boolean findNodeFrameInfo() {

		boolean aeTitleFound = false;
		 
		Session s = SessionManager.getInstance().openSession();

		KnownNodesHome knh = new KnownNodesHome();
		List<KnownNodes> knFound = knh.findByAETitle(this.senderAeTitle, s);

		log.info("Retrieving Multiframe image parameters...");

		if (!knFound.isEmpty()) {
			KnownNodes kn = knFound.get(0);
			if ((kn.getFrameTime() != null) && (kn.getFrameIncrementPointer() != null)) {
				this.frameTime = kn.getFrameTime();
				this.frameIncrementPointer = kn.getFrameIncrementPointer();
				aeTitleFound = true;
				log.info("...done.");
			} else {
				log.info("Calling AETitle does not require Multiframe processing.");
			}

		} else {
			log.error(this.senderAeTitle.toString() + "not found.");
		}

		s.close();
		return aeTitleFound;
	} // End findNodeInfo()

	private String[] findReferredFrames() throws RuntimeException {

		String framesUrl[] = new String[2];
		boolean toPerform = false;
		Connection con = null;
		CallableStatement cs = null;

		ResultSet rs = null;

		try {
			Context jndiContext = null;
			DataSource dataSource = null;

			jndiContext = new InitialContext();
			dataSource = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");

			log.info(this.senderAeTitle + ": Retrieving data");
			con = dataSource.getConnection();

			boolean isOracle = Dbms.isOracle(con);
			if (isOracle) {
				cs = con.prepareCall("{call isConverted(?,?)}");
				cs.registerOutParameter(2, OracleTypes.CURSOR);
			} else {
				cs = con.prepareCall("{call isConverted(?)}");
			}
			cs.setString(1, this.seriesUid);
			cs.execute();

			if (isOracle) {
				rs = (ResultSet) cs.getObject(2);
			} else {
				rs = cs.getResultSet();
			}

			if ((rs != null) && (rs.next())) {
				toPerform = true;
				int counter = 0;
				do {
					if (counter == 0) {
						framesUrl[1] = rs.getString(3); // Get studiesFK
						counter++;
					}
					this.desiredOutputPath = rs.getString(2);
					framesUrl[0] = this.desiredOutputPath + rs.getString(1) + DIRECTORY_SEPARATOR;
				} while (rs.next());
			}

		} catch (RuntimeException re) {
			log.error("Could not retrieve series URL.", re);
			throw re;
		} catch (Exception e) {
			log.error("Error finding referred frames", e);
			throw new RuntimeException(e);
		} finally {

			if (rs != null)
				try {
					rs.close();
				} catch (Exception ex) {
				}
			if (cs != null)
				try {
					cs.close();
				} catch (Exception ex) {
				}
			if (con != null)
				try {
					con.close();
				} catch (Exception ex) {
				}

			if (!toPerform) {
				log.warn("Multiframe image already created or frames instances not found, nothing to do");
				framesUrl = null;
			}

		}

		return framesUrl;

	}// End findReferredFrames()

	private File[] getInstances(String seriesUid, String seriesUrl) throws Exception {

		Context jndiContext = null;
		DataSource dataSource = null;
		try {
			jndiContext = new InitialContext(); // Lazy Initialization
			dataSource = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
		} catch (NamingException nex) {
			log.fatal(LogMessage._NoDatasource, nex);
		}

		Connection con = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		ArrayList<File> rl = null;
		File[] instances = null;

		try {
			con = dataSource.getConnection();
			boolean isOracle = Dbms.isOracle(con);
			if (isOracle) {
				cs = con.prepareCall("{call getInstances(?,?)}");
				cs.registerOutParameter(2, OracleTypes.CURSOR);
			} else {
				cs = con.prepareCall("{call getInstances(?)}");
			}
			cs.setString(1, seriesUid);
			cs.execute();

			if (isOracle) {
				rs = (ResultSet) cs.getObject(2);
			} else {
				rs = cs.getResultSet();
			}

			rl = new ArrayList<File>(19);
			while (rs.next()) {
				rl.add(new File(seriesUrl, rs.getString(1)));
			}

			if ((rl != null) && (rl.size() > 0)) {
				instances = new File[rl.size()];
				rl.toArray(instances);
			}

		} catch (SQLException sex) {
			log.error("Could NOT retrieve instances from database: ", sex);
			throw sex;
		} catch (Exception e) {

			log.error("", e);
			throw e;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception ex) {
				}
			if (cs != null)
				try {
					cs.close();
				} catch (Exception ex) {
				}
			if (con != null)
				try {
					con.close();
				} catch (Exception ex) {
				}
		}
		return instances;

	}

	private void removeOutputDirectory(String outputDir) {

		try {
			File oDir = new File(outputDir);
			if (oDir.exists()) {
				File[] oFiles = oDir.listFiles();
				for (File f : oFiles) {
					f.delete();
				}
				oDir.delete();
			}
		} catch (Exception ex) {
			log.error("Cannot clean output directory " + outputSeriesPath + " please check.", ex);
		}
	}// End removeOutputDirectory()

	// Call stored procedure to update series conversion date
	private void rollback(String seriesUid) {
		Connection con = null;
		CallableStatement cs = null;

		try {
			Context jndiContext = null;
			DataSource dataSource = null;
			try {
				jndiContext = new InitialContext();
				dataSource = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
			} catch (NamingException nex) {
				log.fatal(LogMessage._NoDatasource, nex);
			}

			con = dataSource.getConnection();

			cs = con.prepareCall("{call rollbackConversionDate(?)}");

			cs.setString(1, seriesUid);
			cs.execute();
		} catch (SQLException sex) {
			log.error("Error rolling back", sex);
		} finally {
			if (cs != null)
				try {
					cs.close();
				} catch (Exception ex) {
				}
			if (con != null)
				try {
					con.close();
				} catch (Exception ex) {
				}
		}

	} // End rollback()

	private boolean storeMfImageInfo(String studyUid, String newSeriesUid, String newInstanceUid, String newInstanceHash, String newInstanceSopClass, long instanceSizeInBytes, String pacsAeTitle,
			String callingAeTitle, String modality) {

		boolean success = false;
		Connection con = null;
		CallableStatement cs = null;

		try {
			Context jndiContext = null;
			DataSource dataSource = null;
			try {
				jndiContext = new InitialContext(); // Lazy Initialization
				dataSource = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
			} catch (NamingException nex) {
				log.fatal(LogMessage._NoDatasource, nex);
			}
			con = dataSource.getConnection();
			cs = con.prepareCall("{call completeMfCreation(?,?,?,?,?,?,?,?,?,?)}");
			cs.setString(1, studyUid);
			cs.setString(2, newSeriesUid);
			cs.setString(3, newInstanceUid);
			cs.setString(4, newInstanceHash);
			cs.setString(5, newInstanceSopClass);
			cs.setLong(6, instanceSizeInBytes);
			cs.setString(7, pacsAeTitle);
			cs.setString(8, callingAeTitle);
			cs.setString(9, modality);
			cs.registerOutParameter(10, Types.BIT);
			cs.execute();
			success = cs.getBoolean(10);
		} catch (SQLException sex) {
			log.error("", sex);
			sendMail(ConfigurationSettings.EMAIL_EVENT_ERRORINMF, newSeriesUid, sex.getMessage());
		} finally {
			if (cs != null)
				try {
					cs.close();
				} catch (Exception ex) {
				}
			if (con != null)
				try {
					con.close();
				} catch (Exception ex) {
				}
		}
		return success;
	}

	private void sendMail(String messageType, String newSeries, String message) {
		String[] tokens = { seriesUid, (newSeries == null) ? "None yet" : newSeries, message };
		MailerSystem.send(ConfigurationSettings.EMAIL_EVENT_ERRORINMF, tokens);
	}

}// End DcmFramesToMF.java
