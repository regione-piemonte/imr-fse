/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.wado.utils;

import it.units.htl.dpacs.accessors.Accessor;
import it.units.htl.dpacs.accessors.AccessorFactory;
import it.units.htl.dpacs.valueObjects.DicomMatch;
import it.units.htl.dpacs.valueObjects.Study;
import it.units.htl.wado.VideoConverter;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.SAXWriter;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.util.CloseUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.ToolFactory;

public class BatDicomObject {
    private DicomMatch object;
    private String urlToHardDisk; // NULL if the file is to retrieve nearline, not null if retrievable through a java.io.File
    private String tempUrl = null;
    private Log log = LogFactory.getLog(BatDicomObject.class);
    private static String DIRECTORY_SEPARATOR = "/";
    private static Log debugLog = LogFactory.getLog("DEBUGLOG"); // 4D

    public BatDicomObject(DicomMatch object, String tempUrl) throws Exception {
        this.object = object;
        if ((this.object.study.getStudyStatus().charAt(0) != Study.DPACS_NEARLINE_STATUS) || (this.object.nearlineData == null)) {
            StringBuilder url = new StringBuilder();
            url.append(this.object.study.getFastestAccess()); // It ends with a slash
            url.append(this.object.study.getStudyInstanceUid());
            url.append(DIRECTORY_SEPARATOR).append(this.object.series.getSeriesInstanceUid());
            url.append(DIRECTORY_SEPARATOR).append(this.object.instance.getSopInstanceUid());
            urlToHardDisk = url.toString(); // This is the only place it is set!!!
            File f = new File(urlToHardDisk);
            if (!f.exists()) {
                throw new FileNotFoundException(f.getAbsolutePath() + " not found.");
            }
        } // If the file is nearline, don't try to read the stream, since it's more of a burden
        this.tempUrl = tempUrl;
    }

    public File getFile() {
        return new File(urlToHardDisk);
    }

    public String toXML() {
        String xmlDoc = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
        xmlDoc += "<root>\n";
        DicomObject ds = null;
        try {
            debugLog.info(urlToHardDisk + " Before_readFile"); // 4D
            long start = System.currentTimeMillis(); // 4D
            ds = readFile(Tag.PixelData);
            debugLog.info(urlToHardDisk + " After_readFile " + (System.currentTimeMillis() - start)); // 4D
        } catch (IOException e) {
            log.error("File for instance " + ((urlToHardDisk != null) ? urlToHardDisk : this.object.instance.getSopInstanceUid()) + " not found...");
            return null;
        }
        /*
         * File file = new File(_url); Dcm2Xml toXML = new Dcm2Xml(); StringBuffer sout = toXML.testConvert(file); return sout.toString();
         */
        if (UID.KeyObjectSelectionDocumentStorage.equals(ds.getString(Tag.SOPClassUID))) {
            // if it's a kos, the system has to send the referenced file
            DicomObject seq = ds.get(Tag.CurrentRequestedProcedureEvidenceSequence).getDicomObject(0);
            xmlDoc += "<study uid=\"" + seq.getString(Tag.StudyInstanceUID) + "\">";
            DicomElement seriesSeq = seq.get(Tag.ReferencedSeriesSequence);
            for (int i = 0; i < seriesSeq.countItems(); i++) {
                DicomObject series = seriesSeq.getDicomObject(i);
                DicomElement instances = series.get(Tag.ReferencedSOPSequence);
                xmlDoc += "<series uid=\"" + series.getString(Tag.SeriesInstanceUID) + "\">";
                for (int k = 0; k < instances.countItems(); k++) {
                    DicomObject instance = instances.getDicomObject(k);
                    xmlDoc += "<instance uid=\"" + instance.getString(Tag.ReferencedSOPInstanceUID) + "\" />";
                }
                xmlDoc += "</series>";
            }
            xmlDoc += "</study>";
        } else {
            xmlDoc += "<TransferSyntax>" + ds.getString(Tag.TransferSyntaxUID) + "</TransferSyntax>";
            xmlDoc += "<Modality>" + ds.getString(Tag.Modality) + "</Modality>";
            xmlDoc += "<NumberOfFrames>" + ds.getString(Tag.NumberOfFrames) + "</NumberOfFrames>";
            xmlDoc += "<PhotometricInterpretation>" + ds.getString(Tag.PhotometricInterpretation) + "</PhotometricInterpretation>";
            xmlDoc += "<PixelRepresentation>" + ds.getString(Tag.PixelRepresentation) + "</PixelRepresentation>";
            xmlDoc += "<WindowCenter>" + ds.getFloat(Tag.WindowCenter) + "</WindowCenter>\n";
            xmlDoc += "<WindowWidth>" + ds.getFloat(Tag.WindowWidth) + "</WindowWidth>\n";
            xmlDoc += "<BitsStored>" + ds.getInt(Tag.BitsStored) + "</BitsStored>";
            xmlDoc += "<Columns>" + ds.getInt(Tag.Columns) + "</Columns>";
            xmlDoc += "<Rows>" + ds.getInt(Tag.Rows) + "</Rows>";
            String[] position = ds.getStrings(Tag.ImagePosition);
            xmlDoc += "<ImagePosition>";
            if (position != null) {
                for (int i = 0; i < position.length; i++) {
                    switch (i) {
                    case 0:
                        xmlDoc += "<X>" + position[i] + "</X>";
                        break;
                    case 1:
                        xmlDoc += "<Y>" + position[i] + "</Y>";
                        break;
                    case 2:
                        xmlDoc += "<Z>" + position[i] + "</Z>";
                        break;
                    default:
                        break;
                    }
                }
            }
            xmlDoc += "</ImagePosition>";
        }
        xmlDoc += "</root>\n";
        return xmlDoc;
    }

