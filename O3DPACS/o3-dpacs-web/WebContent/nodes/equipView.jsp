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

	<div id="wrapper"><f:loadBundle var="equipment"
		basename="it.units.htl.web.messages" />
	<div id="contentTitle"><h:outputText
		value="#{equipment.equipView}" /></div>

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
	</h:form>
	<p></p>
	</div>
	<div id="content"><h:form>
	<rich:datascroller align="left" for="nodes" maxPages="10"
			page="#{EquipPageCounter.pageCounter}" id="sc1"/>
		<rich:spacer height="30" />
		<rich:dataTable binding="#{equipmentBackBean.equipTable}" id="nodes"
			var="item" value="#{equipmentBackBean.list}" styleClass="table100"
			rowClasses="oddRow, evenRow"
			rows="30"
			columnClasses="column10,none,none,none,none,none,none,none,none,none,none,none,none">
			<rich:column sortBy="#{item.equipmentType}">
				<f:facet name="header">
					<h:outputText value="#{equipment.equipmentType}" />
				</f:facet>
				<h:outputText value="#{item.equipmentType}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.manufacturer}">
				<f:facet name="header">
					<h:outputText value="#{equipment.manufacturer}" />
				</f:facet>
				<h:outputText value="#{item.manufacturer}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.institutionName}">
				<f:facet name="header">
					<h:outputText value="#{equipment.institutionName}" />
				</f:facet>
				<h:outputText value="#{item.institutionName}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.stationName}">
				<f:facet name="header">
					<h:outputText value="#{equipment.stationName}" />
				</f:facet>
				<h:outputText value="#{item.stationName}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.institutionalDepartmentName}">
				<f:facet name="header">
					<h:outputText value="#{equipment.institutionalDepartmentName}" />
				</f:facet>
				<h:outputText value="#{item.institutionalDepartmentName}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{equipment.manufacturersModelName}" />
				</f:facet>
				<h:outputText value="#{item.manufacturersModelName}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{equipment.deviceSerialNumber}" />
				</f:facet>
				<h:outputText value="#{item.deviceSerialNumber}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{equipment.dateOfLastCalibration}" />
				</f:facet>
				<h:outputText value="#{item.dateOfLastCalibration}">
					<f:convertDateTime type="date" dateStyle="short"
						pattern="dd/MM/yyyy" />
				</h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{equipment.timeOfLastCalibration}" />
				</f:facet>
				<h:outputText value="#{item.timeOfLastCalibration}">
					<f:convertDateTime type="time" timeStyle="short" />
				</h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{equipment.lastCalibratedBy}" />
				</f:facet>
				<h:outputText value="#{item.lastCalibratedBy}"></h:outputText>
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
