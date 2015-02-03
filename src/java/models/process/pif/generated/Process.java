
package models.process.pif.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour anonymous complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="documentation" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="peers" type="{http://www.example.org/PIF}Peer" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="messages" type="{http://www.example.org/PIF}Message" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="behaviour" type="{http://www.example.org/PIF}Workflow"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "documentation",
    "peers",
    "messages",
    "behaviour"
})
@XmlRootElement(name = "Process", namespace = "http://www.example.org/PIF")
public class Process {

    @XmlElement(namespace = "http://www.example.org/PIF", required = true)
    protected String name;
    @XmlElement(namespace = "http://www.example.org/PIF", required = true)
    protected String documentation;
    @XmlElement(namespace = "http://www.example.org/PIF")
    protected List<Peer> peers;
    @XmlElement(namespace = "http://www.example.org/PIF")
    protected List<Message> messages;
    @XmlElement(namespace = "http://www.example.org/PIF", required = true)
    protected Workflow behaviour;

    /**
     * Obtient la valeur de la propriété name.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Définit la valeur de la propriété name.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Obtient la valeur de la propriété documentation.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * Définit la valeur de la propriété documentation.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentation(String value) {
        this.documentation = value;
    }

    /**
     * Gets the value of the peers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the peers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPeers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Peer }
     * 
     * 
     */
    public List<Peer> getPeers() {
        if (peers == null) {
            peers = new ArrayList<Peer>();
        }
        return this.peers;
    }

    /**
     * Gets the value of the messages property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messages property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessages().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Message }
     * 
     * 
     */
    public List<Message> getMessages() {
        if (messages == null) {
            messages = new ArrayList<Message>();
        }
        return this.messages;
    }

    /**
     * Obtient la valeur de la propriété behaviour.
     * 
     * @return
     *     possible object is
     *     {@link Workflow }
     *     
     */
    public Workflow getBehaviour() {
        return behaviour;
    }

    /**
     * Définit la valeur de la propriété behaviour.
     * 
     * @param value
     *     allowed object is
     *     {@link Workflow }
     *     
     */
    public void setBehaviour(Workflow value) {
        this.behaviour = value;
    }

}
