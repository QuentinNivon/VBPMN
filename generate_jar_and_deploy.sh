#!/bin/bash

BROWSER_TO_USE=firefox

export CATALINA_HOME;
./generate_jar.sh;
echo 'Catalina home: |'$CATALINA_HOME'|';
$CATALINA_HOME/bin/shutdown.sh;
rm -r $CATALINA_HOME/webapps/transformation;
rm $CATALINA_HOME/webapps/transformation.war;
cd vbpmn_transformation/target;
cp transformation.war $CATALINA_HOME/webapps/transformation.war;
$CATALINA_HOME/bin/startup.sh;
$BROWSER_TO_USE http://localhost:8080/transformation/index.html;
