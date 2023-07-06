/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import it.units.htl.dpacs.core.services.PacsService;

/**
 * Compression interface for MBean
 * @author Carrara
 */
public interface CompressionSCPMBean extends PacsService{
    public String getTempDir();
    public void setTempDir(String TempDir);
    public Compression getInstance();
}
