package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutParser;
import fr.inria.convecs.optimus.aut.AutWriter;
import fr.inria.convecs.optimus.bpmn.BpmnParser;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessObject;
import fr.inria.convecs.optimus.bpmn.types.process.Task;
import fr.inria.convecs.optimus.nl_to_mc.exceptions.ExpectedException;
import fr.inria.convecs.optimus.py_to_java.ReturnCodes;
import fr.inria.convecs.optimus.util.CommandManager;
import fr.inria.convecs.optimus.util.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class Main
{
	private static final int RETRIEVING_LABELS_FAILED = 17;
	private static final int SPEC_LABELS_CONTAIN_RESERVED_LTL_KEYWORDS = 31;
	private static final int SPEC_LABELS_CONTAIN_RESERVED_LNT_KEYWORD = 35;
	private static final int WEAKTRACE_FAILED = 38;
	private static final int BCG_TO_AUT_FAILED = 39;
	private static final int BCG_SPEC_TO_AUT_FAILED = 40;
	private static final int WEAKTRACE_SPEC_FAILED = 41;
	private static final int CLTS_AUT_TO_BCG_FAILED = 42;
	private static final int CLTS_WEAKTRACE_FAILED = 43;
	private static final int CLTS_BCG_TO_AUT_FAILED = 44;
	private static final String BCG_SPECIFICATION = "problem.bcg";
	private static final String BCG_SPECIFICATION_WEAK = "problem_weak.bcg";
	private static final String AUT_SPECIFICATION = "problem.aut";
	private static final String AUT_SPECIFICATION_WEAK = "problem_weak.aut";
	private static final String BCG_PRODUCT = "product.bcg";
	private static final String WEAK_BCG_PRODUCT = "product_weak.bcg";
	private static final String WEAK_AUT_PRODUCT = "product_weak.aut";
	private static final String GRAPH_3D_CLTS = "clts.graph3D";
	private static final String AUT_CLTS = "clts.aut";
	private static final String BCG_CLTS = "clts.bcg";
	private static final String WEAK_BCG_CLTS = "clts_weak.bcg";
	private static final String AUT_FULL_CLTS = "clts_full.aut";
	private static final String AUTX_FULL_CLTS = "clts_full.autx";

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
			catch (Exception e)
			{
				//throw new IllegalStateException("Some necessary files have not been found or are not valid.");
				e.printStackTrace();
				System.exit(4);
			}

			final File workingDirectory = ((File) commandLineParser.get(CommandLineOption.WORKING_DIRECTORY));

			System.out.println("Reducing BCG product (weaktrace)...");
			MyOwnLogger.append("Reducing BCG product (weaktrace)...");
			final long bcgReductionStartTime = System.nanoTime();
			final String bcgOpenCommand = "bcg_open";
			final String[] bcgOpenArgs = new String[]{BCG_PRODUCT, "reductor", "-weaktrace", WEAK_BCG_PRODUCT};
			final CommandManager bcgOpenCommandManager = new CommandManager(bcgOpenCommand, workingDirectory, bcgOpenArgs);

			try
			{
				bcgOpenCommandManager.execute();
			}
			catch (IOException | InterruptedException e)
			{
				System.out.println("The weak reduction of the BCG product failed.");
				MyOwnLogger.append("The weak reduction of the BCG product failed.");
				System.exit(WEAKTRACE_FAILED);
			}

			if (bcgOpenCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
			{
				System.out.println("The weak reduction of the BCG product failed.");
				MyOwnLogger.append("The weak reduction of the BCG product failed.");
				System.exit(WEAKTRACE_FAILED);
			}

			final long bcgReductionEndTime = System.nanoTime();
			final long bcgReductionTime = bcgReductionEndTime - bcgReductionStartTime;
			MyOwnLogger.append("BCG product reduced in " + Utils.nanoSecToReadable(bcgReductionTime) + ".\n");
			System.out.println("BCG product reduced in " + Utils.nanoSecToReadable(bcgReductionTime) + ".\n");

			System.out.println("Converting BCG product to AUT...");
			MyOwnLogger.append("Converting BCG product to AUT...");
			final long bcgConversionStartTime = System.nanoTime();
			final String bcgIOCommand = "bcg_io";
			final String[] bcgIOArgs = new String[]{WEAK_BCG_PRODUCT, WEAK_AUT_PRODUCT};
			final CommandManager bcgIOCommandManager = new CommandManager(bcgIOCommand, workingDirectory, bcgIOArgs);

			try
			{
				bcgIOCommandManager.execute();
			}
			catch (IOException | InterruptedException e)
			{
				System.out.println("The translation of the BCG product to AUT failed.");
				MyOwnLogger.append("The translation of the BCG product to AUT failed.");
				System.exit(BCG_TO_AUT_FAILED);
			}

			if (bcgIOCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
			{
				System.out.println("The translation of the BCG product to AUT failed.");
				MyOwnLogger.append("The translation of the BCG product to AUT failed.");
				System.exit(BCG_TO_AUT_FAILED);
			}

			final long bcgConversionEndTime = System.nanoTime();
			final long bcgConversionTime = bcgConversionEndTime - bcgConversionStartTime;
			MyOwnLogger.append("BCG product converted to AUT in " + Utils.nanoSecToReadable(bcgConversionTime) + ".\n");
			System.out.println("BCG product converted to AUT in " + Utils.nanoSecToReadable(bcgConversionTime) + ".\n");

			System.out.println("Parsing AUT file...");
			MyOwnLogger.append("Parsing AUT file...");
			final long autParsingStartTime = System.nanoTime();
			final AutParser autParser = new AutParser(new File(workingDirectory + File.separator + WEAK_AUT_PRODUCT));
			final AutGraph autGraph = autParser.parse();
			final long autParsingEndTime = System.nanoTime();
			final long autParsingTime = autParsingEndTime - autParsingStartTime;
			MyOwnLogger.append("AUT file parsed in " + Utils.nanoSecToReadable(autParsingTime) + ".\n");
			System.out.println("AUT file parsed in " + Utils.nanoSecToReadable(autParsingTime) + ".\n");

			final File bpmnFile = (File) commandLineParser.get(CommandLineOption.BPMN_FILE);

			System.out.println("Computing specification labels...");
			MyOwnLogger.append("Computing specification labels...");
			final long labelsComputationStartTime = System.nanoTime();
			final Pair<ArrayList<String>, Integer> labelsAndReturnCode = Main.computeSpecLabels(bpmnFile);

			if (labelsAndReturnCode.getRight() != ReturnCodes.TERMINATION_OK)
			{
				System.out.println("The computation of the specification labels failed.");
				MyOwnLogger.append("The computation of the specification labels failed.");
				System.exit(labelsAndReturnCode.getRight());
			}

			final long labelsComputationEndTime = System.nanoTime();
			final long labelsComputationTime = labelsComputationEndTime - labelsComputationStartTime;
			MyOwnLogger.append("Labels \"" + labelsAndReturnCode.getLeft() + "\" computed in " + Utils.nanoSecToReadable(labelsComputationTime) + ".\n");
			System.out.println("Labels \"" + labelsAndReturnCode.getLeft() + "\" computed in " + Utils.nanoSecToReadable(labelsComputationTime) + ".\n");

			System.out.println("Building CLTS...");
			MyOwnLogger.append("Building CLTS...");
			final long cltsBuildingStartTime = System.nanoTime();
			final CLTSBuilderV2 cltsBuilder = new CLTSBuilderV2(autGraph, labelsAndReturnCode.getLeft());
			final AutGraph clts = cltsBuilder.buildCLTS();
			final long cltsBuildingEndTime = System.nanoTime();
			final long cltsBuildingTime = cltsBuildingEndTime - cltsBuildingStartTime;
			MyOwnLogger.append("CLTS built in " + Utils.nanoSecToReadable(cltsBuildingTime) + ".\n");
			System.out.println("CLTS built in " + Utils.nanoSecToReadable(cltsBuildingTime) + ".\n");

			System.out.println("Writing CLTS to file...");
			MyOwnLogger.append("Writing CLTS to file...");
			final long cltsDumpingStartTime = System.nanoTime();
			final AutWriter autWriter = new AutWriter(clts, new File(workingDirectory + File.separator + AUT_CLTS));
			autWriter.write();
			final long cltsDumpingEndTime = System.nanoTime();
			final long cltsDumpingTime = cltsDumpingEndTime - cltsDumpingStartTime;
			MyOwnLogger.append("CLTS written in " + Utils.nanoSecToReadable(cltsDumpingTime) + ".\n");
			System.out.println("CLTS written in " + Utils.nanoSecToReadable(cltsDumpingTime) + ".\n");

			System.out.println("Converting CLTS to BCG...");
			MyOwnLogger.append("Converting CLTS to BCG...");
			final long cltsAutToBcgStartTime = System.nanoTime();
			final String bcgIOCommand2 = "bcg_io";
			final String[] bcgIOArgs2 = new String[]{AUT_CLTS, BCG_CLTS};
			final CommandManager bcgIOCommandManager2 = new CommandManager(bcgIOCommand2, workingDirectory, bcgIOArgs2);

			try
			{
				bcgIOCommandManager2.execute();
			}
			catch (IOException | InterruptedException e)
			{
				System.out.println("The conversion of the CLTS from AUT to BCG failed.");
				MyOwnLogger.append("The conversion of the CLTS from AUT to BCG failed.");
				System.exit(CLTS_AUT_TO_BCG_FAILED);
			}

			if (bcgIOCommandManager2.returnValue() != ReturnCodes.TERMINATION_OK)
			{
				System.out.println("The conversion of the CLTS from AUT to BCG failed.");
				MyOwnLogger.append("The conversion of the CLTS from AUT to BCG failed.");
				System.exit(CLTS_AUT_TO_BCG_FAILED);
			}

			final long cltsAutToBcgEndTime = System.nanoTime();
			final long cltsAutToBcgTime = cltsAutToBcgEndTime - cltsAutToBcgStartTime;
			MyOwnLogger.append("CLTS converted to BCG in " + Utils.nanoSecToReadable(cltsAutToBcgTime) + ".\n");
			System.out.println("CLTS converted to BCG in " + Utils.nanoSecToReadable(cltsAutToBcgTime) + ".\n");

			System.out.println("Reducing CLTS (weaktrace)...");
			MyOwnLogger.append("Reducing CLTS (weaktrace)...");
			final long cltsReductionStartTime = System.nanoTime();
			final String bcgOpenCommand2 = "bcg_open";
			final String[] bcgOpenArgs2 = new String[]{BCG_CLTS, "reductor", "-weaktrace", WEAK_BCG_CLTS};
			final CommandManager bcgOpenCommandManager2 = new CommandManager(bcgOpenCommand2, workingDirectory, bcgOpenArgs2);

			try
			{
				bcgOpenCommandManager2.execute();
			}
			catch (IOException | InterruptedException e)
			{
				System.out.println("The weak reduction of the CLTS failed.");
				MyOwnLogger.append("The weak reduction of the CLTS failed.");
				System.exit(CLTS_WEAKTRACE_FAILED);
			}

			if (bcgOpenCommandManager2.returnValue() != ReturnCodes.TERMINATION_OK)
			{
				System.out.println("The weak reduction of the CLTS failed.");
				MyOwnLogger.append("The weak reduction of the CLTS failed.");
				System.exit(CLTS_WEAKTRACE_FAILED);
			}

			final long cltsReductionEndTime = System.nanoTime();
			final long cltsReductionTime = cltsReductionEndTime - cltsReductionStartTime;
			MyOwnLogger.append("CLTS reduced in " + Utils.nanoSecToReadable(cltsReductionTime) + ".\n");
			System.out.println("CLTS reduced in " + Utils.nanoSecToReadable(cltsReductionTime) + ".\n");

			System.out.println("Converting CLTS to AUT...");
			MyOwnLogger.append("Converting CLTS to AUT...");
			final long cltsBcgToAutStartTime = System.nanoTime();
			final String bcgIOCommand3 = "bcg_io";
			final String[] bcgIOArgs3 = new String[]{WEAK_BCG_CLTS, AUT_CLTS};
			final CommandManager bcgIOCommandManager3 = new CommandManager(bcgIOCommand3, workingDirectory, bcgIOArgs3);

			try
			{
				bcgIOCommandManager3.execute();
			}
			catch (IOException | InterruptedException e)
			{
				System.out.println("The conversion of the CLTS from BCG to AUT failed.");
				MyOwnLogger.append("The conversion of the CLTS from BCG to AUT failed.");
				System.exit(CLTS_BCG_TO_AUT_FAILED);
			}

			if (bcgIOCommandManager3.returnValue() != ReturnCodes.TERMINATION_OK)
			{
				System.out.println("The conversion of the CLTS from BCG to AUT failed.");
				MyOwnLogger.append("The conversion of the CLTS from BCG to AUT failed.");
				System.exit(CLTS_BCG_TO_AUT_FAILED);
			}

			final long cltsBcgToAutEndTime = System.nanoTime();
			final long cltsBcgToAutTime = cltsBcgToAutEndTime - cltsBcgToAutStartTime;
			MyOwnLogger.append("CLTS converted in " + Utils.nanoSecToReadable(cltsBcgToAutTime) + ".\n");
			System.out.println("CLTS converted in " + Utils.nanoSecToReadable(cltsBcgToAutTime) + ".\n");

			System.out.println("Parsing AUT file...");
			MyOwnLogger.append("Parsing AUT file...");
			final long cltsParsingStartTime = System.nanoTime();
			final AutParser cltsParser = new AutParser(new File(workingDirectory + File.separator + AUT_CLTS));
			final AutGraph cltsGraph = cltsParser.parse();
			final long cltsParsingEndTime = System.nanoTime();
			final long cltsParsingTime = cltsParsingEndTime - cltsParsingStartTime;
			MyOwnLogger.append("AUT file parsed in " + Utils.nanoSecToReadable(cltsParsingTime) + ".\n");
			System.out.println("AUT file parsed in " + Utils.nanoSecToReadable(cltsParsingTime) + ".\n");

			//MANDATORY TO HAVE THE GREEN PART OF THE CLTS
			System.out.println("Reducing BCG specification (weaktrace)...");
			MyOwnLogger.append("Reducing BCG specification (weaktrace)...");
			final long bcgSpecificationReductionStartTime = System.nanoTime();
			final String bcgOpenCommand3 = "bcg_open";
			final String[] bcgOpenArgs3 = new String[]{BCG_SPECIFICATION, "reductor", "-weaktrace", BCG_SPECIFICATION_WEAK};
			final CommandManager bcgOpenCommandManager3 = new CommandManager(bcgOpenCommand3, workingDirectory, bcgOpenArgs3);

			try
			{
				bcgOpenCommandManager3.execute();
			}
			catch (IOException | InterruptedException e)
			{
				System.out.println("The weak reduction of the BCG specification failed.");
				MyOwnLogger.append("The weak reduction of the BCG specification failed.");
				System.exit(WEAKTRACE_SPEC_FAILED);
			}

			if (bcgOpenCommandManager3.returnValue() != ReturnCodes.TERMINATION_OK)
			{
				System.out.println("The weak reduction of the BCG specification failed.");
				MyOwnLogger.append("The weak reduction of the BCG specification failed.");
				System.exit(WEAKTRACE_SPEC_FAILED);
			}

			final long bcgSpecificationReductionEndTime = System.nanoTime();
			final long bcgSpecificationReductionTime = bcgSpecificationReductionEndTime - bcgSpecificationReductionStartTime;
			MyOwnLogger.append("BCG specification reduced in " + Utils.nanoSecToReadable(bcgSpecificationReductionTime) + ".\n");
			System.out.println("BCG specification reduced in " + Utils.nanoSecToReadable(bcgSpecificationReductionTime) + ".\n");

			System.out.println("Converting BCG specification to AUT...");
			MyOwnLogger.append("Converting BCG specification to AUT...");
			final long bcgSpecificationConversionStartTime = System.nanoTime();
			final String bcgIOCommand4 = "bcg_io";
			final String[] bcgIOArgs4 = new String[]{BCG_SPECIFICATION_WEAK, AUT_SPECIFICATION_WEAK};
			final CommandManager bcgIOCommandManager4 = new CommandManager(bcgIOCommand4, workingDirectory, bcgIOArgs4);

			try
			{
				bcgIOCommandManager4.execute();
			}
			catch (IOException | InterruptedException e)
			{
				System.out.println("The translation of the BCG specification to AUT failed.");
				MyOwnLogger.append("The translation of the BCG specification to AUT failed.");
				System.exit(BCG_SPEC_TO_AUT_FAILED);
			}

			if (bcgIOCommandManager4.returnValue() != ReturnCodes.TERMINATION_OK)
			{
				System.out.println("The translation of the BCG specification to AUT failed.");
				MyOwnLogger.append("The translation of the BCG specification to AUT failed.");
				System.exit(BCG_SPEC_TO_AUT_FAILED);
			}

			final long bcgSpecificationConversionEndTime = System.nanoTime();
			final long bcgSpecificationConversionTime = bcgSpecificationConversionEndTime - bcgSpecificationConversionStartTime;
			MyOwnLogger.append("BCG specification converted in " + Utils.nanoSecToReadable(bcgSpecificationConversionTime) + ".\n");
			System.out.println("BCG specification converted in " + Utils.nanoSecToReadable(bcgSpecificationConversionTime) + ".\n");

			System.out.println("Parsing AUT specification...");
			MyOwnLogger.append("Parsing AUT specification...");
			final long autSpecParsingStartTime = System.nanoTime();
			final AutParser autSpecParser = new AutParser(new File(workingDirectory + File.separator + AUT_SPECIFICATION_WEAK));
			final AutGraph specGraph = autSpecParser.parse();
			final long autSpecParsingEndTime = System.nanoTime();
			final long autSpecParsingTime = autSpecParsingEndTime - autSpecParsingStartTime;
			MyOwnLogger.append("AUT specification parsed in " + Utils.nanoSecToReadable(autSpecParsingTime) + ".\n");
			System.out.println("AUT specification parsed in " + Utils.nanoSecToReadable(autSpecParsingTime) + ".\n");

			System.out.println("Building full CLTS...");
			MyOwnLogger.append("Building full CLTS...");
			final long fullCltsBuildingStartTime = System.nanoTime();
			final FullCLTSBuilder fullCLTSBuilder = new FullCLTSBuilder(specGraph, cltsGraph);
			final AutGraph fullCLTS = fullCLTSBuilder.build();
			final long fullCltsBuildingEndTime = System.nanoTime();
			final long fullCltsBuildingTime = fullCltsBuildingEndTime - fullCltsBuildingStartTime;
			MyOwnLogger.append("Full CLTS built in " + Utils.nanoSecToReadable(fullCltsBuildingTime) + ".\n");
			System.out.println("Full CLTS built in " + Utils.nanoSecToReadable(fullCltsBuildingTime) + ".\n");

			System.out.println("Setting CLTS colors...");
			MyOwnLogger.append("Setting CLTS colors...");
			final long cltsColorSettingStartTime = System.nanoTime();
			final CLTSColorManager cltsColorManager = new CLTSColorManager(fullCLTS);
			cltsColorManager.setProperColors();
			final long cltsColorSettingEndTime = System.nanoTime();
			final long cltsColorSettingTime = cltsColorSettingEndTime - cltsColorSettingStartTime;
			MyOwnLogger.append("CLTS colors set in " + Utils.nanoSecToReadable(cltsColorSettingTime) + ".\n");
			System.out.println("CLTS colors set in " + Utils.nanoSecToReadable(cltsColorSettingTime) + ".\n");

			System.out.println("Writing full CLTS to file...");
			MyOwnLogger.append("Writing full CLTS to file...");
			final long fullCltsDumpingStartTime = System.nanoTime();
			final AutWriter autWriter2 = new AutWriter(fullCLTS, new File(workingDirectory + File.separator + AUT_FULL_CLTS));
			autWriter2.write();
			final AutWriter autxWriter = new AutWriter(fullCLTS, new File(workingDirectory + File.separator + AUTX_FULL_CLTS), true);
			autxWriter.write();
			final long fullCltsDumpingEndTime = System.nanoTime();
			final long fullCltsDumpingTime = fullCltsDumpingEndTime - fullCltsDumpingStartTime;
			MyOwnLogger.append("Full CLTS written in " + Utils.nanoSecToReadable(fullCltsDumpingTime) + ".\n");
			System.out.println("Full CLTS written in " + Utils.nanoSecToReadable(fullCltsDumpingTime) + ".\n");

			if ((Boolean) commandLineParser.get(CommandLineOption.TRUNCATE_CLTS))
			{
				System.out.println("Truncating full CLTS...");
				MyOwnLogger.append("Truncating full CLTS...");
				final long cltsTruncationStartTime = System.nanoTime();
				fullCLTSBuilder.truncate();
				final long cltsTruncationEndTime = System.nanoTime();
				final long cltsTruncationTime = cltsTruncationEndTime - cltsTruncationStartTime;
				MyOwnLogger.append("Full CLTS truncated in " + Utils.nanoSecToReadable(cltsTruncationTime) + ".\n");
				System.out.println("Full CLTS truncated in " + Utils.nanoSecToReadable(cltsTruncationTime) + ".\n");
			}

			System.out.println("Converting CLTS to 3DForceGraph...");
			MyOwnLogger.append("Converting CLTS to 3DForceGraph...");
			final long cltsConversionStartTime = System.nanoTime();
			final Aut2Force3DGraph aut2Force3DGraph = new Aut2Force3DGraph(new File(workingDirectory + File.separator + GRAPH_3D_CLTS), fullCLTS);
			aut2Force3DGraph.generateForce3DGraphFile();
			final long cltsConversionEndTime = System.nanoTime();
			final long cltsConversionTime = cltsConversionEndTime - cltsConversionStartTime;
			MyOwnLogger.append("CLTS converted in " + Utils.nanoSecToReadable(cltsConversionTime) + ".\n");
			System.out.println("CLTS converted in " + Utils.nanoSecToReadable(cltsConversionTime) + ".\n");

			final long programEndTime = System.nanoTime();
			final long programTime = programEndTime - programStartTime;
			MyOwnLogger.append("The CLTS generation process took " + Utils.nanoSecToReadable(programTime) + ".\n");
			System.out.println("The CLTS generation process took " + Utils.nanoSecToReadable(programTime) + ".\n");
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
			return Pair.of(new ArrayList<>(), RETRIEVING_LABELS_FAILED);
		}

		final ArrayList<String> labels = new ArrayList<>();

		for (BpmnProcessObject object : bpmnParser.bpmnProcess().objects())
		{
			if (object instanceof Task)
			{
				if (LTLKeywords.ALL_KEYWORDS.contains(object.name().toUpperCase()))
				{
					System.out.println("The specification contains tasks whose labels are reserved LTL keywords.");
					return Pair.of(new ArrayList<>(), SPEC_LABELS_CONTAIN_RESERVED_LTL_KEYWORDS);
				}

				if (LNTKeywords.ALL_KEYWORDS.contains(object.name().toUpperCase()))
				{
					System.out.println("The specification contains tasks whose labels are reserved LNT keywords.");
					return Pair.of(new ArrayList<>(), SPEC_LABELS_CONTAIN_RESERVED_LNT_KEYWORD);
				}

				labels.add(object.name().toUpperCase());
			}
		}

		return Pair.of(labels, ReturnCodes.TERMINATION_OK);
	}
}