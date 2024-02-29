#!/bin/bash

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
rm ../vbpmn_dist/transformation.war
cd target;
cp transformation.war ../../vbpmn_dist/transformation.war
