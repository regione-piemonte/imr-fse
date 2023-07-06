<!-- Comment to prevent IE6 bug -->
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="it.units.htl.dpacs.helpers.GlobalConfigurationLoader"%>
<%@page import="it.units.htl.dpacs.helpers.ConfigurationSettings"%>
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
                        <div id="contentTitle">Runtime Monitor</div>
                        <div id="content">
                            <h:form><jsp:plugin type="applet" jreversion="1.4" code="DynamicDataChart.class" width="100%" height="480" codebase="./" archive="AppletMonitor.jar, jcommon-1.0.10.jar, jfreechart-1.0.6.jar"> 
                                    <jsp:params>
                                        <jsp:param name="server" value="<%=request.getServerName() %>" />
                                        <jsp:param name="port" value="<%=GlobalConfigurationLoader.getConfigParam(ConfigurationSettings.JMX_CONNECTOR_PORT)%>" />
                                        <jsp:param name="maxNumDays" value="86400"/>
                                    </jsp:params>
                            </jsp:plugin></h:form>
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