/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.core;

import java.io.StringReader;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * The class manages the retrieval of MBean variables value from named XML file.
 * Each managed bean in O3-DPACS, is associated to a XML file in the core
 * package where you should put the default values.
 * 
 * @author Claudio Tomasi
 */
public class VarLoader {

	private Document doc = null;
	private String configXML;
	public static final int FROMFILE = 1;
	public static final int FROMSTRING = 2;
	private int mode = 1;
	private static Log log = LogFactory.getLog(VarLoader.class);

	public VarLoader(String xmlFileName, int mod) {
		this.configXML = xmlFileName;
		this.mode = mod;
	}
	
	public VarLoader(String xmlFileName) {
		this.configXML = xmlFileName;
		this.mode = FROMFILE;
	}

	/**
	 * The methods return the values of a selected Attributes list
	 * 
	 * @return an array of the retrieved strings
	 */
	public String[] getStringValues() {
		List<Element> TagList = getSettings("Attributes");
		String[] returnString = new String[TagList.size()];
		int i = 0;
		while (i < TagList.size()) {
			Element el = TagList.get(i);
			returnString[i] = el.getTextNormalize();
			i++;
		}
		return returnString;
	}

	/**
	 * The method browses a TagList and gets the names
	 * 
	 * @return an array of the strings retrieved from tagList Names
	 */
	public String[] getNameOfValues() {
		List<Element> TagList = getSettings("Attributes");
		String[] returnString = new String[TagList.size()];
		int i = 0;
		while (i < TagList.size()) {
			Element el = TagList.get(i);
			returnString[i] = el.getName();
			i++;
		}
		return returnString;
	}

	/**
	 * The method creates a DOM document from a string, being the root element
	 * in the xml file
	 * 
	 * @param varString
	 *            string
	 * @return the list of children tags from root
	 */
	@SuppressWarnings("unchecked")
    private List<Element> getSettings(String varString) {
		try {
			SAXBuilder builder = new SAXBuilder();
			
			switch (this.mode) {
			case FROMFILE:
				URL url = getClass().getResource(this.configXML);
				this.doc = builder.build(url);
				break;
			case FROMSTRING:
				this.doc = builder.build(new StringReader(this.configXML));
				break;
			}
		} catch (JDOMException jdex) {
			log.error("Unable to load xml data", jdex);
		} catch (Exception ioex) {
			log.error("Unable to load xml data", ioex);
		}
		Element root = doc.getRootElement();
		List<Element> TagList = ((Element)root.getChildren(varString).get(0)).getChildren();//Find(varString, root.getChildren()); 
		return TagList;
	}	
}
