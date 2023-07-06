/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.dpacs.dao;

import it.units.htl.dpacs.helpers.CloseableUtils;
import it.units.htl.dpacs.helpers.LogMessage;
import it.units.htl.dpacs.valueObjects.CodeSequence;
import it.units.htl.dpacs.valueObjects.MPPSItem;
import it.units.htl.dpacs.valueObjects.PersonalName;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.net.ActiveAssociation;

/** The implementation of the administration bean */
@Stateless
public class DicomMppsDealerBean implements DicomMppsDealerLocal {
	
	static final Log log = LogFactory.getLog(DicomMppsDealerBean.class);
	private static final long serialVersionUID = 1L;
	
	private @Resource(name="java:/jdbc/mppsDS") DataSource dataSource = null;
	private String SOPInstanceUID;
//	private static final String insertMPPStoBillingCodSequence = "INSERT INTO MPPSBillingProcStepCodeSeq(mppsInfoFK,billingProcStepCodeSeqFK) VALUES (?,?)";
	private static final String insertMPPSInfo = "INSERT INTO MPPSInfo(sopInstanceUID,performedProcedureStepID,performedStationAETitle,performedStationName,performedProcStepStartDate,performedProcStepStartTime,performedProcedureStepStatus,performedProcStepDescr,performedProcTypeDescr,procedureCodeSequenceFK,performedProcedureStepEndDate,performedProcedureStepEndTime,commentsOnPPS,ppsDiscontReasonCodeSeqFK,modality) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//	private static final String insertMPPStoProtocolRelation = "INSERT INTO MPPSInfoToPPCodeSequence(mppsInfoFK,performedProtCodeSeqFK) VALUES (?,?)";
	private static final String insertCodSeq = "INSERT INTO CodeSequences(codeValue, codingSchemeDesignator, codingSchemeVersion, codeMeaning) VALUES (?, ?, ?, ?)";
	private static final String selectInsertedCodSeq = "SELECT max(pk) FROM CodeSequences WHERE (codeValue=? OR codeValue IS NULL) AND (codingSchemeDesignator=? OR codingSchemeDesignator IS NULL) AND (codingSchemeVersion=? OR codingSchemeVersion IS NULL)";
	private static final String selectSOP = "SELECT SopInstanceUID FROM MPPSInfo WHERE (SopInstanceUID=?)";
	
	private ActiveAssociation _assoc = null; 
	private String _callingAE = "";
	
	public DicomMppsDealerBean() {
	} // end constructor


	@PreDestroy
	public void ejbRemove() {
	}

	public void setSessionContext(SessionContext sc) {
	}

	// Business Methods

