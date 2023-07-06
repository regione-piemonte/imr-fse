/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class Users.
 * @see it.units.htl.maps.Users
 * @author Hibernate Tools
 */
public class UsersHome {

	private static final Log log = LogFactory.getLog(UsersHome.class);


	public List findByExample(Users instance, Session s) {
		log.debug("finding Users instance by example");
		try {
			List results = s.createCriteria("it.units.htl.maps.Users").add(Example.create(instance).excludeZeroes()).list();
			log.debug("find by example successful, result size: " + results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
