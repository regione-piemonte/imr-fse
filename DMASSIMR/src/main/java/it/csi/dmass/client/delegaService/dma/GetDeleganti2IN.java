/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.delegaService.dma;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per getDeleganti2IN complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="getDeleganti2IN"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cittadinoDelegante" type="{http://dma.csi.it/}cittadinoDelegante" minOccurs="0" form="unqualified"/&gt;
 *         &lt;element name="cittadinoDelegato" type="{http://dma.csi.it/}cittadinoDelegato" minOccurs="0" form="unqualified"/&gt;
 *         &lt;element name="statoDelega" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="unqualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getDeleganti2IN", propOrder = {
    "cittadinoDelegante",
    "cittadinoDelegato",
    "statoDelega"
})
public class GetDeleganti2IN {

    @XmlElement(namespace = "")
    protected CittadinoDelegante cittadinoDelegante;
    @XmlElement(namespace = "")
    protected CittadinoDelegato cittadinoDelegato;
    @XmlElement(namespace = "")
    protected String statoDelega;

    /**
     * Recupera il valore della proprietà cittadinoDelegante.
     * 
     * @return
     *     possible object is
     *     {@link CittadinoDelegante }
     *     
     */
    public CittadinoDelegante getCittadinoDelegante() {
        return cittadinoDelegante;
    }

    /**
     * Imposta il valore della proprietà cittadinoDelegante.
     * 
     * @param value
     *     allowed object is
     *     {@link CittadinoDelegante }
     *     
     */
    public void setCittadinoDelegante(CittadinoDelegante value) {
        this.cittadinoDelegante = value;
    }

    /**
     * Recupera il valore della proprietà cittadinoDelegato.
     * 
     * @return
     *     possible object is
     *     {@link CittadinoDelegato }
     *     
     */
    public CittadinoDelegato getCittadinoDelegato() {
        return cittadinoDelegato;
    }

    /**
     * Imposta il valore della proprietà cittadinoDelegato.
     * 
     * @param value
     *     allowed object is
     *     {@link CittadinoDelegato }
     *     
     */
    public void setCittadinoDelegato(CittadinoDelegato value) {
        this.cittadinoDelegato = value;
    }

    /**
     * Recupera il valore della proprietà statoDelega.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatoDelega() {
        return statoDelega;
    }

    /**
     * Imposta il valore della proprietà statoDelega.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatoDelega(String value) {
        this.statoDelega = value;
    }

}
