<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>


<f:subview id="btn">
	<a4j:include viewId="button.jsp" ></a4j:include>
	</f:subview>
	<h:panelGrid columns="2">	
		<h:outputText value="#{msg.studyInstanceUID} :" />
		<h:inputText id="pk" disabled="true"
			value="#{treeManager.currentSelection.studyInstanceUid}" />
		<h:outputText value="#{msg.accessionNumber} :" />
		<h:inputText id="acc_id" 
		disabled="#{treeManager.disabled}"
			value="#{treeManager.currentSelection.accessionNumber}" />
		<h:outputText value="#{msg.studyDescription} :" />
		<h:inputText id="studyDescription"
			disabled="#{treeManager.disabled}"
			value="#{treeManager.currentSelection.studyDescription}" />
		<h:outputText value="#{msg.refPhysician} :" />
		<h:inputText id="referringPhysiciansName"
			disabled="#{treeManager.disabled}"
			value="#{treeManager.currentSelection.referringPhysiciansName}" />
		<h:outputText value="#{msg.completionDate} :" />
		<rich:calendar id="studyCompletionDate"
			disabled="#{treeManager.disabled}"
			value="#{treeManager.currentSelection.studyCompletionDate}" />
		<h:outputText value="#{msg.studyDate} :" />
		<rich:calendar id="studyDate" disabled="#{treeManager.disabled}"
			value="#{treeManager.currentSelection.studyDate}" enableManualInput="true" 
		datePattern="dd/MM/yyyy"/>
		
	</h:panelGrid>
	