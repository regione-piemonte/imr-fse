<!-- Comment to prevent IE6 bug -->
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<f:view locale="#{sessionScope.locale}"
	afterPhase="#{messageManager.resetMessage}">
	<head>
	<link rel="stylesheet"
		href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
	<title>Remote O3-DPACS</title>
	</head>
	<body>
	<script type="text/javascript">
	function toggleField(fieldId, disable){
		var el = document.getElementById(fieldId);
		if(disable){
			el.disabled = true;
			el.className = 'disabledTextField';
		} else {
			el.disabled = false;
			el.className = 'enabledTextField';
		}
	}
	</script>
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
		value="#{storage.modifyPhisic}" /></div>

	<div id="content320">
	<center>
	<p></p>
	<h:form id="Media">
		<h:panelGrid id="panelGrid" columns="3" headerClass="searchpar"
			styleClass="table100">
			<f:facet name="header">
				<h:outputText value="#{storage.modifyPhisic}" />
			</f:facet>
			<h:outputLabel rendered="true" value="#{storage.name}" />
			<h:inputText id="name" tabindex="1" maxlength="16"
				value="#{physicalMediaBackBean.storage.name}" required="true" validator="#{physicalMediaBackBean.validatePysicalMediaName}" />
			<h:message for="name" errorClass="error" />

			<h:outputLabel rendered="true" value="#{storage.available}" />
			<h:selectBooleanCheckbox id="available" tabindex="3"
				value="#{physicalMediaBackBean.storage.available}" />
			<h:message for="available" errorClass="error" />

			<h:outputLabel rendered="true" value="#{storage.capacityInBytes}" />
			<h:inputText id="capacityInByte" tabindex="5" maxlength="20"
				value="#{physicalMediaBackBean.storage.capacityInBytes}"
				onfocus="if(document.getElementById('Media:unlimited').checked) {this.disabled=true} else {this.disabled = false}"/>
			<h:message for="capacityInByte" errorClass="error" />

			<h:outputLabel rendered="true" value="#{storage.unlimited}" />
			<h:selectBooleanCheckbox id="unlimited" tabindex="7"
				value="#{physicalMediaBackBean.storageCapacityUnlimited}"
				onclick="toggleField('Media:capacityInByte', this.checked)" />
			<h:message for="unlimited" errorClass="error" />

			<h:outputLabel rendered="true" value="#{storage.urlToStudy}" />
			<h:inputText id="urlToStudy" tabindex="9" maxlength="128"
				value="#{physicalMediaBackBean.storage.urlToStudy}" required="true" />
			<h:message for="urlToStudy" errorClass="error" />

			<h:outputLabel rendered="true" value="#{storage.humanReadableNotes}" />
			<h:inputText id="notes" tabindex="11" maxlength="64"
				value="#{physicalMediaBackBean.storage.humanReadableNotes}" />
			<h:message for="notes" errorClass="error" />

			<h:commandButton tabindex="13" action="Save"
				actionListener="#{physicalMediaBackBean.saveStorage}"
				value="#{storage.save}" />
			<h:commandButton tabindex="14" action="Save"
				value="[.] #{storage.back}" accesskey="." immediate="true" />
		</h:panelGrid>
	</h:form></center>
	<p></p>
	</div>

	<!-- footer content --> <f:subview id="footer">
		<jsp:include page="/commons/footer.inc.html" />
	</f:subview></div>
	</div>
	</center>
	</body>
</f:view>
</html>
