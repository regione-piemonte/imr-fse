/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier.bean;

import it.units.htl.dpacs.postprocessing.verifier.KosMetaInfo;
import it.units.htl.dpacs.postprocessing.verifier.util.PacsEntity;

import java.io.File;
import java.util.Map;

import javax.ejb.Local;


@Local

public interface StudiesVerifierToolsLocal {
    public boolean verifyStudy(String studyInstanceUID,Integer numberOfStoredInstance, PacsEntity sourcePacs, Integer timeout) throws Exception;
    public boolean validateStudy(String mandatoryAttribute, String studyUID, String _getFolderBy) throws Exception;
    public boolean createKosForStudy(KosMetaInfo kosMetaInfo, String kosTempUrl, String studyUID) throws Exception;
    public boolean sendKosToXDS(File kos, Map<String, KosMetaInfo> kosMetaInfoMap,String getFolderBy,String xdsPrefix, Integer maxConnectionPerHost) throws Exception;
    public void insertVerificationEvent(String studyUID, Integer eventType, String eventDescriprion);
    public boolean isStudyPublished(String studyUID);
}
