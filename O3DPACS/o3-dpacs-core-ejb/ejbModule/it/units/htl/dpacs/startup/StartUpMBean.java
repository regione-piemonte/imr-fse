/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.startup;

public interface StartUpMBean {
	public abstract void init() throws Exception;

	public abstract void start() throws Exception;

	public abstract void stop() throws Exception;

	public abstract void destroy() throws Exception;
}
