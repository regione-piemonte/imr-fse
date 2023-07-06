/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.utils.tags;

import it.units.htl.web.ui.messaging.MessageResource;


import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class RegExpsValidator implements Validator {
	
	private String MESSAGE_LETTERS_MAND="message_LettersMand";
	private String MESSAGE_CASES_MAND="message_CasesMand";
	private String MESSAGE_DIGITS_MAND="message_DigitsMand";
	private String MESSAGE_SYMBOLS_MAND="message_SymbolsMand";

	private String PATTERN_LETTERS_MAND="pattern_LettersMand";
	private String PATTERN_CASES_MAND="pattern_CasesMand";
	private String PATTERN_DIGITS_MAND="pattern_DigitsMand";
	private String PATTERN_SYMBOLS_MAND="pattern_SymbolsMand";
	
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        // The value of the component with the validator
		String currentValue = (String) value;
		String patternLetters = (String) component.getAttributes().get(PATTERN_LETTERS_MAND);
		if((patternLetters!=null)&&(!"".equals(patternLetters))){
			if(!currentValue.matches(patternLetters)){
				String messageId = (String) component.getAttributes().get(MESSAGE_LETTERS_MAND);
				String text = MessageResource.getMessageResourceString(context.getApplication().getMessageBundle(), messageId, null,context.getViewRoot().getLocale());
				FacesMessage message = new FacesMessage();
				message.setDetail(text);
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(message);
			}
		}
		
		String patternCases = (String) component.getAttributes().get(PATTERN_CASES_MAND);
		if((patternCases!=null)&&(!"".equals(patternCases))){
			if(!currentValue.matches(patternCases)){
				String messageId = (String) component.getAttributes().get(MESSAGE_CASES_MAND);
				String text = MessageResource.getMessageResourceString(context.getApplication().getMessageBundle(), messageId, null,context.getViewRoot().getLocale());
				FacesMessage message = new FacesMessage();
				message.setDetail(text);
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(message);
			}
		}
		
		String patternDigits = (String) component.getAttributes().get(PATTERN_DIGITS_MAND);
		if((patternDigits!=null)&&(!"".equals(patternDigits))){
			if(!currentValue.matches(patternDigits)){
				String messageId = (String) component.getAttributes().get(MESSAGE_DIGITS_MAND);
				String text = MessageResource.getMessageResourceString(context.getApplication().getMessageBundle(), messageId, null,context.getViewRoot().getLocale());
				FacesMessage message = new FacesMessage();
				message.setDetail(text);
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(message);
			}
		}
		
		String patternSymbols = (String) component.getAttributes().get(PATTERN_SYMBOLS_MAND);
		if((patternSymbols!=null)&&(!"".equals(patternSymbols))){
			if(!currentValue.matches(patternSymbols)){
				String messageId = (String) component.getAttributes().get(MESSAGE_SYMBOLS_MAND);
				String text = MessageResource.getMessageResourceString(context.getApplication().getMessageBundle(), messageId, null,context.getViewRoot().getLocale());
				FacesMessage message = new FacesMessage();
				message.setDetail(text);
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(message);
			}
		}
	 			
        
	}

}
