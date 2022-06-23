/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.delegaService.dmacc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import it.csi.dmass.client.delegaService.dma.GetDeleganti2IN;
import it.csi.dmass.client.delegaService.dma.RichiedenteInfo;


/**
 * <p>Classe Java per getDeleganti2Request complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="getDeleganti2Request"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="richiedente" type="{http://dma.csi.it/}richiedenteInfo" minOccurs="0" form="unqualified"/&gt;
 *         &lt;element name="getDeleganti2IN" type="{http://dma.csi.it/}getDeleganti2IN" minOccurs="0" form="unqualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getDeleganti2Request", propOrder = {
    "richiedente",
    "getDeleganti2IN"
})
public class GetDeleganti2Request {

    protected RichiedenteInfo richiedente;
    protected GetDeleganti2IN getDeleganti2IN;

    /**
     * Recupera il valore della proprietà richiedente.
     * 
     * @return
     *     possible object is
     *     {@link RichiedenteInfo }
     *     
     */
    public RichiedenteInfo getRichiedente() {
        return richiedente;
    }

    /**
     * Imposta il valore della proprietà richiedente.
     * 
     * @param value
     *     allowed object is
     *     {@link RichiedenteInfo }
     *     
     */
    public void setRichiedente(RichiedenteInfo value) {
        this.richiedente = value;
    }

    /**
     * Recupera il valore della proprietà getDeleganti2IN.
     * 
     * @return
     *     possible object is
     *     {@link GetDeleganti2IN }
     *     
     */
    public GetDeleganti2IN getGetDeleganti2IN() {
        return getDeleganti2IN;
    }

    /**
     * Imposta il valore della proprietà getDeleganti2IN.
     * 
     * @param value
     *     allowed object is
     *     {@link GetDeleganti2IN }
     *     
     */
    public void setGetDeleganti2IN(GetDeleganti2IN value) {
        this.getDeleganti2IN = value;
    }

}
