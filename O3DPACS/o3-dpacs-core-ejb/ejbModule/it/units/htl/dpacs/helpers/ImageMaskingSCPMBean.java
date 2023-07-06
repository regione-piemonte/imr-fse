/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import it.units.htl.dpacs.core.services.PacsService;

import javax.management.ObjectName;

/**
 * 
 * @author Carrara
 */
public interface ImageMaskingSCPMBean extends PacsService{

	public ObjectName getDcmServerName();

	public void setDcmServerName(ObjectName dcmServerName);

}
