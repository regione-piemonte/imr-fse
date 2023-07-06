/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.kos.executor;

import it.units.htl.dpacs.dao.UserManager;
import it.units.htl.maps.Users;
import it.units.htl.maps.UsersHome;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.kos.utils.XmlBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomCodingException;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.hibernate.Session;
import org.w3c.dom.Document;

public class ExecutorJnlpGenerator extends HttpServlet {
    private static final long serialVersionUID = -5426595462858462043L;
    final Log log = LogFactory.getLog(ExecutorJnlpGenerator.class);
    private String _stringJNLP = "";

    public void init() {
        InputStream is = getClass().getResourceAsStream("executor.jnlp");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader bfr = new BufferedReader(isr);
        String line = new String();
        try {
            while ((line = bfr.readLine()) != null) {
                _stringJNLP += line;
            }
            bfr.close();
            isr.close();
            is.close();
        } catch (IOException ioex) {
            log.fatal("Unable to load executor.jnlp! The executor servlet doesn't work!", ioex);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP GET request is not supported by KOS filter application.");
    }

    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            // take the dicom file
            // check if the request is multipart (if not no file will be present
            if (ServletFileUpload.isMultipartContent(request)) {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<FileItem> items;
                boolean found = false;
                FileItem kosFilter = null;
                try {
                    // check al the part of the request to get the right file
                    items = upload.parseRequest(request);
                    Iterator<FileItem> it = items.iterator();
                    while (it.hasNext()) {
                        FileItem fi = it.next();
                        if ((fi.getFieldName().equals("kosFilter")) && (fi.getSize() > 0)) {
                            found = true;
                            kosFilter = fi;
                            break;
                        }
                    }
                } catch (FileUploadException fupex) {
                    log.error("Unable to get the file from the request", fupex);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, fupex.getMessage());
                }
                // if the file is not present, a message error will be sent.
                if (!found) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file with 'kosFilter' name.");
                    return;
                } else {
                    if (kosFilter != null) {
                        // here we have the file, now we check if it's a dicom file
                        // and we read the information in it.
                        DicomInputStream dicomInputStream = null;
                        try {
                            dicomInputStream = new DicomInputStream(kosFilter.getInputStream());
                        } catch (DicomCodingException dcex) {
                            log.error("The file isn't a DICOM file! (probably corrupted)", dcex);
                            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The attached file isn't a DICOM files, probably is corrupted...");
                            return;
                        }
                        dicomInputStream.setHandler(new StopTagInputHandler(Tag.PixelData));
                        DicomObject kosDcmObject = dicomInputStream.readDicomObject();
                        if (UID.KeyObjectSelectionDocumentStorage.equals(kosDcmObject.getString(Tag.SOPClassUID))) {
                            XmlBuilder xmlBuilder = new XmlBuilder();
                            Document doc = null;
                            try {
                                doc = xmlBuilder.fromKos(kosDcmObject);
                            } catch (Exception e) {
                                log.error("Unable to create the XML File", e);
                                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                                return;
                            }
                            if (doc != null) {
                                // the xml is valid, now store the data in the db
                                // this code store the relationships inside the kos
                                // try{
                                // KosStorer s = new KosStorer();
                                // s.storeKos(kosDcmObject);
                                // }catch (Exception e) {
                                // log.error("",e);
                                // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to store KOS information in the DB");
                                // return;
                                // }
                                try {
                                    TransformerFactory transfac = TransformerFactory.newInstance();
                                    Transformer trans = transfac.newTransformer();
                                    trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                    trans.setOutputProperty(OutputKeys.INDENT, "yes");
                                    // create string from xml tree
                                    StringWriter sw = new StringWriter();
                                    StreamResult result = new StreamResult(sw);
                                    DOMSource sources = new DOMSource(doc);
                                    trans.transform(sources, result);
                                    String xmlString = sw.toString();
                                    log.trace("Here's the xml:\n\n" + xmlString);
                                    // building new jnlp from the request
                                    String newJnlp = _stringJNLP.replace("__CONFIGURATION__", xmlString);
                                    String codebaseURL = request.getRequestURL().toString();
                                    newJnlp = newJnlp.replaceFirst("___CODEBASE___", codebaseURL);
                                    // take the study that must be open
                                    DicomObject studio = kosDcmObject.get(Tag.CurrentRequestedProcedureEvidenceSequence).getDicomObject(0);
                                    newJnlp = newJnlp.replace("___STUDY_UID___", studio.getString(Tag.StudyInstanceUID));
                                    String webInterfaceURL = request.getRequestURL() + "";
                                    webInterfaceURL = webInterfaceURL.replace("executor", "");
                                    // take the flex ui location
                                    try {
                                        webInterfaceURL += new UserManager().getConfigParam("FlexUiLocation");
                                    } catch (Exception e) {
                                        log.warn("No configuration found for FlexUILocation");
                                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No configuration found for FlexUILocation!");
                                        return;
                                    }
                                    // specified the study that will be displayed
                                    webInterfaceURL += "?studyUID=" + studio.getString(Tag.StudyInstanceUID);
                                    // filtered study
                                    webInterfaceURL += "&isFiltered=true";
                                    String wadoURL = new String();
                                    try {
                                        wadoURL = new UserManager().getConfigParam("WadoUrlForFlex");
                                    } catch (NamingException e) {
                                        log.error("", e);
                                        wadoURL = request.getRequestURL() + "";
                                        wadoURL = wadoURL.replace("executor", "");
                                        wadoURL = wadoURL.replace("web", "wado");
                                    }
                                    // retrieve information for the user for flex service
                                    Users dummyForWadoService = new Users();
                                    dummyForWadoService.setUserName("WadoService");
                                    final Session s = SessionManager.getInstance().openSession();
                                    UsersHome UH = new UsersHome();
                                    List<Users> rsUser = UH.findByExample(dummyForWadoService, s);
                                    s.close();
                                    Users res = null;
                                    if (rsUser.size() > 0) {
                                        res = rsUser.get(0);
                                    } else {
                                        log.warn("No user found for the authentication, please check the DB! (username: WadoService )");
                                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No user find for the authentication, please check the DB! (username: WadoService )");
                                        return;
                                    }
                                    String credential = res.getUserName() + ":" + res.getPassword();
                                    byte[] b = credential.getBytes();
                                    // encode and don't chunk (don't add CR/LF for long strings)
                                    credential = new String(Base64.encodeBase64(b, false));
                                    webInterfaceURL += "&serviceURL=" + wadoURL + "&credentials=" + credential;
                                    webInterfaceURL += "&isNotO3DPACS=false";
                                    if (xmlBuilder.getRetrieveAeTitle() != null)
                                        webInterfaceURL += "&retrieveAeTitle=" + xmlBuilder.getRetrieveAeTitle();
                                    newJnlp = newJnlp.replace("___WEB_URL___", webInterfaceURL);
                                    // sending the file
                                    response.setContentType("application/x-java-jnlp-file");
                                    response.addHeader("Content-Disposition", "inline; filename=o3-executor.jnlp");
                                    response.getWriter().write(newJnlp);
                                } catch (TransformerException e) {
                                    log.error("something worng during the conversion from Dicom File to XML", e);
                                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                                    return;
                                }
                            }
                        } else {
                            log.error("The attached file isn't a KOS! No way to filter.");
                            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The kosFile isn't a KeyObjectSelectionDocumentStorage");
                            return;
                        }
                    }
                }
            } else {
                log.error("The request isn't multipart, no file inside!");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The request isn't multipart");
                return;
            }
        } catch (IOException ioex) {
            log.fatal("Unable to send the response!", ioex);
        }
    }
}
