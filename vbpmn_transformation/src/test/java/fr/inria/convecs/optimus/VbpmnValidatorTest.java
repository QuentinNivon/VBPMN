/**
 * 
 */

package fr.inria.convecs.optimus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import fr.inria.convecs.optimus.config.AppProperty;
import fr.inria.convecs.optimus.validator.ModelValidator;
import fr.inria.convecs.optimus.validator.VbpmnValidator;

/**
 * @author silverquick
 *
 */
public class VbpmnValidatorTest {

	private static final String OUTPUT_PATH = AppProperty.getInstance().getFolder("OUTPUT_PATH");

	private static final String SCRIPTS_PATH = AppProperty.getInstance().getFolder("SCRIPTS_PATH");
	
	@Test
	public void testVbpmnCall() {
		try {
			ModelValidator modelValidator = new VbpmnValidator(SCRIPTS_PATH, OUTPUT_PATH);

			File input = new File("data/output/ExpenseWorkflow.bpmn.pif");
			List<String> option = new ArrayList<String>();
			option.add("conservative");
			modelValidator.validateV2(input, option);

			String result = modelValidator.getResult();

			System.out.println(result);


			FileUtils.cleanDirectory(new File(AppProperty.getInstance().getFolder("OUTPUT_PATH")));
			Assert.assertTrue(!result.contains("error"));
		} catch(IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
	}
}
