/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.delegaService.dmacc;

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 3.2.1
 * 2022-03-31T17:24:29.605+02:00
 * Generated source version: 3.2.1
 * 
 */
public final class DelegaService_DelegaServicePort_Client {

    private static final QName SERVICE_NAME = new QName("http://dmacc.csi.it/", "DelegaService");

    private DelegaService_DelegaServicePort_Client() {
    }

    public static void main(String args[]) throws java.lang.Exception {
        URL wsdlURL = DelegaService_Service.WSDL_LOCATION;
        if (args.length > 0 && args[0] != null && !"".equals(args[0])) { 
            File wsdlFile = new File(args[0]);
            try {
                if (wsdlFile.exists()) {
                    wsdlURL = wsdlFile.toURI().toURL();
                } else {
                    wsdlURL = new URL(args[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
      
        DelegaService_Service ss = new DelegaService_Service(wsdlURL, SERVICE_NAME);
        DelegaService port = ss.getDelegaServicePort();  
        
        {
        System.out.println("Invoking getDeleganti...");
        it.csi.dmass.client.delegaService.dmacc.GetDelegantiRequest _getDeleganti_getDeleganti = null;
        it.csi.dmass.client.delegaService.dmacc.GetDelegantiResponse _getDeleganti__return = port.getDeleganti(_getDeleganti_getDeleganti);
        System.out.println("getDeleganti.result=" + _getDeleganti__return);


        }
        {
        System.out.println("Invoking getDeleganti2...");
        it.csi.dmass.client.delegaService.dmacc.GetDeleganti2Request _getDeleganti2_getDeleganti2 = null;
        it.csi.dmass.client.delegaService.dmacc.GetDeleganti2Response _getDeleganti2__return = port.getDeleganti2(_getDeleganti2_getDeleganti2);
        System.out.println("getDeleganti2.result=" + _getDeleganti2__return);


        }

        System.exit(0);
    }

}
