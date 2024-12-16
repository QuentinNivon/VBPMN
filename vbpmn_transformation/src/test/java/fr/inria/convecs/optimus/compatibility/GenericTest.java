package fr.inria.convecs.optimus.compatibility;

import fr.inria.convecs.optimus.py_to_java.Vbpmn;
import fr.inria.convecs.optimus.util.Balancement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Generic class used as a basis for designing compatibility tests for VBPMN (unit_tests/*).
 * It contains the path of the PIF files (PIF_EXAMPLES_PATH), the name of the Java property returning the path of an
 * available temporary directory depending on the OS (TEMP_DIR_PROPERTY), and the abstract function test() that will
 * be implemented by all child test classes.
 */

public abstract class GenericTest
{
	protected static final String PIF_EXAMPLES_PATH = "src" + File.separator + "main" + File.separator + "resources" +
			File.separator + "pif_examples";
	protected static final String TEMP_DIR_PROPERTY = "java.io.tmpdir";

	public GenericTest()
	{

	}

	@Test
	public abstract void test() throws IOException;

	public String getTempDir() throws IOException
	{
		//Get a temporary directory and clean it.
		final String tmpDir = Files.createTempDirectory("").toFile().getAbsolutePath();
		final String tmpDirsLocation = System.getProperty(TEMP_DIR_PROPERTY);
		assert tmpDir.startsWith(tmpDirsLocation);
		final File tempDir = new File(tmpDir);
		FileUtils.cleanDirectory(tempDir);

		return tmpDir;
	}

	public Pair<String, String> copyTestFiles(final String tempDir,
											  final String pifFile1,
											  final String pifFile2) throws IOException
	{
		//First file.
		final String firstFilePath = tempDir + File.separator + pifFile1;
		final InputStream inputStreamFile1 = Files.newInputStream(Paths.get(PIF_EXAMPLES_PATH + File.separator + pifFile1));
		Files.copy(inputStreamFile1, Paths.get(tempDir + File.separator + pifFile1));
		inputStreamFile1.close();

		//Second file.
		final String secondFilePath = tempDir + File.separator + pifFile2;

		if (!new File(secondFilePath).exists())
		{
			final InputStream inputStreamFile2 = Files.newInputStream(Paths.get(PIF_EXAMPLES_PATH + File.separator + pifFile2));
			Files.copy(inputStreamFile2, Paths.get(tempDir + File.separator + pifFile2));
			inputStreamFile2.close();
		}

		return Pair.of(firstFilePath, secondFilePath);
	}

	public Vbpmn getVbpmnInstance(final String tmpDir,
								  final String... args)
	{
		final String[] nonNullArgs = Arrays.stream(args).filter(Objects::nonNull).toArray(String[]::new);
		return new Vbpmn(nonNullArgs, tmpDir);
	}

	public Vbpmn getVbpmnInstance(final String tmpDir,
								  final Balancement balancement,
								  final String... args)
	{
		final String[] nonNullArgs = Arrays.stream(args).filter(Objects::nonNull).toArray(String[]::new);
		return new Vbpmn(nonNullArgs, tmpDir, balancement);
	}

	public Vbpmn getVbpmnInstance(final String tmpDir,
								  final Collection<String> process1Alphabet,
								  final Collection<String> process2Alphabet,
								  final String... args)
	{
		final String[] nonNullArgs = Arrays.stream(args).filter(Objects::nonNull).toArray(String[]::new);
		return new Vbpmn(nonNullArgs, tmpDir, true, process1Alphabet, process2Alphabet);
	}

	public Vbpmn getVbpmnInstance(final String tmpDir,
								  final Collection<String> process1Alphabet,
								  final Collection<String> process2Alphabet,
								  final Balancement balancement,
								  final String... args)
	{
		final String[] nonNullArgs = Arrays.stream(args).filter(Objects::nonNull).toArray(String[]::new);
		return new Vbpmn(nonNullArgs, tmpDir, true, process1Alphabet, process2Alphabet, balancement);
	}
}
