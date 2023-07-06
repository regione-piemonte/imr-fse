/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.storage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

public class StoragePatches {

    private Log log = LogFactory.getLog("StoragePatches");
    public StoragePatches() {
    }

    public Dataset applyPatches(Dataset s) {
	// Cattinara Patches///////////////////////////////////////////////////////
	log.debug("Applying NON-IHE specific patches for Cattinara Hospital - TS");
	log.debug("now just displaying" + s.getString(Tags.SOPInstanceUID));
	// ////////////////////////////////////////////////////////////////////////
	return s;
    }
}
