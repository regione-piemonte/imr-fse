/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;

public class ServicesConfiguration implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private String serviceName;
	private String configuration;
	private String runFromIp;
	private boolean enabled;

	public ServicesConfiguration() {
	}

	public ServicesConfiguration(String serviceName, boolean enabled) {
		this.serviceName = serviceName;
		this.enabled = enabled;
	}

	public ServicesConfiguration(String serviceName, String configuration,
			boolean enabled) {
		this.serviceName = serviceName;
		this.configuration = configuration;
		this.enabled = enabled;
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getConfiguration() {
		return this.configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getRunFromIp() {
		return runFromIp;
	}

	public void setRunFromIp(String runFromIp) {
		this.runFromIp = runFromIp;
	}

	
}
