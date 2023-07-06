/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.forwarder;

import java.io.IOException;
import org.dcm4che.net.*;

/*__*__*////**
/*__*__*/// * Mantiene informazioni persistenti utili per l'invio di piu' comandi nel corso di una stessa associazione.
/*__*__*/// * Necessario per la realizzazione di servizi di tipo N.
/*__*__*/// * Attualmente conserva un'associazione (solo se e' effettivamente aperta),
/*__*__*/// * mantiene eventualmente un oggetto DcmSubOpProgress,
/*__*__*/// * fornisce un numero progressivo per un eventuale prossimo comando, 
/*__*__*/// * termina correttamente l'associazione su richiesta.
/*__*__*/// *
/*__*__*/// */
class DcmCommunication {

	private Association assoc = null;
	private DcmSubOpProgress progress = null;
	private int releaseTimeout = 4000;

	/* ____ */// /** Creates a new instance of DcmCommunication. If requested,
				// mantains an internal instance of class DcmSubOpProgress. */
	DcmCommunication(Association connectedAssociation, boolean generateProgress) {
		if (connectedAssociation.getState() == Association.ASSOCIATION_ESTABLISHED) {
			assoc = connectedAssociation;
		} else {
			throw new IllegalArgumentException("Association is not established!");
		}
		if (generateProgress)
			progress = new DcmSubOpProgress();
	}

	Association getAssociation() {
		return assoc;
	}

	String releaseAssociation() throws IOException {
		return assoc.release(releaseTimeout).toString(true);
	}

	int nextMessageID() {
		return assoc.nextMsgID();
	}

	void setReleaseTimeout(int timeout) {
		releaseTimeout = timeout;
	}

	DcmSubOpProgress getSubOpsProgress() {
		return this.progress;
	}

}