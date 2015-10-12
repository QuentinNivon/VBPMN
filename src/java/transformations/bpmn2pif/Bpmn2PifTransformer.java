/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * <p>
 * fmt
 * Copyright (C) 2014 Pascal Poizat (@pascalpoizat)
 * emails: pascal.poizat@lip6.fr
 */
package transformations.bpmn2pif;

// fmt

import models.base.IllegalModelException;
import models.base.IllegalResourceException;
import transformations.base.AbstractTransformer;
// vbpmn
import models.process.bpmn.BpmnModel;
import models.process.pif.PifModel;
import models.process.pif.generated.Message;
import models.process.pif.generated.*;
// bpmn2
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.*;
// java
import java.util.*;
import java.util.stream.Collectors;

/**
 * Transformation from BPMN to PIF
 * <p>
 * TODO: more comments
 * TODO: replace cascading method calls by delegation
 * TODO: support the flow between nodes
 * TODO: modify the PIF generation to get correct namespaces (PifPifWriter)
 * <p>
 * differences wrt. the Bpmn2Cif transformation:
 * - no messages are generated for ChoreographyTasks without messages (TODO EXTENSION)
 */
public class Bpmn2PifTransformer extends AbstractTransformer {

    public static final String NAME = "bpmn2pif";
    public static final String VERSION = "1.0";

    Map<String, String> trans_participants; // used to keep track of BPMN:id -> PIF:id for participants (peers)
    Map<String, Peer> participants;         // map of participants (used to optimize wrt the model that uses a list and not a map)
    Map<String, String> messages;           // used to keep track of BPMN:id -> PIF:id for messages
    Map<String, WorkflowNode> nodes;        // used to keep track of BPMN:id -> PIF:node for nodes
    boolean has_initial_state;              // start event has been found
    boolean has_final_state;                // end event has been found

    public Bpmn2PifTransformer() {
        super();
        participants = new HashMap<>();
        trans_participants = new HashMap<>();
        messages = new HashMap<>();
        nodes = new HashMap<>();
        has_initial_state = false;
        has_final_state = false;
    }

    /**
     * Performs the BPMN to PIF transformation.
     *
     * @throws IllegalResourceException
     * @throws IllegalModelException
     */
    @Override
    public void transform() throws IllegalResourceException, IllegalModelException {
        checkModel(inputModel, BpmnModel.class);
        checkModel(outputModel, PifModel.class);
        PifModel mout = ((PifModel) outputModel);
        BpmnModel min = ((BpmnModel) inputModel);
        mout.setName(min.getName());
        setDocumentation(min, mout);
        setParticipants(min, mout);
        setMessages(min, mout);
        setBehavior(min, mout);
        message("** Transformation achieved");
    }

    /**
     * Sets the documentation of the process.
     * TODO ISSUE PROBLEM WITH LOADING FROM BPMN
     *
     * @param min  BPMN model where the documentation is defined
     * @param mout PIF model whee the documentation is to be defined
     */
    private void setDocumentation(BpmnModel min, PifModel mout) {
        List<Documentation> documentations = min.getDocumentation();
        String documentation = documentations.stream().map(x -> x.toString()).collect(Collectors.joining());
        mout.getModel().setDocumentation(documentation);
    }

    /**
     * Sets the participants of the process. pif::Peer.id is bpmn::Participant.name or bpmn::Participant.id if undefined.
     *
     * @param min  BPMN model where the participants are defined
     * @param mout PIF model where the participants are to be defined
     * @throws IllegalModelException
     */
    private void setParticipants(BpmnModel min, PifModel mout) throws IllegalModelException {
        List<Participant> bpmnPeers = min.getParticipants();    // participants in the BPMN model
        for (Participant bpmnPeer : bpmnPeers) {
            Peer pifPeer = new Peer();
            String pifPeerId = getNameOrIdForParticipant(bpmnPeer);
            String bpmnPeerId = bpmnPeer.getId();
            if (bpmnPeerId == null) {
                IllegalModelException e = new IllegalModelException("BPMN model is incorrect (a participant has no id)");
                error(e.getMessage());
                throw e;
            }
            pifPeer.setId(pifPeerId);
            trans_participants.put(bpmnPeerId, pifPeerId);
            participants.put(pifPeerId, pifPeer);
        }
    }

