# Prodotto
Il prodotto ***Immagini in Rete del FSE***   realizza un sistema software per consentire al FSE (Fascicolo Sanitario Elettronico) di rendere disponibili a cittadini e operatori sanitari, laddove previsto in ottemperanza alla normativa specifica in materia di FSE, un pacchetto compresso ZIP contenente le immagini radiologiche associate ai referti di radiologia. 



# Descrizione del prodotto 
Il prodotto è composto attualmente dalle seguenti componenti 
| Componente |Descrizione  |Versione |
|--|--|--|
| DMASS | un modulo per la gestione delle richieste di scarico del pacchetto delle immagini | 1.0.0 |
| O3DPACS | modulo, definito PACS, per la gestione delle immagini | 2.0.0 |
| O3DPACS-PDI | modulo per la gestione dei pacchetti contenti le immagini e il software per la visione delle stesse | 2.0.0 |
| DMASSBATCHCANCPACC | Script per creazione database che memorizza tutte le informazioni relative a utenti, ruoli, collocazioni e abilitazioni attive | 1.0.0 |

Le interfacce utente (implementate con una PWA -progressive web application) per il cittadino sono disponibili sul prodotto (SANSOL) [https://github.com/regione-piemonte/sansol/tree/main/sansolfse]
La pwa richiama servizi JSON/REST disponibili sul prodotto (APISAN) [https://github.com/regione-piemonte/apisan/tree/master/apisanfse]


# Prerequisiti di sistema 

## Software
- [Apache 2.4](https://www.apache.org/)
- [RedHat JBoss EAP 6.4](https://developers.redhat.com/products/eap/download)
- [JDK Oracle 1.8](https://www.oracle.com/java/technologies/downloads/archive/) 
- [PostgreSQL 9.6](https://www.postgresql.org/download/)
- [CentOS 7.6](https://www.centos.org/)

## Dipendenze da sistemi esterni

### Sistema FSE della Regione Piemonte
Il modulo DMASSIMR dipende da Web Services del FSE regionale piemontese per la verifica della possibilità di scaricare il pacchetto delle immagini.

I servizi sono:
- VerificaPin
- VerificaPresenzaImmagini
- verificaOscuramentoDoc
- VerificaUtenteAbilitato
- StatoConsensoEXT
Il WSDL è riportato nella directory DOCS/wsdl-dmass

### Sistema gateway PACS 
Il modulo DMASS dipende dal modulo DMASSIMR che comprende: 
- un servizio JSON/REST (createPDI) invocato per la creazione del pacchetto delle immagini. 
- un batch che gli notifica sul servizio JSON/REST (sendCompleteNotice)  la conclusione della creazione del pacchetto.

### Sistema di autenticazione
Il sistema di autenticazione con cui sono protette le servlet della componente DMASSIMR  è esterno al presente prodotto ed è basato sul framework SHIBBOLETH composto da Service Provider e Identity Provider. 

# Installing
Vedere il file install.pdf nella cartella DOCS 


# Versioning
Per il versionamento del software si usa la tecnica Semantic Versioning (http://semver.org).

# Authors
La lista delle persone che hanno partecipato alla realizzazione del software sono:
- Davide Elia 
- Davide Grosso
- Davide Toppazzini
- Michele Mastrorilli
- Nicola Gaudenzi
- Stefano Spera
- Veronica Berti
- Yvonne Carpegna


# Copyrights
© Copyright Regione Piemonte – 2022


# License
GPL-2.0.

Vedere il file LICENSE.txt per i dettagli.
