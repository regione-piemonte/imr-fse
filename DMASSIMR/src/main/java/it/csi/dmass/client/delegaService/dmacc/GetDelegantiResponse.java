/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.delegaService.dmacc;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import it.csi.dmass.client.delegaService.dma.Paziente;


/**
 * <p>Classe Java per getDelegantiResponse complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="getDelegantiResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://dmacc.csi.it/}serviceResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://dma.csi.it/}deleganti" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getDelegantiResponse", propOrder = {
    "deleganti"
})
public class GetDelegantiResponse
    extends ServiceResponse
{

    @XmlElement(namespace = "http://dma.csi.it/")
    protected List<Paziente> deleganti;

    /**
     * Gets the value of the deleganti property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deleganti property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeleganti().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Paziente }
     * 
     * 
     */
    public List<Paziente> getDeleganti() {
        if (deleganti == null) {
            deleganti = new ArrayList<Paziente>();
        }
        return this.deleganti;
    }

}
