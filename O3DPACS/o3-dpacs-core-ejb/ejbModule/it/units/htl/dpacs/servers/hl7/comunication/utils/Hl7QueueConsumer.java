/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7.comunication.utils;

import it.units.htl.dpacs.helpers.BeansName;
import it.units.htl.dpacs.helpers.MailerSystem;
import it.units.htl.dpacs.servers.hl7.comunication.dao.Hl7ComDbDealerLocal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TimerTask;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.jboss.logging.MDC;
import org.apache.log4j.MDC;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;

public class Hl7QueueConsumer extends TimerTask {
    private Subscriber _destination = null;
    private Hl7ComDbDealerLocal bean = null;
    private final Log log = LogFactory.getLog(Hl7QueueConsumer.class);

    public Hl7QueueConsumer(Subscriber destination) {
        _destination = destination;
    }

    @Override
    public void run() {
        Hl7Message currentSMS = null;
        try {
            initBean();
            if (bean != null) {
                bean.setContext(_destination.name);
            }
            MDC.put("queueConsumer", _destination.name);
            log.info("Reading message for " + _destination.pk);
            ArrayList<Hl7Message> toSend = new ArrayList<Hl7Message>();
            try {
                toSend = bean.getMessageForSubscriber(_destination.pk);
            } catch (SQLException sex) {
                log.error("Unable to get messages for pk " + _destination.pk, sex);
            }
            try {
                if (!toSend.isEmpty()) {
                    HapiContext context = new DefaultHapiContext();
                    Parser p = context.getPipeParser();
                    // A connection object represents a socket attached to an HL7 server
                    Connection connection = context.newClient(_destination.getIp(), _destination.getPort(), false);
                    // The initiator is used to transmit unsolicited messages
                    Initiator initiator = connection.getInitiator();
                    for (Hl7Message sms : toSend) {
                        try {
                            if (sms.retries < Integer.parseInt(_destination.getConfigurationValue(Subscriber.ConfigurationProperty.numberOfRetries.name()))) {
                                currentSMS = sms;
                                Message toSent = p.parse(sms.message);
                                Terser overwriteTerser = new Terser(toSent);
                                overwriteTerser.set("/MSH-3-1", _destination.getConfigurationValue(Subscriber.ConfigurationProperty.sendigApp.name()));
                                overwriteTerser.set("/MSH-5-1", _destination.getConfigurationValue(Subscriber.ConfigurationProperty.receivingApp.name()));
                                if ("false".equals(_destination.getConfigurationValue(Subscriber.ConfigurationProperty.zds.name()))) {
                                    overwriteTerser.set("/ORDER/ORDER_DETAIL/" + TerserLocations.ZDS.studyUID, null);
                                    overwriteTerser.set("/ORDER/ORDER_DETAIL/" + TerserLocations.ZDS.numberOfStudyRelInst, null);
                                    overwriteTerser.set("/ORDER/ORDER_DETAIL/" + TerserLocations.ZDS.application, null);
                                    overwriteTerser.set("/ORDER/ORDER_DETAIL/" + TerserLocations.ZDS.type, null);
                                    overwriteTerser.set("/ORDER/ORDER_DETAIL/" + TerserLocations.ZDS.studyDate, null);
                                }
                                Message response = initiator.sendAndReceive(toSent);
                                Terser terser = new Terser(response);
                                String result = terser.get(TerserLocations.MSA.status);
                                if ("AA".equals(result)) {
                                    log.info("messagePK: " + currentSMS.pk + " ACKed.");
                                    currentSMS.retries = null;
                                    bean.updateRetries(currentSMS, _destination.pk);
                                } else {
                                    String message = terser.get(TerserLocations.ERR.errorMessageCode) + " " + terser.get(TerserLocations.ERR.errorDesc);
                                    log.fatal("Response with a NACK: " + message);
                                    throw new HL7Exception("The server response with an NACK: " + message);
                                }
                            } else {
                                log.fatal("Max number of retries exeeded!");
                                String receipts = _destination.getConfigurationValue(Subscriber.ConfigurationProperty.email.name());
                                MailerSystem.send("MaxNumOfRetries", "it", (receipts != null) ? receipts.split(";") : null, new String[] { sms.message, _destination.address });
                                sms.retries = 1;
                                bean.updateRetries(sms, _destination.pk);
                                break;
                            }
                        } catch (Exception hex) {
                            log.error("Unable to send message to pk " + _destination.pk, hex);
                            if (currentSMS.retries == null) {
                                currentSMS.retries = 1;
                            } else {
                                currentSMS.retries = currentSMS.retries + 1;
                            }
                            bean.updateRetries(currentSMS, _destination.pk);
                            connection.close();
                            break;
                        }
                    }
                    connection.close();
                }
            } catch (HL7Exception ex) {
                log.error("", ex);
            }
        } catch (Exception ex) {
            log.error("Error!!!", ex);
        } finally {
            if (bean != null)
                bean.removeContext();
        }
    }

    public Long getPeriodInMs() throws Exception {
        return Long.parseLong(_destination.getConfigurationValue(Subscriber.ConfigurationProperty.periodInMillis.name()));
    }

    private void initBean() {
        try {
            bean = InitialContext.doLookup(BeansName.LHl7ComDbDealer);
        } catch (NamingException e) {
            log.error("", e);
        }
    }
}
