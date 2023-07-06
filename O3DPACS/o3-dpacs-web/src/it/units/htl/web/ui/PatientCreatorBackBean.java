/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.ui;

import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.maps.Patients;
import it.units.htl.maps.util.SessionManager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hsqldb.Types;

/**
 * Managed bean with scope 'request' used to create new Patients. Currently used
 * in the Merge Tool to create a brand new Patient.
 * 
 * @author petronio
 * 
 */
public class PatientCreatorBackBean {

	private static final Logger log = Logger.getLogger(PatientCreatorBackBean.class);

	private String patientId;
	private String idIssuer;
	private String firstName;
	private String middleName;
	private String lastName;
	private String prefix;
	private String suffix;
	private Date birthDate;
	private String gender = "O";
	private String defaultIdIssuer;
	

	public PatientCreatorBackBean(){
		this.idIssuer=GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.DEFAULT_ID_ISSUER);
		defaultIdIssuer=idIssuer;
	}
	
	/**
	 * JSF ActionListener
	 */
	public void createPatient(ActionEvent event) {
		Patient p = new Patient();

		// required fields
		p.setPatientId(this.patientId.toUpperCase());
		p.setIdIssuer(this.idIssuer.toUpperCase());
		p.setSex(this.gender);
		p.setPatientIdentifierList(this.patientId.toUpperCase());

		// optional fields
		if (this.firstName != null && !this.firstName.equals("")) {
			p.setFirstName(this.firstName.toUpperCase());
		}

		if (this.middleName != null && !this.middleName.equals("")) {
			p.setMiddleName(this.middleName.toUpperCase());
		}

		if (this.lastName != null && !this.lastName.equals("")) {
			p.setLastName(this.lastName.toUpperCase());
		}

		if (this.prefix != null && !this.prefix.equals("")) {
			p.setPrefix(this.prefix.toUpperCase());
		}

		if (this.suffix != null && !this.suffix.equals("")) {
			p.setSuffix(this.suffix.toUpperCase());
		}

		if (this.birthDate != null) {
			p.setBirthDate(new java.sql.Date(this.getBirthDate().getTime()));
		}

		try {
			Long patPk = storeNewPatient(p);
			log.debug("new patient successfully created with pk " + patPk);
			setMergeDestinationFinder();
			resetFields();
		} catch (Exception ex) {
			log.error("cannot create new patient", ex);
		}
	}

	private void resetFields(){
		this.firstName = "";
		this.lastName = "";
		this.middleName = "";
		this.prefix = "";
		this.suffix = "";
		this.birthDate = null;
		this.patientId = "";
		this.idIssuer=defaultIdIssuer;
		this.gender = "O";
	}
	
	/**
	 * Display the created patient in the Find destination result panel of the
	 * Merge tool
	 * 
	 * @param patientId
	 */
	private void setMergeDestinationFinder() {
		FindPatients dest = (FindPatients) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("findDestination");
		if (dest != null) {
			dest.setPatientId(this.patientId);
			dest.setFirstName(this.firstName);
			dest.setLastName(this.lastName);
			dest.setBirthDate(this.birthDate);
		} else {
			log.warn("cannot find session bean 'findDestination'");
		}
	}

	private Long storeNewPatient(Patient p) throws Exception {
		Long ret = null;
		CallableStatement cs = null;
		Connection con = null;

		try {
			con = getDBConnection();
			cs = con.prepareCall("{call addNewPatient(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");

			cs.setString(1, ((p.getLastName() == null) || ("".equals(p.getLastName())) ? null : p.getLastName().toUpperCase()));
			cs.setString(2, ((p.getFirstName() == null) || ("".equals(p.getFirstName())) ? null : p.getFirstName().toUpperCase()));
			cs.setString(3, ("".equals(p.getMiddleName()) ? null : p.getMiddleName()));
			cs.setString(4, ("".equals(p.getPrefix()) ? null : p.getPrefix()));
			cs.setString(5, ("".equals(p.getSuffix()) ? null : p.getSuffix()));
			cs.setDate(6, p.getBirthDate());
			cs.setTime(7, p.getBirthTime());
			cs.setString(8, ((p.getSex() == null) || ("".equals(p.getSex())) ? null : p.getSex().toUpperCase()));
			cs.setString(9, p.getPatientId().toUpperCase());
			cs.setString(10, ((p.getIdIssuer() == null) || ("".equals(p.getIdIssuer())) ? defaultIdIssuer : p.getIdIssuer().toUpperCase()));
			cs.setString(11, p.getEthnicGroup());
			cs.setString(12, p.getPatientComments());
			cs.setString(13, p.getRace());
			cs.setString(14, p.getPatientAddress());
			cs.setString(15, ((p.getPatientAccountNumber() == null) || ("".equals(p.getPatientAccountNumber())) ? null : p.getPatientAccountNumber().toUpperCase()));
			cs.setString(16, ((p.getPatientIdentifierList() == null) || ("".equals(p.getPatientIdentifierList())) ? null : p.getPatientIdentifierList().toUpperCase()));
			cs.setString(17, p.getPatientCity());
			cs.registerOutParameter(18, Types.BIGINT);

			cs.execute();

			ret = cs.getLong(18);
			if (cs.wasNull())
				throw new SQLException("A null patient pk was returned after Patient creation");

			// Maybe send an AuditMessage about new patient creation

		} finally {
			try {
				cs.close();
			} catch (Exception ex) {
				log.error(ex);
			}
			try {
				con.close();
			} catch (Exception ex) {
				log.error(ex);
			}
		}
		return ret;
	}

	private Connection getDBConnection() throws SQLException, NamingException {
		Context jndiContext = new InitialContext();
		DataSource ds = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
		return ds.getConnection();
	}

	/**
	 * Validate the couple patientId + idIssuer: it shall be unique. If idIssuer
	 * has not been specified, the default one will be used.
	 */
	public void validatePatientId(FacesContext context, UIComponent component, Object value) throws ValidatorException {

		// Obtain the patId component from f:attribute
		UIInput patIdComponent = (UIInput) component.getAttributes().get("patientId");
		String patientId = (String) patIdComponent.getValue();
		String idIssuer = (String) value;
		
		if(patientId != null){
			patientId = patientId.toUpperCase();
		}
		if(idIssuer != null){
			idIssuer = idIssuer.toUpperCase();
		}

		if (idIssuer == null || idIssuer.trim().length() == 0) {
			idIssuer = this.defaultIdIssuer;
		}

		Session s = null;
		try {
			s = SessionManager.getInstance().openSession();
			Criteria criteria = s.createCriteria(Patients.class);
			criteria.add(Restrictions.eq("patientId", patientId));
			criteria.add(Restrictions.eq("idIssuer", idIssuer));
			List<?> patients = (List<?>) criteria.list();
			if (patients.size() > 0) {
				FacesMessage message = new FacesMessage();
				message.setSummary("patient id already exists");
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		} finally {
			s.close();
		}

	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public String getIdIssuer() {
		return idIssuer;
	}

	public void setIdIssuer(String idIssuer) {
		this.idIssuer = idIssuer;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
}
