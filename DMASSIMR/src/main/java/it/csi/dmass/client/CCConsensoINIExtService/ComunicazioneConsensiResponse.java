/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.CCConsensoINIExtService;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per comunicazioneConsensiResponse complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="comunicazioneConsensiResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://dmacc.csi.it/}serviceResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="opzioniResponse" type="{http://dma.csi.it/}opzioniType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="regioneAssistenza" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "comunicazioneConsensiResponse", namespace = "http://dmacc.csi.it/", propOrder = {
    "opzioniResponse",
    "regioneAssistenza"
})
public class ComunicazioneConsensiResponse
    extends ServiceResponse
{

    @XmlElement(namespace = "", nillable = true)
    protected List<OpzioniType> opzioniResponse;
    @XmlElement(namespace = "")
    protected String regioneAssistenza;

    /**
     * Gets the value of the opzioniResponse property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the opzioniResponse property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOpzioniResponse().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpzioniType }
     * 
     * 
     */
    public List<OpzioniType> getOpzioniResponse() {
        if (opzioniResponse == null) {
            opzioniResponse = new ArrayList<OpzioniType>();
        }
        return this.opzioniResponse;
    }

    /**
     * Recupera il valore della proprietà regioneAssistenza.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegioneAssistenza() {
        return regioneAssistenza;
    }

    /**
     * Imposta il valore della proprietà regioneAssistenza.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegioneAssistenza(String value) {
        this.regioneAssistenza = value;
    }

}
