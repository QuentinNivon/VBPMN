package fr.inria.convecs.optimus.compatibility;

import fr.inria.convecs.optimus.py_to_java.ShellColor;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

public class FullTests
{
	@Test
	public void test() throws IOException,
			InterruptedException,
			ClassNotFoundException,
			NoSuchMethodException,
			InvocationTargetException,
			InstantiationException,
			IllegalAccessException
	{
		//Check if $CADP is set
		if (System.getenv("CADP") == null)
		{
			throw new RuntimeException("Environment variable $CADP is not set! Please fix this error and retry.");
		}
		else
		{
			System.out.println("Environment variable $CADP is set. Tests start.");
		}

		final String cadpVersion;

		//Compute CADP version in use
		try
		{
			final Process cadpLibCommand = Runtime.getRuntime().exec("cadp_lib -1");
			final BufferedReader stdInput = new BufferedReader(new InputStreamReader(cadpLibCommand.getInputStream()));
			String line;

			// Read the output from the command
			final StringBuilder stdOutBuilder = new StringBuilder();
			while ((line = stdInput.readLine()) != null)
			{
				stdOutBuilder.append(line);
			}

			cadpLibCommand.destroy();

			//Split answer by spaces
			String[] splitAnswer = stdOutBuilder.toString().split("\\s+");
			//The 2nd element is the version code, i.e. "2023k"
			cadpVersion = splitAnswer[1].replace(" ", "");
			System.out.println("----------TESTS ARE PERFORMED FOR THE CADP VERSION \"" + cadpVersion + "\"----------");
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		//Load the Test class located in the compatibility package
		final String path = Paths.get("").toAbsolutePath().toString() + File.separator + "src" + File.separator
				+ "test" + File.separator + "java" + File.separator + "fr" + File.separator + "inria" + File.separator
				+ "convecs" + File.separator + "optimus" + File.separator + "compatibility" + File.separator
				+ "unit_tests";

		System.out.println("Path: " + path);

		for (File file : new File(path).listFiles())
		{
			//Load the Pif2Lnt class located in the package corresponding to the good version
			final Class<? extends GenericTest> testClass = (Class<? extends GenericTest>)
					Class.forName("fr.inria.convecs.optimus.compatibility.unit_tests." + file.getName().replace(".java", ""));
			final Constructor<? extends GenericTest> testConstructor = testClass.getDeclaredConstructor();
			final GenericTest test = testConstructor.newInstance();

			System.out.println(ShellColor.ANSI_CYAN + "Performing " + file.getName() + "..." + ShellColor.ANSI_RESET);
			test.test();
			System.out.println(ShellColor.ANSI_GREEN + file.getName() + " passed!\n" + ShellColor.ANSI_RESET);
		}

		System.out.println(ShellColor.ANSI_GREEN + "----------COMPATIBILITY CHECK SUCCEEDED FOR CADP VERSION \"" + cadpVersion + "\"----------" + ShellColor.ANSI_RESET);
	}
}
