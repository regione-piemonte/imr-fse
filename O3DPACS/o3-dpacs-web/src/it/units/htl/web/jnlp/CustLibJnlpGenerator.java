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

public class CustLibJnlpGenerator extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Log log = LogFactory.getLog(CustLibJnlpGenerator.class);
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/x-java-jnlp-file");
        response.addHeader("Content-Disposition",
                "attachment; filename=custLibJnlp.jnlp");
        String newJNLP = GlobalConfigurationLoader.getConfigParam("CustLibJnlpStandard"); 
        
        String codebaseURL = GlobalConfigurationLoader.getConfigParam("CustLibCodebaseUrl");
        log.debug("The customer library codebase is: " + codebaseURL);
        newJNLP = newJNLP.replaceFirst("___CODEBASE___", codebaseURL);
        response.getWriter().write(newJNLP);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
        
    }
}
