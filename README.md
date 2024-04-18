VBPMN - Verification of BPMN
==============================
The goal of VBPMN is to provide formal modelling and automated 
analysis techniques for ensuring correctness, efficiency, 
and quality of the whole business process execution. 

VBPMN provides a web interface where process designers can upload
BPMN 2.0 models and compare them using various comparison modes. 
The interface provides instant feedback through counter examples and 
helps users correct and refine their models.

Usage
===============================
VBPMN web app is available for download as a web archive (WAR). 
Hosting of the web app requires [Apache Tomcat 8.0](https://tomcat.apache.org/download-80.cgi). 

[**Download VBPMN**](https://quentinnivon.github.io/vbpmn/transformation.war) 

Once you have the web app running, you can access the application 
using the following web page (assuming tomcat is running locally on port 8080)

http://localhost:8080/transformation/home.html  

Required Softwares
=======================================
* [JDK >= 8.0](https://www.oracle.com/fr/java/technologies/downloads/)
* [Apache Tomcat 8.0](https://tomcat.apache.org/download-80.cgi)
* [CADP >= 2023-k](https://cadp.inria.fr/)

Important information
======================================
The VBPMN tool relies on the CADP toolbox that is updated once per month.
If you upgrade CADP on your machine, VBPMN must also be upgraded to a newer version, compliant with the most recent version of CADP.
This can be done by downloading again the [VBPMN WAR file](https://github.com/QuentinNivon/VBPMN/tree/main/vbpmn_dist/transformation.war)
and replacing the old WAR file of the Tomcat by the new one.
If you have any issue with a subsequent version of VBPMN, please send an email to quentin.nivon@inria.fr.

Browser Compatibility
====================================
The web app has been tested on the following browsers.

* Mozilla Firefox 47.0
* Google Chrome 51.0.2704

Contributors
=====================================
* [Pascal Poizat](http://pascalpoizat.github.io/)
* [Gwen Salaün](http://convecs.inria.fr/people/Gwen.Salaun/) 
* [Ajay Krishna](https://about.me/ajaykrishna)
* [Quentin Nivon](https://quentinnivon.github.io/)

License
=============================
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE.md)

Web pages
============
The website is built on top of:
- [Bootstrap](http://getbootstrap.com/), version 3.0.3, licenced under Apache Licence 2.0
- [Font Awesome](http://fortawesome.github.io/Font-Awesome/), version 4.1.0, licenced under SIL OFL 1.1 (desktop and webfont files) and MIT Licence (css and less files)
- [Glyph Icons Halflings](http://glyphicons.com/), released under the same licence as Bootstrap

