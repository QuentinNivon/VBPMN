/**
 * 
 */

package fr.inria.convecs.optimus.transformer;

/**
 * @author ajayk
 *
 */
public interface ContentTransformer {

  /**
   * Transforms the object representation into PIF xml.
   */
  void transform();

  /**
   * Generates the PIF xml in specified format.
   */
  void generateOutput();
}
