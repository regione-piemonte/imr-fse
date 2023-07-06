/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7.comunication;

import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.servers.hl7.comunication.dao.Hl7ComDbDealerBean;
import it.units.htl.dpacs.servers.hl7.comunication.dao.Hl7ComDbDealerLocal;
import it.units.htl.dpacs.servers.hl7.comunication.utils.Hl7QueueConsumer;
import it.units.htl.dpacs.servers.hl7.comunication.utils.Subscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Hl7CommunicationServer implements Hl7CommunicationServerMBean {
    private HashMap<String, Hl7QueueConsumer> queueConsumers = new HashMap<String, Hl7QueueConsumer>();
    private ArrayList<Timer> executors = new ArrayList<Timer>();
    static final Log log = LogFactory.getLog(Hl7CommunicationServer.class);
    private Hl7ComDbDealerLocal bean = null;
    private boolean status = false;

    public Hl7CommunicationServer() {
        if (bean == null) {
            try {
                bean = InitialContext.doLookup(BeansName.LHl7ComDbDealer);
            } catch (NamingException e) {
                log.error("", e);
            }
        }
    }

    @Override
    public boolean startService() throws Exception, UnsupportedOperationException {
        reloadAllSettings();
        executors.clear();
        for (Hl7QueueConsumer hqc : queueConsumers.values()) {
            Timer tt = new Timer();
            tt.scheduleAtFixedRate(hqc, 15000, hqc.getPeriodInMs());
            executors.add(tt);
        }
        status = true;
        return status;
    }

    @Override
    public boolean stopService() throws Exception, UnsupportedOperationException {
        for (Timer consumer : executors) {
            consumer.cancel();
            consumer.purge();
        }
        queueConsumers.clear();
        executors.clear();
        status = false;
        return status;
    }

    @Override
    public boolean statusService() {
        return status;
    }

    @Override
    public boolean reloadSettings() throws Exception, UnsupportedOperationException {
        if (status) {
            stopService();
            startService();
        }
        return true;
    };

    public boolean reloadAllSettings() throws Exception, UnsupportedOperationException {
        Hl7ComDbDealerBean test = new Hl7ComDbDealerBean();
        ArrayList<Subscriber> sb = test.getSubscribers();
        for (Subscriber s : sb) {
            Hl7QueueConsumer consumer = new Hl7QueueConsumer(s);
            queueConsumers.put(s.name, consumer);
        }
        return true;
    }
}
