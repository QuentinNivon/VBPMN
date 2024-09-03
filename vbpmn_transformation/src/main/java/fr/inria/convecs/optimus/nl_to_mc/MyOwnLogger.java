package fr.inria.convecs.optimus.nl_to_mc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;

public class MyOwnLogger
{
	private static final String BASE_FILENAME = "std_mc";
	private static final StringBuilder builder = new StringBuilder();

	private MyOwnLogger()
	{

	}

	public static void append(String msg)
	{
		builder.append("\n\n").append(msg);
	}

	public static void writeStdOut(final File workingDirectory)
	{
		final String stdOutPath = workingDirectory.getPath() + File.separator + BASE_FILENAME + ".out";
		final File stdOutFile = new File(stdOutPath);

		if (stdOutFile.exists()) stdOutFile.delete();

		final PrintWriter printWriter;

		try
		{
			printWriter = new PrintWriter(stdOutFile);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}

		printWriter.print(builder.toString());
		printWriter.flush();
		printWriter.close();
	}

	public static void writeStdErr(final File workingDirectory,
								   final String msg)
	{
		final String stdErrPath = workingDirectory.getPath() + File.separator + BASE_FILENAME + ".err";
		final File stdErrFile = new File(stdErrPath);

		if (stdErrFile.exists()) stdErrFile.delete();

		final PrintWriter printWriter;

		try
		{
			printWriter = new PrintWriter(stdErrFile);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}

		printWriter.print(msg);
		printWriter.flush();
		printWriter.close();
	}
}
