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
 * <p>Classe Java per componenteLocaleServizio complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="componenteLocaleServizio"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://dma.csi.it/}codifica"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="urlServizio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="operazioni" type="{http://dma.csi.it/}componenteLocaleOperazione" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "componenteLocaleServizio", propOrder = {
    "urlServizio",
    "operazioni"
})
public class ComponenteLocaleServizio
    extends Codifica
{

    protected String urlServizio;
    @XmlElement(nillable = true)
    protected List<ComponenteLocaleOperazione> operazioni;

    /**
     * Recupera il valore della proprietà urlServizio.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrlServizio() {
        return urlServizio;
    }

    /**
     * Imposta il valore della proprietà urlServizio.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrlServizio(String value) {
        this.urlServizio = value;
    }

    /**
     * Gets the value of the operazioni property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the operazioni property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOperazioni().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ComponenteLocaleOperazione }
     * 
     * 
     */
    public List<ComponenteLocaleOperazione> getOperazioni() {
        if (operazioni == null) {
            operazioni = new ArrayList<ComponenteLocaleOperazione>();
        }
        return this.operazioni;
    }

}
