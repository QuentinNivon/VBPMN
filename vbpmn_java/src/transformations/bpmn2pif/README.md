# BPMN to PIF

This document is to understand the relation between the BPMN editor in Eclipse and our tools.

## Installation of Eclipse

1. Get Eclipse IDE for Java EE Developers (**tested with Mars**)
  
  [link](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/marsr)

2. Create a directory *MARS_BPMN* somewhere, it will serve as an Eclipse workspace
3. Run Eclipse and upon selection of the workspace browse to select *MARS_BPMN*
4. Install the BPMN plugin:

	a. Help menu > Install New Software
	
	b. Work with: Mars - *http://download.eclipse.org/releases/mars*
	
	c. Type filter text: BPMN
	
	d. Select at least the BPMN2 Metamodel, the BPMN2 Modeler - Diagram Editor, and the BPMN2 Modeler - Examples (**tested with versions 1.2.0.Final, 1.2.0.201506101742, and 1.2.0.201506101742 respectively**)
	
	e. Next (twice), accept the terms of the licence, and Finish (Eclipse should then restart)
	
## Try out

1. Create a new Java project *BPMN_models*, Finish (and accept the default Java perspective if proposed)
2. In the project, create a new folder *models*
3. In the *models* folder, New > Other ... > BPMN2 > Generic BPMN 2.0 Diagram, Next
4. Select the Process type, Next, name the process *Hello.bpmn*, Finish
5. You should get a simple empty process with a start event, an end event, and a sequence flow between them. You can edit this process using the palette.
