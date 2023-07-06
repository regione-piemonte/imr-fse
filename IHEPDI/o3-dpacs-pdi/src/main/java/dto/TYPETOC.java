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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per TYPE_TOC complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="TYPE_TOC">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="URI_FILE" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LINK_STUDY_FOR_TOC" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="URI" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="STUDY_DESCRIPTION" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TYPE_TOC", propOrder = {
    "urifile",
    "linkstudyfortoc"
})
public class TYPETOC {

    @XmlElement(name = "URI_FILE", required = true)
    protected String urifile;
    @XmlElement(name = "LINK_STUDY_FOR_TOC", required = true)
    protected List<TYPETOC.LINKSTUDYFORTOC> linkstudyfortoc;

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
     * Gets the value of the linkstudyfortoc property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkstudyfortoc property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLINKSTUDYFORTOC().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TYPETOC.LINKSTUDYFORTOC }
     * 
     * 
     */
    public List<TYPETOC.LINKSTUDYFORTOC> getLINKSTUDYFORTOC() {
        if (linkstudyfortoc == null) {
            linkstudyfortoc = new ArrayList<TYPETOC.LINKSTUDYFORTOC>();
        }
        return this.linkstudyfortoc;
    }


    /**
     * <p>Classe Java per anonymous complex type.
     * 
     * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="URI" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="STUDY_DESCRIPTION" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "uri",
        "studydescription"
    })
    public static class LINKSTUDYFORTOC {

        @XmlElement(name = "URI", required = true)
        protected String uri;
        @XmlElement(name = "STUDY_DESCRIPTION", required = true)
        protected String studydescription;

        /**
         * Recupera il valore della proprietà uri.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getURI() {
            return uri;
        }

        /**
         * Imposta il valore della proprietà uri.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setURI(String value) {
            this.uri = value;
        }

        /**
         * Recupera il valore della proprietà studydescription.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSTUDYDESCRIPTION() {
            return studydescription;
        }

        /**
         * Imposta il valore della proprietà studydescription.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSTUDYDESCRIPTION(String value) {
            this.studydescription = value;
        }

    }

}
