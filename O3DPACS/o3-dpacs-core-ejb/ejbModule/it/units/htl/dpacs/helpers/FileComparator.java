/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.File;
import java.util.Comparator;

public final class FileComparator implements Comparator<File> {

	public int compare(File file1, File file2) {
		if((file1 == null)||(file2==null))
			throw new NullPointerException();
		return file1.getName().compareTo(file2.getName());
	}
	
}
