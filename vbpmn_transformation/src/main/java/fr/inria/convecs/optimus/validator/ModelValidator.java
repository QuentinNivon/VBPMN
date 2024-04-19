/**
 * 
 */

package fr.inria.convecs.optimus.validator;

import java.io.File;
import java.util.List;

/**
 * @author silverquick
 *
 */
public interface ModelValidator {

  /**
   * 
   * @param modelFile
   * @param options
   */
  public void validateV2(final File modelFile, final List<String> options);

  /**
   * 
   * @param modelFile1
   * @param modelFile2
   * @param options
   */
  public void validateV2(final File modelFile1, final File modelFile2, final List<String> options);

  /**
   * 
   * @return
   */
  public String getResult();

}
