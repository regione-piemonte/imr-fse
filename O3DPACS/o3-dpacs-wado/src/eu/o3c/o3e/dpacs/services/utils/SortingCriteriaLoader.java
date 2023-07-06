/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.dpacs.services.utils;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.LogMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.o3c.o3e.dpacs.services.utils.sorting.SortingCriteria;
import eu.o3c.o3e.dpacs.services.utils.sorting.SortingDirection;
import eu.o3c.o3e.dpacs.services.utils.sorting.SortingType;

public class SortingCriteriaLoader {
    private Log log = LogFactory.getLog(SortingCriteriaLoader.class);
    private HashMap<String, SortingCriteria> sortingCrits = new HashMap<String, SortingCriteria>();

    public SortingCriteriaLoader() {
        Connection con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            con = getDataSource().getConnection();
            st = con.prepareStatement("select  paramValue from GlobalConfiguration where paramKey = ?");
            st.setString(1, "seriesSortCriteria");
            st.execute();
            rs = st.getResultSet();
            if (rs.next()) {
                setCriterias(rs.getString(1));
            } else {
                log.warn("No sorting criteria found. The defaults (hard coded) will be used. ");
                setDefaultsCriteria();
            }
        } catch (Exception ex) {
            log.warn("Unable to load the sorting criteria. The defaults (hard coded) will be used.", ex);
            setDefaultsCriteria();
        } finally {
            CloseableUtils.close(rs);
            CloseableUtils.close(st);
            CloseableUtils.close(con);
        }
    }

   
    public SortingCriteria getSortingCriteriaFormodality(String modality) {
        log.debug("getCriteria for " + modality);
        SortingCriteria sc = null;
        if (modality == null)
            sc = sortingCrits.get("default");
        else
            sc = sortingCrits.get(modality);
        if (sc == null) {
            sc = new SortingCriteria();
            sc.useCriteria(SortingType.INSTANCE_NUMBER);
            sc.setSortingDirection(SortingType.INSTANCE_NUMBER, SortingDirection.ASCENDING);
        }
        return sc;
    }

    private void setDefaultsCriteria() {
        SortingCriteria sc = new SortingCriteria();
        sc.useCriteria(SortingType.INSTANCE_NUMBER);
        sc.setSortingDirection(SortingType.INSTANCE_NUMBER, SortingDirection.ASCENDING);
        sortingCrits.put("default", sc);
    }

    private void setCriterias(String configuration) {
        try {
            Document doc = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(configuration.getBytes());
            doc = db.parse(is);
            NodeList modalities = doc.getElementsByTagName("modality");
            for (int i = 0; i < modalities.getLength(); i++){
                Node modality = modalities.item(i);
                String modalityCode = ((Element)modality).getAttribute("code");
                NodeList criterias = ((Element)modality).getElementsByTagName("criteria");
                SortingCriteria sc = new SortingCriteria();
                for(int k=0; k < criterias.getLength(); k++){
                    Node criteria = criterias.item(k);
                    sc.useCriteria(SortingType.valueOf(((Element)criteria).getAttribute("type")));
                    sc.setSortingDirection(SortingType.valueOf(((Element)criteria).getAttribute("type")),(SortingDirection.valueOf(((Element)criteria).getAttribute("order"))));
                }
                
                sortingCrits.put(modalityCode, sc);
            }
        } catch (Exception e) {
            log.warn("Unable to set criteria");
            setDefaultsCriteria();
        }
    }

    private DataSource getDataSource() {
        DataSource ds = null;
        try {
            Context jndiContext = new InitialContext();
            ds = (DataSource) jndiContext.lookup("java:/jdbc/wadoDS");
        } catch (NamingException nex) {
            log.fatal(LogMessage._NoDatasource, nex);
            try {
                throw nex;
            } catch (Exception e) {
            }
        }
        return ds;
    }
}
