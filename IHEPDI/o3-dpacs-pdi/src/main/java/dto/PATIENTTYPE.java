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
 * <p>Classe Java per PATIENT_TYPE complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="PATIENT_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PATIENT_ID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PATIENT_FIRST_NAME" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PATIENT_MIDDLE_NAME" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PATIENT_LAST_NAME" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PATIENT_BIRTH_DAY" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="STUDY_DESCRIPTION" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ACCESSION_NUMBER" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SERIES_DESC" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="URI_IMG" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PATIENT_TYPE", propOrder = {
    "patientid",
    "patientfirstname",
    "patientmiddlename",
    "patientlastname",
    "patientbirthday",
    "studydescription",
    "accessionnumber",
    "seriesdesc",
    "uriimg"
})
public class PATIENTTYPE {

    @XmlElement(name = "PATIENT_ID", required = true)
    protected String patientid;
    @XmlElement(name = "PATIENT_FIRST_NAME", required = true)
    protected String patientfirstname;
    @XmlElement(name = "PATIENT_MIDDLE_NAME", required = true)
    protected String patientmiddlename;
    @XmlElement(name = "PATIENT_LAST_NAME", required = true)
    protected String patientlastname;
    @XmlElement(name = "PATIENT_BIRTH_DAY", required = true)
    protected String patientbirthday;
    @XmlElement(name = "STUDY_DESCRIPTION", required = true)
    protected String studydescription;
    @XmlElement(name = "ACCESSION_NUMBER", required = true)
    protected String accessionnumber;
    @XmlElement(name = "SERIES_DESC", required = true)
    protected String seriesdesc;
    @XmlElement(name = "URI_IMG", required = true)
    protected String uriimg;

    /**
     * Recupera il valore della proprietà patientid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPATIENTID() {
        return patientid;
    }

    /**
     * Imposta il valore della proprietà patientid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPATIENTID(String value) {
        this.patientid = value;
    }

    /**
     * Recupera il valore della proprietà patientfirstname.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPATIENTFIRSTNAME() {
        return patientfirstname;
    }

    /**
     * Imposta il valore della proprietà patientfirstname.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPATIENTFIRSTNAME(String value) {
        this.patientfirstname = value;
    }

    /**
     * Recupera il valore della proprietà patientmiddlename.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPATIENTMIDDLENAME() {
        return patientmiddlename;
    }

    /**
     * Imposta il valore della proprietà patientmiddlename.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPATIENTMIDDLENAME(String value) {
        this.patientmiddlename = value;
    }

    /**
     * Recupera il valore della proprietà patientlastname.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPATIENTLASTNAME() {
        return patientlastname;
    }

    /**
     * Imposta il valore della proprietà patientlastname.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPATIENTLASTNAME(String value) {
        this.patientlastname = value;
    }

    /**
     * Recupera il valore della proprietà patientbirthday.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPATIENTBIRTHDAY() {
        return patientbirthday;
    }

    /**
     * Imposta il valore della proprietà patientbirthday.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPATIENTBIRTHDAY(String value) {
        this.patientbirthday = value;
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
     * Recupera il valore della proprietà accessionnumber.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getACCESSIONNUMBER() {
        return accessionnumber;
    }

    /**
     * Imposta il valore della proprietà accessionnumber.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setACCESSIONNUMBER(String value) {
        this.accessionnumber = value;
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

}
