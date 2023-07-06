<!-- Comment to prevent IE6 bug -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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
	<f:loadBundle var="msg" basename="it.units.htl.web.messages" />
	<title>Remote O3-DPACS</title>
	</head>
	<body>
	<center>
	<div id="container">
	<f:subview id="header"><jsp:include page="/commons/header.inc.jsp" /></f:subview>
	<f:subview id="navigation"><jsp:include page="/commons/navigation.inc.jsp" /></f:subview>
	<div id="wrapper">
	<div id="contentTitle">O3-DPACS-WEB</div>
	<div id="content" style="vertical-align: top;">


	<rich:modalPanel id="workingPanel" zindex="2000">
		<br/><br/>
		<center><img src="../img/loading.gif" /></center>
	</rich:modalPanel>
	
	
	<div class="instructions">
		<h:outputText value="#{msg.forwardInstructions}"></h:outputText>
	</div>

			<a4j:form	id="sourceForm">
			<h:panelGrid columns="2">
			<rich:panel id="srcPanel" header="#{msg.selectSource}" headerClass="merge-header">


				<a4j:outputPanel id="sourceResults">
					<div id="sourceTreePanel"
						style="width: 400px; height: 350px; overflow: auto;">
						<rich:tree switchType="client" value="#{nodesList.nodesTree}" treeNodeVar="treeNode"
						dropListener="#{treeManager.processNodeDrop}" reRender="srcTree,dstTree"  
						var="item" nodeFace="#{treeNode.parent.parent == null ? 'node' : 'leaf'}" id="srcTree" oncomplete="#{rich:component('workingPanel')}.hide();">
						<rich:treeNode id="Node" icon="../img/node.gif" iconLeaf="../img/node.gif" data="#{item}" acceptedTypes="targetNode" dropValue="#{item.pk}"
							type="node" reRender="srcTree,dstTree"
							ajaxSubmitSelection="false" ondrop="#{rich:component('workingPanel')}.show();"
							dragType="sourceNode" dragValue="#{item.pk}">
							<h:outputText value="#{item.aeTitle}" />
						
						</rich:treeNode>
						
						<rich:treeNode id="targetNode" 
							type="leaf" iconLeaf="../img/nodeIn.gif"  reRender="srcTree,dstTree"
							ajaxSubmitSelection="false" >
							<h:outputText value="#{item.aeTitle}" />
							
							<a4j:commandButton image="../img/delete.gif" styleClass="treeButton" onclick="if(!confirm('#{msg.sureDeleteBinding}'))return false;#{rich:component('workingPanel')}.show();" actionListener="#{treeManager.removeNodeBinding}" data="#{item.pk}_#{treeNode.parent.data.pk}" reRender="srcTree,dstTree">
								<a4j:support event="oncomplete" reRender="srcTree,dstTree" onbeforedomupdate="#{rich:component('workingPanel')}.hide();"></a4j:support>
							</a4j:commandButton>							
														
							
						</rich:treeNode>
						
					</rich:tree></div>
				</a4j:outputPanel>
			</rich:panel>		
			<rich:panel id="dstPanel" header="#{msg.selectTarget}" headerClass="merge-header">

				<a4j:outputPanel id="targetResults">
					<div id="targetTreePanel"
						style="width: 400px; height: 350px; overflow: auto;">
						<rich:tree switchType="client" value="#{nodesList.targetNodesTree}" dropListener="#{treeManager.processNodeDrop}" 
						reRender="srcTree,dstTree" var="item" treeNodeVar="treeNode" 
						nodeFace="#{treeNode.parent.parent == null ? 'node' : 'leaf'}" id="dstTree" oncomplete="#{rich:component('workingPanel')}.hide();">
						<rich:treeNode id="Node" icon="../img/node.gif" iconLeaf="../img/node.gif" data="#{item}" acceptedTypes="sourceNode" dropValue="#{item.pk}"
							type="node" ajaxSubmitSelection="false" reRender="srcTree,dstTree"  ondrop="#{rich:component('workingPanel')}.show();"
							dragType="targetNode" dragValue="#{item.pk}">
							<h:outputText value="#{item.aeTitle}" />
						</rich:treeNode>
						
						<rich:treeNode id="sourceNode" 
							type="leaf" iconLeaf="../img/nodeOut.gif"  reRender="srcTree,dstTree"
							ajaxSubmitSelection="false" >
							<h:outputText value="#{item.aeTitle}" />
							
							<a4j:commandButton image="../img/delete.gif" styleClass="treeButton" onclick="if(!confirm('#{msg.sureDeleteBinding}'))return false;#{rich:component('workingPanel')}.show();" actionListener="#{treeManager.removeNodeBinding}" data="#{item.pk}_#{treeNode.parent.data.pk}" reRender="srcTree,dstTree">
								<a4j:support event="oncomplete" reRender="srcTree,dstTree" onbeforedomupdate="#{rich:component('workingPanel')}.hide();"></a4j:support>
							</a4j:commandButton>							
							
						</rich:treeNode>
					</rich:tree></div>
				</a4j:outputPanel>
				
			</rich:panel>
			</h:panelGrid>			
			</a4j:form>		
	</div>
	<f:subview id="footer"><jsp:include page="/commons/footer.inc.html" /></f:subview></div>
	</div>
	</center>
	</body>
</f:view>
</html>