    /**
     * Sets the messages of the process. pif::Message.id is bpmn::Message.name or bpmn::Message.id if undefined
     * TODO EXTENSION MESSAGES CAN ALSO BE IMPLICIT AND COME FROM SPECIFIC TASKS (CHOREOGRAPHY, SEND, AND RECEIVE TASKS)
     *
     * @param min  BPMN model where the messages are defined
     * @param mout PIF model where the messages are to be defined
     * @throws IllegalModelException
     */
    private void setMessages(BpmnModel min, PifModel mout) throws IllegalModelException {
        List<Message> pifMessages = mout.getModel().getMessages(); // initializes the list of messages
        for (org.eclipse.bpmn2.Message bpmnMessage : min.getMessages()) {
            Message pifMessage = new Message();
            String pifMessageId = getNameOrIdForMessage(bpmnMessage);
            String bpmnMessageId = bpmnMessage.getId();
            if (bpmnMessageId == null) {
                IllegalModelException e = new IllegalModelException("BPMN model is incorrect (a message has no id)");
                error(e.getMessage());
                throw e;
            }
            pifMessage.setId(pifMessageId);
            messages.put(bpmnMessageId, pifMessageId);
            pifMessages.add(pifMessage);
        }
    }

    /**
     * Gets the name of a participant or its id if the name is undefined
     *
     * @param p participant
     * @return name of the participant (if defined) or id (if not)
     * @throws IllegalModelException if neither the name nor the id is defined
     */
    private String getNameOrIdForParticipant(Participant p) throws IllegalModelException {
        String rtr;
        if (p.getName() == null) {
            warning(String.format("BPMN model is incorrect (a %s without name)", p.eClass().getName()));
            if (p.getId() == null) {
                IllegalModelException e = new IllegalModelException(String.format("BPMN model is incorrect (a %s without id)", p.eClass().getName()));
                error(e.getMessage());
                throw e;
            } else {
                rtr = p.getId();
            }
        } else {
            rtr = p.getName();
        }
        return rtr;
    }

    /**
     * Gets the name of a message or its id if the name is undefined
     *
     * @param m message
     * @return name of the message (if defined) or id (if not)
     * @throws IllegalModelException if neither the name nor the id is defined
     */
    private String getNameOrIdForMessage(org.eclipse.bpmn2.Message m) throws IllegalModelException {
        String rtr;
        if (m.getName() == null) {
            warning(String.format("BPMN model is incorrect (a %s without name)", m.eClass().getName()));
            if (m.getId() == null) {
                IllegalModelException e = new IllegalModelException(String.format("BPMN model is incorrect (a %s without id)", m.eClass().getName()));
                error(e.getMessage());
                throw e;
            } else {
                rtr = m.getId();
            }
        } else {
            rtr = m.getName();
        }
        return rtr;
    }

    /**
     * Sets up the behavioral part of the PIF model
     * this is achieved in two steps: 1- nodes, 2- edges
     *
     * @param min  BPMN model source of the transformation
     * @param mout PIF model target of the transformation
     * @throws IllegalModelException
     */
    private void setBehavior(BpmnModel min, PifModel mout) throws IllegalModelException {
        setBehaviorNodes(min, mout);
        setBehaviorEdges(min, mout);
    }

