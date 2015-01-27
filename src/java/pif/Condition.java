
package pif;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour Condition complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="Condition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Condition", namespace = "http://www.example.org/PIF")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T04:47:27+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class Condition {

    @XmlAttribute(name = "value", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T04:47:27+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String value;

    /**
     * Obtient la valeur de la propriété value.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T04:47:27+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getValue() {
        return value;
    }

    /**
     * Définit la valeur de la propriété value.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T04:47:27+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setValue(String value) {
        this.value = value;
    }

}
