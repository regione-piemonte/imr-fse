<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>

<f:loadBundle var="msg" basename="it.units.htl.web.messages"/>
<div id="navigation">
    <table id="navigationTable">
        <tr><td align="left"><f:verbatim><h:outputText value="#{msg.loggedAs}" /> &nbsp; </f:verbatim>
        <h:outputText value="#{userBean.firstName} #{userBean.lastName}" />
        </td><td align="left"><f:verbatim><h:outputText value="#{msg.lastLogin}:"/> &nbsp; </f:verbatim>
        <h:outputText value="#{userBean.lastLoginDate}"><f:convertDateTime pattern="dd/MM/yyyy" /></h:outputText><f:verbatim> &nbsp; </f:verbatim>
        <h:outputText value="#{userBean.lastLoginTime}"><f:convertDateTime pattern="HH:mm:ss" /></h:outputText>
        </td><td align="right"><h:form><h:commandButton rendered="#{!userBean.ldap}" action="changePwd" value="#{msg.changePwd}"></h:commandButton>
        <h:commandButton action="#{loginBackBean.logout}" accesskey="l" value="[l] #{msg.logout}"></h:commandButton></h:form>
</td></tr>
    </table>
    <a4j:include viewId="messagePanel.jsp"></a4j:include>
</div>
