/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.GlobalSettings.PartitioningStrategy;
import it.units.htl.dpacs.helpers.RoiMeasure;
import it.units.htl.dpacs.servers.storage.StorageSCP;
import it.units.htl.dpacs.valueObjects.Instance;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.dpacs.valueObjects.Series;
import it.units.htl.dpacs.valueObjects.Study;

import java.io.File;
import java.util.Vector;

import javax.ejb.Local;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.net.ActiveAssociation;

@Local
public interface DicomStorageDealerLocal {
    // No Exceptions need to be thrown, except for application ones
    public String verifyToStore(Patient p, Study s, Series se, Instance i, String[] params, StorageSCP scp, ActiveAssociation assoc, String defaultIdIssuer, PartitioningStrategy strategy) throws CannotStoreException;

    public int insertInstance(Patient p, Study st, Series se, Instance inst, ActiveAssociation assoc) throws CannotUpdateException;

    public int isInstancePresent(String u, String s, String table, ActiveAssociation assoc);

    public File storeToMedia(Dataset data, Study study, String url, DcmParser parser, DcmEncodeParam decParam, StorageSCP scp, String forcedTs, boolean isImage, ActiveAssociation assoc, Vector<RoiMeasure> total, String param) throws StorageException;

    public boolean updateForwardSchedule(String studyUid, String callingAeTitle);

    public boolean isStudyDeleted(String studyUid, String callingAeTitle);

    public boolean isStudyEditable(String studyInstanceUid, String callingAeTitle);

    public boolean insertStudyVerificationData(String sourceAeTitle, String studyInstanceUid);

    public long completeOldStudies(String insertedBy, int minutes);

    public int addStudyTracking(String studyInstanceUid, String insertedBy);

    public int completeStudy(String studyInstanceUid, String insertedBy);

    public String findStudyForSeries(String[] seriesInstanceUids, int numberOfStudyRelatedInstances);
    
    public boolean stopProcessingInstance(String uid);

    public void updateStudiesAvailability(String studyInstanceUID);
    
    
} // end interface
