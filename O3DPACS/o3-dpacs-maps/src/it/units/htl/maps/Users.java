/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;


import java.util.Date;


public class Users implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private Long pk;
	private String lastName;
	private String firstName;
	private String userName;
	private String email;
	private int roleFk;
	private String realLifeRole;
	private Date lastLoginDate;
	private Date lastLoginTime;
	private String password;
	private Date pwdExpirationDate; 

	public Users() {
	}

	public Users(String lastName, String firstName, String userName,
			String email, int roleFk, String password) {
		this.lastName = lastName;
		this.firstName = firstName;
		this.userName = userName;
		this.email = email;
		this.roleFk = roleFk;
		this.password = password;
	}

	public Users(String lastName, String firstName, String userName,
			String email, int roleFk, String realLifeRole, Date lastLoginDate,
			Date lastLoginTime, String password) {
		this.lastName = lastName;
		this.firstName = firstName;
		this.userName = userName;
		this.email = email;
		this.roleFk = roleFk;
		this.realLifeRole = realLifeRole;
		this.lastLoginDate = lastLoginDate;
		this.lastLoginTime = lastLoginTime;
		this.password = password;
	}

	public Long getPk() {
		return this.pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getRoleFk() {
		return this.roleFk;
	}

	public void setRoleFk(int roleFk) {
		this.roleFk = roleFk;
	}

	public String getRealLifeRole() {
		return this.realLifeRole;
	}

	public void setRealLifeRole(String realLifeRole) {
		this.realLifeRole = realLifeRole;
	}

	public Date getLastLoginDate() {
		return this.lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public Date getLastLoginTime() {
		return this.lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getPwdExpirationDate() {
		return pwdExpirationDate;
	}

	public void setPwdExpirationDate(Date pwdExpirationDate) {
		this.pwdExpirationDate = pwdExpirationDate;
	}

}
