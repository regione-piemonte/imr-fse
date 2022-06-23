/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.delegaService.dmacc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import it.csi.dmass.client.delegaService.dma.GetDelegantiIN;
import it.csi.dmass.client.delegaService.dma.Paziente;
import it.csi.dmass.client.delegaService.dma.Richiedente;


/**
 * <p>Classe Java per getDelegantiRequest complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="getDelegantiRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://dma.csi.it/}richiedente" minOccurs="0"/&gt;
 *         &lt;element ref="{http://dma.csi.it/}cittadinoDelegato" minOccurs="0"/&gt;
 *         &lt;element ref="{http://dma.csi.it/}cittadinoDelegante" minOccurs="0"/&gt;
 *         &lt;element name="getDelegantiIN" type="{http://dma.csi.it/}getDelegantiIN" minOccurs="0" form="unqualified"/&gt;
 *         &lt;element name="idWso2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="unqualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getDelegantiRequest", propOrder = {
    "richiedente",
    "cittadinoDelegato",
    "cittadinoDelegante",
    "getDelegantiIN",
    "idWso2"
})
public class GetDelegantiRequest {

    @XmlElement(namespace = "http://dma.csi.it/")
    protected Richiedente richiedente;
    @XmlElement(namespace = "http://dma.csi.it/")
    protected Paziente cittadinoDelegato;
    @XmlElement(namespace = "http://dma.csi.it/")
    protected Paziente cittadinoDelegante;
    protected GetDelegantiIN getDelegantiIN;
    protected String idWso2;

    /**
     * Recupera il valore della proprietà richiedente.
     * 
     * @return
     *     possible object is
     *     {@link Richiedente }
     *     
     */
    public Richiedente getRichiedente() {
        return richiedente;
    }

    /**
     * Imposta il valore della proprietà richiedente.
     * 
     * @param value
     *     allowed object is
     *     {@link Richiedente }
     *     
     */
    public void setRichiedente(Richiedente value) {
        this.richiedente = value;
    }

    /**
     * Recupera il valore della proprietà cittadinoDelegato.
     * 
     * @return
     *     possible object is
     *     {@link Paziente }
     *     
     */
    public Paziente getCittadinoDelegato() {
        return cittadinoDelegato;
    }

    /**
     * Imposta il valore della proprietà cittadinoDelegato.
     * 
     * @param value
     *     allowed object is
     *     {@link Paziente }
     *     
     */
    public void setCittadinoDelegato(Paziente value) {
        this.cittadinoDelegato = value;
    }

    /**
     * Recupera il valore della proprietà cittadinoDelegante.
     * 
     * @return
     *     possible object is
     *     {@link Paziente }
     *     
     */
    public Paziente getCittadinoDelegante() {
        return cittadinoDelegante;
    }

    /**
     * Imposta il valore della proprietà cittadinoDelegante.
     * 
     * @param value
     *     allowed object is
     *     {@link Paziente }
     *     
     */
    public void setCittadinoDelegante(Paziente value) {
        this.cittadinoDelegante = value;
    }

    /**
     * Recupera il valore della proprietà getDelegantiIN.
     * 
     * @return
     *     possible object is
     *     {@link GetDelegantiIN }
     *     
     */
    public GetDelegantiIN getGetDelegantiIN() {
        return getDelegantiIN;
    }

    /**
     * Imposta il valore della proprietà getDelegantiIN.
     * 
     * @param value
     *     allowed object is
     *     {@link GetDelegantiIN }
     *     
     */
    public void setGetDelegantiIN(GetDelegantiIN value) {
        this.getDelegantiIN = value;
    }

    /**
     * Recupera il valore della proprietà idWso2.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdWso2() {
        return idWso2;
    }

    /**
     * Imposta il valore della proprietà idWso2.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdWso2(String value) {
        this.idWso2 = value;
    }

}
