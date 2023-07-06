/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.atna;

import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.ServicesConfigurationHome;

import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Logger;

import org.dcm4che2.audit.log4j.helpers.SyslogWriter;
import org.dcm4che2.audit.log4j.net.SyslogAppender;
import org.dcm4che2.audit.message.AuditMessage;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class AuditLogService implements AuditLogServiceMBean {
    private static AuditLogService INSTANCE;
    private static Log log = LogFactory.getLog(AuditLogService.class);
    private static Log syslog = LogFactory.getLog("atnaSyslog");
    
    private String sysLogHost = null;
    private int sysLogPort = 0;
    private boolean status = false;

    public static AuditLogService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuditLogService();
        }
        return INSTANCE;
    }

    private AuditLogService() {
    }

    private SyslogWriter getWriter() {
        if ((sysLogHost != null) && (sysLogPort != 0) && status) {
            return new SyslogWriter(sysLogHost + ":" + sysLogPort, null);
        } else {
            return null;
        }
    }

    public boolean SendMessage(AuditMessage msg) {
        try {
            msg.validate();
        } catch (Exception e) {
            log.error("AuditMessage Not Valid", e);
            return false;
        }
        syslog.info(msg);
        
        
        return true;
    }

    public boolean reloadSettings() {
        log.debug("Try to load auditConfiguration!");
        Document doc = null;
        ServicesConfigurationHome sch = new ServicesConfigurationHome();
        ServicesConfiguration sc = sch.findByServiceName("AuditLogServer");
        try {
            SAXBuilder builder = new SAXBuilder();
            if (sc.getConfiguration() != null) {
                doc = builder.build(new StringReader(sc.getConfiguration()));
            } else {
                throw new Exception("No configuration present in the DB! please check the configuration...");
            }
            Element root = doc.getRootElement().getChild("Attributes");
            Element param = root.getChild("SysLogHost");
            if (param != null) {
                sysLogHost = param.getText();
            }
            param = root.getChild("SysLogPort");
            if (param != null) {
                sysLogPort = Integer.parseInt(param.getText());
            }
            
        	
        } catch (JDOMException jdex) {
            log.error("Parsing AuditLogConf FAILED", jdex);
            return false;
        } catch (Exception ex) {
            log.error("Could not find AuditLogConfig", ex);
            return false;
        }
        return true;
    }

    public boolean startService() {
        status = reloadSettings();
        return status;
    }

    public boolean stopService() {
        status = false;
        return true;
    }

    public boolean statusService() {
        return status;
    }
}
