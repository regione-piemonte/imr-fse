/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.users;

import it.units.htl.dpacs.dao.UserManager;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.MailerSystem;
import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.utils.FileHasher;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.faces.context.FacesContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ChangePwdBackBean {

	private String oldPassword;
	private String newPassword;
	private String confirmNewPassword;
	private String cannotRepeatPassword;
	private final String dateFormat = "yyyy-MM-dd";

	private HashMap<String, String> properties;

	private Log log = LogFactory.getLog(ChangePwdBackBean.class);

	public ChangePwdBackBean() {
		init();
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmNewPassword() {
		return confirmNewPassword;
	}

	public void setConfirmNewPassword(String confirmNewPassword) {
		this.confirmNewPassword = confirmNewPassword;
	}

	public String getCannotRepeatPassword() {
		return ("1".equals(cannotRepeatPassword) ? "0" : "1");
	}

	public int getMinimumLength() {
		try {
			return Integer.parseInt((String) properties.get(ConfigurationSettings.PWD_MINIMUM_LENGTH));
		} catch (NumberFormatException nfex) {
			return 1;
		}
	}

	public String getLettersMandPattern() {
		String ret = "";
		try {
			ret = (String) properties.get(ConfigurationSettings.PWD_LETTERS_MAND);
		} catch (Exception ex) {
			return "";
		}
		return ret;
	}

	public String getCasesMandPattern() {
		String ret = "";
		try {
			ret = (String) properties.get(ConfigurationSettings.PWD_BOTHCASES_MAND);
		} catch (Exception ex) {
			return "";
		}
		return ret;
	}

	public String getDigitsMandPattern() {
		String ret = "";
		try {
			ret = (String) properties.get(ConfigurationSettings.PWD_DIGITS_MAND);
		} catch (Exception ex) {
			return "";
		}
		return ret;
	}

	public String getSymbolsMandPattern() {
		String ret = "";
		try {
			ret = (String) properties.get(ConfigurationSettings.PWD_SYMBOLS_MAND);
		} catch (Exception ex) {
			return "";
		}
		return ret;
	}

	private void init() {
		try {
			UserManager manager = new UserManager();
			properties = manager.getPasswordConstraints();
			cannotRepeatPassword = (String) properties.get(ConfigurationSettings.PWD_CANREPEAT);
		} catch (NamingException nex) {
			log.error("An error occurred loading the password settings", nex);
		}
	}

	public String doChange() {
		String hashedNewPassword = new FileHasher().doHash(newPassword.getBytes(), "SHA-1").toLowerCase();
		String hashedOldPassword = new FileHasher().doHash(oldPassword.getBytes(), "SHA-1").toLowerCase();
		UserBean userBean = (UserBean) JSFUtil.getManagedObject("userBean");
		Date newExp = null;
		if (userBean.getPwdExpirationDate() != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, Integer.parseInt((String) properties.get(ConfigurationSettings.PWD_VALIDITY_DAYS)));
			newExp = new Date(calendar.getTimeInMillis());
		}
		UserManager manager = null;
		try {
			manager = new UserManager();
		} catch (NamingException nex) {
			log.error("An error occurred retrieving UserManager", nex);
		}
		if ((manager != null) && (manager.changePassword(userBean.getAccountNo(), userBean.getEmail(), newExp, hashedOldPassword, hashedNewPassword))) {
			userBean.setPassword(hashedNewPassword);
			MessageManager.getInstance().setMessage("PasswordChanged", null);

			String loc = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();


			String[] recipients = new String[]{userBean.getEmail()};
			String[] tokens = new String[] { new SimpleDateFormat(dateFormat).format(newExp) };
			MailerSystem.send(ConfigurationSettings.EMAIL_EVENT_CHANGEDPASSWORD, loc, recipients, tokens);

			return "pwdChanged";
		} else {
			MessageManager.getInstance().setMessage("PasswordNotChanged", null);
			log.warn("Password not changed for user " + userBean.getAccountNo()); // If just this message appears, probably the "old" password was not correct
			return null;
		}
	}

}
