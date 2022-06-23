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
 * <p>Classe Java per informativaIN complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="informativaIN"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="identificativoInformativa" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="identificativoOrganizzazione" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="identificativoUtente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="opzioniRequest" type="{http://dma.csi.it/}opzioniType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="pinCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ruoloUtente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="strutturaUtente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="tipoAttivita" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "informativaIN", propOrder = {
    "identificativoInformativa",
    "identificativoOrganizzazione",
    "identificativoUtente",
    "opzioniRequest",
    "pinCode",
    "ruoloUtente",
    "strutturaUtente",
    "tipoAttivita"
})
public class InformativaIN {

    protected String identificativoInformativa;
    protected String identificativoOrganizzazione;
    protected String identificativoUtente;
    @XmlElement(nillable = true)
    protected List<OpzioniType> opzioniRequest;
    protected String pinCode;
    protected String ruoloUtente;
    protected String strutturaUtente;
    protected String tipoAttivita;

    /**
     * Recupera il valore della proprietà identificativoInformativa.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoInformativa() {
        return identificativoInformativa;
    }

    /**
     * Imposta il valore della proprietà identificativoInformativa.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoInformativa(String value) {
        this.identificativoInformativa = value;
    }

    /**
     * Recupera il valore della proprietà identificativoOrganizzazione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoOrganizzazione() {
        return identificativoOrganizzazione;
    }

    /**
     * Imposta il valore della proprietà identificativoOrganizzazione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoOrganizzazione(String value) {
        this.identificativoOrganizzazione = value;
    }

    /**
     * Recupera il valore della proprietà identificativoUtente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoUtente() {
        return identificativoUtente;
    }

    /**
     * Imposta il valore della proprietà identificativoUtente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoUtente(String value) {
        this.identificativoUtente = value;
    }

    /**
     * Gets the value of the opzioniRequest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the opzioniRequest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOpzioniRequest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OpzioniType }
     * 
     * 
     */
    public List<OpzioniType> getOpzioniRequest() {
        if (opzioniRequest == null) {
            opzioniRequest = new ArrayList<OpzioniType>();
        }
        return this.opzioniRequest;
    }

    /**
     * Recupera il valore della proprietà pinCode.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPinCode() {
        return pinCode;
    }

    /**
     * Imposta il valore della proprietà pinCode.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPinCode(String value) {
        this.pinCode = value;
    }

    /**
     * Recupera il valore della proprietà ruoloUtente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRuoloUtente() {
        return ruoloUtente;
    }

    /**
     * Imposta il valore della proprietà ruoloUtente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRuoloUtente(String value) {
        this.ruoloUtente = value;
    }

    /**
     * Recupera il valore della proprietà strutturaUtente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrutturaUtente() {
        return strutturaUtente;
    }

    /**
     * Imposta il valore della proprietà strutturaUtente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrutturaUtente(String value) {
        this.strutturaUtente = value;
    }

    /**
     * Recupera il valore della proprietà tipoAttivita.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTipoAttivita() {
        return tipoAttivita;
    }

    /**
     * Imposta il valore della proprietà tipoAttivita.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTipoAttivita(String value) {
        this.tipoAttivita = value;
    }

}
