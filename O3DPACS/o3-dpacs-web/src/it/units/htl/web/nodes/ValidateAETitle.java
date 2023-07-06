/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.nodes;

import it.units.htl.maps.KnownNodes;
import it.units.htl.maps.util.SessionManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class ValidateAETitle implements Validator {

	Pattern mask = null;

	private static final String AE_PATTERN = "[ -~&&[^\\\\]]+";

	public void validate(FacesContext arg0, UIComponent arg1, Object arg2)
			throws ValidatorException {
		mask = Pattern.compile(AE_PATTERN);
		Matcher matcher = mask.matcher((String) arg2);
		if (!matcher.matches()) {
			FacesMessage message1 = new FacesMessage();
			message1.setDetail("Malformed AE Title");
			message1.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message1);
		}
//		Session s = SessionManager.getInstance().openSession();
//		DetachedCriteria query = DetachedCriteria.forClass(KnownNodes.class);
//		query.add(Restrictions.like("aeTitle", (String) arg2));
//		List<KnownNodes> result = query.getExecutableCriteria(s).list();
//		s.close();
//		if (!result.isEmpty()) {
//			FacesMessage message2 = new FacesMessage();
//			message2.setDetail("AE Title already existing");
//			message2.setSeverity(FacesMessage.SEVERITY_ERROR);
//			throw new ValidatorException(message2);
//		}
	}

}
