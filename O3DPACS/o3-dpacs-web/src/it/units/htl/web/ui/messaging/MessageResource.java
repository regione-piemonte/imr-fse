/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.ui.messaging;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MessageResource {
	
	protected static ClassLoader getCurrentClassLoader(Object defaultObject){		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();		
		if(loader == null){
			loader = defaultObject.getClass().getClassLoader();
		}		
		return loader;
	}

	public static String getMessageResourceString(
							String bundleName, 
							String key, 
							Object params[], 
							Locale locale){
		
		String text = "";		
		ResourceBundle bundle =	ResourceBundle.getBundle(bundleName, locale, getCurrentClassLoader(params));
		try{
			text = bundle.getString(key);
		} catch(MissingResourceException e){
			text = "?? key " + key + " not found ??";
		}		
		if(params != null){
			MessageFormat mf = new MessageFormat(text, locale);
			text = mf.format(params, new StringBuffer(), null).toString();
		}
		return text;
	}
}
