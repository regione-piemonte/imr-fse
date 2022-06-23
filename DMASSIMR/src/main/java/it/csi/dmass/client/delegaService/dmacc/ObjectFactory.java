/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.delegaService.dmacc;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import it.csi.dmass.client.delegaService.dma.Delegante;
import it.csi.dmass.client.delegaService.dma.GetDeleganti2IN;
import it.csi.dmass.client.delegaService.dma.RichiedenteInfo;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the it.csi.dmass.client.delegaService.dmacc package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Delega_QNAME = new QName("http://dmacc.csi.it/", "delega");
    private final static QName _Delegante_QNAME = new QName("http://dmacc.csi.it/", "delegante");
    private final static QName _GetDeleganti2IN_QNAME = new QName("http://dmacc.csi.it/", "getDeleganti2IN");
    private final static QName _GetDeleganti2Request_QNAME = new QName("http://dmacc.csi.it/", "getDeleganti2Request");
    private final static QName _GetDeleganti2Response_QNAME = new QName("http://dmacc.csi.it/", "getDeleganti2Response");
    private final static QName _GetDelegantiRequest_QNAME = new QName("http://dmacc.csi.it/", "getDelegantiRequest");
    private final static QName _GetDelegantiResponse_QNAME = new QName("http://dmacc.csi.it/", "getDelegantiResponse");
    private final static QName _RichiedenteInfo_QNAME = new QName("http://dmacc.csi.it/", "richiedenteInfo");
    private final static QName _GetDeleganti_QNAME = new QName("http://dmacc.csi.it/", "getDeleganti");
    private final static QName _GetDeleganti2_QNAME = new QName("http://dmacc.csi.it/", "getDeleganti2");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: it.csi.dmass.client.delegaService.dmacc
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetDeleganti2Request }
     * 
     */
    public GetDeleganti2Request createGetDeleganti2Request() {
        return new GetDeleganti2Request();
    }

    /**
     * Create an instance of {@link GetDeleganti2Response }
     * 
     */
    public GetDeleganti2Response createGetDeleganti2Response() {
        return new GetDeleganti2Response();
    }

    /**
     * Create an instance of {@link GetDelegantiRequest }
     * 
     */
    public GetDelegantiRequest createGetDelegantiRequest() {
        return new GetDelegantiRequest();
    }

    /**
     * Create an instance of {@link GetDelegantiResponse }
     * 
     */
    public GetDelegantiResponse createGetDelegantiResponse() {
        return new GetDelegantiResponse();
    }

    /**
     * Create an instance of {@link ServiceResponse }
     * 
     */
    public ServiceResponse createServiceResponse() {
        return new ServiceResponse();
    }

    /**
     * Create an instance of {@link EnsResponse }
     * 
     */
    public EnsResponse createEnsResponse() {
        return new EnsResponse();
    }

    /**
     * Create an instance of {@link EnsMessagebody }
     * 
     */
    public EnsMessagebody createEnsMessagebody() {
        return new EnsMessagebody();
    }

    /**
     * Create an instance of {@link EnsRequest }
     * 
     */
    public EnsRequest createEnsRequest() {
        return new EnsRequest();
    }

    /**
     * Create an instance of {@link ApplicativoVerticale }
     * 
     */
    public ApplicativoVerticale createApplicativoVerticale() {
        return new ApplicativoVerticale();
    }

    /**
     * Create an instance of {@link Codifica }
     * 
     */
    public Codifica createCodifica() {
        return new Codifica();
    }

    /**
     * Create an instance of {@link ApplicazioneRichiedente }
     * 
     */
    public ApplicazioneRichiedente createApplicazioneRichiedente() {
        return new ApplicazioneRichiedente();
    }

    /**
     * Create an instance of {@link it.csi.dmass.client.delegaService.dmacc.Delega }
     * 
     */
    public it.csi.dmass.client.delegaService.dmacc.Delega createDelega() {
        return new it.csi.dmass.client.delegaService.dmacc.Delega();
    }

    /**
     * Create an instance of {@link Errore }
     * 
     */
    public Errore createErrore() {
        return new Errore();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link it.csi.dmass.client.delegaService.dma.Delega }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "delega")
    public JAXBElement<it.csi.dmass.client.delegaService.dma.Delega> createDelega(it.csi.dmass.client.delegaService.dma.Delega value) {
        return new JAXBElement<it.csi.dmass.client.delegaService.dma.Delega>(_Delega_QNAME, it.csi.dmass.client.delegaService.dma.Delega.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Delegante }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "delegante")
    public JAXBElement<Delegante> createDelegante(Delegante value) {
        return new JAXBElement<Delegante>(_Delegante_QNAME, Delegante.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDeleganti2IN }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "getDeleganti2IN")
    public JAXBElement<GetDeleganti2IN> createGetDeleganti2IN(GetDeleganti2IN value) {
        return new JAXBElement<GetDeleganti2IN>(_GetDeleganti2IN_QNAME, GetDeleganti2IN.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDeleganti2Request }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "getDeleganti2Request")
    public JAXBElement<GetDeleganti2Request> createGetDeleganti2Request(GetDeleganti2Request value) {
        return new JAXBElement<GetDeleganti2Request>(_GetDeleganti2Request_QNAME, GetDeleganti2Request.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDeleganti2Response }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "getDeleganti2Response")
    public JAXBElement<GetDeleganti2Response> createGetDeleganti2Response(GetDeleganti2Response value) {
        return new JAXBElement<GetDeleganti2Response>(_GetDeleganti2Response_QNAME, GetDeleganti2Response.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDelegantiRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "getDelegantiRequest")
    public JAXBElement<GetDelegantiRequest> createGetDelegantiRequest(GetDelegantiRequest value) {
        return new JAXBElement<GetDelegantiRequest>(_GetDelegantiRequest_QNAME, GetDelegantiRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDelegantiResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "getDelegantiResponse")
    public JAXBElement<GetDelegantiResponse> createGetDelegantiResponse(GetDelegantiResponse value) {
        return new JAXBElement<GetDelegantiResponse>(_GetDelegantiResponse_QNAME, GetDelegantiResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RichiedenteInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "richiedenteInfo")
    public JAXBElement<RichiedenteInfo> createRichiedenteInfo(RichiedenteInfo value) {
        return new JAXBElement<RichiedenteInfo>(_RichiedenteInfo_QNAME, RichiedenteInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDelegantiRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "getDeleganti")
    public JAXBElement<GetDelegantiRequest> createGetDeleganti(GetDelegantiRequest value) {
        return new JAXBElement<GetDelegantiRequest>(_GetDeleganti_QNAME, GetDelegantiRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDeleganti2Request }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "getDeleganti2")
    public JAXBElement<GetDeleganti2Request> createGetDeleganti2(GetDeleganti2Request value) {
        return new JAXBElement<GetDeleganti2Request>(_GetDeleganti2_QNAME, GetDeleganti2Request.class, null, value);
    }

}
