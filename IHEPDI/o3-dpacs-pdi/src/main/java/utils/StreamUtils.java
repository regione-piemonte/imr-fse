/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package utils;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * Classe per operazioni sugli stream
 */
public class StreamUtils {
	
	private static final Logger log = Logger.getLogger(StreamUtils.class);

	/**
	 * Metodo utile alla chiusura della connessione al database
	 * @param closeable variable contenente la connessione al database
	 */
	public static void close(Connection closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				log.error("unable to close connection", e);
			}
		}
	}

	/**
	 * Metodo utile alla chiusura del ResultSet
	 * @param closeable variable contenente un ResultSet
	 */
	public static void close(ResultSet closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				log.error("unable to close resultset", e);
			}
		}
	}

	/**
	 * Metodo utile alla chiusura dello Statement
	 * @param closeable variable contenente uno Statement
	 */
	public static void close(Statement closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				log.error("unable to close statement", e);
			}
		}
	}

	/**
	 * Metodo utile alla chiusura dello stream
	 * @param closeable variable contenente uno stream
	 */
	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				log.error("unable to close closeable", e);
			}
		}
	}

	/**
	 * Fornisce il tempo di attesa in caso di necessita' di dover tentare di nuovo
	 * la connessione ad un servizio
	 */
	public static void waitForRetry(int wait) {
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Pulisce i contenuti locali
	 * @param file contenuti da pulire
	 */
	public static void cleanLocalFile(String file) {
		log.info("Cleaning: " + file);
		File toDeleteFile = new File(file);
		toDeleteFile.delete();
		log.info("Local file cleaned");
	}
	
	/**
	 * Pulisce la directory locale
	 * @param directory la directory da pulire
	 */
	public static void cleanLocalDirectory(File directory) {
	    File[] files = directory.listFiles();
	    if (files != null) {
	        for (File file: files) {
	        	cleanLocalDirectory(file);
	        }
	    }
	    
	    directory.delete();
	}
    
	/**
	 * Questo metodo crea e scrive un file
	 * @param path percorso in cui creare il file
	 * @param text contenuto del file
	 */
	public static void writeFile(String path, String text) {
		try {
			File viewFile = new File(path);
			FileWriter fileWriter = new FileWriter(viewFile);
			fileWriter.write(text);
			fileWriter.close();
		} catch (IOException e) {
			log.error("Error while writing file: " + e.getMessage(),e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Trasfiresce un file da una directory ad un'altra
	 * @param from directory di partenza
	 * @param to directory destinazione
	 */
	public static void copyFile(String from, String to) throws IOException {
		log.info("FROM: " + from);
		log.info("TO: " + to);
		FileInputStream fis = new FileInputStream(from);
		FileOutputStream fos = new FileOutputStream(to);
		byte[] buffer = new byte[8 * 1024];
		int bytesRead;
		while ((bytesRead = fis.read(buffer)) != -1) {
			fos.write(buffer, 0, bytesRead);
		}
		fis.close();
		fos.close();
	}
	
	/**
	 * Questo metodo crea una cartella
	 * @param directoryPath percorso in cui creare la cartella
	 * @param createdDirectory nome della cartella da creare
	 */
	public static void makeDir(String directoryPath, String createdDirectory) {
		File directory = new File(directoryPath);
		log.info("Creating directory for " + createdDirectory);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}
	
	/**
	 * Crea la directory specificata
	 * @param directory la directory da creare
     * @param withReadMe indica se creare o meno un file read me nella directory
	 * @return the created directory
	 */
	public static File makeDir(String directory, boolean withReadMe) {
		log.info("Creating directory...");
		File dir = new File(directory);
		log.info("Directory to create: " + directory + ", directory as file: " + dir);
		if (!dir.exists()) {
			dir.mkdirs();
			log.info("Directory created successfully");
		}
		
		if (withReadMe) {
			log.info("Creating README.txt file for directory...");
			StreamUtils.createFile(directory, "README.txt", "");
		}
		
		return dir;
	}
	
	/**
	 * Crea un nuovo file nella directory specificata con il nome specificato e il contenuto fornito
	 * @param directoryToZip directory dove creare il file
	 * @param name nome del file
	 * @param content contenuto del file
	 */
	public static void createFile(String directoryToZip, String name, String content) {
		try {
			File script = new File(directoryToZip + "/" + name);
			BufferedWriter writer = new BufferedWriter(new FileWriter(script));
		    writer.write(content);
		    writer.close();
		} catch (Exception e) {
			log.error("Cannot add file due to: " + e.getMessage(), e);
		}
	}
}
