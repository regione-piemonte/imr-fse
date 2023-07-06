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
 * <p>Classe Java per OBJECT_TYPE complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="OBJECT_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="URI" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="URI_FILE" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="URI_IMG" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="OBJECT_NAME" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="OBJECT_INDEX" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="NUM_OBJECT" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="NEXT_OBJECT" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PREV_OBJECT" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="STUDY_NUMBER" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SERIES_NUMBER" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SERIES_DESC" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PATIENT" type="{}PATIENT_TYPE"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OBJECT_TYPE", propOrder = {
    "uri",
    "urifile",
    "uriimg",
    "objectname",
    "objectindex",
    "numobject",
    "nextobject",
    "prevobject",
    "studynumber",
    "seriesnumber",
    "seriesdesc",
    "phonometric",
    "spacing",
    "size",
    "numberFrames",
    "row",
    "column",
    "patient"
})
public class OBJECTTYPE {

    @XmlElement(name = "URI", required = true)
    protected String uri;
    @XmlElement(name = "URI_FILE", required = true)
    protected String urifile;
    @XmlElement(name = "URI_IMG", required = true)
    protected String uriimg;
    @XmlElement(name = "OBJECT_NAME", required = true)
    protected String objectname;
    @XmlElement(name = "OBJECT_INDEX", required = true)
    protected int objectindex;
    @XmlElement(name = "NUM_OBJECT", required = true)
    protected int numobject;
    @XmlElement(name = "NEXT_OBJECT", required = true)
    protected String nextobject;
    @XmlElement(name = "PREV_OBJECT", required = true)
    protected String prevobject;
    @XmlElement(name = "STUDY_NUMBER", required = true)
    protected String studynumber;
    @XmlElement(name = "SERIES_NUMBER", required = true)
    protected String seriesnumber;
    @XmlElement(name = "SERIES_DESC", required = true)
    protected String seriesdesc;
    @XmlElement(name = "PHONOMETRIC", required = true)
    protected String phonometric;
    @XmlElement(name = "SPACING", required = true)
    protected String spacing;
    @XmlElement(name = "SIZE", required = true)
    protected String size;
    @XmlElement(name = "NUMBER_FRAMES", required = true)
    protected Integer numberFrames;
    @XmlElement(name = "ROW", required = true)
    protected Integer row;
    @XmlElement(name = "COLUMN", required = true)
    protected Integer column;
    @XmlElement(name = "PATIENT", required = true)
    protected PATIENTTYPE patient;
    
    
    /**
     * Recupera il valore della proprietà uri.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Integer getCOLUMN() {
        return column;
    }

    /**
     * Imposta il valore della proprietà uri.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCOLUMN(Integer value) {
        this.column = value;
    }
    /**
     * Recupera il valore della proprietà uri.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Integer getROW() {
        return row;
    }

    /**
     * Imposta il valore della proprietà uri.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setROW(Integer value) {
        this.row = value;
    }
    /**
     * Recupera il valore della proprietà uri.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Integer getNUMBERFRAMES() {
        return numberFrames;
    }

    /**
     * Imposta il valore della proprietà uri.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNUMBERFRAMES(Integer value) {
        this.numberFrames = value;
    }
    /**
     * Recupera il valore della proprietà uri.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSIZE() {
        return size;
    }

    /**
     * Imposta il valore della proprietà uri.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSIZE(String value) {
        this.size = value;
    }

    /**
     * Recupera il valore della proprietà uri.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSPACING() {
        return spacing;
    }

    /**
     * Imposta il valore della proprietà uri.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSPACING(String value) {
        this.spacing = value;
    }
    
    /**
     * Recupera il valore della proprietà uri.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPHONOMETRIC() {
        return phonometric;
    }

    /**
     * Imposta il valore della proprietà uri.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPHONOMETRIC(String value) {
        this.phonometric = value;
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
     * Recupera il valore della proprietà uriimg.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getURIIMG() {
        return uriimg;
    }

    /**
     * Imposta il valore della proprietà uriimg.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setURIIMG(String value) {
        this.uriimg = value;
    }

    /**
     * Recupera il valore della proprietà objectname.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOBJECTNAME() {
        return objectname;
    }

    /**
     * Imposta il valore della proprietà objectname.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOBJECTNAME(String value) {
        this.objectname = value;
    }

    /**
     * Recupera il valore della proprietà objectindex.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public int getOBJECTINDEX() {
        return objectindex;
    }

    /**
     * Imposta il valore della proprietà objectindex.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOBJECTINDEX(int value) {
        this.objectindex = value;
    }

    /**
     * Recupera il valore della proprietà numobject.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public int getNUMOBJECT() {
        return numobject;
    }

    /**
     * Imposta il valore della proprietà numobject.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNUMOBJECT(int value) {
        this.numobject = value;
    }

    /**
     * Recupera il valore della proprietà nextobject.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNEXTOBJECT() {
        return nextobject;
    }

    /**
     * Imposta il valore della proprietà nextobject.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNEXTOBJECT(String value) {
        this.nextobject = value;
    }

    /**
     * Recupera il valore della proprietà prevobject.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPREVOBJECT() {
        return prevobject;
    }

    /**
     * Imposta il valore della proprietà prevobject.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPREVOBJECT(String value) {
        this.prevobject = value;
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
     * Recupera il valore della proprietà patient.
     * 
     * @return
     *     possible object is
     *     {@link PATIENTTYPE }
     *     
     */
    public PATIENTTYPE getPATIENT() {
        return patient;
    }

    /**
     * Imposta il valore della proprietà patient.
     * 
     * @param value
     *     allowed object is
     *     {@link PATIENTTYPE }
     *     
     */
    public void setPATIENT(PATIENTTYPE value) {
        this.patient = value;
    }

}
