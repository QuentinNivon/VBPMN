package fr.inria.convecs.optimus.nl_to_mc;

import fr.inria.convecs.optimus.aut.AutEdge;
import fr.inria.convecs.optimus.aut.AutGraph;
import fr.inria.convecs.optimus.aut.AutNode;
import jdk.internal.net.http.common.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;

public class Aut2Force3DGraph
{
	private static final int GROUP = 1;
	private static final String NODES_KEYWORD = "nodes";
	private static final String ID_KEYWORD = "id";
	private static final String GROUP_KEYWORD = "group";
	private static final String LINKS_KEYWORD = "links";
	private static final String SOURCE_KEYWORD = "source";
	private static final String TARGET_KEYWORD = "target";
	private static final String VALUE_KEYWORD = "value";
	private final File forceGraphFile;
	private final AutGraph autGraph;

	public Aut2Force3DGraph(final File forceGraphFile,
							final AutGraph autGraph)
	{
		this.forceGraphFile = forceGraphFile;
		this.autGraph = autGraph;
	}

	public void generateForce3DGraph() throws FileNotFoundException
	{
		final PrintWriter printWriter = new PrintWriter(this.forceGraphFile);
		final Pair<HashSet<AutNode>, HashSet<AutEdge>> nodesAndEdges = this.autGraph.nodesAndEdges();

		printWriter.println("{");
		printWriter.print("	 \"");
		printWriter.print(NODES_KEYWORD);
		printWriter.print("\": [");

		int i = 0;

		for (AutNode autNode : nodesAndEdges.first)
		{
			printWriter.print("	   {\"");
			printWriter.print(ID_KEYWORD);
			printWriter.print("\": \"");
			printWriter.print(autNode.label());
			printWriter.print("\", \"");
			printWriter.print(GROUP_KEYWORD);
			printWriter.print("\": ");
			printWriter.print(GROUP);
			printWriter.print("}");

			if (i != nodesAndEdges.first.size() - 1)
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

		for (AutEdge autEdge : nodesAndEdges.second)
		{
			printWriter.print("	   {\"");
			printWriter.print(SOURCE_KEYWORD);
			printWriter.print("\": \"");
			printWriter.print(autEdge.sourceNode().label());
			printWriter.print("\", \"");
			printWriter.print(TARGET_KEYWORD);
			printWriter.print("\": ");
			printWriter.print(autEdge.targetNode().label());
			printWriter.print("\", \"");
			printWriter.print(VALUE_KEYWORD);
			printWriter.print("\": ");
			printWriter.print(autEdge.label());
			printWriter.print("}");

			if (i != nodesAndEdges.first.size() - 1)
			{
				printWriter.print(",");
			}

			printWriter.println();
			i++;
		}

		printWriter.println("  ]");
		printWriter.println("}");
	}
}
