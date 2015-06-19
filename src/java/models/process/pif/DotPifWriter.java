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
 * Copyright (C) 2015 Pascal Poizat (@pascalpoizat)
 * emails: pascal.poizat@lip6.fr
 */

// TODO ISSUE : pas beau, utiliser plutôt un visiteur ?

// TODO améliorer wrt research articles
// http://ceur-ws.org/Vol-1295/paper4.pdf
// http://www.researchgate.net/publication/221542866_A_Simple_Algorithm_for_Automatic_Layout_of_BPMN_Processes

package models.process.pif;

import models.base.*;
import models.process.pif.generated.*;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DotPifWriter extends AbstractStringModelWriter {
    private static final String TASK_STYLE = "shape=record,style=\"filled,bold\",fixedsize=true,width=3,height=1,fillcolor=white,color=black";
    private static final String FINAL_STYLE = "shape=doublecircle,style=\"filled,bold\",fixedsize=true,width=0.5,fillcolor=black,color=black";
    private static final String INITIAL_STYLE = "shape=circle,style=\"filled,bold\",fixedsize=true,width=0.25,fillcolor=black,color=black";
    private static final String ALLSELECT_STYLE = "shape=circle,style=\"filled,bold\",fixedsize=true,width=0.5,fillcolor=white,color=black,fontsize=18,label=\"+\"";
    private static final String ALLJOIN_STYLE = "shape=circle,style=\"filled,bold\",fixedsize=true,width=0.5,fillcolor=white,color=black,fontsize=18,label=\"+\"";
    private static final String CHOICE_STYLE = "shape=circle,style=\"filled,bold\",fixedsize=true,width=0.5,fillcolor=white,color=black,fontsize=18,label=\"X\"";
    private static final String SIMPLEJOIN_STYLE = "shape=circle,style=\"filled,bold\",fixedsize=true,width=0.5,fillcolor=white,color=black,fontsize=18,label=\"X\"";
    private static final String SUBSETSELECT_STYLE = "shape=circle,style=\"filled,bold\",fixedsize=true,width=0.5,fillcolor=white,color=black,fontsize=18,label=\"O\"";
    private static final String SUBSETJOIN_STYLE = "shape=circle,style=\"filled,bold\",fixedsize=true,width=0.5,fillcolor=white,color=black,fontsize=18,label=\"O\"";
    private static final String TRANSITION_STYLE = "";

    @Override
    public String getSuffix() {
        return "dot";
    }

    @Override
    public String modelToString(AbstractModel model) throws IllegalResourceException, IllegalModelException {
        checkModel(model, PifModel.class);
        PifModel pifModel = (PifModel) model;
        String rtr = "";
        rtr += String.format("digraph %s {\n" +
                        "rankdir=LR;\n" +
                        "fontname=\"Arial\";\n" +
                        "fontsize=\"14\";\n" +
                        "bgcolor=\"transparent\";\n" +
                        "concentrate=true;\n",
                normalizeId(pifModel.getModel().getName()));
        try {
            // generate nodes
            List<WorkflowNode> allStates = new ArrayList<>();
            for (WorkflowNode state : pifModel.getModel().getBehaviour().getNodes()) {
                rtr += modelToString(pifModel, state);
                allStates.add(state);
            }
            // generates edges
            for (WorkflowNode sourceState : allStates) {
                rtr += modelToString(sourceState, sourceState.getOutgoingFlows());
            }
        } catch (IllegalModelException e) {
            e.printStackTrace();
        }
        rtr += "}\n";
        return rtr;
    }

    public String modelToString(WorkflowNode source, List<SequenceFlow> flows) {
        String rtr = "";
        for (SequenceFlow flow : flows) {
            rtr += String.format("%s -> %s [%s];\n", normalizeId(flow.getSource().getId()), normalizeId(flow.getTarget().getId()), TRANSITION_STYLE);
        }
        return rtr;
    }

    public String modelToString(PifModel model, WorkflowNode state) throws IllegalModelException {
        if(state instanceof  InitialEvent) {
            return modelToString(model, (InitialEvent) state);
        }
        if(state instanceof EndEvent) {
            return modelToString(model, (EndEvent) state);
        }
        if(state instanceof Task) {
            return modelToString(model, (Task) state);
        }
        if (state instanceof MessageSending) {
            return modelToString(model, (MessageSending) state);
        }
        if (state instanceof MessageReception) {
            return modelToString(model, (MessageReception) state);
        }
        if (state instanceof Interaction) {
            return modelToString(model, (Interaction) state);
        }
        if (state instanceof JoinGateway) {
            return modelToString(model, (JoinGateway) state);
        }
        if (state instanceof SplitGateway) {
            return modelToString(model, (SplitGateway) state);
        }
        throw new IllegalModelException(String.format("Element %s of class %s is not supported", state.getId(), state.getClass().toString()));
    }

    public String modelToString(PifModel model, Task taskState) {
        return String.format("%s [%s," +
                "label=\"%s\"" +
                "];\n", normalizeId(taskState.getId()), TASK_STYLE, normalizeId(taskState.getId()));

    }

    public String modelToString(PifModel model, InitialEvent initialState) {
        return String.format("%s [%s," +
                "label=\"\"" +
                "];\n", normalizeId(initialState.getId()), INITIAL_STYLE);
    }

    public String modelToString(PifModel model, EndEvent finalState) {
        return String.format("%s [%s," +
                "label=\"\"" +
                "];\n", normalizeId(finalState.getId()), FINAL_STYLE);
    }

    public String modelToString(PifModel model, Interaction state) throws IllegalModelException {
        Message message = state.getMessage();
        String messageSender = state.getInitiatingPeer().getId();
        String messageLabel = message.getId();
        // TODO : lourdeur de JAXB à comprendre, en attendant, généraliser à n receveurs
        // String messageReceiver = state.getReceivingPeers().stream().map(x -> x.getId()).collect(Collectors.joining(","));
        try {
            Object o = state.getReceivingPeers().get(0);
            JAXBElement<Peer> j = (JAXBElement<Peer>)o;
            Peer p = j.getValue();
            String messageReceiver = p.getId();
            return String.format("%s [%s," +
                    "label=\"%s | %s | %s\"" +
                    "];\n", normalizeId(state.getId()), TASK_STYLE, normalizeId(messageSender), normalizeId(messageLabel), normalizeId(messageReceiver));
        }
        catch (IndexOutOfBoundsException e) {
            throw new IllegalModelException("no receiver in Interaction");
        }
    }

    public String modelToString(PifModel model, MessageSending state) throws IllegalModelException {
        Message message = state.getMessage();
        return String.format("%s [%s," +
                "label=\"%s !\"" +
                "];\n", normalizeId(state.getId()), TASK_STYLE, normalizeId(message.getId()));
    }

    public String modelToString(PifModel model, MessageReception state) throws IllegalModelException {
        Message message = state.getMessage();
        return String.format("%s [%s," +
                "label=\"%s ?\"" +
                "];\n", normalizeId(state.getId()), TASK_STYLE, normalizeId(message.getId()));
    }

    public String modelToString(PifModel model, JoinGateway state) throws IllegalModelException {
        if (state instanceof AndJoinGateway) {
            return String.format("%s [%s];\n", normalizeId(state.getId()), ALLJOIN_STYLE);
        }
        if (state instanceof XOrJoinGateway) {
            return String.format("%s [%s];\n", normalizeId(state.getId()), SIMPLEJOIN_STYLE);
        }
        if (state instanceof OrJoinGateway) {
            return String.format("%s [%s];\n", normalizeId(state.getId()), SUBSETJOIN_STYLE);
        }
        throw new IllegalModelException(String.format("Element %s of class %s is not supported", state.getId(), state.getClass().toString()));
    }

    public String modelToString(PifModel model, SplitGateway state) throws IllegalModelException {
        if (state instanceof AndSplitGateway) {
            return String.format("%s [%s];\n", normalizeId(state.getId()), ALLSELECT_STYLE);
        }
        if (state instanceof XOrSplitGateway) {
            return String.format("%s [%s];\n", normalizeId(state.getId()), CHOICE_STYLE);
        }
        if (state instanceof OrSplitGateway) {
            return String.format("%s [%s];\n", normalizeId(state.getId()), SUBSETSELECT_STYLE);
        }
        throw new IllegalModelException(String.format("Element %s of class %s is not supported", state.getId(), state.getClass().toString()));
    }

    /**
     * normalizes ids so that dot accepts them, eg replacing spaces by underscores
     * @param id the raw id to normalize
     * @return the normalized id
     */
    private static String normalizeId(String id) {
        String rtr;
        rtr = id.replace(" ", "_");
        return rtr;
    }

}
