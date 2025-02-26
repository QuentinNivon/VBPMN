package fr.inria.convecs.optimus.py_to_java;

import fr.inria.convecs.optimus.py_to_java.cadp_compliance.generics.BpmnTypesBuilderGeneric;
import fr.inria.convecs.optimus.py_to_java.cadp_compliance.generics.Pif2LntGeneric;
import fr.inria.convecs.optimus.util.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Porting of the ``vbpmn.py'' code to Java code
 * @author Quentin NIVON (quentin.nivon@inria.fr)
 */

public class Vbpmn
{
	private final Logger logger = LoggerFactory.getLogger(Vbpmn.class);

	/*
	 	Command to call SVL.
	 	First argument is the script, second one is the result file.
	 */
	private static final String SVL_CALL_COMMAND = "svl {0} -> {1}";

	/*
		Template for SVL scripts.
		First argument is the SVL contents.
	 */
	private static final String SVL_CAESAR_TEMPLATE =
			"% CAESAR_OPEN_OPTIONS=\"-silent -warning\"\n" +
					"% CAESAR_OPTIONS=\"-more cat\"\n" +
					"{0}\n"
			;

	/*
		Template of the verification of a comparison between two models.
		First argument is the first model (LTS in BCG format).
		Second one is the comparison operation for bisimulator.
		Third one is the equivalence notion (strong, branching, ...).
		Fourth one is the second model (LTS in BCG format).
	 */
	private static final String SVL_COMPARISON_CHECKING_TEMPLATE =
			"% bcg_open \"{0}.bcg\" bisimulator -{1} -{2} -diag \"{3}.bcg\"\n"
			;

	/*
		Template of the verification of formula over a model.
		First argument is the model file (LTS in BCG format).
		Second one is the formula (MCG) file.
	 */
	private static final String SVL_FORMULA_CHECKING_TEMPLATE = "% bcg_open \"{0}.bcg\" evaluator4 -diag \"{1}\"\n";

	/*
		Template for hiding in SVL.
		First and fourth arguments are the model file (LTS in BCG format).
		Second argument is the hiding mode (hiding or hiding all but).
		Third argument is the list of elements to hide (or hide but).
	 */
	private static final String SVL_HIDING_TEMPLATE = "\"{0}.bcg\" = total {1} {2} in \"{0}.bcg\"\n";

	/*
		Template for renaming in SVL.
		First and third arguments are the model file (LTS in BCG format).
		Second argument is the relabelling function.
	 */
	private static final String SVL_RENAMING_TEMPLATE = "\"{0}.bcg\" = total rename {1} in \"{0}.bcg\"\n";

	/*
		Template for making a working copy in SVL.
		First argument is the source model file (LTS in BCG format).
		Second argument is the target model file (LTS in BCG format).
	 */
	private static final String SVL_COPY_TEMPLATE = "% bcg_io \"{0}.bcg\" \"{1}.bcg\"\n";

	private static final String WORK_SUFFIX = "_work";
	private static final String CONSERVATIVE_COMPARISON = "conservative";
	private static final String INCLUSIVE_COMPARISON = "inclusive";
	private static final String EXCLUSIVE_COMPARISON = "exclusive";
	private static final List<String> OPERATIONS_COMPARISON = Arrays.asList(
			CONSERVATIVE_COMPARISON,
			INCLUSIVE_COMPARISON,
			EXCLUSIVE_COMPARISON
	);

	private static final String AND_PROPERTY = "property-and";
	private static final String IMPLIED_PROPERTY = "property-implied";
	private static final String HIDING_OPERATION = "_"; //NOT IN Python CODE
	private static final List<String> OPERATIONS_PROPERTY = Arrays.asList(
			AND_PROPERTY,
			IMPLIED_PROPERTY
	);
	private static final List<String> OPERATIONS = Arrays.asList(
			CONSERVATIVE_COMPARISON,
			INCLUSIVE_COMPARISON,
			EXCLUSIVE_COMPARISON,
			AND_PROPERTY,
			IMPLIED_PROPERTY
	);
	private static final String OPERATIONS_DEFAULT = CONSERVATIVE_COMPARISON;
	private static final String SELECTION_FIRST = "first";
	private static final String SELECTION_SECOND = "second";
	private static final String SELECTION_ALL = "all";
	private static final List<String> SELECTIONS = Arrays.asList(
			SELECTION_FIRST,
			SELECTION_SECOND,
			SELECTION_ALL
	);
	private static final String SELECTIONS_DEFAULT = SELECTION_ALL;
	private static final String EQUAL_OPERATION = "equal";
	private static final String SMALLER_OPERATION = "smaller";
	private static final String GREATER_OPERATION = "greater";
	private static final HashMap<String, String> OPERATION_TO_BISIMULATOR = new HashMap<String, String>(){{
		put(CONSERVATIVE_COMPARISON, EQUAL_OPERATION);
		put(INCLUSIVE_COMPARISON, SMALLER_OPERATION);
		put(EXCLUSIVE_COMPARISON, GREATER_OPERATION);
	}};

