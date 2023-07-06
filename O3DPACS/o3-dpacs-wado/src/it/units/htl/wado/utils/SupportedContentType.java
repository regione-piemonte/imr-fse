/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.wado.utils;

import java.util.ArrayList;

public class SupportedContentType {
	private static ArrayList<String> _contentType;
	
	static{				// Static class initialization is guaranteed to be thread-safe by Java Specification (section section 12.4.2)
		_contentType = new ArrayList<String>();
		_contentType.add("application/dicom");
		_contentType.add("image/jpeg");
		_contentType.add("text/xml");
		_contentType.add("video/quicktime");
		_contentType.add("application/pdf");
		_contentType.add("video/mpeg");
		_contentType.add("video/x-flv");
		_contentType.add("byte");
//		_contentType.add("application/text");
//		_contentType.add("application/html");

	}
	
	public static boolean isSupported(String contentToVerify){
		return _contentType.contains(contentToVerify);
	}
	
}
