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
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
	<f:loadBundle var="node" basename="it.units.htl.web.messages" />
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
	<div id="contentTitle"><h:outputText value="#{node.nodeView}" /></div>

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
		<h:commandButton value="[0] #{node.createNode}" accesskey="0"
			actionListener="#{knownNodesBackBean.createNode}" action="Create"
			rendered="#{sessionScope.actionEnabled.CreateNode}"></h:commandButton>
	</h:form>
	<p></p>
	</div>
	<div id="content"><h:form>
		<rich:datascroller id="topScroller" align="left" for="nodes" maxPages="10"
			page="#{NodesPageCounter.pageCounter}" reRender="bottomScroller" />
		<rich:spacer height="30" />
		<rich:dataTable binding="#{knownNodesBackBean.nodesTable}" id="nodes"  
			var="item" rows="30" value="#{knownNodesBackBean.list}" styleClass="table100"
			rowClasses="oddRow, evenRow"
			columnClasses="column10,none,none,none,none,none,none,none,none,none">
			<rich:column rendered="#{sessionScope.actionEnabled.ModifyNode}"  >
				<f:facet name="header">
					<h:outputText value="" />
				</f:facet>
				<h:commandButton value="#{node.edit}" action="Edit"
					actionListener="#{knownNodesBackBean.editNode}" />
					
			</rich:column>
			<rich:column rendered="#{sessionScope.actionEnabled.DeleteNode}">
				<f:facet name="header">
					<h:outputText value="" />
				</f:facet>
				<h:commandButton value="#{node.delete}" 
					actionListener="#{knownNodesBackBean.deleteNode}" 
					onclick="return confirm('Are you sure you want to delete this AeTitle?')" 
					/>
			</rich:column>
			<rich:column sortBy="#{item.aeTitle}" filterBy="#{item.aeTitle}" filterEvent="onchange" >
				<f:facet name="header">
					<h:outputText value="#{node.aetitle}" />
				</f:facet>
				<h:outputText value="#{item.aeTitle}"></h:outputText>
			</rich:column>
			<rich:column filterBy="#{item.ip}" filterEvent="onblur" >
				<f:facet name="header">
					<h:outputText value="#{node.ip}" />
				</f:facet>
				<h:outputText value="#{item.ip}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{node.port}" />
				</f:facet>
				<h:outputText value="#{item.port}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{node.urlToStudy}" />
				</f:facet>
				<h:outputText value="#{item.physicalMedia.urlToStudy}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{node.equipmentFK}" />
				</f:facet>
				<h:outputText value="#{item.equipment.stationName}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{node.isAnonimized}" />
				</f:facet>
				<h:outputText value="#{item.isAnonimized}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{node.transferSyntaxUID}" />
				</f:facet>
				<h:outputText value="#{item.transferSyntaxUid}"></h:outputText>
			</rich:column>
			
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{node.frameTime}" />
				</f:facet>
				<h:outputText value="#{item.frameTime}"></h:outputText>
			</rich:column>
			
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{node.toVerify}" />
				</f:facet>
				<h:outputText value="#{item.toVerify}"></h:outputText>
			</rich:column>
			
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{node.prefCallingAet}" />
				</f:facet>
				<h:outputText value="#{item.prefCallingAet}"></h:outputText>
			</rich:column>
			
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{node.knownNodeWadoUrl}" />
				</f:facet>
				<h:outputText value="#{item.wadoURL}"></h:outputText>
			</rich:column>
			
		</rich:dataTable>
		<rich:spacer height="10" />
		<rich:datascroller id="bottomScroller" align="left" for="nodes" maxPages="30"
			page="#{NodesPageCounter.pageCounter}" reRender="topScroller" />
		<rich:spacer height="20" />
		</h:form></div>
	<!-- footer content --> <f:subview id="footer">
		<jsp:include page="/commons/footer.inc.html" />
	</f:subview></div>
	</div>
	</center>
	</body>
</f:view>
</html>
