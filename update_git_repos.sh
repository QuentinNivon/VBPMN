#!/bin/bash

CURRENT_DIR=$PWD
RELEASE_DIR="VBPMN_RELEASE"
VBPMN_WAR_LOCAL_PATH=$CURRENT_DIR"/vbpmn_dist/transformation.war"
VBPMN_WAR_SOURCE="/home/quentin/Documents/quentinnivon.github.io/"
VBPMN_WAR_PATH=$VBPMN_WAR_SOURCE"vbpmn/"
VBPMN_WAR_FILE_NAME="transformation.war"

#Copy all files from ``~/Documents/vbpmn'' to ``~/Documents/VBPMN_RELEASE''
cd ../$RELEASE_DIR;
echo Entering $PWD...
echo Cleaning $PWD...
rm -r ./*;
echo Leaving $PWD...
cd $CURRENT_DIR;
echo Entering $PWD...
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
echo Leaving $PWD...
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

#Copy new WAR file to ``~/Documents/quentinnivon.github.io/vbpmn''
rm $VBPMN_WAR_PATH$VBPMN_WAR_FILE_NAME;
cp $VBPMN_WAR_LOCAL_PATH $VBPMN_WAR_PATH;
echo Leaving $PWD...
cd $VBPMN_WAR_SOURCE;
echo Entering $PWD...
git add *;
git commit -m "new WAR";
git push;

#Go back to original directory
echo Leaving $PWD...
cd $CURRENT_DIR;
echo Entering $PWD...
