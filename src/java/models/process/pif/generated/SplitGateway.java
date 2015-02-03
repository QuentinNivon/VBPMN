
package models.process.pif.generated;

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
public abstract class SplitGateway
    extends Gateway
{


}
