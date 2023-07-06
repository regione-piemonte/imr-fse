/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.users;

import it.units.htl.atna.AuditLogService;
import it.units.htl.dpacs.dao.UserManager;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.MailerSystem;
import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.users.util.LDAPAuthenticator;
import it.units.htl.web.utils.FileHasher;
import it.units.htl.web.utils.XmlConfigLoader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.audit.message.UserAuthenticationMessage;
import org.dcm4che2.audit.message.AuditEvent.TypeCode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author francesco.feront
 */
public class LoginBackBean {
    private String username;
    private String password;
    private String email;
    private String locale = "en";
    private final String dateFormat = "yyyy-MM-dd";
    private Log log = LogFactory.getLog(LoginBackBean.class);
    private final static String AUTH_USER = "Authorized_User";
    private HashMap<String, Locale> locales = null;
    private final char[] CHARSFORPASSWORD = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', };
    private final int PASSWORD_LENGTH = 20;

    public LoginBackBean() {
        locales = new HashMap<String, Locale>(4);
        locales.put("English", new Locale("en", "US"));
        locales.put("Italiano", new Locale("it", "IT"));
        locales.put("Nederlands", new Locale("nl", "NL"));
    }

    public List<SelectItem> getLocales() {
        List<SelectItem> test = new ArrayList<SelectItem>();
        test.add(new SelectItem(new Locale("en", "US"), "English"));
        test.add(new SelectItem(new Locale("it", "IT"), "Italiano"));
        test.add(new SelectItem(new Locale("nl", "NL"), "Nederland"));
        return test;
    }

    public void setUsername(String _username) {
        this.username = _username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String _password) {
        this.password = _password;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String login() {
        AuditLogService logService = AuditLogService.getInstance();
        UserAuthenticationMessage msg = new UserAuthenticationMessage(TypeCode.LOGIN);
        if (!"".equals(username)) {
            try {
                msg.addUserPerson(username, "", "", FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath());
                try {
                    msg.addNode(InetAddress.getLocalHost().toString());
                } catch (UnknownHostException e) {
                    log.error("Couldn't get my ip.", e);
                }
                logService.SendMessage(msg);
            } catch (Exception e) {
                log.warn("Unable to send AuditLogMessage", e);
            }
        }
        UserBean ub = null;
        String outcome = "failure";
        if ((ub = verifyUser()) != null) {
            if (!ub.isLoggedIn()) {
                if (ub.getPwdExpirationDate() != null)
                    MessageManager.getInstance().setMessage("PasswordExpired", null);
                else
                    MessageManager.getInstance().setMessage("ProcessUser", null);
            } else {
                java.util.Date utilDate = new java.util.Date();
                Object sqlDate = new java.sql.Date(utilDate.getTime());
                Object sqlTime = new java.sql.Time(utilDate.getTime());
                UserQuery.updateUserField(username, "lastLoginDate", sqlDate);
                UserQuery.updateUserField(username, "lastLoginTime", sqlTime);
                try {
                    UserManager manager = new UserManager();
                    int daysToExp = Integer.parseInt(manager.getConfigParam(ConfigurationSettings.PWD_WARN_DAYS_B4_EXP));
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DATE, daysToExp);
                    if (calendar.getTime().after(ub.getPwdExpirationDate())) {
                        MessageManager.getInstance().setMessage("PasswordExpiring", new String[] { new SimpleDateFormat(dateFormat).format(ub.getPwdExpirationDate()) });
                        return "changePwd";
                    }
                } catch (Exception ex) {
                    log.error("An error occurred verifying expiration date", ex);
                }
                outcome = "success";
            }
        } else {
            MessageManager.getInstance().setMessage("IncorrectCredentials", null);
        }
        return outcome;
    }

