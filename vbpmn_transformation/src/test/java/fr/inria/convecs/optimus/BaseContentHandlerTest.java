/**
 * 
 */

package fr.inria.convecs.optimus;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import fr.inria.convecs.optimus.parser.BaseContentHandler;
import fr.inria.convecs.optimus.parser.ContentHandler;
import fr.inria.convecs.optimus.model.Process;

/**
 * @author ajayk
 *
 */
public class BaseContentHandlerTest {

  String inputLocation = "data/input/ExpenseWorkflow.bpmn";

  @Test
  public void testSimpleFile() {
    // TODO: write actual test
    File input = new File(inputLocation);
    ContentHandler baseHandler = new BaseContentHandler(input);
    baseHandler.handle();
    Process actual = (Process) baseHandler.getOutput();
    Assert.assertSame(actual, actual); // dummy
  }

}
