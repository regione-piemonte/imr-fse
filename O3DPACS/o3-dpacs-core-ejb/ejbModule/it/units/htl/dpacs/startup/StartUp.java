/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.startup;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class StartUp implements StartUpMBean, MBeanRegistration {

	public void destroy() throws Exception {
	}

	public void init() throws Exception {
	}

	public void start() throws Exception {
		O3Dpacs o3d = new O3Dpacs();
		o3d.startServer();
	}

	public void stop() throws Exception {
	}

	public void postDeregister() {
	}

	public void postRegister(Boolean arg0) {
	}

	public void preDeregister() throws Exception {
	}

	public ObjectName preRegister(MBeanServer arg0, ObjectName arg1)
			throws Exception {
		return new ObjectName(":service=StartUp");
	}

}
