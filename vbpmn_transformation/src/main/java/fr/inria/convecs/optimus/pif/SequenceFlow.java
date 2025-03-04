//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.03.10 at 04:11:44 PM CET 
//

package fr.inria.convecs.optimus.pif;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for SequenceFlow complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SequenceFlow">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="source" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *       &lt;attribute name="target" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SequenceFlow")
@XmlSeeAlso({ ConditionalSequenceFlow.class })
public class SequenceFlow {

  @XmlAttribute(name = "id", required = true)
  @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
  @XmlID
  @XmlSchemaType(name = "ID")
  protected String id;
  @XmlAttribute(name = "source", required = true)
  @XmlIDREF
  @XmlSchemaType(name = "IDREF")
  protected WorkflowNode source;
  @XmlAttribute(name = "target", required = true)
  @XmlIDREF
  @XmlSchemaType(name = "IDREF")
  protected WorkflowNode target;

  /**
   * Gets the value of the id property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the value of the id property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setId(String value) {
    this.id = value;
  }

  /**
   * Gets the value of the source property.
   * 
   * @return possible object is {@link Object }
   * 
   */
  public WorkflowNode getSource() {
    return source;
  }

  /**
   * Sets the value of the source property.
   * 
   * @param value
   *          allowed object is {@link Object }
   * 
   */
  public void setSource(WorkflowNode value) {
    this.source = value;
  }

  /**
   * Gets the value of the target property.
   * 
   * @return possible object is {@link Object }
   * 
   */
  public WorkflowNode getTarget() {
    return target;
  }

  /**
   * Sets the value of the target property.
   * 
   * @param value
   *          allowed object is {@link Object }
   * 
   */
  public void setTarget(WorkflowNode value) {
    this.target = value;
  }

}
