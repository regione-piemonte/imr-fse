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
<f:loadBundle var="msg" basename="it.units.htl.web.messages" />
<link rel="stylesheet"
	href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"></meta>
<title>Remote O3-DPACS</title>
	</head>
	<body>
		<script type="text/javascript">
			function delWarn(ver) {
				var obj = document.getElementById(ver);
				if (obj.checked) {
					var cf = confirm('<h:outputText value="#{msg.confirmAnonimized}" />');
					if (cf) {
						obj.checked = true;
					} else {
						obj.checked = false;
					}
				}
			}
		</script>
		<center>
			<div id="container">
				<!-- header content -->
				<f:subview id="header">
					<jsp:include page="/commons/header.inc.jsp" />
				</f:subview>
				<!-- navigation content -->
				<f:subview id="navigation">
					<jsp:include page="/commons/navigation.inc.jsp" />
				</f:subview>
				<div id="wrapper">
					<div id="contentTitle">
						<h:outputText value="#{msg.insertNode}" />
						<br />
					</div>

					<div class="contentNodeForm">
						<center>
							<p></p>
							<h:form id="NewNode">
								<h:outputText value="#{msg.fieldRequired}" />
								<h:panelGrid id="panelGrid" columns="3" styleClass="table100">
									<f:facet name="header">
										<h:outputText value="#{msg.node}" />
									</f:facet>
									<h:outputLabel rendered="true" value="#{msg.aetitle}" />
									<h:inputText id="aetitle" tabindex="1"
										value="#{nodeSaver.nodeToSave.aeTitle}" readonly="false"
										required="true">
										<f:validateLength minimum="1" maximum="16"></f:validateLength>
										<f:validator validatorId="validateAETitle" />
										<f:validator validatorId="validateAETitleAbsence" />
									</h:inputText>
									<h:message for="aetitle" errorClass="error" />

									<h:outputLabel rendered="true" value="#{msg.ip}" />
									<h:inputText id="ip" tabindex="2"
										value="#{nodeSaver.nodeToSave.ip}" readonly="false"
										required="true">
										<f:validator validatorId="validateIP" />
									</h:inputText>
									<h:message for="ip" errorClass="error" />

									<h:outputLabel rendered="true" value="#{msg.port}" />
									<h:inputText id="port" tabindex="3"
										value="#{nodeSaver.nodeToSave.port}" readonly="false"
										required="true">

										<f:validateLongRange minimum="1" maximum="65536" />
									</h:inputText>
									<h:message for="port" errorClass="error" />

									<h:outputLabel rendered="true"
										value="#{msg.preferredStorageFK}" />
									<h:selectOneMenu id="storageFK" tabindex="4"
										value="#{nodeSaver.physicalToSave.pk}">
										<f:selectItems
											value="#{physicalMediaBackBean.physicalMediaOptions}" />
									</h:selectOneMenu>
									<h:message for="storageFK" errorClass="error" />



									<h:outputText value="#{msg.isAnonimized}" />
									<h:selectBooleanCheckbox id="isAnonimized" tabindex="5"
										value="#{nodeSaver.nodeToSave.isAnonimized}"
										onclick="delWarn('NewNode:isAnonimized');" />
									<h:message for="isAnonimized" errorClass="error" />
									
									
									<h:outputText value="#{msg.removePatientId}" styleClass="indentedFormLabel"/>
									<h:selectBooleanCheckbox id="removePatientId" tabindex="5"
										value="#{nodeSaver.nodeToSave.removePatientId}"/>
									<h:message for="removePatientId" errorClass="error" />
									

									<h:outputText value="#{msg.transferSyntaxUID}" />
									<h:selectOneMenu id="transfertSyntaxUID" tabindex="6"
										value="#{nodeSaver.nodeToSave.transferSyntaxUid}">
										<f:selectItems
											value="#{knownNodesBackBean.nodeToSave.supportedTransferSyntaxes}" />
									</h:selectOneMenu>
									<h:message for="transfertSyntaxUID" errorClass="error" />

									<h:outputText value="#{msg.isImageMaskingEnabled}" />
									<h:selectBooleanCheckbox id="isImageMaskingEnabled"
										tabindex="7"
										value="#{nodeSaver.nodeToSave.isImageMaskingEnabled}" />
									<h:message for="isImageMaskingEnabled" errorClass="error" />

									<h:outputText value="#{msg.toVerify}" />
									<h:selectBooleanCheckbox id="toVerify" tabindex="8"
										value="#{nodeSaver.nodeToSave.toVerify}" />
									<h:message for="toVerify" errorClass="error" />

									<h:outputLabel value="#{msg.prefCallingAet}" />
									<h:inputText id="prefCallingAet" tabindex="9"
										value="#{nodeSaver.nodeToSave.prefCallingAet}" />
									<h:message for="prefCallingAet" errorClass="error" />

									<h:outputLabel value="#{msg.knownNodeWadoUrl}" />
									<h:inputText id="knownNodeWadoUrl" tabindex="10"
										value="#{nodeSaver.nodeToSave.wadoURL}">
									</h:inputText>
									<h:message for="knownNodeWadoUrl" errorClass="error" />

								</h:panelGrid>

								<h:panelGrid id="panelGridMf" columns="3"
									styleClass="table100 indented" headerClass="noHeader">
									<f:facet name="header">
										<h:outputText value="#{msg.multiFrame}" />
									</f:facet>

									<h:outputLabel rendered="true"
										value="#{msg.frameIncrementPointer}" />
									<h:selectOneMenu id="frameIncrementPointer" tabindex="10"
										value="#{nodeSaver.nodeToSave.frameIncrementPointer}">
										<f:selectItems
											value="#{knownNodesBackBean.node.supportedFrameIncrementPointers}" />

									</h:selectOneMenu>
									<h:message for="frameIncrementPointer" errorClass="error" />

									<h:outputText value="#{msg.frameTime}" />
									<h:inputText id="frameTime" tabindex="11"
										value="#{nodeSaver.nodeToSave.frameTime}" readonly="false"
										required="false">
										<f:validateDoubleRange minimum="0" maximum="1024" />
									</h:inputText>
									<h:message for="frameTime" errorClass="error" />

								</h:panelGrid>

								<h:panelGrid id="panelGrid2" columns="3" styleClass="table100">
									<f:facet name="header">
										<h:outputText value="#{msg.equipmentFK}" />
									</f:facet>
									<h:outputLabel rendered="true" value="#{msg.equipmentType}" />
									<h:selectOneMenu id="equipmentType" tabindex="12"
										value="#{nodeSaver.equipToSave.equipmentType}" required="true">
										<f:selectItem itemValue="WS" itemLabel="#{msg.workstation}" />
										<f:selectItem itemValue="MODALITY" itemLabel="#{msg.modality}" />
										<f:selectItem itemValue="RIS" itemLabel="#{msg.ris}" />
										<f:selectItem itemValue="MPPS" itemLabel="#{msg.mpps}" />
										<f:selectItem itemValue="OTHER" itemLabel="#{msg.other}" />
									</h:selectOneMenu>
									<h:message for="equipmentType" errorClass="error" />

									<h:outputLabel rendered="true" value="#{msg.manufacturer}" />
									<h:inputText id="manufacturer" tabindex="13" maxlength="64"
										value="#{nodeSaver.equipToSave.manufacturer}" required="true" />
									<h:message for="manufacturer" errorClass="error" />

									<h:outputLabel rendered="true" value="#{msg.institutionName}" />
									<h:inputText id="institutionName" tabindex="14" maxlength="64"
										value="#{nodeSaver.equipToSave.institutionName}"
										required="true" />
									<h:message for="institutionName" errorClass="error" />

									<h:outputLabel rendered="true" value="#{msg.stationName}" />
									<h:inputText id="stationName" tabindex="15" maxlength="16"
										value="#{nodeSaver.equipToSave.stationName}" required="true" />
									<h:message for="stationName" errorClass="error" />

									<h:outputLabel rendered="true"
										value="#{msg.institutionalDepartmentName}" />
									<h:inputText id="institutionalDepartmentName" maxlength="64"
										tabindex="16"
										value="#{nodeSaver.equipToSave.institutionalDepartmentName}" />
									<h:message for="institutionalDepartmentName" errorClass="error" />

									<h:outputLabel rendered="true"
										value="#{msg.manufacturersModelName}" />
									<h:inputText id="manufacturersModelName" tabindex="17"
										maxlength="64"
										value="#{nodeSaver.equipToSave.manufacturersModelName}" />
									<h:message for="manufacturersModelName" errorClass="error" />

									<h:outputLabel rendered="true"
										value="#{msg.deviceSerialNumber}" />
									<h:inputText id="deviceSerialNumber" tabindex="18"
										maxlength="64"
										value="#{nodeSaver.equipToSave.deviceSerialNumber}" />
									<h:message for="deviceSerialNumber" errorClass="error" />

									<h:commandButton tabindex="19" action="Save"
										actionListener="#{nodeSaver.saveNode}" value="#{msg.save}" />
									<h:commandButton tabindex="20" action="Save"
										value="[.] #{msg.back}" immediate="true" accesskey="." />
								</h:panelGrid>
							</h:form>

						</center>
						<p></p>
					</div>


					<!-- footer content -->
					<f:subview id="footer">
						<jsp:include page="/commons/footer.inc.html" />
					</f:subview>

				</div>
			</div>
		</center>
	</body>
</f:view>
</html>