	private static final String BRANCHING_EQUIVALENCE = "branching";
	private static final String STRONG_EQUIVALENCE = "strong";
	private static final String HIDE_ALL_BUT = "hide all but";
	private static final String HIDE = "hide";
	private static final String MCL_FORMULA = "formula.mcl";

	private final String[] sysArgs;
	private final String outputFolder;
	private final boolean compareOrVerify;
	private final boolean forceBcgUsageModel1;
	private final boolean forceBcgUsageModel2;
	private final Balancement balancement;
	private final Collection<String> alphabetModel1;
	private final Collection<String> alphabetModel2;
	private final ArrayList<Pair<Long, String>> executionTimes;


	public Vbpmn(final String[] sysArgs,
				 final String outputFolder)
	{
		this(sysArgs, outputFolder, true, null, null, Balancement.COMPUTE_BALANCEMENT);
	}

	public Vbpmn(final String[] sysArgs,
				 final String outputFolder,
				 final Balancement balancement)
	{
		this(sysArgs, outputFolder, true, null, null, balancement);
	}

	public Vbpmn(final String[] sysArgs,
				 final String outputFolder,
				 final boolean compareOrVerify)
	{
		this(sysArgs, outputFolder, compareOrVerify, null, null, Balancement.COMPUTE_BALANCEMENT);
	}

	public Vbpmn(final String[] sysArgs,
				 final String outputFolder,
				 final boolean compareOrVerify,
				 final Balancement balancement)
	{
		this(sysArgs, outputFolder, compareOrVerify, null, null, balancement);
	}

	public Vbpmn(final String[] sysArgs,
				 final String outputFolder,
				 final boolean compareOrVerify,
				 final Collection<String> alphabetModel1,
				 final Collection<String> alphabetModel2)
	{
		this(sysArgs, outputFolder, compareOrVerify, alphabetModel1, alphabetModel2, Balancement.COMPUTE_BALANCEMENT);
	}

	public Vbpmn(final String[] sysArgs,
				 final String outputFolder,
				 final boolean compareOrVerify,
				 final Collection<String> alphabetModel1,
				 final Collection<String> alphabetModel2,
				 final Balancement balancement)
	{
		this.sysArgs = sysArgs;
		//if (true) throw new IllegalStateException(Arrays.toString(sysArgs));
		this.outputFolder = outputFolder;
		this.compareOrVerify = compareOrVerify;
		this.forceBcgUsageModel1 = alphabetModel1 != null;
		this.forceBcgUsageModel2 = alphabetModel2 != null;
		this.executionTimes = new ArrayList<>();
		this.alphabetModel1 = alphabetModel1 == null ? new ArrayList<>() : alphabetModel1;
		this.alphabetModel2 = alphabetModel2 == null ? new ArrayList<>() : alphabetModel2;
		this.balancement = balancement;
	}

