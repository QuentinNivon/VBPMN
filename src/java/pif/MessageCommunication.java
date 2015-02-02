
package pif;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour MessageCommunication complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="MessageCommunication">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.example.org/PIF}Communication">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageCommunication", namespace = "http://www.example.org/PIF")
@XmlSeeAlso({
    MessageReception.class,
    MessageSending.class
})
public abstract class MessageCommunication
    extends Communication
{


}
