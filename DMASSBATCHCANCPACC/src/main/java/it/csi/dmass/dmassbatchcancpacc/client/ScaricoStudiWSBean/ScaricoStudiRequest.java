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
 * <p>Classe Java per ScaricoStudiRequest complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="ScaricoStudiRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="codiceFiscale" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="email" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="fuoriRegione" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="idReferto" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="periodoConservazione" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="pin" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="strutturaSanitaria" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="sistemaOperativo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="acessionNumbers" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="asr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScaricoStudiRequest", propOrder = {
    "codiceFiscale",
    "email",
    "fuoriRegione",
    "idReferto",
    "periodoConservazione",
    "pin",
    "strutturaSanitaria",
    "sistemaOperativo",
    "acessionNumbers",
    "asr"
})
public class ScaricoStudiRequest {

    protected String codiceFiscale;
    protected String email;
    protected Boolean fuoriRegione;
    protected String idReferto;
    protected String periodoConservazione;
    protected String pin;
    protected String strutturaSanitaria;
    protected String sistemaOperativo;
    protected String acessionNumbers;
    protected String asr;

    /**
     * Recupera il valore della proprietà codiceFiscale.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    /**
     * Imposta il valore della proprietà codiceFiscale.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodiceFiscale(String value) {
        this.codiceFiscale = value;
    }

    /**
     * Recupera il valore della proprietà email.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Imposta il valore della proprietà email.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Recupera il valore della proprietà fuoriRegione.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFuoriRegione() {
        return fuoriRegione;
    }

    /**
     * Imposta il valore della proprietà fuoriRegione.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFuoriRegione(Boolean value) {
        this.fuoriRegione = value;
    }

    /**
     * Recupera il valore della proprietà idReferto.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdReferto() {
        return idReferto;
    }

    /**
     * Imposta il valore della proprietà idReferto.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdReferto(String value) {
        this.idReferto = value;
    }

    /**
     * Recupera il valore della proprietà periodoConservazione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPeriodoConservazione() {
        return periodoConservazione;
    }

    /**
     * Imposta il valore della proprietà periodoConservazione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPeriodoConservazione(String value) {
        this.periodoConservazione = value;
    }

    /**
     * Recupera il valore della proprietà pin.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPin() {
        return pin;
    }

    /**
     * Imposta il valore della proprietà pin.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPin(String value) {
        this.pin = value;
    }

    /**
     * Recupera il valore della proprietà strutturaSanitaria.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrutturaSanitaria() {
        return strutturaSanitaria;
    }

    /**
     * Imposta il valore della proprietà strutturaSanitaria.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrutturaSanitaria(String value) {
        this.strutturaSanitaria = value;
    }

    /**
     * Recupera il valore della proprietà sistemaOperativo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSistemaOperativo() {
        return sistemaOperativo;
    }

    /**
     * Imposta il valore della proprietà sistemaOperativo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSistemaOperativo(String value) {
        this.sistemaOperativo = value;
    }

    /**
     * Recupera il valore della proprietà acessionNumbers.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAcessionNumbers() {
        return acessionNumbers;
    }

    /**
     * Imposta il valore della proprietà acessionNumbers.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcessionNumbers(String value) {
        this.acessionNumbers = value;
    }

    /**
     * Recupera il valore della proprietà asr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAsr() {
        return asr;
    }

    /**
     * Imposta il valore della proprietà asr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAsr(String value) {
        this.asr = value;
    }

}
