/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.wado;

import it.units.htl.atna.AuditLogService;
import it.units.htl.dpacs.valueObjects.DicomMatch;
import it.units.htl.wado.dao.PacsRetrieveManager;
import it.units.htl.wado.utils.BatDicomObject;
import it.units.htl.wado.utils.DcmDirWriter;
import it.units.htl.wado.utils.SupportedContentType;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.dcm4che2.audit.message.AuditEvent.ActionCode;
import org.dcm4che2.audit.message.InstancesAccessedMessage;
import org.dcm4che2.data.TransferSyntax;
import org.w3c.dom.Document;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * This is the servlet that accepts WADO requests.
 * 
 * @author sango
 */
public class WADOServlet extends HttpServlet {
    private static String DIRECTORY_SEPARATOR = "/";
    private static final long serialVersionUID = 1L;
    private static final String RELOADMESSAGE_SUCCESS = "Successfully reloaded configuration";
    private PacsRetrieveManager prm = null;
    private Log log = LogFactory.getLog(WADOServlet.class);
    protected URIResolver uriResolver;
    private static Log debugLog = LogFactory.getLog("DEBUGLOG"); // 4D

    public void init() throws ServletException {
        try {
            prm = new PacsRetrieveManager();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void wadoProcessRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        // requestType, studyUID, seriesUID, objectUID are required
        // if one of these is omitted the request can't be fulfilled
        // show the request url
        log.info("wadoProcessRequest: " + request.getQueryString());
        final String studyUID = request.getParameter("studyUID");
        String seriesUID = request.getParameter("seriesUID");
        String objectUID = request.getParameter("objectUID");
        if ((studyUID == null) || (seriesUID == null) || (objectUID == null)) {
            log.error("ERROR: At least one param is null: " + request.getQueryString());
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request type, some parameters are missed...");
                return;
            } catch (IOException e) {
                log.error("Couldn't send http response.", e);
            }
        }
        // contentType is an optional parameters, have to accept also request
        // without this. If not present default is image/jpeg
        String contentType = request.getParameter("contentType");
        if (contentType != null) {
            if (SupportedContentType.isSupported(contentType) != true) {
                log.error(contentType + " request type not yet suported...");
                response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Content type not yet supported...");
                return;
            }
        } else {
            contentType = "image/jpeg";
        }
        log.info("INSTANCE: " + objectUID + " " + contentType); // 4D
        long overallStart = System.currentTimeMillis(); // 4D
        response.setContentType(contentType);
        log.info(objectUID + " Starting_getFiles"); // 4D
        long start = System.currentTimeMillis(); // 4D
        DicomMatch[] ress = prm.getFiles(studyUID, seriesUID, objectUID);
        log.info(objectUID + " Completed_getFiles " + (System.currentTimeMillis() - start)); // 4D
        if ((ress == null) || (ress[0] == null)) {
            String msg = "This object doesn't exist on the PACS. Requested data: St." + studyUID + " Se. " + seriesUID + " In." + objectUID;
            log.warn(msg);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
            return;
        }
        log.info(objectUID + " Starting_getConfigParam"); // 4D
        start = System.currentTimeMillis(); // 4D
        String tempUrl = prm.getWadoTempUrl();
        log.info(objectUID + " Completed_getConfigParam " + (System.currentTimeMillis() - start)); // 4D
        if (tempUrl == null) {
            log.warn("The WADO temporary directory was not found!!!");
        }
        BatDicomObject _do;
        try {
            debugLog.info(objectUID + " Starting_BatDicomObject"); // 4D
            start = System.currentTimeMillis(); // 4D
            _do = new BatDicomObject(ress[0], tempUrl);
            debugLog.info(objectUID + " Completed_BatDicomObject " + (System.currentTimeMillis() - start)); // 4D
        } catch (FileNotFoundException e) {
            log.error("File not found", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
        } catch (Exception e) {
            log.error("Problem while reading DICOM file.", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, objectUID + ": DICOM file is corrupted.");
            return;
        }
        AuditLogService logService = AuditLogService.getInstance();
        try {
            InstancesAccessedMessage msg = new InstancesAccessedMessage(ActionCode.READ);
            msg.addUserPerson("WadoServiceUser", "", "WadoServiceUser", request.getRemoteHost(), true);
            msg.addUserPerson("WadoService", "", "WadoService", request.getLocalAddr(), false);
            msg.addStudy(studyUID, null);
            String patID = (ress[0].patient.getPatientId() != null && !"".equals(ress[0].patient.getPatientId())) ? ress[0].patient.getPatientId() : "Unknown Patient";
            msg.addPatient(patID, ress[0].patient.getDcmPatientName());
            logService.SendMessage(msg);
        } catch (Exception e) {
            log.warn("Unable to send AuditLogMessage", e);
        }
        if (contentType.equals("text/xml")) {
            try {
                response.getWriter().write(_do.toXML());
                debugLog.info("COMPLETED INSTANCE: " + objectUID + " " + contentType + " " + (System.currentTimeMillis() - overallStart)); // 4D
            } catch (IOException e) {
                log.error("Couldn't send http response.", e);
            }
        }
    	
        if (contentType.equals("byte")) { 
        	int numberOfPixelRows = 0;
            int numberOfPixelColumns = 0;
            double windowCenterOfTheImage = 0;
            double windowWidthOfTheImage = 0;
            int frameNumber = 0;
            if (request.getParameter("rows") != null && !"null".equals(request.getParameter("rows"))) {
                numberOfPixelRows = Integer.parseInt(request.getParameter("rows"));
            }
            if (request.getParameter("columns") != null && !"null".equals(request.getParameter("columns"))) {
                numberOfPixelColumns = Integer.parseInt(request.getParameter("columns"));
            }
            if (request.getParameter("windowCenter") != null && !"null".equals(request.getParameter("windowCenter"))) {
                windowCenterOfTheImage = Double.parseDouble(request.getParameter("windowCenter"));
            }
            if (request.getParameter("windowWidth") != null && !"null".equals(request.getParameter("windowWidth"))) {
                windowWidthOfTheImage = Double.parseDouble(request.getParameter("windowWidth"));
            }
            if (request.getParameter("frameNumber") != null && request.getParameter("frameNumber").length() != 0) {
                frameNumber = Integer.parseInt(request.getParameter("frameNumber"));
                frameNumber -= 1; // Libraries start from zero, but DICOM states the first image is 1
            }
            OutputStream out = null;
            try {
                out = response.getOutputStream();
            } catch (IOException e1) {
                log.error("Couldn't get response outputstream.", e1);
            }
            try {
                BufferedImage im = null;
                byte[] imageBytes = null;
                im = _do.toImage(numberOfPixelRows, numberOfPixelColumns, windowWidthOfTheImage, windowCenterOfTheImage, frameNumber);
                if(im != null) {
                	DataBufferByte dtb = (DataBufferByte) im.getData().getDataBuffer();
                	imageBytes = dtb.getData();
                	out.write(imageBytes);
                }
                
            } catch (Exception e) {
            	 log.error("Error while reading image.", e);
                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
        }
        if (contentType.equals("image/jpeg")) {
            int numberOfPixelRows = 0;
            int numberOfPixelColumns = 0;
            double windowCenterOfTheImage = 0;
            double windowWidthOfTheImage = 0;
            int frameNumber = 0;
            if (request.getParameter("rows") != null && !"null".equals(request.getParameter("rows"))) {
                numberOfPixelRows = Integer.parseInt(request.getParameter("rows"));
            }
            if (request.getParameter("columns") != null && !"null".equals(request.getParameter("columns"))) {
                numberOfPixelColumns = Integer.parseInt(request.getParameter("columns"));
            }
            if (request.getParameter("windowCenter") != null && !"null".equals(request.getParameter("windowCenter"))) {
                windowCenterOfTheImage = Double.parseDouble(request.getParameter("windowCenter"));
            }
            if (request.getParameter("windowWidth") != null && !"null".equals(request.getParameter("windowWidth"))) {
                windowWidthOfTheImage = Double.parseDouble(request.getParameter("windowWidth"));
            }
            if (request.getParameter("frameNumber") != null && request.getParameter("frameNumber").length() != 0) {
                frameNumber = Integer.parseInt(request.getParameter("frameNumber"));
                frameNumber -= 1; // Libraries start from zero, but DICOM states the first image is 1
            }
            OutputStream out = null;
            try {
                out = response.getOutputStream();
            } catch (IOException e1) {
                log.error("Couldn't get response outputstream.", e1);
            }
            JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(out);
            try {
                BufferedImage im = null;
                im = _do.toImage(numberOfPixelRows, numberOfPixelColumns, windowWidthOfTheImage, windowCenterOfTheImage, frameNumber);
                
                if (im != null) {
                    if (request.getParameter("imageQuality") != null) {
                        try {
                            float imQual = Float.parseFloat(request.getParameter("imageQuality"))/100;
                            JPEGEncodeParam jpegEncodeParam = enc.getDefaultJPEGEncodeParam(im);
                            jpegEncodeParam.setQuality(imQual, false);
                            enc.encode(im, jpegEncodeParam);
                        }catch(NumberFormatException ex){
                            log.warn("Image quality is wrong: " + request.getParameter("imageQuality"));
                            enc.encode(im);
                        }
                    } else {
                        enc.encode(im);
                    }
                }
            } catch (IndexOutOfBoundsException ioobex) {
                log.error("Error while reading image.", ioobex);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (ImageFormatException e) {
                log.error("Error while reading image.", e);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e) {
                log.error("Error while reading image.", e);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } finally {
                try {
                    out.close();
                } catch (Exception ex) {
                    log.error(ex);
                }
            }
            log.info("COMPLETED INSTANCE: " + objectUID + " " + contentType + " " + (System.currentTimeMillis() - overallStart)); // 4D
        }
        if (contentType.equals("application/dicom")) {
            response.addHeader("Content-Disposition", "inline; filename=" + objectUID + ".dcm");
            boolean anonymizeObject = false;
            if (request.getParameter("anonymize") != null) {
                if ("yes".equals(request.getParameter("anonymize"))) {
                    anonymizeObject = true;
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The anonymize parameter has a wrong value.The value is " + request.getParameter("anonymize") + ", instead it shall be 'yes'");
                    return;
                }
            }
            TransferSyntax destTS = null;
            if (request.getParameter("transferSyntax") != null) {
                if (request.getParameter("transferSyntax").equals("original")) {
                    destTS = null;
                } else {
                    destTS = TransferSyntax.valueOf(request.getParameter("transferSyntax"));
                }
            } else {
                destTS = TransferSyntax.ExplicitVRLittleEndian;
            }
            // this method send the dicom file over the response output stream, no other action needed!
            if (!_do.toDicomFile(response, anonymizeObject, destTS, prm.getWadoBufferSizeKb(), prm.getWadoLimitForWholeFile())) {
                log.info(request.getQueryString());
                return;
            }
            debugLog.info("COMPLETED INSTANCE: " + objectUID + " " + contentType + " " + (System.currentTimeMillis() - overallStart)); // 4D
        }
        if (contentType.equals("video/quicktime")) {
            response.addHeader("Content-Disposition", "attachment; filename=multiframeMovie.mov");
            File urlMov = _do.toMpegMovie();
            if (urlMov != null) {
                FileInputStream inn = new FileInputStream(urlMov);
                sendAll(inn, response);
                if (!urlMov.delete()) {
                    log.warn("Unable to delete quicktime movie, keep attention!");
                }
                try {
                    inn.close();
                } catch (Exception ex) {
                    log.warn(ex);
                }
                ;
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            debugLog.info("COMPLETED INSTANCE: " + objectUID + " " + contentType + " " + (System.currentTimeMillis() - overallStart)); // 4D
        }
        if (contentType.equals("video/mpeg")) {
            log.debug(request.getRemoteHost() + ":" + request.getRemotePort() + " <-- it makes a request to --> " + request.getLocalAddr() + ":" + request.getLocalPort());
            response.addHeader("Content-Disposition", "attachment; filename=videoOF" + objectUID + ".mpg");
            FileInputStream bis = new FileInputStream(_do.toMpegMovie());
            sendAll(bis, response);
            try {
                bis.close();
            } catch (Exception ex) {
                log.error(ex);
            }
            ;
            debugLog.info("COMPLETED INSTANCE: " + objectUID + " " + contentType + " " + (System.currentTimeMillis() - overallStart)); // 4D
        }
        // FLV request!!
        // http://127.0.0.1/o3-dpacs-wado/wado/wado.toServlet?requestType=WADO&studyUID=123456789&seriesUID=8886.1.127001.20110324103817531.8019&objectUID=9998.1.127001.20110324103818406.7855.0&contentType=video/x-flv
        // WILLY
        // http://127.0.0.1/o3-dpacs-wado/wado/wado.toServlet?requestType=WADO&studyUID=1.2.276.0.50.1.2.1077651648.1976.779466968&seriesUID=1.2.276.0.50.1.3.1077651648.1976.779466970&objectUID=1.2.276.0.50.1.4.1077651648.1976.779466967.1&contentType=video/x-flv
        if (contentType.equals("video/x-flv")) {
            log.info("Try to send an flv!");
            response.addHeader("Content-Disposition", "inline; filename=video.flv");
            Integer fps = null;
            if (request.getParameter("fps") != null) {
                try {
                    fps = Integer.parseInt(request.getParameter("fps"));
                } catch (Exception e) {
                }
            }
            // I've to check if is an mpeg or a multiframe
            File res = (fps != null) ? _do.toFlvMovie(fps) : _do.toFlvMovie();
            FileInputStream fis = new FileInputStream(res);
            response.addHeader("Content-Length", res.length() + "");
            sendAll(fis, response);
            if (!res.delete())
                log.warn("Unable to delete " + res.getAbsolutePath() + " pay attention!");
            debugLog.info("COMPLETED INSTANCE: " + objectUID + " " + contentType + " " + (System.currentTimeMillis() - overallStart)); // 4D
            return;
        }
        if (contentType.equals("application/pdf")) {
            try {
                response.addHeader("Content-Disposition", "attachment; filename=" + objectUID + ".pdf");
                FopFactory fopFactory = FopFactory.newInstance();
                TransformerFactory tFactory = TransformerFactory.newInstance();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
                Source xsltSrc = new StreamSource(this.getClass().getResourceAsStream("/it/units/htl/wado/utils/Template.xsl"));
                Transformer transformer = tFactory.newTransformer(xsltSrc);
                Document source = _do.toXmlForPdf();
                if (source == null) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "While generating the pdf, please check the log!");
                    return;
                }
                Source src = new DOMSource(_do.toXmlForPdf());
                Result res = new SAXResult(fop.getDefaultHandler());
                transformer.setParameter("wadoUrl", request.getRequestURL());
                transformer.transform(src, res);
                response.getOutputStream().write(out.toByteArray());
                response.getOutputStream().flush();
                out.close();
            } catch (Exception e) {
                log.error("While creating pdf.", e);
            }
            debugLog.info("COMPLETED INSTANCE: " + objectUID + " " + contentType + " " + (System.currentTimeMillis() - overallStart)); // 4D
        }
    }

    private void sendAll(InputStream is, HttpServletResponse response) {
        OutputStream out = null;
        try {
            out = response.getOutputStream();
            byte[] b = new byte[1024 * 64];
            int l = is.read(b);
            while (l != -1) {
                out.write(b, 0, l);
                l = is.read(b);
            }
            is.close();
            out.close();
        } catch (IOException e) {
            if (e.getClass().toString().equals("class org.apache.catalina.connector.ClientAbortException")) {
                log.debug("Aborting transfer.  It happens, no problem.", e);
            } else {
                log.error("While sending the wado response", e);
            }
        } finally {
            try {
                is.close();
                out.close();
            } catch (IOException e) {
                if (e.getClass().toString().equals("class org.apache.catalina.connector.ClientAbortException")) {
                    log.debug("Aborting transfer.  It happens, no problem.", e);
                } else {
                    log.error("While sending the wado response", e);
                }
            }
        }
    }

    protected void dicomDirProcessRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.addHeader("Content-Disposition", "attachment; filename=dicomDir.dcm");
        log.info(request.getQueryString());
        String studyUID = request.getParameter("studyUID");
        String accessionNumber = request.getParameter("accessionNumber");
        String patientId = request.getParameter("patientId");
        if (studyUID == null && accessionNumber == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request type, some parameters are missed...");
            return;
        }
        if (studyUID == null) {
            if (patientId != null) {
                studyUID = prm.getStudyUid(accessionNumber, patientId);
            } else {
                studyUID = prm.getStudyUid(accessionNumber);
            }
        }
        debugLog.info("DCMDIR_STUDY: " + studyUID); // 4D
        long overallStart = System.currentTimeMillis(); // 4D
        // String[] dicomFilesUrl = prm.getFiles(studyUID);
        DicomMatch[] dicomFilesUrl = prm.getFiles(studyUID, null, null);
        if (dicomFilesUrl == null) {
            log.error("No objects found for study " + studyUID);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request Type Not Supported");
            return;
        }
        File[] dcmFiles = new File[dicomFilesUrl.length];
        // CURRENTLY ONLY ONLINE STUDIES ARE SUPPORTED
        for (int i = 0; i < dicomFilesUrl.length; i++) {
            StringBuilder url = new StringBuilder();
            url.append(dicomFilesUrl[i].study.getFastestAccess()); // It ends with a slash
            url.append(dicomFilesUrl[i].study.getStudyInstanceUid());
            url.append(DIRECTORY_SEPARATOR).append(dicomFilesUrl[i].series.getSeriesInstanceUid());
            url.append(DIRECTORY_SEPARATOR).append(dicomFilesUrl[i].instance.getSopInstanceUid());
            dcmFiles[i] = new File(url.toString());
        }
        String tempUrl = prm.getWadoTempUrl();
        if (tempUrl == null) {
            log.warn("The WADO temporary directory was not found!!!");
        }
        DcmDirWriter ddw = new DcmDirWriter(dcmFiles, studyUID, tempUrl);
        File f = ddw.getDcmDirFile();
        if (f != null) {
            FileInputStream in = new FileInputStream(f);
            sendAll(in, response);
        }
        // eventually, delete the dcmDir file
        if (!f.delete()) {
            log.warn("cannot delete temporary DcmDir file: " + f.getAbsolutePath());
        }
        debugLog.info("COMPLETED DCMDIR_STUDY: " + studyUID + " " + (System.currentTimeMillis() - overallStart)); // 4D
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String requestType = request.getParameter("requestType");
        debugLog.info("STARTOFMONITORING" + System.currentTimeMillis());
        if (requestType.equals("WADO")) {
            wadoProcessRequest(request, response);
        } else if (requestType.equals("DcmDir")) {
            dicomDirProcessRequest(request, response);
        } else if (requestType.equals("Reload")) {
            processReload(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request Type Not Supported");
        }
        debugLog.info("ENDOFMONITORING" + System.currentTimeMillis());
    }

    protected void processReload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        prm.reloadSettings();
        log.info(RELOADMESSAGE_SUCCESS);
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException ioex) {
            log.error("Error fetching output stream", ioex);
            throw ioex;
        }
        out.println(RELOADMESSAGE_SUCCESS);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
			processRequest(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
			processRequest(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
    }

    public String getServletInfo() {
        return "Short description";
    }
}