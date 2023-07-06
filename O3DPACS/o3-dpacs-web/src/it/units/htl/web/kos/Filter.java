/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.kos;

import it.units.htl.web.Study.ObjectList;
import it.units.htl.web.Study.ObjectListItem;
import it.units.htl.web.Study.SerieList;
import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.utils.XmlConfigLoader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.media.DirectoryRecordType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Filter extends HttpServlet {
    private static final long serialVersionUID = 1L;
    final Log log = LogFactory.getLog(Filter.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP GET request is not supported by KOS filter application.");
    }

    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        log.info("A post request is arrived to kos filter...");
        if (ServletFileUpload.isMultipartContent(request)) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> items;
            try {
                items = upload.parseRequest(request);
                Iterator<FileItem> it = items.iterator();
                boolean found = false;
                while (it.hasNext()) {
                    FileItem fi = it.next();
                    if (fi.getFieldName().equals("kosFilter")) {
                        Dataset ds = readFile(fi, -1);
                        log.debug("The attachment is a KOS? : " + ds.getString(Tags.SOPClassUID).equals(UIDs.KeyObjectSelectionDocument));
                        if (ds.getString(Tags.SOPClassUID).equals(UIDs.KeyObjectSelectionDocument)) {
                            HttpSession session = initSession(request);
                            if (session != null) {
                                session.setAttribute("messageManager", new MessageManager());
                                SerieList filteredSeries = new SerieList();
                                found = true;
                                DcmElement studyElement = ds.get(Tags.CurrentRequestedProcedureEvidenceSeq);
                                // this code checks on pacs db if the instances are present.
                                // DataValidator dataValid = new DataValidator();
                                // DataValidator.ERROR_TYPE res = dataValid.checkKosData(studyElement);
                                // if (res != null) {
                                // switch (res) {
                                // case INSTANCE_NOT_OF_SERIES:
                                // response.sendError(HttpServletResponse.SC_BAD_REQUEST, "At least one instance presents in the KOS file doesn't belong to the referred series.");
                                // break;
                                // case SERIES_NOT_OF_STUDY:
                                // response.sendError(HttpServletResponse.SC_BAD_REQUEST, "At least one serties presents in the KOS file doesn't belong to the referred study.");
                                // break;
                                // case TOO_MANY_REFERENCED_STUDIES:
                                // response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Only one study must be refer in the KOS file.");
                                // break;
                                // default:
                                // response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error while check the KOS file");
                                // break;
                                // }
                                // return;
                                // }
                                for (int k = 0; k < studyElement.countItems(); k++) {
                                    Dataset studyDataset = studyElement.getItem(k);
                                    log.debug("The kos has this study: " + studyDataset.getString(Tags.StudyInstanceUID));
                                    DicomObject patient = new BasicDicomObject();
                                    patient.putString(Tag.PatientName, VR.PN, ds.getString(Tag.PatientName));
                                    patient.putString(Tag.PatientID, VR.LO, ds.getString(Tag.PatientID));
                                    patient.putString(Tag.DirectoryRecordType, VR.CS, DirectoryRecordType.PATIENT);
                                    DcmElement referedSeriesSequence = studyDataset.get(Tags.RefSeriesSeq);
                                    for (int i = 0; i < referedSeriesSequence.countItems(); i++) {
                                        Dataset seriesDataset = referedSeriesSequence.getItem(i);
                                        log.debug("Previous study has this series: " + seriesDataset.getString(Tags.SeriesInstanceUID));
                                        FilteredSeriesListItem sli = new FilteredSeriesListItem();
                                        sli.setSerieInstanceUid(seriesDataset.getString(Tags.SeriesInstanceUID));
                                        // sli.setSeriesDescription(dataValid.getSeries().get(seriesDataset.getString(Tags.SeriesInstanceUID)).getSeriesDescription());
                                        // sli.setModality(dataValid.getSeries().get(seriesDataset.getString(Tags.SeriesInstanceUID)).getModality());
                                        DcmElement referedSopInstances = seriesDataset.get(Tags.RefSOPSeq);
                                        sli.setNumberOfSeriesRelatedInstances(referedSopInstances.countItems() + "");
                                        ObjectList ol = new ObjectList();
                                        String instanceNotFound = "";
                                        for (int j = 0; j < referedSopInstances.countItems(); j++) {
                                            Dataset instanceDataset = referedSopInstances.getItem(j);
                                            try {
                                                ObjectListItem oli = buildObjectListItem(studyDataset.getString(Tags.StudyInstanceUID), seriesDataset.getString(Tags.SeriesInstanceUID), instanceDataset.getString(Tags.RefSOPInstanceUID), session);
                                                ol.add(oli);
                                            } catch (Exception e) {
                                                instanceNotFound += " <br> " + instanceDataset.getString(Tags.RefSOPInstanceUID);
                                                session.setAttribute("notAllFileAreDisplayed", true);
                                                sli.setInWarning(true);
                                            }
                                            log.debug("Previous series has this object:" + instanceDataset.getString(Tags.RefSOPInstanceUID));
                                        }
                                        if (!instanceNotFound.equals("")) {
                                            MessageManager.getInstance(session).setMessage("<div style='color:red; font-weight:bold'>The following instances are missing:  </div>" + instanceNotFound);
                                        }
                                        sli.setFilteredObject(ol);
                                        filteredSeries.add(sli);
                                    }
                                }
                                session.setAttribute("kosFilteredSeries", filteredSeries);
                                session.setAttribute("KosFilterManager", new FilterManager());
                                response.sendRedirect("ondemand/seriesResults.jspf");
                            } else {
                                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                            }
                        }
                    }
                }
                if (!found) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "There is not a KOS file in the request.");
                }
            } catch (FileUploadException ex) {
                log.error("", ex);
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                } catch (IOException e) {
                    log.warn("Unable to send HTTP response", e);
                }
            } catch (Exception ex) {
                log.error("", ex);
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                } catch (IOException e) {
                    log.warn("Unable to send HTTP response", e);
                }
            }
        }
    }

    // this method enable the http session on the client to workaround
    // loginFilter.
    private HttpSession initSession(HttpServletRequest request) {
        request.getSession().invalidate();
        HttpSession session = ((HttpServletRequest) request).getSession(true);
        log.debug("New session for KOS filter on demand!");
        session = ((HttpServletRequest) request).getSession(true);
        session.setAttribute("myIp", request.getServerName());
        session.setAttribute("ClientIp", request.getRemoteHost());
        session.setAttribute("myPort", request.getLocalPort() + "");
        session.setAttribute("isHttps", request.getScheme());
        session.setAttribute("isViewer", "true");
        Document webConfig = XmlConfigLoader.getConfigurationFromDB("WebConfiguration");
        if (webConfig == null) {
            log.error("WebConfiguration not found!");
            return null;
        }
        NodeList patterns = webConfig.getElementsByTagName("pattern");
        String[] areas = new String[patterns.getLength()];
        for (int i = 0; i < patterns.getLength(); i++) {
            areas[i] = patterns.item(i).getTextContent();
        }
        session.setAttribute("vwAreas", areas);
        session.setAttribute("KosFilterManager", new FilterManager());
        return session;
    }

    private ObjectListItem buildObjectListItem(String studyUid, String seriesUid, String instanceUid, HttpSession session) throws Exception {
        ObjectListItem objectListItem = new ObjectListItem();
        objectListItem.setWadoUrl(studyUid, seriesUid, instanceUid, session);
        objectListItem.setSOPinstance(instanceUid);
        String wcenter = null;
        String wwidth = null;
        String photometricInterpretation = null;
        String pixelRepresentation = null;
        String numberOfFrames = null;
        InputStream is = null;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc;
        URL url = new URL(objectListItem.getWadoUrlForXML());
        HttpsURLConnection httpsurlconnection = null;
        if ("https".equals(url.getProtocol())) {
            httpsurlconnection = (HttpsURLConnection) url.openConnection();
            httpsurlconnection.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;// (hostname.equals(session.getPeerHost()));
                }
            });
            httpsurlconnection.connect();
            is = httpsurlconnection.getInputStream();
            doc = docBuilder.parse(is);
        } else {
            is = url.openStream();
            doc = docBuilder.parse(is);
        }
        is.close();
        doc.getDocumentElement().normalize();
        // recupero l'elemento root del file
        NodeList nodeLst = doc.getElementsByTagName("root");
        Node _rootNode = nodeLst.item(0);
        Element _rootElement = (Element) _rootNode;
        NodeList fstNmElmntLst = _rootElement.getElementsByTagName("PhotometricInterpretation");
        Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
        NodeList fstNm = fstNmElmnt.getChildNodes();
        photometricInterpretation = ((Node) fstNm.item(0)).getNodeValue();
        // retrieve windowCenter and windowWidth value if image
        // is monohrome1 and monochrome2
        if (photometricInterpretation.equals("MONOCHROME1") || photometricInterpretation.equals("MONOCHROME2")) {
            fstNmElmntLst = _rootElement.getElementsByTagName("WindowCenter");
            fstNmElmnt = (Element) fstNmElmntLst.item(0);
            fstNm = fstNmElmnt.getChildNodes();
            // if value is null the windowCenter is in the
            // middle of image
            if (!((Node) fstNm.item(0)).getNodeValue().equals("null")) {
                wcenter = ((Node) fstNm.item(0)).getNodeValue();
            } else {
                wcenter = Double.toString(Double.parseDouble(objectListItem.get_maxWindowSize()) / 2);
            }
            // retrieve windowWidth value, if null windowWidth
            // is full open
            fstNmElmntLst = _rootElement.getElementsByTagName("WindowWidth");
            fstNmElmnt = (Element) fstNmElmntLst.item(0);
            fstNm = fstNmElmnt.getChildNodes();
            if (!((Node) fstNm.item(0)).getNodeValue().equals("null")) {
                wwidth = ((Node) fstNm.item(0)).getNodeValue();
            } else {
                wwidth = objectListItem.get_maxWindowSize();
            }
        }
        String transferSyntax;
        fstNmElmntLst = _rootElement.getElementsByTagName("TransferSyntax");
        fstNmElmnt = (Element) fstNmElmntLst.item(0);
        fstNm = fstNmElmnt.getChildNodes();
        transferSyntax = ((Node) fstNm.item(0)).getNodeValue();
        objectListItem.setTransferSyntax(transferSyntax);
        fstNmElmntLst = _rootElement.getElementsByTagName("PixelRepresentation");
        fstNmElmnt = (Element) fstNmElmntLst.item(0);
        fstNm = fstNmElmnt.getChildNodes();
        pixelRepresentation = ((Node) fstNm.item(0)).getNodeValue();
        fstNmElmntLst = _rootElement.getElementsByTagName("BitsStored");
        fstNmElmnt = (Element) fstNmElmntLst.item(0);
        fstNm = fstNmElmnt.getChildNodes();
        objectListItem.setSize(((Node) fstNm.item(0)).getNodeValue());
        fstNmElmntLst = _rootElement.getElementsByTagName("NumberOfFrames");
        fstNmElmnt = (Element) fstNmElmntLst.item(0);
        fstNm = fstNmElmnt.getChildNodes();
        numberOfFrames = ((Node) fstNm.item(0)).getNodeValue();
        fstNmElmntLst = _rootElement.getElementsByTagName("ImagePosition");
        fstNmElmnt = (Element) fstNmElmntLst.item(0);
        NodeList positionNode = fstNmElmnt.getChildNodes();
        if (positionNode.getLength() > 0) {
            String[] imagePosition = new String[positionNode.getLength()];
            for (int pos = 0; pos < positionNode.getLength(); pos++) {
                imagePosition[pos] = ((Node) positionNode.item(pos)).getTextContent();
            }
            objectListItem.setImagePosition(imagePosition);
        }
        fstNmElmntLst = _rootElement.getElementsByTagName("Modality");
        fstNmElmnt = (Element) fstNmElmntLst.item(0);
        objectListItem.setModality(fstNmElmnt.getTextContent());
        objectListItem.setIsMultiframe(numberOfFrames);
        objectListItem.setWcenter(wcenter);
        objectListItem.setWwidth(wwidth);
        objectListItem.set_pixelRepresentation(pixelRepresentation);
        objectListItem.set_photmetricInterpretation(photometricInterpretation);
        return objectListItem;
    }

    private Dataset readFile(FileItem f, int Until) throws IOException {
        InputStream in = f.getInputStream();
        BufferedInputStream dcm_in = new BufferedInputStream(in, 1000000);
        dcm_in.mark(1000000);
        DcmParser dcm_dp = DcmParserFactory.getInstance().newDcmParser(dcm_in);
        FileFormat dcm_ff;
        dcm_ff = dcm_dp.detectFileFormat();
        Dataset dcm_ds = DcmObjectFactory.getInstance().newDataset();
        dcm_ds.readFile(dcm_in, dcm_ff, Until);
        dcm_in.close();
        dcm_in = null;
        dcm_ff = null;
        in.close();
        return dcm_ds;
    }
}
