#!/bin/bash

export CATALINA_HOME
cd vbpmn_transformation;
mvn clean -f pom.xml;
mvn validate -f pom.xml;
mvn compile -f pom.xml;
mvn test -f pom.xml;
mvn package -f pom.xml;
mvn verify -f pom.xml;
mvn install -f pom.xml;
mvn site -f pom.xml;
mvn deploy -f pom.xml;
mvn war:war -f pom.xml;
cd target;
echo 'Catalina home: |'$CATALINA_HOME'|';
$CATALINA_HOME/bin/shutdown.sh;
rm -r $CATALINA_HOME/webapps/transformation;
rm $CATALINA_HOME/webapps/transformation.war;
cp transformation.war $CATALINA_HOME/webapps/transformation.war;
$CATALINA_HOME/bin/startup.sh;
BrowserToUse=firefox
$BrowserToUse http://localhost:8080/transformation/index.html;
