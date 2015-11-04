//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.11.04 à 06:19:30 PM CET 
//


package models.process.pif.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour Interaction complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="Interaction">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.example.org/PIF}Communication">
 *       &lt;sequence>
 *         &lt;element name="initiatingPeer" type="{http://www.w3.org/2001/XMLSchema}IDREF"/>
 *         &lt;element name="receivingPeers" type="{http://www.w3.org/2001/XMLSchema}IDREF" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Interaction", propOrder = {
    "initiatingPeer",
    "receivingPeers"
})
public class Interaction
    extends Communication
{

    @XmlElement(required = true, type = Object.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Peer initiatingPeer;
    @XmlElementRef(name = "receivingPeers", namespace = "http://www.example.org/PIF", type = JAXBElement.class)
    protected List<Peer> receivingPeers;

    /**
     * Obtient la valeur de la propriété initiatingPeer.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Peer getInitiatingPeer() {
        return initiatingPeer;
    }

    /**
     * Définit la valeur de la propriété initiatingPeer.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setInitiatingPeer(Peer value) {
        this.initiatingPeer = value;
    }

    /**
     * Gets the value of the receivingPeers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the receivingPeers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReceivingPeers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * 
     */
    public List<Peer> getReceivingPeers() {
        if (receivingPeers == null) {
            receivingPeers = new ArrayList<Peer>();
        }
        return this.receivingPeers;
    }

}
