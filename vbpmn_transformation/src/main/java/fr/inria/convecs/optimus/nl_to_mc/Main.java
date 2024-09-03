package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutParser;
import fr.inria.convecs.optimus.bpmn.BpmnParser;
import fr.inria.convecs.optimus.bpmn.graph.Graph;
import fr.inria.convecs.optimus.bpmn.graph.GraphToList;
import fr.inria.convecs.optimus.bpmn.writing.generation.GraphicalGenerationWriter;
import fr.inria.convecs.optimus.nl_to_mc.exceptions.ExpectedException;
import fr.inria.convecs.optimus.util.Utils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;

public class Main
{
	public static final boolean LOCAL_SITE = false;
	public static final boolean LOCAL_TESTING = false;
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
				return;
			}

			final File workingDirectory = ((File) commandLineParser.get(CommandLineOption.WORKING_DIRECTORY));

			System.out.println("Parsing original BPMN process...");
			MyOwnLogger.append("Parsing original BPMN process...");
			final long bpmnProcessParsingStartTime = System.nanoTime();
			final BpmnParser bpmnParser;

			try
			{
				bpmnParser = new BpmnParser((File) commandLineParser.get(CommandLineOption.BPMN_FILE));
				bpmnParser.parse();
			}
			catch (ParserConfigurationException | IOException | SAXException e)
			{
				e.printStackTrace();
				System.exit(4);
				return;
			}
			final long bpmnProcessParsingEndTime = System.nanoTime();
			final long bpmnProcessParsingTime = bpmnProcessParsingEndTime - bpmnProcessParsingStartTime;
			MyOwnLogger.append("Original BPMN process parsed in " + Utils.nanoSecToReadable(bpmnProcessParsingTime) + ".\n");
			System.out.println("Original BPMN process parsed in " + Utils.nanoSecToReadable(bpmnProcessParsingTime) + ".\n");

			System.out.println("Parsing full CLTS...");
			MyOwnLogger.append("Parsing full CLTS...");
			final long fullCltsParsingStartTime = System.nanoTime();
			final AutParser fullCltsParser = new AutParser(new File(workingDirectory + File.separator + AUTX_FULL_CLTS), true);
			final AutGraph fullCLTS = fullCltsParser.parse();
			final long fullCltsParsingEndTime = System.nanoTime();
			final long fullCltsParsingTime = fullCltsParsingEndTime - fullCltsParsingStartTime;
			MyOwnLogger.append("Full CLTS parsed in " + Utils.nanoSecToReadable(fullCltsParsingTime) + ".\n");
			System.out.println("Full CLTS parsed in " + Utils.nanoSecToReadable(fullCltsParsingTime) + ".\n");

			System.out.println("Flattening CLTS...");
			MyOwnLogger.append("Flattening CLTS...");
			final long cltsFlatteningStartTime = System.nanoTime();
			final CLTSFlattener flattener = new CLTSFlattener(fullCLTS);
			flattener.flatten();
			final long cltsFlatteningEndTime = System.nanoTime();
			final long cltsFlatteningTime = cltsFlatteningEndTime - cltsFlatteningStartTime;
			MyOwnLogger.append("CLTS flattened in " + Utils.nanoSecToReadable(cltsFlatteningTime) + ".\n");
			System.out.println("CLTS flattened in " + Utils.nanoSecToReadable(cltsFlatteningTime) + ".\n");

			System.out.println("Converting CLTS to BPMN...");
			MyOwnLogger.append("Converting CLTS to BPMN...");
			final long cltsConversionStartTime = System.nanoTime();
			final CLTStoBPMN cltStoBPMN = new CLTStoBPMN(fullCLTS, bpmnParser.bpmnProcess().tasks());
			final Graph bpmnProcess = cltStoBPMN.convert();
			final long cltsConversionEndTime = System.nanoTime();
			final long cltsConversionTime = cltsConversionEndTime - cltsConversionStartTime;
			if (LOCAL_TESTING) System.out.println("Original BPMN process:\n\n" + bpmnProcess);
			MyOwnLogger.append("CLTS converted to BPMN in " + Utils.nanoSecToReadable(cltsConversionTime) + ".\n");
			System.out.println("CLTS converted to BPMN in " + Utils.nanoSecToReadable(cltsConversionTime) + ".\n");

			System.out.println("Folding BPMN process...");
			MyOwnLogger.append("Folding BPMN process...");
			final long bpmnFoldingStartTime = System.nanoTime();
			final BPMNFolder bpmnFolder = new BPMNFolder(bpmnProcess);
			final Graph foldedBpmn = bpmnFolder.fold();
			final long bpmnFoldingEndTime = System.nanoTime();
			final long bpmnFoldingTime = bpmnFoldingEndTime - bpmnFoldingStartTime;
			if (LOCAL_TESTING) System.out.println("Folded BPMN process:\n\n" + foldedBpmn);
			MyOwnLogger.append("BPMN process folded in " + Utils.nanoSecToReadable(bpmnFoldingTime) + ".\n");
			System.out.println("BPMN process folded in " + Utils.nanoSecToReadable(bpmnFoldingTime) + ".\n");

			System.out.println("Merging useless BPMN gateways...");
			MyOwnLogger.append("Merging useless BPMN gateways...");
			final long uselessGatewaysMergingStartTime = System.nanoTime();
			final GatewaysMerger merger = new GatewaysMerger(foldedBpmn);
			merger.mergeGateways();
			final long uselessGatewaysMergingEndTime = System.nanoTime();
			final long uselessGatewaysMergingTime = uselessGatewaysMergingEndTime - uselessGatewaysMergingStartTime;
			MyOwnLogger.append("Useless BPMN gateways merged in " + Utils.nanoSecToReadable(uselessGatewaysMergingTime) + ".\n");
			System.out.println("Useless BPMN gateways merged in " + Utils.nanoSecToReadable(uselessGatewaysMergingTime) + ".\n");

			System.out.println("Writing BPMN process to file...");
			MyOwnLogger.append("Writing BPMN process to file...");
			final long writingBPMNStartTime = System.nanoTime();
			final GraphToList graphToList = new GraphToList(foldedBpmn);
			graphToList.convert();
			final GraphicalGenerationWriter graphicalGenerationWriter = new GraphicalGenerationWriter(
					commandLineParser,
					graphToList.objectsList(),
					"colored"
			);
			graphicalGenerationWriter.write();
			final long writingBPMNEndTime = System.nanoTime();
			final long writingBPMNTime = writingBPMNEndTime - writingBPMNStartTime;
			MyOwnLogger.append("BPMN process written to file in " + Utils.nanoSecToReadable(writingBPMNTime) + ".\n");
			System.out.println("BPMN process written to file in " + Utils.nanoSecToReadable(writingBPMNTime) + ".\n");

			final long programEndTime = System.nanoTime();
			final long programTime = programEndTime - programStartTime;
			MyOwnLogger.append("The coloration process took " + Utils.nanoSecToReadable(programTime) + ".\n");
			System.out.println("The coloration process took " + Utils.nanoSecToReadable(programTime) + ".\n");
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
}