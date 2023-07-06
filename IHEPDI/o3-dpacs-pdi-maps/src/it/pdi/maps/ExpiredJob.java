/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.pdi.maps;

import java.io.Serializable;

public class ExpiredJob implements Serializable {
	
	private static final long serialVersionUID = -8703666954864171736L;
	
	public ExpiredJob(String jobId, String zipName, String requestId, String digest) {
		this.jobId = jobId;
		this.zipName = zipName;
		this.requestId = requestId;
		this.digest = digest;
	}

	public String getJobId() {
		return jobId;
	}
	
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	public String getZipName() {
		return zipName;
	}
	
	public void setZipName(String zipName) {
		this.zipName = zipName;
	}
	
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}


	private String jobId;
	private String zipName;
	private String requestId;
	private String digest;
	
}
