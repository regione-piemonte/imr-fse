/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.forwarder;

/*__*__*////**
/*__*__*///* Serve per comunicare in tempo reale l'andamento delle sub-operations
/*__*__*///* in una operazione di rete Dicom
/*__*__*///*/
public class DcmSubOpProgress {

	private DcmSubOpProgress.DcmSubOpStatus currentStatus;
	private DcmSubOpProgress.DcmSubOpStatus previousStatus;
	private boolean ended = false;
	private boolean started = false;

	public DcmSubOpProgress() {
		previousStatus = null;
		currentStatus = null;
	}

	public synchronized void reset() {
		previousStatus = null;
		currentStatus = null;
		ended = false;
		started = true;
		this.notifyAll();
	}

	public synchronized void writeStatus(
			DcmSubOpProgress.DcmSubOpStatus newStatus) {
		currentStatus = newStatus;
		this.notifyAll();
	}

	public synchronized DcmSubOpProgress.DcmSubOpStatus readStatus() {
		if ((started) & (!ended)) {
			while ((currentStatus == null) & (!ended)) {
				try {
					this.wait();
				} catch (InterruptedException ie) {
				}
			}
			if (currentStatus != null) {
				previousStatus = currentStatus;
				currentStatus = null;
			}
		}
		return previousStatus;
	}

	public synchronized boolean hasEnded() {
		return ended;
	}

	public synchronized boolean hasStarted() {
		return started;
	}

	public synchronized void end() {
		ended = true;
		started = false;
		notifyAll();
	}

	/* ____ */// /**
	/* ____ */// * Memorizza lo stato di una sub-operation
	/* ____ */// * in una operazione di rete Dicom
	/* ____ */// */
	public static class DcmSubOpStatus {

		private int remainingOps;
		private int completedOps;
		private int failedOps;
		private int warningOps;

		public DcmSubOpStatus(int rema, int comp, int fail, int warn) {
			remainingOps = rema;
			completedOps = comp;
			failedOps = fail;
			warningOps = warn;
		}

		public int getRemainingOperations() {
			return remainingOps;
		}

		public int getCompletedOperations() {
			return completedOps;
		}

		public int getFailedOperations() {
			return failedOps;
		}

		public int getWarningOperations() {
			return warningOps;
		}

		public String toString() {
			return new String("Remaining = " + remainingOps + "; completed = "
					+ completedOps + "; failed = " + failedOps + "; warning = "
					+ warningOps);
		}
	}
}
