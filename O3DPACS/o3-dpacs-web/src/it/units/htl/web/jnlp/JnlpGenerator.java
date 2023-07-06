/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.jnlp;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.helpers.GlobalSettings;
import it.units.htl.web.utils.XmlConfigLoader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Servlet implementation class JnlpGenerator
 */
public class JnlpGenerator extends HttpServlet {
    private Log log = LogFactory.getLog(JnlpGenerator.class);
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public JnlpGenerator() {
        super();
    }

    private static final String SERVICENAME = "WebSettings";
    private static final String SERVICEPARAM_XMX = "jnlpXmx";
    private static final String DEFAULTCONFIG_XMX = "512m";
    private static final String DEFAULT_DATA = "-data=";
    private static final String DEFAULT_DATASOURCE = "-datasource=url";
    private static final String DEFAULT_WORKSPACE = "workspace=\"/temp/\"";
    private static final String PATTERN_XMX = "[0-9]+[kKmMgG]";
    private static final String TAG_APPLICATION_ARGUMENT = "argument";
    
    
    private static final String kRwsConfDir = "RwsConfDir";   
    private static final String kRwsXmlConfigUrl = "RwsXmlConfigUrl";
    private static final String kRwsIntegrationType = "RwsIntegrationType";
    
    private static final String INTEGRATIONTYPE_XMLGATEWAY = "XmlGateway";
    private static final String INTEGRATIONTYPE_QUERYTOPACS = "QueryToPacs";
    
    
    private static final String ACCESSIONNUMBER_PLACEHOLDER = "___ACCESSIONNUMBER___";
    private static final String ADDITIONAL_AUTHENTICATION = "___ADDITIONAL_AUTH___";
    private static final String PATIENTID_PLACEHOLDER = "___PATIENTID___";
    private static final String CONFDIR_PLACEHOLDER = "__RWS__CONF__DIR__";
    private static final String CONFIGURATION_URL_PLACEHOLDER = "__XML__CONFIG__URL__";
    private static final String DCMDIR_URL_PLACEHOLDER = "__WADO__DICOMDIR__URL__";
	private static final String COOKIE_STATE = "_shibstate_";
	private static final String COOKIE_SESSION = "_shibsession_";
	private static final String COOKIE_SAML = "_saml_idp";
	private static final String GATEWAY_PLACEHOLDER = "___GATEWAY_RWS___";
	private static final String XMX_PLACEHOLDER = "___XMX___";
	private static final String CODEBASE_PLACEHOLDER = "___CODEBASE___";
	private static final String IMAGELIBRARYCODEBASE_PLACEHOLDER = "__IMAGELIBRARYCODEBASE___";
	private static final String CUSTOMERLIBCODEBASE_PLACEHOLDER = "__CUSTOMERLIBCODEBASE__";
	private static final String DEFAULT_XML_BUILDER = "getStudyXml?studyUid=";

    private String getXmx() {
        String ret = DEFAULTCONFIG_XMX;
        Document config = XmlConfigLoader.getConfigurationFromDB(SERVICENAME);
        if (config != null) {
            NodeList nodes = config.getElementsByTagName(SERVICEPARAM_XMX);
            try {
                String xmxValue = nodes.item(0).getTextContent();
                Pattern mask = Pattern.compile(PATTERN_XMX);
                Matcher matcher = mask.matcher(xmxValue);
                if (matcher.matches()) {
                    ret = xmxValue;
                } else {
                    log.warn("The Xmx value does not abide by the syntax. The default Xmx will be used: " + DEFAULTCONFIG_XMX);
                }
            } catch (Exception ex) {
                log.warn("The default Xmx will be used: " + DEFAULTCONFIG_XMX, ex);
            }
        } else {
            log.warn("No ServiceConfiguration found for " + SERVICENAME + ". The default Xmx will be used: " + DEFAULTCONFIG_XMX);
        }
        return ret;
    }

