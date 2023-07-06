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
	<title><h:outputText value="#{msg.serieResults}" /></title>
	<script src="swf/js/swfobject.js" type="text/javascript">
		//swfobject.embedSWF("swf/o3dpacswebflex.swf", "flex", "1300", "500", "9.0.0");
	</script>
	<script type="text/javascript">
		function getLocationHref(){
			return window.location.href;
		}
	</script>
	</head>
	<style>
</style>
	<body style="overflow: hidden" onResize="doResize()" >
	<div id="container"><f:subview id="header">
		<jsp:include page="/commons/header.inc.jsp" />
	</f:subview> <f:subview id="navigation" rendered="#{empty sessionScope.isViewer}">
		<jsp:include page="/commons/navigation.inc.jsp" />
	</f:subview>
	<div id="wrapper" style="horizontal-align: center;">
	<div class="backAction" style="text-align: left;"><h:form>
  		<h:commandButton value="[.] #{msg.backToStudies}"
  		 action="#{seriesResultsBackBean.backToStudies}" rendered="#{empty sessionScope.isViewer}" accesskey='.'/>
		 </h:form></div>
	  <a4j:form>
		<div align="center">
		  <div id="flex" align="center">
		     <a4j:outputPanel id="flexPanel" > 
		       
			   <f:verbatim>
			   
			    <span display="block" style="height: 100%; margin: 0; padding: 0;">	
			    
			    <!--		    
			      <object id="flexMovie" type="application/x-shockwave-flash" data="swf/o3dpacswebflex.swf" align="top">
				    <param value="swf/o3dpacswebflex.swf" name="movie" />
				      <param name="allowScriptAccess" value="sameDomain" />
                		<param name="allowFullScreen" value="true" />
                		<param name='flashVars' value='localeChain=<h:outputText value="#{sessionScope.locale}" />'/>
			      </object>
			    -->
			     
			      <iframe
			      	id="htmlViewer"
			      	src="html/index.html"
			      	align="top"
			      	allowfullscreen="true"
			      	style="height: 576px; width: 768px; border: none"
			      	credentials='<h:outputText value="#{flexSessionHandlerBackBean.authorization}" />'
			      	studyUID='<h:outputText value="#{flexSessionHandlerBackBean.studyUID}" />' 
			      	serviceURL='<h:outputText value="#{flexSessionHandlerBackBean.wadoUrl}" />'
			      	keyImagesOnly='<h:outputText value="#{flexSessionHandlerBackBean.keyImagesOnly}" />'
			      >
			      </iframe>
   
			      <script	type="text/javascript" language="javascript">
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

					//document.getElementById("flexMovie").style.height=(getWindowHeight()-160)+"px";
					//document.getElementById("flexMovie").style.width=(getWindowWidth()-60)+"px";	
						
					document.getElementById("htmlViewer").style.height=(getWindowHeight()-160)+"px";
					document.getElementById("htmlViewer").style.width=(getWindowWidth()-60)+"px";
					
			</script>
			    </span>
			  </f:verbatim>
		    </a4j:outputPanel>
		  </div>
		</div>
    </a4j:form> 
    <!-- footer content --> 
    </div>
	</div>
	</body>
</f:view> 
</html>