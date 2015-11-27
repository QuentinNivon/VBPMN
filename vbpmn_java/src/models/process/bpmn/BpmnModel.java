/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * <p>
 * vbpmn
 * Copyright (C) 2014 Pascal Poizat (@pascalpoizat)
 * emails: pascal.poizat@lip6.fr
 */

package models.process.bpmn;

// fmt

import models.base.AbstractModel;
import models.base.IllegalModelException;
// bpmn2
import org.eclipse.bpmn2.*;
// ecore
import org.eclipse.bpmn2.Process;
import org.eclipse.emf.ecore.EObject;
// java
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// reference for BPMN2 meta model
// https://git.eclipse.org/c/bpmn2/org.eclipse.bpmn2.git/tree/org.eclipse.bpmn2/src/org/eclipse/bpmn2

public class BpmnModel extends AbstractModel {

    private Definitions model;
    private FlowElementsContainer behavior;
    private boolean is_choreography;

    private static final String CHOREOGRAPHY_TYPE = "Choreography";
    private static final String PROCESS_TYPE = "Process";

    public BpmnModel() {
        super();
        model = null;
        behavior = null;
        is_choreography = false;
    }

    public void setModel(Definitions definition) throws IllegalModelException {
        model = definition;
        setProcess();
    }

    public Definitions getModel() {
        return model;
    }

    /**
     * sets the flow elements container (Process or Choreography) from the BPMN model
     * TODO the setup of is_choreography is not correct
     *
     * @throws IllegalModelException if the model does not contain a flow element container
     */
    private void setProcess() throws IllegalModelException {
        boolean found = false;
        if (model.eContents().size() > 0) {
            for (EObject definition : model.eContents()) {
                System.out.println(definition.eClass().getName());
                if (definition.eClass().getName().equals(CHOREOGRAPHY_TYPE)) {
                    behavior = (Choreography) definition;
                    is_choreography = true;
                    found = true;
                    break;
                } else if (definition.eClass().getName().equals(PROCESS_TYPE)) {
                    behavior = (Process) definition;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalModelException("No flow elements container definition found in the model");
            }
        } else {
            throw new IllegalModelException("No definitions found in the model");
        }
    }

    public String getId() {
        return behavior.getId();
    }

    public String getName() {
        if (is_choreography) {
            return ((Choreography) behavior).getName();
        } else {
            return ((Process) behavior).getName();
        }
    }

    /**
     * Gets the list of participants for the model. If a process, there cannot be participants.
     *
     * @return the list of participants (if choreography) or empty list (if process)
     */
    public List<Participant> getParticipants() {
        if (is_choreography) {
            return ((Choreography) behavior).getParticipants();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Gets the list of flow elements for the model.
     *
     * @return the list of flow elements
     */
    public List<FlowElement> getFlowElements() {
        return behavior.getFlowElements();
    }

    /**
     * Gets the list of messages for the model.
     *
     * @return the list of messages
     */
    public List<org.eclipse.bpmn2.Message> getMessages() {
        return model.getRootElements().stream()
                .filter(element -> element instanceof Message)
                .map(element -> (Message) element)
                .collect(Collectors.toList());
    }

    /**
     * Gets the documentation for the model ( = the one for the root model + the one for the behavior )
     *
     * @return the documentation
     */
    public List<Documentation> getDocumentation() {
        List<Documentation> rtr = new ArrayList<>();
        if (model != null)
            rtr.addAll(model.getDocumentation());
        if (behavior != null)
        rtr.addAll(behavior.getDocumentation());
        return rtr;
    }

}

