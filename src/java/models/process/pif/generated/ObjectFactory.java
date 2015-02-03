
package models.process.pif.generated;

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

    private final static QName _InteractionReceivingPeers_QNAME = new QName("http://www.example.org/PIF", "receivingPeers");
    private final static QName _WorkflowNodeOutgoingFlows_QNAME = new QName("http://www.example.org/PIF", "outgoingFlows");
    private final static QName _WorkflowNodeIncomingFlows_QNAME = new QName("http://www.example.org/PIF", "incomingFlows");
    private final static QName _WorkflowFinalNodes_QNAME = new QName("http://www.example.org/PIF", "finalNodes");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: pif
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Process }
     * 
     */
    public Process createProcess() {
        return new Process();
    }

    /**
     * Create an instance of {@link Peer }
     * 
     */
    public Peer createPeer() {
        return new Peer();
    }

    /**
     * Create an instance of {@link Message }
     * 
     */
    public Message createMessage() {
        return new Message();
    }

    /**
     * Create an instance of {@link Workflow }
     * 
     */
    public Workflow createWorkflow() {
        return new Workflow();
    }

    /**
     * Create an instance of {@link models.process.pif.generated.Condition }
     * 
     */
    public Condition createCondition() {
        return new Condition();
    }

    /**
     * Create an instance of {@link XOrSplitGateway }
     * 
     */
    public XOrSplitGateway createXOrSplitGateway() {
        return new XOrSplitGateway();
    }

    /**
     * Create an instance of {@link models.process.pif.generated.AndSplitGateway }
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
     * Create an instance of {@link models.process.pif.generated.ConditionalSequenceFlow }
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
     * Create an instance of {@link Interaction }
     * 
     */
    public Interaction createInteraction() {
        return new Interaction();
    }

    /**
     * Create an instance of {@link MessageReception }
     * 
     */
    public MessageReception createMessageReception() {
        return new MessageReception();
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
     * Create an instance of {@link models.process.pif.generated.EndEvent }
     * 
     */
    public EndEvent createEndEvent() {
        return new EndEvent();
    }

    /**
     * Create an instance of {@link models.process.pif.generated.AndJoinGateway }
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

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.example.org/PIF", name = "finalNodes", scope = Workflow.class)
    @XmlIDREF
    public JAXBElement<Object> createWorkflowFinalNodes(Object value) {
        return new JAXBElement<Object>(_WorkflowFinalNodes_QNAME, Object.class, Workflow.class, value);
    }

}