    /**
     * Sets up the behavioral part of the PIF model (nodes)
     * TODO EXTENSION USE A DESIGN PATTERN
     *
     * @param min  BPMN model source of the transformation
     * @param mout PIF model target of the transformation
     * @throws IllegalModelException
     */
    private void setBehaviorNodes(BpmnModel min, PifModel mout) throws IllegalModelException {
        List<FlowElement> flowElements = min.getFlowElements();
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof StartEvent) {
                transform_StartEvent(min, mout, (StartEvent) flowElement);
            } else if (flowElement instanceof EndEvent) {
                transform_EndEvent(min, mout, (EndEvent) flowElement);
            } else if (flowElement instanceof Gateway) {
                transform_Gateway(min, mout, (Gateway) flowElement);
            } else if (flowElement instanceof ReceiveTask) { // should be BEFORE the test on Task since ReceiveTask is a subclass of it
                transform_Receive(min, mout, (ReceiveTask) flowElement);
            } else if (flowElement instanceof SendTask) { // should be BEFORE the test on Task since SendTask is a subclass of it
                transform_Send(min, mout, (SendTask) flowElement);
            } else if (flowElement instanceof Task) {
                transform_Task(min, mout, (Task) flowElement);
            } else if (flowElement instanceof ChoreographyTask) {
                transform_ChoreographyTask(min, mout, (ChoreographyTask) flowElement);
            } else if (!(flowElement instanceof SequenceFlow)) {
                throw new IllegalModelException(String.format("Unsupported BPMN flow element: %s", flowElement));
            }
        }
        if (!has_initial_state) {
            IllegalModelException e = new IllegalModelException("BPMN model is incorrect (no start event)");
            error(e.getMessage());
            throw e;
        }
        if (!has_final_state) {
            IllegalModelException e = new IllegalModelException("BPMN model is incorrect (no end event)");
            error(e.getMessage());
            throw e;
        }
    }

    /**
     * Sets up the behavioral part of the PIF model (nodes)
     *
     * @param min  BPMN model source of the transformation
     * @param mout PIF model target of the transformation
     * @throws IllegalModelException
     */
    private void setBehaviorEdges(BpmnModel min, PifModel mout) throws IllegalModelException {
    }

    /**
     * Transforms a BPMN ChoreographyTask into a PIF Interaction
     *
     * @param min         BPMN model
     * @param mout        PIF model
     * @param flowElement BPMN ChoreographyTask to transform
     * @throws IllegalModelException
     */
    private void transform_ChoreographyTask(BpmnModel min, PifModel mout, ChoreographyTask flowElement) throws IllegalModelException {
        checkInStrict(flowElement, 1, true);
        checkOutStrict(flowElement, 1, true);
        models.process.pif.generated.Interaction task = new models.process.pif.generated.Interaction();
        task.setId(flowElement.getId());
        String initiatingPeerId = flowElement.getId();
        task.setInitiatingPeer(participants.get(trans_participants.get(initiatingPeerId)));
        List<Peer> receivingPeers = task.getReceivingPeers();
        receivingPeers = new ArrayList<Peer>();
        for (Participant participant : flowElement.getParticipantRefs()) {
            receivingPeers.add(participants.get(trans_participants.get(participant.getId())));
        }
        mout.addNode(task);
    }

    private void transform_Receive(BpmnModel min, PifModel mout, ReceiveTask flowElement) {
        // TODO:
    }

    private void transform_Send(BpmnModel min, PifModel mout, SendTask flowElement) {
        // TODO:
    }

    /**
     * Transforms a BPMN Task into a PIF Task
     *
     * @param min         BPMN model
     * @param mout        PIF model
     * @param flowElement BPMN Task to transform
     * @throws IllegalModelException
     */
    private void transform_Task(BpmnModel min, PifModel mout, Task flowElement) throws IllegalModelException {
        checkInStrict(flowElement, 1, true);
        checkOutStrict(flowElement, 1, true);
        models.process.pif.generated.Task task = new models.process.pif.generated.Task();
        task.setId(flowElement.getId());
        mout.addNode(task);
    }

    private void transform_Gateway(BpmnModel min, PifModel mout, Gateway flowElement) {
        // TODO:
    }

    /**
     * Transforms a BPMN EndEvent into a PIF EndEvent
     *
     * @param min       BPMN model
     * @param mout      PIF model
     * @param bpmnEvent BPMN EndEvent to transform
     * @throws IllegalModelException
     */
    private void transform_EndEvent(BpmnModel min, PifModel mout, EndEvent bpmnEvent) throws IllegalModelException {
        checkInStrict(bpmnEvent, 1, true);
        checkOutStrict(bpmnEvent, 0, true);
        //
        if (has_final_state) {
            this.warning("BPMN model has more than one end event");
        } else {
            has_final_state = true;
        }
        //
        models.process.pif.generated.EndEvent pifEvent = new models.process.pif.generated.EndEvent();
        pifEvent.setId(getAndCheckId(bpmnEvent));
        mout.addNode(pifEvent);
        mout.addFinalNode(pifEvent);
    }

    /**
     * Transforms a BPMN StartEvent into a PIF InitialEvent
     *
     * @param min       BPMN model
     * @param mout      PIF model
     * @param bpmnEvent BPMN StartEvent to transform
     * @throws IllegalModelException
     */
    private void transform_StartEvent(BpmnModel min, PifModel mout, StartEvent bpmnEvent) throws IllegalModelException {
        checkInStrict(bpmnEvent, 0, true);
        checkOutStrict(bpmnEvent, 1, true);
        //
        if (has_initial_state) {
            IllegalModelException e = new IllegalModelException("BPMN model is incorrect (more than one start event)");
            error(e.getMessage());
            throw e;
        } else {
            has_initial_state = true;
        }
        //
        InitialEvent pifEvent = new InitialEvent();
        pifEvent.setId(getAndCheckId(bpmnEvent));
        mout.addNode(pifEvent);
        mout.setInitialNode(pifEvent);
    }

    /**
     * Checks if a node has exactly a given number of incoming flows.
     *
     * @param flowNode the node to check
     * @param count    the expected number of incoming flows
     * @param warn     whether a warning should be issued or not
     * @return true if the the number of incoming flows of flowNode is count, else false
     */
    private boolean checkInStrict(FlowNode flowNode, int count, boolean warn) {
        boolean rtr = true;
        if (flowNode.getIncoming().size() != count) {
            rtr = false;
            if (warn) {
                warning("BPMN model is incorrect (" + flowNode + " should have only " + count + " predecessor(s))");
            }
        }
        return rtr;
    }

    /**
     * Checks if a node has exactly a given number of outgoing flows.
     *
     * @param flowNode the node to check
     * @param count    the expected number of outgoing flows
     * @param warn     whether a warning should be issued or not
     * @return true if the the number of outgoing flows of flowNode is count, else false
     */
    private boolean checkOutStrict(FlowNode flowNode, int count, boolean warn) {
        boolean rtr = true;
        if (flowNode.getOutgoing().size() != count) {
            rtr = false;
            if (warn) {
                warning("BPMN model is incorrect (" + flowNode + " should have only " + count + " sucessor(s))");
            }
        }
        return rtr;
    }

    /**
     * Checks if a node has strictly more than a given number of incoming flows.
     *
     * @param flowNode the node to check
     * @param count    the expected number of incoming flows
     * @param warn     whether a warning should be issued or not
     * @return true if the the number of incoming flows of flowNode is strictly more than count, else false
     */
    private boolean checkInMore(FlowNode flowNode, int count, boolean warn) {
        boolean rtr = true;
        if (flowNode.getIncoming().size() <= count) {
            rtr = false;
            if (warn) {
                warning("BPMN model is incorrect (" + flowNode + " should have more than " + count + " predecessor(s))");
            }
        }
        return rtr;
    }

    /**
     * Checks if a node has strictly more than a given number of outgoing flows.
     *
     * @param flowNode the node to check
     * @param count    the expected number of outgoing flows
     * @param warn     whether a warning should be issued or not
     * @return true if the the number of outgoing flows of flowNode is strictly more than count, else false
     */
    private boolean checkOutMore(FlowNode flowNode, int count, boolean warn) {
        boolean rtr = true;
        if (flowNode.getOutgoing().size() <= count) {
            rtr = false;
            if (warn) {
                warning("BPMN model is incorrect (" + flowNode + " should have more than " + count + " sucessor(s))");
            }
        }
        return rtr;
    }

    /**
     * Gets the id from a BPMN element and checks if defined
     *
     * @param bpmnElement element to get the id from
     * @return the id of the element if defined
     * @throws IllegalModelException
     */
    public String getAndCheckId(BaseElement bpmnElement) throws IllegalModelException {
        String id = bpmnElement.getId();
        if (id == null) {
            IllegalModelException e = new IllegalModelException(String.format("BPMN model is incorrect (%s without id)", bpmnElement.getClass().toString()));
            error(e.getMessage());
            throw e;
        }
        return id;
    }

    /**
     * Gets the peer from a list given its id
     * required since the generated code from the XSD file is a list and not a map
     *
     * @param peers list of the peers
     * @param id    id of the peer to find
     * @return the peer (if found) or null else
     */
    public Peer getPeerFromId(List<Peer> peers, String id) {
        Optional<Peer> peer = peers.parallelStream().filter(x -> x.getId() == id).findAny();
        if (peer.isPresent()) {
            return peer.get();
        } else {
            return null;
        }
    }

    @Override
    public void about() {
        System.out.println(NAME + " " + VERSION);
    }
}
