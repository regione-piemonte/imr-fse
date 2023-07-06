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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.gson.Gson;


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
 *         &lt;element name="CSS" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="STYLE" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="JS" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="JAVASCRIPT" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="INDEX" type="{}INDEX_TYPE"/>
 *         &lt;element name="HOME" type="{}HOME_TYPE"/>
 *         &lt;element name="SELECT" type="{}SELECT_TYPE"/>
 *         &lt;element name="TOC" type="{}TYPE_TOC"/>
 *         &lt;element name="STUDIES">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="STUDY" type="{}STUDY_TYPE" maxOccurs="unbounded"/>
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
@XmlType(name = "", propOrder = {
    "css",
    "js",
    "index",
    "home",
    "select",
    "toc",
    "studies"
})
@XmlRootElement(name = "DICOM_VIEWER_DATA")
public class DICOMVIEWERDATA {

    @XmlElement(name = "CSS", required = false)
    protected List<DICOMVIEWERDATA.CSS> css;
    @XmlElement(name = "JS", required = true)
    protected List<DICOMVIEWERDATA.JS> js;
    @XmlElement(name = "INDEX", required = true)
    protected INDEXTYPE index;
    @XmlElement(name = "HOME", required = true)
    protected HOMETYPE home;
    @XmlElement(name = "SELECT", required = true)
    protected SELECTTYPE select;
    @XmlElement(name = "TOC", required = true)
    protected TYPETOC toc;
    @XmlElement(name = "STUDIES", required = true)
    protected DICOMVIEWERDATA.STUDIES studies;

    /**
     * Gets the value of the css property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the css property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCSS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DICOMVIEWERDATA.CSS }
     * 
     * 
     */
    public List<DICOMVIEWERDATA.CSS> getCSS() {
        if (css == null) {
            css = new ArrayList<DICOMVIEWERDATA.CSS>();
        }
        return this.css;
    }

    /**
     * Gets the value of the js property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the js property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DICOMVIEWERDATA.JS }
     * 
     * 
     */
    public List<DICOMVIEWERDATA.JS> getJS() {
        if (js == null) {
            js = new ArrayList<DICOMVIEWERDATA.JS>();
        }
        return this.js;
    }

    /**
     * Recupera il valore della proprietà index.
     * 
     * @return
     *     possible object is
     *     {@link INDEXTYPE }
     *     
     */
    public INDEXTYPE getINDEX() {
        return index;
    }

    /**
     * Imposta il valore della proprietà index.
     * 
     * @param value
     *     allowed object is
     *     {@link INDEXTYPE }
     *     
     */
    public void setINDEX(INDEXTYPE value) {
        this.index = value;
    }

    /**
     * Recupera il valore della proprietà home.
     * 
     * @return
     *     possible object is
     *     {@link HOMETYPE }
     *     
     */
    public HOMETYPE getHOME() {
        return home;
    }

    /**
     * Imposta il valore della proprietà home.
     * 
     * @param value
     *     allowed object is
     *     {@link HOMETYPE }
     *     
     */
    public void setHOME(HOMETYPE value) {
        this.home = value;
    }

    /**
     * Recupera il valore della proprietà select.
     * 
     * @return
     *     possible object is
     *     {@link SELECTTYPE }
     *     
     */
    public SELECTTYPE getSELECT() {
        return select;
    }

    /**
     * Imposta il valore della proprietà select.
     * 
     * @param value
     *     allowed object is
     *     {@link SELECTTYPE }
     *     
     */
    public void setSELECT(SELECTTYPE value) {
        this.select = value;
    }

    /**
     * Recupera il valore della proprietà toc.
     * 
     * @return
     *     possible object is
     *     {@link TYPETOC }
     *     
     */
    public TYPETOC getTOC() {
        return toc;
    }

    /**
     * Imposta il valore della proprietà toc.
     * 
     * @param value
     *     allowed object is
     *     {@link TYPETOC }
     *     
     */
    public void setTOC(TYPETOC value) {
        this.toc = value;
    }

    /**
     * Recupera il valore della proprietà studies.
     * 
     * @return
     *     possible object is
     *     {@link DICOMVIEWERDATA.STUDIES }
     *     
     */
    public DICOMVIEWERDATA.STUDIES getSTUDIES() {
        return studies;
    }

    /**
     * Imposta il valore della proprietà studies.
     * 
     * @param value
     *     allowed object is
     *     {@link DICOMVIEWERDATA.STUDIES }
     *     
     */
    public void setSTUDIES(DICOMVIEWERDATA.STUDIES value) {
        this.studies = value;
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
     *         &lt;element name="STYLE" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "style"
    })
    public static class CSS {

        @XmlElement(name = "STYLE", required = true)
        protected String style;

        /**
         * Recupera il valore della proprietà style.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSTYLE() {
            return style;
        }

        /**
         * Imposta il valore della proprietà style.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSTYLE(String value) {
            this.style = value;
        }

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
     *         &lt;element name="JAVASCRIPT" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "javascript"
    })
    public static class JS {

        @XmlElement(name = "JAVASCRIPT", required = true)
        protected String javascript;

        /**
         * Recupera il valore della proprietà javascript.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getJAVASCRIPT() {
            return javascript;
        }

        /**
         * Imposta il valore della proprietà javascript.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setJAVASCRIPT(String value) {
            this.javascript = value;
        }

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
     *         &lt;element name="STUDY" type="{}STUDY_TYPE" maxOccurs="unbounded"/>
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
        "study"
    })
    public static class STUDIES {

        @XmlElement(name = "STUDY", required = true)
        protected List<STUDYTYPE> study;

        /**
         * Gets the value of the study property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the study property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSTUDY().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link STUDYTYPE }
         * 
         * 
         */
        public List<STUDYTYPE> getSTUDY() {
            if (study == null) {
                study = new ArrayList<STUDYTYPE>();
            }
            return this.study;
        }

    }
    
    @Override
    public String toString() {
    	return new Gson().toJson(this).toString();
    }

}
