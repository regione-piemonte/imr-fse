/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.dpacs.services;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.helpers.LogMessage;
import it.units.htl.wado.dao.PacsRetrieveManager;

import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;

import eu.o3c.o3e.dpacs.services.utils.SortingCriteriaLoader;
import eu.o3c.o3e.dpacs.services.utils.UserAuthenticator;
import eu.o3c.o3e.dpacs.services.utils.objects.Instance;
import eu.o3c.o3e.dpacs.services.utils.objects.Patient;
import eu.o3c.o3e.dpacs.services.utils.objects.Series;
import eu.o3c.o3e.dpacs.services.utils.objects.Study;
import eu.o3c.o3e.dpacs.services.utils.sorting.DicomImageComparator;
import eu.o3c.o3e.dpacs.services.utils.sorting.SortingCriteria;

/**
 * Servlet implementation class SearchServices
 */
public class SearchServices extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final String WADOURL = "WadoUrl";
    private final String IMAGE_QUALITY = "fuiImageQuality";
    private Log log = LogFactory.getLog(SearchServices.class);
    private DataSource dataSource;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchServices() {
        super();
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
        doWork(request, response);
    }

    private synchronized void doWork(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String wadoUrl = null;
        // starts setting parameters
        String studyUID = request.getParameter("studyUID");
        String seriesUID = request.getParameter("seriesUID");
        String sourceAeTitle = request.getParameter("retrieveAeTitle");
        try {
            if (UserAuthenticator.isUserValid(request)) {
                dataSource = getDataSource();
                // setting wadoUrl
                if ((studyUID == null || "".equals(studyUID)) || (seriesUID == null || "".equals(seriesUID))) {
                    // if study and/or series are missing, the servlet responds 400 (BAD REQUEST)
                    log.error("studyUID and/or seriesUID are null, check the URL.");
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Check the request: " + ((studyUID == null) ? " studyUID is missing" : "") + ((seriesUID == null) ? " seriesUID is missing" : ""));
                    return;
                }
                String queryIdentify = System.currentTimeMillis()+"";
                log.debug(queryIdentify + " get this query:  stUID: " + studyUID + "|seriesUID:"+seriesUID );
                // Search for data about the couple study/series
                Series seriesInfo = null;
                try {
                    seriesInfo = getSeriesBasicInfo(studyUID, seriesUID);
                } catch (Exception ex) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Problem while searching for this series " + seriesUID + " of this study " + studyUID);
                    log.error("Problem while searching for this series " + seriesUID + " of this study " + studyUID, ex);
                    return;
                }
                if (seriesInfo == null) {
                    // if an error occurred during 'getSeriesBasicInfo()', seriesInfo is null and the servlet responds 404 (NOT FOUND)
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Series not found UID (study/series):" + studyUID + "/" + seriesUID);
                    return;
                }
                // setting wadoUrl
                boolean gateway=false;
                PacsRetrieveManager prm;
                try {
                    prm = new PacsRetrieveManager();
                    if (sourceAeTitle != null) {
                        wadoUrl = prm.getWadoUrl(sourceAeTitle);
                    }
                    if (wadoUrl == null) {
                    	wadoUrl=prm.getGatewayJpegWadoUrl(studyUID);
                    	gateway=(wadoUrl!=null);
                    }
                    if (wadoUrl == null) {
						wadoUrl = prm.getConfigParam(WADOURL);
						if (request.getHeader(WADOURL) != null) {
							wadoUrl = request.getHeader(WADOURL);
						}
                    }
                    if (wadoUrl == null) {
                        log.info("Error!! No way to found wadoUrl!");
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No wadoUrl found!");
                        return;
                    }
                    log.debug("The wadoUrl is " + wadoUrl);
                	if(!wadoUrl.contains("?")){
                		wadoUrl+="?";
                	}else if(!wadoUrl.endsWith("&")){
                		wadoUrl+="&";
                	}
                } catch (NamingException nex) {
                    log.error("Error initializing wadourl");
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error initializing wadourl");
                    return;
                }
                // here series does exist, instances cannot be null (at least empty)
                Study studyBasicInfo = getStudyBasicInfo(studyUID);
                Patient patientBasicInfo = getPatientBasicInfo(studyUID);
                ArrayList<Instance> instances = getSeriesInstances(seriesInfo, studyUID);
                try {
                    writeRepsponse(response, studyUID, patientBasicInfo, studyBasicInfo, seriesInfo, instances, wadoUrl, gateway);
                } catch (Exception e) {
                    log.error("Unable to build the XML response.");
                    throw e;
                }
                log.debug(queryIdentify + " end.");
            } else {
                // if authentication fails, the servlet responds 401 (UNAUTHORIZED)
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Client unauthorized");
                return;
            }
        } catch (Exception e) {
            log.error("", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }
    }

    // servlet methods
    private void writeRepsponse(HttpServletResponse response, String studyUID, Patient patientBasicInfo, Study studyBasicInfo, Series seriesInfo, ArrayList<Instance> instances, String wadoUrl, boolean gateway) throws XMLStreamException, IOException {
        try {
            if(!gateway) {
            	getInformationFromDicomObject(instances, seriesInfo, studyUID);
            	if("KO".equalsIgnoreCase(seriesInfo.getModality()))
            		for(Instance i : instances) {
            			ArrayList<Instance> li = new ArrayList<Instance>(i.getReferencedInstances());
            			getInformationFromDicomObject(li, seriesInfo, studyUID);
            		}
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }
        Instance[] instancesArray = new Instance[instances.size()];
        instances.toArray(instancesArray);
        SortingCriteriaLoader scl = new SortingCriteriaLoader();
        SortingCriteria sc = scl.getSortingCriteriaFormodality(seriesInfo.getModality());
        DicomImageComparator dic = new DicomImageComparator(sc);
        Arrays.sort(instancesArray, dic);
        String imageQuality = null;
        PacsRetrieveManager prm;
        try {
            prm = new PacsRetrieveManager();
            imageQuality = prm.getConfigParam(IMAGE_QUALITY);
            log.debug("The imageQuality is " + imageQuality);
        } catch (NamingException nex) {
            log.warn("No image quality found!");
        }
        // create the XML StreamWriter on the response outputstream
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(response.getOutputStream());
        writer.writeStartDocument("1.0");
        writer.writeStartElement("results");
        // write the patient tag
        writer.writeStartElement("patient");
        writer.writeAttribute("patientID", (patientBasicInfo.getPatientId() == null) ? "" : patientBasicInfo.getPatientId());
        writer.writeAttribute("lastName", (patientBasicInfo.getLastName() == null) ? "" : patientBasicInfo.getLastName());
        writer.writeAttribute("middleName", (patientBasicInfo.getMiddleName() == null) ? "" : patientBasicInfo.getMiddleName());
        writer.writeAttribute("firstName", (patientBasicInfo.getFirstName() == null) ? "" : patientBasicInfo.getFirstName());
        writer.writeAttribute("birthDate", (patientBasicInfo.getBirthDate() != null) ? patientBasicInfo.getBirthDate().toString() : "");
        // write the study tag
        writer.writeStartElement("study");
        writer.writeAttribute("uid", (studyUID == null) ? "" : studyUID);
        writer.writeAttribute("date", (studyBasicInfo.getDate() == null) ? "" : studyBasicInfo.getDate().toString());
        String studyTime = "";
        if (studyBasicInfo.getTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("k:mm:ss");
            try {
                studyTime = sdf.format(studyBasicInfo.getTime());
            } catch (RuntimeException rex) {
                log.error("Error parsing study time.", rex);
                studyTime = "";
            }
        }
        writer.writeAttribute("time", (studyTime == null) ? "" : studyTime);
        writer.writeAttribute("description", (studyBasicInfo.getDescription() == null) ? "" : studyBasicInfo.getDescription());
        // write series tag
        writer.writeStartElement("series");
        writer.writeAttribute("uid", (seriesInfo.getSeriesInstanceUid() == null) ? "" : seriesInfo.getSeriesInstanceUid());
        writer.writeAttribute("modality", (seriesInfo.getModality() == null) ? "" : seriesInfo.getModality());
        for (int i = 0; i < instancesArray.length; i++) {
            writer.writeStartElement("instance");
            if ((seriesInfo.getStudyStatus().charAt(0) == Study.DPACS_OPEN_STATUS)||(seriesInfo.getStudyStatus().charAt(0) == Study.DPACS_NEARLINE_STATUS)) {
                Instance currentInstance = instancesArray[i];
                writer.writeAttribute("uid", (currentInstance.getSopInstanceUID() == null) ? "" : currentInstance.getSopInstanceUID());
                writer.writeAttribute("number", (currentInstance.getInstanceNumber() == null) ? "" : currentInstance.getInstanceNumber());
                writer.writeAttribute("wadourl", wadoUrl + "requestType=WADO&studyUID=" + studyUID
                        + "&seriesUID=" + seriesInfo.getSeriesInstanceUid()
                        + "&objectUID=" + currentInstance.getSopInstanceUID());
                writer.writeAttribute("numberOfFrames", (currentInstance.getNumberOfFrames() == null) ? "" : currentInstance.getNumberOfFrames());
                writer.writeAttribute("windowCenter", (currentInstance.getWindowCenter() == null) ? "" : currentInstance.getWindowCenter());
                writer.writeAttribute("windowWidth", (currentInstance.getWindowWidth() == null) ? "" : currentInstance.getWindowWidth());
                writer.writeAttribute("rows", (currentInstance.getRows() == null) ? "" : currentInstance.getRows());
                writer.writeAttribute("columns", (currentInstance.getColumns() == null) ? "" : currentInstance.getColumns());
                writer.writeAttribute("imagePositionPatientX", (currentInstance.getImagePositionPatient() != null) ? currentInstance.getImagePositionPatient()[0] : "");
                writer.writeAttribute("imagePositionPatientY", (currentInstance.getImagePositionPatient() != null) ? currentInstance.getImagePositionPatient()[1] : "");
                writer.writeAttribute("imagePositionPatientZ", (currentInstance.getImagePositionPatient() != null) ? currentInstance.getImagePositionPatient()[2] : "");
                writer.writeAttribute("photometricInterpretation", (currentInstance.getPhotometricInterpretation() == null) ? "" : currentInstance.getPhotometricInterpretation());
                writer.writeAttribute("bitPerPixel", (currentInstance.getBitPerPixel() == null) ? "" : currentInstance.getBitPerPixel());
                writer.writeAttribute("rescaleSlope", (currentInstance.getRescaleSlope() == null) ? "" : currentInstance.getRescaleSlope());
                writer.writeAttribute("rescaleIntercept", (currentInstance.getRescaleIntercept() == null) ? "" : currentInstance.getRescaleIntercept());
                writer.writeAttribute("transferSyntax", (currentInstance.getTransferSyntax() == null) ? "" : currentInstance.getTransferSyntax());
                if (imageQuality != null) {
                    writer.writeAttribute("imageQuality", imageQuality);
                }
                                
                writer.writeAttribute("contentType", (currentInstance.getMimeType() == null) ? "" : currentInstance.getMimeType());

                if("KO".equalsIgnoreCase(seriesInfo.getModality())) {
                	writer.writeAttribute("contentDate", (currentInstance.getContentDate() == null) ? "" : currentInstance.getContentDate());
                	writer.writeAttribute("contentTime", (currentInstance.getContentTime() == null) ? "" : currentInstance.getContentTime());
                	writer.writeAttribute("codeMeaning", (currentInstance.getMeaning() == null) ? "" : currentInstance.getMeaning());
                	
                	if(currentInstance.getReferencedInstances() != null) {
                		writer.writeAttribute("numberOfReferencedInstances", String.valueOf(currentInstance.getReferencedInstances().size()));
                		for(Instance ins : currentInstance.getReferencedInstances()) {
                			writer.writeStartElement("referencedInstance");
                			if ((seriesInfo.getStudyStatus().charAt(0) == Study.DPACS_OPEN_STATUS)||(seriesInfo.getStudyStatus().charAt(0) == Study.DPACS_NEARLINE_STATUS)) {
                                
                                writer.writeAttribute("uid", (ins.getSopInstanceUID() == null) ? "" : ins.getSopInstanceUID());
                                writer.writeAttribute("number", (ins.getInstanceNumber() == null) ? "" : ins.getInstanceNumber());
                                writer.writeAttribute("referencedSeries", ins.getReferencedSeries());
                                writer.writeAttribute("wadourl", wadoUrl + "requestType=WADO&studyUID=" + studyUID
                                        + "&seriesUID=" + ins.getReferencedSeries()
                                        + "&objectUID=" + ins.getSopInstanceUID());
                                writer.writeAttribute("numberOfFrames", (ins.getNumberOfFrames() == null) ? "" : ins.getNumberOfFrames());
                                writer.writeAttribute("windowCenter", (ins.getWindowCenter() == null) ? "" : ins.getWindowCenter());
                                writer.writeAttribute("windowWidth", (ins.getWindowWidth() == null) ? "" : ins.getWindowWidth());
                                writer.writeAttribute("rows", (ins.getRows() == null) ? "" : ins.getRows());
                                writer.writeAttribute("columns", (ins.getColumns() == null) ? "" : ins.getColumns());
                                writer.writeAttribute("imagePositionPatientX", (ins.getImagePositionPatient() != null) ? ins.getImagePositionPatient()[0] : "");
                                writer.writeAttribute("imagePositionPatientY", (ins.getImagePositionPatient() != null) ? ins.getImagePositionPatient()[1] : "");
                                writer.writeAttribute("imagePositionPatientZ", (ins.getImagePositionPatient() != null) ? ins.getImagePositionPatient()[2] : "");
                                writer.writeAttribute("photometricInterpretation", (ins.getPhotometricInterpretation() == null) ? "" : ins.getPhotometricInterpretation());
                                writer.writeAttribute("bitPerPixel", (ins.getBitPerPixel() == null) ? "" : ins.getBitPerPixel());
                                writer.writeAttribute("rescaleSlope", (ins.getRescaleSlope() == null) ? "" : ins.getRescaleSlope());
                                writer.writeAttribute("rescaleIntercept", (ins.getRescaleIntercept() == null) ? "" : ins.getRescaleIntercept());
                                writer.writeAttribute("transferSyntax", (ins.getTransferSyntax() == null) ? "" : ins.getTransferSyntax());
                                if (imageQuality != null) {
                                    writer.writeAttribute("imageQuality", imageQuality);
                                }
                                                
                                writer.writeAttribute("contentType", (ins.getMimeType() == null) ? "" : ins.getMimeType());
                                
                                if (ins.getPixelSpacing() != null) {
                                    writer.writeStartElement("pixelSpacing");
                                    writer.writeAttribute("rowSpacing", (ins.getPixelSpacing()[0] == null) ? "" : ins.getPixelSpacing()[0]);
                                    writer.writeAttribute("columnSpacing", (ins.getPixelSpacing()[1] == null) ? "" : ins.getPixelSpacing()[1]);
                                    writer.writeEndElement();
                                }
                                if (ins.getImagerPixelSpacing() != null) {
                                    writer.writeStartElement("imagerPixelSpacing");
                                    writer.writeAttribute("rowSpacing", (ins.getImagerPixelSpacing()[0] == null) ? "" : ins.getImagerPixelSpacing()[0]);
                                    writer.writeAttribute("columnSpacing", (ins.getImagerPixelSpacing()[1] == null) ? "" : ins.getImagerPixelSpacing()[1]);
                                    writer.writeEndElement();
                                }
                			}
                			writer.writeEndElement();
                		}
                	}
                }
                
                if (currentInstance.getPixelSpacing() != null) {
                    writer.writeStartElement("pixelSpacing");
                    writer.writeAttribute("rowSpacing", (currentInstance.getPixelSpacing()[0] == null) ? "" : currentInstance.getPixelSpacing()[0]);
                    writer.writeAttribute("columnSpacing", (currentInstance.getPixelSpacing()[1] == null) ? "" : currentInstance.getPixelSpacing()[1]);
                    writer.writeEndElement();
                }
                if (currentInstance.getImagerPixelSpacing() != null) {
                    writer.writeStartElement("imagerPixelSpacing");
                    writer.writeAttribute("rowSpacing", (currentInstance.getImagerPixelSpacing()[0] == null) ? "" : currentInstance.getImagerPixelSpacing()[0]);
                    writer.writeAttribute("columnSpacing", (currentInstance.getImagerPixelSpacing()[1] == null) ? "" : currentInstance.getImagerPixelSpacing()[1]);
                    writer.writeEndElement();
                }
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
    }

    private DataSource getDataSource() {
        if (dataSource == null) {
            DataSource ds = null;
            try {
                Context jndiContext = new InitialContext();
                ds = (DataSource) jndiContext.lookup("java:/jdbc/wadoDS");
            } catch (NamingException nex) {
                log.fatal(LogMessage._NoDatasource, nex);
                try {
                    throw nex;
                } catch (Exception e) {
                }
            }
            return ds;
        } else {
            return dataSource;
        }
    }

    private Study getStudyBasicInfo(String study) {
        Study ret = new Study();
        Connection connection = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(connection);
            if (isOracle) {
                cs = connection.prepareCall("{call getStudyBasicInfo(?,?)}");
                cs.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                cs = connection.prepareCall("{call getStudyBasicInfo(?)}");
            }
            cs.setString(1, study);
            cs.execute();
            if (isOracle) {
                rs = (ResultSet) cs.getObject(2);
            } else {
                rs = cs.getResultSet();
            }
            if (rs != null && rs.next()) {
                Study studyBasicInfo = new Study();
                studyBasicInfo.setDate(rs.getDate(1));
                studyBasicInfo.setTime(rs.getTimestamp(2));
                studyBasicInfo.setDescription(rs.getString(3));
                ret = studyBasicInfo;
            } else {
                log.error("Error retrieving study basic info");
                return null;
            }
        } catch (Exception ex) {
            ret = null;
            log.error("Error retrieving study basic info", ex);
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
            try {
                cs.close();
            } catch (Exception ex) {
            }
            try {
                connection.close();
            } catch (Exception ex) {
            }
        }
        return ret;
    }

    private Patient getPatientBasicInfo(String study) {
        Patient ret = new Patient();
        Connection connection = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(connection);
            if (isOracle) {
                cs = connection.prepareCall("{call getPatientBasicInfo(?,?)}");
                cs.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                cs = connection.prepareCall("{call getPatientBasicInfo(?)}");
            }
            cs.setString(1, study);
            cs.execute();
            if (isOracle) {
                rs = (ResultSet) cs.getObject(2);
            } else {
                rs = cs.getResultSet();
            }
            if (rs != null && rs.next()) {
                Patient patientBasicInfo = new Patient();
                patientBasicInfo.setPatientId(rs.getString(6));
                patientBasicInfo.setLastName(rs.getString(1));
                patientBasicInfo.setMiddleName(rs.getString(3));
                patientBasicInfo.setFirstName(rs.getString(2));
                patientBasicInfo.setBirthDate(rs.getDate(8));
                ret = patientBasicInfo;
            } else {
                log.error("Error retrieving patient basic info");
                return null;
            }
        } catch (Exception ex) {
            ret = null;
            log.error("Error retrieving patient basic info", ex);
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
            try {
                cs.close();
            } catch (Exception ex) {
            }
            try {
                connection.close();
            } catch (Exception ex) {
            }
        }
        return ret;
    }

    private ArrayList<Instance> getSeriesInstances(Series series, String studyUID) {
        // retrieves instances information, useful to read every file from storage. Here study and series cannot be null.
        ArrayList<Instance> ret = new ArrayList<Instance>();
        Connection connection = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(connection);
            if (isOracle) {
                cs = connection.prepareCall("{call getSeriesInstances(?,?)}");
                cs.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                cs = connection.prepareCall("{call getSeriesInstances(?)}");
            }
            cs.setString(1, series.getSeriesInstanceUid());
            cs.execute();
            if (isOracle) {
                rs = (ResultSet) cs.getObject(2);
            } else {
                rs = cs.getResultSet();
            }
            if (rs != null) {
                while (rs.next()) {
                    Instance instance = new Instance();
                    instance.setSopInstanceUID(rs.getString(1));
                    instance.setSopClassUID(rs.getString(2));
                    instance.setInstanceNumber(rs.getString(3));
                    instance.setMimeType(rs.getString(4));
                    instance.setModality(series.getModality());
                    if("KO".equalsIgnoreCase(instance.getModality())) {
                    	log.debug(instance.getSopInstanceUID() + ": Retrieving KO code meaning");
                    	instance.setMeaning(getCodeMeaning(instance));
                    	
                    	log.debug(instance.getSopInstanceUID() + ": Retrieving KO referenced instances");
                    	instance.setReferencedInstances(getKOReferencedInstances(instance, studyUID));
                    }
                    ret.add(instance);
                }
            }
        } catch (Exception ex) {
            ret = null;
            log.error("Error retrieving instances", ex);
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
            try {
                cs.close();
            } catch (Exception ex) {
            }
            try {
                connection.close();
            } catch (Exception ex) {
            }
        }
        return ret;
    }

    private List<Instance> getKOReferencedInstances(Instance instance, String studyUID) {
    	 // retrieves referenced instances information of the current study, useful to read every file from storage. Here study and series cannot be null.
        ArrayList<Instance> ret = new ArrayList<Instance>();
        Connection connection = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(connection);
            if (isOracle) {
                cs = connection.prepareCall("{call getKOReferencedInstances(?,?,?)}");
                cs.registerOutParameter(3, OracleTypes.CURSOR);
            } else {
                cs = connection.prepareCall("{call getKOReferencedInstances(?,?)}");
            }
            cs.setString(1, instance.getSopInstanceUID());
            cs.setString(2, studyUID);
            cs.execute();
            if (isOracle) {
                rs = (ResultSet) cs.getObject(3);
            } else {
                rs = cs.getResultSet();
            }
            if (rs != null) {
                while (rs.next()) {
                    Instance ins = new Instance();
                    ins.setSopInstanceUID(rs.getString(1));
                    ins.setSopClassUID(rs.getString(2));
                    ins.setInstanceNumber(rs.getString(3));
                    ins.setReferencedSeries(rs.getString(4));
                    ins.setMimeType(rs.getString(5));
                    ins.setModality(instance.getModality());                    
                    ret.add(ins);
                }
            }
        } catch (Exception ex) {
            ret = null;
            log.error("Error retrieving KO referenced instances", ex);
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
            try {
                cs.close();
            } catch (Exception ex) {
            }
            try {
                connection.close();
            } catch (Exception ex) {
            }
        }
        return ret;
	}

	private String getCodeMeaning(Instance instance) throws Exception {
    	// Here study and series cannot be null
        String meaning = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement("SELECT c.codemeaning FROM CODESEQUENCES c INNER JOIN KEYOBJECTS k ON c.PK = k.CODESEQUENCESFK WHERE k.SOPINSTANCEUID = ?");
            ps.setString(1, instance.getSopInstanceUID());
            ps.execute();
            rs = ps.getResultSet();
            if (rs != null && rs.next()) {
                meaning = rs.getString(1);
            } else {
                // Code meaning not combined or not existing
                log.error("Code meaning not combined or not existing");
                return null;
            }
        } catch (Exception ex) {
        	meaning = null;
            log.error("Error retrieving instance Code Meaning", ex);
            throw ex;
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
            try {
                ps.close();
            } catch (Exception ex) {
            }
            try {
                connection.close();
            } catch (Exception ex) {
            }
        }
        return meaning;
	}

	private Series getSeriesBasicInfo(String study, String series) throws Exception {
        // Here study and series cannot be null
        Series ret = new Series();
        Connection connection = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(connection);
            if (isOracle) {
                cs = connection.prepareCall("{call getSeriesBasicInfo(?,?,?)}");
                cs.registerOutParameter(3, OracleTypes.CURSOR);
            } else {
                cs = connection.prepareCall("{call getSeriesBasicInfo(?,?)}");
            }
            cs.setString(1, study);
            cs.setString(2, series);
            cs.execute();
            if (isOracle) {
                rs = (ResultSet) cs.getObject(3);
            } else {
                rs = cs.getResultSet();
            }
            if (rs != null && rs.next()) {
                // study and series correctly combined
                Series seriesInfo = new Series();
                seriesInfo.setSeriesInstanceUid(rs.getString(1));
                seriesInfo.setModality(rs.getString(2));
                // needed to order instances
                seriesInfo.setPath(rs.getString(3));
                seriesInfo.setStudyStatus(rs.getString(4));
                ret = seriesInfo;
            } else {
                // study/series not combined or not existing
                log.error("Study/series not combined or not existing");
                return null;
            }
        } catch (Exception ex) {
            ret = null;
            log.error("Error retrieving instances basic information", ex);
            throw ex;
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
            try {
                cs.close();
            } catch (Exception ex) {
            }
            try {
                connection.close();
            } catch (Exception ex) {
            }
        }
        return ret;
    }

    private void getInformationFromDicomObject(ArrayList<Instance> instances, Series seriesInfo, String studyUID) throws Exception {
        StringBuilder url = new StringBuilder();
        url.append(seriesInfo.getPath()).append(studyUID).append("/").append(seriesInfo.getSeriesInstanceUid()).append("/");
        // urlToHardDisk is the url to the series requested by the client
        String urlToHardDisk = url.toString();
        for (int i = 0; i < instances.size(); i++) {
        	if(instances.get(i).getReferencedSeries() != null) {
        		//It's a referenced instance. I need to get the referenced series
        		 try {
        			 Series refSeries = getSeriesBasicInfo(studyUID, instances.get(i).getReferencedSeries());
        			 url = new StringBuilder();
        			 url.append(refSeries.getPath()).append(studyUID).append("/").append(refSeries.getSeriesInstanceUid()).append("/");
        			 urlToHardDisk = url.toString();
                 } catch (Exception ex) {
                     log.error("Problem while searching for this referenced series " + instances.get(i).getReferencedSeries() + " of this study " + studyUID, ex);
                     throw ex;
                 }
        		
        	}
            StringBuilder instanceUrl = new StringBuilder();
            instanceUrl.append(urlToHardDisk);
            instanceUrl.append(instances.get(i).getSopInstanceUID());
            File f = new File(instanceUrl.toString());
            DicomInputStream dim = null;
            DicomObject doo=null;
            try {
                dim = new DicomInputStream(f);
                dim.setHandler(new StopTagInputHandler(Tag.PixelData));
                doo = dim.readDicomObject();
            } catch (Exception e) {
                log.error("Problems reading an instance", e);
                throw e;
            } finally {
                try {
                    dim.close();
                } catch (Exception e) {
                }
            }
            if(doo!=null){
            	// sets imagePosition, useful for ordering
                instances.get(i).setImagePositionPatient(doo.getStrings(Tag.ImagePositionPatient));
                instances.get(i).setNumberOfFrames(doo.getString(Tag.NumberOfFrames));
                instances.get(i).setWindowCenter(doo.getString(Tag.WindowCenter));
                instances.get(i).setWindowWidth(doo.getString(Tag.WindowWidth));
                instances.get(i).setRows(doo.getString(Tag.Rows));
                instances.get(i).setColumns(doo.getString(Tag.Columns));
                instances.get(i).setImagePositionPatient(doo.getStrings(Tag.ImagePositionPatient));
                instances.get(i).setPhotometricInterpretation(doo.getString(Tag.PhotometricInterpretation));
                instances.get(i).setBitPerPixel(doo.getString(Tag.BitsStored));
                instances.get(i).setRescaleSlope(doo.getString(Tag.RescaleSlope));
                instances.get(i).setRescaleIntercept(doo.getString(Tag.RescaleIntercept));
                instances.get(i).setTransferSyntax(doo.getString(Tag.TransferSyntaxUID));
                instances.get(i).setPixelSpacing(doo.getStrings(Tag.PixelSpacing));
                instances.get(i).setImagerPixelSpacing(doo.getStrings(Tag.ImagerPixelSpacing));
                instances.get(i).setInstanceDateTime(doo.getString(Tag.CreationTime));
                instances.get(i).setEchoTime(doo.getString(Tag.EchoTime));
                
				if ("KO".equalsIgnoreCase(seriesInfo.getModality()) && instances.get(i).getReferencedSeries() == null) {
					// It's a Key Object referenced instance 
					Date contentDate = doo.getDate(Tag.ContentDate);
					Date contentTime = doo.getDate(Tag.ContentTime);
					if (contentDate != null && contentTime != null) {
						SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
						SimpleDateFormat sdfTime = new SimpleDateFormat("k:mm:ss");
						
						instances.get(i).setContentDate(new java.sql.Date(contentDate.getTime()).toString());

						try {
							String dateFormat = sdfDate.format(contentDate);
							String timeFormat = sdfTime.format(contentTime);
							String dateTime = dateFormat.concat(" ").concat(timeFormat);
							Date parse = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(dateTime);
							Long time = parse.getTime();
							instances.get(i).setInstanceDateTime(time.toString());
						} catch (RuntimeException rex) {
							log.error("Error parsing DateTime.", rex);
							instances.get(i).setInstanceDateTime("");
						}

						try {
							instances.get(i).setContentTime(sdfTime.format(contentTime));
						} catch (RuntimeException rex) {
							log.error("Error parsing content time.", rex);
							instances.get(i).setContentTime("");
						}

					} else {
						instances.get(i).setInstanceDateTime("");
						instances.get(i).setContentDate("");
						instances.get(i).setContentTime("");
					}
                }
            }
        }
    }
}
