/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import ca.uhn.hl7v2.HL7Exception;


public class HL7SegmentException extends HL7Exception{
    private static final long serialVersionUID = 1L;

    public HL7SegmentException(String reason) {
        super(reason);
    }
}
