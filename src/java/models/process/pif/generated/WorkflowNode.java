//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.11.04 à 08:45:53 PM CET 
//


package models.process.pif.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Classe Java pour WorkflowNode complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="WorkflowNode">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="incomingFlows" type="{http://www.w3.org/2001/XMLSchema}IDREF" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="outgoingFlows" type="{http://www.w3.org/2001/XMLSchema}IDREF" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorkflowNode", propOrder = {
    "incomingFlows",
    "outgoingFlows"
})
@XmlSeeAlso({
    Task.class,
    InitialEvent.class,
    EndEvent.class,
    Gateway.class,
    Communication.class
})
public abstract class WorkflowNode {

    @XmlElementRef(name = "incomingFlows", namespace = "http://www.example.org/PIF", type = JAXBElement.class, required = false)
    protected List<JAXBElement<Object>> incomingFlows;
    @XmlElementRef(name = "outgoingFlows", namespace = "http://www.example.org/PIF", type = JAXBElement.class, required = false)
    protected List<JAXBElement<Object>> outgoingFlows;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    /**
     * Gets the value of the incomingFlows property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the incomingFlows property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIncomingFlows().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * 
     */
    public List<JAXBElement<Object>> getIncomingFlows() {
        if (incomingFlows == null) {
            incomingFlows = new ArrayList<JAXBElement<Object>>();
        }
        return this.incomingFlows;
    }

    /**
     * Gets the value of the outgoingFlows property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the outgoingFlows property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOutgoingFlows().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * 
     */
    public List<JAXBElement<Object>> getOutgoingFlows() {
        if (outgoingFlows == null) {
            outgoingFlows = new ArrayList<JAXBElement<Object>>();
        }
        return this.outgoingFlows;
    }

    /**
     * Obtient la valeur de la propriété id.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Définit la valeur de la propriété id.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
