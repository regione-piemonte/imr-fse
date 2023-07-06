/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;


import java.util.HashSet;
import java.util.Set;

public class PhysicalMedia implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private Long pk;
	private String name;
	private Integer nextDevice;
	private Boolean available;
	private String type;
	private Character purpose;
	private Long capacityInBytes;
	// private Long highWaterInBytes; // renamed to toleranceInBytes
	private Long toleranceInBytes;
	private Long filledBytes;
	private String urlToStudy;
	private String humanReadableNotes;
	private Set knownNodes = new HashSet(0);

	public PhysicalMedia() {
	}

	public PhysicalMedia(String name, Integer nextDevice, Boolean available,
			String type, Character purpose, Long capacityInBytes,
			Long toleranceInBytes, Long filledBytes, String urlToStudy,
			String humanReadableNotes, Set knownNodes) {
		this.name = name;
		this.nextDevice = nextDevice;
		this.available = available;
		this.type = type;
		this.purpose = purpose;
		this.capacityInBytes = capacityInBytes;
		this.toleranceInBytes = toleranceInBytes;
		this.filledBytes = filledBytes;
		this.urlToStudy = urlToStudy;
		this.humanReadableNotes = humanReadableNotes;
		this.knownNodes = knownNodes;
	}

	public Long getPk() {
		return this.pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNextDevice() {
		return this.nextDevice;
	}

	public void setNextDevice(Integer nextDevice) {
		this.nextDevice = nextDevice;
	}

	public Boolean getAvailable() {
		return this.available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Character getPurpose() {
		return this.purpose;
	}

	public void setPurpose(Character purpose) {
		this.purpose = purpose;
	}

	public Long getCapacityInBytes() {
		return this.capacityInBytes;
	}

	public void setCapacityInBytes(Long capacityInBytes) {
		this.capacityInBytes = capacityInBytes;
	}

	public Long getToleranceInBytes() {
		return this.toleranceInBytes;
	}

	public void setToleranceInBytes(Long toleranceInBytes) {
		this.toleranceInBytes = toleranceInBytes;
	}

	public Long getFilledBytes() {
		return this.filledBytes;
	}

	public void setFilledBytes(Long filledBytes) {
		this.filledBytes = filledBytes;
	}

	public String getUrlToStudy() {
		return this.urlToStudy;
	}

	public void setUrlToStudy(String urlToStudy) {
		this.urlToStudy = urlToStudy;
	}

	public String getHumanReadableNotes() {
		return this.humanReadableNotes;
	}

	public void setHumanReadableNotes(String humanReadableNotes) {
		this.humanReadableNotes = humanReadableNotes;
	}

	public Set getKnownNodes() {
		return this.knownNodes;
	}

	public void setKnownNodes(Set knownNodes) {
		this.knownNodes = knownNodes;
	}

}
