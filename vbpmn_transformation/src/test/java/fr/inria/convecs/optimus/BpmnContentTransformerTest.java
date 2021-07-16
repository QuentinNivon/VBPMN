/**
 * 
 */
package fr.inria.convecs.optimus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import fr.inria.convecs.optimus.transformer.BpmnContentTransformer;

/**
 * @author ajayk
 *
 */
public class BpmnContentTransformerTest {

	String inputFileName = "data/input/test.bpmn";

	@Test
	public void testBpmnLayout() throws IOException {
		
		InputStream inputStream = new FileInputStream(new File(inputFileName));
		String inputString = IOUtils.toString(inputStream,  StandardCharsets.UTF_8);
		BpmnContentTransformer transformer = new BpmnContentTransformer(inputString);
		transformer.transform();
		System.out.println(transformer.getBpmnLayout());

	}

}
