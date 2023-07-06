/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.ui;

import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.ui.messaging.MessageManager;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ServiceConfigurationBean {
	
	protected List<SelectItem> services=null;
	protected String selectedService=null;
	protected String selectedServiceXml=null;
	private Log log = LogFactory.getLog(ServiceConfigurationBean.class);
	

	public ServiceConfigurationBean() {
		getServices();
		if(services!=null){
			selectedService=(String)services.get(0).getValue();
			fillSelection(selectedService);
		}
	}

	public void setServices(List<SelectItem> services) {
		this.services = services;
	}

	public List<SelectItem> getServices() {
		if(services!=null)
			return services;
		List<SelectItem> ret=null;
		
		SessionFactory sm = SessionManager.getInstance();        
    	StatelessSession s = sm.openStatelessSession();
    	
    	String hql="SELECT sc.serviceName FROM ServicesConfiguration sc ORDER BY sc.serviceName ASC";
		Query query=s.createQuery(hql);
		List<String> result = (List<String>)query.list();
		
		if((result!=null)&&(result.size()>0)){
			ret=new ArrayList<SelectItem>(result.size());
			for(String st : result){
				ret.add(new SelectItem(st,st));
			}
		}
		s.close();
		services=ret;
		return ret;
	}

	public String getSelectedServiceXml() {
		return selectedServiceXml;
	}

	public void setSelectedServiceXml(String selectedServiceXml) {
		this.selectedServiceXml = selectedServiceXml;
	}

	
	public void selectName(ValueChangeEvent event){
		String selected=(String)event.getNewValue();
		this.selectedService=selected;
		fillSelection(selected);
	}

	protected void fillSelection(String selected){
		SessionFactory sm = SessionManager.getInstance();        
    	StatelessSession s = sm.openStatelessSession();
    	
    	ServicesConfiguration result = (ServicesConfiguration) s.get(ServicesConfiguration.class, selected);

    	if(result!=null){
    		setSelectedServiceXml(result.getConfiguration());
    	}
		s.close();		
	}
	
	public void uploadXml(ActionEvent event){
		
		String serviceToChange=this.selectedService;
		String newServiceXml=this.selectedServiceXml;
		
		// Verify well-formedness
		boolean wellFormed=true;
		try{
			DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = dBF.newDocumentBuilder();
	        StringReader reader=new StringReader(newServiceXml);
	        InputSource is = new InputSource(reader);
	        Document doc = builder.parse(is);
		}catch(Exception ex){
			wellFormed=false;
		}
		
        if(wellFormed){
        	try{
				SessionFactory sm = SessionManager.getInstance();        
		    	StatelessSession s = sm.openStatelessSession();
		    	
				s.beginTransaction();
				ServicesConfiguration sc=(ServicesConfiguration)s.get(ServicesConfiguration.class, serviceToChange);
				if(sc!=null){
					sc.setConfiguration(newServiceXml);
				}
				s.update(sc);
				s.getTransaction().commit();
        	}catch(Exception ex){
        		MessageManager.getInstance().setMessage("notUpdatedXml", new String[]{serviceToChange});
        		return;
        	}
			fillSelection(serviceToChange);
			MessageManager.getInstance().setMessage("updatedXml", new String[]{serviceToChange});
        }else{
        	MessageManager.getInstance().setMessage("notWellFormed", new String[]{serviceToChange});
        }
	}

	public String getSelectedService() {
		return selectedService;
	}

	public void setSelectedService(String selectedService) {
		this.selectedService = selectedService;
		fillSelection(this.selectedService);
	}

}
