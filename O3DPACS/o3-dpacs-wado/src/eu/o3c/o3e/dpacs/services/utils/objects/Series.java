/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.dpacs.services.utils.objects;

public class Series {
    private String seriesInstanceUid;
    private String modality;
    private String numberOfInstances;
    private String description;
    private String thumbnail;

    private String path;
    private String studyStatus;
    
    
    public Series() {
    }

    public String getSeriesInstanceUid() {
        return seriesInstanceUid;
    }

    public void setSeriesInstanceUid(String seriesInstanceUid) {
        this.seriesInstanceUid = seriesInstanceUid;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(String numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setStudyStatus(String val) {
        studyStatus = val;
        
    }

    public String getPath() {
        return path;
    }

    public String getStudyStatus() {
        return studyStatus;
    }

    public void setPath(String path) {
        this.path = path;
    }
}