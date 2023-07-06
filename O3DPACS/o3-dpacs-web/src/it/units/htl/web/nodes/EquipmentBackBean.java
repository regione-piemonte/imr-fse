/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.nodes;



import it.units.htl.maps.Equipment;
import it.units.htl.maps.util.SessionManager;
import java.util.ArrayList;
import java.util.List;
import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import org.hibernate.StatelessSession;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;

public class EquipmentBackBean {
// Backing bean for insert, edit and delete a single Equipment object
	
	private UIData equipTable;
	private Equipment equipment;
	
	private int pagina = 1;
	
	
	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	public EquipmentBackBean() {
		equipment = new Equipment();
	}
	
	public List<Equipment> getList(){
		Session s = SessionManager.getInstance().openSession();
		DetachedCriteria query = DetachedCriteria.forClass(Equipment.class);
		List<Equipment> equipments = query.getExecutableCriteria(s).list();
		s.close();
		return equipments;
	}
	
	public ArrayList<SelectItem> getEquipmentOptions(){
		List<Equipment> result = this.getList();
		ArrayList<SelectItem> option = new ArrayList<SelectItem>();
		for (int i = 0; i < result.size(); i++) {
			String stationName = result.get(i).getStationName();
			if (stationName==null) stationName="      ";
			option.add(new SelectItem(
					Integer.parseInt(result.get(i).getPk().toString()),
					stationName));
		}
		return option;
	}
	
	public void editEquipment(ActionEvent AE) {
		equipment = (Equipment) equipTable.getRowData();
		
	}
	
	public void createEquipment(ActionEvent AE){
		equipment = new Equipment();
	}
	
	public void deleteEquipment(ActionEvent AE){
		// TODO set deleting node
	}
	
	public void saveEquipment(ActionEvent AE){
		if (equipment.getPk() == null){
			Session s = SessionManager.getInstance().openSession();
			s.beginTransaction();
			Long generatedId = (Long) s.save(equipment);
			s.getTransaction().commit();
		} else {
			Session s = SessionManager.getInstance().openSession();
			s.beginTransaction();
			s.update(equipment);
			s.getTransaction().commit();
		}
	}
	
	public UIData getEquipTable() {
		return equipTable;
	}

	public void setEquipTable(UIData equipTable) {
		this.equipTable = equipTable;
	}
	
	public Equipment getEquipment() {
		return equipment;
	}

	public void setEquipment(Equipment equipment) {
		this.equipment = equipment;
	}
	
}
