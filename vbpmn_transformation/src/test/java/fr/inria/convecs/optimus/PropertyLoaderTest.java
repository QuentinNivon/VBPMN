/**
 * 
 */

package fr.inria.convecs.optimus;

import org.junit.Assert;
import org.junit.Test;

import fr.inria.convecs.optimus.config.AppProperty;

/**
 * @author silverquick
 *
 */
public class PropertyLoaderTest {

  @Test
  public void testPropertyLoad() {
    String expected = "/tmp/vbpmn/input/";

    String actual = AppProperty.getInstance().getValue("UPLOAD_PATH");

    Assert.assertTrue(actual.equals(expected));
  }

}
