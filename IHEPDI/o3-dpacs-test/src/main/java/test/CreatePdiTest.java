/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import test.dto.CreatePdiDTO;
import test.dto.CreatePdiResponseDTO;
import test.dto.ErrorDTO;
import test.dto.SuccessDTO;

public class CreatePdiTest {

	private static final Logger logger = LogManager.getLogger(CreatePdiTest.class);
	
	/**
	 * Fornisce i DTO contenenti le informazioni per i test
	 * @param path il path del file di configurazione contenente i test da eseguire
	 * @return la lista di DTO contenenti le informazioni per i test
	 */
	public List<CreatePdiDTO> getCreatePdiTests(String path) {
		List<CreatePdiDTO> createPdiDTOs = new ArrayList<CreatePdiDTO>();
		
		File file = new File(path);
		try {
			List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
			for (String line : lines) {
				if (line.startsWith(COMMENT)) {
					continue;
				}
				
				List<String> values = Arrays.asList(line.split(SPLITTER));
				
				createPdiDTOs.add(new CreatePdiDTO(
						values.get(0), // testCode
						values.get(1), // testDescription
						values.get(2), // requestID
						values.get(3), // patientID
						values.get(4), // studyUID
						values.get(5), // accessionNumber
						values.get(6), // idIssuer
						values.get(7), // azienda
						values.get(8), // struttura
						new CreatePdiResponseDTO(
								Integer.parseInt(values.get(9)), // code
								values.get(10)))); // response
			}
		} catch (IOException e) {
			logger.info("Cannot read file for building requests");
			e.printStackTrace();
			
			return null;
		}
		
		return createPdiDTOs;
	}
	
	/**
	 * Esegue un test per il servizio
	 * @param createPdiDTO il DTO contenete le informazioni per il test
	 * @return true se il test va a buon fine, false altrimenti
	 */
	public boolean runTestPdi(CreatePdiDTO createPdiDTO) {
		CreatePdiResponseDTO response = callCreatePdi(createPdiDTO);
		if (!checkResponse(response, createPdiDTO)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Verifica l'esito della richiesta con l'esito atteso
	 * @param response la risposta da verificare
	 * @param createPdiDTO il DTO contenente la risposta attesa
	 * @return true se l'esito della richiesta rispecchia l'esito atteso, false altrimenti
	 */
	private boolean checkResponse(CreatePdiResponseDTO response, CreatePdiDTO createPdiDTO) {
		logger.info("Checking response ---> " + response);
		CreatePdiResponseDTO expectedResponse = createPdiDTO.getExpected();
		int code = response.getCode();
		boolean sameCode = code == expectedResponse.getCode();
		if (code == 200 && sameCode) {
			return true;
		}
		
		if (sameCode && response.getResponse().equals(expectedResponse.getResponse())) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Effettua chiamata al servizio
	 * @param createPdiDTO il DTO contenente le informazioni necessarie a chiamare il servizio
	 * @return il DTO di risposta costruito sulla base della risposta del servizio
	 */
	private CreatePdiResponseDTO callCreatePdi(CreatePdiDTO createPdiDTO) {
		WebTarget webTarget = ClientBuilder.newClient().target(SERVICE_URL);
		Invocation.Builder invocationBuilder = webTarget.request()
				.header(SERVICE_AUTH_HEADER, SERVICE_AUTH);

		Response response = invocationBuilder.post(
				Entity.entity(getParamsForCall(createPdiDTO), 
				MediaType.APPLICATION_FORM_URLENCODED_TYPE), 
				Response.class);
		
		String entity = response.readEntity(String.class);
		Gson gson = new Gson();
		if (response.getStatus() == 200) {
			SuccessDTO success = gson.fromJson(entity, SuccessDTO.class);
			return new CreatePdiResponseDTO(response.getStatus(), success.getJobId());
		} else {
			ErrorDTO error = gson.fromJson(entity, ErrorDTO.class);
			return new CreatePdiResponseDTO(response.getStatus(), error.getDescrErrore());
		}
	}

	/**
	 * Fornisce i parametri per la richiesta
	 * @param createPdiDTO il DTO da cui recuperare i parametri
	 * @return i parametri per la richiesta
	 */
	private Form getParamsForCall(CreatePdiDTO createPdiDTO) {
		Form form = new Form();
		
		String requestID = createPdiDTO.getRequestID();
		String patientID = createPdiDTO.getPatientID();
		String studyUID = createPdiDTO.getStudyUID();
		String accessionNumber = createPdiDTO.getAccessionNumber();
		String idIssuer = createPdiDTO.getIdIssuer();
		String azienda = createPdiDTO.getAzienda();
		String struttura = createPdiDTO.getStruttura();

		if (patientID != null && !patientID.isEmpty()) {
			form.param(PATIENT_ID, patientID);
		}

		if (requestID != null && !requestID.isEmpty()) {
			form.param(REQUEST_ID, requestID);
		}
		
		if (studyUID != null && !studyUID.isEmpty()) {
			form.param(STUDY_UID, studyUID);
		}
		
		if (accessionNumber != null && !accessionNumber.isEmpty()) {
			form.param(ACCESSION_NUMBER, accessionNumber);
		}
		
		if (idIssuer != null && !idIssuer.isEmpty()) {
			form.param(ID_ISSUER, idIssuer);
		}
		
		if (azienda != null && !azienda.isEmpty()) {
			form.param(AZIENDA, azienda);
		}
		
		if (struttura != null && !struttura.isEmpty()) {
			form.param(STRUTTURA, struttura);
		}
		
		return form;
	}
	
	private static final String STRUTTURA = "struttura";
	private static final String AZIENDA = "azienda";
	private static final String ID_ISSUER = "idIssuer";
	private static final String ACCESSION_NUMBER = "accessionNumber";
	private static final String STUDY_UID = "studyUID";
	private static final String REQUEST_ID = "requestID";
	private static final String PATIENT_ID = "patientID";
	private static final String SERVICE_AUTH = "Administrator:21b702c478e15b3891bf4142be0512877f77df60";
	private static final String SERVICE_AUTH_HEADER = "Authorization";
	private static final String SERVICE_URL = "http://localhost:8080/o3-dpacs-pdi/CreatePdi";
	private static final String SPLITTER = "#";
	private static final String COMMENT = "<->";
}