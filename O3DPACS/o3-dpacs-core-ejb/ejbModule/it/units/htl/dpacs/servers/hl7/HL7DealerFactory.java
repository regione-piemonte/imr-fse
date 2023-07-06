/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.servers.hl7;

import it.units.htl.dpacs.servers.HL7Server;

public class HL7DealerFactory {
	
	public static enum SupportedHL7 {
		
		ORMO01("ORM^O01"),
		ADTA02("ADT^A02"),
		ADTA03("ADT^A03"),
		ADTA06("ADT^A06"),
		ADTA07("ADT^A07"),
		ADTA08("ADT^A08"),
		ADTA12("ADT^A12"),
		ADTA13("ADT^A13"),
		ADTA28("ADT^A28"),
		ADTA31("ADT^A31"),
		ADTA37("ADT^A37"),
		ADTA40("ADT^A40"),
		ADTA45("ADT^A45"),
		MDMT02("MDM^T02"),
		MDMT10("MDM^T10");
		
		private final String type;
		
		SupportedHL7(String type){
			this.type=type;
		}
		
		public String type(){return this.type;}
		
	};
	
	
	public static HL7Dealer getInstance(SupportedHL7 s, HL7Server hl7){
		switch(s){
			case ORMO01:
				return new ORMO01(hl7);
			case ADTA02:
				return new ADTA02(hl7);
			case ADTA03:
				return new ADTA03(hl7);
			case ADTA06:
				return new ADTA06(hl7);
			case ADTA07:
				return new ADTA07(hl7);
			case ADTA08:
				return new ADTA08(hl7);
			case ADTA12:
				return new ADTA12(hl7);
			case ADTA13:
				return new ADTA13(hl7);
			case ADTA28:
                return new ADTA28(hl7);
			case ADTA31:
				return new ADTA31(hl7);
			case ADTA37:
                return new ADTA37(hl7);
			case ADTA40:
				return new ADTA40(hl7);
			case ADTA45:
				return new ADTA45(hl7);
			case MDMT02:
				return new MDMT02(hl7);
			case MDMT10:
				return new MDMT10(hl7);
			default:
				throw new UnsupportedOperationException("Requested Unsupported HL7Dealer: "+s.type());
		}
		
	}

}
