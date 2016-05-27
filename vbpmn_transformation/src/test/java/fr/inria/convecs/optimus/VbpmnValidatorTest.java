/**
 * 
 */

package fr.inria.convecs.optimus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import fr.inria.convecs.optimus.validator.ModelValidator;
import fr.inria.convecs.optimus.validator.VbpmnValidator;

/**
 * @author silverquick
 *
 */
public class VbpmnValidatorTest {

  @Test
  public void testVbpmnCall() {
    ModelValidator modelValidator = new VbpmnValidator();

    File input = new File("data/output/ExpenseWorkflow.bpmn.pif");
    List<String> option = new ArrayList<String>();
    option.add("conservative");
    modelValidator.validate(input, option);

    String result = modelValidator.getResult();

    System.out.println(result);

    Assert.assertNotNull(result);
  }
}
