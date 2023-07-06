/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;

import it.units.htl.atna.AuditLogService;
import it.units.htl.maps.KnownNodesHome;
import it.units.htl.maps.NodesForwardMappingId;
import it.units.htl.maps.Patients;
import it.units.htl.maps.Series;
import it.units.htl.maps.Studies;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.users.JSFUtil;
import it.units.htl.web.users.UserBean;
import it.units.htl.web.utils.MergeMaker;

import org.ajax4jsf.component.html.HtmlAjaxCommandButton;
import org.ajax4jsf.context.AjaxContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che2.audit.message.InstancesAccessedMessage;
import org.dcm4che2.audit.message.ParticipantObjectDescription;
import org.dcm4che2.audit.message.PatientRecordMessage;
import org.dcm4che2.audit.message.AuditEvent.ActionCode;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

import org.richfaces.component.html.HtmlTreeNode;
import org.richfaces.event.DropEvent;
import org.richfaces.event.NodeSelectedEvent;

import com.sun.corba.se.spi.orbutil.fsm.Action;

public class TreeManager {
	private Object currentSelection = null;
	private Boolean disabled = true;
	private static Log log = LogFactory.getLog(TreeManager.class);
	private Studies study;
	private Patients patient;
	private Series serie;
	private String type;
	private Exception lastException;

	public Exception getLastException() {
		return lastException;
	}

	public void setLastException(Exception lastException) {
		this.lastException = lastException;
	}

	/**
	 * Ensure that the the following patient fields, if not null, are in
	 * upper-case:
	 * <ul>
	 * <li>patient id</li>
	 * <li>id issuer</li>
	 * <li>firstname</li>
	 * <li>lastname</li>
	 * <li>middlename</li>
	 * <li>prefix</li>
	 * <li>suffix</li>
	 * </ul>
	 * 
	 * @param p
	 */
	private void toUpperCase(Patients p) {
		if (p.getPatientId() != null)
			p.setPatientId(p.getPatientId().toUpperCase());
		if (p.getIdIssuer() != null)
			p.setIdIssuer(p.getIdIssuer().toUpperCase());
		if (p.getFirstName() != null)
			p.setFirstName(p.getFirstName().toUpperCase());
		if (p.getLastName() != null)
			p.setLastName(p.getLastName().toUpperCase());
		if (p.getMiddleName() != null)
			p.setMiddleName(p.getMiddleName().toUpperCase());
		if (p.getPrefix() != null)
			p.setPrefix(p.getPrefix().toUpperCase());
		if (p.getSuffix() != null)
			p.setSuffix(p.getSuffix().toUpperCase());
	}

