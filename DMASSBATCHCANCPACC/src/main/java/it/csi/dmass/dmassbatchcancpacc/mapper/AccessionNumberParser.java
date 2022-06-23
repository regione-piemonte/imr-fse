/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.csi.dmass.dmassbatchcancpacc.mapper;

import java.util.ArrayList;
import java.util.List;

import it.csi.dmass.dmassbatchcancpacc.dto.AccessionNumber;

public class AccessionNumberParser {
	
	private List<String> listNumber = new ArrayList<String>();
	private String aetitle;
	private String patientID;
	
	public AccessionNumberParser(String accession_numbers) {
		this.parse(accession_numbers);
	}
	
	private void parse(String accession_numbers) {
		
		List<AccessionNumber> listAccessionNumber = new ArrayList<AccessionNumber>();
		for (String accession_number : accession_numbers.split("\\|-\\|")) {
			AccessionNumber accessionNumber = extractedAccessionNumber(accession_number);			
			listAccessionNumber.add(accessionNumber);
			listNumber.add(accessionNumber.getNumber());
		}
		
		AccessionNumber firstAccessionNumber = listAccessionNumber.get(0);
		this.aetitle=firstAccessionNumber.getAetitle();
		this.patientID=firstAccessionNumber.getPatientID();
	}
	
	
	
	public List<String> getNumbers() {
		return listNumber;
	}



	public String getAetitle() {
		return aetitle;
	}



	public String getPatientID() {
		return patientID;
	}



	public AccessionNumber extractedAccessionNumber(String accession_number) {
		AccessionNumber accessionNumber = new AccessionNumber();
		String[] fields = accession_number.split("\\$");
		// retrocompatibilita'
		int idx = 0;
		for (String field : fields) {
			switch (idx) {
			case 0:
				accessionNumber.setNumber(field);
				break;
			case 1:
				accessionNumber.setAetitle(field);
				break;
			case 2:
				accessionNumber.setPatientID(field);
				break;
			case 3:
				accessionNumber.setIssuer(field);
				break;
			default:
				break;
			}
			idx++;
		}
		return accessionNumber;
	}
	 
}
