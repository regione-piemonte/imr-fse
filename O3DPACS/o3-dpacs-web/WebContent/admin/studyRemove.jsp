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
                        <div id="contentTitle"><h:outputText value="#{msg.removeStudy}"/></div>
                        <div id="contentRemove">
                            <h:form><p></p>
                            <b><h:outputText value="#{msg.studyDetails}"/></b><p></p>
                                <h:panelGrid columns="2" styleClass="table100" rowClasses="oddRow, evenRow" cellspacing="3" columnClasses="col25,col75">
                                    <h:outputLabel rendered="true" value="#{msg.patientFirstName}"></h:outputLabel>
                                    <h:outputText value="#{selectedStudy.firstName}"></h:outputText>
                                    <h:outputLabel rendered="true" value="#{msg.patientLastName}"></h:outputLabel>
                                    <h:outputText value="#{selectedStudy.lastName}"></h:outputText>
                                    <h:outputLabel rendered="true" value="#{msg.patientBirthDate}"></h:outputLabel>
                                    <h:outputText value="#{selectedStudy.birthDate}"></h:outputText>
                                    <h:outputLabel rendered="true" value="#{msg.patientId}"></h:outputLabel>
                                    <h:outputText value="#{selectedStudy.patientId}"></h:outputText> 
                                    <h:outputLabel rendered="true" value="#{msg.modality}"></h:outputLabel>
                                    <h:outputText value="#{selectedStudy.modalitiesInStudy}"></h:outputText>
                                    <h:outputLabel rendered="true" value="#{msg.images}"></h:outputLabel>
                                    <h:outputText value="#{selectedStudy.numberOfStudyRelatedInstances}"></h:outputText>
                                    <h:outputLabel rendered="true" value="#{msg.studyDate}"></h:outputLabel>
                                    <h:outputText value="#{selectedStudy.studyDate}"></h:outputText>
                                    <h:outputLabel rendered="true" value="#{msg.studyTime}"></h:outputLabel>
                                    <h:outputText value="#{selectedStudy.studyTime}"></h:outputText>
                                    <h:outputLabel rendered="true" value="#{msg.studyDescription}"></h:outputLabel>
                                    <h:outputText value="#{selectedStudy.studyDescription}"></h:outputText>
                                </h:panelGrid><p></p>
                                <hr><p></p>
                                <b><h:outputText value="#{msg.actions}"/></b><p></p>
                                <h:commandButton value="#{msg.delete}" action="#{studiesResultsBackingBean.studyRemove}" onclick="return confirm('Are you sure you want to delete this Study?')"></h:commandButton>
                                
                                
                                <p></p>
                                <b><h:outputText value="#{msg.reasons}"/></b>
                                <h:inputTextarea label="ATTENTION! " styleClass="reason" id="reason" value="#{studiesResultsBackingBean.actionReason}" rows="3" cols="35" required="true">
                                	<f:validateLength maximum="255"/>
                                </h:inputTextarea>
                                <h:message for="reason" errorClass="error"/>
                                <p></p>
                                <hr><p></p>
                                </h:form><h:form>
                                <h:commandButton value="#{msg.back}" action="back"></h:commandButton>
                            </h:form><p></p>
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