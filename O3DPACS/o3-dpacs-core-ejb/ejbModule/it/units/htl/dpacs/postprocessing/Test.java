/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing;

import it.units.htl.dpacs.postprocessing.multiframe.DcmFramesToMF;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DcmFramesToMF a = new DcmFramesToMF("Modality", "O3-PACS", "1.2.840.113745.101000.1008000.38181.5599.6331002");
		String b = a.processFrames();
		
		System.out.println(b);
				
		return;
		

	}

}
