/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

public class Image extends Instance{

	 // Private Attributes:
	private String /*int*/ samplesPerPixel=null;		 
        // From ; Into Images.samplesPerPixel
	private String /*int*/ rows=null;				
        // From ; Into Images.rows
	private String /*int*/ columns=null;				
        // From ; Into Images.columns
	private String /*int*/ bitsAllocated=null;		
        // From ; Into Images.bitsAllocated
	private String /*int*/ bitsStored=null;			
        // From ; Into Images.bitsStored
	private String /*int*/ highBit=null;			
        // From ; Into Images.highBit
	private String /*int*/ pixelRepresentation=null;	
        // From ; Into Images.pixelRepresentation
	protected Integer numberOfFrames;
	
        public Image(){}
	public Image(String siu){
		sopInstanceUid=prepareString(siu, 64);
	}	

	public void setSamplesPerPixel(String spp){
		samplesPerPixel=prepareInt(spp);
	}
	public void setRows(String r){
		rows=prepareInt(r);
	}
	public void setColumns(String c){
		columns=prepareInt(c);
	}
	public void setBitsAllocated(String ba){
		bitsAllocated=prepareInt(ba);
	}
	public void setBitsStored(String bs){
		bitsStored=prepareInt(bs);
	}
	public void setHighBit(String hb){
		highBit=prepareInt(hb);
	}
	public void setPixelRepresentation(String pr){
		pixelRepresentation=prepareInt(pr);
	}
	public String getSamplesPerPixel(){
		return samplesPerPixel;
	}
	public String getRows(){
		return rows;
	}
	public String getColumns(){
		return columns;
	}
	public String getBitsAllocated(){
		return bitsAllocated;
	}
	public String getBitsStored(){
		return bitsStored;
	}
	public String getHighBit(){
		return highBit;
	}
	public String getPixelRepresentation(){
		return pixelRepresentation;
	}
	public Integer getNumberOfFrames() {
		return numberOfFrames;
	}
	public void setNumberOfFrames(Integer numberOfFrames) {
		this.numberOfFrames = numberOfFrames;
	}

	public void reset(){
		samplesPerPixel=null;
		rows=null;				
		columns=null;			
		bitsAllocated=null;	
		bitsStored=null;		
		highBit=null;			
		pixelRepresentation=null;
	}
	
}	
 
