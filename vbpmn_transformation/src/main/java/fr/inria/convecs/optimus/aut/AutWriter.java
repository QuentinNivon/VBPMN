package fr.inria.convecs.optimus.aut;

import fr.inria.convecs.optimus.nl_to_mc.CLTSBuilderV2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

public class AutWriter
{
	private final AutGraph graph;
	private final File cltsFile;
	private final boolean enhance;    //Controls whether the output file should be in AUT or AUTX (extended AUT) format

	public AutWriter(final AutGraph graph,
					 final File file)
	{
		this.graph = graph;
		this.cltsFile = file;
		this.enhance = false;
	}

	public AutWriter(final AutGraph graph,
					 final File file,
					 final boolean enhance)
	{
		this.graph = graph;
		this.cltsFile = file;
		this.enhance = enhance;
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
	 * @param edges           the edges of the graph
	 * @param currentNode     the node on which the computation should be performed
	 */
	private void normalize(final HashMap<Integer, Integer> correspondences,
						   final HashSet<AutEdge> edges,
						   final AutState currentNode)
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
			if (autEdge.label().contains("DUMMY")) continue; //Do not rewrite DUMMY_LOOPY_LABELS

			printWriter.print("(");
			printWriter.print(correspondences.get(autEdge.sourceNode().label()));

			if (this.enhance
				&& autEdge.sourceNode().getStateType() != null)
			{
				printWriter.print(":N:");
				printWriter.print(autEdge.sourceNode().getStateType().getValue());
			}

			printWriter.print(", ");
			printWriter.print(
				CLTSBuilderV2.CONSIDER_FULL_PATH ?
				autEdge.label().replace(" !ACC", "").replace("\"", "") :
				autEdge.label()
			);

			if (this.enhance)
			{
				printWriter.print(":");
				printWriter.print(autEdge.getColor().getValue());
			}

			printWriter.print(", ");
			printWriter.print(correspondences.get(autEdge.targetNode().label()));
			printWriter.println(")");
		}
	}
}