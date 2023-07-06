<!-- Comment to prevent IE6 bug -->
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!-- JSF's Library -->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <f:view locale="#{sessionScope.locale}" afterPhase="#{messageManager.resetMessage}">
        <head>
            <link rel="stylesheet"  href="<h:outputText value="#{facesContext.externalContext.requestContextPath}/css/style.css" />" />
            <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
            <title>Remote O3-DPACS</title>
        </head>
        <body>
            <center>
                <div id="container">
                    
                    <!-- header content -->
                    <f:subview id="header">
                        <jsp:include page="/commons/header.inc.jsp" />
                    </f:subview>
                    
                    <!-- navigation content -->                    
                    <f:subview id="navigation">
                        <jsp:include page="/commons/navigation.inc.jsp" />
                    </f:subview>
                    
                    <div id="wrapper">
                        <div id="contentTitle">O3-DPACS-WEB</div>
                        <div id="content">
                            <p>A seguito dell'esperienza maturata dal laboratorio HTL con il progetto DPACS, è in corso di sviluppo 
                            una nuova versione del software per la gestione dei dati e immagini cliniche, denominata O3-DPACS.</p>
                            <p>Sono peculiari le scelte implementative, in primis legate alla preoccupazione di non obbligare la 
                                soluzione proposta all'utilizzo di un solo sistema operativo. E' utilizzata la piattaforma Java, 
                                in particolare la Enterprise Edition, che può contare su prodotti che possono garantire caratteristiche 
                                aggiuntive, quali la modularità, scalabilità e la messaggistica, oltre alla più ovvia portabilità. 
                                La seconda è dichiarare il sistema open-source: questa politica può assicurare quella flessibilità 
                                necessaria a promuovere una vasta adozione dei sistemi informativi e di comunicazione nel settore della 
                                sanità. Inoltre, la scalabilità e la configurabilità del software permettono di prevedere gli scenari 
                                d'applicazione più diversi per ogni dimensione d'azienda. Non ultima, l'adozione di un'interfaccia web 
                            rende gli strumenti per la personalizzazione e l'amministrazione più accessibili.</p>
                            <p>Il sistema è pensato per essere conforme ad alcuni profili IHE, pur mantenendo la possibilità di 
                                ulteriori estensioni per operare in ambienti non conformi. Tali estensioni possono essere implementate 
                                adhoc in maniera estremamente semplice grazie alla modularità con cui il sistema è stato sviluppato. 
                                Da ciò la possibilità di utilizzare O3-DPACS in un "ambiente IHE" pur mantenendo la compatibilità nelle 
                                comunicazioni con l'esterno. Una eventuale integrazione transnazionale, necessità primaria per l'area 
                                geo-politica nella quale si sviluppa il progetto e non solo, può essere ottenuta con altrettanta 
                            semplicità.</p>
                        </div>
                        
                        <!-- footer content -->
                        <f:subview id="footer">                 
                            <jsp:include page="/commons/footer.inc.html" />
                        </f:subview>
                        
                    </div>
                </div>
            </center>
        </body>
    </f:view>
</html>