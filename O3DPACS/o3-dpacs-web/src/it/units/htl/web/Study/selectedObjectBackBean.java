/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import javax.faces.context.FacesContext;


public class selectedObjectBackBean {
	public ObjectListItem get_selected() {
		return (ObjectListItem) FacesContext.getCurrentInstance()
		.getExternalContext().getSessionMap().get("currentImageDisplayed");
	}
}