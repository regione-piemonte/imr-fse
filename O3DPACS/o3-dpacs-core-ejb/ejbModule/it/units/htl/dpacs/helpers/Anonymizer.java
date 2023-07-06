/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import it.units.htl.dpacs.dao.DicomDbDealer;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.Tag;

/**
 * Anonimizer is the class that anonimizes dicom data. Anonimized is meant as: -
 * taking away the name and set NO_SURNAME and NO_NAME - Bringing birthdate to 1
 * day of birth year (age) - Keeping the patient ID
 * 
 * Two type of anonimization are implemented, one for storing one for query
 * 
 * @author Mbe
 */
public class Anonymizer {
	static final Log log = LogFactory.getLog(Anonymizer.class);
	private static DicomDbDealer bean = null;

	/**
	 * Goes to Database (KnownNodes) to see if the data from AE needs
	 * anonimization
	 * 
	 * @param AE
	 *            AE Tile
	 * @return if the data should be anonimized for that AE
	 */
	public static boolean isAnonymized(String AE) {
		return bean.isAnonymized(AE);
	}
	
	public static boolean hasToRemovePatientId(String ae){
		return bean.hasToRemovePatientId(ae);
	}

	public Anonymizer() {
			try {
			    bean = InitialContext.doLookup(BeansName.LDicomDbDealer);
			} catch (NamingException nex) {
                log.fatal("Unable to find DicomDbDealer!", nex);
            } 
	}

	/**
	 * Performs the anonimization
	 * 
	 * @param in
	 *            The dataset to anonimize
	 * @param isQuery
	 *            if you are anonimizing an incoming query or an outcoming
	 *            data/storage data
	 * @return anonimized dataset
	 */
	public Dataset anonymize(Dataset in, boolean isQuery, boolean alsoPatientId) {
		if (in != null) {
			if (!isQuery) {
				in.putPN(Tags.PatientName, "NO_SURNAME^NO_NAME");
				if (in.getString(Tags.RequestingPhysician) != null)
					in.putPN(Tags.RequestingPhysician,
							"NO_DOC_NAME^NO_DOC_SURNAME");
				if (in.getString(Tags.ReferringPhysicianName) != null)
					in.putPN(Tags.ReferringPhysicianName,
							"NO_DOC_NAME^NO_DOC_SURNAME");
				if (in.getString(Tags.InstitutionName) != null)
					in.putLO(Tags.InstitutionName, "NO_INSTITUTION");
				if (in.getString(Tags.InstitutionAddress) != null)
					in.putST(Tags.InstitutionAddress, "NO_INSTITUTION");
				SimpleDateFormat dfDate = new SimpleDateFormat("yyyyMMdd");
				String date = in.getString(Tags.PatientBirthDate);
				if (date != null) {
					log.debug("Date before: " + date);
					date = date.substring(0, 4);
					log.debug("Date after : " + date);
					try {
						in.putDA(Tags.PatientBirthDate, dfDate.parse(date
								+ "0101"));
					} catch (ParseException e) {
						log.error("",e);
					}
				}
				if(alsoPatientId){
					in.putLO(Tag.PatientID, in.getString(Tags.StudyInstanceUID));
				}
			} else {
				in.putPN(Tags.PatientName, "*");
			}
		}
		return in;
	}
	
	/**
	 * @param sourceDataset
	 *         the dataset that will remove the Patient Name and the Patient ID
	 * @return the dataset without the Patient Name and the Patient ID
	 */
	
	public Dataset removeNameAndPatientId(Dataset src){
	    src.putPN(Tag.PatientName, "NO_SURNAME^NO_NAME");
	    src.putLO(Tag.PatientID, "NO_ID");
	    return src;
	}

}
