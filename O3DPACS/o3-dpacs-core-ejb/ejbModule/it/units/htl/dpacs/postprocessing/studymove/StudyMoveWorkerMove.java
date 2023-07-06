/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.studymove;

import it.units.htl.dpacs.dao.StudyMoveRemote;
import it.units.htl.dpacs.helpers.BeansName;

import javax.naming.InitialContext;


public class StudyMoveWorkerMove extends StudyMoveWorker{
	
	
	public StudyMoveWorkerMove(int minimumLife, int timeout) {
		super(minimumLife, timeout);		
	}
	
	public synchronized void run() {
		String[] toMark=getAccessionNumberToMark();
		StudyMoveRemote bean=null;
		log.info("Start this round");
		if(toMark!=null){
			try{
				bean = InitialContext.doLookup(BeansName.RStudyMove);
				long ret=-1;
				for(String accessionNumberToMoveUid: toMark){
					
						ret=bean.moveStudy(accessionNumberToMoveUid, timeout);
						if(ret<0)
							log.error("Error Move MOVE_STUDY_HISTORY.ID = " + accessionNumberToMoveUid);
					
				}
			}catch(Exception ex){
				log.error("An exception occurred when moving a study", ex);
			}
			log.info("Finished moving process");
		}

		log.info("End work for this round");
	}

	
	
}
