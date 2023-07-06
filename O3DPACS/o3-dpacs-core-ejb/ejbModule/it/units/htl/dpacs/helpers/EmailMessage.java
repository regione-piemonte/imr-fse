/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.helpers;

import java.io.Serializable;

public class EmailMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String language;
	private String subject;
	private String bodyText;
	private long targetRole;
	private String[] recipients;

	public EmailMessage() {
	}

	public EmailMessage(String id, String language, String subject, String bodyText, long targetRole) {
		this.id = id;
		this.language = language;
		this.subject = subject;
		this.bodyText = bodyText;
		this.targetRole = targetRole;
	}

	public EmailMessage(String id, String language, String subject, String bodyText) {
		this.id = id;
		this.language = language;
		this.subject = subject;
		this.bodyText = bodyText;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	public long getTargetRole() {
		return targetRole;
	}

	public void setTargetRole(long targetRole) {
		this.targetRole = targetRole;
	}

	public String[] getRecipients() {
		return recipients;
	}

	public void setRecipients(String[] recipients) {
		this.recipients = recipients;
	}
}
