/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.nodes;

import it.units.htl.dpacs.core.ServerRemote;
import it.units.htl.dpacs.dao.DicomQueryDealerBean;
import it.units.htl.maps.Equipment;
import it.units.htl.maps.KnownNodes;
import it.units.htl.maps.PhysicalMedia;
import it.units.htl.maps.PhysicalMediaHome;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.ui.messaging.MessageManager;

import java.rmi.RemoteException;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;

public class KnownNodesBackBean {
	// BAcking bean for insert, edit and delete a single node
	private UIData nodesTable;
	private KnownNodes node;
	private Equipment equipment;
	// private DataContext dataContext;
	private String status;
	private int pagina = 1;
	private Long storageFK = 0L;
	private Long equipmentFK = 0L;
	private Equipment equipToSave = new Equipment();
	private KnownNodes nodeToSave = new KnownNodes();

	public KnownNodes getNodeToSave() {
		return nodeToSave;
	}

	public void setNodeToSave(KnownNodes nodeToSave) {
		this.nodeToSave = nodeToSave;
	}

	public Equipment getEquipToSave() {
		return equipToSave;
	}

	public void setEquipToSave(Equipment equipToSave) {
		this.equipToSave = equipToSave;
	}

	final Log log = LogFactory.getLog(DicomQueryDealerBean.class);
	public List<KnownNodes> KnownNodesList = null;

	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	private PhysicalMedia physicalMedia;

	public KnownNodesBackBean() {
		equipment = new Equipment();
		node = new KnownNodes();
	}

	@SuppressWarnings("unchecked")
    public List<KnownNodes> getList() {
		if (KnownNodesList == null) {
			Session s = SessionManager.getInstance().openSession();
			DetachedCriteria query = DetachedCriteria.forClass(KnownNodes.class);
			KnownNodesList = query.getExecutableCriteria(s).list();
			s.close();
		}
		return KnownNodesList;
	}

	public void editNode(ActionEvent AE) {
		node = (KnownNodes) nodesTable.getRowData();
		physicalMedia = node.getPhysicalMedia();
		storageFK = physicalMedia.getPk();
		equipment = node.getEquipment();
		equipmentFK = equipment.getPk();
	}

	public void createNode(ActionEvent AE) {
		equipment = new Equipment();
		node = new KnownNodes();
	}

	public void deleteNode(ActionEvent AE) {
		Session s = SessionManager.getInstance().openSession();
		Transaction tx = s.beginTransaction();
		try {
			s.delete(((KnownNodes) nodesTable.getRowData()));
			tx.commit();
			KnownNodesList = null;
			reloadNodes();
		} catch (Exception e) {
			tx.rollback();
			log.warn("Unable to delete this node: " + ((KnownNodes) nodesTable.getRowData()).getAeTitle(), e);
			MessageManager.getInstance().setMessage("Unable to delete the dicom node! " + e.getMessage());
		}
	}

	public void updateNode(ActionEvent AE) {
		Session s = SessionManager.getInstance().openSession();
		s.beginTransaction();
		try {
			PhysicalMediaHome phome = new PhysicalMediaHome();
			node.setPhysicalMedia(phome.findById(storageFK));
			if (node.getTransferSyntaxUid() == null || node.getTransferSyntaxUid().equals("null")) {
				node.setTransferSyntaxUid(null);
			}
			// PACSWEB-67
			if (node.getPrefCallingAet() == null || node.getPrefCallingAet().length() == 0) {
				log.debug("Hard-coded default value for prefCallingAet: 'O3-DPACS'");
				node.setPrefCallingAet("O3-DPACS");
			}
			s.update(node.getEquipment());
			node.setAeTitle(node.getAeTitle().trim());

			// if wadoUrl field is empty, force a null value in DB
			if (node.getWadoURL() == null || node.getWadoURL().trim().length() == 0) {
				node.setWadoURL(null);
			}

			s.update(node);
			s.getTransaction().commit();
			reloadNodes();
		} catch (Exception e) {
			MessageManager.getInstance().setMessage("Unable to add this new node: " + node.getAeTitle() + " due to: " + e.getCause());
			log.error("Couldn't update knownNode:", e);
			s.getTransaction().rollback();
		}
	}

	void reloadNodes() {
		try {
			getConnection().reloadSettings();
			setStatus("success");
		} catch (RemoteException e) {
			log.error("Unable to reload configuration automatically", e);
			setStatus("Failed");
		}
		if (!"success".equals(status)) {
			MessageManager.getInstance().setMessage("Unable to reload configuration of knownnodes...restart PACS!");
		} else {
			MessageManager.getInstance().setMessage("Node configuration reloaded properly!");
		}
	}

	public KnownNodes getNode() {
		return node;
	}

	public void setNode(KnownNodes node) {
		this.node = node;
	}

	public UIData getNodesTable() {
		return nodesTable;
	}

	public void setNodesTable(UIData nodesTable) {
		this.nodesTable = nodesTable;
	}

	public Long getEquipmentFK() {
		return equipmentFK;
	}

	public void setEquipmentFK(Long equipmentFK) {
		this.equipmentFK = equipmentFK;
	}

	public Long getStorageFK() {
		return storageFK;
	}

	public void setStorageFK(Long storageFK) {
		this.storageFK = storageFK;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Equipment getEquipment() {
		return equipment;
	}

	public void setEquipment(Equipment equipment) {
		this.equipment = equipment;
	}

	protected ServerRemote getConnection() {
		ServerRemote serverRemote = null;
		try {
//		    serverRemote = InitialContext.doLookup("o3-dpacs/ServerBean/remote");
		    serverRemote = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/ServerBean!it.units.htl.dpacs.core.ServerRemote");
		} catch (NamingException e) {
			log.error("", e);
		} 
		return serverRemote;
	}
}