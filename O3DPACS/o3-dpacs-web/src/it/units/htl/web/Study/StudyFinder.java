/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import it.units.htl.dpacs.dao.DicomQueryDealerLocal;
import it.units.htl.dpacs.dao.DicomQueryDealerRemote;
import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.valueObjects.DicomConstants;
import it.units.htl.dpacs.valueObjects.DicomMatch;
import it.units.htl.dpacs.valueObjects.Image;
import it.units.htl.dpacs.valueObjects.NonImage;
import it.units.htl.dpacs.valueObjects.Patient;
import it.units.htl.dpacs.valueObjects.Series;
import it.units.htl.dpacs.valueObjects.StructRep;
import it.units.htl.dpacs.valueObjects.Study;
import it.units.htl.web.utils.MyX509TrustManager;
import it.units.htl.web.utils.XmlConfigLoader;

import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.dict.UIDs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Sara, francesco.feront, Max, Cuorpo, MaTTeO Sangalli, arlind
 */
public class StudyFinder {
    private static Patient patient = new Patient();
    private static Study study = new Study("");
    private static Series serie = new Series("");
    private static Image imageObject = new Image();
    private static DicomMatch[] dicomMatches;
    private static Log log = LogFactory.getLog(StudyFinder.class);
    private static final String SERVICENAME = "WebSettings";
    private static final String SERVICEPARAM_THUMBS_DIMENSION = "thumbsDimension";
    private static final String DEFAULTCONFIG_THUMBS_DIMENSION = "200";
    private static String thumbsDimension;

    // Creates new instance of StudyFinder
    public StudyFinder() {
    }

    static {
        loadThumbsDimension();
    }

