/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.users;



import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Sara, francesco.feront
 */

public class UserBean {
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String email;
    private int role;
    private String realLifeRole;
    private String lastLoginTime;
    private String lastLoginDate;
    private Date pwdExpirationDate;
    private boolean isLdap;
    
    
    private boolean loggedIn;
    
    private static final int ADMINISTRATOR = 1;
    private static final int SUPERUSER = 2;
    private static final int USER = 3;
    private static final int PHYSICIAN = 4;
    private long accountNo = -1;
    
    public UserBean() {
    }
    
    public UserBean(int _accountNo, String _firstName, String _lastName, String _userName, String _password, String _email, int _role, String _realLifeRole, String _lastLoginDate, String _lastLoginTime) {
        this.setAccountNo(_accountNo);
        this.setFirstName(_firstName);
        this.setLastName(_lastName);
        this.setUserName(_userName);
        this.setPassword(_password);
        this.setEmail(_email);
        this.setRole(_role);
        this.setRealLifeRole(_realLifeRole);
        this.setLastLoginDate(_lastLoginDate);
        this.setLastLoginTime(_lastLoginTime);
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String _firstName) {
        this.firstName = _firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String _lastName) {
        this.lastName = _lastName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String _userName) {
        this.userName = _userName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String _password) {
        this.password = _password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String _email) {
        this.email = _email;
    }
    
    public int getRole() {
        return role;
    }
    
    public boolean isAdmin() {
        boolean verifyRole = false;
        if(role==ADMINISTRATOR) verifyRole = true;
        return verifyRole;
    }
    
    public boolean isSuperUser() {
    	boolean verifyRole = false;
    	if(role==SUPERUSER || role==ADMINISTRATOR) verifyRole = true;
    	return verifyRole;
    }
    
    public boolean isPhysician() {
    	boolean verifyRole = false;
    	if(role==PHYSICIAN) verifyRole = true;
    	return verifyRole;
    }
    
    public void setRole(int _role) {
        this.role = _role;
    }
    
    public String getRealLifeRole() {
        return realLifeRole;
    }
    
    public void setRealLifeRole(String _realLifeRole) {
        this.realLifeRole = _realLifeRole;
    }
    
    public void setLastLoginDate(String _lastLoginDate) {
        this.lastLoginDate = _lastLoginDate;
    }
    
    public String getLastLoginDate() {
        return lastLoginDate;
    }
    
       public void setLastLoginTime(String _lastLoginTime) {
        this.lastLoginTime = _lastLoginTime;
    }
    
    public String getLastLoginTime() {
        return lastLoginTime;
    }
    
    public Date getPwdExpirationDate() {
		return pwdExpirationDate;
	}

	public void setPwdExpirationDate(Date pwdExpirationDate) {
		this.pwdExpirationDate = pwdExpirationDate;
	}

	public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
    
    public boolean isLoggedIn() {
        return loggedIn;
    }
    
    public void setAccountNo(long accountNo) {
        this.accountNo = accountNo;
    }
    
    public long getAccountNo() {
        return accountNo;
    }

    public boolean isLdap() {
        return isLdap;
    }

    public void setLdap(boolean isLdap) {
        this.isLdap = isLdap;
    }
}
