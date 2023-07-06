/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.launcher.utils;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.ConfigurationLoader;
import it.units.htl.web.Connector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

public class ViewerConfigRetriever {
    private Log log = LogFactory.getLog(ViewerConfigRetriever.class);
    private String configuration;

    public ViewerConfigRetriever(String viewerID) throws Exception {
        log.info("Get configuration for " + viewerID);
        configuration = getViewerConfig(viewerID);
        if (configuration == null || "".equals(configuration)) {
            throw new Exception("No viewer found for id : " + viewerID);
        }
    }

    private String getViewerConfig(String viewerID) {
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        String config = null;
        try{
            connection = Connector.getInstance().getConnection();
            st = connection.prepareStatement("SELECT config FROM ViewersConfig WHERE viewerId = ?");
            st.setString(1, viewerID);
            rs = st.executeQuery();
            if(rs.next()){
                config = rs.getString(1);
            }
        }catch(Exception ex){
            log.error("",ex);
        }finally{
            CloseableUtils.close(rs);
            CloseableUtils.close(st);
            CloseableUtils.close(connection);
        }
        return config;
    }

    public String getConfigurationValue(String key) throws NoSuchFieldException, Exception {
        if (configuration == null) {
            throw new NoSuchFieldException("No configuration for the node. " + configuration);
        } else {
            ConfigurationLoader cfg = new ConfigurationLoader();
            Document doc = cfg.loadConfiguration(configuration);
            return cfg.getNodeValue(doc, key);
        }
    }
}
