
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>

	
<rich:modalPanel id="messagePanel" showWhenRendered="#{not empty messageManager.message}" width="400" autosized="true"> 
    	<f:facet name="controls">
            <h:panelGroup>
                <h:graphicImage value="/img/close.png" style="cursor:pointer" id="hidelink"/>
                <rich:componentControl for="messagePanel" attachTo="hidelink" operation="hide" event="onclick"/>
            </h:panelGroup>
        </f:facet>
        <f:facet name="header">
        	<h:outputText value="o3-dpacs-web says..." />
        </f:facet>
        <center>
        <h:panelGrid columns="1"  style="text-align:center">
        	<div class="messagePanelText">
        		<h:outputText value="#{messageManager.message}" styleClass="messagePanelText" escape="false"></h:outputText>
			</div>
			<h:commandButton value="#{msg.close}"  onclick="#{rich:component('messagePanel')}.hide()" />
		</h:panelGrid>
		</center>
		
    </rich:modalPanel>