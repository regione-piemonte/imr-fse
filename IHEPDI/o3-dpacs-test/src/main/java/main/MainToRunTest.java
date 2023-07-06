/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package main;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import test.CreatePdiTest;
import test.dto.CreatePdiDTO;

public class MainToRunTest {
	
	private static final Logger logger = LogManager.getLogger(MainToRunTest.class);
	
	public static void main(String[] args) {
		CreatePdiTest createPdiTest = new CreatePdiTest();

		logger.info("Ottengo i test da eseguire...");		
		List<CreatePdiDTO> dtos = createPdiTest.getCreatePdiTests(PATH);
		
		logger.info("Inizio esecuzione dei test...");
		for (CreatePdiDTO dto : dtos) {
			logger.info("DTO ---> " + dto);
				
			String esito = getEsito(createPdiTest.runTestPdi(dto));
			
			logger.info(new StringBuilder("Ho eseguito il test ") 
					.append(dto.getTestCode())
					.append(" - ")
					.append(dto.getTestDescription())
					.append(" con esito ")
					.append(esito)
					.toString());
		}
		
		logger.info("Esecuzione dei test terminata");
		logger.info("Test passati: " + numberTestPassed + ", test falliti: " + numberTestFailed);
	}
	
	private static String getEsito(Boolean esito) {
		if (esito) {
			numberTestPassed++;
		
			return PASSED;
		}
		
		numberTestFailed++;
		
		return FAILED;
	}

	private static int numberTestPassed = 0;
	private static int numberTestFailed = 0;

	private static final String PATH = "src/main/resources/test.txt";
	private static final String PASSED = "PASSED";
	private static final String FAILED = "FAILED";
}