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
 * <p>Classe Java per VerificaStatoRichiesta2Response complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="VerificaStatoRichiesta2Response"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://dma.csi.it/}serviceResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="statoRichiesta" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="zipName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="directory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="checksum" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="arvchivioDocumentoIlec" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="codDocumentoDipartimentale" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="idDocumentoIlec" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VerificaStatoRichiesta2Response", propOrder = {
    "statoRichiesta",
    "zipName",
    "directory",
    "checksum",
    "arvchivioDocumentoIlec",
    "codDocumentoDipartimentale",
    "idDocumentoIlec"
})
public class VerificaStatoRichiesta2Response
    extends ServiceResponse
{

    protected String statoRichiesta;
    protected String zipName;
    protected String directory;
    protected String checksum;
    protected String arvchivioDocumentoIlec;
    protected String codDocumentoDipartimentale;
    protected Long idDocumentoIlec;

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
     * Recupera il valore della proprietà checksum.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Imposta il valore della proprietà checksum.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChecksum(String value) {
        this.checksum = value;
    }

    /**
     * Recupera il valore della proprietà arvchivioDocumentoIlec.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArvchivioDocumentoIlec() {
        return arvchivioDocumentoIlec;
    }

    /**
     * Imposta il valore della proprietà arvchivioDocumentoIlec.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArvchivioDocumentoIlec(String value) {
        this.arvchivioDocumentoIlec = value;
    }

    /**
     * Recupera il valore della proprietà codDocumentoDipartimentale.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodDocumentoDipartimentale() {
        return codDocumentoDipartimentale;
    }

    /**
     * Imposta il valore della proprietà codDocumentoDipartimentale.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodDocumentoDipartimentale(String value) {
        this.codDocumentoDipartimentale = value;
    }

    /**
     * Recupera il valore della proprietà idDocumentoIlec.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getIdDocumentoIlec() {
        return idDocumentoIlec;
    }

    /**
     * Imposta il valore della proprietà idDocumentoIlec.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIdDocumentoIlec(Long value) {
        this.idDocumentoIlec = value;
    }

}
