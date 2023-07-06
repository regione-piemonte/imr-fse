/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing;

import java.util.Random;

public class UidGenerator {

	private String studyBase;
	private String seriesBase;
	private int seriesIndex;
	private int instanceIndex;
	private long instanceCreationTime;

	public static final String O3EnterpriseUidRoot = "1.2.826.0.1.3680043.2.619";

	public UidGenerator() {
		reset();
	}

	public void reset() {
		studyBase = null;
		seriesBase = null;
		seriesIndex = 1;
		instanceIndex = 1;
	}

	public long getInstanceCreationTime() {
		return this.instanceCreationTime;
	}

	public String getNewStudyUid() {
		seriesIndex = 1;
		instanceIndex = 1;

		int randomValue = new Random().nextInt(10000);
		String randomString = Integer.toString(randomValue);

		this.instanceCreationTime = System.currentTimeMillis();
		String timeString = Long.toString(instanceCreationTime);

		studyBase = O3EnterpriseUidRoot + "." + randomString + "." + timeString;
		return studyBase;
	}

	public String getNewSeriesUid() {
		if (studyBase == null)
			getNewStudyUid();
		instanceIndex = 1;
		seriesBase = studyBase + "." + seriesIndex;
		seriesIndex++;
		return seriesBase;
	}

	public String getNewInstanceUid() {
		if (seriesBase == null)
			getNewSeriesUid();
		String instanceUid = seriesBase + "." + instanceIndex;
		instanceIndex++;
		return instanceUid;
	}

	public String[] getCompleteUidInfo() {
		if (seriesBase == null)
			getNewSeriesUid();
		String[] uid = new String[3];
		uid[0] = studyBase;
		uid[1] = studyBase + "." + seriesIndex;
		uid[2] = seriesBase + "." + instanceIndex;
		instanceIndex++;

		return uid;
	}

	public String getSeriesBase() {
		if (seriesBase == null)
			getNewSeriesUid();
		return seriesBase;
	}
}
