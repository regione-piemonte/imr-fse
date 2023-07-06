/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.verifier.util;

public class VerificationItem {
    private String studyUid;
    private Integer numOfStudyRelatedInstances;
    private PacsEntity source = new PacsEntity();
    public PacsEntity getSource() {
        return source;
    }
    public void setSource(PacsEntity source) {
        this.source = source;
    }
    public Integer getNumOfStudyRelatedInstances() {
        return numOfStudyRelatedInstances;
    }
    public void setNumOfStudyRelatedInstances(Integer numOfStudyRelatedInstances) {
        this.numOfStudyRelatedInstances = numOfStudyRelatedInstances;
    }
    public String getStudyUid() {
        return studyUid;
    }
    public void setStudyUid(String studyUid) {
        this.studyUid = studyUid;
    }
}
