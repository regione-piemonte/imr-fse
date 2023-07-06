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
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Example;

public class PhysicalMediaHome {

	private static final Log log = LogFactory.getLog(PhysicalMediaHome.class);

	private final SessionFactory sessionFactory = SessionManager.getInstance();

	public PhysicalMedia findById(java.lang.Long id) {
		log.debug("getting PhysicalMedia instance with id: " + id);
		try {
			Session s = sessionFactory.openSession();
			s.beginTransaction();
			PhysicalMedia instance = (PhysicalMedia) s.get(PhysicalMedia.class, id);
			s.getTransaction().commit();
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(PhysicalMedia instance) {
		log.debug("finding PhysicalMedia instance by example");
		try {
			List results = sessionFactory.getCurrentSession().createCriteria(
					"PhysicalMedia").add(Example.create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