	public int insertMPPSItem(MPPSItem m, ActiveAssociation assoc) {
		Connection con = null;
		
		PreparedStatement selectInsertedCodSeqPS = null;
		PreparedStatement selectSOPps = null;
		PreparedStatement insertMPPStoProtocolRelationPS = null;
		PreparedStatement insertMPPStoBillingCodSequencePS = null;
		PreparedStatement insertStatement = null;
		PreparedStatement insertRelationStatement = null;
		PreparedStatement insertRadiation = null;
		PreparedStatement findDosePk = null;
		PreparedStatement insertFCS = null;
		PreparedStatement insertExposure = null;
		PreparedStatement insertBillingItem = null;
		PreparedStatement findBillingPk = null;
		PreparedStatement insertQuantityItem = null;
		PreparedStatement insertSeries = null;
		PreparedStatement findSeriesPk = null;
		PreparedStatement InsertPhtoPPSPS = null;
		PreparedStatement InsertOptoPPSPS = null;
		PreparedStatement insertSSA = null;
		PreparedStatement selectStatement = null;
		
		ResultSet isThereAlreadySOPInstance = null;
		ResultSet maxDosePkRS = null;
		ResultSet maxBillingPkRS = null;
		ResultSet maxSpkRS = null;
		ResultSet rs = null;
		
		try{
			con = dataSource.getConnection();
		}catch (SQLException ex){
			log.error(LogMessage._NoDBConnection, ex);
		}
		
		_assoc = assoc;
		_callingAE = _assoc.getAssociation().getCallingAET();
		int STATUS = 0;
		SOPInstanceUID = m.getMPPSSOPInstance();
		
		// Fill MPPSInfo Table
		try {
			log.info(_callingAE + ": MPPS Dealer: Inserting MPPS info, instance " + SOPInstanceUID);
			selectSOPps = con.prepareStatement(selectSOP);
			selectSOPps.setString(1, SOPInstanceUID);
			isThereAlreadySOPInstance = selectSOPps.executeQuery();
			if (isThereAlreadySOPInstance.next() == false) {
				log.debug(_callingAE + ":Receive a new MPPS SOP Instance: going to store Instance");
			} else {
				log.info(_callingAE + ":SOP Instance already in DataBase, can't store this Instance");
				return 0110;
			}
			if ("IN PROGRESS".equalsIgnoreCase(m
					.getPerformedProcedureStepStatus())) {
				log.debug(_callingAE + ":MPPS Dealer: Inserting MPPS info, IN PROGRESS instance " + SOPInstanceUID);
			} else {
				log.debug(_callingAE + ": Received N-Create MPPS Message is not valid: has not IN PROGRESS status");
				log.fatal(_callingAE + ": MPPS Dealer: Not a valid status, about to return");
				return 0110;
			}
			
			insertStatement = con.prepareStatement(insertMPPSInfo);
			insertStatement.setString(1, m.getMPPSSOPInstance());
			insertStatement.setString(2, m.getPerformedProcedureStepID());
			insertStatement.setString(3, m.getPerformedStationAETitle());
			insertStatement.setString(4, m.getPerformedStationName());
			insertStatement.setDate(5, m.getPerformedProcedureStepStartDate());
			insertStatement.setTime(6, m.getPerformedProcedureStepStartTime());
			insertStatement.setString(7, m.getPerformedProcedureStepStatus());
			insertStatement.setString(8, m.getPerformedProcedureStepDescription());
			insertStatement.setString(9, m.getPerformedProcedureTypeDescription());
			if (m.getProcedureCodeSequence() != null) {
				insertStatement.setLong(10, storeCodeSequence(m.getProcedureCodeSequence()));
			} else {
				insertStatement.setLong(10, 0);
			}
			insertStatement.setDate(11, m.getPerformedProcedureStepEndDate());
			insertStatement.setTime(12, m.getPerformedProcedureStepEndTime());
			insertStatement.setString(13, m.getCommentsOnPPS());
			if (m.getDiscontinuationReasonCodeSequence() != null) {
				insertStatement.setLong(14, storeCodeSequence(m.getDiscontinuationReasonCodeSequence()));
			} else {
				insertStatement.setLong(14, 0);
				insertStatement.setString(15, m.getModality());
			}

			// insertStatement.setString(16,m.getReferencedStudySequenceSOPInstance());
			insertStatement.executeUpdate();
			if (m.getPerformedProtocolCodeSequence() != null) {
				log.debug(_callingAE + ": STARTING: Inserting Protocol Code Sequence");
				CodeSequence[] temp = m.getPerformedProtocolCodeSequence();
				
				insertRelationStatement = con.prepareStatement("INSERT into MPPSInfoToPPCodeSequence VALUES (?,?)");
				insertRelationStatement.setString(1, SOPInstanceUID);
				for (int i = 0; i < temp.length; i++) {
					long fk = storeCodeSequence(temp[i]);
					insertRelationStatement.setLong(2, fk);
					insertRelationStatement.executeUpdate();
				}
				log.debug(_callingAE + ": FINISHED: Inserting Protocol Code Sequence");
			}
			if (m.getBillingProcedureStepCodeSequence() != null) {
				log.debug(_callingAE + ": STARTING: Inserting Billing procedure step code sequence");
				CodeSequence[] temp = m.getBillingProcedureStepCodeSequence();
				insertRelationStatement = con.prepareStatement("INSERT into MPPSBillingProcStepCodeSeq (billingProcStepCodeSeqFK,mppsInfoFK)VALUES (?,?)");
				insertRelationStatement.setString(2, SOPInstanceUID);
				for (int i = 0; i < temp.length; i++) {
					long fk = storeCodeSequence(temp[i]);
					insertRelationStatement.setLong(1, fk);
					insertRelationStatement.executeUpdate();
				}
				log	.debug(_callingAE + ": FINISHED: Inserting Billing procedure step code sequence");
			}

			// finisce MPPSINFO con tuute le reference, ora tocca a
			// mppsradiation dose
			
			insertRadiation = con.prepareStatement("insert into MPPSRadiationDoses (anatomicStructureCodeSeqFK,totalTimeOfFluoroscopy,totalNumberOfExposures,distanceSourceToDetector,distanceSourceToEntrance,entranceDose,entranceDoseMGY,exposedArea,imageAreaDoseProduct,commentsOnRadiationDose,mppsInfoFK) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
			log.debug("STARTING: Inserting into MPPSRadiationDoses");
			if (m.getAnatomicsStructureSpaceOrRegionCodeSequence() != null) {
				insertRadiation.setLong(1, storeCodeSequence(m.getAnatomicsStructureSpaceOrRegionCodeSequence()));
			} else {
				insertRadiation.setLong(1, 0);
			}
			if (m.getTotalTimeOfFluoroscopy() == null) {
				insertRadiation.setNull(2, Types.INTEGER);
			} else {
				insertRadiation.setInt(2, Integer.parseInt(m.getTotalTimeOfFluoroscopy()));
			}
			if (m.getTotalNumberOfExposures() == null) {
				insertRadiation.setNull(3, Types.INTEGER);
			} else {
				insertRadiation.setInt(3, Integer.parseInt(m.getTotalNumberOfExposures()));
			}
			if (m.getDistanceSourceToDetector() == null) {
				insertRadiation.setNull(4, Types.DOUBLE);
			} else {
				insertRadiation.setDouble(4, Double.parseDouble(m.getDistanceSourceToDetector()));
			}
			if (m.getDistanceSourceToEntrance() == null) {
				insertRadiation.setNull(5, Types.DOUBLE);
			} else {
				insertRadiation.setDouble(5, Double.parseDouble(m.getDistanceSourceToEntrance()));
			}
			if (m.getEntranceDose() == null) {
				insertRadiation.setNull(6, Types.INTEGER);
			} else {
				insertRadiation.setInt(6, Integer.parseInt(m.getEntranceDose()));
			}
			if (m.getEntranceDoseMGY() == null) {
				insertRadiation.setNull(7, Types.DOUBLE);
			} else {
				insertRadiation.setDouble(7, Double.parseDouble(m.getEntranceDoseMGY()));
			}
			if (m.getExposedArea() == null) {
				insertRadiation.setNull(8, Types.INTEGER);
			} else {
				insertRadiation.setInt(8, Integer.parseInt(m.getExposedArea()));
			}
			if (m.getImageAreaDoseProduct() == null) {
				insertRadiation.setNull(9, Types.DOUBLE);
			} else {
				insertRadiation.setDouble(9, Double.parseDouble(m.getImageAreaDoseProduct()));
			}
			insertRadiation.setString(10, m.getCommentsOnRadiationDose());
			insertRadiation.setString(11, SOPInstanceUID);
			insertRadiation.executeUpdate();
			// genera l'ultima pk
			
			String find = "SELECT max(pk) FROM MPPSRadiationDoses WHERE mppsInfoFK=?";
			findDosePk = con.prepareStatement(find);
			findDosePk.setString(1, SOPInstanceUID);
			long maxDosePk = 000000;
			maxDosePkRS = findDosePk.executeQuery();
			if (maxDosePkRS.next()) {
				maxDosePk = maxDosePkRS.getLong(1);
			} else {
				log.error(_callingAE + ": Can't get max PK");
			}
			log.debug(_callingAE + ": COMPLETED: insert into MPPSRadiationDoses");
			// ----------------------------mppsFilmConsumptionTable---------------------------------------------
			if (m.getFilmConsumptionSequence() != null) {
				log.debug(_callingAE + ": MPPS Dealer: Started to deal with Film Consumption");
				MPPSItem.FilmConsumptionSequence[] tempo = m.getFilmConsumptionSequence();
				insertFCS = con.prepareStatement("insert into MPPSFilmConsumptionSequences (filmSizeId,numberOfFilms,mediumType,mppsInfoFK) VALUES (?,?,?,?)");
				for (int i = 0; i < tempo.length; i++) {
					insertFCS.setString(1, tempo[i].getFilmSizeID());
					if (tempo[i].getNumberOfFilms() == null) {
						insertFCS.setNull(2, Types.BIGINT);
					} else {
						insertFCS.setLong(2, Long.parseLong(tempo[i].getNumberOfFilms()));
					}
					insertFCS.setString(3, tempo[i].getMediumType());
					insertFCS.setString(4, SOPInstanceUID);
					insertFCS.executeUpdate();
				}
				log.debug(_callingAE + ": FINISHED: FilmConsumptionSequence");
			}
			if (m.getExposureDoseSequence() != null) {
				log.debug(_callingAE + ": STARTING: exposure dose sequence");
				MPPSItem.ExposureDoseSequence[] tempu = m.getExposureDoseSequence();
				insertExposure = con.prepareStatement("insert into MPPSExposureDoseSequences (radiationMode,kvp,xRayTubeCurrent,exposureTime,filterType,filterMaterial,MPPSRadiationDoseFK) VALUES (?,?,?,?,?,?,?)");
				for (int i = 0; i < tempu.length; i++) {
					insertExposure.setString(1, tempu[i].getRadiationMode());
					insertExposure.setString(2, tempu[i].getKvp());
					insertExposure.setString(3, tempu[i].getxRayTubeCurrent());
					insertExposure.setString(4, tempu[i].getExposureTime());
					insertExposure.setString(5, tempu[i].getFilterType());
					insertExposure.setString(6, tempu[i].getFilterMaterial());
					insertExposure.setLong(7, maxDosePk);
					insertExposure.executeUpdate();
				} // end for
				log.debug(_callingAE + ": FINISHED: Exposure Dose Sequence");
			} // end if
			if (m.getBillingItemSequence() != null) {
				log.debug(_callingAE + ": STARTING:  Billing Item SEQUENCE");
				MPPSItem.BillingItemSequence[] temps = m.getBillingItemSequence();
				insertBillingItem = con.prepareStatement("insert into MPPSBillingSeq (billingItemCodeSequenceFK,mppsInfoFK) VALUES (?,?)");
				for (int i = 0; i < temps.length; i++) {
					if (temps[i].getBillingItemCode() != null) {
						insertBillingItem.setLong(1, storeCodeSequence(temps[i].getBillingItemCode()));
					} else {
						insertBillingItem.setLong(1, 0);
					}
					insertBillingItem.setString(2, SOPInstanceUID);
					insertBillingItem.executeUpdate();
					// genera l'ultima pk----------------------------------
					String findQ = "SELECT max(pk) FROM MPPSBillingSeq WHERE mppsInfoFK=?";
					findBillingPk = con.prepareStatement(findQ);
					findBillingPk.setString(1, SOPInstanceUID);
					long maxBillingPk = 000000;
					maxBillingPkRS = findBillingPk.executeQuery();
					if (maxBillingPkRS.next()) {
						maxBillingPk = maxBillingPkRS.getLong(1);
					} else {
						log.error(_callingAE + ": Can't get max PK");
					}
					insertQuantityItem = con.prepareStatement("insert into MPPSQuantitySequences (quantity,measuringUnitsCodeSequenceFK,mppsBillingSequenceFK) VALUES (?,?,?)");
					log.debug(_callingAE + ": Going to insert " + temps[i].getQuantity());
					insertQuantityItem.setFloat(1, Float.parseFloat(temps[i].getQuantity()));
					if (temps[i].getMeasuringUnitsSequence() != null) {
						insertQuantityItem.setLong(2,storeCodeSequence(temps[i].getMeasuringUnitsSequence()));
					} else {
						insertQuantityItem.setLong(2, 0);
					}
					insertQuantityItem.setLong(3, maxBillingPk);
					insertQuantityItem.executeUpdate();
				} // end for mpps supplies
				log.debug(_callingAE + ": FINISHED: Billing table WRITTEN");
			}
			// --------------------------------MPPS PERFORMED SERIES
			// SEQUENCE_------------------------
			if (m.getPerformedSeriesSequence() != null) {
				log.debug("STARTING: MPPS Dealer to deal with Performed Series Sequence");
				MPPSItem.PerformedSeriesSequence[] tempp = m.getPerformedSeriesSequence();
				insertSeries = con.prepareStatement("insert into MPPSPerformedSeriesSequences (protocolName,retrieveAETitle,seriesDescription,seriesFK,mppsInfoFK) VALUES (?,?,?,?,?)");
				for (int i = 0; i < tempp.length; i++) {
					insertSeries.setString(1, tempp[i].getProtocolName());
					insertSeries.setString(2, tempp[i].getRetrieveAETitle());
					insertSeries.setString(3, tempp[i].getseriesDescription());
					insertSeries.setString(4, tempp[i].getSeriesInstanceUID());
					insertSeries.setString(5, SOPInstanceUID);
					insertSeries.executeUpdate();
					String findS = "SELECT max(pk) FROM MPPSPerformedSeriesSequences WHERE mppsInfoFK=?";
					findSeriesPk = con.prepareStatement(findS);
					findSeriesPk.setString(1, SOPInstanceUID);
					long maxSpk = 000000;
					maxSpkRS = findSeriesPk.executeQuery();
					if (maxSpkRS.next()) {
						maxSpk = maxSpkRS.getLong(1);
					} else {
						log.error(_callingAE + ": Can't find max PK");
					}
					log.debug("STARTING: Dealing with physicians...");
					String[] Pnames = tempp[i].getPerformingPhysicianName();
					if (Pnames != null) {
						List<PersonalName> Pns = convertStringToPersonalName(Pnames);
						for (int j = 0; j < Pns.size(); j++) {
							long pk = storePersonnel((PersonalName) Pns.get(j));
							String InsertPhtoPPS = "INSERT INTO PhysiciansToPSS (performingPhysiciansNameFK,mppsPerformedSeriesSequenceFK) VALUES (?,?)";
							InsertPhtoPPSPS = con.prepareStatement(InsertPhtoPPS);
							InsertPhtoPPSPS.setLong(1, pk);
							InsertPhtoPPSPS.setLong(2, maxSpk);
							InsertPhtoPPSPS.executeUpdate();
						}
					} else {
						log.error(_callingAE + ": PerformingPhysicianName is null");
					}
					log.debug(_callingAE + ": STARTING: Dealing with operators...");
					String[] Onames = tempp[i].getOperatorsName();
					if (Onames != null) {
						List<PersonalName> Ons = convertStringToPersonalName(Onames);
						for (int j = 0; j < Ons.size(); j++) {
							long pk = storePersonnel((PersonalName) Ons.get(j));
							String InsertOptoPPS = "INSERT INTO OperatorsToPSS (operatorFK,mppsPerformedSeriesSequenceFK) VALUES (?,?)";
							InsertOptoPPSPS = con.prepareStatement(InsertOptoPPS);
							InsertOptoPPSPS.setLong(1, pk);
							InsertOptoPPSPS.setLong(2, maxSpk);
							InsertOptoPPSPS.executeUpdate();
						}
					} else {
						log.error(_callingAE + ": Operator Name is null");
					}
				}
				log.debug(_callingAE + ": COMPLETED: performed series sequence");
			}

			// ------------------------------------scheduled step
			// attribute------------------------------
			if (m.getScheduledStepAttributesSequence() != null) {
				log.debug(_callingAE + ": STARTING: MPPS Dealer to deal with Scheduled Step Attribute");
				try {
					MPPSItem.ScheduledStepAttributesSequence[] tempssa = m.getScheduledStepAttributesSequence();
					insertSSA = con.prepareStatement("insert into MPPSSchedStepAttrSeq (accessionnumber,placerordernumberimserreq,fillerordernumberimserreq,schedProcStepDescription,mppsinfofk,studyfk) VALUES (?,?,?,?,?,?)");
					insertRelationStatement = con.prepareStatement("INSERT into MPPSSchedStepToProtCodeSeq (mppsSchedStepAttrSeqFK,schedProtocolCodeSequenceFK) VALUES (?,?)");
					for (int i = 0; i < tempssa.length; i++) {
						insertSSA.setString(1, tempssa[i].getAccessionNumber());
						insertSSA.setString(2, tempssa[i].getPlaceOrderNumbreInSerReq());
						insertSSA.setString(3, tempssa[i].getFillerOrderNumberInSerReq());
						insertSSA.setString(4, tempssa[i].getScheduledProcedureDescription());
						insertSSA.setString(5, SOPInstanceUID);
						insertSSA.setString(6, tempssa[i].getStudyInstanceUID());
						insertSSA.executeUpdate();
						selectStatement = con.prepareStatement("SELECT max(pk) FROM MPPSSchedStepAttrSeq WHERE (mppsInfoFK=?)");
						selectStatement.setString(1, SOPInstanceUID);
						rs = selectStatement.executeQuery();
						long key = 0;
						if (rs.next()) {
							key = rs.getLong(1);
						}
						insertRelationStatement.setLong(1, key);
						if (tempssa[i].getScheduledProtocolCodeSequence() != null) {
							CodeSequence[] daCambiare = tempssa[i].getScheduledProtocolCodeSequence();
							if (daCambiare.length > 0) {
								for (int j = 0; j < daCambiare.length; j++) {
									long fk = storeCodeSequence(daCambiare[j]);
									insertRelationStatement.setLong(2, fk);
									insertRelationStatement.executeUpdate();
								}
							}
						} else {
							log.error(_callingAE + ": Scheduled Protocol Code Sequence is null");
						}
					}
				} catch (SQLException e) {
					log.error(_callingAE, e);
					STATUS = 0110;
				}
				log.debug(_callingAE + ": COMPLETED: scheduled step attribute sequence");
			}
		}catch (SQLException sex){
			log.error(_callingAE, sex);
			STATUS = 0110;
		}finally{
			CloseableUtils.close(isThereAlreadySOPInstance);
			CloseableUtils.close(maxDosePkRS);
			CloseableUtils.close(maxBillingPkRS);
			CloseableUtils.close(maxSpkRS);
			CloseableUtils.close(rs);
			
			CloseableUtils.close(selectInsertedCodSeqPS);
			CloseableUtils.close(selectSOPps);
			CloseableUtils.close(insertMPPStoProtocolRelationPS);
			CloseableUtils.close(insertMPPStoBillingCodSequencePS);
			CloseableUtils.close(insertStatement);
			CloseableUtils.close(insertRelationStatement);
			CloseableUtils.close(findDosePk);
			CloseableUtils.close(insertFCS);
			CloseableUtils.close(insertExposure);
			CloseableUtils.close(insertBillingItem);
			CloseableUtils.close(findBillingPk);
			CloseableUtils.close(insertQuantityItem);
			CloseableUtils.close(insertSeries);
			CloseableUtils.close(findSeriesPk);
			CloseableUtils.close(InsertPhtoPPSPS);
			CloseableUtils.close(InsertOptoPPSPS);
			CloseableUtils.close(insertSSA);
			CloseableUtils.close(selectStatement);
			
			CloseableUtils.close(con);	
		}
		return STATUS;
	} // end insertMPPSItem()

	public int updateMPPSItem(MPPSItem m, ActiveAssociation assoc ) {
		Connection con = null;
		PreparedStatement selectSOPps = null;
		PreparedStatement insertCodSeqPS = null;
		PreparedStatement insertMPPStoProtocolRelationPS = null;
		PreparedStatement selectInsertedCodSeqPS = null;
		PreparedStatement insertMPPStoBillingCodSequencePS = null;
		ResultSet isThereAlreadySOPInstance = null;
		
		try{
			con = dataSource.getConnection();
		} catch (SQLException ex) {
			log.error(LogMessage._NoDBConnection, ex);
			return 0110;
		}
		_assoc = assoc;
		_callingAE = _assoc.getAssociation().getCallingAET();
		int STATUS = 0;
		long maxDosePk = 0;
		log.info(_callingAE + ": MPPS Dealer: Starting Updating MPPS data");
		
		try{
			try{
				String selectSOP = "SELECT SopInstanceUID,performedProcedureStepStatus FROM MPPSInfo WHERE (SopInstanceUID=?)";
				SOPInstanceUID = m.getMPPSSOPInstance();
				log.debug(_callingAE + ": Form db Filler: SOP INSTANCE IS   : "	+ SOPInstanceUID);

				selectSOPps = con.prepareStatement(selectSOP);
				selectSOPps.setString(1, SOPInstanceUID);
				isThereAlreadySOPInstance = selectSOPps.executeQuery();
				if (isThereAlreadySOPInstance.next() == false) {
					log.error(_callingAE + ": SopInstance doesn't exist. Can't update.");
					return 0110;
				} else {
					log.debug(_callingAE + ": SOP Instance already in DataBase, updating");
					String status = isThereAlreadySOPInstance.getString("performedProcedureStepStatus");
					log.debug(_callingAE + ": Checking instance MPPS Status on Message: status is "+ m.getPerformedProcedureStepStatus());
					if ("IN PROGRESS".equalsIgnoreCase(status)) {
						log.debug(_callingAE + ": Correct Status");
					} else {
						log.error(_callingAE + ": Status Incorrect can't update Instances whose status is not IN PROGRESS");
						return 0110;
					}
				}
			}catch (Exception e) {
				STATUS = 0110;
			}finally{
				CloseableUtils.close(isThereAlreadySOPInstance);
			}
			
			// set permitted filed of MPPS INFO table
			log.debug(_callingAE + ": Starting update of MPPS INFO table");
			if (m.getPerformedProcedureStepStatus() != null) {
				log.info(_callingAE + ": MPPS Dealer: Updating MPPS " + SOPInstanceUID + " "+ m.getPerformedProcedureStepStatus());
				PreparedStatement updateStatement = null;
				try{					
					updateStatement = con.prepareStatement("UPDATE MPPSInfo SET performedProcedureStepStatus=? where SOPinstanceUID=?");
					updateStatement.setString(2, SOPInstanceUID);
					updateStatement.setString(1, m.getPerformedProcedureStepStatus());
					updateStatement.executeUpdate();
				}catch (SQLException e) {
					log.debug(_callingAE + ": Can't Update performedProcedureStepStatus...", e);
					STATUS = 0110;
				}finally{
					CloseableUtils.close(updateStatement);
				}
			}

			if (m.getPerformedProcedureStepDescription() != null) {
				PreparedStatement updateStatement = null;
				try{
					updateStatement = con.prepareStatement("UPDATE  MPPSInfo SET performedProcStepDescr=? where sopInstanceUID=?");
					updateStatement.setString(2, SOPInstanceUID);
					updateStatement.setString(1, m.getPerformedProcedureStepDescription());
					updateStatement.executeUpdate();
				} catch (SQLException e) {
					log.warn(_callingAE + ": Can't Update performedProcStepDescr...", e);
					STATUS = 0110;
				}finally{
					CloseableUtils.close(updateStatement);
				}
			}

			if (m.getPerformedProcedureTypeDescription() != null) {
				PreparedStatement updateStatement = null;
				try{					
					updateStatement = con.prepareStatement("UPDATE MPPSInfo SET performedProcTypeDescr=? where sopInstanceUID=?");
					updateStatement.setString(2, SOPInstanceUID);
					updateStatement.setString(1, m.getPerformedProcedureTypeDescription());
					updateStatement.executeUpdate();
				} catch (SQLException e) {
					log.debug(_callingAE + ": Can't Update performedProcTypeDescr...",e );
					STATUS = 0110;
				}finally{
					CloseableUtils.close(updateStatement);
				}
			}
			log.debug(_callingAE + ": STARTING: update of procedure code sequence");
			if (m.getProcedureCodeSequence() != null) {
				PreparedStatement updatecs = null;
				try{
					updatecs = con.prepareStatement("UPDATE MPPSInfo set procedureCodeSequenceFK=? where SOPInstanceUID=?");
					updatecs.setLong(1, storeCodeSequence(m.getProcedureCodeSequence()));
					updatecs.setString(2, SOPInstanceUID);
					updatecs.executeUpdate();
				} catch (SQLException e) {
					log.debug(_callingAE + ": Can't Update ProcedureCodeSequencefk...", e);
				}finally{
					CloseableUtils.close(updatecs);
				}
			}

			if (m.getPerformedProcedureStepEndDate() != null) {
				PreparedStatement updateStatement = null;
				try{
					updateStatement = con.prepareStatement("UPDATE  MPPSInfo SET performedProcedureStepEndDate=? where sopInstanceUID=?");
					updateStatement.setString(2, SOPInstanceUID);
					updateStatement.setDate(1, m.getPerformedProcedureStepEndDate());
					updateStatement.executeUpdate();
				} catch (SQLException e) {
					log.debug(_callingAE + ": Can't Update PerformedProcedureStepEndDate...",e );
					STATUS = 0110;
				}finally{
					CloseableUtils.close(updateStatement);
				}
			}

			if (m.getPerformedProcedureStepEndTime() != null) {
				PreparedStatement updateStatement = null;
				try{
					updateStatement = con.prepareStatement("UPDATE  MPPSInfo SET performedProcedureStepEndTime=? where sopInstanceUID=?");
					updateStatement.setString(2, SOPInstanceUID);
					updateStatement.setTime(1, m.getPerformedProcedureStepEndTime());
					updateStatement.executeUpdate();
				} catch (SQLException e) {
					log.debug(_callingAE + ": Can't Update PerformedProcedureStepEndTim...",e );
					STATUS = 0110;
				}finally{
					CloseableUtils.close(updateStatement);
				}
			}

			if (m.getCommentsOnPPS() != null) {
				PreparedStatement updateStatement = null;
				try{
					updateStatement = con.prepareStatement("UPDATE  MPPSInfo SET commentsOnPPS=? where sopInstanceUID=?");
					updateStatement.setString(2, SOPInstanceUID);
					updateStatement.setString(1, m.getCommentsOnPPS());
					updateStatement.executeUpdate();
				} catch (SQLException e) {
					log.debug(_callingAE + ": Can't Update CommentsOnPPS...", e);
					STATUS = 0110;
				}finally{
					CloseableUtils.close(updateStatement);
				}
			}

			if (m.getDiscontinuationReasonCodeSequence() != null) {
				PreparedStatement updatecs = null;
				try{
					updatecs = con.prepareStatement("UPDATE  MPPSInfo set procedureCodeSequenceFK=? where sopInstanceUID=?");
					updatecs.setLong(1, storeCodeSequence(m.getProcedureCodeSequence()));
					updatecs.setString(2, SOPInstanceUID);
					updatecs.executeUpdate();
				} catch (SQLException e) {
					log.debug(_callingAE + ": Can't Update ppsDiscontReasonCodeSeqFK...", e);
					STATUS = 0110;
				}finally{
					CloseableUtils.close(updatecs);
				}
			}
			log.debug(_callingAE + ": MPPS info is updated");
			log.debug(_callingAE + ": Starting Update of performedProtocolCodeSequence");
			if (m.getPerformedProtocolCodeSequence() != null) {
				log.debug(_callingAE + ": Delete relations in mppsinfoTOppCodeSequence");
				log.info(SOPInstanceUID);
				
				Statement deleteStatement = null;
				PreparedStatement insertRelationStatement = null;
				
				try{
					String qryDeeleteInfos = "DELETE FROM MPPSInfoToPPCodeSequence WHERE mppsInfoFK='"+ SOPInstanceUID + "'";
					deleteStatement = con.createStatement();
					deleteStatement.execute(qryDeeleteInfos);
				} catch (SQLException e) {
					log.error(_callingAE + ": Couldn't delete MPPSInfoToPPCodeSequence", e);
					STATUS = 0110;
				}finally{
					CloseableUtils.close(deleteStatement);
				}
				
				try{
					ArrayList<CodeSequence> temp = new ArrayList<CodeSequence>(Arrays.asList(m.getPerformedProtocolCodeSequence()));
					if ((temp != null) && (temp.size() > 0)){
						insertRelationStatement = con.prepareStatement("INSERT INTO MPPSInfoToPPCodeSequence(mppsInfoFK, performedProtCodeSeqFK) VALUES (?,?)");
						insertRelationStatement.setString(1, SOPInstanceUID);
						log.debug(_callingAE + ": Trying to insert codesequences from protocol code...");
						for (int i = 0; i < temp.size(); i++) {
							boolean salta = false;
							for (int k = 0; k < i; k++) {
								if (temp.get(k).equals(temp.get(i))) {
									salta = true;
								}
							}
							long fk = 0;
							if (!salta){
								fk = storeCodeSequence(temp.get(i));
								if (fk == 0) {
									continue;
								}
								insertRelationStatement.setLong(2, fk);
								insertRelationStatement.executeUpdate();
							}else{
								log.warn(_callingAE + ": Just inserted, don't need duplicate. " + SOPInstanceUID + "/" + fk);
							}
						}
					} // end if
				}catch (SQLException e) {
					log.error(_callingAE + ": error on  mppsinfotoppcodesequence", e);
					STATUS = 0110;
				}finally{
					CloseableUtils.close(insertRelationStatement);
				}
				log.debug(_callingAE + ": Update of performedProtocolCodeSequence COMPLETED");
			}

			if (m.getBillingProcedureStepCodeSequence() != null) {
				log.debug(_callingAE + ": Starting Update of Billing procedure Step code sequence");
				PreparedStatement deleteAStatement = null;
				PreparedStatement insertRelationStatement = null;
				
				try{
					deleteAStatement = con.prepareStatement("DELETE FROM  MPPSBillingProcStepCodeSeq where (mppsInfoFK=?)");
					deleteAStatement.setString(1, SOPInstanceUID);
					deleteAStatement.executeUpdate();
				}catch (SQLException e) {
					log.debug(_callingAE + ": error on MPPSBillingProcStepCodeSeq", e);
					STATUS = 0110;
				}finally{
					CloseableUtils.close(deleteAStatement);
				}

				try{
					CodeSequence[] temp = m.getBillingProcedureStepCodeSequence();
					log.debug(temp.length);
					insertRelationStatement = con.prepareStatement("INSERT into MPPSBillingProcStepCodeSeq (billingProcStepCodeSeqFK,mppsInfoFK)VALUES (?,?)");
					insertRelationStatement.setString(2, SOPInstanceUID);
					for (int i = 0; i < temp.length; i++) {
						long fk = storeCodeSequence(temp[i]);
						insertRelationStatement.setLong(1, fk);
						insertRelationStatement.executeUpdate();
					}
				}catch (SQLException e) {
					log.debug(_callingAE + ": error on mppsinfotobillingprocedurestepcodesequence", e);
					STATUS = 0110;
				}finally{
					CloseableUtils.close(insertRelationStatement);
				}
				log.debug(_callingAE + ": BILLING PROCEDURE STEP CODE SEQUENCE: COMPLETED");
			}

			log.debug(_callingAE + ": UPDATING RADIATION DOSE TABLE");

			PreparedStatement deleteBStatement = null;
			try{
				deleteBStatement = con.prepareStatement("DELETE FROM  MPPSRadiationDoses where (mppsInfoFK=?)");
				deleteBStatement.setString(1, SOPInstanceUID);
				deleteBStatement.executeUpdate();
			} catch (SQLException e) {
				log.debug(_callingAE + ": error on mppsinfotobillingprocedurestepcodesequence",e);
				STATUS = 0110;
			}finally{
				CloseableUtils.close(deleteBStatement);
			}

			PreparedStatement insertRadiation = null;
			ResultSet maxDosePkRS = null;
			PreparedStatement findDosePk = null;
			try{
				insertRadiation = con.prepareStatement("INSERT into MPPSRadiationDoses (anatomicStructureCodeSeqFK,totalTimeOfFluoroscopy,totalNumberOfExposures,distanceSourceToDetector,distanceSourceToEntrance,entranceDose,entranceDoseMGY,exposedArea,imageAreaDoseProduct,commentsOnRadiationDose,mppsInfoFK) VALUES (?,?,?,?,?,?,?,?,?,?,?)");

				if (m.getAnatomicsStructureSpaceOrRegionCodeSequence() != null) {
					insertRadiation.setLong(1, storeCodeSequence(m.getAnatomicsStructureSpaceOrRegionCodeSequence()));
				} else {
					insertRadiation.setLong(1, 0);
				}
				if (m.getTotalTimeOfFluoroscopy() == null) {
					insertRadiation.setNull(2, Types.INTEGER);
				} else {
					insertRadiation.setInt(2, Integer.parseInt(m.getTotalTimeOfFluoroscopy()));
				}
				if (m.getTotalNumberOfExposures() == null) {
					insertRadiation.setNull(3, Types.INTEGER);
				} else {
					insertRadiation.setInt(3, Integer.parseInt(m.getTotalNumberOfExposures()));
				}
				if (m.getDistanceSourceToDetector() == null) {
					insertRadiation.setNull(4, Types.DOUBLE);
				} else {
					insertRadiation.setDouble(4, Double.parseDouble(m.getDistanceSourceToDetector()));
				}
				if (m.getDistanceSourceToEntrance() == null) {
					insertRadiation.setNull(5, Types.DOUBLE);
				} else {
					insertRadiation.setDouble(5, Double.parseDouble(m.getDistanceSourceToEntrance()));
				}
				if (m.getEntranceDose() == null) {
					insertRadiation.setNull(6, Types.INTEGER);
				} else {
					insertRadiation.setInt(6, Integer.parseInt(m.getEntranceDose()));
				}
				if (m.getEntranceDoseMGY() == null) {
					insertRadiation.setNull(7, Types.DOUBLE);
				} else {
					insertRadiation.setDouble(7, Double.parseDouble(m.getEntranceDoseMGY()));
				}
				if (m.getExposedArea() == null) {
					insertRadiation.setNull(8, Types.INTEGER);
				} else {
					insertRadiation.setInt(8, Integer.parseInt(m.getExposedArea()));
				}
				if (m.getImageAreaDoseProduct() == null) {
					insertRadiation.setNull(9, Types.DOUBLE);
				} else {
					insertRadiation.setDouble(9, Double.parseDouble(m.getImageAreaDoseProduct()));
				}
				insertRadiation.setString(10, m.getCommentsOnRadiationDose());
				insertRadiation.setString(11, SOPInstanceUID);

				insertRadiation.executeUpdate();
				// genera l'ultima pk
				String find = "SELECT max(pk) FROM MPPSRadiationDoses WHERE mppsInfoFK=?";
				findDosePk = con.prepareStatement(find);
				findDosePk.setString(1, SOPInstanceUID);

				maxDosePk = 000000;

				maxDosePkRS = findDosePk.executeQuery();
				if (maxDosePkRS.next()) {
					maxDosePk = maxDosePkRS.getLong(1);
				} // else
			}catch (SQLException e) {
				log.debug(_callingAE + ": error on mppsinfotobillingprocedurestepcodesequence", e);
				STATUS = 0110;
			}finally{
				CloseableUtils.close(maxDosePkRS);
				CloseableUtils.close(insertRadiation);
				CloseableUtils.close(findDosePk);
			}

			log.debug(_callingAE + ": EXPOSURE DOSE TABLE WRITTEN");

			// ----------------------------mppsFilmConsumptionTable---------------------------------------------
			if (m.getFilmConsumptionSequence() != null) {
				log.debug(_callingAE + ": Starting Update of Film Consumption table");
				PreparedStatement deleteCStatement = null;
				PreparedStatement insertFCS = null;
				
				try{
					deleteCStatement = con.prepareStatement("DELETE FROM  MPPSFilmConsumptionSequences where (mppsInfoFK=?)");
					deleteCStatement.setString(1, SOPInstanceUID);

					deleteCStatement.executeUpdate();

					MPPSItem.FilmConsumptionSequence[] tempo = m.getFilmConsumptionSequence();
					insertFCS = con.prepareStatement("insert into MPPSFilmConsumptionSequences (filmSizeID,numberOfFilms,mediumType,mppsInfoFK) VALUES (?,?,?,?)");

					for (int i = 0; i < tempo.length; i++) {
						insertFCS.setString(1, tempo[i].getFilmSizeID());
						if (tempo[i].getNumberOfFilms() == null) {
							insertFCS.setNull(2, Types.BIGINT);
						} else {
							insertFCS.setLong(2, Long.parseLong(tempo[i].getNumberOfFilms()));
						}
						insertFCS.setString(3, tempo[i].getMediumType());
						insertFCS.setString(4, SOPInstanceUID);
						insertFCS.executeUpdate();
					} // end for
				}catch (SQLException e) {
					log.debug(_callingAE + ": error on mppsinfotobillingprocedurestepcodesequence", e);
					STATUS = 0110;
				}finally{
					CloseableUtils.close(deleteCStatement);
					CloseableUtils.close(insertFCS);
				}

				log.debug(_callingAE + ": FilmConsumptionSequence Completed");
			} // end if

			if (m.getExposureDoseSequence() != null) {
				log.debug(_callingAE + ": MPPS Dealer: Dealing with Exposure Dose Sequence");
				log.debug(_callingAE + ": Starting Update of Exposure dose sequence");

				PreparedStatement deleteDStatement = null;
				PreparedStatement insertExposure = null;
				
				try{
					deleteDStatement = con.prepareStatement("DELETE FROM  MPPSExposureDoseSequences where (mppsRadiationDoseFK=?)");
					deleteDStatement.setLong(1, maxDosePk);
					deleteDStatement.executeUpdate();

					MPPSItem.ExposureDoseSequence[] tempu = m.getExposureDoseSequence();
					insertExposure = con.prepareStatement("insert into MPPSExposureDoseSequences (radiationMode,kvp,xRayTubeCurrent,exposureTime,filterType,filterMaterial,MPPSRadiationDoseFK) VALUES (?,?,?,?,?,?,?)");

					for (int i = 0; i < tempu.length; i++) {
						insertExposure.setString(1, tempu[i].getRadiationMode());
						insertExposure.setString(2, tempu[i].getKvp());
						insertExposure.setString(3, tempu[i].getxRayTubeCurrent());
						insertExposure.setString(4, tempu[i].getExposureTime());
						insertExposure.setString(5, tempu[i].getFilterType());
						insertExposure.setString(6, tempu[i].getFilterMaterial());
						insertExposure.setLong(7, maxDosePk);

						insertExposure.executeUpdate();
					} // end for
				}catch(SQLException e) {
					log.debug(_callingAE + ": error on mppsinfotobillingprocedurestepcodesequence", e);
					STATUS = 0110;
				}finally{
					CloseableUtils.close(deleteDStatement);
					CloseableUtils.close(insertExposure);
				}

				log.debug(_callingAE + ": Exposure Dose Sequence Completed");
			} // end if
			if (m.getBillingItemSequence() != null) {
				log.debug(_callingAE + ": Starting Update of billing items and supplies");
				PreparedStatement findBillingPk = null;
				PreparedStatement deleteFStatement = null;
				PreparedStatement deleteEStatement = null;
				PreparedStatement insertBillingItem = null;
				PreparedStatement insertQuantityItem = null;
				ResultSet maxBillingPkRS = null;
				
				try{
					String findQ = "SELECT max(pk) FROM MPPSBillingSeq WHERE mppsInfoFK=?";
					findBillingPk = con.prepareStatement(findQ);
					findBillingPk.setString(1, SOPInstanceUID);
					long maxBillingPk = 000000;
					maxBillingPkRS = findBillingPk.executeQuery();
					if (maxBillingPkRS.next()) {
						maxBillingPk = maxBillingPkRS.getLong(1);
					} else {
						log.error(_callingAE + ": Can't get max pk");
					}
					log.debug(_callingAE + ": max billing pk:  " + maxBillingPk);
					deleteFStatement = con.prepareStatement("DELETE FROM MPPSQuantitySequences where (mppsBillingSequenceFK=?)");
					deleteFStatement.setLong(1, maxBillingPk);
					deleteFStatement.executeUpdate();
					deleteEStatement = con.prepareStatement("DELETE FROM MPPSBillingSeq where (mppsInfoFK=?)");
					deleteEStatement.setString(1, SOPInstanceUID);
					deleteEStatement.executeUpdate();
					log.debug(_callingAE + ": Billing Item SEQUENCE");
					MPPSItem.BillingItemSequence[] temps = m.getBillingItemSequence();

					insertBillingItem = con.prepareStatement("insert into MPPSBillingSeq (billingItemCodeSequenceFK,mppsInfoFK) VALUES (?,?)");
					for (int i = 0; i < temps.length; i++) {
						if (temps[i].getBillingItemCode() != null) {
							insertBillingItem.setLong(1, storeCodeSequence(temps[i].getBillingItemCode()));
						} else {
							log.debug(_callingAE + "Code item not recognized");
							insertBillingItem.setLong(1, 0);
						}
						insertBillingItem.setString(2, SOPInstanceUID);
						insertBillingItem.executeUpdate();
						// genera l'ultima pk----------------------------------
						findBillingPk = null;
						findQ = "SELECT max(pk) FROM MPPSBillingSeq WHERE mppsInfoFK=?";
						findBillingPk = con.prepareStatement(findQ);
						findBillingPk.setString(1, SOPInstanceUID);
						maxBillingPk = 000000;
						maxBillingPkRS = findBillingPk.executeQuery();
						if (maxBillingPkRS.next()) {
							maxBillingPk = maxBillingPkRS.getLong(1);
						} else {
							log.error(_callingAE + "Can't get max PK");
						}
						insertQuantityItem = con.prepareStatement("insert into MPPSQuantitySequences (quantity,measuringUnitsCodeSequenceFK,mppsBillingSequenceFK) VALUES (?,?,?)");
						insertQuantityItem.setDouble(1, Double.parseDouble(temps[i].getQuantity()));
						if (temps[i].getMeasuringUnitsSequence() != null) {
							insertQuantityItem.setLong(2, storeCodeSequence(temps[i].getMeasuringUnitsSequence()));
						} else {
							insertQuantityItem.setLong(2, 0);
						}
						insertQuantityItem.setLong(3, maxBillingPk);
						insertQuantityItem.executeUpdate();
					} // end for mpps supplies
				} catch (SQLException e) {
					log.debug(_callingAE + "error on mppsinfotobillingprocedurestepcodesequence",e );
					STATUS = 0110;
				}finally{
					CloseableUtils.close(findBillingPk);
					CloseableUtils.close(deleteFStatement);
					CloseableUtils.close(deleteEStatement);
					CloseableUtils.close(insertBillingItem);
					CloseableUtils.close(insertQuantityItem);
					CloseableUtils.close(maxBillingPkRS);
				}
				
				log.debug(_callingAE + "Billing table written");
			} // end if
			
			if (m.getPerformedSeriesSequence() != null) {
				if (m.getPerformedSeriesSequence().length != 0) {
					log.debug(_callingAE + "Analizying performed series..........");
					PreparedStatement selectSched = null;
					PreparedStatement delPhRel = null;
					PreparedStatement delOpRel = null;
					PreparedStatement delSched = null;
					ResultSet rs = null;
					
					try{
						selectSched = con.prepareStatement("SELECT * FROM MPPSPerformedSeriesSequences WHERE mppsInfoFK=?");
						selectSched.setString(1, SOPInstanceUID);
						rs = selectSched.executeQuery();
						String pssPk = "a";
//						PreparedStatement selPh = con.prepareStatement("SELECT * FROM PhysiciansToPss WHERE mppsPerformedSeriesSequenceFK=?");
						while (rs.next()) {
							pssPk = rs.getString("pk");
							delPhRel = con.prepareStatement("DELETE FROM PhysiciansToPss WHERE mppsPerformedSeriesSequenceFK=?");
							delPhRel.setString(1, pssPk);
							delPhRel.executeUpdate();
							delOpRel = con.prepareStatement("DELETE FROM OperatorsToPss WHERE mppsPerformedSeriesSequenceFK=?");
							delOpRel.setString(1, pssPk);
							delOpRel.executeUpdate();
						}
						delSched = con.prepareStatement("DELETE FROM MPPSPerformedSeriesSequences WHERE mppsInfoFK=?");
						delSched.setString(1, SOPInstanceUID);
						delSched.executeUpdate(); /**/
					}catch (SQLException e) {
						log.error(_callingAE + "problems updating Scheduled Step Attribute Sequence table, error:", e);
					}finally{
						CloseableUtils.close(rs);
						CloseableUtils.close(delSched);
						CloseableUtils.close(delOpRel);
						CloseableUtils.close(delPhRel);
						CloseableUtils.close(selectSched);
					}

					// ----------storing Performed Series Sequence
					PreparedStatement insertSeries = null;
					PreparedStatement findSeriesPk = null;
					PreparedStatement insertPhtoPPSPS = null;
					PreparedStatement insertOptoPPSPS = null;
					ResultSet maxSpkRS = null;
					
					try{
						MPPSItem.PerformedSeriesSequence[] tempp = m.getPerformedSeriesSequence();
						 insertSeries = con.prepareStatement("INSERT into MPPSPerformedSeriesSequences (protocolName,retrieveAETitle,seriesDescription,seriesFK,mppsInfoFK) values (?,?,?,?,?)");
						for(int i = 0; i < tempp.length; i++) {
							insertSeries.setString(1, tempp[i].getProtocolName());
							insertSeries.setString(2, tempp[i].getRetrieveAETitle());
							insertSeries.setString(3, tempp[i].getseriesDescription());
							insertSeries.setString(4, tempp[i].getSeriesInstanceUID());
							insertSeries.setString(5, SOPInstanceUID);
							insertSeries.executeUpdate();							
							String findS = "SELECT max(pk) FROM MPPSPerformedSeriesSequences WHERE mppsInfoFK=?";
							findSeriesPk = con.prepareStatement(findS);
							findSeriesPk.setString(1, SOPInstanceUID);
							long maxSpk = 000000;
							maxSpkRS = findSeriesPk.executeQuery();
							if (maxSpkRS.next()) {
								maxSpk = maxSpkRS.getLong(1);
							} // else {log.debug("non ho trovato pk massima");}
							String[] Pnames = tempp[i].getPerformingPhysicianName();
							if (Pnames != null) {
								List<PersonalName> Pns = convertStringToPersonalName(Pnames);
								for (int j = 0; j < Pns.size(); j++) {
									long pk = storePersonnel((PersonalName) Pns.get(j));
									String InsertPhtoPPS = "INSERT INTO PhysiciansToPSS (performingPhysiciansNameFK,mppsPerformedSeriesSequenceFK) VALUES (?,?)";
									insertPhtoPPSPS = con.prepareStatement(InsertPhtoPPS);

									insertPhtoPPSPS.setLong(1, pk);
									insertPhtoPPSPS.setLong(2, maxSpk);
									insertPhtoPPSPS.executeUpdate();
								}
							}
							String[] Onames = tempp[i].getOperatorsName();
							if (Onames != null) {
								List<PersonalName> Ons = convertStringToPersonalName(Onames);
								for (int j = 0; j < Ons.size(); j++) {
									long pk = storePersonnel((PersonalName) Ons.get(j));
									String InsertOptoPPS = "INSERT INTO OperatorsToPSS (operatorFK,mppsPerformedSeriesSequenceFK) VALUES (?,?)";
									insertOptoPPSPS = con.prepareStatement(InsertOptoPPS);
									insertOptoPPSPS.setLong(1, pk);
									insertOptoPPSPS.setLong(2, maxSpk);
									insertOptoPPSPS.executeUpdate();
								}
							}
						}
					}catch (SQLException e){
						log.error(_callingAE, e);
						STATUS = 0110;
					}finally{
						CloseableUtils.close(maxSpkRS);
						CloseableUtils.close(insertOptoPPSPS);
						CloseableUtils.close(insertPhtoPPSPS);
						CloseableUtils.close(findSeriesPk);
						CloseableUtils.close(insertSeries);
					}
				}
			} // end if performed Series
			return STATUS;
		}finally{
			CloseableUtils.close(insertMPPStoBillingCodSequencePS);	
			CloseableUtils.close(selectInsertedCodSeqPS);	
			CloseableUtils.close(insertMPPStoProtocolRelationPS);	
			CloseableUtils.close(insertCodSeqPS);	
			CloseableUtils.close(selectSOPps);	
			CloseableUtils.close(con);	
		}
	}

	// Private methods:
	private long storeCodeSequence(CodeSequence cs) {
		Connection con = null;
		PreparedStatement selectInsertedCodSeqPS = null;
		PreparedStatement insertCodSeqPS = null;
		ResultSet rs = null;
		
		int res = 0; // To know how many rows were affected (should be 1, can be
		long insId = 0; // The Id of the inserted row.
		if ((cs == null) || ((cs.getCodeValue() == null)	&& (cs.getCodingSchemeDesignator() == null) && (cs.getCodingSchemeVersion() == null))) {
			return insId; // ... Return the default one!!!
		}		
		
		try{
			// Check whether the CodSeq is already in the DB, stored by a previous study, for instance...
			con = dataSource.getConnection();
			selectInsertedCodSeqPS = con.prepareStatement(selectInsertedCodSeq);
			selectInsertedCodSeqPS.setString(1, cs.getCodeValue());
			selectInsertedCodSeqPS.setString(2, cs.getCodingSchemeDesignator());
			selectInsertedCodSeqPS.setString(3, cs.getCodingSchemeVersion());
			rs = selectInsertedCodSeqPS.executeQuery();
			if (rs.next()) {
				// log.debug("Already Stored: "+rs.getLong(1));
				// log.debug("If already stored gives zero we can store the new codesequence");
				insId = rs.getLong(1); // The CodeSequence was already present!
										// Otherwise, go on and insert it!
				if (insId != 0) {
					return insId; // if the returned row is the first one,
									// assume the current codeSeq is to be added
									// and go on
				}
			}
			insertCodSeqPS = con.prepareStatement(insertCodSeq);
			insertCodSeqPS.setString(1, cs.getCodeValue());
			insertCodSeqPS.setString(2, cs.getCodingSchemeDesignator());
			insertCodSeqPS.setString(3, cs.getCodingSchemeVersion());
			insertCodSeqPS.setString(4, cs.getCodeMeaning());
			res = insertCodSeqPS.executeUpdate();
			log.debug(_callingAE + ": Inserted a CodeSequence: ");
			if (res == 1) {
				rs = selectInsertedCodSeqPS.executeQuery(); // The PreparedStatement is ready since the previous call!
				rs.next(); // Exactly one record should be in the ResultSet
				insId = rs.getLong(1); // Get the value of the first and only
										// column
				log.debug(_callingAE + ": Retrieved CodeSequence " + insId);
			} // end if
			log.debug(_callingAE + ": MPPS Dealer: Just operated with Code Sequence " + insId);
		} catch (SQLException sex) {
			log.error(_callingAE , sex);
		} catch (Exception ex) {
			log.error(_callingAE, ex);
		}finally{
			CloseableUtils.close(rs);
			CloseableUtils.close(insertCodSeqPS);
			CloseableUtils.close(selectInsertedCodSeqPS);
			CloseableUtils.close(con);
		}
		return insId; // It returns the id of the inserted row.
	} // end storeCodeSequence()

	private List<PersonalName> convertStringToPersonalName(String[] names) {
		int nextCaret = 0;
		List<PersonalName> pnArray = new ArrayList<PersonalName>(1);
		PersonalName pn = null;
		if (names != null) {
			for (int k = names.length - 1; k >= 0; k--) {
				pn = new PersonalName();
				// /if((names[k]==null)||(names[k].equals("")))
				// pn.setLastName(DicomConstants.JOHN_DOE);
				// else{
				if (names[k].indexOf('^') != -1) {
					// At least the first two fields are specified
					pn.setLastName(names[k].substring(0, names[k].indexOf('^')));
					nextCaret = names[k].indexOf('^', names[k].indexOf('^') + 1); // this is the position of the second ^
					if (nextCaret != -1) {
						// At least the first three fields are specified
						pn.setFirstName(names[k].substring(names[k].indexOf('^') + 1, nextCaret));
						if (names[k].indexOf('^', nextCaret + 1) != -1) {
							// At least the first four fields are specified
							pn.setMiddleName(names[k].substring(nextCaret + 1,names[k].indexOf('^', nextCaret + 1)));
							if (names[k].lastIndexOf('^') > names[k].indexOf('^', nextCaret + 1)) {
								pn.setPrefix(names[k].substring(names[k].indexOf('^', nextCaret + 1) + 1,names[k].lastIndexOf('^')));
								pn.setSuffix(names[k].substring(names[k].lastIndexOf('^')));
							} else {
								pn.setPrefix(names[k].substring(names[k].indexOf('^', nextCaret + 1) + 1));
								}
						} else {
							pn.setMiddleName(names[k].substring(nextCaret + 1));
						}
					} else {
						// Last and First Name specified
						pn.setFirstName(names[k].substring(names[k].indexOf('^') + 1));
					}
				} else {
					// Just one field specified
					pn.setLastName(names[k]);
				}
				pnArray.add(pn);
			}
		} // end if
		return pnArray;
	}

	@SuppressWarnings("unused")
	private void justChangeState(MPPSItem m) {
		Connection con = null;
		PreparedStatement changeState = null;
		
		try{
			con = dataSource.getConnection();
			changeState = con.prepareStatement("UPDATE MPPSInfo set performedProcedureStepStatus=? WHERE sopInstanceUID=?");
			changeState.setString(1, m.getPerformedProcedureStepStatus());
			changeState.setString(2, m.getMPPSSOPInstance());
			changeState.executeUpdate();
		}catch (SQLException e){
			log.error(_callingAE, e);
		}finally{
			CloseableUtils.close(changeState);
			CloseableUtils.close(con);
		}
	}

	private long storePersonnel(PersonalName pn) {
		Connection con = null;
		PreparedStatement personnelPresentPS = null;
		PreparedStatement insertPersonnelPS = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		
		long pk = 0;
		
		String personnelPresent = "SELECT pk FROM Personnel WHERE lastName=? AND firstName=? AND (middleName=? OR middleName IS NULL) AND (prefix=? OR prefix IS NULL) AND (suffix=? OR suffix IS NULL)";
		String insertPersonnel = "INSERT INTO Personnel(lastName, firstName, middleName, prefix, suffix) VALUES(?, ?, ?, ?, ?)";
		try{
			con = dataSource.getConnection();
			personnelPresentPS = con.prepareStatement(personnelPresent);
			insertPersonnelPS = con.prepareStatement(insertPersonnel);
			String ln = pn.getLastName();
			String fn = pn.getFirstName();
			String mn = pn.getMiddleName();
			String pr = pn.getPrefix();
			String su = pn.getSuffix();
			personnelPresentPS.setString(1, ln); // First I want to check
													// whether she's already
													// present
			personnelPresentPS.setString(2, fn);
			personnelPresentPS.setString(3, mn);
			personnelPresentPS.setString(4, pr);
			personnelPresentPS.setString(5, su);
			rs = personnelPresentPS.executeQuery();
			if (rs.next()) {
				pk = rs.getLong(1);
				log.debug("Personnel present");
			} else {
				// Otherwise insert her ...
				insertPersonnelPS.setString(1, ln);
				insertPersonnelPS.setString(2, fn);
				insertPersonnelPS.setString(3, mn);
				insertPersonnelPS.setString(4, pr);
				insertPersonnelPS.setString(5, su);
				insertPersonnelPS.executeUpdate(); // Insert the employee
				log.debug("New personnel inserted");
				// FIXME possible error...verify!
				rs2 = personnelPresentPS.executeQuery(); // Retrieve her id
				rs2.next();
				pk = rs2.getLong(1);
			} // end else
		} catch (SQLException sex) {
			log.error(_callingAE + ": while adding personnel", sex);
		} catch (Exception ex) {
			log.error(_callingAE + ": while adding personnel", ex);
		}finally{
			CloseableUtils.close(rs);
			CloseableUtils.close(rs2);
			CloseableUtils.close(personnelPresentPS);
			CloseableUtils.close(insertPersonnelPS);
			CloseableUtils.close(con);
		}
		return pk;
	} // end storePersonnel()
}