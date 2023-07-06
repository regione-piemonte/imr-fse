/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

//import it.units.htl.dpacs.helpers.MaskingParameter;
import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.ConfigurationSettings;
import it.units.htl.dpacs.helpers.GlobalConfigurationLoader;
import it.units.htl.dpacs.helpers.GlobalSettings;
import it.units.htl.dpacs.helpers.GlobalSettings.PartitioningStrategy;
import it.units.htl.dpacs.postprocessing.verifier.util.DcmMover;
import it.units.htl.dpacs.postprocessing.verifier.util.DcmQuerier;
import it.units.htl.dpacs.postprocessing.verifier.util.DcmCommonObject;
import it.units.htl.dpacs.valueObjects.DicomConstants;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dcm4che.dict.Tags;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;



@Stateless
public class StudyMoveBean implements StudyMoveLocal, StudyMoveRemote {

	private Log log = LogFactory.getLog(StudyMoveBean.class);
	private @Resource(name="java:/jdbc/dbDS") DataSource dataSource;
	
	private static HashMap<Integer, String> studyLevelTags=new HashMap<Integer, String>();
	static {
		studyLevelTags.put(Tag.PatientName,null);
		studyLevelTags.put(Tag.PatientBirthDate,null);
		studyLevelTags.put(Tag.PatientSex,null);
		studyLevelTags.put(Tag.PatientID,null);
		studyLevelTags.put(Tag.IssuerOfPatientID,null);
		studyLevelTags.put(Tag.StudyInstanceUID,null);
		studyLevelTags.put(Tag.StudyID,null);
		studyLevelTags.put(Tag.StudyDate,null);
		studyLevelTags.put(Tag.StudyDescription,null);
	};
	
	private static HashMap<Integer, String> seriesLevelTags=new HashMap<Integer, String>();
	static {
		seriesLevelTags.put(Tag.SeriesInstanceUID,null);
		seriesLevelTags.put(Tag.SeriesNumber,null);
		seriesLevelTags.put(Tag.Modality,null);
		seriesLevelTags.put(Tag.SeriesDescription,null);
	};
	
