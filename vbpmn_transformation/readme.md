VBPMN Transformation Project
===============================

Project Description
-------------------------------

VBPMN framework facilitates formal modelling and analysis of BPMN
processes. The transformation project is a component in the framework
that provides a web based graphical interface to compare BPMN 2.0
models.  The comparison is a two step process. Firstly, the BPMN 2.0
compliant models are taken as input. Using the transformation logic,
the BPMN 2.0 XML files are converted into XML based PIF format.  As a
next step, these PIF files are given as an input to the VBPMN
scripts. Internally, the project invokes the python scripts which in
turn connects to CADP. The results of comparison are displayed on the
UI (True or False).  In case of a mismatch (i.e. False result), the
counterexample model is generated (In postscript format).

Developer Setup
-------------------------------

This is a maven based web project hosted on the tomcat server. 

Software Requirements to run the project locally

### Software

* JDK 1.8+ [link](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* Tomcat 8.0+ [link](http://tomcat.apache.org/download-80.cgi)
* Eclipse OR Intellij OR Maven [link](https://eclipse.org/downloads/packages/release/Mars/2)
* Library dependencies mentioned in the maven pom.xml (if maven is available, these would be automatically resolved)
* CADP inria [link](http://cadp.inria.fr/)
* Python (with pyxb 1.1.5) (pip installer can be used)

### Application Configuration

If you are using Eclipse, use the following steps to import the project into workspace
1. Open Eclipse
2. Click on File -> Import
3. Type **Maven** in the searchbox available in the wizard
4. Select **Existing Maven Projects**
5. Click **Next**
6. Browse to the transformation code location and select the **pom.xml** file
7. Click finish

For Intellij, please follow the following tutorial [link](https://www.jetbrains.com/help/idea/2016.1/importing-project-from-maven-model.html)

For installing `tomcat server` in Eclispe, follow the tutorial [link](https://www.eclipse.org/webtools/jst/components/ws/1.0/tutorials/InstallTomcat/InstallTomcat.html)

The current project structure is as follows

|transformation
|-- data
|   |-- input
|   |-- output
|   `-- pif.xsd
|-- logs
|-- pom.xml
|-- readme.md
|-- **scripts**
|   |-- __init__.py
|   |-- pif2lnt.py
|   |-- pif2lnt.pyc
|   |-- pif.py
|   |-- pif.pyc
|   `-- vbpmn.py
`-- src
    |-- main
    |   |-- java
    |   |   `-- fr
    |   |-- resources
    |   |   |-- **logback.xml**
    |   |   |-- pif.xsd
    |   |   \`-- **transformation.properties**
    |   `-- webapp
    |       |-- home.html
    |       |-- index.jsp
    |       |-- media
    |       `-- WEB-INF
    `-- test
        `-- java
            `-- fr

* --- !MODIFY! `resources/transformation.properties` - Change the location of `SCRIPTS_PATH` to point to the location where VBPMN python scripts are located

* --- !MODIFY! In the tomcat `server.xml` (found in the servers project in Eclipse) add the following line: 
`<Context docBase="/tmp/vbpmn/output" path="/transformation/results"/>`
Append it inside `<host>` tag, it is required if you need to view the
postscript counterexample files. Alternatively, you can create a
separate context file for the instance if you prefer. For more
details: See
[link](http://www.moreofless.co.uk/static-content-web-pages-images-tomcat-outside-war/)



Testing and Usage
--------------------------------------------

##Samples

A few sample inputs are available in the data folder of the
project. For testing you can use: `simple.bpmn`, `simple2.bpmn` and
`ExpenseWorkflow.bpmn`.  The BPMN 2.0 models generated using Activiti,
Bonita, jBPM or any other BPMN 2.0 compliant modelers can be given as
input.

###Restrictions and Limitations

As of now PIF generation code does not handle the following scenarios in BPMN models (XML input).
* Model with subprocess.
* Single model with multiple processes.

The PIF generator assumes that the input is a BPMN model with only one
process in it. For example: model with choreographies would fail.  All
the tags/elements that do not have a corresponding tag/element in PIF
are skipped during the PIF generation process.

The project is a quick implementation, things like exception handling,
HMTL/JS code are bit messy. It might throw some exceptions. However,
all the exceptions are logged to a file and the console to help debug
the issue.
