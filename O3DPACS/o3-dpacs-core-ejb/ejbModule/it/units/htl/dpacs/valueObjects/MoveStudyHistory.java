/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.valueObjects;



import java.util.Date;

/**
 * MoveStudyHistory 
 */
public class MoveStudyHistory implements java.io.Serializable {


	private static final long serialVersionUID = 1L;
	private Long id;
	private String calledAet;
	private String callingAet;
	private String moveAet;
	private String accessionnumber;
	private Date eventTime;
	private Date startMov;
	private Date endMov;
	private Byte idRetry;
	private String ris;
	private String messageId;
	private String errorMessage;
	private Long knownnodefk;
	//taskid:300060 bug:34065
	private String patientID;
	//taskid:326054 bug:37966
	private String idIssuer;

	public MoveStudyHistory() {
	}

	public MoveStudyHistory(Long id, String calledAet, String callingAet,
			String moveAet, Long knownnodefk, String patientID) {
		this.id = id;
		this.calledAet = calledAet;
		this.callingAet = callingAet;
		this.moveAet = moveAet;
		this.knownnodefk = knownnodefk;
		this.patientID = patientID;
	}

	public MoveStudyHistory(Long id, String calledAet, String callingAet,
			String moveAet, String accessionnumber, Date eventTime,
			Date startMov, Date endMov, Byte idRetry,
			String ris, String messageId, String errorMessage,
			Long knownnodefk, String patientID) {
		this.id = id;
		this.calledAet = calledAet;
		this.callingAet = callingAet;
		this.moveAet = moveAet;
		this.accessionnumber = accessionnumber;
		this.eventTime = eventTime;
		this.startMov = startMov;
		this.endMov = endMov;
		this.idRetry = idRetry;
		this.ris = ris;
		this.messageId = messageId;
		this.errorMessage = errorMessage;
		this.knownnodefk = knownnodefk;
		this.patientID = patientID;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCalledAet() {
		return this.calledAet;
	}

	public void setCalledAet(String calledAet) {
		this.calledAet = calledAet;
	}

	public String getCallingAet() {
		return this.callingAet;
	}

	public void setCallingAet(String callingAet) {
		this.callingAet = callingAet;
	}

	public String getMoveAet() {
		return this.moveAet;
	}

	public void setMoveAet(String moveAet) {
		this.moveAet = moveAet;
	}

	public String getAccessionnumber() {
		return this.accessionnumber;
	}

	public void setAccessionnumber(String accessionnumber) {
		this.accessionnumber = accessionnumber;
	}

	public Date getEventTime() {
		return this.eventTime;
	}

	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}

	public Date getStartMov() {
		return this.startMov;
	}

	public void setStartMov(Date startMov) {
		this.startMov = startMov;
	}

	public Date getEndMov() {
		return this.endMov;
	}

	public void setEndMov(Date endMov) {
		this.endMov = endMov;
	}

	public Byte getIdRetry() {
		return this.idRetry;
	}

	public void setIdRetry(Byte idRetry) {
		this.idRetry = idRetry;
	}

	public String getRis() {
		return this.ris;
	}

	public void setRis(String ris) {
		this.ris = ris;
	}

	public String getMessageId() {
		return this.messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Long getKnownnodefk() {
		return this.knownnodefk;
	}

	public void setKnownnodefk(Long knownnodefk) {
		this.knownnodefk = knownnodefk;
	}

	public String getPatientID() {
		return patientID;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public String getIdIssuer() {
		return idIssuer;
	}

	public void setIdIssuer(String idIssuer) {
		this.idIssuer = idIssuer;
	}

	@Override
	public String toString() {
		return "MoveStudyHistory [id=" + id + ", calledAet=" + calledAet + ", callingAet=" + callingAet + ", moveAet="
				+ moveAet + ", accessionnumber=" + accessionnumber + ", eventTime=" + eventTime + ", startMov="
				+ startMov + ", endMov=" + endMov + ", idRetry=" + idRetry + ", ris=" + ris + ", messageId=" + messageId
				+ ", errorMessage=" + errorMessage + ", knownnodefk=" + knownnodefk + ", patientID=" + patientID
				+ ", idIssuer=" + idIssuer + ", getId()=" + getId() + ", getCalledAet()=" + getCalledAet()
				+ ", getCallingAet()=" + getCallingAet() + ", getMoveAet()=" + getMoveAet() + ", getAccessionnumber()="
				+ getAccessionnumber() + ", getEventTime()=" + getEventTime() + ", getStartMov()=" + getStartMov()
				+ ", getEndMov()=" + getEndMov() + ", getIdRetry()=" + getIdRetry() + ", getRis()=" + getRis()
				+ ", getMessageId()=" + getMessageId() + ", getErrorMessage()=" + getErrorMessage()
				+ ", getKnownnodefk()=" + getKnownnodefk() + ", getPatientID()=" + getPatientID() + ", getIdIssuer()="
				+ getIdIssuer() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
}
