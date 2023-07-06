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
            <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
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
                        <div id="contentTitle"><h:outputText value="#{msg.adminArea}"/></div>                        
                        <div id="content">
                        <p></p>
                        <h:outputLink styleClass="link" value="#{facesContext.externalContext.requestContextPath}/server/serverMenu.jspf" rendered="#{sessionScope.actionEnabled.ServerLifeCicle}" accesskey="6">
                        	<h:outputText value="[6] #{msg.serverLifeCycle}"/>
                        </h:outputLink> &nbsp;
                        <h:outputLink styleClass="link" value="#{facesContext.externalContext.requestContextPath}/runtime/runtimeSettings.jspf" rendered="#{sessionScope.actionEnabled.RuntimeSettings}">
                        	<h:outputText value="#{msg.runtimeSettings}"/>
                        </h:outputLink> &nbsp;
                        <h:outputLink styleClass="link" value="#{facesContext.externalContext.requestContextPath}/monitors/monitoring.jspf" accesskey="7">
                        	<h:outputText value="[7] #{msg.runtimeMonitor}"/>
                        </h:outputLink>&nbsp;
                        <h:outputLink styleClass="link" value="#{facesContext.externalContext.requestContextPath}/admin/dbConfig.jspf" accesskey="8">
                       		<h:outputText value="[8] #{msg.configuration}"/>
                        </h:outputLink>&nbsp;
                        <h:outputLink styleClass="link" value="#{facesContext.externalContext.requestContextPath}/admin/studiesVerification.jspf" 
                        rendered="#{sessionScope.actionEnabled.StudyVerificationStatus}"
                        accesskey="9">
                       		<h:outputText value="[9] #{msg.studyVerificationStatus}"/>
                        </h:outputLink>&nbsp;
                        <h:outputLink styleClass="link" value="#{facesContext.externalContext.requestContextPath}/admin/operatorStat.jspf"
                        	rendered="#{sessionScope.actionEnabled.OperatorStatistics}" >
                       		<h:outputText value="#{msg.operatorsStat}"/>
                        </h:outputLink>
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