

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<html xmlns="http://www.w3.org/1999/xhtml"
>
 <head>
            <link rel="stylesheet"  href="css/style.css" />
            <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
            <f:loadBundle var="msg" basename="it.units.htl.web.messages"/>
            <title>Remote O3-DPACS</title>


</head>
<body>
<h3>test test test</h3>
<f:view locale="#{sessionScope.locale}" afterPhase="#{messageManager.resetMessage}">

<a4j:outputPanel id="prova">
	<a4j:include viewId="commons/messagePanel.jsp" />
</a4j:outputPanel>
</f:view>
</body>
</html>