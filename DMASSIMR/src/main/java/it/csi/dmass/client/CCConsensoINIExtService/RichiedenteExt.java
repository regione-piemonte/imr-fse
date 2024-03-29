/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.CCConsensoINIExtService;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per richiedenteExt complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="richiedenteExt"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="applicazione" type="{http://dma.csi.it/}applicazioneRichiedente" minOccurs="0"/&gt;
 *         &lt;element name="codiceFiscale" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="gradoDelega" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="numeroTransazione" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="regime" type="{http://dma.csi.it/}regimeDMA" minOccurs="0"/&gt;
 *         &lt;element name="ruolo" type="{http://dma.csi.it/}ruoloDMA" minOccurs="0"/&gt;
 *         &lt;element name="tipoDelega" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="tokenOperazione" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "richiedenteExt", propOrder = {
    "applicazione",
    "codiceFiscale",
    "gradoDelega",
    "numeroTransazione",
    "regime",
    "ruolo",
    "tipoDelega",
    "tokenOperazione"
})
public class RichiedenteExt {

    protected ApplicazioneRichiedente applicazione;
    protected String codiceFiscale;
    protected String gradoDelega;
    protected String numeroTransazione;
    protected RegimeDMA regime;
    protected RuoloDMA ruolo;
    protected String tipoDelega;
    protected String tokenOperazione;

    /**
     * Recupera il valore della proprietà applicazione.
     * 
     * @return
     *     possible object is
     *     {@link ApplicazioneRichiedente }
     *     
     */
    public ApplicazioneRichiedente getApplicazione() {
        return applicazione;
    }

    /**
     * Imposta il valore della proprietà applicazione.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicazioneRichiedente }
     *     
     */
    public void setApplicazione(ApplicazioneRichiedente value) {
        this.applicazione = value;
    }

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
     * Recupera il valore della proprietà gradoDelega.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGradoDelega() {
        return gradoDelega;
    }

    /**
     * Imposta il valore della proprietà gradoDelega.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGradoDelega(String value) {
        this.gradoDelega = value;
    }

    /**
     * Recupera il valore della proprietà numeroTransazione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumeroTransazione() {
        return numeroTransazione;
    }

    /**
     * Imposta il valore della proprietà numeroTransazione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumeroTransazione(String value) {
        this.numeroTransazione = value;
    }

    /**
     * Recupera il valore della proprietà regime.
     * 
     * @return
     *     possible object is
     *     {@link RegimeDMA }
     *     
     */
    public RegimeDMA getRegime() {
        return regime;
    }

    /**
     * Imposta il valore della proprietà regime.
     * 
     * @param value
     *     allowed object is
     *     {@link RegimeDMA }
     *     
     */
    public void setRegime(RegimeDMA value) {
        this.regime = value;
    }

    /**
     * Recupera il valore della proprietà ruolo.
     * 
     * @return
     *     possible object is
     *     {@link RuoloDMA }
     *     
     */
    public RuoloDMA getRuolo() {
        return ruolo;
    }

    /**
     * Imposta il valore della proprietà ruolo.
     * 
     * @param value
     *     allowed object is
     *     {@link RuoloDMA }
     *     
     */
    public void setRuolo(RuoloDMA value) {
        this.ruolo = value;
    }

    /**
     * Recupera il valore della proprietà tipoDelega.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTipoDelega() {
        return tipoDelega;
    }

    /**
     * Imposta il valore della proprietà tipoDelega.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTipoDelega(String value) {
        this.tipoDelega = value;
    }

    /**
     * Recupera il valore della proprietà tokenOperazione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTokenOperazione() {
        return tokenOperazione;
    }

    /**
     * Imposta il valore della proprietà tokenOperazione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTokenOperazione(String value) {
        this.tokenOperazione = value;
    }

}
