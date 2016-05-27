/**
 * 
 */

package fr.inria.convecs.optimus.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ajayk
 *
 */
public class CommandExecutor {

  private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

  private String stdOut;
  private String stdError;
  private List<String> command;
  private File directory;

  /**
   * 
   * @param command
   */
  public CommandExecutor(List<String> command, File directory) {
    this.command = command;
    this.directory = directory;
  }

  public int executeCommand() {
    int intValue = -99;
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(command);
      processBuilder.directory(directory);
      Process process = processBuilder.start();

      InputStream output = process.getInputStream();
      InputStream error = process.getErrorStream();

      stdOut = IOUtils.toString(output, StandardCharsets.UTF_8.name());
      stdError = IOUtils.toString(error, StandardCharsets.UTF_8.name());

      intValue = process.waitFor();
    } catch (IOException e) {
      logger.warn("Execption executing the system command", e);
      throw new RuntimeException(e);

    } catch (InterruptedException e) {
      logger.warn("InterruptedException - Unable to get the exit value", e);
      throw new RuntimeException(e);
    }
    return intValue;
  }

  public String getErrors() {
    return stdError;
  }

  public String getOutput() {
    return stdOut;
  }
}
