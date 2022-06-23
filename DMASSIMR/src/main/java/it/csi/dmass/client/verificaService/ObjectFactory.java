/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.verificaService;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the it.csi.dmass.client.verificaService package. 
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

    private final static QName _VerificaOscuramentoDocRequest_QNAME = new QName("http://dmacc.csi.it/", "VerificaOscuramentoDocRequest");
    private final static QName _VerificaPinRequest_QNAME = new QName("http://dmacc.csi.it/", "VerificaPinRequest");
    private final static QName _RichiedenteInfo_QNAME = new QName("http://dmacc.csi.it/", "richiedenteInfo");
    private final static QName _VerificaOscuramentoDoc_QNAME = new QName("http://dmacc.csi.it/", "verificaOscuramentoDoc");
    private final static QName _VerificaOscuramentoDocResponse_QNAME = new QName("http://dmacc.csi.it/", "verificaOscuramentoDocResponse");
    private final static QName _VerificaPin_QNAME = new QName("http://dmacc.csi.it/", "verificaPin");
    private final static QName _VerificaPinResponse_QNAME = new QName("http://dmacc.csi.it/", "verificaPinResponse");
    private final static QName _VerificaUtenteAbilitato_QNAME = new QName("http://dmacc.csi.it/", "verificaUtenteAbilitato");
    private final static QName _VerificaUtenteAbilitatoResponse_QNAME = new QName("http://dmacc.csi.it/", "verificaUtenteAbilitatoResponse");
    private final static QName _DatiDocumentoResponse_QNAME = new QName("http://dma.csi.it/", "datiDocumentoResponse");
    private final static QName _ListaProfili_QNAME = new QName("http://dma.csi.it/", "listaProfili");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: it.csi.dmass.client.verificaService
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link VerificaOscuramentoDocRequest }
     * 
     */
    public VerificaOscuramentoDocRequest createVerificaOscuramentoDocRequest() {
        return new VerificaOscuramentoDocRequest();
    }

    /**
     * Create an instance of {@link VerificaPinRequest }
     * 
     */
    public VerificaPinRequest createVerificaPinRequest() {
        return new VerificaPinRequest();
    }

    /**
     * Create an instance of {@link RichiedenteInfo }
     * 
     */
    public RichiedenteInfo createRichiedenteInfo() {
        return new RichiedenteInfo();
    }

    /**
     * Create an instance of {@link VerificaOscuramentoDocResponse }
     * 
     */
    public VerificaOscuramentoDocResponse createVerificaOscuramentoDocResponse() {
        return new VerificaOscuramentoDocResponse();
    }

    /**
     * Create an instance of {@link VerificaPinResponse }
     * 
     */
    public VerificaPinResponse createVerificaPinResponse() {
        return new VerificaPinResponse();
    }

    /**
     * Create an instance of {@link VerificaUtenteAbilitatoRequest }
     * 
     */
    public VerificaUtenteAbilitatoRequest createVerificaUtenteAbilitatoRequest() {
        return new VerificaUtenteAbilitatoRequest();
    }

    /**
     * Create an instance of {@link VerificaUtenteAbilitatoResponse }
     * 
     */
    public VerificaUtenteAbilitatoResponse createVerificaUtenteAbilitatoResponse() {
        return new VerificaUtenteAbilitatoResponse();
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
     * Create an instance of {@link Errore }
     * 
     */
    public Errore createErrore() {
        return new Errore();
    }

    /**
     * Create an instance of {@link DatiDocumentoResponse }
     * 
     */
    public DatiDocumentoResponse createDatiDocumentoResponse() {
        return new DatiDocumentoResponse();
    }

    /**
     * Create an instance of {@link Ruolo }
     * 
     */
    public Ruolo createRuolo() {
        return new Ruolo();
    }

    /**
     * Create an instance of {@link DatiDocumento }
     * 
     */
    public DatiDocumento createDatiDocumento() {
        return new DatiDocumento();
    }

    /**
     * Create an instance of {@link ServiceResponse }
     * 
     */
    public ServiceResponse createServiceResponse() {
        return new ServiceResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificaOscuramentoDocRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "VerificaOscuramentoDocRequest")
    public JAXBElement<VerificaOscuramentoDocRequest> createVerificaOscuramentoDocRequest(VerificaOscuramentoDocRequest value) {
        return new JAXBElement<VerificaOscuramentoDocRequest>(_VerificaOscuramentoDocRequest_QNAME, VerificaOscuramentoDocRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificaPinRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "VerificaPinRequest")
    public JAXBElement<VerificaPinRequest> createVerificaPinRequest(VerificaPinRequest value) {
        return new JAXBElement<VerificaPinRequest>(_VerificaPinRequest_QNAME, VerificaPinRequest.class, null, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificaOscuramentoDocRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "verificaOscuramentoDoc")
    public JAXBElement<VerificaOscuramentoDocRequest> createVerificaOscuramentoDoc(VerificaOscuramentoDocRequest value) {
        return new JAXBElement<VerificaOscuramentoDocRequest>(_VerificaOscuramentoDoc_QNAME, VerificaOscuramentoDocRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificaOscuramentoDocResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "verificaOscuramentoDocResponse")
    public JAXBElement<VerificaOscuramentoDocResponse> createVerificaOscuramentoDocResponse(VerificaOscuramentoDocResponse value) {
        return new JAXBElement<VerificaOscuramentoDocResponse>(_VerificaOscuramentoDocResponse_QNAME, VerificaOscuramentoDocResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificaPinRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "verificaPin")
    public JAXBElement<VerificaPinRequest> createVerificaPin(VerificaPinRequest value) {
        return new JAXBElement<VerificaPinRequest>(_VerificaPin_QNAME, VerificaPinRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificaPinResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "verificaPinResponse")
    public JAXBElement<VerificaPinResponse> createVerificaPinResponse(VerificaPinResponse value) {
        return new JAXBElement<VerificaPinResponse>(_VerificaPinResponse_QNAME, VerificaPinResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificaUtenteAbilitatoRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "verificaUtenteAbilitato")
    public JAXBElement<VerificaUtenteAbilitatoRequest> createVerificaUtenteAbilitato(VerificaUtenteAbilitatoRequest value) {
        return new JAXBElement<VerificaUtenteAbilitatoRequest>(_VerificaUtenteAbilitato_QNAME, VerificaUtenteAbilitatoRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerificaUtenteAbilitatoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dmacc.csi.it/", name = "verificaUtenteAbilitatoResponse")
    public JAXBElement<VerificaUtenteAbilitatoResponse> createVerificaUtenteAbilitatoResponse(VerificaUtenteAbilitatoResponse value) {
        return new JAXBElement<VerificaUtenteAbilitatoResponse>(_VerificaUtenteAbilitatoResponse_QNAME, VerificaUtenteAbilitatoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DatiDocumentoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dma.csi.it/", name = "datiDocumentoResponse")
    public JAXBElement<DatiDocumentoResponse> createDatiDocumentoResponse(DatiDocumentoResponse value) {
        return new JAXBElement<DatiDocumentoResponse>(_DatiDocumentoResponse_QNAME, DatiDocumentoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Codifica }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dma.csi.it/", name = "listaProfili")
    public JAXBElement<Codifica> createListaProfili(Codifica value) {
        return new JAXBElement<Codifica>(_ListaProfili_QNAME, Codifica.class, null, value);
    }

}