	@SuppressWarnings("unchecked") //Prevents Java from outputting warnings concerning the cast of Class<capture of ?>
	// to Class<? extends Pif2LntGeneric>
	public boolean execute()
	{
		final long startTime = System.nanoTime();

		//Initialise parser
		final Namespace args = this.parseArgs();
		final File pif1 = new File((String) args.getList("models").get(0));
		final File pif2 = new File((String) args.getList("models").get(1));

		//Check if process is balanced or not
		final long checkProcessBalanceStartTime = System.nanoTime();
		final boolean processIsBalanced = this.balancement == Balancement.COMPUTE_BALANCEMENT ?
										((this.forceBcgUsageModel1 || PifUtil.isPifBalanced(pif1))
										&& (this.forceBcgUsageModel2 || PifUtil.isPifBalanced(pif2))) :
										this.balancement == Balancement.FORCE_BALANCEMENT;
		final long checkProcessBalanceEndTime = System.nanoTime();
		final long checkProcessBalanceTime = checkProcessBalanceEndTime - checkProcessBalanceStartTime;
		this.executionTimes.add(Pair.of(checkProcessBalanceTime, "Checking if the process is balanced took " + Utils.nanoSecToReadable(checkProcessBalanceTime)));

		//Get CADP version
		final long computeCADPVersionStartTime = System.nanoTime();
		final String cadpVersionDir = this.getCadpVersion();
		final long computeCADPVersionEndTime = System.nanoTime();
		final long computeCADPVersionTime = computeCADPVersionEndTime - computeCADPVersionStartTime;
		this.executionTimes.add(Pair.of(computeCADPVersionTime, "Retrieving the installed CADP version took " + Utils.nanoSecToReadable(computeCADPVersionTime)));

		//Load the good Pif2Lnt class (depending on the CADP version)
		final long pif2LntClassRetrievalStartTime = System.nanoTime();
		final Pif2LntGeneric pif2lnt = this.loadPif2LntGenericClass(cadpVersionDir);
		pif2lnt.setBalance(processIsBalanced);
		pif2lnt.setOutputFolder(this.outputFolder);
		final long pif2LntClassRetrievalEndTime = System.nanoTime();
		final long pif2LntClassRetrievalTime = pif2LntClassRetrievalEndTime - pif2LntClassRetrievalStartTime;
		this.executionTimes.add(Pair.of(pif2LntClassRetrievalTime, "Retrieving the Pif2Lnt class to use took " + Utils.nanoSecToReadable(pif2LntClassRetrievalTime)));

		//Load the good BpmnTypesBuilder class (depending on the CADP version)
		final long bpmnTypesBuilderClassRetrievalStartTime = System.nanoTime();
		final BpmnTypesBuilderGeneric bpmnTypesBuilder = this.loadBpmnTypesBuilderClass(cadpVersionDir);
		bpmnTypesBuilder.setOutputDirectory(this.outputFolder);
		bpmnTypesBuilder.dumpBpmnTypesFile();
		final long bpmnTypesBuilderClassRetrievalEndTime = System.nanoTime();
		final long bpmnTypesBuilderClassRetrievalTime = bpmnTypesBuilderClassRetrievalEndTime - bpmnTypesBuilderClassRetrievalStartTime;
		this.executionTimes.add(Pair.of(bpmnTypesBuilderClassRetrievalTime, "Retrieving the BpmnTypesBuilder class to use took " + Utils.nanoSecToReadable(bpmnTypesBuilderClassRetrievalTime)));

		//If in lazy mode, rebuild the BCG files only if needed
		final boolean lazy = args.get("lazy") != null
				&& args.getBoolean("lazy");

		//(Re)build the first model
		final long firstProcessConversionStartTime = System.nanoTime();
		final Triple<Integer, String, Collection<String>> result1 =
			this.forceBcgUsageModel1 ?
			new Triple<>(ReturnCodes.TERMINATION_OK, FilenameUtils.getBaseName(pif1.getName()), this.alphabetModel1) :
			(
				lazy ?
				pif2lnt.load(pif1, this.compareOrVerify) :
				pif2lnt.generate(pif1, this.compareOrVerify)
			);
		final long firstProcessConversionEndTime = System.nanoTime();
		final long firstProcessConversionTime = firstProcessConversionEndTime - firstProcessConversionStartTime;
		this.executionTimes.add(Pair.of(firstProcessConversionTime, "The generation of the LNT code of the first process took " + Utils.nanoSecToReadable(firstProcessConversionTime)));

		//(Re)build the second model
		final long secondProcessConversionStartTime = System.nanoTime();
		final Triple<Integer, String, Collection<String>> result2;

		if (OPERATIONS_COMPARISON.contains(args.getString("operation")))
		{
			//We are comparing processes, thus we need to build the two processes
			result2 =
				this.forceBcgUsageModel2 ?
				new Triple<>(ReturnCodes.TERMINATION_OK, FilenameUtils.getBaseName(pif2.getName()), this.alphabetModel2) :
				(
					lazy ?
					pif2lnt.load(pif2, this.compareOrVerify) :
					pif2lnt.generate(pif2, this.compareOrVerify)
				);
		}
		else
		{
			result2 = result1;
		}

		final long secondProcessConversionEndTime = System.nanoTime();
		final long secondProcessConversionTime = secondProcessConversionEndTime - secondProcessConversionStartTime;
		this.executionTimes.add(Pair.of(secondProcessConversionTime, "The generation of the LNT code of the second process took " + Utils.nanoSecToReadable(secondProcessConversionTime)));

		//If one of the models could not be loaded => ERROR
		if (result1.getLeft() != ReturnCodes.TERMINATION_OK)
		{
			final String errorMessage = this.getErrorMessage(pif1, result1);
			System.out.println(errorMessage);
			logger.error(errorMessage);
			throw new IllegalStateException(errorMessage);
		}

		if (result2.getLeft() != ReturnCodes.TERMINATION_OK)
		{
			final String errorMessage = this.getErrorMessage(pif2, result2);
			System.out.println(errorMessage);
			logger.error(errorMessage);
			throw new IllegalStateException(errorMessage);
		}

		/*
			Checks if we compare up to a context.
			TODO Gwen : refine synchronization sets computation (_EM vs _REC)
			TODO Pascal : what about if we have hiding and/or renaming + context-awareness? different alphabets should
			 be used?
		 */

		final ArrayList<String> syncSet1 = new ArrayList<>();
		final ArrayList<String> syncSet2 = new ArrayList<>();

		if (args.get("context") != null)
		{
			final String pifContextModel = args.getString("context");
			System.out.println("Converting \"" + pifContextModel + "\" to LTS...");
			Triple<Integer, String, Collection<String>> result = lazy ? pif2lnt.load(new File(pifContextModel)) : pif2lnt.generate(new File(pifContextModel));

			for (String symbol : result.getRight())
			{
				if (result1.getRight().contains(symbol))
				{
					syncSet1.add(symbol);
				}
				if (result2.getRight().contains(symbol))
				{
					syncSet2.add(symbol);
				}
			}
		}

		final boolean result;
		final long comparisonEvaluationStartTime = System.nanoTime();
		final String mode;

		if (this.compareOrVerify)
		{
			final Checker comparator;

			//Check whether we compare based on an equivalence or based on a property
			if (OPERATIONS_COMPARISON.contains(args.getString("operation")))
			{
				comparator = new ComparisonChecker(
						result1.getMiddle(),
						result2.getMiddle(),
						args.getString("operation"),
						args.getList("hiding"),
						args.get("exposemode") != null && args.getBoolean("exposemode"),
						args.get("renaming") == null ? new HashMap<>() : args.get("renaming"),
						args.getString("renamed") == null ? "all" : args.getString("renamed"),
						new ArrayList[]{syncSet1, syncSet2}
				);
				mode = "The comparison of the processes took ";
			}
			else
			{
				comparator = new FormulaChecker(
						result1.getMiddle(),
						result2.getMiddle(),
						args.getString("formula")
				);
				mode = "The evaluation of the formula took ";
			}

			result = comparator.call();
		}
		else
		{
			result = true;
			mode = "Not comparing nor evaluating took ";
		}

		final long comparisonEvaluationEndTime = System.nanoTime();
		final long comparisonEvaluationTime = comparisonEvaluationEndTime - comparisonEvaluationStartTime;
		this.executionTimes.add(Pair.of(comparisonEvaluationTime, mode + Utils.nanoSecToReadable(comparisonEvaluationTime)));

		//Perform comparison and process result
		final long endTime = System.nanoTime();
		final long totalTime = endTime - startTime;
		this.executionTimes.add(Pair.of(totalTime, "Overall execution took " + Utils.nanoSecToReadable(totalTime)));

		final File execTimeFile = new File(outputFolder + File.separator + "time.txt");
		final PrintStream printStream;

		try
		{
			printStream = new PrintStream(execTimeFile);
		}
		catch (FileNotFoundException e)
		{
			logger.error("Could not write \"time.txt\" file: {}", String.valueOf(e));
			throw new RuntimeException(e);
		}

		printStream.print("The execution took " + Utils.nanoSecToReadable(totalTime));
		printStream.flush();
		printStream.close();

		final int returnValue = result ? ReturnCodes.TERMINATION_OK : ReturnCodes.TERMINATION_ERROR;
		//System.out.println("Result: " + result);

		return result;
	}

