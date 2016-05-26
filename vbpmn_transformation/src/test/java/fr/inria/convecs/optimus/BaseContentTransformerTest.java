/**
 * 
 */
package fr.inria.convecs.optimus;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import fr.inria.convecs.optimus.model.Process;
import fr.inria.convecs.optimus.parser.BaseContentHandler;
import fr.inria.convecs.optimus.parser.ContentHandler;
import fr.inria.convecs.optimus.transformer.BaseContentTransformer;
import fr.inria.convecs.optimus.transformer.ContentTransformer;
import fr.inria.convecs.optimus.util.XmlUtil;

/**
 * @author ajayk
 * Test to generate the PIF file
 */
public class BaseContentTransformerTest {

	String inputFileName = "ExpenseWorkflow.bpmn";

	String inputLocation = "data/input/"+inputFileName;
	String outputLocation = "data/output/"+inputFileName+".pif";
	String schemaLocation = "data/pif.xsd";

	@Test
	public void testParseAndTransform()
	{
		//TODO: write actual test
		File input = new File(inputLocation);
		File output = new File(outputLocation);
		ContentHandler baseHandler = new BaseContentHandler(input);
		baseHandler.handle();
		Process actual =(Process)baseHandler.getOutput();

		ContentTransformer baseTransformer = new BaseContentTransformer(actual, output);
		baseTransformer.transform();
		Assert.assertTrue(XmlUtil.isDocumentValid(new File(outputLocation), new File(schemaLocation)));

	}


}
