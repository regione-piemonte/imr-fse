/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.dpacs.services.utils.objects;

import java.util.ArrayList;
import java.util.Date;

public class Study {
    public static final char DPACS_OPEN_STATUS = 'o';
    public static final char DPACS_NEARLINE_STATUS = 'n';
    private Date date;
    private Date time;
    private String description;
    private String accessionNumber;
    private String uid;
    private String stuydStatus;

    private ArrayList<Series> series = new ArrayList<Series>();
    
    public ArrayList<Series> getSeries() {
        return series;
    }

    public void setSeries(ArrayList<Series> series) {
        this.series = series;
    }

    public Study() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

	public String getStuydStatus() {
		return stuydStatus;
	}

	public void setStuydStatus(String stuydStatus) {
		this.stuydStatus = stuydStatus;
	}
}