    public void init() {
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/x-java-jnlp-file");
        response.addHeader("Content-Disposition",
                "inline; filename=rws.jnlp");
        
        
        String newJNLP = GlobalConfigurationLoader.getConfigParam("RwsJnlpStandard");
        String vRwsConfDir = GlobalConfigurationLoader.getConfigParam(kRwsConfDir);
        String vRwsXmlConfigUrl = GlobalConfigurationLoader.getConfigParam(kRwsXmlConfigUrl);
        String vRwsIntegrationType = GlobalConfigurationLoader.getConfigParam(kRwsIntegrationType);
        
        
        String dcmDirURL = request.getParameter("studyURL");
        // insert wado url to retrieve dicomdir obj of study
        if ((vRwsIntegrationType == null || !INTEGRATIONTYPE_QUERYTOPACS.equals(vRwsIntegrationType))) {
        	if(INTEGRATIONTYPE_XMLGATEWAY.equals(vRwsIntegrationType)){   
        		
        		StringBuilder gatewayConfig=new StringBuilder();
            	gatewayConfig.append(getArgument(vRwsConfDir));
            	gatewayConfig.append(getArgument(DEFAULT_DATASOURCE));
            	gatewayConfig.append(getArgument(DEFAULT_DATA+GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.CONFIG_WADOURL_FLEX)+DEFAULT_XML_BUILDER+request.getParameter("studyUID")));
            	newJNLP = newJNLP.replaceFirst(GATEWAY_PLACEHOLDER, gatewayConfig.toString());
        		
        		newJNLP = newJNLP.replaceFirst(DCMDIR_URL_PLACEHOLDER,"");
	            newJNLP = newJNLP.replaceFirst(PATIENTID_PLACEHOLDER, "");
	            newJNLP = newJNLP.replaceFirst(ACCESSIONNUMBER_PLACEHOLDER, "");
	            newJNLP = newJNLP.replaceFirst(CONFIGURATION_URL_PLACEHOLDER, "");
	            newJNLP = newJNLP.replaceFirst(CONFDIR_PLACEHOLDER, "");
	            
        	}else{
        		if(dcmDirURL == null || "".equals(dcmDirURL)){
	                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing studyURL for DICOM dir.");
	                return;
	            }
	            if (request.getParameter("studyUID") != null) {
	                dcmDirURL += "&amp;studyUID=" + request.getParameter("studyUID");
	            } else {
	                if (request.getParameter("patientId") != null) {
	                    dcmDirURL += "&amp;patientId=" + request.getParameter("patientId");
	                }
	                dcmDirURL += "&amp;accessionNumber=" + request.getParameter("accessionNumber");
	            }
	            newJNLP = newJNLP.replaceFirst(DCMDIR_URL_PLACEHOLDER,getArgument(dcmDirURL));
	            newJNLP = newJNLP.replaceFirst(PATIENTID_PLACEHOLDER, getArgument(""));
	            newJNLP = newJNLP.replaceFirst(ACCESSIONNUMBER_PLACEHOLDER, getArgument(""));
	            newJNLP = newJNLP.replaceFirst(GATEWAY_PLACEHOLDER, "");
        	}
        } else {
            newJNLP = newJNLP.replaceFirst(DCMDIR_URL_PLACEHOLDER,getArgument("OPENFROM_PIDandACN"));
            String patientId = request.getParameter("patientId");
            String accNum = request.getParameter("accessionNumber");
            if(patientId == null || "".equals(patientId)){
                patientId = getPatientIdFromAccNum(accNum);
            }
            newJNLP = newJNLP.replaceFirst(PATIENTID_PLACEHOLDER, getArgument(patientId));
            newJNLP = newJNLP.replaceFirst(ACCESSIONNUMBER_PLACEHOLDER, getArgument(accNum));
            newJNLP = newJNLP.replaceFirst(GATEWAY_PLACEHOLDER, "");
        }

        newJNLP = newJNLP.replaceFirst(CONFIGURATION_URL_PLACEHOLDER, getArgument(vRwsXmlConfigUrl));
        newJNLP = newJNLP.replaceFirst(CONFDIR_PLACEHOLDER, getArgument(vRwsConfDir));
        
        
        // replace codebase with webserver's address
        String codebaseURL = GlobalConfigurationLoader.getConfigParam("RwsCodebaseUrl");
        newJNLP = newJNLP.replaceFirst(XMX_PLACEHOLDER, getXmx());
        newJNLP = newJNLP.replaceFirst(CODEBASE_PLACEHOLDER, codebaseURL);
        String jaiJnlpUrl = request.getRequestURL().toString().replace("JnlpGenerator", "ImageLibJnlpGenerator");
        newJNLP = newJNLP.replaceFirst(IMAGELIBRARYCODEBASE_PLACEHOLDER, jaiJnlpUrl);
        String customerLibJnlp = request.getRequestURL().toString().replace("JnlpGenerator", "CustLibJnlpGenerator");
        
        newJNLP = newJNLP.replaceFirst(CUSTOMERLIBCODEBASE_PLACEHOLDER, customerLibJnlp);
        
        
        
        Cookie cookies[] = request.getCookies();
	    if (cookies != null) {
	    	String shibState="";
	    	String shibSession="";
	    	String samlIdp="";
			for (int i = 0; i < cookies.length; i++) {
				Cookie c = cookies[i];
				log.debug("Cookie - name: " + c.getName() + " - value: " + c.getValue() + " - comment: " + c.getComment() + " - expires: " + c.getMaxAge());
				if (c.getName().startsWith(COOKIE_STATE)) {
					shibState=getArgument(c.getName())+getArgument(c.getValue());
				} else if (c.getName().startsWith(COOKIE_SESSION)) {
					shibSession=getArgument(c.getName())+getArgument(c.getValue());
				} else if (c.getName().equalsIgnoreCase(COOKIE_SAML)) {
					samlIdp=getArgument(c.getValue());
				}
			}
			newJNLP = newJNLP.replaceFirst(ADDITIONAL_AUTHENTICATION, shibState+shibSession+samlIdp);
	    }else{
	    	newJNLP = newJNLP.replaceFirst(ADDITIONAL_AUTHENTICATION, "");
	    }
        
        response.getWriter().write(newJNLP);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        
    }
    
    private String getPatientIdFromAccNum(String accNum){
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String patientId = null; 
        try{
            con = getConnection();
            ps = con.prepareStatement("SELECT patientId FROM Patients pt " +
            		"INNER JOIN Studies st on st.patientFk = pt.pk " +
            		"WHERE st.accessionNumber = ?");
            ps.setString(1, accNum);
            ps.execute();
            rs = ps.getResultSet();
            if(rs.next())
                patientId = rs.getString(1);
        }catch(Exception ex){
            log.error("While retrieving patientId ", ex);
        }finally{
            CloseableUtils.close(rs);
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
        }
        return patientId;
    }
    
    private Connection getConnection() throws NamingException, SQLException {
        DataSource ds;
        ds = InitialContext.doLookup("java:/jdbc/dbDS");
        return ds.getConnection();
    }
    
    private String getArgument(String arg){
        if(arg != null && !"".equals(arg)){
            return "<" + TAG_APPLICATION_ARGUMENT + ">" + arg + "</" + TAG_APPLICATION_ARGUMENT + ">";
        }else{
            return ""; 
        }
    }
    
}
