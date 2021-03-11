/**
 * 
 */
package fr.inria.convecs.optimus;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import fr.inria.convecs.optimus.transformer.ContentTransformer;
import fr.inria.convecs.optimus.transformer.PifContentTransformer;

/**
 * @author ajayk
 *
 */
public class PifContentTransformerTest {

	String inputFileName = "ExpenseWorkflow.bpmn.pif";

	String inputLocation = "data/output/" + inputFileName;
	String outputLocation = "data/output/" + inputFileName + ".bpmn";
	//String schemaLocation = "data/pif.xsd";

	@Test
	public void testPifGeneration() {
		// TODO: write actual test

		try {
			File input = new File(inputLocation);
			File output = new File(outputLocation);

			ContentTransformer transformer = 
					new PifContentTransformer(input, output);
			transformer.transform();

		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGeneratePifFromDir() {
		// TODO: write actual test
		String input = "data/pif/";
		String output = "data/bpmn/";
		
		try {
			File inputDir = new File(input);
			File outputDir = new File(output);

			List<File> filesList = Files.walk(Paths.get(input))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
			
			for(File inputFile: filesList)
			{
				try {
				System.out.println("\n Processing file: "+inputFile.getName());
				File outputFile = new File(output+FilenameUtils
						.removeExtension(inputFile.getName())+".bpmn");
				ContentTransformer transformer = 
						new PifContentTransformer(inputFile, outputFile);
				transformer.transform();
				} catch(Exception e)
				{
					//pass
					e.printStackTrace();
				}
			}

		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