    public static void loadThumbsDimension() {
        Document config = XmlConfigLoader.getConfigurationFromDB(SERVICENAME);
        if (config != null) {
            NodeList nodes = config.getElementsByTagName(SERVICEPARAM_THUMBS_DIMENSION);
            try {
                thumbsDimension = nodes.item(0).getTextContent();
                Integer.parseInt(thumbsDimension);
            } catch (Exception ex) {
                log.warn(SERVICEPARAM_THUMBS_DIMENSION + " NOT defined. The default setting of " + DEFAULTCONFIG_THUMBS_DIMENSION + " will be used");
                thumbsDimension = DEFAULTCONFIG_THUMBS_DIMENSION;
            }
        } else {
            log.warn(SERVICEPARAM_THUMBS_DIMENSION + " NOT defined. The default setting of " + DEFAULTCONFIG_THUMBS_DIMENSION + " will be used");
            thumbsDimension = DEFAULTCONFIG_THUMBS_DIMENSION;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean getStudies(String firstName, String lastName, Date birthDate, String patientId, String studyId, String accessNumber, String modalitiesInStudy, Date studyDate,
            String studyDescription, String studyInstanceUID) {
        try {
            patient.reset();
            study.reset();
            // Setting Parameters for Patient
            // wild-cards allowed for firstName and lastName fields
            if (firstName != null && ("*".equals(firstName) || "%".equals(firstName))) {
                patient.setFirstName(DicomConstants.UNIVERSAL_MATCHING);
            } else {
                patient.setFirstName(firstName);
            }
            if (lastName != null && ("*".equals(lastName) || "%".equals(lastName))) {
                patient.setLastName(DicomConstants.UNIVERSAL_MATCHING);
            } else {
                patient.setLastName(lastName);
            }
            if (birthDate != null) {
                try {
                    java.sql.Date sqlDate = new java.sql.Date(birthDate.getTime());
                    patient.setBirthDate(sqlDate);
                    patient.setBirthDateAsString(sqlDate.toString().replace("-", ""));
                } catch (Exception e) {
                    patient.setBirthDate(null);
                    log.info("", e);
                }
            } else {
                patient.setBirthDate(null);
            }
            patient.setPatientId(patientId);// PatientId
            // Setting Parameters for Studies
            study.setStudyId(studyId);// StudyID
            study.setAccessionNumber(accessNumber);// AccessNumber
            String[] mod = {};// Modality
            if ("".equals(modalitiesInStudy) || modalitiesInStudy == null)
                study.setModalitiesInStudy(mod);
            else
                study.setModalitiesInStudy(modalitiesInStudy.split("-"));
            if (studyDate != null && !"".equals(studyDate.getTime())) {
                try {
                    java.sql.Date sqlDate = new java.sql.Date(studyDate.getTime());
                    study.setStudyDate(sqlDate);
                    study.setStudyDateString(sqlDate.toString().replace("-", ""));
                } catch (Exception e) {
                    study.setStudyDate(null);
                }
            }
            study.setStudyInstanceUid(studyInstanceUID);
            // wild-cards allowed for study description field
            if (studyDescription != null && ("*".equals(studyDescription) || "%".equals(studyDescription))) {
                study.setStudyDescription(DicomConstants.UNIVERSAL_MATCHING);
            } else {
                study.setStudyDescription(studyDescription);
            }
            // Execute Query
            PacsConnector pacsConnection = new PacsConnector();
            DicomQueryDealerRemote queryDealerRemote = pacsConnection.getQueryDealer();
            
//            Hashtable props = new Hashtable();
//            props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
//            props.put("jboss.naming.client.ejb.context",false);
//            Context context = new InitialContext(props);
//            DicomQueryDealerRemote queryDealerRemote = (DicomQueryDealerRemote) context.lookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DicomQueryDealerBean!it.units.htl.dpacs.dao.DicomQueryDealerRemote");

            
            // DicomQueryDealerSLRemote
            // queryDealerRemote=PacsConnector.getInstance().getConnection();
            dicomMatches = null;
            dicomMatches = queryDealerRemote.studyRootMatch(patient, study, 500, "o3-dpacs-web", false);
            // To remove all the older matches
            // StudyList.clear();
            for (int i = 0; i < dicomMatches.length - 1; i++) {
                StudyListItem studyListItem = new StudyListItem();
                if (Integer.parseInt(dicomMatches[i].study.getNumberOfStudyRelatedInstances()) > 0) {
                    studyListItem.setStudyDate(dicomMatches[i].study.getStudyDate());
                    studyListItem.setStudyTime(dicomMatches[i].study.getStudyTime());
                    studyListItem.setModalitiesInStudy(dicomMatches[i].study.getModalitiesInStudy());
                    studyListItem.setStudyDescription(dicomMatches[i].study.getStudyDescription());
                    studyListItem.setStudyStatus(dicomMatches[i].study.getStudyStatus());
                    studyListItem.setFirstName(dicomMatches[i].patient.getFirstName());
                    studyListItem.setLastName(dicomMatches[i].patient.getLastName());
                    studyListItem.setStudyInstanceUid(dicomMatches[i].study.getStudyInstanceUid());
                    studyListItem.setPatientId(dicomMatches[i].patient.getPatientId());
                    studyListItem.setBirthDate(dicomMatches[i].patient.getBirthDate());
                    studyListItem.setNumberOfStudyRelatedInstances(dicomMatches[i].study.getNumberOfStudyRelatedInstances());
                    studyListItem.setAccessionNumber(dicomMatches[i].study.getAccessionNumber());
                    studyList().add(studyListItem);
                }
            }
            pacsConnection.closeConnection();
        } catch (Exception e) {
            log.error("", e);
        }
        return true;
    }

    public static DicomMatch[] getStudy(String _studyInstanceUID) {
        try {
            patient.reset();
            study.reset();
            serie.reset();
            patient.setLastName("*");
            study.setStudyInstanceUid(_studyInstanceUID);
            PacsConnector pacsConnection = new PacsConnector();
            DicomQueryDealerRemote queryDealerRemote = pacsConnection.getQueryDealer();
            dicomMatches = queryDealerRemote.studyRootMatch(patient, study, 500, "o3-dpacs-web", false);
            pacsConnection.closeConnection();
        } catch (Exception e) {
            log.error("", e);
        }
        return dicomMatches;
    }

    public static boolean getSeries(String _studyUID) {
        try {
            patient.reset();
            study.reset();
            serie.reset();
            patient.setLastName("*");
            study.setStudyInstanceUid(_studyUID);
            PacsConnector pacsConnection = new PacsConnector();
            DicomQueryDealerRemote queryDealerRemote = pacsConnection.getQueryDealer();
            dicomMatches = queryDealerRemote.studyRootMatch(patient, study, serie, "");
            // To remove all the older matches
            // SerieList.clear();
            for (int i = 0; i < dicomMatches.length - 1; i++) {
                SerieListItem serieListItem = new SerieListItem();
                serieListItem.setSerieInstanceUid(dicomMatches[i].series.getSeriesInstanceUid());
                serieListItem.setSerieDate(dicomMatches[i].study.getStudyDate());
                serieListItem.setSerieTime(dicomMatches[i].study.getStudyTime());
                serieListItem.setModality(dicomMatches[i].series.getModality());
                serieListItem.setSeriesDescription(dicomMatches[i].series.getSeriesDescription());
                serieListItem.setSerieStatus(dicomMatches[i].study.getStudyStatus());
                serieListItem.setSeriesNumber(dicomMatches[i].series.getSeriesNumber());
                serieListItem.setNumberOfSeriesRelatedInstances(dicomMatches[i].series.getNumberOfSeriesRelatedInstances());
                serieList().add(serieListItem);
            }
            pacsConnection.closeConnection();
        } catch (Exception e) {
            log.error("While retrieving series...", e);
            return false;
        }
        return true;
    }

    private static SerieList serieList() {
        FacesContext context = context();
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        ValueExpression ve = context.getApplication().getExpressionFactory().createValueExpression(elContext, "#{serieList}", Object.class);
        return ((SerieList) ve.getValue(elContext));
    }

    public static boolean getObjects(String _serieUid) {
        try {
            patient.reset();
            study.reset();
            serie.reset();
            imageObject.reset();
            patient.setLastName("*");
            serie.setSeriesInstanceUid(_serieUid);
            // Execute Query
            PacsConnector pacsConnection = new PacsConnector();
            DicomQueryDealerRemote queryDealerRemote = pacsConnection.getQueryDealer();
            dicomMatches = queryDealerRemote.queryInstanceLevel(patient, study, serie, new NonImage(), "");
            String wcenter = null;
            String wwidth = null;
            String photometricInterpretation = null;
            String pixelRepresentation = null;
            String numberOfFrames = null;
            boolean isHttps = false;
            InputStream is = null;
            SSLContext sctx = null;
            HttpServletRequest request = (HttpServletRequest) javax.faces.context.FacesContext.getCurrentInstance().getExternalContext().getRequest();
            HttpSession session = request.getSession();
            if (((String) session.getAttribute(ConfigurationSettings.CONFIG_WADOURL)).startsWith("https")) {
                isHttps = true;
                String wadoPortWeb = (String) session.getAttribute("myPort");
                System.setProperty("https.proxyPort", wadoPortWeb);
                sctx = SSLContext.getInstance("SSLv3");
                TrustManager[] tm = new TrustManager[] { new MyX509TrustManager() };
                KeyManager[] km = null;
                SecureRandom sr = null;
                sctx.init(km, tm, sr);
                SSLSocketFactory sslsocketfactory = sctx.getSocketFactory();
                HttpsURLConnection.setDefaultSSLSocketFactory(sslsocketfactory);
            }
            boolean ordinable = false;
            for (int i = 0; i < dicomMatches.length - 1; i++) {
                ordinable = false;
                ObjectListItem objectListItem = new ObjectListItem();
                objectListItem.setWadoUrl(dicomMatches[i].study.getStudyInstanceUid(), dicomMatches[i].series.getSeriesInstanceUid(), dicomMatches[i].instance.getSopInstanceUid());
                if (dicomMatches[i].instance instanceof StructRep) {
                    objectListItem.setModality("SR");
                    ((StructRep) dicomMatches[i].instance).getContentDate();
                    ((StructRep) dicomMatches[i].instance).getContentTime();
                    ((StructRep) dicomMatches[i].instance).getCompletionFlag();
                }
                if ((dicomMatches[i].instance instanceof Image) || (UIDs.VideoEndoscopicImageStorage.equals(dicomMatches[i].instance.getSopClassUid()))) {
                    if (dicomMatches[i].instance instanceof Image) {
                        objectListItem.setRows(((Image) dicomMatches[i].instance).getRows());
                        objectListItem.setColumns(((Image) dicomMatches[i].instance).getColumns());
                        objectListItem.setSize(((Image) dicomMatches[i].instance).getBitsStored());
                    }
                    objectListItem.setViewedCols(thumbsDimension);
                    objectListItem.setViewedRows(thumbsDimension);
                    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                    Document doc;
                    URL url = new URL(objectListItem.getWadoUrlForXML());
                    if ("https".equals(url.getProtocol())) {
                        HttpsURLConnection httpsurlconnection = (HttpsURLConnection) url.openConnection();
                        httpsurlconnection.setHostnameVerifier(new HostnameVerifier() {
                            public boolean verify(String hostname, SSLSession session) {
                                return true;// (hostname.equals(session.getPeerHost()));
                            }
                        });
                        httpsurlconnection.connect();
                        is = httpsurlconnection.getInputStream();
                        doc = docBuilder.parse(is);
                    } else {
                        is = url.openStream();
                        doc = docBuilder.parse(is);
                    }
                    is.close();
                    doc.getDocumentElement().normalize();
                    // recupero l'elemento root del file
                    NodeList nodeLst = doc.getElementsByTagName("root");
                    Node _rootNode = nodeLst.item(0);
                    Element _rootElement = (Element) _rootNode;
                    NodeList fstNmElmntLst = _rootElement.getElementsByTagName("PhotometricInterpretation");
                    Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
                    NodeList fstNm = fstNmElmnt.getChildNodes();
                    photometricInterpretation = ((Node) fstNm.item(0)).getNodeValue();
                    // retrieve windowCenter and windowWidth value if image
                    // is monohrome1 and monochrome2
                    if (photometricInterpretation.equals("MONOCHROME1") || photometricInterpretation.equals("MONOCHROME2")) {
                        fstNmElmntLst = _rootElement.getElementsByTagName("WindowCenter");
                        fstNmElmnt = (Element) fstNmElmntLst.item(0);
                        fstNm = fstNmElmnt.getChildNodes();
                        // if value is null the windowCenter is in the
                        // middle of image
                        if (!((Node) fstNm.item(0)).getNodeValue().equals("null")) {
                            wcenter = ((Node) fstNm.item(0)).getNodeValue();
                        } else {
                            wcenter = Double.toString(Double.parseDouble(objectListItem.get_maxWindowSize()) / 2);
                        }
                        // retrieve windowWidth value, if null windowWidth
                        // is full open
                        fstNmElmntLst = _rootElement.getElementsByTagName("WindowWidth");
                        fstNmElmnt = (Element) fstNmElmntLst.item(0);
                        fstNm = fstNmElmnt.getChildNodes();
                        if (!((Node) fstNm.item(0)).getNodeValue().equals("null")) {
                            wwidth = ((Node) fstNm.item(0)).getNodeValue();
                        } else {
                            wwidth = objectListItem.get_maxWindowSize();
                        }
                    }
                    String transferSyntax;
                    fstNmElmntLst = _rootElement.getElementsByTagName("TransferSyntax");
                    fstNmElmnt = (Element) fstNmElmntLst.item(0);
                    fstNm = fstNmElmnt.getChildNodes();
                    transferSyntax = ((Node) fstNm.item(0)).getNodeValue();
                    objectListItem.setTransferSyntax(transferSyntax);
                    fstNmElmntLst = _rootElement.getElementsByTagName("PixelRepresentation");
                    fstNmElmnt = (Element) fstNmElmntLst.item(0);
                    fstNm = fstNmElmnt.getChildNodes();
                    pixelRepresentation = ((Node) fstNm.item(0)).getNodeValue();
                    fstNmElmntLst = _rootElement.getElementsByTagName("NumberOfFrames");
                    fstNmElmnt = (Element) fstNmElmntLst.item(0);
                    fstNm = fstNmElmnt.getChildNodes();
                    numberOfFrames = ((Node) fstNm.item(0)).getNodeValue();
                    fstNmElmntLst = _rootElement.getElementsByTagName("ImagePosition");
                    fstNmElmnt = (Element) fstNmElmntLst.item(0);
                    NodeList positionNode = fstNmElmnt.getChildNodes();
                    if (positionNode.getLength() > 0) {
                        String[] imagePosition = new String[positionNode.getLength()];
                        for (int pos = 0; pos < positionNode.getLength(); pos++) {
                            imagePosition[pos] = ((Node) positionNode.item(pos)).getTextContent();
                        }
                        objectListItem.setImagePosition(imagePosition);
                        ordinable = true;
                    }
                    fstNmElmntLst = _rootElement.getElementsByTagName("Modality");
                    fstNmElmnt = (Element) fstNmElmntLst.item(0);
                    objectListItem.setModality(fstNmElmnt.getTextContent());
                    objectListItem.setIsMultiframe(numberOfFrames);
                    objectListItem.setWcenter(wcenter);
                    objectListItem.setWwidth(wwidth);
                    objectListItem.set_pixelRepresentation(pixelRepresentation);
                    objectListItem.set_photmetricInterpretation(photometricInterpretation);
                }
                objectListItem.setInstanceNumber(dicomMatches[i].instance.getInstanceNumber());
                objectList().add(objectListItem);
            }
            if (ordinable) {
                ObjectListItem[] obiArray = new ObjectListItem[objectList().getObjectsMatched().size()];
                objectList().getObjectsMatched().toArray(obiArray);
                Arrays.sort(obiArray);
                objectList().getObjectsMatched().clear();
                for (int k = 0; k < obiArray.length; k++) {
                    objectList().add(obiArray[k]);
                }
            }
            // context().getExternalContext().getSessionMap().put("imagesNumber","0");
            // context().getExternalContext().getSessionMap().put("imageLoadedNumber","0");
            pacsConnection.closeConnection();
        } catch (Exception e) {
            log.error("", e);
        }
        return true;
    }

    // Reads the thumbsDimension from DB
    private static String getThumbsDimension() {
        // String ret = DEFAULTCONFIG_THUMBS_DIMENSION;
        return thumbsDimension;
    }

    private static ObjectList objectList() {
        FacesContext context = context();
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        ValueExpression ve = context.getApplication().getExpressionFactory().createValueExpression(elContext, "#{objectList}", Object.class);
        return ((ObjectList) ve.getValue(elContext));
    }

    private static StudyList studyList() {
        FacesContext context = context();
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        ValueExpression ve = context.getApplication().getExpressionFactory().createValueExpression(elContext, "#{studyList}", Object.class);
        return ((StudyList) ve.getValue(elContext));
    }

    protected static FacesContext context() {
        return (FacesContext.getCurrentInstance());
    }
}