/**
 * 
 */
package fr.inria.convecs.optimus;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import fr.inria.convecs.optimus.pif.Process;

/**
 * @author ajayk
 *
 */
public class PifUnmarshallTest {

	String inputFileName = "SimpleInclusive.pif";

	String inputLocation = "data/output/" + inputFileName;
	//String outputLocation = "data/output/" + inputFileName + ".pif";
	//String schemaLocation = "data/pif.xsd";

	@Test
	public void testParseAndTransform() {
		// TODO: write actual test

		try {
			File input = new File(inputLocation);

			JAXBContext jaxbContext = JAXBContext.newInstance(Process.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Process process = (Process) jaxbUnmarshaller.unmarshal(input);
			System.out.println(process.getName());
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
