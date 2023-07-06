/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileHasher {
	static final Log log = LogFactory.getLog(FileHasher.class);
	int bufferSize;
	
	public String doHash(File file, String hashAlgorithm) {
		MessageDigest messageDigest;
		String result = null;
		FileInputStream streamForHash = null;

		String bufferSizeRes = null;
	
		try {
			//bufferSizeRes = MBeanDealer.getPropertyFromMbean(new ObjectName("it.units.htl.dpacs.servers:type=DicomServer,index=3"), "getFileHasherBufferSizeInKb"); //GDC
			bufferSizeRes = MBeanDealer.getPropertyFromMbean(new ObjectName("it.units.htl.dpacs.servers:type=DicomServer,index=3"), "FileHasherBufferSizeInKb");
		} catch (MalformedObjectNameException mone) {	
		    log.error("error reading buffer size", mone);
		} catch (NullPointerException npe) {
			log.error("error reading buffer size", npe);
		}
		
		bufferSize = Integer.parseInt(bufferSizeRes);

		try {
			messageDigest = MessageDigest.getInstance(hashAlgorithm);
			streamForHash = new FileInputStream(file);
			
			byte[] buffer = new byte[bufferSize];

			int bytesRead = streamForHash.read(buffer);
			while (bytesRead != -1) {
				messageDigest.update(buffer, 0, bytesRead);
				bytesRead = streamForHash.read(buffer);
            }			
			byte[] output;
			output = messageDigest.digest();
			result = getHexString(output);
			
		} catch (NoSuchAlgorithmException e) {
			log.error("Error making hash",e);
		} catch (FileNotFoundException e) {
			log.error("Error making hash",e);
		} catch (IOException e) {
			log.error("Error making hash",e);
		} finally {
			try {
				streamForHash.close();
			} catch (Exception ex) {
			}
		}
		return result;
	}
	
	public String doHash(byte[] file, String hashAlgorithm) {
		MessageDigest messageDigest;
		String result = null;
		try {
			messageDigest = MessageDigest.getInstance(hashAlgorithm);
			messageDigest.update(file);
			byte[] output;
			output = messageDigest.digest();
			result = getHexString(output);
		} catch (NoSuchAlgorithmException e) {
			log.error("Error making hash",e);
		} 
		return result;
	}
	
	private String getHexString(byte[] b) {
		StringBuilder builder = new StringBuilder(40);
		int len = b.length;
		for (int i = 0; i < len; i++) {
			builder.append(Integer.toString((b[i] & 0xFF) + 0x100, 16)
					.substring(1));
		}
		return builder.toString();
	}
}