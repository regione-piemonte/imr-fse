/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7.comunication.dao;

import it.units.htl.dpacs.servers.hl7.comunication.utils.Hl7Message;
import it.units.htl.dpacs.servers.hl7.comunication.utils.Subscriber;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.ejb.Local;


@Local
public interface Hl7ComDbDealerLocal {
    public ArrayList<Subscriber> getSubscribers();
    public ArrayList<Hl7Message> getMessageForSubscriber(long pk) throws SQLException;
    public void updateRetries(Hl7Message sms, long nodePk);
    public void setContext(String name);
    public void removeContext();
}
