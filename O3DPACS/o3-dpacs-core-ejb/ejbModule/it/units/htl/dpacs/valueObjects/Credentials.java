/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.io.Serializable;

public class Credentials implements Serializable{
	
	private String userName;
	private String password;
	private String method;
	
	public Credentials() {
		this.userName = null;
		this.password = null;
		this.method = null;
	}
	
	public Credentials(String userName, String password, String method) {
		this.userName = userName;
		this.password = password;
		this.method = method;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}	

}
