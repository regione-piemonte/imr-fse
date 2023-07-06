/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.kos.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import it.units.htl.dpacs.helpers.LogMessage;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;

public class KosStorer {
    final Log log = LogFactory.getLog(KosStorer.class);

    private enum DICOM_LEVEL {
        study, series, instance
    };

    private String currStudy = null;
    private String currSeries = null;
    private String kosSopInstance = null;

    public KosStorer() {
    }

    public boolean storeKos(DicomObject source) {
        Connection myConn = null;
        CallableStatement cs = null;
        try {
            myConn = getConnection();
            kosSopInstance = source.getString(Tag.SOPInstanceUID);
            // Remove old reference
            cleanRelationship(myConn, kosSopInstance);
            // insert the parent of the filtered study (the Kos File)
            cs = myConn.prepareCall("{call insertKosRelationship(?,?)}");
            cs.setString(1, kosSopInstance);
            cs.setNull(2, Types.VARCHAR);
            cs.execute();
            for (int i = 0; i < source.get(Tag.CurrentRequestedProcedureEvidenceSequence).countItems(); i++) {
                insertKosRelationship(cs, source.get(Tag.CurrentRequestedProcedureEvidenceSequence).getDicomObject(i), DICOM_LEVEL.study);
            }
            return true;
        } catch (Exception e) {
            log.error("", e);
        } finally {
            try {
                myConn.close();
            } catch (SQLException e) {
            }
        }
        return false;
    }

    private void cleanRelationship(Connection myCon, String kosSopInstance) throws SQLException {
        CallableStatement csErase = myCon.prepareCall("{call removeKosRelationship(?)}");
        csErase.setString(1, kosSopInstance);
        csErase.execute();
    }

    private void insertKosRelationship(CallableStatement cs, DicomObject source, DICOM_LEVEL l) throws SQLException {
        switch (l) {
        case study:
            DicomElement series = source.get(Tag.ReferencedSeriesSequence);
            currStudy = source.getString(Tag.StudyInstanceUID);
            cs.setString(1, currStudy);
            cs.setString(2, kosSopInstance);
            cs.execute();
            for (int i = 0; i < series.countItems(); i++) {
                insertKosRelationship(cs, series.getDicomObject(i), DICOM_LEVEL.series);
            }
            break;
        case series:
            DicomElement instances = source.get(Tag.ReferencedSOPSequence);
            currSeries = source.getString(Tag.SeriesInstanceUID);
            cs.setString(1, currSeries);
            cs.setString(2, currStudy);
            cs.execute();
            for (int i = 0; i < instances.countItems(); i++) {
                insertKosRelationship(cs, instances.getDicomObject(i), DICOM_LEVEL.instance);
            }
            break;
        case instance:
            if (UID.KeyObjectSelectionDocumentStorage.equals(source.getString(Tag.ReferencedSOPClassUID))) {
                String sopInstance = source.getString(Tag.ReferencedSOPInstanceUID);
                cs.setString(1, sopInstance);
                cs.setString(2, currSeries);
                cs.execute();
            }
            break;
        }
    }

    private Connection getConnection() {
        DataSource myDs = null;
        try {
            Context jndiContext = new InitialContext();
            myDs = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
            if (myDs != null) {
                return myDs.getConnection();
            } else {
                return null;
            }
        } catch (NamingException nex) {
            log.fatal(LogMessage._NoDatasource, nex);
        } catch (SQLException sex) {
            log.fatal(LogMessage._CouldntGet + " DB connection.", sex);
        }
        return null;
    }
}
