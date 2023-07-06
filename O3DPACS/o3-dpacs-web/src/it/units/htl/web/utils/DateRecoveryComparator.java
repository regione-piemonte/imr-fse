/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.utils;

import it.units.htl.web.Study.RecoveryListItem;

import java.text.SimpleDateFormat;
import java.util.Comparator;

public class DateRecoveryComparator implements Comparator<RecoveryListItem> {

	public int compare(RecoveryListItem d1, RecoveryListItem d2) {
		SimpleDateFormat sdf=new SimpleDateFormat(RecoveryListItem.FORMAT_DATE);
		try {
			return sdf.parse(d1.getDeprecationTime()).compareTo(sdf.parse(d2.getDeprecationTime()));
		} catch (Exception ex) {
			return -1;
		}
	}

}

