//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.03.10 at 04:11:44 PM CET 
//

package fr.inria.convecs.optimus.pif;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
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
@XmlType(name = "", propOrder = { "name", "documentation", "peers", "messages", "behaviour" })
@XmlRootElement(name = "Process")
public class Process {

  @XmlElement(required = true)
  protected String name;
  @XmlElement(required = true)
  protected String documentation;
  protected List<Peer> peers;
  protected List<Message> messages;
  @XmlElement(required = true)
  protected Workflow behaviour;

  /**
   * Gets the value of the name property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setName(String value) {
    this.name = value;
  }

  /**
   * Gets the value of the documentation property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDocumentation() {
    return documentation;
  }

  /**
   * Sets the value of the documentation property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setDocumentation(String value) {
    this.documentation = value;
  }

  /**
   * Gets the value of the peers property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the peers property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getPeers().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link Peer }
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
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the messages property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getMessages().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link Message }
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
   * Gets the value of the behaviour property.
   * 
   * @return possible object is {@link Workflow }
   * 
   */
  public Workflow getBehaviour() {
    return behaviour;
  }

  /**
   * Sets the value of the behaviour property.
   * 
   * @param value
   *          allowed object is {@link Workflow }
   * 
   */
  public void setBehaviour(Workflow value) {
    this.behaviour = value;
  }

}
