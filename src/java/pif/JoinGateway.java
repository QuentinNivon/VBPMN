
package pif;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour JoinGateway complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="JoinGateway">
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
@XmlType(name = "JoinGateway", namespace = "http://www.example.org/PIF")
@XmlSeeAlso({
    OrJoinGateway.class,
    AndJoinGateway.class,
    XOrJoinGateway.class
})
public abstract class JoinGateway
    extends Gateway
{


}
