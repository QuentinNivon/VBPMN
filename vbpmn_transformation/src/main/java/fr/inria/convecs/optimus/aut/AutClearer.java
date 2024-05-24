package fr.inria.convecs.optimus.aut;

import java.util.HashSet;

public class AutClearer
{
	private static final String DUMMY_LOOPY_TRANSITION = "DUMMY_LOOPY_LABEL";
	private static final String INTERNAL_TRANSITION = "i";
	private static final String ACCEPTING_TRANSITION = "!ACC";

	private AutClearer()
	{

	}

	public static AutGraph clear(final AutGraph autGraph)
	{
		final AutNode firstNode = AutClearer.findFirstUsefulNode(autGraph.startNode(), new HashSet<>());

		if (firstNode == null) throw new IllegalStateException("No interesting node found in the counter-example!");

		final AutGraph clearedGraph = new AutGraph(new AutNode(firstNode.label()));



		return clearedGraph;
	}

	//Private methods

	private static AutNode findFirstUsefulNode(final AutNode currentNode,
											   final HashSet<AutNode> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return null;
		}

		visitedNodes.add(currentNode);

		for (final AutEdge outgoingTransition : currentNode.outgoingEdges())
		{
			if (!outgoingTransition.label().equals(INTERNAL_TRANSITION)
				&& !outgoingTransition.label().endsWith(ACCEPTING_TRANSITION)
				&& !outgoingTransition.label().equals(DUMMY_LOOPY_TRANSITION))
			{
				//We found a useful transition: return the current node
				return currentNode;
			}
		}

		for (final AutEdge outgoingTransition : currentNode.outgoingEdges())
		{
			final AutNode usefulNode = AutClearer.findFirstUsefulNode(outgoingTransition.targetNode(), visitedNodes);

			if (usefulNode != null)
			{
				return usefulNode;
			}
		}

		return null;
	}
}
