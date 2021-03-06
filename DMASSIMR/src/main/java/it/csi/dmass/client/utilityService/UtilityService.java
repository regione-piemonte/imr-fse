/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.client.utilityService;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 3.2.1
 * 2022-03-31T17:24:27.083+02:00
 * Generated source version: 3.2.1
 * 
 */
@WebService(targetNamespace = "http://dmacc.csi.it/", name = "UtilityService")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface UtilityService {

    @WebMethod
    @WebResult(name = "GetMessaggiDiServizioResponse", targetNamespace = "http://dmacc.csi.it/", partName = "GetMessaggiDiServizioResponse")
    public GetMessaggiDiServizioResponse getMessaggiDiServizio(
        @WebParam(partName = "getMessaggiDiServizio", name = "getMessaggiDiServizio", targetNamespace = "http://dmacc.csi.it/")
        GetMessaggiDiServizioRequest getMessaggiDiServizio
    );

    @WebMethod
    @WebResult(name = "SetAuditResponse", targetNamespace = "http://dmacc.csi.it/", partName = "SetAuditResponse")
    public SetAuditResponse setAudit(
        @WebParam(partName = "setAudit", name = "setAudit", targetNamespace = "http://dmacc.csi.it/")
        SetAuditRequest setAudit
    );
}
