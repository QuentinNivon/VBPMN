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
		final AutState firstNode = AutClearer.findFirstUsefulNode(autGraph.startNode(), new HashSet<>());

		if (firstNode == null) throw new IllegalStateException("No interesting node found in the counter-example!");

		final AutGraph clearedGraph = new AutGraph(new AutState(firstNode.label()));



		return clearedGraph;
	}

	//Private methods

	private static AutState findFirstUsefulNode(final AutState currentNode,
												final HashSet<AutState> visitedNodes)
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
			final AutState usefulNode = AutClearer.findFirstUsefulNode(outgoingTransition.targetNode(), visitedNodes);

			if (usefulNode != null)
			{
				return usefulNode;
			}
		}

		return null;
	}
}
