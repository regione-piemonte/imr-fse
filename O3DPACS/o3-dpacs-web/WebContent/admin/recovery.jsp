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
	<f:loadBundle var="msg" basename="it.units.htl.web.messages" />
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

	<div id="wrapper">
	<div id="contentTitle"><h:outputText value="#{msg.recoveryList}" /></div>
	<div id="content"><h:form>
	<a4j:commandButton value="Refresh" reRender="recovery"/>
	
	<br />
	<rich:spacer height="20" />
		<rich:datascroller align="left" for="recovery" maxPages="10"
			page="#{RecoveryPageCounter.pageCounter}" reRender="sc2" id="sc1" />
		<rich:spacer height="30" />
		<rich:dataTable id="recovery"
			binding="#{recoveryBackBean.recoveriesTable}"
			value="#{recoveryBackBean.recoveryList}" var="item" width="100%"
			rowClasses="oddRow, evenRow" rows="30"
			columnClasses="column10,none,none,none,none,none,none,none,none,none">
			<rich:column>
				<f:facet name="header">
					<h:outputText value="" />
				</f:facet>
				<h:commandButton value="#{msg.recovery}"
					action="#{recoveryBackBean.recovery}" />
			</rich:column>
			<rich:column sortBy="#{item.user}" filterBy="#{item.user}" filterEvent="onchange">
				<f:facet name="header">
					<h:outputText value="#{msg.user}" />
				</f:facet>
				<h:outputText value="#{item.user}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.objectType}">
				<f:facet name="header">
					<h:outputText value="#{msg.objectType}" />
				</f:facet>
				<h:outputText value="#{item.objectType}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.currentUid}">
				<f:facet name="header">
					<h:outputText value="#{msg.currentUid}" />
				</f:facet>
				<h:outputText value="#{item.currentUid}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.originalUid}" filterBy="#{item.originalUid}" filterEvent="onchange">
				<f:facet name="header">
					<h:outputText value="#{msg.originalUid}" />
				</f:facet>
				<h:outputText value="#{item.originalUid}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.patientId}" filterBy="#{item.patientId}" filterEvent="onchange">
				<f:facet name="header">
					<h:outputText value="#{msg.patientId}" />
				</f:facet>
				<h:outputText value="#{item.patientId}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.deprecationTime}" comparator="#{dateRecoveryComparator}">
				<f:facet name="header">
					<h:outputText value="#{msg.removalDateTime}" />
				</f:facet>
				<h:outputText value="#{item.deprecationTime}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{msg.reason}" />
				</f:facet>
				<h:outputText value="#{item.reason}"></h:outputText>
			</rich:column>
		</rich:dataTable>
		<rich:spacer height="10" />
		<rich:datascroller align="left" for="recovery" maxPages="20"
			page="#{RecoveryPageCounter.pageCounter}" id="sc2" reRender="sc1" />
			
		<rich:spacer height="20" />
		<br />
		<a4j:commandButton value="Refresh" reRender="recovery" />
	</h:form></div>
	</div>
	
	<!-- footer content --> <f:subview id="footer">
		<jsp:include page="/commons/footer.inc.html" />
	</f:subview></div>
	</center>
	</body>
</f:view>
</html>

