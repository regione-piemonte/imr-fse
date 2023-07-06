/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.utils;

import java.util.Date;

public class OperatorDetail {
    private String studyUID;
    private String seriesUID;
    private String accessionNumber;
    private String modality;
    private Integer numberOfImages;
    private Date studyDate;
    private String aeTitle;
    
    public String getAeTitle() {
        return aeTitle;
    }
    public void setAeTitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }
    public String getSeriesUID() {
        return seriesUID;
    }
    public void setSeriesUID(String seriesUID) {
        this.seriesUID = seriesUID;
    }   

    public String getStudyUID() {
        return studyUID;
    }
    public String getAccessionNumber() {
        return accessionNumber;
    }
    public String getModality() {
        return modality;
    }
    public Integer getNumberOfImages() {
        return numberOfImages;
    }
    public Date getStudyDate() {
        return studyDate;
    }
    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }
    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }
    public void setModality(String modality) {
        this.modality = modality;
    }
    public void setNumberOfImages(Integer numberOfImages) {
        this.numberOfImages = numberOfImages;
    }
    public void setStudyDate(Date studyDate) {
        this.studyDate = studyDate;
    }
    
    
}
