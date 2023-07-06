<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>


<script type="text/javascript">
var first = true;
	var _chi;
	var _dove;
	var _first = true;

	var Drag = {

		obj :null,

		init : function(o, oRoot, minX, maxX, minY, maxY, bSwapHorzRef,
				bSwapVertRef, fXMapper, fYMapper) {
			o.onmousedown = Drag.start;
			document.body.style.cursor = 'pointer';
			o.hmode = bSwapHorzRef ? false : true;
			o.vmode = bSwapVertRef ? false : true;

			o.root = oRoot && oRoot != null ? oRoot : o;

			if (o.hmode && isNaN(parseInt(o.root.style.left)))
				o.root.style.left = "0px";
			if (o.vmode && isNaN(parseInt(o.root.style.top)))
				o.root.style.top = "0px";
			if (!o.hmode && isNaN(parseInt(o.root.style.right)))
				o.root.style.right = "0px";
			if (!o.vmode && isNaN(parseInt(o.root.style.bottom)))
				o.root.style.bottom = "0px";

			o.minX = typeof minX != 'undefined' ? minX : null;
			o.minY = typeof minY != 'undefined' ? minY : null;
			o.maxX = typeof maxX != 'undefined' ? maxX : null;
			o.maxY = typeof maxY != 'undefined' ? maxY : null;

			o.xMapper = fXMapper ? fXMapper : null;
			o.yMapper = fYMapper ? fYMapper : null;

			o.root.onDragStart = new Function();
			o.root.onDragEnd = new Function();
			o.root.onDrag = new Function();
		},

		start : function(e) {
			var o = Drag.obj = this;
			e = Drag.fixE(e);
			var y = parseInt(o.vmode ? o.root.style.top : o.root.style.bottom);
			var x = parseInt(o.hmode ? o.root.style.left : o.root.style.right);
			o.root.onDragStart(x, y);

			o.lastMouseX = e.clientX;
			o.lastMouseY = e.clientY;

			if (o.hmode) {
				if (o.minX != null)
					o.minMouseX = e.clientX - x + o.minX;
				if (o.maxX != null)
					o.maxMouseX = o.minMouseX + o.maxX - o.minX;
			} else {
				if (o.minX != null)
					o.maxMouseX = -o.minX + e.clientX + x;
				if (o.maxX != null)
					o.minMouseX = -o.maxX + e.clientX + x;
			}

			if (o.vmode) {
				if (o.minY != null)
					o.minMouseY = e.clientY - y + o.minY;
				if (o.maxY != null)
					o.maxMouseY = o.minMouseY + o.maxY - o.minY;
			} else {
				if (o.minY != null)
					o.maxMouseY = -o.minY + e.clientY + y;
				if (o.maxY != null)
					o.minMouseY = -o.maxY + e.clientY + y;
			}

			document.onmousemove = Drag.drag;
			document.onmouseup = Drag.end;

			return false;
		},

		drag : function(e) {
			e = Drag.fixE(e);
			var o = Drag.obj;

			var ey = e.clientY;
			var ex = e.clientX;
			var y = parseInt(o.vmode ? o.root.style.top : o.root.style.bottom);
			var x = parseInt(o.hmode ? o.root.style.left : o.root.style.right);
			var nx, ny;

			if (o.minX != null)
				ex = o.hmode ? Math.max(ex, o.minMouseX) : Math.min(ex,
						o.maxMouseX);
			if (o.maxX != null)
				ex = o.hmode ? Math.min(ex, o.maxMouseX) : Math.max(ex,
						o.minMouseX);
			if (o.minY != null)
				ey = o.vmode ? Math.max(ey, o.minMouseY) : Math.min(ey,
						o.maxMouseY);
			if (o.maxY != null)
				ey = o.vmode ? Math.min(ey, o.maxMouseY) : Math.max(ey,
						o.minMouseY);

			nx = x + ((ex - o.lastMouseX) * (o.hmode ? 1 : -1));
			ny = y + ((ey - o.lastMouseY) * (o.vmode ? 1 : -1));

			if (o.xMapper)
				nx = o.xMapper(y);
			else if (o.yMapper)
				ny = o.yMapper(x);

			Drag.obj.root.style[o.hmode ? "left" : "right"] = nx + "px";
			Drag.obj.root.style[o.vmode ? "top" : "bottom"] = ny + "px";
			Drag.obj.lastMouseX = ex;
			Drag.obj.lastMouseY = ey;

			Drag.obj.root.onDrag(nx, ny);
			return false;
		},

		end : function() {
			document.onmousemove = null;
			document.onmouseup = null;
			Drag.obj.root.onDragEnd(
					parseInt(Drag.obj.root.style[Drag.obj.hmode ? "left"
							: "right"]),
					parseInt(Drag.obj.root.style[Drag.obj.vmode ? "top"
							: "bottom"]));
			Drag.obj = null;
		},

		fixE : function(e) {
			if (typeof e == 'undefined')
				e = window.event;
			if (typeof e.layerX == 'undefined')
				e.layerX = e.offsetX;
			if (typeof e.layerY == 'undefined')
				e.layerY = e.offsetY;
			return e;
		}
	};

	function initPan(img, cont) {
		var Img = document.getElementById(img);
		var Box = document.getElementById(cont);

		var newImg = new Image();
		newImg.src = Img.src;

		var imgWidth = newImg.width;
		var imgHeight = newImg.height;

		var boxWidth = document.body.offsetWidth - 2; // min is voor de borders van de DIV
		var boxHeight = document.body.offsetHeight - 2; // min is voor de borders van de DIV
		var panX = boxWidth - imgWidth;
		var panY = boxHeight - imgHeight;

		Drag.init(Img, null, panX, 0, panY, 0);

	}

	function resize(id, coeff) {
		var pic = document.getElementById(id);
		var w = Math.round(pic.width * coeff);
		var h = Math.round(pic.height * coeff);
		if (w > 1 && h > 1) {
			pic.width = w;
			pic.heigth = h;
			var Box = document.getElementById(_dove);

			var imgWidth = pic.width;
			var imgHeight = pic.height;

			var boxWidth = Box.offsetWidth - 2;
			var boxHeight = Box.offsetHeight - 2;
			var panX = boxWidth - imgWidth;
			var panY = boxHeight - imgHeight;
			Drag.init(pic, null, panX, 0, panY, 0);
		}
	}

	function enlarge(id) {
		resize(id, 1.1);
	}

	function decrease(id) {
		resize(id, 0.9);
	}

	function handle(delta) {
		if (delta < 0) {
			enlarge(_chi);
		} else
			decrease(_chi);
	}
	function wheel(event) {
		var delta = 0;
		if (!event)
			event = window.event;
		if (event.wheelDelta) {
			delta = event.wheelDelta / 120;
			if (window.opera)
				delta = -delta;
		} else if (event.detail) {
			delta = -event.detail / 3;
		}
		if (delta)
			handle(delta);
		if (event.preventDefault)
			event.preventDefault();
		event.returnValue = false;
	}

	var _firstFit = true;
	var scaleOf = 1;
	function fit(immagine, contenitore) {
		
		var pic = document.getElementById(immagine);
		var box = document.getElementById(contenitore);
		if (_firstFit) {
			var scaleOfWidth = box.offsetWidth / pic.width;
			var scaleOfHeight = box.offsetHeight / pic.height;
			//alert(scaleOfWidth+'='+box.offsetWidth+'/'+pic.width+' '+scaleOfHeight+'='+box.offsetHeight+'/'+pic.height);
			if (scaleOfWidth < scaleOfHeight) {
				resize(immagine, scaleOfWidth);
				scaleOf = scaleOfWidth;
			} else {
				resize(immagine, scaleOfHeight);
				scaleOf = scaleOfHeight;
			}
			_firstFit = false;
		} else {
			resize(immagine, scaleOf);
		}
		
	}

	function initZoom(immagine, contenitore) {
		_chi = immagine;
		_dove = contenitore;
		if (_first) {
			initPan(immagine, contenitore);
			_first = false;
		}
		if (window.addEventListener)
			window.addEventListener('DOMMouseScroll', wheel, false);
		window.onmousewheel = document.onmousewheel = wheel;
	}
	function removeZoom() {
		if (window.removeEventListener)
			window.removeEventListener('DOMMouseScroll', wheel, false);
		window.onmousewheel = document.onmousewheel = null;
	}

