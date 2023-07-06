/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.io.Serializable;

/**
 * A class for taking care of DICOM personal name format
 * @author Mbe
 */

public class PersonalName implements HtlVo,Serializable{

	  // Private Attributes:

	private static final long serialVersionUID = -7751195202200795972L;
	
	private	String lastName=null;		
         // From DICOM; Into *.lastName
	private	String firstName=null;		
         // From DICOM; Into *.firstName
	private	String middleName=null;		
         // From DICOM; Into *.middleName
	private	String prefix=null;		
         // From DICOM; Into *.prefix
	private	String suffix=null;		
         // From DICOM; Into *.suffix

	  // Constructors:

	public PersonalName(){}		  // Default Constructor

	public PersonalName(String ln, String fn, String mn, String p, String s){
		lastName=prepareString(ln, 60);
		firstName=prepareString(fn, 60);
		middleName=prepareString(mn, 60);
		prefix=prepareString(p, 60);
		suffix=prepareString(s, 60);
	}

	public void setLastName(String ln){
		lastName=prepareString(ln, 60);
	}
	public void setFirstName(String fn){
		firstName=prepareString(fn, 60);
	}
	public void setMiddleName(String mn){
		middleName=prepareString(mn, 60);
	}
	public void setPrefix(String p){
		prefix=prepareString(p, 60);
	}
	public void setSuffix(String s){
		suffix=prepareString(s, 60);
	}
	public String getLastName(){
		return lastName;
	}
	public String getFirstName(){
		return firstName;
	}
	public String getMiddleName(){
		return middleName;
	}
	public String getPrefix(){
		return prefix;
	}
	public String getSuffix(){
		return suffix;
	}

	public String prepareString(String arg, int len){
		if (arg==null) return null;
		String temp=arg.trim();
		return ((temp.length()>len)? temp.substring(0, len) : temp);
	}
	
	public String prepareLong(String arg){return null;}
	public String prepareInt(String arg){return null;}
	public void setToPerform(char arg){}
	public char getToPerform(){return ' ';}


	public void reset(){
		lastName=null;
		firstName=null;
		middleName=null;
		prefix=null;	
		suffix=null;	
	
	}


	
	public static void main(String args[]){

	}	
     
}