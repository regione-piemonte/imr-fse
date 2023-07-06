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
            <f:loadBundle var="msg" basename="it.units.htl.web.messages"/>
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
                        <div id="contentTitle"><h:outputText value="#{msg.operationDetails}"/></div>
                        <div id="content">
                            <h:form><p></p>
                                <b><h:outputText value="#{msg.studyDetails}"/></b><p></p>
                                <h:panelGrid id="studygrid" columns="2" styleClass="table100" rowClasses="oddRow, evenRow" cellspacing="3">
                                    <h:outputLabel rendered="true" value="#{msg.patientId}"></h:outputLabel>
                                    <h:outputText value="#{recoveryBackBean.selectedRecovery.patientFK}"></h:outputText> 
                                    <h:outputLabel rendered="true" value="#{msg.patientName}"></h:outputLabel>
                                    <h:outputText value="#{recoveryBackBean.selectedRecovery.patientName}"></h:outputText> 
                                    <h:outputLabel rendered="true" value="#{msg.studySerieTime}"></h:outputLabel>
                                    <h:outputText value="#{recoveryBackBean.selectedRecovery.studyTime}"></h:outputText>
                                    <h:outputLabel rendered="true" value="#{msg.studySeriePath}"></h:outputLabel>
                                    <h:outputText value="#{recoveryBackBean.selectedRecovery.pathName}"></h:outputText>
                                </h:panelGrid><p></p><hr><p></p>
                                <b>Action's Details</b><p></p>
                                <h:panelGrid id="actgrid" columns="2" styleClass="table100" rowClasses="oddRow, evenRow" cellspacing="3">
                                    <h:outputLabel rendered="true" value="#{msg.user}"></h:outputLabel>
                                    <h:outputText value="#{recoveryBackBean.selectedRecovery.user}"></h:outputText>
                                     
                                    <h:outputLabel rendered="true" value="#{msg.details}"></h:outputLabel>
                                    <h:outputText value="#{recoveryBackBean.selectedRecovery.description}"></h:outputText>
                                    <h:outputLabel rendered="true" value="#{msg.removalDateTime}"></h:outputLabel>
                                    <h:outputText value="#{recoveryBackBean.selectedRecovery.time}"></h:outputText>
                                </h:panelGrid><p></p>
                                <h:commandButton action="back" value="#{msg.back}" />
                            </h:form>
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

