VBPMN Transformation Project
=============================

This is a maven based web project host on the tomcat server. Project provides an interface to compare two BPMN models.

Software Requirements to run the project locally
* JDK 1.8+
* Tomcat 8.0+
* Eclipse / Intellij / Maven 
* Library dependencies mentioned in the maven pom.xml
* CADP http://cadp.inria.fr/
* Python (with pyxb 1.1.5)

In addition to libraries, additional folders need to be configured as part of the project.
`resources/transformation.properties` file has the list of folders to be created (or change path to point to right folders)
`scripts` folder contains the vbpmn scripts that need to be executed to perform comparison of models. 

Sample inputs are available in the data folder of the project. 

PIF generation code does not handle the following scenarios in BPMN xml input.
* Model with subprocess.
* Single model xml input with multiple processes.
The PIF generator assumes that the input is a BPMN model with a single process in it. For example: model with choreographies would fail.
All the tags/elements that do not have a corresponding tag/element in PIF are skipped during the PIF generation process.  

The project is a quick implementation, things like exception handling, HMTL/JS code are bit messy. 
Most of the exceptions are logged into console and log file. The location of the log file can be set in `resources/logback.xml`  