/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import it.units.htl.dpacs.servers.HL7Server;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

public class ADTA37 extends HL7Dealer {
    public ADTA37(HL7Server hl7) {
        super(hl7);
    }

    @Override
    protected void parse(Message hm) throws HL7Exception {
    }

    @Override
    protected void run() throws Exception {
        
    }
}
