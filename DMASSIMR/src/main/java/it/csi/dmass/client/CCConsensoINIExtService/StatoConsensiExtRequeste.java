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
 * <p>Classe Java per statoConsensiExtRequeste complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="statoConsensiExtRequeste"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="paziente" type="{http://dma.csi.it/}paziente" minOccurs="0"/&gt;
 *         &lt;element name="richiedente" type="{http://dma.csi.it/}richiedenteExt" minOccurs="0"/&gt;
 *         &lt;element name="statoConsensiIN" type="{http://dma.csi.it/}statoConsensiIN" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "statoConsensiExtRequeste", namespace = "http://dmacc.csi.it/", propOrder = {
    "paziente",
    "richiedente",
    "statoConsensiIN"
})
public class StatoConsensiExtRequeste {

    @XmlElement(namespace = "")
    protected Paziente paziente;
    @XmlElement(namespace = "")
    protected RichiedenteExt richiedente;
    @XmlElement(namespace = "")
    protected StatoConsensiIN statoConsensiIN;

    /**
     * Recupera il valore della proprietà paziente.
     * 
     * @return
     *     possible object is
     *     {@link Paziente }
     *     
     */
    public Paziente getPaziente() {
        return paziente;
    }

    /**
     * Imposta il valore della proprietà paziente.
     * 
     * @param value
     *     allowed object is
     *     {@link Paziente }
     *     
     */
    public void setPaziente(Paziente value) {
        this.paziente = value;
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

    /**
     * Recupera il valore della proprietà statoConsensiIN.
     * 
     * @return
     *     possible object is
     *     {@link StatoConsensiIN }
     *     
     */
    public StatoConsensiIN getStatoConsensiIN() {
        return statoConsensiIN;
    }

    /**
     * Imposta il valore della proprietà statoConsensiIN.
     * 
     * @param value
     *     allowed object is
     *     {@link StatoConsensiIN }
     *     
     */
    public void setStatoConsensiIN(StatoConsensiIN value) {
        this.statoConsensiIN = value;
    }

}
