/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;


import it.units.htl.maps.util.SessionManager;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

public class ServicesConfigurationHome {
    private static final Log log = LogFactory.getLog(ServicesConfigurationHome.class);
    

    public ServicesConfiguration findByServiceName(java.lang.String serviceName) {
        ServicesConfiguration loadedConfig = null;
        Session s = null;
        try {
            SessionFactory sm = SessionManager.getInstance();
            s = sm.openSession();
            loadedConfig = (ServicesConfiguration) s.get(ServicesConfiguration.class, serviceName);
        } catch (RuntimeException rex) {
            log.error("get failed", rex);
            throw rex;
        } finally {
            try {
                s.close();
            } catch (Exception ex) {
            }
        }
        return loadedConfig;
    }

    public List<ServicesConfiguration> findByExample(ServicesConfiguration instance) {
        log.debug("finding ServicesConfiguration instance by example");
        Session s = null;
        try {
            SessionFactory sm = SessionManager.getInstance();
            s = sm.openSession();
            List results = s.createCriteria("it.units.htl.maps.ServicesConfiguration").add(Example.create(instance)).list();
            log.debug("find by example successful, result size: " + results.size());
            return results;
        } catch (RuntimeException re) {
            log.error("find by example failed", re);
            throw re;
        } finally {
            try {
                s.close();
            } catch (Exception ex) {
            }
        }
    }

    public List<ServicesConfiguration> getAll() {
        log.debug("finding ServicesConfiguration instance by example");
        Session s = null;
        try {
            s = SessionManager.getInstance().openSession();
            List results = s.createCriteria("it.units.htl.maps.ServicesConfiguration").list();
            log.debug("find by example successful, result size: " + results.size());
            return results;
        } catch (RuntimeException re) {
            log.error("find by example failed", re);
            throw re;
        } finally {
            try {
                s.clear();
                s.flush();
                s.close();
                s = null;
            } catch (Exception e) {
                log.warn("Unable to close the session...", e);
            }
        }
    }
}
