#!/bin/bash

cd vbpmn_transformation;
mvn clean test -DskipTests=false -Dtest=fr.inria.convecs.optimus.compatibility.FullTests;
