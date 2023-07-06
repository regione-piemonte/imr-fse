/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package it.units.htl.web.security;

import it.units.htl.maps.ServicesConfiguration;
import it.units.htl.maps.util.SessionManager;
import it.units.htl.web.ui.messaging.MessageManager;
import it.units.htl.web.utils.XmlConfigLoader;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;

import javax.faces.model.SelectItem;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author sangalli
 * 
 */
public class GroupManagement {

	private static Logger log = Logger.getLogger(GroupManagement.class);

	// list of area accessible to the selected group
	private ArrayList<AreaPolicy> selectedGroupVisibleAreas = new ArrayList<AreaPolicy>();

	// list of allowed actions for the selected group 
	private ArrayList<ActionPolicy> selectedGroupAllowedActions = new ArrayList<ActionPolicy>();

	// content of the WebConfiguration service configuration
	private Document webConfiguration;

	// content of the WebSecurity service configuration in which groups are defined
	private Document webSecurity;

	// details of group being created
	private String newGroupDescr = "";
	private String newGroupName = "";

	private Integer selectedGroupId = null;
	private ArrayList<SelectItem> groupIdItems;

	//	private MessageManager messageManager;

	public GroupManagement() {
		//		messageManager = (MessageManager)((HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getAttribute("messageManager");
		loadConfigurations();
	}

	/**
	 * Read configurations from database
	 */
	private void loadConfigurations() {
		webConfiguration = XmlConfigLoader.getConfigurationFromDB("WebConfiguration");
		webSecurity = XmlConfigLoader.getConfigurationFromDB("WebSecurity");
	}

	/**
	 * Force the reloading of the configurations and fill the current group
	 * configuration
	 */
	public void forceReload() {
		loadConfigurations();
		loadSelectedGroupConfiguration();
		reloadGroupsRoles();
	}

	/**
	 * Load the selected group configuration in order to display the details.
	 * Note that this method does not reload any configuration.
	 */
	public void loadSelectedGroupConfiguration() {
		selectedGroupVisibleAreas.clear();
		selectedGroupAllowedActions.clear();

		if (webConfiguration == null) {
			selectedGroupId = null;
			MessageManager.getInstance().setMessage("unableToLoad", new String[] { "WebConfiguration" });
			return;
		}
		NodeList configuredAreas = webConfiguration.getElementsByTagName("pattern");
		NodeList configuredActions = webConfiguration.getElementsByTagName("type");

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		String expre;
		Object res = null;
		for (int i = 0; i < configuredAreas.getLength(); i++) {
			try {
				// retrieve the visible areas for the selected group
				expre = "count(//group[@id='" + selectedGroupId + "']/canView/pattern[text()='"
						+ configuredAreas.item(i).getTextContent() + "']) = 1";
				res = xpath.evaluate(expre, webSecurity, XPathConstants.BOOLEAN);
				selectedGroupVisibleAreas.add(new AreaPolicy(
						configuredAreas.item(i).getTextContent(),
						configuredAreas.item(i).getAttributes().getNamedItem("descr").getTextContent(),
						(Boolean) res));
			} catch (XPathExpressionException e) {
				log.error("", e);
				MessageManager.getInstance().setMessage("exceptionOccour", new String[] { e.getMessage() });
			}
		}
		for (int i = 0; i < configuredActions.getLength(); i++) {
			// retrieve the allowed actions for the selected group
			expre = "count(//group[@id='" + selectedGroupId
					+ "']/action/type[text()='"
					+ configuredActions.item(i).getTextContent() + "']) = 1";
			try {
				res = xpath.evaluate(expre, webSecurity, XPathConstants.BOOLEAN);
				selectedGroupAllowedActions.add(new ActionPolicy(
						configuredActions.item(i).getTextContent(),
						(Boolean) res,
						configuredActions.item(i).getAttributes().getNamedItem("descr").getTextContent()));
			} catch (XPathExpressionException e) {
				log.error("", e);
				MessageManager.getInstance().setMessage("exceptionOccour", new String[] { e.getMessage() });
			}
		}
	}

