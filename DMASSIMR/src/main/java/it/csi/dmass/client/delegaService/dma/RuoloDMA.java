/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.csi.dmass.client.delegaService.dma;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per ruoloDMA complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="ruoloDMA"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://dma.csi.it/}codifica"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="aziendaSanitaria" type="{http://dma.csi.it/}aziendaSanitaria" minOccurs="0"/&gt;
 *         &lt;element name="profilo" type="{http://dma.csi.it/}profilo" minOccurs="0"/&gt;
 *         &lt;element name="flagVisibilePerConsenso" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ruoloDMA", propOrder = {
    "aziendaSanitaria",
    "profilo",
    "flagVisibilePerConsenso"
})
public class RuoloDMA
    extends Codifica
{

    protected AziendaSanitaria aziendaSanitaria;
    protected Profilo profilo;
    protected String flagVisibilePerConsenso;

    /**
     * Recupera il valore della proprietà aziendaSanitaria.
     * 
     * @return
     *     possible object is
     *     {@link AziendaSanitaria }
     *     
     */
    public AziendaSanitaria getAziendaSanitaria() {
        return aziendaSanitaria;
    }

    /**
     * Imposta il valore della proprietà aziendaSanitaria.
     * 
     * @param value
     *     allowed object is
     *     {@link AziendaSanitaria }
     *     
     */
    public void setAziendaSanitaria(AziendaSanitaria value) {
        this.aziendaSanitaria = value;
    }

    /**
     * Recupera il valore della proprietà profilo.
     * 
     * @return
     *     possible object is
     *     {@link Profilo }
     *     
     */
    public Profilo getProfilo() {
        return profilo;
    }

    /**
     * Imposta il valore della proprietà profilo.
     * 
     * @param value
     *     allowed object is
     *     {@link Profilo }
     *     
     */
    public void setProfilo(Profilo value) {
        this.profilo = value;
    }

    /**
     * Recupera il valore della proprietà flagVisibilePerConsenso.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFlagVisibilePerConsenso() {
        return flagVisibilePerConsenso;
    }

    /**
     * Imposta il valore della proprietà flagVisibilePerConsenso.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFlagVisibilePerConsenso(String value) {
        this.flagVisibilePerConsenso = value;
    }

}
