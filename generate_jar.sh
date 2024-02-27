#!/bin/bash

cd vbpmn_transformation;
mvn clean:clean;
mvn resources:resources -f pom.xml;
mvn war:war -f pom.xml;
