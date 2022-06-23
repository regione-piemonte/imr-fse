/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.CCConsensoINIExtService;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per recuperoInformativaExtRequeste complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="recuperoInformativaExtRequeste"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="informativaIN" type="{http://dma.csi.it/}informativaIN" minOccurs="0"/&gt;
 *         &lt;element name="richiedente" type="{http://dma.csi.it/}richiedenteExt" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "recuperoInformativaExtRequeste", namespace = "http://dmacc.csi.it/", propOrder = {
    "informativaIN",
    "richiedente"
})
public class RecuperoInformativaExtRequeste {

    @XmlElement(namespace = "")
    protected InformativaIN informativaIN;
    @XmlElement(namespace = "")
    protected RichiedenteExt richiedente;

    /**
     * Recupera il valore della proprietà informativaIN.
     * 
     * @return
     *     possible object is
     *     {@link InformativaIN }
     *     
     */
    public InformativaIN getInformativaIN() {
        return informativaIN;
    }

    /**
     * Imposta il valore della proprietà informativaIN.
     * 
     * @param value
     *     allowed object is
     *     {@link InformativaIN }
     *     
     */
    public void setInformativaIN(InformativaIN value) {
        this.informativaIN = value;
    }

    /**
     * Recupera il valore della proprietà richiedente.
     * 
     * @return
     *     possible object is
     *     {@link RichiedenteExt }
     *     
     */
    public RichiedenteExt getRichiedente() {
        return richiedente;
    }

    /**
     * Imposta il valore della proprietà richiedente.
     * 
     * @param value
     *     allowed object is
     *     {@link RichiedenteExt }
     *     
     */
    public void setRichiedente(RichiedenteExt value) {
        this.richiedente = value;
    }

}
