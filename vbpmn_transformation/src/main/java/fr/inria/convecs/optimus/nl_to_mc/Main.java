package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutParser;
import fr.inria.convecs.optimus.aut.AutWriter;
import fr.inria.convecs.optimus.bpmn.BpmnParser;
import fr.inria.convecs.optimus.bpmn.graph.Graph;
import fr.inria.convecs.optimus.bpmn.graph.GraphToList;
import fr.inria.convecs.optimus.bpmn.types.process.BpmnProcessObject;
import fr.inria.convecs.optimus.bpmn.types.process.Task;
import fr.inria.convecs.optimus.bpmn.writing.generation.GraphicalGenerationWriter;
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
	public static final boolean LOCAL_TESTING = false;
	private static final boolean MINIMIZE_CLTS = true;
	private static final int RETRIEVING_LABELS_FAILED = 17;
	private static final int SPEC_LABELS_CONTAIN_RESERVED_LTL_KEYWORDS = 31;
	private static final int SPEC_LABELS_CONTAIN_RESERVED_LNT_KEYWORD = 35;
	private static final int WEAKTRACE_FAILED = 38;
	private static final int BCG_PRODUCT_TO_AUT_FAILED = 39;
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

			System.out.println("Reducing BCG specification (weaktrace)...");
			final long bcgReductionStartTime = System.nanoTime();
			final String bcgOpenCommand = "bcg_open";
			final String[] bcgOpenArgs = new String[]{BCG_SPECIFICATION, "reductor", "-weaktrace", BCG_SPECIFICATION_WEAK};
			final CommandManager bcgOpenCommandManager = new CommandManager(bcgOpenCommand, workingDirectory, bcgOpenArgs);

			try
			{
				bcgOpenCommandManager.execute();
			}
			catch (IOException | InterruptedException e)
			{
				System.exit(WEAKTRACE_SPEC_FAILED);
			}

			if (bcgOpenCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
			{
				System.exit(WEAKTRACE_SPEC_FAILED);
			}

			final long bcgReductionEndTime = System.nanoTime();
			final long bcgReductionTime = bcgReductionEndTime - bcgReductionStartTime;
			System.out.println("BCG specification reduced in " + Utils.nanoSecToReadable(bcgReductionTime) + ".\n");

			System.out.println("Converting BCG specification to AUT...");
			final long bcgConversionStartTime = System.nanoTime();
			final String bcgIOCommand = "bcg_io";
			final String[] bcgIOArgs = new String[]{BCG_SPECIFICATION_WEAK, AUT_SPECIFICATION_WEAK};
			final CommandManager bcgIOCommandManager = new CommandManager(bcgIOCommand, workingDirectory, bcgIOArgs);

			try
			{
				bcgIOCommandManager.execute();
			}
			catch (IOException | InterruptedException e)
			{
				System.exit(BCG_SPEC_TO_AUT_FAILED);
			}

			if (bcgIOCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
			{
				System.exit(BCG_SPEC_TO_AUT_FAILED);
			}

			final long bcgConversionEndTime = System.nanoTime();
			final long bcgConversionTime = bcgConversionEndTime - bcgConversionStartTime;
			System.out.println("BCG specification converted in " + Utils.nanoSecToReadable(bcgConversionTime) + ".\n");

			System.out.println("Parsing AUT specification...");
			final long autSpecParsingStartTime = System.nanoTime();
			final AutParser autSpecParser = new AutParser(new File(workingDirectory + File.separator + AUT_SPECIFICATION_WEAK));
			final AutGraph specGraph = autSpecParser.parse();
			final long autSpecParsingEndTime = System.nanoTime();
			final long autSpecParsingTime = autSpecParsingEndTime - autSpecParsingStartTime;
			System.out.println("AUT specification parsed in " + Utils.nanoSecToReadable(autSpecParsingTime) + ".\n");

			if (MINIMIZE_CLTS)
			{
				System.out.println("Converting CLTS to BCG...");
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
					System.exit(CLTS_AUT_TO_BCG_FAILED);
				}

				if (bcgIOCommandManager2.returnValue() != ReturnCodes.TERMINATION_OK)
				{
					System.exit(CLTS_AUT_TO_BCG_FAILED);
				}

				final long cltsAutToBcgEndTime = System.nanoTime();
				final long cltsAutToBcgTime = cltsAutToBcgEndTime - cltsAutToBcgStartTime;
				System.out.println("CLTS converted to BCG in " + Utils.nanoSecToReadable(cltsAutToBcgTime) + ".\n");

				System.out.println("Reducing CLTS (weaktrace)...");
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
					System.exit(CLTS_WEAKTRACE_FAILED);
				}

				if (bcgOpenCommandManager2.returnValue() != ReturnCodes.TERMINATION_OK)
				{
					System.exit(CLTS_WEAKTRACE_FAILED);
				}

				final long cltsReductionEndTime = System.nanoTime();
				final long cltsReductionTime = cltsReductionEndTime - cltsReductionStartTime;
				System.out.println("CLTS reduced in " + Utils.nanoSecToReadable(cltsReductionTime) + ".\n");

				System.out.println("Converting CLTS to AUT...");
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
					System.exit(CLTS_BCG_TO_AUT_FAILED);
				}

				if (bcgIOCommandManager3.returnValue() != ReturnCodes.TERMINATION_OK)
				{
					System.exit(CLTS_BCG_TO_AUT_FAILED);
				}

				final long cltsBcgToAutEndTime = System.nanoTime();
				final long cltsBcgToAutTime = cltsBcgToAutEndTime - cltsBcgToAutStartTime;
				System.out.println("CLTS converted in " + Utils.nanoSecToReadable(cltsBcgToAutTime) + ".\n");
			}

			System.out.println("Parsing CLTS...");
			final long cltsParsingStartTime = System.nanoTime();
			final AutParser autCltsParser = new AutParser(new File(workingDirectory + File.separator + AUT_CLTS));
			final AutGraph cltsGraph = autCltsParser.parse();
			final long cltsParsingEndTime = System.nanoTime();
			final long cltsParsingTime = cltsParsingEndTime - cltsParsingStartTime;
			System.out.println("CLTS parsed in " + Utils.nanoSecToReadable(cltsParsingTime) + ".\n");

			System.out.println("Building full CLTS...");
			final long cltsBuildingStartTime = System.nanoTime();
			final FullCLTSBuilder fullCLTSBuilder = new FullCLTSBuilder(specGraph, cltsGraph);
			final AutGraph fullCLTS = fullCLTSBuilder.build();
			final long cltsBuildingEndTime = System.nanoTime();
			final long cltsBuildingTime = cltsBuildingEndTime - cltsBuildingStartTime;
			System.out.println("Full CLTS built in " + Utils.nanoSecToReadable(cltsBuildingTime) + ".\n");

			System.out.println("Setting CLTS colors...");
			final long cltsColorSettingStartTime = System.nanoTime();
			final CLTSColorManager cltsColorManager = new CLTSColorManager(fullCLTS);
			cltsColorManager.setProperColors();
			final long cltsColorSettingEndTime = System.nanoTime();
			final long cltsColorSettingTime = cltsColorSettingEndTime - cltsColorSettingStartTime;
			System.out.println("CLTS colors set in " + Utils.nanoSecToReadable(cltsColorSettingTime) + ".\n");

			System.out.println("Writing CLTS to file...");
			final long cltsDumpingStartTime = System.nanoTime();
			final AutWriter autWriter = new AutWriter(fullCLTS, new File(workingDirectory + File.separator + AUT_FULL_CLTS), false);
			autWriter.write();
			final AutWriter autxWriter = new AutWriter(fullCLTS, new File(workingDirectory + File.separator + AUTX_FULL_CLTS), true);
			autxWriter.write();
			final long cltsDumpingEndTime = System.nanoTime();
			final long cltsDumpingTime = cltsDumpingEndTime - cltsDumpingStartTime;
			System.out.println("CLTS written in " + Utils.nanoSecToReadable(cltsDumpingTime) + ".\n");

			final CLTStoBPMN cltStoBPMN = new CLTStoBPMN(fullCLTS);
			final Graph bpmnProcess = cltStoBPMN.convert();
			System.out.println(bpmnProcess.toString());

			final GraphToList graphToList = new GraphToList(bpmnProcess);
			graphToList.convert();
			final GraphicalGenerationWriter graphicalGenerationWriter = new GraphicalGenerationWriter(
					commandLineParser,
					graphToList.objectsList(),
					"colored"
			);
			graphicalGenerationWriter.write();
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
	}
}