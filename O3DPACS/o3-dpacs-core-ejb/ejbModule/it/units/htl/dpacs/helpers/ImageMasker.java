/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.units.htl.dpacs.helpers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;

/**
 *
 * @author Alessandro
 */
public class ImageMasker {

    private static ImageMasker imgMask = null;
    static final Log log = LogFactory.getLog("ImageMasker");
    private static DcmParser dcm_dp;

    /** Creates a new instance of ImageMasker */
    private ImageMasker() {
    }

    private static Dataset loadDicomDataset(File dcm) {
        Dataset dcm_ds = null;
        FileInputStream dcm_fis = null;
        BufferedInputStream dcm_in =null;
        try {
            dcm_fis = new FileInputStream(dcm);
            dcm_in = new BufferedInputStream(dcm_fis);
            dcm_dp = DcmParserFactory.getInstance().newDcmParser(dcm_in);
            FileFormat dcm_ff = dcm_dp.detectFileFormat();
            log.info("FileFormat: " + dcm_ff.toString());

            dcm_ds = DcmObjectFactory.getInstance().newDataset();
            dcm_ds.readFile(dcm_in, dcm_ff, -1);
            
        } catch (Exception e) {
            log.info("Error");
        }finally{
        	try{dcm_in.close();}catch(Exception ex){}
        	try{dcm_fis.close();}catch(Exception ex){}
        }
        
        return dcm_ds;
    }


    public static synchronized void graphAnonymize(File dicomFile, Vector<RoiMeasure> rects, File outFile) {

        Dataset dataset = loadDicomDataset(dicomFile);
        int numberOfFrames = dataset.getInt(Tags.NumberOfFrames, -1);
      
        if (numberOfFrames == -1) {
            numberOfFrames = 1;
        }
        
        int pc =  dataset.getInt(Tags.PlanarConfiguration, -1);
                    
                    
        if(numberOfFrames == -1)
            numberOfFrames = 1;
            
        if(pc == -1)
            pc = 0;
     
        if(numberOfFrames > 1 || pc == 1){
            graphMultiAnonymize(dataset, rects);
        } else {
            graphAnonymize(dataset, rects);
        }
        FileOutputStream fos=null;

        try {
            fos = new FileOutputStream(outFile);
            dataset.writeFile(fos, (DcmEncodeParam) dcm_dp.getDcmDecodeParam());
        } catch (IOException e) {
            log.error("Error on Saving", e);
        }finally{
        	try{fos.close();}catch(Exception ex){}
        }
    }

    public static void graphMultiAnonymize(Dataset dataset, Vector<RoiMeasure> rects) {

        BufferedImage[] imgs = getFramesFromDataset(dataset);

        for (int k = 0; k < imgs.length; k++) {
            Graphics g = imgs[k].getGraphics();

            for (int j = 0; j < rects.size(); j++) {
                RoiMeasure rm = rects.elementAt(j);
                g.setColor(Color.BLACK);
                int x1 = (int) rm.getP1().getX();
                int y1 = (int) rm.getP1().getY();
                int w = (int) Math.abs(rm.getP1().getX()-rm.getP2().getX());
                int h = (int) Math.abs(rm.getP1().getY()-rm.getP2().getY());
                g.fillRect(x1, y1, w, h);
            }
        }

        makeForMultiframe(dataset, imgs);
    }

    private static void graphAnonymize(Dataset dataset, Vector<RoiMeasure> rects) {
           
        BufferedImage image = null;
        try{
        	image=dataset.toBufferedImage(1);
        }catch(UnsupportedOperationException uoex){
        	log.error("Unable to mask image: POSSIBLY COMPRESSED");
        	throw uoex;
        }
        
        Graphics g = image.getGraphics();
        
        for(int j=0; j<rects.size(); j++){
            RoiMeasure rm = rects.elementAt(j);
            g.setColor(Color.BLACK);
            int x1 = (int)rm.getP1().getX();
            int y1 = (int)rm.getP1().getY();
            int w = (int)Math.abs(rm.getP1().getX()-rm.getP2().getX());
            int h = (int)Math.abs(rm.getP1().getY()-rm.getP2().getY());
            g.fillRect(x1, y1, w, h);
        }
        
        int spp = dataset.getInt(Tags.SamplesPerPixel, 1);
        String pi = dataset.getString(Tags.PhotometricInterpretation);
        Rectangle r = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        
        dataset.putBufferedImage(image, r, true);
        
        dataset.putUS(Tags.SamplesPerPixel, spp);// Type 1
        dataset.putCS(Tags.PhotometricInterpretation, pi);// Type 1
    
    }

    private static BufferedImage[] getFramesFromDataset(Dataset dataset) {
        int numberOfFrames = 1;
        BufferedImage[] frames = null;

        numberOfFrames = dataset.getInt(Tags.NumberOfFrames, -1);

        if (numberOfFrames == -1) {
            numberOfFrames = 1;
        }

        frames = new BufferedImage[numberOfFrames];
        try{
        	for (int i = 0; i < numberOfFrames; i++) {
        		frames[i] = dataset.toBufferedImage(i + 1);
        		frames[i].setAccelerationPriority(1);
        	}
        }catch(UnsupportedOperationException uoex){
        	log.error("Unable to mask image: POSSIBLY COMPRESSED");
        	throw uoex;
        }

        return frames;
    }

    
     private static BufferedImage makeForMultiframe(Dataset dataset, BufferedImage[] bi){
        
        dataset.putUS(Tags.Rows, bi[0].getHeight()*bi.length);// Type 1
        dataset.putIS(Tags.NumberOfFrames, 1);
        
        BufferedImage big = dataset.toBufferedImage();
        
        Graphics2D g = (Graphics2D) big.getGraphics();
        for(int i=0; i<bi.length; i++){
            g.drawImage(bi[i], 0, i*bi[0].getHeight(), null);
        }
        
        Rectangle r = new Rectangle(0, 0, bi[0].getWidth(), bi[0].getHeight()*bi.length);
        
        
        int spp = dataset.getInt(Tags.SamplesPerPixel, 1);
        String pi = dataset.getString(Tags.PhotometricInterpretation);
        
        dataset.putBufferedImage(big, r, true);
        
        dataset.putIS(Tags.NumberOfFrames, bi.length);
        dataset.putUS(Tags.Rows, bi[0].getHeight());// Type 1
        dataset.putUS(Tags.Columns, bi[0].getWidth());// Type 1
        dataset.putUS(Tags.SamplesPerPixel, spp);// Type 1
        dataset.putCS(Tags.PhotometricInterpretation, pi);// Type 1
        
        return big;
    }

}