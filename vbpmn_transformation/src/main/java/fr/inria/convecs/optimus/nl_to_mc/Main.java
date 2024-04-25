package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.model.Process;
import fr.inria.convecs.optimus.parser.BaseContentHandler;
import fr.inria.convecs.optimus.parser.ContentHandler;
import fr.inria.convecs.optimus.py_to_java.Vbpmn;
import fr.inria.convecs.optimus.transformer.BaseContentTransformer;
import fr.inria.convecs.optimus.transformer.ContentTransformer;
import fr.inria.convecs.optimus.util.CommandManager;
import fr.inria.convecs.optimus.util.Utils;
import fr.inria.convecs.optimus.util.XmlUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Main
{
	private static final String DUMMY_LOOPY_LABEL = "DUMMY_LOOPY_LABEL";
	private static final String LNT_GENERIC_NAME = "process";
	private static final String COUNTEREXAMPLE_FILE = "diag";
	private static final String BUCHI_AUTOMATA = "buchi.hoa";
	private static final String SVL_SCRIPT_NAME = "task.svl";
	private static final String RESULT_FILE_NAME = "res.txt";
	private static final String ID_FILE = "id.lnt";
	private static final String BPMN_TYPES_FILE = "bpmntypes.lnt";
	private static final String PIF_SCHEMA = "pif.xsd";

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
		final File[] files = Objects.requireNonNull(workingDirectory.listFiles());

		for (File file : files)
		{
			if (file.getName().endsWith(".bpmn"))
			{
				bpmnFile = file;
				break;
			}
		}

		System.out.println("Generating PIF file...");
		final File pifFile = Main.parseAndTransform(workingDirectory, bpmnFile);
		System.out.println("PIF file generated.\n");

		System.out.println("Generating LNT file...");
		final File lntSpec = Main.generateLNT(workingDirectory, pifFile);
		System.out.println("LNT file generated.\n");

		System.out.println("Retrieving LTL property...");
		final File ltlProperty = Main.retrieveLTLProperty(workingDirectory);
		System.out.println("LTL property retrieved.\n");

		System.out.println("Computing specification labels...");
		final ArrayList<String> labels = Main.computeSpecLabels(workingDirectory, lntSpec);
		System.out.println("Labels computed: " + labels + ".\n");

		System.out.println("Generating Büchi automata...");
		Main.generateBuchiAutomata(workingDirectory, ltlProperty);
		System.out.println("Büchi automata generated.\n");

		System.out.println("Generating the SVL script...");
		Main.generateSVLScript(workingDirectory, lntSpec, labels);
		System.out.println("SVL script generated.\n");

		System.out.println("Executing the SVL script...");
		Main.executeSVLScript(workingDirectory);
		System.out.println("SVL script executed.\n");

		System.out.println("Cleaning the counterexample...");
		final File counterExample = Main.generateProperCounterexample(workingDirectory);
		System.out.println("Counterexample cleaned.\n");

		System.out.println("Converting counterexample to VIS format...");
		final Aut2Vis aut2Vis = new Aut2Vis(workingDirectory, counterExample, COUNTEREXAMPLE_FILE);
		final File visFile = aut2Vis.generateVisFile();
		System.out.println("Counterexample converted.\n");

		System.out.println("Cleaning working directory...");
		Main.finalClean(workingDirectory);
		System.out.println("Directory cleaned.\n");
	}

	private static File generateLNT(File workingDir, File pifFile) throws IOException, InterruptedException
	{
		final Vbpmn vbpmn = new Vbpmn(new String[]{
				pifFile.getAbsolutePath(),
				pifFile.getAbsolutePath(),
				"property-implied",
				"--formula",
				"false"
		}, workingDir.getAbsolutePath());
		vbpmn.execute();

		final File[] dirFiles = workingDir.listFiles();

		assert dirFiles != null;

		File lntSpec = null;

		for (File file : dirFiles)
		{
			if (file.getName().endsWith(".mcl")
				|| file.getName().endsWith(".pif")
				|| file.getName().equals(RESULT_FILE_NAME)
				|| file.getName().equals("time.txt")
				|| file.getName().equals("evaluator4"))
			{
				file.delete();
			}
			else if (file.getName().endsWith(".lnt"))
			{
				if (!file.getName().equals(BPMN_TYPES_FILE)
					&& !file.getName().equals(ID_FILE))
				{
					lntSpec = file;
				}
			}
		}

		Main.cleanDirBeforeEvaluation(dirFiles);

		if (lntSpec == null)
		{
			throw new IllegalStateException("No LNT specification found in \"" + workingDir + "\".");
		}

		return lntSpec;
	}

	private static File retrieveLTLProperty(File workingDir)
	{
		for(File file : Objects.requireNonNull(workingDir.listFiles()))
		{
			if (file.getName().endsWith(".ltl"))
			{
				return file;
			}
		}

		throw new IllegalStateException("No LTL property found in \"" + workingDir + "\".");
	}

	private static File parseAndTransform(File workingDir,
										  File input) throws URISyntaxException
	{
		final String pifSchema = workingDir.getAbsolutePath() + File.separator + PIF_SCHEMA;
		final ContentHandler baseHandler = new BaseContentHandler(input);
		baseHandler.handle();
		final Process processOutput = (Process) baseHandler.getOutput();
		final String outputFileName = input.getParentFile().getAbsolutePath() + File.separator + processOutput.getId() + ".pif";
		final File outputFile = new File(outputFileName);
		final ContentTransformer baseTransformer = new BaseContentTransformer(processOutput, outputFile);
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

	private static void cleanDirBeforeEvaluation(File[] dirFiles)
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

	private static ArrayList<String> computeSpecLabels(File workingDir,
													   File lntSpec) throws IOException, InterruptedException
	{
		//Convert LNT spec to BCG
		final String lntOpenCommand = "lnt.open";
		final String[] lntOpenArgs = new String[]{"-silent", lntSpec.getName(), "generator", LNT_GENERIC_NAME + ".bcg"};
		final CommandManager lntOpenCommandManager = new CommandManager(lntOpenCommand, workingDir, lntOpenArgs);
		lntOpenCommandManager.execute();

		//Reduce LTS with weaktrace
		final String bcgOpenCommand = "bcg_open";
		final String[] bcgOpenArgs = new String[]{"process.bcg", "reductor", "-weaktrace", LNT_GENERIC_NAME + "_weaktraced.bcg"};
		final CommandManager bcgOpenCommandManager = new CommandManager(bcgOpenCommand, workingDir, bcgOpenArgs);
		bcgOpenCommandManager.execute();

		//Retrieve LTS labels
		final String bcgInfoCommand = "bcg_info";
		final String[] bcgInfoArgs = new String[]{"-labels", LNT_GENERIC_NAME + "_weaktraced.bcg"};
		final CommandManager bcgInfoCommandManager = new CommandManager(bcgInfoCommand, workingDir, bcgInfoArgs);
		bcgInfoCommandManager.execute();

		(new File(workingDir + File.separator + LNT_GENERIC_NAME + "_weaktraced.bcg")).delete();
		final String[] splitStdout = bcgInfoCommandManager.stdOut().split("\n");
		final ArrayList<String> labels = new ArrayList<>();

		for (int i = 1; i < splitStdout.length; i++)
		{
			final String label = splitStdout[i];
			final String trimmedLabel = Utils.trim(label);

			if (!trimmedLabel.equals("i"))
			{
				labels.add(trimmedLabel);
			}
		}

		System.out.println("Labels retrieved: \"" + labels + "\".");
		return labels;
	}

	private static void generateBuchiAutomata(File workingDir,
											  File ltlProperty) throws IOException, InterruptedException
	{
		final FileInputStream inputStream = new FileInputStream(workingDir.getAbsolutePath() + File.separator + ltlProperty.getName());
		final Scanner scanner = new Scanner(inputStream);
		final StringBuilder propertyBuilder = new StringBuilder();
		String separator = "";

		while(scanner.hasNextLine())
		{
			final String line = Utils.trim(scanner.nextLine());

			if (!line.isEmpty())
			{
				propertyBuilder.append(separator)
						.append(line);
				separator = " ";
			}
		}

		scanner.close();
		inputStream.close();

		final String negProperty = ltlProperty.getName().replace(".ltl", "") + "_neg.ltl";
		final PrintWriter ltlNegationPrintWriter = new PrintWriter(workingDir.getAbsolutePath() + File.separator + negProperty);
		ltlNegationPrintWriter.print("! ( ");
		ltlNegationPrintWriter.print(propertyBuilder);
		ltlNegationPrintWriter.print(" )");
		ltlNegationPrintWriter.flush();
		ltlNegationPrintWriter.close();

		final String ltl2tgbaCommand = "ltl2tgba";
		final String[] ltl2tgbaArgs = new String[]{"--file=" + negProperty, "--hoaf=t"};
		final CommandManager ltl2tgbaCommandManager = new CommandManager(ltl2tgbaCommand, workingDir, ltl2tgbaArgs);
		ltl2tgbaCommandManager.execute();

		System.out.println("Buchi automata:\n\n" + ltl2tgbaCommandManager.stdOut());

		final PrintWriter buchiAutomataPrintWriter = new PrintWriter(workingDir.getAbsolutePath() + File.separator + BUCHI_AUTOMATA);
		buchiAutomataPrintWriter.println(ltl2tgbaCommandManager.stdOut());
		buchiAutomataPrintWriter.flush();
		buchiAutomataPrintWriter.close();

		(new File(workingDir + File.separator + negProperty)).delete();
	}

	private static void generateSVLScript(File workingDir,
										  File lntSpecification,
										  ArrayList<String> labels) throws IOException, InterruptedException
	{
		final String svlCommand = "hoa2svl";
		final ArrayList<String> svlArgs = new ArrayList<>();
		svlArgs.add(BUCHI_AUTOMATA);
		svlArgs.add(lntSpecification.getName());
		svlArgs.addAll(labels);

		final CommandManager svlCommandManager = new CommandManager(svlCommand, workingDir, svlArgs);
		svlCommandManager.execute();
	}

	private static void executeSVLScript(File workingDir) throws IOException, InterruptedException
	{
		final String svlCommand = "svl";
		final String[] svlArgs = new String[]{SVL_SCRIPT_NAME};
		final CommandManager svlCommandManager = new CommandManager(svlCommand, workingDir, svlArgs);
		svlCommandManager.execute();

		final PrintWriter printWriter = new PrintWriter(workingDir + File.separator + RESULT_FILE_NAME);

		if (svlCommandManager.stdOut().contains("TRUE"))
		{
			System.out.println("Property was evaluated to \"TRUE\"!");
			printWriter.println("TRUE");
		}
		else
		{
			System.out.println("Property was evaluated to \"FALSE\"!");
			printWriter.println("FALSE");
		}

		printWriter.flush();
		printWriter.close();
	}

	private static File generateProperCounterexample(File workingDir) throws IOException, InterruptedException
	{
		//Minimize counterexample with divbranching to remove "i" transitions
		final String bcgMinCommand = "bcg_min";
		final String[] bcgMinArgs = new String[]{"-divbranching", COUNTEREXAMPLE_FILE + ".bcg", COUNTEREXAMPLE_FILE + "_div.bcg"};
		final CommandManager bcgMinCommandManager = new CommandManager(bcgMinCommand, workingDir, bcgMinArgs);
		bcgMinCommandManager.execute();

		//Convert example to AUT
		final String bcgIOCommand = "bcg_io";
		final String[] bcgIOArgs = new String[]{COUNTEREXAMPLE_FILE + "_div.bcg", COUNTEREXAMPLE_FILE + "_div.aut"};
		final CommandManager bcgIOCommandManager = new CommandManager(bcgIOCommand, workingDir, bcgIOArgs);
		bcgIOCommandManager.execute();

		return new File(workingDir.getAbsolutePath() + File.separator + COUNTEREXAMPLE_FILE + "_div.aut");
	}

	private static void finalClean(File workingDirectory)
	{
		for (File file : Objects.requireNonNull(workingDirectory.listFiles()))
		{
			if (file.getName().endsWith(".o")
				|| file.getName().endsWith(".ps")
				|| file.getName().equals("task.err#0")
				|| file.getName().equals("reductor"))
			{
				file.delete();
			}
		}
	}
}