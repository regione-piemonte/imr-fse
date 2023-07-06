/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import java.sql.Connection;

public class Dbms {

	private static volatile Boolean oracle;
	
	public static boolean isOracle(Connection conn){
		if(oracle==null){
			try {
				oracle=new Boolean(conn.getMetaData().getURL().startsWith("jdbc:oracle:"));
			} catch (Exception ex) {
				oracle=new Boolean(false);
			}
		}
		return oracle.booleanValue();
	}
	
	public static boolean isExternalOracle(Connection con){
		boolean ret=false;
		try {
			ret=con.getMetaData().getURL().startsWith("jdbc:oracle:");
		} catch (Exception ex) {
			ret=false;
		}
		return ret;
	}
	
}
