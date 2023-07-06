<!-- Comment to prevent IE6 bug -->
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.ajax4jsf.org/rich" prefix="rich"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" >
<f:view locale="#{sessionScope.locale}"
	afterPhase="#{messageManager.resetMessage}">
	<head>
		<link rel="stylesheet" href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
		<f:loadBundle var="msg" basename="it.units.htl.web.messages" />
		<title><h:outputText value="#{msg.reconciliation}" /></title>
		<style type="text/css" media="screen"> 
            html, body  { height:100%; }
            body { margin:0; padding:0; overflow:auto; text-align:center; 
                   background-color: #ffffff; }   
            object:focus { outline:none; }
            #flashContent { display:none; }
        </style>
        <link rel="stylesheet" type="text/css" href="history/history.css" />
        <script type="text/javascript" src="history/history.js"></script>
        <script type="text/javascript" src="swfobject.js"></script>
        <script type="text/javascript">
            // For version detection, set to min. required Flash Player version, or 0 (or 0.0.0), for no version detection. 
            var swfVersionStr = "11.1.0";
            // To use express install, set to playerProductInstall.swf, otherwise the empty string. 
            var xiSwfUrlStr = "playerProductInstall.swf";
            var flashvars = {};
    	    flashvars.localeChain="<h:outputText value='#{sessionScope.locale}' />";
            var params = {};
            params.quality = "high";
            params.bgcolor = "#ffffff";
            params.allowscriptaccess = "sameDomain";
            params.allowfullscreen = "true";
            params.allowfullscreeninteractive = "true";
			var attributes = {};
            attributes.id = "Reconciliation";
            attributes.name = "Reconciliation";
            attributes.align = "middle";
            swfobject.embedSWF(
                "Reconciliation.swf", "flashContent", 
                "100%", "100%", 
                swfVersionStr, xiSwfUrlStr, 
                flashvars, params, attributes);
            // JavaScript enabled so display the flashContent div in case it is not replaced with a swf object.
            swfobject.createCSS("#flashContent", "display:block;text-align:left;");
        </script>
        <script type="text/javascript">
		function getLocationHref(){
			return window.location.href;
		}
	</script>
	</head>
	<style>
</style>
	<body onload="afterAll()" >
		<div id="container">
			<f:subview id="header">
				<jsp:include page="/commons/header.inc.jsp" />
			</f:subview>
			<f:subview id="navigation" rendered="#{empty sessionScope.isViewer}">
				<jsp:include page="/commons/navigation.inc.jsp" />
			</f:subview>
		
		<div id="wrapper" style="horizontal-align: center;">
		<div id="flex" align="center">
		<f:verbatim>
		        <div id="flashContent">
            <p>
                To view this page ensure that Adobe Flash Player version 
                11.1.0 or greater is installed. 
            </p>
            <script type="text/javascript"> 
                var pageHost = ((document.location.protocol == "https:") ? "https://" : "http://"); 
                document.write("<a href='http://www.adobe.com/go/getflashplayer'><img src='" 
                                + pageHost + "www.adobe.com/images/shared/download_buttons/get_flash_player.gif' alt='Get Adobe Flash player' /></a>" ); 
            </script> 
        </div>
        
        <noscript>
            <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="100%" height="100%" id="Reconciliation">
                <param name="movie" value="Reconciliation.swf" />
                <param name="quality" value="high" />
                <param name="bgcolor" value="#ffffff" />
                <param name="allowScriptAccess" value="sameDomain" />
                <param name="allowFullScreen" value="true" />
                <param name="allowFullScreenInteractive" value="true" />
                <param name='flashVars' value='localeChain=<h:outputText value="#{sessionScope.locale}" />'/>
                <!--[if !IE]>-->
                <object type="application/x-shockwave-flash" data="Reconciliation.swf" width="100%" height="100%">
                    <param name="quality" value="high" />
                    <param name="bgcolor" value="#ffffff" />
                    <param name="allowScriptAccess" value="sameDomain" />
                    <param name="allowFullScreenInteractive" value="true" />
                    <param name="allowFullScreen" value="true" />
                    <param name='flashVars' value='localeChain=<h:outputText value="#{sessionScope.locale}" />'/>
                <!--<![endif]-->
                <!--[if gte IE 6]>-->
                    <p> 
                        Either scripts and active content are not permitted to run or Adobe Flash Player version
                        11.1.0 or greater is not installed.
                    </p>
                <!--<![endif]-->
                    <a href="http://www.adobe.com/go/getflashplayer">
                        <img src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Get Adobe Flash Player" />
                    </a>
                <!--[if !IE]>-->
                </object>
                <!--<![endif]-->
            </object>
        </noscript>
        
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
					document.getElementById("Reconciliation").style.height=(getWindowHeight()-160)+"px";
					document.getElementById("Reconciliation").style.width=(getWindowWidth()-100)+"px";
					function afterAll(){
						document.getElementById("Reconciliation").style.height=(getWindowHeight()-160)+"px";
						document.getElementById("Reconciliation").style.width=(getWindowWidth()-100)+"px";
					}
			</script>
			</f:verbatim>
			</div>
			</div>
	</body>
</f:view>
</html>