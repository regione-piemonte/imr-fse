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
 * A class keeping all the information of a Series
 * @author Mbe
 */
public class Series implements HtlVo, Serializable{
    
    private String seriesInstanceUid=null;
     // From ; Into Series.seriesInstanceUID
    private List<String> uidsToMatch=null;
     // From Dicom; used only in matches
    private String /*long*/ seriesNumber=null;
     // From ; Into Series.seriesNumber
    private	String modality=null;
     // From ; Into Series.modality
    private	String bodyPartExamined=null;
     // From ; Into Series.bodyPartExamined
    private	String /*char*/ seriesStatus=null;
     // From DPACS; Into Series.seriesStatus 2Keep?
    private Equipment equipment=null; 
     // From Series.equipmentFK; used just in Find
    private String AETitle=null;
     // Used just to relate the series' equipment to the right machine!
    private String /*int*/ numberOfSeriesRelatedInstances=null;
     // From ; Into Series.numberOfSeriesRelatedInstances
    private String seriesDescription=null;
    
    private String operatorsName=null;
    private char toPerform=DicomConstants.INSERT;
    // ---
    
     // Constructors:
    
    public Series(){}
     // Default Constructor
    public Series(String uId){
        setSeriesInstanceUid(uId);
    }
    
    public Series(String uid, boolean truncate){
		if(truncate)
			setSeriesInstanceUid(uid);
		else
			this.seriesInstanceUid=uid;
	}
    
    public Series(String uId, char toPerform){}
     // Basic Constructor
    
     // Accessor Methods:
    
    public void setSeriesInstanceUid(String sid){
        uidsToMatch=null;
         // Avoid setting both the UID and the list of UIDs!!
        seriesInstanceUid=prepareString(sid, 64);
    }
    public void addUidToMatch(String uidtm){
        seriesInstanceUid=null;
        if (uidsToMatch==null)
            uidsToMatch=new ArrayList<String>(3);
         //	3 ids per query should be enough!
        uidsToMatch.add(prepareString(uidtm, 64));
    }
    public void setSeriesNumber(String sn){
        seriesNumber=prepareLong(sn);
    }
    public void setModality(String m){
        modality=prepareString(m, 16);
    }
    public void setBodyPartExamined(String bpe){
        bodyPartExamined=prepareString(bpe, 16);
    }
    public void setSeriesStatus(String ss){
        seriesStatus=prepareString(ss, 1);
    }
    public void setNumberOfSeriesRelatedInstances(String nosri){
        // ---
        numberOfSeriesRelatedInstances=prepareInt(nosri);
    }
    public void setAETitle(String aet){
        AETitle=prepareString(aet, 64);
    }
    public String getSeriesInstanceUid(){
        return seriesInstanceUid;
    }
    public String[] getUidsToMatch(){
        if (uidsToMatch==null) return null;
        int s=uidsToMatch.size();
        String[] temp=new String[s];
        uidsToMatch.toArray(temp);
         // The array is returned in temp, 'cos it's certainly long enough!
        return temp;
    }
    public String getSeriesNumber(){
        return seriesNumber;
    }
    public String getModality(){
        return modality;
    }
    public String getBodyPartExamined(){
        return bodyPartExamined;
    }
    public String getSeriesStatus(){
        return seriesStatus;
    }
    public String getNumberOfSeriesRelatedInstances(){
        return numberOfSeriesRelatedInstances;
    }
    
    public void setSeriesDescription(String descr){
        seriesDescription=descr;}
    
    public String getSeriesDescription(){
        return seriesDescription;}
    
    
    public String getAETitle(){
        return AETitle;
    }
    
   // ---
    
    public void setEquipment(Equipment e){
        equipment=e;
    }
    public Equipment getEquipment(){
        return equipment;
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
        seriesInstanceUid=null;
        uidsToMatch=null;
        seriesNumber=null;
        modality=null;
        bodyPartExamined=null;
        seriesStatus=null;
        equipment=null;
        numberOfSeriesRelatedInstances=null;
        toPerform=DicomConstants.INSERT;
    }
    public String getOperatorsName() {
        return operatorsName;
    }
    public void setOperatorsName(String operatorsName) {
        this.operatorsName = operatorsName;
    }
    
   }
 