	public void saveModify() {
		UserBean userBean = (UserBean) JSFUtil.getManagedObject("userBean");
		AuditLogService atnaService = AuditLogService.getInstance();
		if (currentSelection instanceof Patients) {
			Patients patientToSave = (Patients) currentSelection;
			toUpperCase(patientToSave);
			log.debug("Try to save modify of Patient pk = " + patientToSave.getPk());
			StatelessSession s = SessionManager.getInstance().openStatelessSession();
			s.beginTransaction();
			try {
				s.update(patientToSave);
			} catch (Exception e) {
				log.error("Error during modify patient data...", e);
				MessageManager.getInstance().setMessage(e.getMessage());
				return;
			}
			s.getTransaction().commit();
			try {
				PatientRecordMessage msg = new PatientRecordMessage(ActionCode.UPDATE);
				msg.addPatient(patientToSave.getPatientId(), patientToSave.getFirstName() + "^" + patientToSave.getLastName());
				try {
					msg.addUserPerson(userBean.getUserName(),
							userBean.getAccountNo() + "",
							userBean.getFirstName() + " " + userBean.getLastName(),
							InetAddress.getLocalHost().toString(), true);
				} catch (UnknownHostException e) {
					log.warn("Couldn't get local ip", e);
				}
				atnaService.SendMessage(msg);
			} catch (Exception e) {
				log.warn("Unable to send AuditLogMessage", e);
			}
		}
		if (currentSelection instanceof Studies) {
			Studies studyToSave = (Studies) currentSelection;
			log.debug("Try to save modify of Study pk = " + studyToSave.getStudyInstanceUid());
			StatelessSession s = SessionManager.getInstance().openStatelessSession();
			s.beginTransaction();
			try {
				s.update(studyToSave);
			} catch (Exception e) {
				log.error("Error during modify patient data...", e);
				MessageManager.getInstance().setMessage(e.getMessage());
				return;
			}
			s.getTransaction().commit();
			try {
				InstancesAccessedMessage msg = new InstancesAccessedMessage(ActionCode.UPDATE);
				msg.addPatient(studyToSave.getPatients().getPatientId(), studyToSave.getPatients().getFirstName() + "^" + studyToSave.getPatients().getLastName());
				msg.addStudy(studyToSave.getStudyInstanceUid(), null);
				try {
					msg.addUserPerson(userBean.getUserName() + ": Modified Study",
							userBean.getAccountNo() + "",
							userBean.getFirstName() + " " + userBean.getLastName(),
							InetAddress.getLocalHost().toString(), true);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					log.warn("Couldn't get local ip", e);
				}
				atnaService.SendMessage(msg);
			} catch (Exception e) {
				log.warn("Unable to send AuditLogMessage", e);
			}
		}
		if (currentSelection instanceof Series) {
			Series serieToSave = (Series) currentSelection;
			log.debug("Try to save modify of Serie pk = " + serieToSave.getSeriesInstanceUid());
			StatelessSession s = SessionManager.getInstance()
					.openStatelessSession();
			s.beginTransaction();
			try {
				s.update(serieToSave);
			} catch (Exception e) {
				log.error("Error during modify patient data...", e);
				MessageManager.getInstance().setMessage(e.getMessage());
				return;
			}
			s.getTransaction().commit();
			try {
				InstancesAccessedMessage msg = new InstancesAccessedMessage(ActionCode.UPDATE);
				msg.addPatient(serieToSave.getStudies().getPatients().getPatientId(), serieToSave.getStudies().getPatients().getFirstName() + "^"
						+ serieToSave.getStudies().getPatients().getLastName());
				msg.addStudy(serieToSave.getStudies().getStudyInstanceUid(), null);
				try {
					msg.addUserPerson(userBean.getUserName() + ": Modified Serie",
							userBean.getAccountNo() + "",
							userBean.getFirstName() + " " + userBean.getLastName(),
							InetAddress.getLocalHost().toString(), true);
				} catch (UnknownHostException e) {
					log.warn("Couldn't get local ip", e);
				}
				atnaService.SendMessage(msg);
			} catch (Exception e) {
				log.warn("Unable to send AuditLogMessage", e);
			}
		}
		MessageManager.getInstance().setMessage("dataUpdated", null);
	}

	private String mergeResult = "";

	public String getMergeResult() {
		return mergeResult;
	}

	public String makeMerge() {
		MergeMaker merger = new MergeMaker();
		UserBean userBean = (UserBean) JSFUtil.getManagedObject("userBean");
		mergeResult = "";
		if (type.equals("StP")) {
			if (study != null && patient != null) {
				if (merger.putStudyInPatient(study, patient, userBean)) {
					mergeResult = "Merge accomplished!";
					((FindPatients) JSFUtil.getManagedObject("findDestination"))._toRefresh = true;
					((FindPatients) JSFUtil.getManagedObject("patientsList"))._toRefresh = true;
				} else {
					log.error(type + ": error during merge.");
					mergeResult = "An error occours during merge...please see the log file.";
				}
			} else {
				log.debug("StudyTOPatient : nothing to merge...");
				mergeResult = "No selection make...nothing to merge";
			}
		}
		if (type.equals("StS")) {
			if (study != null && serie != null) {
				if (merger.putSeriesInStudy(serie, study, userBean)) {
					mergeResult = "Merge accomplished!";
					((FindPatients) JSFUtil.getManagedObject("findDestination"))._toRefresh = true;
					((FindPatients) JSFUtil.getManagedObject("patientsList"))._toRefresh = true;
				} else {
					log.error(type + ": error during merge.");
					mergeResult = "An error occours during merge...please see the log file";
				}
			} else {
				log.debug("SeriesTOStudy : nothing to merge...");
				mergeResult = "No selection make...nothing to merge";
			}
		}
		type = "";
		return "accomplished";
	}

