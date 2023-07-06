/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

public class JnlpGeneratorSupportBean {
	private String _jnlpUrl = "";
	public String get_jnlpUrl(){
		return _jnlpUrl;
	}
	public void set_jnlpUrl(String jnlpUrl){
		_jnlpUrl = jnlpUrl;
	}
}