	public ArrayList<Pair<Long, String>> times()
	{
		return this.executionTimes;
	}

	//Private methods
	private String getErrorMessage(final File pifProcess,
								   final Triple<Integer, String, Collection<String>> triple)
	{
		final String errorMessage;

		if (triple.getLeft() != ReturnCodes.TERMINATION_UNBALANCED_INCLUSIVE_CYCLE)
		{
			errorMessage = "Error while loading model \"" + pifProcess.getAbsolutePath() + "\". Please verify " +
					"that your input model is correct (in particular, BPMN objects and flows should not contain the" +
					" \"-\" symbol in their \"id\" attribute).";

		}
		else
		{
			errorMessage = "Unbalanced inclusive gateways inside loops are not supported by the current version of" +
					" VBPMN, but model \"" + pifProcess.getAbsolutePath() + "\" contains some.";
		}

		return errorMessage;
	}

	private Namespace parseArgs()
	{
		final ArgumentParser parser = ArgumentParsers.newFor("vbpmn").build()
				.description("Compares two PIF processes.")
				.version("${prog} 1.0");
		parser.addArgument("--version")
				.action(Arguments.version());
		parser.addArgument("models")
				.metavar("Model")
				.nargs(2)
				.help("the models to compare (filenames of PIF files)");
		parser.addArgument("operation")
				.metavar("OP")
				.choices(OPERATIONS)
				.help("the comparison operation");
		parser.addArgument("--formula")
				.metavar("Formula")
				.help("temporal logic formula to check (used only if operation is in " + OPERATIONS_PROPERTY + ")");
		parser.addArgument("--hiding")
				.nargs("*")
				.help("list of alphabet elements to hide or to expose (based on --exposemode)");
		parser.addArgument("--exposemode")
				.action(Arguments.storeTrue())
				.help("decides whether the arguments for --hiding should be the ones hidden (default) or the ones" +
						" exposed (if this option is set)");
		parser.addArgument("--context")
				.metavar("Context")
				.help("context to compare with reference to (filename of a PIF file)");
		parser.addArgument("--renaming")
				.metavar("old:new")
				.nargs("*")
				.setDefault(new HashMap<>())
				.help("list of renamings");
		parser.addArgument("--renamed")
				.nargs("?")
				.choices(SELECTIONS)
				.setConst(SELECTIONS_DEFAULT)
				.setDefault(SELECTIONS_DEFAULT)
				.help("gives the model to apply renaming to (first, second, or all (default))");
		parser.addArgument("--lazy")
				.action(Arguments.storeTrue())
				.help("does not recompute the BCG model if it already exists and is more recent than the PIF model");

		//Parse arguments
		final Namespace args;

		try
		{
			args = parser.parseArgs(sysArgs);

			if (OPERATIONS_PROPERTY.contains(args.getString("operation"))
					&& args.get("formula") == null)
			{
				System.out.println("missing formula in presence of property based comparison.");
				logger.error("missing formula in presence of property based comparison.");
				throw new RuntimeException("missing formula in presence of property based comparison.");
			}
			if (!OPERATIONS_PROPERTY.contains(args.getString("operation"))
					&& args.get("formula") != null)
			{
				logger.warn("formula in presence of equivalence based comparison will not be used.");
				System.out.println("formula in presence of equivalence based comparison will not be used.");
			}
		}
		catch (ArgumentParserException e)
		{
			parser.printHelp();
			logger.error(String.valueOf(e));
			throw new IllegalStateException();
		}

		return args;
	}

