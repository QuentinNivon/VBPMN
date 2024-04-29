package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.model.Process;
import fr.inria.convecs.optimus.nl_to_mc.exceptions.ExpectedException;
import fr.inria.convecs.optimus.parser.BaseContentHandler;
import fr.inria.convecs.optimus.parser.ContentHandler;
import fr.inria.convecs.optimus.py_to_java.Vbpmn;
import fr.inria.convecs.optimus.transformer.BaseContentTransformer;
import fr.inria.convecs.optimus.transformer.ContentTransformer;
import fr.inria.convecs.optimus.util.CommandManager;
import fr.inria.convecs.optimus.util.Utils;
import fr.inria.convecs.optimus.util.XmlUtil;
import jdk.internal.net.http.common.Pair;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Main
{
	public static final boolean LOCAL_TESTING = false;
	private static final String DUMMY_LOOPY_LABEL = "DUMMY_LOOPY_LABEL";
	private static final String LNT_GENERIC_NAME = "process";
	private static final String COUNTEREXAMPLE_FILE = "diag";
	private static final String BUCHI_AUTOMATA = "buchi.hoa";
	private static final String SVL_SCRIPT_NAME = "task.svl";
	private static final String TRUE_RESULT_FILE_NAME = "res_true.txt";
	private static final String FALSE_RESULT_FILE_NAME = "res_false.txt";
	private static final String LTL_PROPERTY = "property.ltl";
	private static final String ID_FILE = "id.lnt";
	private static final String BPMN_TYPES_FILE = "bpmntypes.lnt";
	private static final String PIF_SCHEMA = "pif.xsd";
	private static final String REMOTE_PIF_FILE_LOCATION = "/home/quentin_nivon/nl_to_mc/public/";
	private static final int TRANSLATION_TO_PIF_FAILED = 12;
	private static final int TRANSLATION_TO_LNT_FAILED = 13;
	private static final int PROPERTY_GENERATION_FAILED = 14;
	private static final int LNT_TO_BCG_FAILED = 15;
	private static final int WEAKTRACING_BCG_FAILED = 16;
	private static final int RETRIEVING_LABELS_FAILED = 17;
	private static final int READING_PROPERTY_FILE_FAILED = 18;
	private static final int WRITING_PROPERTY_NEGATION_FAILED = 19;
	private static final int TRANSLATING_PROPERTY_TO_BUCHI_AUTOMATA_FAILED = 20;
	private static final int WRITING_BUCHI_AUTOMATA_FILE_FAILED = 21;
	private static final int SVL_SCRIPT_GENERATION_FAILED = 22;
	private static final int SVL_SCRIPT_EXECUTION_FAILED = 23;
	private static final int WRITING_PROPERTY_EVALUATION_FILE_FAILED = 24;
	private static final int COUNTEREXAMPLE_DETERMINATION_FAILED = 25;
	private static final int COUNTEREXAMPLE_TO_AUT_FAILED = 26;
	private static final int AUT_TO_VIS_CONVERSION_FAILED = 27;
	private static final int WRITING_LTL_PROPERTY_FAILED = 28;
	private static final int UNEXPECTED_ERROR = 146548449;

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, ExpectedException
	{
		try
		{
			final CommandLineParser commandLineParser;

			try
			{
				commandLineParser = new CommandLineParser(args);
			}
			catch (FileNotFoundException e)
			{
				//throw new IllegalStateException("Some necessary files have not been found or are not valid.");
				System.exit(4);
				return;
			}

			final File workingDirectory = ((File) commandLineParser.get(CommandLineOption.WORKING_DIRECTORY));

			if (!workingDirectory.isDirectory())
			{
				//throw new IllegalStateException("Path \"" + workingDirectory.getAbsolutePath() + "\" does not point to a valid directory.");
				System.exit(4);
			}

			final File bpmnFile = (File) commandLineParser.get(CommandLineOption.BPMN_FILE);

			System.out.println("Generating PIF file...");
			final File pifFile = Main.parseAndTransform(workingDirectory, bpmnFile);

			if (pifFile == null)
			{
				System.exit(TRANSLATION_TO_PIF_FAILED);
			}

			System.out.println("PIF file generated.\n");

			System.out.println("Generating LNT file...");
			final File lntSpec = Main.generateLNT(workingDirectory, pifFile);

			if (lntSpec == null)
			{
				System.exit(TRANSLATION_TO_LNT_FAILED);
			}

			System.out.println("LNT file generated.\n");

			System.out.println("Retrieving LTL property...");
			final Pair<File, Integer> ltlPropertyAndReturnValue = Main.retrieveLTLProperty(
					workingDirectory,
					commandLineParser.get(CommandLineOption.TEMPORAL_PROPERTY),
					(String) commandLineParser.get(CommandLineOption.API_KEY)
			);

			if (ltlPropertyAndReturnValue.second != 0)
			{
				System.exit(ltlPropertyAndReturnValue.second);
			}
			System.out.println("LTL property retrieved.\n");

			System.out.println("Computing specification labels...");
			final Pair<ArrayList<String>, Integer> labelsAndReturnCode = Main.computeSpecLabels(workingDirectory, lntSpec);

			if (labelsAndReturnCode.second != 0)
			{
				System.exit(labelsAndReturnCode.second);
			}
			System.out.println("Labels computed: " + labelsAndReturnCode + ".\n");

			System.out.println("Generating Büchi automata...");
			final int buchiReturnValue = Main.generateBuchiAutomata(workingDirectory, ltlPropertyAndReturnValue.first);

			if (buchiReturnValue != 0)
			{
				System.exit(buchiReturnValue);
			}
			System.out.println("Büchi automata generated.\n");

			System.out.println("Generating the SVL script...");
			final int svlGenReturnValue = Main.generateSVLScript(workingDirectory, lntSpec, labelsAndReturnCode.first);

			if (svlGenReturnValue != 0)
			{
				System.exit(svlGenReturnValue);
			}
			System.out.println("SVL script generated.\n");

			System.out.println("Executing the SVL script...");
			final int svlExecReturnValue = Main.executeSVLScript(workingDirectory);

			if (svlExecReturnValue != 0)
			{
				System.exit(svlExecReturnValue);
			}
			System.out.println("SVL script executed.\n");

			System.out.println("Cleaning the counterexample...");
			final Pair<File, Integer> counterExample = Main.generateProperCounterexample(workingDirectory);

			if (counterExample.second != 0)
			{
				System.exit(counterExample.second);
			}
			System.out.println("Counterexample cleaned.\n");

			System.out.println("Converting counterexample to VIS format...");
			try
			{
				final Aut2Vis aut2Vis = new Aut2Vis(workingDirectory, counterExample.first, COUNTEREXAMPLE_FILE);
				final File visFile = aut2Vis.generateVisFile();
			}
			catch (IOException e)
			{
				System.exit(AUT_TO_VIS_CONVERSION_FAILED);
			}
			System.out.println("Counterexample converted.\n");

			System.out.println("Cleaning working directory...");
			Main.finalClean(workingDirectory);
			System.out.println("Directory cleaned.\n");
		}
		catch (Exception e)
		{
			System.exit(UNEXPECTED_ERROR);
		}
	}

	private static File generateLNT(File workingDir, File pifFile)
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
				|| file.getName().equals(TRUE_RESULT_FILE_NAME)
				|| file.getName().equals(FALSE_RESULT_FILE_NAME)
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

		//throw new IllegalStateException("No LNT specification found in \"" + workingDir + "\".");

		return lntSpec;
	}

	private static Pair<File, Integer> retrieveLTLProperty(final File workingDir,
														   final Object temporalLogicObject,
														   final String apiKey)
	{
		if (temporalLogicObject instanceof File)
		{
			//The temporal logic property has been uploaded: nothing to do.
			return new Pair<>((File) temporalLogicObject, 0);
		}
		else if (temporalLogicObject instanceof String)
		{
			//We got a description: ask GPT
			final String ltlProperty;

			try
			{
				ltlProperty = ChatGPTManager.generateAnswer((String) temporalLogicObject, apiKey);
			}
			catch (Exception e)
			{
				return new Pair<>(null, PROPERTY_GENERATION_FAILED);
			}

			System.out.println("Generated LTL property: " + ltlProperty);

			final File ltlPropertyFile = new File(workingDir.getAbsolutePath() + File.separator + LTL_PROPERTY);

			try
			{
				final PrintWriter printWriter = new PrintWriter(ltlPropertyFile);
				printWriter.println(ltlProperty);
				printWriter.flush();
				printWriter.close();
			}
			catch (FileNotFoundException e)
			{
				return new Pair<>(null, WRITING_LTL_PROPERTY_FAILED);
			}

			return new Pair<>(ltlPropertyFile, 0);
		}
		else
		{
			throw new IllegalStateException("Unsupported object type: " + temporalLogicObject.getClass().getName());
		}
	}

	private static File parseAndTransform(File workingDir,
										  File input)
	{
		final String pifSchema = REMOTE_PIF_FILE_LOCATION + File.separator + PIF_SCHEMA;
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
			//throw new RuntimeException("Unable to transform the file <Schema Validation Error>: " + input.getName());
			return null;
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

	private static Pair<ArrayList<String>, Integer> computeSpecLabels(File workingDir,
																	  File lntSpec)
	{
		//Convert LNT spec to BCG
		final String lntOpenCommand = "lnt.open";
		final String[] lntOpenArgs = new String[]{"-silent", lntSpec.getName(), "generator", LNT_GENERIC_NAME + ".bcg"};
		final CommandManager lntOpenCommandManager = new CommandManager(lntOpenCommand, workingDir, lntOpenArgs);

		try
		{
			lntOpenCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return new Pair<>(new ArrayList<>(), LNT_TO_BCG_FAILED);
		}

		if (lntOpenCommandManager.returnValue() != 0)
		{
			return new Pair<>(new ArrayList<>(), LNT_TO_BCG_FAILED);
		}

		//Reduce LTS with weaktrace
		final String bcgOpenCommand = "bcg_open";
		final String[] bcgOpenArgs = new String[]{"process.bcg", "reductor", "-weaktrace", LNT_GENERIC_NAME + "_weaktraced.bcg"};
		final CommandManager bcgOpenCommandManager = new CommandManager(bcgOpenCommand, workingDir, bcgOpenArgs);

		try
		{
			bcgOpenCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return new Pair<>(new ArrayList<>(), WEAKTRACING_BCG_FAILED);
		}

		if (bcgOpenCommandManager.returnValue() != 0)
		{
			return new Pair<>(new ArrayList<>(), WEAKTRACING_BCG_FAILED);
		}

		//Retrieve LTS labels
		final String bcgInfoCommand = "bcg_info";
		final String[] bcgInfoArgs = new String[]{"-labels", LNT_GENERIC_NAME + "_weaktraced.bcg"};
		final CommandManager bcgInfoCommandManager = new CommandManager(bcgInfoCommand, workingDir, bcgInfoArgs);

		try
		{
			bcgInfoCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return new Pair<>(new ArrayList<>(), RETRIEVING_LABELS_FAILED);
		}

		if (bcgInfoCommandManager.returnValue() != 0)
		{
			return new Pair<>(new ArrayList<>(), RETRIEVING_LABELS_FAILED);
		}

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
		return new Pair<>(labels, 0);
	}

	private static int generateBuchiAutomata(File workingDir,
											 File ltlProperty)
	{
		final StringBuilder propertyBuilder = new StringBuilder();
		final FileInputStream inputStream;

		try
		{
			inputStream = new FileInputStream(workingDir.getAbsolutePath() + File.separator + ltlProperty.getName());
			final Scanner scanner = new Scanner(inputStream);
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
		}
		catch (IOException e)
		{
			return READING_PROPERTY_FILE_FAILED;
		}

		final String negProperty = ltlProperty.getName().replace(".ltl", "") + "_neg.ltl";
		try
		{
			final PrintWriter ltlNegationPrintWriter = new PrintWriter(workingDir.getAbsolutePath() + File.separator + negProperty);
			ltlNegationPrintWriter.print("! ( ");
			ltlNegationPrintWriter.print(propertyBuilder);
			ltlNegationPrintWriter.print(" )");
			ltlNegationPrintWriter.flush();
			ltlNegationPrintWriter.close();
		}
		catch (FileNotFoundException e)
		{
			return WRITING_PROPERTY_NEGATION_FAILED;
		}

		final String ltl2tgbaCommand = "ltl2tgba";
		final String[] ltl2tgbaArgs = new String[]{"--file=" + negProperty, "--hoaf=t"};
		final CommandManager ltl2tgbaCommandManager = new CommandManager(ltl2tgbaCommand, workingDir, ltl2tgbaArgs);

		try
		{
			ltl2tgbaCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return TRANSLATING_PROPERTY_TO_BUCHI_AUTOMATA_FAILED;
		}

		System.out.println("Buchi automata:\n\n" + ltl2tgbaCommandManager.stdOut());

		try
		{
			final PrintWriter buchiAutomataPrintWriter = new PrintWriter(workingDir.getAbsolutePath() + File.separator + BUCHI_AUTOMATA);
			buchiAutomataPrintWriter.println(ltl2tgbaCommandManager.stdOut());
			buchiAutomataPrintWriter.flush();
			buchiAutomataPrintWriter.close();
		}
		catch (FileNotFoundException e)
		{
			return WRITING_BUCHI_AUTOMATA_FILE_FAILED;
		}

		(new File(workingDir + File.separator + negProperty)).delete();

		return 0;
	}

	private static int generateSVLScript(File workingDir,
										 File lntSpecification,
										 ArrayList<String> labels)
	{
		final String svlCommand = "hoa2svl";
		final ArrayList<String> svlArgs = new ArrayList<>();
		svlArgs.add(BUCHI_AUTOMATA);
		svlArgs.add(lntSpecification.getName());
		svlArgs.addAll(labels);

		final CommandManager svlCommandManager = new CommandManager(svlCommand, workingDir, svlArgs);

		try
		{
			svlCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return SVL_SCRIPT_GENERATION_FAILED;
		}

		return 0;
	}

	private static int executeSVLScript(File workingDir)
	{
		final String svlCommand = "svl";
		final String[] svlArgs = new String[]{SVL_SCRIPT_NAME};
		final CommandManager svlCommandManager = new CommandManager(svlCommand, workingDir, svlArgs);

		try
		{
			svlCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return SVL_SCRIPT_EXECUTION_FAILED;
		}

		final PrintWriter printWriter;

		try
		{
			if (svlCommandManager.stdOut().contains("TRUE"))
			{
				System.out.println("Property was evaluated to \"TRUE\"!");
				printWriter = new PrintWriter(workingDir + File.separator + TRUE_RESULT_FILE_NAME);
				printWriter.println(svlCommandManager.stdOut());
			}
			else
			{
				System.out.println("Property was evaluated to \"FALSE\"!");
				printWriter = new PrintWriter(workingDir + File.separator + FALSE_RESULT_FILE_NAME);
				printWriter.println(svlCommandManager.stdOut());
			}
		}
		catch (FileNotFoundException e)
		{
			return WRITING_PROPERTY_EVALUATION_FILE_FAILED;
		}

		printWriter.flush();
		printWriter.close();

		return 0;
	}

	private static Pair<File, Integer> generateProperCounterexample(File workingDir)
	{
		//Minimize counterexample with divbranching to remove "i" transitions
		final String bcgMinCommand = "bcg_min";
		final String[] bcgMinArgs = new String[]{"-divbranching", COUNTEREXAMPLE_FILE + ".bcg", COUNTEREXAMPLE_FILE + "_div.bcg"};
		final CommandManager bcgMinCommandManager = new CommandManager(bcgMinCommand, workingDir, bcgMinArgs);

		try
		{
			bcgMinCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return new Pair<>(null, COUNTEREXAMPLE_DETERMINATION_FAILED);
		}

		//Convert example to AUT
		final String bcgIOCommand = "bcg_io";
		final String[] bcgIOArgs = new String[]{COUNTEREXAMPLE_FILE + "_div.bcg", COUNTEREXAMPLE_FILE + "_div.aut"};
		final CommandManager bcgIOCommandManager = new CommandManager(bcgIOCommand, workingDir, bcgIOArgs);

		try
		{
			bcgIOCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return new Pair<>(null, COUNTEREXAMPLE_TO_AUT_FAILED);
		}

		return new Pair<>(new File(workingDir.getAbsolutePath() + File.separator + COUNTEREXAMPLE_FILE + "_div.aut"), 0);
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