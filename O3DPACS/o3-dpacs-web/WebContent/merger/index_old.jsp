<!-- Comment to prevent IE6 bug -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
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
		<center>
			<div id="container">
				<f:subview id="header"><jsp:include
						page="/commons/header.inc.jsp" /></f:subview>
				<f:subview id="navigation"><jsp:include
						page="/commons/navigation.inc.jsp" /></f:subview>
				<div id="wrapper">
					<div id="contentTitle">O3-DPACS-WEB</div>
					<div id="content" style="vertical-align: top;">
						<rich:modalPanel id="waitPanel">
							<f:facet name="header">
								<h:panelGroup>
									<h:outputText value="Waiting for search..." />
								</h:panelGroup>
							</f:facet>
							<f:facet name="controls">
								<h:panelGroup>
									<a4j:form>
										<a4j:commandLink
											onclick="#{rich:component('waitPanel')}.hide();"
											style="border: 0;">
											<h:graphicImage value="/img/close.png" style="cursor:pointer"
												id="hidelink">
											</h:graphicImage>
										</a4j:commandLink>
									</a4j:form>
								</h:panelGroup>
							</f:facet>
							<h:outputText value="Waiting for search..."></h:outputText>
						</rich:modalPanel>
						<a4j:form id="sourceForm">
							<h:panelGrid columns="2"
								columnClasses="mergeToolColumn1 mergeToolColumn2">
								<rich:panel id="srcPanel" header="#{msg.sourceOfMerge}"
									headerClass="merge-header">
									<h:panelGrid columns="2">
										<h:outputText value="#{msg.patientFirstName}" />
										<h:inputText id="s_name" value="#{patientsList.firstName}" />
										<h:outputText value="#{msg.patientLastName}" />
										<h:inputText id="s_lastName" value="#{patientsList.lastName}" />
										<h:outputText value="#{msg.patientId}" />
										<h:inputText id="s_patientID"
											value="#{patientsList.patientId}" />
										<h:outputText value="#{msg.patientBirthDate}" />
										<rich:calendar id="s_studyDate" enableManualInput="true"
											value="#{patientsList.birthDate}" datePattern="dd/MM/yyyy" />
										<a4j:commandButton id="s_search"
											onclick="#{rich:component('waitPanel')}.show();"
											value="#{msg.search}" action="#{treeManager.removeSelection}">
											<a4j:support event="oncomplete"
												reRender="sourceResults,detailsSelection"
												oncomplete="#{rich:component('waitPanel')}.hide();"></a4j:support>
										</a4j:commandButton>
									</h:panelGrid>
									<br />
									<a4j:outputPanel id="sourceResults">
										<div id="treePanel" style="height: 200px; overflow: auto;">
											<rich:tree switchType="client"
												value="#{patientsList.patientsTree}" var="item"
												nodeFace="#{item.class}" id="srcTree">
												<rich:treeNode id="Paziente" data="#{item}"
													type="class it.units.htl.maps.Patients"
													nodeSelectListener="#{treeManager.patientNodeSelection}"
													reRender="detailsSelection" ajaxSubmitSelection="true">
													<h:outputText
														value="#{item.patientId} - #{item.lastName} #{item.firstName} - #{item.birthDateYYYYMMDD }" />
													<a4j:support event="oncomplete" reRender="detailsSelection"></a4j:support>
												</rich:treeNode>
												<rich:treeNode id="Studio" data="#{item}"
													type="class it.units.htl.maps.Studies"
													nodeSelectListener="#{treeManager.patientNodeSelection}"
													reRender="detailsSelection" ajaxSubmitSelection="true"
													dragType="study" dragValue="#{item}">
													<h:outputText
														value="#{item.studyDateYYYYMMDD} - #{item.studyTime} - #{item.studyDescription}"></h:outputText>
												</rich:treeNode>
												<rich:treeNode id="Serie" data="#{item}"
													type="class it.units.htl.maps.Series"
													nodeSelectListener="#{treeManager.patientNodeSelection}"
													reRender="detailsSelection" ajaxSubmitSelection="true"
													dragValue="#{item}" dragType="serie">
													<h:outputText
														value="#{item.seriesNumber} - #{item.modality} - #{item.bodyPartExamined}"></h:outputText>
												</rich:treeNode>
											</rich:tree>
										</div>
									</a4j:outputPanel>
								</rich:panel>
								<rich:panel id="dstPanel" header="#{msg.mergeDestination}"
									headerClass="merge-header">
									<h:panelGrid columns="2">
										<h:outputText value="#{msg.patientFirstName}" />
										<h:inputText id="d_name" value="#{findDestination.firstName}" />
										<h:outputText value="#{msg.patientLastName}" />
										<h:inputText id="d_lastName"
											value="#{findDestination.lastName}" />
										<h:outputText value="#{msg.patientId}" />
										<h:inputText id="d_patientID"
											value="#{findDestination.patientId}" />
										<h:outputText value="#{msg.patientBirthDate}" />
										<rich:calendar id="d_studyDate"
											value="#{findDestination.birthDate}" datePattern="dd/MM/yyyy"
											enableManualInput="true" />
										<a4j:commandButton id="d_search" value="#{msg.search}"
											action="#{dstTreeManager.removeSelection}"
											onclick="#{rich:component('waitPanel')}.show();">
											<a4j:support event="oncomplete" reRender="destinationResult"
												oncomplete="#{rich:component('waitPanel')}.hide();"></a4j:support>
										</a4j:commandButton>
									</h:panelGrid>
									<br />
									<a4j:outputPanel id="destinationResult">
										<div id="dstTreePanel" style="height: 200px; overflow: auto;">
											<rich:tree id="dstTree"
												value="#{findDestination.patientsTree}" var="dstItem"
												nodeFace="#{dstItem.class}" ajaxSingle="true">
												<rich:treeNode id="dstPatient"
													type="class it.units.htl.maps.Patients"
													nodeSelectListener="#{dstTreeManager.patientNodeSelection}"
													ajaxSubmitSelection="true">
													<rich:dropSupport id="supportStoP" acceptedTypes="study"
														dropValue="#{dstItem}"
														dropListener="#{dstTreeManager.processDrop}"
														oncomplete="#{rich:component('confirmPanel')}.show();"
														reRender="confirmPanel">
													</rich:dropSupport>
													<h:outputText
														value="#{dstItem.patientId} - #{dstItem.lastName} #{dstItem.firstName} - #{dstItem.birthDateYYYYMMDD }" />
												</rich:treeNode>
												<rich:treeNode id="dstStudy" data="#{dstItem}"
													type="class it.units.htl.maps.Studies"
													nodeSelectListener="#{dstTreeManager.patientNodeSelection}">
													<rich:dropSupport id="supportStoS" acceptedTypes="serie"
														dropValue="#{dstItem}"
														dropListener="#{dstTreeManager.processDrop}"
														oncomplete="#{rich:component('confirmPanel')}.show();"
														reRender="confirmPanel">
													</rich:dropSupport>
													<h:outputText
														value="#{dstItem.studyDateYYYYMMDD} - #{dstItem.studyTime} - #{dstItem.studyDescription}"></h:outputText>
												</rich:treeNode>
												<rich:treeNode id="dstSerie" data="#{dstItem}"
													type="class it.units.htl.maps.Series"
													nodeSelectListener="#{dstTreeManager.patientNodeSelection}"
													ajaxSingle="true">
													<h:outputText
														value="#{dstItem.seriesNumber} - #{dstItem.modality} - #{dstItem.bodyPartExamined}"></h:outputText>
												</rich:treeNode>
											</rich:tree>
										</div>
									</a4j:outputPanel>
								</rich:panel>
							</h:panelGrid>
						</a4j:form>

						<h:panelGrid columns="2"
							columnClasses="mergeToolColumn1 mergeToolColumn2">
							<a4j:outputPanel id="detailsSelection">
								<h:form>
									<rich:panel header="#{msg.editSelected}"
										headerClass="merge-header">
										<f:subview id="pnl">
											<a4j:include viewId="panel/patientPanel.jsp"
												rendered="#{treeManager.currentSelection.class == 'class it.units.htl.maps.Patients'}" />
											<a4j:include viewId="panel/panelStudy.jsp"
												rendered="#{treeManager.currentSelection.class == 'class it.units.htl.maps.Studies'}" />
										</f:subview>
									</rich:panel>
								</h:form>
							</a4j:outputPanel>

							<h:form>
								<rich:panel header="New Patient" headerClass="merge-header">
									<h:panelGrid columns="3">
										<h:outputText value="#{msg.patientId}*: " />
										<h:inputText id="patientId" required="true"
											binding="#{patientId}" value="#{patientCreator.patientId}"
											maxlength="64">
										</h:inputText>
										<h:message for="patientId" errorClass="error" />
										<h:outputText value="#{msg.idIssuer}*: " />
										<h:inputText id="idIssuer" value="#{patientCreator.idIssuer}"
											required="true" maxlength="64"
											validator="#{patientCreator.validatePatientId}">
											<f:attribute name="patientId" value="#{patientId}" />
										</h:inputText>
										<h:message for="idIssuer" />
										<h:outputText value="#{msg.firstName}: " />
										<h:inputText id="firstName"
											value="#{patientCreator.firstName}" maxlength="60" />
										<h:message for="firstName" />
										<h:outputText value="#{msg.middleName}: " />
										<h:inputText id="middleName"
											value="#{patientCreator.middleName}" maxlength="60" />
										<h:message for="middleName" />
										<h:outputText value="#{msg.lastName}: " />
										<h:inputText id="lastName" value="#{patientCreator.lastName}"
											maxlength="60" />
										<h:message for="lastName" />
										<h:outputText value="#{msg.prefix}: " />
										<h:inputText id="prefix" value="#{patientCreator.prefix}"
											maxlength="60" />
										<h:message for="prefix" />
										<h:outputText value="#{msg.suffix}: " />
										<h:inputText id="suffix" value="#{patientCreator.suffix}"
											maxlength="60" />
										<h:message for="suffix" />
										<h:outputText value="#{msg.patientBirthDate}: " />
										<rich:calendar id="birthDate" enableManualInput="true"
											datePattern="dd/MM/yyyy" value="#{patientCreator.birthDate}" />
										<h:message for="birthDate" />
										<h:outputText value="#{msg.gender}: " />
										<rich:inplaceSelect id="gender"
											value="#{patientCreator.gender}" required="true">
											<f:selectItem itemValue="M" itemLabel="#{msg.male}" />
											<f:selectItem itemValue="F" itemLabel="#{msg.female}" />
											<f:selectItem itemValue="O" itemLabel="#{msg.other}" />
										</rich:inplaceSelect>
										<h:message for="gender" />
									</h:panelGrid>

									<h:commandButton value="Create"
										action="#{dstTreeManager.removeSelection}"
										actionListener="#{patientCreator.createPatient}">
										<a4j:support event="oncomplete" reRender="dstPanel" />
									</h:commandButton>
								</rich:panel>
							</h:form>
						</h:panelGrid>


						<rich:modalPanel id="confirmPanel" resizeable="false"
							autosized="true" width="600">
							<f:facet name="header">
								<h:panelGroup>
									<h:outputText value="Viewer" />
								</h:panelGroup>
							</f:facet>
							<f:facet name="controls">
								<h:panelGroup>
									<a4j:form>
										<a4j:commandLink
											onclick="#{rich:component('confirmPanel')}.hide();"
											style="border: 0;">
											<h:graphicImage value="/img/close.png" style="cursor:pointer"
												id="hidelink">
											</h:graphicImage>
										</a4j:commandLink>
									</a4j:form>
								</h:panelGroup>
							</f:facet>
							<rich:panel id="mergeResults"
								rendered="#{dstTreeManager.mergeResult != ''}">
								<h:panelGrid columns="1"></h:panelGrid>
								<h:outputText value="#{dstTreeManager.mergeResult}" />
								<br />
								<a4j:form>
									<a4j:commandButton value="OK"
										oncomplete="#{rich:component('confirmPanel')}.hide(); "
										reRender="destinationResult, srcTree" />
								</a4j:form>
							</rich:panel>
							<rich:panel rendered="#{dstTreeManager.type == 'StP'}">
								<h:panelGrid columns="2" columnClasses="question, evidenced">
									<h:outputText value="Vuoi mettere lo studio:"></h:outputText>
									<h:outputText value="#{dstTreeManager.study.studyInstanceUid}"></h:outputText>
									<h:outputText value="del giorno:"></h:outputText>
									<h:outputText value="#{dstTreeManager.study.studyDate}"></h:outputText>
									<h:outputText value="del Paziente:"></h:outputText>
									<h:outputText
										value="#{dstTreeManager.study.patients.firstName} #{dstTreeManager.study.patients.lastName }"></h:outputText>
									<h:outputText value="nel Paziente:"></h:outputText>
									<h:outputText
										value="#{dstTreeManager.patient.patientId }: #{dstTreeManager.patient.firstName} #{dstTreeManager.patient.lastName}?"></h:outputText>
								</h:panelGrid>
								<a4j:form
									onsubmit="this.style.display='none'; document.getElementById('progressBar').style.display = 'inline';">
									<a4j:commandButton action="#{dstTreeManager.makeMerge}"
										value="#{msg.confirm}"
										oncomplete="#{rich:component('confirmPanel')}.show(); "
										reRender="confirmPanel" />
									<a4j:commandButton
										oncomplete="#{rich:component('confirmPanel')}.hide();"
										value="#{msg.cancel}">
									</a4j:commandButton>
								</a4j:form>
								<div id="progressBar" style="display: none;">
									<img src="../img/loading.gif" />
								</div>
							</rich:panel>
							<rich:panel rendered="#{dstTreeManager.type == 'StS'}">
								<h:panelGrid columns="2" columnClasses="question, evidenced">
									<h:outputText value="Vuoi inserire la serie: " />
									<h:outputText value="#{dstTreeManager.serie.seriesInstanceUid}" />
									<h:outputText value="dello Studio: " />
									<h:outputText
										value=" #{dstTreeManager.serie.studies.studyInstanceUid }" />
									<h:outputText value="nello Studio: " />
									<h:outputText value=" #{dstTreeManager.study.studyInstanceUid}" />
									<h:outputText value="del Paziente: " />
									<h:outputText
										value=" #{dstTreeManager.study.patients.firstName} #{dstTreeManager.study.patients.lastName}" />
								</h:panelGrid>
								<a4j:form
									onsubmit="this.style.display='none'; document.getElementById('progressBar').style.display = 'inline';">
									<a4j:commandButton action="#{dstTreeManager.makeMerge}"
										value="#{msg.confirm}"
										oncomplete="#{rich:component('confirmPanel')}.show(); "
										reRender="confirmPanel" />
									<a4j:commandButton
										oncomplete="#{rich:component('confirmPanel')}.hide();"
										value="#{msg.cancel}">
									</a4j:commandButton>
								</a4j:form>
								<div id="progressBar" style="display: none;">
									<img src="../img/loading.gif" />
								</div>
							</rich:panel>
						</rich:modalPanel>

					</div>
					<f:subview id="footer"><jsp:include
							page="/commons/footer.inc.html" /></f:subview>
				</div>
			</div>
		</center>
	</body>
</f:view>
</html>