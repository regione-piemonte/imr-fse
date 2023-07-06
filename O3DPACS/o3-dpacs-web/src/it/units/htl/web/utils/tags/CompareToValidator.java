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

public class CompareToValidator implements Validator {
	
	private String ATTRIBUTE="compareToId";
	private String MESSAGE="message";
	private String CHECKFOR="checkFor";
	private String ENABLED="enabled";
	
	private String CHECKFOR_EQUALITY="equality";
	private String CHECKFOR_DIVERSITY="diversity";
	
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        // The value of the component with the validator
		String currentValue = (String) value;
        
		// The value of the component to compare
        String otherComponentId = (String) component.getAttributes().get(ATTRIBUTE);
        UIInput otherComponent = (UIInput) context.getViewRoot().findComponent(otherComponentId);
        String otherValue = (String) otherComponent.getValue();

        if(otherValue==null)	// The other component was already validated with errors
        	return;
               
        
        String messageId = (String) component.getAttributes().get(MESSAGE);
        String checkFor = (String) component.getAttributes().get(CHECKFOR);
        String enabled = (String) component.getAttributes().get(ENABLED);

        if("0".equals(enabled))		// The validator was disabled
        	return;

        
        if(CHECKFOR_EQUALITY.equals(checkFor)){
        	if (!otherValue.equals(currentValue)) {
        		String text = MessageResource.getMessageResourceString(context.getApplication().getMessageBundle(), messageId, null,context.getViewRoot().getLocale());
    			FacesMessage message = new FacesMessage();
    			message.setDetail(text);
    			message.setSeverity(FacesMessage.SEVERITY_ERROR);
    			throw new ValidatorException(message);
    			
    		}	
        }else if(CHECKFOR_DIVERSITY.equals(checkFor)){
        	if (otherValue.equals(currentValue)) {
        		String text = MessageResource.getMessageResourceString(context.getApplication().getMessageBundle(), messageId, null,context.getViewRoot().getLocale());
    			FacesMessage message = new FacesMessage();
    			message.setDetail(text);
    			message.setSeverity(FacesMessage.SEVERITY_ERROR);
    			throw new ValidatorException(message);
    			
    		}	
        }
        
	}

}
