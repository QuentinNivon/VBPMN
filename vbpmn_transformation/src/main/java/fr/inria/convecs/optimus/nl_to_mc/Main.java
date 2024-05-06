package fr.inria.convecs.optimus.nl_to_mc;

import com.sun.tools.jdeprscan.scan.Scan;
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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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
	private static final String TEMPORARY_COUNTEREXAMPLE = COUNTEREXAMPLE_FILE + ".tmp";
	private static final String VBPMN_COUNTEREXAMPLE_FILE = "evaluator.bcg";
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

			System.out.println("Retrieving temporal logic property...");
			final long propertyRetrievingStartTime = System.nanoTime();
			final Pair<File, Integer> ltlPropertyAndReturnValue = Main.retrieveLTLProperty(
					workingDirectory,
					commandLineParser.get(CommandLineOption.TEMPORAL_PROPERTY),
					(String) commandLineParser.get(CommandLineOption.API_KEY)
			);

			if (ltlPropertyAndReturnValue.getRight() != 0)
			{
				System.exit(ltlPropertyAndReturnValue.getRight());
			}
			final long propertyRetrievingEndTime = System.nanoTime();
			final long propertyRetrievingTime = propertyRetrievingEndTime - propertyRetrievingStartTime;
			System.out.println("Temporal logic property retrieved in " + Utils.nanoSecToReadable(propertyRetrievingTime) + "\n");

			System.out.println("Generating PIF file...");
			final long pifFileGenerationStartTime = System.nanoTime();
			final File pifFile = Main.parseAndTransform(workingDirectory, bpmnFile);

			if (pifFile == null)
			{
				System.exit(TRANSLATION_TO_PIF_FAILED);
			}

			final long pifFileGenerationEndTime = System.nanoTime();
			final long pifFileGenerationTime = pifFileGenerationEndTime - pifFileGenerationStartTime;
			System.out.println("PIF file generated in " + Utils.nanoSecToReadable(pifFileGenerationTime) + ".\n");

			System.out.println("Generating LNT file...");
			final long lntFileGenerationStartTime = System.nanoTime();
			final Triple<File, Boolean, Integer> lntSpecAndEvaluation = Main.generateLNT(workingDirectory, pifFile, ltlPropertyAndReturnValue.getLeft());

			if (lntSpecAndEvaluation.getRight() != 0)
			{
				System.exit(lntSpecAndEvaluation.getRight());
			}

			final long lntFileGenerationEndTime = System.nanoTime();
			final long lntFileGenerationTime = lntFileGenerationEndTime - lntFileGenerationStartTime;
			System.out.println("LNT file generated in " + Utils.nanoSecToReadable(lntFileGenerationTime) + ".\n");

			if (lntSpecAndEvaluation.getMiddle() == null)
			{
				//The property is not written in MCL
				System.out.println("Computing specification labels...");
				final long labelsComputationStartTime = System.nanoTime();
				final Pair<ArrayList<String>, Integer> labelsAndReturnCode = Main.computeSpecLabels(workingDirectory, lntSpecAndEvaluation.getLeft());

				if (labelsAndReturnCode.getRight() != 0)
				{
					System.exit(labelsAndReturnCode.getRight());
				}

				final long labelsComputationEndTime = System.nanoTime();
				final long labelsComputationTime = labelsComputationEndTime - labelsComputationStartTime;
				System.out.println("Labels \"" + labelsAndReturnCode.getLeft() + "\" computed in " + Utils.nanoSecToReadable(labelsComputationTime) + ".\n");

				System.out.println("Generating Büchi automata...");
				final long buchiAutomataGenerationStartTime = System.nanoTime();
				final int buchiReturnValue = Main.generateBuchiAutomata(workingDirectory, ltlPropertyAndReturnValue.getLeft());

				if (buchiReturnValue != 0)
				{
					System.exit(buchiReturnValue);
				}

				final long buchiAutomataGenerationEndTime = System.nanoTime();
				final long buchiAutomataGenerationTime = buchiAutomataGenerationEndTime - buchiAutomataGenerationStartTime;
				System.out.println("Büchi automata generated in " + Utils.nanoSecToReadable(buchiAutomataGenerationTime) + ".\n");

				System.out.println("Generating the SVL script...");
				final long svlScriptGenerationStartTime = System.nanoTime();
				final int svlGenReturnValue = Main.generateSVLScript(workingDirectory, lntSpecAndEvaluation.getLeft(), labelsAndReturnCode.getLeft());

				if (svlGenReturnValue != 0)
				{
					System.exit(svlGenReturnValue);
				}

				final long svlScriptGenerationEndTime = System.nanoTime();
				final long svlScriptGenerationTime = svlScriptGenerationEndTime - svlScriptGenerationStartTime;
				System.out.println("SVL script generated in " + Utils.nanoSecToReadable(svlScriptGenerationTime) + ".\n");

				System.out.println("Executing the SVL script...");
				final long svlScriptExecutionStartTime = System.nanoTime();
				final int svlExecReturnValue = Main.executeSVLScript(workingDirectory);

				if (svlExecReturnValue != 0)
				{
					System.exit(svlExecReturnValue);
				}

				final long svlScriptExecutionEndTime = System.nanoTime();
				final long svlScriptExecutionTime = svlScriptExecutionEndTime - svlScriptExecutionStartTime;
				System.out.println("SVL script executed in " + Utils.nanoSecToReadable(svlScriptExecutionTime) + ".\n");
			}

			System.out.println("Cleaning the counterexample...");
			final long cleaningCounterExampleStartTime = System.nanoTime();
			final Pair<File, Integer> counterExample = Main.generateProperCounterexample(workingDirectory);

			if (counterExample.getRight() != 0)
			{
				System.exit(counterExample.getRight());
			}
			final long cleaningCounterExampleEndTime = System.nanoTime();
			final long cleaningCounterExampleTime = cleaningCounterExampleEndTime - cleaningCounterExampleStartTime;
			System.out.println("Counterexample cleaned in " + Utils.nanoSecToReadable(cleaningCounterExampleTime) + ".\n");

			System.out.println("Converting counterexample to VIS format...");
			final long convertingCounterExampleStartTime = System.nanoTime();
			try
			{
				final Aut2Vis aut2Vis = new Aut2Vis(workingDirectory, counterExample.getLeft(), COUNTEREXAMPLE_FILE);
				final File visFile = aut2Vis.generateVisFile();
			}
			catch (IOException e)
			{
				System.exit(AUT_TO_VIS_CONVERSION_FAILED);
			}
			final long convertingCounterExampleEndTime = System.nanoTime();
			final long convertingCounterExampleTime = convertingCounterExampleEndTime - convertingCounterExampleStartTime;
			System.out.println("Counterexample converted in " + Utils.nanoSecToReadable(convertingCounterExampleTime) + ".\n");

			System.out.println("Cleaning working directory...");
			Main.finalClean(workingDirectory);
			System.out.println("Directory cleaned.\n");
		}
		catch (Exception e)
		{
			System.exit(UNEXPECTED_ERROR);
		}
	}

	private static Triple<File, Boolean, Integer> generateLNT(final File workingDir,
															  final File pifFile,
															  final File temporalLogicProperty)
	{
		final String mclProperty;

		if (temporalLogicProperty.getName().endsWith(".mcl"))
		{
			//The property can be evaluated directly using VBPMN
			final StringBuilder mclPropertyBuilder = new StringBuilder();

			try
			{
				final FileInputStream inputStream = new FileInputStream(temporalLogicProperty);
				final Scanner scanner = new Scanner(inputStream);

				while (scanner.hasNextLine())
				{
					mclPropertyBuilder.append(scanner.nextLine())
							.append(" ");
				}

				scanner.close();
				inputStream.close();

				mclProperty = mclPropertyBuilder.toString();
			}
			catch (IOException e)
			{
				return Triple.of(null, null, READING_PROPERTY_FILE_FAILED);
			}
		}
		else
		{
			mclProperty = null;
		}

		if (mclProperty == null)
		{
			try
			{
				final Vbpmn vbpmn = new Vbpmn(new String[]{
						pifFile.getAbsolutePath(),
						pifFile.getAbsolutePath(),
						"property-implied",
						"--formula",
						"false"
				}, workingDir.getAbsolutePath(), false);
				vbpmn.execute();
			}
			catch (Error e)
			{
				return Triple.of(null, null, TRANSLATION_TO_LNT_FAILED);
			}

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
			return Triple.of(lntSpec, null, 0);
		}
		else
		{
			//Launch VBPMN with the MCL formula directly
			final boolean result;

			try
			{
				final Vbpmn vbpmn = new Vbpmn(new String[]{
						pifFile.getAbsolutePath(),
						pifFile.getAbsolutePath(),
						"property-implied",
						"--formula",
						mclProperty
				}, workingDir.getAbsolutePath());
				result = vbpmn.execute();
			}
			catch (Error e)
			{
				return Triple.of(null, null, SVL_SCRIPT_EXECUTION_FAILED);
			}

			final File currentCounterExample = new File(workingDir.getAbsolutePath() + File.separator + VBPMN_COUNTEREXAMPLE_FILE);
			final File tempCounterExample = new File(workingDir.getAbsolutePath() + File.separator + TEMPORARY_COUNTEREXAMPLE);

			if (!result)
			{
				//Property was evaluated to FALSE => Keep the counter-example
				final boolean renameSucceeded = currentCounterExample.renameTo(tempCounterExample);
			}

			final File[] dirFiles = workingDir.listFiles();

			assert dirFiles != null;

			for (File file : dirFiles)
			{
				if (file.getName().endsWith(".pif")
					|| file.getName().equals(TRUE_RESULT_FILE_NAME)
					|| file.getName().equals(FALSE_RESULT_FILE_NAME)
					|| file.getName().equals("time.txt")
					|| file.getName().equals("evaluator4"))
				{
					file.delete();
				}
			}

			Main.cleanDirBeforeEvaluation(dirFiles);

			final PrintWriter printWriter;
			final File counterExample;

			try
			{
				if (result)
				{
					System.out.println("Property was evaluated to \"TRUE\"!");
					printWriter = new PrintWriter(workingDir + File.separator + TRUE_RESULT_FILE_NAME);
					printWriter.println("true");
					counterExample = null;
				}
				else
				{
					System.out.println("Property was evaluated to \"FALSE\"!");
					printWriter = new PrintWriter(workingDir + File.separator + FALSE_RESULT_FILE_NAME);
					printWriter.println("false");
					counterExample = new File(workingDir.getAbsolutePath() + File.separator + COUNTEREXAMPLE_FILE + ".bcg");
					final boolean renameWorked = tempCounterExample.renameTo(counterExample);
				}
			}
			catch (FileNotFoundException e)
			{
				return Triple.of(null, null, WRITING_PROPERTY_EVALUATION_FILE_FAILED);
			}

			printWriter.flush();
			printWriter.close();

			return Triple.of(counterExample, result, 0);
		}
	}

	private static Pair<File, Integer> retrieveLTLProperty(final File workingDir,
														   final Object temporalLogicObject,
														   final String apiKey)
	{
		if (temporalLogicObject instanceof File)
		{
			//The temporal logic property has been uploaded: nothing to do.
			return Pair.of((File) temporalLogicObject, 0);
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
				return Pair.of(null, PROPERTY_GENERATION_FAILED);
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
				return Pair.of(null, WRITING_LTL_PROPERTY_FAILED);
			}

			return Pair.of(ltlPropertyFile, 0);
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
			return Pair.of(new ArrayList<>(), LNT_TO_BCG_FAILED);
		}

		if (lntOpenCommandManager.returnValue() != 0)
		{
			return Pair.of(new ArrayList<>(), LNT_TO_BCG_FAILED);
		}

		//Retrieve LTS labels
		final String bcgInfoCommand = "bcg_info";
		final String[] bcgInfoArgs = new String[]{"-labels", LNT_GENERIC_NAME + ".bcg"};
		final CommandManager bcgInfoCommandManager = new CommandManager(bcgInfoCommand, workingDir, bcgInfoArgs);

		try
		{
			bcgInfoCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return Pair.of(new ArrayList<>(), RETRIEVING_LABELS_FAILED);
		}

		if (bcgInfoCommandManager.returnValue() != 0)
		{
			return Pair.of(new ArrayList<>(), RETRIEVING_LABELS_FAILED);
		}

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

		return Pair.of(labels, 0);
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

		if (ltl2tgbaCommandManager.returnValue() != 0)
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

		if (svlCommandManager.returnValue() != 0)
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

		if (svlCommandManager.returnValue() != 0)
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
		if (!new File(workingDir.getAbsolutePath() + File.separator + COUNTEREXAMPLE_FILE + ".bcg").exists())
		{
			return Pair.of(null, 0);
		}

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
			return Pair.of(null, COUNTEREXAMPLE_DETERMINATION_FAILED);
		}

		if (bcgMinCommandManager.returnValue() != 0)
		{
			return Pair.of(null, COUNTEREXAMPLE_DETERMINATION_FAILED);
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
			return Pair.of(null, COUNTEREXAMPLE_TO_AUT_FAILED);
		}

		if (bcgIOCommandManager.returnValue() != 0)
		{
			return Pair.of(null, COUNTEREXAMPLE_TO_AUT_FAILED);
		}

		return Pair.of(new File(workingDir.getAbsolutePath() + File.separator + COUNTEREXAMPLE_FILE + "_div.aut"), 0);
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