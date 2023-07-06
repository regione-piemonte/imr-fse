/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

/**
 * Should be an unused Value Object for Worklist Entries....
 * @author Mbe
 */
public class WLItem implements HtlVo{
    
    public char getToPerform() {
		return ' ';
    }
    
    public String prepareInt(String arg) {
		return null;
	}
    
    public String prepareLong(String arg) {
		return null;
	}
    
    public String prepareString(String arg, int len) {
		return null;
	}
    
    public void reset() {
    }
    
    public void setToPerform(char arg) {
    }
    
}