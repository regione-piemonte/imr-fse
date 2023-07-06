/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.flex.utils;

public class OperatorStat {
    private String opeCode;
    private Integer numOfseries;
    public String getOpeCode() {
        return opeCode;
    }
    public Integer getNumOfseries() {
        return numOfseries;
    }
    public void setOpeCode(String opeCode) {
        this.opeCode = opeCode;
    }
    public void setNumOfseries(Integer numOfseries) {
        this.numOfseries = numOfseries;
    }
    
}
