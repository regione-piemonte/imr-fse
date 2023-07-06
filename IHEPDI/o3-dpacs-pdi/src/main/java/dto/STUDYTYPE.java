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
 * <p>Classe Java per STUDY_TYPE complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="STUDY_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="URI_FILE" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="STUDY_DESCRIPTION" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LIST_SERIES">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="SERIES" type="{}SERIES_TYPE" maxOccurs="unbounded"/>
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
@XmlType(name = "STUDY_TYPE", propOrder = {
    "urifile",
    "studydescription",
    "listseries"
})
public class STUDYTYPE {

    @XmlElement(name = "URI_FILE", required = true)
    protected String urifile;
    @XmlElement(name = "STUDY_DESCRIPTION", required = true)
    protected String studydescription;
    @XmlElement(name = "LIST_SERIES", required = true)
    protected STUDYTYPE.LISTSERIES listseries;

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

    /**
     * Recupera il valore della proprietà listseries.
     * 
     * @return
     *     possible object is
     *     {@link STUDYTYPE.LISTSERIES }
     *     
     */
    public STUDYTYPE.LISTSERIES getLISTSERIES() {
        return listseries;
    }

    /**
     * Imposta il valore della proprietà listseries.
     * 
     * @param value
     *     allowed object is
     *     {@link STUDYTYPE.LISTSERIES }
     *     
     */
    public void setLISTSERIES(STUDYTYPE.LISTSERIES value) {
        this.listseries = value;
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
     *         &lt;element name="SERIES" type="{}SERIES_TYPE" maxOccurs="unbounded"/>
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
        "series"
    })
    public static class LISTSERIES {

        @XmlElement(name = "SERIES", required = true)
        protected List<SERIESTYPE> series;

        /**
         * Gets the value of the series property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the series property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSERIES().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SERIESTYPE }
         * 
         * 
         */
        public List<SERIESTYPE> getSERIES() {
            if (series == null) {
                series = new ArrayList<SERIESTYPE>();
            }
            return this.series;
        }

    }

}
