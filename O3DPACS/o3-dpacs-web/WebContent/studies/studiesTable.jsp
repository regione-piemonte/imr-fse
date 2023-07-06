
<!-- Comment to prevent IE6 bug -->
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@taglib uri="http://example.com/functions" prefix="o3" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="javax.faces.context.FacesContext"%><html xmlns="http://www.w3.org/1999/xhtml">
<f:view locale="#{sessionScope.locale}" afterPhase="#{messageManager.resetMessage}">
	<head>
	<script language="javascript">
	function openURL(url){
		window.open(url, 'provaWin');
		return false;
	}
	</script>
	
	<link rel="stylesheet"
		href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
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
	<div id="contentTitle"><h:outputText
		value="#{msg.studiesResults}" /></div>
	<div id="content">
	<h:form>
	
		<div class="backAction" style="text-align: left;">
				<h:commandButton value="[.] #{msg.backToSearch}" action="#{studiesResultsBackingBean.backToSearch}" accesskey="."></h:commandButton>
		</div>
		<center>
	<rich:datascroller align="center" for="tableStudies" maxPages="10"	page="#{StudyPageCounter.pageCounter}" id="sc1" renderIfSinglePage="false"/>
		<rich:dataTable value="#{studyList.studiesMatched}" var="item"
			width="100%" id="tableStudies" headerClass="headerTable" rows="50"
			styleClass="studiesTable" rowClasses="oddRow, evenRow"
			columnClasses="none,none,none,none,none,none,none,none,none,none,none,none" >
			<rich:column rendered="#{sessionScope.actionEnabled.ViewStudy}" style="text-align:center;"> 
				<f:facet name="header">
				</f:facet>
				<h:commandLink action="#{studiesResultsBackingBean.serieView}" style="#{('OFF' eq item.studyStatusLabel)?'visibility:hidden;':''}">
					<img src="../img/buttons/search.gif" width="25" height="25"
						border="0" alt="<h:outputText value="#{msg.view}" />" />
				</h:commandLink>
			</rich:column>
			<rich:column rendered="#{sessionScope.actionEnabled.DeprecateStudies}" style="text-align:center;">
				<f:facet name="header">
					<h:outputText value="" />
				</f:facet>
				<h:commandLink action="#{studiesResultsBackingBean.studyDetRemove}" style="#{('OFF' eq item.studyStatusLabel)?'visibility:hidden;':''}">
					<img src="../img/buttons/trash.gif" width="25" height="25"
						border="0" alt="<h:outputText value="#{msg.remove}" />" />
				</h:commandLink>
			</rich:column>
			<rich:column rendered="#{sessionScope.actionEnabled.OpenRWSLite}" style="text-align:center;">
				<f:facet name="header">
				</f:facet>
				
					<%
					String rURL = request.getRequestURL().toString();
					String cP = request.getContextPath();
					String addr = rURL.substring(0, rURL.indexOf(cP));
					String jnlpURL = addr+cP+"/JnlpGenerator";
					%>
					
					<a 
					href="<%=jnlpURL%>
					<h:outputText value="?accessionNumber=#{item.accessionNumber}&patientId=#{item.patientId}" rendered="#{o3:getConfigParam('RwsIntegrationType') == 'QueryToPacs'}"/>
					<h:outputText value="?studyURL=#{item.wadoURL}" rendered="#{o3:getConfigParam('RwsIntegrationType') != 'QueryToPacs'}"/>" 
					onclick="openURL(this.href); return false;" style="<h:outputText value="#{('OFF' eq item.studyStatusLabel)?'visibility:hidden;':''}" />">				
					<img src="../img/buttons/o3rws.gif" width="25" height="25"
					border="0" alt="<h:outputText value="#{msg.openOnRWS}" />" />
				</a>
				
			</rich:column>
			<rich:column sortBy="#{item.studyDate}" style="text-align:center;">
				<f:facet name="header">
					<h:outputText value="#{msg.date}" />
				</f:facet>
				<h:outputText value="#{item.studyDate}">
					<f:convertDateTime pattern="dd/MM/yyyy" />
				</h:outputText>
			</rich:column>
			<rich:column style="text-align:center;">
				<f:facet name="header">
					<h:outputText value="#{msg.time}" />
				</f:facet>
				<h:outputText value="#{item.studyTime}">
					<f:convertDateTime pattern="HH:mm:ss" />
				</h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.accessionNumber}" style="text-align:center;">
				<f:facet name="header">
					<h:outputText value="#{msg.accessionNumber}" />
				</f:facet>
				<h:outputText value="#{item.accessionNumber}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.modalitiesInStudy}" style="text-align:center;">
				<f:facet name="header">
					<h:outputText value="#{msg.modality}" />
				</f:facet>
				<h:outputText value="#{item.modalitiesInStudy}"></h:outputText>
			</rich:column>
			<rich:column >
				<f:facet name="header">
					<h:outputText value="#{msg.description}" />
				</f:facet>
				<h:outputText value="#{item.studyDescription}"></h:outputText>
			</rich:column>
			<rich:column >
				<f:facet name="header">
					<h:outputText value="#{msg.firstName}" />
				</f:facet>
				<h:outputText value="#{item.firstName}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.lastName}" >
				<f:facet name="header">
					<h:outputText value="#{msg.lastName}" />
				</f:facet>
				<h:outputText value="#{item.lastName}"></h:outputText>
			</rich:column>
			<rich:column sortBy="#{item.patientId}">
				<f:facet name="header">
					<h:outputText value="#{msg.patientId}" />
				</f:facet>
				<h:outputText value="#{item.patientId}"></h:outputText>
			</rich:column>
			<rich:column style="text-align:center;">
				<f:facet name="header">
					<h:outputText value="#{msg.patientBirthDate}" />
				</f:facet>
				<h:outputText value="#{item.birthDate}"></h:outputText>
			</rich:column>
			<rich:column style="text-align:center;">
				<f:facet name="header">
					<h:outputText value="#{msg.status}" />
				</f:facet>
				<h:outputText value="#{item.studyStatusLabel}"></h:outputText>
			</rich:column>
			<rich:column style="text-align:center;">
				<f:facet name="header">
					<h:outputText value="#{msg.images}" />
				</f:facet>
				<h:outputText value="#{item.numberOfStudyRelatedInstances}"></h:outputText>
			</rich:column>
		</rich:dataTable>
		</center>
	</h:form></div>
	<!-- footer content --> <f:subview id="footer">
		<jsp:include page="/commons/footer.inc.html" />
	</f:subview></div>
	</center>
	</body>
</f:view>
</html>