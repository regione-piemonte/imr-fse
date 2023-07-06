<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	
	<head>
		<link rel="stylesheet" href="css/style.css" />
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
		<f:loadBundle var="msg" basename="it.units.htl.web.messages" />
		<title>Remote O3-DPACS</title>
	</head>
	<body>
		
		<f:view locale="#{sessionScope.locale}" afterPhase="#{messageManager.resetMessage}">
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
							<h:form id="changePassword">
								<h:panelGrid columns="3" cellspacing="3">
									
									<h:outputLabel rendered="true" value="#{msg.oldPassword}"></h:outputLabel>
									<h:inputSecret id="oldPassword" tabindex="1" value="#{changePwdBackBean.oldPassword}" required="true"></h:inputSecret>
									<h:message for="oldPassword" errorClass="error"/>
									
									<h:outputLabel rendered="true" value="#{msg.newPassword}"></h:outputLabel>
									<h:inputSecret id="newPassword" tabindex="2" value="#{changePwdBackBean.newPassword}"  required="true" >
									<f:validateLength minimum="#{changePwdBackBean.minimumLength}"/>
									<f:validator validatorId="compareToValidator" />
            							<f:attribute name="compareToId" value="changePassword:oldPassword" />
            							<f:attribute name="message" value="PasswordsCannotBeTheSame" />
            							<f:attribute name="checkFor" value="diversity" />
            							<f:attribute name="enabled" value="#{changePwdBackBean.cannotRepeatPassword}" />
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
									<h:message for="newPassword" errorClass="error"/>
									
									<h:outputLabel rendered="true" value="#{msg.confirmNewPassword}"></h:outputLabel>
									<h:inputSecret id="confirmNewPassword" tabindex="3" value="#{changePwdBackBean.confirmNewPassword}" required="true">
										<f:validator validatorId="compareToValidator" />
            							<f:attribute name="compareToId" value="changePassword:newPassword" />
            							<f:attribute name="message" value="PasswordsMustMatch" />
            							<f:attribute name="checkFor" value="equality" />
            							<f:attribute name="enabled" value="1" />
									</h:inputSecret>
									<h:message for="confirmNewPassword" errorClass="error"/>
									
									
									<h:commandButton value="#{msg.changePwd}" action="#{changePwdBackBean.doChange}" tabindex="4"></h:commandButton>
								
								
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
		</f:view>
	</body>

</html>





