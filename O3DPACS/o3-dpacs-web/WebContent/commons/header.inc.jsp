<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<f:verbatim>
    <div id="header">
    <table><tr>
    <td align="left"><div id="logoSmall"></div></td>
    <td align="left">
    <div id="headerTitle">O3-DPACS-WEB, The Web Interface to O3-DPACS</div>
    <div id="menu"><ul>   

<f:loadBundle var="msg" basename="it.units.htl.web.messages"/>
<h:outputText value="<li><a href='#{facesContext.externalContext.requestContextPath}/studies/studiesSearch.jspf' accesskey='1'>[1] #{msg.pacsBrowser}</a></li>" rendered="#{sessionScope.actionEnabled.BrowsePacs}" escape="false"/>
<h:outputText value="<li><a href='#{facesContext.externalContext.requestContextPath}/merger/index.jspf' accesskey='2'>[2] #{msg.merger}</a></li>" rendered="#{sessionScope.actionEnabled.MergeUtilities}" escape="false"/>
<h:outputText value="<li><a href='#{facesContext.externalContext.requestContextPath}/reconciliation/index.jspf' > #{msg.reconciliation}</a></li>" rendered="#{sessionScope.actionEnabled.StudiesRecovery}" escape="false"/>

<h:outputText value="<li><a href='#{facesContext.externalContext.requestContextPath}/admin/recovery.jspf' > #{msg.studySerieRecovery}</a></li>" rendered="#{sessionScope.actionEnabled.StudiesRecovery}" escape="false"/>


<h:outputText value="<li><a href='#{facesContext.externalContext.requestContextPath}/nodes/nodesView.jspf' accesskey='3'>[3] #{msg.nodeManager}</a></li>" rendered="#{sessionScope.actionEnabled.NodesManagement}" escape="false"/>
<h:outputText value="<li><a href='#{facesContext.externalContext.requestContextPath}/forwarding/index.jspf' >#{msg.forwarding}</a></li>" rendered="#{sessionScope.actionEnabled.ForwardManagement}" escape="false"/>
<h:outputText value="<li><a href='#{facesContext.externalContext.requestContextPath}/admin/webuser/usersView.jspf' accesskey='4'>[4] #{msg.userManager}</a></li>" rendered="#{sessionScope.actionEnabled.WebUserMan}" escape="false"/>
<h:outputText value="<li><a href='#{facesContext.externalContext.requestContextPath}/admin/index.jspf' accesskey='5'>[5] #{msg.adminArea}</a></li>" rendered="#{sessionScope.actionEnabled.AdminArea}" escape="false"/>

    </ul></div>
    </td></tr>
    </table>
    </div>
</f:verbatim>
