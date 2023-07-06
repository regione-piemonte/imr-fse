<!-- Comment to prevent IE6 bug -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<f:view locale="#{sessionScope.locale}"
	afterPhase="#{messageManager.resetMessage}">
	<head>
	<link rel="stylesheet"
		href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
	<f:loadBundle var="msg" basename="it.units.htl.web.messages" />
	<script type="text/javascript">
		var modified=0;
	</script>
	<title>Remote O3-DPACS</title>
	</head>
	<body>
	<center>
	<div id="container">
		<f:subview id="header">
			<jsp:include page="/commons/header.inc.jsp" />
		</f:subview>
		<f:subview id="navigation">
			<jsp:include page="/commons/navigation.inc.jsp" />
		</f:subview>
	<div id="wrapper">
	<div id="contentTitle">O3-DPACS-WEB</div>
	<div id="content" style="vertical-align: top;">

	<div style="height: 560px;">
		<h:form>
			<div class="inputRow">
			<div class="inputLabel"><h:outputLabel value="#{msg.service}" />
		</div>
		<div class="inputField">
			<h:selectOneListbox value="#{serviceBean.selectedService}" id="servName" size="1" valueChangeListener="#{serviceBean.selectName}" onchange="if(modified==1){if(!confirm('#{msg.textChanged}'))return false;}submit();">
				<f:selectItems value="#{serviceBean.services}" />
			</h:selectOneListbox>
		</div>
		</div>
	</h:form> <h:form id="configForm">
		<div class="inputRow">
		<div class="inputLabel"><h:outputLabel value="#{msg.xmlValue}" />
		</div>
		<div class="inputField"><h:inputTextarea id="configText"
			onchange="modified=1;" cols="50" rows="30"
			value="#{serviceBean.selectedServiceXml}"></h:inputTextarea></div>
		</div>
		<div class="inputRow">
		<div class="inputLabel">
		<h:panelGrid columns="2"><h:commandButton id="saveConfig"
			actionListener="#{serviceBean.uploadXml}" value="#{msg.save}"></h:commandButton>
		<h:commandButton value="[.] #{msg.back}" action="back" accesskey="."/></h:panelGrid>
		</div>
		</div>

	</h:form></div>


	</div>
	<f:subview id="footer"><jsp:include
			page="/commons/footer.inc.html" /></f:subview></div>
	</div>
	</center>
	</body>
</f:view>
</html>