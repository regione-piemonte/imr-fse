/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

/**
 * An interface for common ValueObject methods
 * @author Mbe
 */

public interface HtlVo{

	public String prepareString(String arg, int len);
	public String prepareLong(String arg);
	public String prepareInt(String arg);
	public void setToPerform(char arg);
	public char getToPerform();
	public void reset();	
         // It should just set every private attribute to their initial state

}	
