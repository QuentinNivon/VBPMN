package fr.inria.convecs.optimus.compatibility;

import fr.inria.convecs.optimus.py_to_java.ShellColor;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class FullTests
{
	@Test
	public void test() throws IOException, InterruptedException
	{
		final String cadpVersion;

		//Compute CADP version in use
		try
		{
			final Process cadpLibCommand = Runtime.getRuntime().exec("cadp_lib -1");
			final BufferedReader stdInput = new BufferedReader(new InputStreamReader(cadpLibCommand.getInputStream()));
			final BufferedReader stdError = new BufferedReader(new InputStreamReader(cadpLibCommand.getErrorStream()));
			String line;

			// Read the output from the command
			final StringBuilder stdOutBuilder = new StringBuilder();
			while ((line = stdInput.readLine()) != null)
			{
				stdOutBuilder.append(line);
			}

			// Read any errors from the attempted command
			final StringBuilder stdErrBuilder = new StringBuilder();
			while ((line = stdError.readLine()) != null)
			{
				stdErrBuilder.append(line);
			}

			cadpLibCommand.destroy();

			//Split answer by spaces
			String[] splitAnswer = stdOutBuilder.toString().split("\\s+");
			//The 2nd element is the version code, i.e. "2023k"
			cadpVersion = splitAnswer[1].replace(" ", "");
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		//Test 1
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 1..." + ShellColor.ANSI_RESET);
		new Test1().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 1 passed!\n" + ShellColor.ANSI_RESET);

		//Test 2
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 2..." + ShellColor.ANSI_RESET);
		new Test2().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 2 passed!\n" + ShellColor.ANSI_RESET);

		//Test 3
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 3..." + ShellColor.ANSI_RESET);
		new Test3().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 3 passed!\n" + ShellColor.ANSI_RESET);

		//Test 4
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 4..." + ShellColor.ANSI_RESET);
		new Test4().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 4 passed!\n" + ShellColor.ANSI_RESET);

		//Test 5
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 5..." + ShellColor.ANSI_RESET);
		new Test5().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 5 passed!\n" + ShellColor.ANSI_RESET);

		//Test 6
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 6..." + ShellColor.ANSI_RESET);
		new Test6().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 6 passed!\n" + ShellColor.ANSI_RESET);

		//Test 7
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 7..." + ShellColor.ANSI_RESET);
		new Test7().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 7 passed!\n" + ShellColor.ANSI_RESET);

		//Test 8
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 8..." + ShellColor.ANSI_RESET);
		new Test8().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 8 passed!\n" + ShellColor.ANSI_RESET);

		//Test 9
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 9..." + ShellColor.ANSI_RESET);
		new Test9().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 9 passed!\n" + ShellColor.ANSI_RESET);

		//Test 10
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 10..." + ShellColor.ANSI_RESET);
		new Test10().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 10 passed!\n" + ShellColor.ANSI_RESET);

		//Test 11
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 11..." + ShellColor.ANSI_RESET);
		new Test11().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 11 passed!\n" + ShellColor.ANSI_RESET);

		//Test 12
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 12..." + ShellColor.ANSI_RESET);
		new Test12().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 12 passed!\n" + ShellColor.ANSI_RESET);

		//Test 13
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 13..." + ShellColor.ANSI_RESET);
		new Test13().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 13 passed!\n" + ShellColor.ANSI_RESET);

		//Test 14
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 14..." + ShellColor.ANSI_RESET);
		new Test14().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 14 passed!\n" + ShellColor.ANSI_RESET);

		//Test 15
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 15..." + ShellColor.ANSI_RESET);
		new Test15().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 15 passed!\n" + ShellColor.ANSI_RESET);

		//Test 16
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 16..." + ShellColor.ANSI_RESET);
		new Test16().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 16 passed!\n" + ShellColor.ANSI_RESET);

		//Test 17
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 17..." + ShellColor.ANSI_RESET);
		new Test17().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 17 passed!\n" + ShellColor.ANSI_RESET);

		//Test 18
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 18..." + ShellColor.ANSI_RESET);
		new Test18().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 18 passed!\n" + ShellColor.ANSI_RESET);

		//Test 19
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 19..." + ShellColor.ANSI_RESET);
		new Test19().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 19 passed!\n" + ShellColor.ANSI_RESET);

		//Test 20
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 20..." + ShellColor.ANSI_RESET);
		new Test20().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 20 passed!\n" + ShellColor.ANSI_RESET);

		//Test 21
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 21..." + ShellColor.ANSI_RESET);
		new Test21().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 21 passed!\n" + ShellColor.ANSI_RESET);

		//Test 22
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 22..." + ShellColor.ANSI_RESET);
		new Test22().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 22 passed!\n" + ShellColor.ANSI_RESET);

		//Test 23
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 23..." + ShellColor.ANSI_RESET);
		new Test23().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 23 passed!\n" + ShellColor.ANSI_RESET);

		//Test 24
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 24..." + ShellColor.ANSI_RESET);
		new Test24().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 24 passed!\n" + ShellColor.ANSI_RESET);

		//Test 25
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 25..." + ShellColor.ANSI_RESET);
		new Test25().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 25 passed!\n" + ShellColor.ANSI_RESET);

		//Test 26
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 26..." + ShellColor.ANSI_RESET);
		new Test26().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 26 passed!\n" + ShellColor.ANSI_RESET);

		//Test 27
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 27..." + ShellColor.ANSI_RESET);
		new Test27().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 27 passed!\n" + ShellColor.ANSI_RESET);

		//Test 28
		System.out.println(ShellColor.ANSI_BLUE + "Performing test 28..." + ShellColor.ANSI_RESET);
		new Test28().test();
		System.out.println(ShellColor.ANSI_BLUE + "Test 28 passed!\n" + ShellColor.ANSI_RESET);

		System.out.println(ShellColor.ANSI_GREEN + "----------COMPATIBILITY CHECK SUCCEEDED FOR CADP VERSION \"" + cadpVersion + "\"----------" + ShellColor.ANSI_RESET);
	}
}
