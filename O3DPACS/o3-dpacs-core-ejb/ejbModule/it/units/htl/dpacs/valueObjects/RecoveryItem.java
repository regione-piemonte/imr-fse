/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;

import java.io.Serializable;
import java.util.Date;

public class RecoveryItem implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public long pk;
	public String objectType;
	public String eventType;
	public String currentUid;
	public String originalUid;
	public String reason;
	public String deprecatedBy;
	public Date deprecatedOn;
	public String patientId;
	
	public static final String TYPE_SERIES="SE";
	public static final String TYPE_STUDY="ST";
	
	public RecoveryItem() {}
	
	public RecoveryItem(long pk, String objectType, String eventType, String currentUid, String originalUid, String reason, String deprecatedBy, Date deprecatedOn, String patientId) {
		this.pk = pk;
		this.objectType = objectType;
		this.eventType = eventType;
		this.currentUid = currentUid;
		this.originalUid = originalUid;
		this.reason = reason;
		this.deprecatedBy = deprecatedBy;
		this.deprecatedOn = deprecatedOn;
		this.patientId=patientId;
	}
	
	
	public long getPk() {
		return pk;
	}
	public void setPk(long pk) {
		this.pk = pk;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public String getCurrentUid() {
		return currentUid;
	}
	public void setCurrentUid(String currentUid) {
		this.currentUid = currentUid;
	}
	public String getOriginalUid() {
		return originalUid;
	}
	public void setOriginalUid(String originalUid) {
		this.originalUid = originalUid;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getDeprecatedBy() {
		return deprecatedBy;
	}
	public void setDeprecatedBy(String deprecatedBy) {
		this.deprecatedBy = deprecatedBy;
	}
	public Date getDeprecatedOn() {
		return deprecatedOn;
	}
	public void setDeprecatedOn(Date deprecatedOn) {
		this.deprecatedOn = deprecatedOn;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}
	
}