	private static HashMap<Integer, String> instanceLevelTags=new HashMap<Integer, String>();
	static {
		instanceLevelTags.put(Tag.SOPInstanceUID,null);
		instanceLevelTags.put(Tag.SOPClassUID,null);
		instanceLevelTags.put(Tag.InstanceNumber,null);
	};
	
	
	public long moveStudy(String mshu, Integer timeout){

		long moveStudyHistoryUid=0;
		Connection con = null;
		CallableStatement cs=null;
		CallableStatement cs1=null;

		
		try {
			moveStudyHistoryUid=Long.parseLong(mshu);
			
			MoveInfo moveInfo=getMoveInfo(moveStudyHistoryUid);
			
			con = dataSource.getConnection();
			con.setAutoCommit(false);
			
			log.info("Richiesta move per MOVESTUDYHISTORY.id ="+moveStudyHistoryUid);
			
			cs1=con.prepareCall("{call updateMoveStudy(?,?)}");
			cs1.setLong(1, moveStudyHistoryUid);
			cs1.setInt(2,0);
			cs1.execute();
			con.commit(); 

			//Si richiama la Move
			 boolean rtr = moveAccessionNumber(moveInfo.accessionNumber, moveInfo.remoteAEtitle, moveInfo.remoteIp, moveInfo.remotePort, moveInfo.localAEtitle, moveInfo.destAEtitle, moveInfo.patientID, moveInfo.idIssuer,timeout, mshu, con );

             if (rtr) {
            	cs1.setInt(2,1);
     			cs1.execute();
     			con.commit();
     			log.info("Richiesta move per MOVESTUDYHISTORY.id ="+moveStudyHistoryUid+" Terminato correttamente");
             }else{
            	 log.info("Richiesta move per MOVESTUDYHISTORY.id ="+moveStudyHistoryUid+" Terminato con errori");
             }
			 
			
			
		} catch (Exception ex) {
			try{con.rollback();}catch(Exception iex){log.error("Could not rollback", iex);}
			log.error("", ex);
		} finally {
			CloseableUtils.close(cs);
			CloseableUtils.close(cs1);
			CloseableUtils.close(con);
		
		}
		return 0;
	};
	
	
	private boolean moveAccessionNumber(String accessionNumber, String remoteAEtitle, String remoteIp, int remotePort, String localAEtitle, String destAEtitle, String patientID, String idIssuer,Integer timeout, String moveStudyHistoryUid, Connection con ) {
		//Open AccessionNumber File
		try {
			
			        DcmMover cMove = new DcmMover(remoteAEtitle, remoteIp,remotePort, localAEtitle,  0);
				  
					DcmQuerier cQ = new DcmQuerier(remoteAEtitle, remoteIp, remotePort, localAEtitle, 0, timeout);
			        
					HashMap<Integer, String> matchingKeys = new HashMap<Integer, String>();
					matchingKeys.put(Tag.AccessionNumber, accessionNumber);
					//taskid:330912 bug:38483
    				log.info("moveAccessionNumber|Filter accessionNumber:" +accessionNumber + " moveStudyHistoryUid:" +moveStudyHistoryUid);
					
					//taskid:300060 bug:34065
					if(patientID!=null)
					{
						if(matchingKeys.containsKey(Tag.PatientID))
							matchingKeys.remove(Tag.PatientID);
						
						matchingKeys.put(Tag.PatientID,patientID);
						//taskid:330912 bug:38483
        				log.info("moveAccessionNumber|Filter patientID:" +patientID);
					}

					//taskid:326054 bug:37966
					if(idIssuer!=null)
					{
						if(matchingKeys.containsKey(Tag.IssuerOfPatientID))
							matchingKeys.remove(Tag.IssuerOfPatientID);
						
						matchingKeys.put(Tag.IssuerOfPatientID,idIssuer);
						//taskid:330912 bug:38483
        				log.info("moveAccessionNumber|Filter idIssuer:" +idIssuer);
					}		
					
					List<DicomObject> lista = null;
					DicomObject cmd = null;
					String studyInstanceUID = null;
					
					//setto il messaggio di errore
	        		String msg_err = "StudyMove: AccessionNumber Failed " + accessionNumber;
	        		if(patientID!=null)
	        			msg_err += " and patientID " + patientID;
	        		if(idIssuer!=null)
	        			msg_err += " and idIssuer " + idIssuer;

			        try {
			        	lista = cQ.doQuery(matchingKeys, DcmCommonObject.QueryRetrieveLevel.STUDY);
			        	
			        	//se la lista e' vuota segnalo errore
			        	if(lista.isEmpty()) {			        		
//			        		writeError(moveStudyHistoryUid,msg_err, con); 
			        		writeError(moveStudyHistoryUid, "ERR_218", con); //ERR_218 - Accession number inesistente o privo di immagini
			        		return false;
			        	}
			        	
			        	boolean res=true;
			        	
			        	for (int i=0;i<lista.size();i++){
			        		cmd = (DicomObject) lista.get(i);
			                
			        		log.info("*******************************************************");
			                log.info(cmd);
			                log.info("*******************************************************");
			        	
			        		//extract issuer. Tt is not mandatory in the queryDcm result
			        		if(idIssuer!=null)
			        		{
				        		//extract issuer. It is not mandatory in the queryDcm result
			        			String issuer = cmd.getString(Tag.IssuerOfPatientID);
			        			
			        			//se la risposta della query DCM non contiene il TAG IssuerOfPatientID comunque si effettua la movimentazione
			        			//in caso contrario verifica se la risposta relativa all'idIssuer coincide con quella richiesta
			        			if(issuer!=null)
			        			{
			        				// Issuer of Patient ID is type 3
			        				// The meaning of a zero length Type 3 Data Element shall be precisely the 
			        				// same as that element being absent from the Data Set.
			        				if(!issuer.equals(idIssuer))
			        				{
				        				res = false;
				        				continue;
			        				}
			        			}
			        			else
			        			{
			        				log.info("extract issuer is null by Tag.IssuerOfPatientID");
			        				log.info("moving anyway exams with issuer: " +idIssuer);
			        			}
			        		}

			        		studyInstanceUID = 	cmd.getString(Tag.StudyInstanceUID);
			        		
							if (studyInstanceUID != null) {
						        try {
									//taskid:330912 bug:38483
				    				log.info("moveAccessionNumber|Move studyInstanceUID:" +studyInstanceUID + " destAEtitle:" +destAEtitle);
						        	cMove.doMove(studyInstanceUID, destAEtitle);
						        } catch (Exception e) {
						           	writeError(moveStudyHistoryUid,"StudyMove: Failed to check for association exception. AccessionNumber" + accessionNumber + " "+e.toString(), con);
						           	res = false;
						        }
							}
			        	}
			        	if(!res)
			        		writeError(moveStudyHistoryUid,msg_err, con);
			        		
			        	return res;
			        } catch (Exception e) {
			        	if(e instanceof IOException || e instanceof InterruptedException) {
			        		writeError(moveStudyHistoryUid, "ERR_219", con); //ERR_219 - Problemi di connessione
			        		return false;
			        	} else {
			        		writeError(moveStudyHistoryUid,msg_err + " "+e.toString(), con);
			        		return false;
			        	}
			        }
    		
    	} catch (Exception e) {
    		writeError(moveStudyHistoryUid,"StudyMove: Generic Error " + accessionNumber + " "+e.toString(), con);
    		return false;
    	}

		//return true;
  
	}
	
