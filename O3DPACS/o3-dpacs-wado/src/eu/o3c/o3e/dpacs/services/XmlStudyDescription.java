/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.dpacs.services;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.LogMessage;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;




public class XmlStudyDescription extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final String TAG_ROOT="Root";
    private static final String TAG_CONFIGURATION="Configuration";
    private static final String TAG_INTEGRATION="Integration";
    private static final String TAG_RWSSTYLE="RwsStyle";
    private static final String TAG_SECURE="SecureConnection";
    private static final String TAG_RETRIEVE="RetrieveMode";
    private static final String TAG_WTL="Wtl";
    private static final String TAG_PATIENT="Patient";
    private static final String TAG_PATIENTNAME="PatientName";
    private static final String TAG_PATIENTID="PatientID";
    private static final String TAG_PATIENTBIRTHDATE="PatientBirthDate";
    private static final String TAG_PATIENTSEX="PatientSex";
    private static final String TAG_STUDY="Study";
    private static final String TAG_STUDYINSTANCEUID="StudyInstanceUID";
    private static final String TAG_ACCNUM="AccessionNumber";
    private static final String TAG_STUDYDATE="StudyDate";
    private static final String TAG_STUDYDESCRIPTION="StudyDescription";
    private static final String TAG_SERIES="Series";
    private static final String TAG_SERIESINSTANCEUID="SeriesInstanceUID";
    private static final String TAG_SERIESDATE="SeriesDate";
    private static final String TAG_SERIESTIME="SeriesTime";
    private static final String TAG_MODALITY="Modality";
    private static final String TAG_SERIESDESCRIPTION="SeriesDescription";
    private static final String TAG_SERIESNUMBER="SeriesNumber";
    private static final String TAG_INSTANCE="Instance";
    private static final String TAG_SOPINSTANCEUID="SOPInstanceUID";
    private static final String TAG_REFERENCEDFILEID="ReferencedFileID";
    private static final String TAG_INSTANCENUMBER="InstanceNumber";
    private static final String TAG_REFERENCEDSOPCLASSUID="ReferencedSOPClassUID";

    private Log log = LogFactory.getLog(XmlStudyDescription.class);
    private DataSource dataSource;
    
 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public XmlStudyDescription() {
        super();
        try {
            Context jndiContext = new InitialContext(); // Lazy Initialization
            dataSource = (DataSource) jndiContext.lookup("java:/jdbc/dbDS");
        } catch (NamingException nex) {
            log.fatal(LogMessage._NoDatasource, nex);
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doWork(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //doWork(request, response);
    }

    private void doWork(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String studyUid=request.getParameter("studyUid");
    	try{
    		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(response.getOutputStream());
            writer.writeStartElement(TAG_ROOT);
            writer.writeEmptyElement(TAG_CONFIGURATION);
            writer.writeStartElement(TAG_INTEGRATION);
	            writer.writeStartElement(TAG_RWSSTYLE);
	            writer.writeCharacters("lite");
	            writer.writeEndElement();
	            writer.writeStartElement(TAG_SECURE);
	            writer.writeCharacters("false");
	            writer.writeEndElement();
	            writer.writeStartElement(TAG_RETRIEVE);
	            writer.writeCharacters("wadourl");
	            writer.writeEndElement();
            writer.writeEndElement();	// End Integration
            writer.writeStartElement(TAG_WTL);
            
            manageQueryData(writer, studyUid);
            writer.writeEndElement();
            writer.writeEndElement();
            writer.close();
    	}catch(Exception ex){
    		
    	}finally{
    		
    	}
    }

	private void manageQueryData(XMLStreamWriter writer, final String studyUid) throws XMLStreamException {
		
		Connection con = null;
		CallableStatement csStudies=null;
		CallableStatement csSeries=null;
		CallableStatement csInstances=null;
		ResultSet rsStudies=null;
		ResultSet rsSeries=null;
		ResultSet rsInstances=null;
		
		try{
			
			con = dataSource.getConnection();
			
			boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
            	csStudies = con.prepareCall("{call getStudyLevelMetadata(?,?)}");
            	csStudies.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
            	csStudies = con.prepareCall("{call getStudyLevelMetadata(?)}");
            }
            csStudies.setString(1, studyUid);
            csStudies.execute();
            if (isOracle) {
            	rsStudies = (ResultSet) csStudies.getObject(2);
            } else {
            	rsStudies = csStudies.getResultSet();
            }
           	
            if ((rsStudies!=null) && (rsStudies.next()) && (rsStudies.getString(5)!=null)) {
            	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
            	String baseUrl=rsStudies.getString(9);
            	if(baseUrl==null)
            		baseUrl="";
            	if(!baseUrl.contains("?")){
            		baseUrl+="?";
            	}else if(!baseUrl.endsWith("&")){
            		baseUrl+="&";
            	}
            	writer.writeStartElement(TAG_PATIENT);
            		writer.writeStartElement(TAG_PATIENTNAME);
            		writeCharacters(writer, rsStudies.getString(1));
            		writer.writeEndElement();
            		writer.writeStartElement(TAG_PATIENTID);
            		writeCharacters(writer, rsStudies.getString(2));
            		writer.writeEndElement();
            		writer.writeStartElement(TAG_PATIENTBIRTHDATE);
            		Date temp=rsStudies.getDate(3);
            		if(temp!=null)
            			writeCharacters(writer, sdf.format(temp));
            		writer.writeEndElement();
            		writer.writeStartElement(TAG_PATIENTSEX);
            		writeCharacters(writer, rsStudies.getString(4));
            		writer.writeEndElement();
            		writer.writeStartElement(TAG_STUDY);
            			writer.writeStartElement(TAG_STUDYINSTANCEUID);
            			writeCharacters(writer, rsStudies.getString(5));
            			writer.writeEndElement();	
            			writer.writeStartElement(TAG_ACCNUM);
            			writeCharacters(writer, rsStudies.getString(6));
            			writer.writeEndElement();
            			
            			writer.writeStartElement(TAG_STUDYDATE);
                		temp=rsStudies.getDate(7);
                		if(temp!=null)
                			writeCharacters(writer, sdf.format(temp));
                		writer.writeEndElement();
                		
            			writer.writeStartElement(TAG_STUDYDESCRIPTION);
            			writeCharacters(writer, rsStudies.getString(8));
            			writer.writeEndElement();
            			
            			if (isOracle) {
                        	csSeries = con.prepareCall("{call getSeriesLevelMetadata(?,?)}");
                        	csSeries.registerOutParameter(2, OracleTypes.CURSOR);
                        } else {
                        	csSeries = con.prepareCall("{call getSeriesLevelMetadata(?)}");
                        }
            			csSeries.setString(1, studyUid);
            			csSeries.execute();
                        if (isOracle) {
                        	rsSeries = (ResultSet) csSeries.getObject(2);
                        } else {
                        	rsSeries = csSeries.getResultSet();
                        }
                        if(rsSeries!=null){
                        	
                        	while(rsSeries.next()){
                        		writer.writeStartElement(TAG_SERIES);
                        			String seriesUid=rsSeries.getString(1);
	                        		writer.writeStartElement(TAG_SERIESINSTANCEUID);
	                        		writeCharacters(writer, seriesUid);
	                        		writer.writeEndElement();
	                        		writer.writeStartElement(TAG_MODALITY);
	                        		writeCharacters(writer, rsSeries.getString(2));
	                        		writer.writeEndElement();
	                        		writer.writeStartElement(TAG_SERIESDESCRIPTION);
	                        		writeCharacters(writer, rsSeries.getString(3));
	                        		writer.writeEndElement();	
	                        		writer.writeStartElement(TAG_SERIESNUMBER);
	                        		writeCharacters(writer, rsSeries.getString(4));
	                        		writer.writeEndElement();
	                        		writer.writeEmptyElement(TAG_SERIESDATE);
	                        		writer.writeEmptyElement(TAG_SERIESTIME);
	                        		
	                        		if (isOracle) {
	                                	csInstances = con.prepareCall("{call getInstanceLevelMetadata(?,?)}");
	                                	csInstances.registerOutParameter(2, OracleTypes.CURSOR);
	                                } else {
	                                	csInstances = con.prepareCall("{call getInstanceLevelMetadata(?)}");
	                                }
	                        		csInstances.setString(1, seriesUid);
	                        		csInstances.execute();
	                                if (isOracle) {
	                                	rsInstances = (ResultSet) csInstances.getObject(2);
	                                } else {
	                                	rsInstances = csInstances.getResultSet();
	                                }
	                                
	                                if(rsInstances!=null){
	                                	while(rsInstances.next()){
	                                		writer.writeStartElement(TAG_INSTANCE);
	                                		String instanceUid=rsInstances.getString(1);
		                                		writer.writeStartElement(TAG_SOPINSTANCEUID);
		    	                        		writeCharacters(writer, instanceUid);
		    	                        		writer.writeEndElement();
		    	                        		writer.writeStartElement(TAG_REFERENCEDSOPCLASSUID);
		    	                        		writeCharacters(writer, rsInstances.getString(2));
		    	                        		writer.writeEndElement();
		    	                        		writer.writeStartElement(TAG_INSTANCENUMBER);
		    	                        		writeCharacters(writer, rsInstances.getString(3));
		    	                        		writer.writeEndElement();
		    	                        		writer.writeStartElement(TAG_REFERENCEDFILEID);
		    	                        		String url="requestType=WADO&studyUID="+studyUid+"&seriesUID="+seriesUid+"&objectUID="+instanceUid;
		    	                        		writeCharacters(writer, baseUrl+url);
		    	                        		writer.writeEndElement();
	                                		writer.writeEndElement();
	                                	}
	                                }
	                        		
	                        		
                        		writer.writeEndElement();		// Series
                        	} 
                        }

            		writer.writeEndElement();		// Study
                	
	            writer.writeEndElement();
            	
            }
            
		}catch(Exception ex){
			log.error("An error occurred retrieving Study metadata", ex);
		}finally{
			CloseableUtils.close(rsInstances);
			CloseableUtils.close(rsSeries);
			CloseableUtils.close(rsStudies);
			CloseableUtils.close(csInstances);
			CloseableUtils.close(csSeries);
			CloseableUtils.close(csStudies);
			CloseableUtils.close(con);
			
		}
		
	}

	private void writeCharacters(XMLStreamWriter writer, String arg) throws XMLStreamException {
		if(arg!=null)
			writer.writeCharacters(arg);
		
	}
	
    
 }
