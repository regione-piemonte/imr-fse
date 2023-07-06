/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.nodes;

import it.units.htl.maps.PhysicalMedia;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.ui.messaging.MessageManager;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class PhysicalMediaBackBean {
	// Backing bean for insert, edit and delete a single PhysicalMedia object

	private UIData storageTable;
	private PhysicalMedia storage;
	private int pagina = 1;
	private List<PhysicalMedia> PhysicalMediaList = null;
	boolean isStorageCapacityUnlimited;

	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	public PhysicalMediaBackBean() {
		storage = new PhysicalMedia();
	}

	public List<PhysicalMedia> getList() {
		if (PhysicalMediaList == null) {
			Session s = SessionManager.getInstance().openSession();
			DetachedCriteria query = DetachedCriteria
					.forClass(PhysicalMedia.class);
			PhysicalMediaList = query.getExecutableCriteria(s).list();
			s.close();
		}
		return PhysicalMediaList;
	}

	public ArrayList<SelectItem> getPhysicalMediaOptions() {
		Session s = SessionManager.getInstance().openSession();
		DetachedCriteria query = DetachedCriteria.forClass(PhysicalMedia.class);
		query.add(Restrictions.like("type", "HD"));
		List<PhysicalMedia> result = query.getExecutableCriteria(s).list();
		ArrayList<SelectItem> PhysicalMediaOption = new ArrayList<SelectItem>();
		for (int i = 0; i < result.size(); i++) {
			String url = result.get(i).getName() + " - ("
					+ result.get(i).getUrlToStudy() + ")";
			if (url == null)
				url = "      ";
			PhysicalMediaOption.add(new SelectItem(Integer.parseInt(result
					.get(i).getPk().toString()), url));
		}
		s.close();
		return PhysicalMediaOption;
	}

	public void editStorage(ActionEvent AE) {
		storage = (PhysicalMedia) storageTable.getRowData();
	}

	public void createStorage(ActionEvent AE) {
		storage = new PhysicalMedia();
	}

	public void deleteStorage(ActionEvent AE) {
		// TODO set deleting node
	}

	public void saveStorage(ActionEvent AE) {
		// replace \\ with /
		String rightUrl = storage.getUrlToStudy().replace("\\", "/");
		// remove unwanted spaces
		rightUrl = rightUrl.trim();
		// check if last char is /
		if (rightUrl.charAt(rightUrl.length() - 1) != '/') {
			rightUrl += "/";
		}
		storage.setUrlToStudy(rightUrl);

		Session s = SessionManager.getInstance().openSession();
		s.beginTransaction();
		if (storage.getPk() == null) {
			storage.setPurpose('f');
			storage.setType("HD");
			storage.setFilledBytes(0L);
			Long generatedId = (Long) s.save(storage);
			PhysicalMediaList = null;
		} else {
			s.update(storage);
		}
		s.getTransaction().commit();

		MessageManager.getInstance().setMessage(
				"Physical Media saved correctly");
	}

	public UIData getStorageTable() {
		return storageTable;
	}

	public void setStorageTable(UIData storageTable) {
		this.storageTable = storageTable;
	}

	public PhysicalMedia getStorage() {
		return storage;
	}

	public void setStorage(PhysicalMedia storage) {
		this.storage = storage;
	}

	public boolean isStorageCapacityUnlimited() {
		return storage.getCapacityInBytes() == null;
	}

	public void setStorageCapacityUnlimited(boolean isStorageCapacityUnlimited) {
		if (isStorageCapacityUnlimited) {
			storage.setCapacityInBytes(null);
		}
	}

	/**
	 * When creating a new node, check that the name is available, i.e. there
	 * are no other Physical Media with the same name.
	 * 
	 * @param context
	 * @param component
	 * @param input
	 */
	public void validatePysicalMediaName(FacesContext context,
			UIComponent component, Object input) {

		// creating a new media?
		if (storage.getPk() == null) {
			String name = (String) input;
			Session session = SessionManager.getInstance().openSession();
			List results = session
					.createQuery("from PhysicalMedia as pm where pm.name = ?")
					.setString(0, name).list();
			if (!results.isEmpty()) {
				String msg = MessageManager.getInstance().getLocalizedMessage(
						"duplicatePhysicalMediaName", null);
				FacesMessage fm = new FacesMessage(msg, null);
				throw new ValidatorException(fm);
			}
		}
	}
}
