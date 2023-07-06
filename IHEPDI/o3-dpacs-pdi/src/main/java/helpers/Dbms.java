/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package helpers;

import java.sql.Connection;

/**
 * Classe che si occupa delle interazioni con il database
 */
public class Dbms {

	private static volatile Boolean oracle;
	
	/**
	 * Metodo utile alla verifica del tipo di connessione
	 * @param conn connessione
	 * @return true nel caso in cui la connessione fa riferimento ad un database di tipo Oracle, false altrimenti
	 */
	public static boolean isOracle(Connection conn){
		if (oracle == null) {
			try {
				oracle = new Boolean(conn.getMetaData().getURL().startsWith("jdbc:oracle:"));
			} catch (Exception ex) {
				oracle = new Boolean(false);
			}
		}
		
		return oracle.booleanValue();
	}
	
	/**
	 * Metod che verifica se la connessione e' esterna  
	 * @param con connessione
	 * @return true se la connessione al database e' esterna, false altrimenti
	 */
	public static boolean isExternalOracle(Connection con){
		boolean ret = false;
		try {
			ret = con.getMetaData().getURL().startsWith("jdbc:oracle:");
		} catch (Exception ex) {
			ret = false;
		}
		
		return ret;
	}
	
}
