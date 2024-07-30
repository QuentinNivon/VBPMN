package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutParser;
import fr.inria.convecs.optimus.aut.AutWriter;
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
	private static final int RETRIEVING_LABELS_FAILED = 17;
	private static final int SPEC_LABELS_CONTAIN_RESERVED_LTL_KEYWORDS = 31;
	private static final int SPEC_LABELS_CONTAIN_RESERVED_LNT_KEYWORD = 35;
	private static final int WEAKTRACE_FAILED = 38;
	private static final int BCG_TO_AUT_FAILED = 39;
	private static final String BCG_PRODUCT = "product.bcg";
	private static final String WEAK_BCG_PRODUCT = "product_weak.bcg";
	private static final String WEAK_AUT_PRODUCT = "product_weak.aut";
	private static final String GRAPH_3D_CLTS = "clts.graph3D";
	private static final String AUT_CLTS = "clts.aut";

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

			System.out.println("Reducing BCG product (weaktrace)...");
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
				System.exit(WEAKTRACE_FAILED);
			}

			if (bcgOpenCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
			{
				System.exit(WEAKTRACE_FAILED);
			}

			final long bcgReductionEndTime = System.nanoTime();
			final long bcgReductionTime = bcgReductionEndTime - bcgReductionStartTime;
			System.out.println("BCG product reduced in " + Utils.nanoSecToReadable(bcgReductionTime) + ".\n");

			System.out.println("Converting BCG product to AUT...");
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
				System.exit(BCG_TO_AUT_FAILED);
			}

			if (bcgIOCommandManager.returnValue() != ReturnCodes.TERMINATION_OK)
			{
				System.exit(BCG_TO_AUT_FAILED);
			}

			final long bcgConversionEndTime = System.nanoTime();
			final long bcgConversionTime = bcgConversionEndTime - bcgConversionStartTime;
			System.out.println("BCG product converted to AUT in " + Utils.nanoSecToReadable(bcgConversionTime) + ".\n");

			System.out.println("Parsing AUT file...");
			final long autParsingStartTime = System.nanoTime();
			final AutParser autParser = new AutParser(new File(workingDirectory + File.separator + WEAK_AUT_PRODUCT));
			final AutGraph autGraph = autParser.parse();
			final long autParsingEndTime = System.nanoTime();
			final long autParsingTime = autParsingEndTime - autParsingStartTime;
			System.out.println("AUT file parsed in " + Utils.nanoSecToReadable(autParsingTime) + ".\n");

			final File bpmnFile = (File) commandLineParser.get(CommandLineOption.BPMN_FILE);

			System.out.println("Computing specification labels...");
			final long labelsComputationStartTime = System.nanoTime();
			final Pair<ArrayList<String>, Integer> labelsAndReturnCode = Main.computeSpecLabels(bpmnFile);

			if (labelsAndReturnCode.getRight() != ReturnCodes.TERMINATION_OK)
			{
				System.exit(labelsAndReturnCode.getRight());
			}

			final long labelsComputationEndTime = System.nanoTime();
			final long labelsComputationTime = labelsComputationEndTime - labelsComputationStartTime;
			System.out.println("Labels \"" + labelsAndReturnCode.getLeft() + "\" computed in " + Utils.nanoSecToReadable(labelsComputationTime) + ".\n");

			System.out.println("Building CLTS...");
			final long cltsBuildingStartTime = System.nanoTime();
			final CLTSBuilderV2 cltsBuilder = new CLTSBuilderV2(autGraph, labelsAndReturnCode.getLeft());
			final AutGraph clts = cltsBuilder.buildCLTS();
			final long cltsBuildingEndTime = System.nanoTime();
			final long cltsBuildingTime = cltsBuildingEndTime - cltsBuildingStartTime;
			System.out.println("CLTS built in " + Utils.nanoSecToReadable(cltsBuildingTime) + ".\n");

			System.out.println("Writing CLTS to file...");
			final long cltsDumpingStartTime = System.nanoTime();
			final AutWriter autWriter = new AutWriter(clts, new File(workingDirectory + File.separator + AUT_CLTS));
			autWriter.write();
			final long cltsDumpingEndTime = System.nanoTime();
			final long cltsDumpingTime = cltsDumpingEndTime - cltsDumpingStartTime;
			System.out.println("CLTS written in " + Utils.nanoSecToReadable(cltsDumpingTime) + ".\n");

			System.out.println("Converting CLTS to 3DForceGraph...");
			final long cltsConversionStartTime = System.nanoTime();
			final Aut2Force3DGraph aut2Force3DGraph = new Aut2Force3DGraph(new File(workingDirectory + File.separator + GRAPH_3D_CLTS), clts);
			aut2Force3DGraph.generateForce3DGraphFile();
			final long cltsConversionEndTime = System.nanoTime();
			final long cltsConversionTime = cltsConversionEndTime - cltsConversionStartTime;
			System.out.println("CLTS converted in " + Utils.nanoSecToReadable(cltsConversionTime) + ".\n");
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

				labels.add(object.name());
			}
		}

		return Pair.of(labels, ReturnCodes.TERMINATION_OK);
	}
}