/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.viewer.auth;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;

import it.units.htl.web.viewer.auth.utils.KTCServiceStub;

public class ServiceCaller2 {
    public void doCall(){
    KTCServiceStub stub = null;
    try {
        stub = new KTCServiceStub("http://172.18.63.14/ktc/Custom.KS.KTCService.cls");
    } catch (AxisFault e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    stub._getServiceClient().getOptions().setProperty(
            org.apache.axis2.transport.http.HTTPConstants.HTTP_PROTOCOL_VERSION,
            org.apache.axis2.transport.http.HTTPConstants.HEADER_PROTOCOL_10);

    
//    KTCServiceStub.Echo echo = new KTCServiceStub.Echo();
//    echo.setStr("Hello from Tipws");    
//    try {
//        System.out.println(stub.echo(echo).getEchoResult());
//    } catch (RemoteException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }
    
    
    KTCServiceStub.GetSessionInfo sinfo = new KTCServiceStub.GetSessionInfo();
    
    sinfo.setServer("W2K3KSVMTDF");
    sinfo.setUser("89983J");
    sinfo.setSessionId("000233232jklskk002332338948763");

    KTCServiceStub.GetSessionInfoResponse resp = null;
    try {
        resp = stub.getSessionInfo(sinfo);
    } catch (RemoteException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    System.out.println("User: " + resp.getGetSessionInfoResult().getLogonUserName() + " " +
                           "Cookie: " + resp.getGetSessionInfoResult().getSessionCookie());
    }
}
