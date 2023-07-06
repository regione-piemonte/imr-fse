/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import javax.faces.context.FacesContext;

public class selectingObjectBackBean {
	private ObjectListItem _selected = null;

	public void set_selected() {
		_selected = oggetto();
		_selected.setViewedCols(_selected.getColumns());
		_selected.setViewedRows(_selected.getRows());
		context().getExternalContext().getSessionMap().put("currentImageDisplayed", _selected);
	}

	public void removeselected(){
		_selected = null;
		context().getExternalContext().getSessionMap().remove("currentImageDisplayed");
	}
	
	protected FacesContext context() {
		return (FacesContext.getCurrentInstance());
	}

	private ObjectListItem oggetto() {
		ObjectListItem oggetto = (ObjectListItem) context()
				.getExternalContext().getRequestMap().get("item");
		return (oggetto);
	}

}
