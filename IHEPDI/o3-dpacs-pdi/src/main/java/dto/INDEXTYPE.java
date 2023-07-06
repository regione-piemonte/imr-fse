/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.8-b130911.1802 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2022.02.01 alle 11:50:45 AM CET 
//


package dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per INDEX_TYPE complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="INDEX_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="URI_FILE" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PATH_FIRST_STUDY" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PATH_FIRST_SERIES" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PATH_FIRST_IMG" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "INDEX_TYPE", propOrder = {
    "urifile",
    "pathfirststudy",
    "pathfirstseries",
    "pathfirstimg"
})
public class INDEXTYPE {

    @XmlElement(name = "URI_FILE", required = true)
    protected String urifile;
    @XmlElement(name = "PATH_FIRST_STUDY", required = true)
    protected String pathfirststudy;
    @XmlElement(name = "PATH_FIRST_SERIES", required = true)
    protected String pathfirstseries;
    @XmlElement(name = "PATH_FIRST_IMG", required = true)
    protected String pathfirstimg;

    /**
     * Recupera il valore della proprietà urifile.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getURIFILE() {
        return urifile;
    }

    /**
     * Imposta il valore della proprietà urifile.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setURIFILE(String value) {
        this.urifile = value;
    }

    /**
     * Recupera il valore della proprietà pathfirststudy.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPATHFIRSTSTUDY() {
        return pathfirststudy;
    }

    /**
     * Imposta il valore della proprietà pathfirststudy.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPATHFIRSTSTUDY(String value) {
        this.pathfirststudy = value;
    }

    /**
     * Recupera il valore della proprietà pathfirstseries.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPATHFIRSTSERIES() {
        return pathfirstseries;
    }

    /**
     * Imposta il valore della proprietà pathfirstseries.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPATHFIRSTSERIES(String value) {
        this.pathfirstseries = value;
    }

    /**
     * Recupera il valore della proprietà pathfirstimg.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPATHFIRSTIMG() {
        return pathfirstimg;
    }

    /**
     * Imposta il valore della proprietà pathfirstimg.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPATHFIRSTIMG(String value) {
        this.pathfirstimg = value;
    }

}
