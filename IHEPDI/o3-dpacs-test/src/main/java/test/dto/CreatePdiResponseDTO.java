/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package test.dto;

public class CreatePdiResponseDTO {

	public CreatePdiResponseDTO(int code, String response) {
		this.code = code;
		this.response = response;
	}

	public CreatePdiResponseDTO() {}

	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public String getResponse() {
		return response;
	}
	
	public void setResponse(String response) {
		this.response = response;
	}
	
	@Override
	public String toString() {
		return "CreatePdiResponseDTO [code=" + code + ", response=" + response + "]";
	}

	private int code;
	private String response;
}
