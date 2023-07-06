/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.utils;

import java.util.Date;

public class StudyVerificationItem {
    private String studyUID = "";
    private Integer sourcePacsFk = null;
    private Date lastChangDate = null;
    private Integer studyStatus = null;
    private Date lastTryToChangeStatus = null;
    private String lastErrorMessage = null;
    private Boolean toBeIgnored = null;
    private String accessionNumber = null;
    private String sourceAeTitle = null;
    
    public String getSourceAeTitle() {
        return sourceAeTitle;
    }

    public void setSourceAeTitle(String sourceAeTitle) {
        this.sourceAeTitle = sourceAeTitle;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public Boolean getToBeIgnored() {
        return toBeIgnored;
    }

    public void setToBeIgnored(Boolean toBeIgnored) {
        this.toBeIgnored = toBeIgnored;
    }

    public Date getLastInsDate() {
        return lastChangDate;
    }

    public void setLastInsDate(Date lastChangDate) {
        this.lastChangDate = lastChangDate;
    }

    public Integer getSourcePacsFk() {
        return sourcePacsFk;
    }

    public void setSourcePacsFk(Integer sourcePacsFk) {
        this.sourcePacsFk = sourcePacsFk;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public Integer getStudyStatus() {
        return studyStatus;
    }

    public Date getLastTryToChangeStatus() {
        return lastTryToChangeStatus;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    public void setStudyStatus(Integer studyStatus) {
        this.studyStatus = studyStatus;
    }

    public void setLastTryToChangeStatus(Date lastTryToChangeStatus) {
        this.lastTryToChangeStatus = lastTryToChangeStatus;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }
}