    public String forgotPassword() {
        if ((email == null) || ("".equals(email)))
            return null;
        try {
            UserManager manager = new UserManager();
            long userId = manager.getUserForEmail(email);
            if (userId == 0) {
                MessageManager.getInstance().setMessage("EmailNotFound", null);
                return "forgotPass";
            }
            int validityDays = Integer.parseInt(manager.getConfigParam(ConfigurationSettings.PWD_VALIDITY_DAYS));
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, validityDays);
            String newPassword = generatePassword();
            String hashedPassword = new FileHasher().doHash(newPassword.getBytes(), "SHA-1").toLowerCase();
            if (!manager.updatePassword(userId, hashedPassword, calendar.getTime())) {
                MessageManager.getInstance().setMessage("errorChangingPwd", null);
                return "forgotPass";
            }
            // data have been saved on DB: I need to warn the user by email
            String loc = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
            String[] recipients = new String[] { email };
            String[] tokens = new String[] { newPassword, new SimpleDateFormat(dateFormat).format(calendar.getTime()) };
            MailerSystem.send(ConfigurationSettings.EMAIL_EVENT_NEWPASSWORD, loc, recipients, tokens);
        } catch (Exception ex) {
            MessageManager.getInstance().setMessage("errorNewPwd", null);
            return "forgotPass";
        }
        MessageManager.getInstance().setMessage("passwordCreated", null);
        return "forgotPass";
    }

    public void chooseLocaleFromMenu(ValueChangeEvent event) {
        String current = (String) event.getNewValue();
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale((Locale) locales.get(current));
        JSFUtil.storeOnSession(FacesContext.getCurrentInstance(), "locale", locales.get(current));
    }

    private UserBean verifyUser() {
        UserBean currentUser = null;
        try {
            LDAPAuthenticator auth = null;
            try {
                auth = new LDAPAuthenticator();
            } catch (Exception e) {
                log.debug("No LDAP configuration found.", e);
            }
            SearchResult sr = null;
            if (auth != null)
                sr = auth.doLogin(username, password);
                if (sr != null) {
                log.debug("User on LDAP!!!");
                currentUser = new UserBean();
                UserUtil.copyUserProperties(sr, currentUser);
                
                if (currentUser.getRole() == 0)
                    MessageManager.getInstance().setMessage("userWithoutGroup", new String[]{currentUser.getUserName()});
            } else {
                String hashedPassword = new FileHasher().doHash(password.getBytes(), "SHA-1").toLowerCase();
                currentUser = UserQuery.findUser(username, hashedPassword);
            }
            if ((currentUser != null) && (currentUser.isLoggedIn())) {
                UserBean managedUserBean = (UserBean) JSFUtil.getManagedObject("userBean");
                UserUtil.copyUserProperties(currentUser, managedUserBean);
                if (loadConfig(managedUserBean.getRole() + "")) {
                    // Place authorized user on session to disable security filter
                    JSFUtil.storeOnSession(FacesContext.getCurrentInstance(), AUTH_USER, "Authorized_User");
                } else {
                    JSFUtil.storeOnSession(FacesContext.getCurrentInstance(), "userBean", null);
                }
            }
        } catch (Exception ex) {
            log.error("An error occurred retrieving login information", ex);
        }
        return currentUser;
    }

    public String logout() throws IOException {
        ExternalContext ectx = FacesContext.getCurrentInstance()
                .getExternalContext();
        HttpSession session = (HttpSession) ectx.getSession(false);
        FacesContext ctx = FacesContext.getCurrentInstance();
        Application app = ctx.getApplication();
        try {
            UserBean managedUserBean = (UserBean) JSFUtil
                    .getManagedObject("userBean");
            AuditLogService logService = AuditLogService.getInstance();
            UserAuthenticationMessage msg = new UserAuthenticationMessage(
                    TypeCode.LOGOUT);
            msg.addUserPerson(managedUserBean.getUserName(), managedUserBean
                    .getLastLoginDate()
                    + " "
                    + managedUserBean.getLastLoginTime()
                    + " "
                    + managedUserBean.getFirstName()
                    + " "
                    + managedUserBean.getLastName(), managedUserBean.getUserName(),
                    (String) session.getAttribute("ClientIp"));
            try {
                msg.addNode(InetAddress.getLocalHost().toString());
            } catch (UnknownHostException e) {
                log.error("Couldn't get my ip.", e);
            }
            logService.SendMessage(msg);
        } catch (Exception e) {
            log.warn("Unable to send AuditLogMessage", e);
        }
        session.invalidate();
        app.getNavigationHandler().handleNavigation(ctx, "/index.jsp",
                "welcome");
        return null;
    }

    public String getLocale() {
        return locale;
    }

    private boolean loadConfig(String userRole) {
        try {
            Document config = XmlConfigLoader.getConfigurationFromDB("WebSecurity");
            if (config == null) {
                log.error("Unable to load security policy of web!");
                return false;
            }
            NodeList users = config.getElementsByTagName("group");
            boolean found = false;
            for (int i = 0; i < users.getLength(); i++) {
                // una volta identificato il gruppo salvo le sue policy in sessione.
                if (userRole.equals(users.item(i).getAttributes().getNamedItem("id").getTextContent())) {
                    Element e = (Element) users.item(i);
                    NodeList pattern = e.getElementsByTagName("pattern");
                    ArrayList<String> patterns = new ArrayList<String>();
                    for (int p = 0; p < pattern.getLength(); p++) {
                        patterns.add(pattern.item(p).getTextContent());
                    }
                    NodeList enabledActions = e.getElementsByTagName("type");
                    HashMap<String, Boolean> actionEnabled = new HashMap<String, Boolean>();
                    for (int k = 0; k < enabledActions.getLength(); k++) {
                        actionEnabled.put(enabledActions.item(k).getTextContent(),
                                true);
                    }
                    JSFUtil.storeOnSession(FacesContext.getCurrentInstance(),
                            "actionEnabled", actionEnabled);
                    JSFUtil.storeOnSession(FacesContext.getCurrentInstance(),
                            "secPolicies", patterns);
                    found = true;
                }
            }
            if (!found) {
                log.error("Policies for this userRole not found!!");
                return false;
            }
            // prendo le aree da controllare e le salvo nella sessione
            Document webConfig = XmlConfigLoader.getConfigurationFromDB("WebConfiguration");
            if (webConfig == null) {
                log.error("WebConfiguration not found!");
                return false;
            }
            NodeList patterns = webConfig.getElementsByTagName("pattern");
            String[] areas = new String[patterns.getLength()];
            for (int i = 0; i < patterns.getLength(); i++) {
                areas[i] = patterns.item(i).getTextContent();
            }
            JSFUtil.storeOnSession(FacesContext.getCurrentInstance(), "areas", areas);
            return true;
        } catch (Exception e) {
            log.warn("Unable to load config for this webuser", e);
            return false;
        }
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String generatePassword() {
        StringBuilder builder = new StringBuilder();
        int l = CHARSFORPASSWORD.length;
        for (int i = 0; i < PASSWORD_LENGTH; ++i) {
            int r = 0;
            try {
                r = (int) Math.floor(Math.random() * l); // [0,36[
                builder.append(CHARSFORPASSWORD[r]);
            } catch (RuntimeException rex) {
                log.warn("An exception occurred generating a character", rex);
                builder.append(CHARSFORPASSWORD[0]);
            }
        }
        return builder.toString();
    }
}
