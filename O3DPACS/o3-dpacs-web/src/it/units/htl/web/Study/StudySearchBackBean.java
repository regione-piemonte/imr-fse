/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

/**
 * @author Sara
 */
public class StudySearchBackBean {
    public String studyView() {
        studyList().findStudies();
        return "view";
    }

    private StudyList studyList() {
        FacesContext context = context();
        ELContext elContext = FacesContext.getCurrentInstance()
                .getELContext();
        ValueExpression ve =
                context.getApplication().getExpressionFactory()
                .createValueExpression(elContext, "#{studyList}", Object.class);
        return ((StudyList) ve.getValue(elContext));
    }

    protected FacesContext context() {
        return (FacesContext.getCurrentInstance());
    }

    public void resetFormAL(javax.faces.event.ActionEvent event) {
        StudyList sl = studyList();
        sl.setFirstName("");
        sl.setLastName("");
        sl.setBirthDate(null);
        sl.setPatientId("");
        sl.setStudyDate(null);
        sl.setAccessNumber("");
        sl.setModalitiesInStudy("");
        sl.setStudyDescription("");
        sl.setStudyId("");
        sl.setStudyInstanceUID("");
    }
}
