/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.wado.dao;

import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import it.units.htl.dpacs.dao.Dbms;
import it.units.htl.dpacs.helpers.LogMessage;
import it.units.htl.dpacs.valueObjects.DicomMatch;

import it.units.htl.dpacs.valueObjects.NearlineData;
import it.units.htl.dpacs.valueObjects.NonImage;

import it.units.htl.dpacs.valueObjects.Patient;

import it.units.htl.dpacs.valueObjects.Series;

import it.units.htl.dpacs.valueObjects.Study;

import oracle.jdbc.driver.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.Types;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class is used from WADO servlet to get infos about an instance and get it back. The servlet will process it. This class uses methods inside O3-DPACS core bean to find and get the images. No access to DB.
 * 
 * @author Mbe
 */
public class PacsRetrieveManager {
    public static String PARAM_TEMPURL = "WadoTempUrl";
    public static String PARAM_WADOBUFFERSIZE = "wadoBufferSizeKb";
    public static String PARAM_WADOLIMITFORWHOLE = "wadoLimitForWholeKb";
    public static String PARAM_PATIENTIDENABLED = "patientID";
    public static String PARAM_STUDYUIDENABLED = "studyUID";
    public static String PARAM_ACCESSIONNUMBERENABLED = "accessionNumber";
    public static String PARAM_SHOWONLYTHISENABLED = "showOnlyThis";
    
    private Element configParam = null;
    
    private static final String SERVICETAG_ROOT="configuration";
    
    
    private static Log log = LogFactory.getLog(PacsRetrieveManager.class);
    // private DicomMoveDealerSLLocal bean = null;
    private DataSource dataSource;
    private String wadoTempUrl;
    private int wadoBufferSizeKb;
    private int wadoLimitForWholeFile;
    private boolean patientIdEnabled;
    private boolean accNumEnabled;
    private boolean studyUidEnabled;
    private boolean showOnlyThisEnabled;
    
    private static Log debugLog = LogFactory.getLog("DEBUGLOG"); // 4D

    /**
     * The constructor need the reference to the bean used to get the images.
     * 
     * @param inBean
     */
    public PacsRetrieveManager() throws NamingException {
        try {
            Context jndiContext = new InitialContext(); // Lazy Initialization
            dataSource = (DataSource) jndiContext.lookup("java:/jdbc/wadoDS");
        } catch (NamingException nex) {
            log.fatal(LogMessage._NoDatasource, nex);
            throw nex;
        }
        reloadSettings();
    }