</script>
<a4j:outputPanel id="imageContainer" style="text-align: center;"
	ajaxRendered="true">
	<rich:panel id="pannelloTest" style="text-align: center; "
		onmouseover="initZoom('wadoBigImage', 'imgContainer');"
		onmouseout="removeZoom();">
		<div id="imgContainer" style="overflow: hidden; text-align: center;">
		<img id="wadoBigImage"
			src="<h:outputText value='#{selectedObjectBackBean._selected.wadoUrlForImage}'/>"
			alt="wait while loading the image..."
			onload="fit('wadoBigImage', 'imgContainer');"
			style="position: relative; left: 0; top: 0; cursor: pointer;" /> <script
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
				document.getElementById("imgContainer").style.height=(getWindowHeight()-200)+"px";
				document.getElementById("imgContainer").style.width=(getWindowWidth()-100)+"px";
			</script></div>
	</rich:panel>
	<rich:hotKey id="zoomIn" disableInInputTypes="true"
		disableInInput="true" key="ctrl+up"
		handler="enlarge('wadoBigImage');return false;" />
	<rich:hotKey id="zoomOut" disableInInputTypes="true"
		disableInInput="true" key="ctrl+down"
		handler="decrease('wadoBigImage');return false;" />
</a4j:outputPanel>

