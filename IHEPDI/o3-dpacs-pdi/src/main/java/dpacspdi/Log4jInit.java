/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package dpacspdi;

import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import dao.PdiRetrieveManager;
import helpers.ConfigurationSettings;

public class Log4jInit extends HttpServlet {

	private static final long serialVersionUID = 4923986907986937189L;
	private Logger log = Logger.getLogger(this.getClass().getName());
  
  public void init()
  {
    try
    {
      ServletContext context = getServletContext();
      URL url = context.getResource("/WEB-INF/log4j.properties");
      PropertyConfigurator.configure(url);
//      System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.SimpleLog");
//      System.setProperty("org.apache.commons.logging.simplelog.defaultlog","warn");
//      System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.SimpleLog");
      
      log.info("Reset numCurrenteThread");
      PdiRetrieveManager prm = new PdiRetrieveManager();
      prm.updateTo3pdiConf(ConfigurationSettings.NUM_CURRENT_THREAD, String.valueOf(0));
      log.info("Reset numCurrenteThread OK");
      
      
      log.info("LOG4J LOAD");
    }
    catch (Exception e)
    {
    	log.error("LOG4J ERROR", e);
    }
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res) {}
}

