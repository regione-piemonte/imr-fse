/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.units.htl.web;

/**
 *
 * @author Giorgio Faustini
 */
public class DataDescriptor {
    
    private String studyUID=null;
    private String seriesUID=null;
    private String instanceUID=null;
    private String table=null;
    
    /** Creates a new instance of DataDescriptor */
    public DataDescriptor() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

    public String getStudyUID() {
        return studyUID;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    public String getSeriesUID() {
        return seriesUID;
    }

    public void setSeriesUID(String seriesUID) {
        this.seriesUID = seriesUID;
    }

    public String getInstanceUID() {
        return instanceUID;
    }

    public void setInstanceUID(String instanceUID) {
        this.instanceUID = instanceUID;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
    
}
