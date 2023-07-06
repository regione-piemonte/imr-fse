/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.ui;

import it.units.htl.web.users.JSFUtil;
import it.units.htl.web.utils.XmlConfigLoader;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LanguageManager {
    private Log log = LogFactory.getLog(LanguageManager.class);
    private HashMap<String, Locale> supportedLanguages = new HashMap<String, Locale>();
    private boolean first = true;

    public LanguageManager() {
        loadLanguages();
        FacesContext.getCurrentInstance().getApplication().setMessageBundle("it.units.htl.web.messages");
        FacesContext.getCurrentInstance().getApplication().setSupportedLocales(supportedLanguages.values());
        Locale defaultLang = new Locale("en");
        
        FacesContext.getCurrentInstance().getApplication().setDefaultLocale(defaultLang);
        JSFUtil.storeOnSession(FacesContext.getCurrentInstance(), "locale", defaultLang);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(defaultLang);
    }

    public List<SelectItem> getLocales() {
        List<SelectItem> languageForCombo = new ArrayList<SelectItem>();
        for (String supportedLanguage : supportedLanguages.keySet()) {
            languageForCombo.add(new SelectItem(supportedLanguages.get(supportedLanguage), supportedLanguage));
        }
        if (first) {
            try {
                Enumeration<Locale> langsOfBrowser = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getLocales();
                while (langsOfBrowser.hasMoreElements()) {
                    Locale lang = langsOfBrowser.nextElement();
                    
                    if (supportedLanguages.containsValue(lang)) {
                        JSFUtil.storeOnSession(FacesContext.getCurrentInstance(), "locale", lang);
                        FacesContext.getCurrentInstance().getViewRoot().setLocale(lang);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("while loading languages...", e);
            }
            first = false;
        }
        return languageForCombo;
    }

    public String getLanguage() {
        return ((Locale) JSFUtil.getManagedObject("locale")).toString();
    }
    public void setLanguage(String language) {
        JSFUtil.storeOnSession(FacesContext.getCurrentInstance(), "locale", new Locale(language));
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(language));
    }

    public void loadLanguages() {
        Document config = XmlConfigLoader.getConfigurationFromDB("WebLanguages");
        if (config == null) {
            log.error("Unable to load supported languaes. I'll take eng!");
            supportedLanguages.put("English", new Locale("en"));
        }
        NodeList configuredLanguages = config.getElementsByTagName("language");
        for (int i = 0; i < configuredLanguages.getLength(); i++) {
            Element lingua = (Element) configuredLanguages.item(i);
            supportedLanguages.put(lingua.getElementsByTagName("label").item(0).getTextContent(), new Locale(lingua.getElementsByTagName("id").item(0).getTextContent()));
        }
    }
}
