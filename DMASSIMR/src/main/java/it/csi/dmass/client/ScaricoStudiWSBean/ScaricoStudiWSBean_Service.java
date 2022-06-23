/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.client.ScaricoStudiWSBean;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 3.2.1
 * 2022-03-31T17:24:27.734+02:00
 * Generated source version: 3.2.1
 * 
 */
@WebServiceClient(name = "ScaricoStudiWSBean", 
                  wsdlLocation = "classpath:wsdl/ScaricoStudiWSBean.wsdl",
                  targetNamespace = "http://dmass.csi.it/") 
public class ScaricoStudiWSBean_Service extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://dmass.csi.it/", "ScaricoStudiWSBean");
    public final static QName ScaricoStudiWSBeanPort = new QName("http://dmass.csi.it/", "ScaricoStudiWSBeanPort");
    static {
        URL url = ScaricoStudiWSBean_Service.class.getClassLoader().getResource("wsdl/ScaricoStudiWSBean.wsdl");
        if (url == null) {
            java.util.logging.Logger.getLogger(ScaricoStudiWSBean_Service.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "classpath:wsdl/ScaricoStudiWSBean.wsdl");
        }       
        WSDL_LOCATION = url;   
    }

    public ScaricoStudiWSBean_Service(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public ScaricoStudiWSBean_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ScaricoStudiWSBean_Service() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    public ScaricoStudiWSBean_Service(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    public ScaricoStudiWSBean_Service(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public ScaricoStudiWSBean_Service(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }    




    /**
     *
     * @return
     *     returns ScaricoStudiWSBean
     */
    @WebEndpoint(name = "ScaricoStudiWSBeanPort")
    public ScaricoStudiWSBean getScaricoStudiWSBeanPort() {
        return super.getPort(ScaricoStudiWSBeanPort, ScaricoStudiWSBean.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ScaricoStudiWSBean
     */
    @WebEndpoint(name = "ScaricoStudiWSBeanPort")
    public ScaricoStudiWSBean getScaricoStudiWSBeanPort(WebServiceFeature... features) {
        return super.getPort(ScaricoStudiWSBeanPort, ScaricoStudiWSBean.class, features);
    }

}