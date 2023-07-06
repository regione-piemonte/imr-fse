/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.wado;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.units.htl.wado.dao.PacsRetrieveManager;
import it.units.htl.wado.utils.DcmDirWriter;

public class DicomDirWriter extends HttpServlet {

	private static final long serialVersionUID = 5309293897528427684L;
	private Log log = LogFactory.getLog(DicomDirWriter.class);
	
	public void init(ServletConfig config) throws ServletException {
		log.info("Init " + getClass().getName() + " servlet...");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.info("doGet...");
		doWork(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.info("doPost...");
		doWork(request, response);
	}
	
	protected synchronized void doWork(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		String studiesByComma = request.getParameter("studies");
		log.info("Studies by comma: " + studiesByComma);
		List<String> studies = Arrays.asList(studiesByComma.split(","));
		log.info("Studies for DICOMDIR: " + studies);
		
		PacsRetrieveManager prm;
		try {
			prm = new PacsRetrieveManager();
			
			String tempUrl = prm.getWadoTempUrl();
	        if (tempUrl == null) {
	            log.warn("The WADO temporary directory was not found");
	        }
	        log.info("Creating DCMDIR generator...");
	        DcmDirWriter ddw = new DcmDirWriter(studies, tempUrl, prm);
	        log.info("DCMDIR generator created");
	        File dicomDirfile = ddw.getDcmDirFile();
	        if (dicomDirfile != null) {
	            FileInputStream in = new FileInputStream(dicomDirfile);
	            sendDicomDir(in, response);
	        }
	        if (!dicomDirfile.delete()) {
	            log.warn("Cannot delete temporary DcmDir file: " + dicomDirfile.getAbsolutePath());
	        }
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	private void sendDicomDir(InputStream is, HttpServletResponse response) {
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
                log.debug("Aborting transfer due to: ", e);
            } else {
                log.error("While sending the wado response: ", e);
            }
        } finally {
            try {
                is.close();
                out.close();
            } catch (IOException e) {
                if (e.getClass().toString().equals("class org.apache.catalina.connector.ClientAbortException")) {
                    log.debug("Aborting transfer due to: ", e);
                } else {
                    log.error("While sending the wado response: ", e);
                }
            }
        }
    }
}
