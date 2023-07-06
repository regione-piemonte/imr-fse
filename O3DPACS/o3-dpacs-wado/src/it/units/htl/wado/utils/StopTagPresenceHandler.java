/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.wado.utils;

import java.io.IOException;

import org.dcm4che2.io.DicomInputHandler;
import org.dcm4che2.io.DicomInputStream;

public class StopTagPresenceHandler implements DicomInputHandler {
	private final long stopTag;
	private boolean stopTagRead;

	public StopTagPresenceHandler(int stopTag) {
		this.stopTag = stopTag & 0xffffffffL;		
	}
	
	public boolean readValue(DicomInputStream in) throws IOException {
		
		if((in.tag() & 0xffffffffL) == stopTag)
			stopTagRead=true;
		if ((in.tag() & 0xffffffffL) >= stopTag 
				&& in.level() == 0)
			return false;
		return in.readValue(in);
	}

	public boolean isStopTagRead() {
		return stopTagRead;
	}

}
