/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.Study;


import it.units.htl.dpacs.dao.DeprecationRemote;
import it.units.htl.dpacs.valueObjects.RecoveryItem;
import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.users.JSFUtil;
import it.units.htl.web.users.UserBean;

import java.util.ArrayList;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIData;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RecoveryBackBean {
    private UIData recoveriesTable = null;
    public ArrayList<RecoveryListItem> _recoveryList = null;
    private RecoveryListItem selectedRecovery = new RecoveryListItem();
    private static Log log = LogFactory.getLog(RecoveryBackBean.class);
    
    public RecoveryBackBean() { }
    
    public ArrayList<RecoveryListItem> getRecoveryList() {

    	ArrayList<RecoveryListItem> ret=null;
    	DeprecationRemote bean=null; 
    	try{
//        	bean = InitialContext.doLookup("o3-dpacs/DeprecationBean/remote");
        	bean = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DeprecationBean!it.units.htl.dpacs.dao.DeprecationRemote");
            RecoveryItem[] recoveries=bean.getPossibleRecoveries(null);
            if(recoveries!=null && recoveries.length>0){
            	ret=new ArrayList<RecoveryListItem>(recoveries.length);
            	for(RecoveryItem ri: recoveries){
            		ret.add(new RecoveryListItem(ri));
            	}
            }
        
        }catch(Exception ex){
        	log.error("An exception occurred during study deprecation", ex);
        }
        return ret;
    }
    
    public String recovery() {
    	String answer=null;
        selectedRecovery = (RecoveryListItem) recoveriesTable.getRowData();
        DeprecationRemote bean=null;
        long ret=0;
        try{
//        	bean = InitialContext.doLookup("o3-dpacs/DeprecationBean/remote");
        	bean = InitialContext.doLookup("java:jboss/exported/o3-dpacs-ear/o3-dpacs-core-ejb/DeprecationBean!it.units.htl.dpacs.dao.DeprecationRemote");
        	UserBean userBean = (UserBean) JSFUtil.getManagedObject("userBean");
            long userPk=userBean.getAccountNo();
            
            if(RecoveryItem.TYPE_SERIES.equals(selectedRecovery.getObjectType())){
            	ret=bean.recoverSeries(Long.parseLong(selectedRecovery.getPk()), selectedRecovery.getCurrentUid(), selectedRecovery.getOriginalUid(), userPk);
            }else if(RecoveryItem.TYPE_STUDY.equals(selectedRecovery.getObjectType())){
            	ret=bean.recoverStudy(Long.parseLong(selectedRecovery.getPk()), selectedRecovery.getCurrentUid(), selectedRecovery.getOriginalUid(), userPk);
            }
            // TODO: Manage
        }catch(Exception ex){
        	log.error("An exception occurred during study deprecation", ex);
        }
        if(ret<=0){
        	answer="error";
        	log.error("Recovery did not succeed, return code="+ ret);
        	MessageManager.getInstance().setMessage("Recovery did not succeed, return code="+ ret);
        }else{
        	answer="recovery";
        	MessageManager.getInstance().setMessage("Recovery succeeded!");
        }

        return answer;
    }
    
    public String recoveryView() {
        selectedRecovery = (RecoveryListItem) recoveriesTable.getRowData();
        return "view";
    }
    
    public RecoveryListItem getSelectedRecovery() {
        return selectedRecovery;
    }
    
    public UIData getRecoveriesTable() {
        return recoveriesTable;
    }
    
    public void setRecoveriesTable(UIData _recoveriesTable) {
        this.recoveriesTable = _recoveriesTable;
    }
}
