/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.postprocessing.studymove;

import it.units.htl.dpacs.dao.StudyMoveRemote;
import it.units.htl.dpacs.helpers.BeansName;

import javax.naming.InitialContext;



public class StudyMoveWorkerFind extends StudyMoveWorker{
	
	
	public StudyMoveWorkerFind(int minimumLife, int timeout) {
		super(minimumLife, timeout);		
	}
	
	
	public synchronized void run() {
		String[] toMark=getAccessionNumberToMark();		// This yields the ids of the studies to query 
		StudyMoveRemote bean=null;
		if(toMark!=null){
			try{
				bean = InitialContext.doLookup(BeansName.RStudyMove);
				long ret=-1;
				for(String accessionNumberToMoveUid: toMark){
					
					ret= bean.retrieveStudyMetadata(Long.parseLong(accessionNumberToMoveUid), timeout);
					if(ret<0)
						log.error("Error Move MOVE_STUDY_HISTORY.ID = " + accessionNumberToMoveUid);
					
				}
			}catch(Exception ex){
				log.error("An exception occurred when marking a study for deletion", ex);
			}
			log.info("Finished marking process");
		}


		log.info("End work for this round");
		
		
	}

	
	
}
