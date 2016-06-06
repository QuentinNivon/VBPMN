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
* Eclipse / Intellij / Maven [link](https://eclipse.org/downloads/packages/release/Mars/2)
* Library dependencies mentioned in the maven pom.xml (if maven is available, these would be automatically resolved)
* CADP inria [link](http://cadp.inria.fr/)
* Python (with pyxb 1.1.5) (pip installer can be used)

### Application Configuration

In addition to the above mentioned software libraries, additional
folders need to be configured as part of the project. The current
project structure is as follows

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

* --- !MODIFY! `resources/transformation.properties` - It has the list
       of folders to be available for the program to run (create new
       folders or change path to point to right folders)

* Exceptions are logged into console and log file. The location of the
  log file location can be set in `resources/logback.xml`

* `scripts` folder contains the vbpmn python scripts that need to be
  executed to perform comparison of models.

* In the tomcat `server.xml` add location of output folder as follows
(inside `<host>` tag), it is required if you need to view the
postscript counterexample files. Alternatively, you can create a
separate context file for the instance if you prefer. For more
details: See
[link](http://www.moreofless.co.uk/static-content-web-pages-images-tomcat-outside-war/)
`<Context docBase="/tmp/vbpmn/output"
path="/transformation/results"/>`


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
