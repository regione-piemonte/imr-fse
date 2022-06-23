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
 * <p>Classe Java per statoConsensiResponse complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="statoConsensiResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://dmacc.csi.it/}serviceResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="statoConsensiOUT" type="{http://dma.csi.it/}statoConsensiOUT" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "statoConsensiResponse", namespace = "http://dmacc.csi.it/", propOrder = {
    "statoConsensiOUT"
})
public class StatoConsensiResponse
    extends ServiceResponse
{

    @XmlElement(namespace = "")
    protected StatoConsensiOUT statoConsensiOUT;

    /**
     * Recupera il valore della proprietà statoConsensiOUT.
     * 
     * @return
     *     possible object is
     *     {@link StatoConsensiOUT }
     *     
     */
    public StatoConsensiOUT getStatoConsensiOUT() {
        return statoConsensiOUT;
    }

    /**
     * Imposta il valore della proprietà statoConsensiOUT.
     * 
     * @param value
     *     allowed object is
     *     {@link StatoConsensiOUT }
     *     
     */
    public void setStatoConsensiOUT(StatoConsensiOUT value) {
        this.statoConsensiOUT = value;
    }

}
