/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.security;

/**
 * @author sangalli
 *
 */
public class ActionPolicy {
	
	
	private String type;
	private boolean enabled;
	private String description;
	
	
	public ActionPolicy(String _type, boolean _enabled, String _desc){
		type = _type;
		enabled = _enabled;
		description = _desc;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
