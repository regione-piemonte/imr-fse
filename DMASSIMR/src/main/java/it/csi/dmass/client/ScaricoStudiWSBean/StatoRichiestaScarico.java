/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.ScaricoStudiWSBean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per statoRichiestaScarico complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="statoRichiestaScarico"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idReferto" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="idPacchetto" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="statoRichiesta" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="dimensione" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "statoRichiestaScarico", propOrder = {
    "idReferto",
    "idPacchetto",
    "statoRichiesta",
    "dimensione"
})
public class StatoRichiestaScarico {

    protected String idReferto;
    protected String idPacchetto;
    protected String statoRichiesta;
    protected String dimensione;

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
     * Recupera il valore della proprietà idPacchetto.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdPacchetto() {
        return idPacchetto;
    }

    /**
     * Imposta il valore della proprietà idPacchetto.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdPacchetto(String value) {
        this.idPacchetto = value;
    }

    /**
     * Recupera il valore della proprietà statoRichiesta.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatoRichiesta() {
        return statoRichiesta;
    }

    /**
     * Imposta il valore della proprietà statoRichiesta.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatoRichiesta(String value) {
        this.statoRichiesta = value;
    }

    /**
     * Recupera il valore della proprietà dimensione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDimensione() {
        return dimensione;
    }

    /**
     * Imposta il valore della proprietà dimensione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDimensione(String value) {
        this.dimensione = value;
    }

}
