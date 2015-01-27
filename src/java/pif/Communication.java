
package pif;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour Communication complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
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
@XmlType(name = "Communication", namespace = "http://www.example.org/PIF")
@XmlSeeAlso({
    Interaction.class,
    MessageCommunication.class
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T04:47:27+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public abstract class Communication
    extends WorkflowNode
{

    @XmlAttribute(name = "message", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T04:47:27+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected Object message;

    /**
     * Obtient la valeur de la propriété message.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T04:47:27+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public Object getMessage() {
        return message;
    }

    /**
     * Définit la valeur de la propriété message.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T04:47:27+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setMessage(Object value) {
        this.message = value;
    }

}