	private void writeError(String mshu, String error, Connection con ) {
		
		long moveStudyHistoryUid=0;
		PreparedStatement ps=null;
		ResultSet rs=null;


		try {
			log.error(error);
  			moveStudyHistoryUid=Long.parseLong(mshu);
			ps=con.prepareStatement("UPDATE MoveStudyHistory SET errorMessage = ? WHERE id=? ");
  			ps.setString(1, error);
  			ps.setLong(2, moveStudyHistoryUid);
			ps.execute();
			con.commit();	// Since autoCommit is off
		} catch (Exception ex) {
			try{con.rollback();}catch(Exception iex){log.error("Could not rollback", iex);}
			log.error("", ex);
		} finally {
            CloseableUtils.close(ps);
            CloseableUtils.close(con);
		}
		
  
	}

	public long retrieveStudyMetadata(long moveStudyHistoryUid, Integer timeout) {
		long ret=-1;
		
		MoveInfo info=getMoveInfo(moveStudyHistoryUid);
		
		DcmQuerier query = new DcmQuerier(info.remoteAEtitle, info.remoteIp, info.remotePort, info.localAEtitle, 0, timeout);
        
		HashMap<Integer, String> matchingKeysStudy = new HashMap<Integer, String>();
		matchingKeysStudy.put(Tag.AccessionNumber, info.accessionNumber);
		//taskid:330912 bug:38483
		log.info("retrieveStudyMetadata|Filter accessionNumber:" +info.accessionNumber);
		matchingKeysStudy.putAll(studyLevelTags);
		
		//taskid:300060 bug:34065
		if(info.patientID!=null)
		{
			if(matchingKeysStudy.containsKey(Tag.PatientID))
				matchingKeysStudy.remove(Tag.PatientID);
			
			matchingKeysStudy.put(Tag.PatientID,info.patientID);
			//taskid:330912 bug:38483
			log.info("retrieveStudyMetadata|Filter patientID:" +info.patientID);
		}

		//taskid:326054 bug:37966
		if(info.idIssuer!=null)
		{
			if(matchingKeysStudy.containsKey(Tag.IssuerOfPatientID))
				matchingKeysStudy.remove(Tag.IssuerOfPatientID);
			
			matchingKeysStudy.put(Tag.IssuerOfPatientID,info.idIssuer);
			//taskid:330912 bug:38483
			log.info("retrieveStudyMetadata|Filter idIssuer:" +info.idIssuer);
		}	
		
		List<DicomObject> studies = null;
		DicomObject stCmd = null;
		PartitioningStrategy partitioningStrategy=GlobalSettings.getPartitioningStrategy();
		String defaultIdIssuer=GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.DEFAULT_ID_ISSUER);
		try {
			// 1st query: Specify AccNum, get studyUID and other Patient/Study info
        	studies = query.doQuery(matchingKeysStudy, DcmCommonObject.QueryRetrieveLevel.STUDY);
        	
        	for (int i=0;i<studies.size();i++){
        		
        		// Managing SQL connections inside this method for performance reasons
        		Connection con = null;
    			CallableStatement csStudies=null;
    			CallableStatement csSeries=null;
    			CallableStatement csInstances=null;
    			CallableStatement csEnd=null;
    			
        		try{
        			
        			con = dataSource.getConnection();
        			con.setAutoCommit(false);
        			log.info("Move request moveStudyHistoryUid = "+moveStudyHistoryUid);
        			csEnd=con.prepareCall("{call updateMoveStudy(?,?)}");
	    			csEnd.setLong(1, moveStudyHistoryUid);
	    			csEnd.setInt(2,0);
	    			csEnd.execute();
	    			con.commit();
	    			
        			csStudies=con.prepareCall("{call storeStudyLevelMetadata(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
        			
        			
	        		stCmd = (DicomObject) studies.get(i);
	        		if(stCmd!=null){
	        			String studyInstanceUID = 	stCmd.getString(Tag.StudyInstanceUID);
	        			String pName=stCmd.getString(Tag.PatientName);
	        			String[] names={null, null, null, null, null};
	        			if(pName!=null){
	        				String[] patientName = pName.split("(\\^)");
		                    if (patientName.length > 0) {
		                        for (int p = 0; p < patientName.length; p++) {
		                            if(patientName[p]!=null)
		                            	names[p]=patientName[p].toUpperCase();
		                        }
		                    }
	        			}
	        			if(names[0]==null)
	        				names[0]=DicomConstants.JOHN_DOE;
	        			java.util.Date td = new java.util.Date();
	        	        td = stCmd.getDate(Tag.PatientBirthDate);
	        	        	
	        			csStudies.setString(1, names[0]);
	        			csStudies.setString(2, names[1]);
	        			csStudies.setString(3, names[2]);
	        			csStudies.setString(4, names[3]);
	        			csStudies.setString(5, names[4]);
	        			if (td != null) {
	        	            csStudies.setDate(6,new java.sql.Date(td.getTime()));
	        	        }else{
	        	        	csStudies.setNull(6, Types.DATE);
	        	        }
	        			csStudies.setString(7, stCmd.getString(Tag.PatientSex));
	        			csStudies.setString(8, stCmd.getString(Tag.PatientID));
	        			String idIssuer=stCmd.getString(Tag.IssuerOfPatientID);
	        			if(partitioningStrategy.equals(PartitioningStrategy.CALLED)){	
	        				// If partitioning according to called AET (i.e. how the storage SCU calls this PACS) use the local AET: it is the AET the PACS uses as a FIND SCU, hence the AET registered on the remote node )
	                    	idIssuer=info.localAEtitle.toUpperCase();	
	                    }else if(partitioningStrategy.equals(PartitioningStrategy.CALLING)){
	                    	idIssuer=info.remoteAEtitle.toUpperCase();
	                    }
	        			if(idIssuer==null)
	        				idIssuer=defaultIdIssuer;
	        			csStudies.setString(9, idIssuer);
	        			
	        			csStudies.setString(10, studyInstanceUID);
	        			csStudies.setString(11, stCmd.getString(Tag.StudyID));
	        			td = stCmd.getDate(Tag.StudyDate);
	        			if (td != null) {
	        	            csStudies.setDate(12,new java.sql.Date(td.getTime()));
	        	        }else{
	        	        	csStudies.setNull(12, Types.DATE);
	        	        }
	        			csStudies.setString(13, stCmd.getString(Tag.AccessionNumber));
	        			csStudies.setString(14, stCmd.getString(Tag.StudyDescription));
	        			csStudies.setLong(15, info.knownNode);
	        			csStudies.registerOutParameter(16, Types.INTEGER);
	        			csStudies.execute();
	        			if(csStudies.getInt(16)<=0)
	        				throw new Exception("ERROR STORING STUDY "+studyInstanceUID+" - CODE: "+csStudies.getInt(16));
	        			
	        			
	        			HashMap<Integer, String> matchingKeysSeries = new HashMap<Integer, String>();
	        			matchingKeysSeries.put(Tag.StudyInstanceUID, studyInstanceUID);
	        			//taskid:330912 bug:38483
	        			log.info("retrieveStudyMetadata|FilterSeries studyInstanceUID:" +studyInstanceUID);
	        			matchingKeysSeries.putAll(seriesLevelTags);
	        			List<DicomObject> series = null;
	        			DicomObject seCmd = null;
	
	        			// 2nd query: Specify studyUID, get seriesUID and other Series info
	        			series=query.doQuery(matchingKeysSeries, DcmCommonObject.QueryRetrieveLevel.SERIES);
	        			csSeries=con.prepareCall("{call storeSeriesLevelMetadata(?,?,?,?,?,?)}");
    					csSeries.setString(1, studyInstanceUID);
    					
	        			for (int j=0;j<series.size();j++){
	        				seCmd = (DicomObject) series.get(j);
	        				if(seCmd!=null){
	        					String seriesInstanceUID = 	seCmd.getString(Tag.SeriesInstanceUID);	

	        					csSeries.setString(2, seriesInstanceUID);
	        					csSeries.setString(3, seCmd.getString(Tag.Modality));
	        					csSeries.setInt(4, seCmd.getInt(Tag.SeriesNumber));
	        					csSeries.setString(5, seCmd.getString(Tag.SeriesDescription));
	        					csSeries.registerOutParameter(6, Types.INTEGER);
	        					csSeries.execute();
	        					if(csSeries.getInt(6)<=0)
	    	        				throw new Exception("ERROR STORING SERIES "+seriesInstanceUID+" OF STUDY "+studyInstanceUID+"- CODE: "+csSeries.getInt(6));

	        					HashMap<Integer, String> matchingKeysInstance = new HashMap<Integer, String>();
	                			matchingKeysInstance.put(Tag.StudyInstanceUID, studyInstanceUID);
	    	        			//taskid:330912 bug:38483
	    	        			log.info("retrieveStudyMetadata|FilterInstance studyInstanceUID:" +studyInstanceUID);
	                			matchingKeysInstance.put(Tag.SeriesInstanceUID, seriesInstanceUID);
	    	        			//taskid:330912 bug:38483
	    	        			log.info("retrieveStudyMetadata|FilterInstance seriesInstanceUID:" +seriesInstanceUID);
	                			matchingKeysInstance.putAll(instanceLevelTags);
	                			List<DicomObject> instances = null;
	                			DicomObject inCmd = null;
	                			
	                			// 3rd query: Specify seriesUID, get instanceUID and other Instance info
	                			instances=query.doQuery(matchingKeysInstance, DcmCommonObject.QueryRetrieveLevel.IMAGE);
	                			csInstances=con.prepareCall("{call storeInstanceLevelMetadata(?,?,?,?,?,?)}");
	                			csInstances.setString(1, studyInstanceUID);
	                			csInstances.setString(2, seriesInstanceUID);
	        					
	                			for (int k=0;k<instances.size();k++){
	                				inCmd = (DicomObject) instances.get(k);
	                				if(inCmd!=null){
	                					String sopInstanceUID = 	inCmd.getString(Tag.SOPInstanceUID);
	                					
	                					csInstances.setString(3, sopInstanceUID);
	                					csInstances.setString(4, inCmd.getString(Tag.SOPClassUID));
	                					csInstances.setInt(5, inCmd.getInt(Tag.InstanceNumber));
	                					csInstances.registerOutParameter(6, Types.INTEGER);
	                					csInstances.execute();
	    	        					if(csInstances.getInt(6)<=0)
	    	    	        				throw new Exception("ERROR STORING INSTANCE "+sopInstanceUID+" OF SERIES "+seriesInstanceUID+" OF STUDY "+studyInstanceUID+" - CODE: "+csInstances.getInt(6));
	                				}
	                			}
	        				}	// end Series
	        			}
	        		
	        		}
	        		con.commit();
	        		
	    			csEnd.setInt(2,1);
	    			csEnd.execute();
	    			log.info("moveStudyHistoryUid = " +moveStudyHistoryUid+"trattato correttamente");
	    			con.commit();
	        		ret=1;
        		}catch(Exception ex){
        			try{con.rollback();}catch(Exception iex){log.error("Could not rollback transaction", iex);}
        			log.error("ERROR retrieving metadata", ex);
        			writeError(""+moveStudyHistoryUid,ex.getMessage(), con);
        		}finally{
        			CloseableUtils.close(csEnd);
        			CloseableUtils.close(csInstances);
        			CloseableUtils.close(csSeries);
        			CloseableUtils.close(csStudies);
        			CloseableUtils.close(con);
        		}
        		
        	}
        } catch (Exception e) {
        	log.error(e);
        }	
		
		return ret;
	}
		