	public void processDrop(DropEvent event) {
		mergeResult = "";
		type = "";
		study = null;
		patient = null;
		serie = null;
		if (event.getDragValue() instanceof Studies) {
			if (((Studies) event.getDragValue()).getPatients().getPk().equals(((Patients) event.getDropValue()).getPk())) {
				mergeResult = "Source and destination patient are the same...";
			} else {
				type = "StP";
				study = (Studies) event.getDragValue();
				patient = (Patients) event.getDropValue();
			}
		}
		if (event.getDragValue() instanceof Series) {
			if (((Series) event.getDragValue()).getStudies().getStudyInstanceUid().equals(((Studies) event.getDropValue()).getStudyInstanceUid())) {
				mergeResult = "Source and destination study are the same...";
			} else {
				type = "StS";
				serie = (Series) event.getDragValue();
				study = (Studies) event.getDropValue();
			}
		}
	}

	public Studies getStudy() {
		return study;
	}

	public Patients getPatient() {
		return patient;
	}

	public Series getSerie() {
		return serie;
	}

	public String getType() {
		return type;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled() {
		if (disabled)
			disabled = false;
		else
			disabled = true;
	}

	public Object getCurrentSelection() {
		return currentSelection;
	}

	public void setCurrentSelection(Object currentPatient) {
		this.currentSelection = currentPatient;
	}

	public String removeSelection() {
		this.currentSelection = null;
		return "do";
	}

	public void patientNodeSelection(NodeSelectedEvent event) {
		mergeResult = "";
		disabled = true;
		HtmlTreeNode tree = (HtmlTreeNode) event.getComponent();
		setCurrentSelection(tree.getData());
	}

	public void processNodeDrop(DropEvent event) {
		long sourceNodeId = 0;
		long targetNodeId = 0;
		if ("sourceNode".equals(event.getDragType())) {
			sourceNodeId = ((Long) event.getDragValue()).longValue();
			targetNodeId = ((Long) event.getDropValue()).longValue();
		} else if ("targetNode".equals(event.getDragType())) {
			targetNodeId = ((Long) event.getDragValue()).longValue();
			sourceNodeId = ((Long) event.getDropValue()).longValue();
		}
		if (sourceNodeId != targetNodeId) {
			NodesForwardMappingId mId = new NodesForwardMappingId(sourceNodeId, targetNodeId);
			KnownNodesHome knh = new KnownNodesHome();
			Session sess = null;
			try {
				sess = SessionManager.getInstance().openSession();
				if (!knh.addNodeForwardMapping(mId, null, sess))
					log.warn("Could not add NodeForwardMapping: from " + sourceNodeId + " to" + targetNodeId);
				else {
					AjaxContext ac = AjaxContext.getCurrentInstance();
					try {
						ac.addComponentToAjaxRender(((HtmlTreeNode) event.getDraggableSource()).getParent());
						ac.addComponentToAjaxRender(((HtmlTreeNode) event.getSource()).getParent());
					} catch (Exception ex) {
					}
				}
			} catch (Exception ex) {
				log.error("Exception trying to add NodeForwardMapping", ex);
			} finally {
				if (sess.isOpen())
					sess.close();
			}
		}
	}

	public void removeNodeBinding(ActionEvent event) {
		HtmlAjaxCommandButton button = (HtmlAjaxCommandButton) event.getComponent();
		String data = (String) button.getAttributes().get("data");
		NodesForwardMappingId mId = null;
		Session sess = null;
		try {
			String[] ids = data.split("_", 2); // The first id is that of the leaf, the second that of the parent
			if ("sourceNode".equals(button.getParent().getId()))
				mId = new NodesForwardMappingId(Long.parseLong(ids[0]), Long.parseLong(ids[1]));
			else if ("targetNode".equals(button.getParent().getId()))
				mId = new NodesForwardMappingId(Long.parseLong(ids[1]), Long.parseLong(ids[0]));
			KnownNodesHome knh = new KnownNodesHome();
			sess = SessionManager.getInstance().openSession();
			if (!knh.deleteNodeForwardMapping(mId, sess))
				log.warn("Could not delete NodeForwardMapping");
		} catch (Exception ex) {
			log.error("Exception trying to delete NodeForwardMapping", ex);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
	}
}
