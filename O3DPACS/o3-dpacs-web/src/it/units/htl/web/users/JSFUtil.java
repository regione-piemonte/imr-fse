/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.users;

import java.util.Map;

import javax.faces.context.FacesContext;

/**
 *
 * @author francesco.feront
 */

public class JSFUtil {
    
  /*public static ValueBinding getValueBinding(String expression)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    return context.getApplication().createValueBinding(expression);
  }*/
   
  /*public static String getValueBindingString(String expression)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ValueBinding currentBinding =  context.getApplication().createValueBinding(expression);
    return (String) currentBinding.getValue(context);
   
  }*/
    
    public static Object getManagedObject(String objectName) {
        FacesContext context = FacesContext.getCurrentInstance();
        @SuppressWarnings("deprecation")
        Object requestedObject =  context.getApplication().getVariableResolver().resolveVariable(context, objectName);
        return  requestedObject;
    }
    
    public static void storeOnSession(FacesContext ctx, String key, Object object) {
        Map<String, Object> sessionState = ctx.getExternalContext().getSessionMap();
        sessionState.put(key, object);
    }
}