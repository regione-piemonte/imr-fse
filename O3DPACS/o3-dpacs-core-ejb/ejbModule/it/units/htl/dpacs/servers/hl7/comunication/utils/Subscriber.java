/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7.comunication.utils;

import org.w3c.dom.Document;

import it.units.htl.dpacs.helpers.ConfigurationLoader;

public class Subscriber {
    public static enum ConfigurationProperty {
        sendigApp,
        receivingApp,
        numberOfRetries,
        periodInMillis,
        email,
        zds;
    }

    public long pk;
    public String name;
    public String address;
    public String configuration;

    
    public String getIp(){
        return address.split(":")[0];
    }
    
    public Integer getPort(){
        return Integer.parseInt(address.split(":")[1]);
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