	private class MoveInfo{
		String accessionNumber; 
		String remoteAEtitle;
		String remoteIp;
		int remotePort;
		String localAEtitle;
		String destAEtitle;
		long knownNode;
		String patientID;
		String idIssuer;
	}
	
	private MoveInfo getMoveInfo(long moveStudyHistoryUid){
		
		MoveInfo ret=null;
		
		Connection con = null;
		CallableStatement cs=null;
		
		try {
			con = dataSource.getConnection();
			
			cs=con.prepareCall("{call getParamsToRetrieveAccNum(?,?,?,?,?,?,?,?,?,?)}");
			cs.setLong(1, moveStudyHistoryUid);
			cs.registerOutParameter(2, Types.VARCHAR);
			cs.registerOutParameter(3, Types.VARCHAR);
			cs.registerOutParameter(4, Types.VARCHAR);
			cs.registerOutParameter(5, Types.VARCHAR);
			cs.registerOutParameter(6, Types.VARCHAR);
			cs.registerOutParameter(7, Types.INTEGER);
			cs.registerOutParameter(8, Types.INTEGER);
			//taskid:300060 bug:34065
			cs.registerOutParameter(9, Types.VARCHAR);
			//taskid:326054 bug:37966
			cs.registerOutParameter(10, Types.VARCHAR);
			cs.execute();
			
			ret=new MoveInfo();
			ret.remoteAEtitle  =cs.getString(2); // CALLED_AET
			ret.localAEtitle   =cs.getString(3); // CALLING_AET
			ret.destAEtitle	   =cs.getString(4); // MOVE_AET	
			ret.accessionNumber=cs.getString(5); // ACCESSIONNUMBER
			ret.remoteIp       =cs.getString(6); // IP
			ret.remotePort     =cs.getInt(7);    // PORT
			ret.knownNode	   =cs.getInt(8);	 // KNOWNNODE
			//taskid:300060 bug:34065
			ret.patientID      =cs.getString(9); // PATIENT_ID
			//taskid:300060 bug:34065
			ret.idIssuer       =cs.getString(10); // ID_ISSUER
			
			if(ret.accessionNumber==null)
				throw new Exception("No accessionNumber found for id "+moveStudyHistoryUid);
 	
		} catch (Exception ex) {
			log.error("", ex);
		} finally {
			CloseableUtils.close(cs);
			CloseableUtils.close(con);
		}
		return ret;
	}
}
