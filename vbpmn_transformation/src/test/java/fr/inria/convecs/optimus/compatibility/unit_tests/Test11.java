package fr.inria.convecs.optimus.compatibility.unit_tests;

import fr.inria.convecs.optimus.compatibility.GenericTest;
import fr.inria.convecs.optimus.py_to_java.ShellColor;
import fr.inria.convecs.optimus.py_to_java.Vbpmn;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Test11 extends GenericTest
{
	private static final String PIF_EXAMPLES_PATH = "src" + File.separator + "main" + File.separator + "resources" +
			File.separator + "pif_examples";
	private static final String TEMP_DIR_PROPERTY = "java.io.tmpdir";
	private static final String PIF_FILE_1 = "Process_6a.pif";
	private static final String PIF_FILE_2 = "Process_6b.pif";
	private static final String COMPARISON_MODE = "conservative";
	private static final boolean EXPECTED_RESULT = false;

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
		final String[] args = {firstFilePath, secondFilePath, COMPARISON_MODE};
		final Vbpmn vbpmn = new Vbpmn(args, tmpDir);
		final boolean result = vbpmn.execute();

		if (result != EXPECTED_RESULT)
		{
			System.out.println(ShellColor.ANSI_RED + "The result of this test should be \"" + EXPECTED_RESULT + "\" but is \"" + result + "\"." + ShellColor.ANSI_RESET);
			throw new RuntimeException("The result of this test should be \"" + EXPECTED_RESULT + "\" but is \"" + result + "\".");
		}
	}
}
