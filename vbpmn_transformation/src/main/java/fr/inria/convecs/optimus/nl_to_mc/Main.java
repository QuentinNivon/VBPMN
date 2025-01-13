package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutParser;
import fr.inria.convecs.optimus.bpmn.BpmnParser;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessObject;
import fr.inria.convecs.optimus.bpmn.types.process.Task;
import fr.inria.convecs.optimus.model.Process;
import fr.inria.convecs.optimus.nl_to_mc.exceptions.ExpectedException;
import fr.inria.convecs.optimus.parser.BaseContentHandler;
import fr.inria.convecs.optimus.parser.ContentHandler;
import fr.inria.convecs.optimus.py_to_java.ReturnCodes;
import fr.inria.convecs.optimus.py_to_java.Vbpmn;
import fr.inria.convecs.optimus.transformer.BaseContentTransformer;
import fr.inria.convecs.optimus.transformer.ContentTransformer;
import fr.inria.convecs.optimus.util.CommandManager;
import fr.inria.convecs.optimus.util.Utils;
import fr.inria.convecs.optimus.util.XmlUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class Main
{
	public static final boolean OLD_WEBSITE = false;
	public static final boolean LOCAL_SITE = true;
	public static final boolean LOCAL_TESTING = false;
	private static final int BCG_FILE_REDUCTION_THRESHOLD = 1000;

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, ExpectedException
	{
		CommandLineParser commandLineParser = null;

		try
		{
			final long programStartTime = System.nanoTime();

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
			MyOwnLogger.append("Retrieving temporal logic property...");
			final long propertyRetrievingStartTime = System.nanoTime();
			final Pair<File, Integer> ltlPropertyAndReturnValue = Main.retrieveLTLProperty(
					workingDirectory,
					commandLineParser.get(CommandLineOption.TEMPORAL_PROPERTY),
					(String) commandLineParser.get(CommandLineOption.API_KEY)
			);

			if (ltlPropertyAndReturnValue.getRight() != ReturnCodes.TERMINATION_OK)
			{
				MyOwnLogger.append("Temporal logic property retrieval failed.");
				System.out.println("Temporal logic property retrieval failed.");
				System.exit(ltlPropertyAndReturnValue.getRight());
			}
			final long propertyRetrievingEndTime = System.nanoTime();
			final long propertyRetrievingTime = propertyRetrievingEndTime - propertyRetrievingStartTime;
			MyOwnLogger.append("Temporal logic property retrieved in " + Utils.nanoSecToReadable(propertyRetrievingTime) + "\n");
			System.out.println("Temporal logic property retrieved in " + Utils.nanoSecToReadable(propertyRetrievingTime) + "\n");

			System.out.println("Generating PIF file...");
			MyOwnLogger.append("Generating PIF file...");
			final long pifFileGenerationStartTime = System.nanoTime();
			final File pifFile = Main.parseAndTransform(bpmnFile);

			if (pifFile == null)
			{
				MyOwnLogger.append("The BPMN process could not be translated to the PIF format.");
				System.out.println("The BPMN process could not be translated to the PIF format.");
				System.exit(ReturnCodes.TRANSLATION_TO_PIF_FAILED);
			}

			final long pifFileGenerationEndTime = System.nanoTime();
			final long pifFileGenerationTime = pifFileGenerationEndTime - pifFileGenerationStartTime;
			MyOwnLogger.append("PIF file generated in " + Utils.nanoSecToReadable(pifFileGenerationTime) + ".\n");
			System.out.println("PIF file generated in " + Utils.nanoSecToReadable(pifFileGenerationTime) + ".\n");

			System.out.println("Generating LNT file...");
			MyOwnLogger.append("Generating LNT file...");
			final long lntFileGenerationStartTime = System.nanoTime();
			final Triple<File, Boolean, Integer> lntSpecAndEvaluation = Main.generateLNT(workingDirectory, pifFile, ltlPropertyAndReturnValue.getLeft());

			if (lntSpecAndEvaluation.getRight() != ReturnCodes.TERMINATION_OK)
			{
				MyOwnLogger.append("The generation of the LNT file failed.");
				System.out.println("The generation of the LNT file failed.");
				System.exit(lntSpecAndEvaluation.getRight());
			}

			final long lntFileGenerationEndTime = System.nanoTime();
			final long lntFileGenerationTime = lntFileGenerationEndTime - lntFileGenerationStartTime;
			MyOwnLogger.append("LNT file generated in " + Utils.nanoSecToReadable(lntFileGenerationTime) + ".\n");
			System.out.println("LNT file generated in " + Utils.nanoSecToReadable(lntFileGenerationTime) + ".\n");

			if (lntSpecAndEvaluation.getMiddle() == null)
			{
				//Generate the BCG of the LNT spec
				System.out.println("Generating BCG file...");
				MyOwnLogger.append("Generating BCG file...");
				final long bcgFileGenerationStartTime = System.nanoTime();
				final int bcgFileGenerationReturnCode = Main.generateBCGFile(lntSpecAndEvaluation.getLeft(), workingDirectory);

				if (bcgFileGenerationReturnCode != ReturnCodes.TERMINATION_OK)
				{
					MyOwnLogger.append("The generation of the BCG file failed.");
					System.out.println("The generation of the BCG file failed.");
					System.exit(bcgFileGenerationReturnCode);
				}

				final long bcgFileGenerationEndTime = System.nanoTime();
				final long bcgFileGenerationTime = bcgFileGenerationEndTime - bcgFileGenerationStartTime;
				MyOwnLogger.append("BCG file generated in " + Utils.nanoSecToReadable(bcgFileGenerationTime) + ".\n");
				System.out.println("BCG file generated in " + Utils.nanoSecToReadable(bcgFileGenerationTime) + ".\n");

				//Get the size of the BCG file (CANNOT FAIL)
				System.out.println("Retrieving BCG file size...");
				MyOwnLogger.append("Retrieving BCG file size...");
				final long bcgFileSizeRetrievalStartTime = System.nanoTime();
				final int bcgFileSize = Main.retrieveBCGFileSize(workingDirectory, Filename.BCG_SPEC_FILE_NAME);
				final boolean performReduction = bcgFileSize > BCG_FILE_REDUCTION_THRESHOLD;
				final long bcgFileSizeRetrievalEndTime = System.nanoTime();
				final long bcgFileSizeRetrievalTime = bcgFileSizeRetrievalEndTime - bcgFileSizeRetrievalStartTime;
				MyOwnLogger.append("BCG file size retrieved in " + Utils.nanoSecToReadable(bcgFileSizeRetrievalTime) + ".\n");
				System.out.println("BCG file size retrieved in " + Utils.nanoSecToReadable(bcgFileSizeRetrievalTime) + ".\n");

				//The property is not written in MCL
				System.out.println("Computing specification labels...");
				MyOwnLogger.append("Computing specification labels...");
				final long labelsComputationStartTime = System.nanoTime();
				final Pair<ArrayList<String>, Integer> labelsAndReturnCode = Main.computeSpecLabels(bpmnFile);

				if (labelsAndReturnCode.getRight() != ReturnCodes.TERMINATION_OK)
				{
					MyOwnLogger.append("The generation of the BCG file failed.");
					System.out.println("The generation of the BCG file failed.");
					System.exit(labelsAndReturnCode.getRight());
				}

				final long labelsComputationEndTime = System.nanoTime();
				final long labelsComputationTime = labelsComputationEndTime - labelsComputationStartTime;
				MyOwnLogger.append("Labels \"" + labelsAndReturnCode.getLeft() + "\" computed in " + Utils.nanoSecToReadable(labelsComputationTime) + ".\n");
				System.out.println("Labels \"" + labelsAndReturnCode.getLeft() + "\" computed in " + Utils.nanoSecToReadable(labelsComputationTime) + ".\n");

				System.out.println("Generating Büchi automata...");
				MyOwnLogger.append("Generating Büchi automata...");
				final long buchiAutomataGenerationStartTime = System.nanoTime();
				final Pair<String, Integer> buchiAutomataAndReturnValue = Main.generateBuchiAutomata(workingDirectory, ltlPropertyAndReturnValue.getLeft());

				if (buchiAutomataAndReturnValue.getRight() != ReturnCodes.TERMINATION_OK)
				{
					MyOwnLogger.append("The generation of the Büchi automata failed.");
					System.out.println("The generation of the Büchi automata failed.");
					System.exit(buchiAutomataAndReturnValue.getRight());
				}

				final long buchiAutomataGenerationEndTime = System.nanoTime();
				final long buchiAutomataGenerationTime = buchiAutomataGenerationEndTime - buchiAutomataGenerationStartTime;
				MyOwnLogger.append("Büchi automata generated in " + Utils.nanoSecToReadable(buchiAutomataGenerationTime) + ".\n");
				System.out.println("Büchi automata generated in " + Utils.nanoSecToReadable(buchiAutomataGenerationTime) + ".\n");

				System.out.println("Verifying property labels...");
				MyOwnLogger.append("Verifying property labels...");
				final long propertyLabelsVerificationStartTime = System.nanoTime();
				final int exitCode = Main.retrieveAndVerifyPropertyLabels(workingDirectory, labelsAndReturnCode.getLeft(), buchiAutomataAndReturnValue.getLeft());

				if (exitCode != ReturnCodes.TERMINATION_OK)
				{
					MyOwnLogger.append("The verification of the property labels failed.");
					System.out.println("The verification of the property labels failed.");
					System.exit(exitCode);
				}

				final long propertyLabelsVerificationEndTime = System.nanoTime();
				final long propertyLabelsVerificationTime = propertyLabelsVerificationEndTime - propertyLabelsVerificationStartTime;
				MyOwnLogger.append("Property labels verified in " + Utils.nanoSecToReadable(propertyLabelsVerificationTime) + ".\n");
				System.out.println("Property labels verified in " + Utils.nanoSecToReadable(propertyLabelsVerificationTime) + ".\n");

				System.out.println("Generating the SVL script...");
				MyOwnLogger.append("Generating the SVL script...");
				final long svlScriptGenerationStartTime = System.nanoTime();
				final int svlGenReturnValue = Main.generateSVLScript(workingDirectory, lntSpecAndEvaluation.getLeft(), labelsAndReturnCode.getLeft(), performReduction);

				if (svlGenReturnValue != ReturnCodes.TERMINATION_OK)
				{
					MyOwnLogger.append("The generation of the SVL script failed.");
					System.out.println("The generation of the SVL script failed.");
					System.exit(svlGenReturnValue);
				}

				final long svlScriptGenerationEndTime = System.nanoTime();
				final long svlScriptGenerationTime = svlScriptGenerationEndTime - svlScriptGenerationStartTime;
				MyOwnLogger.append("SVL script generated in " + Utils.nanoSecToReadable(svlScriptGenerationTime) + ".\n");
				System.out.println("SVL script generated in " + Utils.nanoSecToReadable(svlScriptGenerationTime) + ".\n");

				System.out.println("Executing the SVL script...");
				MyOwnLogger.append("Executing the SVL script...");
				final long svlScriptExecutionStartTime = System.nanoTime();
				final int svlExecReturnValue = Main.executeSVLScript(workingDirectory);

				if (svlExecReturnValue != ReturnCodes.TERMINATION_OK)
				{
					MyOwnLogger.append("The execution of the SVL script failed.");
					System.out.println("The execution of the SVL script failed.");
					System.exit(svlExecReturnValue);
				}

				final long svlScriptExecutionEndTime = System.nanoTime();
				final long svlScriptExecutionTime = svlScriptExecutionEndTime - svlScriptExecutionStartTime;
				MyOwnLogger.append("SVL script executed in " + Utils.nanoSecToReadable(svlScriptExecutionTime) + ".\n");
				System.out.println("SVL script executed in " + Utils.nanoSecToReadable(svlScriptExecutionTime) + ".\n");
			}

			System.out.println("Cleaning the counterexample...");
			MyOwnLogger.append("Cleaning the counterexample...");
			final long cleaningCounterExampleStartTime = System.nanoTime();
			final Pair<File, Integer> counterExample = Main.generateProperCounterexample(workingDirectory);

			if (counterExample.getRight() != ReturnCodes.TERMINATION_OK)
			{
				MyOwnLogger.append("The cleaning of the counterexample failed.");
				System.out.println("The cleaning of the counterexample failed.");
				System.exit(counterExample.getRight());
			}
			final long cleaningCounterExampleEndTime = System.nanoTime();
			final long cleaningCounterExampleTime = cleaningCounterExampleEndTime - cleaningCounterExampleStartTime;
			MyOwnLogger.append("Counterexample cleaned in " + Utils.nanoSecToReadable(cleaningCounterExampleTime) + ".\n");
			System.out.println("Counterexample cleaned in " + Utils.nanoSecToReadable(cleaningCounterExampleTime) + ".\n");

			System.out.println("Converting counterexample to VIS format...");
			MyOwnLogger.append("Converting counterexample to VIS format...");
			final long convertingCounterExampleStartTime = System.nanoTime();
			try
			{
				final Aut2Vis aut2Vis = new Aut2Vis(workingDirectory, counterExample.getLeft(), Filename.COUNTEREXAMPLE_FILE);
				final File visFile = aut2Vis.generateVisFile();
			}
			catch (IOException e)
			{
				System.out.println("The translation of the counter-example to VIS failed.");
				MyOwnLogger.append("The translation of the counter-example to VIS failed.");
				System.exit(ReturnCodes.AUT_TO_VIS_CONVERSION_FAILED);
			}
			final long convertingCounterExampleEndTime = System.nanoTime();
			final long convertingCounterExampleTime = convertingCounterExampleEndTime - convertingCounterExampleStartTime;
			MyOwnLogger.append("Counterexample converted in " + Utils.nanoSecToReadable(convertingCounterExampleTime) + ".\n");
			System.out.println("Counterexample converted in " + Utils.nanoSecToReadable(convertingCounterExampleTime) + ".\n");

			System.out.println("Cleaning working directory...");
			MyOwnLogger.append("Cleaning working directory...");
			Main.finalClean(workingDirectory);
			System.out.println("Directory cleaned.\n");
			MyOwnLogger.append("Directory cleaned.\n");

			final long programEndTime = System.nanoTime();
			final long programTime = programEndTime - programStartTime;
			System.out.println("Generation and verification of the property took " + Utils.nanoSecToReadable(programTime) + ".\n");
			MyOwnLogger.append("Generation and verification of the property took " + Utils.nanoSecToReadable(programTime) + ".\n");
		}
		catch (Exception e)
		{
			if (commandLineParser != null)
			{
				MyOwnLogger.writeStdOut((File) commandLineParser.get(CommandLineOption.WORKING_DIRECTORY));
				MyOwnLogger.writeStdErr((File) commandLineParser.get(CommandLineOption.WORKING_DIRECTORY), Utils.getStackTrace(e));
			}
			throw e;
		}

		MyOwnLogger.writeStdOut((File) commandLineParser.get(CommandLineOption.WORKING_DIRECTORY));
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
				return Triple.of(null, null, ReturnCodes.READING_PROPERTY_FILE_FAILED);
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
				return Triple.of(null, null, ReturnCodes.TRANSLATION_TO_LNT_FAILED);
			}

			final File[] dirFiles = workingDir.listFiles();

			assert dirFiles != null;

			File lntSpec = null;

			for (File file : dirFiles)
			{
				if (file.getName().endsWith(".mcl")
						|| file.getName().endsWith(".pif")
						|| file.getName().equals(Filename.TRUE_RESULT_FILE_NAME)
						|| file.getName().equals(Filename.FALSE_RESULT_FILE_NAME)
						|| file.getName().equals("time.txt")
						|| file.getName().equals("evaluator4"))
				{
					file.delete();
				}
				else if (file.getName().endsWith(".lnt"))
				{
					if (!file.getName().equals(Filename.BPMN_TYPES_FILE)
							&& !file.getName().equals(Filename.ID_FILE))
					{
						lntSpec = file;
					}
				}
			}

			Main.cleanDirBeforeEvaluation(dirFiles);
			return Triple.of(lntSpec, null, ReturnCodes.TERMINATION_OK);
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
				return Triple.of(null, null, ReturnCodes.SVL_SCRIPT_EXECUTION_FAILED);
			}

			final File currentCounterExample = new File(workingDir.getAbsolutePath() + File.separator + Filename.VBPMN_COUNTEREXAMPLE_FILE);
			final File tempCounterExample = new File(workingDir.getAbsolutePath() + File.separator + Filename.TEMPORARY_COUNTEREXAMPLE);

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
					|| file.getName().equals(Filename.TRUE_RESULT_FILE_NAME)
					|| file.getName().equals(Filename.FALSE_RESULT_FILE_NAME)
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
					printWriter = new PrintWriter(workingDir + File.separator + Filename.TRUE_RESULT_FILE_NAME);
					printWriter.println("true");
					counterExample = null;
				}
				else
				{
					System.out.println("Property was evaluated to \"FALSE\"!");
					printWriter = new PrintWriter(workingDir + File.separator + Filename.FALSE_RESULT_FILE_NAME);
					printWriter.println("false");
					counterExample = new File(workingDir.getAbsolutePath() + File.separator + Filename.COUNTEREXAMPLE_FILE + ".bcg");
					final boolean renameWorked = tempCounterExample.renameTo(counterExample);
				}
			}
			catch (FileNotFoundException e)
			{
				return Triple.of(null, null, ReturnCodes.WRITING_PROPERTY_EVALUATION_FILE_FAILED);
			}

			printWriter.flush();
			printWriter.close();

			return Triple.of(counterExample, result, ReturnCodes.TERMINATION_OK);
		}
	}

	private static Pair<File, Integer> retrieveLTLProperty(final File workingDir,
														   final Object temporalLogicObject,
														   final String apiKey)
	{
		if (!(temporalLogicObject instanceof String)
			&& !(temporalLogicObject instanceof File))
		{
			throw new IllegalStateException("Unsupported object type: " + temporalLogicObject.getClass().getName());
		}

		final String property;

		if (temporalLogicObject instanceof File)
		{
			try
			{
				final FileInputStream fileInputStream = new FileInputStream(((File) temporalLogicObject));
				final Scanner scanner = new Scanner(fileInputStream);
				final StringBuilder propertyBuilder = new StringBuilder();

				while (scanner.hasNextLine())
				{
					propertyBuilder.append(scanner.nextLine());
				}

				scanner.close();
				fileInputStream.close();

				property = propertyBuilder.toString();
			}
			catch (IOException e)
			{
				return Pair.of(null, ReturnCodes.READING_PROPERTY_FILE_FAILED);
			}

			((File) temporalLogicObject).delete();
		}
		else
		{
			//We got a description: ask GPT
			try
			{
				property = ChatGPTManager.generateAnswer((String) temporalLogicObject, apiKey).replace("\n", "").replace("\\\"", "\"");
			}
			catch (Exception e)
			{
				return Pair.of(null, ReturnCodes.PROPERTY_GENERATION_FAILED);
			}

			System.out.println("Generated LTL property: " + property);
			MyOwnLogger.append("Generated LTL property: " + property);
		}

		final File ltlPropertyFile = new File(workingDir.getAbsolutePath() + File.separator + Filename.LTL_PROPERTY);

		try
		{
			final PrintWriter printWriter = new PrintWriter(ltlPropertyFile);
			printWriter.println(property.toUpperCase()); //Needed because the LNT spec contains tasks in upper case
			printWriter.flush();
			printWriter.close();
		}
		catch (FileNotFoundException e)
		{
			return Pair.of(null, ReturnCodes.WRITING_LTL_PROPERTY_FAILED);
		}

		return Pair.of(ltlPropertyFile, ReturnCodes.TERMINATION_OK);
	}

	private static File parseAndTransform(File input)
	{
		final String pifSchema = LOCAL_SITE ?
				Filename.LOCAL_PIF_FILE :
				(LOCAL_TESTING ? Filename.LOCAL_PIF_FILE : Filename.REMOTE_PIF_FILE);
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

	private static Pair<ArrayList<String>, Integer> computeSpecLabels(final File bpmnFile)
	{
		final BpmnParser bpmnParser;

		try
		{
			bpmnParser = new BpmnParser(bpmnFile);
			bpmnParser.parse();
		}
		catch (ParserConfigurationException | IOException | SAXException e)
		{
			return Pair.of(new ArrayList<>(), ReturnCodes.RETRIEVING_LABELS_FAILED);
		}

		final ArrayList<String> labels = new ArrayList<>();

		for (BpmnProcessObject object : bpmnParser.bpmnProcess().objects())
		{
			if (object instanceof Task)
			{
				if (LTLKeywords.ALL_KEYWORDS.contains(object.id().toUpperCase()))
				{
					System.out.println("The specification contains tasks whose labels are reserved LTL keywords.");
					return Pair.of(new ArrayList<>(), ReturnCodes.SPEC_LABELS_CONTAIN_RESERVED_LTL_KEYWORDS);
				}

				if (LNTKeywords.ALL_KEYWORDS.contains(object.id().toUpperCase()))
				{
					System.out.println("The specification contains tasks whose labels are reserved LNT keywords.");
					return Pair.of(new ArrayList<>(), ReturnCodes.SPEC_LABELS_CONTAIN_RESERVED_LNT_KEYWORD);
				}

				labels.add(object.id()); //Work on IDs instead of Names (so does VBPMN)
			}
		}

		/*//Convert LNT spec to BCG
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

		if (lntOpenCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
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

		if (bcgInfoCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
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
				if (LTLKeyword.ALL_KEYWORDS.contains(trimmedLabel))
				{
					System.out.println("The specification contains tasks whose labels are reserved LTL keywords.");
					return Pair.of(new ArrayList<>(), SPEC_LABELS_CONTAIN_RESERVED_LTL_KEYWORDS);
				}

				labels.add(trimmedLabel);
			}
		}*/

		return Pair.of(labels, ReturnCodes.TERMINATION_OK);
	}

	private static Pair<String, Integer> generateBuchiAutomata(final File workingDir,
															   final File ltlProperty)
	{
		final StringBuilder propertyBuilder = new StringBuilder();
		final FileInputStream inputStream;

		try
		{
			inputStream = new FileInputStream(workingDir.getAbsolutePath() + File.separator + ltlProperty.getName());
			final Scanner scanner = new Scanner(inputStream);
			String separator = "";

			while (scanner.hasNextLine())
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
			return Pair.of("", ReturnCodes.READING_PROPERTY_FILE_FAILED);
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
			return Pair.of("", ReturnCodes.WRITING_PROPERTY_NEGATION_FAILED);
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
			return Pair.of("", ReturnCodes.TRANSLATING_PROPERTY_TO_BUCHI_AUTOMATA_FAILED);
		}

		if (ltl2tgbaCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
		{
			return Pair.of("", ReturnCodes.TRANSLATING_PROPERTY_TO_BUCHI_AUTOMATA_FAILED);
		}

		System.out.println("Buchi automata:\n\n" + ltl2tgbaCommandManager.stdOut());

		try
		{
			final PrintWriter buchiAutomataPrintWriter = new PrintWriter(workingDir.getAbsolutePath() + File.separator + Filename.BUCHI_AUTOMATA);
			buchiAutomataPrintWriter.println(ltl2tgbaCommandManager.stdOut());
			buchiAutomataPrintWriter.flush();
			buchiAutomataPrintWriter.close();
		}
		catch (FileNotFoundException e)
		{
			return Pair.of("", ReturnCodes.WRITING_BUCHI_AUTOMATA_FILE_FAILED);
		}

		(new File(workingDir + File.separator + negProperty)).delete();

		return Pair.of(ltl2tgbaCommandManager.stdOut(), ReturnCodes.TERMINATION_OK);
	}

	private static int generateSVLScript(final File workingDir,
										 final File lntSpecification,
										 final ArrayList<String> labels,
										 final boolean performReduction)
	{
		final String svlCommand = "hoa2svl";
		final ArrayList<String> svlArgs = new ArrayList<>();
		svlArgs.add(Filename.BUCHI_AUTOMATA);
		svlArgs.add(lntSpecification.getName());
		if (performReduction) svlArgs.add("--reduction");

		for (String label : labels)
		{
			svlArgs.add(label.toUpperCase());
		}

		final CommandManager svlCommandManager = new CommandManager(svlCommand, workingDir, svlArgs);

		try
		{
			svlCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return ReturnCodes.SVL_SCRIPT_GENERATION_FAILED;
		}

		if (svlCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
		{
			return ReturnCodes.SVL_SCRIPT_GENERATION_FAILED;
		}

		return ReturnCodes.TERMINATION_OK;
	}

	private static int executeSVLScript(File workingDir)
	{
		final String svlCommand = "svl";
		final String[] svlArgs = {Filename.SVL_SCRIPT_NAME};
		final CommandManager svlCommandManager = new CommandManager(svlCommand, workingDir, svlArgs);

		try
		{
			svlCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return ReturnCodes.SVL_SCRIPT_EXECUTION_FAILED;
		}

		if (svlCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
		{
			return ReturnCodes.SVL_SCRIPT_EXECUTION_FAILED;
		}

		final PrintWriter printWriter;

		try
		{
			if (svlCommandManager.stdOut().contains("TRUE"))
			{
				System.out.println("Property was evaluated to \"TRUE\"!");
				printWriter = new PrintWriter(workingDir + File.separator + Filename.TRUE_RESULT_FILE_NAME);
				printWriter.println(svlCommandManager.stdOut());
			}
			else
			{
				System.out.println("Property was evaluated to \"FALSE\"!");
				printWriter = new PrintWriter(workingDir + File.separator + Filename.FALSE_RESULT_FILE_NAME);
				printWriter.println(svlCommandManager.stdOut());
			}
		}
		catch (FileNotFoundException e)
		{
			return ReturnCodes.WRITING_PROPERTY_EVALUATION_FILE_FAILED;
		}

		printWriter.flush();
		printWriter.close();

		return ReturnCodes.TERMINATION_OK;
	}

	private static Pair<File, Integer> generateProperCounterexample(File workingDir)
	{
		if (!new File(workingDir.getAbsolutePath() + File.separator + Filename.COUNTEREXAMPLE_FILE + ".bcg").exists())
		{
			return Pair.of(null, ReturnCodes.DIAGNOSTIC_FILE_MISSING);
		}

		//Minimize counterexample with weaktrace to remove "i" transitions
		final String bcgMinCommand = "bcg_open";
		final String[] bcgMinArgs = {
			Filename.COUNTEREXAMPLE_FILE + ".bcg",
			"reductor",
			"-weaktrace",
			Filename.COUNTEREXAMPLE_FILE + "_weak.bcg"
		};
		final CommandManager bcgMinCommandManager = new CommandManager(bcgMinCommand, workingDir, bcgMinArgs);

		try
		{
			bcgMinCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return Pair.of(null, ReturnCodes.COUNTEREXAMPLE_DETERMINATION_FAILED);
		}

		if (bcgMinCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
		{
			return Pair.of(null, ReturnCodes.COUNTEREXAMPLE_DETERMINATION_FAILED);
		}

		//Convert example to AUT
		final String bcgIOCommand = "bcg_io";
		final String[] bcgIOArgs = {
			Filename.COUNTEREXAMPLE_FILE + "_weak.bcg",
			Filename.COUNTEREXAMPLE_FILE + "_weak.aut"
		};
		final CommandManager bcgIOCommandManager = new CommandManager(bcgIOCommand, workingDir, bcgIOArgs);

		try
		{
			bcgIOCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return Pair.of(null, ReturnCodes.COUNTEREXAMPLE_TO_AUT_FAILED);
		}

		if (bcgIOCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
		{
			return Pair.of(null, ReturnCodes.COUNTEREXAMPLE_TO_AUT_FAILED);
		}

		return Pair.of(new File(workingDir.getAbsolutePath() + File.separator + Filename.COUNTEREXAMPLE_FILE + "_weak.aut"), ReturnCodes.TERMINATION_OK);
	}

	private static int retrieveAndVerifyPropertyLabels(final File workingDirectory,
													   final ArrayList<String> specificationLabels,
													   final String buchiAutomata)
	{
		//Retrieve Büchi automata labels line
		final String[] buchiAutomataLines = buchiAutomata.split("\\r?\\n");
		String labelsLine = null;

		for (String buchiAutomataLine : buchiAutomataLines)
		{
			if (buchiAutomataLine.startsWith("AP:"))
			{
				labelsLine = buchiAutomataLine;
				break;
			}
		}

		if (labelsLine == null)
		{
			return ReturnCodes.BUCHI_AUTOMATA_HAS_NO_LABELS;
		}

		//Retrieve Büchi automata labels
		final ArrayList<String> buchiAutomataLabels = new ArrayList<>();
		int doubleQuoteIndex = labelsLine.indexOf('"');

		while (doubleQuoteIndex != -1)
		{
			labelsLine = labelsLine.substring(doubleQuoteIndex + 1);
			final int nextQuoteIndex = labelsLine.indexOf('"');
			final String currentLabel = labelsLine.substring(0, nextQuoteIndex);
			buchiAutomataLabels.add(currentLabel.toUpperCase());
			labelsLine = labelsLine.substring(nextQuoteIndex + 1);
			doubleQuoteIndex = labelsLine.indexOf('"');
		}

		if (buchiAutomataLabels.isEmpty())
		{
			return ReturnCodes.BUCHI_AUTOMATA_HAS_NO_LABELS;
		}

		//Verify that property does not contain reserved LTL keywords
		for (String buchiLabel : buchiAutomataLabels)
		{
			if (LTLKeywords.ALL_KEYWORDS.contains(buchiLabel.toUpperCase()))
			{
				return ReturnCodes.PROPERTY_LABELS_CONTAIN_RESERVED_LTL_KEYWORD;
			}
		}

		//Remove specification labels from Buchi automata labels
		for (String specLabel : specificationLabels)
		{
			buchiAutomataLabels.remove(specLabel.toUpperCase());
		}

		if (!buchiAutomataLabels.isEmpty())
		{
			System.out.println("/!\\ WARNING /!\\ Labels \"" + buchiAutomataLabels + "\" belong to the property but do" +
					" not belong to the specification!");

			try
			{
				final PrintWriter printWriter = new PrintWriter(workingDirectory.getAbsolutePath() + File.separator + Filename.WARNING_FILE_NAME);
				printWriter.println(buchiAutomataLabels);
				printWriter.flush();
				printWriter.close();
			}
			catch (FileNotFoundException e)
			{
				return ReturnCodes.WARNING_FILE_WRITING_FAILED;
			}
		}

		return ReturnCodes.TERMINATION_OK;
	}

	private static int generateBCGFile(final File lntSpec,
									   final File workingDir)
	{
		final String lntOpenCommand = "lnt.open";
		final String[] lntOpenArgs = new String[]{lntSpec.getName(), "generator", "problem.bcg"};
		final CommandManager lntOpenCommandManager = new CommandManager(lntOpenCommand, workingDir, lntOpenArgs);

		try
		{
			lntOpenCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			return ReturnCodes.BCG_GENERATION_FAILED;
		}

		if (lntOpenCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
		{
			return ReturnCodes.BCG_GENERATION_FAILED;
		}

		return ReturnCodes.TERMINATION_OK;
	}

	private static int retrieveBCGFileSize(final File workingDir,
										   final String bcgFileName)
	{
		final String bcgInfoCommand = "bcg_info";
		final String[] bcgInfoArgs = {
			"-size",
			bcgFileName
		};
		final CommandManager bcgInfoCommandManager = new CommandManager(bcgInfoCommand, workingDir, bcgInfoArgs);

		try
		{
			bcgInfoCommandManager.execute();
		}
		catch (IOException | InterruptedException e)
		{
			throw new RuntimeException();
		}

		if (bcgInfoCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
		{
			throw new RuntimeException();
		}

		final String answer = bcgInfoCommandManager.stdOut();
		final String sizeStr = Utils.trim(answer.substring(0, answer.indexOf("states")));

		if (!Utils.isAnInt(sizeStr))
		{
			throw new RuntimeException();
		}

		return Integer.parseInt(sizeStr);
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

	private static void cleanExit(final CommandLineParser commandLineParser,
								  final int exitCode,
								  final Exception e)
	{
		MyOwnLogger.writeStdOut((File) commandLineParser.get(CommandLineOption.WORKING_DIRECTORY));

		if (exitCode != ReturnCodes.TERMINATION_OK)
		{
			MyOwnLogger.writeStdErr((File) commandLineParser.get(CommandLineOption.WORKING_DIRECTORY), Utils.getStackTrace(e));
		}

		System.exit(exitCode);
	}
}