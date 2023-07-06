/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {

	public static final String SHA1 = "SHA-1";
	
	/**
	 * Calcola la digest per il file fornito in input
	 * @param file file per cui calcolare la digest
	 * @param algorithm l'algoritmo per la digest
	 * @return la digest per il file fornito in input
	 */
	public static String calcolaDigest(File file, String algorithm) {
		if (!file.exists() || !file.isFile()) { 
    		return null; 
    	}
		
    	byte[] mdbytes = null;
    	FileInputStream fis  = null;
        try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			fis = new FileInputStream(file);
			byte[] dataBytes = new byte[1024];
			int nread = 0; 
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}
			mdbytes = md.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if (fis != null) { 
					fis.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
        if (mdbytes == null) {
        	return null;
        }
        
        StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
		  sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		
        return sb.toString();
    }
}
