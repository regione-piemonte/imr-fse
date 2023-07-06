/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.viewer;

import it.units.htl.dpacs.dao.UserManager;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.web.utils.BuildStudyResult;
import it.units.htl.web.utils.XmlConfigLoader;
import it.units.htl.web.viewer.auth.utils.KTCServiceStub;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ViewerFilter implements Filter {
    private Log log = LogFactory.getLog(this.getClass());
    
    private static final String SERVICENAME="WebSettings";
    private static final String SERVICEPARAM_VIEWPARAM="viewParameter";
    
    private String fieldToUse;

	private static final String PARAM_FILTERVALUE="fv";
	private static final String PARAM_FILTERVALUE_LEGACY="aN";

    // private FilterConfig fc = null;
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestURI = ((HttpServletRequest) request).getRequestURI();
        String queryString = ((HttpServletRequest) request).getQueryString();
        HttpSession session = ((HttpServletRequest) request).getSession(true);
        if (session.isNew()) {
            log.info("New session for o3-dpacs-viewer!");
            // session = ((HttpServletRequest) request).getSession(true);
            session.setAttribute("myIp", request.getServerName());
            session.setAttribute("ClientIp", request.getRemoteHost());
            session.setAttribute("myPort", request.getLocalPort() + "");
        }
        session.setAttribute("isHttps", request.getScheme());
        if (!requestURI.subSequence(requestURI.lastIndexOf("/"), requestURI.length()).equals("/seriesResults.view")) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            String filterValue = request.getParameter(PARAM_FILTERVALUE);
            if(!paramIsValid(filterValue))
            	filterValue=request.getParameter(PARAM_FILTERVALUE_LEGACY);
            if (!paramIsValid(filterValue) && !paramIsValid((String) session.getAttribute("isViewer"))) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Filter value is missing in request.");
                return;
            }
            if (queryString != null) {
                if (!paramIsValid((String) session.getAttribute("isViewer")) || !session.getAttribute("isViewer").equals(filterValue)) {
                    // here the system must get the configuration for the authentication
                    Document authConfig = null;
                    authConfig = XmlConfigLoader.getConfigurationFromDB("ViewerAuthentication");
                    if (authConfig != null) {
                        NodeList config = authConfig.getElementsByTagName("configuration");
                        Element _rootNode = (Element) config.item(0);
                        NodeList requiredParams = _rootNode.getElementsByTagName("requiredParam");
                        HashMap<String, String> paramValue = new HashMap<String, String>();
                        for (int i = 0; i < requiredParams.getLength(); i++) {
                            String paramName = requiredParams.item(i).getTextContent();
                            if (paramName != null) {
                                if (paramIsValid(request.getParameter(paramName))) {
                                    paramValue.put(paramName, request.getParameter(paramName));
                                } else {
                                    log.error("Missing some authentication param");
                                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Missing some parameters in the request, unable to make authentication. MISSED PARAM IS: " + paramName);
                                    return;
                                }
                            } else {
                                log.error("Error in ViewerAuthentication configuration! please check the DB");
                                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Wrong configuration for the required params list.");
                                return;
                            }
                        }
                        // here the system has check all the params
                        KTCServiceStub stub = null;
                        try {
                            stub = new KTCServiceStub("http://"+paramValue.get("server")+"/ktc/Custom.KS.KTCService.cls");
                        } catch (AxisFault afex) {
                            log.error("Unable to istantiate WS Stub", afex);
                            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication failed. Please see the server log. " + afex.getMessage());
                            return;
                        }
                        stub._getServiceClient().getOptions().setProperty(HTTPConstants.HTTP_PROTOCOL_VERSION, HTTPConstants.HEADER_PROTOCOL_10);
                        KTCServiceStub.GetSessionInfo sinfo = new KTCServiceStub.GetSessionInfo();
                        
                        
                        sinfo.setServer(paramValue.get("server"));
                        sinfo.setUser(paramValue.get("user"));
                        sinfo.setSessionId(paramValue.get("sessionId"));

                        KTCServiceStub.GetSessionInfoResponse resp = null;
                        try {
                            resp = stub.getSessionInfo(sinfo);
                        } catch (RemoteException e) {
                            log.error("Unable to get session info from WS.", e);
                            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication failed. Please see the server log. " + e.getMessage());
                            return;
                        }
                        log.info("User: " + resp.getGetSessionInfoResult().getLogonUserName() + " " +  "Cookie: " + resp.getGetSessionInfoResult().getSessionCookie());
                        String authUser = resp.getGetSessionInfoResult().getLogonUserName();
                        String cookie = resp.getGetSessionInfoResult().getSessionCookie();
                        if ((authUser == null) || (cookie == null) ||
                                ("".equals(authUser)) || ("".equals(cookie)) ||
                                ("null".equals(authUser) || ("null".equals(cookie)))) {
                            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
                            return;
                        }
                    } else {
                        log.info("Authentication for viewer is disabled, keep attention.");
                    }
                    Document webConfig = null;
                    webConfig = XmlConfigLoader.getConfigurationFromDB("WebConfiguration");
                    if (webConfig == null) {
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND, "WebConfiguration not found, please contact the admininstrator");
                        return;
                    }
                    NodeList patterns = webConfig.getElementsByTagName("pattern");
                    String[] areas = new String[patterns.getLength()];
                    for (int i = 0; i < patterns.getLength(); i++) {
                        areas[i] = patterns.item(i).getTextContent();
                    }
                    //String whoFilter = (session.getServletContext().getInitParameter("ViewerFilterField") != null) ? session.getServletContext().getInitParameter("ViewerFilterField") : "accessionNumber";
                    session.setAttribute("vwAreas", areas);
//                  setup wadoURL config param
                    String wUrl="";
                    if(session.getAttribute(ConfigurationSettings.CONFIG_WADOURL)==null){
                        try {
                            wUrl=new UserManager().getConfigParam(ConfigurationSettings.CONFIG_WADOURL);
                        } catch (NamingException nex) {
                            log.error("Error retrieving WadoUrl",nex);
                            wUrl="";
                        }
                        session.setAttribute(ConfigurationSettings.CONFIG_WADOURL, wUrl);
                    }
                    BuildStudyResult bs = new BuildStudyResult();
                    if (bs.getSeries(this.fieldToUse, filterValue, session)) {
                        session.setAttribute("isViewer", filterValue);
                        chain.doFilter(request, response);
                    } else {
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND, "Sorry! Something goes wrong when looking for series of the study, please contact administrator.");
                        return;
                    }
                } else {
                    chain.doFilter(request, response);
                }
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    private boolean paramIsValid(String param) {
    	return ((param!=null)&&(param.length()>0));
    }

    private String initFieldToUse(){
    	String ret=null;
    	
    	Document config = XmlConfigLoader.getConfigurationFromDB(SERVICENAME);
    	if(config!=null){
			NodeList nodes=config.getElementsByTagName(SERVICEPARAM_VIEWPARAM);
			try{
				ret=nodes.item(0).getTextContent();
			}catch(Exception ex){
				log.warn(SERVICEPARAM_VIEWPARAM+" NOT defined. The default settings will be used");
				ret=null;
			}	
		}else{
			log.warn(SERVICEPARAM_VIEWPARAM+" NOT defined. The default settings will be used");
		}
    	
    	ret=StudyParameterMap.getMappedField(ret);
    	
    	return ret;
    } 
    
    public void init(FilterConfig filterConfig) throws ServletException {
    	this.fieldToUse=initFieldToUse();
    }
}