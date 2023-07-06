<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>

<rich:panel id="movie" >
<center>
	<div id="forIE" >
			<object classid="clsid:E23FE9C6-778E-49D4-B537-38FCDE4887D8"
				codebase="http://downloads.videolan.org/pub/videolan/vlc/latest/win32/axvlc.cab" id="vlcIE" style="display: block; margin-bottom: 20px;">
				<param name="Src" value="<h:outputText value="#{selectedObjectBackBean._selected.wadoUrlForMpeg}" />" />
				<param name="ShowDisplay" value="false" />
				<param name="Loop" value="true" />
				<param name="AutoPlay" value="true" />
			</object>
			<a href="javascript:;" onclick='document.getElementById("vlcIE").play();'><img title="<h:outputText value="#{msg.play}" />" src="../img/movie/play.png" style="border-width: 0px;" /></a> 
			<a href="javascript:;" onclick='document.getElementById("vlcIE").pause();'><img title="<h:outputText value="#{msg.pause}" />" src="../img/movie/pause.png" style="border-width: 0px;"/></a> 
			<a href="javascript:;" onclick='document.getElementById("vlcIE").stop();'><img title="<h:outputText value="#{msg.stop}" />" src="../img/movie/stop.png" style="border-width: 0px;"/></a>			
			<a href="javascript:;" onclick='document.getElementById("vlcIE").fullscreen();'><img title="<h:outputText value="#{msg.fullscreen}" />"  src="../img/movie/fullscreen.png" style="border-width: 0px;"/></a>
			<a href="<h:outputText value="#{selectedObjectBackBean._selected.wadoUrlForMpeg}" />"><img title="<h:outputText value="#{msg.download}" />" src="../img/movie/download.png" style="border-width: 0px;"/></a>
		</div>
	
	
	
	
	<div id="forFF" style="margin 0px auto auto;">
	<embed id="vlc" type="application/x-vlc-plugin"
		version="VideoLAN.VLCPlugin.2" src="<h:outputText value="#{selectedObjectBackBean._selected.wadoUrlForMpeg}" />"
		width="540" height="432" style="display: block; margin-bottom: 20px;" />
		<a id="1" href="javascript:;" onclick='vlc.playlist.playItem(0);'><img title="<h:outputText value="#{msg.play}" />" src="../img/movie/play.png" style="border-width: 0px;" style="border: none;" /></a>
		<a href="javascript:;" onclick='vlc.playlist.togglePause();'><img title="<h:outputText value="#{msg.pause}" />" src="../img/movie/pause.png" style="border-width: 0px;" /></a> 
		<a href="javascript:;" onclick='vlc.playlist.stop();'><img title="<h:outputText value="#{msg.stop}" />" src="../img/movie/stop.png" style="border-width: 0px;" /></a>		
		<a href="javascript:;" onclick='vlc.video.toggleFullscreen();'><img title="<h:outputText value="#{msg.fullscreen}" />"  src="../img/movie/fullscreen.png" style="border-width: 0px;" /></a> 
		<a href="<h:outputText value="#{selectedObjectBackBean._selected.wadoUrlForMpeg}" />"><img title="<h:outputText value="#{msg.download}" />" src="../img/movie/download.png" style="border-width: 0px;" /></a> 
	</div>
</center>
	<script type="text/javascript">
		function resize(){
			document.getElementById("vlcIE").style.width="540px";
			document.getElementById("vlcIE").style.height="432px";
		}
		var IE = /*@cc_on!@*/false;
		if(IE){
			document.getElementById("forIE").style.display = "block";
			document.getElementById("forFF").style.display = "none";
			window.setTimeout('resize()',500);
		}else{
			document.getElementById("forIE").style.display = "none";
			document.getElementById("forFF").style.display = "block";
		}
</script>
</rich:panel>