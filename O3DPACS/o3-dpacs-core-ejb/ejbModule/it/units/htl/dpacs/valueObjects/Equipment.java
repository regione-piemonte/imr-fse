/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;

/**
 * Defines an equipment with desciption. Such as workstation, CT etc....
 * @author Mbe
 */
public class Equipment implements HtlVo, Serializable{

	private String equipmentType=null;		
     	private String manufacturer=null; 		
     	private	String institutionName=null;		
     	private	String stationName=null;		
     	private	String institutionalDepartmentName=null;
     	private String manufacturersModelName=null;	
        private String deviceSerialNumber=null;		
        private Date dateOfLastCalibration=null;	
        private Date dateOfLastCalibrationLate=null;
        private Time timeOfLastCalibration=null;	
        private Time timeOfLastCalibrationLate=null;	
        private String lastCalibratedBy=null;		
        private String conversionType=null;		
        private String secondaryCaptureDeviceId=null;
        private char toPerform;
         
        public void setEquipmentType(String et){
		equipmentType=prepareString(et, 16);
	}
	public void setManufacturer(String m){
		manufacturer=prepareString(m, 64);
	}
	public void setInstitutionName(String insn){
		institutionName=prepareString(insn, 64);
	}
	public void setStationName(String sn){
		stationName=prepareString(sn, 16);
	}
	public void setInstitutionalDepartmentName(String idn){
		institutionalDepartmentName=prepareString(idn, 64);
	}
	public void setManufacturersModelName(String mmn){
		manufacturersModelName=prepareString(mmn, 64);
	}
	public void setDeviceSerialNumber(String dsn){
		deviceSerialNumber=prepareString(dsn, 64);
	}
	public void setDateOfLastCalibration(Date dolc){
		dateOfLastCalibration=dolc;
	}
	public void setDateOfLastCalibrationRange(Date dolcEarly, Date dolcLate){
		dateOfLastCalibration=dolcEarly;
		dateOfLastCalibrationLate=dolcLate;
	}
	public void setTimeOfLastCalibration(Time tolc){
		timeOfLastCalibration=tolc;
	}
	public void setTimeOfLastCalibrationRange(Time tolcEarly, Time tolcLate){
		timeOfLastCalibration=tolcEarly;
		timeOfLastCalibrationLate=tolcLate;
	}
	public void setLastCalibratedBy(String lcb){
		lastCalibratedBy=prepareString(lcb, 64);
	}
	public void setConversionType(String ct){
		conversionType=prepareString(ct, 16);
	}
	public void setSecondaryCaptureDeviceId(String scdi){
		secondaryCaptureDeviceId=prepareString(scdi, 64);
	}
	public String getEquipmentType(){
		return equipmentType;
	}
	public String getManufacturer(){
		return manufacturer;
	}
	public String getInstitutionName(){
		return institutionName;
	}
	public String getStationName(){
		return stationName;
	}
	public String getInstitutionalDepartmentName(){
		return institutionalDepartmentName;
	}
	public String getManufacturersModelName(){
		return manufacturersModelName;
	}
	public String getDeviceSerialNumber(){
		return deviceSerialNumber;
	}
	public Date getDateOfLastCalibration(){
		return dateOfLastCalibration;
	}

	public String getDateOfLastCalibration(DateFormat df){
		if (dateOfLastCalibration==null) return null;
		return ((df==null)? Long.toString(dateOfLastCalibration.getTime()) : df.format(dateOfLastCalibration)); 
	}
	public Date getDateOfLastCalibrationLate(){
		return dateOfLastCalibrationLate;
	}
	
	public String getDateOfLastCalibrationLate(DateFormat df){
		if (dateOfLastCalibrationLate==null) return null;
		return ((df==null)? Long.toString(dateOfLastCalibrationLate.getTime()) : df.format(dateOfLastCalibrationLate)); 
	}
	public Time getTimeOfLastCalibration(){
		return timeOfLastCalibration;
	}
	
	public String getTimeOfLastCalibration(DateFormat tf){
		if (timeOfLastCalibration==null) return null;
		return ((tf==null)? Long.toString(timeOfLastCalibration.getTime()) : tf.format(timeOfLastCalibration)); 
	}
	public Time getTimeOfLastCalibrationLate(){
		return timeOfLastCalibrationLate;
	}
	public String getTimeOfLastCalibrationLate(DateFormat tf){
		if (timeOfLastCalibrationLate==null) return null;
		return ((tf==null)? Long.toString(timeOfLastCalibrationLate.getTime()) : tf.format(timeOfLastCalibrationLate)); 
	}
	public String getLastCalibratedBy(){
		return lastCalibratedBy;
	}
	public String getConversionType(){
		return conversionType;
	}
	public String getSecondaryCaptureDeviceId(){
		return secondaryCaptureDeviceId;
	}

	public String prepareString(String arg, int len){
		if (arg==null) return null;
		String temp=arg.trim();
		return ((temp.length()>len)? temp.substring(0, len) : temp);
	}
	
	public String prepareLong(String arg){/* No implementation needed */ return null;}
	public String prepareInt(String arg){/* No implementation needed */ return null;}
	public void setToPerform(char arg){
		if((arg==DicomConstants.INSERT) || (arg==DicomConstants.FIND) || (arg==DicomConstants.UPDATE)) toPerform=arg;
		else toPerform=DicomConstants.INSERT;
	}
	public char getToPerform(){
		return toPerform;
	}

	public void reset(){
		equipmentType=null;		
		manufacturer=null; 		
		institutionName=null;	
		stationName=null;		
		institutionalDepartmentName=null;
		manufacturersModelName=null;	
		deviceSerialNumber=null;		
		dateOfLastCalibration=null;		
		dateOfLastCalibrationLate=null;	
		timeOfLastCalibration=null;		
		timeOfLastCalibrationLate=null;
		lastCalibratedBy=null;		
		conversionType=null;		
		secondaryCaptureDeviceId=null;	
		toPerform=DicomConstants.INSERT;
	}

	public static void main(String[] args){

	}	
      
}	

