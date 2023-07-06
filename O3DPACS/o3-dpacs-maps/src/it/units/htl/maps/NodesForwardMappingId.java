/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.maps;

// Generated 24-Aug-2009 15:18:34 by Hibernate Tools 3.2.4.GA

/**
 * NodesforwardmappingId generated by hbm2java
 */
public class NodesForwardMappingId implements java.io.Serializable {

	private long sourceNodeFk;
	private long targetNodeFk;

	public NodesForwardMappingId() {
	}

	public NodesForwardMappingId(long sourceNodeFk, long targetNodeFk) {
		this.sourceNodeFk = sourceNodeFk;
		this.targetNodeFk = targetNodeFk;
	}

	public long getSourceNodeFk() {
		return this.sourceNodeFk;
	}

	public void setSourceNodeFk(long sourceNodeFk) {
		this.sourceNodeFk = sourceNodeFk;
	}

	public long getTargetNodeFk() {
		return this.targetNodeFk;
	}

	public void setTargetNodeFk(long targetNodeFk) {
		this.targetNodeFk = targetNodeFk;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof NodesForwardMappingId))
			return false;
		NodesForwardMappingId castOther = (NodesForwardMappingId) other;

		return (this.getSourceNodeFk() == castOther.getSourceNodeFk())
				&& (this.getTargetNodeFk() == castOther.getTargetNodeFk());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (int) this.getSourceNodeFk();
		result = 37 * result + (int) this.getTargetNodeFk();
		return result;
	}

}
