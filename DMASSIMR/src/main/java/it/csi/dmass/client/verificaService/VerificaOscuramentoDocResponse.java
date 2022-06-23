/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.verificaService;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per verificaOscuramentoDocResponse complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="verificaOscuramentoDocResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://dma.csi.it/}serviceResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://dma.csi.it/}datiDocumentoResponse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "verificaOscuramentoDocResponse", propOrder = {
    "datiDocumentoResponse"
})
public class VerificaOscuramentoDocResponse
    extends ServiceResponse
{

    @XmlElement(namespace = "http://dma.csi.it/")
    protected DatiDocumentoResponse datiDocumentoResponse;

    /**
     * Recupera il valore della proprietà datiDocumentoResponse.
     * 
     * @return
     *     possible object is
     *     {@link DatiDocumentoResponse }
     *     
     */
    public DatiDocumentoResponse getDatiDocumentoResponse() {
        return datiDocumentoResponse;
    }

    /**
     * Imposta il valore della proprietà datiDocumentoResponse.
     * 
     * @param value
     *     allowed object is
     *     {@link DatiDocumentoResponse }
     *     
     */
    public void setDatiDocumentoResponse(DatiDocumentoResponse value) {
        this.datiDocumentoResponse = value;
    }

}
