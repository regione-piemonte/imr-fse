/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.jnlp;

import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImageLibJnlpGenerator extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Log log = LogFactory.getLog(ImageLibJnlpGenerator.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageLibJnlpGenerator() {
        super();
    }

    public void init() {
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/x-java-jnlp-file");
        response.addHeader("Content-Disposition", "attachment; filename=imageLib.jnlp");
        String newJNLP = GlobalConfigurationLoader.getConfigParam("ImageLibJnlpStandard");
        String codebaseURL = GlobalConfigurationLoader.getConfigParam("ImageLibCodebaseUrl");
        log.info(codebaseURL);
        newJNLP = newJNLP.replaceFirst("___CODEBASE___", codebaseURL);
        response.getWriter().write(newJNLP);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
    }
}
