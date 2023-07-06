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
                        <div id="contentTitle"><h:outputText value="#{msg.editUser}"/></div>
                        <div id="content"><h:form>
                                    <h:panelGrid id="panelGrid" columns="3" cellspacing="3">
                                        <f:facet name="header">
                                            <h:outputText value="#{msg.userEdit}" />
                                        </f:facet>
                                        <h:outputLabel rendered="true" value="#{msg.lastName}" />
                                        <h:inputText id="lastname" tabindex="1" value="#{userBackBean.user.lastName}" required="true" />
                                        <h:message for="lastname" errorClass="error"/>
                                        
                                        <h:outputLabel rendered="true" value="#{msg.firstName}" />
                                        <h:inputText id="firstname" tabindex="2" value="#{userBackBean.user.firstName}" required="true" />
                                        <h:message for="firstname" errorClass="error"/>
                                        
                                        <h:outputLabel rendered="true" value="#{msg.username}" />
                                        <h:inputText id="username" tabindex="3" value="#{userBackBean.user.userName}" required="true" readonly="#{not empty userBackBean.user.pk}">
                                        	<f:validateLength minimum="3" maximum="59"/>
                                       	</h:inputText>
                                        <h:message for="username" errorClass="error"/>
                                        
                                        <h:outputLabel rendered="true" value="#{msg.email}" />
                                        <h:inputText id="email" tabindex="4" value="#{userBackBean.user.email}" validator="#{userBackBean.validateEmail}">
                                        	
                                       	</h:inputText>
                                        <h:message for="email" errorClass="error"/>
                                        
                                        <h:outputLabel rendered="#{empty userBackBean.user.pk}" value="#{msg.newpassword}" />
                                        <h:inputSecret rendered="#{empty userBackBean.user.pk}" id="newpassword" tabindex="5" value="#{userBackBean.user.password}" redisplay="true" required="true">
                                        	<f:validateLength minimum="#{changePwdBackBean.minimumLength}"/>
                                        	<f:validator validatorId="strengthValidator" />
		            							<f:attribute name="message_LettersMand" value="LettersMandatory" />
												<f:attribute name="message_CasesMand" value="BothCasesMandatory" />
												<f:attribute name="message_DigitsMand" value="DigitsMandatory" />
												<f:attribute name="message_SymbolsMand" value="SymbolsMandatory" />
												
												<f:attribute name="pattern_LettersMand" value="#{changePwdBackBean.lettersMandPattern}" />
												<f:attribute name="pattern_CasesMand" value="#{changePwdBackBean.casesMandPattern}" />
												<f:attribute name="pattern_DigitsMand" value="#{changePwdBackBean.digitsMandPattern}" />
												<f:attribute name="pattern_SymbolsMand" value="#{changePwdBackBean.symbolsMandPattern}" />
                                        </h:inputSecret>
                                        <h:message rendered="#{empty userBackBean.user.pk}" for="newpassword" errorClass="error"/>
                                       
                                        
                                        <h:outputLabel rendered="true" value="#{msg.privileges}" />
                                        
                                        <h:selectOneMenu id="privileges" tabindex="7" value="#{userBackBean.groupId}">
											<f:selectItems value="#{userBackBean.groupIdItems}" />
										</h:selectOneMenu>
										
                                       
                                        <h:message for="privileges" errorClass="error"/>
                                        
                                        <h:outputLabel rendered="true" value="#{msg.realLifeRole}" />
                                        <h:inputText id="realliferole" tabindex="8" value="#{userBackBean.user.realLifeRole}" />
                                        <h:message for="realliferole" errorClass="error"/>
                                        
                                        <h:commandButton tabindex="0" action="#{userBackBean.saveUser}" value="#{msg.save}" >
                                        </h:commandButton>
                                        <h:commandButton tabindex="9" action="Save" value="[.] #{msg.cancel}" immediate="true" accesskey="."/>
                                    </h:panelGrid>		
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
