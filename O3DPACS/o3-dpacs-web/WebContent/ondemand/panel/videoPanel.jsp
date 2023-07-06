<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>


<rich:panel id="movie" >
<center>
<OBJECT classid='clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B'
	codebase='http://www.apple.com/qtactivex/qtplugin.cab'
	width="<h:outputText value="#{selectedObjectBackBean._selected.columns+50}" />px" 
	height="<h:outputText value="#{selectedObjectBackBean._selected.rows+50}" />px"	>
	<param name='src' value="<h:outputText value="#{selectedObjectBackBean._selected.wadoUrlForMov}" />" />
	<param name='autoplay' value="true">
	<param name='controller' value="true">
	<param name='loop' value="true">
	<EMBED src="<h:outputText value="#{selectedObjectBackBean._selected.wadoUrlForMov}" />"
		width="<h:outputText value="#{selectedObjectBackBean._selected.columns+50}" />px" 
		height="<h:outputText value="#{selectedObjectBackBean._selected.rows+50}" />px"
		autoplay="true" controller="true" 
		loop="true">
	</EMBED> </OBJECT>
	</center>
	<h:outputText>To view multiframe image, you need QuickTime player, downlodable <a href="http://www.apple.com/it/quicktime/download/">here</a></h:outputText>
</rich:panel>