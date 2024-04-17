package fr.inria.convecs.optimus.compatibility.unit_tests;

import fr.inria.convecs.optimus.compatibility.GenericTest;
import fr.inria.convecs.optimus.py_to_java.ShellColor;
import fr.inria.convecs.optimus.py_to_java.Vbpmn;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Test30 extends GenericTest
{
	private static final String PIF_EXAMPLES_PATH = "src" + File.separator + "main" + File.separator + "resources" +
			File.separator + "pif_examples";
	private static final String TEMP_DIR_PROPERTY = "java.io.tmpdir";
	private static final String PIF_FILE_1 = "Process_8F.pif"; //Unbalanced inclusive gateway inside a loop => should not work
	private static final String PIF_FILE_2 = "Process_8F.pif";
	private static final String PROPERTY_MODE = "property-implied";
	private static final String FORMULA_ARG = "--formula";
	private static final String FORMULA = "true";

	@Test
	public void test() throws IOException, InterruptedException
	{
		//Get a temporary directory and clean it
		final String tmpDir = Files.createTempDirectory("").toFile().getAbsolutePath();
		final String tmpDirsLocation = System.getProperty(TEMP_DIR_PROPERTY);
		assert tmpDir.startsWith(tmpDirsLocation);
		final File tempDir = new File(tmpDir);
		FileUtils.cleanDirectory(tempDir);

		//Copy the files of the test to the temporary directory
		//First file
		final String firstFilePath = tmpDir + File.separator + PIF_FILE_1;
		try (final InputStream inputStream = Files.newInputStream(Paths.get(PIF_EXAMPLES_PATH + File.separator + PIF_FILE_1)))
		{
			Files.copy(inputStream, Paths.get(tmpDir + File.separator + PIF_FILE_1));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		//Second file
		final String secondFilePath = tmpDir + File.separator + PIF_FILE_2;
		if (!new File(secondFilePath).exists())
		{
			try (final InputStream inputStream = Files.newInputStream(Paths.get(PIF_EXAMPLES_PATH + File.separator + PIF_FILE_2)))
			{
				Files.copy(inputStream, Paths.get(tmpDir + File.separator + PIF_FILE_2));
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		//Start VBPMN
		final String[] args = {firstFilePath, secondFilePath, PROPERTY_MODE, FORMULA_ARG, FORMULA};
		final Vbpmn vbpmn = new Vbpmn(args, tmpDir);

		try
		{
			final boolean result = vbpmn.execute();
		}
		catch (IllegalStateException e)
		{
			if (e.getMessage().toLowerCase().contains("unbalanced inclusive gateways"))
			{
				//We properly detected the unbalanced inclusive gateway
				return;
			}
		}

		final String errorMessage = "Process \"" + firstFilePath + "\" contains an unbalanced inclusive gateway but" +
				" it was not detected";
		System.out.println(ShellColor.ANSI_RED + errorMessage + ShellColor.ANSI_RESET);
		throw new RuntimeException(errorMessage);
	}
}
