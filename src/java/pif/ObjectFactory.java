
package pif;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the pif package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Communication_QNAME = new QName("http://www.example.org/PIF", "Communication");
    private final static QName _XOrJoinGateway_QNAME = new QName("http://www.example.org/PIF", "XOrJoinGateway");
    private final static QName _MessageCommunication_QNAME = new QName("http://www.example.org/PIF", "MessageCommunication");
    private final static QName _EndEvent_QNAME = new QName("http://www.example.org/PIF", "EndEvent");
    private final static QName _AndJoinGateway_QNAME = new QName("http://www.example.org/PIF", "AndJoinGateway");
    private final static QName _Gateway_QNAME = new QName("http://www.example.org/PIF", "Gateway");
    private final static QName _OrSplitGateway_QNAME = new QName("http://www.example.org/PIF", "OrSplitGateway");
    private final static QName _OrJoinGateway_QNAME = new QName("http://www.example.org/PIF", "OrJoinGateway");
    private final static QName _WorkflowNode_QNAME = new QName("http://www.example.org/PIF", "WorkflowNode");
    private final static QName _MessageSending_QNAME = new QName("http://www.example.org/PIF", "MessageSending");
    private final static QName _Process_QNAME = new QName("http://www.example.org/PIF", "Process");
    private final static QName _Workflow_QNAME = new QName("http://www.example.org/PIF", "Workflow");
    private final static QName _MessageReception_QNAME = new QName("http://www.example.org/PIF", "MessageReception");
    private final static QName _Peer_QNAME = new QName("http://www.example.org/PIF", "Peer");
    private final static QName _SequenceFlow_QNAME = new QName("http://www.example.org/PIF", "SequenceFlow");
    private final static QName _SplitGateway_QNAME = new QName("http://www.example.org/PIF", "SplitGateway");
    private final static QName _Message_QNAME = new QName("http://www.example.org/PIF", "Message");
    private final static QName _ConditionalSequenceFlow_QNAME = new QName("http://www.example.org/PIF", "ConditionalSequenceFlow");
    private final static QName _InitialEvent_QNAME = new QName("http://www.example.org/PIF", "InitialEvent");
    private final static QName _JoinGateway_QNAME = new QName("http://www.example.org/PIF", "JoinGateway");
    private final static QName _Task_QNAME = new QName("http://www.example.org/PIF", "Task");
    private final static QName _AndSplitGateway_QNAME = new QName("http://www.example.org/PIF", "AndSplitGateway");
    private final static QName _XOrSplitGateway_QNAME = new QName("http://www.example.org/PIF", "XOrSplitGateway");
    private final static QName _Condition_QNAME = new QName("http://www.example.org/PIF", "Condition");
    private final static QName _InteractionReceivingPeers_QNAME = new QName("http://www.example.org/PIF", "receivingPeers");
    private final static QName _WorkflowFinalNodes_QNAME = new QName("http://www.example.org/PIF", "finalNodes");
    private final static QName _WorkflowNodeOutgoingFlows_QNAME = new QName("http://www.example.org/PIF", "outgoingFlows");
    private final static QName _WorkflowNodeIncomingFlows_QNAME = new QName("http://www.example.org/PIF", "incomingFlows");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: pif
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link XOrSplitGateway }
     * 
     */
    public XOrSplitGateway createXOrSplitGateway() {
        return new XOrSplitGateway();
    }

    /**
     * Create an instance of {@link Condition }
     * 
     */
    public Condition createCondition() {
        return new Condition();
    }

    /**
     * Create an instance of {@link AndSplitGateway }
     * 
     */
    public AndSplitGateway createAndSplitGateway() {
        return new AndSplitGateway();
    }

    /**
     * Create an instance of {@link Task }
     * 
     */
    public Task createTask() {
        return new Task();
    }

    /**
     * Create an instance of {@link InitialEvent }
     * 
     */
    public InitialEvent createInitialEvent() {
        return new InitialEvent();
    }

    /**
     * Create an instance of {@link Message }
     * 
     */
    public Message createMessage() {
        return new Message();
    }

    /**
     * Create an instance of {@link ConditionalSequenceFlow }
     * 
     */
    public ConditionalSequenceFlow createConditionalSequenceFlow() {
        return new ConditionalSequenceFlow();
    }

    /**
     * Create an instance of {@link SequenceFlow }
     * 
     */
    public SequenceFlow createSequenceFlow() {
        return new SequenceFlow();
    }

    /**
     * Create an instance of {@link Peer }
     * 
     */
    public Peer createPeer() {
        return new Peer();
    }

    /**
     * Create an instance of {@link MessageReception }
     * 
     */
    public MessageReception createMessageReception() {
        return new MessageReception();
    }

    /**
     * Create an instance of {@link Workflow }
     * 
     */
    public Workflow createWorkflow() {
        return new Workflow();
    }

    /**
     * Create an instance of {@link Process }
     * 
     */
    public Process createProcess() {
        return new Process();
    }

    /**
     * Create an instance of {@link MessageSending }
     * 
     */
    public MessageSending createMessageSending() {
        return new MessageSending();
    }

    /**
     * Create an instance of {@link OrJoinGateway }
     * 
     */
    public OrJoinGateway createOrJoinGateway() {
        return new OrJoinGateway();
    }

    /**
     * Create an instance of {@link OrSplitGateway }
     * 
     */
    public OrSplitGateway createOrSplitGateway() {
        return new OrSplitGateway();
    }

    /**
     * Create an instance of {@link EndEvent }
     * 
     */
    public EndEvent createEndEvent() {
        return new EndEvent();
    }

    /**
     * Create an instance of {@link AndJoinGateway }
     * 
     */
    public AndJoinGateway createAndJoinGateway() {
        return new AndJoinGateway();
    }

    /**
     * Create an instance of {@link XOrJoinGateway }
     * 
     */
    public XOrJoinGateway createXOrJoinGateway() {
        return new XOrJoinGateway();
    }

    /**
     * Create an instance of {@link Interaction }
     * 
     */
    public Interaction createInteraction() {
        return new Interaction();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Communication }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "Communication")
    public JAXBElement<Communication> createCommunication(Communication value) {
        return new JAXBElement<Communication>(_Communication_QNAME, Communication.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XOrJoinGateway }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "XOrJoinGateway")
    public JAXBElement<XOrJoinGateway> createXOrJoinGateway(XOrJoinGateway value) {
        return new JAXBElement<XOrJoinGateway>(_XOrJoinGateway_QNAME, XOrJoinGateway.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MessageCommunication }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "MessageCommunication")
    public JAXBElement<MessageCommunication> createMessageCommunication(MessageCommunication value) {
        return new JAXBElement<MessageCommunication>(_MessageCommunication_QNAME, MessageCommunication.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EndEvent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "EndEvent")
    public JAXBElement<EndEvent> createEndEvent(EndEvent value) {
        return new JAXBElement<EndEvent>(_EndEvent_QNAME, EndEvent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AndJoinGateway }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "AndJoinGateway")
    public JAXBElement<AndJoinGateway> createAndJoinGateway(AndJoinGateway value) {
        return new JAXBElement<AndJoinGateway>(_AndJoinGateway_QNAME, AndJoinGateway.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Gateway }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "Gateway")
    public JAXBElement<Gateway> createGateway(Gateway value) {
        return new JAXBElement<Gateway>(_Gateway_QNAME, Gateway.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OrSplitGateway }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "OrSplitGateway")
    public JAXBElement<OrSplitGateway> createOrSplitGateway(OrSplitGateway value) {
        return new JAXBElement<OrSplitGateway>(_OrSplitGateway_QNAME, OrSplitGateway.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OrJoinGateway }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "OrJoinGateway")
    public JAXBElement<OrJoinGateway> createOrJoinGateway(OrJoinGateway value) {
        return new JAXBElement<OrJoinGateway>(_OrJoinGateway_QNAME, OrJoinGateway.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WorkflowNode }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "WorkflowNode")
    public JAXBElement<WorkflowNode> createWorkflowNode(WorkflowNode value) {
        return new JAXBElement<WorkflowNode>(_WorkflowNode_QNAME, WorkflowNode.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MessageSending }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "MessageSending")
    public JAXBElement<MessageSending> createMessageSending(MessageSending value) {
        return new JAXBElement<MessageSending>(_MessageSending_QNAME, MessageSending.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Process }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "Process")
    public JAXBElement<Process> createProcess(Process value) {
        return new JAXBElement<Process>(_Process_QNAME, Process.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Workflow }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "Workflow")
    public JAXBElement<Workflow> createWorkflow(Workflow value) {
        return new JAXBElement<Workflow>(_Workflow_QNAME, Workflow.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MessageReception }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "MessageReception")
    public JAXBElement<MessageReception> createMessageReception(MessageReception value) {
        return new JAXBElement<MessageReception>(_MessageReception_QNAME, MessageReception.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Peer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "Peer")
    public JAXBElement<Peer> createPeer(Peer value) {
        return new JAXBElement<Peer>(_Peer_QNAME, Peer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SequenceFlow }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "SequenceFlow")
    public JAXBElement<SequenceFlow> createSequenceFlow(SequenceFlow value) {
        return new JAXBElement<SequenceFlow>(_SequenceFlow_QNAME, SequenceFlow.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SplitGateway }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "SplitGateway")
    public JAXBElement<SplitGateway> createSplitGateway(SplitGateway value) {
        return new JAXBElement<SplitGateway>(_SplitGateway_QNAME, SplitGateway.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Message }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "Message")
    public JAXBElement<Message> createMessage(Message value) {
        return new JAXBElement<Message>(_Message_QNAME, Message.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConditionalSequenceFlow }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "ConditionalSequenceFlow")
    public JAXBElement<ConditionalSequenceFlow> createConditionalSequenceFlow(ConditionalSequenceFlow value) {
        return new JAXBElement<ConditionalSequenceFlow>(_ConditionalSequenceFlow_QNAME, ConditionalSequenceFlow.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InitialEvent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "InitialEvent")
    public JAXBElement<InitialEvent> createInitialEvent(InitialEvent value) {
        return new JAXBElement<InitialEvent>(_InitialEvent_QNAME, InitialEvent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JoinGateway }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "JoinGateway")
    public JAXBElement<JoinGateway> createJoinGateway(JoinGateway value) {
        return new JAXBElement<JoinGateway>(_JoinGateway_QNAME, JoinGateway.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Task }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "Task")
    public JAXBElement<Task> createTask(Task value) {
        return new JAXBElement<Task>(_Task_QNAME, Task.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AndSplitGateway }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "AndSplitGateway")
    public JAXBElement<AndSplitGateway> createAndSplitGateway(AndSplitGateway value) {
        return new JAXBElement<AndSplitGateway>(_AndSplitGateway_QNAME, AndSplitGateway.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XOrSplitGateway }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "XOrSplitGateway")
    public JAXBElement<XOrSplitGateway> createXOrSplitGateway(XOrSplitGateway value) {
        return new JAXBElement<XOrSplitGateway>(_XOrSplitGateway_QNAME, XOrSplitGateway.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Condition }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "Condition")
    public JAXBElement<Condition> createCondition(Condition value) {
        return new JAXBElement<Condition>(_Condition_QNAME, Condition.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "receivingPeers", scope = Interaction.class)
    @XmlIDREF
    public JAXBElement<Object> createInteractionReceivingPeers(Object value) {
        return new JAXBElement<Object>(_InteractionReceivingPeers_QNAME, Object.class, Interaction.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "finalNodes", scope = Workflow.class)
    @XmlIDREF
    public JAXBElement<Object> createWorkflowFinalNodes(Object value) {
        return new JAXBElement<Object>(_WorkflowFinalNodes_QNAME, Object.class, Workflow.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "outgoingFlows", scope = WorkflowNode.class)
    @XmlIDREF
    public JAXBElement<Object> createWorkflowNodeOutgoingFlows(Object value) {
        return new JAXBElement<Object>(_WorkflowNodeOutgoingFlows_QNAME, Object.class, WorkflowNode.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "incomingFlows", scope = WorkflowNode.class)
    @XmlIDREF
    public JAXBElement<Object> createWorkflowNodeIncomingFlows(Object value) {
        return new JAXBElement<Object>(_WorkflowNodeIncomingFlows_QNAME, Object.class, WorkflowNode.class, value);
    }

}
