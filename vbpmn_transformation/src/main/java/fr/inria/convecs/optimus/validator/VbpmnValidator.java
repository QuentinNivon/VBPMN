/**
 * 
 */

package fr.inria.convecs.optimus.validator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.convecs.optimus.config.AppProperty;
import fr.inria.convecs.optimus.util.CommandExecutor;

/**
 * @author silverquick
 *
 */
public class VbpmnValidator implements ModelValidator {

  private static final Logger logger = LoggerFactory.getLogger(ModelValidator.class);

  private static final String SCRIPTS_FOLDER = AppProperty.getInstance().getValue("SCRIPTS_FOLDER");

  private static final String OUTPUT_FOLDER = AppProperty.getInstance().getValue("OUTPUT_FOLDER");

  private String result;

  /*
   * (non-Javadoc)
   * 
   * @see fr.inria.convecs.optimus.validator.ModelValidator#validate(java.io.File, java.lang.String)
   */
  @Override
  public void validate(final File modelFile, final List<String> options) {

    validate(modelFile, modelFile, options);

  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.inria.convecs.optimus.validator.ModelValidator#validate(java.io.File, java.io.File,
   * java.lang.String)
   */
  @Override
  public void validate(final File modelFile1, final File modelFile2, final List<String> options) {
    List<String> vbpmnCommand = new ArrayList<String>();
    vbpmnCommand.add("python");
    vbpmnCommand.add(SCRIPTS_FOLDER + "vbpmn.py");
    vbpmnCommand.add(modelFile1.getAbsolutePath());
    vbpmnCommand.add(modelFile2.getAbsolutePath());
    vbpmnCommand.addAll(options);
    logger.debug("The command is: {}", vbpmnCommand.toString());
    try {
      CommandExecutor commandExecutor = new CommandExecutor(vbpmnCommand, new File(OUTPUT_FOLDER));
      int execResult = commandExecutor.executeCommand();

      logger.debug("The return value of execution of command is: {}", execResult);

      String response = handleResponse(commandExecutor.getOutput().trim(),
          commandExecutor.getErrors().trim());

      if (response.equalsIgnoreCase("FALSE")) {
        String bcgFileName = "bisimulator.bcg";
        File bcgFile = new File(OUTPUT_FOLDER + bcgFileName);
        this.result = generatePostScriptFiles(bcgFile);
      } else {
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
    StringBuilder resultBuilder = new StringBuilder();
    if (null != stdOut) {
      logger.debug("The stdout of command execution: {}", stdOut);
      // TODO: crude method -cleaner approach required
      if (stdOut.contains("ERROR")) {
        resultBuilder.append("Internal error executing the command: ").append(stdOut);
      } else {
        String lastLine = stdOut.substring(stdOut.lastIndexOf("\n")).trim();
        if (!(lastLine.equalsIgnoreCase("TRUE") || lastLine.equalsIgnoreCase("FALSE"))) {
          resultBuilder.append(stdOut);
        } else {
          resultBuilder.append(lastLine);
        }
      }
    } else {
      resultBuilder.append("*** Unable to process request - No Stdout Result Available ***");
    }

    if (null != stdErr) {
      logger.debug("The stderr of command execution: {}", stdErr);
    }
    return resultBuilder.toString();
  }

  /**
   * 
   * @param name
   * @param name2
   * @return
   */
  private String generatePostScriptFiles(File bcgFile) {
    String result = null;
    try {
      executeBcgDraw(bcgFile.getAbsolutePath());

      result = new StringBuilder().append("FALSE").append("|")
          .append(bcgFile.getName().replace(".bcg", ".ps")).toString();

    } catch (Exception e) {
      logger.warn("Error generating postscript files {}", e);
      result = "FALSE \n (Could not generate the postscript files)";

    }

    return result;
  }

  private void executeBcgDraw(String absolutePath) throws IOException, InterruptedException {
    List<String> command = new ArrayList<String>();
    command.add("bcg_draw");
    command.add("-ps");
    command.add(absolutePath);

    CommandExecutor commandExecutor = new CommandExecutor(command, new File(OUTPUT_FOLDER));
    int execResult = commandExecutor.executeCommand();

    logger.debug("The exec result of command [ {} ] is {}", command, execResult);

    if (execResult != 0) {
      throw new RuntimeException("Erorr executing BCG draw - " + commandExecutor.getErrors());
    }

  }
}
