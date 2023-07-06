<!-- Comment to prevent IE6 bug -->
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<f:view locale="#{sessionScope.locale}" afterPhase="#{messageManager.resetMessage}">
	<head>
	<link rel="stylesheet"
		href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<title>Remote O3-DPACS</title>
	</head>
	<body>
	<center>
	<div id="container"><!-- header content --> <f:subview
		id="header">
		<jsp:include page="/commons/header.inc.jsp" />
	</f:subview> <!-- navigation content --> <f:subview id="navigation">
		<jsp:include page="/commons/navigation.inc.jsp" />
	</f:subview>

	<div id="wrapper"><f:loadBundle var="storage"
		basename="it.units.htl.web.messages" />
	<div id="contentTitle"><h:outputText
		value="#{storage.phisicView}" /></div>

	<div><h:form>
		<ul>
			<li><h:outputLink value="equipView.jspf" accesskey="6" >
				<f:verbatim >[6] Equipment</f:verbatim>
			</h:outputLink></li>
			<li><h:outputLink value="storageView.jspf" accesskey="7">
				<f:verbatim>[7] PhysicalMedia</f:verbatim>
			</h:outputLink></li>
			<li><h:outputLink value="nodesView.jspf" accesskey="8">
				<f:verbatim>[8] KnownNodes</f:verbatim>
			</h:outputLink></li>
		</ul>
		<p></p>
		<h:commandButton value="[0] #{storage.cretePhisic}" accesskey="0"
			actionListener="#{physicalMediaBackBean.createStorage}"
			rendered="#{sessionScope.actionEnabled.AddPM}"
			action="Edit-Create"></h:commandButton>
	</h:form>
	<p></p>
	</div>
	<div id="content"><h:form>
		<rich:datascroller align="left" for="nodes" maxPages="10"
			page="#{PhysicalMediaPageCounter.pageCounter}" reRender="sc2" id="sc1" />
		<rich:spacer height="30" />
		<rich:dataTable binding="#{physicalMediaBackBean.storageTable}"
			id="nodes" var="item" value="#{physicalMediaBackBean.list}"
			styleClass="table100" rowClasses="oddRow, evenRow" rows="30"
			columnClasses="column10,none,none,none,none,none,none,none,none,none,none,none">
			<rich:column rendered="#{sessionScope.actionEnabled.ModPM}">
				<f:facet name="header">
					<h:outputText value="" />
				</f:facet>
				<h:commandButton value="#{storage.edit}" action="Edit-Create"
					actionListener="#{physicalMediaBackBean.editStorage}" 
					/>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{storage.available}" />
				</f:facet>
				<h:selectBooleanCheckbox title="enable" value="#{item.available}"
					disabled="true"></h:selectBooleanCheckbox>
			</rich:column>
			<rich:column sortBy="#{item.name}" filterBy="#{item.name}" filterEvent="onblur" >
				<f:facet name="header">
					<h:outputText value="#{storage.name}" />
				</f:facet>
				<h:outputText value="#{item.name}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.type}" filterBy="#{item.type}" filterEvent="onblur" filterValue="HD">
				<f:facet name="header">
					<h:outputText value="#{storage.type}" />
				</f:facet>
				<h:outputText value="#{item.type}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{storage.purpose}" />
				</f:facet>
				<h:outputText value="#{item.purpose}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{storage.capacityInGigaBytes}" />
				</f:facet>
				<h:outputText value="#{item.capacityInBytes != null ? item.capacityInBytes/1073741824 : storage.unlimitedStorage}">
					<f:convertNumber type="number" maxFractionDigits="2"/>
				</h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.filledBytes}">
				<f:facet name="header">
					<h:outputText value="#{storage.filledGigaBytes}" />
				</f:facet>
				<h:outputText value="#{item.filledBytes/1073741824}">
					<f:convertNumber type="number" maxFractionDigits="2"/>
				</h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{storage.urlToStudy}" />
				</f:facet>
				<h:outputText value="#{item.urlToStudy}"></h:outputText>
			</rich:column>
		</rich:dataTable>
		
	</h:form></div>

	<!-- footer content --> <f:subview id="footer">
		<jsp:include page="/commons/footer.inc.html" />
	</f:subview></div>
	</div>
	</center>
	</body>
</f:view>
</html>
