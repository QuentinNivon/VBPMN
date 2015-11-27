#!/usr/bin/env bash

# pif2dot should be on your PATH
# eg using:
# export PATH=$PATH:/Users/pascalpoizat/IdeaProjects/vbpmn/vbpmn_java/src/java/transformations/pif2dot

# change this to the path where transformations.pif2dot is
# TODO : integrate into FMT
export PIF2DOTPATH="/Users/pascalpoizat/IdeaProjects/vbpmn/out/production/vbpmn"

# change this to the path where fmt.jar is
export FMTPATH="/Users/pascalpoizat/IdeaProjects/fmt/archive/fmt.jar"

# change this to the path where pif.xsd is
# TODO : integrate into FMT
export PIFXSDPATH="/Users/pascalpoizat/IdeaProjects/vbpmn/model/pif.xsd"

# do not change below
java -cp "$FMTPATH:$PIF2DOTPATH" transformations.pif2dot.Pif2Dot $PIFXSDPATH $1 $2
