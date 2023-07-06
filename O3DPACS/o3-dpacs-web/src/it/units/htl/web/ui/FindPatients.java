/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.ui;

import it.units.htl.maps.Patients;
import it.units.htl.maps.PatientsHome;
import it.units.htl.maps.Series;
import it.units.htl.maps.Studies;
import it.units.htl.maps.util.SessionManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

public class FindPatients {

	private String firstName = "";
	private String lastName = "";
	private String patientId = "";

	private Date birthDate = null;
	private Date tmpBirthDate = new Date();
	private String tmpfirstName = "";
	private String tmplastName = "";
	private String tmppatientId = "";
	
	public boolean _toRefresh = true;
	private List<Patients> patientResults = new ArrayList<Patients>();
//	private static Log log = LogFactory.getLog(FindPatients.class);

	
	public List<Patients> getPazienti(){
		Session s = SessionManager.getInstance().openSession();
		List<Patients> p = getPazienti(s);
		s.close();
		return p;
	}
	
	public List<Patients> getPazienti(Session s) {
		if (((firstName == null)&&(lastName==null)&& (patientId==null)&&(birthDate==null))
				||
			("".equals(firstName) && "".equals(lastName) && "".equals(patientId) && birthDate == null)) {
			return new ArrayList<Patients>();
		}
		if (!(firstName.equals(tmpfirstName) && lastName.equals(tmplastName) && patientId
				.equals(tmppatientId) && (tmpBirthDate == birthDate)) || _toRefresh) {
			PatientsHome PH = new PatientsHome();
			Patients PToFind = new Patients();			 
			if (firstName != "") {
				PToFind.setFirstName(firstName.replaceAll("\\*", "%").replaceAll("\\?","_"));
			}
			if (lastName != "") {
				PToFind.setLastName(lastName.replaceAll("\\*", "%").replaceAll("\\?","_"));
			}
			if (patientId != "") {
				PToFind.setPatientId(patientId.replaceAll("\\*", "%").replaceAll("\\?","_"));
			}
			if (birthDate != null) {
				if (birthDate != tmpBirthDate) {
					PToFind.setBirthDate(birthDate);
				}
			}
			patientResults = PH.findByExample(PToFind, s); 
			tmpfirstName = firstName;
			tmplastName = lastName;
			tmppatientId = patientId;
			tmpBirthDate = birthDate;
			_toRefresh = false;
		}
		return patientResults;
		
	}

	@SuppressWarnings("unchecked")
	public TreeNode getPatientsTree() {
		Session sess = SessionManager.getInstance().openSession();
		TreeNode root = new TreeNodeImpl();
		root.setData("Root Node");
		ArrayList<Patients> patients = (ArrayList<Patients>) getPazienti(sess);
		if (patients.size() > 0) {
			for (int i = 0; i < patients.size(); i++) {
				TreeNode childPaziente = new TreeNodeImpl();
				childPaziente.setData(patients.get(i));
				Set<Studies> studyResults = patients.get(i).getStudies();
				int m = 0;
				for (Iterator<Studies> st = studyResults.iterator(); st
						.hasNext();) {
					TreeNode childStudio = new TreeNodeImpl();
					Studies s = st.next();
					childStudio.setData(s);
					int l = 0;
					for (Iterator<Series> se = s.getSeries().iterator(); se
							.hasNext();) {
						TreeNode childSerie = new TreeNodeImpl();
						childSerie.setData(se.next());
						childStudio.addChild(l++, childSerie);
					}
					childPaziente.addChild(m++, childStudio);
				}
				root.addChild(i, childPaziente);
			}
		}
		sess.close();
		return root;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	protected FacesContext context() {
		return (FacesContext.getCurrentInstance());
	}
}
