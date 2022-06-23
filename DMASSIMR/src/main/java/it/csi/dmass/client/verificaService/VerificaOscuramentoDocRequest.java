/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.verificaService;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per VerificaOscuramentoDocRequest complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="VerificaOscuramentoDocRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="richiedente" type="{http://dma.csi.it/}richiedenteInfo" minOccurs="0"/&gt;
 *         &lt;element name="datiDocumento" type="{http://dma.csi.it/}datiDocumento" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VerificaOscuramentoDocRequest", propOrder = {
    "richiedente",
    "datiDocumento"
})
public class VerificaOscuramentoDocRequest {

    protected RichiedenteInfo richiedente;
    protected DatiDocumento datiDocumento;

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
     * Recupera il valore della proprietà datiDocumento.
     * 
     * @return
     *     possible object is
     *     {@link DatiDocumento }
     *     
     */
    public DatiDocumento getDatiDocumento() {
        return datiDocumento;
    }

    /**
     * Imposta il valore della proprietà datiDocumento.
     * 
     * @param value
     *     allowed object is
     *     {@link DatiDocumento }
     *     
     */
    public void setDatiDocumento(DatiDocumento value) {
        this.datiDocumento = value;
    }

}
