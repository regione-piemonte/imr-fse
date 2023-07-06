/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AutoReconciliation implements Serializable {
    private static Log log = LogFactory.getLog(AutoReconciliation.class);
    private static final long serialVersionUID = 1L;
    private static Boolean autoReco = null;

    public static boolean isEnabled() {
        if (autoReco == null)
            loadSettings();
        return autoReco.booleanValue(); // In case loadSettings fails, this will throw a NullPointerException
    }

    private static synchronized void loadSettings() {
        String v = GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.AUTO_RECONCILIATION);
        if (v != null) {
            autoReco = (("1".equals(v)) || ("true".equalsIgnoreCase(v)) || ("yes".equalsIgnoreCase(v))) ? new Boolean(true) : new Boolean(false);
        } else {
            log.fatal("COULD NOT RETRIEVE AUTO_RECO SETTING");
        }
    }
}
