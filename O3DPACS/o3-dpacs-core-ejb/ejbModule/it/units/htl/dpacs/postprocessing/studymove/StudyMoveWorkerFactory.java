/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.studymove;

public class StudyMoveWorkerFactory {

	private static final String WORKER_MOVE="MOVE";
	private static final String WORKER_FIND="FIND";
	
	public static StudyMoveWorker getInstance(String type, int minimumLife, int timeout){
		if(WORKER_MOVE.equals(type))
			return new StudyMoveWorkerMove(minimumLife, timeout);
		else if(WORKER_FIND.equals(type))
			return new StudyMoveWorkerFind(minimumLife, timeout);
		else
			return null;
	} 

}
