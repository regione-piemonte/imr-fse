/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package utils;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import dto.ErrorDTO;
import dto.SuccessDTO;

public class ResponseUtils {

	/**
	 * Scrive errore sulla risposta
	 * @param response la risposta su cui scrivere l'errore
	 * @param codErrore il codice dell'errore riscontrato
	 * @param descrErrore la descrizione dell'errore riscontrato
	 */
	public static void writeErrorOnResponse(HttpServletResponse response, String codErrore, String descrErrore) throws IOException {
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.getWriter().append(ErrorDTO.buildErrorDTO(codErrore, descrErrore));
	}
	
	/**
	 * Scrive succsso sulla risposta
	 * @param response la risposta su cui scrivere l'errore
	 * @param jobId l'id del job attivato
	 */
	public static void writeSuccessOnResponse(HttpServletResponse response, String jobId) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().append(SuccessDTO.buildSuccessDTO(jobId));
	}
}
