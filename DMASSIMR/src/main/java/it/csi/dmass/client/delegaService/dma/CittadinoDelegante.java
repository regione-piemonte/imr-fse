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
 * <p>Classe Java per cittadinoDelegante complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="cittadinoDelegante"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://dma.csi.it/}cittadinoDelega"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idDelegante" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="unqualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cittadinoDelegante", propOrder = {
    "idDelegante"
})
public class CittadinoDelegante
    extends CittadinoDelega
{

    @XmlElement(namespace = "")
    protected String idDelegante;

    /**
     * Recupera il valore della proprietà idDelegante.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdDelegante() {
        return idDelegante;
    }

    /**
     * Imposta il valore della proprietà idDelegante.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdDelegante(String value) {
        this.idDelegante = value;
    }

}