    public BufferedImage toImage(int Rows, int Cols, double WindowWidth, double WindowCenter, int frameNumber) throws IOException {
        debugLog.info(urlToHardDisk + " Before_readFile"); // 4D
        long start = System.currentTimeMillis(); // 4D
        DicomObject dicomObj = readFile(-1);
        debugLog.info(urlToHardDisk + " After_readFile " + (System.currentTimeMillis() - start)); // 4D
        if (!dicomObj.contains(Tag.PixelData)) {
            return null;
        }
        Iterator<ImageReader> iter = ImageIO.getImageReadersByMIMEType("application/dicom");
        ImageReader reader = null;
        try {
            do {
                reader = iter.next();
            } 
            //while (!(reader instanceof org.dcm4cheri.imageio.plugins.DcmImageReader));
            while (!(reader instanceof org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader)); //GDC
        } catch (NoSuchElementException ex) {
            // this should be investigated
            log.debug("cannot find any ImageReader for application/dicom, loading dcm4che2 ImageReader directly");
            DicomImageReaderSpi spi = new DicomImageReaderSpi();
            reader = spi.createReaderInstance();
        } catch (Exception e) {
            log.error("WADO: while loading image reader.", e);
        }
        DicomImageReadParam readParam = (DicomImageReadParam) reader.getDefaultReadParam();
        readParam.setWindowCenter(Float.parseFloat(WindowCenter + ""));
        readParam.setWindowWidth(Float.parseFloat(WindowWidth + ""));
        readParam.setVoiLutFunction(DicomImageReadParam.SIGMOID);
        readParam.setAutoWindowing(true);
        debugLog.info(urlToHardDisk + " In_toImage"); // 4D
        debugLog.info(urlToHardDisk + " toFileInputStream"); // 4D
        InputStream fis = toFileInputStream();
        debugLog.info(urlToHardDisk + " toFileInputStream done"); // 4D
        debugLog.info(urlToHardDisk + " createImageInputStream"); // 4D
        ImageInputStream iis = ImageIO.createImageInputStream(fis);
        debugLog.info(urlToHardDisk + " created ImageInputStream"); // 4D
        BufferedImage bi;
        try {
            debugLog.info(urlToHardDisk + " setInput"); // 4D
            reader.setInput(iis, false);
            debugLog.info(urlToHardDisk + " input set"); // 4D
            if (!dicomObj.contains(Tag.NumberOfFrames))
                frameNumber = 0; // if the instance is single-framed the parameter must be ignored

            debugLog.info(urlToHardDisk + " read"); // 4D
            bi = reader.read(frameNumber, readParam);
            debugLog.info(urlToHardDisk + " readed"); // 4D
            if (bi == null) {
                log.error("Couldn't read the image!");
                return null;
            }
        } finally {
            CloseUtils.safeClose(iis);
            CloseUtils.safeClose(fis);
        }
        if (Cols != 0 || Rows != 0) {
            if (Cols == 0) {
                Cols = Rows;
            }
            if (Rows == 0) {
                Rows = Cols;
            }
            bi = scaleToSize(Cols, Rows, bi);
            debugLog.info(urlToHardDisk + " scaled ToSize"); // 4D
        }
        return bi;
    }

