/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * This TimerTask is scheduled in the DicomServer class during the server startup. This
 * task checks, for each PhysicalMedia found (having type 'HD'), the remaining size on
 * disk. To perform this job, the sizeInBytes value of the PhysicalMedia is compared with
 * its filledBytes, and the gap is eventually compared with the tolerance value. If the
 * tolerance is overcame, a warning email is sent. If the PhysicalMedia sizeInBytes is
 * null (meaning unlimited size), the free space of the actual disk is retrieved via java.
 * 
 * @author giacomo petronio
 * 
 */
public class PhysicalMediaTimerTask extends TimerTask {

	private static Logger log = Logger.getLogger(PhysicalMediaTimerTask.class);
	private Context jndiContext;

	@Override
	public void run() {
		log.debug("Entering PhysicalMediaTimerTask run()");
		Connection myConn = null;
		ResultSet rs = null;
		Statement stat = null;

		List<String> puffyPhysicalMediaList = new ArrayList<String>();

		try {
			myConn = getDBConnection();
			stat = myConn.createStatement();
			rs = stat.executeQuery("SELECT pk, name, capacityInBytes, filledBytes, toleranceInBytes, urlToStudy FROM PhysicalMedia WHERE type = 'HD'");
			while (rs.next()) {
				boolean isRunningOutOfSpace = false;
				boolean unboundedPhysicalMedia = false;
				long pk = rs.getLong(1);
				String physicalMediaName = rs.getString(2);
				long capacityInBytes = rs.getLong(3);
				if (rs.wasNull()) {
					unboundedPhysicalMedia = true;
				}
				long filledBytes = rs.getLong(4);
				long toleranceInBytes = rs.getLong(5);
				String urlToStudy = rs.getString(6);
				log.info("Checking physicalMedia (pk=" + pk + ", name=" + physicalMediaName + ", capacityInBytes=" + capacityInBytes + ", filledBytes=" + filledBytes + ", toleranceInBytes="
						+ toleranceInBytes + ", urlToStudy=" + urlToStudy);

				long freeSpaceOnDisk = 0;

				if (unboundedPhysicalMedia) {
					File f = new File(urlToStudy);
					long usableSpaceOnDisk = f.getUsableSpace();
					long totalSpaceOnDisk = f.getTotalSpace();
					log.debug("unbounded physical media, data retrieved programmatically: usableSpace=" + usableSpaceOnDisk + ", totalSpace=" + totalSpaceOnDisk);
					freeSpaceOnDisk = usableSpaceOnDisk;
				} else {
					freeSpaceOnDisk = capacityInBytes - filledBytes;
				}

				if (freeSpaceOnDisk < toleranceInBytes) {
					isRunningOutOfSpace = true;
					puffyPhysicalMediaList.add(physicalMediaName);
					log.warn("please, be worried: freeSpace (" + freeSpaceOnDisk + ") < tolerance(" + toleranceInBytes + ")? " + isRunningOutOfSpace);
				} else {
					log.debug("don't worry: freeSpace (" + freeSpaceOnDisk + ") < tolerance(" + toleranceInBytes + ")? " + isRunningOutOfSpace);
				}

			} // rs.next()

			if (!puffyPhysicalMediaList.isEmpty()) {
				Iterator<String> it = puffyPhysicalMediaList.iterator();
				String names = "";
				while (it.hasNext()) {
					String name = it.next();
					names = names.concat(name);
					if (it.hasNext()) {
						names = names.concat(", ");
					}
				}
				MailerSystem.send(ConfigurationSettings.EMAIL_EVENT_PMOUTOFSPACE, new String[] { names });
			}
		} catch (SQLException sqlEx) {
			log.error("Cannot retrieve PhysicalMedia informations from DB during the Physical Media timer task", sqlEx);
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				log.error("error closing ResultSet", e);
			}
			try {
				stat.close();
			} catch (SQLException e) {
				log.error("error closing Statement", e);
			}
			try {
				myConn.close();
			} catch (SQLException e) {
				log.error("error closing DB Connection", e);
			}
		}
		log.debug("leaving run()");
	}

	private Connection getDBConnection() throws SQLException {
		Connection dbConn = null;
		try {
			if (jndiContext == null) {
				jndiContext = new InitialContext();
			}

			DataSource dataSource = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
			dbConn = dataSource.getConnection();
		} catch (NamingException ex) {
			log.error("Unable to retrieve a connection from dbDS", ex);
		}

		return dbConn;
	}
}
