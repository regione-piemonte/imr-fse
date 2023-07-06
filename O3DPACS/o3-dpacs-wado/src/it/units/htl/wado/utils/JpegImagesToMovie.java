/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.wado.utils;

import it.units.htl.wado.WADOServlet;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Vector;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.media.multiplexer.video.QuicktimeMux;

public class JpegImagesToMovie {
	Vector<String> images; // jpeg image file path list
	VideoFormat format; // format of movie to be created..
	private Log log = LogFactory.getLog(WADOServlet.class);
	
	private static Log debugLog = LogFactory.getLog("DEBUGLOG");		// 4D
	
	// Sample code.
	public String doIt(int Width, int Height, float frameRate, Vector<String> Immagini, String movName) {
		try {
			// change width, height, and framerate!
			// Excercise: Read width and height of first image and use that.
			debugLog.info(movName+" Starting_doIt");		// 4D
	        long start=System.currentTimeMillis(); // 4D
			init(Width, Height, frameRate);
			images = Immagini;
			File dest = new File(movName);
			createMovie(dest);
			deleteImages();
			log.debug(movName);
			debugLog.info(movName+" Completed_doIt "+(System.currentTimeMillis()-start));		// 4D
			return movName;
		} catch (Exception e) {	
			log.error("While generating movie from multiframe.", e);
			return null;
		}
	}

	// return jpeg image bytes of image zIndex (zero-based index)
	public byte[] getImageBytes(int zIndex) throws IOException {
		if (images == null)
			return null;
		if (zIndex >= images.size())
			return null;
		String imageFile = (String) images.elementAt(zIndex);
		// Open a random access file for the next image.
		RandomAccessFile raFile = new RandomAccessFile(imageFile, "r");
		byte data[] = new byte[(int) raFile.length()];
		raFile.readFully(data);
		raFile.close();
		return data;
	}
	private void deleteImages(){
		File f;
		for(int i = 0; i < images.size(); i++){
			f = new File((String)images.elementAt(i));
			f.delete();
		}
	}

	// Call this before converting a movie;
	// Use movie width, height;

	public void init(int width, int height, float frameRate) {
		format = new VideoFormat(VideoFormat.JPEG,
				new Dimension(width, height), Format.NOT_SPECIFIED,
				Format.byteArray, frameRate);
	}

	public void createMovie(File out) throws Exception {
		if (format == null)
			throw new Exception("Call init() first.");		
		QuicktimeMux mux = null; // AVI not working, otherwise would use
		// BasicMux
		if (out.getPath().endsWith(".mov")) {
			
			mux = new QuicktimeMux();
			mux.setContentDescriptor(new ContentDescriptor(
					FileTypeDescriptor.QUICKTIME));
		} else
			throw new Exception(
					"bad movie file extension. Only .mov supported.");
		// create dest file media locator.
		// This sample assumes writing a QT movie to a file.
		MediaLocator ml = new MediaLocator(new URL("file:"
				+ out.getAbsolutePath()));
		com.sun.media.datasink.file.Handler dataSink = new com.sun.media.datasink.file.Handler();
		dataSink.setSource(mux.getDataOutput()); // associate file with mux
		dataSink.setOutputLocator(ml);
		dataSink.open();
		dataSink.start();
		// video only in this sample.
		mux.setNumTracks(1);
		// JPEGFormat was the only kind I got working.
		mux.setInputFormat(format, 0);
		mux.open();
		// Each jpeg goes in a Buffer.
		// When done, buffer must contain EOM flag (and zero length data?).
		Buffer buffer = new Buffer();
		for (int x = 0;; x++) {
			read(x, buffer); // read in next file. x is zero index
			mux.doProcess(buffer, 0);
			if (buffer.isEOM())
				break;
		}
		mux.close();
		// close it up
		dataSink.close();
	}

	// Read jpeg image into Buffer
	// id is zero based index of file to get.
	// Always starts at zero and increments by 1
	// Buffer is a jmf structure
	public void read(int id, Buffer buf) throws IOException {
		byte b[] = getImageBytes(id);
		if (b == null) {
			// We are done. Set EndOfMedia.
			buf.setEOM(true);
			buf.setOffset(0);
			buf.setLength(0);
		} else {
			buf.setData(b);
			buf.setOffset(0);
			buf.setLength(b.length);
			buf.setFormat(format);
			buf.setFlags(buf.getFlags() | Buffer.FLAG_KEY_FRAME);
		}
	}
}