<!-- Comment to prevent IE6 bug -->
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>

<%
//setto le variabili di ambiente, l'ip del richiedente e l'url del jnlp
session.setAttribute("myIp",request.getServerName() );
session.setAttribute("ClientIp",request.getRemoteHost());
session.setAttribute("isHttps",request.getScheme());
session.setAttribute("myPort",request.getLocalPort()+"");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    
        <head>
            <link rel="stylesheet"  href="css/style.css" />
            <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
            <f:loadBundle var="msg" basename="it.units.htl.web.messages"/>
            <title>Remote O3-DPACS</title>
        </head>
        <body>
        
<f:view locale="#{sessionScope.locale}" afterPhase="#{messageManager.resetMessage}">
<%if(session.getAttribute("userBean")==null){ %>
<a4j:outputPanel id="prova">
<a4j:include viewId="commons/messagePanel.jsp" />
</a4j:outputPanel>
            <center>
                <div id="container">
                    <div id="header">
                        <table><tr>
                                <td align="left"><div id="logoSmall"></div></td>
                                <td align="left">
                                    <div id="headerTitle">O3-DPACS-WEB, The Web Interface to O3-DPACS</div>
                            </td></tr>
                        </table>
                    </div>
                    <div id="navigation">
                        <table id="navigationTable">
                            <tr><td align="left">
	                            <h:form id="indexform">
		                            Username: <h:inputText id="username" value="#{loginBackBean.username}" /> &nbsp; 
		                            Password: <h:inputSecret id="password" value="#{loginBackBean.password}" /> &nbsp; &nbsp;
		                            <h:commandButton value="#{msg.login}" action="#{loginBackBean.login}" />
		                            <a id="fgtPwdButton" onclick="document.getElementById('fp').component.show();" href="#" style="text-decoration: none; color: black;">
		                            	<h:outputText value="#{msg.forgotPwd}"/>
		                            </a>
	                            </h:form>
                            </td>
                            <td align="right">
                            <h:outputText value="#{msg.chooselanguage}"/>
                            <h:form id="languageForm">
                            <h:selectOneMenu  id="selectedLang" value="#{languageManager.language}" onchange="submit()" >
                            	<f:selectItems value="#{languageManager.locales}" />
                            </h:selectOneMenu>
                            </h:form></td>
                            </tr>
                        </table>
						<rich:modalPanel id="fp" minHeight="120" minWidth="450" autosized="true">
							<f:facet name="header">
								<h:outputText value="#{msg.forgotPwd}" />
							</f:facet>
							<f:facet name="controls">
							</f:facet>
								 <h:form id="fgtPwd">
								 	<h:outputText escape="false" value="#{msg.ForgotPasswordLabel}" />
								 	<br/>
								 	<div style="text-align: center; margin-top: 20px;">
			                            <h:inputText id="email" value="#{loginBackBean.email}" /> &nbsp; 
			                            <h:commandButton value="#{msg.ok}" action="#{loginBackBean.forgotPassword}" />
			                            <a onclick="document.getElementById('fp').component.hide()" href="#" style="text-decoration: none; color: black;"/>
			                            	<h:outputText value="#{msg.cancel}"/>
			                            </a>
		                            </div>
	                            </h:form>
						</rich:modalPanel>
                    </div>
                    <div id="wrapper">
                        <div id="contentTitle">O3-DPACS-WEB</div>
                        <div id="content">
                            <p><h:outputText value="#{msg.welcome}"/></p>
                            <p><h:outputText value="#{msg.enterName}"/></p>
                            
	                        
                        </div>
                        <!-- footer content -->
                        <%@ include file="commons/footer.inc.html" %>
                    </div>
                </div>
            </center>
<%}else{ %>
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
                        <div id="contentTitle">O3-DPACS-WEB</div>
                        <div id="content">
                            <p><h:outputText value="#{msg.welcome}"/></p>
                                               
                                <h:graphicImage id="alertImg" url="/img/dicom.png"></h:graphicImage>
                                <p><b><h:outputText value="#{msg.warning}"/></b></p>
                                <p><h:outputText value="#{msg.disclaimer}"></h:outputText></p>
                                <p><h:outputText value="#{msg.rwsSite}"></h:outputText></p>
								<p><h:outputText value="#{msg.CeMarked}#{licenceReader.markedTo}" rendered="#{licenceReader.markedTo!=null}"/>
								<h:outputText value="#{msg.NotCeMarked}" rendered="#{licenceReader.markedTo==null}"/></p>
                        </div>
                        
                        <!-- footer content -->
                        <f:subview id="footer">                 
                            <jsp:include page="/commons/footer.inc.html" />
                        </f:subview>
                        
                    </div>
                </div>
                
            </center>
<%} %>
            </f:view>
        </body>
    
</html>
                        
                        
                        
                        
                        
                        