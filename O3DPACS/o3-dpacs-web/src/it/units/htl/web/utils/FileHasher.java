/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.units.htl.web.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileHasher {
	static final Log log = LogFactory.getLog(FileHasher.class);
	
	public String doHash(File file, String hashAlgorithm) {
		MessageDigest messageDigest;
		String result = null;
		try {
			messageDigest = MessageDigest.getInstance(hashAlgorithm);
			FileInputStream streamForHash = null;
			streamForHash = new FileInputStream(file);
			byte[] input = new byte[streamForHash.available()];
			streamForHash.read(input);
			messageDigest.update(input);
			byte[] output;
			output = messageDigest.digest();
			result = getHexString(output);
			try {
				streamForHash.close();
			} catch (Exception ex) {
			}
		} catch (NoSuchAlgorithmException e) {
			log.error("Error making hash",e);
		} catch (FileNotFoundException e) {
			log.error("Error making hash",e);
		} catch (IOException e) {
			log.error("Error making hash",e);
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
