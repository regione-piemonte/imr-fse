/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

public class DbmsTransformer {

	public static String getDateAdditionString(String connectionUrl, String columnToAdd, String secondsToAdd) throws Exception{
		String ret="";
		
			if(connectionUrl.contains(":mysql"))
				ret="DATE_ADD("+columnToAdd+", INTERVAL "+secondsToAdd+" SECOND)";
			// ADD HERE ADDITIONAL SUPPORTED DBMSes
			else		// DEFAULT: MySQL
				ret="DATE_ADD("+columnToAdd+", INTERVAL "+secondsToAdd+" SECOND)";
		
		
		return ret;
		
	}
	
	public static String getCurrentUtcTimeString(String connectionUrl) throws Exception{
		String ret="";
		
			if(connectionUrl.contains(":mysql"))
				ret="UTC_TIMESTAMP()";			
			else		// DEFAULT: MySQL
				ret="UTC_TIMESTAMP()";
		
		return ret;
		
	}
	
	public static String getLimitHeaderString(String connectionUrl, String limitValue) throws Exception{
		String ret="";
		
			if(connectionUrl.contains(":mysql"))
				ret="";
			// For example, SQL Server would return "TOP "+limitValue 
			// ADD HERE ADDITIONAL SUPPORTED DBMSes
			else	// DEFAULT: MySQL
				ret="";
		
		return ret;
		
	}
	
	public static String getLimitFooterString(String connectionUrl, String limitValue) throws Exception{
		String ret="";
		
			if(connectionUrl.contains(":mysql"))
				ret="LIMIT "+ limitValue;
			// ADD HERE ADDITIONAL SUPPORTED DBMSes
			else	// DEFAULT: MySQL
				ret="LIMIT "+ limitValue;
		
		return ret;
		
	}

	public static String getConcatString(String connectionUrl, String arg1, String arg2) throws Exception{
		String ret="";
		
			if(connectionUrl.contains(":mysql"))
				ret="CONCAT("+arg1+","+arg2+")";
			else	// DEFAULT: MySQL
				ret="CONCAT("+arg1+","+arg2+")";
		
		return ret;		
	}
	
	public static String getSubstringFromStart(String connectionUrl, String arg, int length) throws Exception{
		String ret="";
		
			if(connectionUrl.contains(":mysql"))
				ret="SUBSTR("+arg+",1,"+length+")";
			else	// DEFAULT: MySQL
				ret="SUBSTR("+arg+",1,"+length+")";
		
		return ret;	
	}

	
}
