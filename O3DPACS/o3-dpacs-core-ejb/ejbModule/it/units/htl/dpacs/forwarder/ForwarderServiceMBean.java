/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.forwarder;

import it.units.htl.dpacs.core.services.PacsService;


public interface ForwarderServiceMBean  extends PacsService{
	public boolean startService();
	public boolean stopService();
}