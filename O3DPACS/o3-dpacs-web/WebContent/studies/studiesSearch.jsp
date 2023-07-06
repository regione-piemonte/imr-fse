
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
<f:view locale="#{sessionScope.locale}"
	afterPhase="#{messageManager.resetMessage}">
	<head>
<link rel="stylesheet"
	href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<f:loadBundle var="msg" basename="it.units.htl.web.messages" />
<title>Remote O3-DPACS</title>
	</head>
	<body>

		<script type="text/javascript">
			function resetForm() {
				document.getElementById('mainform:firstname').value = '';
				document.getElementById('mainform:lastname').value = '';
				document.getElementById('mainform:birthday').value = '';
				document.getElementById('mainform:patientID').value = '';
				document.getElementById('mainform:studyDateInputDate').value = '';
				document.getElementById('mainform:accessNumber').value = '';
				document.getElementById('mainform:modality').value = '';
				document.getElementById('mainform:studyId').value = '';
				document.getElementById('mainform:studyDescription').value = '';
				document.getElementById('mainform:studyInstanceUID').value = '';
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
						<h:outputText value="#{msg.studiesSearch}" />
					</div>
					<div id="content400">
						<h:form id="mainform">
							<p></p>
							<h:panelGrid styleClass="table100" columns="3" cellspacing="3"
								columnClasses="studySearchLabelColumn,studySearchInputColumn,studySearchMessageColumn">

								<h:outputLabel rendered="true" value="#{msg.patientFirstName}"></h:outputLabel>
								<h:inputText id="firstname" tabindex="1"
									value="#{studyList.firstName}"></h:inputText>
								<h:message for="firstname"></h:message>
								<h:outputLabel rendered="true" value="#{msg.patientLastName}"></h:outputLabel>
								<h:inputText id="lastname" tabindex="2"
									value="#{studyList.lastName}"></h:inputText>
								<h:message for="lastname" />
								<h:outputLabel rendered="true" value="#{msg.patientBirthDate}"></h:outputLabel>
								<h:inputText id="birthday" tabindex="3"
									value="#{studyList.birthDate}">
									<f:convertDateTime type="date" pattern="dd/MM/yyyy" />
								</h:inputText>
								<h:message for="birthday" />
								<h:outputLabel rendered="true" value="#{msg.patientId}"></h:outputLabel>
								<h:inputText id="patientID" tabindex="4"
									value="#{studyList.patientId}"
									validator="#{studyList.validateWildcardsFreeFields}"></h:inputText>
								<h:message for="patientID" />
								<h:outputLabel rendered="true" value="──────────────"></h:outputLabel>
								<h:outputLabel rendered="true" value=""></h:outputLabel>
								<h:outputLabel rendered="true" value=""></h:outputLabel>
								<h:outputLabel rendered="true"
									value="#{msg.studyDate} (dd/mm/year)"></h:outputLabel>
								<rich:calendar id="studyDate" value="#{studyList.studyDate}"
									datePattern="dd/MM/yyyy" showApplyButton="false"
									cellWidth="24px" cellHeight="22px" style="width:200px" />
								<h:message for="studyDate" />
								<h:outputLabel rendered="true" value="#{msg.accessionNumber}"></h:outputLabel>
								<h:inputText id="accessNumber" tabindex="5"
									value="#{studyList.accessNumber}"
									validator="#{studyList.validateWildcardsFreeFields}"></h:inputText>
								<h:message for="accessNumber" />
								<h:outputLabel rendered="true" value="#{msg.modality}"></h:outputLabel>
								<h:inputText id="modality" tabindex="6"
									value="#{studyList.modalitiesInStudy}"
									validator="#{studyList.validateWildcardsFreeFields}"></h:inputText>
								<h:message for="modality" />
								<h:outputLabel rendered="true" value="#{msg.studyId}"></h:outputLabel>
								<h:inputText id="studyId" tabindex="7"
									value="#{studyList.studyId}"
									validator="#{studyList.validateWildcardsFreeFields}"></h:inputText>
								<h:message for="studyId" />
								<h:outputLabel rendered="true" value="#{msg.studyDescription}"></h:outputLabel>
								<h:inputText id="studyDescription" tabindex="8"
									value="#{studyList.studyDescription}"></h:inputText>
								<h:message for="studyDescription" />
								<h:outputLabel rendered="true" value="#{msg.studyInstanceUID}"></h:outputLabel>
								<h:inputText id="studyInstanceUID" tabindex="9"
									value="#{studyList.studyInstanceUID}"
									validator="#{studyList.validateWildcardsFreeFields}"></h:inputText>
								<h:message for="studyInstanceUID" />

								<h:commandButton value="[t] #{msg.search}" style="width: auto;"
									action="#{studySearchBackBean.studyView}" accesskey="t"
									tabindex="12"></h:commandButton>

								<h:commandButton value="#{msg.reset}" onclick="resetForm()"
									style="width: auto;"
									actionListener="#{studySearchBackBean.resetFormAL}"
									tabindex="13"></h:commandButton>

								<h:outputLabel rendered="true" value="" style="width: auto;" />
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