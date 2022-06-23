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
 * <p>Classe Java per recuperoInformativaResponse complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="recuperoInformativaResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://dmacc.csi.it/}serviceResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="informativaOUT" type="{http://dma.csi.it/}informativaOUT" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "recuperoInformativaResponse", namespace = "http://dmacc.csi.it/", propOrder = {
    "informativaOUT"
})
public class RecuperoInformativaResponse
    extends ServiceResponse
{

    @XmlElement(namespace = "")
    protected InformativaOUT informativaOUT;

    /**
     * Recupera il valore della proprietà informativaOUT.
     * 
     * @return
     *     possible object is
     *     {@link InformativaOUT }
     *     
     */
    public InformativaOUT getInformativaOUT() {
        return informativaOUT;
    }

    /**
     * Imposta il valore della proprietà informativaOUT.
     * 
     * @param value
     *     allowed object is
     *     {@link InformativaOUT }
     *     
     */
    public void setInformativaOUT(InformativaOUT value) {
        this.informativaOUT = value;
    }

}
