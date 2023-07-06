<!-- Comment to prevent IE6 bug -->
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.ajax4jsf.org/rich" prefix="rich"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>

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
	<script type="text/javascript">
		var previousDivSelected=null;
		var previousClass = null;
		function selectIt(row){
			if (previousDivSelected){
				 previousDivSelected.parentNode.parentNode.parentNode.className = previousClass;
			}
			previousDivSelected = row;
			previousClass = row.parentNode.parentNode.parentNode.className;
			row.parentNode.parentNode.parentNode.className += " selectedRow";
		}
	</script>
	
	<script
			type="text/javascript" language="javascript">
	           function getWindowHeight(){
	    			if (document.all){
	        			return document.body.offsetHeight; 
	    			}else{ 
	        			return window.innerHeight; 
	    			}
				}

				function getWindowWidth(){ 
	    			if (document.all){ 
	        			return document.body.offsetWidth; 
	    			} else { 
	        			return window.innerWidth; 
	    			}
				}           
	</script>
	<style>
</style>
	<body>
	<center><rich:modalPanel id="preloadPanel" zindex="2000">
		<f:facet name="header">
			<h:outputText value="Loading images of the series...please wait..." />
		</f:facet>
		<f:facet name="controls">
			<h:panelGroup>
				<a4j:form>
					<a4j:commandLink
						oncomplete="#{rich:component('preloadPanel')}.hide()">
						<h:graphicImage value="/img/close.png" styleClass="hidelink"
							id="hidelink" />
					</a4j:commandLink>
				</a4j:form>
			</h:panelGroup>
		</f:facet>
		<center><img src="../img/loading.gif" /><br />
		Retrieving image of this series...please wait...</center>
	</rich:modalPanel>
	<div id="container"><f:subview id="header" rendered="#{empty sessionScope.isViewer}">
		<jsp:include page="/commons/header.inc.jsp" />
	</f:subview> 
	<f:subview id="navigation" rendered="#{empty sessionScope.isViewer}">
		<jsp:include page="/commons/navigation.inc.jsp" />
	</f:subview>

	<div id="wrapper">
	<div id="contentTitle"><h:outputText value="#{msg.serieResults}" /></div>
	<div id="content"><h:panelGrid columns="2"></h:panelGrid>

	<div class="backAction" style="text-align: left;"><h:form>
		<h:commandButton value="[.] #{msg.backToStudies}"
			action="#{seriesResultsBackBean.backToStudies}" rendered="#{empty sessionScope.isViewer}" accesskey='.'/>
	</h:form></div>
	<h:panelGrid columns="2" styleClass="top" rowClasses="top"
		columnClasses="none,tmp">
		<rich:dataTable value="#{serieList.seriesMatched}" var="item"
			id="tableStudies" headerClass="headerTable" styleClass="tableResults"
			rowClasses="oddRow, evenRow"
			columnClasses="column10,column10,none,none,none,none,none,none,none">
			<rich:toolTip>
		            <span style="white-space:nowrap">
						<h:outputText value="#{msg.description}: " />
		                <h:outputText value="#{item.seriesDescription}"></h:outputText>
		            </span>
        		</rich:toolTip>
			<rich:column >
			<a4j:form>
				<a4j:commandLink action="#{seriesResultsBackBean.objectView}"
				onclick="Richfaces.showModalPanel('preloadPanel'); selectIt(this);"
				oncomplete="Richfaces.hideModalPanel('preloadPanel')"  
				reRender="images" >
					<img src="../img/buttons/search.gif" width="16" height="16"
						border="0" alt="<h:outputText value="#{msg.view}" />" />
						<a4j:actionparam name="detail" value="detail"/>
				</a4j:commandLink>
				
				</a4j:form>
			</rich:column>

			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{msg.date}" />
				</f:facet>
				<rich:toolTip>
					<span style="white-space: nowrap"> <h:outputText
						value="#{item.serieTime}">
						<f:convertDateTime pattern="HH:mm:ss" />
					</h:outputText> </span>
				</rich:toolTip>
				<h:outputText value="#{item.serieDate}">
					<f:convertDateTime pattern="dd/MM/yyyy" />
				</h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{msg.shortmod}" />
				</f:facet>
				<h:outputText value="#{item.modality}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{msg.description}" />
				</f:facet>
				<h:outputText value="#{item.shortDescription}"></h:outputText>
				<rich:toolTip>
					<span style="white-space: nowrap"> <h:outputText
						value="#{item.seriesDescription}"></h:outputText> </span>
				</rich:toolTip>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{msg.number}" />
				</f:facet>
				<h:outputText value="#{item.seriesNumber}"></h:outputText>
			</rich:column>
			<rich:column>
				<f:facet name="header">
					<h:outputText value="#{msg.images}" />
				</f:facet>
				<h:outputText value="#{item.numberOfSeriesRelatedInstances}"></h:outputText>
			</rich:column>
		</rich:dataTable>
		<a4j:outputPanel id="images">

			<rich:dataGrid id="imagesGrid" value="#{objectList.objectsMatched}"
				styleClass="adaptiveCell" var="item" align="center"
				columns="#{seriesResultsBackBean.thumbsPerRow}"
				elements="#{seriesResultsBackBean.thumbsPerPage}"
				rendered="#{objectList.objectsMatched != null}">
				<f:facet name="header">
					<h:form>
						<rich:datascroller rendered="#{objectList.objectsMatched != null}"
							for="imagesGrid" fastControls="hide" renderIfSinglePage="false"
							id="topScroll" page="#{SeriesPageCounter.pageCounter}"
							onclick="#{rich:component('preloadPanel')}.show();"
							oncomplete="#{rich:component('preloadPanel')}.hide();" />
					</h:form>

				</f:facet>
				<h:outputLink value="#{item.wadoUrlForPDF}"
					rendered="#{item.isStructRep}" target="_blank">
					<h:graphicImage value="../img/lis_pdf.gif" width="80" height="83"  style="border-width:0px;"/>
					<br> <h:outputText value="#{msg.openPDF}" />
				</h:outputLink>
				<a4j:form>
					<a4j:outputPanel id="thumbContainer"
						rendered="#{!item.isStructRep && item.viewer != 'mpegVideoPanel' }">
						<a4j:commandLink action="#{imagesResultsBackBean.set_selected}"
							reRender="immagine"
							oncomplete="Richfaces.showModalPanel('BigImagePopup',{width:getWindowWidth()-20, height:getWindowHeight()-20})">
							<img src="<h:outputText value="#{item.wadoUrlForImage}"/>" style="border-width:0px;"/>
						</a4j:commandLink>
					</a4j:outputPanel>
					<a4j:outputPanel id="thumContainerIfMpeg"
						rendered="#{!item.isStructRep && item.viewer == 'mpegVideoPanel'}">
						<a4j:commandLink action="#{imagesResultsBackBean.set_selected}" rendered="#{sessionScope.isHttps != 'https'}"
							reRender="immagine"
							oncomplete='Richfaces.showModalPanel("BigImagePopup",{width:800,height:600})'>
							<img src="../img/mpeg.jpg"  style="border-width: 0px;"/>
						</a4j:commandLink>
						<h:outputLink value="#{item.wadoUrlForMpeg}" rendered="#{sessionScope.isHttps == 'https'}" >
							<img src="../img/mpeg.jpg" title="<h:outputText value="#{msg.download}"/>" style="border-width: 0px;"/>
						</h:outputLink>
					</a4j:outputPanel>
				</a4j:form>
				<f:facet name="footer">
					<h:form>
						<rich:datascroller rendered="#{objectList.objectsMatched != null}"
							for="imagesGrid" fastControls="hide" renderIfSinglePage="false"
							id="bottomScroll" page="#{SeriesPageCounter.pageCounter}"
							onclick="#{rich:component('preloadPanel')}.show();"
							oncomplete="#{rich:component('preloadPanel')}.hide();" />
					</h:form>
				</f:facet>
			</rich:dataGrid>

		</a4j:outputPanel>
	</h:panelGrid>
	
	 <a4j:outputPanel id="immagine">
		<rich:modalPanel id="BigImagePopup"
			rendered="#{selectedObjectBackBean._selected != null}" moveable="false" >
			<f:facet name="header">
				<h:panelGroup>
					<h:outputText value="Viewer"></h:outputText>
				</h:panelGroup>
			</f:facet>
			<f:facet name="controls">
				<h:panelGroup>
					<a4j:form>
						<a4j:commandLink
							oncomplete="#{rich:component('BigImagePopup')}.hide();"
							actionListener="#{imagesResultsBackBean.removeSelected}"
							style="border: 0;">
							<h:graphicImage value="/img/close.png" style="cursor:pointer"
								id="hidelink">
							</h:graphicImage>
						</a4j:commandLink>
					</a4j:form>
				</h:panelGroup>
			</f:facet>
			<a4j:outputPanel id="viewer">
				<a4j:include id="imagePanel" viewId="panel/imagePanel.jsp"
					rendered="#{selectedObjectBackBean._selected.viewer == 'imagePanel'}" />
				<a4j:include id="videoPanel" viewId="panel/videoPanel.jsp"
					rendered="#{selectedObjectBackBean._selected.viewer == 'videoPanel'}" />
				<a4j:include id="noPanel" viewId="panel/noplayerPanel.jsp"
					rendered="#{selectedObjectBackBean._selected.viewer == 'noPanel'}" />
				<a4j:include id="mpegVideoPanel" viewId="panel/mpegVideoPanel.jsp" 
					rendered="#{selectedObjectBackBean._selected.viewer == 'mpegVideoPanel'}"  />
			</a4j:outputPanel>
		</rich:modalPanel>
	</a4j:outputPanel>
	</div>

	<!-- footer content --> <f:subview id="footer" rendered="#{empty sessionScope.isViewer}">
		<jsp:include page="/commons/footer.inc.html" />
	</f:subview></div>
	</div>
	</center>
	</body>
</f:view>
</html>

