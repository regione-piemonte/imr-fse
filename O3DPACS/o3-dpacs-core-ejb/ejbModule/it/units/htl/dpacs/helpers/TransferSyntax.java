/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import org.dcm4che.dict.UIDs;

/**
 *
 * @author Carrara
 */
public class TransferSyntax {
    
    private String ts;
    private String name;
    
    public static TransferSyntax JpegLossless = new TransferSyntax(UIDs.JPEGLossless);
    public static TransferSyntax Jpeg2000Lossless = new TransferSyntax(UIDs.JPEG2000Lossless);
    public static TransferSyntax ImplicitVRLittleEndian = new TransferSyntax(UIDs.ImplicitVRLittleEndian);
    public static TransferSyntax ExplicitVRLittleEndian = new TransferSyntax(UIDs.ExplicitVRLittleEndian);
    public static TransferSyntax ExplicitVRBigEndian = new TransferSyntax(UIDs.ExplicitVRBigEndian);
    public static TransferSyntax JPEGBaseline = new TransferSyntax(UIDs.JPEGBaseline);
    
    private TransferSyntax(String ts) { 
        this.ts = ts;
        this.name = "Unrecognized Transfer Syntax";
        if (ts.equalsIgnoreCase(UIDs.JPEGLossless)) this.name = "Jpeg Lossless";
        if (ts.equalsIgnoreCase(UIDs.JPEG2000Lossless)) this.name = "Jpeg 2000 Lossless";
        if (ts.equalsIgnoreCase(UIDs.ImplicitVRLittleEndian)) this.name = "Implicit VR, Little Endian";
        if (ts.equalsIgnoreCase(UIDs.ExplicitVRLittleEndian)) this.name = "Explicit VR, Little Endian";
        if (ts.equalsIgnoreCase(UIDs.ExplicitVRBigEndian)) this.name = "Explicit VR, Big Endian";
        if (ts.equalsIgnoreCase(UIDs.JPEGBaseline)) this.name = "Jpeg Baseline Lossy";
    }
    
    @Override
    public String toString() {
        return name + " - " + ts;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return ts;
    }
    
    public static TransferSyntax getTransferSyntaxByName(String tsyn) {
        if (tsyn.equalsIgnoreCase(UIDs.JPEGLossless)) return JpegLossless;
        if (tsyn.equalsIgnoreCase(UIDs.JPEG2000Lossless)) return Jpeg2000Lossless;
        if (tsyn.equalsIgnoreCase(UIDs.ExplicitVRBigEndian)) return ExplicitVRBigEndian;
        if (tsyn.equalsIgnoreCase(UIDs.ExplicitVRLittleEndian)) return ExplicitVRLittleEndian;
        if (tsyn.equalsIgnoreCase(UIDs.ImplicitVRLittleEndian)) return ImplicitVRLittleEndian;
        if (tsyn.equalsIgnoreCase(UIDs.JPEGBaseline)) return JPEGBaseline;
        return new TransferSyntax(tsyn);
    }

    public boolean equals(TransferSyntax ts2) {
        if (this.getValue().equalsIgnoreCase(ts2.getValue()))
        return true;
        else return false;
    }
    
}