	private String getCadpVersion()
	{
		final String cadpVersionDir;

		try
		{
			final Map<String, String> environment = System.getenv();

			/*logger.debug("Environment variables found:\n");

			for (String key : environment.keySet())
			{
				logger.debug("- {} : {}", key, environment.get(key));
			}*/

			if (System.getenv("CADP") == null)
			{
				logger.error("Environment variable $CADP is not set! Please fix this error and retry.");
				throw new RuntimeException("Environment variable $CADP is not set! Please fix this error and retry.");
			}

			if (System.getenv("PATH") != null
				&& !System.getenv("PATH").contains("cadp"))
			{
				logger.error("Environment variable $PATH exists but does not contain \"cadp\" ({})", System.getenv("PATH"));
				throw new RuntimeException("Environment variable $PATH exists but does not contain \"cadp\" (" +
						System.getenv("PATH") + ")");
			}

			//logger.debug("CADP dir: \"{}\".", System.getenv("CADP"));

			final CommandManager commandManager = new CommandManager("cadp_lib", new File(outputFolder), "-1");
			commandManager.execute();

			//Split answer by spaces
			final String[] splitAnswer = commandManager.stdOut().split("\\s+");
			//The 2nd element is the version code, i.e. "2023k"
			cadpVersionDir = "_" + splitAnswer[1].replace(" ", "").replace("-", "");
			//System.out.println("CADP VERSION: " + cadpVersionDir);
		}
		catch (IOException | InterruptedException e)
		{
			throw new RuntimeException(e);
		}

		return cadpVersionDir;
	}

	@SuppressWarnings("unchecked")
	private Pif2LntGeneric loadPif2LntGenericClass(final String cadpVersionDir)
	{
		final Pif2LntGeneric pif2lnt;

		try
		{
			//Load the Pif2Lnt class located in the package corresponding to the good CADP version
			final String classPath = "fr.inria.convecs.optimus.py_to_java.cadp_compliance." + cadpVersionDir + ".Pif2Lnt";
			final Class<? extends Pif2LntGeneric> pif2LntClass = (Class<? extends Pif2LntGeneric>) Class.forName(classPath);
			final Constructor<? extends Pif2LntGeneric> pif2LntConstructor = pif2LntClass.getDeclaredConstructor();
			pif2lnt = pif2LntConstructor.newInstance();
		}
		catch (ClassNotFoundException
			   | NoSuchMethodException
			   | InvocationTargetException
			   | InstantiationException
			   | IllegalAccessException e)
		{
			final String errorMessage = "Please make sure that you downloaded the latest version of VBPMN. \nIf yes," +
					" please check that the path \"<your_tomcat_installation_path>" + File.separator + "webapps" +
					File.separator + "transformation" + File.separator + "WEB-INF" + File.separator + "classes" +
					File.separator + "fr" + File.separator + "inria" + File.separator + "convecs" + File.separator +
					"optimus" + File.separator + "py_to_java" + File.separator + "cadp_compliance" + File.separator +
					cadpVersionDir + "\" exists and contains the file \"Pif2Lnt.class\". \nIf yes, please send an" +
					" email to the staff.";
			System.out.println(errorMessage);
			logger.error(errorMessage);
			throw new RuntimeException(errorMessage, e);
		}

		return pif2lnt;
	}

