/**
 * 
 */
package fr.inria.convecs.optimus;

import java.io.File;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.junit.Test;

import fr.inria.convecs.optimus.pif.Process;
import fr.inria.convecs.optimus.util.PifUtil;

/**
 * @author ajayk
 *
 */
public class PifUnmarshallTest {

	String inputFileName = "PublishingSystem.pif";

	String inputLocation = "data/output/" + inputFileName;
	//String outputLocation = "data/output/" + inputFileName + ".pif";
	//String schemaLocation = "data/pif.xsd";

	@Test
	public void testUnmarshallandBalanced() {
		// TODO: write actual test

		try {
			File input = new File(inputLocation);

			System.out.println(PifUtil.isPifBalanced(input));
			
			JAXBContext jaxbContext = JAXBContext.newInstance(Process.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Process process = (Process) jaxbUnmarshaller.unmarshal(input);
			System.out.println(PifUtil.isProcessBalanced(process));
			System.out.println(process.getName());
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
