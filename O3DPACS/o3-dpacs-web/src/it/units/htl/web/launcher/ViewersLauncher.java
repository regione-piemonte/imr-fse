/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.launcher;

import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.web.launcher.utils.AESUtils;
import it.units.htl.web.launcher.utils.UrlPatternManipulator;
import it.units.htl.web.launcher.utils.ViewerConfigRetriever;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.jboss.logging.MDC;
import org.apache.log4j.MDC;

import eu.o3c.o3e.libraries.ws.TokenVerifier;

public class ViewersLauncher extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Log log = LogFactory.getLog(ViewersLauncher.class);
    private TokenVerifier nn = new TokenVerifier();

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse) Mandatory params: accNum idPat IDViewer Token
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AESUtils aes = new AESUtils();
        String context = System.currentTimeMillis() + "";
        MDC.put("requestContext", context);
        
        
        try {           
            
            if (req.getParameter("encrData") != null && !"".equals(req.getParameter("encrData"))) {
                // get the key
                String secretKey = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.ENC_KEY);
                log.trace("The key is " + secretKey);
                aes.setKeyValue(secretKey);
                log.debug("Encr data: " + req.getParameter("encrData"));
                String decrittedReq = aes.domachoDecrypt(req.getParameter("encrData"));
                log.debug("Decrytted data is : " + decrittedReq);
                HashMap<String, String> inputs = checkRequest(decrittedReq);
                if (inputs != null) {
                    log.debug("Verify the token");
                    if (nn.verifyToken(inputs.get("Token"))) {
                        log.debug("Get information about the viewer : " + inputs.get("IDViewer"));
                        ViewerConfigRetriever vcr = new ViewerConfigRetriever(inputs.get("IDViewer"));
                        String urlPattern = vcr.getConfigurationValue("urlPattern");
                        if (urlPattern != null && !"".equals(urlPattern)) {
                            log.debug("Manipulates the URL  pattern!");
                            String newUrl = UrlPatternManipulator.manipulateUrl(urlPattern, inputs);
                            log.debug(UrlPatternManipulator.manipulateUrl(urlPattern, inputs));
                            if (newUrl != null) {
                                resp.getWriter().write(newUrl);
//                                resp.sendRedirect(newUrl);
                            } else {
                                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while manipulating url!");
                                return;
                            }
                        } else {
                            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing URL Pattern for  " + inputs.get("IDViewer"));
                            return;
                        }
                    }else{
                        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token not verified!!!");
                        return;
                    }
                } else {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing some parameters!");
                    return;
                }
            } else {
                log.error("Missing encrData param!");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing some parameters!");
                return;
            }
        } catch (Exception e) {
            log.error("", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        } finally {
            MDC.remove("requestContext");
        }
    }

    private HashMap<String, String> checkRequest(String request) {
        HashMap<String, String> inputs = new HashMap<String, String>();
        String[] parameters = request.split("&");
        if (parameters.length >= 4) {
            for (int i = 0; i < parameters.length; i++) {
                String param = parameters[i];
                String[] splitted = param.split("=");
                if (splitted.length == 2)
                    inputs.put(splitted[0], splitted[1]);
                else
                    inputs.put(splitted[0], null);
            }
            if ((inputs.get("accNum") == null) || ("".equals(inputs.get("accNum"))))
                return null;
            if ((inputs.get("idPat") == null) || ("".equals(inputs.get("idPat"))))
                return null;
            if ((inputs.get("IDViewer") == null) || ("".equals(inputs.get("IDViewer"))))
                return null;
            if ((inputs.get("Token") == null) || ("".equals(inputs.get("Token"))))
                return null;
            return inputs;
        }
        return null;
    }
}
