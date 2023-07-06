/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;

import it.units.htl.dpacs.valueObjects.RecoveryItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class RecoveryListItem {
    
    public static final String FORMAT_DATE = "dd-MM-yyyy HH:mm:ss";
	private String pk;
    private String user;
    private String patientId;
    private String objectType;
    private String reason;
    private Date deprecationTime;
    private String originalUid;
    private String currentUid;

    
    public RecoveryListItem(){}
    public RecoveryListItem(RecoveryItem ri){
    	setPk(ri.getPk());
    	setUser(ri.getDeprecatedBy());
    	setObjectType(ri.getObjectType());
    	setReason(ri.getReason());
    	setCurrentUid(ri.getCurrentUid());
    	setOriginalUid(ri.getOriginalUid());
    	setPatientId(ri.getPatientId());
    	setDeprecationTime(ri.getDeprecatedOn());
    }
            
    public String getPk() {
        return pk;
    }
    
    public void setPk(long _pk) {
        this.pk = String.valueOf(_pk);
    }
    
    public String getUser() {
        return user;
    }
    
    public void setUser(String _user) {
        this.user = _user;
    }
    
    public String getObjectType() {
        return objectType;
    }
        
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getDeprecationTime() {
    	String time=null;
        if(this.deprecationTime!=null){
        	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE);
        	time = sdf.format(this.deprecationTime);
        }
        return time;
    }
    
    
    public void setDeprecationTime(Date deprecationTime) {
        this.deprecationTime=deprecationTime;
    }
        
    public String getOriginalUid() {
        return originalUid;
    }
    
    public void setOriginalUid(String originalUid) {
        this.originalUid = originalUid;
    }
    
	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}
	public String getPatientId() {
		return patientId;
	}
	public String getCurrentUid() {
		return currentUid;
	}
	public void setCurrentUid(String currentUid) {
		this.currentUid = currentUid;
	}
    
}

