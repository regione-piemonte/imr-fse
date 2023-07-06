<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>

<f:subview id="btn">
	<a4j:include viewId="button.jsp"></a4j:include>
</f:subview>
<h:panelGrid columns="2">
	<h:outputText value="#{msg.pk} :" />
	<h:inputText id="pk" disabled="true"
		value="#{treeManager.currentSelection.pk}" />
	<h:outputText value="#{msg.firstName} :" />
	<h:inputText id="firstName" disabled="#{treeManager.disabled}"
		value="#{treeManager.currentSelection.firstName}" />
	<h:outputText value="#{msg.middleName} :" />
	<h:inputText id="middleName" disabled="#{treeManager.disabled}"
		value="#{treeManager.currentSelection.middleName}" />
	<h:outputText value="#{msg.lastName} :" />
	<h:inputText id="lastName" disabled="#{treeManager.disabled}"
		value="#{treeManager.currentSelection.lastName}" />
	<h:outputText value="#{msg.prefix} :" />
	<h:inputText id="prefix" disabled="#{treeManager.disabled}"
		value="#{treeManager.currentSelection.prefix}" />
	<h:outputText value="#{msg.suffix} :" />
	<h:inputText id="suffix" disabled="#{treeManager.disabled}"
		value="#{treeManager.currentSelection.suffix}" />
	<h:outputText value="#{msg.patientBirthDate} :" />
	<rich:calendar id="birthDate" disabled="#{treeManager.disabled}"
		value="#{treeManager.currentSelection.birthDate}"
		enableManualInput="true" datePattern="dd/MM/yyyy">
	</rich:calendar>
	<h:outputText value="#{msg.patientBirthTime} :" />
	<rich:calendar id="birthTime" disabled="#{treeManager.disabled}"
		datePattern="HH.mm.ss"
		value="#{treeManager.currentSelection.birthTime}" />
	<h:outputText value="#{msg.gender} :" />
	<rich:inplaceSelect defaultLabel="#{treeManager.currentSelection.sex}"
		value="#{treeManager.currentSelection.sex}">
		<f:selectItem itemValue="M" itemLabel="#{msg.male}" />
		<f:selectItem itemValue="F" itemLabel="#{msg.female}" />
		<f:selectItem itemValue="O" itemLabel="#{msg.other}" />
	</rich:inplaceSelect>
	<h:outputText value="#{msg.patientId} :" />
	<h:inputText id="patientId" disabled="#{treeManager.disabled}"
		value="#{treeManager.currentSelection.patientId}" />
</h:panelGrid>

