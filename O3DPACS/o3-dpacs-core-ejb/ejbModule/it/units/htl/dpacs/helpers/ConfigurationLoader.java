/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ConfigurationLoader {
    private Logger log = Logger.getLogger(ConfigurationLoader.class);

    public Document loadConfiguration(Object whatToLoad) throws Exception, IOException {
        log.debug("Loading a configuration...");
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        if (whatToLoad instanceof File) {
            doc = db.parse((File) whatToLoad);
        } else if (whatToLoad instanceof String) {
            
            InputSource is = new InputSource(new StringReader((String) whatToLoad));
            doc = db.parse(is);
        }
        doc.getDocumentElement().normalize();
        return doc;
    }

    public String getNodeValueInPosition(Document doc, String nodeName, int position) {
        String nodeValue = null;
        NodeList node = (NodeList) doc.getElementsByTagName(nodeName);
        if (node.getLength() > 0) {
            if (node.getLength() <= position) {
                nodeValue = node.item(position).getTextContent();
            } else {
                nodeValue = node.item(0).getTextContent();
            }
        }
        return nodeValue;
    }

    public String getNodeValue(Document doc, String nodeName) {
        return getNodeValueInPosition(doc, nodeName, 0);
    }
}
