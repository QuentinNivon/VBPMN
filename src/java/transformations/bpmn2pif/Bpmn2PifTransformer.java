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

import com.sun.xml.internal.rngom.parse.host.Base;
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

 * TODO: more comments
 * TODO: use a visitor for the transformation
 * TODO: replace cascading method calls by delegation
 * TODO: modify the PIF generation to get correct namespaces (PifPifWriter)
 * TODO: support more than one message in choreography tasks (requires change in the PIF format)
 * TODO: support case with 2 tasks having the same name but different ids (requires change in the PIF format to deal with id+name)
 * TODO: we have only IDs in PIF (no NAMEs) and due to ID/IDREF rules, we cannot get a PIF element ID from a BPMN element Name (e.g., in case of whitespace)
 */
public class Bpmn2PifTransformer extends AbstractTransformer {

    public static final String NAME = "bpmn2pif";
    public static final String VERSION = "1.0";

    ObjectFactory factory;
    Map<String, String> trans_participants; // used to keep track of BPMN:id -> PIF:id for participants (peers)
    Map<String, String> trans_messages;     // used to keep track of BPMN:id -> PIF:id for messages
    // no need for a BPMN:id -> PIF:id for nodes since we use the id in both case (for the time being, maybe this could be changed later on for traceability purposes)
    boolean has_initial_state;              // start event has been found
    boolean has_final_state;                // end event has been found

