<!-- Comment to prevent IE6 bug -->
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.ajax4jsf.org/rich" prefix="rich"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
	xmlns:s="http://jboss.com/products/seam/taglib">
	
	<style>
		.alignTop {vertical-align:top;}
	</style>

<f:view locale="#{sessionScope.locale}" afterPhase="#{messageManager.resetMessage}">
	<head>
	<link rel="stylesheet"
		href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
	<f:loadBundle var="msg" basename="it.units.htl.web.messages" />
	<title>Remote O3-DPACS</title>
	</head>
	<body>
	<center>
	<div id="container"><f:subview id="header" >
		<jsp:include page="/commons/header.inc.jsp" />
	</f:subview> <f:subview id="navigation">
		<jsp:include page="/commons/navigation.inc.jsp" />
	</f:subview>


	<div id="wrapper">
	<div id="contentTitle"><h:outputText value="#{msg.adminArea}" /></div>
	<div id="content" align="center">
	<center><h:panelGrid columns="4">
		<h:form>
			<h:commandButton value="[.] #{msg.back}" action="back" accesskey="."></h:commandButton>
		</h:form>
		<a4j:form ajaxSubmit="true">
			<h:panelGrid columns="4">
				<h:outputText value="#{msg.selectGroup}" />
				<h:selectOneMenu id="groupsList" value="#{groupManagement.groupId}">
					<f:selectItems value="#{groupManagement.groupIdItems}" />
					<a4j:support event="onchange"
						action="#{groupManagement.loadSelectedGroupConfiguration}"
						reRender="AreePanel, messagePanel" />
				</h:selectOneMenu>				
			
		
			<h:commandButton value="#{msg.loadGroupConfiguration}" id="loadGroupBtn">
				<a4j:support event="onclick"
							action="#{groupManagement.forceReload}"
							reRender="AreePanel, messagePanel, groupsList" />
			</h:commandButton>
		
			<h:commandButton value="#{msg.addGroup}" id="link">
				<rich:componentControl for="panel" attachTo="link" operation="show"
					event="onclick" />
			</h:commandButton>
		
			</h:panelGrid>
		</a4j:form>
	
	</h:panelGrid> <a4j:form>

		<a4j:outputPanel id="AreePanel">
			<rich:panel rendered="#{groupManagement.groupId != null}">
				<f:facet name="header">
        		<h:outputText value="#{groupManagement.selectedGroupName}" />
        		</f:facet>
				<h:panelGrid columns="2" columnClasses="alignTop, alignTop">
					<rich:dataTable id="AreeNew" 
						value="#{groupManagement.configuration}" var="item"
						headerClass="merge-header" rowClasses="oddRow, evenRow">
						<f:facet name="header">
							<rich:columnGroup>
								<rich:column colspan="2">
									<h:outputText value="#{msg.selectGroupAreas}"
										styleClass="merge-header" />
								</rich:column>
							</rich:columnGroup>
						</f:facet>
						<rich:column>
							<h:outputText value="#{item.descr}" />
						</rich:column>
						<rich:column>
							<h:selectBooleanCheckbox value="#{item.enabled}" />
						</rich:column>
					</rich:dataTable>

					<rich:dataTable value="#{groupManagement.actionPolicies}"
						var="item" headerClass="merge-header" rowClasses="oddRow, evenRow">
						<f:facet name="header">
							<rich:columnGroup>
								<rich:column colspan="2">
									<h:outputText value="#{msg.selectGroupActions}"
										styleClass="merge-header" />
								</rich:column>
							</rich:columnGroup>
						</f:facet>
						<rich:column>
							<h:outputText value="#{item.description}" />
						</rich:column>
						<rich:column>
							<h:selectBooleanCheckbox value="#{item.enabled}" />
						</rich:column>
					</rich:dataTable>
				</h:panelGrid>
				<a4j:commandButton action="#{groupManagement.saveConfiguration}"
					value="Save" reRender="messagePanel"
					rendered="#{groupManagement.groupId != null}" />
			</rich:panel>
		</a4j:outputPanel>

	</a4j:form></center>

	<rich:modalPanel id="panel" autosized="true" resizeable="false">
		<f:facet name="header">
			<h:panelGroup>
				<h:outputText value="#{msg.addGroup}"></h:outputText>
			</h:panelGroup>
		</f:facet>
		<f:facet name="controls">
			<h:panelGroup>
				<a4j:form>
					<a4j:commandLink oncomplete="#{rich:component('panel')}.hide()"
						reRender="groupsList">
						<h:graphicImage value="/img/close.png" styleClass="hidelink"
							id="hidelink" />
					</a4j:commandLink>
				</a4j:form>
			</h:panelGroup>
		</f:facet>
		<a4j:form>
		<h:panelGrid columns="2">
			
			<h:outputText value="#{msg.groupName}*" styleClass="nowrap"></h:outputText>
			<h:inputText value="#{groupManagement.newGroupName}"> </h:inputText>
			<h:outputText value="#{msg.groupDescription}*" styleClass="nowrap"></h:outputText>
			<h:inputText value="#{groupManagement.newGroupDescr}"></h:inputText>
			<rich:spacer width="0" height="0" />
			<a4j:commandButton action="#{groupManagement.insertNew}" value="#{msg.save}"
				oncomplete="#{rich:component('panel')}.hide()" reRender="messagePanel,groupsList"></a4j:commandButton>
			<h:outputText value="* are required" styleClass="info"/>
		</h:panelGrid>
		</a4j:form>
	</rich:modalPanel></div>
	<f:subview id="footer">
		<jsp:include page="/commons/footer.inc.html" />
	</f:subview></div>
	</div>
	</center>
	</body>
</f:view>
</html>