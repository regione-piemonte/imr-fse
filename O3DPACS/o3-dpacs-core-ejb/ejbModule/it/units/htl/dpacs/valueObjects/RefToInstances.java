/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.util.List;		
 // MBTODO Internally use two arrays instead: more efficient in getting info
import java.util.ArrayList;

/**
 * The class takes into account the sequence of referenced istances
 * @author Mbe
 */
public class RefToInstances implements HtlVo{

	// Private Attributes:
	
	private String series=null;
	private List<OwnRecord> instances=null;		
         // A list of Records
	private int counter=0;
	
	public void setSeries(String s){
		if (s!=null) series=prepareString(s, 64);
	}
	
	public int addInstance(String instanceUid, String sopClassUid){
		if((instanceUid==null) || (sopClassUid==null)) return -1;
		if(instances==null) instances=new ArrayList<OwnRecord>();		
                 // 10 instances per series referenced by another one
		instances.add(new OwnRecord(prepareString(instanceUid, 64), prepareString(sopClassUid, 64)));
		return 1;
	}
	public String getSeries(){
		return series;
	}
	public String[] getNextInstance(){
		try{
			OwnRecord r=instances.get(counter);
			counter++;	
                         // Don't take it inside the previous line: it won't work if an Exception occurs!
				String[] s={r.instUid, r.sopClUid};
			return s;
		}catch(IndexOutOfBoundsException ioobe){
			return null;
		}
	}

	public String prepareString(String arg, int len){
		String temp=arg.trim();
		return ((temp.length()>len)? temp.substring(0, len) : temp);
	}
	
	public String prepareLong(String arg){/* No implementation needed */ return null;}	
	public String prepareInt(String arg){/* No implementation needed */ return null;}
	public void setToPerform(char arg){/* No implementation needed */}
	public char getToPerform(){/* No implementation needed */ return ' ';}

	public void reset(){
		series=null;
		instances=null;	
		counter=0;
	}
	
	  
	/**
	 * An element of the sequence
	 */
	private class OwnRecord{
		
		public String instUid;
		public String sopClUid;
		
		public OwnRecord(String i, String s){
			instUid=i;
			sopClUid=s;
		}
	}

	
	
	
}	
 // end class
