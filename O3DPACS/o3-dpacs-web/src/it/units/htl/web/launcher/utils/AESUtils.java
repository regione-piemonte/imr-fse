/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.launcher.utils;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AESUtils {
    private final String ALGO = "AES";
    private String keyValue;

    public String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        Base64 bs = new Base64();
        String encryptedValue = new String(bs.encode(encVal));
        return encryptedValue;
    }

    public String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        Base64 bs = new Base64();
        byte[] decordedValue = bs.decode(encryptedData.getBytes());
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    public String domachoDecrypt(String encrData) {
        byte[] decodedString = Base64.decodeBase64(encrData.getBytes());
        String toString = new String(decodedString);
        StringBuilder sb = new StringBuilder();
        int k = 0;
        for (int i = 0; i < toString.length(); i++) {
            int chI = (int) toString.charAt(i);
            int keyCh = (int) keyValue.codePointAt(k);
            int val = chI - keyCh;
            if (val < 0) {
                val += 128;
            }
            sb.append((char) (val));
            k += 1;
            if (k == keyValue.length())
                k = 0;
        }
        return sb.toString();
    }

    private Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue.getBytes(), ALGO);
        return key;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }
}
