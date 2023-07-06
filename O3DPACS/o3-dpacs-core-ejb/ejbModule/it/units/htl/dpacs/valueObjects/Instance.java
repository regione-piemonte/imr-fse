/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * The default value object for an instance, it may be an image, a report, a non image etc,
 * casting to the children classes
 * @author Mbe
 */
public abstract class Instance implements HtlVo, Serializable{

	public static final String TYPE_IMAGE="I";
	public static final String TYPE_NON_IMAGE="NI";
	public static final String TYPE_PRES_STATE="PS";
	public static final String TYPE_OVERLAY="OV";
	public static final String TYPE_STRUCT_REP="SR";
	public static final String TYPE_KEY_OBJECT="KO";
	
	protected String sopInstanceUid=null;		
         // From ; Into *.sopInstanceUID 
	protected List<String> uidsToMatch=null;		
         // From Dicom; used only in matches
	protected String sopClassUid=null; 		
         // From ; Into *.sopClassUID
	protected List<String> sopsToMatch=null;
        protected String /*long*/ instanceNumber=null;		
         // From ; Into *.instanceNumber
	protected String /*char*/ deprecated="0";	
         // From ; Into *.deprecated
	
	private char toPerform=DicomConstants.INSERT;		
        
        public Instance(){}
        
	public Instance(String sopInstanceUid, String sopClassUid, char toPerform){}
        
	public void setSopInstanceUid(String siu){
		uidsToMatch=null;		
                 // Avoid setting both the UID and the list of UIDs!!
		sopInstanceUid=prepareString(siu, 64);
	}
	public void addUidToMatch(String uidtm){
		sopInstanceUid=null;
		if (uidsToMatch==null)
			uidsToMatch=new ArrayList<String>(3);	
                 //	3 ids per query should be enough!
		uidsToMatch.add(prepareString(uidtm, 64));
	}
	public void setSopClassUid(String scu){
                sopsToMatch=null;
		sopClassUid=prepareString(scu, 64);
	}
        public void addSopClassToMatch(String scuid){
		sopClassUid=null;
		if (sopsToMatch==null)
			sopsToMatch=new ArrayList<String>(3);	
                 //	3 ids per query should be enough!
		sopsToMatch.add(prepareString(scuid, 64));
        }
	public void setInstanceNumber(String inum){
		instanceNumber=prepareLong(inum);
	}
	public void setDeprecated(String d){
		deprecated=prepareString(d, 1);
	}
	public String getSopInstanceUid(){
		return sopInstanceUid;
	}
	public String[] getUidsToMatch(){
		if (uidsToMatch==null) return null;
		int s=uidsToMatch.size();
		String[] temp=new String[s];
		uidsToMatch.toArray(temp);	
                 // The array is returned in temp, 'cos it's certainly long enough!
		return temp;
	}
	public String getSopClassUid(){
		return sopClassUid;
	}
        public String[] getSopClassesToMatch(){
		if (sopsToMatch==null) return null;
		int s=sopsToMatch.size();
		String[] temp=new String[s];
		sopsToMatch.toArray(temp);	
                 // The array is returned in temp, 'cos it's certainly long enough!
		return temp;
        }
	public String getInstanceNumber(){
		return instanceNumber;
	}
	public boolean isDeprecated(){
		return ((DicomConstants.DEPRECATED).equals(deprecated));
	}

	public String prepareString(String arg, int len){
		if (arg==null) return null;
		String temp=arg.trim();
		return ((temp.length()>len)? temp.substring(0, len) : temp);
	}
	
	public String prepareLong(String arg){
		if(arg==null)return null;
                String temp=null;
		try{
			temp=(Long.valueOf(arg.trim())).toString();
		}catch(NumberFormatException e){
			temp=null;
		}
		return temp;
	}
	public String prepareInt(String arg){
		if(arg==null)return null;
                String temp=null;
		try{
			temp=(Integer.valueOf(arg.trim())).toString();
		}catch(NumberFormatException e){
			temp=null;
		}
		return temp;
	}
	public void setToPerform(char arg){
		if((arg==DicomConstants.INSERT) || (arg==DicomConstants.FIND) || (arg==DicomConstants.UPDATE)) toPerform=arg;
		else toPerform=DicomConstants.INSERT;
	}
	public char getToPerform(){
		return toPerform;
	}


	public void reset(){
		sopInstanceUid=null;	
		uidsToMatch=null;
		sopClassUid=null; 		
		instanceNumber=null;	
		deprecated=null;	
		toPerform=DicomConstants.INSERT;
	}


	public static void main(String[] args){

	}	
}