package fr.inria.convecs.optimus.util;

import fr.inria.convecs.optimus.py_to_java.PyToJavaUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager
{
	 private final String command;
	 private final ArrayList<String> args;
	 private final StringBuilder stdOut;
	 private final StringBuilder stdErr;
	 private final File workingDirectory;
	 private int returnValue;

	 public CommandManager(String command,
												 File workingDirectory,
												 String... args)
     {
        this.command = command;
			this.args = new ArrayList<>();
			this.args.addAll(Arrays.asList(args));
			this.workingDirectory = workingDirectory;
			this.stdOut = new StringBuilder();
			this.stdErr = new StringBuilder();
	 }

	 public CommandManager(String command,
												 File workingDirectory,
												 List<String> args)
	 {
			this.command = command;
			this.args = new ArrayList<>(args);
			this.workingDirectory = workingDirectory;
			this.stdOut = new StringBuilder();
			this.stdErr = new StringBuilder();
	 }

	 public void execute() throws IOException, InterruptedException
	 {
			ArrayList<String> command = new ArrayList<>();
			command.add(this.command);
			command.addAll(this.args);
			System.out.println("Command to execute: \"" + PyToJavaUtils.join(command, " ") + "\".");
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.directory(this.workingDirectory);
			Process process = processBuilder.start();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			boolean forceQuit = false;

			String line;
			while((line = bufferedReader.readLine()) != null)
			{
				 this.stdOut.append(line).append("\n");

				 if (line.contains("impossible rendez-vous detected"))
				 {
						forceQuit = true;
						break;
				 }
			}

			if (forceQuit)
			{
				 bufferedReader.close();
				 process.destroy();
				 this.returnValue = 0;

				 while (process.isAlive()){}
			}
			else
			{
				 this.returnValue = process.waitFor();
			}
	 }

	 public String stdOut()
	 {
			return this.stdOut.toString();
	 }

	 public String stdErr()
	 {
			return this.stdErr.toString();
	 }

	 public int returnValue()
	 {
			return this.returnValue;
	 }
}
