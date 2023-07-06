/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.kos.rws;

import it.units.htl.web.kos.utils.XmlBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.w3c.dom.Document;

public class KosRwsJnlpGenerator extends HttpServlet {
    private static final long serialVersionUID = 1L;
    final Log log = LogFactory.getLog(this.getClass());
    private String _stringJNLP = "";

    public void init() {
        InputStream is = getClass().getResourceAsStream("rws.jnlp");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader bfr = new BufferedReader(isr);
        String linea = new String();
        try {
            while ((linea = bfr.readLine()) != null) {
                _stringJNLP += linea;
            }
            bfr.close();
            isr.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP GET request is not supported by KOS filter application.");
    }

    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
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
                        if (fi.getSize() > 0) {
                            found = true;
                            DicomInputStream dicomInputStream = new DicomInputStream(fi.getInputStream());
                            DicomObject kosDcmObject = dicomInputStream.readDicomObject();
                            XmlBuilder xmlBuilder = new XmlBuilder();
                            if (kosDcmObject != null) {
                                Document doc = xmlBuilder.fromKos(kosDcmObject);
                                if (doc != null) {
                                    TransformerFactory transfac = TransformerFactory.newInstance();
                                    Transformer trans = transfac.newTransformer();
                                    trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                                    trans.setOutputProperty(OutputKeys.INDENT, "yes");
                                    // create string from xml tree
                                    StringWriter sw = new StringWriter();
                                    StreamResult result = new StreamResult(sw);
                                    DOMSource sources = new DOMSource(doc);
                                    trans.transform(sources, result);
                                    String xmlString = sw.toString();
                                    log.trace("Here's the xml:\n\n" + xmlString);
//                                    building new jnlp from the request
                                    String newJnlp = _stringJNLP.replace("__CONFIGURATION__", xmlString);
                                    String codebaseURL = request.getRequestURL().toString().replaceAll("kosrwsjnlpgenerator", "rws");
                                    newJnlp = newJnlp.replaceFirst("___CODEBASE___", codebaseURL);
                                    String jaiJnlpUrl = request.getRequestURL().toString().replace("kosrwsjnlpgenerator", "ImageLibJnlpGenerator");
                                    newJnlp = newJnlp.replaceFirst("__JAICODEBASE___", jaiJnlpUrl);
//                                    sending the file
                                    response.setContentType("application/x-java-jnlp-file");
                                    response.addHeader("Content-Disposition", "inline; filename=rws.jnlp");
                                    response.getWriter().write(newJnlp);
                                }
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
                }
            } catch (Exception ex) {
                log.error("", ex);
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                } catch (IOException e) {
                }
            }
        }
    }
}
