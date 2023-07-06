/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.Serializable;
import java.util.StringTokenizer;


/**
 * The class AE data keeps all the informations about an Application Entity including
 * host, port and so on
 * @author Mbe
 */
public class AEData implements Serializable {

    private static final long serialVersionUID = 7777777L;
    
    // Constants -----------------------------------------------------
    static final String[] EMPTY_STRING_ARRAY = {};
    // Variables -----------------------------------------------------
    private final String title;
    private final String host;
    private final int port;
    private final String cipherSuites;
    private final boolean mobile;
    
    private final boolean _isAnonymized;
    private final boolean _canDeanonymized;

    // Constructors --------------------------------------------------
    public AEData(String title, String host, int port, String cipherSuites) {
        this.title = title;
        this.host = host;
        this.port = port;
        this.cipherSuites = cipherSuites;
        this.mobile = false;
        _isAnonymized = false;
        _canDeanonymized = false;
    }

    public AEData(String title, String host, int port, String cipherSuites, boolean mobile) {
        this.title = title;
        this.host = host;
        this.port = port;
        this.cipherSuites = cipherSuites;
        this.mobile = mobile;
        _isAnonymized = false;
        _canDeanonymized = false;
    }
    
    public AEData(String title, String host, int port,String cipherSuites, boolean isAnonymized, boolean canDeanonymized){
        this.title = title;
        this.host = host;
        this.port = port;
        this.cipherSuites = null;
        this.mobile = false;
        _isAnonymized = isAnonymized;
        _canDeanonymized = canDeanonymized;
    }

    /** Getter for property title.
     * @return Value of property title.
     */
    public java.lang.String getTitle() {
        return title;
    }

    /** Getter for property host.
     * @return Value of property host.
     */
    public java.lang.String getHost() {
        return host;
    }

    /** Getter for property port.
     * @return Value of property port.
     */
    public int getPort() {
        return port;
    }

    /** Getter for property cipherSuites.
     * @return Value of property cipherSuites.
     */
    public java.lang.String[] getCipherSuites() {
        if (cipherSuites == null || cipherSuites.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        StringTokenizer stk = new StringTokenizer(cipherSuites, " ,");
        String[] retval = new String[stk.countTokens()];
        for (int i = 0; i < retval.length; ++i) {
            retval[i] = stk.nextToken();
        }
        return retval;
    }

    public boolean isAnonymized() {
        return _isAnonymized;
    }

    public boolean canDeanonymized() {
        return _canDeanonymized;
    }

    public boolean isMobile() {
        return mobile;
    }

   
    /**
     * From dcm4che, if you need to use TLS for comunicating to an AE, the 
     * appropriate protocol should be used. This is stored in Knownnodes table
     * and parsed here for initiating associations.
     * @return the protocol prefix string
     */
    private String prefix() {
        if (cipherSuites == null || cipherSuites.length() == 0) {
            return "dicom://";
        }
        if ("SSL_RSA_WITH_NULL_SHA".equals(cipherSuites)) {
            return "dicom-tls.nodes://";
        }
        if ("SSL_RSA_WITH_3DES_EDE_CBC_SHA".equals(cipherSuites)) {
            return "dicom-tls.3des://";
        }
        return "dicom-tls://";
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(prefix()).append(getTitle()).append('@').append(getHost()).append(':').append(getPort());
        return sb.toString();
    }
}