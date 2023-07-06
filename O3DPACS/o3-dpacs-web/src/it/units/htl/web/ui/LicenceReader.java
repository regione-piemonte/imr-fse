/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.InputStream;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;






public class LicenceReader {
	
	private final String CUSTOMER_CODE_REGEX = "[Cc][Uu][Ss][Tt][Oo][Mm][Ee][Rr]: .*";
	private String customer=null;
	private boolean readFile=false;

	
	public String getMarkedTo(){
		  if (!readFile)
		  searchFile();
		  return customer;
		 }
	
	
	private void searchFile(){
		
		InputStream is = null;

		
		  
		  try{
		   
			  
			  
			  is = LicenceReader.class.getResourceAsStream
			  ("/serial.rel.pdf");
			  
			  PdfReader reader = new PdfReader(is);			  
			  PdfTextExtractor textExtractor = new PdfTextExtractor(reader);
			  String text = textExtractor.getTextFromPage(1);
			  
			  Pattern pat = Pattern.compile(CUSTOMER_CODE_REGEX);
			  Matcher match = pat.matcher(text);
			  
			  if (match.find()) {
				  customer = match.group();
				  customer = customer.substring(10);
				  
				
				
			  } else {
				  customer=null; 
			  }
			  
			  
		   readFile=true;
		   
		  
		   
		  }catch(Exception ex){
		   customer=null;
		  }
		  finally {
		     
			  if (is!=null)
				try {
					is.close();
				} catch (IOException e) {
				}
		    }
		 }

	



	private boolean isMarked(){
	  if (!readFile)
	   searchFile();
	  return customer==null;
	 }


}