/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.utils;

public class StudiesStatistic {
    
    private Integer unregStudiesCounter = 20;
    private Integer unverifiedStudiesCounter = 25;
    private Integer studiesToVerifyCounter = 50;
    private Integer receivedStudiesCounter = 100;
    private Integer successStudies = null;

    public Integer getSuccessStudies() {
        return successStudies;
    }

    public void setSuccessStudies(Integer successStudies) {
        this.successStudies = successStudies;
    }

    public Integer getUnregStudiesCounter() {
        return unregStudiesCounter;
    }

    public Integer getUnverifiedStudiesCounter() {
        return unverifiedStudiesCounter;
    }

    public Integer getStudiesToVerifyCounter() {
        return studiesToVerifyCounter;
    }

    public Integer getReceivedStudiesCounter() {
        return receivedStudiesCounter;
    }

    public void setUnregStudiesCounter(Integer unregStudiesCounter) {
        this.unregStudiesCounter = unregStudiesCounter;
    }

    public void setUnverifiedStudiesCounter(Integer unverifiedStudiesCounter) {
        this.unverifiedStudiesCounter = unverifiedStudiesCounter;
    }

    public void setStudiesToVerifyCounter(Integer studiesToVerifyCounter) {
        this.studiesToVerifyCounter = studiesToVerifyCounter;
    }

    public void setReceivedStudiesCounter(Integer receivedStudiesCounter) {
        this.receivedStudiesCounter = receivedStudiesCounter;
    }
}
