/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.utils;
//
//import java.io.FileInputStream;
//import java.security.KeyStore;
//
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.TrustManagerFactory;
//import javax.net.ssl.X509TrustManager;
//import javax.security.cert.CertificateException;
//import javax.security.cert.X509Certificate;
//
//class MyX509TrustManager implements X509TrustManager {
//
//	     /*
//	      * The default PKIX X509TrustManager9.  We'll delegate
//	      * decisions to it, and fall back to the logic in this class if the
//	      * default X509TrustManager doesn't trust it.
//	      */
//	     X509TrustManager pkixTrustManager;
//
//	     MyX509TrustManager() throws Exception {
//	         // create a "default" JSSE X509TrustManager.
//
//	         KeyStore ks = KeyStore.getInstance("JKS");
//	         ks.load(new FileInputStream("trustedCerts"),
//	             "passphrase".toCharArray());
//
//	         TrustManagerFactory tmf =
//			TrustManagerFactory.getInstance("PKIX");
//	         tmf.init(ks);
//
//	         TrustManager tms [] = tmf.getTrustManagers();
//
//	         /*
//	          * Iterate over the returned trustmanagers, look
//	          * for an instance of X509TrustManager.  If found,
//	          * use that as our "default" trust manager.
//	          */
//	         for (int i = 0; i < tms.length; i++) {
//	             if (tms[i] instanceof X509TrustManager) {
//	                 pkixTrustManager = (X509TrustManager) tms[i];
//	                 return;
//	             }
//	         }
//
//	         /*
//	          * Find some other way to initialize, or else we have to fail the
//	          * constructor.
//	          */
//	         throw new Exception("Couldn't initialize");
//	     }
//
//	     /*
//	      * Delegate to the default trust manager.
//	      */
//	     public void checkClientTrusted(X509Certificate[] chain, String authType)
//	                 throws CertificateException {
//	         try {
//	             pkixTrustManager.checkClientTrusted(chain, authType);
//	         } catch (CertificateException excep) {
//	             // do any special handling here, or rethrow exception.
//	         }
//	     }
//
//	     /*
//	      * Delegate to the default trust manager.
//	      */
//	     public void checkServerTrusted(X509Certificate[] chain, String authType)
//	                 throws CertificateException {
//	         try {
//	             pkixTrustManager.checkServerTrusted(chain, authType);
//	         } catch (CertificateException excep) {
//	             /*
//	              * Possibly pop up a dialog box asking whether to trust the
//	              * cert chain.
//	              */
//	         }
//	     }
//
//	     /*
//	      * Merely pass this through.
//	      */
//	     public X509Certificate[] getAcceptedIssuers() {
//	         return pkixTrustManager.getAcceptedIssuers();
//	     }
//
//		
//	}

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class MyX509TrustManager implements X509TrustManager
{
public void checkClientTrusted(X509Certificate[] arg0, String arg1)
throws CertificateException {
}

public void checkServerTrusted(X509Certificate[] arg0, String arg1)
throws CertificateException {
}

public X509Certificate[] getAcceptedIssuers() {
return null;
}

public boolean isServerTrusted(java.security.cert.X509Certificate[] x509Certificates) {
return true;
}

public boolean isClientTrusted(java.security.cert.X509Certificate[] x509Certificates) {
return true;
}
}
