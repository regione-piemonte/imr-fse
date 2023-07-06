/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.users;

import it.units.htl.dpacs.dao.UserManager;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.MailerSystem;
import it.units.htl.maps.Users;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.utils.FileHasher;
import it.units.htl.web.utils.XmlConfigLoader;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.exception.ConstraintViolationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Sara
 * 
 */
public class UserBackBean {

	private Users user;
	private UIData usersTable;
	//	private String lastLogin;

	private Log log = LogFactory.getLog(UserBackBean.class);
	private final String dateFormat = "yyyy-MM-dd";

	private int pagina = 1;

	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	public UserBackBean() {
		user = new Users();

	}

	@SuppressWarnings("unchecked")
	public List<Users> getList() {
		Session s = SessionManager.getInstance().openSession();
		DetachedCriteria query = DetachedCriteria.forClass(Users.class);
		List<Users> users = query.getExecutableCriteria(s).list();
		s.close();
		return users;
	}

	public void editUser(ActionEvent AE) {
		user = (Users) usersTable.getRowData();
	}

	public void createUser(ActionEvent AE) {
		user = new Users();
	}

	public void deleteUser(ActionEvent AE) {
	}

	public String saveUser() {
		String outcome = "fail";

		Session s = SessionManager.getInstance().openSession();
		boolean adding = false;
		boolean isExc = false;
		String originalPassword = null;
		try {
			if (user.getPk() == null) { // adding a user
				originalPassword = user.getPassword();
				user.setPassword(new FileHasher().doHash(originalPassword.getBytes(), "SHA-1").toLowerCase());
				adding = true;
			} else { // Modifying an already present user
				user.setPassword(null);
			}

			s.saveOrUpdate(user);
			s.flush();

			if (adding) {
				UserManager manager = null;
				Date newExp = null;
				try {
					manager = new UserManager();
					int daysToAdd = Integer.parseInt(manager.getConfigParam(ConfigurationSettings.PWD_VALIDITY_DAYS));
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, daysToAdd);
					newExp = new Date(calendar.getTimeInMillis());
					manager.setExpirationDate(user.getPk(), newExp);

				} catch (Exception ex) {
					log.error("An error occurred changing the expiration date", ex);
				}
				try {
					if (manager != null) {
						String loc = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
						String[] recipients = new String[] { user.getEmail() };
						String[] tokens = new String[] { user.getUserName(), originalPassword, new SimpleDateFormat(dateFormat).format(newExp) };
						MailerSystem.send(ConfigurationSettings.EMAIL_EVENT_ADDEDASUSER, loc, recipients, tokens);
					}

				} catch (Exception ex) {
					log.error("An error occurred sending the email", ex);
					throw ex;
				}
			}

			MessageManager.getInstance().setMessage("configurationSaved", null);
			outcome = "Save";

		} catch (ConstraintViolationException e) {
			log.warn("Sql constraint violated");
			MessageManager.getInstance().setMessage("sqlConstraintViolated", new String[] { e.getMessage() });
			isExc = true;
		} catch (Exception e) {
			log.error("Generic exception saving user", e);
			MessageManager.getInstance().setMessage("exceptionOccour", new String[] { e.getMessage() });
			isExc = true;
		} finally {
			s.close();
			if (adding && isExc) {
				this.user.setPk(null);
			}
		}
		return outcome;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public UIData getUsersTable() {
		return usersTable;
	}

	public void setUsersTable(UIData usersTable) {
		this.usersTable = usersTable;
	}

	private Vector<SelectItem> groupIdItems = new Vector<SelectItem>();

	private void getGroupsRoles() {
		groupIdItems.clear();
		Document groupsRoles = XmlConfigLoader.getConfigurationFromDB("WebSecurity");
		NodeList groupsList = groupsRoles.getElementsByTagName("group");
		for (int groupNum = 0; groupNum < groupsList.getLength(); groupNum++) {
			String groupId = groupsList.item(groupNum).getAttributes()
					.getNamedItem("id").getTextContent();
			NodeList groupPoliciesNL = groupsList.item(groupNum)
					.getChildNodes();
			groupIdItems.add(new SelectItem(groupId, ((Node) groupPoliciesNL)
					.getAttributes().getNamedItem("descr").getTextContent()));
		}

	}

	public Vector<SelectItem> getGroupIdItems() {
		getGroupsRoles();
		return groupIdItems;
	}

	public Integer getGroupId() {
		return user.getRoleFk();
	}

	public void setGroupId(Integer nextSWVersion) {
		user.setRoleFk(nextSWVersion);
	}

	public void validateEmail(FacesContext context, UIComponent toValidate,
			Object value) {
		String email = (String) value;
		if (email.indexOf('@') == -1) {
			((UIInput) toValidate).setValid(false);
			FacesMessage message = new FacesMessage("Invalid Email");
			context.addMessage(toValidate.getClientId(context), message);
		}
	}

	public String getLastLogin() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(user.getLastLoginDate().getTime()
				+ user.getLastLoginTime().getTime());
		Date d = c.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String loginTime = sdf.format(d);
		return loginTime;

	}

	public void setLastLogin(String lastLogin) {
		Calendar c = Calendar.getInstance();
		Date utilDate = c.getTime();
		user.setLastLoginDate(utilDate);
		user.setLastLoginTime(utilDate);
		//		this.lastLogin = lastLogin;
	}

}