	@SuppressWarnings("unchecked")
	private BpmnTypesBuilderGeneric loadBpmnTypesBuilderClass(final String cadpVersionDir)
	{
		final BpmnTypesBuilderGeneric bpmnTypesBuilder;

		try
		{
			//Load the BpmnTypesBuilder class located in the package corresponding to the good version
			final String classPath = "fr.inria.convecs.optimus.py_to_java.cadp_compliance." + cadpVersionDir + ".BpmnTypesBuilder";
			final Class<? extends BpmnTypesBuilderGeneric> bpmnTypesBuilderClass = (Class<? extends BpmnTypesBuilderGeneric>) Class.forName(classPath);
			final Constructor<? extends BpmnTypesBuilderGeneric> bpmnTypesBuilderConstructor = bpmnTypesBuilderClass.getDeclaredConstructor();
			bpmnTypesBuilder = bpmnTypesBuilderConstructor.newInstance();
		}
		catch (ClassNotFoundException
			   | NoSuchMethodException
			   | InvocationTargetException
			   | InstantiationException
			   | IllegalAccessException e)
		{
			final String errorMessage = "Please make sure that you downloaded the latest version of VBPMN. \nIf yes," +
					" please check that the path \"<your_tomcat_installation_path>" + File.separator + "webapps" +
					File.separator + "transformation" + File.separator + "WEB-INF" + File.separator + "classes" +
					File.separator + "fr" + File.separator + "inria" + File.separator + "convecs" + File.separator +
					"optimus" + File.separator + "py_to_java" + File.separator + "cadp_compliance" + File.separator +
					cadpVersionDir + "\" exists and contains the file \"BpmnTypesBuilder.class\". \nIf yes, please" +
					" send an email to the staff.";
			System.out.println(errorMessage);
			logger.error(errorMessage);
			throw new RuntimeException(errorMessage, e);
		}

		return bpmnTypesBuilder;
	}

	//Sub-classes

	/*
		This class represents the superclass of all classes performing some formal checking on two LTS models (stores
		in BCG format files)
	 */
	abstract static class Checker
	{
		protected static final String CHECKER_FILE = "check.svl";
		protected static final String DIAGNOSTIC_FILE = "res.txt";
		protected final String model1;
		protected final String model2;

		/**
		 * Sets up the Checker.
		 *
		 * @param model1 is the filename of the first model (LTS in a BCG file)
		 * @param model2 is the filename of the second model (LTS in a BCG file)
		 */
		public Checker(final String model1,
					   final String model2)
		{
			this.model1 = model1;
			this.model2 = model2;
		}

		/**
		 Generates the SVL script to check the property on both models.

		 @param filename is the filename of the SVL script to create.
		 */
		public abstract void genSVL(final String filename);

		/**
		 * Reification of a Checker as a callable object.
		 *
		 * TODO
		 */
		public abstract boolean call();
	}

	// This class is used to perform comparison operations on two models (LTS stored in two BCG format files)
	class ComparisonChecker extends Checker
	{
		private final String operation;
		private final List<String> hiding;
		private final String renamed;
		private final boolean exposeMode;
		private final Map<String, String> renaming;
		private final List<String>[] syncSets;

		/**
		 * Sets up the ComparisonChecker.
		 *
		 * @param model1 is the filename of the first model (LTS in a BCG file)
		 * @param model2 is the filename of the second model (LTS in a BCG file)
		 * @param operation is the comparison operation (in Vbpmn.OPERATIONS)
		 * @param hiding is the list of elements to hide (or to expose, wrt @exposeMode)
		 * @param exposeMode states whether the elements in @hiding should be exposed or not
		 * @param renaming is the correspondence between old labels and new labels
		 * @param syncSets is the couple of lists of alphabets to synchronize on (one for each model)
		 */
		public ComparisonChecker(final String model1,
								 final String model2,
								 final String operation,
								 final List<String> hiding,
								 final boolean exposeMode,
								 final Map<String, String> renaming,
								 final String renamed,
								 final List<String>[] syncSets)
		{
			super(model1, model2);

			if (!OPERATIONS.contains(operation)
				|| operation.equals(HIDING_OPERATION))
			{
				logger.error("Operation should be in {} and \"_\" is only for hiding. Received \"{}\".", OPERATIONS, operation);
				throw new RuntimeException("Operation should be in " + OPERATIONS + " and \"_\" is only for hiding. " +
						"Received \"" + operation + "\".");
			}

			if (!SELECTIONS.contains(renamed))
			{
				logger.error("Selection should be in {}. Received \"{}\".", SELECTIONS, renamed);
				throw new RuntimeException("Selection should be in " + SELECTIONS + ". Received \"" + renamed + "\".");
			}

			this.operation = operation;
			this.hiding = hiding;
			this.renamed = renamed;
			this.exposeMode = exposeMode;
			this.renaming = renaming;
			this.syncSets = syncSets;
		}

