/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.dmassbatchcancpacc.client.ScaricoStudiWSBean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per SetPacchettoCancellatoRequest complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="SetPacchettoCancellatoRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idRichiestaScarico" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SetPacchettoCancellatoRequest", propOrder = {
    "idRichiestaScarico"
})
public class SetPacchettoCancellatoRequest {

    protected String idRichiestaScarico;

    /**
     * Recupera il valore della proprietà idRichiestaScarico.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdRichiestaScarico() {
        return idRichiestaScarico;
    }

    /**
     * Imposta il valore della proprietà idRichiestaScarico.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdRichiestaScarico(String value) {
        this.idRichiestaScarico = value;
    }

}
