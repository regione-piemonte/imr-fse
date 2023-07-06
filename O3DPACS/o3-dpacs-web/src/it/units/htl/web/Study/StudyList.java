/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.users.JSFUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;

/**
 * 
 * @author Sara
 * 
 */

public class StudyList {

	private String firstName;
	private String lastName;
	private Date birthDate;
	private String patientId;
	private String selectedStudy;//
	private HashMap<String, String> objectToStudy;//

	private int pagina = 1;

	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	private Date studyDate;

	private String accessNumber;
	private String modalitiesInStudy;
	private String studyDescription;
	private String studyId;
	private String studyInstanceUID;
	public ArrayList<StudyListItem> studiesMatched = new ArrayList<StudyListItem>();

	public StudyList() {
		objectToStudy = new HashMap<String, String>();

	}

	public void selectStudy(ValueChangeEvent event) {
		String s = (String) event.getNewValue();
		selectedStudy = objectToStudy.get(s);
		JSFUtil.storeOnSession(FacesContext.getCurrentInstance(), "selectedStudy", selectedStudy);
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}

	public String getStudyInstanceUID() {
		return studyInstanceUID;
	}

	// Patient's Details
	public void setFirstName(String _firstName) {
		this.firstName = _firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setLastName(String _lastName) {
		this.lastName = _lastName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setBirthDate(Date _birthDate) {
		this.birthDate = _birthDate;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setPatientId(String _patientId) {
		this.patientId = _patientId;
	}

	public String getPatientId() {
		return patientId;
	}

	// Study's Details
	//    public void setStudyDate(String _studyDate) {
	//        this.studyDate = _studyDate;
	//    }
	//    public String getStudyDate() {
	//        return studyDate;
	//    }

	public Date getStudyDate() {
		return studyDate;
	}

	public void setStudyDate(Date selectedDate) {
		this.studyDate = selectedDate;
	}

	public void setAccessNumber(String _accessNumber) {
		this.accessNumber = _accessNumber;
	}

	public String getAccessNumber() {
		return accessNumber;
	}

	public void setModalitiesInStudy(String _modalitiesInStudy) {
		this.modalitiesInStudy = _modalitiesInStudy;
	}

	public String getModalitiesInStudy() {
		return modalitiesInStudy;
	}

	public void setStudyDescription(String _studyDescription) {
		this.studyDescription = _studyDescription;
	}

	public String getStudyDescription() {
		return studyDescription;
	}

	public void setStudyId(String _studyId) {
		this.studyId = _studyId;
	}

	public String getStudyId() {
		return studyId;
	}

	public ArrayList<StudyListItem> getStudiesMatched() {
		return studiesMatched;
	}

	public void setStudiesMatched(ArrayList<StudyListItem> arrayList) {
		this.studiesMatched = arrayList;
	}

	public void findStudies() {
		studiesMatched.clear();
		String procFirstName = firstName;
		String procLastName = lastName;
		String regex = "^$|%|\\*"; // matches '' OR '*' OR '%'
		if (!firstName.matches(regex))
			procFirstName += "%";
		if (!lastName.matches(regex))
			procLastName += "%";
		StudyFinder.getStudies(procFirstName, procLastName, birthDate, patientId, studyId, accessNumber, modalitiesInStudy, studyDate, studyDescription, studyInstanceUID);
		for (int i = 0; i < studiesMatched.size(); i++) {//
			objectToStudy.put(studiesMatched.get(i).toString(), studiesMatched.get(i).getStudyInstanceUid());//
		}//
		/*
		 * firstName=""; lastName=""; birthDate=""; patientId="";
		 * studyDate=null; accessNumber=""; modalitiesInStudy="";
		 * studyDescription=""; studyId=""; studyInstanceUID="";
		 */
	}

	public void add(StudyListItem studyListItem) {
		studiesMatched.add(studyListItem);

	}

	/**
	 * This validation method throws a ValidatorException if it founds one or
	 * more occurrence of any of the special characters '*' or '%'
	 * 
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validateWildcardsFreeFields(FacesContext context, UIComponent component, Object object) {
		String value = (String) object;
		if (value.matches(".*\\*+.*|.*%+.*")) {
			MessageManager mm = new MessageManager();
			String localized = mm.getLocalizedMessage("wildcardsNotAllowed", null);
			FacesMessage message = new FacesMessage(localized);
			context.addMessage(null, message);
			throw new ValidatorException(message);

		}
	}

}
