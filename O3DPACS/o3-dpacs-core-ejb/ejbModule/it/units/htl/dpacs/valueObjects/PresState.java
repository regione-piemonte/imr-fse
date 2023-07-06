/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.ArrayList;
import java.text.DateFormat;

/**
 * The instance class for presentation states
 * @author Mbe
 */
public class PresState extends Instance {

    // Private Attributes:
    private String presentationLabel = null;
    // From ; Into PresStates.presentationLabel
    private String presentationDescription = null;
    // From ; Into PresStates.presentationDescription
    private Date presentationCreationDate = null;
    // From ; Into PresStates.presentationCreationDate
    private Date presentationCreationDateLate = null;
    // For matching on ranges
    private Time presentationCreationTime = null;
    // From ; Into PresStates.presentationCreationTime
    private Time presentationCreationTimeLate = null;
    // For matching on ranges
    private String presentationCreatorsName = null;
    // From ; Into PresStates.presentationCreatorsName
    private String recommendedViewingMode = null;
    // From ; Into PresStates.recommendedViewingMode
    private List<RefToInstances> rtis = null;
    // The list containing the referenced instances for each series

    public PresState() {
    }

    public PresState(String siu) {
        sopInstanceUid = prepareString(siu, 64);
    }

    public void setPresentationLabel(String pl) {
        presentationLabel = prepareString(pl, 16);
    }

    public void setPresentationDescription(String pd) {
        presentationDescription = prepareString(pd, 64);
    }

    public void setPresentationCreationDate(Date pcd) {
        presentationCreationDate = pcd;
    }

    public void setPresentationCreationDateRange(Date pcdEarly, Date pcdLate) {
        presentationCreationDate = pcdEarly;
        presentationCreationDateLate = pcdLate;
    }

    public void setPresentationCreationTime(Time pct) {
        presentationCreationTime = pct;
    }

    public void setPresentationCreationTimeRange(Time pctEarly, Time pctLate) {
        presentationCreationTime = pctEarly;
        presentationCreationTimeLate = pctLate;
    }

    public void setPresentationCreatorsName(String pcn) {
        presentationCreatorsName = prepareString(pcn, 64);
    }

    public void setRecommendedViewingMode(String rvm) {
        recommendedViewingMode = prepareString(rvm, 16);
    }

    public String getPresentationLabel() {
        return presentationLabel;
    }

    public String getPresentationDescription() {
        return presentationDescription;
    }

    public Date getPresentationCreationDate() {
        return presentationCreationDate;
    }

    ///**
    //@param df It should be got by DateFormat.getDateInstance to have consistent results
    //*/
    public String getPresentationCreationDate(DateFormat df) {
        if (presentationCreationDate == null) {
            return null;
        }
        return (df == null) ? Long.toString(presentationCreationDate.getTime()) : df.format(presentationCreationDate);
    }

    public Date getPresentationCreationDateLate() {
        return presentationCreationDateLate;
    }

    ///**
    //@param df It should be got by DateFormat.getDateInstance to have consistent results
    //*/
    public String getPresentationCreationDateLate(DateFormat df) {
        if (presentationCreationDateLate == null) {
            return null;
        }
        return (df == null) ? Long.toString(presentationCreationDateLate.getTime()) : df.format(presentationCreationDateLate);
    }

    public Time getPresentationCreationTime() {
        return presentationCreationTime;
    }

    ///**
    //@param tf It should be got by DateFormat.getTimeInstance to have consistent results
    //*/
    public String getPresentationCreationTime(DateFormat tf) {
        if (presentationCreationTime == null) {
            return null;
        }
        return (tf == null) ? Long.toString(presentationCreationTime.getTime()) : tf.format(presentationCreationTime);
    }

    public Time getPresentationCreationTimeLate() {
        return presentationCreationTimeLate;
    }

    public String getPresentationCreationTimeLate(DateFormat tf) {
        if (presentationCreationTimeLate == null) {
            return null;
        }
        return (tf == null) ? Long.toString(presentationCreationTimeLate.getTime()) : tf.format(presentationCreationTimeLate);
    }

    public String getPresentationCreatorsName() {
        return presentationCreatorsName;
    }

    public String getRecommendedViewingMode() {
        return recommendedViewingMode;
    }

    public void addReferencedSeries(RefToInstances r) {
        if (rtis == null) {
            rtis = new ArrayList<RefToInstances>(3);
        }
        // 3 series referenced by one pres state should be enough!
        rtis.add(r);
    }

    public RefToInstances[] getReferencedSeries() {
        if (rtis == null) {
            return null;
        }
        int s = rtis.size();
        RefToInstances[] temp = new RefToInstances[s];
        rtis.toArray(temp);
        // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }

    public void reset() {
        presentationLabel = null;
        presentationDescription = null;
        presentationCreationDate = null;
        presentationCreationDateLate = null;
        presentationCreationTime = null;
        presentationCreationTimeLate = null;
        presentationCreatorsName = null;
        recommendedViewingMode = null;
        rtis = null;
    }

    public static void main(String[] args) {
    }
}