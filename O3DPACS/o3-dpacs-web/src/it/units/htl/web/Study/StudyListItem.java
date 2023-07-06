/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import it.units.htl.dpacs.dao.UserManager;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.web.ui.messaging.MessageResource;

import java.sql.Time;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 
 * @author Sara
 * 
 */
public class StudyListItem {

	private static final String PREFIX_STUDYSTATUS = "ss_";
	
	private Date studyDate = null;
	private Time studyTime = null;
	private String[] modalitiesInStudy;
	private String studyDescription;
	private String firstName;
	private String lastName;
	private String studyInstanceUid;
	private String patientId;
	private String age;
	private String numberOfStudyRelatedInstances;
	private Date birthDate;
	private String studyStatus;
	private String accessionNumber;

	public Date getStudyDate() {
		return studyDate;
	}

	public void setStudyDate(Date _studyDate) {
		this.studyDate = _studyDate;
	}

	public Time getStudyTime() {
		return studyTime;
	}

	public void setStudyTime(Time _studyTime) {
		this.studyTime = _studyTime;
	}

	public String getModalitiesInStudy() {
		String modality = "";
		if (modalitiesInStudy != null) {
			for (int i = 0; i < modalitiesInStudy.length; i++) {
				modality = modality + modalitiesInStudy[i] + " ";
			}
		}
		return modality;
	}

	public void setModalitiesInStudy(String[] _modalitiesInStudy) {
		this.modalitiesInStudy = _modalitiesInStudy;
	}

	public String getStudyDescription() {
		if (studyDescription == null)
			studyDescription = "";
		return studyDescription;
	}

	public void setStudyDescription(String _studyDescription) {
		this.studyDescription = _studyDescription;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String _firstName) {
		this.firstName = _firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String _lastName) {
		this.lastName = _lastName;
	}

	public String getStudyInstanceUid() {
		return studyInstanceUid;
	}

	public void setStudyInstanceUid(String _studyInstanceUid) {
		this.studyInstanceUid = _studyInstanceUid;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String _patientId) {
		this.patientId = _patientId;
	}

	public String getAge() {
		return age;
	}

	public void setBirthDate(Date _birthDate) {
		try {
			this.birthDate = _birthDate;
			Calendar calendar = new GregorianCalendar();
			Calendar calendar2 = new GregorianCalendar();
			calendar2.setTimeInMillis(_birthDate.getTime());
			this.age = String.valueOf(calendar.get(Calendar.YEAR)
					- calendar2.get(Calendar.YEAR));
		} catch (Exception e) {
			this.age = "";
		}

	}

	public String getNumberOfStudyRelatedInstances() {
		return numberOfStudyRelatedInstances;
	}

	public void setNumberOfStudyRelatedInstances(
			String _numberOfStudyRelatedInstances) {
		this.numberOfStudyRelatedInstances = _numberOfStudyRelatedInstances;
	}

	public Date getBirthDate() { 
		return birthDate;
	}

	private String initWadoUrl(HttpSession s){
		HttpSession session = null;
		if(s==null){
			HttpServletRequest request = (HttpServletRequest) javax.faces.context.FacesContext.getCurrentInstance().getExternalContext().getRequest();
			session = request.getSession(false);
		}else{
			session=s;
		}
		
		String wUrl="";
		if(session.getAttribute(ConfigurationSettings.CONFIG_WADOURL)==null){
			try {
				wUrl=new UserManager().getConfigParam(ConfigurationSettings.CONFIG_WADOURL);
			} catch (NamingException nex) {
				wUrl="";
			}
			session.setAttribute(ConfigurationSettings.CONFIG_WADOURL, wUrl);
		}else{
			wUrl=(String)session.getAttribute(ConfigurationSettings.CONFIG_WADOURL);
		}
		return wUrl;
	}
	
	public String getwadoURL() {
		String wUrl=initWadoUrl(null);
		String wadoUrl=wUrl+"?requestType=DcmDir&studyUID=" + studyInstanceUid;
		return wadoUrl;
	}
	
	public void setStudyStatus(String studyStatus){
		this.studyStatus=studyStatus;
	}

	public String getStudyStatus(){
		return studyStatus;
	}
	
	public String getStudyStatusLabel() {
		FacesContext context = FacesContext.getCurrentInstance();
		String text = MessageResource.getMessageResourceString(context.getApplication().getMessageBundle(), PREFIX_STUDYSTATUS+studyStatus, null,context.getViewRoot().getLocale());
		return text;
	}

	public String getAccessionNumber() {
		return accessionNumber;
	}

	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}
	
}
