/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.Serializable;

public class StudyAvailabilityPojo implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String studyUID;
    public boolean completed;
    public boolean published;
//    in or out the wl
    public boolean toReconcile;
}
