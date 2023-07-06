/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.ui.messaging;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpSession;


public class MessageManager {
	
	private String currentMessage = "";
	
	public static MessageManager getInstance(){
		return (MessageManager)((HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getAttribute("messageManager");
	}
	public static MessageManager getInstance(HttpSession session){
        return (MessageManager)session.getAttribute("messageManager");
    }
	
	
	public void resetMessage(){
		currentMessage = "";
	}
	
	public void resetMessage(PhaseEvent e) {
		if (e.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			currentMessage = "";
		}
	}
	
	
	public String getMessage(){
		return currentMessage;
	}
	
	public void setMessage(String plainMessage){
		currentMessage = plainMessage;
	}

	public void setMessage(String resourceName, String[] resourceParam){
		currentMessage = getLocalizedMessage(resourceName, resourceParam);
	}
	
	public String getLocalizedMessage(String resourceName, String[] param) {
		FacesContext context = FacesContext.getCurrentInstance();
		String text = MessageResource.getMessageResourceString(context
				.getApplication().getMessageBundle(), resourceName, param,
				context.getViewRoot().getLocale());

		return text;
	}
	
	
}
