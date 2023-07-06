/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.nodes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class ValidateIP implements Validator {
	
	Pattern mask  = null;
//	private static final String IP_PATTERN ="(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).){2}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	private static final String IP_PATTERN ="^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
	
	public void validate(FacesContext arg0, UIComponent arg1, Object arg2)
			throws ValidatorException {
		mask = Pattern.compile(IP_PATTERN);
		Matcher matcher = mask.matcher((String) arg2);
		if (!matcher.matches()){
			FacesMessage message = new FacesMessage();
			message.setDetail("Wrong IP address");
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
	}

}
