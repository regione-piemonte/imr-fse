/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.ui;

import it.units.htl.maps.KnownNodes;
import it.units.htl.maps.KnownNodesHome;
import it.units.htl.maps.NodesForwardMappingId;
import it.units.htl.maps.Patients;
import it.units.htl.maps.PatientsHome;
import it.units.htl.maps.Series;
import it.units.htl.maps.Studies;
import it.units.htl.maps.util.SessionManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.faces.context.FacesContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

public class FindNodes {


	public boolean _toRefresh = true;
	
	private List<KnownNodes> nodesResults = new ArrayList<KnownNodes>();

	private static Log log = LogFactory.getLog(FindNodes.class);

	
	public List<KnownNodes> getNodes(){
		Session s = SessionManager.getInstance().openSession();
		List<KnownNodes> p = getNodes(s);
		s.close();
		return p;
	}
	
	@SuppressWarnings("unchecked")
	public List<KnownNodes> getNodes(Session s) {						
		KnownNodesHome knh=new KnownNodesHome();		
		KnownNodes knToFind = new KnownNodes();
		nodesResults = knh.findAll(s);
		
		return nodesResults;
	}
	
	public List<KnownNodes> getSourceNodesForTarget(long tId, Session s) {						
		KnownNodesHome knh=new KnownNodesHome();		
		NodesForwardMappingId targetId = new NodesForwardMappingId();
		targetId.setTargetNodeFk(tId);
		List<KnownNodes> results = knh.findSourcesForTarget(targetId, s);
		
		return results;
	}

	public List<KnownNodes> getTargetNodesForSource(long sId, Session s) {						
		KnownNodesHome knh=new KnownNodesHome();		
		NodesForwardMappingId sourceId = new NodesForwardMappingId();
		sourceId.setSourceNodeFk(sId);
		List<KnownNodes> results = knh.findTargetsForSource(sourceId, s);
		
		return results;
	}

	@SuppressWarnings("unchecked")
	public TreeNode getNodesTree() {
		Session sess = SessionManager.getInstance().openSession();
		TreeNode root = new TreeNodeImpl();
		root.setData("Root Node");
		ArrayList<KnownNodes> nodes = (ArrayList<KnownNodes>) getNodes(sess);
		if (nodes.size() > 0) {
			List<KnownNodes> inner=null;
			TreeNode childNode = null;
			for (int i = 0; i < nodes.size(); i++) {
				childNode = new TreeNodeImpl();
				childNode.setData(nodes.get(i));
				
				// Deal with nodes that are targets of this source
				inner=getTargetNodesForSource(nodes.get(i).getPk(), sess);
				if((inner!=null)&&(inner.size()>0)){
					TreeNode nestedNode = null;
					for(int j=0; j<inner.size(); ++j){
						nestedNode = new TreeNodeImpl();
						nestedNode.setData(inner.get(j));
						childNode.addChild(j, nestedNode);
					}
					inner=null;
				}
				
				
				root.addChild(i, childNode);
			}
		}
		sess.close();
		return root;
	}

	@SuppressWarnings("unchecked")
	public TreeNode getTargetNodesTree() {
		Session sess = SessionManager.getInstance().openSession();
		TreeNode root = new TreeNodeImpl();
		root.setData("Root Node");
		ArrayList<KnownNodes> nodes = (ArrayList<KnownNodes>) getNodes(sess);
		if (nodes.size() > 0) {
			List<KnownNodes> inner=null;
			TreeNode childNode = null;
			for (int i = 0; i < nodes.size(); i++) {
				childNode = new TreeNodeImpl();
				childNode.setData(nodes.get(i));
				
				// Deal with nodes that are sources of this target
				inner=getSourceNodesForTarget(nodes.get(i).getPk(), sess);
				if((inner!=null)&&(inner.size()>0)){
					TreeNode nestedNode = null;
					for(int j=0; j<inner.size(); ++j){
						nestedNode = new TreeNodeImpl();
						nestedNode.setData(inner.get(j));
						childNode.addChild(j, nestedNode);
					}
					inner=null;
				}
				
				
				root.addChild(i, childNode);
			}
		}
		sess.close();
		return root;
	}

	protected FacesContext context() {
		return (FacesContext.getCurrentInstance());
	}
}
