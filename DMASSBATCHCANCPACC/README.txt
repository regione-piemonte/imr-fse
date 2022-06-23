NOTE: 
specificare in environment.[local\prod\test].properties i parametri 
secondo l'environment
configurare run configuration -> maven build
Goals=pacakge
profile=local\prod\test

aggiunte come property https.protocols=TLSv1.2

eseguire il run

il comanda dovrebbe essere alla fine tipo
mvn -Dhttps.protocols=TLSv1.2 ... 
 
Per compilare il jar con maven eseguire run->