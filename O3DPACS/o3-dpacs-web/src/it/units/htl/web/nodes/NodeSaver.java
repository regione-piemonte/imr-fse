/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.nodes;

import javax.faces.event.ActionEvent;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.units.htl.maps.Equipment;
import it.units.htl.maps.KnownNodes;
import it.units.htl.maps.PhysicalMedia;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.users.JSFUtil;
import it.units.htl.web.users.UserBean;

public class NodeSaver {
	private Log log = LogFactory.getLog(NodeSaver.class);
	
	private PhysicalMedia phi = new PhysicalMedia();
	public Equipment equipToSave = new Equipment();
	public KnownNodes nodeToSave = new KnownNodes();

	public void saveNode(ActionEvent AE){
		Session s = null;
		Transaction tx = null;
		boolean added = false;
		UserBean userBean = (UserBean) JSFUtil.getManagedObject("userBean");
		String user = userBean.getFirstName().concat(" " + userBean.getLastName());
		equipToSave.setLastCalibratedBy(user);
		java.util.Date date = new java.util.Date();
		equipToSave.setDateOfLastCalibration(date);
		equipToSave.setTimeOfLastCalibration(date);
		try{		
			s = SessionManager.getInstance().openSession();
			tx = s.getTransaction();
			tx.begin();
			s.save(equipToSave);		
			if (nodeToSave.getTransferSyntaxUid() == null
					|| nodeToSave.getTransferSyntaxUid().equals("null")) {
				nodeToSave.setTransferSyntaxUid(null);
			}
			// PACSWEB-67
			if (nodeToSave.getPrefCallingAet() == null
					|| nodeToSave.getPrefCallingAet().length() == 0) {
				nodeToSave.setPrefCallingAet("O3-DPACS");
			}
			nodeToSave.setPhysicalMedia(phi);
			nodeToSave.setEquipment(equipToSave);		
			nodeToSave.setIsEnabled(true);
			nodeToSave.setForwardEndConfirmation('N');
			s.save(nodeToSave);
			tx.commit();
			added = true;
		}catch (Exception e) {
			MessageManager.getInstance().setMessage("Unable to add this new node: " + nodeToSave.getAeTitle() + " due to: " + e.getCause());
			log.error("Unable to add this new node: " + nodeToSave.getAeTitle(), e);
			tx.rollback();
		}
		if(added){
			((KnownNodesBackBean)JSFUtil.getManagedObject("knownNodesBackBean")).KnownNodesList = null;
			((KnownNodesBackBean)JSFUtil.getManagedObject("knownNodesBackBean")).reloadNodes();
			MessageManager.getInstance().setMessage("Node saved correctly");
		}
	}
	
	public PhysicalMedia getPhysicalToSave() {
		return phi;
	}
	public void setPhysicalToSave(PhysicalMedia phi) {
		this.phi = phi;
	}
	public KnownNodes getNodeToSave() {
		return nodeToSave;
	}
	public void setNodeToSave(KnownNodes nodeTosave) {
		this.nodeToSave = nodeTosave;
	}
	public Equipment getEquipToSave() {
		return equipToSave;
	}
	public void setEquipToSave(Equipment equipToSave) {
		this.equipToSave = equipToSave;
	}
	public void setPhysicalMedia(PhysicalMedia phi){
		nodeToSave.setPhysicalMedia(phi);
	}
	public PhysicalMedia getPhysicalMedia(){
		return nodeToSave.getPhysicalMedia();
	}	
}