		/**
		 * Generates an SVL script to check the property on both models.
		 *
		 * @param filename is the filename of the SVL script to create.
		 */
		@Override
		public void genSVL(final String filename)
		{
			String equivalenceVersion = BRANCHING_EQUIVALENCE;
			final StringBuilder svlCommands = new StringBuilder();
			//Add commands to make copies of the models and not change them.
			final String workModel1 = this.model1 + WORK_SUFFIX;
			final String workModel2 = this.model2 + WORK_SUFFIX;
			svlCommands.append(PyToJavaUtils.parametrize(SVL_COPY_TEMPLATE, this.model1, workModel1));
			svlCommands.append(PyToJavaUtils.parametrize(SVL_COPY_TEMPLATE, this.model2, workModel2));

			//If required, perform hiding (on BOTH models).
			if (this.hiding != null)
			{
				equivalenceVersion = BRANCHING_EQUIVALENCE;
				final String hideMode;

				if (this.exposeMode)
				{
					hideMode = HIDE_ALL_BUT;
				}
				else
				{
					hideMode = HIDE;
				}

				svlCommands.append(PyToJavaUtils.parametrize(
						SVL_HIDING_TEMPLATE,
						workModel1,
						hideMode,
						PyToJavaUtils.join(this.hiding, ","))
				);
				svlCommands.append(PyToJavaUtils.parametrize(
						SVL_HIDING_TEMPLATE,
						workModel2,
						hideMode,
						PyToJavaUtils.join(this.hiding, ","))
				);
			}

			/*
				Perform renaming.
				Done AFTER having hidden TODO: is this ok? shouldn't we allow more freedom in the ordering of things?
			 */

			if (!this.renaming.isEmpty())
			{
				final ArrayList<String> renamings = new ArrayList<>();

				for (final String oldName : this.renaming.keySet())
				{
					final String newName = this.renaming.get(oldName);
					renamings.add(oldName + " -> " + newName);
				}

				switch (this.renamed)
				{
					case SELECTION_ALL:
						svlCommands.append(PyToJavaUtils.parametrize(
								SVL_RENAMING_TEMPLATE,
								workModel1,
								PyToJavaUtils.join(renamings, ","))
						);
						svlCommands.append(PyToJavaUtils.parametrize(
								SVL_RENAMING_TEMPLATE,
								workModel2,
								PyToJavaUtils.join(renamings, ","))
						);
						break;
					case SELECTION_FIRST:
						svlCommands.append(PyToJavaUtils.parametrize(
								SVL_RENAMING_TEMPLATE,
								workModel1,
								PyToJavaUtils.join(renamings, ","))
						);
						break;
					case SELECTION_SECOND:
						svlCommands.append(PyToJavaUtils.parametrize(
								SVL_RENAMING_TEMPLATE,
								workModel2,
								PyToJavaUtils.join(renamings, ","))
						);
						break;
					default:
						//Should never happen
						logger.error("The list of elements to rename is not empty but the selectionis \"{}\"!", this.renamed);
						throw new IllegalStateException("The list of elements to rename is not empty but the selection" +
								"is \"" + this.renamed + "\"!");
				}
			}

			/*
				Add the command to perform the comparison.
        		Equivalences are strong (by default) but we use branching in case of hiding. //TODO Branching by default is an error?
			 */

			svlCommands.append(PyToJavaUtils.parametrize(
					SVL_COMPARISON_CHECKING_TEMPLATE,
					workModel1,
					OPERATION_TO_BISIMULATOR.get(this.operation),
					equivalenceVersion,
					workModel2)
			);

			//TODO VERIFIER LA CREATION DU FICHIER AINSI QUE SON CONTENU
			final String template = PyToJavaUtils.parametrize(SVL_CAESAR_TEMPLATE, svlCommands.toString());
			final File templateFile = new File(outputFolder + File.separator + filename);
			final PrintStream printStream;

			try
			{
				printStream = new PrintStream(templateFile);
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}

			printStream.print(template);
			printStream.close();
		}

