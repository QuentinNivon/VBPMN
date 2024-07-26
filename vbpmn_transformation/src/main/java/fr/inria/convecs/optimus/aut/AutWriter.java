package fr.inria.convecs.optimus.aut;

import fr.inria.convecs.optimus.nl_to_mc.CLTSBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

public class AutWriter
{
	private final AutGraph graph;
	private final File cltsFile;

	public AutWriter(final AutGraph graph,
					 final File file)
	{
		this.graph = graph;
		this.cltsFile = file;
	}

	public void write() throws FileNotFoundException
	{
		final PrintWriter printWriter = new PrintWriter(this.cltsFile);
		final HashMap<Integer, Integer> correspondences = new HashMap<>();
		final HashSet<AutEdge> edges = new HashSet<>();
		this.normalize(correspondences, edges, this.graph.startNode());
		this.write(printWriter, correspondences, edges);
		printWriter.close();
	}

	//Private methods

	/**
	 * This method is used to normalize the aut graph given as parameter
	 * of the constructor of this class.
	 * It might be useful in cases where the original graph has been
	 * truncated, and thus some nodes' labels are not compliant anymore
	 * with the AUT format.
	 *
	 * @param correspondences the correspondence between old labels and new labels (filled by this method)
	 * @param edges the edges of the graph
	 * @param currentNode the node on which the computation should be performed
	 */
	private void normalize(final HashMap<Integer, Integer> correspondences,
						   final HashSet<AutEdge> edges,
						   final AutNode currentNode)
	{
		if (correspondences.containsKey(currentNode.label()))
		{
			return;
		}

		correspondences.put(currentNode.label(), correspondences.size());

		for (AutEdge outgoingEdge : currentNode.outgoingEdges())
		{
			edges.add(outgoingEdge);
			this.normalize(correspondences, edges, outgoingEdge.targetNode());
		}
	}

	private void write(final PrintWriter printWriter,
					   final HashMap<Integer, Integer> correspondences,
					   final HashSet<AutEdge> edges)
	{
		//Header
		printWriter.print("des (0, ");
		printWriter.print(edges.size());
		printWriter.print(", ");
		printWriter.print(correspondences.size());
		printWriter.println(")");

		//Transitions
		for (AutEdge autEdge : edges)
		{
			printWriter.print("(");
			printWriter.print(correspondences.get(autEdge.sourceNode().label()));
			printWriter.print(", ");
			printWriter.print(
				CLTSBuilder.CONSIDER_FULL_PATH ?
				autEdge.label().replace(" !ACC", "").replace("\"", "") :
				autEdge.label()
			);
			printWriter.print(", ");
			printWriter.print(correspondences.get(autEdge.targetNode().label()));
			printWriter.println(")");
		}
	}
}
