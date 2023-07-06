<!-- Comment to prevent IE6 bug -->
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib prefix="a4j" uri="http://richfaces.org/a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<f:view locale="#{sessionScope.locale}"
	afterPhase="#{messageManager.resetMessage}">
	<head>
	<link rel="stylesheet"
		href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"></meta>
	<f:loadBundle var="msg" basename="it.units.htl.web.messages" />
	<title>Remote O3-DPACS</title>
	</head>
	<body>
	<center>
	<div id="container"><f:subview id="header">
		<jsp:include page="/commons/header.inc.jsp" />
	</f:subview><f:subview id="navigation">
		<jsp:include page="/commons/navigation.inc.jsp" />
	</f:subview>

	<div id="wrapper">
	<div id="contentTitle"><h:outputText value="#{msg.serverManager}" /></div>
	<div id="content320">
	<h:form>
	<h:commandButton value="[.] #{msg.back}" accesskey="."
			action="back" /></h:form>
	<h:panelGrid id="grid" columns="1"
		cellpadding="15" width="100%">
		<h:outputText value="#{msg.serverPage}">
		</h:outputText>
	</h:panelGrid> <h:panelGrid id="grid2" columns="1" cellpadding="30" width="70%">
		<a4j:form>
			<h:panelGrid columns="1" styleClass="center">
				<a4j:commandButton value="[6] #{msg.startServer}"
					actionListener="#{startStopServers.start}"  accesskey="6" reRender="grid3" />
				<a4j:commandButton value="[7] #{msg.stopServer}"
					actionListener="#{startStopServers.stop}" accesskey="7" reRender="grid3" />
				<a4j:commandButton value="[8] #{msg.reload}"
					actionListener="#{startStopServers.reload}" accesskey="8" reRender="grid3" />
			</h:panelGrid>
		</a4j:form>
	</h:panelGrid> <rich:panel id="panelStatus">
		<h:panelGrid id="grid3" columns="1" cellpadding="20" width="70%">

			<h:outputText value="#{msg.result} "></h:outputText>
			<b><h:outputText value="#{startStopServers.status} "></h:outputText></b>
			<h:outputText value="#{msg.serverStatus} "></h:outputText>
			<h:dataTable
				value="#{startStopServers.servicesStatus}" var="service" style="text-align:center;">
				<rich:column style="text-align:center;">
					<f:facet name="header">
						<h:outputText value="#{msg.service}" />
					</f:facet>
					<h:outputText value="#{service.name}" />
				</rich:column>
				<rich:column style="text-align:center;">
					<f:facet name="header">
						<h:outputText value="#{msg.serviceEnabled}" />
					</f:facet>
					<h:graphicImage alt="running" url="../img/server/running.png"
						rendered="#{service.enabled}" />
					<h:graphicImage alt="stopped" url="../img/server/stopped.png"
						rendered="#{!service.enabled}" />
				</rich:column>
				<rich:column style="text-align:center;">
					<f:facet name="header">
						<h:outputText value="#{msg.serviceRunning}" />
					</f:facet>
					
					<h:graphicImage alt="running" url="../img/server/running.png"
						rendered="#{service.status}" />
					<h:graphicImage alt="stopped" url="../img/server/stopped.png"
						rendered="#{!service.status}" />
				</rich:column>
			</h:dataTable>
		</h:panelGrid>
	</rich:panel></div>

	<!-- footer content --> <f:subview id="footer">
		<jsp:include page="/commons/footer.inc.html" />
	</f:subview></div>
	</div>
	</center>
	</body>
</f:view>
</html>