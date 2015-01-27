
package pif;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SplitGateway complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SplitGateway">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.example.org/PIF}Gateway">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SplitGateway", namespace = "http://www.example.org/PIF")
@XmlSeeAlso({
    XOrSplitGateway.class,
    AndSplitGateway.class,
    OrSplitGateway.class
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2015-01-27T06:19:59+01:00", comments = "JAXB RI v2.2.8-b130911.1802")
public abstract class SplitGateway
    extends Gateway
{


}
