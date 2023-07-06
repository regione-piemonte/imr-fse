/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.libraries.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TokenVerifier {
    private Log log = LogFactory.getLog(TokenVerifier.class);
    /**
     * @param args
     */
    public boolean verifyToken(String token){
        log.info(token + " verified!!");
        if("invalid".equals(token)) return false;
        return true;
    }
}