	public ActionPolicy[] getActionPolicies() {
		ActionPolicy[] ap = new ActionPolicy[selectedGroupAllowedActions.size()];
		selectedGroupAllowedActions.toArray(ap);
		return ap;
	}

	public AreaPolicy[] getConfiguration() {
		AreaPolicy[] p = new AreaPolicy[selectedGroupVisibleAreas.size()];
		selectedGroupVisibleAreas.toArray(p);
		return p;
	}

	/**
	 * Reload the list of groups reading from the webSecurity Document. Note
	 * that this method does not refresh the webSecurity content.
	 */
	private void reloadGroupsRoles() {
		if (groupIdItems == null) {
			groupIdItems = new ArrayList<SelectItem>();
		} else {
			groupIdItems.clear();
		}

		NodeList groupsList = webSecurity.getElementsByTagName("group");
		for (int groupNum = 0; groupNum < groupsList.getLength(); groupNum++) {
			String groupId = groupsList.item(groupNum).getAttributes().getNamedItem("id").getTextContent();
			NodeList groupPoliciesNL = groupsList.item(groupNum).getChildNodes();
			String groupDesc = ((Node) groupPoliciesNL).getAttributes().getNamedItem("descr").getTextContent();
			groupIdItems.add(new SelectItem(groupId, groupDesc));
		}
	}

	/**
	 * Save the webSecurity configuration
	 */
	public void saveConfiguration() {
		// per prima cosa recupero il nodelist del gruppo che voglio salvare
		String qryGetGroup = "//group[@id='" + selectedGroupId + "']";
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		NodeList o = null;
		try {
			o = (NodeList) xpath.evaluate(qryGetGroup, webSecurity, XPathConstants.NODESET);
		} catch (XPathExpressionException e1) {
			log.error("Error during saveConfiguration", e1);
			MessageManager.getInstance().setMessage(e1.getCause().getMessage());
		}

		// riscrivo la parte relativa alle aree a cui il gruppo ha accesso
		Element delCanView = (Element) ((Element) o.item(0)).getElementsByTagName("canView").item(0);
		if (delCanView != null) {
			((Element) o.item(0)).removeChild(delCanView);
		}
		o.item(0).appendChild(((Element) o.item(0)).getOwnerDocument().createElement("canView"));
		NodeList canView = ((Element) o.item(0)).getElementsByTagName("canView");
		for (int i = 0; i < selectedGroupVisibleAreas.size(); i++) {
			if (selectedGroupVisibleAreas.get(i).getEnabled()) {
				Node nn = webSecurity.createElement("pattern");
				nn.setTextContent(selectedGroupVisibleAreas.get(i).getArea());
				((Element) canView.item(0)).appendChild(nn);
			}
		}

		// riscrivo la parte relativa alle azioni che il gruppo puo' compiere
		Element delAction = (Element) ((Element) o.item(0)).getElementsByTagName("action").item(0);
		if (delAction != null) {
			((Element) o.item(0)).removeChild(delAction);
		}
		o.item(0).appendChild(((Element) o.item(0)).getOwnerDocument().createElement("action"));
		NodeList actionEnabled = ((Element) o.item(0)).getElementsByTagName("action");
		removeAll(actionEnabled.item(0), Node.ELEMENT_NODE, "type");

		for (int i = 0; i < selectedGroupAllowedActions.size(); i++) {
			if (selectedGroupAllowedActions.get(i).isEnabled()) {
				Node nn = webSecurity.createElement("type");
				nn.setTextContent(selectedGroupAllowedActions.get(i).getType());
				((Element) actionEnabled.item(0)).appendChild(nn);
			}
		}
		((Element) webSecurity.getFirstChild()).appendChild(o.item(0));
		webSecurity.normalizeDocument();
		log.debug("This is what i want to save ");
		log.debug(xmlToString(webSecurity.getChildNodes()));

		Session s = SessionManager.getInstance().openSession();
		ServicesConfiguration sc = new ServicesConfiguration();
		sc.setConfiguration(xmlToString(webSecurity.getChildNodes()));
		sc.setServiceName("WebSecurity");
		s.beginTransaction();
		s.saveOrUpdate(sc);
		s.getTransaction().commit();
		MessageManager.getInstance().setMessage("configurationSaved", null);
	}

