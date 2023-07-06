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
 * <p>Classe Java per SERIES_TYPE complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="SERIES_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="URI" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="URI_FILE" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SERIES_NUMBER" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SERIES_DESC" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="STUDY_NUMBER" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LIST_OBJECT">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="OBJECT" type="{}OBJECT_TYPE" maxOccurs="unbounded"/>
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
@XmlType(name = "SERIES_TYPE", propOrder = {
    "uri",
    "urifile",
    "seriesnumber",
    "seriesdesc",
    "studynumber",
    "firstobject",
    "lastobject",
    "modality",
    "listobject"
})
public class SERIESTYPE {

    @XmlElement(name = "URI", required = true)
    protected String uri;
    @XmlElement(name = "URI_FILE", required = true)
    protected String urifile;
    @XmlElement(name = "SERIES_NUMBER", required = true)
    protected String seriesnumber;
    @XmlElement(name = "SERIES_DESC", required = true)
    protected String seriesdesc;
    @XmlElement(name = "STUDY_NUMBER", required = true)
    protected String studynumber;
    @XmlElement(name = "FIRST_OBJECT", required = true)
    protected String firstobject;
    @XmlElement(name = "LAST_OBJECT", required = true)
    protected String lastobject;
    @XmlElement(name = "MODALITY", required = true)
    protected String modality;
    @XmlElement(name = "LIST_OBJECT", required = true)
    protected SERIESTYPE.LISTOBJECT listobject;

    /**
     * Recupera il valore della proprietà studynumber.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFIRSTOBJECT() {
        return firstobject;
    }

    /**
     * Imposta il valore della proprietà studynumber.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLASTOBJECT(String value) {
        this.lastobject = value;
    }
    /**
     * Recupera il valore della proprietà studynumber.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLASTOBJECT() {
        return lastobject;
    }

    /**
     * Imposta il valore della proprietà studynumber.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFIRSTOBJECT(String value) {
        this.firstobject = value;
    }
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
     * Recupera il valore della proprietà seriesnumber.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSERIESNUMBER() {
        return seriesnumber;
    }

    /**
     * Imposta il valore della proprietà seriesnumber.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSERIESNUMBER(String value) {
        this.seriesnumber = value;
    }

    /**
     * Recupera il valore della proprietà seriesdesc.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSERIESDESC() {
        return seriesdesc;
    }

    /**
     * Imposta il valore della proprietà seriesdesc.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSERIESDESC(String value) {
        this.seriesdesc = value;
    }

    /**
     * Recupera il valore della proprietà studynumber.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSTUDYNUMBER() {
        return studynumber;
    }

    /**
     * Imposta il valore della proprietà studynumber.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSTUDYNUMBER(String value) {
        this.studynumber = value;
    }

    public String getMODALITY() {
		return modality;
	}

	public void setMODALITY(String modality) {
		this.modality = modality;
	}

	/**
     * Recupera il valore della proprietà listobject.
     * 
     * @return
     *     possible object is
     *     {@link SERIESTYPE.LISTOBJECT }
     *     
     */
    public SERIESTYPE.LISTOBJECT getLISTOBJECT() {
        return listobject;
    }

    /**
     * Imposta il valore della proprietà listobject.
     * 
     * @param value
     *     allowed object is
     *     {@link SERIESTYPE.LISTOBJECT }
     *     
     */
    public void setLISTOBJECT(SERIESTYPE.LISTOBJECT value) {
        this.listobject = value;
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
     *         &lt;element name="OBJECT" type="{}OBJECT_TYPE" maxOccurs="unbounded"/>
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
        "object"
    })
    public static class LISTOBJECT {

        @XmlElement(name = "OBJECT", required = true)
        protected List<OBJECTTYPE> object;

        /**
         * Gets the value of the object property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the object property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOBJECT().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link OBJECTTYPE }
         * 
         * 
         */
        public List<OBJECTTYPE> getOBJECT() {
            if (object == null) {
                object = new ArrayList<OBJECTTYPE>();
            }
            return this.object;
        }

    }

}
