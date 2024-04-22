package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.model.Process;
import fr.inria.convecs.optimus.parser.BaseContentHandler;
import fr.inria.convecs.optimus.parser.ContentHandler;
import fr.inria.convecs.optimus.py_to_java.PyToJavaUtils;
import fr.inria.convecs.optimus.py_to_java.ReturnCodes;
import fr.inria.convecs.optimus.py_to_java.ShellColor;
import fr.inria.convecs.optimus.py_to_java.Vbpmn;
import fr.inria.convecs.optimus.service.ValidationService;
import fr.inria.convecs.optimus.transformer.BaseContentTransformer;
import fr.inria.convecs.optimus.transformer.ContentTransformer;
import fr.inria.convecs.optimus.util.XmlUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.print.DocFlavor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Main
{
	private static final String DUMMY_LOOPY_LABEL = "DUMMY_LOOPY_LABEL";
	private static final String LNT_GENERIC_NAME = "process";
	private static final String COUNTEREXAMPLE_FILE = "diag";
	private static final String PIF_SCHEMA = "/pif.xsd";

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException
	{
		if (args.length != 1)
		{
			throw new IllegalStateException("Bad number of command line arguments found (expected 1 but found " + args.length + ").");
		}

		final File workingDirectory = new File(args[0]);

		if (!workingDirectory.isDirectory())
		{
			throw new IllegalStateException("Path \"" + workingDirectory.getAbsolutePath() + "\" does not point to a valid directory.");
		}

		File bpmnFile = null;

		for (File file : Objects.requireNonNull(workingDirectory.listFiles()))
		{
			if (file.getName().endsWith(".bpmn"))
			{
				//We suppose that there is only one .bpmn file in the directory
				bpmnFile = file;
				break;
			}
		}

		System.out.println("Generating PIF file...");
		final File pifFile = Main.parseAndTransform(workingDirectory, bpmnFile);
		System.out.println("PIF file generated.");

		System.out.println("Generating LNT file...");
		final File lntSpec = Main.generateLNT(workingDirectory, pifFile);
		System.out.println("LNT file generated.");

		System.out.println("Retrieving LTL property...");
		final File ltlProperty = Main.retrieveLTLProperty(workingDirectory);
		System.out.println("LTL property retrieved.");

		System.out.println("Computing specification labels...");
		final String labels = Main.computeSpecLabels(workingDirectory, lntSpec);
		System.out.println("Labels computed: " + labels + ".");

		System.out.println("Generating Büchi automata...");
		Main.generateBuchiAutomata(workingDirectory, ltlProperty);
		System.out.println("Büchi automata generated.");

		System.out.println("Generating the SVL script...");
		Main.generateSVLScript(workingDirectory, lntSpec, labels);
		System.out.println("SVL script generated.");

		System.out.println("Executing the SVL script...");
		Main.executeSVLScript(workingDirectory);
		System.out.println("SVL script executed.");

		System.out.println("Cleaning the counterexample...");
		final File counterExample = Main.generateProperCounterexample(workingDirectory);
		System.out.println("Counterexample cleaned.");

		/*final String stdOut = evaluationResults[0];

		if (stdOut.contains("FALSE"))
		{
			System.out.println("The property was evaluated to False.");
		}
		else
		{
			System.out.println("The property was evaluated to True.");
		}*/
	}

	//Private methods

	private static File generateLNT(final File workingDir,
									final File pifFile) throws IOException, InterruptedException
	{
		final Vbpmn vbpmn = new Vbpmn(
				new String[]{
						pifFile.getAbsolutePath(),
						pifFile.getAbsolutePath(),
						"property-implied",
						"--formula",
						"false"							//SIMPLEST PROPERTY TO EVALUATE, WILL FAIL ON FIRST STATE
				},
				workingDir.getAbsolutePath()
		);

		vbpmn.execute();

		final File[] dirFiles = workingDir.listFiles();

		assert dirFiles != null;

		File lntSpec = null;

		for (File file : dirFiles)
		{
			if (file.getName().endsWith(".mcl")
				|| file.getName().endsWith(".pif")
				|| file.getName().equals("res.txt")
				|| file.getName().equals("time.txt")
				|| file.getName().equals("evaluator4"))
			{
				file.delete();
			}
			else if (file.getName().endsWith(".lnt"))
			{
				if (!file.getName().equals("bpmntypes.lnt")
					&& !file.getName().equals("id.lnt"))
				{
					lntSpec = file;
				}
			}
		}
		Main.cleanDirBeforeEvaluation(dirFiles);

		if (lntSpec == null) throw new IllegalStateException("No LNT specification found in \"" + workingDir + "\".");

		return lntSpec;
	}

	private static File retrieveLTLProperty(final File workingDir)
	{
		for (File file : Objects.requireNonNull(workingDir.listFiles()))
		{
			if (file.getName().endsWith(".ltl"))
			{
				return file;
			}
		}

		throw new IllegalStateException("No LTL property found in \"" + workingDir + "\".");
	}

	private static File parseAndTransform(final File workingDir,
										  final File input) throws URISyntaxException
	{
		String pifSchema = workingDir.getAbsolutePath() + File.separator + "pif.xsd";

		ContentHandler baseHandler = new BaseContentHandler(input);
		baseHandler.handle();
		Process processOutput = (Process) baseHandler.getOutput();

		String outputFileName = input.getParentFile().getAbsolutePath() + File.separator + processOutput.getId() + ".pif";
		File outputFile = new File(outputFileName);

		ContentTransformer baseTransformer = new BaseContentTransformer(processOutput, outputFile);
		baseTransformer.transform();
		if (XmlUtil.isDocumentValid(outputFile, new File(pifSchema)))
		{
			return outputFile;
		}
		else
		{
			throw new RuntimeException("Unable to transform the file <Schema Validation Error>: " + input.getName());
		}
	}

	private static void cleanDirBeforeEvaluation(final File[] dirFiles)
	{
		for (File file : dirFiles)
		{
			if (file.getName().startsWith("buchi.")
				|| file.getName().endsWith(".bcg")
				|| file.getName().endsWith(".o")
				|| file.getName().endsWith(".aut")
				|| file.getName().endsWith(".log")
				|| file.getName().endsWith(".exp")
				|| file.getName().endsWith(".svl")
				|| file.getName().equals("reductor")
				|| file.getName().equals("task.err#0"))
			{
				file.delete();
			}
		}
	}

	private static String computeSpecLabels(final File workingDir,
											final File lntSpec) throws IOException, InterruptedException
	{
		//Convert LNT spec to BCG
		final String lntOpenCommand = "lnt.open -silent " + lntSpec.getName() + " generator " + LNT_GENERIC_NAME + ".bcg";
		final String[] lntOpenCommand2 = {"lnt.open", "-silent", lntSpec.getName(), "generator", LNT_GENERIC_NAME + ".bcg"};
		System.out.println("lnt.open command: \"" + lntOpenCommand + "\"");

		final java.lang.Process lntOpenProcess = Runtime.getRuntime().exec(
				lntOpenCommand2,
				null,
				new File(workingDir.getAbsolutePath())
		);

		final InputStream error = lntOpenProcess.getErrorStream();
		final String stdError = IOUtils.toString(error, StandardCharsets.UTF_8);
		System.out.println(stdError);
		System.out.println("BCG FILE GENERATED");

		lntOpenProcess.waitFor();
		error.close();

		//Reduce LTS with weaktrace
		final String bcgOpenCommand = "bcg_open " + LNT_GENERIC_NAME + ".bcg reductor -weaktrace " + LNT_GENERIC_NAME + "_weaktraced.bcg";
		System.out.println("bcg_open command: \"" + bcgOpenCommand + "\"");
		final java.lang.Process bcgOpenProcess = Runtime.getRuntime().exec(
				bcgOpenCommand,
				null,
				new File(workingDir.getAbsolutePath())
		);
		bcgOpenProcess.waitFor();

		System.out.println("BCG PROCESS REDUCED");

		//Retrieve LTS labels
		final String bcgIOCommand = "bcg_io -labels " + LNT_GENERIC_NAME + "_weaktraced.bcg";
		System.out.println("bcg_io command: \"" + bcgIOCommand + "\"");
		final java.lang.Process bcgIOProcess = Runtime.getRuntime().exec(
				bcgIOCommand,
				null,
				new File(workingDir.getAbsolutePath())
		);
		final InputStream output = bcgIOProcess.getInputStream();
		final InputStream error2 = bcgIOProcess.getErrorStream();
		final String stdOut = IOUtils.toString(output, StandardCharsets.UTF_8);
		final String stdError2 = IOUtils.toString(error2, StandardCharsets.UTF_8);
		final int exitValue = bcgIOProcess.waitFor();
		output.close();
		error2.close();
		final StringBuilder labelsBuilder =  new StringBuilder();

		final String[] splitStdout = stdOut.split("\n");

		for (int i = 1; i < splitStdout.length; i++)
		{
			final String label = splitStdout[i].replace(" ", "");

			if (!label.equals("i"))
			{
				labelsBuilder.append(label)
						.append(" ");
			}
		}

		System.out.println("LABELS RETRIEVED");

		labelsBuilder.append(DUMMY_LOOPY_LABEL);
		return labelsBuilder.toString();
	}

	private static void generateBuchiAutomata(final File workingDir,
											  final File ltlProperty) throws IOException, InterruptedException
	{
		//Generate Büchi automata
		final String ltl2tgbaCommand = "ltl2tgba --file=" + ltlProperty.getName() + " --hoaf=t > buchi.hoa";

		final java.lang.Process ltl2tgbaProcess = Runtime.getRuntime().exec(
				ltl2tgbaCommand,
				null,
				new File(workingDir.getAbsolutePath())
		);
		ltl2tgbaProcess.waitFor();
	}

	private static void generateSVLScript(final File workingDir,
										  final File lntSpecification,
										  final String labels) throws IOException, InterruptedException
	{
		//Generate SVL script
		final String hoa2svlCommand = "/home/quentin/LTLtoBuchi/LTL/HOA2SVLv2/Executable.x64/hoa2svl buchi.hoa " + lntSpecification.getName() + " " + labels;

		final java.lang.Process hoa2svlProcess = Runtime.getRuntime().exec(
				hoa2svlCommand,
				null,
				new File(workingDir.getAbsolutePath())
		);
		hoa2svlProcess.waitFor();
	}

	private static void executeSVLScript(final File workingDir) throws IOException, InterruptedException
	{
		//Generate SVL script
		final String svlCommand = "svl task.svl";

		final java.lang.Process svlProcess = Runtime.getRuntime().exec(
				svlCommand,
				null,
				new File(workingDir.getAbsolutePath())
		);
		svlProcess.waitFor();
	}

	private static File generateProperCounterexample(final File workingDir) throws IOException, InterruptedException
	{
		//Determinize the counterexample (with weaktrace)
		final String weaktraceCommand = "bcg_open " + COUNTEREXAMPLE_FILE + ".bcg reductor -weaktrace " + COUNTEREXAMPLE_FILE + "_weaktraced.bcg";

		final java.lang.Process weaktraceProcess = Runtime.getRuntime().exec(
				weaktraceCommand,
				null,
				new File(workingDir.getAbsolutePath())
		);
		weaktraceProcess.waitFor();

		//Translate counterexample from BCG to AUT
		final String bcgIOCommand = "bcg_io " + COUNTEREXAMPLE_FILE + "_weaktraced.bcg " + COUNTEREXAMPLE_FILE + "_weaktraced.aut";

		final java.lang.Process bcgIOProcess = Runtime.getRuntime().exec(
				bcgIOCommand,
				null,
				new File(workingDir.getAbsolutePath())
		);
		bcgIOProcess.waitFor();

		return new File(workingDir.getAbsolutePath() + File.separator + COUNTEREXAMPLE_FILE + "_weaktraced.aut");
	}
}
