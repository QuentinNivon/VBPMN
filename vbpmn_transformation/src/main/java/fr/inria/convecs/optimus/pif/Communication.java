//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.03.10 at 04:11:44 PM CET 
//


package fr.inria.convecs.optimus.pif;


import jakarta.xml.bind.annotation.*;

/**
 * <p>Java class for Communication complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Communication">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.example.org/PIF}WorkflowNode">
 *       &lt;attribute name="message" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Communication")
@XmlSeeAlso({
    Interaction.class,
    MessageCommunication.class
})
public abstract class Communication
    extends WorkflowNode
{

    @XmlAttribute(name = "message", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Message message;

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setMessage(Message value) {
        this.message = value;
    }

}
