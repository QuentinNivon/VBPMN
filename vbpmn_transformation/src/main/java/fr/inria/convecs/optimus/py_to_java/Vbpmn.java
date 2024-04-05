package fr.inria.convecs.optimus.py_to_java;

import fr.inria.convecs.optimus.py_to_java.cadp_compliance._2024c.BpmnTypesBuilder;
import fr.inria.convecs.optimus.util.PifUtil;
import fr.inria.convecs.optimus.validator.ModelValidator;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Triple;
import org.jgrapht.alg.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Vbpmn
{
	/**
	 * Porting of the ``vbpmn.py'' code to Java code
	 * @author Quentin NIVON (quentin.nivon@inria.fr)
	 */

	private Logger logger = LoggerFactory.getLogger(Vbpmn.class);

	/*
	 	Command to call SVL.
	 	First argument is the script, second one is the result file.
	 */
	private static final String SVL_CALL_COMMAND = "svl {0} > {1}";

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

	public Vbpmn(final String[] sysArgs,
				 final String outputFolder)
	{
		this.sysArgs = sysArgs;
		this.outputFolder = outputFolder;
	}

	public boolean execute()
	{
		final long startTime = System.nanoTime();

		//Initialise parser
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
				throw new RuntimeException("missing formula in presence of property based comparison.");
			}
			if (!OPERATIONS_PROPERTY.contains(args.getString("operation"))
					&& args.get("formula") != null)
			{
				System.out.println("formula in presence of equivalence based comparison will not be used.");
			}
		}
		catch (ArgumentParserException e)
		{
			parser.printHelp();
			throw new IllegalStateException();
		}

		//Check if process is balanced or not
		final File pif1 = new File((String) args.getList("models").get(0));
		final File pif2 = new File((String) args.getList("models").get(1));
		final boolean processIsBalanced = PifUtil.isPifBalanced(pif1) && PifUtil.isPifBalanced(pif2);

		final String cadpVersionDir;

		try
		{
			final Process cadpLibCommand = Runtime.getRuntime().exec("cadp_lib -1");
			final BufferedReader stdInput = new BufferedReader(new InputStreamReader(cadpLibCommand.getInputStream()));
			final BufferedReader stdError = new BufferedReader(new InputStreamReader(cadpLibCommand.getErrorStream()));
			String line;

			// Read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			final StringBuilder stdOutBuilder = new StringBuilder();
			while ((line = stdInput.readLine()) != null)
			{
				stdOutBuilder.append(line);
			}
			System.out.println(stdOutBuilder);

			// Read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			final StringBuilder stdErrBuilder = new StringBuilder();
			while ((line = stdError.readLine()) != null)
			{
				stdErrBuilder.append(line);
			}
			System.out.println(stdErrBuilder);
			cadpLibCommand.destroy();

			//Split answer by spaces
			String[] splitAnswer = stdOutBuilder.toString().split("\\s+");
			//The 2nd element is the version code, i.e. "2023k"
			cadpVersionDir = "_" + splitAnswer[1].replace(" ", "").replace("-", "");
			System.out.println("CADP VERSION: " + cadpVersionDir);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		//Load the good Pif2Lnt class (depending on the CADP version)
		final Pif2LntGeneric pif2lnt;
		try
		{
			//Load the Pif2Lnt class located in the package corresponding to the good version
			final Class<? extends Pif2LntGeneric> pif2LntClass = (Class<? extends Pif2LntGeneric>)
					Class.forName("fr.inria.convecs.optimus.py_to_java.cadp_compliance." + cadpVersionDir + ".Pif2Lnt");
			final Constructor<? extends Pif2LntGeneric> pif2LntConstructor = pif2LntClass.getDeclaredConstructor();
			pif2lnt = pif2LntConstructor.newInstance();
			pif2lnt.setBalance(processIsBalanced);
			pif2lnt.setOutputFolder(this.outputFolder);
		}
		catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
			   IllegalAccessException e)
		{
			System.out.println("Please make sure that the path \"fr.inria.convecs.optimus.py_to_java.cadp_compliance."
					+ cadpVersionDir + "\" exists and contains \"Pif2Lnt.java\"). If yes, please send an email to the staff.");
			throw new RuntimeException(e);
		}

		//Load the good BpmnTypesBuilder class (depending on the CADP version)
		try
		{
			//Load the Pif2Lnt class located in the package corresponding to the good version
			final Class<? extends BpmnTypesBuilderGeneric> bpmnTypesBuilderClass = (Class<? extends BpmnTypesBuilderGeneric>)
					Class.forName("fr.inria.convecs.optimus.py_to_java.cadp_compliance." + cadpVersionDir + ".BpmnTypesBuilder");
			final Constructor<? extends BpmnTypesBuilderGeneric> bpmnTypesBuilderConstructor = bpmnTypesBuilderClass.getDeclaredConstructor();
			final BpmnTypesBuilderGeneric bpmnTypesBuilder = bpmnTypesBuilderConstructor.newInstance();
			bpmnTypesBuilder.setOutputDirectory(outputFolder);
			bpmnTypesBuilder.dumpBpmnTypesFile();
		}
		catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
			   IllegalAccessException e)
		{
			System.out.println("Please make sure that the path \"fr.inria.convecs.optimus.py_to_java.cadp_compliance."
					+ cadpVersionDir + "\" exists and contains \"BpmnTypesBuilder.java\"). If yes, please send an email to the staff.");
			throw new RuntimeException(e);
		}

		//If in lazy mode, rebuild the BCG files only if needed
		final boolean lazy = args.get("--lazy") != null
				&& args.getBoolean("--lazy");

		//(Re)build the first model
		final String pifModel1 = (String) args.getList("models").get(0);
		final Triple<Integer, String, Collection<String>> result1 = lazy ? pif2lnt.load(pifModel1) : pif2lnt.generate(pifModel1);
		//(Re)build the second model
		final String pifModel2 = (String) args.getList("models").get(1);
		final Triple<Integer, String, Collection<String>> result2 = lazy ? pif2lnt.load(pifModel2) : pif2lnt.generate(pifModel2);

		//If one of the models could not be loaded => ERROR
		if (result1.getLeft() != ReturnCodes.TERMINATION_OK
			|| result2.getLeft() != ReturnCodes.TERMINATION_OK)
		{
			System.out.println("Error in loading models.");
			throw new IllegalStateException();
		}

		/*
			Checks if we compare up to a context.
			TODO Gwen : refine synchronization sets computation (_EM vs _REC)
			TODO Pascal : what about if we have hiding and/or renaming + context-awareness? different alphabets should
			 be used?
		 */

		final ArrayList<String> syncSet1 = new ArrayList<>();
		final ArrayList<String> syncSet2 = new ArrayList<>();

		if (args.get("--context") != null)
		{
			final String pifContextModel = args.getString("--context");
			System.out.println("Converting \"" + pifContextModel + "\" to LTS...");
			Triple<Integer, String, Collection<String>> result = lazy ? pif2lnt.load(pifContextModel) : pif2lnt.generate(pifContextModel);

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

		final Checker comparator;

		//Check whether we compare based on an equivalence or based on a property
		if (OPERATIONS_COMPARISON.contains(args.getString("operation")))
		{
			comparator = new ComparisonChecker(
					result1.getMiddle(),
					result2.getMiddle(),
					args.getString("operation"),
					args.getList("--hiding"),
					args.get("--exposemode") != null && args.getBoolean("--exposemode"),
					args.get("--renaming") == null ? new HashMap<>() : args.get("--renaming"),
					args.getString("--renamed") == null ? "all" : args.getString("--renamed"),
					new ArrayList[]{syncSet1, syncSet2}
			);
		}
		else
		{
			comparator = new FormulaChecker(
					result1.getMiddle(),
					result2.getMiddle(),
					args.getString("--formula")
			);
		}

		//Perform comparison and process result
		final boolean result = comparator.call();
		final long endTime = System.nanoTime();
		final long totalTime = endTime - startTime;

		final File templateFile = new File(outputFolder + File.separator + "time.txt");
		final PrintStream printStream;

		try
		{
			printStream = new PrintStream(templateFile);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}

		printStream.print("The execution took " + totalTime + " ns");
		printStream.close();

		final int returnValue = result ? ReturnCodes.TERMINATION_OK : ReturnCodes.TERMINATION_ERROR;
		System.out.println("Result: " + result);

		return result;
	}

	/*
		This class represents the superclass of all classes performing some formal checking on two LTS models (stores
		in BCG format files)
	 */
	class Checker
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
		public void genSVL(final String filename)
		{
			throw new NotImplementedException("genSVL() is not implemented!"); //TODO ``in class %s" % self.__class__.__name__)''
		}

		/**
		 * Reification of a Checker as a callable object.
		 *
		 * TODO
		 */
		public boolean call()
		{
			return true;
		}
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
				throw new RuntimeException("Operation should be in " + OPERATIONS + " and \"_\" is only for hiding. " +
						"Received \"" + operation + "\".");
			}

			if (!SELECTIONS.contains(renamed))
			{
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
		 * Generates SVL script to check the property on both models.
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
				//TODO CHECK FUNCTIONEMENT
				final Process svlCommand = Runtime.getRuntime().exec(PyToJavaUtils.parametrize(
					SVL_CALL_COMMAND,
					Checker.CHECKER_FILE,
					Checker.DIAGNOSTIC_FILE
				), null, new File(outputFolder));
				final int exitValue = svlCommand.waitFor();

				if (exitValue != ReturnCodes.TERMINATION_OK)
				{
					throw new RuntimeException("An error occurred during the execution of the SVL script.");
				}

				final Process grepCommand = Runtime.getRuntime().exec(
						"grep TRUE " + Checker.DIAGNOSTIC_FILE, null, new File(outputFolder)
				);
				final int exitValue2 = grepCommand.waitFor();

				return exitValue2 != ReturnCodes.TERMINATION_ERROR;
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
			printStream.close();

			//Generate SVL
			this.genSVL(Checker.CHECKER_FILE);

			try
			{
				//TODO CHECK FUNCTIONEMENT
				final Process svlCommand = Runtime.getRuntime().exec(PyToJavaUtils.parametrize(
					SVL_CALL_COMMAND,
					Checker.CHECKER_FILE,
					Checker.DIAGNOSTIC_FILE
				), null, new File(outputFolder));
				final int exitValue = svlCommand.waitFor();

				if (exitValue != ReturnCodes.TERMINATION_OK)
				{
					throw new RuntimeException("An error occurred during the execution of the SVL script.");
				}

				final Process grepCommand = Runtime.getRuntime().exec(
						"grep FALSE " + Checker.DIAGNOSTIC_FILE, null, new File(outputFolder)
				);
				final int exitValue2 = grepCommand.waitFor();

				return exitValue2 == ReturnCodes.TERMINATION_ERROR;
			}
			catch (IOException | InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
