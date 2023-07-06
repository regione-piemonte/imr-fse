<!-- Comment to prevent IE6 bug -->
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <f:view locale="#{sessionScope.locale}" afterPhase="#{messageManager.resetMessage}">
        <head>
            <link rel="stylesheet"  href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
            <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
            <f:loadBundle var="equipment" basename="it.units.htl.web.messages"/>
            <title>Remote O3-DPACS</title>
        </head>
        <body>
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
                        <div id="contentTitle"><h:outputText value="#{equipment.modifyEquip}"/></div>
                        
                        <div id="content320"><center><p></p><h:form>
                                    <h:panelGrid id="panelGrid" columns="2" headerClass="searchpar" styleClass="table100">
                                        <f:facet name="header">
                                            <h:outputText value="#{equipment.editEquipment}" />
                                        </f:facet>
                                        <h:outputLabel rendered="true" value="#{equipment.equipmentType}" />
                                        <h:inputText id="equipmentType" tabindex="1" value="#{equipmentBackBean.equipment.equipmentType}" />
                                        <h:outputLabel rendered="true" value="#{equipment.manufacturer}" />
                                        <h:inputText id="manufacturer" tabindex="2" value="#{equipmentBackBean.equipment.manufacturer}" />
                                        <h:outputLabel rendered="true" value="#{equipment.institutionName}" />
                                        <h:inputText id="institutionName" tabindex="3" value="#{equipmentBackBean.equipment.institutionName}" />
                                        <h:outputLabel rendered="true" value="#{equipment.stationName}" />
                                        <h:inputText id="stationName" tabindex="4" value="#{equipmentBackBean.equipment.stationName}" />
                                        <h:outputLabel rendered="true" value="#{equipment.institutionalDepartmentName}" />
                                        <h:inputText id="institutionalDepartmentName" tabindex="5" value="#{equipmentBackBean.equipment.institutionalDepartmentName}" />
                                        <h:outputLabel rendered="true" value="#{equipment.manufacturersModelName}" />
                                        <h:inputText id="manufacturersModelName" tabindex="6" value="#{equipmentBackBean.equipment.manufacturersModelName}" />
                                        <h:outputLabel rendered="true" value="#{equipment.deviceSerialNumber}" />
                                        <h:inputText id="deviceSerialNumber" tabindex="7" value="#{equipmentBackBean.equipment.deviceSerialNumber}" />
                                        <h:outputLabel rendered="true" value="#{equipment.dateOfLastCalibration} [dd/mm/yyyy]" />
                                        <h:inputText id="dateOfLastCalibration" tabindex="8" value="#{equipmentBackBean.equipment.dateOfLastCalibration}">
                                            <f:convertDateTime type="date" dateStyle="default" pattern="dd/MM/yyyy"/>
                                        </h:inputText>
                                        <h:outputLabel rendered="true" value="#{equipment.timeOfLastCalibration} [h.mm]" />
                                        <h:inputText id="timeOfLastCalibration" tabindex="9" value="#{equipmentBackBean.equipment.timeOfLastCalibration}">
                                            <f:convertDateTime type="time" timeStyle="short" pattern="HH.mm"/>
                                        </h:inputText>
                                        <h:outputLabel rendered="true" value="#{equipment.lastCalibratedBy}"/>
                                        <h:inputText id="lastCalibratedBy" tabindex="10" value="#{equipmentBackBean.equipment.lastCalibratedBy}"/>
                                        <h:outputLabel rendered="true" value="#{equipment.conversionType}"/>
                                        <h:inputText id="conversionType" tabindex="11" value="#{equipmentBackBean.equipment.conversionType}"/>
                                        <h:outputLabel rendered="true" value="#{equipment.secondaryCaptureDeviceId}"/>
                                        <h:inputText id="secondaryCaptureDeviceID" tabindex="12" value="#{equipmentBackBean.equipment.secondaryCaptureDeviceId}"/>
                                        <h:commandButton tabindex="0" action="Save" actionListener="#{equipmentBackBean.saveEquipment}" value="#{equipment.save}" />
                                        <h:commandButton tabindex="0" action="Save" value="#{equipment.back}" immediate="true" />
                                    </h:panelGrid>		
                            </h:form></center><p></p>
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
