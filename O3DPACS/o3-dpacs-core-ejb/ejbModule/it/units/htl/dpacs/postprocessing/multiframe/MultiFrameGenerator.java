/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.multiframe;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
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

public class MultiFrameGenerator {

	private static Log log = LogFactory.getLog(MultiFrameGenerator.class);
	private static String tsuid = UID.ImplicitVRLittleEndian;
	private static TransferSyntax destinationSyntax = TransferSyntax.valueOf(tsuid);
	static DicomObject ds;

	protected MultiFrameGenerator() {
	}

	public String getMfModality(File dcmFile) throws IOException {
		ImageReader reader=null;
		FileImageInputStream input=null;
		String attrString = null;
		try{
			
			reader = new DicomImageReaderSpi().createReaderInstance();
			input = new FileImageInputStream(dcmFile);
			reader.setInput(input);
			DicomStreamMetaData streamMeta = (DicomStreamMetaData) reader.getStreamMetadata();
			ds = streamMeta.getDicomObject();
			attrString = ds.getString(Tag.Modality);
		}catch(Exception ex){
			attrString=null;
			throw new IOException("Could not retrieve Modality Tag", ex);
		}finally{
			if(input!=null)
				try{input.close();}catch(Exception ex){}
			if(reader!=null)
				try{reader.dispose();}catch(Exception ex){}
		}
		return attrString;
	} // End getMfModality()

	public String getMfSopClass(File dcmFile) throws IOException {
		ImageReader reader=null;
		FileImageInputStream input=null;
		String attrString = null;
		try{
			reader = new DicomImageReaderSpi().createReaderInstance();
			input = new FileImageInputStream(dcmFile);
			reader.setInput(input);
			DicomStreamMetaData streamMeta = (DicomStreamMetaData) reader.getStreamMetadata();
			ds = streamMeta.getDicomObject();
			attrString = ds.getString(Tag.SOPClassUID);
		}catch(Exception ex){
			attrString=null;
			throw new IOException("Could not retrieve SOP Class UID Tag", ex);
		}finally{
			if(input!=null)
				try{input.close();}catch(Exception ex){}
			if(reader!=null)
				try{reader.dispose();}catch(Exception ex){}
		}
		return attrString;
	} // End getMfSopClass()

	public void recodeImages(File[] src, File dest, String sopInstanceUid, String seriesInstanceUid, String frameIncrementPointer, Float frameTime) throws IOException {

		int instCount = src.length;
		if (dest.exists())
			dest.delete();
		ImageWriter writer = null;
		FileImageOutputStream output=null;
		try{
			writer=new DicomImageWriterSpi().createWriterInstance();
			output = new FileImageOutputStream(dest);
			writer.setOutput(output);
			boolean first = true;
			log.info("Reading frame images...");
			ImageReader reader=null;
			FileImageInputStream input=null;
			try{
				for (File f : src) {
					reader = new DicomImageReaderSpi().createReaderInstance();
					input = new FileImageInputStream(f);
					reader.setInput(input);
		
					DicomStreamMetaData writeMeta = (DicomStreamMetaData) writer.getDefaultStreamMetadata(null);
		
					if (first) {
		
						log.info("Writing output image metadata...");
						DicomStreamMetaData streamMeta = (DicomStreamMetaData) reader.getStreamMetadata();
						ds = streamMeta.getDicomObject();
						destinationSyntax = TransferSyntax.valueOf(ds.getString(Tag.TransferSyntaxUID));
		
						this.insertMetaData(ds, instCount, frameTime, sopInstanceUid, seriesInstanceUid);
						DicomObject newDs = new BasicDicomObject();
						ds.copyTo(newDs);
						writeMeta.setDicomObject(newDs);
		
						writer.prepareWriteSequence(writeMeta);
						first = false;
						log.info("Done.");
						log.info("Loading frames instances...");
					}
					LookupTable lut = prepareBitStrip(writeMeta, reader);
					WritableRaster r = (WritableRaster) reader.readRaster(0, null);
					ColorModel cm = ColorModelFactory.createColorModel(ds);
					BufferedImage bi = new BufferedImage(cm, r, false, null);
					if (lut != null) {
						log.info("Lookup operation started...");
						lut.lookup(bi.getRaster(), bi.getRaster());
						log.info("done.");
					}
					IIOImage iioimage = new IIOImage(bi, null, null);
					writer.writeToSequence(iioimage, null);
					reader.dispose();
					input.close();
				}
			}catch(Exception ex){
				log.error("Error creating MF", ex);
				throw new IOException(ex);
			}finally{
				if(input!=null)
					try{input.close();}catch(Exception ex){}
				if(reader!=null)
					try{reader.dispose();}catch(Exception ex){}
			}
			log.info("Done.");
			log.info("Output image creation started...");
			try {
				writer.endWriteSequence();
				log.info("Done.");
			} catch (Exception e) {
				log.error("Output image could NOT be written. ", e);
			}
		}finally{
			if(output!=null)
				try{output.close();}catch(Exception ex){}
			if(writer!=null)
				try{writer.dispose();}catch(Exception ex){}
		}
	}

	protected static LookupTable prepareBitStrip(DicomStreamMetaData meta, ImageReader reader) throws IOException {
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
		short[] slut = new short[entries];
		int range = entries - 1;
		for (int i = 0; i < entries; i++) {
			slut[i] = (short) ((4095 * i) / range);
		}
		if (signed) {
			ds.putInt(Tag.PixelRepresentation, VR.IS, 0);
		}
		LookupTable lut = new ShortLookupTable(stored, signed, -mm[0], 12, slut);
		return lut;
	}

	private void insertMetaData(DicomObject newDs, int numberOfFrames, Float frameTime, String sopInstanceUid, String seriesInstanceUid) {
		newDs.putString(Tag.TransferSyntaxUID, VR.UI, destinationSyntax.uid());
		newDs.putInt(Tag.NumberOfFrames, VR.IS, numberOfFrames);
		newDs.putInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTime);
		newDs.putString(Tag.FrameTime, VR.DS, frameTime.toString());

		newDs.remove(Tag.SeriesNumber);
		newDs.putInt(Tag.SeriesNumber, VR.IS, 1);
		newDs.remove(Tag.InstanceNumber);
		newDs.putInt(Tag.InstanceNumber, VR.IS, 1);

		newDs.remove(Tag.SOPInstanceUID);
		newDs.putString(Tag.SOPInstanceUID, VR.UI, sopInstanceUid);

		newDs.remove(Tag.SeriesInstanceUID);
		newDs.putString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUid);

		newDs.remove(Tag.MediaStorageSOPInstanceUID);
		newDs.putString(Tag.MediaStorageSOPInstanceUID, VR.UI, sopInstanceUid);
	};

} // End MultiFrameGenerator.java