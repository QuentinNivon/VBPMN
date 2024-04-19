/**
 * 
 */

package fr.inria.convecs.optimus.validator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.inria.convecs.optimus.py_to_java.ReturnCodes;
import fr.inria.convecs.optimus.py_to_java.Vbpmn;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.convecs.optimus.util.CommandExecutor;
import fr.inria.convecs.optimus.util.PifUtil;

/**
 * @author silverquick
 *
 */
public class VbpmnValidator implements ModelValidator {
	private Logger logger = LoggerFactory.getLogger(ModelValidator.class);

	private String scriptsFolder;

	private String outputFolder;

	private String result;

	public VbpmnValidator(String scriptsFolder, String outputFolder) {
		this.scriptsFolder = scriptsFolder;
		this.outputFolder = outputFolder;
		System.out.println("OUTPUT FOLDER: " + this.outputFolder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.inria.convecs.optimus.validator.ModelValidator#validate(java.io.File, java.lang.String)
	 */
	@Override
	public void validateV2(final File modelFile, final List<String> options) {

		validateV2(modelFile, modelFile, options);

	}

	public void validateV2(final File modelFile1,
					final File modelFile2,
					final List<String> options)
	{
		final boolean isBalanced = PifUtil.isPifBalanced(modelFile1) && PifUtil.isPifBalanced(modelFile2);
		logger.debug("The input is balanced: {}", isBalanced);

		final ArrayList<String> command = new ArrayList<>();
		command.add(modelFile1.getAbsolutePath());
		command.add(modelFile2.getAbsolutePath());
		command.addAll(options);

		logger.debug("The command is: {}", command);

		final Vbpmn vbpmn = new Vbpmn(command.toArray(new String[0]), this.outputFolder);
		final boolean result = vbpmn.execute();

		/*if (result != ReturnCodes.TERMINATION_OK)
		{
			throw new RuntimeException("Failed to execute VBPMN (return code " + result + ").");
		}*/

		final StringBuilder builder = new StringBuilder();
		builder.append(result)
				.append("|");

		try
		{
			final String dotModel1 = generateDotFile(modelFile1.getAbsolutePath().replace(".pif", ".bcg"));
			final String dotModel2 = generateDotFile(modelFile2.getAbsolutePath().replace(".pif", ".bcg"));

			builder.append(dotModel1)
					.append("|")
					.append(dotModel2);

			if (!result)
			{
				String bcgFileName = "bisimulator.bcg";
				if (options.contains("property-implied")
					|| options.contains("property-and"))
				{
					bcgFileName = "evaluator.bcg";
				}

				final File bcgFile = new File(this.outputFolder + File.separator + bcgFileName);
				final String dotBcg = generateDotFile(bcgFile.getAbsolutePath());
				builder.append("|").append(dotBcg);
			}
		}
		catch (IOException | InterruptedException e)
		{
			throw new RuntimeException(e);
		}

		this.result = builder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.inria.convecs.optimus.validator.ModelValidator#validate(java.io.File, java.io.File,
	 * java.lang.String)
	 */

	public void validate(final File modelFile1, final File modelFile2, final List<String> options) {
		Boolean isBalanced = false;
		isBalanced = PifUtil.isPifBalanced(modelFile1);
		isBalanced = isBalanced && PifUtil.isPifBalanced(modelFile2);
		logger.debug("The input isBalanced? : {}", isBalanced);
		List<String> vbpmnCommand = new ArrayList<String>();
		vbpmnCommand.add("python");
		if(isBalanced)
			vbpmnCommand.add(scriptsFolder + File.separator + "vbpmn.py");
		else
			vbpmnCommand.add(scriptsFolder + File.separator + "vbpmn.py");
		vbpmnCommand.add(modelFile1.getAbsolutePath());
		vbpmnCommand.add(modelFile2.getAbsolutePath());
		vbpmnCommand.addAll(options);
		logger.debug("The command is: {}", vbpmnCommand.toString());
		try {
			File outputDirectory = new File(outputFolder);
			//Files.copy(new File(scriptsFolder + File.separator + getBpmnTypesFilePath() + File.separator + "bpmntypes.lnt").toPath(),
			//		new File(outputFolder + File.separator +"bpmntypes.lnt").toPath());
			CommandExecutor commandExecutor = new CommandExecutor(vbpmnCommand, outputDirectory);
			int execResult = commandExecutor.executeCommand();

			logger.debug("The return value of execution of command is: {}", execResult);

			String response = handleResponse(commandExecutor.getOutput().trim(),
					commandExecutor.getErrors().trim());

			StringBuilder resultBuilder = new StringBuilder();
			if(response.equalsIgnoreCase("TRUE") || response.equalsIgnoreCase("FALSE"))
			{
				resultBuilder.append(response).append("|");
				String dotModel1 = generateDotFile(modelFile1.getAbsolutePath().replace(".pif", ".bcg"));
				String dotModel2 = generateDotFile(modelFile2.getAbsolutePath().replace(".pif", ".bcg"));
				resultBuilder.append(dotModel1).append("|");
				resultBuilder.append(dotModel2);
				if (response.equalsIgnoreCase("FALSE")) {
					String bcgFileName = "bisimulator.bcg";
					if(options.contains("property-implied") || options.contains("property-and"))
						bcgFileName = "evaluator.bcg";
					File bcgFile = new File(outputFolder + File.separator + bcgFileName);
					String dotBcg = generateDotFile(bcgFile.getAbsolutePath());
					resultBuilder.append("|").append(dotBcg);
				}
				this.result = resultBuilder.toString();
			}
			else 
			{
				this.result = response;
			}

		} catch (Exception e) {
			logger.error("Failed executing the command", e);
			throw new RuntimeException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.inria.convecs.optimus.validator.ModelValidator#getResult()
	 */
	@Override
	public String getResult() {
		return this.result;
	}

	/**
	 * 
	 * @param commandExecutor
	 * @return
	 */
	private String handleResponse(final String stdOut, final String stdErr) {
		StringBuilder responseBuilder = new StringBuilder();

		if (null != stdErr && !stdErr.isEmpty()) 
		{
			logger.debug("The stderr of command execution: {}", stdErr);
			responseBuilder.append("Std error executing the command: ").append(stdErr);

		} 
		else if (null != stdOut && !stdOut.isEmpty()) 
		{
			logger.debug("The stdout of command execution: {}", stdOut);
			// TODO: crude method -cleaner approach required
			if (stdOut.contains("ERROR")) 
			{
				responseBuilder.append("Internal error executing the command: ").append(stdOut);
			} 
			else 
			{
				final int index = stdOut.lastIndexOf("\n");

				if (index == -1)
				{
					responseBuilder.append(stdOut);
				}
				else
				{
					String lastLine = stdOut.substring(stdOut.lastIndexOf("\n")).trim();

					if (!(lastLine.equalsIgnoreCase("TRUE") || lastLine.equalsIgnoreCase("FALSE")))
					{
						responseBuilder.append(stdOut);
					}
					else
					{
						responseBuilder.append(lastLine);
					}
				}
			}
		} 
		else 
		{
			responseBuilder.append("*** Unable to process request - No Result Available ***");
		}

		return responseBuilder.toString();
	}

	private String generateDotFile(String absolutePath) throws IOException, InterruptedException {
		String dotFile = absolutePath.replace(".bcg", ".dot");
		logger.debug("dot file: {}", dotFile);
		List<String> command = new ArrayList<String>();
		command.add("bcg_io");
		command.add(absolutePath);
		command.add(dotFile);

		CommandExecutor commandExecutor = new CommandExecutor(command, new File(outputFolder));
		int execResult = commandExecutor.executeCommand();

		logger.debug("The exec result of command [ {} ] is {}", command, execResult);

		if (execResult != 0) {
			throw new RuntimeException("Erorr executing BCG draw - " + commandExecutor.getErrors());
		}

		File outputFile = new File(dotFile);

		String dotOutput = FileUtils.readFileToString(outputFile, "UTF-8");
		dotOutput = dotOutput.replaceAll("\\R", " "); // Java 8 carriage return replace

		return dotOutput.trim();
	}
}
