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
 * <p>Classe Java per ElencoPacchetti complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="ElencoPacchetti"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idRichiestaScarico" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="directory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="zipName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ElencoPacchetti", propOrder = {
    "idRichiestaScarico",
    "directory",
    "zipName"
})
public class ElencoPacchetti {

    protected String idRichiestaScarico;
    protected String directory;
    protected String zipName;

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

    /**
     * Recupera il valore della proprietà directory.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Imposta il valore della proprietà directory.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirectory(String value) {
        this.directory = value;
    }

    /**
     * Recupera il valore della proprietà zipName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getZipName() {
        return zipName;
    }

    /**
     * Imposta il valore della proprietà zipName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setZipName(String value) {
        this.zipName = value;
    }

}
