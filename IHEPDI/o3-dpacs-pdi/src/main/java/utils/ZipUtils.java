/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * Fornisce metodi per la compressione del Contenuto PDI
 */
public class ZipUtils {
	
	private static final Logger log = Logger.getLogger(ZipUtils.class);

	/**
	 * Crea uno ZIP a partire dalla directory specificata
	 * @param directoryToZip directory da comprimere
	 * @param directoryForZip directory dove creare il file ZIP
	 * @param fileName il nome del file ZIP
	 */
    public static void zipDirectory(String directoryToZip, String directoryForZip, String fileName) {
		log.info("Zipping directory: " + directoryForZip);
    	try {
	    	String zipTo = directoryForZip + fileName + ".zip";
	        FileOutputStream fos = new FileOutputStream(zipTo);
	        ZipOutputStream zipOut = new ZipOutputStream(fos);
	        File fileToZip = new File(directoryToZip);
        	zipFile(fileToZip, directoryToZip, zipOut, directoryToZip);
	        zipOut.close();
	        fos.close();
    	} catch (Exception e) {
			log.error("Failed to zip directory due to: " + e.getMessage(),e);
			e.printStackTrace();
		}
		log.info("Directory zipped successfully");
    }

    /**
     * Crea l'effettivo file ZIP
     * @param fileToZip file da comprimere
     * @param fileName nome del file da comprimere
     */
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, String root) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        
        
        if (fileToZip.isDirectory()) {
        	if (fileName.endsWith("/")) {
        		if(!fileName.equals(root)) {
        			if(fileName.contains(root)) {
        				fileName = fileName.substring(root.length()+1);
        			}
        			fileName = fileName.substring(root.length()+1);
        			zipOut.putNextEntry(new ZipEntry(fileName));
        			zipOut.closeEntry();
        		} 
        	} else {
        		if(!fileName.equals(root)) {
        			if(fileName.contains(root)) {
        				fileName = fileName.substring(root.length()+1);
        			}
        			zipOut.putNextEntry(new ZipEntry(fileName + "/"));
        			zipOut.closeEntry();
        		} 
        	}
        	File[] children = fileToZip.listFiles();
        	for (File childFile : children) {
        		zipFile(childFile, fileName + "/" + childFile.getName(), zipOut, root);
        	}
        	return;

        }
        
        FileInputStream fis = new FileInputStream(fileToZip);
        
        if(!fileName.equals(root)) {
        	if(fileName.contains(root)) {
        		fileName = fileName.substring(root.length()+1);
        	}
	        ZipEntry zipEntry = new ZipEntry(fileName);
	        zipOut.putNextEntry(zipEntry);
	        byte[] bytes = new byte[1024];
	        int length;
	        while ((length = fis.read(bytes)) >= 0) {
	            zipOut.write(bytes, 0, length);
	        }
        }
        fis.close();
    }
    
    /**
     * Decomprimere un file zip fornito in input nella directory specificata
     * @param fileZip file da decomprimere
     * @param to directory dove decomprimere
     */
    public static void unzip(String fileZip, String to) throws IOException {
    	File destDir = new File(to);
        byte[] buffer = new byte[1024];
        @SuppressWarnings("resource")
		ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
       }
        
       zis.closeEntry();
       zis.close();
    }
    
    /**
     * Gestisce i contenuti del file zip
     * @param destinationDir la directory destinazione
     * @param zipEntry contenuto del file zip
     * @return un file o directory per il contento del file zip
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
