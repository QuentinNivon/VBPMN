#!/bin/bash

CURRENT_DIR=$PWD
RELEASE_DIR=VBPMN_RELEASE

cd ../$RELEASE_DIR;
rm -r ./*;
cd $CURRENT_DIR;
cp -r vbpmn_dist ../$RELEASE_DIR;
cp -r vbpmn_transformation ../$RELEASE_DIR;
cp check_compatibility.sh ../$RELEASE_DIR;
cp generate_jar.sh ../$RELEASE_DIR;
cp generate_jar_and_deploy.sh ../$RELEASE_DIR;
cp Install.md ../$RELEASE_DIR;
cp LICENSE.md ../$RELEASE_DIR;
cp NOTICE.md ../$RELEASE_DIR;
cp README.md ../$RELEASE_DIR;
cp README_CADP_UPDATE.txt ../$RELEASE_DIR;
rm -r ../$RELEASE_DIR/vbpmn_transformation/data;
rm -r ../$RELEASE_DIR/vbpmn_transformation/target;
rm -r ../$RELEASE_DIR/vbpmn_transformation/ter_docs;
git add *;
git commit -m "new version";
git push;
