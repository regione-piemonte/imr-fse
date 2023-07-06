/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.utils;

import it.units.htl.dpacs.dao.DicomQueryDealerRemote;
import it.units.htl.dpacs.valueObjects.DicomMatch;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.dpacs.valueObjects.Series;
import it.units.htl.dpacs.valueObjects.Study;
import it.units.htl.web.Study.ObjectList;
import it.units.htl.web.Study.PacsConnector;
import it.units.htl.web.Study.SerieList;
import it.units.htl.web.Study.SerieListItem;
import it.units.htl.web.Study.SeriesResultsBackBean;
import it.units.htl.web.Study.StudyList;
import it.units.htl.web.Study.StudyListItem;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BuildStudyResult {
    private static DicomMatch[] dicomMatches;
    private Log log = LogFactory.getLog(this.getClass());

    public BuildStudyResult(){       
    }
    
    
    public boolean getSeries(String fieldToUse, String filterValue, HttpSession ss) {
        try {
            Study s = new Study("");
            Method methodToCall=Study.class.getDeclaredMethod("set"+fieldToUse, String.class);
            methodToCall.invoke(s, filterValue);
//            if ("accessionNumber".equals(whoFilter)) {
//                s.setAccessionNumber(filterValue);
//            } else if ("studyInstanceUID".equals(whoFilter)) {
//                s.setStudyInstanceUid(filterValue);
//            } else {
//                log.error(whoFilter + ": Not a valid filter");
//                throw new Exception(whoFilter + ": Check the configuration of filter field!");
//            }
            SerieList sl = new SerieList();
            // Execute Query
            PacsConnector pacsConnection = new PacsConnector();
            DicomQueryDealerRemote queryDealerRemote = pacsConnection.getQueryDealer();
            dicomMatches = queryDealerRemote.studyRootMatch(new Patient(), s, new Series(), "");
            for (int i = 0; i < dicomMatches.length - 1; i++) {
                SerieListItem serieListItem = new SerieListItem();
                serieListItem.setSerieInstanceUid(dicomMatches[i].series.getSeriesInstanceUid());
                serieListItem.setSerieDate(dicomMatches[i].study.getStudyDate());
                serieListItem.setSerieTime(dicomMatches[i].study.getStudyTime());
                serieListItem.setModality(dicomMatches[i].series.getModality());
                serieListItem.setSeriesDescription(dicomMatches[i].series.getSeriesDescription());
                serieListItem.setSerieStatus(dicomMatches[i].study.getStudyStatus());
                serieListItem.setSeriesNumber(dicomMatches[i].series.getSeriesNumber());
                serieListItem.setNumberOfSeriesRelatedInstances(dicomMatches[i].series.getNumberOfSeriesRelatedInstances());
                sl.add(serieListItem);
            }
            pacsConnection.closeConnection();
            ss.setAttribute("seriesResultsBackBean", new SeriesResultsBackBean());
            ss.setAttribute("objectList", new ObjectList());
            ss.setAttribute("serieList", sl);
        } catch (Exception e) {
            log.error("WebViewer says:", e);
            return false;
        }
        return true;
    }

    public void buildResults(String accNumber, String patientId) {
        PacsConnector pacsConnection = new PacsConnector();
        DicomQueryDealerRemote queryDealerRemote = pacsConnection.getQueryDealer();

        Study s = new Study("");

        s.setAccessionNumber(accNumber);
        dicomMatches = null;
        try {
            dicomMatches = queryDealerRemote.studyRootMatch(new Patient(), s, 500, "o3-dpacs-web", false);
        } catch (RemoteException e) {
            log.error("When searching study from UID", e);
        }
        for (int i = 0; i < dicomMatches.length - 1; i++) {
            StudyListItem studyListItem = new StudyListItem();
            studyListItem.setStudyDate(dicomMatches[i].study.getStudyDate());
            studyListItem.setStudyTime(dicomMatches[i].study.getStudyTime());
            studyListItem.setModalitiesInStudy(dicomMatches[i].study.getModalitiesInStudy());
            studyListItem.setStudyDescription(dicomMatches[i].study.getStudyDescription());
            studyListItem.setStudyStatus(dicomMatches[i].study.getStudyStatus());
            studyListItem.setFirstName(dicomMatches[i].patient.getFirstName());
            studyListItem.setLastName(dicomMatches[i].patient.getLastName());
            studyListItem.setStudyInstanceUid(dicomMatches[i].study.getStudyInstanceUid());
            studyListItem.setPatientId(dicomMatches[i].patient.getPatientId());
            studyListItem.setBirthDate(dicomMatches[i].patient.getBirthDate());
            studyListItem.setNumberOfStudyRelatedInstances(dicomMatches[i].study.getNumberOfStudyRelatedInstances());
            studyList().add(studyListItem);

        }

        pacsConnection.closeConnection();
    }

    private static SerieList serieList() {
        FacesContext context = context();
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        ValueExpression ve = context.getApplication().getExpressionFactory().createValueExpression(elContext, "#{serieList}", Object.class);
        return ((SerieList) ve.getValue(elContext));
    }

    private static StudyList studyList() {
        FacesContext context = context();
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        ValueExpression ve = context.getApplication().getExpressionFactory().createValueExpression(elContext, "#{studyList}", Object.class);
        return ((StudyList) ve.getValue(elContext));
    }

    protected static FacesContext context() {
        return (FacesContext.getCurrentInstance());
    }
}