		/**
		 * Checks if an equivalence or preorder yields between two models.
		 * Done by generating first an SVL script and then calling it.
		 *
		 * @return true if the equivalence/preorder yields, false otherwise.
		 */
		@Override
		public boolean call()
		{
			this.genSVL(Checker.CHECKER_FILE);

			try
			{
				//TODO CHECK FUNCTIONING + REVERIF
				final String command = "svl";
				final String[] args = {Checker.CHECKER_FILE, "->", Checker.DIAGNOSTIC_FILE};
				final CommandManager commandManager = new CommandManager(command, new File(outputFolder), args);
				commandManager.execute();

				if (commandManager.returnValue() != ReturnCodes.TERMINATION_OK)
				{
					throw new RuntimeException("An error occurred during the execution of the SVL script:\n\n" + commandManager.stdErr());
				}

				if (!commandManager.stdOut().contains("TRUE")
						&& !commandManager.stdOut().contains("FALSE"))
				{
					throw new RuntimeException("An error occurred during the execution of the SVL script. See the" +
							".log file for more information.");
				}

				final File resFile = new File(outputFolder + File.separator + DIAGNOSTIC_FILE);
				final PrintWriter printWriter;

				try
				{
					printWriter = new PrintWriter(resFile);
				}
				catch (IOException e)
				{
					throw new RuntimeException();
				}

				printWriter.println(commandManager.stdOut());
				printWriter.flush();
				printWriter.close();

				return commandManager.stdOut().contains("TRUE");
			}
			catch (IOException | InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	/*
		This class is used to perform model checking operations on two models (LTS stored in two BCG format files)
		wrt an MCL property (stored in an MCL file).
	 */
	class FormulaChecker extends Checker
	{
		private static final String FORMULA_FILE = "formula.mcl";
		private final String formula;

		/**
		 * Sets up the FormulaChecker.
		 *
		 * @param model1 is the filename of the first model (LTS in a BCG file)
		 * @param model2 is the filename of the second model (LTS in a BCG file)
		 * @param formula is the filename of the property file (MCL file)
		 */
		public FormulaChecker(final String model1,
							  final String model2,
							  final String formula)
		{
			super(model1, model2);
			this.formula = formula;
		}

		@Override
		public void genSVL(final String filename)
		{
			final StringBuilder svlCommands = new StringBuilder(PyToJavaUtils.parametrize(
					SVL_FORMULA_CHECKING_TEMPLATE,
					this.model1,
					MCL_FORMULA)
			);

			if (!this.model1.equals(this.model2))
			{
				svlCommands.append(PyToJavaUtils.parametrize(
						SVL_FORMULA_CHECKING_TEMPLATE,
						this.model2,
						MCL_FORMULA)
				);
			}

			final String template = PyToJavaUtils.parametrize(SVL_CAESAR_TEMPLATE, svlCommands.toString());
			final File templateFile = new File(outputFolder + File.separator + filename);
			final PrintStream printStream;

			try
			{
				printStream = new PrintStream(templateFile);
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}

			printStream.print(template);
			printStream.close();
		}

		/**
		 * Checks if a formula yields on two models.
		 * Done by generating first an SVL script and then calling it.
		 *
		 * @return true if no errors were detected by SVL, false otherwise
		 */
		@Override
		public boolean call()
		{
			//Write formula to file
			final File formulaFile = new File(outputFolder + File.separator + FormulaChecker.FORMULA_FILE);
			final PrintStream printStream;

			try
			{
				printStream = new PrintStream(formulaFile);
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}

			printStream.print(this.formula); //TODO: not very clean ...
			printStream.flush();
			printStream.close();

			//Generate SVL
			this.genSVL(Checker.CHECKER_FILE);

			try
			{
				final String command = "svl";
				final String[] args = {Checker.CHECKER_FILE, "->", Checker.DIAGNOSTIC_FILE};
				final CommandManager commandManager = new CommandManager(command, new File(outputFolder), args);
				commandManager.execute();

				if (commandManager.returnValue() != ReturnCodes.TERMINATION_OK)
				{
					throw new RuntimeException("An error occurred during the execution of the SVL script:\n\n" + commandManager.stdErr());
				}

				if (!commandManager.stdOut().contains("TRUE")
					&& !commandManager.stdOut().contains("FALSE"))
				{
					throw new RuntimeException("An error occurred during the execution of the SVL script. See the .log" +
							" file for more information.");
				}

				final File resFile = new File(outputFolder + File.separator + DIAGNOSTIC_FILE);
				final PrintWriter printWriter;

				try
				{
					printWriter = new PrintWriter(resFile);
				}
				catch (IOException e)
				{
					throw new RuntimeException();
				}

				printWriter.println(commandManager.stdOut());
				printWriter.flush();
				printWriter.close();

				return commandManager.stdOut().contains("TRUE");
			}
			catch (IOException | InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
