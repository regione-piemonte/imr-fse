/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.kos;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Iterator;

import it.units.htl.maps.Series;
import it.units.htl.maps.Studies;
import it.units.htl.maps.util.SessionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.hibernate.Session;

public class DataValidator {
    final Log log = LogFactory.getLog(DataValidator.class);

    public static enum ERROR_TYPE {
        SERIES_NOT_OF_STUDY, INSTANCE_NOT_OF_SERIES, TOO_MANY_REFERENCED_STUDIES
    };

    private HashMap<String, Series> seriesDataToCheck = new HashMap<String, Series>();

    public HashMap<String, Series> getSeries() {
        return seriesDataToCheck;
    }

    @SuppressWarnings("unchecked")
    public ERROR_TYPE checkKosData(DcmElement studies) {
        if (studies.length() > 1) {
            return ERROR_TYPE.TOO_MANY_REFERENCED_STUDIES;
        }
        // this dataset contains the data of the study presents in the kos file
        Dataset refStudy = studies.getItem(0);
        // this is his studyUID
        String refStudyUID = refStudy.getString(Tags.StudyInstanceUID);
        // Create a new session for hibernate
        Session s = SessionManager.getInstance().openSession();
        Studies studyInformation = (Studies) s.get(Studies.class, refStudyUID);
        Iterator<Series> seriesInformation = studyInformation.getSeries().iterator();
        // have to build the data of the series
        while (seriesInformation.hasNext()) {
            Series seriesOnDB = seriesInformation.next();
            seriesDataToCheck.put(seriesOnDB.getSeriesInstanceUid(), seriesOnDB);
        }
        log.debug("The kos has this study: " + refStudy.getString(Tags.StudyInstanceUID));
        // this element contains the data of the series included in the kos file
        DcmElement dicomSeriesRefByStudy = refStudy.get(Tags.RefSeriesSeq);
        // have to check every single series
        for (int i = 0; i < dicomSeriesRefByStudy.countItems(); i++) {
            // the dataset of the series to check
            Dataset seriesDataset = dicomSeriesRefByStudy.getItem(i);
            log.debug("Previous study has this series: " + seriesDataset.getString(Tags.SeriesInstanceUID));
            // the UID of this series
            String seriesToCheck = seriesDataset.getString(Tags.SeriesInstanceUID);
            if (!seriesDataToCheck.keySet().contains(seriesToCheck)) {
                log.error("This series doesn't belong to the refered study");
                return ERROR_TYPE.SERIES_NOT_OF_STUDY;
            }
            // this is the data present in the db
            ArrayList<String> sopInstancesOfSeries = (ArrayList<String>) s.createSQLQuery("select sopInstanceUID from" + " (SELECT sopInstanceUID, seriesFK from Images" + "  union all SELECT sopInstanceUID, seriesFK from NonImages" + "  union all SELECT sopInstanceUID, seriesFK from PresStates" + "  union all SELECT sopInstanceUID, seriesFK from Overlays" + "  union all SELECT sopInstanceUID, seriesFK from StructReps)ins where seriesFK = '" + seriesToCheck + "'").list();
            // this is the data present in the kos file
            DcmElement dicomObjectRefByseries = seriesDataset.get(Tags.RefSOPSeq);
            for (int numRefSop = 0; numRefSop < dicomObjectRefByseries.countItems(); numRefSop++) {
                Dataset instanceDataset = dicomObjectRefByseries.getItem(numRefSop);
                if (!sopInstancesOfSeries.contains(instanceDataset.getString(Tags.RefSOPInstanceUID))) {
                    log.error("This instance doesn't belong to refered series!");
                    return ERROR_TYPE.INSTANCE_NOT_OF_SERIES;
                }
            }
        }
        s.close();
        return null;
    }
}
