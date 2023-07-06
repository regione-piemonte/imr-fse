/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package objects;

import java.io.Serializable;

import com.google.gson.Gson;

/**
 * Mantiene le informazioni per il paziente
 * necessarie alla generazione del contenuto PDI
 */
public class Patient implements Serializable {

	private static final long serialVersionUID = -8358071762100520488L;
	
	/**
	 * ID del paziente
	 */
	private String patientId = null;
	
	/**
	 * Cognome del paziente
	 */
    private String lastName = null;
    
    /**
     * Nome del paziente
     */
    private String firstName = null;
    
    /**
     * Secondo nome del paziente
     */
    private String middleName = null;
    
    /**
     * Data di nascita del paziente
     */
    private String birth = null;
    
    public Patient() {}
    
	/**
	 * Permette di impostare il cognome del paziente
	 * @param lastName il cognome del paziente
	 */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

	/**
	 * Permette di impostare il nome del paziente
	 * @param firstName il nome del paziente
	 */
    public void setFirstName(String firstName) {
    	this.firstName = firstName;
    }

	/**
	 * Permette di impostare il secondo nome del paziente
	 * @param middleName il secondo nome del paziente
	 */
    public void setMiddleName(String middleName) {
    	this.middleName = middleName;
    }

	/**
	 * Permette di impostare l'id del paziente
	 * @param patientId l'id del paziente
	 */
    public void setPatientId(String patientId) {
    	this.patientId = patientId;
    }
    
	/**
	 * Permette di impostare la data di nascita del paziente
	 * @param birth la data di nascita del paziente
	 */
	public void setBirth(String birth) {
		this.birth = birth;
	}

    public String getPatientId() {
        return patientId;
    }

	/**
	 * Fornisce il cognome del paziente
	 * @return il cognome del paziente
	 */
    public String getLastName() {
        return lastName;
    }

    /**
	 * Fornisce il nome del paziente
	 * @return il nome del paziente
	 */
    public String getFirstName() {
        return firstName;
    }

    /**
	 * Fornisce il secondo nome del paziente
	 * @return il secondo nome del paziente
	 */
    public String getMiddleName() {
        return middleName;
    }

    /**
	 * Fornisce la data di nascita del paziente
	 * @return la data di nascita del paziente
	 */
	public String getBirth() {
		return birth;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}