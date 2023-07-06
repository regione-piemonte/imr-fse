/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.core.services;

/**
 * @author sangalli
 * 
 */
public interface PacsService {
	public static final String START_SERVICE = "startService";
	public static final String GET_STATUS = "statusService";
	public static final String STOP_SERVICE = "stopService"; 
	public static final String RELOAD_SETTINGS = "reloadSettings";

	public boolean startService() throws Exception, UnsupportedOperationException;
	public boolean stopService() throws Exception, UnsupportedOperationException;
	public boolean statusService();
	public boolean reloadSettings() throws Exception, UnsupportedOperationException;
}