    // EXAMPLE URL: http//://140.105.63.28:8080/o3-dpacs-wado/wadoServlet.toServlet?requestType=WADO&studyUID=1.2.826.0.1.3680043.2.1192.529&seriesUID=1.2.392.200036.9125.3.48195611465.64459794305.14203&objectUID=1.2.392.200036.9125.4.0.17774576.9056256.940786034&contentType=image%2Fjpeg
    /**
     * Get back the file corresponding to these keys
     * 
     * @param study
     *            the study UID
     * @param series
     *            the series UID
     * @param instance
     *            the instance UID
     * @return an array of objects which holds also nearline info
     */
    public DicomMatch[] getFiles(String study, String series, String instance) {
        if ((study == null) && (series == null) && (instance == null)) {
            log.error("NO UID SPECIFIED!");
            return null;
        }
        Connection con = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        DicomMatch[] res = null;
        ArrayList<DicomMatch> rl = null;
        try {
            con = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(con);
            if (isOracle) {
                cs = con.prepareCall("{call getRetrievalInfo(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
                cs.registerOutParameter(17, OracleTypes.CURSOR);
            } else {
                cs = con.prepareCall("{call getRetrievalInfo(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            }
            cs.setString(1, study);
            if (series != null) {
                cs.setString(2, series);
            } else {
                cs.setNull(2, Types.VARCHAR);
            }
            if (instance != null) {
                cs.setString(3, instance);
            } else {
                cs.setNull(3, Types.VARCHAR);
            }
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.registerOutParameter(6, Types.VARCHAR);
            cs.registerOutParameter(7, Types.DATE);
            cs.registerOutParameter(8, Types.VARCHAR);
            cs.registerOutParameter(9, Types.VARCHAR);
            cs.registerOutParameter(10, Types.DATE);
            cs.registerOutParameter(11, Types.TIME);
            cs.registerOutParameter(12, Types.DATE);
            cs.registerOutParameter(13, Types.TIME);
            cs.registerOutParameter(14, Types.DATE);
            cs.registerOutParameter(15, Types.TIME);
            cs.registerOutParameter(16, Types.VARCHAR);
            debugLog.info(instance + " Starting_getRetrievalInfo"); // 4D
            long start = System.currentTimeMillis(); // 4D
            cs.execute();
            debugLog.info(instance + " Completed_getRetrievalInfo " + (System.currentTimeMillis() - start)); // 4D
            try {
                if (isOracle) {
                    rs = (ResultSet) cs.getObject(17);
                } else {
                    rs = cs.getResultSet();
                }
            } catch (SQLException sex) {
            } // This is because the cursor/resultset could not be open (it's inside an "if" in the stored procedure)!!
            if (rs != null) {
                rl = new ArrayList<DicomMatch>((instance != null) ? 1 : 101);
                while (rs.next()) {
                    DicomMatch row = new DicomMatch();
                    row.study = new Study(rs.getString(1));
                    row.patient = new Patient();
                    row.patient.setPrimaryKey(rs.getString(2));
                    row.study.setStudyStatus(rs.getString(3));
                    row.study.setFastestAccess(rs.getString(7));
                    row.series = new Series(rs.getString(4));
                    row.instance = new NonImage(rs.getString(5));
                    String deviceType = rs.getString(6);
                    if ((deviceType != null) && (Study.DPACS_NEARLINE_STATUS == row.study.getStudyStatus().charAt(0))) {
                        row.nearlineData = new NearlineData(deviceType, rs.getString(8));
                        row.nearlineData.setDeviceUrl(row.study.getFastestAccess());
                    }
                    rl.add(row);
                } // end while
                String patientName = cs.getString(4);
                String patientId = cs.getString(5);
                String idIssuer = cs.getString(6);
                Date birthDate = cs.getDate(7);
                String sex = cs.getString(8);
                String studyId = cs.getString(9);
                Date studyDate = cs.getDate(10);
                Time studyTime = cs.getTime(11);
                Date completionDate = cs.getDate(12);
                Time completionTime = cs.getTime(13);
                Date verifiedDate = cs.getDate(14);
                Time verifiedTime = cs.getTime(15);
                String accessionNumber = cs.getString(16);
                for (DicomMatch row : rl) {
                    row.patient.setDcmPatientName(patientName);
                    row.patient.setPatientId(patientId);
                    row.patient.setIdIssuer(idIssuer);
                    row.patient.setBirthDate(birthDate);
                    row.patient.setSex(sex);
                    row.study.setStudyId(studyId);
                    row.study.setStudyDate(studyDate);
                    row.study.setStudyTime(studyTime);
                    row.study.setStudyCompletionDate(completionDate);
                    row.study.setStudyCompletionTime(completionTime);
                    row.study.setStudyVerifiedDate(verifiedDate);
                    row.study.setStudyVerifiedTime(verifiedTime);
                    row.study.setAccessionNumber(accessionNumber);
                }
            } else {
                log.error("NO OBJECT FOUND");
                log.error("	 Study: " + study);
                if (series != null)
                    log.error("	 Series: " + series);
                if (instance != null)
                    log.error("	 Instance: " + instance);
            }
        } catch (SQLException sex) {
            log.error("An error occurred calling getRetrievalInfo", sex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ex) {
                }
            }
            if (cs != null) {
                try {
                    cs.close();
                } catch (Exception ex) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ex) {
                }
            }
        }
        if ((rl != null) && (rl.size() > 0)) {
            res = new DicomMatch[rl.size()];
            rl.toArray(res);
        }
        return res;
    }
  public String getStudyUid(String accessionNumber) {
        Connection connection = null;
        PreparedStatement cs = null;
        ResultSet rs = null;
        String studyUID = null;
        try {
            connection = dataSource.getConnection();
            cs = connection.prepareStatement("SELECT studyInstanceUID FROM Studies WHERE accessionNumber = ?");
            cs.setString(1, accessionNumber);
            cs.execute();
            rs = cs.getResultSet();
            int res = 0;
            while (rs.next()) { 
                studyUID = rs.getString(1);
                res++;
            }
            if(res!=1) return null;
        } catch (Exception ex) {
            log.error("While searching studyUID for this accessionNumber: " + accessionNumber, ex);
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            if (cs != null)
                try {
                    cs.close();
                } catch (SQLException e) {
                }
            if (connection != null)
                try {
                    connection.close();
                } catch (SQLException e) {
                }
        }
        return studyUID;
    }
    
    
    public String getStudyUid(String accessionNumber, String patientId) {
        Connection connection = null;
        PreparedStatement cs = null;
        ResultSet rs = null;
        String studyUID = null;
        try {
            connection = dataSource.getConnection();
            cs = connection.prepareStatement("SELECT studyInstanceUID FROM Studies st " +
            		"INNER JOIN Patients pt ON pt.pk = st.patientFk " +
            		"where st.accessionNumber = ? and pt.patientId = ? ");
            cs.setString(1, accessionNumber);
            cs.setString(2, patientId);
            cs.execute();
            rs = cs.getResultSet();
            while (rs.next()) {
                studyUID = rs.getString(1);
            }
        } catch (Exception ex) {
            log.error("While searching studyUID for this accessionNumber/patientId : " + accessionNumber + "/"+patientId, ex);
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            if (cs != null)
                try {
                    cs.close();
                } catch (SQLException e) {
                }
            if (connection != null)
                try {
                    connection.close();
                } catch (SQLException e) {
                }
        }
        return studyUID;
    }
    public String getWadoUrl(String retrieveAetitle) {
        Connection connection = null;
        PreparedStatement cs = null;
        ResultSet rs = null;
        String wadoUrl = null;
        try {
            connection = dataSource.getConnection();
            cs = connection.prepareStatement("SELECT jpegWado FROM KnownNodes WHERE aeTitle = ?");
            cs.setString(1, retrieveAetitle);
            cs.execute();
            rs = cs.getResultSet();
            while (rs.next()) {
                wadoUrl = rs.getString(1);
            }
        } catch (Exception ex) {
            log.error("While searching wadoUrl for this retrieveAeTitle: " + wadoUrl, ex);
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            if (cs != null)
                try {
                    cs.close();
                } catch (SQLException e) {
                }
            if (connection != null)
                try {
                    connection.close();
                } catch (SQLException e) {
                }
        }
        return wadoUrl;
    }

    public String getGatewayWadoUrl(String study) {
        Connection connection = null;
        CallableStatement cs = null;
        String wadoUrl = null;
        try {
            connection = dataSource.getConnection();
            cs = connection.prepareCall("{call getGatewayWadoForStudy(?,?,?)}");
            cs.setString(1, study);
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.registerOutParameter(3, Types.VARCHAR);
            
            cs.execute();
            
            wadoUrl=cs.getString(2);
        } catch (SQLException sex) {
            log.error("While searching wadoUrl for this study: " + study);
            log.error(sex.getMessage());
        } catch (Exception ex) {
            log.error("While searching wadoUrl for this study: " + study, ex);
        } finally {
            
            if (cs != null)
                try {
                    cs.close();
                } catch (SQLException e) {
                }
            if (connection != null)
                try {
                    connection.close();
                } catch (SQLException e) {
                }
        }
        return wadoUrl;
    }
    
    public String getGatewayJpegWadoUrl(String study) {
        Connection connection = null;
        CallableStatement cs = null;
        String wadoUrl = null;
        try {
            connection = dataSource.getConnection();
            cs = connection.prepareCall("{call getGatewayWadoForStudy(?,?,?)}");
            cs.setString(1, study);
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.registerOutParameter(3, Types.VARCHAR);
            
            cs.execute();
            
            wadoUrl=cs.getString(3);
        } catch (SQLException sex) {
            log.error("While searching wadoUrl for this study: " + study);
            log.error(sex.getMessage());
        } catch (Exception ex) {
            log.error("While searching wadoUrl for this study: " + study, ex);
        } finally {
            
            if (cs != null)
                try {
                    cs.close();
                } catch (SQLException e) {
                }
            if (connection != null)
                try {
                    connection.close();
                } catch (SQLException e) {
                }
        }
        return wadoUrl;
    }
    
    public String getConfigParam(String key) {
        String ret = null;
        Connection connection = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            boolean isOracle = Dbms.isOracle(connection);
            if (isOracle) {
                cs = connection.prepareCall("{call getGlobalConfiguration(?,?)}");
                cs.registerOutParameter(2, OracleTypes.CURSOR);
            } else {
                cs = connection.prepareCall("{call getGlobalConfiguration(?)}");
            }
            cs.setString(1, key);
            if (isOracle) {
                cs.execute();
                rs = (ResultSet) cs.getObject(2);
            } else {
                rs = cs.executeQuery();
            }
            if ((rs != null) && (rs.next()))
                ret = rs.getString(1);
        } catch (Exception ex) {
            log.error("An error occurred retrieving " + key + " parameter: ", ex);
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

    public boolean loadConfiguration() {
        boolean ret = false;
        Connection connection = null;
        Statement st=null;
        ResultSet rs=null;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            connection = dataSource.getConnection();
            st=connection.createStatement();
            rs=st.executeQuery("SELECT configuration FROM ServicesConfiguration WHERE serviceName='WadoConfiguration'");
            String confValue=null;
            while(rs.next()){
            	confValue=rs.getString(1);
            }
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			StringReader reader = new StringReader(confValue);
			InputSource is = new InputSource(reader);
			Document doc = docBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName(SERVICETAG_ROOT);
			Node _rootNode = nodeLst.item(0);
			configParam = (Element) _rootNode;
			ret=true;
        } catch (Exception ex) {
            log.error("An error occurred retrieving configuration", ex);
            ret=false;
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            }
            try {
                st.close();
            } catch (Exception ex) {
            }
            try {
                connection.close();
            } catch (Exception ex) {
            }
        }
        return ret;
    }
    
