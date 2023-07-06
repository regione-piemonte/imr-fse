/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.ui;

import it.units.htl.maps.Patients;
import it.units.htl.maps.Series;
import it.units.htl.maps.Studies;

public class MergerPanel {
	private Studies srcStudy = null;
	private Series srcSerie = null;
	private Patients dstPatients = null;	
	private Studies dstStudy = null;
	
	
	public Studies getSrcStudy() {
		return srcStudy;
	}
	public void setSrcStudy(Studies srcStudy) {
		this.srcStudy = srcStudy;
	}
	
	public Series getSrcSerie() {
		return srcSerie;
	}
	public void setSrcSerie(Series srcSerie) {
		this.srcSerie = srcSerie;
	}
	public Patients getDstPatients() {
		return dstPatients;
	}
	public void setDstPatients(Patients dstPatients) {
		this.dstPatients = dstPatients;
	}
	public Studies getDstStudy() {
		return dstStudy;
	}
	public void setDstStudy(Studies dstStudy) {
		this.dstStudy = dstStudy;
	}
	
	public void destroyMergerPanel(){
		srcStudy = null;
		srcSerie = null;
		dstPatients = null;	
		dstStudy = null;
	}

}
