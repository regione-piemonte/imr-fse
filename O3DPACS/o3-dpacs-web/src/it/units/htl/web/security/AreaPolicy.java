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
public class AreaPolicy {
	private boolean _enabled = true;
	private String _area;
	private String _descr = "";
	
	
	public String getDescr() {
		return _descr;
	}

	public void setDescr(String _descr) {
		this._descr = _descr;
	}

	public AreaPolicy(String name,String descr, boolean enabled){
		_area = name;
		_enabled = enabled;
		_descr = descr;
	}
		
	public String getArea() {
		return _area;
	}
	public void setArea(String area) {
		_area = area;
	}
	public boolean getEnabled() {
		return _enabled;
	}
	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}
	
	
}
