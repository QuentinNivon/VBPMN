#!/bin/bash

CURRENT_DIR=$PWD
RELEASE_DIR=VBPMN_RELEASE

cd ../$RELEASE_DIR;
echo Entering $PWD...
echo Cleaning $PWD...
rm -r ./*;
echo Leaving $PWD...
cd $CURRENT_DIR;
echo Entering $CURRENT_DIR...
echo Copying files...
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
echo Leaving $CURRENT_DIR...
cd ../$RELEASE_DIR;
echo Entering $PWD...
echo Removing undesired files...
rm -r vbpmn_transformation/data;
rm -r vbpmn_transformation/target;
rm -r vbpmn_transformation/ter_docs;
echo Adding files to git...
git add *;
git commit -m "new version";
git push;
echo Leaving $PWD...
cd $CURRENT_DIR;
echo Entering $CURRENT_DIR...
