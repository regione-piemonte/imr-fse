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
            <link rel="stylesheet"  href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
            <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"></meta>
            <f:loadBundle var="msg" basename="it.units.htl.web.messages" />
            <title>Remote O3-DPACS</title>
        </head>
        <body>
    		    <script type="text/javascript">
					function delWarn(ver){
						var obj = document.getElementById(ver);
						if(obj.checked){
							var cf = confirm( '<h:outputText value="#{msg.confirmAnonimized}" />' );
							if(cf){
								obj.checked = true;
							}else{
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
                    	<f:loadBundle var="node" basename="it.units.htl.web.messages"/>
                        <div id="contentTitle"><h:outputText value="#{node.modifyNode}" /><br/>
                        </div>
                        
                        <div class="contentNodeForm"><center><p></p><h:form id="EditNode">
                        <h:outputText value="#{msg.fieldRequired}" />
                                    <h:panelGrid id="panelGrid" columns="3" headerClass="searchpar" styleClass="table100">
                                        <f:facet name="header">
                                            <h:outputText value="#{node.node}" />
                                        </f:facet>
                                        <h:outputLabel rendered="true" value="#{node.aetitle}" />
                                        <h:inputText id="aetitle" tabindex="1" value="#{knownNodesBackBean.node.aeTitle}" readonly="false" required="true">
                                        	<f:validateLength minimum="1" maximum="16"></f:validateLength>
                                        	<f:validator validatorId="validateAETitle"/>
                                        </h:inputText>
                                        <h:message for="aetitle" errorClass="error"/>
                                        <h:outputLabel rendered="true" value="#{node.ip}" />
                                        <h:inputText id="ip" tabindex="2" value="#{knownNodesBackBean.node.ip}" readonly="false" required="true">
                                            <f:validator validatorId="validateIP"/>
                                        </h:inputText>
                                        <h:message for="ip" errorClass="error"/>
                                        
                                        <h:outputLabel rendered="true" value="#{node.port}" />
                                        <h:inputText id="port" tabindex="3" value="#{knownNodesBackBean.node.port}" readonly="false" required="true">
                                            <f:validateLongRange minimum="1" maximum="65536"/>
                                        </h:inputText>
                                        <h:message for="port" errorClass="error"/>
                                        
                                        <h:outputLabel rendered="true" value="#{node.preferredStorageFK}" />
                                        <%-- defaultLabel="#{knownNodesBackBean.node.physicalMedia.urlToStudy}" --%>
                                        <h:selectOneMenu id="storageFK" tabindex="4" value="#{knownNodesBackBean.storageFK}" >
        										<f:selectItems value="#{physicalMediaBackBean.physicalMediaOptions}" />
    									</h:selectOneMenu>
                                        
                                        <h:message for="storageFK" errorClass="error"/>
                                        <h:outputText value="#{node.isAnonimized}" />
                                        <h:selectBooleanCheckbox id="isAnonimized" tabindex="5" value="#{knownNodesBackBean.node.isAnonimized}" onclick="delWarn('EditNode:isAnonimized');"/>
                                        <h:message for="isAnonimized" errorClass="error"/>
                                        
                                        
                                      <h:outputText value="#{node.removePatientId}" styleClass="indentedFormLabel"/>
                                      <h:selectBooleanCheckbox id="removePatientId" tabindex="5" value="#{knownNodesBackBean.node.removePatientId}"/>
                                      <h:message for="removePatientId" errorClass="error"/>
                                        
                                        <h:outputText value="#{node.transferSyntaxUID}" />
                                        <%-- defaultLabel="#{knownNodesBackBean.node.transferSyntaxUid}">--%>
                            			<h:selectOneMenu id="transfertSyntaxUID" tabindex="6" value="#{knownNodesBackBean.node.transferSyntaxUid}" >
        										<f:selectItems value="#{knownNodesBackBean.node.supportedTransferSyntaxes}" />
    									</h:selectOneMenu>
                                        <h:message for="transfertSyntaxUID" errorClass="error"/>
                                        
                                        <h:outputText value="#{node.isImageMaskingEnabled}" />
                                        <h:selectBooleanCheckbox id="isImageMaskingEnabled" tabindex="7" value="#{knownNodesBackBean.node.isImageMaskingEnabled}" />
                                        <h:message for="isImageMaskingEnabled" errorClass="error"/>
                                        
                                        <h:outputText value="#{node.toVerify}" />
                                        <h:selectBooleanCheckbox id="toVerify" tabindex="8" value="#{knownNodesBackBean.node.toVerify}" />
                                        <h:message for="toVerify" errorClass="error"/>
                                        
                                        <h:outputLabel rendered="true" value="#{node.prefCallingAet}" />
                                        <h:inputText id="prefCallingAet" tabindex="9" value="#{knownNodesBackBean.node.prefCallingAet}">
                                        </h:inputText>
                                        <h:message for="prefCallingAet" errorClass="error"/>
                                        
                                        <h:outputLabel rendered="true" value="#{node.knownNodeWadoUrl}" />
                                        <h:inputText id="knownNodeWadoUrl" tabindex="10" value="#{knownNodesBackBean.node.wadoURL}">
                                        </h:inputText>
                                        <h:message for="knownNodeWadoUrl" errorClass="error"/>
                                        
                                        </h:panelGrid>
                                        
                                       
                                       
                                       
                            			<h:panelGrid id="panelGridMf" columns="3" styleClass="table100 indented" headerClass="noHeader">
                                        <f:facet name="header">
                                            <h:outputText value="#{msg.multiFrame}" />
                                        </f:facet>
                                        
                                        <h:outputLabel rendered="true" value="#{msg.frameIncrementPointer}" />
                                        <h:selectOneMenu id="frameIncrementPointer" tabindex="20" value="#{knownNodesBackBean.node.frameIncrementPointer}">
                                            <f:selectItems value="#{knownNodesBackBean.node.supportedFrameIncrementPointers}"/>    
                                        </h:selectOneMenu>
                                        <h:message for="frameIncrementPointer" errorClass="error"/>
                                        
                                        <h:outputText value="#{msg.frameTime}" />
                                        <h:inputText id="frameTime" tabindex="21" value="#{knownNodesBackBean.node.frameTime}" readonly="false" required="false">
                                            <f:validateDoubleRange minimum="0" maximum="1024"/>
                                        </h:inputText>
                                        <h:message for="frameTime" errorClass="error"/>
                                        
                                       </h:panelGrid>
                                        
                                        

                                        <h:panelGrid id="EquipID" columns="2" headerClass="searchpar" styleClass="table100">
                                        <f:facet name="header">
                                            <h:outputText value="#{msg.editEquipment}" />
                                        </f:facet>
                                        <h:outputLabel rendered="true" value="#{msg.equipmentType}" />
                                        
                                        <h:selectOneMenu id="equipmentType" tabindex="30" value="#{knownNodesBackBean.node.equipment.equipmentType}" required="true">
                                            <f:selectItem
										        itemValue="WS"
										        itemLabel="#{msg.workstation}"/>
										    <f:selectItem
										        itemValue="MODALITY"
										        itemLabel="#{msg.modality}"/>
										    <f:selectItem
										        itemValue="RIS"
										        itemLabel="#{msg.ris}"/>
										    <f:selectItem
										        itemValue="MPPS"
										        itemLabel="#{msg.mpps}"/>
										    <f:selectItem
										        itemValue="OTHER"
										        itemLabel="#{msg.other}"/>                                           
                                        </h:selectOneMenu>
                                        
                                        
                                        <h:outputLabel rendered="true" value="#{msg.manufacturer}" />
                                        <h:inputText id="manufacturer" tabindex="31" value="#{knownNodesBackBean.node.equipment.manufacturer}" />
                                        <h:outputLabel rendered="true" value="#{msg.institutionName}" />
                                        <h:inputText id="institutionName" tabindex="32" value="#{knownNodesBackBean.node.equipment.institutionName}" />
                                        <h:outputLabel rendered="true" value="#{msg.stationName}" />
                                        <h:inputText id="stationName" tabindex="33" value="#{knownNodesBackBean.node.equipment.stationName}" />
                                        <h:outputLabel rendered="true" value="#{msg.institutionalDepartmentName}" />
                                        <h:inputText id="institutionalDepartmentName" tabindex="34" value="#{knownNodesBackBean.node.equipment.institutionalDepartmentName}" />
                                        <h:outputLabel rendered="true" value="#{msg.manufacturersModelName}" />
                                        <h:inputText id="manufacturersModelName" tabindex="35" value="#{knownNodesBackBean.node.equipment.manufacturersModelName}" />
                                        <h:outputLabel rendered="true" value="#{msg.deviceSerialNumber}" />
                                        <h:inputText id="deviceSerialNumber" tabindex="36" value="#{knownNodesBackBean.node.equipment.deviceSerialNumber}" />
                                        <h:outputLabel rendered="true" value="#{msg.dateOfLastCalibration} [dd/mm/yyyy]" />
                                        <h:inputText id="dateOfLastCalibration" tabindex="37" value="#{knownNodesBackBean.node.equipment.dateOfLastCalibration}">
                                            <f:convertDateTime type="date" dateStyle="default" pattern="dd/MM/yyyy"/>
                                        </h:inputText>
                                        <h:outputLabel rendered="true" value="#{msg.timeOfLastCalibration} [hh.mm]" />
                                        <h:inputText id="timeOfLastCalibration" tabindex="38" value="#{knownNodesBackBean.node.equipment.timeOfLastCalibration}">
                                            <f:convertDateTime type="time" timeStyle="short" pattern="HH.mm"/>
                                        </h:inputText>
                                        <h:outputLabel rendered="true" value="#{msg.lastCalibratedBy}"/>
                                        <h:inputText id="lastCalibratedBy" tabindex="39" value="#{knownNodesBackBean.node.equipment.lastCalibratedBy}"/>
                                        <h:outputLabel rendered="true" value="#{msg.conversionType}"/>
                                        <h:inputText id="conversionType" tabindex="40" value="#{knownNodesBackBean.node.equipment.conversionType}"/>
                                        <h:outputLabel rendered="true" value="#{msg.secondaryCaptureDeviceId}"/>
                                        <h:inputText id="secondaryCaptureDeviceID" tabindex="41" value="#{knownNodesBackBean.node.equipment.secondaryCaptureDeviceId}"/>
                                    </h:panelGrid>
                                    <h:panelGrid>
                                        <h:commandButton tabindex="50" action="Save" actionListener="#{knownNodesBackBean.updateNode}" value="#{node.save}" />
                                        <h:commandButton tabindex="51" action="Save" value="[.] #{node.back}" accesskey="." immediate="true" />
                                    </h:panelGrid>
                            </h:form>
                            
                            </center><p></p>
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