	private static void removeAll(Node node, short nodeType, String name) {
		if (node.getNodeType() == nodeType
				&& (name == null || node.getNodeName().equals(name))) {
			node.getParentNode().removeChild(node);
		} else {
			// Visit the children
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				removeAll(list.item(i), nodeType, name);
			}
		}
	}

	private String xmlToString(NodeList o)
			throws TransformerFactoryConfigurationError {
		String xml = null;
		try {
			Source source = new DOMSource(o.item(0));
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory fac = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = fac.newTransformer();
			Properties p = new Properties();
			p.put(OutputKeys.INDENT, "yes");
			transformer.setOutputProperties(p);
			transformer.transform(source, result);
			xml = stringWriter.getBuffer().toString();
		} catch (TransformerException e) {
			log.error("Error during xmlToString", e);
			MessageManager.getInstance().setMessage(e.getMessage());
		}
		return xml;
	}

	public void insertNew() {
		Element newGroup = webSecurity.createElement("group");
		if ("".equals(newGroupName) || newGroupName == null) {
			MessageManager.getInstance().setMessage("fieldNeed",
					new String[] { MessageManager.getInstance().getLocalizedMessage("groupName", null) });
			;
			return;
		}
		if ("".equals(newGroupDescr) || newGroupDescr == null) {
			MessageManager.getInstance().setMessage(
					"fieldNeed",
					new String[] { MessageManager.getInstance().getLocalizedMessage("groupDescription", null) });
			return;
		}
		newGroup.setAttribute("name", newGroupName);
		newGroup.setAttribute("descr", newGroupDescr);
		newGroup.setAttribute("id", (groupIdItems.size() + 1) + "");
		webSecurity.getFirstChild().appendChild(newGroup);
		Session s = SessionManager.getInstance().openSession();
		ServicesConfiguration sc = new ServicesConfiguration();
		sc.setConfiguration(xmlToString(webSecurity.getChildNodes()));
		sc.setServiceName("WebSecurity");
		s.beginTransaction();
		s.saveOrUpdate(sc);
		s.getTransaction().commit();
		reloadGroupsRoles();
		newGroupDescr = newGroupName = "";
		MessageManager.getInstance().setMessage("configurationSaved", null);
	}

	public String getNewGroupDescr() {
		return newGroupDescr;
	}

	public void setNewGroupDescr(String newGroupDescr) {
		this.newGroupDescr = newGroupDescr;
	}

	public String getNewGroupName() {
		return newGroupName;
	}

	public void setNewGroupName(String newGroupName) {
		this.newGroupName = newGroupName;
	}

	public ArrayList<SelectItem> getGroupIdItems() {
		if (groupIdItems == null) {
			reloadGroupsRoles();
		}
		return groupIdItems;
	}

	public String getSelectedGroupName() {
		if (groupIdItems != null && selectedGroupId != null) {
			for (SelectItem item : groupIdItems) {
				String currentItemValue = (String) item.getValue();
				int currentItemId = Integer.parseInt(currentItemValue);
				if (selectedGroupId == currentItemId) {
					return item.getLabel();
				}
			}
		}

		log.warn("this is a bug, group not selected!");
		return null;
	}

	public Integer getGroupId() {
		return selectedGroupId;
	}

	public void setGroupId(Integer nextSWVersion) {
		this.selectedGroupId = nextSWVersion;
	}

}
