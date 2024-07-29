package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutEdge;
import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutNode;
import fr.inria.convecs.optimus.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;

public class Aut2Force3DGraph
{
	private static final int DEFAULT_GROUP = 1;
	private static final int INITIAL_GROUP = 2;
	private static final int TERMINAL_GROUP = 3;
	private static final double CURVATURE_VALUE = 1;
	private static final String DEFAULT_COLOR = "#0000FF";
	private static final String INITIAL_COLOR = "#FFFF00";
	private static final String TERMINAL_COLOR = "#FF0000";
	private static final String NODES_KEYWORD = "nodes";
	private static final String ID_KEYWORD = "id";
	private static final String GROUP_KEYWORD = "group";
	private static final String COLOR_KEYWORD = "color";
	private static final String LINKS_KEYWORD = "links";
	private static final String SOURCE_KEYWORD = "source";
	private static final String TARGET_KEYWORD = "target";
	private static final String VALUE_KEYWORD = "value";
	private static final String CURVATURE_KEYWORD = "curvature";
	private final File forceGraphFile;
	private final AutGraph autGraph;

	public Aut2Force3DGraph(final File forceGraphFile,
							final AutGraph autGraph)
	{
		this.forceGraphFile = forceGraphFile;
		this.autGraph = autGraph;
	}

	public void generateForce3DGraphFile() throws FileNotFoundException
	{
		final PrintWriter printWriter = new PrintWriter(this.forceGraphFile);
		final Pair<HashSet<AutNode>, HashSet<AutEdge>> nodesAndEdges = this.autGraph.nodesAndEdges();

		printWriter.println("{");
		printWriter.print("  \"");
		printWriter.print(NODES_KEYWORD);
		printWriter.println("\": [");

		int i = 0;

		for (AutNode autNode : nodesAndEdges.getFirst())
		{
			printWriter.print("    {\"");
			printWriter.print(ID_KEYWORD);
			printWriter.print("\": \"");
			printWriter.print(autNode.label());
			printWriter.print("\", \"");
			printWriter.print(COLOR_KEYWORD);
			printWriter.print("\": \"");
			printWriter.print(autNode.incomingEdges().isEmpty() ? INITIAL_COLOR : (autNode.outgoingEdges().isEmpty() ? TERMINAL_COLOR : DEFAULT_COLOR));
			printWriter.print("\"}");

			if (i != nodesAndEdges.getFirst().size() - 1)
			{
				printWriter.print(",");
			}

			printWriter.println();
			i++;
		}

		printWriter.println("  ],");
		printWriter.print("  \"");
		printWriter.print(LINKS_KEYWORD);
		printWriter.println("\": [");

		i = 0;

		for (AutEdge autEdge : nodesAndEdges.getSecond())
		{
			printWriter.print("    {\"");
			printWriter.print(SOURCE_KEYWORD);
			printWriter.print("\": \"");
			printWriter.print(autEdge.sourceNode().label());
			printWriter.print("\", \"");
			printWriter.print(TARGET_KEYWORD);
			printWriter.print("\": \"");
			printWriter.print(autEdge.targetNode().label());
			printWriter.print("\", \"");

			if (autEdge.isCurved())
			{
				printWriter.print(CURVATURE_KEYWORD);
				printWriter.print("\": ");
				printWriter.print(CURVATURE_VALUE);
				printWriter.print(", \"");
			}

			printWriter.print(VALUE_KEYWORD);
			printWriter.print("\": \"");
			printWriter.print(
				CLTSBuilder.CONSIDER_FULL_PATH ?
				autEdge.label().replace(" !ACC", "").replace("\"", "") :
				autEdge.label()
			);
			printWriter.print("\"}");

			if (i != nodesAndEdges.getSecond().size() - 1)
			{
				printWriter.print(",");
			}

			printWriter.println();
			i++;
		}

		printWriter.println("  ]");
		printWriter.println("}");
		printWriter.close();
	}
}
