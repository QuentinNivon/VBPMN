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
	  
    String actual = AppProperty.getInstance().getFolder("SCRIPTS_PATH").trim();

    Assert.assertTrue(!actual.isEmpty());
  }

}
