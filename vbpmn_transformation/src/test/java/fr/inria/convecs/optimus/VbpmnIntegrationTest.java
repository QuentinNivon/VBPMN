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
import fr.inria.convecs.optimus.model.Process;
import fr.inria.convecs.optimus.parser.BaseContentHandler;
import fr.inria.convecs.optimus.parser.ContentHandler;
import fr.inria.convecs.optimus.transformer.BaseContentTransformer;
import fr.inria.convecs.optimus.transformer.ContentTransformer;
import fr.inria.convecs.optimus.util.XmlUtil;
import fr.inria.convecs.optimus.validator.ModelValidator;
import fr.inria.convecs.optimus.validator.VbpmnValidator;

/**
 * @author silverquick
 *
 */
public class VbpmnIntegrationTest {

	String inputFileName = "ExpenseWorkflow.bpmn";
	String inputLocation = "data/input/" + inputFileName;
	String outputLocation = "data/output/" + inputFileName + ".pif";
	String schemaLocation = "data/pif.xsd";

	List<String> vbpmnOptions = new ArrayList<String>();

	@Test
	public void testVbpmn() {
		try {
			File input = new File(inputLocation);
			File output = new File(outputLocation);
			vbpmnOptions.add("conservative");
			ContentHandler baseHandler = new BaseContentHandler(input);
			baseHandler.handle();
			Process actual = (Process) baseHandler.getOutput();

			ContentTransformer baseTransformer = new BaseContentTransformer(actual, output);
			baseTransformer.transform();
			// TODO: Multiple asserts!!
			Assert.assertTrue(XmlUtil.isDocumentValid(new File(outputLocation), new File(schemaLocation)));

			ModelValidator modelValidator = new VbpmnValidator();

			modelValidator.validate(output, vbpmnOptions);

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
