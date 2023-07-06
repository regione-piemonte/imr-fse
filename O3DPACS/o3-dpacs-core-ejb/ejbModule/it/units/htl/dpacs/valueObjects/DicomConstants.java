/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

/**
 * Constants used in O3-DPACS
 * @author Mbe
 */
public class DicomConstants{

        public static final String JOHN_DOE                     = "NN";
    	public static final String DEPRECATED			    = "1";
        // For Matching:
	public static final String UNIVERSAL_MATCHING		 = "";	 
        // Used when a tag is empty-> Dicom universal matching
	public static final String MULTIPLE_WC_MATCHING		 = "*";	
        // To be replaced in DAO by % TODO: mind character sets!
	public static final String CHARACTER_WC_MATCHING	 = "?";	
        // TODO: mind character sets!
	public static final String FIND_NUMBER_OF_RELATED	 = "-1";
        // To state that an optional field is to be returned when matching!
	public static final long NO_DATETIME_LOWER_LIMIT	 = 0;
	public static final long NO_DATETIME_UPPER_LIMIT	 = Long.MAX_VALUE;
         // To use in DAOs:
        public static final char FIRST_LEVEL_STORAGE='f';
	public static final char SECOND_LEVEL_STORAGE='s';
	public static final char THIRD_LEVEL_STORAGE='t';
	public static final char RELOADED_LEVEL_STORAGE='r';
	public static final char EDUCATIONAL_LEVEL_STORAGE='e';
	public static final char TEMP_LEVEL_STORAGE='x';
	public static final String MOBILE="t";
         // As Commands:
	public static final char INSERT			 = 'i';
	public static final char UPDATE			 = 'u';
	public static final char FIND			 = 'f';
	public static final char ALREADY_PRESENT = 'p';

	public static final int QUERY=0;
	public static final int RETRIEVE=1;
	public static final int STORE=2;
	public static final int MPPS=3;
}	
