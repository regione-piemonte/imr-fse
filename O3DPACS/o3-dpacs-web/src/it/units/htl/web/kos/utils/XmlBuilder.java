/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.kos.utils;

import it.units.htl.maps.KnownNodes;
import it.units.htl.maps.util.SessionManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class XmlBuilder {
    final Log log = LogFactory.getLog(XmlBuilder.class);
    // used to keep in mind which dicom tags are necessary to build the xml file
    private HashMap<POSITION, HashMap<String, Integer>> necessaryTag = new HashMap<POSITION, HashMap<String, Integer>>();
    // used to keep the current position when lookup for the necessary tags.
    private POSITION curPos = null;
    
    private String retrieveAeTitle = null;

    // the possible positions
    private enum POSITION {
        Patient, Study, Series, Instance
    };

    public XmlBuilder() {
    }

    public Document fromKos(DicomObject source) throws Exception {
        log.debug("Start converting kos source into xml format");
        Document doc = null;
        Document baseDoc = null;
        baseDoc = getBaseDocument();
        Element wtlPart = (Element) baseDoc.getElementsByTagName("Wtl").item(0);
        necessaryTag.clear();
        necessaryTag.put(POSITION.Patient, new HashMap<String, Integer>());
        necessaryTag.put(POSITION.Study, new HashMap<String, Integer>());
        necessaryTag.put(POSITION.Series, new HashMap<String, Integer>());
        necessaryTag.put(POSITION.Instance, new HashMap<String, Integer>());
        necessaryTag(wtlPart);
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        doc = docBuilder.newDocument();
        doc = (Document) addChildren(source, doc, null, POSITION.Patient);
        log.debug("Import new Patient's node into wtl: " + wtlPart.appendChild(baseDoc.importNode(doc.getElementsByTagName("Patient").item(0), true)) != null);
        wtlPart.removeChild(baseDoc.getElementsByTagName("Patient").item(0));
        return baseDoc;
    }

    private Document getBaseDocument() throws SAXException, IOException {
        DOMParser parser = new DOMParser();
        parser.parse(new InputSource(this.getClass().getResourceAsStream("embConfig.xml")));
        return parser.getDocument();
    }

    private void necessaryTag(Element where) {
        NodeList wtlNodes = where.getChildNodes();
        for (int i = 0; i < wtlNodes.getLength(); i++) {
            if (!wtlNodes.item(i).getNodeName().equals("#text")) {
                String tagName = ((Element) wtlNodes.item(i)).getTagName();
                boolean isLeaf = !((Element) wtlNodes.item(i)).hasChildNodes();
                if (isLeaf) {
                    Integer dicomName = null;
                    try {
                        log.debug("Leaf: " + tagName + " Of " + curPos + " which in dicom is " + Class.forName("org.dcm4che2.data.Tag").getField(tagName).get(null));
                        dicomName = (Integer) Class.forName("org.dcm4che2.data.Tag").getField(tagName).get(null);
                    } catch (NoSuchFieldException e) {
                        log.error("Requested tag doesn't exist. --> " + tagName);
                    } catch (Exception e) {
                        log.error("While checking or tag.", e);
                    }
                    if (dicomName != null) {
                        necessaryTag.get(curPos).put(tagName, dicomName);
                    }
                } else {
                    curPos = POSITION.valueOf(tagName);
                    necessaryTag((Element) wtlNodes.item(i));
                }
            }
        }
    }

    private String wadoURLofReqAETitle = null;
    private String currStudy = null;
    private String currSeries = null;

    @SuppressWarnings("unchecked")
    private Node addChildren(DicomObject source, Document destination, Element parent, POSITION level) throws Exception {
        Element levelNode = destination.createElement(level.name());
        HashMap<String, Integer> necessaryTagForLevel = necessaryTag.get(level);
        for (String tagName : necessaryTagForLevel.keySet()) {
            Element tag = destination.createElement(tagName);
            Text tagValue = destination.createTextNode((source.getString(necessaryTagForLevel.get(tagName)) != null) ? source.getString(necessaryTagForLevel.get(tagName)) : "");
            tag.appendChild(tagValue);
            levelNode.appendChild(tag);
        }
        switch (level) {
        case Patient:
            addChildren(source.get(Tag.CurrentRequestedProcedureEvidenceSequence).getDicomObject(0), destination, levelNode, POSITION.Study);
            destination.appendChild(levelNode);
            break;
        case Study:
            DicomElement series = source.get(Tag.ReferencedSeriesSequence);
            currStudy = source.getString(Tag.StudyInstanceUID);
            Text studyDescription = destination.createTextNode("S:" + currStudy);
            levelNode.getElementsByTagName("StudyDescription").item(0).appendChild(studyDescription);
            for (int i = 0; i < series.countItems(); i++) {
                addChildren(series.getDicomObject(i), destination, levelNode, POSITION.Series);
                parent.appendChild(levelNode);
            }
            break;
        case Series:
            log.debug("This series is retrievable in : " + source.getString(Tag.RetrieveAETitle));
            String aeTitle = source.getString(Tag.RetrieveAETitle);
            if(retrieveAeTitle == null)retrieveAeTitle = aeTitle;
            Session s = SessionManager.getInstance().openSession();
            List<KnownNodes> res = s.createCriteria(KnownNodes.class).add(Expression.eq("aeTitle", aeTitle)).list();
            if ((res.size() == 1) && (res.get(0).getWadoURL() != null) && (!"".equals(res.get(0).getWadoURL()))) {
                log.debug("The wado url of " + aeTitle + " is " + res.get(0).getWadoURL());
                wadoURLofReqAETitle = ((KnownNodes) res.get(0)).getWadoURL();
            } else {
                throw new Exception("No configuration found for this AETITLE: " + aeTitle);
            }
            DicomElement instances = source.get(Tag.ReferencedSOPSequence);
            currSeries = source.getString(Tag.SeriesInstanceUID);
            String mod = ""; 
            if(instances.countItems() > 0){
                String sopClass = instances.getDicomObject(0).getString(Tag.ReferencedSOPClassUID);
                if(UID.KeyObjectSelectionDocumentStorage.equals(sopClass)){
                    mod = "KO";
                }else{
                    mod = "OT";
                }
            }
            levelNode.getElementsByTagName("Modality").item(0).setTextContent(source.getString(Tag.Modality,mod));
            Text seriesDescription = destination.createTextNode("S:" + currSeries);
            levelNode.getElementsByTagName("SeriesDescription").item(0).appendChild(seriesDescription);
            for (int i = 0; i < instances.countItems(); i++) {
                addChildren(instances.getDicomObject(i), destination, levelNode, POSITION.Instance);
                parent.appendChild(levelNode);
            }
            break;
        case Instance:
            String url = wadoURLofReqAETitle + "?requestType=WADO&" + "studyUID=" + currStudy + "&"
                    + "seriesUID=" + currSeries + "&" + "objectUID=" + source.getString(Tag.ReferencedSOPInstanceUID) + "&contentType=application/dicom";
            Text retrValue = destination.createTextNode(url);
            levelNode.getElementsByTagName("ReferencedFileID").item(0).appendChild(retrValue);
            Text sopInstanceUID = destination.createTextNode(source.getString(Tag.ReferencedSOPInstanceUID));
            levelNode.getElementsByTagName("SOPInstanceUID").item(0).appendChild(sopInstanceUID);
            parent.appendChild(levelNode);
            break;
        }
        return destination;
    }

    public String getRetrieveAeTitle() {
        return retrieveAeTitle;
    }
}