    public Bpmn2PifTransformer() {
        super();
        factory = new ObjectFactory();
        trans_participants = new HashMap<>();
        trans_messages = new HashMap<>();
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
     *
     * @param min  BPMN model where the documentation is defined
     * @param mout PIF model whee the documentation is to be defined
     */
    private void setDocumentation(BpmnModel min, PifModel mout) {
        List<Documentation> documentations = min.getDocumentation();
        String documentation = documentations.stream().map(x -> x.getText()).collect(Collectors.joining());
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
        for (Participant bpmnPeer : min.getParticipants()) {
            Peer pifPeer = new Peer();
            String pifPeerId = getAndCheckId(bpmnPeer);
            String bpmnPeerId = bpmnPeer.getId();
            if (bpmnPeerId == null) {
                IllegalModelException e = new IllegalModelException("BPMN model is incorrect (a participant has no id)");
                error(e.getMessage());
                throw e;
            }
            pifPeer.setId(pifPeerId);
            trans_participants.put(bpmnPeerId, pifPeerId);
            mout.addPeer(pifPeer);
        }
    }

    /**
     * Sets the messages of the process. pif::Message.id is bpmn::Message.name or bpmn::Message.id if undefined
     *
     * @param min  BPMN model where the messages are defined
     * @param mout PIF model where the messages are to be defined
     * @throws IllegalModelException
     */
    private void setMessages(BpmnModel min, PifModel mout) throws IllegalModelException {
        for (org.eclipse.bpmn2.Message bpmnMessage : min.getMessages()) {
            Message pifMessage = new Message();
            String pifMessageId = getAndCheckId(bpmnMessage);
            String bpmnMessageId = bpmnMessage.getId();
            if (bpmnMessageId == null) {
                IllegalModelException e = new IllegalModelException("BPMN model is incorrect (a message has no id)");
                error(e.getMessage());
                throw e;
            }
            pifMessage.setId(pifMessageId);
            trans_messages.put(bpmnMessageId, pifMessageId);
            mout.addMessage(pifMessage);
        }
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
     *
     * @param min  BPMN model source of the transformation
     * @param mout PIF model target of the transformation
     * @throws IllegalModelException
     */
    private void setBehaviorNodes(BpmnModel min, PifModel mout) throws IllegalModelException {
        for (FlowElement flowElement : min.getFlowElements()) {
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
        try {
            min.getFlowElements().stream().filter(flowElement -> flowElement instanceof SequenceFlow).forEach(flowElement -> {
                SequenceFlow bpmnSequenceFlow = (SequenceFlow) flowElement;
                String bpmnSourceNodeId = bpmnSequenceFlow.getSourceRef().getId();
                String bpmnTargetNodeId = bpmnSequenceFlow.getTargetRef().getId();
                if (!mout.hasNode(bpmnSourceNodeId) || !mout.hasNode(bpmnTargetNodeId)) {
                    throw new RuntimeException(); // trick due to Java 8 management of Exceptions in stream commands
                }
                WorkflowNode pifSourceNode = mout.getNode(bpmnSourceNodeId);
                WorkflowNode pifTargetNode = mout.getNode(bpmnTargetNodeId);
                models.process.pif.generated.SequenceFlow pifSequenceFlow = new models.process.pif.generated.SequenceFlow();
                pifSequenceFlow.setId(bpmnSequenceFlow.getId());
                // links peers <-> sequence flow
                pifSequenceFlow.setSource(pifSourceNode);
                pifSequenceFlow.setTarget(pifTargetNode);
                pifSourceNode.getOutgoingFlows().add(factory.createWorkflowNodeOutgoingFlows(pifSequenceFlow));
                pifTargetNode.getIncomingFlows().add(factory.createWorkflowNodeIncomingFlows(pifSequenceFlow));
                // adding in the output model
                mout.getModel().getBehaviour().getSequenceFlows().add(pifSequenceFlow); // TODO clean this
            });
        } catch (RuntimeException e) {
            IllegalModelException e2 = new IllegalModelException("BPMN model is incorrect (unknown node reference in sequence flow)");
            error(e2.getMessage());
            throw e2;
        }
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
        // check if we have messages, if not generate one
        if (flowElement.getMessageFlowRef().size() != 0) {
            if (flowElement.getMessageFlowRef().size() != 1) {
                warning(String.format("More than one message for choreography task %s, only one will be used", flowElement.getId()));
            }
            task.setMessage(mout.getMessage(trans_messages.get(flowElement.getMessageFlowRef().get(0).getMessageRef().getId())));
        } else {
            Message m = new Message();
            String id = "Message_" + getAndCheckId(flowElement);
            if (mout.hasMessage(id)) {
                throw new IllegalModelException(String.format("Impossible to generate a new message (id) for choreography task %s", flowElement.getId()));
            }
            m.setId(id);
            trans_messages.put(m.getId(), m.getId());
            mout.addMessage(m);
            task.setMessage(m);
        }
        // deal with the initiating and receiving peers
        String bpmnInitiatingPeerId = flowElement.getInitiatingParticipantRef().getId();
        task.setInitiatingPeer(mout.getPeer(trans_participants.get(bpmnInitiatingPeerId)));
        for (Participant bpmnParticipant : flowElement.getParticipantRefs()) {
            String bpmnParticipantId = bpmnParticipant.getId();
            if (!(bpmnInitiatingPeerId.equals(bpmnParticipantId))) {
                task.getReceivingPeers().add(factory.createInteractionReceivingPeers(mout.getPeer(trans_participants.get(bpmnParticipantId))));
            }
        }
        // add node
        mout.addNode(task);
    }

    /**
     * Transforms a BPMN ReceiveTask into a PIF MessageReception
     *
     * @param min         BPMN model
     * @param mout        PIF model
     * @param flowElement BPMN ReceiveTask to transform
     * @throws IllegalModelException
     */
    private void transform_Receive(BpmnModel min, PifModel mout, ReceiveTask flowElement) throws IllegalModelException {
        checkInStrict(flowElement, 1, true);
        checkOutStrict(flowElement, 1, true);
        models.process.pif.generated.MessageReception task = new models.process.pif.generated.MessageReception();
        task.setId(flowElement.getId());
        // check if we have messages, if not generate one
        if (flowElement.getMessageRef() != null) {
            task.setMessage(mout.getMessage(trans_messages.get(flowElement.getMessageRef().getId())));
        } else {
            Message m = new Message();
            String id = "Message_" + task.getId();
            if (mout.hasMessage(id)) {
                throw new IllegalModelException(String.format("Impossible to generate a new message (id) for choreography task %s", flowElement.getId()));
            }
            m.setId(id);
            trans_messages.put(m.getId(), m.getId());
            mout.addMessage(m);
            task.setMessage(m);
        }
        // add node
        mout.addNode(task);
    }

    /**
     * Transforms a BPMN SendTask into a PIF MessageSending
     *
     * @param min         BPMN model
     * @param mout        PIF model
     * @param flowElement BPMN SendTask to transform
     * @throws IllegalModelException
     */
    private void transform_Send(BpmnModel min, PifModel mout, SendTask flowElement) throws IllegalModelException {
        checkInStrict(flowElement, 1, true);
        checkOutStrict(flowElement, 1, true);
        models.process.pif.generated.MessageSending task = new models.process.pif.generated.MessageSending();
        task.setId(flowElement.getId());
        // check if we have messages, if not generate one
        if (flowElement.getMessageRef() != null) {
            task.setMessage(mout.getMessage(trans_messages.get(flowElement.getMessageRef().getId())));
        } else {
            Message m = new Message();
            String id = "Message_" + task.getId();
            if (mout.hasMessage(id)) {
                throw new IllegalModelException(String.format("Impossible to generate a new message (id) for choreography task %s", flowElement.getId()));
            }
            m.setId(id);
            trans_messages.put(m.getId(), m.getId());
            mout.addMessage(m);
            task.setMessage(m);
        }
        // add node
        mout.addNode(task);
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
        task.setId(getAndCheckId(flowElement));
        mout.addNode(task);
    }

    /**
     * Transforms a BPMN Gateway into a PIF Gateway
     *
     * @param min         BPMN model
     * @param mout        PIF model
     * @param flowElement BPMN Gateway to transform
     * @throws IllegalModelException
     */
    private void transform_Gateway(BpmnModel min, PifModel mout, Gateway flowElement) throws IllegalModelException {
        GatewayDirection direction = getDirection(flowElement);
        if (direction.getValue() == GatewayDirection.CONVERGING_VALUE) {
            transform_JoinGateway(min, mout, flowElement);
        } else if (direction.getValue() == GatewayDirection.DIVERGING_VALUE) {
            transform_SplitGateway(min, mout, flowElement);
        } else {
            IllegalModelException e = new IllegalModelException("BPMN model incorrect (1-1, mixed or unknown type gateway " + flowElement.getId() + " not supported)");
            error(e.getMessage());
            throw e;
        }
    }

    /**
     * Transforms a BPMN Join Gateway into a PIF Join Gateway
     *
     * @param min         BPMN model
     * @param mout        PIF model
     * @param flowElement BPMN Gateway to transform
     * @throws IllegalModelException
     */
    private void transform_JoinGateway(BpmnModel min, PifModel mout, Gateway flowElement) throws IllegalModelException {
        JoinGateway gw;
        if (flowElement instanceof ParallelGateway) {
            gw = new AndJoinGateway();
        } else if (flowElement instanceof  ExclusiveGateway) {
            gw = new XOrJoinGateway();
        } else if (flowElement instanceof  InclusiveGateway) {
            gw = new OrJoinGateway();
        } else {
            IllegalModelException e = new IllegalModelException("BPMN model incorrect (gateway " + flowElement.getId() + " type is not supported)");
            error(e.getMessage());
            throw e;
        }
        gw.setId(flowElement.getId());
        mout.addNode(gw);
    }

    /**
     * Transforms a BPMN Split Gateway into a PIF Split Gateway
     *
     * @param min         BPMN model
     * @param mout        PIF model
     * @param flowElement BPMN Gateway to transform
     * @throws IllegalModelException
     */
    private void transform_SplitGateway(BpmnModel min, PifModel mout, Gateway flowElement) throws IllegalModelException {
        SplitGateway gw;
        if (flowElement instanceof ParallelGateway) {
            gw = new AndSplitGateway();
        } else if (flowElement instanceof  ExclusiveGateway) {
            gw = new XOrSplitGateway();
        } else if (flowElement instanceof  InclusiveGateway) {
            gw = new OrSplitGateway();
        } else {
            IllegalModelException e = new IllegalModelException("BPMN model incorrect (gateway " + flowElement.getId() + " type is not supported)");
            error(e.getMessage());
            throw e;
        }
        gw.setId(flowElement.getId());
        mout.addNode(gw);
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
     * Gets the direction (split or join) of a gateway and check it is consistent wrt. the sequence flow
     *
     * @param gateway the gateway to check and get the direction for
     * @return the direction of the gateway
     */
    private GatewayDirection getDirection(Gateway gateway) {
        GatewayDirection direction;
        int given_dir;
        given_dir = gateway.getGatewayDirection().getValue();
        if (this.checkOutStrict(gateway, 1, false) && (this.checkInMore(gateway, 1, false))) {
            direction = GatewayDirection.CONVERGING;
            if (given_dir != direction.getValue())
                warning("BPMN model is incorrect (gateway " + gateway.getId() + " should be defined to be converging)");
        } else if (this.checkInStrict(gateway, 1, false) && (this.checkOutMore(gateway, 1, false))) {
            direction = org.eclipse.bpmn2.GatewayDirection.DIVERGING;
            if (given_dir != direction.getValue())
                this.warning("BPMN model is incorrect (gateway " + gateway.getId() + " should be defined to be diverging)");
        } else // 1-1 (no-op gateway) or n-m (unsupported)
            direction = org.eclipse.bpmn2.GatewayDirection.UNSPECIFIED;
        return direction;
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
