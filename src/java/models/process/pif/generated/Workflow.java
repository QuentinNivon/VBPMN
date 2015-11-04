//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.11.04 à 06:19:30 PM CET 
//


package models.process.pif.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour Workflow complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="Workflow">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="nodes" type="{http://www.example.org/PIF}WorkflowNode" maxOccurs="unbounded"/>
 *         &lt;element name="sequenceFlows" type="{http://www.example.org/PIF}SequenceFlow" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="initialNode" type="{http://www.w3.org/2001/XMLSchema}IDREF"/>
 *         &lt;element name="finalNodes" type="{http://www.w3.org/2001/XMLSchema}IDREF" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Workflow", propOrder = {
    "nodes",
    "sequenceFlows",
    "initialNode",
    "finalNodes"
})
public class Workflow {

    @XmlElement(required = true)
    protected List<WorkflowNode> nodes;
    protected List<SequenceFlow> sequenceFlows;
    @XmlElement(required = true, type = Object.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected InitialEvent initialNode;
    @XmlElementRef(name = "finalNodes", namespace = "http://www.example.org/PIF", type = JAXBElement.class, required = false)
    protected List<EndEvent> finalNodes;

    /**
     * Gets the value of the nodes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nodes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNodes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WorkflowNode }
     * 
     * 
     */
    public List<WorkflowNode> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<WorkflowNode>();
        }
        return this.nodes;
    }

    /**
     * Gets the value of the sequenceFlows property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sequenceFlows property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSequenceFlows().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SequenceFlow }
     * 
     * 
     */
    public List<SequenceFlow> getSequenceFlows() {
        if (sequenceFlows == null) {
            sequenceFlows = new ArrayList<SequenceFlow>();
        }
        return this.sequenceFlows;
    }

    /**
     * Obtient la valeur de la propriété initialNode.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public InitialEvent getInitialNode() {
        return initialNode;
    }

    /**
     * Définit la valeur de la propriété initialNode.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setInitialNode(InitialEvent value) {
        this.initialNode = value;
    }

    /**
     * Gets the value of the finalNodes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the finalNodes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFinalNodes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * 
     */
    public List<EndEvent> getFinalNodes() {
        if (finalNodes == null) {
            finalNodes = new ArrayList<EndEvent>();
        }
        return this.finalNodes;
    }

}