<rich:hotKey id="nextImageHK" disableInInputTypes="true"
	disableInInput="true" key="shift+right"
	handler="nextImage(); return false;" />
<rich:hotKey id="prevImageHK" disableInInputTypes="true"
	disableInInput="true" key="shift+left"
	handler="prevImage(); return false;" />
<a4j:form>
	<a4j:jsFunction name="nextImage"
		action="#{imagesResultsBackBean.get_next}" reRender="imageContainer" />
	<a4j:jsFunction name="prevImage"
		action="#{imagesResultsBackBean.get_prev}" reRender="imageContainer" />
</a4j:form>
<h:panelGrid columns="1" styleClass="scrollerGroup">
	 <a4j:form>
		<rich:inputNumberSlider id="imageChanger"
			value="#{imagesResultsBackBean._currentImage}"
			styleClass="innerScroller" 
			maxValue="#{imagesResultsBackBean._totalImages}" step="1"
			minValue="1" showToolTip="true" enableManualInput="false"
			showInput="true" rendered="#{imagesResultsBackBean._totalImages > 1}">
			<a4j:support event="onchange" reRender="imageContainer" />
		</rich:inputNumberSlider>
	</a4j:form> 
	<h:panelGrid id="grigliaSlider" columns="2" columnClasses="ex,none"
		styleClass="innerScroller">
		<h:outputText value="#{msg.wwidth}"
			rendered="#{not empty selectedObjectBackBean._selected.wwidth}" />
		<a4j:form id="windowWidthForm">
			<rich:inputNumberSlider id="windowWidth"
				value="#{selectedObjectBackBean._selected.wwidth}" width="600px"
				maxValue="#{selectedObjectBackBean._selected._maxWindowSize}"
				step="1" showToolTip="true" enableManualInput="false"
				showInput="true"
				rendered="#{not empty selectedObjectBackBean._selected.wwidth}">
				<a4j:support event="onchange" reRender="pannelloTest" />
			</rich:inputNumberSlider>
		</a4j:form>
		<h:outputText value="#{msg.wcenter}"
			rendered="#{not empty selectedObjectBackBean._selected.wcenter}" />
		<a4j:form id="windowCenterForm">
			<rich:inputNumberSlider id="windowCenter"
				value="#{selectedObjectBackBean._selected.wcenter}" width="600px"
				maxValue="#{selectedObjectBackBean._selected._maxCenterPoint}"
				minValue="#{selectedObjectBackBean._selected._minCenterPoint}"
				step="1" showToolTip="true" enableManualInput="false"
				showInput="true"
				rendered="#{not empty selectedObjectBackBean._selected.wcenter}">
				<a4j:support event="onchange" reRender="pannelloTest" />
			</rich:inputNumberSlider>
		</a4j:form>
	</h:panelGrid>
</h:panelGrid>

