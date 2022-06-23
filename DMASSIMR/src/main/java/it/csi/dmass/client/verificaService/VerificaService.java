/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.client.verificaService;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 3.2.1
 * 2022-03-31T17:24:28.766+02:00
 * Generated source version: 3.2.1
 * 
 */
@WebService(targetNamespace = "http://dmacc.csi.it/", name = "VerificaService")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface VerificaService {

    @WebMethod
    @WebResult(name = "verificaOscuramentoDocResponse", targetNamespace = "http://dmacc.csi.it/", partName = "verificaOscuramentoDocResponse")
    public VerificaOscuramentoDocResponse verificaOscuramentoDoc(
        @WebParam(partName = "verificaOscuramentoDoc", name = "verificaOscuramentoDoc", targetNamespace = "http://dmacc.csi.it/")
        VerificaOscuramentoDocRequest verificaOscuramentoDoc
    );

    @WebMethod
    @WebResult(name = "verificaPinResponse", targetNamespace = "http://dmacc.csi.it/", partName = "verificaPinResponse")
    public VerificaPinResponse verificaPin(
        @WebParam(partName = "verificaPin", name = "verificaPin", targetNamespace = "http://dmacc.csi.it/")
        VerificaPinRequest verificaPin
    );

    @WebMethod
    @WebResult(name = "verificaUtenteAbilitatoResponse", targetNamespace = "http://dmacc.csi.it/", partName = "verificaUtenteAbilitatoResponse")
    public VerificaUtenteAbilitatoResponse verificaUtenteAbilitato(
        @WebParam(partName = "verificaUtenteAbilitato", name = "verificaUtenteAbilitato", targetNamespace = "http://dmacc.csi.it/")
        VerificaUtenteAbilitatoRequest verificaUtenteAbilitato
    );
}