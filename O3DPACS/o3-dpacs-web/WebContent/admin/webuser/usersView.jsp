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
	<div id="contentTitle"><h:outputText value="#{msg.userManagement}" /></div>
	<div><h:form>
		<h:commandButton value="[6] #{msg.createUser}" accesskey="6"
			actionListener="#{userBackBean.createUser}" action="Edit-Create"
			rendered="#{sessionScope.actionEnabled.CreateWebUser}" />
			<rich:spacer width="10"/>
		<h:commandButton accesskey="7"
			value="[7] #{msg.manageGroup}" action="Manage-Groups"
			rendered="#{sessionScope.actionEnabled.ManageGroups}" />
	</h:form>
	<p></p>
	</div>
	<div id="content"><h:form>
		<rich:datascroller align="left" for="users" maxPages="10"
			page="#{UserPageCounter.pageCounter}" reRender="sc2" id="sc1" />
		<rich:spacer height="30" />
		<rich:dataTable binding="#{userBackBean.usersTable}" id="users"
			var="item" value="#{userBackBean.list}" styleClass="table100"
			rowClasses="oddRow, evenRow" rows="30"
			columnClasses="column10,none,none,none,none,none,none,none,none,none">
			<rich:column rendered="#{sessionScope.actionEnabled.EditWebUser}">
				<f:facet name="header">
					<h:outputText value="" />
				</f:facet>
				<h:commandButton value="#{msg.edit}" action="Edit-Create" actionListener="#{userBackBean.editUser}" rendered="#{0 lt item.roleFk}" />
			</rich:column>
			<rich:column sortBy="#{item.lastName}">
				<f:facet name="header">
					<h:outputText value="#{msg.lastName}" />
				</f:facet>
				<h:outputText value="#{item.lastName}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{msg.firstName}" />
				</f:facet>
				<h:outputText value="#{item.firstName}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.userName}">
				<f:facet name="header">
					<h:outputText value="#{msg.username}" />
				</f:facet>
				<h:outputText value="#{item.userName}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{msg.email}" />
				</f:facet>
				<h:outputText value="#{item.email}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{msg.privileges}" />
				</f:facet>
				<h:outputText value="#{item.roleFk}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{msg.realLifeRole}" />
				</f:facet>
				<h:outputText value="#{item.realLifeRole}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{msg.lastLogin}" />
				</f:facet>
				<h:outputText value="#{item.lastLoginDate}  #{item.lastLoginTime}"></h:outputText>
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