    public/* synchronized */String getWadoTempUrl() {
        return wadoTempUrl;
    }

    public/* synchronized */int getWadoBufferSizeKb() {
        return wadoBufferSizeKb;
    }

    public/* synchronized */int getWadoLimitForWholeFile() {
        return wadoLimitForWholeFile;
    }

    public boolean isPatientIdEnabled() {
		return patientIdEnabled;
	}

	public void setPatientIdEnabled(boolean patientIdEnabled) {
		this.patientIdEnabled = patientIdEnabled;
	}

	public boolean isAccNumEnabled() {
		return accNumEnabled;
	}

	public void setAccNumEnabled(boolean accNumEnabled) {
		this.accNumEnabled = accNumEnabled;
	}

	public boolean isStudyUidEnabled() {
		return studyUidEnabled;
	}

	public void setStudyUidEnabled(boolean studyUidEnabled) {
		this.studyUidEnabled = studyUidEnabled;
	}

	public boolean isShowOnlyThisEnabled() {
		return showOnlyThisEnabled;
	}

	public void setShowOnlyThisEnabled(boolean showOnlyThisEnabled) {
		this.showOnlyThisEnabled = showOnlyThisEnabled;
	}

	public/* synchronized */void reloadSettings() {
		if(loadConfiguration()){
	        this.wadoTempUrl = getParam(PARAM_TEMPURL);
	        this.wadoBufferSizeKb = Integer.parseInt(getParam(PARAM_WADOBUFFERSIZE));
	        this.wadoLimitForWholeFile = Integer.parseInt(getParam(PARAM_WADOLIMITFORWHOLE)) * 1024;
	        this.patientIdEnabled="TRUE".equalsIgnoreCase(getParam(PARAM_PATIENTIDENABLED));
	        this.accNumEnabled="TRUE".equalsIgnoreCase(getParam(PARAM_ACCESSIONNUMBERENABLED));
	        this.studyUidEnabled="TRUE".equalsIgnoreCase(getParam(PARAM_STUDYUIDENABLED));
	        this.showOnlyThisEnabled="TRUE".equalsIgnoreCase(getParam(PARAM_SHOWONLYTHISENABLED));
		}
    }
	
	private String getParam(String paramName) {
		NodeList fstNmElmntLst = configParam.getElementsByTagName(paramName);
		if ((fstNmElmntLst == null) || (fstNmElmntLst.getLength() == 0))
			return null;
		Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
		NodeList fstNm = fstNmElmnt.getChildNodes();
		Node node = fstNm.item(0);
		if (node != null) {
			return node.getNodeValue();
		} else {
			return null;
		}
	}
}