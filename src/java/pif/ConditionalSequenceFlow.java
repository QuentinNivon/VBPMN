
package pif;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ConditionalSequenceFlow complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ConditionalSequenceFlow">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.example.org/PIF}SequenceFlow">
 *       &lt;sequence>
 *         &lt;element name="condition" type="{http://www.example.org/PIF}Condition"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConditionalSequenceFlow", namespace = "http://www.example.org/PIF", propOrder = {
    "condition"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T06:03:22+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class ConditionalSequenceFlow
    extends SequenceFlow
{

    @XmlElement(namespace = "http://www.example.org/PIF", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T06:03:22+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected Condition condition;

    /**
     * Obtient la valeur de la propriété condition.
     * 
     * @return
     *     possible object is
     *     {@link Condition }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T06:03:22+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public Condition getCondition() {
        return condition;
    }

    /**
     * Définit la valeur de la propriété condition.
     * 
     * @param value
     *     allowed object is
     *     {@link Condition }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T06:03:22+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setCondition(Condition value) {
        this.condition = value;
    }

}
