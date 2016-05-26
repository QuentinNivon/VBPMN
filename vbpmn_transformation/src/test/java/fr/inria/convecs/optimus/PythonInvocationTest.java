/**
 * 
 */
package fr.inria.convecs.optimus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.inria.convecs.optimus.util.CommandExecutor;

/**
 * @author silverquick
 *
 */
public class PythonInvocationTest {

	String inputFileName = "ExpenseWorkflow.bpmn";

	String inputLocation = "data/input/"+inputFileName;
	String outputLocation = "data/output/"+inputFileName+".pif";
	String schemaLocation = "data/pif.xsd";

	@Test
	public void testVbpmnCall()
	{
		List<String> command = new ArrayList<String>();
		//command.add("/bin/bash");
		//command.add("-c");
		//command.add("python vbpmn.py ExpenseWorkflow.bpmn.pif ExpenseWorkflow.bpmn.pif conservative");
		//command.add("ls");
		command.add("python");
		command.add("scripts/vbpmn.py");
		command.add("data/output/ExpenseWorkflow.bpmn.pif");
		command.add("data/output/ExpenseWorkflow.bpmn.pif");
		command.add("conservative");
		command.add("--hiding");
		command.add("a");

		String path = "/tmp/vbpmn/output/";
		//path = "C:\\Users\\ajayk\\git\\ter\\vbpmn_transformation\\";
		
		// execute the command
		CommandExecutor commandExecutor = new CommandExecutor(command, new File(path));
		int result;
		try {
			result = commandExecutor.executeCommand();

			// get the stdout and stderr from the command that was run
			String stdout = commandExecutor.getOutput();
			String stderr = commandExecutor.getErrors();

			// print the stdout and stderr
			System.out.println("The numeric result of the command was: " + result);
			System.out.println("STDOUT:");
			System.out.println(stdout);
			System.out.println("STDERR:");
			System.out.println(stderr);


			/*String line;
			//Process p = Runtime.getRuntime().exec("/bin/bash -c ls");
			Process p = Runtime.getRuntime().exec("/bin/bash -c 'python scripts/vbpmn.py scripts/ExpenseWorkflow.bpmn.pif scripts/ExpenseWorkflow.bpmn.pif conservative'");
			BufferedReader bri = new BufferedReader
					(new InputStreamReader(p.getInputStream()));
			BufferedReader bre = new BufferedReader
					(new InputStreamReader(p.getErrorStream()));
			while ((line = bri.readLine()) != null) {
				System.out.println(line);
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
				System.out.println(line);
			}
			bre.close();
			p.waitFor();
			System.out.println("----------------------> Done.");*/
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
