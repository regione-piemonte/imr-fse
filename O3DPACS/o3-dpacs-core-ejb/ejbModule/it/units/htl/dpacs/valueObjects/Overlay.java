/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

/**
 * The class is the instance class for Overlays
 * @author Mbe
 */
public class Overlay extends Instance {

    // Private Attributes:
    private String overlayNumber = null; /*long*/
    // From ; Into Overlays.overlayNumber
    private String overlayRows = null; /*int*/
    // From ; Into Overlays.overlayRows
    private String overlayColumns = null; /*int*/
    // From ; Into Overlays.overlayColumns
    private String overlayType = null;
    // From ; Into Overlays.overlayType
    private String overlayBitsAllocated = null; /*int*/
    // From ; Into Overlays.overlayBitsAllocated
    private RefToInstances rti = null;
    // Constructors:

    public Overlay() {
    }

    public Overlay(String siu) {
        sopInstanceUid = prepareString(siu, 64);
    }
    // Accessor Methods:

    public void setOverlayNumber(String oln) {
        overlayNumber = prepareLong(oln);
    }

    public void setOverlayRows(String olr) {
        overlayRows = prepareInt(olr);
    }

    public void setOverlayColumns(String olc) {
        overlayColumns = prepareInt(olc);
    }

    public void setOverlayType(String olt) {
        overlayType = prepareString(olt, 16);
    }

    public void setOverlayBitsAllocated(String olba) {
        overlayBitsAllocated = prepareInt(olba);
    }

    public String getOverlayNumber() {
        return overlayNumber;
    }

    public String getOverlayRows() {
        return overlayRows;
    }

    public String getOverlayColumns() {
        return overlayColumns;
    }

    public String getOverlayType() {
        return overlayType;
    }

    public String getOverlayBitsAllocated() {
        return overlayBitsAllocated;
    }

    public void setReferencedInstances(RefToInstances r) {
        rti = r;
    }

    public RefToInstances getReferencedInstances() {
        return rti;
    }

    public void reset() {
        overlayNumber = null;
        overlayRows = null;
        overlayColumns = null;
        overlayType = null;
        overlayBitsAllocated = null;
        RefToInstances rti = null;
    }

    public static void main(String[] args) {
    }
    // end main
}