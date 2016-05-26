VBPMN Transformation Project
=============================

VBPMN framework facilitates formal modelling and analysis of BPMN processes. The transformation project is a component in the framework that provides a graphical interface to compare BPMN 2.0 models.
The comparison is a two step process. Firstly, the BPMN 2.0 compliant models are taken as input. Using the transformation logic, the XML files are converted into XML based PIF format. 
As a next step, these PIF files are given as an input to the VBPMN scripts. Internally, the project invokes the python scripts which in turn connects to CADP. The results of comparison are displayed on the UI.
In case of a mismatch (i.e. False result), the counter example models are generated (In postscript format).

As of now PIF generation code does not handle the following scenarios in BPMN models (XML input).
* Model with subprocess.
* Single model with multiple processes.
The PIF generator assumes that the input is a BPMN model with only one process in it. For example: model with choreographies would fail.
All the tags/elements that do not have a corresponding tag/element in PIF are skipped during the PIF generation process.

This is a maven based web project hosted on the tomcat server. 

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

In the tomcat server.xml add location of output folder as follows (inside `<host>` tag), it is required if you need to view the postscript files.
`<Context docBase="/tmp/vbpmn/output" path="/transformation/results"/>`

Sample inputs are available in the data folder of the project. For testing you can use: `simple.bpmn`, `simple2.bpmn` and `ExpenseWorkflow.bpmn`. 
The BPMN 2.0 models generated using Activiti, Bonita, jBPM or any other BPMN 2.0 compliant modelers can be given as input.

The project is a quick implementation, things like exception handling, HMTL/JS code are bit messy. 
Most of the exceptions are logged into console and log file. The location of the log file can be set in `resources/logback.xml`