    public File toMpegMovie() throws IOException {
        DicomInputStream dis = null;
        FileOutputStream fos = null;
        try {
            dis = new DicomInputStream(new File(urlToHardDisk));
            TransformerHandler th = getTransformerHandler();
            th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
            th.setResult(new StreamResult(System.out));
            final SAXWriter writer = new SAXWriter(th, null);
            writer.setBaseDir(new File(tempUrl + "/" + object.instance.getSopInstanceUid()));
            int[] dd = { Tag.PixelData };
            writer.setExclude(dd);
            dis.setHandler(writer);
            dis.readDicomObject(new BasicDicomObject(), -1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } finally {
            if (fos != null)
                try {
                    fos.close();
                } catch (Exception e2) {
                }
            if (dis != null)
                try {
                    dis.close();
                } catch (Exception e) {
                }
        }
        return (new File(tempUrl + "/" + object.instance.getSopInstanceUid() + "/7FE00010/2"));
    }

    /**
     * It sends through HttpResponse Outputstream the dicom object The DicomObject will be transcoded if requested or need.
     * 
     * @param response
     *            , the HttpResponse of the servlet
     * @param anonymize
     *            , if true the instance will be anonymized
     * @param destinationSyntax
     *            the transferSyntax of the output file
     * @return true if everything is ok, false otherwise.
     */
    public boolean toDicomFile(HttpServletResponse response, boolean anonymize, TransferSyntax destinationSyntax, int bufferKbs, int maximumBytes) {
        // take the file
        InputStream in = null;
        if (urlToHardDisk != null) {
            try {
                in = new FileInputStream(urlToHardDisk);
            } catch (FileNotFoundException e) {
                log.error("Problem while accessing to the instance : " + urlToHardDisk, e);
                return false;
            }
        } else {
            Accessor accessor = null;
            try {
                accessor = AccessorFactory.getAccessor(this.object.nearlineData.getDeviceType(), this.object.nearlineData.getDeviceUrl(), this.object.nearlineData.getCredentials());
                in = accessor.getFile(this.object.nearlineData.getDirectUrl());
            } catch (Exception ex) {
                log.error("Unable to retrieve nearline data", ex);
                return false;
            } finally {
                if (accessor != null)
                    accessor.close();
            }
        }
        DicomInputStream dis = null;
        BufferedInputStream iis = null;
        OutputStream out = null;
        DicomOutputStream dos = null;
        try {
            // open the file as DicomInputStream
            iis = new BufferedInputStream(in);
            dis = new DicomInputStream(iis);
            // read the file meta info block
            DicomObject fmiAttrs = dis.readFileMetaInformation();
            String sourceSyntaxUid = UID.ImplicitVRLittleEndian;
            if (fmiAttrs != null) {
                sourceSyntaxUid = fmiAttrs.getString(Tag.TransferSyntaxUID, UID.ImplicitVRLittleEndian);
            }
            TransferSyntax sourceSyntax = TransferSyntax.valueOf(sourceSyntaxUid);
            boolean recodeImages;
            if (destinationSyntax == null) {
                recodeImages = false;
            } else {
                // check if the source or destination syntax are encapsulated
                boolean bothRaw = (!sourceSyntax.encapsulated()) && (!destinationSyntax.encapsulated());
                recodeImages = !(bothRaw || sourceSyntax.equals(destinationSyntax));
                // the system have to check if it is a video or if there is a reader for this type of instance
                // if the instance is a video or there isn't a reader for the content, the instance couldn't be recoded
                if (recodeImages && ("1.2.840.10008.1.2.4.100".equals(sourceSyntaxUid) || "1.2.840.10008.1.2.4.102".equals(sourceSyntaxUid) || (!sourceSyntax.uncompressed() && (!"1.2.840.10008.1.2.4.70".equals(sourceSyntaxUid))))) {
                    recodeImages = false;
                }
            }
            // Build the new dataset of the object, with the right PatientInformation and study Information
            DicomObject newData = new BasicDicomObject();
            if (!anonymize) {
                newData.putString(Tag.PatientID, VR.LO, object.patient.getPatientId());
                newData.putString(Tag.IssuerOfPatientID, VR.LO, object.patient.getIdIssuer());
                newData.putString(Tag.PatientName, VR.PN, object.patient.getDcmPatientName());
                newData.putDate(Tag.PatientBirthDate, VR.DA, object.patient.getBirthDate());
            } else {
                newData.putString(Tag.PatientName, VR.PN, "NO_SURNAME^NO_NAME");
                newData.putString(Tag.PatientBirthDate, VR.DA, "19000101");
                newData.putString(Tag.PatientID, VR.LO, "NO_ID");
            }
            newData.putString(Tag.StudyInstanceUID, VR.UI, object.study.getStudyInstanceUid());
            newData.putString(Tag.AccessionNumber, VR.SH, object.study.getAccessionNumber());
            newData.putString(Tag.StudyID, VR.SH, object.study.getStudyId());
            newData.putString(Tag.StudyDate, VR.DA, object.study.getStudyDate(new SimpleDateFormat("yyyyMMdd")));
            newData.putString(Tag.StudyTime, VR.TM, object.study.getStudyTime(new SimpleDateFormat("hhmmss")));
            newData.putString(Tag.StudyVerifiedDate, VR.DA, object.study.getStudyVerifiedDate(new SimpleDateFormat("yyyyMMdd")));
            newData.putString(Tag.StudyVerifiedTime, VR.TM, object.study.getStudyVerifiedTime(new SimpleDateFormat("hhmmss")));
            newData.putString(Tag.StudyCompletionDate, VR.DA, object.study.getStudyCompletionDate(new SimpleDateFormat("yyyyMMdd")));
            newData.putString(Tag.StudyCompletionTime, VR.TM, object.study.getStudyCompletionTime(new SimpleDateFormat("hhmmss")));
            StopTagPresenceHandler handler=new StopTagPresenceHandler(Tag.PixelData);
            dis.setHandler(handler);
            // take all tha metadata in the instance except the pixel data
            debugLog.info(urlToHardDisk + " Before_readingDicomObject"); // 4D
            long start = System.currentTimeMillis(); // 4D
            DicomObject dataOnly = dis.readDicomObject();
            debugLog.info(urlToHardDisk + " After_readingDicomObject " + (System.currentTimeMillis() - start)); // 4D
            if(!handler.isStopTagRead()){
            	recodeImages=false;
            	destinationSyntax=sourceSyntax;
            }
            if (recodeImages) {
                log.debug("Have to recode the instance from " + sourceSyntaxUid + " to " + destinationSyntax.uid());
                dis.close();
                // FIXME:it is very slow for nearline data!
                dis = new DicomInputStream(toFileInputStream());
                InstanceCoder ic = new InstanceCoder();
                // this method transcodes the instance and sends it over the HttpResponse outputstream
                if (!ic.recodeImage(dis, tempUrl, object.instance.getSopInstanceUid(), destinationSyntax, response, newData)) {
                    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "While converting image in the new transfer syntax");
                    return false;
                }
            } else {
                log.debug("Nothing to do for this transfer syntax: " + sourceSyntaxUid);
                out = response.getOutputStream();
                dos = new DicomOutputStream(out);
                dos.setAutoFinish(true);
                // PROPOSAL
                // FileMetaInformation initFmi = new FileMetaInformation(fmiAttrs);
                // dataOnly.initFileMetaInformation(initFmi.getMediaStorageSOPClassUID(), initFmi.getMediaStorageSOPInstanceUID(), destinationSyntax.uid());
                // TransferSyntax ts = dis.getTransferSyntax();
                TransferSyntax ts = sourceSyntax;
                dataOnly.initFileMetaInformation(ts.uid());
                newData.copyTo(dataOnly);
                // PROPOSAL
                // dos.setTransferSyntax(destinationSyntax.uid());
                dos.setTransferSyntax(ts);
                dos.writeFileMetaInformation(dataOnly.fileMetaInfo());
                // PROPOSAL
                // dos.writeDataset(dataOnly, destinationSyntax.uid());
                dos.writeDataset(dataOnly, ts);
                int valLen = dis.valueLength();
                dos.writeHeader(dis.tag(), dis.vr(), valLen);
                debugLog.info(urlToHardDisk + " StartingReadingFile ");
                start = System.currentTimeMillis(); // 4D
                debugLog.info("<<<<<<<<<Retrieving Availability");
                int sizeToRead = dis.available();
                debugLog.info("<<<<<<<<<Retrieved Availability");
                if ((sizeToRead == 0) || (sizeToRead > maximumBytes)) {
                    debugLog.info("----------------- Xeptional sizeToRead " + sizeToRead);
                    sizeToRead = bufferKbs * 1024;
                }
                byte[] b = new byte[sizeToRead];
                int l = dis.read(b);
                while (l != -1) {
                    dos.write(b, 0, l);
                    l = dis.read(b);
                }
                debugLog.info(urlToHardDisk + " FinishedReadingFile " + (System.currentTimeMillis() - start));
                debugLog.info(urlToHardDisk + " Before_readingDicomObject2nd"); // 4D
                start = System.currentTimeMillis(); // 4D
                dataOnly = dis.readDicomObject();
                debugLog.info(urlToHardDisk + " After_readingDicomObject2nd " + (System.currentTimeMillis() - start)); // 4D
                // PROPOSAL
                // dos.writeDataset(dataOnly,destinationSyntax.uid());
                dos.writeDataset(dataOnly, ts);
            }
        } catch (Exception e) {
            log.error("", e);
            return false;
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception ex) {
                log.error("Error closing in", ex);
            }
            try {
                if (dos != null)
                    dos.close();
            } catch (Exception ex) {
                log.error("Error closing dos", ex);
            }
            try {
                if (out != null)
                    out.close();
            } catch (Exception ex) {
                log.error("Error closing out", ex);
            }
            try {
                if (dis != null)
                    dis.close();
            } catch (Exception ex) {
                log.error("Error closing dis", ex);
            }
            try {
                if (iis != null)
                    iis.close();
            } catch (Exception ex) {
                log.error("Error closing iis", ex);
            }
        }
        return true;
    }

    public File toFlvMovie() throws IOException {
        return toFlvMovie(0);
    }

    public File toFlvMovie(int fps) throws IOException {
        // here we have to take two roads, convert from Multiframe or convert an mpeg
        DicomObject ds = readFile(Tag.PixelData);
        log.info(ds.contains(Tag.NumberOfFrames) + " " + ds.getInt(Tag.NumberOfFrames));
        if ("1.2.840.10008.1.2.4.100".equals(ds.getString(Tag.TransferSyntaxUID)) || "1.2.840.10008.1.2.4.102".equals(ds.getString(Tag.TransferSyntaxUID))) {
            // converting an mpeg to flv
            log.info("Converting an mpeg to flv");
            File mpeg = toMpegMovie();
            IMediaReader reader = ToolFactory.makeReader(mpeg.getAbsolutePath());
            reader.addListener(ToolFactory.makeWriter(tempUrl + "/" + object.instance.getSopInstanceUid() + ".flv", reader));
            while (reader.readPacket() == null)
                ;
            mpeg.delete();
            return new File(tempUrl + "/" + object.instance.getSopInstanceUid() + ".flv");
        } else {
            // converting a multiframe to an flv
            log.info("converting a MF to flv");
            VideoConverter vc = new VideoConverter(tempUrl, fps);
            File f = new File(urlToHardDisk);
            File outPut = vc.convert(f);
            return outPut;
        }
    }

    public Document toXmlForPdf() {
        Document document = null;
        try {
            debugLog.info(urlToHardDisk + " Before_readFile"); // 4D
            long start = System.currentTimeMillis(); // 4D
            DicomObject doo = readFile(Tag.PixelData);
            debugLog.info(urlToHardDisk + " After_readFile " + (System.currentTimeMillis() - start)); // 4D
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder loader = factory.newDocumentBuilder();
            document = loader.newDocument();
            Element root = document.createElement("root");
            addTag(root, document, doo);
            document.appendChild(root);
        } catch (IOException e) {
            log.error("While converting in xml", e);
            return null;
        } catch (ParserConfigurationException e) {
            log.error("While converting in xml", e);
            return null;
        }
        return document;
    }

    private void addTag(Element where, Document generator, DicomObject source) {
        if (source != null) {
            Iterator<DicomElement> test = source.datasetIterator();
            while (test.hasNext()) {
                Element ee = generator.createElement("tag");
                DicomElement e = test.next();
                ee.setAttribute("tag", Integer.toHexString(e.tag()) + "");
                ee.setAttribute("code", e.vr() + "");
                if (e.vr() == VR.SQ) {
                    for (int k = 0; k < e.countItems(); k++) {
                        addTag(ee, generator, e.getDicomObject(k));
                    }
                } else {
                    ee.setTextContent(e.getString(new SpecificCharacterSet("UTF-8"), false));
                }
                if (ee.hasAttributes() || ee.hasChildNodes()) {
                    where.appendChild(ee);
                }
            }
        }
    }

    private InputStream toFileInputStream() throws IOException {
        FileInputStream in = null;
        InputStream nin = null;
        if (urlToHardDisk != null) {
            debugLog.info(urlToHardDisk + " Starting_toFileInputStream"); // 4D
            long start = System.currentTimeMillis(); // 4D
            File file = new File(urlToHardDisk);
            in = new FileInputStream(file);
            debugLog.info(urlToHardDisk + " ReturningFrom_toFileInputStream " + (System.currentTimeMillis() - start)); // 4D
            return in;
        } else {
            Accessor accessor = null;
            try {
                accessor = AccessorFactory.getAccessor(this.object.nearlineData.getDeviceType(), this.object.nearlineData.getDeviceUrl(), this.object.nearlineData.getCredentials());
                nin = accessor.getFile(this.object.nearlineData.getDirectUrl());
                return nin;
            } catch (Exception ex) {
                log.error("Unable to retrieve nearline data");
                throw new IOException(ex);
            } finally {
                if (accessor != null)
                    accessor.close();
            }
        }
    }

    private DicomObject readFile(int until) throws IOException {
        InputStream iis = toFileInputStream();
        DicomInputStream dis = new DicomInputStream(iis);
        dis.setHandler(new StopTagInputHandler(until));
        DicomObject res = dis.readDicomObject();
        if (dis != null)
            try {
                dis.close();
            } catch (Exception ex) {
            }
        if (iis != null)
            try {
                iis.close();
            } catch (Exception ex) {
            }
        return res;
    }

    private static TransformerHandler getTransformerHandler()
            throws TransformerConfigurationException, IOException {
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        return tf.newTransformerHandler();
    }

    private BufferedImage scaleToSize(int nMaxWidth, int nMaxHeight, BufferedImage imgSrc) {
        int nHeight = imgSrc.getHeight();
        int nWidth = imgSrc.getWidth();
        double scaleX = (double) nMaxWidth / (double) nWidth;
        double scaleY = (double) nMaxHeight / (double) nHeight;
        double fScale = Math.min(scaleX, scaleY);
        return scale(fScale, imgSrc);
    }

    private BufferedImage scale(double scale, BufferedImage srcImg) {
        if (scale == 1) {
            return srcImg;
        }
        AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale), null);
        return op.filter(srcImg, null);
    }
}
