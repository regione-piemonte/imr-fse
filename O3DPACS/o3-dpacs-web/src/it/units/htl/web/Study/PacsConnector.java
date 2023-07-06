/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.units.htl.web.Study;

import it.units.htl.dpacs.dao.DicomQueryDealerRemote;
import it.units.htl.dpacs.helpers.BeansName;

import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Cuorpo
 */
public class PacsConnector {

	private Log log = LogFactory.getLog(PacsConnector.class);

	/** Creates a new instance of PacsConnector */
	public PacsConnector() {
	}

	public DicomQueryDealerRemote getQueryDealer() {
		DicomQueryDealerRemote query = null;
		try {
			
			log.debug("=======> O3-DPACS connecting... <=======");
			query = InitialContext.doLookup(BeansName.RQueryDealer);
			if (query == null)
				log.debug("=======> O3-DPACS not connected! <=======");
			else
				log.debug("=======> O3-DPACS connected! <=======");
			return query;
		} catch (Exception ex) {
			log.error("", ex);
			return null;
		}
	}

	public void closeConnection() {

	}

}
