/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.wado.utils;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Implementation;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.image.ColorModelFactory;
import org.dcm4che2.image.LookupTable;
import org.dcm4che2.image.ShortLookupTable;
import org.dcm4che2.image.VOIUtils;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageWriterSpi;

/**
 * this class provides methods to recode an instance at the specific transfer syntax
 */
public class InstanceCoder {
    private static String destTransSyntax = UID.ExplicitVRLittleEndian;
    private static TransferSyntax destinationSyntax = TransferSyntax.valueOf(destTransSyntax);
    private Log log = LogFactory.getLog(InstanceCoder.class);
    static DicomObject ds;

    public boolean recodeImage(InputStream source, String wadoTempFolder, String sopInstanceUID, TransferSyntax destinationSyntax, HttpServletResponse response, DicomObject substituteData) {
        ImageInputStream iis = null;
        ImageWriter writer =null;
        //boolean closedSequence=false;
        try {
            ImageReader reader = new DicomImageReaderSpi().createReaderInstance();
            writer = new DicomImageWriterSpi().createWriterInstance();
            iis = new MemoryCacheImageInputStream(source);
            reader.setInput(iis);
            writer.setOutput(ImageIO.createImageOutputStream(response.getOutputStream()));
            DicomStreamMetaData streamMeta = (DicomStreamMetaData) reader.getStreamMetadata();
            DicomObject ds = streamMeta.getDicomObject();
            DicomStreamMetaData writeMeta = (DicomStreamMetaData) writer.getDefaultStreamMetadata(null);
            DicomObject newDs = new BasicDicomObject();
            ds.copyTo(newDs);
            substituteData.copyTo(newDs);
            writeMeta.setDicomObject(newDs);
            int frames = ds.getInt(Tag.NumberOfFrames, 1);
            LookupTable lut = prepareBitStrip(writeMeta, reader);
            newDs.putString(Tag.TransferSyntaxUID, VR.UI, destinationSyntax.uid());
            newDs.putString(Tag.ImplementationClassUID, VR.UI, Implementation.classUID());		// Otherwise it looks like dcm4che 
            newDs.putString(Tag.ImplementationVersionName, VR.SH, Implementation.versionName());
            writer.prepareWriteSequence(writeMeta);  
                for (int i = 0; i < frames; i++) {
                    WritableRaster r = (WritableRaster) reader.readRaster(i, null);
                    ColorModel cm = ColorModelFactory.createColorModel(ds);
                    BufferedImage bi = new BufferedImage(cm, r, false, null);
                    if (lut != null) {
                        lut.lookup(bi.getRaster(), bi.getRaster());
                    }
                    IIOImage iioimage = new IIOImage(bi, null, null);
                    writer.writeToSequence(iioimage, null);
                }
            writer.endWriteSequence();
            //closedSequence=true;
        }catch (IIOException iioex) {
            log.error("While reconding the instance:"+iioex.getMessage());
            //if(!closedSequence)
            //	try{writer.endWriteSequence();}catch(Exception ex){log.error("Error closing sequence",ex);}
            return false;
        }catch (Exception e) {
            log.error("While reconding the instance...",e);
            return false;
        } finally {
            if (iis != null) {
                try {
                    iis.close();
                } catch (Exception e) {
                }
            }
        }
        return true;
    }

    private LookupTable prepareBitStrip(DicomStreamMetaData meta,
            ImageReader reader) throws IOException {
        if (!destinationSyntax.uid().equals(UID.JPEGExtended24))
            return null;
        DicomObject ds = meta.getDicomObject();
        int stored = ds.getInt(Tag.BitsStored);
        if (stored < 13)
            return null;
        int frames = ds.getInt(Tag.NumberOfFrames, 1);
        WritableRaster r = (WritableRaster) reader.readRaster(0, null);
        int[] mm = VOIUtils.calcMinMax(ds, r);
        if (frames > 1) {
            r = (WritableRaster) reader.readRaster(frames - 1, null);
            int[] mm2 = VOIUtils.calcMinMax(ds, r);
            mm[0] = Math.min(mm[0], mm2[0]);
            mm[1] = Math.min(mm[1], mm2[1]);
        }
        if (frames > 2) {
            r = (WritableRaster) reader.readRaster(frames / 2 - 1, null);
            int[] mm2 = VOIUtils.calcMinMax(ds, r);
            mm[0] = Math.min(mm[0], mm2[0]);
            mm[1] = Math.min(mm[1], mm2[1]);
        }
        ds.putInt(Tag.SmallestImagePixelValue, VR.IS, mm[0]);
        ds.putInt(Tag.LargestImagePixelValue, VR.IS, mm[1]);
        int maxVal = mm[1];
        if (mm[0] < 0) {
            maxVal = Math.max(maxVal, 1 - mm[0]);
            maxVal *= 2;
        }
        int bits = 0;
        while (maxVal > 0) {
            bits++;
            maxVal >>= 1;
        }
        boolean signed = ds.getInt(Tag.PixelRepresentation) == 1;
        if (bits < 13 && mm[0] >= 0) {
            ds.putInt(Tag.BitsStored, VR.IS, bits);
            ds.putInt(Tag.HighBit, VR.IS, bits - 1);
            ds.putInt(Tag.PixelRepresentation, VR.IS, 0);
            return null;
        }
        ds.putInt(Tag.BitsStored, VR.IS, 12);
        ds.putInt(Tag.HighBit, VR.IS, 11);
        // Number of entries required
        int entries = mm[1] - mm[0] + 1;
        short[] sLut = new short[entries];
        int range = entries - 1;
        for (int i = 0; i < entries; i++) {
            sLut[i] = (short) ((4095 * i) / range);
        }
        if (signed) {
            ds.putInt(Tag.PixelRepresentation, VR.IS, 0);
        }
        LookupTable lut = new ShortLookupTable(stored, signed, -mm[0], 12, sLut);
        return lut;
    }
}
