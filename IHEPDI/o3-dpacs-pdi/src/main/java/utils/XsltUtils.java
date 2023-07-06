/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import com.sun.org.apache.xalan.internal.xsltc.trax.*;

import dto.DICOMVIEWERDATA;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Classe di supporto all'utilizzo di XSLT
 */
public class XsltUtils {
	
	private static final Logger log = Logger.getLogger(DicomViewGenerator.class);
	
	/**
	 * Produce l'htm a partire dal'xml applicando l'xslt
	 * @param xmlPath il path dell'xml
	 * @param htmPath il path dell'htm generato
	 * @param xsltPath il path dell'xslt
	 */
	public static Boolean makeXslTrasformation(String xmlPath, String htmPath, String xsltPath) {
		log.info("Getting all files...");
		try {
			File fileIn = new File(xmlPath);
			File fileOut = new File(htmPath); 
			File xslt = new File (xsltPath);
			String s = null;
			s = s.equals("") ? s :"";
			
			log.info("Applying XSLT...");
			xmlTransform(xslt, fileIn, fileOut);
			log.info("XSLT applied");
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			log.error("Cannot make xml trasformation due to: " + e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Produce l'htm a partire dal'xml applicando l'xslt
	 * @param xmlData oggettto contenente le informazioni da utilizzare per al trasformazione XSLT
	 * @param htmPath il path dell'htm generato
	 * @param xsltPath il path dell'xslt
	 */
	public static Boolean makeXslTrasformation(DICOMVIEWERDATA xmlData, String htmPath, String xsltPath) {
		log.info("Getting all files...");
		try {

			String	xmlString	= convertClassToXML(xmlData, DICOMVIEWERDATA.class);
			File fileOut = new File(htmPath); 
			File xslt = new File (xsltPath);

			log.info("Applying XSLT to XML...");
			log.debug(xmlString);
			xmlTransform(xslt, xmlString, fileOut);
			log.info("XSLT applied");
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			log.error("Cannot make xml trasformation due to: " + e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Applica una trasformazione xslt ad un file generandone un altro
	 * Utilizza il motore xslt standard di Java
	 * @param xslFile the xsl file
	 * @param xmlString stringa contenente l'xml utilizzato per la generazione delle pagine
	 * @param outFile the out file
	 * @throws IOException Signals that an I/O exception has occurred
	 * @throws URISyntaxException the URI syntax exception
	 * @throws TransformerException the transformer exception
	 */
	private static void xmlTransform(File xslFile, File fileIn, File outFile) throws IOException, URISyntaxException, TransformerException {
		//TransformerFactory factory = TransformerFactory.newInstance();
		
		TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl();
		Source xslt = new StreamSource(xslFile);
		Transformer transformer = factory.newTransformer(xslt);
		 
		transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
		Source text = new StreamSource(fileIn);
		transformer.transform(text, new StreamResult(outFile));
	}
	
	/**
	 * Applica una trasformazione xslt ad un file generandone un altro
	 * Utilizza il motore xslt standard di Java
	 * @param xslFile the xsl file
	 * @param xmlString stringa contenente l'xml utilizzato per la generazione delle pagine
	 * @param outFile the out file
	 * @throws IOException Signals that an I/O exception has occurred
	 * @throws URISyntaxException the URI syntax exception
	 * @throws TransformerException the transformer exception
	 */
	private static void xmlTransform(File xslFile, String xmlString, File outFile) throws IOException, URISyntaxException, TransformerException {
		//TransformerFactory factory = TransformerFactory.newInstance();
		
		TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl();
		Source xslt = new StreamSource(xslFile);
		Transformer transformer = factory.newTransformer(xslt);
		 
		transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
		Source text = new StreamSource(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
		transformer.transform(text, new StreamResult(outFile));
	}
	
	
	/**
	 * Convert class to XML.
	 *
	 * @param obj the obj
	 * @param myClass the my class
	 * @return the string
	 */
	public static String convertClassToXML(Object obj, Class myClass){
		try {
			
			JAXBContext jaxbContext = JAXBContext.newInstance(myClass);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
			StringWriter sw = new StringWriter();
		
			jaxbMarshaller.marshal(obj, sw);
			String xmlString = sw.toString();
						
			return xmlString;
		
		} catch (JAXBException e) {
			e.printStackTrace();
			System.err.println("Errore durante marshalling Object: " + e.getMessage());
			log.error("Errore durante marshalling Object: " + e.getMessage(), e);
			return null;
		}
		
	}
	
}
