/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.wado;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.video.ConverterFactory;

/**
 * This class take in input a DICOM instance and convert the instance into a flv file The class will store the file into a configurable directory (taken from GlobalConfiguration at startup)
 * 
 * @author sango
 */
public class VideoConverter {
    private String _storageFolder = null;
    private Long fps = null;
    private Log log = LogFactory.getLog(VideoConverter.class);

    public VideoConverter(String storageFolder, int _fps) {
        _storageFolder = storageFolder;
        fps = (long) _fps;
    }

    public File convert(File dicomSource) throws IOException {
        Iterator<ImageReader> iter = ImageIO.getImageReadersByMIMEType("application/dicom");
        ImageReader reader = null;
        try {
            do {
                reader = iter.next();
            } while (!(reader instanceof org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader));
        } catch (NoSuchElementException ex) {
            // this should be investigated
            log.debug("cannot find any ImageReader for application/dicom, loading dcm4che2 ImageReader directly");
            DicomImageReaderSpi spi = new DicomImageReaderSpi();
            reader = spi.createReaderInstance();
        } catch (Exception e) {
            log.error("WADO: while loading image reader.", e);
        }
        DicomImageReadParam readParam = (DicomImageReadParam) reader.getDefaultReadParam();
        File generatedVideo = null;
        FileInputStream fis = null;
        DicomInputStream dis = null;
        try {
            fis = new FileInputStream(dicomSource);
            dis = new DicomInputStream(fis);
            dis.setHandler(new StopTagInputHandler(Tag.PixelData));
            DicomObject dicomObj = dis.readDicomObject();
            if (dicomObj.contains(Tag.WindowCenter))
                readParam.setWindowCenter(dicomObj.getFloat(Tag.WindowCenter));
            if (dicomObj.contains(Tag.WindowWidth))
                readParam.setWindowWidth(dicomObj.getFloat(Tag.WindowWidth));
            readParam.setVoiLutFunction(DicomImageReadParam.SIGMOID);
            readParam.setAutoWindowing(false);
            int width = dicomObj.getInt(Tag.Columns);
            int height = dicomObj.getInt(Tag.Rows);
            int numOfFrames = dicomObj.getInt(Tag.NumberOfFrames);
            DicomElement frameInc = dicomObj.get(Tag.toTag(dicomObj.getString(Tag.FrameIncrementPointer)));
            if (fps == 0) {
                Float ff = frameInc.getFloat(false);
                float[] frameIncrements = null;
                if (frameInc.vr() == VR.FL && frameInc.vm(null) > 1) {
                    frameIncrements = frameInc.getFloats(false);
                    if(frameIncrements != null)
                        fps = (long)frameIncrements[0];
                }else{
                    fps = ff.longValue();
                }
            }else{
                fps = 1000/fps;
            }
            dis.close();
            fis.close();
            log.info("Converting " + numOfFrames + " frames!");
            if (numOfFrames <= 1)
                return null;
            fis = new FileInputStream(dicomSource);
            ImageInputStream iis = ImageIO.createImageInputStream(fis);
            reader.setInput(iis);
            int videoStreamIndex = 0;
            int videoStreamId = 0;
            // create a media writer and specify the output file
            IMediaWriter writer = ToolFactory.makeWriter(_storageFolder + "/" + dicomObj.getString(Tag.SOPInstanceUID) + ".flv");
            writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);
            long startTime = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            for (int index = 0; index < numOfFrames; index++)
            {
                BufferedImage frame = reader.read(index, readParam);
                BufferedImage iv = ConverterFactory.convertToType(frame, BufferedImage.TYPE_3BYTE_BGR);
                currentTime += fps;
                writer.encodeVideo(0, iv, currentTime - startTime, TimeUnit.MILLISECONDS);
            }
            log.info("Job finished in " + (System.currentTimeMillis() - startTime) / 1000);
            writer.close();
            generatedVideo = new File(_storageFolder + "/" + dicomObj.getString(Tag.SOPInstanceUID) + ".flv");
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (dis != null)
                try {
                    dis.close();
                } catch (IOException e) {
                }
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                }
        }
        return generatedVideo;
    }
}
