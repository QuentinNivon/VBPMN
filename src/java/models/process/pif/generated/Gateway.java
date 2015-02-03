
package models.process.pif.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour Gateway complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="Gateway">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.example.org/PIF}WorkflowNode">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Gateway", namespace = "http://www.example.org/PIF")
@XmlSeeAlso({
    SplitGateway.class,
    JoinGateway.class
})
public abstract class Gateway
    extends WorkflowNode
{


}
