<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<h:panelGrid columns="1"> 
<h:outputText style="font-weight:bold;" value="#{msg.modifiesOnDb}" />
<h:panelGroup>
<a4j:commandButton action="#{treeManager.setDisabled}"
	rendered="#{treeManager.disabled}" image="/img/buttons/modify.gif"
	reRender="detailsSelection">
	<rich:toolTip followMouse="true" direction="top-right" showDelay="500">
		<span style="white-space: nowrap"> Enable modify. </span>
	</rich:toolTip>
</a4j:commandButton>
<a4j:commandButton action="#{treeManager.setDisabled}"
	rendered="#{!treeManager.disabled}" image="/img/buttons/modify_en.gif"
	reRender="detailsSelection">
	<rich:toolTip followMouse="true" direction="top-right" showDelay="500">
		<span style="white-space: nowrap"> Disable modify. </span>
	</rich:toolTip>
</a4j:commandButton>
<a4j:commandButton action="#{treeManager.saveModify}" disabled="#{treeManager.disabled}"
	image="/img/buttons/save.gif"
	reRender="detailsSelection, sourceResults, destinationResult">
	<rich:toolTip followMouse="true" direction="top-right" showDelay="500">
		<span style="white-space: nowrap"> Save modifies. </span>
	</rich:toolTip>
</a4j:commandButton>
</h:panelGroup>
</h:panelGrid>