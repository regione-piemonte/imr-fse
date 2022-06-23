/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.delegaService.dmacc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import it.csi.dmass.client.delegaService.dma.Deleganti;
import it.csi.dmass.client.delegaService.dma.ServiceResponse;


/**
 * <p>Classe Java per getDeleganti2Response complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="getDeleganti2Response"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://dma.csi.it/}serviceResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="deleganti" type="{http://dma.csi.it/}deleganti" minOccurs="0" form="unqualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getDeleganti2Response", propOrder = {
    "deleganti"
})
public class GetDeleganti2Response
    extends ServiceResponse
{

    protected Deleganti deleganti;

    /**
     * Recupera il valore della proprietà deleganti.
     * 
     * @return
     *     possible object is
     *     {@link Deleganti }
     *     
     */
    public Deleganti getDeleganti() {
        return deleganti;
    }

    /**
     * Imposta il valore della proprietà deleganti.
     * 
     * @param value
     *     allowed object is
     *     {@link Deleganti }
     *     
     */
    public void setDeleganti(Deleganti value) {
        this.deleganti = value;
    }

}
